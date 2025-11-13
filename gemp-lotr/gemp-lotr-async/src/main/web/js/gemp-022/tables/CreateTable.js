class CreateTable {
	popup = null;
	comm = null;
	mainHall = null;
	deckManager = null;
	formatManager = null;
	
	deckSelector = null;
	
	// Div that shows the 4 buttons when first creating a table
	mainSelection = null;
	
	backButton = null;
	
	//These four objects manage their respective form
	createBotTable = null;
	createUnrankedTable = null;
	createLeagueTable = null;
	createTournament = null;
	
	//These four buttons each summon the above appropriate forms
	createBotButton = null;
	createUnrankedButton = null;
	createLeagueButton = null;
	createTournamentButton = null;
	
	constructor(mainHall, comm, popup, formatManager, deckManager) {
		var that = this;
		
		this.comm = comm;
		this.mainHall = mainHall;
		this.popup = popup;
		this.deckManager = deckManager;
		this.formatManager = formatManager;
		
		this.mainSelection = $("#create-table-selection");
		
		this.createBotTable = new CreateBotTable(mainHall, comm, $("#create-bot-table"), formatManager, deckManager);
		this.createUnrankedTable = new CreateUnrankedTable(mainHall, comm, $("#create-unranked-table"), formatManager, deckManager);
		this.createLeagueTable = new CreateLeagueTable(mainHall, comm, $("#create-league-table"), formatManager, deckManager);
		this.createTournament = new CreateTournament(mainHall, comm, $("#create-tournament"), formatManager, deckManager);
		
		this.backButton = $("#create-table-back-button").button().click(
			function() {
				if(that.mainSelection.is(":visible")) {
					that.popup.dialog("close");
				}
				else {
					that.hideAll();
				}
			});
		
		
		this.createBotButton = $("#create-bot-table-button").button().click(
			function() {
				that.mainSelection.hide();
				that.createBotTable.show();
				that.popup.dialog('option', 'title', 'Create Table ▸ Bot');
				that.backButton.focus();
			});
		
		this.createUnrankedButton = $("#create-unranked-table-button").button().click(
			function() {
				that.mainSelection.hide();
				that.createUnrankedTable.show();
				that.popup.dialog('option', 'title', 'Create Table ▸ Unranked');
				that.backButton.focus();
			});
		
		this.createLeagueButton = $("#create-league-table-button").button().click(
			function() {
				that.mainSelection.hide();
				that.createLeagueTable.show();
				that.popup.dialog('option', 'title', 'Create Table ▸ League');
				that.backButton.focus();
			});
		
		this.createTournamentButton = $("#create-tournament-button").button().click(
			function() {
				that.mainSelection.hide();
				that.createTournament.show();
				that.popup.dialog('option', 'title', 'Create Table ▸ Tournament');
				that.backButton.focus();
			});
		
		this.popup.dialog({
			autoOpen: false,
			closeOnEscape: true,
			closeText: "",
			resizable: true,
			modal: true,
			show: 100,
			hide: 100,
			minWidth: 450,
			width: 900,
			minHeight: 400,
			height: 800,
			title: "Create Table",
			open: function(event, ui) { 
	
			},
			close: function() {
				//form[0].reset();
			}
		});
		
		//this.deckSelector = new SelectDeck(this.comm, $("#create-decks"), this.deckManager);
	}
	
	hideAll() {
		this.createBotTable.hide();
		this.createUnrankedTable.hide();
		this.createLeagueTable.hide();
		this.createTournament.hide();
		
		this.mainSelection.show();
		this.popup.dialog('option', 'title', 'Create Table');
	}
	
	hideAndCloseOnSuccess(that) {
		return (success) => {
			if(success) {
				that.hideAll();
				that.popup.dialog("close");
			}
		}
	}
	
	showPopup() {
		var that = this;
		this.formatManager.updateFormats();
		this.deckManager.updateDecks();
		this.createBotTable.hide();
		this.createUnrankedTable.hide();
		this.createLeagueTable.hide();
		this.createTournament.hide();
		
		this.mainSelection.show();
		
		this.popup.dialog("open");
		
		var unrankedDefaultFlow = loadFromCookie("unranked-table-default-flow", "false");
		if(unrankedDefaultFlow === "true") {
			this.createUnrankedButton.click();
		}
	}
	
	static getResponse(outputControl, callback=null) {
		return (xml) => {
			if (xml != null && xml != "OK" && (xml.response !== null && xml.response != "OK" )) {
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