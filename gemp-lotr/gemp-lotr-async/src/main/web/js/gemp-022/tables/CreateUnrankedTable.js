/**
 * Play ▸ Open Casual Table: a 1-on-1 table in a format and timer of the player's choice, optionally private or
 * invite-only.  (The class keeps its old "Unranked" name: hallUi feeds the hall's player list to updatePlayers.)
 *
 * Remembered between visits (cookies): the format ("unranked-table-last-format"), the timer
 * ("unranked-table-last-timer"), "Open this flow by default" ("unranked-table-default-flow") and the deck per format
 * (SelectDeck, "unranked-table-decks").
 *
 * Invite-only: the invitee is picked with a PlayerPicker (playerPicker.js): a dropdown of the players in the hall, or
 * type to search every registered player (GET /hall/players).  The server checks the name exists and seats only that
 * player.  By the hall's convention the table's description is the invitee's name.
 */
class CreateUnrankedTable extends TableFlow {
	deckSelector = null;

	formatDropdown = null;
	timerDropdown = null;
	descText = null;
	inviteCheckbox = null;
	inviteeInput = null;
	invitePicker = null;
	privateCheckbox = null;
	keepopenCheckbox = null;
	defaultFlowCheckbox = null;
	createTableButton = null;

	currentPlayer = "";
	syncing = false;

	constructor(mainHall, comm, div, formatManager, deckManager) {
		super(mainHall, comm, div, formatManager, deckManager, $("#unranked-result"));
		var that = this;

		this.deckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager, "unranked-table");

		this.deckDropdown = this.deckSelector.playerDeckDropdown;
		this.formatDropdown = $("#unranked-format");
		this.timerDropdown = $("#unranked-timer");
		this.descText = $("#unranked-desc");
		this.inviteCheckbox = $("#unranked-invite-only");
		this.inviteeInput = $("#unranked-invitee");
		this.invitePicker = new PlayerPicker(this.inviteeInput, {
			search: function (prefix, limit, done, failed) {
				that.comm.searchHallPlayers(prefix, limit, function (json) {
					done((json && json.players) || []);
				}, TableFlow.errorMap(function () {
					failed();
				}));
			}
		});
		this.privateCheckbox = $("#unranked-private");
		this.keepopenCheckbox = $("#unranked-keep-open");
		this.defaultFlowCheckbox = $("#unranked-default-flow");

		this.inviteCheckbox.on("change", () => that.updateInviteState());

		this.defaultFlowCheckbox.on("change", () => {
			saveToCookie("unranked-table-default-flow", that.defaultFlowCheckbox.prop("checked") ? "true" : "false");
		});

		this.createTableButton = $("#submit-unranked-table-button").button().click(this.submitTable(this));

		this.formatManager.registerFormatDropdownUpdate(this.formatDropdown);
		this.formatManager.registerTimerDropdownUpdate(this.timerDropdown);

		// Picking a deck switches the format to the deck's (when that format is offered in the hall).
		var followDeck = function () {
			if (that.syncing)
				return;
			var deck = that.deckSelector.getSelectedDeckInfo();
			if (deck == null)
				return;
			var formatCode = that.formatManager.lookupFormatByName(deck.targetFormat);
			if (formatCode == null || !TableFlow.hasOption(that.formatDropdown, formatCode))
				return;       // a deck of a format the hall does not offer leaves the format alone
			that.syncing = true;
			try {
				TableFlow.want(that.formatDropdown, formatCode);
				that.formatDropdown.trigger("change");
			} finally {
				that.syncing = false;
			}
		};
		this.deckSelector.playerDeckDropdown.on("change", followDeck);
		this.deckSelector.libraryDeckDropdown.on("change", followDeck);

		// Picking a format by hand brings back the deck last used in it, if any - unless the player picked the current
		// deck by hand: a Standard deck taken into an Expanded table must not be swapped for the last Expanded deck.
		this.formatDropdown.on("change", function (event) {
			that.showFormatInfo();
			if (that.syncing || !event.originalEvent)
				return;
			if (that.deckSelector.hasUserChoice())
				return;
			var remembered = that.deckSelector.readMemory()[that.formatDropdown.val()];
			if (remembered == null)
				return;
			that.syncing = true;
			try {
				that.deckSelector.select(remembered[0], remembered[1] == "library" ? "library" : "player");
			} finally {
				that.syncing = false;
			}
		});
		this.timerDropdown.on("change", () => that.showTimerInfo());

