class CreateLeagueTable {
	mainHall = null;
	comm = null;
	// Should be the create-bot-table div
	mainDiv = null;
	formatManager = null;
	deckManager = null;
	
	deckSelector = null;
	
	leagueDropdown = null;
	timerDropdown = null;
	descText = null;
	inviteCheckbox = null;
	privateCheckbox = null;
	createTableButton = null;
	
	resultDiv = null;
	
	leagueUI = null;
	
	constructor(mainHall, comm, div, formatManager, deckManager) {
		var that = this;
		
		this.mainHall = mainHall;
		this.comm = comm;
		this.mainDiv = div;
		this.formatManager = formatManager;
		this.deckManager = deckManager;
		
		this.deckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager, "league-table")
		
		this.leagueDropdown = $("#league-format");		
		this.resultDiv = $("#league-result");
		
		this.createTableButton = $("#submit-league-table-button").button().click(this.submitTable(this));
		
		this.formatManager.registerLeagueDropdownUpdate(this.leagueDropdown);
		
	}
	
	hide() {
		this.mainDiv.hide();
	}
	
	show() {
		var that = this;
		
		this.leagueUI = new LeagueResultsUI("/gemp-lotr-server", () => {
			that.formatManager.updateFormats();}
		);
		
		that.mainDiv.show();
		
	}
	
	updateResult(text) {
		this.resultDiv.html(text);
	}
	
	submitTable(that) {
		return () => {
			//that.disableSubmission();
			var format = that.leagueDropdown.val();
			
			if(format == null || format === "") {
				that.updateResult("You must select a league.  If you are not enrolled in one, join one below.");
				return;
			}
			
			var deck = that.deckSelector.getSelectedDeck();
			
			if(deck == null || deck === "") {
				that.updateResult("You must select a deck.  Remember that if this is a sealed or draft league, you may only use cards issued by this league.");
				return;
			}

			that.comm.createTable(format, deck, null, null, false, false,
					CreateTable.getResponse(that.resultDiv, that.mainHall.tableCreator.hideAndCloseOnSuccess(that.mainHall.tableCreator)),
					CreateTable.getCreateErrorMap(that.resultDiv)
				);
			
		};
	}
}