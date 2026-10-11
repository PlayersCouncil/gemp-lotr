package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_15_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<String, String>()
				{{
					put("troll1", "15_112");
					put("troll2", "15_112");

					// [orc] Orcs.  None has text that matters away from an underground site or outside a skirmish.
					put("pit1", "11_117");          // Denizen of the Black Pit: cost 4, strength 10, vitality 3, site 4
					put("pit2", "11_117");
					put("khazad1", "11_115");       // Denizen of Khazad-dum: cost 3, strength 9, vitality 2, site 4
					put("khazad2", "11_115");
					put("sentry", "15_98");         // Black Gate Sentry: unique, cost 5, vitality 3, site 4; a skirmish ability
					// An [orc] minion that is no Orc.  Its start-of-assignment offer needs more companions than minions.
					put("halftroll", "14_12");      // Half-troll of Far Harad: cost 3, strength 8, vitality 2
					// An Orc minion of another culture.
					put("runner", "1_178");         // Goblin Runner: [moria] Orc; adds (2) only when it is played
					// [orc] cards that are no minions, in the support area.
					put("beckoning", "12_83");      // The Beckoning Shadow: nothing below 5 burdens
					// Chaotic Clash: "Each time you play a minion, it is twilight cost +1."  A cost never goes below 0,
					// so only a troll that costs 11 tells a discount of 10 from a larger one.
					put("clash", "17_68");
				}},
				// Site 2 is Ettenmoors: plains, Shadow number 2.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MountaintrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mountain-troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 10
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 22
		 * Vitality: 6
		 * Site Number: 5
		 * Game Text: When you play this minion, you may discard 5 [orc] minions from play to make it twilight cost
		 *   -10 and <b>fierce</b>.
		 *   <b>Shadow:</b> Remove (3) to play an [orc] Orc from your discard pile. Its twilight cost is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll1");

		assertEquals("Mountain-troll", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertEquals(10, card.getBlueprint().getTwilightCost());
		assertEquals(22, card.getBlueprint().getStrength());
		assertEquals(6, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		// Fierce is given by its text, not printed.
		assertFalse(scn.HasKeyword(card, Keyword.FIERCE));
	}

	@Test
	public void WhenPlayedFiveOrcMinionsMayBeDiscardedToMakeItTwilightCostMinus10AndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll1 = scn.GetShadowCard("troll1");
		var troll2 = scn.GetShadowCard("troll2");
		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var khazad = scn.GetShadowCard("khazad1");
		var sentry = scn.GetShadowCard("sentry");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		var beckoning = scn.GetShadowCard("beckoning");
		var clash = scn.GetShadowCard("clash");
		scn.MoveCardsToHand(troll1);
		scn.MoveCardsToSupportArea(beckoning, clash);

		scn.StartGame();
		// Six [orc] minions, one more than the five: Orcs, a unique Orc, a Half-troll and another Mountain-troll.
		scn.MoveMinionsToTable(troll2, pit1, pit2, khazad, sentry, halftroll, runner);
		scn.SetTwilight(17);
		scn.FreepsPass();

		// 17 set + 1 companion + 2 for the site
		assertEquals(20, scn.GetTwilight());
		// 10 + 1 for Chaotic Clash
		assertEquals(11, scn.GetTwilightCost(troll1));

		scn.ShadowPlayCard(troll1);
		assertTrue(scn.ShadowDecisionAvailable("Do you wish to choose cards to discard?"));
		assertEquals(20, scn.GetTwilight());

		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll2, pit1, pit2, khazad, sentry, halftroll));
		// A [moria] Orc and two [orc] conditions are not [orc] minions.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, beckoning, clash));
		assertEquals(5, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(pit1, pit2, khazad, sentry, halftroll);
		assertInZone(Zone.DISCARD, pit1, pit2, khazad, sentry, halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll1, troll2, runner);
		assertInZone(Zone.SUPPORT, beckoning, clash);

		// 11 - 10 to play, + 2 roaming: its site number is 5 and this is site 2.  A discount of 11 would leave 18.
		assertEquals(17, scn.GetTwilight());
		assertTrue(scn.HasKeyword(troll1, Keyword.FIERCE));
		// "It" is the troll that was played, not every Mountain-troll.
		assertFalse(scn.HasKeyword(troll2, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FierceLastsThroughTheRegroupPhaseAndASecondMoveUntilTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll1");
		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var khazad1 = scn.GetShadowCard("khazad1");
		var khazad2 = scn.GetShadowCard("khazad2");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(troll);

		scn.StartGame();
		// Exactly five [orc] minions: a yes discards them all, and the troll is the only minion left.
		scn.MoveMinionsToTable(pit1, pit2, khazad1, khazad2, sentry);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(20, scn.GetTwilight());
		scn.ShadowPlayCard(troll);

		assertTrue(scn.ShadowDecisionAvailable("Do you wish to choose cards to discard?"));
		assertFalse(scn.HasKeyword(troll, Keyword.FIERCE));

		scn.ShadowChooseYes();
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
		assertInZone(Zone.DISCARD, pit1, pit2, khazad1, khazad2, sentry);
		// 20, less 2 roaming: the troll was otherwise free.
		assertEquals(18, scn.GetTwilight());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		// Nobody is assigned to the troll, in the first round or the fierce one.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		// The second move was the last the turn allows: the regroup phase ends the turn, and the minion with it.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();
		// The Shadow player drew cards when he reconciled before the second move, and reconciles again now.
		scn.ShadowDeclineReconciliation();
		assertInZone(Zone.DISCARD, troll);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	@Ignore("Q-0275: the text names no time for fierce. As built the troll is fierce for as long as it is in play; by the default for an effect with no stated duration (Q-0018) it would be fierce to the end of the Shadow phase it was played in.")
	public void FierceEndsWithTheShadowPhaseTheTrollWasPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll1");
		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var khazad1 = scn.GetShadowCard("khazad1");
		var khazad2 = scn.GetShadowCard("khazad2");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(troll);

		scn.StartGame();
		scn.MoveMinionsToTable(pit1, pit2, khazad1, khazad2, sentry);
		scn.SetTwilight(17);
		scn.FreepsPass();

		scn.ShadowPlayCard(troll);
		scn.ShadowChooseYes();
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertFalse(scn.HasKeyword(troll, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherOrcMinionPlayedIsOfferedNoDiscountAndTheTrollsOwnMayBeDeclinedForTheFullCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll1 = scn.GetShadowCard("troll1");
		var troll2 = scn.GetShadowCard("troll2");
		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var khazad1 = scn.GetShadowCard("khazad1");
		var khazad2 = scn.GetShadowCard("khazad2");
		var sentry = scn.GetShadowCard("sentry");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(khazad2, troll2);

		scn.StartGame();
		scn.MoveMinionsToTable(troll1, pit1, pit2, khazad1, sentry, halftroll);
		scn.SetTwilight(14);
		scn.FreepsPass();

		// 14 set + 1 companion + 2 for the site
		assertEquals(17, scn.GetTwilight());

		// An [orc] minion played beside a Mountain-troll, with five other [orc] minions in play, is not "this minion".
		scn.ShadowPlayCard(khazad2);
		assertInZone(Zone.SHADOW_CHARACTERS, khazad2);
		// 3 to play + 2 roaming
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(troll2);
		assertTrue(scn.ShadowDecisionAvailable("Do you wish to choose cards to discard?"));

		scn.ShadowChooseNo();
		assertInZone(Zone.SHADOW_CHARACTERS, troll1, troll2, pit1, pit2, khazad1, khazad2, sentry, halftroll);
		// 10 to play + 2 roaming: its site number is 5 and this is site 2.
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(troll2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troll1, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourOrcMinionsInPlayNoDiscountIsOfferedAndItIsPlayedForTenAndRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll1");
		var pit = scn.GetShadowCard("pit1");
		var khazad = scn.GetShadowCard("khazad1");
		var sentry = scn.GetShadowCard("sentry");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(troll);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		// Four [orc] minions, and beside them an Orc of another culture and an [orc] condition: neither is a fifth.
		scn.MoveMinionsToTable(pit, khazad, sentry, halftroll, runner);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 companion + 2 for the site: one short of 10 to play + 2 roaming.
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.HAND, troll);
		assertFalse(scn.ShadowPlayAvailable(troll));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(troll));

		// Nothing is asked: the troll is played at once, for all of it.
		scn.ShadowPlayCard(troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troll, pit, khazad, sentry, halftroll, runner);
		assertInZone(Zone.SUPPORT, beckoning);
		assertFalse(scn.HasKeyword(troll, Keyword.FIERCE));
	}

	@Test
	@Ignore("F-0482: with five [orc] minions to discard the troll is offered only when the pool holds 7 (each of the five counted as 1 off its cost of 10, and 2 roaming); its cost at the discount is 0, so 2 should do")
	public void IsOfferedWithOnlyItsRoamingPenaltyInThePoolWhenFiveOrcMinionsCanBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll1");
		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var khazad1 = scn.GetShadowCard("khazad1");
		var sentry = scn.GetShadowCard("sentry");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(troll);

		scn.StartGame();
		scn.MoveMinionsToTable(pit1, pit2, khazad1, sentry, halftroll);
		scn.FreepsPass();
		// 10 - 10 to play, + 2 roaming.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.HAND, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(troll));
	}

	@Test
	@Ignore("F-0483: with a pool that cannot pay 10 and roaming, the player is still asked whether to discard, and a no empties the pool and puts the troll in the discard pile; the discount is the only way to pay and should be taken without a question")
	public void WithAPoolThatCannotPayInFullTheFiveAreDiscardedWithoutAQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll1");
		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var khazad1 = scn.GetShadowCard("khazad1");
		var sentry = scn.GetShadowCard("sentry");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(troll);

		scn.StartGame();
		scn.MoveMinionsToTable(pit1, pit2, khazad1, sentry, halftroll);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// 4 set + 1 companion + 2 for the site: 5 short of 10 to play + 2 roaming.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);
		assertFalse(scn.ShadowDecisionAvailable("Do you wish to choose cards to discard?"));
		assertInZone(Zone.DISCARD, pit1, pit2, khazad1, sentry, halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		// 2 roaming, and nothing for the troll.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityRemovesThreeToPlayAnOrcOrcFromTheDiscardPileAtTwilightCostMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll1 = scn.GetShadowCard("troll1");
		var troll2 = scn.GetShadowCard("troll2");
		var pit = scn.GetShadowCard("pit1");
		var sentry = scn.GetShadowCard("sentry");
		var khazad1 = scn.GetShadowCard("khazad1");
		var khazad2 = scn.GetShadowCard("khazad2");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		var beckoning = scn.GetShadowCard("beckoning");
		// Two [orc] Orcs in the discard pile, one of them unique.  Beside them an [orc] Half-troll, an [orc] Troll,
		// a [moria] Orc and an [orc] condition.  A third [orc] Orc is in hand, not in the discard pile.
		scn.MoveCardsToDiscard(pit, sentry, halftroll, troll2, runner, beckoning);
		scn.MoveCardsToHand(khazad2);

		scn.StartGame();
		scn.MoveMinionsToTable(troll1, khazad1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(troll1));

		scn.ShadowUseCardAction(troll1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(pit, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, troll2, runner, beckoning, khazad2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(pit);
		// 3 for the ability; 4 - 2 for the Denizen; 2 roaming: its site number is 4 and this is site 2.
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, pit);
		assertEquals(0, scn.GetWoundsOn(pit));
		assertInZone(Zone.DISCARD, sentry, halftroll, troll2, runner, beckoning);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// "Its": an [orc] Orc then played from hand costs what is printed on it.
		scn.ShadowPlayCard(khazad2);
		assertInZone(Zone.SHADOW_CHARACTERS, khazad2);
		// 3 to play + 2 roaming
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolPaysTheThreeAndTheOrcWithItsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll1");
		var pit = scn.GetShadowCard("pit1");
		scn.MoveCardsToDiscard(pit);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 companion + 2 for the site: one short of 3 for the ability + 4 - 2 for the Denizen + 2 roaming.
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(troll));

		scn.ShadowUseCardAction(troll);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, pit);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcOrcInYourOwnDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll1 = scn.GetShadowCard("troll1");
		var troll2 = scn.GetShadowCard("troll2");
		var pit = scn.GetShadowCard("pit1");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		var beckoning = scn.GetShadowCard("beckoning");
		var theirPit = scn.GetFreepsCard("pit1");
		// In the Shadow player's discard pile: an [orc] Half-troll, an [orc] Troll, a [moria] Orc and an [orc]
		// condition.  The only [orc] Orc in a discard pile is the other player's, in his own.
		scn.MoveCardsToDiscard(halftroll, troll2, runner, beckoning, theirPit);

		scn.StartGame();
		scn.MoveMinionsToTable(troll1);
		scn.SetTwilight(17);
		scn.FreepsPass();

		assertEquals(20, scn.GetTwilight());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertFalse(scn.ShadowActionAvailable(troll1));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// With an [orc] Orc of his own there, it is.
		scn.MoveCardsToDiscard(pit);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(troll1));
		assertEquals(20, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
