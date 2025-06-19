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

		this.comm.getPlayerInfo(function(json)
				{
					that.userInfo = json;
				});

		this.comm.getTournamentAvailableFormats(function(json)
				{
                    that.setupTournamentSpawner(json);
				});

		this.chat = chat;
		this.chat.tournamentCallback = function(from, message) {
			var thisName = that.userInfo.name
			if (from == "TournamentSystem" && that.inTournament) {
				that.showDialog("Tournament Update", message, 320);
			} else if (from.startsWith("TournamentSystemTo:")) {
				// Extract and split the user list
				let users = from.split(":")[1].split(";");

				// Check if thisName is in the list
				if (users.includes(thisName)) {
					that.showDialog("Tournament Update", message, 320);
				}
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
			hallSettingsStr = "1|1|0|0|0|0|0|0";
		var hallSettings = hallSettingsStr.split("|");

		this.initTable(hallSettings[0] == "1", "waitingTablesHeader", "waitingTablesContent");
		this.initTable(hallSettings[1] == "1", "playingTablesHeader", "playingTablesContent");
		this.initTable(hallSettings[2] == "1", "finishedTablesHeader", "finishedTablesContent");
		// Those 3 indexes are applied dynamically if there is some data for those sections
//		this.initTable(hallSettings[3] == "1", "wcQueuesHeader", "wcQueuesContent");
//		this.initTable(hallSettings[4] == "1", "wcEventsHeader", "wcEventsContent");
//		this.initTable(hallSettings[5] == "1", "scheduledQueuesHeader", "scheduledQueuesContent");
		this.initTable(hallSettings[6] == "1", "recurringQueuesHeader", "recurringQueuesContent");
		this.initTable(hallSettings[7] == "1", "queueSpawnerHeader", "queueSpawnerContent");

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
				
				if(format == null || format === "") {
					that.showErrorDialog("Table creation error", "You must select a format", false, false);
					return;
				}
				
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

	addWcQueuesSection: function() {
        if ($("#wcQueuesHeader").length) return;

        const $header = $("<div>")
            .attr("id", "wcQueuesHeader")
            .addClass("eventHeader wc-queues")
            .html(`World Championship Queues<span class='count'>(0)</span>`);

        const $content = $("<div>")
            .attr("id", "wcQueuesContent")
            .addClass("visibilityToggle")
            .append(
                $("<table>").addClass("tables wc-queues").append(`
                    <tr>
                        <th width='10%'>Format</th>
                        <th width='8%'>Collection</th>
                        <th width='30%'>Event Name</th>
                        <th width='16%'>Starts</th>
                        <th width='16%'>System</th>
                        <th width='8%'>Cost</th>
                        <th width='12%'>Prizes</th>
                    </tr>
                `)
            );

        this.insertEventSection($header, $content, "wcQueues");
    },

    addWcEventsSection: function() {
        if ($("#wcEventsHeader").length) return;

        const $header = $("<div>")
            .attr("id", "wcEventsHeader")
            .addClass("eventHeader wc-events")
            .html(`World Championship Events<span class='count'>(0)</span>`);

        const $content = $("<div>")
            .attr("id", "wcEventsContent")
            .addClass("visibilityToggle")
            .append(
                $("<table>").addClass("tables wc-events").append(`
                    <tr>
                        <th width='10%'>Format</th>
                        <th width='8%'>Collection</th>
                        <th width='20%'>Event Name</th>
                        <th width='10%'>System</th>
                        <th width='16%'>Stage</th>
                        <th width='6%'>Round</th>
                        <th width='8%'>Players</th>
                    </tr>
                `)
            );

        this.insertEventSection($header, $content, "wcEvents");
    },

    addScheduledQueuesSection: function() {
        if ($("#scheduledQueuesHeader").length) return;

        const $header = $("<div>")
            .attr("id", "scheduledQueuesHeader")
            .addClass("eventHeader scheduledQueues")
            .html(`Scheduled Events<span class='count'>(0)</span>`);

        const $content = $("<div>")
            .attr("id", "scheduledQueuesContent")
            .addClass("visibilityToggle")
            .append(
                $("<table>").addClass("tables scheduledQueues").append(`
                    <tr>
                        <th width='10%'>Format</th>
                        <th width='8%'>Collection</th>
                        <th width='30%'>Event Name</th>
                        <th width='16%'>Starts</th>
                        <th width='16%'>System</th>
                        <th width='8%'>Cost</th>
                        <th width='12%'>Prizes</th>
                    </tr>
                `)
            );

        this.insertEventSection($header, $content, "scheduledQueues");
    },

    insertEventSection: function($header, $content, sectionKey) {
        const sectionOrder = [
			"wcQueues",
			"wcEvents",
			"scheduledQueues"
		];

		const sectionIds = {
			wcQueues: "wcQueuesContent",
			wcEvents: "wcEventsContent",
			scheduledQueues: "scheduledQueuesContent"
		};

		const index = sectionOrder.indexOf(sectionKey);
		if (index === -1) {
			console.error("Unknown section key:", sectionKey);
			return;
		}

		// Look ahead for any section that should come after this one
		for (let i = index + 1; i < sectionOrder.length; i++) {
			const $nextContent = $("#" + sectionIds[sectionOrder[i]]);
			if ($nextContent.length) {
				const $nextHeader = $("#" + sectionOrder[i] + "Header");
				$header.insertBefore($nextHeader);
				$content.insertBefore($nextHeader);
				return;
			}
		}

		// Fallback insert
		const $anchor = $("#recurringQueuesHeader");
		$header.insertBefore($anchor);
		$content.insertBefore($anchor);

		// Load settings
		let hallSettingsStr = $.cookie("hallSettings") || "1|1|0|0|0|0|0";
		const hallSettings = hallSettingsStr.split("|");

		// Determine which section it is
		const headerId = $header.attr("id");
		const contentId = $content.attr("id");

		let settingIndex = null;

		if (headerId === "wcQueuesHeader" && contentId === "wcQueuesContent") {
			settingIndex = 3;
		} else if (headerId === "wcEventsHeader" && contentId === "wcEventsContent") {
			settingIndex = 4;
		} else if (headerId === "scheduledQueuesHeader" && contentId === "scheduledQueuesContent") {
			settingIndex = 5;
		}

		if (settingIndex !== null) {
			this.initTable(hallSettings[settingIndex] === "1", headerId, contentId);
		}
    },

    removeWcQueuesSection: function() {
        $("#wcQueuesHeader").remove();
        $("#wcQueuesContent").remove();
    },

    removeWcEventsSection: function() {
        $("#wcEventsHeader").remove();
        $("#wcEventsContent").remove();
    },

    removeScheduledQueuesSection: function() {
        $("#scheduledQueuesHeader").remove();
        $("#scheduledQueuesContent").remove();
    },

	setupTournamentSpawner: function(json) {
		var that = this;


        function validateTournamentForm() {
            const gameType = $("#gameTypeSelect").val();
            const format = $("#formatSelect").val();
            const pairingType = $("#pairingType").val();
            const playerCount = parseInt($("#numPlayers").val(), 10);
            const deckDuration = parseInt($("#deckDuration").val(), 10);
            const draftTimer = $("#draftTimer").val();
            const competitive = $("#competitiveSelect").val();
            const startableEarly = $("#startableEarlySelect").val();
            const readyCheck = $("#readyCheckSelect").val();

            const formatMaxPlayers = $("#formatSelect").find("option:selected").data("maxplayers");
            var validMaxPlayers = true;
            if (formatMaxPlayers) {
                validMaxPlayers = playerCount <= formatMaxPlayers;
            }

            const validPlayerCount = Number.isInteger(playerCount) && playerCount >= 1 && validMaxPlayers;
            const validDeckDuration = (Number.isInteger(deckDuration) && deckDuration >= 5) || gameType === "constructed";
            const draftValid = gameType !== "table_draft" || draftTimer;
            const validPlayerReadyCombo = playerCount > 2 || readyCheck < 0;

            const allValid =
                gameType &&
                format &&
                pairingType &&
                validPlayerCount &&
                validDeckDuration &&
                competitive &&
                startableEarly &&
                readyCheck &&
                validPlayerReadyCombo &&
                draftValid;

            if (allValid) {
                $("#createTournamentButton").prop("disabled", false);
                $("#createTournamentButton").removeClass("ui-state-disabled");
            } else {
                $("#createTournamentButton").prop("disabled", true);
                $("#createTournamentButton").addClass("ui-state-disabled");
            }
        };

        $("#gameTypeSelect").on("change", function () {
            // Advanced settings toggle
            const $advanced = $("#advancedFields").empty();
            const $toggleAdvanced = $("#toggleAdvancedFields").show().text("Show advanced settings");
            let advancedVisible = false;
            $toggleAdvanced.off("click").on("click", function () {
                advancedVisible = !advancedVisible;
                $advanced.toggle(advancedVisible);
                if (advancedVisible) {
                    // Scroll to bottom so that the new field can be seen
                    $("#queueSpawnerForm").get(0).scrollIntoView({ behavior: "smooth", block: "end" });
                }
                $(this).text(advancedVisible ? "Hide advanced settings" : "Show advanced settings");
            });


            const gameType = $(this).val();
            const formatListMap = {
                constructed: json.constructed,
                sealed: json.sealed,
                solodraft: json.soloDrafts,
                table_draft: json.tableDrafts
            };

            const formats = formatListMap[gameType] || [];

            const $fields = $("#dynamicFields");
            $fields.empty(); // clear previous dynamic fields
            if (formats.length === 0) return;

            // Create Format label and select
            const $formatRow = $("<div>").addClass("formRow");
            const $label = $("<label>").attr("for", "formatSelect").text("Format:");
            const $select = $("<select>")
              .attr("id", "formatSelect")
              .attr("name", "format")
              .append($("<option disabled selected>").text("Select format"));
            $formatRow.append($label, $select);

            // Fill format options
            for (const format of formats) {
                const $option = $("<option>")
                    .val(format.code)
                    .text(format.name);

                // For live draft, save the table capacity
                if (gameType === "table_draft" && format.maxPlayers) {
                    $option.attr("data-maxplayers", format.maxPlayers);
                }
                // And recommended timer
                if (gameType === "table_draft" && format.recommendedTimer) {
                    $option.attr("data-recommendedtimer", format.recommendedTimer);
                }

                $select.append($option);
            }

			// Draft info link
            const $draftInfoRow = $("<div>").addClass("formRow");
            const $draftInfoLabel = $("<label>").text("Draft Format Info:");
            const $draftInfoLink = $("<label>").addClass("italic").text("Select format first");
            if (gameType === "table_draft") {
                $select.on("change", function () {
					// Create and append new info row
					$draftInfoLink
						.removeClass("italic")
						.addClass("draftFormatInfo")
						.attr("draftCode", $("#formatSelect").val())
						.text($(this).find("option:selected").text());
            	});
            }
            $draftInfoRow.append($draftInfoLabel, $draftInfoLink);

            // Number of players
            const $playersRow = $("<div>").addClass("formRow");
            const $playersLabel = $("<label>").attr("for", "numPlayers").text("Number of players:");
            const $playersInput = $("<input>")
                .attr({ type: "number", id: "numPlayers", name: "numPlayers", min: 1 })
                .val(4);
            $playersRow.append($playersLabel, $playersInput);

            // Modify the max number of players based on format selected (if needed)
            $select.on("change", function () {
                const selectedFormat = $(this).find("option:selected");
                const maxPlayers = selectedFormat.data("maxplayers");

                if (maxPlayers) {
                    $playersInput.attr("max", maxPlayers);
                    const currentVal = parseInt($playersInput.val(), 10);
                    if (currentVal > maxPlayers) {
                        $playersInput.val(maxPlayers);
                    }
                } else {
                    $playersInput.removeAttr("max");
                }
            });

            // Pairing type
            const $pairingRow = $("<div>").addClass("formRow");
            const $pairingLabel = $("<label>").attr("for", "pairingType").text("Pairing type:");
            const $pairingSelect = $("<select>")
                .attr({ id: "pairingType", name: "pairingType" })
                .append($("<option>").val("SWISS").text("Swiss - losers play next rounds too"))
                .append($("<option>").val("SINGLE_ELIMINATION").text("Single elimination"));
            $pairingRow.append($pairingLabel, $pairingSelect);

            // Deck-building duration
            const $durationRow = $("<div>").addClass("formRow");
            const $durationLabel = $("<label>").attr("for", "deckDuration").text("Deck-building duration (minutes, minimum 5):");
            const $durationInput = $("<input>")
                .attr({ type: "number", id: "deckDuration", name: "deckDuration", min: 5 });
            if (gameType === "table_draft") {
                $durationInput.val("15");
            } else {
                $durationInput.val("30");
            }
            $durationRow.append($durationLabel, $durationInput);

            // Competitive
            const $competitiveRow = $("<div>").addClass("formRow");
            const $competitiveLabel = $("<label>").attr("for", "competitiveSelect").text("Competitive:");
            const $competitiveSelect = $("<select>")
                .attr({ id: "competitiveSelect", name: "competitive" })
                .append($("<option>").val("false").text("Games can be spectated"))
                .append($("<option>").val("true").text("No spectate; hidden names in queue"));
            $competitiveRow.append($competitiveLabel, $competitiveSelect);
            if (gameType === "constructed") {
            	$competitiveSelect.val("true");
            }

            // Early start
            const $earlyStartRow = $("<div>").addClass("formRow");
            const $earlyStartLabel = $("<label>").attr("for", "startableEarlySelect").text("Early start:");
            const $earlyStartSelect = $("<select>")
                .attr({ id: "startableEarlySelect", name: "startableEarly" })
                .append($("<option>").val("true").text("Can be started with less players"))
                .append($("<option>").val("false").text("Starts only with all player slots filled"));
            $earlyStartRow.append($earlyStartLabel, $earlyStartSelect);
            // Set default to false for constructed games
            if (gameType === "constructed") {
            	$earlyStartSelect.val("false");
            }

            // Ready check
            const $readyCheckRow = $("<div>").addClass("formRow");
            const $readyCheckLabel = $("<label>").attr("for", "readyCheckSelect").text("Ready check:");
            const $readyCheckSelect = $("<select>")
                .attr({ id: "readyCheckSelect", name: "readyCheck" })
                .append($("<option>").val("-1").text("No, just start the tournament"))
                .append($("<option>").val("60").text("1 minute to confirm"))
                .append($("<option>").val("90").text("1 minute 30 seconds to confirm"))
                .append($("<option>").val("180").text("3 minutes to confirm"));
            $readyCheckRow.append($readyCheckLabel, $readyCheckSelect);
            // Set default to 90 seconds as we start with 4 players
            $readyCheckSelect.val("90");

            // Append to form
            $fields.append($("<div>").append($formatRow));
            if (gameType === "table_draft") {
            	$fields.append($("<div>").append($draftInfoRow));
            }
            $fields.append($("<div>").append($playersRow));

            $advanced.append($("<div>").append($pairingRow));
            if (gameType !== "constructed") {
            	$advanced.append($("<div>").append($durationRow));
            }
            $advanced.append(
                $("<div>").append($competitiveRow),
                $("<div>").append($earlyStartRow),
                $("<div>").append($readyCheckRow)
            );

            // Add live draft timer
            const $timerRow = $("<div>").addClass("formRow");
            const $timerLabel = $("<label>").attr("for", "draftTimer").text("Draft timer:");
            const $timerSelect = $("<select>")
                .attr({ id: "draftTimer", name: "draftTimer" })
                .append($("<option disabled selected>").text("Select timer"));
            $timerRow.append($timerLabel, $timerSelect);
            if (gameType === "table_draft") {
                for (const timerType of json.draftTimerTypes) {
                    $timerSelect.append(
                        $("<option>").val(timerType).text(timerType.replace("_", " ").toLowerCase().replace(/\b\w/g, c => c.toUpperCase()))
                    );
                }

                $advanced.append(
                    $("<div>").append($timerRow)
                );

                // Modify the timer based on format selected
                $select.on("change", function () {
                    const selectedFormat = $(this).find("option:selected");
                    const recommendedTimer = selectedFormat.data("recommendedtimer");

                    if (recommendedTimer) {
                        $timerSelect.val(recommendedTimer);
                    }
                });
            }

            // Add validating listeners
            $select.on("change", validateTournamentForm);
            $pairingSelect.on("change", validateTournamentForm);
            $durationInput.on("input", validateTournamentForm);
            $competitiveSelect.on("change", validateTournamentForm);
            $earlyStartSelect.on("change", validateTournamentForm);
            $readyCheckSelect.on("change", validateTournamentForm);
            if (gameType === "table_draft") {
                $timerSelect.on("change", validateTournamentForm);
            }

            // Add listener for ready check / player count combo
            $playersInput.on("input", function () {
                const playerCount = parseInt($(this).val(), 10);
                const $readyCheck = $("#readyCheckSelect");

                if (playerCount <= 2) {
                    // Set to -1 (no ready check) and disable other options
                    $readyCheck.val("-1");
                    $readyCheck.find("option").each(function () {
                        if ($(this).val() !== "-1") {
                            $(this).prop("disabled", true);
                        }
                    });
                } else {
                    // Enable all options and select 90 seconds
                    $readyCheck.find("option").prop("disabled", false);
                    $readyCheck.val("90");
                }

                validateTournamentForm();
            });

            // Submit button
            $('#createTournamentButton').show();
            $('#createTournamentButton').off('click').on('click', () => {
                that.createTournament();
            });
            validateTournamentForm();

            // Scroll to bottom so that the new field can be seen
            $("#queueSpawnerForm").get(0).scrollIntoView({ behavior: "smooth", block: "end" });
        });
	},

	createTournament: function() {
		var that = this;

		const deck = this.decksSelect.val();

		const type = $("#gameTypeSelect").val();
		// Depending on the type, pick the correct format code from the formatSelect dropdown
		const formatCode = $("#formatSelect").val();

		// Assign the format codes based on the selected game type
		let constructedFormatCode = null;
		let sealedFormatCode = null;
		let soloDraftFormatCode = null;
		let tableDraftFormatCode = null;

		if (type === "constructed") {
        	constructedFormatCode = formatCode;
        } else if (type === "sealed") {
			sealedFormatCode = formatCode;
		} else if (type === "solodraft") {
			soloDraftFormatCode = formatCode;
		} else if (type === "table_draft") {
			tableDraftFormatCode = formatCode;
		}

		// Draft timer only applies to table draft
		const tableDraftTimer = type === "table_draft" ? $("#draftTimer").val() : null;

		const playoff = $("#pairingType").val();
		const competitive = $("#competitiveSelect").val();
		const startableEarly = $("#startableEarlySelect").val();
		const readyCheck = $("#readyCheckSelect").val();


		// Number input
		const deckbuildingDuration = parseInt($("#deckDuration").val(), 10) || 15;
		const maxPlayers = parseInt($("#numPlayers").val(), 10) || 4;

		// Call the communication layer with all gathered values
		this.comm.createTournament(
			type,
			deck,
			maxPlayers,
			constructedFormatCode,
			sealedFormatCode,
			soloDraftFormatCode,
			tableDraftFormatCode,
			tableDraftTimer,
			playoff,
			deckbuildingDuration,
			competitive,
			startableEarly,
			readyCheck,
			function(json) {
				// Success callback
				that.showDialog("Tournament Update", "Tournament queue created successfully");
			},
			this.hallErrorMap()
		);
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
		const ids = [
			"waitingTablesContent",
			"playingTablesContent",
			"finishedTablesContent",
			"wcQueuesContent",
			"wcEventsContent",
			"scheduledQueuesContent",
			"recurringQueuesContent",
			"queueSpawnerContent"
		];

		// Load current cookie (or use default if missing)
		let hallSettingsStr = $.cookie("hallSettings") || "1|1|0|0|0|0|0|0";
		let currentSettings = hallSettingsStr.split("|");

		// Update only if the element exists
		for (let i = 0; i < ids.length; i++) {
			const $el = $("#" + ids[i]);
			if ($el.length) {
				currentSettings[i] = $el.hasClass("hidden") ? "0" : "1";
			}
		}

		const newHallSettings = currentSettings.join("|");
		console.log("New settings: " + newHallSettings);
		$.cookie("hallSettings", newHallSettings, { expires: 365 });
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
			},
			"400":function() {
				that.showErrorDialog("Tournament creation error", "Something went wrong, check all tournament parameters. If creating a constructed tournament, make sure that your selected deck is valid.", false, false);
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
				var isWC = queue.getAttribute("wc") == "true";
				var isRecurring = queue.getAttribute("recurring") == "true";
				var isScheduled = queue.getAttribute("scheduled") == "true";
				var displayInWaitingTables = queue.getAttribute("displayInWaitingTables") == "true";
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
									if (type !== null)
										type = type.toLowerCase();
									var queueName = queueInfo.getAttribute("queue");
									var queueStart = queueInfo.getAttribute("start");
									that.comm.joinQueue(queueId, deck, function (xml) {
										var result = that.processResponse(xml);
										if(result) {
											that.inTournament = true;
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
											that.showDialog("Joined Tournament", message, 320);
										}
									}, that.hallErrorMap());
								};
							})(queue));
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
										}
									});
								}
							})(queue));
						actionsField.append(but);

						if (queue.getAttribute("startable") == "true") {
							var startBut = $("<button>Start Now</button>");
							$(startBut).button().click((
								function(queueInfo) {
									return function() {
										var queueId = queueInfo.getAttribute("id");
										that.comm.startQueue(queueId, function (xml) {
											var result = that.processResponse(xml);
										});
									}
								})(queue));
							actionsField.append(startBut);
						}

						if (+queue.getAttribute("readyCheckSecsRemaining") > -1) { // The '+' sign converts string to number
							if ($("button:contains('READY CHECK')").length === 0 && queue.getAttribute("confirmedReadyCheck") == "false") {
								that.showDialog("Ready Check", "Ready Check started for the <b>" + queue.getAttribute("queue")
									+ "</b> tournament.<br><br>To confirm you are present, click the Ready Check button in the Waiting Tables Section within the next "
									+ queue.getAttribute("readyCheckSecsRemaining") + " seconds.", 230);
							}
							var checkBut = $("<button>READY CHECK - " + queue.getAttribute("readyCheckSecsRemaining") + " s</button>");
							$(checkBut).button().click((
								function(queueInfo) {
									return function() {
										var queueId = queueInfo.getAttribute("id");
										that.comm.confirmReadyCheckQueue(queueId, function (xml) {
											var result = that.processResponse(xml);
										});
									}
								})(queue));
							actionsField.append(checkBut);
							if (queue.getAttribute("confirmedReadyCheck") == "true") {
								$(checkBut).prop("disabled", true).text("Waiting for others - " + queue.getAttribute("readyCheckSecsRemaining") + " s");
							}
						}
					}

					var prizesBut = $("<button>Show Prizes</button>");
					$(prizesBut).button().click((
						function(queueInfo) {
							return function() {
								var infoDialog = $("<div></div>")
                                				.dialog({
                                					autoOpen:false,
                                					closeOnEscape:true,
                                					resizable:false,
                                					title:"Prizes details",
                                					closeText: ""
                                				});

								var prizeDescription = $(queueInfo.getAttribute("prizes")).attr("value");
								if (prizeDescription) {
									infoDialog.html(prizeDescription);
								} else {
									infoDialog.html("<p>No prize information available.</p>");
								}

								infoDialog.html(prizeDescription);

								infoDialog.dialog({width: 300, height: 150});
								infoDialog.dialog("open");
							}
						})(queue));
					actionsField.append(prizesBut);


					var type = queue.getAttribute("type");
					if(type !== null)
						type = type.toLowerCase();
					if(type === "sealed") {
						type = "Sealed";
					}
					else if (type === "solodraft") {
						type = "Solo Draft";
					}
					else if (type === "table_solodraft") {
						type = "Solo Table Draft";
					}
					else if (type === "table_draft") {
						type = "Table Draft";
					}
					else if (type === "constructed") {
						type = "Constructed";
					}

					var rowstr = "<tr class='queue" + id + "'><td>" + queue.getAttribute("format") + "</td>";
					var collectionTd = "<td>" + queue.getAttribute("collection") + "</td>";
					var queueNameTd = "<td>" + queue.getAttribute("queue") + "</td>";
					var startTd = "<td>" + queue.getAttribute("start") + "</td>";
					var systemTd = "<td>" + queue.getAttribute("system") + "</td>";
					var costPrizesTds = "<td align='right'>" + formatPrice(queue.getAttribute("cost")) + "</td><td>" + queue.getAttribute("prizes") + "</td>";
					if (type.includes("Sealed") || type.includes("Draft")) {
						collectionTd = "<td>" + type + "</td>";

						queueNameTd = "<td><div";
						if (type.includes("Table") && queue.hasAttribute("draftCode")) {
							queueNameTd += " class='draftFormatInfo' draftCode='"+ queue.getAttribute("draftCode") + "'";
						}
						queueNameTd += ">" + queue.getAttribute("queue") + "</div></td>";

					}
					rowstr += collectionTd +
						queueNameTd +
						startTd +
						systemTd +
						costPrizesTds +
						"</tr>";
						
					var row = $(rowstr);

					// Row for tournament queue waiting table
					var tablesRow = $("<tr class='table" + id + "'></tr>");
					tablesRow.append("<td>" + queue.getAttribute("format") + "</td>");
					let htmlTd = "<td>";
					if (isWC) {
						htmlTd += "World Championship";
					} else {
						// For system, ignore all after ',' (min players)
						htmlTd += queue.getAttribute("system").split(',')[0] + " Tournament";
					}
					htmlTd += " - " + type + " - <div style='display:inline'"
					if (type.includes("Table") && queue.hasAttribute("draftCode")) {
						htmlTd += " class='draftFormatInfo' draftCode='"+ queue.getAttribute("draftCode") + "'";
					}
					htmlTd += ">" + queue.getAttribute("queue") + "</div></td>";
					tablesRow.append(htmlTd);
					tablesRow.append("<td>" + queue.getAttribute("start") + "</td>");
					tablesRow.append("<td>" + queue.getAttribute("playerList") + "</td>");
					tablesRow.append(actionsField);
					if (joined == "true") {
						tablesRow.addClass("played");
					}
					if (isWC) {
						tablesRow.addClass("bold");
					}

					if (action == "add") {
						if(isWC) {
							that.addWcQueuesSection();
							$("table.wc-queues", this.tablesDiv)
							.append(row);
						} else if (isRecurring) {
							$("table.recurringQueues", this.tablesDiv)
							.append(row);
						} else if (isScheduled) {
							that.addScheduledQueuesSection();
							$("table.scheduledQueues", this.tablesDiv)
							.append(row);
						}
						// Display queues in waiting tables section
						if (displayInWaitingTables) {
							$("table.waitingTables", this.tablesDiv)
								.append(tablesRow);
						}
					} else if (action == "update") {
						$(".queue" + id, this.tablesDiv).replaceWith(row);
						// Display queues with waiting players also as waiting tables
						if (displayInWaitingTables) {
							var existingRow = $(".table" + id, this.tablesDiv);
							if (existingRow.length > 0) {
								// If the row exists, replace it
								existingRow.replaceWith(tablesRow);
							} else {
								// If the row does not exist, append it
								$("table.waitingTables", this.tablesDiv).append(tablesRow);
							}
						} else {
							// Remove tournaments displayed as tables
							$(".table" + id, this.tablesDiv).remove();
						}
					}

					this.animateRowUpdate(".queue" + id);
					
				} else if (action == "remove") {
					// Remove from both the Waiting Tables Section and Queue Sections
					$(".queue" + id, this.tablesDiv).remove();
					$(".table" + id, this.tablesDiv).remove();
				}
			}
			
			if ($('.wc-queues tr').length <= 1) {
				that.removeWcQueuesSection();
			}

			if ($('.scheduledQueues tr').length <= 1) {
				that.removeScheduledQueuesSection();
			}

			var tournaments = root.getElementsByTagName("tournament");
			for (var i = 0; i < tournaments.length; i++) {
				var tournament = tournaments[i];
				var id = tournament.getAttribute("id");
				var isWC = tournament.getAttribute("wc") == "true";
				var action = tournament.getAttribute("action");
				var type = tournament.getAttribute("type");
				if(type !== null)
					type = type.toLowerCase();
				var stage = tournament.getAttribute("stage");
					if(stage !== null)
						stage = stage.toLowerCase();
				var joinable = tournament.getAttribute("joinable") === "true";
				var abandoned = tournament.getAttribute("abandoned") === "true";
				if (action == "add" || action == "update") {
					var actionsField = $("<td></td>");

					var joined = tournament.getAttribute("signedUp");
					if (joined == "true") {
						that.inTournament = true;
						debugger;
						if(type === "solodraft" && (stage === "deck-building" || stage === "registering decks" || stage === "awaiting kickoff")) {
							var but = $("<button>Go to Draft</button>");
							$(but).button().click((
								function(tourneyInfo) {
									var tourneyId = tournament.getAttribute("id");
									return function() {
										var win = window.open("/gemp-lotr/soloDraft.html?eventId=" + tourneyId, '_blank');
										if (win) {
											//Browser has allowed it to be opened
											win.focus();
										}
									}
								}
							)(tournament));
							actionsField.append(but);
						}
						if(type === "table_solodraft" && (stage === "deck-building" || stage === "registering decks" || stage === "awaiting kickoff")) {
							var but = $("<button>Go to Draft</button>");
							$(but).button().click((
								function(tourneyInfo) {
									var tourneyId = tournament.getAttribute("id");
									return function() {
										var win = window.open("/gemp-lotr/tableDraft.html?eventId=" + tourneyId, '_blank');
										if (win) {
											//Browser has allowed it to be opened
											win.focus();
										}
									}
								}
							)(tournament));
							actionsField.append(but);
						}
						if(type === "table_draft" && stage === "drafting") {
							var but = $("<button>Go to Draft</button>");
							$(but).button().click((
								function(tourneyInfo) {
									var tourneyId = tournament.getAttribute("id");
									return function() {
										var win = window.open("/gemp-lotr/tableDraft.html?eventId=" + tourneyId, '_blank');
										if (win) {
											//Browser has allowed it to be opened
											win.focus();
										}
									}
								}
							)(tournament));
							actionsField.append(but);
						}
						if((type === "sealed" || type === "solodraft" || type === "table_solodraft" || type === "table_draft") && (stage === "deck-building" || stage === "registering decks" || stage === "awaiting kickoff" || stage === "paused between rounds")) {
								var but = $("<button>Register Deck</button>");
								$(but).button().click((
									function(tourneyInfo) {
										var tourneyId = tournament.getAttribute("id");
										var tourneyName = tournament.getAttribute("name");
										
										return function () {
											that.comm.registerLimitedTournamentDeck(tourneyId, that.decksSelect.val(), function (xml) {
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
						if(joinable) {
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

					// Tournament for for tournament table (not in Playing Tables Section)
					var rowstr = "";
					rowstr += "<tr class='tournament" + id + "'><td>" + tournament.getAttribute("format") + "</td>";
					if(type === "sealed") {
						rowstr += "<td>Sealed</td>";
					}
					else if (type === "solodraft") {
						rowstr += "<td>Solo Draft</td>";

					}
					else if (type === "table_solodraft") {
						rowstr += "<td>Solo Table Draft</td>";
					}
					else if (type === "table_draft") {
						rowstr += "<td>Table Draft</td>";
					}
					else {
						rowstr += "<td>" + tournament.getAttribute("collection") + "</td>";
					}
					rowstr += "<td>" + tournament.getAttribute("name") + "</td>" +
						"<td>" + tournament.getAttribute("system") + "</td>";
					if (tournament.hasAttribute("timeRemaining")) {
						rowstr += "<td>" + tournament.getAttribute("stage") + " - " + tournament.getAttribute("timeRemaining") + "</td>";
					} else {
						rowstr += "<td>" + tournament.getAttribute("stage") + "</td>";
					}
					rowstr += "<td>" + tournament.getAttribute("round") + "</td>" +
						"<td><div class='prizeHint' title='Competing Players' value='" + tournament.getAttribute("playerList") + "<br><br>* = abandoned'>" + tournament.getAttribute("playerCount") + "</div></td></tr>";
					var row = $(rowstr);

					// Row for tournament playing table
					var displayType = type;
					if(type === "sealed") {
						displayType = "Sealed";
					}
					else if (type === "solodraft") {
						displayType = "Solo Draft";
					}
					else if (type === "table_solodraft") {
						displayType = "Solo Table Draft";
					}
					else if (type === "table_draft") {
						displayType = "Table Draft";
					}
					else if (type === "constructed") {
						displayType = "Constructed";
					}
					var tablesRow = $("<tr class='table" + id + "'></tr>");
					tablesRow.append("<td>" + tournament.getAttribute("format") + "</td>");
					if (isWC) {
						tablesRow.append("<td>World Championship - " + displayType + " - " + tournament.getAttribute("name") + "</td>");
					} else {
						tablesRow.append("<td>" + tournament.getAttribute("system") + " Tournament - " + displayType + " - " + tournament.getAttribute("name") + "</td>");
					}
					if (tournament.hasAttribute("timeRemaining")) {
						tablesRow.append("<td>" + tournament.getAttribute("stage") + " - " + tournament.getAttribute("timeRemaining") + "</td>");
					} else if (tournament.getAttribute("stage") === "Playing Games") {
						tablesRow.append("<td>" + tournament.getAttribute("stage") + " - Round " + tournament.getAttribute("round") + "</td>");
					} else {
						tablesRow.append("<td>" + tournament.getAttribute("stage") + "</td>");
					}
					if (tournament.getAttribute("playerCount") <= 8) {
						tablesRow.append("<td>" + tournament.getAttribute("playerList") + "</td>");
					} else {
						tablesRow.append("<td><div class='prizeHint' title='Competing Players' value='" + tournament.getAttribute("playerList") + "<br><br>* = abandoned'>" + tournament.getAttribute("playerCount") + "</div></td>");
					}
					tablesRow.append(actionsField);
					if (joined == "true") {
						tablesRow.addClass("played");
					}
					if (isWC) {
						tablesRow.addClass("bold");
					}

					if (action == "add") {
						// Right now the only tournament section is for WC events
						if (isWC) {
							that.addWcEventsSection();
							$("table.wc-events", this.tablesDiv)
							.append(row);
						}
						// Display running tournaments also as playing tables
						$("table.playingTables", this.tablesDiv)
							.append(tablesRow)

						if (joined == "true") {
							// Open draft window
							if ((type === "table_solodraft" && (stage === "deck-building" || stage === "registering decks" || stage === "awaiting kickoff"))
							|| (type === "table_draft" && stage === "drafting")) {
								var tourneyId = tournament.getAttribute("id");
								window.open("/gemp-lotr/tableDraft.html?eventId=" + tourneyId, '_blank');
								this.PlaySound("gamestart");
							} else if (type === "solodraft" && (stage === "deck-building" || stage === "registering decks" || stage === "awaiting kickoff")) {
								var tourneyId = tournament.getAttribute("id");
								window.open("/gemp-lotr/soloDraft.html?eventId=" + tourneyId, '_blank');
								this.PlaySound("gamestart");
							}
						}

					} else if (action == "update") {
						// Update row in tournaments sections
						$(".tournament" + id, this.tablesDiv).replaceWith(row);

						// Update row in playing tables section
						var existingRow = $(".table" + id, this.tablesDiv);
						if (existingRow.length > 0) {
							// If the row exists, replace it
							existingRow.replaceWith(tablesRow);
						} else {
							// If the row does not exist, append it
							$("table.playingTables", this.tablesDiv).append(tablesRow);
						}
					}

					this.animateRowUpdate(".tournament" + id);
				} else if (action == "remove") {
					// Remove tournament both from playing tables section and tournament sections
					$(".tournament" + id, this.tablesDiv).remove();
					$(".table" + id, this.tablesDiv).remove();
				}
			}
			
			if($('.wc-events tr').length <= 1) {
				that.removeWcEventsSection();
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

			$(".count", $(".eventHeader.recurringQueues")).html("(" + ($("tr", $("table.recurringQueues")).length - 1) + ")");
			$(".count", $(".eventHeader.scheduledQueues")).html("(" + ($("tr", $("table.scheduledQueues")).length - 1) + ")");
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
				
				var defaultItem = "<option value='' disabled selected>Select Format</option>"
				this.supportedFormatsSelect.append(defaultItem);
				
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
