class DeckManager {
	playerDecks = [];
	libraryDecks = [];
	
	updateCallbacks = [];
	
	constructor(comm) {
		this.comm = comm;
	}
	
	registerUpdate(callback) {
		this.updateCallbacks.push(callback);
	}
	
	registerDropdownUpdate(dropdown) {
		var that = this;
		
		this.updateCallbacks.push(() => {
			var currentDeck = dropdown.val();
			dropdown.empty();
			
			for (const deck of that.playerDecks) {
				var option = $("<option/>")
					.attr("value", deck.name)
					.text(Deck.formatDeck(deck));
				dropdown.append(option);
			}
			
			dropdown.val(currentDeck);
			dropdown.change();
		});
	}
	
	updateDecks(mainCallback) {
		var that = this;
		
		this.comm.getDecks(function (xml) {
			that.playerDecks = Deck.processDecksFromXML(xml);
			
			that.comm.getLibraryDecks(function(xml) {
				that.libraryDecks = Deck.processDecksFromXML(xml);
				
				that.updateCallbacks.forEach((callback) => callback());
				
				if(mainCallback !== undefined) {
					mainCallback();	
				}
			});
		});
	}
}