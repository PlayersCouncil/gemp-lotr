/**
 * The Join dialog and the hall's join buttons: join a waiting table, join a tournament queue, join a running
 * tournament late, and register a deck for a limited tournament.
 *
 * One dialog serves all four; its title and heading say which (and which event).  Every attempt reports through
 * TableFlow.respond, so the outcome is always shown: in the dialog's result box, or, for the sealed / draft queue and
 * late joins that need no deck and so open no dialog, in a hall dialog.  Server refusals ("Queue is full", "Sign-up
 * opens at ...", an invalid deck) arrive as <error> and are shown as failures.
 *
 * The deck is remembered per flow and event format (join-table: last overall; join-queue / late-join: per format;
 * register-deck: per tournament), and a player with no decks gets a Deck Library deck preselected.
 *
 * Entry points used by the hall (hallUi.js) and the calendar:
 *   generateJoinButton(tableId)
 *   generateJoinQueueButton(queue)          contract C3: queue is the <queue> element from the hall poll; returns a
 *                                           jQuery button whose click runs the join-queue flow
 *   startQueueJoin(queue)                   the same flow without a button; queue may be any object with
 *                                           getAttribute(name) for "id", "type", "queue", "start" and "format"
 *   generateLateJoinButton(tournament)      the <tournament> element
 *   generateRegisterDeckButton(tournament)
 */
class JoinTable extends TableFlow {
	popup = null;

	joinButton = null;
	heading = null;
	subtitle = null;

	deckSelector = null;

	constructor(mainHall, comm, popup, formatManager, deckManager) {
		super(mainHall, comm, $("#join-table-options"), formatManager, deckManager, $("#join-result"));
		this.popup = popup;

		this.joinButton = $("#submit-join-table-button");
		this.heading = $("#join-table-heading");
		this.subtitle = $("#join-table-subtitle");

		this.popup.dialog({
			autoOpen: false,
			closeOnEscape: true,
			closeText: "",
			resizable: true,
			modal: true,
			show: 100,
			hide: 300,
			minWidth: 320,
			width: CreateTable.fitWidth(900),
			height: "auto",
			maxHeight: CreateTable.fitHeight(),
			title: "Join Table",
			classes: {"ui-dialog": "play-dialog"}
		});

		// one picker; each flow keeps its own deck memory (SelectDeck.setFlow)
		this.deckSelector = new SelectDeck(this.comm, $("#join-table-options").find(".player-deck"), deckManager, "join-table");

		TableFlow.bindInfoToggles(this.popup);
	}

	// Opens the dialog for one flow: title, heading, button label, deck memory, and a fresh "Ready." result.
	resetJoinForm(that, text, newCallback, options) {
		options = options || {};
		that.deckSelector.setFlow(options.flow || "join-table");

		var title = options.title || text;
		that.popup.dialog("option", "title", title);
		that.heading.text(title);
		if (options.subtitle) {
			that.subtitle.text(options.subtitle).prop("hidden", false);
		} else {
			that.subtitle.text("").prop("hidden", true);
		}

		that.joinButton.off("click");
		that.joinButton.button().button("option", "label", text);
		that.joinButton.click(newCallback);

		that.resetResult();
		that.deckSelector.requestRestore(options.memoryKey);
		that.showPopup(that);
	}

	showPopup(that) {
		that = that || this;
		that.deckManager.updateDecks();
		that.popup.dialog("open");
	}

	hidePopup(that) {
		(that || this).popup.dialog("close");
	}

	// ---- join a waiting table ----

	generateJoinButton(tableId) {
		var that = this;
		var hallButton = $("<button>Join Table</button>");
		$(hallButton).button().click(function () {
			that.resetJoinForm(that, "Join Table", that.submitTable(that, tableId), {flow: "join-table"});
		});

		return hallButton;
	}

	submitTable(that, tableId) {
		return () => {
			var deck = that.deckSelector.getSelection();

			if(deck == null) {
				that.updateResult("You must select a deck: one of yours, or one from the Deck Library.");
				return;
			}
			that.deckSelector.remember(deck, null);

			var result = that.respond({
				title: "Join Table",
				quietSuccess: true,      // the game starts and opens from the hall
				onSuccess: function () {
					that.hidePopup(that);
				}
			});
			that.comm.joinTable(tableId, deck.name, result.callback, result.errorMap, deck.source);
		};
	}

