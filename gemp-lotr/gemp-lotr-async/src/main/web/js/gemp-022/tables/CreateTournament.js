/**
 * Play ▸ Create Tournament: a player-made tournament (constructed, sealed, live draft or solo draft) that others
 * sign up for; its creator is signed up with it.
 *
 * The form is reset each time the Play popup opens.  Handlers are bound once.  The player-count rule only acts when
 * the count crosses the 2-player line: at 2 or fewer there is no ready check and no pairing choice; going back above
 * restores what the player had chosen (the page defaults: 1 minute ready check, Early Start on).
 * The constructed deck is remembered per format ("tournament-decks").
 */
class CreateTournament extends TableFlow {
	tournamentTypeDropdown = null;
	formatDropdown = null;

	draftInfoText = null;
	draftInfoRow = null;

	playerCountInput = null;
	pairingDropdown = null;
	deckbuildingDurationInput = null;
	durationRow = null;

	deckSelector = null;

	draftTimerDropdown = null;
	privateCheckbox    = null;
	earlyStartCheckbox = null;
	readyCheckDropdown = null;

	submitTournamentButton = null;

	formatsJson = null;
	smallTournament = false;     // the player count is 2 or fewer
	savedLargeSettings = null;   // {readyCheck, earlyStart} chosen while above 2 players
	advancedVisible = false;

	constructor(mainHall, comm, div, formatManager, deckManager) {
		super(mainHall, comm, div, formatManager, deckManager, $("#tournament-result"));
		var that = this;

		this.tournamentTypeDropdown = $("#tournament-type-select");
		this.formatDropdown = $("#tournament-format");

		this.draftInfoText = $("#tournament-draft-info");
		this.draftInfoRow = $("#tournament-draft-info-row");

		this.playerCountInput  = $("#tournament-players");
		this.pairingDropdown  = $("#tournament-pairing");
		this.deckbuildingDurationInput  = $("#tournament-deckbuilding");
		this.durationRow  = $("#tournament-duration-row");

		this.deckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager, "tournament");

		this.advancedToggle = $("#tournament-advanced-toggle").button();
		this.advancedDiv = $("#tournament-advanced-fields");

		this.privateCheckbox  = $("#tournament-private");
		this.earlyStartCheckbox  = $("#tournament-early-start");
		this.readyCheckDropdown  = $("#tournament-ready-check");
		this.draftTimerDropdown  = $("#tournament-draft-timer");
		this.draftTimerRow  = $("#tournament-timer-row");

		this.submitTournamentButton = $("#submit-tournament-button").button().click(() => {
			if(that.validateTournamentForm(that)) {
				that.createTournament(that);
			}
		});

		// ---- handlers, bound once ----
		this.advancedToggle.on("click", function () {
			that.setAdvancedVisible(!that.advancedVisible);
		});
		this.tournamentTypeDropdown.on("change", () => that.typeChanged());
		this.formatDropdown.on("change", () => that.formatChanged());
		this.pairingDropdown.on("change", () => that.validateTournamentForm(that));
		this.deckbuildingDurationInput.on("input", () => that.validateTournamentForm(that));
		this.playerCountInput.on("input", () => that.playerCountChanged());

		// asked for once the hall goes live (not for a visitor reading Help / Server Info: GempHallSession)
		var loadFormats = function () {
			that.comm.getTournamentAvailableFormats(function(json)
			{
				that.setupTournamentSpawner(json);
			}, TableFlow.errorMap(function (message) {
				that.showResult("Could not load the tournament formats: " + message, "error");
			}));
		};
		if (mainHall && typeof mainHall.whenStarted == "function")
			mainHall.whenStarted(loadFormats);
		else
			loadFormats();

