var GempLotrHallUI = Class.extend({
	comm:null,
	chat:null,
	supportedFormatsInitialized:false,
	supportedFormatsSelect:null,
	decksSelect:null,
	tableDescInput:null,
	timerSelect:null,
	createTableButton:null,
	isPrivateCheckbox:null,
	isInviteOnlyCheckbox:null,

	tablesDiv:null,
	buttonsDiv:null,
	adminTab:null,
	userInfo:null,
	
	inTournament:false,

	pocketDiv:null,
	pocketValue:null,
	hallChannelId: null,

	init:function (url, chat) {
		var that = this;
		
		this.chat = chat;
		this.chat.tournamentCallback = function(message) {
			if(that.inTournament) {
				that.showDialog("Tournament Update", message, 320);
			}
		};
		
		$("#chat").resizable({
			handles: "n",
			minHeight: 100,
			distance: 20
		});
		
		var storedChatSize = $.cookie("chatResize");
		if (storedChatSize == null)
			storedChatSize = 300;
		
		$("#chat").height(storedChatSize);
		
		$("#chat").resize(function() {
			$.cookie("chatResize", $("#chat").height(), { expires:365 });
		});
		
		this.comm = new GempLotrCommunication(url, function (xhr, ajaxOptions, thrownError) {
			if (thrownError != "abort") {
				if (xhr != null) {
					if (xhr.status == 401) {
						that.chat.appendMessage("Game hall problem - You're not logged in, go to the <a href='index.html'>main page</a> to log in", "warningMessage");
						return;
					} 
					else if (xhr.status == 504) {
						console.log("HTTP error communicating with server: " + xhr.status);
						return;
					}
					else if (xhr.status != 504) {
						that.chat.appendMessage("The game hall had a problem communicating with the server (" + xhr.status + "), no new updates will be displayed.", "warningMessage");
						that.chat.appendMessage("Reload the browser page (press F5) to resume the game hall functionality.", "warningMessage");
						return;
					}
				}
				that.chat.appendMessage("The game hall had a problem communicating with the server, no new updates will be displayed.", "warningMessage");
				that.chat.appendMessage("Reload the browser page (press F5) to resume the game hall functionality.", "warningMessage");
			}
		});

		this.tablesDiv = $("#tablesDiv");
		this.tableDescInput = $("#tableDescInput");
		this.isPrivateCheckbox = $("#isPrivateCheckbox");
		this.isInviteOnlyCheckbox = $("#isInviteOnlyCheckbox");
		this.pocketDiv = $("#pocketDiv");
		this.supportedFormatsSelect = $("#supportedFormatsSelect");
		this.createTableButton = $("#createTableBut");
		this.decksSelect = $("#decksSelect");
		this.timerSelect = $("#timerSelect");
		this.buttonsDiv = $("#buttonsDiv");
		
		this.adminTab = $("#tabs > ul :nth-child(7)");
		this.adminTab.hide();
		
		this.comm.getPlayerInfo(function(json)
				{ 
					that.userInfo = json;
						if(that.userInfo.type.includes("a") || that.userInfo.type.includes("l"))
						{
							that.adminTab.show();
						}
						else
						{
							that.adminTab.hide();
						}
				});
		

		var hallSettingsStr = $.cookie("hallSettings");
		if (hallSettingsStr == null)
			hallSettingsStr = "1|1|0|0|0|0|0";
		var hallSettings = hallSettingsStr.split("|");

		this.initTable(hallSettings[0] == "1", "waitingTablesHeader", "waitingTablesContent");
		this.initTable(hallSettings[1] == "1", "playingTablesHeader", "playingTablesContent");
		this.initTable(hallSettings[2] == "1", "finishedTablesHeader", "finishedTablesContent");
		this.initTable(hallSettings[3] == "1", "tournamentQueuesHeader", "tournamentQueuesContent");
		this.initTable(hallSettings[4] == "1", "activeTournamentsHeader", "activeTournamentsContent");
		this.initTable(hallSettings[5] == "1", "wcQueuesHeader", "wcQueuesContent");
		this.initTable(hallSettings[6] == "1", "wcEventsHeader", "wcEventsContent");
		
		$('#wcQueuesHeader').hide();
		$('#wcQueuesContent').hide();
		$('#wcEventsHeader').hide();
		$('#wcEventsContent').hide();
		
		$("#deckbuilder-button").button();
		$("#bug-button").button();
		$("#report-button").button();
		$("#discord-button").button();
		$("#wiki-button").button();
		$("#merchant-button").button();

		$(this.createTableButton).button().click(
			function () {
				that.createTableButton.attr("disabled", "disabled");
				that.createTableButton.addClass("ui-state-disabled")
				that.createTableButton.removeClass("ui-state-focus")
				var format = that.supportedFormatsSelect.val();
				var deck = that.decksSelect.val();
				var tableDesc = that.tableDescInput.val();
				var timer = that.timerSelect.val();
				var isPrivate = that.isPrivateCheckbox.is(':checked');
				var isInviteOnly = that.isInviteOnlyCheckbox.is(':checked');
				if (deck != null)
					console.log("creating table");
					that.comm.createTable(format, deck, timer, tableDesc, isPrivate, isInviteOnly, function (xml) {
						console.log("received table response");
						that.processResponse(xml);
					});
			});

		this.getHall();
		this.updateDecks();
		
		
	},
	
	initTable: function(displayed, headerID, tableID) {
		var header = $("#" + headerID);

		var content = $("#" + tableID);

		var that = this;
		var toggle = function() {
			if (content.hasClass("hidden"))
				content.removeClass("hidden");
			else
				content.addClass("hidden");
			content.toggle("blind", {}, 200);
			that.updateHallSettings();
		};
		
		header.click(toggle);

		if (displayed) {
			content.show();
		} else {
			content.addClass("hidden");
			content.hide();
		}
	},


	updateHallSettings: function() {
		var visibilityToggle = $(".visibilityToggle", this.tablesDiv);
		var getSettingValue =
			function(index) {
				return $(visibilityToggle[index]).hasClass("hidden") ? "0" : "1";
			};

		var newHallSettings = getSettingValue(0) + "|" + getSettingValue(1) + "|" + getSettingValue(2) + "|" + getSettingValue(3) + "|" + getSettingValue(4)+ "|" + getSettingValue(5)+ "|" + getSettingValue(6);
		console.log("New settings: " + newHallSettings);
		$.cookie("hallSettings", newHallSettings, { expires:365 });
	},

	getHall: function() {
		var that = this;

		this.comm.getHall(
			function(xml) {
				that.processHall(xml);
			}, this.hallErrorMap());
	},

	updateHall:function () {
		var that = this;
		this.comm.updateHall(
			function (xml) {
				that.processHall(xml);
			}, this.hallChannelId, this.hallErrorMap());
	},

	hallErrorMap:function() {
		var that = this;
		return {
			"0": function() {
				that.showErrorDialog("Server connection error", "Unable to connect to server. Either server is down or there is a problem with your internet connection.", true, false);
			},
			"401":function() {
				that.showErrorDialog("Authentication error", "You are not logged in", false, true);
			},
			"409":function() {
				that.showErrorDialog("Concurrent access error", "You are accessing Game Hall from another browser or window. Close this window or if you wish to access Game Hall from here, click \"Refresh page\".", true, false);
			},
			"410":function() {
				that.showErrorDialog("Inactivity error", "You were inactive for too long and have been removed from the Game Hall. If you wish to re-enter, click \"Refresh page\".", true, false);
			}
		};
	},

	showErrorDialog:function(title, text, reloadButton, mainPageButton) {
		var buttons = {};
		if (reloadButton) {
			buttons["Refresh page"] =
				function () {
					location.reload(true);
				};
		}
		if (mainPageButton) {
			buttons["Go to main page"] =
				function() {
					location.href = "/gemp-lotr/";
				};
		}

		var dialog = $("<div></div>").dialog({
			title: title,
			resizable: false,
			height: 160,
			modal: true,
			buttons: buttons,
			closeText: ''
		}).text(text);
	},
	
	showDialog:function(title, text, height) {
		if(height == null)
			height = 200
		var dialog = $("<div></div>").dialog({
			title: title,
			resizable: true,
			height: height,
			modal: true,
			closeOnEscape: true,
			buttons: [
				{
					text: "OK",
					click: function() {
						$( this ).dialog( "close" );
					}
				}
			],
			closeText: ''
		}).html(text);	
	},

	updateDecks:function () {
		var that = this;
		this.comm.getDecks(function (xml) {
			count = xml.documentElement.getElementsByTagName("deck").length;
			if(count == 0)
			{
				that.comm.getLibraryDecks(function(xml) {
					that.processDecks(xml);
				});
			}
			else
			{
				that.processDecks(xml);
			}
			
		});
	},

	processResponse:function (xml) {
		if (xml != null && xml != "OK") {
			var root = xml.documentElement;
			if (root.tagName == "error") {
				var message = root.getAttribute("message");
				this.chat.appendMessage(message, "warningMessage");
				
				this.showDialog("Error", message, 320);
				return false;
			}
			else if(root.tagName == "response") {
				var message = root.getAttribute("message");
				this.chat.appendMessage(message, "warningMessage");
				
				this.showDialog("Response", message, 320);
				return true;
			}
		}
		return true;
	},

	processDecks:function (xml) {
		var root = xml.documentElement;
		
		function formatDeckName(formatName, deckName)
		{
			return "[" + formatName + "] - " + deckName;
		}
		if (root.tagName == "decks") {
			this.decksSelect.html("");
			var decks = root.getElementsByTagName("deck");
			for (var i = 0; i < decks.length; i++) {
				var deck = decks[i];
				var deckName = deck.childNodes[0].nodeValue;
				var formatName = deck.getAttribute("targetFormat");
				var deckElem = $("<option/>")
						.attr("value", deckName)
						.text(formatDeckName(formatName, deckName));
				this.decksSelect.append(deckElem);
			}
			this.decksSelect.css("display", "");
		}
	},

	animateRowUpdate: function(rowSelector) {
		$(rowSelector, this.tablesDiv)
			.css({borderTopColor:"#000000", borderLeftColor:"#000000", borderBottomColor:"#000000", borderRightColor:"#000000"})
			.animate({borderTopColor:"#ffffff", borderLeftColor:"#ffffff", borderBottomColor:"#ffffff", borderRightColor:"#ffffff"}, "fast");
	},
	
	PlaySound: function(soundObj) {
		var myAudio = document.getElementById(soundObj);
		myAudio.play();
	},
	
	AddTesterFlag: function() {
		var that = this;
		
		that.comm.addTesterFlag(function () {
			window.location.reload(true);
		});
	},
	
	RemoveTesterFlag: function() {
		var that = this;
		
		that.comm.removeTesterFlag(function () {
			window.location.reload(true);
		});
	},
	
	MigrateTrophies: function() {
		var that = this;
		
		that.comm.migrateTrophies(function () {
			window.location.reload(true);
		});
	},

	processHall:function (xml) {
		var that = this;
		
		var root = xml.documentElement;
		if (root.tagName == "hall") {
			this.hallChannelId = root.getAttribute("channelNumber");

			var currency = parseInt(root.getAttribute("currency"));
			if (currency != this.pocketValue) {
				this.pocketValue = currency;
				//this.pocketDiv.html(formatPrice(currency));
			}

			var motd = root.getAttribute("motd");
			if (motd != null)
				$("#motd").html("<b>MOTD:</b> " + motd);

			var serverTime = root.getAttribute("serverTime");
			if (serverTime != null)
				$(".serverTime").text("Server time: " + serverTime);

			var queues = root.getElementsByTagName("queue");
			for (var i = 0; i < queues.length; i++) {
				var queue = queues[i];
				var id = queue.getAttribute("id");
				var isWC = id.toLowerCase().includes("wc");
				var action = queue.getAttribute("action");
				if (action == "add" || action == "update") {
					var actionsField = $("<td></td>");

					var joined = queue.getAttribute("signedUp");
					if (joined != "true" && queue.getAttribute("joinable") == "true") {
						var but = $("<button>Join Queue</button>");
						$(but).button().click((
							function(queueInfo) {
								return function () {
									var deck = that.decksSelect.val();
									
									if (deck == null)
										return;
									
									var queueId = queueInfo.getAttribute("id");
									var type = queueInfo.getAttribute("type");
									if(type !== null)
										type = type.toLowerCase();
									var queueName = queueInfo.getAttribute("queue");
									var queueStart = queueInfo.getAttribute("start");
									that.comm.joinQueue(queueId, deck, function (xml) {
										var result = that.processResponse(xml);
										if(result) {
											that.inTournament = true;
											let message = "You have signed up to participate in the <b>" + queueName
											 + "</b> tournament.<br><br>You will use a snapshot of your '<b>" + deck +"</b>' deck as it is right now.  " + 
											 "If you need to change or update your deck, you will need to leave the queue and rejoin.<br><br>" +
											 "The first game begins at " + queueStart + ".	Good luck!";
											 
											if(type === "sealed") {
												message = "You have signed up to participate in the <b>" + queueName
											 + "</b> tournament.<br><br>When the event begins, you will be issued sealed packs to open and make a deck.  " + 
											 "At any time during the deckbuilding phase and for a short time after it ends, you will need to lock-in your deck before the tournament begins.<br><br>" +
											 "Deckbuilding begins at " + queueStart + ".	Good luck!";
											}
											that.showDialog("Joined Tournament", message, 320);
										}
									}, that.hallErrorMap());
								};
							}
							)(queue));
						actionsField.append(but);
					} else if (joined == "true") {
						that.inTournament = true;
						var but = $("<button>Leave Queue</button>");
						$(but).button().click((
							function(queueInfo) {
								return function() {
									var queueId = queueInfo.getAttribute("id");
									var queueName = queueInfo.getAttribute("queue");
									var queueStart = queueInfo.getAttribute("start");
									that.comm.leaveQueue(queueId, function (xml) {
										var result = that.processResponse(xml);
										
										if(result) {
											that.inTournament = false;
											that.showDialog("Left Tournament", "You have been removed from the <b>" + queueName
											 + "</b> tournament.<br><br>If you wish to rejoin, you will need to requeue before it starts at " + queueStart + ".", 230);
										}
									});
								}
							})(queue));
						actionsField.append(but);
					}

					var rowstr = "<tr class='queue" + id + "'><td>" + queue.getAttribute("format") + "</td>";
					if(isWC) {
						rowstr = "<tr class='wc-queue" + id + "'><td>" + queue.getAttribute("format") + "</td>" +
						"<td>" + queue.getAttribute("queue") + "</td>" +
						"<td>" + queue.getAttribute("start") + "</td>" +
						"<td>" + queue.getAttribute("system") + "</td>" +
						"<td><div class='prizeHint' title='Queued Players' value='" + queue.getAttribute("playerList") + "'>" + queue.getAttribute("playerCount") + "</div></td>" +
						"</tr>";
					}
					else {
						rowstr += "<td>" + queue.getAttribute("collection") + "</td>" +
						"<td>" + queue.getAttribute("queue") + "</td>" +
						"<td>" + queue.getAttribute("start") + "</td>" +
						"<td>" + queue.getAttribute("system") + "</td>" +
						"<td><div class='prizeHint' title='Queued Players' value='" + queue.getAttribute("playerList") + "'>" + queue.getAttribute("playerCount") + "</div></td>" +
						"<td align='right'>" + formatPrice(queue.getAttribute("cost")) + "</td>" +
						"<td>" + queue.getAttribute("prizes") + "</td>" +
						"</tr>";
					}
						
					var row = $(rowstr);
					row.append(actionsField);

					if (action == "add") {
						if(isWC) {
							$("table.wc-queues", this.tablesDiv)
							.append(row);
						}
						else {
							$("table.queues", this.tablesDiv)
							.append(row);
						}
					} else if (action == "update") {
						if(isWC) {
							$(".wc-queue" + id, this.tablesDiv).replaceWith(row);
						}
						else {
							$(".queue" + id, this.tablesDiv).replaceWith(row);
						}
						
					}

					if(isWC) {
						this.animateRowUpdate(".wc-queue" + id);
					}
					else {
						this.animateRowUpdate(".queue" + id);
					}
					
				} else if (action == "remove") {
					if(isWC) {
						$(".wc-queue" + id, this.tablesDiv).remove();
					}
					else {
						$(".queue" + id, this.tablesDiv).remove();
					}
					
				}
			}
			
			if($('.wc-queues tr').length <= 1) {
				$('#wcQueuesHeader').hide();
				$('#wcQueuesContent').hide();
			} else {
				$('#wcQueuesHeader').show();
			}

			var tournaments = root.getElementsByTagName("tournament");
			for (var i = 0; i < tournaments.length; i++) {
				var tournament = tournaments[i];
				var id = tournament.getAttribute("id");
				var isWC = id.includes("wc");
				var action = tournament.getAttribute("action");
				var type = tournament.getAttribute("type");
				if(type !== null)
					type = type.toLowerCase();
				var stage = tournament.getAttribute("stage");
					if(stage !== null)
						stage = stage.toLowerCase();
				var abandoned = tournament.getAttribute("abandoned") === "true";
				if (action == "add" || action == "update") {
					var actionsField = $("<td></td>");

					var joined = tournament.getAttribute("signedUp");
					if (joined == "true") {
						that.inTournament = true;
						debugger;
						if(type === "sealed" && (stage === "deck-building" || stage === "registering decks" || stage === "awaiting kickoff" || stage === "paused between rounds")) {
								var but = $("<button>Register Deck</button>");
								$(but).button().click((
									function(tourneyInfo) {
										var tourneyId = tournament.getAttribute("id");
										var tourneyName = tournament.getAttribute("name");
										
										return function () {
											that.comm.registerSealedTournamentDeck(tourneyId, that.decksSelect.val(), function (xml) {
														that.processResponse(xml);
												});
										};
									}
									)(tournament));
								actionsField.append(but);
						}
						
						var but = $("<button>Abandon Tournament</button>");
						$(but).button().click((
							function(tourneyInfo) {
								var tourneyId = tournament.getAttribute("id");
								var tourneyName = tournament.getAttribute("name");
								
								return function () {
									let isExecuted = confirm("Are you sure you want to resign from the " + tourneyName + " tournament? This cannot be undone.");
									
									if(isExecuted) {
										that.inTournament = false;
										that.comm.dropFromTournament(tourneyId, function (xml) {
											that.processResponse(xml);
									});
									}
								};
							}
							)(tournament));
						actionsField.append(but);
					}
					else if(!abandoned){
						if(stage === "deck-building" || stage === "drafting" || stage === "awaiting kickoff"
						   || stage === "preparing" || stage === "paused between rounds") {
							var but = $("<button>Join Tournament</button>");
							$(but).button().click((
								function(tourneyInfo) {
									var tourneyId = tournament.getAttribute("id");
									var tourneyName = tournament.getAttribute("name");
									
									return function () {
											that.comm.joinTournamentLate(tourneyId, that.decksSelect.val(), function (xml) {
												that.processResponse(xml);
										});
								};
							}
							)(tournament));
							actionsField.append(but);
						}
					}

					var rowstr = "";
					
					if(isWC) {
						rowstr = $("<tr class='wc-tournament" + id + "'><td>" + tournament.getAttribute("format") + "</td>" +
						"<td>" + tournament.getAttribute("name") + "</td>" +
						"<td>" + tournament.getAttribute("system") + "</td>" +
						"<td>" + tournament.getAttribute("stage") + "</td>" +
						"<td>" + tournament.getAttribute("round") + "</td>" +
						"<td><div class='prizeHint' title='Competing Players' value='" + tournament.getAttribute("playerList") + "<br><br>* = abandoned'>" + tournament.getAttribute("playerCount") + "</div></td>" +
						"</tr>");
					}
					else {
						rowhtml = "<tr class='tournament" + id + "'><td>" + tournament.getAttribute("format") + "</td>";
						if(type === "sealed") {
							rowhtml += "<td>Limited</td>";
						}
						else {
							rowhtml += "<td>" + tournament.getAttribute("collection") + "</td>";
						}
						
						rowhtml += "<td>" + tournament.getAttribute("name") + "</td>" +
						"<td>" + tournament.getAttribute("system") + "</td>" +
						"<td>" + tournament.getAttribute("stage") + "</td>" +
						"<td>" + tournament.getAttribute("round") + "</td>" +
						"<td><div class='prizeHint' title='Competing Players' value='" + tournament.getAttribute("playerList") + "<br><br>* = abandoned'>" + tournament.getAttribute("playerCount") + "</div></td>" +
						"</tr>";
						
						rowstr = $(rowhtml);
					}

					var row = $(rowstr);
					row.append(actionsField);

					if (action == "add") {
						if(isWC) {
							$("table.wc-events", this.tablesDiv)
							.append(row);
						}
						else {
							$("table.tournaments", this.tablesDiv)
							.append(row);
						}
						
					} else if (action == "update") {
						if(isWC) {
							$(".wc-tournament" + id, this.tablesDiv).replaceWith(row);
						}
						else {
							$(".tournament" + id, this.tablesDiv).replaceWith(row);
						}
						
					}

					this.animateRowUpdate(".tournament" + id);
				} else if (action == "remove") {
					if(isWC) {
						$(".wc-tournament" + id, this.tablesDiv).remove();
					}
					else {
						$(".tournament" + id, this.tablesDiv).remove();
					}
				}
			}
			
			if($('.wc-events tr').length <= 1) {
				$('#wcEventsHeader').hide();
				$('#wcEventsContent').hide();
			} else {
				$('#wcEventsHeader').show();
			}

			var tables = root.getElementsByTagName("table");
			for (var i = 0; i < tables.length; i++) {
				var table = tables[i];
				var id = table.getAttribute("id");
				var action = table.getAttribute("action");
				if (action == "add" || action == "update") {
					var status = table.getAttribute("status");

					var gameId = table.getAttribute("gameId");
					var statusDescription = table.getAttribute("statusDescription");
					var watchable = table.getAttribute("watchable");
					var playersAttr = table.getAttribute("players");
					var formatName = table.getAttribute("format");
					var tournamentName = table.getAttribute("tournament");
					var userDesc = table.getAttribute("userDescription");
					var isPrivate = (table.getAttribute("isPrivate") === "true");
					var isInviteOnly = (table.getAttribute("isInviteOnly") === "true");
					var inviteForYou = isInviteOnly && userDesc === chat.userName;
					var players = new Array();
					if (playersAttr.length > 0)
						players = playersAttr.split(",");
					var playing = table.getAttribute("playing");
					var winner = table.getAttribute("winner");

					var row = $("<tr class='table" + id + "'></tr>");

					row.append("<td>" + formatName + "</td>");
					var name = "<td>" + tournamentName;
					if(isPrivate) 
					{
						if(!!userDesc)
						{
							if(isInviteOnly)
							{
								name += " - <i>Private match for user '" + userDesc + "'.";
							}
							else 
							{
								name += " - <i>Private match: [" + userDesc + "]</i>";
							}
						}
						else {
							name += " - <i>Private.</i>";
						}
					}
					else 
					{
						if(!!userDesc)
						{
							if(isInviteOnly)
							{
								name += " - <i>Match for user '" + userDesc + "'.";
							}
							else 
							{
								name += " - <i>[" + userDesc + "]</i>";
							}
						}
					}
					
					name += "</td>";
					
					//TODO: Replace this with an actual fix on the server side 
					if(name.includes("Casual - WC")) {
						name = "<td><b>2024 World Championship" + userDesc + "</b></td>"
					}
					row.append(name);
					row.append("<td>" + statusDescription + "</td>");

					var playersStr = "";
					for (var playerI = 0; playerI < players.length; playerI++) {
						if (playerI > 0)
							playersStr += ", ";
						playersStr += players[playerI];
					}
					row.append("<td>" + playersStr + "</td>");

					var lastField = $("<td></td>");
					if (status == "WAITING") {
						if (playing == "true") {
							var that = this;

							var but = $("<button>Leave Table</button>");
							$(but).button().click((
								function(tableId) {
									return function() {
										that.comm.leaveTable(tableId);
									};
								})(id));
							lastField.append(but);
						} 
						else if(!isInviteOnly || inviteForYou) {
							var that = this;

							var but = $("<button>Join Table</button>");
							$(but).button().click((
								function(tableId) {
									return function() {
										var deck = that.decksSelect.val();
										if (deck != null)
											that.comm.joinTable(tableId, deck, function (xml) {
												that.processResponse(xml);
											});
									};
								})(id));
							lastField.append(but);
						}
					} else if (status == "PLAYING") {
						if (playing == "true") {
							var participantId = getUrlParam("participantId");
							var participantIdAppend = "";
							if (participantId != null)
								participantIdAppend = "&participantId=" + participantId;

							var but = $("<button>Play Match</button>");
							var link = $("<a href='game.html?gameId=" + gameId + participantIdAppend + "' target='_blank'></a>");
							link.append(but);
							but.button();
							lastField.append(link);
						} else if (watchable == "true") {
							var participantId = getUrlParam("participantId");
							var participantIdAppend = "";
							if (participantId != null)
								participantIdAppend = "&participantId=" + participantId;

							var but = $("<button>Spectate</button>");
							var link = $("<a target='_blank' href='game.html?gameId=" + gameId + participantIdAppend + "'></a>");
							link.append(but);
							but.button();
							lastField.append(link);
						}
					} else if (status == "FINISHED") {
						if (winner != null) {
							lastField.append(winner);
						}
					}

					row.append(lastField);

					if (action == "add") {
						if (status == "WAITING") {
							$("table.waitingTables", this.tablesDiv)
								.append(row);
						} else if (status == "PLAYING") {
							$("table.playingTables", this.tablesDiv)
								.append(row);
						} else if (status == "FINISHED") {
							$("table.finishedTables", this.tablesDiv)
								.append(row);
						}
					} else if (action == "update") {
						if (status == "WAITING") {
							if ($(".table" + id, $("table.waitingTables")).length > 0) {
								$(".table" + id, this.tablesDiv).replaceWith(row);
							} else {
								$(".table" + id, this.tablesDiv).remove();
								$("table.waitingTables", this.tablesDiv)
									.append(row);
							}
						} else if (status == "PLAYING") {
							if ($(".table" + id, $("table.playingTables")).length > 0) {
								$(".table" + id, this.tablesDiv).replaceWith(row);
							} else {
								$(".table" + id, this.tablesDiv).remove();
								$("table.playingTables", this.tablesDiv)
									.append(row);
							}
						} else if (status == "FINISHED") {
							if ($(".table" + id, $("table.finishedTables")).length > 0) {
								$(".table" + id, this.tablesDiv).replaceWith(row);
							} else {
								$(".table" + id, this.tablesDiv).remove();
								$("table.finishedTables", this.tablesDiv)
									.append(row);
							}
						}

						this.animateRowUpdate(".table" + id);
					}

					if (playing == "true")
						row.addClass("played");
					
					if(inviteForYou)
						row.addClass("privateForPlayer");
				} else if (action == "remove") {
					$(".table" + id, this.tablesDiv).remove();
				}
			}

			$(".count", $(".eventHeader.queues")).html("(" + ($("tr", $("table.queues")).length - 1) + ")");
			$(".count", $(".eventHeader.tournaments")).html("(" + ($("tr", $("table.tournaments")).length - 1) + ")");
			$(".count", $(".eventHeader.waitingTables")).html("(" + ($("tr", $("table.waitingTables")).length - 1) + ")");
			$(".count", $(".eventHeader.playingTables")).html("(" + ($("tr", $("table.playingTables")).length - 1) + ")");
			$(".count", $(".eventHeader.finishedTables")).html("(" + ($("tr", $("table.finishedTables")).length - 1) + ")");
			
			$(".count", $(".eventHeader.wc-queues")).html("(" + ($("tr", $("table.wc-queues")).length - 1) + ")");
			$(".count", $(".eventHeader.wc-events")).html("(" + ($("tr", $("table.wc-events")).length - 1) + ")");

			var games = root.getElementsByTagName("newGame");
			for (var i=0; i<games.length; i++) {
				var waitingGameId = games[i].getAttribute("id");
				var participantId = getUrlParam("participantId");
				var participantIdAppend = "";
				if (participantId != null)
					participantIdAppend = "&participantId=" + participantId;
				window.open("/gemp-lotr/game.html?gameId=" + waitingGameId + participantIdAppend, "_blank");
			}
			if (games.length > 0) {				
				this.PlaySound("gamestart");
			}

			if (!this.supportedFormatsInitialized) {
				var formats = root.getElementsByTagName("format");
				for (var i = 0; i < formats.length; i++) {
					var format = formats[i].childNodes[0].nodeValue;
					var type = formats[i].getAttribute("type");
					
					var item = "<option value='" + type + "'>" + format + "</option>"
					
					this.supportedFormatsSelect.append(item);
				}
				this.supportedFormatsInitialized = true;
			}

			that.createTableButton.removeAttr("disabled");
			that.createTableButton.removeClass("ui-state-disabled")

			setTimeout(function () {
				that.updateHall();
			}, 100);
		}
	}
});
