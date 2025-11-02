class Deck {
	
	name = null;
	targetFormat = null;
	
	constructor(name, format) {
		this.name = name;
		this.targetFormat = format;
	}
	
	static processDecksFromXML(xml) {
		var root = xml.documentElement;
		
		if (root.tagName != "decks")
			return [];
		
		var decks = [];
		
		var xmlDecks = root.getElementsByTagName("deck");
		for (var i = 0; i < xmlDecks.length; i++) {
			var xmlDeck = xmlDecks[i];
			var deckName = xmlDeck.childNodes[0].nodeValue;
			var formatName = xmlDeck.getAttribute("targetFormat");
			decks.push(new Deck(deckName, formatName));
		}
		
		return decks;
	}
	
	static formatDeckName(formatName, deckName)
	{
		return "[" + formatName + "] - " + deckName;
	}
	
	static formatDeck(deck)
	{
		return Deck.formatDeckName(deck.targetFormat, deck.name);
	}
}