package com.gempukku.lotro.cards.unofficial.pc.errata.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_15_112_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll1", "65_112");
					put("troll2", "65_112");

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
					// An [orc] card that is no minion.
					put("beckoning", "12_83");      // The Beckoning Shadow: support-area condition; nothing below 5 burdens
				}},
				// Site 2 is Ettenmoors: plains, Shadow number 2.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MountainTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mountain-troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 13
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: When you play this minion, you may discard 5 other [orc] minions from play to make it
		 * strength +10 and <b>fierce</b> until the end of the turn.
		 * Shadow: Remove (2) to play an [orc] Orc from your discard pile. It comes into play exhausted.
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
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		// Fierce is given by its text, not printed.
		assertFalse(scn.HasKeyword(card, Keyword.FIERCE));
	}

	@Test
	public void PlayedForEightAndRoamingAndWithFourOtherOrcMinionsInPlayMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 7 set + 1 companion + 2 for the site
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);
		// 8 to play + 2 roaming: its site number is 5 and this is site 2.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, pit, khazad, sentry, halftroll, runner);
		assertEquals(13, scn.GetStrength(troll));
		assertFalse(scn.HasKeyword(troll, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedFiveOrcMinionsMayBeDiscardedToMakeItStrengthPlus10AndFierce() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.MoveCardsToHand(troll1);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		// Six [orc] minions, one more than the five: Orcs, a unique Orc, a Half-troll and another Mountain-troll.
		scn.MoveMinionsToTable(troll2, pit1, pit2, khazad, sentry, halftroll, runner);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 companion + 2 for the site
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(troll1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll1));
		// The Mountain-troll already in play was not played.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll2));
		assertEquals(13, scn.GetStrength(troll1));
		assertFalse(scn.HasKeyword(troll1, Keyword.FIERCE));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(troll2, pit1, pit2, khazad, sentry, halftroll));
		// A [moria] Orc and an [orc] condition are not [orc] minions.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, beckoning));
		// The troll just played is not one of the five.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll1));
		assertEquals(5, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(pit1, pit2, khazad, sentry, halftroll);
		assertInZone(Zone.DISCARD, pit1, pit2, khazad, sentry, halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll1, troll2, runner);
		assertInZone(Zone.SUPPORT, beckoning);

		assertEquals(23, scn.GetStrength(troll1));
		assertTrue(scn.HasKeyword(troll1, Keyword.FIERCE));
		// "It" is the troll that was played, not every Mountain-troll.
		assertEquals(13, scn.GetStrength(troll2));
		assertFalse(scn.HasKeyword(troll2, Keyword.FIERCE));

		// 8 to play + 2 roaming, and nothing more for the trigger.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcMinionPlayedMakesNoOfferAndASecondTrollsOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.SetTwilight(12);
		scn.FreepsPass();

		// 12 set + 1 companion + 2 for the site
		assertEquals(15, scn.GetTwilight());

		// An [orc] minion played beside a Mountain-troll, with five other [orc] minions to discard, is not "this minion".
		scn.ShadowPlayCard(khazad2);
		assertInZone(Zone.SHADOW_CHARACTERS, khazad2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 3 to play + 2 roaming
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(troll2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(troll1));

		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, troll1, troll2, pit1, pit2, khazad1, khazad2, sentry, halftroll);
		assertEquals(13, scn.GetStrength(troll2));
		assertFalse(scn.HasKeyword(troll2, Keyword.FIERCE));
		assertEquals(13, scn.GetStrength(troll1));
		assertFalse(scn.HasKeyword(troll1, Keyword.FIERCE));
		// 8 to play + 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StrengthAndFierceLastThroughTheRegroupPhaseAndASecondMoveUntilTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll1");
		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var khazad1 = scn.GetShadowCard("khazad1");
		var khazad2 = scn.GetShadowCard("khazad2");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(troll);

		scn.StartGame();
		// Exactly five [orc] minions: taking the offer discards them all, and the troll is the only minion left.
		scn.MoveMinionsToTable(pit1, pit2, khazad1, khazad2, sentry);
		scn.SetTwilight(7);
		scn.FreepsPass();

		scn.ShadowPlayCard(troll);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(troll));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, pit1, pit2, khazad1, khazad2, sentry);
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(23, scn.GetStrength(troll));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		// Nobody is assigned to the troll, in the first round or the fierce one.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(23, scn.GetStrength(troll));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertEquals(23, scn.GetStrength(troll));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));

		// The second move was the last the turn allows: the regroup phase ends the turn, and the minion with it.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(23, scn.GetStrength(troll));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();
		// The Shadow player drew cards when he reconciled before the second move, and reconciles again now.
		scn.ShadowDeclineReconciliation();
		assertInZone(Zone.DISCARD, troll);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ShadowAbilityRemovesTwoToPlayAnOrcOrcFromTheDiscardPileExhausted() throws DecisionResultInvalidException, CardNotFoundException {
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
		// 2 for the ability + 4 for the Denizen + 2 roaming: its site number is 4 and this is site 2.
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, pit);
		assertInZone(Zone.DISCARD, sentry, halftroll, troll2, runner, beckoning);
		// Vitality 3: exhausted is two wounds, not one.
		assertEquals(2, scn.GetWoundsOn(pit));
		assertEquals(1, scn.GetVitality(pit));
		// Only the Orc played by the ability.
		assertEquals(0, scn.GetWoundsOn(troll1));
		assertEquals(0, scn.GetWoundsOn(khazad1));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// An [orc] Orc then played from hand comes into play as usual.
		scn.ShadowPlayCard(khazad2);
		assertInZone(Zone.SHADOW_CHARACTERS, khazad2);
		assertEquals(0, scn.GetWoundsOn(khazad2));
		// 3 to play + 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilThePoolPaysTheTwoAndTheOrcWithItsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll1");
		var pit = scn.GetShadowCard("pit1");
		scn.MoveCardsToDiscard(pit);

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// 4 set + 1 companion + 2 for the site: one short of 2 for the ability + 4 for the Denizen + 2 roaming.
		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(troll));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(troll));

		scn.ShadowUseCardAction(troll);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, pit);
		assertEquals(2, scn.GetWoundsOn(pit));
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
