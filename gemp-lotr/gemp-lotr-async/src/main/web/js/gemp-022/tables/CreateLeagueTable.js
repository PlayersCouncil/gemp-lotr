/**
 * Play ▸ Open League Table: a ranked table in a league the player has joined, and below it the current leagues
 * (the Events tab's list mode: one row per league with a details drawer holding the Join button).
 *
 * The league list is built once, into its own container (#play-league-list), so it shares no element ids with the
 * Events tab's list; it is refreshed each time the panel opens.  After a join the new league is selected and the
 * next steps are offered (create a table; go to the draft; open the deck builder for a Sealed / Draft league).
 * The deck is remembered per league ("league-table-decks").
 */
class CreateLeagueTable extends TableFlow {
	deckSelector = null;

	leagueDropdown = null;
	createTableButton = null;

	leagueUI = null;
	nextSteps = null;

	constructor(mainHall, comm, div, formatManager, deckManager) {
		super(mainHall, comm, div, formatManager, deckManager, $("#league-result"));
		var that = this;

		this.deckSelector = new SelectDeck(this.comm, $(div).find(".player-deck"), deckManager, "league-table");

		this.leagueDropdown = $("#league-format");
		this.nextSteps = $("#play-league-next-steps");

		this.createTableButton = $("#submit-league-table-button").button().click(this.submitTable(this));

		this.formatManager.registerLeagueDropdownUpdate(this.leagueDropdown);
		this.leagueDropdown.on("change", function (event) {
			if (event.originalEvent)
				that.deckSelector.restoreUnlessChosen(that.leagueDropdown.val());
		});
	}

	hide() {
		this.mainDiv.hide();
	}

	show() {
		var league = TableFlow.cookie("league-table-last-league");
		TableFlow.want(this.leagueDropdown, league);
		this.deckSelector.requestRestore(league);
		this.nextSteps.prop("hidden", true).empty();

		if (this.leagueUI == null) {
			this.leagueUI = new LeagueResultsUI("/gemp-lotr-server",
				(leagueCode) => this.leagueJoined(leagueCode),
				{
					list: $("#play-league-list"),
					onJoinError: (leagueCode, message) => this.showNextStepsMessage("Could not join the league: " + message, true)
				});
		} else {
			this.leagueUI.loadResults();
		}

		this.mainDiv.show();
	}

	// After a successful join (from the list below): refresh the League dropdown with the new league selected and
	// say what to do next.
	leagueJoined(leagueCode) {
		var that = this;
		TableFlow.want(this.leagueDropdown, leagueCode);
		this.formatManager.updateFormats(function () {
			that.showNextSteps(leagueCode);
		});
	}

	showNextStepsMessage(text, isError) {
		this.nextSteps.empty()
			.toggleClass("play-error", !!isError)
			.append($("<div></div>").text(text))
			.prop("hidden", false);
	}

	showNextSteps(leagueCode) {
		var that = this;
		var league = this.formatManager.getLeague(leagueCode);
		var name = league ? league.name : leagueCode;
		this.showNextStepsMessage("You joined " + name + ". Next:", false);

		var buttons = $("<div class='play-next-buttons'></div>");
		buttons.append($("<button></button>").text("Create a table in this league").button().click(function () {
			TableFlow.want(that.leagueDropdown, leagueCode);
			that.leagueDropdown.trigger("change");
			that.deckSelector.requestRestore(leagueCode);
			that.leagueDropdown.focus();
			var form = $("#league-table-options")[0];
			if (form && form.scrollIntoView)
				form.scrollIntoView({block: "start"});
		}));
		this.nextSteps.append(buttons);

		// Sealed / draft leagues: the cards come from the league
		this.comm.getLeague(leagueCode, function (xml) {
			var root = xml && xml.documentElement;
			if (root == null || root.tagName != "league")
				return;
			if (root.getAttribute("draftable") == "true") {
				buttons.append($("<button></button>").text("Go to draft").button().click(function () {
					var win = window.open("/gemp-lotr/soloDraft.html?eventId=" + encodeURIComponent(leagueCode), "_blank");
					if (win)
						win.focus();
				}));
			}
			var limited = false;
			var series = root.getElementsByTagName("serie");
			for (var i = 0; i < series.length; i++) {
				if (series[i].getAttribute("limited") == "true")
					limited = true;
			}
			if (limited) {
				buttons.append($("<a class='ui-button ui-widget ui-corner-all' href='deckBuild.html' target='_blank' rel='noopener'></a>")
					.text("Open the deck builder"));
				that.nextSteps.append($("<div class='page-hint'></div>").text(
					"This league issues its own cards: in the deck builder, choose the league's collection to open your packs"
					+ " and build a deck from them. League tables only accept such decks."));
			}
		}, TableFlow.errorMap(function () {
			// the next steps above still apply
		}));
	}

	submitTable(that) {
		return () => {
			var format = that.leagueDropdown.val();

			if(format == null || format === "") {
				that.updateResult("You must select a league.  If you are not enrolled in one, join one below.");
				return;
			}

			var deck = that.deckSelector.getSelection();

			if(deck == null) {
				that.updateResult("You must select a deck.  Remember that if this is a sealed or draft league, you may only use cards issued by this league.");
				return;
			}

			that.deckSelector.remember(deck, format);
			saveToCookie("league-table-last-league", format);

			var result = that.respond({
				title: "Create League Table",
				quietSuccess: true,
				onSuccess: function () {
					that.mainHall.tableCreator.closeAfterSuccess();
				}
			});
			that.comm.createTable(format, deck.name, null, null, false, false,
					result.callback, result.errorMap, deck.source);
		};
	}
}
