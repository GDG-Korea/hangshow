import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.xml.DOMConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.*;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.gdata.client.authn.oauth.*;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.*;
import com.google.gdata.data.batch.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.*;

public class ServerMain {
	public static String default_db_url = "jdbc:mysql://localhost";
	public static String default_db_user = "transdb";
	public static String default_db_passwd = "transdb!@#$";
	static String _USERNAME = "";
	static String _PASSWORD = "";
	final static String SPREADSHEET_NAME = "Hangshow-Feed";
	final static String SPREADSHEET_SHEET_MAIN = "Main";
	final static String SPREADSHEET_SHEET_REGID = "RegIdList";
	final static String SPREADSHEET_READY = "Ready";

	final static String FIELD_MAIN_YOUTUBE = "youtubecontentid";
	final static String FIELD_MAIN_TITLE = "title";
	final static String FIELD_MAIN_DESC = "description";
	final static String FIELD_MAIN_TAGS = "tags";
	final static String FIELD_MAIN_OWNER = "ownerid";
	final static String FIELD_MAIN_CONFERENCE = "conferencename";
	final static String FIELD_MAIN_START = "timestart";

	final static String FIELD_REGID_REGID = "regid";
	final static String FIELD_REGID_USERID = "userid";

	final static String QEURYSTRING_YOUTUBE = "youtubecontentid";
	final static String QEURYSTRING_TITLE = "title";
	final static String QEURYSTRING_DESC = "description";
	final static String QEURYSTRING_TAGS = "tags";
	final static String QEURYSTRING_OWNER = "ownerid";
	final static String QEURYSTRING_CONFERENCE = "conferencename";
	final static String QEURYSTRING_START = "timestart";

	final static String GCM_API_KEY = "AIzaSyAcsMfsSRZIf4EA-UxPQhvBiyYjDO1Cvgs";
	final static String GCM_REG_ID = "APA91bFnEeAN_VnAuOOeTQ3BRnqGr2w53ETt2YnosrvmGmtDgoWnPDdp6MmfXZ9eFyMqgMO_ikpk4GBCdQ3YwHLSkr3eftXKUCzlFc4pHla93okwlC5-ro98ULQcxOi1kg1TqetHvcfFsArz2zvmbf9LFeUZC_S4bA";

	static SpreadsheetEntry _mainSpreadSheet = null;
	static SpreadsheetService _service = null;
	static WorksheetEntry _mainWorkSheet = null;
	static WorksheetEntry _regIdSheet = null;
	static ServerMain _mainServer = new ServerMain();
//	static Logger _log = LoggerFactory.getLogger(ServerMain.class);
	static Logger _log = null;

	final static String SAMPLE_INSERT_URI = "http://localhost:4567/onair/insert?youtube-content-id=1111&title=행쇼테스트&description=행쇼테스트입니다&tags=test&owner-id=11111&conference-name=devfest2013&time-start=10:00:00";

//	static void _log.info(String str) {
//		_log.info(str);
//	}

