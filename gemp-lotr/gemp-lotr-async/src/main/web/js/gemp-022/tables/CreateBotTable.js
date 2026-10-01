/**
 * Play ▸ Play Against Bots: a solo game against the bot, Fellowship Block unless the player unlocks other formats.
 * The player's deck may be one of theirs or a Deck Library deck; the bot's is a random FotR starter or one of the
 * player's decks.  The deck is remembered per format ("bot-table-decks").
 */
class CreateBotTable extends TableFlow {
	playerDeckSelector = null;
	botDeckSelector = null;

	unlockFormatsButton = null;
	formatDropdown = null;
	createTableButton = null;

	constructor(mainHall, comm, div, formatManager, deckManager) {
		super(mainHall, comm, div, formatManager, deckManager, $("#bot-result"));
		var that = this;

		this.playerDeckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager, "bot-table");
		this.botDeckSelector = new SelectDeck(this.comm, $(div).find(".bot-deck"), deckManager, "bot",
			{fixedFirstOption: "Random FotR Starter"});

		this.unlockFormatsButton = $("#bot-unlock-format").button().click(() => {
			that.unlockFormatsButton.hide();
			that.formatDropdown.prop("disabled", false);
			that.mainHall.showDialog("Here There Be Dragons", "WARNING: The bots have only been trained on starters from FOTR block.  As such, they are likely to break when presented with unfamiliar mechanics, which may be as novel as threats or site manipulation, or as mundane as 'assigning allies to skirmishes'.<br/><br/>Use formats beyond Fellowship Block at your own risk, and do not be surprised if they break.", 300);
		});
		this.formatDropdown = $("#bot-format");

		this.createTableButton = $("#submit-bot-table-button").button().click(this.submitTable(this));

		this.formatManager.registerFormatDropdownUpdate(this.formatDropdown);
		this.formatDropdown.on("change", () => {
			TableFlow.renderFormatInfo($("#help-bot-format"), that.formatManager, that.formatDropdown.val());
		});
	}

	hide() {
		this.mainDiv.hide();
	}

	show() {
		var format = this.formatDropdown.prop("disabled") ? "fotr_block" : (this.formatDropdown.val() || "fotr_block");
		TableFlow.want(this.formatDropdown, format);
		this.playerDeckSelector.requestRestore(format);
		TableFlow.renderFormatInfo($("#help-bot-format"), this.formatManager, this.formatDropdown.val() || format);
		this.mainDiv.show();
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

	submitTable(that) {
		return () => {
			var format = that.formatDropdown.val();

			if(format == null || format === "") {
				that.updateResult("You must select a format.");
				return;
			}

			// "" = a random FotR starter
			var botDeck = that.botDeckSelector.getSelectedDeck();

			var yourDeck = that.playerDeckSelector.getSelection();

			if(yourDeck == null) {
				that.updateResult("You must select a deck for you to use: one of yours, or one from the Deck Library.");
				return;
			}

			that.playerDeckSelector.remember(yourDeck, format);

			// Success: the game opens from the hall as soon as it starts.
			var result = that.respond({
				title: "Create Bot Table",
				quietSuccess: true,
				onSuccess: function () {
					that.mainHall.tableCreator.closeAfterSuccess();
				}
			});
			that.comm.createSoloTable(format, yourDeck.name, botDeck, false, result.callback, result.errorMap, yourDeck.source);
		};
	}
}
