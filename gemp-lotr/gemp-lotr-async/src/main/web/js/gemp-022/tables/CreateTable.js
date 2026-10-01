/**
 * The Play popup: the menu of four flows (Bot, Casual, League, Tournament) and the forms behind them.
 *
 * Opening it refreshes formats, leagues and decks, resets every result box to "Ready." and, when "Open this flow by
 * default" is on, goes straight to the Casual form (whose remembered deck / format / timer are applied once the lists
 * arrive, see SelectDeck.requestRestore and TableFlow.want).
 *
 * window.gempOpenCasualInvite(playerName) (contract C1) opens the Casual form set to invite that player.
 */
class CreateTable {
	popup = null;
	comm = null;
	mainHall = null;
	deckManager = null;
	formatManager = null;

	// Div that shows the 4 buttons when first creating a table
	mainSelection = null;

	backButton = null;

	//These four objects manage their respective form
	createBotTable = null;
	createUnrankedTable = null;     // the Casual form (class name kept: hallUi feeds it the player list)
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

		this.formatManager.setErrorOutput($("#play-load-error"));

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
				that.openFlow(that.createBotTable, "Bot");
			});

		this.createUnrankedButton = $("#create-unranked-table-button").button().click(
			function() {
				that.openFlow(that.createUnrankedTable, "Casual");
			});

		this.createLeagueButton = $("#create-league-table-button").button().click(
			function() {
				that.openFlow(that.createLeagueTable, "League");
			});

		this.createTournamentButton = $("#create-tournament-button").button().click(
			function() {
				that.openFlow(that.createTournament, "Tournament");
			});

		this.popup.dialog({
			autoOpen: false,
			closeOnEscape: true,
			closeText: "",
			resizable: true,
			modal: true,
			show: 100,
			hide: 100,
			minWidth: 320,
			minHeight: 300,
			width: CreateTable.fitWidth(CreateTable.WIDTH),
			height: CreateTable.fitHeight(CreateTable.HEIGHT),
			title: "Create Table",
			classes: {"ui-dialog": "play-dialog"},
			open: function() {
				// the window may have changed size since the last open
				that.fitToWindow();
			}
		});

		$(window).on("resize.playPopup", function () {
			if (that.popup.dialog("instance") && that.popup.dialog("isOpen"))
				that.fitToWindow();
		});

		TableFlow.bindInfoToggles(this.popup);
	}

	// The Play popup keeps ONE size for the menu and every form (the largest form scrolls inside it): 900 x 800, the
	// size it always had, clamped to the window (a fixed 800px popup ran off a 768px laptop screen).
	static WIDTH = 900;
	static HEIGHT = 800;

	static fitWidth(preferred) {
		var width = (typeof window != "undefined" && window.innerWidth) ? window.innerWidth : preferred + 40;
		return Math.max(300, Math.min(preferred, width - 32));
	}

	static fitHeight(preferred) {
		preferred = preferred || CreateTable.HEIGHT;
		var height = (typeof window != "undefined" && window.innerHeight) ? window.innerHeight : preferred + 40;
		return Math.max(300, Math.min(preferred, height - 32));
	}

	fitToWindow() {
		this.popup.dialog("option", {
			width: CreateTable.fitWidth(CreateTable.WIDTH),
			height: CreateTable.fitHeight(CreateTable.HEIGHT),
			position: {my: "center", at: "center", of: window}
		});
	}

	openFlow(flow, name) {
		this.hideFlows();
		this.mainSelection.hide();
		flow.show();
		this.popup.dialog('option', 'title', 'Create Table ▸ ' + name);
		this.backButton.focus();
	}

	hideFlows() {
		this.createBotTable.hide();
		this.createUnrankedTable.hide();
		this.createLeagueTable.hide();
		this.createTournament.hide();
	}

	hideAll() {
		this.hideFlows();

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

	closeAfterSuccess() {
		this.hideAll();
		this.popup.dialog("close");
	}

	showPopup() {
		this.formatManager.updateFormats();
		this.deckManager.updateDecks();
		this.hideFlows();
		for (const flow of [this.createBotTable, this.createUnrankedTable, this.createLeagueTable, this.createTournament])
			flow.resetResult();
		this.createTournament.resetForm();

		this.mainSelection.show();
		this.popup.dialog('option', 'title', 'Create Table');

		this.popup.dialog("open");

		if(TableFlow.cookie("unranked-table-default-flow") === "true") {
			this.createUnrankedButton.click();
		}
	}

	// Contract C1: the Casual form with invite-only on and `playerName` as the invitee.
	openCasualInvite(playerName) {
		this.showPopup();
		if (this.createUnrankedTable.mainDiv.css("display") == "none")
			this.createUnrankedButton.click();
		this.createUnrankedTable.setInvite(playerName);
	}

	// Kept for any caller outside the Play flows; new code uses TableFlow.respond.
	static getResponse(outputControl, callback=null) {
		return (data) => {
			var result = TableFlow.readResponse(data);
			$(outputControl).text(result.message);
			if (callback != null)
				callback(result.ok);
			return result.ok;
		};
	}

	static getCreateErrorMap(outputControl, callback=null) {
		return TableFlow.errorMap(function (message) {
			$(outputControl).text(message);
			if (callback != null)
				callback();
		});
	}
}

// Contract C1 (the user list's "Invite to table" calls it when it exists).
window.gempOpenCasualInvite = function (playerName) {
	var hall = window.hall;
	if (hall == null || hall.tableCreator == null)
		return false;
	hall.tableCreator.openCasualInvite(playerName);
	return true;
};
