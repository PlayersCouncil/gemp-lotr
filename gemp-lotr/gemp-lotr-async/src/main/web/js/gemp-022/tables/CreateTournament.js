class CreateTournament {
	comm = null;
	// Should be the create-tournament-table div
	mainDiv = null;
	formatManager = null;
	deckManager = null;
	
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
	
	resultDiv = null;
	
	constructor(comm, div, formatManager, deckManager) {
		var that = this;
		
		this.comm = comm;
		this.mainDiv = div;
		this.formatManager = formatManager;
		this.deckManager = deckManager;
		
		this.tournamentTypeDropdown = $("#tournament-type-select");
		this.formatDropdown = $("#tournament-format");
		
		this.draftInfoText = $("#tournament-draft-info");
		this.draftInfoRow = $("#tournament-draft-info-row");
		
		this.playerCountInput  = $("#tournament-players");
		this.pairingDropdown  = $("#tournament-pairing");
		this.deckbuildingDurationInput  = $("#tournament-deckbuilding");
		this.durationRow  = $("#tournament-duration-row");
		
		this.deckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager);

		this.advancedToggle = $("#tournament-advanced-toggle").button();
		this.advancedDiv = $("#tournament-advanced-fields");
		
		this.privateCheckbox  = $("#tournament-private");
		this.earlyStartCheckbox  = $("#tournament-early-start");
		this.readyCheckDropdown  = $("#tournament-ready-check");
		this.draftTimerDropdown  = $("#tournament-draft-timer");
		this.draftTimerRow  = $("#tournament-timer-row");
		
		this.resultDiv = $("#tournament-result");
		
		this.submitTournamentButton = $("#submit-tournament-button").button().click(() => {
			if(that.validateTournamentForm(that)) {
				that.createTournament(that);
			}
		});
		
		this.comm.getTournamentAvailableFormats(function(json)
		{
			that.setupTournamentSpawner(json);
		});
	}
	
	hide() {
		this.mainDiv.hide();
	}
	
	show() {
		this.mainDiv.show();
	}
	
	updateResult(text) {
		this.resultDiv.html(text);
	}
	
	createTournament(that) {
		const deck = that.deckSelector.getSelectedDeck();
		
		

		const type = that.tournamentTypeDropdown.val();
		
		if(type == "constructed" && (deck == null || deck === "")) {
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

		// Call the communication layer with all gathered values
		this.comm.createTournament(
			type,
			deck,
			maxPlayers,
			formatCode,
			tableDraftTimer,
			playoff,
			deckbuildingDuration,
			competitive,
			startableEarly,
			readyCheck,
			CreateTable.getResponse(that.resultDiv),
			CreateTable.getCreateErrorMap(that.resultDiv)
		);
	}
	
	
	validateTournamentForm(that) {
		const gameType = that.tournamentTypeDropdown.val();
		const format = that.formatDropdown.val();
		const pairingType = that.pairingDropdown.val();
		const playerCount = parseInt(that.playerCountInput.val());
		const deckDuration = parseInt(that.deckbuildingDurationInput.val());

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
	
	setupTournamentSpawner(json) {
		var that = this;

		that.tournamentTypeDropdown.on("change", function () {
			// One-time reveal of controls hidden on startup
			$("#tournament-startup").show();
			
			// Advanced settings toggle
			const advancedDiv = that.advancedDiv.hide();
			const toggleAdvanced = that.advancedToggle.show().text("Show Advanced Settings");
			let advancedVisible = false;
			toggleAdvanced.off("click").on("click", function () {
				advancedVisible = !advancedVisible;
				advancedDiv.toggle(advancedVisible);
				$(this).text(advancedVisible ? "Hide Advanced Settings" : "Show Advanced Settings");
			});


			const gameType = $(this).val();
			const formatListMap = {
				constructed: json.constructed,
				sealed: json.sealed,
				solodraft: json.soloDrafts,
				table_draft: json.tableDrafts
			};
			
			//Hiding of controls which don't apply to every tournament type
			that.mainDiv.find(".conditional-display").hide();
			
			//Revealing of just the conditional controls which apply to the current type
			switch(String(gameType)) {
				case "constructed":
					that.deckSelector.mainDiv.show();
					break;
				case "sealed":
					that.durationRow.show();
					that.deckbuildingDurationInput.val("30");
					break;
				case "table_draft":
					that.draftInfoRow.show();
					that.durationRow.show();
					that.deckbuildingDurationInput.val("15");
					that.draftTimerRow.show();
					break;
				case "solodraft":
					that.durationRow.show();
					that.deckbuildingDurationInput.val("30");
					break;
			}

			const formats = formatListMap[gameType] || [];

			if (formats.length === 0) 
				return;


			that.formatDropdown.empty();
			// Fill format options
			for (const format of formats) {
				const option = $("<option>")
					.val(format.code)
					.attr("data-maxplayers", format.maxPlayers)
					.attr("data-recommendedtimer", format.recommendedTimer)
					.text(format.name);

				that.formatDropdown.append(option);
			}
			
			that.draftTimerDropdown.empty();
			for (const timerType of json.draftTimerTypes) {
				var option = $("<option>")
					.val(timerType)
					.text(timerType.replace("_", " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase()))
					
				that.draftTimerDropdown.append(option);
			}
			
			that.formatDropdown.on("change", function () {
					if(gameType === "table_draft") {
						that.draftInfoText
							.attr("draftCode", that.formatDropdown.val())
							.text(that.formatDropdown.text());
							
						that.draftInfoRow.show();
						that.deckbuildingDurationInput.val("15");					
					}
					else {
						that.draftInfoRow.hide();
						that.deckbuildingDurationInput.val("30");
					}
					
					// Modify the max number of players based on format selected (if needed)
					const selectedFormat = that.formatDropdown.find("option:selected");
					const maxPlayers = selectedFormat.data("maxplayers");
					const recommendedTimer = selectedFormat.data("recommendedtimer");

					if (maxPlayers) {
						that.playerCountInput.attr("max", maxPlayers);
						const currentVal = parseInt(that.playerCountInput.val());
						if (currentVal > maxPlayers) {
							that.playerCountInput.val(maxPlayers);
						}
					} else {
						that.playerCountInput.removeAttr("max");
					}
					
					if (recommendedTimer) {
						that.draftTimerDropdown.val(recommendedTimer);
					}
					
					that.validateTournamentForm(that);
				});

			// Add validating listeners
			that.pairingDropdown.on("change", that.validateTournamentForm);
			that.deckbuildingDurationInput.on("input", that.validateTournamentForm);
			
			// Add listener for ready check / player count combo
			that.playerCountInput.on("input", function () {
				const playerCount = parseInt($(this).val());

				if (playerCount <= 2) {
					// Set to -1 (no ready check) and disable other options
					that.readyCheckDropdown.val("-1");
					that.readyCheckDropdown.prop("disabled", true);
				} else {
					// Enable all options and select 90 seconds
					that.readyCheckDropdown.prop("disabled", false);
					that.readyCheckDropdown.val("120");
				}

				that.validateTournamentForm(that);
			});
			
			that.validateTournamentForm(that);
		});
	}
}