class JoinTable {
	popup = null;
	comm = null;
	mainHall = null;
	deckManager = null;
	formatManager = null;

	joinButton = null;
	resultDiv = null;
	joinResult = null;
	
	deckSelector = null;
	
	constructor(mainHall, comm, popup, formatManager, deckManager) {
		this.comm = comm;
		this.mainHall = mainHall;
		this.popup = popup;
		this.deckManager = deckManager;
		this.formatManager = formatManager;
		
		this.joinButton = $("#submit-join-table-button");
		this.resultDiv = $("#join-result-label");
		this.joinResult = $("#join-result");
		
		this.popup.dialog({
			autoOpen: false,
			closeOnEscape: true,
			closeText: "",
			resizable: true,
			modal: true,
			show: 100,
			hide: 300,
			width: $(window).width()-200,
			height: $(window).height() / 4,
			title: "Join Table",
			close: function() {
				//form[0].reset();
			}
		});
		
		this.deckSelector = new SelectDeck(this.comm, $("#join-table-options").find(".player-deck"), deckManager)
	}
	
	showPopup(that) {
		that.deckManager.updateDecks();
		that.resultDiv.hide();
		that.popup.dialog("open");
	}
	
	hidePopup(that) {
		that.popup.dialog("close");
	}
	
	generateJoinButton(tableId) {
		var that = this;
		var hallButton = $("<button>Join Table</button>");
		$(hallButton).button().click((
			function(id) {
				return function() {
					that.showPopup(that);
					
					that.joinButton.button().click(that.submitTable(that, tableId));
				};
			})(tableId));
		
		return hallButton;
	}
	
	submitTable(that, tableId) {
		return () => {

			var deck = that.deckSelector.getSelectedDeck();

			if(deck == null || deck === "") {
				that.updateResult("You must select a deck.");
				return;
			}
			
			that.comm.joinTable(tableId, deck, 
					JoinTable.getResponse(that.joinResult, (success) => { 
						if(success) {
							that.hidePopup(that);
						}
						else {
							that.resultDiv.show(); 
						}
					}),
					JoinTable.getCreateErrorMap(that.joinResult, () => { that.resultDiv.show(); })
			);
			
		};
	}
	
	updateResult(text) {
		this.resultDiv.show();
		this.joinResult.html(text);
	}
	
	generateJoinQueueButton(queue) {
		var that = this;
		var hallButton = $("<button>Join Queue</button>");
		
		$(hallButton).button().click((
			function(queueInfo) {
				return function () {
					
					that.showPopup(that);

					that.joinButton.button().click(that.submitQueue(that, queueInfo));

				};
			})(queue));
		
		return hallButton;
	}
	
	submitQueue(that, queueInfo) {
		return () => {

			var deck = that.deckSelector.getSelectedDeck();

			if(deck == null || deck === "") {
				that.updateResult("You must select a deck.");
				return;
			}
			
			var queueId = queueInfo.getAttribute("id");
			var type = queueInfo.getAttribute("type");
			if (type !== null)
				type = type.toLowerCase();
			var queueName = queueInfo.getAttribute("queue");
			var queueStart = queueInfo.getAttribute("start");
			that.comm.joinQueue(queueId, deck, function (xml) {
				var result = (JoinTable.getResponse(that.joinResult))(xml);
				if(result) {
					that.mainHall.setPendingTournament(true);
					that.hidePopup(that);
					
					let message = "You have signed up to participate in the <b>" + queueName
					 + "</b> tournament.<br><br>You will use a snapshot of your '<b>" + deck +"</b>' deck as it is right now. " +
					 "If you need to change or update your deck, you will need to leave the queue and rejoin.<br><br>" +
					 "The first game begins at " + queueStart + ".	Good luck!";
					 
					if(type === "sealed") {
						message = "You have signed up to participate in the <b>" + queueName
					 + "</b> tournament.<br><br>When the event begins, you will be issued sealed packs to open and make a deck. " +
					 "At any time during the deck building phase, you will need to lock-in your deck before the tournament begins.<br><br>" +
					 "Deck building begins at " + queueStart + ".	Good luck!";
					}

					if(type === "solodraft") {
						message = "You have signed up to participate in the <b>" + queueName
					 + "</b> tournament.<br><br>When the event begins, use the 'Go to Draft' button in the Playing Tables Section, and then build your deck in the Deck Builder. " +
					 "At any time during the deck building phase, you will need to lock-in your deck before the tournament begins.<br><br>" +
					 "Deck building begins at " + queueStart + ".	Good luck!";
					}

					if(type === "table_solodraft") {
						message = "You have signed up to participate in the <b>" + queueName
					 + "</b> tournament.<br><br>When the event begins, use the 'Go to Draft' button in the Playing Tables Section, and then build your deck in the Deck Builder. " +
					 "At any time during the deck building phase, you will need to lock-in your deck before the tournament begins.<br><br>" +
					 "Deck building begins at " + queueStart + ".	Good luck!";
					}

					if(type === "table_draft") {
						message = "You have signed up to participate in the <b>" + queueName
					 + "</b> tournament.<br><br>When the event begins, use the 'Go to Draft' button in the Playing Tables Section, and then build your deck in the Deck Builder. " +
					 "At any time during the deck building phase, you will need to lock-in your deck before the tournament begins.<br><br>" +
					 "Draft begins at " + queueStart + ".	Good luck!";
					}
					that.mainHall.showDialog("Joined Tournament", message, 320);
				}
				else {
					that.resultDiv.show(); 
				}
			}, JoinTable.getCreateErrorMap(that.joinResult));
			
			
		};
	}
	
	
	
	static getResponse(outputControl, callback=null) {
		return (xml) => {
			if (xml != null && xml != "OK") {
				var root = xml.documentElement;
				if (root.tagName == "error") {
					var message = root.getAttribute("message");
					outputControl.html(message);
					if(callback!=null) {
						callback(false);
					}
					return false;
				}
				else if(root.tagName == "response") {
					var message = root.getAttribute("message");
					outputControl.html(message);
					if(callback!=null) {
						callback(true);
					}
					return true;
				}
			}
			outputControl.html("OK");
			if(callback!=null) {
				callback(true);
			}
			return true;
		};
	}
	
	
	static getCreateErrorMap(outputControl, callback=null) {
		return {
			"0":function() {
				outputControl.html("0: Server has been shut down or there was a problem with your internet connection.");
				if(callback!=null)
					callback();
			},
			"400":function(xhr, status, request) {
				var message = xhr.getResponseHeader("message");
				if(message != null) {
					outputControl.html("400; malformed input: " + message);
				}
				else {
					outputControl.html("400: One of the provided parameters was malformed.  Double-check your input and try again.");
				}
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
				outputControl.html("404: Info not found.  Check that your input is correct with removed whitespace and try again.");
				if(callback!=null)
					callback();
			},
			"410": function() {
				outputControl.html("410: You have been inactive for too long and were loggedout. Refresh the page if you wish to re-stablish connection.");
				if(callback!=null)
					callback();
			},
			"500": function() {
				outputControl.html("500: Server error. One of the provided parameters was probably malformed.  Double-check your input and try again.");
				if(callback!=null)
					callback();
			}
		};
	}
}