class CreateBotTable {
	mainHall = null;
	comm = null;
	// Should be the create-bot-table div
	mainDiv = null;
	formatManager = null;
	deckManager = null;
	
	playerDeckSelector = null;
	botDeckSelector = null;
	
	unlockFormatsButton = null;
	formatDropdown = null;
	createTableButton = null;
	
	resultDiv = null;
	
	constructor(mainHall, comm, div, formatManager, deckManager) {
		var that = this;
		
		this.mainHall = mainHall;
		this.comm = comm;
		this.mainDiv = div;
		this.formatManager = formatManager;
		this.deckManager = deckManager;
		
		this.playerDeckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager)
		this.botDeckSelector = new SelectDeck(this.comm, $(div).find(".bot-deck"), deckManager)
		
		this.unlockFormatsButton = $("#bot-unlock-format").button().click(() => {
			that.unlockFormatsButton.hide();
			that.formatDropdown.prop("disabled", false);
			that.mainHall.showDialog("Here There Be Dragons", "WARNING: The bots have only been trained on starters from FOTR block.  As such, they are likely to break when presented with unfamiliar mechanics, which may be as novel as threats or site manipulation, or as mundane as 'assigning allies to skirmishes'.<br/><br/>Use formats beyond Fellowship Block at your own risk, and do not be surprised if they break.", 300);
		});
		this.formatDropdown = $("#bot-format");
		
		this.resultDiv = $("#bot-result");
		
		this.createTableButton = $("#submit-bot-table-button").button().click(this.submitTable(this));
		
		this.formatManager.registerFormatDropdownUpdate(this.formatDropdown);
		this.deckManager.registerUpdate(() => {
			var option = $("<option/>")
					.attr("value", "")
					.text("Random FOTR Starter");
			that.botDeckSelector.playerDeckDropdown.prepend(option);
			that.botDeckSelector.playerDeckDropdown.change();
			that.botDeckSelector.playerDeckDropdown.val("");
		});
		
		
	}
	
	hide() {
		this.mainDiv.hide();
	}
	
	show() {
		var that = this;
		
		this.formatDropdown.val("fotr_block")
		that.mainDiv.show();
	}
	
	enableSubmission() {
		this.createTableButton.removeAttr("disabled");
		this.createTableButton.removeClass("ui-state-disabled")
	}
	
	disableSubmission() {
		this.createTableButton.attr("disabled", "disabled");
		this.createTableButton.addClass("ui-state-disabled")
		this.createTableButton.removeClass("ui-state-focus")
	}
	
	updateResult(text) {
		this.resultDiv.html(text);
	}
	//<option value="" selected>Random FotR Starter</option>
	submitTable(that) {
		return () => {
			//that.disableSubmission();
			var format = that.formatDropdown.val();
			
			if(format == null || format === "") {
				that.updateResult("You must select a format.");
				return;
			}
			
			var botDeck = that.botDeckSelector.getSelectedDeck();
			
			if(botDeck == null) {
				that.updateResult("You must select a deck for the bot to use.");
				return;
			}
			
			var yourDeck = that.playerDeckSelector.getSelectedDeck();
			
			if(yourDeck == null || yourDeck === "") {
				that.updateResult("You must select a deck for you to use.");
				return;
			}

			that.comm.createSoloTable(format, yourDeck, botDeck, false, 
          CreateTable.getResponse(that.resultDiv),
					CreateTable.getCreateErrorMap(that.resultDiv));
			
		};
	}
}