	public static void main(String[] args) throws AuthenticationException,
			MalformedURLException, IOException, ServiceException {

		
		DOMConfigurator.configure("./conf/log4j.xml");
		_log = LoggerFactory.getLogger(ServerMain.class);
		
		/*
		 * String jsSample =
		 * "{ \"name1\": \"50\", \"name2\": \"2\", \"name3\": \"3\"}";
		 * JSONObject jsonobj = (JSONObject)JSONValue.parse(jsSample);
		 * System.out
		 * .println("name1:"+jsonobj.get("name1")+",name2:"+jsonobj.get
		 * ("name2")+",name3:"+jsonobj.get("name3"));
		 */

		_mainServer.init();
		_mainServer.initSpart();

		/*
		 * // Create a local representation of the new worksheet. WorksheetEntry
		 * worksheet = new WorksheetEntry(); worksheet.setTitle(new
		 * PlainTextConstruct("New Worksheet")); worksheet.setColCount(10);
		 * worksheet.setRowCount(20);
		 * 
		 * // Send the local representation of the worksheet to the API for //
		 * creation. The URL to use here is the worksheet feed URL of our //
		 * spreadsheet. URL worksheetFeedUrl =
		 * spreadsheet.getWorksheetFeedUrl(); service.insert(worksheetFeedUrl,
		 * worksheet);
		 */

		get(new Route("/hello") {
			@Override
			public Object handle(Request request, Response response) {
				return "Hello World!";
			}
		});

		post(new Route("/hello") {
			@Override
			public Object handle(Request request, Response response) {
				return "Hello World: " + request.body();
			}
		});

		/*
		 * get(new Route("/private") {
		 * 
		 * @Override public Object handle(Request request, Response response) {
		 * response.status(401); return "Go Away!!!"; } });
		 * 
		 * get(new Route("/users/:name") {
		 * 
		 * @Override public Object handle(Request request, Response response) {
		 * return "Selected user: " + request.params(":name"); } });
		 * 
		 * get(new Route("/news/:section") {
		 * 
		 * @Override public Object handle(Request request, Response response) {
		 * response.type("text/xml"); return
		 * "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>" +
		 * request.params("section") +
		 * ":"+request.queryParams("hams")+"</news>"; } });
		 * 
		 * get(new Route("/protected") {
		 * 
		 * @Override public Object handle(Request request, Response response) {
		 * halt(403, "I don't think so!!!"); return null; } });
		 * 
		 * get(new Route("/redirect") {
		 * 
		 * @Override public Object handle(Request request, Response response) {
		 * response.redirect("/news/world"); return null; } });
		 * 
		 * get(new Route("/") {
		 * 
		 * @Override public Object handle(Request request, Response response) {
		 * return "root"; } });
		 */

		_log.info("!!!!START!!!!");

	}

	/*
	 * public static void initDB(){ try { // The newInstance() call is a work
	 * around for some // broken Java implementations
	 * 
	 * Class.forName("com.mysql.jdbc.Driver").newInstance();
	 * 
	 * Properties props = new Properties(); props.put("databases.names",
	 * "base"); props.put("database.base.driver", DatabasePool.MYSQL_DRIVER);
	 * props.put("database.base.url", default_db_url);
	 * props.put("database.base.user", default_db_user);
	 * props.put("database.base.password", default_db_passwd);
	 * props.put("database.base.min", "0"); props.put("database.base.max", "5");
	 * props.put("database.base.alias", "core");
	 * 
	 * DatabasePool.getInstance().init();
	 * DatabasePool.getInstance().start(props);
	 * 
	 * } catch (Exception ex) { ex.printStackTrace();
	 * logDebugln("mysql's jdbc driver intializing fail!"); System.exit(1); } }
	 * 
	 * public boolean connectDB(){
	 * 
	 * try { closeDB(); _conn = DatabasePool.get("core"); // _conn =
	 * DriverManager.getConnection(default_db_url,default_db_user,
	 * default_db_passwd); _stmt = _conn.createStatement(); } catch
	 * (SQLException ex) { // handle any errors
	 * logDebugln("DB connection is Fail - " + default_db_url);
	 * logDebugln("SQLException: " + ex.getMessage()); logDebugln("SQLState: " +
	 * ex.getSQLState()); logDebugln("VendorError: " + ex.getErrorCode());
	 * return false; } return true; }
	 */
	public void printAllRows(WorksheetEntry worksheet) throws IOException,
			ServiceException {
		// Fetch the list feed of the worksheet.
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = _service.getFeed(listFeedUrl, ListFeed.class);

		_log.info("\t[" + worksheet.getTitle().getPlainText()
				+ "] worksheet size:" + listFeed.getEntries().size() + "\n");

		// Create a local representation of the new row.

		String strLog = ""; 
		// Iterate through each row, printing its cell values.
		for (ListEntry row : listFeed.getEntries()) {
			// Print the first column's cell value
			// _log.info("\t\t["+row.getTitle().getPlainText() +
			// "]\t");
			// Iterate over the remaining columns, and print each cell value
			strLog = "";
			for (String tag : row.getCustomElements().getTags()) {
				strLog += "\t" + tag + ": "+ row.getCustomElements().getValue(tag);
			}
			_log.info(strLog);
		}
	}

