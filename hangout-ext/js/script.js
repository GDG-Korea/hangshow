//var SEND_URL = "http://183.111.25.79/onair/insert";
var SEND_URL = "http://localhost/onair/insert";
var LIST_URL = "http://localhost/onair/list";
var ONAIR_URL = "https://plus.google.com/hangouts";
var SAMPLE_INSERT_URI = "http://localhost:4567/onair/insert?youtube-content-id=1111&title=hangshow-test&description=this-is-hangshow-test&tags=test&owner-id=11111&conference-name=devfest2013&time-start=10:00:00";	 
var topic = "";
var youtubeId;
var userId;
var tags="test";
function init() {

	  // When API is ready...
	gapi.hangout.onApiReady.add(
		function(eventObj) {
			var id = gapi.hangout.getLocalParticipantId();
			console.log("id:"+id);
			var feed = gapi.hangout.layout.getDefaultVideoFeed();
			var canvas = gapi.hangout.layout.getVideoCanvas();
			canvas.setVideoFeed(feed);
			canvas.setWidth(491);
			canvas.setPosition(280 , 40);
			console.log("onApiReady");

			topic = gapi.hangout.getTopic();
			userId = gapi.hangout.getParticipantId();
			if(gapi.hangout.onair.isOnAirHangout()){

				youtubeId = gapi.hangout.onair.getYouTubeLiveId();
				$("#sorry_message").html("OnAir Mode!")
				$("#sorry_message").css("visibility","visible");
				$("#bt_sorry").click(function(){
					canvas.setVisible(true);
					$.get(SAMPLE_INSERT_URI,function(data,status){
						alert("Data: " + data + "\nStatus: " + status);
					});			  			  
				});

			}
			else{
				$("#sorry_message").css("visibility","visible");
				$("#bt_sorry").click(function(){
					var data = {};
					data.youtubecontentid = "youtube-id";
					data.title = topic;
					data.description = topic;
					data.tags = "devfest2013";
					data.timestart = "11:00:00";
					sendDataGet(data,SEND_URL);
				});
				$("#bt_link").click(function(){
					var url="http://naver.com";
					window.open(ONAIR_URL);
					// window.location.href = url;
					// alert("click");
					// document.location.href=url;
				});
				$("#bt_list").click(function(){
					var data = {};
					data = sendDataGet(data,LIST_URL);
					console.log(data);
				});
			}

	    });
	}


	function sendDataGet(input,url)
	{
	    // $.ajaxPrefilter('json', function(options, orig, jqXHR) {
	    //     return 'jsonp';
	    // });
		var ret = "{}";
	    url = url + "?" + decodeURIComponent( $.param( input ) );
		$.get(url,function(){
			console.log("Sent");
		})  
		.done(function(data) {
			console.log( "Success:");
			console.log( data );
			ret = data;
		})
		.fail(function(data) {
			console.log( "Error:"+data );
		})
		return ret;



    // $.ajax({
    //     url: SEND_URL
    //     , crossDomain: true
    //     , dataType: "json"
    //     , type: 'GET'
    //     , data: data
    //     , success: function( data, textStatus, jqXHR )
    //     {
    //         alert("sccess : " + data.channel.item.length );
    //     }
    //     , error: function( jqXHR, textStatus, errorThrown )
    //     {
    //         alert( textStatus + ", " + errorThrown );
    //     }
    // });
}			

// Add startup listener immediately.  If you need an
// OAuth 2.0 access token, your startup will be different.
init();	
