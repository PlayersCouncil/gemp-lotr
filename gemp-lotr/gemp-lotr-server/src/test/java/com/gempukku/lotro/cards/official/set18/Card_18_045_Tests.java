package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_045_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "18_45");
					put("bow2", "18_45");
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] Man ranger, not unique; a regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man ranger; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger; strength +3 against a Nazgul only
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man who is no ranger; Knight is its whole text
					put("swordsman", "1_117");  // Swordsman of the Northern Kingdom: a [gondor] Skirmish event
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession for Aragorn
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunadansBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Dúnadan's Bow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: +1
		 * Game Text: Bearer must be a [gondor] ranger.<br><b>Archery:</b> Discard 2 [gondor] cards from hand to make the fellowship archery total +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Dúnadan's Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnAGondorRangerAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bow = scn.GetFreepsCard("bow");
		var ranger = scn.GetFreepsCard("ranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(ranger, aragorn, arwen, knight);
		scn.MoveCardsToHand(bow);

		scn.StartGame();

		assertInHand(bow);
		assertTrue(scn.FreepsPlayAvailable(bow));
		scn.FreepsPlayCard(bow);
		// [gondor] rangers of two titles; not a ranger of another culture, nor a [gondor] Man who is no ranger, nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(bow, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityDiscardsTwoGondorCardsFromHandToMakeTheFellowshipArcheryTotalOneMoreForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var bow2 = scn.GetFreepsCard("bow2");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var swordsman = scn.GetFreepsCard("swordsman");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(ranger);
		scn.AttachCardsTo(ranger, bow);
		// [gondor] cards of three types (a companion, an event and two possessions), and an [elven] card.
		scn.MoveCardsToHand(knight, swordsman, sword, bow2, arwen);

		scn.StartGame();
		assertAttachedTo(bow, ranger);
		assertInHand(knight, swordsman, sword, bow2, arwen);
		scn.MoveMinionsToTable(troop);

		// The phase before, with the cost payable: not offered.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bow));
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// Nobody in the fellowship is an archer.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(bow));

		scn.FreepsUseCardAction(bow);
		// Exactly two, of the [gondor] cards only.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, swordsman, sword, bow2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen));
		scn.FreepsChooseCards(knight, swordsman);

		assertInDiscard(knight, swordsman);
		assertInHand(sword, bow2, arwen);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));

		// No limit: with two [gondor] cards left it is offered again, and adds another 1.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bow));
		scn.FreepsUseCardAction(bow);
		assertInDiscard(sword, bow2);
		assertInHand(arwen);
		assertEquals(2, scn.GetFreepsArcheryTotal());

		scn.ShadowPass();
		scn.FreepsPass();
		scn.ShadowPass();

		// The two archery wounds go to the only minion; the +1s lasted the archery phase and no longer.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertAttachedTo(bow, ranger);
	}

	@Test
	public void NotOfferedWithOnlyOneGondorCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(ranger);
		scn.AttachCardsTo(ranger, bow);
		// Two cards in hand, one of them [elven].
		scn.MoveCardsToHand(knight, arwen);

		scn.StartGame();
		assertAttachedTo(bow, ranger);
		assertInHand(knight, arwen);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(bow));
	}
}