	public String printAllJson(WorksheetEntry worksheet) throws IOException,
			ServiceException {
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = _service.getFeed(listFeedUrl, ListFeed.class);

		JSONArray jsList = new JSONArray();

		_log.info("[printAllJson] " + "worksheet size:"
				+ listFeed.getEntries().size() + "\n");

		String jsResult = "";
		for (ListEntry row : listFeed.getEntries()) {
			JSONObject jsObj = new JSONObject();
			for (String tag : row.getCustomElements().getTags()) {
				jsObj.put(tag, row.getCustomElements().getValue(tag));
			}
			jsList.add(jsObj);
		}
		jsResult = jsList.toJSONString();
		_log.info("Json:" + jsResult);
		return jsResult;
	}

	public String checkValid(Object obj) {
		if (obj instanceof String) {
			return (String) obj;
		}
		return new String("");
	}

	public void insertRegIdRow(Request req) {
		WorksheetEntry worksheet = _regIdSheet;
		try {
			ListEntry row = new ListEntry();
			row.getCustomElements().setValueLocal(FIELD_REGID_REGID,
					checkValid(req.queryParams(FIELD_REGID_REGID)));
			row.getCustomElements().setValueLocal(FIELD_REGID_USERID,
					checkValid(req.queryParams(FIELD_REGID_USERID)));

			URL listFeedUrl = worksheet.getListFeedUrl();
			row = _service.insert(listFeedUrl, row);
		} catch (Exception e) {
			e.printStackTrace();
			_log.info("insertRegIdRow error:" + e.toString());
			_log.info("Error: _RegIdSheet");
		}
	}

	public void insertMainRow(Request req) throws IOException, ServiceException {
		WorksheetEntry worksheet = _mainWorkSheet;
		try {
			ListEntry row = new ListEntry();
			row.getCustomElements().setValueLocal(FIELD_MAIN_YOUTUBE,
					checkValid(req.queryParams(FIELD_MAIN_YOUTUBE)));
			row.getCustomElements().setValueLocal(FIELD_MAIN_TITLE,
					checkValid(req.queryParams(FIELD_MAIN_TITLE)));
			row.getCustomElements().setValueLocal(FIELD_MAIN_DESC,
					checkValid(req.queryParams(FIELD_MAIN_DESC)));
			row.getCustomElements().setValueLocal(FIELD_MAIN_TAGS,
					checkValid(req.queryParams(FIELD_MAIN_TAGS)));
			row.getCustomElements().setValueLocal(FIELD_MAIN_OWNER,
					checkValid(req.queryParams(FIELD_MAIN_OWNER)));
			row.getCustomElements().setValueLocal(FIELD_MAIN_CONFERENCE,
					checkValid(req.queryParams(FIELD_MAIN_CONFERENCE)));
			row.getCustomElements().setValueLocal(FIELD_MAIN_START,
					checkValid(req.queryParams(FIELD_MAIN_START)));

			URL listFeedUrl = worksheet.getListFeedUrl();
			row = _service.insert(listFeedUrl, row);
		} catch (Exception e) {
			e.printStackTrace();
			_log.info("insertMainRow error:" + e.toString());
			_log.info("Error: _mainWorkSheet");
		}
	}