	// ---- join a tournament queue (contract C3) ----

	generateJoinQueueButton(queue) {
		var that = this;
		var hallButton = $("<button>Join Queue</button>");

		$(hallButton).button().click(function () {
			that.startQueueJoin(queue);
		});

		return hallButton;
	}

	startQueueJoin(queueInfo) {
		var type = JoinTable.lowerType(queueInfo);
		if(type == "constructed") {
			var name = queueInfo.getAttribute("queue");
			this.resetJoinForm(this, "Join Queue", this.submitQueue(this, queueInfo, true), {
				flow: "join-queue",
				title: "Join Queue: " + (name || "tournament"),
				subtitle: JoinTable.describeEvent(queueInfo),
				memoryKey: queueInfo.getAttribute("format")
			});
		}
		//sealed, solo draft, table draft, table solo draft
		//i.e. something where joining the queue does not yet provide a deck
		else {
			this.submitQueue(this, queueInfo, false)();
		}
	}

	submitQueue(that, queueInfo, requiresDeck) {
		return () => {
			var deck = requiresDeck ? that.deckSelector.getSelection() : null;

			if(requiresDeck && deck == null) {
				that.updateResult("You must select a deck: one of yours, or one from the Deck Library.");
				return;
			}
			if (deck != null)
				that.deckSelector.remember(deck, queueInfo.getAttribute("format"));

			var queueId = queueInfo.getAttribute("id");
			var type = JoinTable.lowerType(queueInfo);
			var queueName = queueInfo.getAttribute("queue");
			var queueStart = queueInfo.getAttribute("start");

			var result = that.respond({
				title: "Join Queue: " + (queueName || "tournament"),
				onSuccess: function () {
					that.mainHall.setPendingTournament(true);
					that.hidePopup(that);
					that.mainHall.showDialog("Joined Tournament", JoinTable.queueJoinedMessage(type, queueName, deck ? deck.name : null, queueStart), 320);
					return true;
				}
			});
			that.comm.joinQueue(queueId, deck != null ? deck.name : "", result.callback, result.errorMap,
				deck != null ? deck.source : null);
		};
	}

	// The confirmation after a queue join (HTML: every server-provided value is escaped).
	static queueJoinedMessage(type, queueName, deckName, queueStart) {
		var e = TableFlow.escapeHtml;
		var start = "You have signed up to participate in the <b>" + e(queueName) + "</b> tournament.<br><br>";
		switch (type) {
			case "sealed":
				return start + "When the event begins, you will be issued sealed packs to open and make a deck. " +
					"At any time during the deck building phase, you will need to lock-in your deck before the tournament begins.<br><br>" +
					"Deck building begins at " + e(queueStart) + ". Good luck!";
			case "solodraft":
			case "table_solodraft":
				return start + "When the event begins, use the 'Go to Draft' button in the Playing Tables Section, and then build your deck in the Deck Builder. " +
					"At any time during the deck building phase, you will need to lock-in your deck before the tournament begins.<br><br>" +
					"Deck building begins at " + e(queueStart) + ". Good luck!";
			case "table_draft":
				return start + "When the event begins, use the 'Go to Draft' button in the Playing Tables Section, and then build your deck in the Deck Builder. " +
					"At any time during the deck building phase, you will need to lock-in your deck before the tournament begins.<br><br>" +
					"Draft begins at " + e(queueStart) + ". Good luck!";
			default:
				return start + "You will use a snapshot of your '<b>" + e(deckName) + "</b>' deck as it is right now. " +
					"If you need to change or update your deck, you will need to leave the queue and rejoin.<br><br>" +
					"The first game begins " + JoinTable.startPhrase(queueStart) + ". Good luck!";
		}
	}

	// "at 2026-09-24 18:00" / "When 8 players join" -> readable after "The first game begins"
	static startPhrase(start) {
		var text = String(start == null ? "" : start);
		if (/^at /i.test(text) || /^when /i.test(text))
			return TableFlow.escapeHtml(text.charAt(0).toLowerCase() + text.substring(1));
		return "at " + TableFlow.escapeHtml(text);
	}

