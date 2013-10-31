import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

/**
 * 여러개의 커넥션풀들을 가지고 있는 데이터베이스 커넥션 풀.
 * Alias를 지정해두었을 경우 커넥션풀 이름 대신 alias를 사용할 수도 있다.
 * 참고로 SQLite를 사용할 경우  드라이버는 http://www.zentus.com/sqlitejdbc/ 에서 다운로드 받고 설명을 참조하기 바란다.
 * 하지만 꼭 위의 사이트에 있는 드라이버를 사용해야 하는 것은 아니다.
 * 
 */
public class DatabasePool
{
	private static DatabasePool THIS = null;

	private Map<String,GenericObjectPool> unmodifiablePoolMap = null;
	private Map<String,GenericObjectPool> poolMap = null;
	private Map<String,DataSource> unmodifiableSourceMap = null;
	private Map<String,DataSource> sourceMap = null;

	private String defaultName = null;
	private static String driver = null;

	public static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
	
	public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";

	public static final String SQLITE_DRIVER = "org.sqlite.JDBC";
	
	private final Logger log = Logger.getLogger("service");

	/**
	 * DatabasePool 객체를 생성한다.
	 */
	private DatabasePool()
	{
	}

	/**
	 * 기본 pool로부터 데이터베이스 연결을 하나 가져온다.
	 */
	public static Connection get() throws SQLException, IllegalArgumentException
	{
		return getInstance().getConnection();
	}

	/**
	 * 주어진 pool로부터 데이터베이스 연결을 하나 가져온다.
	 */
	public static Connection get( String name ) throws SQLException, IllegalArgumentException
	{
		return getInstance().getConnection(name);
	}

	
	public static String getDriver()
	{
		return driver;
	}
	/**
	 * DatabasePool 객체를 가져온다.
	 */
	public static DatabasePool getInstance()
	{
		if(THIS == null)
			THIS = new DatabasePool();
		
		return THIS;
	}

	/**
	 * 기본 이름을 가지는 데이터베이스 연결을 풀로부터 가져온다.
	 */
	public Connection getConnection() throws SQLException, IllegalArgumentException
	{
		return getConnection(defaultName);
	}

	/**
	 * 주어진 이름을 가지는 데이터베이스 연결을 풀로부터 가져온다.
	 */
	public Connection getConnection( String name ) throws SQLException, IllegalArgumentException
	{
		if( unmodifiableSourceMap==null )
			throw new IllegalStateException("Service doesn't start yet");

		DataSource src = unmodifiableSourceMap.get(name);
	
		if( src == null )
			throw new IllegalArgumentException("PoolName("+ name +") not found. PoolNames are " + unmodifiableSourceMap.keySet());

		return src.getConnection();
	}

    /**
     * 주어진 이름을 가지는 DataSource 객체를 반환한다.
     * @param name String
     * @return DataSource
     * @throws SQLException
     */
    public DataSource getDataSource(String name) throws SQLException
    {	
		if( unmodifiableSourceMap==null )
			throw new IllegalStateException("Service doesn't start yet");
		
		DataSource src = unmodifiableSourceMap.get(name);
		if( src==null )
			throw new IllegalArgumentException("PoolName("+ name +") not found. PoolNames are " + unmodifiableSourceMap.keySet());
		
		return src;
    }

	/**
	 * 이 데이터베이스 풀의 기본이름을 지정한다.
	 */
	public void setDefault( String name )
	{
		this.defaultName = name;
	}

	/**
	 * 이 데이터베이스 풀의 기본이름을 가져온다. 만약 기본이름이
	 * 지정되어있지 않다면, 최초로 설치된 풀의 이름을 가져오게 된다.
	 */
	public String getDefault()
	{
		return this.defaultName;
	}

	public void init() throws IOException
	{
		poolMap = new HashMap<String,GenericObjectPool>();
		unmodifiablePoolMap = new HashMap<String,GenericObjectPool>();
		sourceMap = new HashMap<String,DataSource>();
		unmodifiableSourceMap = new HashMap<String,DataSource>();
	}