	public void loadConf() {
		try {
			Properties p = new Properties();

			// ini 파일 읽기
			p.load(new FileInputStream("./conf/conf.ini"));

			// Key 값 읽기
			_USERNAME = p.getProperty("google-id").trim();
			_PASSWORD = p.getProperty("password").trim();

			_log.info(p.toString());
			//p.list(System.out);

			// ini 파일 쓰기
			// p.store( new FileOutputStream("conf.ini"), "done.");
		} catch (Exception e) {
			_log.info(e.toString());
		}
	}

	public String getHTML(String urlToRead) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(	new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
			_log.info("getHTML error:" + e.toString());
		}
		return result;
	}

	public List<String> getRegidList() throws IOException, ServiceException{
		ArrayList<String> regidlist = new ArrayList<String>(); 
		WorksheetEntry worksheet = _regIdSheet;
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = _service.getFeed(listFeedUrl, ListFeed.class);

		for (ListEntry row : listFeed.getEntries()) {
			for (String tag : row.getCustomElements().getTags()) {
				if(tag.equals(FIELD_REGID_REGID))
					regidlist.add(row.getCustomElements().getValue(tag));
			}
		}
		
		return regidlist;
	}
	
	
	public void sendGCM(Request request) throws IOException, ServiceException {
		Sender sender = new Sender(GCM_API_KEY); // 구글 코드에서 발급받은 서버 키
		Message msg = new Message.Builder()
				.addData(FIELD_MAIN_YOUTUBE, request.queryParams(FIELD_MAIN_YOUTUBE)) // 데이터 추가
				.addData(FIELD_MAIN_TITLE, request.queryParams(FIELD_MAIN_TITLE)) // 데이터 추가
				.build();

		// 푸시 전송. 파라미터는 푸시 내용, 보낼 단말의 id, 마지막은 잘 모르겠음

		MulticastResult results = sender.send(msg, getRegidList(), 5);

		_log.info("getHTML:"  + getHTML("http://183.111.25.79/onair/list"));

		// 결과 처리
		for(Result result : results.getResults()){
			if (result.getMessageId() != null) {
				String regid = result.getCanonicalRegistrationId();
				_log.info("\tSuccess: regid - " + regid);
			} else {
				String regid = result.getCanonicalRegistrationId();
				String error = result.getErrorCodeName(); // 에러 내용 받기
				_log.info("\tError: regid - " + regid+", error - "+error);
				if (Constants.ERROR_INTERNAL_SERVER_ERROR.equals(error)) {
					// 구글 푸시 서버 에러
				} else {
				}
			}
		}
		
	}

	
	
	public void init() throws IOException, ServiceException {
		_log.info("Step 0: load conf");
		_mainServer.loadConf();

		_log.info("Step 1: Login");
		_service = new SpreadsheetService("MySpreadsheetIntegration-v1");
		_service.setUserCredentials(_USERNAME, _PASSWORD);
		_service.setProtocolVersion(SpreadsheetService.Versions.V3);

		URL SPREADSHEET_FEED_URL = new URL(
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full");

		_log.info("Step 2: SpreadSheet Entries");
		SpreadsheetFeed feed = _service.getFeed(SPREADSHEET_FEED_URL,
				SpreadsheetFeed.class);
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();

		_log.info("Step 3: Show SpreadSheet's Name");
		// Iterate through all of the spreadsheets returned
		for (SpreadsheetEntry spreadsheet : spreadsheets) {
			// Print the title of this spreadsheet to the screen
			_log.info(spreadsheet.getTitle().getPlainText());
			if (spreadsheet.getTitle().getPlainText().equals(SPREADSHEET_NAME)) {
				_mainSpreadSheet = spreadsheet;
			}
		}

		_log.info("Step 4: Select Main Sheet");
		// app's needs.
		if (_mainSpreadSheet == null) {
			_log.info("Cannot find Main Sheet");
			int idxSheet = 0;
			_mainSpreadSheet = spreadsheets.get(idxSheet);
			_log.info(idxSheet + " of the sheet index -  title:"
					+ _mainSpreadSheet.getTitle().getPlainText());
		} else {
		}

		// Make a request to the API to fetch information about all
		// worksheets in the spreadsheet.
		List<WorksheetEntry> worksheets = _mainSpreadSheet.getWorksheets();

		// Iterate through each worksheet in the spreadsheet.
		for (WorksheetEntry worksheet : worksheets) {
			// Get the worksheet's title, row count, and column count.
			String title = worksheet.getTitle().getPlainText();
			int rowCount = worksheet.getRowCount();
			int colCount = worksheet.getColCount();

			// Print the fetched information to the screen for this worksheet.
			if (title.equals(SPREADSHEET_SHEET_MAIN)) {
				_mainWorkSheet = worksheet;
				_log.info("\t[Main WorkSheet]: " + title + " -  rows:"
						+ rowCount + " cols: " + colCount);
			} else if (title.equals(SPREADSHEET_SHEET_REGID)) {
				_regIdSheet = worksheet;
				_log.info("\t[RegId WorkSheet]: " + title
						+ " -  rows:" + rowCount + " cols: " + colCount);

			} else {
				_log.info("\t[Sub WorkSheet]: " + title + " -  rows:"
						+ rowCount + " cols: " + colCount);
			}
		}

		_mainServer.printAllRows(_mainWorkSheet);
		_mainServer.printAllRows(_regIdSheet);
	}

	void initSpart() {
		setPort(80);
		get(new Route("/onair/insert") {
			@Override
			public Object handle(Request request, Response response) {
				_log.info("GET "+request.url()+",Query:"+request.queryString());
				response.header("Access-Control-Allow-Origin", "*");
				response.type("application/json");
				try {
					if (checkValid(request.queryParams(FIELD_MAIN_YOUTUBE))
							.equals("")) {
						return "{\"status\":\"NOT_EXIST_YOUTUBEID\"}";
					}
					_mainServer.insertMainRow(request);
					_mainServer.printAllRows(_mainServer._mainWorkSheet);
					sendGCM(request);					
				} catch (IOException e) {
					e.printStackTrace();
					_log.info(e.toString());
				} catch (ServiceException e) {
					e.printStackTrace();
					_log.info(e.toString());
				}
				return "{ \"" + FIELD_MAIN_YOUTUBE + "\" : \""
						+ request.queryParams(FIELD_MAIN_YOUTUBE) + "\"}";
			}
		});

		get(new Route("/onair/regid") {
			@Override
			public Object handle(Request request, Response response) {
				_log.info("GET "+request.url()+",Query:"+request.queryString());
				response.header("Access-Control-Allow-Origin", "*");
				response.type("application/json");
				String jsRs = "{\"status\":\"success\"}";

				try {
					if (checkValid(request.queryParams(FIELD_REGID_REGID))
							.equals("")) {
						jsRs = "{\"status\":\"NOT_EXIST_REGID\"}";
						return jsRs;
					}
					insertRegIdRow(request);
					_mainServer.printAllRows(_mainServer._regIdSheet);
				} catch (IOException e) {
					e.printStackTrace();
					_log.info(e.toString());
				} catch (ServiceException e) {
					e.printStackTrace();
					_log.info(e.toString());
				}
				return jsRs;
			}
		});

		get(new Route("/onair/list") {
			@Override
			public Object handle(Request request, Response response) {
				_log.info("GET "+request.url()+",Query:"+request.queryString());
				response.header("Access-Control-Allow-Origin", "*");
				response.type("application/json");
				String jsRs = "{}";
				try {
					jsRs = _mainServer.printAllJson(_mainServer._mainWorkSheet);
				} catch (IOException e) {
					e.printStackTrace();
					_log.info(e.toString());
				} catch (ServiceException e) {
					e.printStackTrace();
					_log.info(e.toString());
				}
				return jsRs;
			}
		});

	}

}