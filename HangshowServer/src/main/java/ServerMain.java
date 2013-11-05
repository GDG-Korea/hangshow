import static spark.Spark.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import spark.*;

import com.google.gdata.client.authn.oauth.*;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.*;
import com.google.gdata.data.batch.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.*;

public class ServerMain {
	public static String default_db_url= "jdbc:mysql://localhost";
	public static String default_db_user= "transdb";
	public static String default_db_passwd="transdb!@#$";
	
	
	public static void main(String[] args) 	      
			throws AuthenticationException, MalformedURLException, IOException, ServiceException {
		
	    String _USERNAME = "gdg.hangshow@gmail.com";
	    String PASSWORD = "Hangsh0w";

		System.out.println("Step 1: Login");
	    SpreadsheetService service =
	        new SpreadsheetService("MySpreadsheetIntegration-v1");
	    service.setUserCredentials(_USERNAME, PASSWORD);		
	    service.setProtocolVersion(SpreadsheetService.Versions.V3);
	    
	    URL SPREADSHEET_FEED_URL = new URL(
	            "https://spreadsheets.google.com/feeds/spreadsheets/private/full");
	    
		System.out.println("Step 2");
	    SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
	    List<SpreadsheetEntry> spreadsheets = feed.getEntries();

		System.out.println("Step 3");
	    // Iterate through all of the spreadsheets returned
	    for (SpreadsheetEntry spreadsheet : spreadsheets) {
	      // Print the title of this spreadsheet to the screen
	      System.out.println(spreadsheet.getTitle().getPlainText());
	    }	    
		
	    // TODO: Choose a spreadsheet more intelligently based on your
	    // app's needs.
	    int idxSheet = 0;
	    SpreadsheetEntry spreadsheet = spreadsheets.get(idxSheet);
	    System.out.println(idxSheet+" of the sheet index -  title:"+spreadsheet.getTitle().getPlainText());

	    // Make a request to the API to fetch information about all
	    // worksheets in the spreadsheet.
	    List<WorksheetEntry> worksheets = spreadsheet.getWorksheets();

	    // Iterate through each worksheet in the spreadsheet.
	    for (WorksheetEntry worksheet : worksheets) {
	      // Get the worksheet's title, row count, and column count.
	      String title = worksheet.getTitle().getPlainText();
	      int rowCount = worksheet.getRowCount();
	      int colCount = worksheet.getColCount();

	      // Print the fetched information to the screen for this worksheet.
	      System.out.println("\t" + title + "- rows:" + rowCount + " cols: " + colCount);
	      
	      
		    // Fetch the list feed of the worksheet.
		    URL listFeedUrl = worksheet.getListFeedUrl();
		    ListFeed listFeed = service.getFeed(listFeedUrl, ListFeed.class);
		    
		    
		    System.out.print("\t"+"worksheet size:"+listFeed.getEntries().size()+"\n");
		    
		    // Create a local representation of the new row.

		    // Iterate through each row, printing its cell values.
		    for (ListEntry row : listFeed.getEntries()) {
		      // Print the first column's cell value
		      //System.out.print("\t\t["+row.getTitle().getPlainText() + "]\t");
		      // Iterate over the remaining columns, and print each cell value
		      
		      for (String tag : row.getCustomElements().getTags()) {
		        System.out.print("\t"+tag+":"+row.getCustomElements().getValue(tag));
		      }
		      System.out.println();
		    }
		    
		    ListEntry row = new ListEntry();
		    row.getCustomElements().setValueLocal("test1", "Joe");
		    row.getCustomElements().setValueLocal("test2", "Smith");
		    row.getCustomElements().setValueLocal("test3", "26");
		    row.getCustomElements().setValueLocal("test4", "176");
		    
		    //row = service.insert(listFeedUrl, row);	      
	    }
	    
/*	    // Create a local representation of the new worksheet.
	    WorksheetEntry worksheet = new WorksheetEntry();
	    worksheet.setTitle(new PlainTextConstruct("New Worksheet"));
	    worksheet.setColCount(10);
	    worksheet.setRowCount(20);

	    // Send the local representation of the worksheet to the API for
	    // creation.  The URL to use here is the worksheet feed URL of our
	    // spreadsheet.
	    URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
	    service.insert(worksheetFeedUrl, worksheet);	    
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

		get(new Route("/private") {
			@Override
			public Object handle(Request request, Response response) {
				response.status(401);
				return "Go Away!!!";
			}
		});

		get(new Route("/users/:name") {
			@Override
			public Object handle(Request request, Response response) {
				return "Selected user: " + request.params(":name");
			}
		});

		get(new Route("/news/:section") {
			@Override
			public Object handle(Request request, Response response) {
				response.type("text/xml");
				return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>"
						+ request.params("section") + "</news>";
			}
		});

		get(new Route("/protected") {
			@Override
			public Object handle(Request request, Response response) {
				halt(403, "I don't think so!!!");
				return null;
			}
		});

		get(new Route("/redirect") {
			@Override
			public Object handle(Request request, Response response) {
				response.redirect("/news/world");
				return null;
			}
		});

		get(new Route("/") {
			@Override
			public Object handle(Request request, Response response) {
				return "root";
			}
		});
		
		System.out.println("!!!!TEST!!!!");

	}
	
	
	
/*	public static void initDB(){
    	try {
    		// The newInstance() call is a work around for some
    		// broken Java implementations
    		
    		Class.forName("com.mysql.jdbc.Driver").newInstance();
    		
    		Properties props = new Properties();
    		props.put("databases.names", "base");
    		props.put("database.base.driver", DatabasePool.MYSQL_DRIVER);
    		props.put("database.base.url", default_db_url);
    		props.put("database.base.user", default_db_user);
    		props.put("database.base.password", default_db_passwd);
    		props.put("database.base.min", "0");
    		props.put("database.base.max", "5");
    		props.put("database.base.alias", "core");
    		
    		DatabasePool.getInstance().init();
    		DatabasePool.getInstance().start(props);
    		
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		logDebugln("mysql's jdbc driver intializing fail!");
    		System.exit(1);
    	}
	}	
	
	public boolean connectDB(){
		
		try {
			closeDB();
			_conn = DatabasePool.get("core");
//			_conn = DriverManager.getConnection(default_db_url,default_db_user, default_db_passwd);
			_stmt = _conn.createStatement();
		} catch (SQLException ex) {
			// handle any errors
			logDebugln("DB connection is Fail - " + default_db_url);
			logDebugln("SQLException: " + ex.getMessage());
			logDebugln("SQLState: " + ex.getSQLState());
			logDebugln("VendorError: " + ex.getErrorCode());
			return false;
		}
		return true;
	}
*/	

	
}