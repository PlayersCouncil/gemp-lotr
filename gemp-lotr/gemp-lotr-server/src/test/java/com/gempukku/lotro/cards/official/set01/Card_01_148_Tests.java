package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lieutenant", "1_148");
					put("lieutenant2", "1_148");
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, damage +1 and nothing else
					// Saruman, Keeper of Isengard: an [isengard] minion who is no Uruk-hai (a Wizard), strength 8; he makes
					// Uruk-hai fierce and cannot be assigned, neither of which changes a strength
					put("saruman", "3_68");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukLieutenantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Lieutenant
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>This minion is strength +1 for each other Uruk-hai you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lieutenant");

		assertEquals("Uruk Lieutenant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlusOneOnceAnotherUrukHaiIsPlayedAndNotForAMinionWhoIsNoUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var saruman = scn.GetShadowCard("saruman");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToHand(party);

		scn.StartGame();
		scn.MoveMinionsToTable(lieutenant, saruman);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Saruman is an [isengard] minion but no Uruk-hai: nothing to count.
		assertEquals(8, scn.GetStrength(lieutenant));

		scn.ShadowPlayCard(party);
		assertInZone(Zone.SHADOW_CHARACTERS, party);
		// 8 printed + 1 for the Raiding Party; the Raiding Party itself is left at its own 9.
		assertEquals(9, scn.GetStrength(lieutenant));
		assertEquals(9, scn.GetStrength(party));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CountsEachOtherUrukHaiOfAnyCultureAndAnotherLieutenantButNotItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lieutenant = scn.GetShadowCard("lieutenant");
		var lieutenant2 = scn.GetShadowCard("lieutenant2");
		var guard = scn.GetShadowCard("guard");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(lieutenant, lieutenant2, guard, saruman);

		// Each Lieutenant: 8 printed + 1 for the other Lieutenant + 1 for the [sauron] Cirith Ungol Guard; not
		// itself, and not Saruman.
		assertEquals(10, scn.GetStrength(lieutenant));
		assertEquals(10, scn.GetStrength(lieutenant2));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