		this.updateInviteState();
	}

	hide() {
		this.mainDiv.hide();
	}

	show() {
		var format = TableFlow.cookie("unranked-table-last-format");
		TableFlow.want(this.formatDropdown, format);
		TableFlow.want(this.timerDropdown, TableFlow.cookie("unranked-table-last-timer") || "default");
		this.deckSelector.requestRestore(format);

		this.defaultFlowCheckbox.prop("checked", TableFlow.cookie("unranked-table-default-flow") === "true");

		this.showFormatInfo();
		this.showTimerInfo();
		this.mainDiv.show();
	}

	// ---- (i) texts that follow the selection ----

	showFormatInfo() {
		TableFlow.renderFormatInfo($("#help-unranked-format"), this.formatManager, this.formatDropdown.val());
	}

	showTimerInfo() {
		var target = $("#help-unranked-timer");
		var timer = this.formatManager.findTimer(this.timerDropdown.val());
		// the server's GameTimer.describeLimits(): "Each player has a total time bank of 45 minutes, and will time
		// out with a loss if they run out of their time bank or take longer than 6 minutes between actions."
		var base = "Each player has a total time bank (the first number), and will time out with a loss if they run out of their time bank or take longer than the second number between actions.";
		target.text(timer && timer.description ? timer.description : base);
	}

	// ---- invite-only ----

	updateInviteState() {
		var invite = this.inviteCheckbox.prop("checked");
		this.invitePicker.enable(invite);
		this.descText.prop("disabled", invite);
		this.descText.attr("placeholder", invite ? "An invite-only table's description is the invited player's name" : "");
	}

	// Contract C1: invite-only, inviting playerName.
	setInvite(playerName) {
		var name = CreateUnrankedTable.bareName(playerName);
		this.inviteCheckbox.prop("checked", true);
		this.updateInviteState();
		this.invitePicker.val(name);       // offered in the picker too, even if they are not in the hall
	}

	// The chat user list sends bare names (contract C2), or the older "* name" / "+ name" for admins and league
	// admins.  Only that known prefix is removed; names may contain '-' and '_'.
	static bareName(name) {
		return String(name == null ? "" : name).replace(/^[*+] /, "").trim();
	}

	static sameName(a, b) {
		return String(a).toLowerCase() === String(b).toLowerCase();
	}

	updatePlayers(rawPlayers, currentPlayer) {
		this.currentPlayer = CreateUnrankedTable.bareName(currentPlayer);
		var me = this.currentPlayer;
		let players = (rawPlayers || []).map(CreateUnrankedTable.bareName)
			.filter((name) => name !== "" && !(me !== "" && CreateUnrankedTable.sameName(name, me)));

		this.invitePicker.setSelf(me);
		this.invitePicker.setPlayers(players);
	}

	// ---- submit ----

	submitTable(that) {
		return () => {

			var format = that.formatDropdown.val();

			if(format == null || format === "") {
				that.updateResult("You must select a format.");
				return;
			}

			var deck = that.deckSelector.getSelection();
			var tableDesc = that.descText.val();
			var timer = that.timerDropdown.val() || "default";
			var isPrivate = that.privateCheckbox.is(':checked');
			var isInviteOnly = that.inviteCheckbox.is(':checked');
			var keepOpen = that.keepopenCheckbox.is(':checked');

			if(isInviteOnly) {
				var invitee = CreateUnrankedTable.bareName(that.invitePicker.val());
				if(invitee === "") {
					that.updateResult("If set to invite-only, you must name a player to invite (all others will be disallowed from joining your table).");
					return;
				}
				if (that.currentPlayer !== "" && CreateUnrankedTable.sameName(invitee, that.currentPlayer)) {
					that.updateResult("You cannot invite yourself; name another player.");
					return;
				}

				tableDesc = invitee;
			}

			if(deck == null) {
				that.updateResult("You must select a deck: one of yours, or one from the Deck Library.");
				return;
			}

			that.deckSelector.remember(deck, format);
			saveToCookie("unranked-table-last-format", format);
			saveToCookie("unranked-table-last-timer", timer);

			var result = that.respond({
				title: "Create Table",
				quietSuccess: !keepOpen,
				onSuccess: function () {
					if (keepOpen) {
						that.showResult("Table created. It waits in Waiting Tables until an opponent joins.", "success");
						return true;
					}
					that.mainHall.tableCreator.closeAfterSuccess();
				}
			});
			that.comm.createTable(format, deck.name, timer, tableDesc, isPrivate, isInviteOnly,
					result.callback, result.errorMap, deck.source);
		};
	}
}
