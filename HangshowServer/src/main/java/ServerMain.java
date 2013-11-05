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
    final static String _USERNAME = "gdg.hangshow@gmail.com";
    final static String _PASSWORD = "Hangsh0w";
    final static String SPREADSHEET_NAME = "Hangshow-Feed";
    final static String SPREADSHEET_SHEET_MAIN = "Main";
    final static String SPREADSHEET_READY ="Ready";
    
    final static String FIELD_YOUTUBE ="youtube-content-id";
    final static String FIELD_TITLE ="title";
    final static String FIELD_DESC ="description";
    final static String FIELD_TAGS ="tags";
    final static String FIELD_OWNER ="owner-id";
    final static String FIELD_CONFERENCE ="conference-name";
    final static String FIELD_START ="time-start";
    
    final static String QEURYSTRING_YOUTUBE ="youtube-content-id";
    final static String QEURYSTRING_TITLE ="title";
    final static String QEURYSTRING_DESC ="description";
    final static String QEURYSTRING_TAGS ="tags";
    final static String QEURYSTRING_OWNER ="owner-id";
    final static String QEURYSTRING_CONFERENCE ="conference-name";
    final static String QEURYSTRING_START ="time-start";
    
    static SpreadsheetEntry _mainSheet = null;
    static SpreadsheetService _service = null;
    static WorksheetEntry _mainWorksheet = null;
    static ServerMain _mainServer = new ServerMain();
    final static String SAMPLE_INSERT_URI = "http://localhost:4567/onair/insert?youtube-content-id=1111&title=행쇼테스트&description=행쇼테스트입니다&tags=test&owner-id=11111&conference-name=devfest2013&time-start=10:00:00";

    public void printAllRows(WorksheetEntry worksheet) throws IOException, ServiceException{
		// Fetch the list feed of the worksheet.
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = _service.getFeed(listFeedUrl, ListFeed.class);

		System.out.print("\t" + "worksheet size:"
				+ listFeed.getEntries().size() + "\n");

		// Create a local representation of the new row.

		// Iterate through each row, printing its cell values.
		for (ListEntry row : listFeed.getEntries()) {
			// Print the first column's cell value
			// System.out.print("\t\t["+row.getTitle().getPlainText() +
			// "]\t");
			// Iterate over the remaining columns, and print each cell value

			for (String tag : row.getCustomElements().getTags()) {
				System.out.print("\t" + tag + ":"
						+ row.getCustomElements().getValue(tag));
			}
			System.out.println();
		}
    }
    
    public void insertRow(Request req) throws IOException, ServiceException{
		if(_mainWorksheet != null){
			ListEntry row = new ListEntry();
			row.getCustomElements().setValueLocal(FIELD_YOUTUBE, 	req.queryParams(FIELD_YOUTUBE));
			row.getCustomElements().setValueLocal(FIELD_TITLE, 		req.queryParams(FIELD_TITLE));
			row.getCustomElements().setValueLocal(FIELD_DESC, 		req.queryParams(FIELD_DESC));
			row.getCustomElements().setValueLocal(FIELD_TAGS, 		req.queryParams(FIELD_TAGS));
			row.getCustomElements().setValueLocal(FIELD_OWNER, 		req.queryParams(FIELD_OWNER));
			row.getCustomElements().setValueLocal(FIELD_CONFERENCE, req.queryParams(FIELD_CONFERENCE));
			row.getCustomElements().setValueLocal(FIELD_START, 		req.queryParams(FIELD_START));

			URL listFeedUrl = _mainWorksheet.getListFeedUrl();
			row = _service.insert(listFeedUrl, row);
		}
    }
    
	public static void main(String[] args) 	      
			throws AuthenticationException, MalformedURLException, IOException, ServiceException {
		

		
		get(new Route("/onair/insert") {
			@Override
			public Object handle(Request request, Response response) {
				response.type("text/xml");
				
				try {
					_mainServer.insertRow(request);
					_mainServer.printAllRows(_mainServer._mainWorksheet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				//return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<status>"+request.queryParams(FIELD_YOUTUBE)+"</status>";
				return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<status>"+request.queryParams(FIELD_YOUTUBE)+"</status>";
			}
		});
		
		
		System.out.println("Step 1: Login");
		_service = new SpreadsheetService("MySpreadsheetIntegration-v1");
		_service.setUserCredentials(_USERNAME, _PASSWORD);
		_service.setProtocolVersion(SpreadsheetService.Versions.V3);

		URL SPREADSHEET_FEED_URL = new URL(
				"https://spreadsheets.google.com/feeds/spreadsheets/private/full");

		System.out.println("Step 2: SpreadSheet Entries");
		SpreadsheetFeed feed = _service.getFeed(SPREADSHEET_FEED_URL,
				SpreadsheetFeed.class);
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();

		System.out.println("Step 3: Show SpreadSheet's Name");
		// Iterate through all of the spreadsheets returned
		for (SpreadsheetEntry spreadsheet : spreadsheets) {
			// Print the title of this spreadsheet to the screen
			System.out.println(spreadsheet.getTitle().getPlainText());
			if (spreadsheet.getTitle().getPlainText().equals(SPREADSHEET_NAME)) {
				_mainSheet = spreadsheet;
			}
		}

		System.out.println("Step 4: Select Main Sheet");
		// TODO: Choose a spreadsheet more intelligently based on your
		// app's needs.
		if (_mainSheet != null) {
			System.out.println("Cannot find Main Sheet");
		} else {
			int idxSheet = 0;
			_mainSheet = spreadsheets.get(idxSheet);
			System.out.println(idxSheet + " of the sheet index -  title:"
					+ _mainSheet.getTitle().getPlainText());
		}

		// Make a request to the API to fetch information about all
		// worksheets in the spreadsheet.
		List<WorksheetEntry> worksheets = _mainSheet.getWorksheets();

	    // Iterate through each worksheet in the spreadsheet.
		for (WorksheetEntry worksheet : worksheets) {
			// Get the worksheet's title, row count, and column count.
			String title = worksheet.getTitle().getPlainText();
			int rowCount = worksheet.getRowCount();
			int colCount = worksheet.getColCount();

			// Print the fetched information to the screen for this worksheet.
			System.out.println("\t" + title + "- rows:" + rowCount + " cols: "
					+ colCount);
			if (title.equals(SPREADSHEET_SHEET_MAIN)) {
				_mainWorksheet = worksheet;
			}
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
						+ request.params("section") + ":"+request.queryParams("hams")+"</news>";
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