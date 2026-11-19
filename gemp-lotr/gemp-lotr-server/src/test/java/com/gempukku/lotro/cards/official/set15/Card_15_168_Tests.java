package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_168_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("searching", "15_168");
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, hunter 6, no other text
					put("ugluk", "15_172");     // Uglúk, Ugly Fellow: [uruk-hai] Uruk-hai, fierce, hunter 3; his trigger needs a skirmish of his
					put("murderous", "11_195"); // Murderous Uruk: [uruk-hai] Uruk-hai, damage +1 and muster, no hunter
					put("herdsman", "15_83");   // Hunting Herdsman: [men] Man, hunter 1; +3 for each follower (none here)
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 only
					// Saruman, Instigator of Insurrection: [isengard] Wizard; while 6 companions can be spotted, each minion
					// gains hunter 2; his response is used only on purpose
					put("saruman", "17_37");

					put("guardian", "17_7");    // Elven Guardian: [elven] Elf companion, hunter 1; a maneuver heal used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: strength +1 per race spotted, nothing else
					put("elf", "1_53");         // Lorien Elf: no keyword, a requirement to play only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SearchingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Searching Uruk
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br>Searching Uruk is strength +1 for each other [uruk-hai] hunter you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("searching");

		assertEquals("Searching Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus1ForEachOtherUrukHaiHunterAndNotForOtherHuntersOrUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(guardian);

		var searching = scn.GetShadowCard("searching");
		var butcher = scn.GetShadowCard("butcher");
		var ugluk = scn.GetShadowCard("ugluk");
		var murderous = scn.GetShadowCard("murderous");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCardsToHand(butcher, ugluk);

		scn.StartGame();
		assertInHand(butcher);
		assertInHand(ugluk);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(searching, murderous, herdsman);
		scn.ShadowRefreshDecision();

		// Beside an [uruk-hai] Uruk-hai who is no hunter, a [men] hunter and an [elven] hunter companion, and
		// not counting himself: his printed 5.
		assertEquals(5, scn.GetStrength(searching));

		scn.ShadowPlayCard(butcher);
		// One other [uruk-hai] hunter: 5 + 1.  The Butcher keeps his own 5.
		assertEquals(6, scn.GetStrength(searching));
		assertEquals(5, scn.GetStrength(butcher));

		scn.ShadowPlayCard(ugluk);
		// Two: 5 + 2.  Nobody else's strength moves.
		assertEquals(7, scn.GetStrength(searching));
		assertEquals(5, scn.GetStrength(butcher));
		assertEquals(9, scn.GetStrength(ugluk));
		assertEquals(8, scn.GetStrength(murderous));
		assertEquals(8, scn.GetStrength(herdsman));
		assertEquals(6, scn.GetStrength(guardian));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnotherCultureWhoGainsHunterIsNotCountedAndAnUrukHaiCultureMinionWhoGainsItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Six companions with Frodo, for Saruman's hunter 2.
		scn.MoveCompanionsToTable("guardian", "aragorn", "gimli", "gandalf", "elf");

		var searching = scn.GetShadowCard("searching");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");
		var murderous = scn.GetShadowCard("murderous");

		scn.StartGame();
		scn.MoveMinionsToTable(searching, guard, saruman);

		// Every minion is hunter 2 by Saruman: the [sauron] Uruk-hai and the [isengard] Wizard are hunters of
		// other cultures and are not counted.
		assertEquals(2, scn.GetKeywordCount(guard, Keyword.HUNTER));
		assertEquals(2, scn.GetKeywordCount(saruman, Keyword.HUNTER));
		assertEquals(3, scn.GetKeywordCount(searching, Keyword.HUNTER));
		assertEquals(5, scn.GetStrength(searching));

		scn.MoveMinionsToTable(murderous);
		// The Murderous Uruk, an [uruk-hai] minion made a hunter, is counted: 5 + 1.
		assertEquals(2, scn.GetKeywordCount(murderous, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(searching));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
