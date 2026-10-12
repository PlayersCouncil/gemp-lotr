package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_015_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gwaihir", "101_15");

					// Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4, vitality 4, and the
					// Frodo signet.  His text only changes his own strength.
					put("gandalf", "1_72");
					// Gandalf's Cart: a [gandalf] possession for the support area, with Gandalf's name in its
					// title.  Its fellowship ability needs an artifact or possession in hand, and there is none.
					put("cart", "1_73");

					// Aragorn, Ranger of the North: [gondor] Man, vitality 4, the Gandalf signet.  A maneuver
					// ability, never used here.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3, the Gandalf signet.  A skirmish ability,
					// never used here.
					put("gimli", "1_13");
					// Dwarf Guard: a companion with no signet, vitality 2.
					put("guard", "1_7");

					// Shadow cards for a discard pile, of four cultures and three types.  None is ever played.
					put("runner", "1_178");     // Goblin Runner: [moria] minion
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] minion
					put("hate", "1_250");       // Hate: [sauron] maneuver event
					put("breath", "1_207");     // Black Breath: [wraith] condition
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, for the other discard pile
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GwaihirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Gwaihir, Lord of Eagles
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Eagle
		 * Home: 4
		 * Strength: 8
		 * Vitality: 2
		 * Game Text: To play, spot Gandalf.
		 * Each time the fellowship moves to an opponent's site, you may heal a companion with the Gandalf signet.
		 * Regroup: Discard Gwaihir to make the move limit for this turn +1; the Shadow player may take up to 2
		 * Shadow cards into hand from their discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gwaihir");

		assertEquals("Gwaihir", card.getBlueprint().getTitle());
		assertEquals("Lord of Eagles", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.EAGLE, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 4)));
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var cart = scn.GetFreepsCard("cart");
		// A [gandalf] card in play that is not Gandalf, though it has his name in its title.
		scn.MoveCardsToSupportArea(cart);
		scn.MoveCardsToHand(gwaihir, gandalf);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gwaihir));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gwaihir));

		scn.FreepsPlayCard(gwaihir);
		assertInZone(Zone.SUPPORT, gwaihir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MovingToAnOpponentsSiteHealsAChosenCompanionWithTheGandalfSignet() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveAlliesToTable(gwaihir);
		scn.MoveCompanionsToTable(gandalf, aragorn, gimli, guard);

		scn.StartGame();

		// Aragorn and Gimli have the Gandalf signet.  Frodo and this Gandalf have the Frodo signet, the Dwarf
		// Guard has none, and Gwaihir is a [gandalf] ally.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(gandalf, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(gwaihir, 1);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The move to site 2, which the opponent puts down.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(scn.P2(), scn.GetCurrentSite().getOwner());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));

		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, gandalf, guard, gwaihir));

		scn.FreepsChooseCard(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(gwaihir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningHealsNoOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveAlliesToTable(gwaihir);
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedOnAMoveToYourOwnSiteAndIsOnTheNextMoveToAnOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(gwaihir);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 2);

		// Site 2 is already on the adventure path, and it came from the adventure deck of Gwaihir's owner.
		scn.MoveCardToAdventurePath(scn.GetFreepsSite(2));

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(scn.P1(), scn.GetCurrentSite().getOwner());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// No minions, so the turn goes on to the regroup phase and the choice of a second move.
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The move to site 3, which the opponent puts down.
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(scn.P2(), scn.GetCurrentSite().getOwner());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));

		// Aragorn is the only wounded companion with the Gandalf signet.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HealsTheSameCompanionAgainOnASecondMoveToAnOpponentsSiteInTheSameTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(gwaihir);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.AddWoundsToChar(aragorn, 2);

		// The move from site 1 to site 2.
		scn.FreepsPass();
		assertEquals(scn.P2(), scn.GetCurrentSite().getOwner());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The move from site 2 to site 3.
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(scn.P2(), scn.GetCurrentSite().getOwner());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsGwaihirAndAllowsAThirdMoveThatTurnAndNoFourth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveAlliesToTable(gwaihir);

		var runner = scn.GetShadowCard("runner");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToDiscard(runner, raiders);

		scn.StartGame();

		// Not in the fellowship phase.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gwaihir));

		// With no minion in play the turn goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(gwaihir));
		assertInZone(Zone.SUPPORT, gwaihir);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsUseCardAction(gwaihir);
		assertInZone(Zone.DISCARD, gwaihir);
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(3, scn.GetMoveLimit());

		// The Shadow player is asked for the cards at once, with no yes/no before it, in words that say none need
		// be chosen (Q-0143), and takes nothing: the choice is answered with no cards.
		assertTrue(scn.ShadowDecisionAvailable("you may choose none"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, raiders));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();
		assertInDiscard(runner, raiders);
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The second move, from site 2 to site 3.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());

		// A regroup phase later, with Gwaihir gone, a third move is still allowed.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());

		// A fourth is not: when the regroup phase is over the turn ends without the choice being put.
		scn.SkipToMovementDecision();
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		// On the next turn the move limit is 2 again, and on the next turn of Gwaihir's owner.
		assertEquals(2, scn.GetMoveLimit());
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerTakesTwoShadowCardsFromTheirOwnDiscardPileIntoHandAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveAlliesToTable(gwaihir);

		var runner = scn.GetShadowCard("runner");
		var raiders = scn.GetShadowCard("raiders");
		var hate = scn.GetShadowCard("hate");
		var breath = scn.GetShadowCard("breath");
		// A Free Peoples card in the Shadow player's discard pile, and a Shadow card in the discard pile of
		// Gwaihir's owner.  The Shadow player's own Goblin Patrol Troop is in their draw deck.
		var theirGuard = scn.GetShadowCard("guard");
		var myTroop = scn.GetFreepsCard("troop");
		scn.MoveCardsToDiscard(runner, raiders, hate, breath, theirGuard, myTroop);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DECK, scn.GetShadowCard("troop"));

		scn.FreepsUseCardAction(gwaihir);
		// It is the Shadow player who is asked, and nobody else.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, raiders, hate, breath));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(theirGuard, myTroop));
		// Four to choose from, and no more than 2 may be taken.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCardBPFromSelection(raiders, breath);
		assertInHand(raiders, breath);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(runner, hate, theirGuard, myTroop);
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetFreepsDiscardCount());       // the Goblin Patrol Troop and Gwaihir
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayTakeJustOneOfSeveralShadowCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveAlliesToTable(gwaihir);

		var runner = scn.GetShadowCard("runner");
		var raiders = scn.GetShadowCard("raiders");
		var hate = scn.GetShadowCard("hate");
		scn.MoveCardsToDiscard(runner, raiders, hate);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());

		scn.FreepsUseCardAction(gwaihir);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, raiders, hate));

		// "Up to 2": one is enough.
		scn.ShadowChooseCardBPFromSelection(hate);
		assertInHand(hate);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(runner, raiders);
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoShadowCardInTheirDiscardPileTheShadowPlayerIsAskedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gwaihir = scn.GetFreepsCard("gwaihir");
		scn.MoveAlliesToTable(gwaihir);

		// The only card in the Shadow player's discard pile is a Free Peoples card.
		var theirGuard = scn.GetShadowCard("guard");
		scn.MoveCardsToDiscard(theirGuard);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetMoveLimit());

		scn.FreepsUseCardAction(gwaihir);
		// The move limit is raised all the same, and the regroup phase goes on with nothing put to anyone.
		assertInZone(Zone.DISCARD, gwaihir);
		assertEquals(3, scn.GetMoveLimit());
		assertInDiscard(theirGuard);
		assertEquals(0, scn.GetShadowHandCount());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