	/**
	 * 풀링 서비스를 시작한다.
	 * 데이터베이스에 대한 설정은 아래와 같이 Properties 에 설정을 담아 파라미터로 입력한다.	
	 * 	Properties props = new Properties();
	 *	props.put("databases.names", "base,stat");
	 *	props.put("database.base.driver", DatabasePool.MYSQL_DRIVER);
	 *	props.put("database.base.url", "jdbc:mysql://127.0.0.1:3306/db명?characterEncoding=utf-8&amp;autoReconnect=true");
	 *	//props.put("database.base.url", "jdbc:sqlite://127.0.0.1:3306/db명?characterEncoding=utf-8&amp;autoReconnect=true");
	 *	props.put("database.base.user", "user");
	 *	props.put("database.base.password", "password");
	 *	props.put("database.base.min", "5");
	 *	props.put("database.base.max", "20");
	 *	props.put("database.base.alias", "core");
     *
	 * @param prop Properties 데이터베이스 설정.
	 * @throws IOException, ClassNotFoundException 
	 */
	public void start(Properties prop) throws IOException, ClassNotFoundException
	{
		install(prop);
	}

	private void install(Properties prop) throws IOException, ClassNotFoundException
	{
		String[] names = prop.getProperty("databases.names").split("\\,");
		for(String name : names)
		{
			String prefix = "database." + name;
			driver = prop.getProperty(prefix + ".driver");
			String url = prop.getProperty(prefix + ".url");
			String user = prop.getProperty(prefix + ".user");
			String pass = prop.getProperty(prefix + ".password");
			String[] alias = prop.getProperty(prefix + ".alias").split("\\,");
			int min = Integer.parseInt(
				prop.getProperty("database." + name + ".min", "3"));
			int max = Integer.parseInt(
				prop.getProperty("database." + name + ".max", "10"));

			log.debug( "Jdbc Driver: " + driver );
			log.debug( "Jdbc Url   : " + url );
			log.debug( "Jdbc User  : " + user );

			if( driver==null || url==null )
			{
				log.warn( "Database(" + name + ") not found. It will be skipped." );
				continue;
			}

			try
			{
				Class.forName(driver);
			}
			catch(Exception e)
			{
				throw new ClassNotFoundException("DatabaseConnectionPool didn't prepare. ClassNotFound: " + driver);
			}

			GenericObjectPool pool = new GenericObjectPool(null);
			pool.setMaxActive(max);
			pool.setMaxIdle(min);
			pool.setMinIdle(min);
			//pool.setWhenExhaustedAction( GenericObjectPool.WHEN_EXHAUSTED_FAIL );
			pool.setWhenExhaustedAction( GenericKeyedObjectPool.WHEN_EXHAUSTED_BLOCK );
			
			// Pool에서 Connection을 받아와 DB에 Query문을 날리기 전에   해당 Connection이 Active한지 Check하고     Active하지 않으면 해당 Connection을 다시 생성합니다
			// 아래는 주석처리... IDLE일 경우.. MySQL에 커넥션 관련 에러가 나서 주석처리한다. yongyong
			pool.setTestOnBorrow(true);
			pool.setTestOnReturn(true);
			pool.setTestWhileIdle(true);
	
			ConnectionFactory cf = new DriverManagerConnectionFactory(url, user, pass);
			PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, pool,
				null, null, false, true);
			PoolingDataSource src = new PoolingDataSource(pool);

			List<Connection> list = new ArrayList<Connection>(min);
			try
			{
				for(int i=0; i<min; i++)
				{
					log.info( "Database(" + name + ") warmed up... " + (i+1) );
					list.add(src.getConnection());
				}
				
				for(Connection con : list)
					con.close();
			}
			catch( SQLException e )
			{
				log.fatal( "Database(" + name + ") warming up failed.", e );
				System.exit(2);
			}

			poolMap.put(name, pool);
			sourceMap.put(name, src);

			// 최초로 적힌 Database이름이 Default name이 된다.
			if( defaultName==null )
				defaultName = name;

			log.info( "Database(" + name + ") was successfully installed into the pool." );

			log.info( "JNDI bind DatabasePool(" + name + ") service to 'jdbc/" + name + "'" );

			for(String nick : alias)
			{
				poolMap.put(nick, pool);
				sourceMap.put(nick, src);

				log.info( "JNDI bind DatabasePool(" + name + ") service to 'jdbc/" + nick + "'" );
			}
		}
		
