package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_051_Tests
{

	// The Fellowship-block path: site 4, Balin's Tomb, is underground; site 2, Ettenmoors, is a plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "2_51");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, damage +1 and nothing else
					put("spearman", "2_65");      // Goblin Spearman: [moria] Orc, cost 2; damage +2 only with a Goblin Spear
					put("fear", "1_278");         // Strength Born of Fear: a [sauron] support-area condition, no minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBalrogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: The Balrog, Durin's Bane
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 12
		 * Type: Minion
		 * Subtype: Balrog
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 4
		 * Game Text: Damage +1. Fierce.
		 * While you can spot The Balrog, discard all other minions. Skip the archery phase.
		 * Discard The Balrog if not underground.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balrog");

		assertEquals("The Balrog", card.getBlueprint().getTitle());
		assertEquals("Durin's Bane", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.BALROG, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(12, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedUndergroundItDiscardsEveryOtherMinionAndEachMinionPlayedAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var spearman = scn.GetShadowCard("spearman");
		var fear = scn.GetShadowCard("fear");
		scn.MoveCardsToHand(balrog, spearman);

		scn.StartGame();
		scn.SkipToSite(3);
		// The move to Balin's Tomb, site 4: underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A [moria] Orc and an [isengard] Uruk-hai in play, and a Shadow condition that is no minion.
		scn.MoveMinionsToTable(troop, savage);
		scn.MoveCardsToSupportArea(fear);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);

		scn.ShadowPlayCard(balrog);
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertInDiscard(troop, savage);
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A minion played while the Balrog is in play is discarded as it comes in.
		assertTrue(scn.ShadowPlayAvailable(spearman));
		scn.ShadowPlayCard(spearman);
		assertInDiscard(spearman);
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheArcheryPhaseIsSkippedWhileTheBalrogIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(balrog);

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.PassCurrentPhaseActions();
		// From the maneuver phase straight to the assignment phase.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
	}

	@Test
	public void WithoutTheBalrogTheArcheryPhaseFollowsTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(troop);

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.PassCurrentPhaseActions();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void PlayedAwayFromAnUndergroundSiteItIsDiscardedAndTheOtherMinionsWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(balrog);

		scn.StartGame();
		// The move to Ettenmoors, site 2: a plains.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(troop);
		// 12 + 2 for roaming
		scn.SetTwilight(14);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(balrog));

		// Its text is carried out in the order printed (Ketura on the Watcher in the Water, F-0139): the other
		// minions go first, and then The Balrog itself.
		scn.ShadowPlayCard(balrog);
		assertInDiscard(balrog, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
