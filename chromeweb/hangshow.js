// 아이콘 클릭 이벤트를 설정합니다.
// Called when the user clicks on the browser action.
chrome.browserAction.onClicked.addListener(function(tab) {
	var action_url = "https://hangoutsapi.talkgadget.google.com/hangouts/_?gid=800618133436";
	chrome.tabs.create({url: action_url});
});  
