class SelectDeck {
	comm = null;
	mainDiv = null;
	deckManager = null;

	formatDropdown = null;
	playerDeckDropdown = null;
	libraryDeckDropdown = null;
	
	constructor(comm, div, deckManager) {
		this.comm = comm;
		this.mainDiv = div;
		this.deckManager = deckManager;
		
		//this.mainDiv.tabs();
		
		this.formatDropdown = $(div).find(".player-deck-format")
		this.playerDeckDropdown = $(div).find(".player-deck-dropdown");
		this.libraryDeckDropdown = $(div).find(".library-deck-dropdown");
		
		this.deckManager.registerDropdownUpdate(this.playerDeckDropdown);
	}
	
	getSelectedDeck() {
		return this.playerDeckDropdown.val();
	}
	
	
	

}