		this.resetForm();
	}

	hide() {
		this.mainDiv.hide();
	}

	show() {
		this.mainDiv.show();
	}

	setAdvancedVisible(visible) {
		this.advancedVisible = visible;
		this.advancedDiv.toggle(visible);
		this.advancedToggle.button("option", "label", visible ? "Hide Advanced Settings" : "Show Advanced Settings");
	}

	// Back to the page's defaults (each Play open).
	resetForm() {
		this.tournamentTypeDropdown.val("");
		$("#tournament-startup").hide();
		this.mainDiv.find(".info-text").prop("hidden", true);
		this.mainDiv.find(".info-toggle").attr("aria-expanded", "false");
		this.playerCountInput.val("4");
		this.pairingDropdown.val("SWISS").prop("disabled", false);
		this.readyCheckDropdown.val("60").prop("disabled", false);
		this.earlyStartCheckbox.prop("checked", true);
		this.privateCheckbox.prop("checked", false);
		this.smallTournament = false;
		this.savedLargeSettings = null;
		this.setAdvancedVisible(false);
		this.resetResult();
	}

	setupTournamentSpawner(json) {
		this.formatsJson = json;
		this.draftTimerDropdown.empty();
		for (const timerType of (json.draftTimerTypes || [])) {
			var option = $("<option>")
				.val(timerType)
				.text(timerType.replace("_", " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase()));
			this.draftTimerDropdown.append(option);
		}
		// a type chosen before the formats arrived
		if (this.tournamentTypeDropdown.val())
			this.typeChanged();
	}

	typeChanged() {
		var that = this;
		var gameType = String(this.tournamentTypeDropdown.val() || "");
		if (gameType === "")
			return;

		$("#tournament-startup").show();
		this.mainDiv.find(".info-text").prop("hidden", true);
		this.mainDiv.find(".info-toggle").attr("aria-expanded", "false");

		//Hiding of controls which don't apply to every tournament type
		this.mainDiv.find(".conditional-display").hide();

		//Revealing of just the conditional controls which apply to the current type
		switch(gameType) {
			case "constructed":
				this.deckSelector.mainDiv.show();
				break;
			case "sealed":
				this.durationRow.show();
				this.deckbuildingDurationInput.val("30");
				break;
			case "table_draft":
				this.draftInfoRow.show();
				this.durationRow.show();
				this.deckbuildingDurationInput.val("15");
				this.draftTimerRow.show();
				break;
			case "solodraft":
				this.durationRow.show();
				this.deckbuildingDurationInput.val("30");
				break;
		}

		var json = this.formatsJson || {};
		const formatListMap = {
			constructed: json.constructed,
			sealed: json.sealed,
			solodraft: json.soloDrafts,
			table_draft: json.tableDrafts
		};
		const formats = formatListMap[gameType] || [];

		this.formatDropdown.empty();
		for (const format of formats) {
			const option = $("<option>")
				.val(format.code)
				.attr("data-maxplayers", format.maxPlayers)
				.attr("data-recommendedtimer", format.recommendedTimer)
				.text(format.name);
			that.formatDropdown.append(option);
		}
		this.formatDropdown.change();
		this.validateTournamentForm(this);
	}

	formatChanged() {
		var gameType = String(this.tournamentTypeDropdown.val() || "");
		if(gameType === "table_draft") {
			this.draftInfoText
				.attr("draftCode", this.formatDropdown.val())
				.text($("option:selected", this.formatDropdown).text())
				.addClass("draftFormatInfo");
			this.draftInfoRow.show();
		}
		else {
			this.draftInfoRow.hide();
		}

		// Modify the max number of players based on format selected (if needed)
		const selectedFormat = this.formatDropdown.find("option:selected");
		const maxPlayers = selectedFormat.data("maxplayers");
		const recommendedTimer = selectedFormat.data("recommendedtimer");

		if (maxPlayers) {
			this.playerCountInput.attr("max", maxPlayers);
			const currentVal = parseInt(this.playerCountInput.val());
			if (currentVal > maxPlayers) {
				this.playerCountInput.val(maxPlayers);
				this.playerCountChanged();
			}
		} else {
			this.playerCountInput.removeAttr("max");
		}

		if (recommendedTimer) {
			this.draftTimerDropdown.val(recommendedTimer);
		}

		var formatInfo = $("#help-tournament-format");
		if (gameType === "constructed") {
			TableFlow.renderFormatInfo(formatInfo, this.formatManager, this.formatDropdown.val());
			this.deckSelector.restoreUnlessChosen(this.formatDropdown.val());
		} else {
			formatInfo.empty()
				.append($("<div class='format-intro'></div>").text(TableFlow.FORMAT_INTRO))
				.append($("<div></div>").text(gameType === "sealed"
					? "Everyone receives the same sealed product for this format and builds a deck from it."
					: "The set of packs drafted from; the deck is built from the cards drafted."));
		}

		this.validateTournamentForm(this);
	}

	// Only crossing the 2-player line changes the other settings.
	playerCountChanged() {
		const playerCount = parseInt(this.playerCountInput.val());
		const small = !isNaN(playerCount) && playerCount <= 2;

		if (small && !this.smallTournament) {
			this.savedLargeSettings = {
				readyCheck: this.readyCheckDropdown.val(),
				earlyStart: this.earlyStartCheckbox.prop("checked")
			};
			// Set to -1 (no ready check) and disable other options
			this.readyCheckDropdown.val("-1").prop("disabled", true);
			this.earlyStartCheckbox.prop("checked", false);
			this.pairingDropdown.prop("disabled", true);
		} else if (!small && this.smallTournament) {
			var saved = this.savedLargeSettings || {readyCheck: "60", earlyStart: true};
			this.readyCheckDropdown.prop("disabled", false).val(saved.readyCheck);
			this.earlyStartCheckbox.prop("checked", saved.earlyStart);
			this.pairingDropdown.prop("disabled", false);
		}
		this.smallTournament = small;

		this.validateTournamentForm(this);
	}

	validateTournamentForm(that) {
		that = that || this;
		const gameType = that.tournamentTypeDropdown.val();
		const format = that.formatDropdown.val();
		const pairingType = that.pairingDropdown.val();

		const allValid =
			gameType &&
			format &&
			pairingType;

		if (allValid) {
			that.submitTournamentButton.prop("disabled", false);
			that.submitTournamentButton.removeClass("ui-state-disabled");
			return true;
		} else {
			that.submitTournamentButton.prop("disabled", true);
			that.submitTournamentButton.addClass("ui-state-disabled");
			return false;
		}
	}

	createTournament(that) {
		const type = that.tournamentTypeDropdown.val();
		const deck = (type == "constructed") ? that.deckSelector.getSelection() : null;

		if(type == "constructed" && deck == null) {
			that.updateResult("You must select your deck before starting a Constructed tournament.");
			return;
		}

		// Depending on the type, pick the correct format code from the formatSelect dropdown
		const formatCode = that.formatDropdown.val();

		// Draft timer only applies to table draft
		const tableDraftTimer = that.draftTimerDropdown.val();

		const playoff = that.pairingDropdown.val();
		const competitive = that.privateCheckbox.is(':checked');
		const startableEarly = that.earlyStartCheckbox.is(':checked');
		const readyCheck = that.readyCheckDropdown.val();

		// Number input
		const deckbuildingDuration = parseInt(that.deckbuildingDurationInput.val());
		const maxPlayers = parseInt(that.playerCountInput.val());

		if (isNaN(maxPlayers) || maxPlayers < 1) {
			that.updateResult("Enter the number of players (1 or more).");
			return;
		}

		if (deck != null)
			that.deckSelector.remember(deck, formatCode);

		var formatName = that.formatDropdown.find("option:selected").text();
		var result = that.respond({
			title: "Create Tournament",
			onSuccess: function () {
				that.mainHall.setPendingTournament(true);
				that.mainHall.tableCreator.closeAfterSuccess();
				that.mainHall.showDialog("Tournament Created",
					"Your " + (competitive ? "Competitive" : "Casual") + " " + TableFlow.escapeHtml(formatName)
					+ " tournament is open, and you are signed up for it. It is listed in Waiting Tables, where other players"
					+ " can join it; it starts when " + maxPlayers + " players have joined"
					+ (startableEarly ? ", or earlier when you press its Start button." : "."), 260);
				return true;
			}
		});

		// Call the communication layer with all gathered values
		this.comm.createTournament(
			type,
			deck != null ? deck.name : "",
			maxPlayers,
			formatCode,
			tableDraftTimer,
			playoff,
			isNaN(deckbuildingDuration) ? "" : deckbuildingDuration,
			competitive,
			startableEarly,
			readyCheck,
			result.callback,
			result.errorMap,
			deck != null ? deck.source : null
		);
	}
}
