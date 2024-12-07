var announcementDialog = null;

function announcementDeliveryService(comm, json) {
	if(announcementDialog != null)
		return;
	
	console.log("Delivered an announcement:");
	console.log(json);

	var buttons = {};
	buttons["Dismiss"] = function () {
		comm.dismissAnnouncement();
		announcementDialog.dialog("close");
	};

	buttons["Remind Me Later"] = function () {
		comm.snoozeAnnouncement();
		announcementDialog.dialog("close");
	};
	
	var closeCleanup = function() {
		comm.snoozeAnnouncement();
		announcementDialog = null;
		$("#announcement-dialog").remove();
	};

	announcementDialog = $("<div id='announcement-dialog'></div>").dialog({
		title:"ANNOUNCEMENT - " + json.title,
		buttons: buttons,
		autoOpen:false,
		closeOnEscape:true,
		close: closeCleanup,
		resizable:false,
		width:800,
		height:$(window).height() * 0.9,
		closeText: ''
	});
		
	$("#announcement-dialog").html(json.content);
	announcementDialog.dialog("open");
	//Otherwise any links cause it to scroll to the link
	$("#announcement-dialog").scrollTop("0"); 
}