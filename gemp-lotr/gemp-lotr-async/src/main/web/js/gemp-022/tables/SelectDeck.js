/**
 * The deck picker of one Play / Join flow: a "Your Deck" dropdown (.player-deck-dropdown) and, when the markup has
 * one, a "Select Library Deck" dropdown (.library-deck-dropdown).  Choosing a deck in either clears the other.
 *
 *   var selector = new SelectDeck(comm, $div, deckManager, "unranked-table");
 *   selector.requestRestore("pc_movie");   // on open: select the last deck used for that format (or the last
 *                                          // one overall), once the deck lists have arrived
 *   var choice = selector.getSelection();  // {name, source: "player" | "library"} or null
 *   selector.remember(choice, "pc_movie"); // on submit
 *
 * The last decks are kept per flow in one cookie ("<flow>-decks"), keyed by format / league / event, with "_" for
 * the last one overall.  If the viewer owns no decks, a library deck is preselected.
 */
class SelectDeck {
	comm = null;
	mainDiv = null;
	deckManager = null;

	playerDeckDropdown = null;
	libraryDeckDropdown = null;

	cookieName = null;
	legacyDeckCookie = null;
	pendingRestore = false;
	restoreKey = null;
	fixedFirstOption = null;
	// true while the current deck is one the player picked by hand (rather than restored or defaulted by the flow)
	chosenByUser = false;

	static MAX_REMEMBERED = 12;

	// cookie: the flow's name ("unranked-table", "join-table", ...); options.fixedFirstOption: an option kept at the
	// top of the player dropdown (the bot's "Random FotR Starter").
	constructor(comm, div, deckManager, cookie, options) {
		var that = this;
		options = options || {};
		this.comm = comm;
		this.mainDiv = $(div);
		this.deckManager = deckManager;
		this.fixedFirstOption = options.fixedFirstOption || null;

		this.playerDeckDropdown = this.mainDiv.find(".player-deck-dropdown");
		this.libraryDeckDropdown = this.mainDiv.find(".library-deck-dropdown");

		this.setFlow(cookie || "deck");

		// a change with an originalEvent is the player's own pick; select() and the restores trigger change without one
		this.playerDeckDropdown.on("change.selectDeck", function (event) {
			if (that.playerDeckDropdown.val())
				that.libraryDeckDropdown.val("");
			that.chosenByUser = !!event.originalEvent;
		});
		this.libraryDeckDropdown.on("change.selectDeck", function (event) {
			if (that.libraryDeckDropdown.val())
				that.playerDeckDropdown.val("");
			that.chosenByUser = !!event.originalEvent;
		});

		this.deckManager.registerUpdate(() => that.rebuild());
		if (this.deckManager.loaded)
			this.rebuild();
	}

	// Which flow's memory this picker uses (the Join dialog's one picker serves four flows).
	setFlow(flow) {
		this.cookieName = flow + "-decks";
		this.legacyDeckCookie = flow + "-last-deck";
	}

	hasLibrary() {
		return this.libraryDeckDropdown.length > 0;
	}

	// ---- the dropdowns ----

	rebuild() {
		var previous = this.getSelection();
		var previousChosenByUser = this.chosenByUser;

		this.playerDeckDropdown.empty();
		if (this.fixedFirstOption != null) {
			this.playerDeckDropdown.append($("<option/>").attr("value", "").text(this.fixedFirstOption));
		} else {
			var ownsDecks = this.deckManager.playerDecks.length > 0;
			this.playerDeckDropdown.append($("<option/>").attr("value", "")
				.text(ownsDecks ? "Choose one of your decks" : "You have no decks yet"));
		}
		for (const deck of this.deckManager.playerDecks)
			this.playerDeckDropdown.append($("<option/>").attr("value", deck.name).text(Deck.formatDeck(deck)));

		if (this.hasLibrary()) {
			this.libraryDeckDropdown.empty();
			this.libraryDeckDropdown.append($("<option/>").attr("value", "").text("Choose a Deck Library deck"));
			for (const deck of this.deckManager.libraryDecks)
				this.libraryDeckDropdown.append($("<option/>").attr("value", deck.name).text(Deck.formatDeck(deck)));
		}

		this.playerDeckDropdown.val("");
		this.libraryDeckDropdown.val("");

		if (this.pendingRestore) {
			this.pendingRestore = false;
			var remembered = this.recall(this.restoreKey);
			if (remembered != null && this.select(remembered.name, remembered.source))
				return;
		}
		if (previous != null && this.select(previous.name, previous.source)) {
			this.chosenByUser = previousChosenByUser;     // a refreshed deck list keeps a hand-picked deck hand-picked
			return;
		}
		this.selectDefault();
	}