	static describeEvent(info) {
		var parts = [];
		var format = info.getAttribute("format");
		if (format)
			parts.push("Format: " + format);
		var start = info.getAttribute("start");
		if (start)
			parts.push("Starts: " + start);
		return parts.join(" · ");
	}

	static lowerType(info) {
		var type = info.getAttribute("type");
		return type == null ? null : String(type).toLowerCase();
	}

	// ---- join a running tournament late ----

	generateLateJoinButton(tournament) {
		var that = this;
		var hallButton = $("<button>Join Tournament</button>");

		$(hallButton).button().click(function () {
			var type = JoinTable.lowerType(tournament);
			if(type == "constructed") {
				var name = tournament.getAttribute("name");
				that.resetJoinForm(that, "Join Tournament", that.submitJoinLate(that, tournament, true), {
					flow: "late-join",
					title: "Join Tournament: " + (name || "tournament"),
					subtitle: tournament.getAttribute("format") ? "Format: " + tournament.getAttribute("format") : null,
					memoryKey: tournament.getAttribute("format")
				});
			}
			//sealed, solo draft, table draft, table solo draft
			//i.e. something where joining the queue does not yet provide a deck
			else {
				that.submitJoinLate(that, tournament, false)();
			}
		});

		return hallButton;
	}

	submitJoinLate(that, tourneyInfo, requiresDeck) {
		return () => {
			var deck = requiresDeck ? that.deckSelector.getSelection() : null;

			if(requiresDeck && deck == null) {
				that.updateResult("You must select a deck: one of yours, or one from the Deck Library.");
				return;
			}
			if (deck != null)
				that.deckSelector.remember(deck, tourneyInfo.getAttribute("format"));

			var tourneyId = tourneyInfo.getAttribute("id");
			var tourneyName = tourneyInfo.getAttribute("name");

			var result = that.respond({
				title: "Join Tournament: " + (tourneyName || "tournament"),
				onSuccess: function (message) {
					that.mainHall.setPendingTournament(true);
					that.hidePopup(that);
					that.showDialog("Joined Tournament", message);
					return true;
				}
			});
			that.comm.joinTournamentLate(tourneyId, deck != null ? deck.name : "", result.callback, result.errorMap,
				deck != null ? deck.source : null);
		};
	}

	// ---- register a deck for a limited tournament ----

	generateRegisterDeckButton(tourneyInfo) {
		var that = this;
		var hallButton = $("<button>Register Deck</button>");
		$(hallButton).button().click(function () {
			var name = tourneyInfo.getAttribute("name");
			that.resetJoinForm(that, "Register Deck", that.submitRegistration(that, tourneyInfo), {
				flow: "register-deck",
				title: "Register Deck: " + (name || "tournament"),
				subtitle: "Choose the deck you built from this tournament's cards.",
				memoryKey: tourneyInfo.getAttribute("id")
			});
		});

		return hallButton;
	}

	submitRegistration(that, tourneyInfo) {
		return () => {
			var deck = that.deckSelector.getSelection();

			if(deck == null) {
				that.updateResult("You must select a deck.");
				return;
			}
			that.deckSelector.remember(deck, tourneyInfo.getAttribute("id"));

			var tourneyId = tourneyInfo.getAttribute("id");
			var tourneyName = tourneyInfo.getAttribute("name");

			var result = that.respond({
				title: "Register Deck: " + (tourneyName || "tournament"),
				onSuccess: function (message) {
					that.mainHall.setPendingTournament(true);
					that.hidePopup(that);
					that.showDialog("Deck Registered", message);
					return true;
				}
			});
			that.comm.registerLimitedTournamentDeck(tourneyId, deck.name, result.callback, result.errorMap, deck.source);
		};
	}

	// Kept for any caller outside the Play flows; new code uses TableFlow.respond.
	static getResponse(outputControl, callback=null) {
		return CreateTable.getResponse(outputControl, callback);
	}

	static getCreateErrorMap(outputControl, callback=null) {
		return CreateTable.getCreateErrorMap(outputControl, callback);
	}
}
