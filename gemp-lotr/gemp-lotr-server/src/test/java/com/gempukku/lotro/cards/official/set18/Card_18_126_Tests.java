package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_126_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marchers", "18_126");

					put("hounding", "11_188");   // Hounding Uruk: [uruk-hai] Uruk-hai, cost 2, strength 5; +3 only while a companion is exhausted
					put("saruman", "17_116");    // Saruman, Master of the White Hand: an [uruk-hai] Wizard, cost 4, strength 8;
					                             // companions resistance -1, and a skirmish ability used only on purpose
					put("guard", "10_81");       // Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1, no other text
					put("endless", "13_161");    // Endless Assault: an [uruk-hai] condition; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHandMarchersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: White Hand Marchers
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>This minion is strength +1 for each other [uruk-hai] minion you can spot.
		*/

		var scn = GetScenario();

		var marchers = scn.GetFreepsCard("marchers");

		assertEquals("White Hand Marchers", marchers.getBlueprint().getTitle());
		assertNull(marchers.getBlueprint().getSubtitle());
		assertTrue(marchers.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, marchers.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, marchers.getBlueprint().getCulture());
		assertEquals(CardType.MINION, marchers.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, marchers.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(marchers, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(marchers, Keyword.DAMAGE));
		assertEquals(4, marchers.getBlueprint().getTwilightCost());
		assertEquals(9, marchers.getBlueprint().getStrength());
		assertEquals(2, marchers.getBlueprint().getVitality());
		assertEquals(5, marchers.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlus1ForEachOtherUrukHaiCultureMinionAndNotForAnUrukHaiOfAnotherCultureOrAnUrukHaiCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marchers = scn.GetShadowCard("marchers");
		var hounding = scn.GetShadowCard("hounding");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var endless = scn.GetShadowCard("endless");

		scn.StartGame();
		scn.MoveCardsToHand(hounding, saruman);
		scn.MoveMinionsToTable(marchers, guard);
		scn.MoveCardsToSupportArea(endless);
		scn.SetTwilight(10);
		assertInHand(hounding, saruman);
		assertInZone(Zone.SUPPORT, endless);
		scn.FreepsPass();

		// A [sauron] Uruk-hai and an [uruk-hai] condition count for nothing, and the Marchers do not count
		// themselves: printed 9.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(9, scn.GetStrength(marchers));

		// An [uruk-hai] Uruk-hai.
		scn.ShadowPlayCard(hounding);
		assertEquals(10, scn.GetStrength(marchers));
		// The minion counted is not strengthened itself: printed 5, with no companion exhausted.
		assertEquals(5, scn.GetStrength(hounding));

		// An [uruk-hai] minion who is no Uruk-hai.
		scn.ShadowPlayCard(saruman);
		assertEquals(11, scn.GetStrength(marchers));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiCultureWizardAloneMakesTheMarchersStrength10() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marchers = scn.GetShadowCard("marchers");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(marchers, saruman);

		// The count is the culture's, not the race's: Saruman is no Uruk-hai and is counted.
		assertEquals(10, scn.GetStrength(marchers));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