		unmodifiablePoolMap = Collections.unmodifiableMap(poolMap);
		unmodifiableSourceMap = Collections.unmodifiableMap(sourceMap);
	}

	public void stop()
	{
		// 풀을 모두 닫고, 바이바이 한다.
		if( poolMap!=null )
		{
			for(String name : poolMap.keySet())
			{
				GenericObjectPool pool = poolMap.get(name);
				try
				{
					if(pool != null)
						pool.close();
					log.info( "Database(" + name + ") was successfully uninstalled from the pool." );
				}
				catch(IllegalStateException ignore)
				{
					
				}
				catch( Exception e )
				{
					log.error("ObjectPool close failed.", e);
				}
			}
			poolMap.clear();
			poolMap = null;
		}
		if( sourceMap!=null )
		{
			sourceMap.clear();
			sourceMap = null;
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException
	{
		Properties props = new Properties();
		
		props.put("databases.names", "base,stat");
		props.put("database.base.driver", DatabasePool.MYSQL_DRIVER);
	
		props.put("database.base.url", "jdbc:mysql://127.0.0.1:3306/nms?characterEncoding=utf-8&amp;autoReconnect=true");
		
		//props.put("database.base.url", "jdbc:sqlite://127.0.0.1:3306/nms?characterEncoding=utf-8&amp;autoReconnect=true");
		props.put("database.base.user", "user");
		props.put("database.base.password", "password");
		props.put("database.base.min", "5");
		props.put("database.base.max", "20");
		props.put("database.base.alias", "core");

		DatabasePool.getInstance().init();
		DatabasePool.getInstance().start(props);
		
		Connection conn = null;
		PreparedStatement ps = null;

		try
		{
			conn = DatabasePool.get("core");
			conn.setAutoCommit(false);
			String query = "INSERT INTO media_group_info(id, protocol, channel_name, broadcast_view, monitorable, enabled_onetime_url, onetime_url_server, onetime_url_id, enabled_sms, sms_phones, monitoring_period) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			ps = conn.prepareStatement(query);
			
//			ps.setString(1, group.getId());
//			ps.setString(2, group.getProtocol());
//			ps.setString(3, group.getChannel());
//			ps.setString(4, group.enabledBroadcastView() ? "ON" : "OFF");
//			ps.setString(5, group.isMonitorable() ? "ON" : "OFF");
//			ps.setString(6, group.enabledOneTimeURL() ? "ON" : "OFF");
//			ps.setString(7, group.getOneTimeURLServer());
//			ps.setString(8, group.getOneTimeId());
//			ps.setString(9, group.enabledSMS() ? "ON" : "OFF");
//			ps.setString(10, group.getSMSPhones());
//			ps.setLong(11, group.getMonitoringPeriod());
			
			ps.executeUpdate();
			
		}
		catch(SQLException ex)
		{
			//logger.severe("MediaGroupDAO - an error occurred while inserting a group into the database " + ex);

			try
			{
				DbUtils.rollback(conn);
			}
			catch (SQLException ignore)
			{
			}
			
			//throw ex;
		}
		finally
		{
			try
			{
				if(conn != null)
					conn.setAutoCommit(true);
			}
			catch (SQLException ignore)
			{
			} 
			finally
			{
				DbUtils.closeQuietly(conn, ps, null);
			}
		}

	}
}