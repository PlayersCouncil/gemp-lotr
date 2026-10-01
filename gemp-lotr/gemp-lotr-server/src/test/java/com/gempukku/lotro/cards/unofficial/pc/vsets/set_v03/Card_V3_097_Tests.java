package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_097_Tests
{

// ----------------------------------------
// OMINOUS SKY TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sky", "103_97");
					put("sky2", "103_97");       // Second copy for discard pile retrieval tests
					put("orc", "1_271");          // Orc Soldier - [Sauron] Orc, cost 2
					put("orc2", "1_271");
					put("troll", "6_106");        // Troll of Udun, [Sauron] Troll, cost 10
					put("hollowing", "3_54");     // Hollowing of Isengard - [Isengard] Condition (non-Twilight)
					put("uruk", "1_151");         // Uruk Savage - Uruk-hai (not Orc or Troll), cost 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OminousSkyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ominous Sky
		 * Unique: 3
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight. Each time you play an Orc or Troll, add (1) (limit once per phase).
		 * 	<b>Shadow</b> <i>or</i> <b>Regroup</b>: Remove (2) to choose a Shadow condition from your discard pile. Shuffle it into your draw deck (or you may play it if it is twilight).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sky");

		assertEquals("Ominous Sky", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(3, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}



// ========================================
// TRIGGER TESTS - Playing Orc/Troll
// ========================================

	@Test
	public void PlayingAnOrcAddsOneTwilightWithoutHinderingThis() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata: the trigger is no longer optional and no longer costs hindering this condition
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToHand(orc);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		var twilight = scn.GetTwilight();

		// Orc Soldier costs 2 + 2 roaming = 4 twilight
		scn.ShadowPlayCard(orc);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(sky));
		assertEquals(twilight - 4 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingATrollAddsOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToHand(troll);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		var twilight = scn.GetTwilight();
		int cost = troll.getBlueprint().getTwilightCost() + 2; // roaming
		scn.ShadowPlayCard(troll);

		assertFalse(scn.IsHindered(sky));
		assertEquals(twilight - cost + 1, scn.GetTwilight());
	}

	@Test
	public void AddsTwilightOnlyOncePerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToHand(orc, orc2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		var twilight = scn.GetTwilight();

		scn.ShadowPlayCard(orc);
		assertEquals(twilight - 4 + 1, scn.GetTwilight());

		// second Orc this phase: no bonus
		scn.ShadowPlayCard(orc2);
		assertEquals(twilight - 4 + 1 - 4, scn.GetTwilight());
		assertFalse(scn.IsHindered(sky));
	}

	@Test
	public void EachCopyHasItsOwnLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		var orc2 = scn.GetShadowCard("orc2");
		scn.MoveCardsToSupportArea(sky, sky2);
		scn.MoveCardsToHand(orc, orc2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		var twilight = scn.GetTwilight();

		scn.ShadowPlayCard(orc);
		// two identical required triggers may be offered in either order
		if (scn.ShadowDecisionAvailable("Required"))
			scn.ShadowChooseAction("0");
		assertEquals(twilight - 4 + 2, scn.GetTwilight());

		scn.ShadowPlayCard(orc2);
		assertEquals(twilight - 4 + 2 - 4, scn.GetTwilight());
	}

	@Test
	public void OminousSkyDoesNotTriggerWhenPlayingUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		// Uruk-hai are not Orcs or Trolls
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToHand(uruk);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		var twilight = scn.GetTwilight();
		// Uruk Savage costs 2 + 2 roaming = 4 twilight
		scn.ShadowPlayCard(uruk);

		assertEquals(twilight - 4, scn.GetTwilight());
	}

	@Test
	public void HinderedOminousSkyDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(sky);
		scn.HinderCard(sky);
		scn.MoveCardsToHand(orc);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		var twilight = scn.GetTwilight();
		scn.ShadowPlayCard(orc);

		assertEquals(twilight - 4, scn.GetTwilight());
	}

// ========================================
// ACTIVATED ABILITY TESTS - Shadow/Regroup
// ========================================

	@Test
	public void OminousSkyAbilityAvailableDuringShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(hollowing);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.ShadowActionAvailable(sky));
	}

	@Test
	public void OminousSkyAbilityAvailableDuringRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(hollowing);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(sky));
	}

	@Test
	public void OminousSkyAbilityNotAvailableDuringManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var hollowing = scn.GetShadowCard("hollowing");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(hollowing);
		scn.MoveMinionsToTable(orc); // Need a minion to reach Maneuver

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.ShadowActionAvailable(sky));
	}

	@Test
	public void OminousSkyAbilityRequires2Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var orc = scn.GetShadowCard("orc");
		var hollowing = scn.GetShadowCard("hollowing");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(hollowing);
		scn.MoveCardsToHand(orc);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(orc);
		// Ominous Sky's (now required) trigger adds (1)

		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(sky));
	}

	@Test
	public void OminousSkyAbilityShufflesNonTwilightConditionIntoDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var hollowing = scn.GetShadowCard("hollowing"); // Non-Twilight condition
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(hollowing);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertInZone(Zone.DISCARD, hollowing);
		var twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(sky);
		// Only one Shadow condition in discard, so auto-selected

		assertEquals(twilight - 2, scn.GetTwilight());
		assertInZone(Zone.DECK, hollowing);
	}

	@Test
	public void OminousSkyAbilityPlayingTwilightConditionDoesNotShuffleIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(sky2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();
		var twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(sky);
		// Costs 2 twilight, sky2 auto-selected
		assertEquals(twilight - 2, scn.GetTwilight());

		scn.ShadowChooseYes(); // Choose to play sky2

		// sky2 costs 2 twilight to play
		assertEquals(twilight - 2 - 2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sky2); // Played, not shuffled
	}

	@Test
	public void OminousSkyAbilityDecliningToPlayTwilightConditionShufflesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(sky2);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();
		scn.SetTwilight(10);

		scn.ShadowUseCardAction(sky);
		assertEquals(8, scn.GetTwilight());

		scn.ShadowChooseNo(); // Decline to play sky2

		assertEquals(8, scn.GetTwilight()); // No additional cost
		assertInZone(Zone.DECK, sky2); // Shuffled into deck
	}

	@Test
	public void OminousSkyAbilityDoesNotOfferToPlayUnplayableTwilightCondition() throws DecisionResultInvalidException, CardNotFoundException {
		// If the Twilight condition isn't playable (e.g., not enough twilight), just shuffle it
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(sky2);
		scn.MoveCardsToHand(orc);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(orc);
		// Ominous Sky's (now required) trigger adds (1)

		assertEquals(2, scn.GetTwilight());

		scn.ShadowUseCardAction(sky);
		// After paying 2, twilight = 0, can't afford sky2's cost of 1

		// Should NOT be offered to play since it's not playable
		assertFalse(scn.ShadowDecisionAvailable("Would you like to play"));
		assertInZone(Zone.DECK, sky2); // Just shuffled
	}
}
