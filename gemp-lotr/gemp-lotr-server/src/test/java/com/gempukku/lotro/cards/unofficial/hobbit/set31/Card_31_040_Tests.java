package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_040_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 ([shire]) as the Ring-bearer. Its
	// rivers are Esgaroth (site 6, also a sanctuary; a start-of-regroup offer to play an ally from the draw deck,
	// declined) and Erebor (site 7, also a mountain; a Shadow ability); Mirkwood (site 5) is a forest and no river
	// ("When the fellowship moves to Mirkwood, discard Gandalf": no Gandalf is laid).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barrels", "31_40");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, vitality 4; a fellowship ability
					put("thorin", "30_19");
					// Fili, Brother of Kili: [dwarven] Dwarf companion, vitality 3; a response to a [dwarven]
					// companion about to take a wound, never reached (exertions are no wounds)
					put("fili", "30_10");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, vitality 3; a fellowship ability
					put("balin", "30_2");
					// Dain Ironfoot, Lord of the Iron Hills: a [dwarven] Dwarf ALLY; Thorin +1, and a start-of-regroup
					// offer that costs a [dwarven] follower (none in play, so never made)
					put("dain", "32_2");

					// Goblin Runner: [moria] Orc; a text on being played, cheated in so it does not fire
					put("runner", "31_30");
					// Wicked Spider: a fierce [spider] Spider; a skirmish ability never reached
					put("spider", "31_55");
					// Dark Bats: [moria] Creature; a Shadow ability not used
					put("bats", "32_30");
					// Smaug, The Golden: [smaug] Dragon; discarded if not at a mountain (Erebor is one), and a
					// Shadow regroup ability left unused
					put("smaug", "30_66");
					// Hatred Rekindled: a [gundabad] condition in the Shadow support area, a Shadow card that is no
					// minion; a Shadow ability only
					put("hatred", "30_34");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BarrelsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Barrels
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Exert 2 [dwarven] companions to discard up to 2 minions (except Smaug) and draw 3 cards. If the fellowship is at a river, make the move limit +1 for this turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("barrels");

		assertEquals("Barrels", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtEreborItExertsTwoDwarvenCompanionsToDiscardUpToTwoMinionsButNotSmaugDrawsThreeAndRaisesTheMoveLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrels = scn.GetFreepsCard("barrels");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var fili = scn.GetFreepsCard("fili");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		var runner = scn.GetShadowCard("runner");
		var spider = scn.GetShadowCard("spider");
		var bats = scn.GetShadowCard("bats");
		var smaug = scn.GetShadowCard("smaug");
		var hatred = scn.GetShadowCard("hatred");
		scn.MoveCompanionsToTable(thorin, fili, balin);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.SkipToSite(6);
		// The move to Erebor, site 7, the turn's first.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		// The minions are laid in the Shadow phase and left unassigned, so they stand in the regroup phase.
		scn.MoveMinionsToTable(runner);
		scn.MoveMinionsToTable(spider);
		scn.MoveMinionsToTable(bats);
		scn.MoveMinionsToTable(smaug);
		scn.MoveCardsToSupportArea(hatred);
		scn.SetFreepsHand(barrels);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("runner"), scn.GetFreepsCard("spider"), scn.GetFreepsCard("bats"), scn.GetFreepsCard("hatred"));
		assertInZone(Zone.SHADOW_CHARACTERS, runner, spider, bats, smaug);
		assertInZone(Zone.SUPPORT, hatred);
		assertInHand(barrels);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertInZone(Zone.SHADOW_CHARACTERS, runner, spider, bats, smaug);
		assertTrue(scn.FreepsPlayAvailable(barrels));
		scn.FreepsPlayCard(barrels);

		// The cost: two of the three [dwarven] companions; not the [dwarven] ally, nor the [shire] Bilbo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, fili, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dain, bilbo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(thorin, fili);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(dain));

		// Minions of two cultures and three races, up to two of them; not Smaug, nor a Shadow condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, spider, bats));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smaug, hatred));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(runner, spider);

		assertInDiscard(barrels, runner, spider);
		assertInZone(Zone.SHADOW_CHARACTERS, bats, smaug);
		assertInZone(Zone.SUPPORT, hatred);
		// Barrels left the hand and three cards were drawn.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		// Erebor is a river.
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AtEsgarothTheMoveLimitIsPlusOneForTheRestOfTheTurnAndNotTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrels = scn.GetFreepsCard("barrels");
		var thorin = scn.GetFreepsCard("thorin");
		var fili = scn.GetFreepsCard("fili");
		scn.MoveCompanionsToTable(thorin, fili);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetFreepsHand(barrels);
		assertInHand(barrels);
		// The move to Esgaroth, site 6, the turn's first; its start-of-regroup offer is declined.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(scn.GetCurrentSite()));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(fili));
		assertTrue(scn.FreepsPlayAvailable(barrels));
		scn.FreepsPlayCard(barrels);

		// The two [dwarven] companions exert unasked; no minion to discard.
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(fili));
		assertInDiscard(barrels);
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// The second move: in the Shadow phase after it the limit is still 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		// In the next regroup phase a third move is offered, and made.
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetMoveCount());

		// The next turn, the other player's, has the limit of 2.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void AtMirkwoodTheMoveLimitStaysTwoAndWithNoMinionTheThreeCardsAreStillDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrels = scn.GetFreepsCard("barrels");
		var thorin = scn.GetFreepsCard("thorin");
		var fili = scn.GetFreepsCard("fili");
		scn.MoveCompanionsToTable(thorin, fili);

		scn.StartGame();
		scn.SkipToSite(4);
		// The move to Mirkwood, site 5, a forest and no river.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.SetFreepsHand(barrels);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("runner"), scn.GetFreepsCard("spider"), scn.GetFreepsCard("bats"), scn.GetFreepsCard("hatred"));
		scn.FreepsRefreshDecision();
		assertInHand(barrels);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());

		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(fili));
		assertTrue(scn.FreepsPlayAvailable(barrels));
		scn.FreepsPlayCard(barrels);

		// No minion in play: the exertions are paid, nothing is discarded or asked, and the three cards are drawn.
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetWoundsOn(fili));
		assertInDiscard(barrels);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotPlayableWithOneDwarvenCompanionAbleToExertBesideADwarvenAllyAndBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barrels = scn.GetFreepsCard("barrels");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var fili = scn.GetFreepsCard("fili");
		var dain = scn.GetFreepsCard("dain");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thorin, fili);
		scn.MoveAlliesToTable(dain);
		// Fili, vitality 3, is exhausted.
		scn.AddWoundsToChar(fili, 2);
		scn.MoveCardsToHand(barrels);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertInHand(barrels);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		// Thorin can exert, and so could the [dwarven] ally and the [shire] Ring-bearer, but only one [dwarven]
		// companion can: not playable.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(barrels));
	}
}
