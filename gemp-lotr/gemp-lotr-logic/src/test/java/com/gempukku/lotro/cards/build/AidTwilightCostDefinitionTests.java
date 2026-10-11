package com.gempukku.lotro.cards.build;

import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.LotroCardBlueprintLibrary;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * The twilight cost of a follower's Aid, as a definition gives it and a blueprint keeps it.
 *
 * A follower's "Aid - (2)." is written as an aidCost effect whose cost is paid when the Aid is taken; nothing of
 * it was kept that could be read off the card.  The blueprint now answers getAidTwilightCost() with the number,
 * for the definitions whose Aid is one cost of twilight and a plain number of it: an AddTwilight (a Free Peoples
 * follower adds it) or a RemoveTwilight (a Shadow follower removes it), with the amount written out, or left out
 * (both effects then take 1).  For every other Aid the answer is null: one paid in a burden, a threat, an exertion
 * or a card from hand, one paid in twilight and something else besides, and one whose twilight is worked out in
 * play.  A card with no Aid answers null too.
 *
 * The number is a reading and nothing more.  What is paid is the definition's cost, as before: the virtual table's
 * own test of GetAidTwilightCost (virtualtable/AidCostReadingTests, in the server's tests) sets the number read
 * beside the twilight an Aid moves.
 *
 * Nothing is played here: each test hands the library a few definitions and reads what each blueprint kept.  The
 * texts are JSON, which the library reads as it reads HJSON.  No game and no table: plain JUnit, beside the
 * builder that reads a definition.
 */
public class AidTwilightCostDefinitionTests
{
	// The definition of a Free Peoples follower whose Aid is the cost given (a JSON object or an array of them).
	private static String follower(String id, String aidCost) {
		return "\"" + id + "\": { \"title\": \"Test Follower\", \"unique\": false, \"side\": \"Free Peoples\", \"culture\": \"Gandalf\","
				+ " \"twilight\": 1, \"type\": \"Follower\","
				+ " \"effects\": { \"type\": \"aidCost\", \"cost\": " + aidCost + " } }";
	}

	// The definition of a Free Peoples condition with no effect at all.
	private static String condition(String id) {
		return "\"" + id + "\": { \"title\": \"Test Card\", \"unique\": false, \"side\": \"Free Peoples\", \"culture\": \"Gandalf\","
				+ " \"twilight\": 0, \"type\": \"Condition\", \"keywords\": \"Support Area\" }";
	}

	private static Map<String, LotroCardBlueprint> load(String... definitions) throws Exception {
		final String text = "{" + String.join(",", definitions) + "}";
		return LotroCardBlueprintLibrary.loadCardsFromFile(new LotroCardBlueprintBuilder(),
				new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void AnAidOfOneTwilightCostKeepsItsNumber() throws Exception {
		var loaded = load(
				// "Aid - (2)."
				follower("0_9201", "{ \"type\": \"addTwilight\", \"amount\": 2 }"),
				// With no amount the effect adds 1: "Aid - (1)."
				follower("0_9202", "{ \"type\": \"addTwilight\" }"),
				// Case does not matter in the name of the effect.
				follower("0_9203", "{ \"type\": \"AddTwilight\", \"amount\": 3 }"),
				// A Shadow follower's Aid removes the twilight.
				follower("0_9204", "{ \"type\": \"removeTwilight\", \"amount\": 2 }"),
				follower("0_9205", "{ \"type\": \"RemoveTwilight\" }"),
				// A number written as text is read as the effect reads it.
				follower("0_9206", "{ \"type\": \"addTwilight\", \"amount\": \"4\" }"),
				// One cost in a list is still one cost.
				follower("0_9207", "[ { \"type\": \"addTwilight\", \"amount\": 2 } ]"));

		assertEquals(Integer.valueOf(2), loaded.get("0_9201").getAidTwilightCost());
		assertEquals(Integer.valueOf(1), loaded.get("0_9202").getAidTwilightCost());
		assertEquals(Integer.valueOf(3), loaded.get("0_9203").getAidTwilightCost());
		assertEquals(Integer.valueOf(2), loaded.get("0_9204").getAidTwilightCost());
		assertEquals(Integer.valueOf(1), loaded.get("0_9205").getAidTwilightCost());
		assertEquals(Integer.valueOf(4), loaded.get("0_9206").getAidTwilightCost());
		assertEquals(Integer.valueOf(2), loaded.get("0_9207").getAidTwilightCost());
	}

	@Test
	public void AnAidPaidInAnythingElseKeepsNoNumber() throws Exception {
		var loaded = load(
				// "Aid - Add a burden.", "Aid - Add 2 burdens."
				follower("0_9211", "{ \"type\": \"addBurdens\" }"),
				follower("0_9212", "{ \"type\": \"addBurdens\", \"amount\": 2 }"),
				// "Aid - Add a threat."
				follower("0_9213", "{ \"type\": \"addThreats\" }"),
				// "Aid - Exert a [gondor] companion."
				follower("0_9214", "{ \"type\": \"exert\", \"select\": \"choose(culture(gondor),companion)\" }"),
				// Twilight and something else besides: the Aid is not a twilight cost alone.
				follower("0_9215", "[ { \"type\": \"exert\", \"select\": \"choose(companion)\" }, { \"type\": \"addTwilight\", \"amount\": 2 } ]"),
				follower("0_9216", "[ { \"type\": \"addTwilight\", \"amount\": 1 }, { \"type\": \"addTwilight\", \"amount\": 1 } ]"),
				// Twilight that is worked out in play is no number to keep.
				follower("0_9217", "{ \"type\": \"addTwilight\", \"amount\": \"1-2\" }"),
				follower("0_9218", "{ \"type\": \"addTwilight\", \"amount\": { \"type\": \"forEachYouCanSpot\", \"filter\": \"companion\" } }"),
				follower("0_9219", "{ \"type\": \"removeTwilight\", \"amount\": \"twilightCount\" }"));

		for (int i = 1; i <= 9; i++) {
			assertTrue("0_921" + i, loaded.containsKey("0_921" + i));
			assertNull("0_921" + i, loaded.get("0_921" + i).getAidTwilightCost());
		}
	}

	@Test
	public void ACardWithNoAidKeepsNoNumber() throws Exception {
		var loaded = load(condition("0_9221"));

		assertTrue(loaded.containsKey("0_9221"));
		assertNull(loaded.get("0_9221").getAidTwilightCost());
	}
}
