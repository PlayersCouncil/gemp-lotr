package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_269_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fires", "7_269");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, site 6, strength 7; his text answers a weapon played on him
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc, site 5, strength 10; his text works only while he is stacked
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), site 6, strength 9, damage +1
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]), site 4, strength 5; his text needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion to hold a threat; a maneuver ability used only on purpose
					put("dwarf", "1_7");        // Dwarf Guard: a companion with no text once in play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A [sauron] Orc of site number 6 at site 2, and four companions to hold four threats.
	protected VirtualTableScenario GetThreatsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fires", "7_269");
					put("chieftain", "1_266");
					put("aragorn", "1_89");
					put("gimli", "1_13");
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FiresRagedUncheckedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Fires Raged Unchecked
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: The site number of each [sauron] Orc is -1 for each threat.
		 * While you can spot 3 threats, each [sauron] Orc that is not roaming is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fires");

		assertEquals("Fires Raged Unchecked", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "each [sauron] Orc that is not roaming is strength +1": the definition made every [sauron] Orc stronger.
	@Test
	public void ASauronOrcStillRoamingAtThreeThreatsIsNotStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatsScenario();

		scn.MoveCompanionsToTable("aragorn", "gimli", "sam");

		var fires = scn.GetShadowCard("fires");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(fires);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetStrength(chieftain));

		// Site number 6 - 3 for the threats is 3, at site 2: the Chieftain is still roaming.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertEquals(7, scn.GetStrength(chieftain));

		// With a fourth threat his site number is 2 and he roams no longer.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertEquals(8, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheSiteNumberOfEachSauronOrcIsMinusOneForEachThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("dwarf"));
		scn.MoveCardsToSupportArea(fires);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, pillager, guard, hound);

		assertEquals(0, scn.GetThreats());
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertEquals(5, scn.GetMinionSiteNumber(pillager));
		assertEquals(6, scn.GetMinionSiteNumber(guard));
		assertEquals(4, scn.GetMinionSiteNumber(hound));

		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(5, scn.GetMinionSiteNumber(chieftain));
		assertEquals(4, scn.GetMinionSiteNumber(pillager));
		assertEquals(6, scn.GetMinionSiteNumber(guard));
		assertEquals(4, scn.GetMinionSiteNumber(hound));

		scn.AddThreats(1);
		assertEquals(2, scn.GetThreats());
		assertEquals(4, scn.GetMinionSiteNumber(chieftain));
		assertEquals(3, scn.GetMinionSiteNumber(pillager));
		// The [sauron] Uruk-hai and the [wraith] Orc keep their printed site numbers.
		assertEquals(6, scn.GetMinionSiteNumber(guard));
		assertEquals(4, scn.GetMinionSiteNumber(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtExactlyThreeThreatsEachSauronOrcThatIsNotRoamingIsStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var chieftain = scn.GetShadowCard("chieftain");
		var pillager = scn.GetShadowCard("pillager");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		// At site 6 none of the minions is roaming, whatever the threats.
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("dwarf"));
		scn.MoveCardsToSupportArea(fires);
		scn.MoveMinionsToTable(chieftain, pillager, guard, hound);
		scn.AddThreats(2);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(pillager, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(guard, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(hound, Keyword.ROAMING));
		// One threat short: printed strengths.
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(10, scn.GetStrength(pillager));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(hound));

		scn.AddThreats(1);
		assertEquals(3, scn.GetThreats());
		assertEquals(8, scn.GetStrength(chieftain));
		assertEquals(11, scn.GetStrength(pillager));
		// The [sauron] Uruk-hai and the [wraith] Orc are no [sauron] Orcs.
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
