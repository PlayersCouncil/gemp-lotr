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
		// 800px, or nearly the whole window on a narrower screen
		width:Math.min(800, Math.floor($(window).width() * 0.95)),
		height:$(window).height() * 0.9,
		closeText: ''
	});
	
	var content = json.content;
	content = content.replaceAll("<br/>", "");
		
	$("#announcement-dialog").html(content);
	// A link to a hall page (#patch-notes/..., #events, ...: a patch note's "Read the full patch notes here") counts as
	// Dismiss: the popup closes, so the page it opens is not left hidden behind it.  Ctrl/Shift/middle clicks are left
	// to the browser (a new tab).
	$("#announcement-dialog").on("click", "a[href^='#']", function (event) {
		if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey)
			return;
		var hash = $(this).attr("href");
		if (hash.length < 2)
			return;
		event.preventDefault();
		comm.dismissAnnouncement();
		announcementDialog.dialog("close");
		if (window.GempLinks && GempLinks.open(hash))
			return;
		window.location.hash = hash;
	});
	announcementDialog.dialog("open");
	//Otherwise any links cause it to scroll to the link
	$("#announcement-dialog").scrollTop("0"); 
}