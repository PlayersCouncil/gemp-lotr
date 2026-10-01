$("banMain").ready(
	function () {
		
		$("#reset-button").button().click(
			function () {
				let execute = confirm("Are you sure you want to reset the password for '" + $("#reset-input").val() + "'?  This action cannot be undone.");
					
				if(!execute)
					return;
					
				let resultdiv = $("#reset-result");
				resultdiv.html("Processing...");
				
				hall.comm.resetUserPassword($("#reset-input").val(), function (string) {
					resultdiv.html(string);
				}, banErrorMap(resultdiv));
			});
		
		$("#permaban-button").button().click(
			function () {
				let resultdiv = $("#permaban-result");
				resultdiv.html("Processing...");
				
				hall.comm.permabanUser($("#permaban-input").val(), function (string) {
					resultdiv.html(string);
				}, banErrorMap(resultdiv));
			});
		
		$("#tempban-button").button().click(
			function () {
				let resultdiv = $("#tempban-result");
				resultdiv.html("Processing...");
				
				hall.comm.tempbanUser($("#tempban-input").val(), $("#temp-ban-duration-select").val(), function (string) {
					resultdiv.html(string);
				}, banErrorMap(resultdiv));
			});
		
		$("#unban-button").button().click(
			function () {
				let resultdiv = $("#unban-result");
				resultdiv.html("Processing...");
				
				hall.comm.unbanUser($("#unban-input").val(), function (string) {
					resultdiv.html(string);
				}, banErrorMap(resultdiv));
			});
		
		$("#sus-button").button().click(
			function () {
				let resultdiv = $("#sus-result");
				resultdiv.html("Processing...");
				
				hall.comm.susUserSearch($("#sus-input").val(), susUserPopulate, banErrorMap(resultdiv), function() {
					$("#displayUsers").hide();
				});
			});
		
		
	});

function susUserPopulate(xml) {
	var root = xml.documentElement;
	if(root == null)
	{
		xml = new DOMParser().parseFromString(xml,"text/xml");
		root = xml.documentElement;
	}
	
	
	if (root.tagName == 'players') {
		var playersDiv = $("#displayUsers");
		playersDiv.show();
		playersDiv.html("");
		playersDiv.append("Similar accounts:");
		var form = $("<div id='banMultipleForm' style='overflow-x:scroll; width:min(120%,1350px); margin-left: max(-50%, -150px);'></div>");

		var table = $("<table class='tables'></table>");
		table.append("<tr style='position:sticky'><th></th><th>Id</th><th>Name</th><th>Password (hash)</th><th>Create IP</th><th>Login IP</th><th>User status</th></tr>");

		var players = root.getElementsByTagName("player");
		for (var j=0; j<players.length; j++) {
			var player = players[j];
			var id=player.getAttribute("id");
			var name=player.getAttribute("name");
			var password = player.getAttribute("password");
			var createIp = player.getAttribute("createIp");
			var loginIp = player.getAttribute("loginIp");
			var status = player.getAttribute("status");
			table.append("<tr><td style='position:sticky;left:0;z-index:2;background-color:#121212;'><input type='checkbox' name='login' value='"+name+"'></td><td style='position:sticky;left:20px;z-index:2;background-color:#121212;'>"+id+"</td><td style='position:sticky;left:77px;z-index:2;background-color:#121212;'>"+name+"</td><td>"+password+"</td><td>"+createIp+"</td><td>"+loginIp+"</td><td>"+status+"</td></tr>");
		}
		form.append(table);
		
		playersDiv.append(form);
		playersDiv.append("<input type='button' value='Permaban Selected' onClick='banMultiple();'> <span id='ban-multiple-result' style='display:inline-block;'>Ready.</span>");
		
		$("#sus-result").html("OK");
	}
}

function banMultiple() {
	var actionSuccess = function () {
		alert("Operation was successful");
	};
	
	var data = {};
	var resultdiv = $("#ban-multiple-result");
	resultdiv.html("Processing...");
	
	var inputs = $("input[type='text'], input[type='checkbox']:checked, option:selected", $("#banMultipleForm").eq(0)).each(
				function () {
					var input = $(this);
					var name = null;
					var value = null;
					if (input.prop("tagName") == "INPUT") {
						name = input.attr("name");
						value = input.val();
					} else if (input.prop("tagName") == "OPTION") {
						name = input.parents("select").attr("name");
						value = input.attr("value");
					}
					if (name != null && value != null) {
						if (data[name] == null)
							data[name] = new Array();
						data[name].push(value);
					}
				});

	if(data.login == null)
	{
		$("#ban-multiple-result").html("Please check one or more users to ban.");
		return;
	}
	hall.comm.banMultiple(data.login, function (string) {
			$("#ban-multiple-result").html("OK");
		}, banErrorMap(resultdiv));
}


function banErrorMap(outputControl, callback=null) {
	return {
		"0":function() {
			outputControl.html("0: Server has been shut down or there was a problem with your internet connection.", "warningMessage");
			if(callback!=null)
				callback();
		},
		"401":function() {
			outputControl.html("401: You are not logged in.");
			if(callback!=null)
				callback();
		},
		"403": function() {
			outputControl.html("403: You do not have permission to perform such actions.");
			if(callback!=null)
				callback();
		},
		"404": function() {
			outputControl.html("404: User not found.  Check that you have capitalized it correctly and removed whitespace and try again.");
			if(callback!=null)
				callback();
		},
		"410": function() {
			outputControl.html("410: You have been inactive for too long and were logged out. Refresh the page if you wish to re-establish connection.");
			if(callback!=null)
				callback();
		}
	};
}