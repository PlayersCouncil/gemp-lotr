package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_310_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_310");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRoamingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hatred", "7_310");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2, site 6; its text answers a weapon played on it
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, cost 2, site 6; a skirmish ability only
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai, cost 2, site 6; Damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, cost 1, site 4; its text is about being played
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that 2 threats can lie
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SauronsHatredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Sauron's Hatred
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [sauron] Orc.<br><b>Shadow:</b> Remove a threat to play a [sauron] Orc without paying any roaming penalty.<br><b>Regroup:</b> Spot a [sauron] Orc and remove (4) to add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Sauron's Hatred", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShadowAbilityRemovesAThreatToPlayASauronOrcForItsPrintedCostAndTheOrcIsStillRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hatred = scn.GetShadowCard("hatred");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(hatred);
		scn.MoveCardsToHand(chieftain, soldier, sentry, runner);

		scn.StartGame();
		// Two companions hold the 2 threats.
		scn.AddThreats(2);
		// The fellowship moves to site 2, where every minion in the Shadow hand is roaming (sites 6, 6, 6 and 4).
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// With 4 in the pool the Chieftain is played from hand in the ordinary way: 2 to play + 2 roaming.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(chieftain));

		// With exactly its printed cost in the pool it is not, and the Hatred's ability is offered all the same.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(chieftain));
		assertFalse(scn.ShadowPlayAvailable(soldier));
		assertTrue(scn.ShadowActionAvailable(hatred));
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.HAND, chieftain);

		scn.ShadowUseCardAction(hatred);
		// The two [sauron] Orcs are the choice.  The [sauron] Uruk-hai (cost 2) and the [moria] Orc (cost 1) are
		// not, though the pool would pay for either once the penalty is waived.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		// One threat is the cost.
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);
		assertInZone(Zone.HAND, soldier, sentry, runner);
		// 2 to play and no roaming penalty: 2 - 2.
		assertEquals(0, scn.GetTwilight());
		// The penalty is waived, not the roaming: a minion of site 6 at site 2.
		assertTrue(scn.HasKeyword(chieftain, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