	// Nothing chosen: a player with no decks gets a library deck; otherwise their first deck (as the dropdown used
	// to show).  The bot's selector keeps its fixed first option.
	selectDefault() {
		if (this.fixedFirstOption != null) {
			this.playerDeckDropdown.val("");
			this.playerDeckDropdown.trigger("change");
			return;
		}
		if (this.deckManager.playerDecks.length == 0) {
			if (this.hasLibrary() && this.deckManager.libraryDecks.length > 0)
				this.select(this.deckManager.libraryDecks[0].name, "library");
			return;
		}
		this.select(this.deckManager.playerDecks[0].name, "player");
	}

	// Selects a deck; false if it is not in that list.  Triggers change so dependants (the Casual format) follow.
	select(name, source) {
		if (name == null || name === "")
			return false;
		var dropdown = (source == "library") ? this.libraryDeckDropdown : this.playerDeckDropdown;
		if (!dropdown.length || !TableFlow.hasOption(dropdown, name))
			return false;
		dropdown.val(name);
		dropdown.trigger("change");
		return true;
	}

	clear() {
		this.chosenByUser = false;
		this.playerDeckDropdown.val("");
		this.libraryDeckDropdown.val("");
	}

	// {name, source} of the chosen deck, or null.
	getSelection() {
		var library = this.hasLibrary() ? this.libraryDeckDropdown.val() : null;
		if (library)
			return {name: library, source: "library"};
		var own = this.playerDeckDropdown.val();
		if (own)
			return {name: own, source: "player"};
		return null;
	}

	// The chosen deck as a Deck (with its target format), or null.
	getSelectedDeckInfo() {
		var choice = this.getSelection();
		return choice == null ? null : this.deckManager.findDeck(choice.name, choice.source);
	}

	// Older callers: the chosen deck's name ("" = none).
	getSelectedDeck() {
		var choice = this.getSelection();
		return choice == null ? "" : choice.name;
	}

	// ---- memory ----

	// On opening the flow: select the last deck used under `key` (format code, league code...; null = last overall)
	// as soon as the deck lists are known.  Applied now if they already are.
	requestRestore(key) {
		this.chosenByUser = false;
		this.restoreKey = (key == null || key === "") ? null : String(key);
		this.pendingRestore = true;
		if (this.deckManager.loaded) {
			var remembered = this.recall(this.restoreKey);
			if (remembered != null && this.select(remembered.name, remembered.source))
				this.pendingRestore = false;
		}
	}

	// Whether the current deck is one the player picked by hand in this flow.
	hasUserChoice() {
		return this.chosenByUser && this.getSelection() != null;
	}

	// As requestRestore, but leaves a deck the player picked by hand alone: picking a format or league then brings back
	// the deck last used in it only when the current deck was restored or defaulted, not chosen (e.g. a Standard deck
	// the player wants to take into an Expanded table stays selected).
	restoreUnlessChosen(key) {
		if (!this.hasUserChoice())
			this.requestRestore(key);
	}

	// The remembered {name, source} for key, falling back to the last one overall.
	recall(key) {
		var memory = this.readMemory();
		var entry = (key != null && memory[key]) ? memory[key] : memory["_"];
		if (entry == null) {
			// before this memory existed the flows saved "<flow>-last-deck" (the player's own deck)
			var legacy = TableFlow.cookie(this.legacyDeckCookie);
			return legacy == null ? null : {name: legacy, source: "player"};
		}
		return {name: entry[0], source: entry[1] == "library" ? "library" : "player"};
	}

	remember(choice, key) {
		if (choice == null)
			return;
		var memory = this.readMemory();
		var entry = [choice.name, choice.source];
		if (key != null && key !== "") {
			delete memory[key];
			memory[String(key)] = entry;     // re-inserted last = most recent
		}
		delete memory["_"];
		memory["_"] = entry;
		var keys = Object.keys(memory);
		while (keys.length > SelectDeck.MAX_REMEMBERED) {
			var oldest = keys.shift();
			if (oldest !== "_")
				delete memory[oldest];
		}
		try {
			saveToCookie(this.cookieName, JSON.stringify(memory));
		} catch (ignored) {
			// no cookie, no memory
		}
	}

	readMemory() {
		var raw = TableFlow.cookie(this.cookieName);
		if (raw == null)
			return {};
		try {
			var parsed = JSON.parse(raw);
			return (parsed != null && typeof parsed == "object" && !Array.isArray(parsed)) ? parsed : {};
		} catch (ignored) {
			return {};
		}
	}
}
