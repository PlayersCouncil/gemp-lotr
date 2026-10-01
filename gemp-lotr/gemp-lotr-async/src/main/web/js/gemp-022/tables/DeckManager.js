/**
 * The viewer's decks and the Deck Library's decks, fetched together (every Play / Join open refreshes them).
 * Deck selectors (SelectDeck) register a callback that rebuilds their dropdowns once both lists have arrived.
 */
class DeckManager {
	playerDecks = [];
	libraryDecks = [];
	loaded = false;          // true once both lists have arrived at least once

	updateCallbacks = [];

	constructor(comm) {
		this.comm = comm;
	}

	registerUpdate(callback) {
		this.updateCallbacks.push(callback);
	}

	// The deck named `name` in the given source ("player" or "library"), or null.
	findDeck(name, source) {
		var list = (source == "library") ? this.libraryDecks : this.playerDecks;
		for (const deck of list) {
			if (deck.name === name)
				return deck;
		}
		return null;
	}

	updateDecks(mainCallback) {
		var that = this;
		var finish = function () {
			that.loaded = true;
			that.updateCallbacks.forEach((callback) => callback());
			if (mainCallback !== undefined)
				mainCallback();
		};

		// A failed list leaves the previous one in place; the selectors still rebuild, so a pending restore or
		// library preselection happens with whatever is known.
		this.comm.getDecks(function (xml) {
			that.playerDecks = Deck.processDecksFromXML(xml);
			that.comm.getLibraryDecks(function (xml) {
				that.libraryDecks = Deck.processDecksFromXML(xml);
				finish();
			}, TableFlow.errorMap(finish));
		}, TableFlow.errorMap(finish));
	}
}
