package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_142_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("voice", "51_142");
					put("voice2", "51_142");    // a second copy: an [isengard] card that is no minion

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer, vitality 4

					put("uruk1", "1_151");      // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5, damage +1
					put("uruk2", "1_151");
					put("forger1", "3_56");     // Isengard Forger: [isengard] Orc; a Shadow ability used only on purpose
					put("forger2", "3_56");
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TraitorsVoiceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Traitor's Voice
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, spot an [isengard] minion.
		 * Regroup: Spot 4 [isengard] minions, an exhausted Ring-bearer, or 5 burdens to make the move limit for this turn -1 (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("voice");

		assertEquals("Traitor's Voice", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedToTheSupportAreaOnceAnIsengardMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var voice2 = scn.GetShadowCard("voice2");
		var uruk1 = scn.GetShadowCard("uruk1");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(voice, uruk1);
		scn.MoveCardsToSupportArea(voice2);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An Uruk-hai of another culture, and an [isengard] card that is no minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(voice));

		scn.ShadowPlayCard(uruk1);
		assertTrue(scn.ShadowPlayAvailable(voice));

		scn.ShadowPlayCard(voice);
		assertInZone(Zone.SUPPORT, voice);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedToTheSupportAreaOnceAnIsengardOrcCanBeSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var voice2 = scn.GetShadowCard("voice2");
		var forger1 = scn.GetShadowCard("forger1");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(voice, forger1);
		scn.MoveCardsToSupportArea(voice2);

		scn.StartGame();
		scn.MoveMinionsToTable(sentry);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// An Uruk-hai of another culture, and an [isengard] card that is no minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(voice));

		scn.ShadowPlayCard(forger1);
		assertTrue(scn.ShadowPlayAvailable(voice));

		scn.ShadowPlayCard(voice);
		assertInZone(Zone.SUPPORT, voice);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedOneShortOfEachRequirementAndTheFellowshipMayMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var forger1 = scn.GetShadowCard("forger1");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Three [isengard] minions and a [sauron] Uruk-hai; Frodo (vitality 4) one wound short of exhausted, and
		// Sam, who is Ring-bound but not the Ring-bearer, exhausted; 4 burdens.
		scn.MoveMinionsToTable(uruk1, uruk2, forger1, sentry);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 3);
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(voice));

		scn.ShadowPass();
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void SpottingFourIsengardMinionsMakesTheMoveLimitOneLessForThisTurnOncePerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var forger1 = scn.GetShadowCard("forger1");
		var forger2 = scn.GetShadowCard("forger2");
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Two [isengard] Uruk-hai and two [isengard] Orcs.
		scn.MoveMinionsToTable(uruk1, uruk2, forger1, forger2);
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(1, scn.GetMoveCount());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(voice));

		scn.ShadowUseCardAction(voice);
		assertEquals(1, scn.GetMoveLimit());

		// Once per phase: the four minions can still be spotted.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(voice));

		// The fellowship has moved as often as the limit allows: it is not asked to move again and the turn ends
		// (the other player's fellowship phase is next), and that turn's move limit is 2 again.
		scn.ShadowPass();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void SpottingAnExhaustedRingBearerMakesTheMoveLimitOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Vitality 4: exhausted with 3 wounds.
		scn.AddWoundsToChar(frodo, 3);
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(voice));

		scn.ShadowUseCardAction(voice);
		assertEquals(1, scn.GetMoveLimit());

		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void SpottingFiveBurdensMakesTheMoveLimitOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var voice = scn.GetShadowCard("voice");
		scn.MoveCardsToSupportArea(voice);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(voice));

		scn.ShadowUseCardAction(voice);
		assertEquals(1, scn.GetMoveLimit());

		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
