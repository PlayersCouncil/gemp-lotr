package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_305_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "7_305");
					put("savage2", "7_305");    // a second copy: another [sauron] Orc for the first
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; text on a weapon PLAYED on him only
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc of another title, strength 10; his text works only while he is stacked
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5; its text needs 2 wounds on the Ring-bearer
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai (no Orc), strength 9; damage +1 only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: his text is about being overwhelmed
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength +2 only while he bears a weapon
					put("sam", "1_311");        // Sam, Son of Hamfast: no requirement to play; abilities used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: an ally, a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcSavageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Savage
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: While you can spot 6 companions and another [sauron] Orc, this minion is strength +9 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("savage");

		assertEquals("Orc Savage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyItIsPlainUntilASixthCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		// Frodo and four more: 5 companions, and an ally beside them.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("merry"));
		scn.MoveAlliesToTable(scn.GetFreepsCard("barliman"));
		scn.MoveCardsToHand(sam);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, chieftain);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, sam);
		assertEquals(9, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.FreepsPlayCard(sam);

		// Six companions and another [sauron] Orc: 9 + 9, and fierce.  The Chieftain is neither.
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(18, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(7, scn.GetStrength(chieftain));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BesideAWraithOrcAndASauronUrukHaiItIsPlainUntilASauronOrcComes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo and five more: 6 companions.
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("merry"), scn.GetFreepsCard("sam"));

		var savage = scn.GetShadowCard("savage");
		var pillager = scn.GetShadowCard("pillager");
		var hound = scn.GetShadowCard("hound");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, hound, guard);

		// An Orc who is not [sauron] and a [sauron] minion who is no Orc.
		assertEquals(9, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.MoveMinionsToTable(pillager);

		assertEquals(18, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(10, scn.GetStrength(pillager));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoSavagesEachSpotTheOtherAsAnotherSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("boromir"), scn.GetFreepsCard("gimli"),
				scn.GetFreepsCard("merry"), scn.GetFreepsCard("sam"));

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		// Alone, it has no other [sauron] Orc to spot.
		assertEquals(9, scn.GetStrength(savage));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.MoveMinionsToTable(savage2);

		assertEquals(18, scn.GetStrength(savage));
		assertTrue(scn.HasKeyword(savage, Keyword.FIERCE));
		assertEquals(18, scn.GetStrength(savage2));
		assertTrue(scn.HasKeyword(savage2, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
