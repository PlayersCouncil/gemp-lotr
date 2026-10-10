package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arrows", "1_68");
					put("arrows2", "1_68");

					put("legolas", "1_50");       // Legolas, Greenleaf: Elf companion and an archer in print, vitality 3; an archery ability of his own, used only on purpose
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion and no archer, vitality 3; strength against a Nazgul only
					put("rumil", "1_57");         // Rumil, Elven Protector: an Elf and an archer, but an ally; an archery ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion who is no Elf; a maneuver ability used only on purpose
					put("bow", "1_41");           // Elven Bow: "Bearer must be an Elf. Bearer is an archer."

					put("troop", "1_177");        // Goblin Patrol Troop: a minion with no text, so that there is an archery phase
					// Cards for the Shadow player's hand.
					put("savage", "1_151");       // Uruk Savage
					put("runner", "1_178");       // Goblin Runner
					put("marksman", "1_176");     // Goblin Marksman
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWhiteArrowsofLorienStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The White Arrows of Lórien
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Game Text: <b>Tale</b>. Bearer must be an Elf companion.
		 * 	<b>Archery:</b> If bearer is an archer, exert bearer to make an opponent discard 2 cards at random from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arrows");

		assertEquals("The White Arrows of Lórien", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForOneOnAnElfCompanionAndOnNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var rumil = scn.GetFreepsCard("rumil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, arwen, aragorn);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(arrows);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(arrows));

		scn.FreepsPlayCard(arrows);
		// An Elf companion who is an archer and one who is not are both offered. Rumil is an Elf and an archer,
		// but an ally; Aragorn and Frodo are companions who are no Elves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rumil, aragorn, frodo));
		scn.FreepsChooseCard(arwen);

		assertAttachedTo(arrows, arwen);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcherBearerExertsAndTheOpponentDiscardsTwoCardsFromHandWithNoChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var arrows2 = scn.GetFreepsCard("arrows2");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(legolas, arrows);
		scn.AttachCardsTo(arwen, arrows2);
		scn.MoveCardsToFreepsHand("aragorn", "rumil");
		scn.MoveCardsToShadowHand("savage", "runner", "marksman", "aragorn");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		// The copy Legolas bears is offered: he is an archer. The copy Arwen bears is not, though an archer
		// stands beside her and she could exert.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(arrows));
		assertFalse(scn.FreepsActionAvailable(arrows2));

		scn.FreepsUseCardAction(arrows);

		// Its bearer exerts, and nobody else.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		// Two of the opponent's four cards are gone, to his own discard pile, and nobody was asked which: the
		// game has gone on to his turn to act.
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertAttachedTo(arrows, legolas);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ABearerWhoIsAnArcherByAnElvenBowMayUseItAndIsTheOneWhoExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(arwen, arrows, bow);
		scn.MoveCardsToShadowHand("savage", "runner", "marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(arrows));

		scn.FreepsUseCardAction(arrows);

		// Arwen bears it, so Arwen exerts: not Legolas, the archer in print, who bears nothing.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AgainstAHandOfOneThatCardIsDiscardedAndAgainstAnEmptyHandTheBearerStillExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		var shadowSavage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, arrows);
		scn.MoveCardsToShadowHand("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetShadowHandCount());
		assertInHand(shadowSavage);

		// As much of "discard 2" as can be done.
		scn.FreepsUseCardAction(arrows);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(shadowSavage);
		assertEquals(1, scn.GetShadowDiscardCount());

		// With no card left to discard the exertion can still be paid, and the ability used for nothing.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(arrows));

		scn.FreepsUseCardAction(arrows);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheArcherBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrows = scn.GetFreepsCard("arrows");
		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, arrows);
		scn.MoveCardsToShadowHand("savage", "runner", "marksman");

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(legolas, 2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(3, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arrows));

		scn.RemoveWoundsFromChar(legolas, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(arrows));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
