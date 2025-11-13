class SelectDeck {
	comm = null;
	mainDiv = null;
	deckManager = null;

	formatDropdown = null;
	playerDeckDropdown = null;
	libraryDeckDropdown = null;
	
	deckCookieName = "last-deck";
	formatCookieName = "last-format";
	
	constructor(comm, div, deckManager, cookie) {
		this.comm = comm;
		this.mainDiv = div;
		this.deckManager = deckManager;
		
		this.formatDropdown = $(div).find(".player-deck-format")
		this.playerDeckDropdown = $(div).find(".player-deck-dropdown");
		this.libraryDeckDropdown = $(div).find(".library-deck-dropdown");
		
		if(cookie !== undefined) {
			this.deckCookieName   = cookie + "-last-deck";
			this.formatCookieName = cookie + "-last-format";
		}
		// this.playerDeckDropdown.val(loadFromCookie(this.deckCookieName, ""));
		// this.formatDropdown.val(loadFromCookie(this.formatCookieName, ""));
		
		this.deckManager.registerDropdownUpdate(this.playerDeckDropdown);
	}
	
	getSelectedDeck() {
		saveToCookie(this.deckCookieName, this.playerDeckDropdown.val());
		saveToCookie(this.formatCookieName, this.formatDropdown.val());
		return this.playerDeckDropdown.val();
	}
	
	
	

}