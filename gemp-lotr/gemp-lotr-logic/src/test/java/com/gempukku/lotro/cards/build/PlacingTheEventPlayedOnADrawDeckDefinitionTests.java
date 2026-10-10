package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * The effects PutPlayedEventOnTopOfDrawDeck and PutPlayedEventOnBottomOfDrawDeck take a field "filter": "self" (the
 * default) is the card they are written on, and "played" is the card whose play is being answered (F-0287).  Any
 * other word is refused when the card is loaded.
 *
 * What the two effects do in a game is in the server module, mechanics/PlacingTheEventPlayedOnADrawDeckTests.
 * Nothing is played here: the library is handed a few definitions and is read for which of them it kept.  The
 * texts are JSON, which the library reads as it reads HJSON.  No game and no table: plain JUnit, beside the builder
 * that reads a definition.
 */
public class PlacingTheEventPlayedOnADrawDeckDefinitionTests
{
	// The definition of a Free Peoples condition: "Response: If you play an event, place <a card> on top of
	// (beneath) your draw deck", the card being the one the filter names.  With no filter given, none is written.
	private static String response(String id, String type, String filter) {
		return "\"" + id + "\": { \"title\": \"Test Card\", \"unique\": false, \"side\": \"Free Peoples\", \"culture\": \"Shire\","
				+ " \"twilight\": 0, \"type\": \"Condition\", \"keywords\": \"Support Area\","
				+ " \"effects\": { \"type\": \"Response\", \"trigger\": { \"type\": \"played\", \"filter\": \"your,event\" },"
				+ " \"effect\": { \"type\": \"" + type + "\""
				+ (filter == null ? "" : ", \"filter\": \"" + filter + "\"") + " } } }";
	}

	@Test
	public void ADefinitionNamesTheCardItselfOrTheCardPlayedAndNothingElse() throws Exception {
		final String top = "PutPlayedEventOnTopOfDrawDeck";
		final String bottom = "PutPlayedEventOnBottomOfDrawDeck";
		final String definitions = "{"
				+ response("0_9201", top, null) + ","
				+ response("0_9202", top, "self") + ","
				+ response("0_9203", top, "played") + ","
				// Case does not matter.
				+ response("0_9204", top, "Played") + ","
				+ response("0_9205", bottom, null) + ","
				+ response("0_9206", bottom, "self") + ","
				+ response("0_9207", bottom, "played") + ","
				+ response("0_9208", bottom, "Played") + ","
				// Neither effect chooses a card, reads one from memory or takes a filter of cards.
				+ response("0_9211", top, "memory(playedEvent)") + ","
				+ response("0_9212", top, "choose(event)") + ","
				+ response("0_9213", top, "event") + ","
				+ response("0_9214", bottom, "memory(playedEvent)") + ","
				+ response("0_9215", bottom, "choose(event)") + ","
				+ response("0_9216", bottom, "event")
				+ "}";

		var loaded = LotroCardBlueprintLibrary.loadCardsFromFile(new LotroCardBlueprintBuilder(),
				new ByteArrayInputStream(definitions.getBytes(StandardCharsets.UTF_8)));

		for (int i = 1; i <= 8; i++)
			assertTrue("0_920" + i, loaded.containsKey("0_920" + i));
		for (int i = 1; i <= 6; i++)
			assertFalse("0_921" + i, loaded.containsKey("0_921" + i));
	}
}
