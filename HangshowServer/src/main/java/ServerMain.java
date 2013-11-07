import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import spark.*;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
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
    
    final static String FIELD_YOUTUBE ="youtubecontentid";
    final static String FIELD_TITLE ="title";
    final static String FIELD_DESC ="description";
    final static String FIELD_TAGS ="tags";
    final static String FIELD_OWNER ="ownerid";
    final static String FIELD_CONFERENCE ="conferencename";
    final static String FIELD_START ="timestart";
    
    final static String FIELD_REGID ="regid";
    final static String FIELD_USERID ="userid";

    final static String QEURYSTRING_YOUTUBE ="youtubecontentid";
    final static String QEURYSTRING_TITLE ="title";
    final static String QEURYSTRING_DESC ="description";
    final static String QEURYSTRING_TAGS ="tags";
    final static String QEURYSTRING_OWNER ="ownerid";
    final static String QEURYSTRING_CONFERENCE ="conferencename";
    final static String QEURYSTRING_START ="timestart";
    
    final static String GCM_API_KEY = "AIzaSyAcsMfsSRZIf4EA-UxPQhvBiyYjDO1Cvgs";
    final static String GCM_REG_ID = "APA91bFnEeAN_VnAuOOeTQ3BRnqGr2w53ETt2YnosrvmGmtDgoWnPDdp6MmfXZ9eFyMqgMO_ikpk4GBCdQ3YwHLSkr3eftXKUCzlFc4pHla93okwlC5-ro98ULQcxOi1kg1TqetHvcfFsArz2zvmbf9LFeUZC_S4bA";
    
    static SpreadsheetEntry _mainSheet = null;
    static SpreadsheetService _service = null;
    static WorksheetEntry _mainWorksheet = null;
    static ServerMain _mainServer = new ServerMain();
    
    
    final static String SAMPLE_INSERT_URI = "http://localhost:4567/onair/insert?youtube-content-id=1111&title=행쇼테스트&description=행쇼테스트입니다&tags=test&owner-id=11111&conference-name=devfest2013&time-start=10:00:00";

    
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
           rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           while ((line = rd.readLine()) != null) {
              result += line;
           }
           rd.close();
        } catch (Exception e) {
           e.printStackTrace();
           System.out.println("getHTML error:"+e.toString());
        }
        return result;
     }
    
    public void sendGCM() throws IOException{
	   Sender sender = new Sender(GCM_API_KEY);  //구글 코드에서 발급받은 서버 키
	   Message msg = new Message.Builder()
	       								.addData(FIELD_YOUTUBE, "EfDfdyBldz0")  //데이터 추가
	       								.addData(FIELD_TITLE, "HangShow Team JJang!!!")  //데이터 추가
	       								.build();
	
	   //푸시 전송. 파라미터는 푸시 내용, 보낼 단말의 id, 마지막은 잘 모르겠음 
	   Result result = sender.send(msg, GCM_REG_ID, 5);
	   
	   System.out.println("getHTML:"+getHTML("http://183.111.25.79/onair/list"));
	
	   //결과 처리
	   if(result.getMessageId() != null) {
	      System.out.println("Push Seuccess!");
	   }
	   else {
	      String error = result.getErrorCodeName();   //에러 내용 받기
	      System.out.println("Error:"+error);
	  //에러 처리
		  if(Constants.ERROR_INTERNAL_SERVER_ERROR.equals(error)) {
		     //구글 푸시 서버 에러
		  }
		  else{}
		  }
    }

	public static void main(String[] args) 	      
			throws AuthenticationException, MalformedURLException, IOException, ServiceException {
		_mainServer.sendGCM();
		
/*		String jsSample =   "{ \"name1\": \"50\", \"name2\": \"2\", \"name3\": \"3\"}";
		JSONObject jsonobj = (JSONObject)JSONValue.parse(jsSample);
		System.out.println("name1:"+jsonobj.get("name1")+",name2:"+jsonobj.get("name2")+",name3:"+jsonobj.get("name3"));
*/

		setPort(80);
		get(new Route("/onair/insert") {
			@Override
			public Object handle(Request request, Response response){
				response.header("Access-Control-Allow-Origin","*");
				try {
					System.out.println("/onair/insert - step1");
					response.type("application/json");
					System.out.println("/onair/insert - step2");
					_mainServer.insertRow(request);
					System.out.println("/onair/insert - step3");
					
					System.out.println("/onair/insert");
					_mainServer.printAllRows(_mainServer._mainWorksheet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.toString());
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.toString());
				}				
				//return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<status>"+request.queryParams(FIELD_YOUTUBE)+"</status>";
				return "{ \""+FIELD_YOUTUBE+"\" : \""+request.queryParams(FIELD_YOUTUBE)+"\"}";
			}
		});
		
		get(new Route("/onair/regid") {
			@Override
			public Object handle(Request request, Response response){
				response.header("Access-Control-Allow-Origin","*");
				String jsRs = "{\"status\":\"success\"}";
				try {
					response.type("application/json");
					jsRs = _mainServer.printAllJson(_mainServer._mainWorksheet); 
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println(e.toString());
				} catch (ServiceException e) {
					e.printStackTrace();
					System.out.println(e.toString());
				}				
				return jsRs;
			}
		});
		
		get(new Route("/onair/list") {
			@Override
			public Object handle(Request request, Response response){
				response.header("Access-Control-Allow-Origin","*");
				String jsRs = "{}";
				try {
					response.type("application/json");
					jsRs = _mainServer.printAllJson(_mainServer._mainWorksheet); 
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println(e.toString());
				} catch (ServiceException e) {
					e.printStackTrace();
					System.out.println(e.toString());
				}				
				return jsRs;
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
		if (_mainSheet == null) {
			System.out.println("Cannot find Main Sheet");
			int idxSheet = 0;
			_mainSheet = spreadsheets.get(idxSheet);
			System.out.println(idxSheet + " of the sheet index -  title:"
					+ _mainSheet.getTitle().getPlainText());
		} else {
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
			if (title.equals(SPREADSHEET_SHEET_MAIN)) {
				_mainWorksheet = worksheet;
				System.out.println("\t[Main WorkSheet]: " + title + " -  rows:" + rowCount + " cols: "
						+ colCount);
			}
			else{
				System.out.println("\t[Sub WorkSheet]: " + title + " -  rows:" + rowCount + " cols: "
						+ colCount);
			}
		}
		
		_mainServer.printAllRows(_mainWorksheet);
		
	    
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
		
		System.out.println("!!!!START!!!!");

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
    
    public String printAllJson(WorksheetEntry worksheet) throws IOException, ServiceException{
		URL listFeedUrl = worksheet.getListFeedUrl();
		ListFeed listFeed = _service.getFeed(listFeedUrl, ListFeed.class);
		
		JSONArray jsList = new JSONArray();
		
		System.out.print("[printAllJson] " + "worksheet size:"
				+ listFeed.getEntries().size() + "\n");

		String jsResult = ""; 
		for (ListEntry row : listFeed.getEntries()) {
			JSONObject jsObj = new JSONObject();
			for (String tag : row.getCustomElements().getTags()) {
				jsObj.put(tag,row.getCustomElements().getValue(tag));
			}
			jsList.add(jsObj);
		}
		jsResult = jsList.toJSONString();
		System.out.println("Json:"+jsResult);
		return jsResult;
    }
    
    
    public String checkValid(Object obj){
    	if(obj instanceof String){
    		return (String) obj;
    	}
    	return new String("");
    }
    
    public void insertRow(Request req) throws IOException, ServiceException{
		System.out.println("/onair/insert - step4-1 _mainWorksheet:"+_mainWorksheet);
		if(_mainWorksheet != null){
			System.out.println("/onair/insert - step4-2");
			ListEntry row = new ListEntry();
			row.getCustomElements().setValueLocal(FIELD_YOUTUBE, 		checkValid(req.queryParams(FIELD_YOUTUBE)));
			row.getCustomElements().setValueLocal(FIELD_TITLE, 			checkValid(req.queryParams(FIELD_TITLE)));
			row.getCustomElements().setValueLocal(FIELD_DESC, 			checkValid(req.queryParams(FIELD_DESC)));
			row.getCustomElements().setValueLocal(FIELD_TAGS, 			checkValid(req.queryParams(FIELD_TAGS)));
			row.getCustomElements().setValueLocal(FIELD_OWNER, 			checkValid(req.queryParams(FIELD_OWNER)));
			row.getCustomElements().setValueLocal(FIELD_CONFERENCE, 	checkValid(req.queryParams(FIELD_CONFERENCE)));
			row.getCustomElements().setValueLocal(FIELD_START, 			checkValid(req.queryParams(FIELD_START)));
//			row.getCustomElements().setValueLocal(FIELD_YOUTUBE, 	"insertRow-test");
//			row.getCustomElements().setValueLocal(FIELD_TITLE, 		"insertRow-test");
//			row.getCustomElements().setValueLocal(FIELD_DESC, 		"insertRow-test");
//			row.getCustomElements().setValueLocal(FIELD_TAGS, 		"insertRow-test");
//			row.getCustomElements().setValueLocal(FIELD_OWNER, 		"insertRow-test");
//			row.getCustomElements().setValueLocal(FIELD_CONFERENCE, "insertRow-test");
//			row.getCustomElements().setValueLocal(FIELD_START, 		"insertRow-test");
			System.out.println("/onair/insert - step4-3");

			URL listFeedUrl = _mainWorksheet.getListFeedUrl();
			System.out.println("/onair/insert - step5");
			row = _service.insert(listFeedUrl, row);
			System.out.println("/onair/insert - step6");
		}
    }
    
	
}