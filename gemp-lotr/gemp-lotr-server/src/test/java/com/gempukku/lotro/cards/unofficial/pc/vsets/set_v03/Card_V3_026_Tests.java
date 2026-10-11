package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_026_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blind", "103_26");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability never used here
					put("eomer", "4_267");      // Éomer, Third Marshal of Riddermark: a [rohan] Man, vitality 3; his text needs a skirmish
					put("ithilien", "4_130");   // Ranger of Ithilien: a [gondor] Man who is Ring-bound, vitality 3; his text needs a skirmish
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] Man who is an ally, vitality 2; a fellowship ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man to play from hand for (3); a skirmish ability only
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: an Elf, vitality 3; stronger at sites 6 to 8 and nothing else
					put("deadman", "10_27");    // Dead Man of Dunharrow: a [gondor] companion who is a Wraith and no Man; his text is about being played

					put("grima", "4_154");      // Gríma, Wormtongue: a Man who is an [isengard] minion, vitality 3; a maneuver ability only
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("armory", "1_173");     // Goblin Armory: a Shadow condition in the support area; answers [moria] weapons played and wounds on [moria] Orcs
					// Axe Strike, a skirmish event: cards to hold and to draw
					put("fodder1", "1_3");
					put("fodder2", "1_3");
					put("fodder3", "1_3");
					put("fodder4", "1_3");
					put("fodder5", "1_3");
					put("fodder6", "1_3");
					put("fodder7", "1_3");
					put("fodder8", "1_3");
					put("fodder9", "1_3");
					put("fodder10", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KeepHimBlindtoAllElseThatMovesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Keep Him Blind to All Else That Moves
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this, heal each Free Peoples Man.
		 * 	Shadow: The Shadow player may discard this to reconcile their hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blind");

		assertEquals("Keep Him Blind to All Else That Moves", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedForTwoEachFreePeoplesManIsHealedOnceAndNobodyElseAndNotAgainWhenAnotherCardIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blind = scn.GetFreepsCard("blind");
		var aragorn = scn.GetFreepsCard("aragorn");
		var eomer = scn.GetFreepsCard("eomer");
		var ithilien = scn.GetFreepsCard("ithilien");
		var barliman = scn.GetFreepsCard("barliman");
		var boromir = scn.GetFreepsCard("boromir");
		var haldir = scn.GetFreepsCard("haldir");
		var deadman = scn.GetFreepsCard("deadman");
		var grima = scn.GetShadowCard("grima");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, eomer, ithilien, haldir, deadman);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(blind, boromir);

		scn.StartGame();
		// A Man of the Shadow's: on the table beside this condition only when an effect plays it outside the
		// fellowship phase, so he is put there by the rig.
		scn.MoveMinionsToTable(grima);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(eomer, 1);
		scn.AddWoundsToChar(ithilien, 1);
		scn.AddWoundsToChar(barliman, 1);
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(deadman, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(grima, 1);

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(blind));

		scn.FreepsPlayCard(blind);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, blind);
		// One wound off each Man: a [gondor] companion, a [rohan] companion, a Ring-bound companion, an ally.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(0, scn.GetWoundsOn(ithilien));
		assertEquals(0, scn.GetWoundsOn(barliman));
		// An Elf, a [gondor] Wraith, a Hobbit, and a Man who is not of the Free Peoples.
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(deadman));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(grima));
		// Nothing was asked.
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another card played while this one is in play is not "this".
		scn.FreepsPlayCard(boromir);

		// 2 + 3 for Boromir
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscardItInTheShadowPhaseToDrawHisOwnHandUpToEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blind = scn.GetFreepsCard("blind");
		var fodder1 = scn.GetShadowCard("fodder1");
		scn.MoveCardsToSupportArea(blind);
		scn.MoveCardsToFreepsHand("fodder1", "fodder2");
		scn.MoveCardsToShadowHand("fodder1", "fodder2", "fodder3");

		scn.StartGame();

		// Its owner has no use of it.
		assertFalse(scn.FreepsActionAvailable(blind));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.FreepsPass();

		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int freepsDiscard = scn.GetFreepsDiscardCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetShadowHandCount());
		assertInZone(Zone.SUPPORT, blind);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(blind));

		scn.ShadowUseCardAction(blind);

		// Discarded to its owner's pile.
		assertInZone(Zone.DISCARD, blind);
		assertEquals(freepsDiscard + 1, scn.GetFreepsDiscardCount());
		// The reconciling begins with the discard a player may make.
		assertTrue(scn.ShadowHasCardChoiceAvailable(fodder1));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowDeclineReconciliation();

		// 3 held + 5 drawn
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 5, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		// The other player's hand and deck are as they were.
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertFalse(scn.ShadowActionAvailable(blind));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ReconcilingAHandOfMoreThanEightDiscardsDownToEightAndDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blind = scn.GetFreepsCard("blind");
		var fodder1 = scn.GetShadowCard("fodder1");
		var fodder2 = scn.GetShadowCard("fodder2");
		scn.MoveCardsToSupportArea(blind);
		scn.MoveCardsToShadowHand("fodder1", "fodder2", "fodder3", "fodder4", "fodder5",
				"fodder6", "fodder7", "fodder8", "fodder9", "fodder10");

		scn.StartGame();
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertEquals(10, scn.GetShadowHandCount());

		scn.ShadowUseCardAction(blind);

		// Ten held: whether or not the discard a player may make first is taken, two cards have to go, and
		// the game asks for the two together.
		assertInZone(Zone.DISCARD, blind);
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(fodder1, fodder2);

		assertInZone(Zone.DISCARD, fodder1, fodder2);
		assertEquals(shadowDiscard + 2, scn.GetShadowDiscardCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void LeftUnusedItStaysInPlayAndIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blind = scn.GetFreepsCard("blind");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(blind);
		scn.MoveCardsToShadowHand("fodder1", "fodder2", "fodder3");

		scn.StartGame();
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(blind));

		scn.ShadowPass();

		assertInZone(Zone.SUPPORT, blind);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blind));

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blind));
	}

	@Test
	public void ReconcilingRestoresTheShadowPlayersHinderedCardsAndNotTheOtherPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blind = scn.GetFreepsCard("blind");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetShadowCard("armory");
		var fodder1 = scn.GetShadowCard("fodder1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(blind, armory);
		scn.MoveCardsToShadowHand("fodder1", "fodder2", "fodder3");

		scn.StartGame();
		scn.HinderCard(armory, aragorn);
		scn.FreepsPass();

		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();
		assertTrue(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(aragorn));

		scn.ShadowUseCardAction(blind);
		// The discard a player may make first, taken this time.
		scn.ShadowChooseCard(fodder1);

		// 2 held + 6 drawn
		assertInZone(Zone.DISCARD, fodder1);
		assertEquals(shadowDiscard + 1, scn.GetShadowDiscardCount());
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 6, scn.GetShadowDeckCount());
		// His own hindered card is restored; the Free Peoples player has not reconciled.
		assertFalse(scn.IsHindered(armory));
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
