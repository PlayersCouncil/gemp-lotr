package com.gempukku.lotro.mechanics.hinder;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * What a hindered card bears is still in play, and cannot touch its bearer.
 *
 * Comprehensive Rules 5.0, "hinder": "Cards which are borne by hindered cards cannot interact with their bearer
 * but are otherwise still in play and active; they can be interacted with and use their game text normally."
 * Its examples: Gimli's Battle Axe on a hindered Gimli gives him nothing ("he cannot gain damage or strength
 * bonuses") and still does what does not rely on him; and "Gwemegil is borne by Arwen, who becomes hindered.
 * Gwemegil's skirmish special ability cannot exert Arwen, but it can still be activated and discard cards from
 * hand to make nobody strength +1."  The same entry: "Hindered characters thus cannot be exerted, wounded, or
 * healed."
 *
 * A hindered card cannot be given something to bear either.  It has no title, culture, type or race for a card
 * to find it by (the same entry; a card borne by a hindered companion is not "borne by a companion", Ketura on
 * Q-0108), and every item names its bearer by one of those, so no play and no transfer can choose it.
 *
 * In every test the hindering is only how the board is laid, and is done by the rig.
 */
public class CardsBorneByAHinderedCardTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Aragorn, Ranger of the North: a [gondor] Man, vitality 4; a maneuver ability never used here
					put("aragorn", "1_89");
					// Unexpected Visitor: [gondor] condition on a [gondor] companion.  "Regroup: Discard this
					// condition to discard a minion and remove (4)."  Its other text gives each minion an
					// assignment ability, which nobody uses here.
					put("visitor", "7_126");
					// Aragorn's Bow: ranged weapon, "Bearer must be Aragorn.  He is an archer.  Archery: Exert
					// Aragorn to wound a minion; Aragorn does not add to the fellowship archery total."
					put("bow", "1_90");
					// Armor: "Bearer must be a Man."  Cost 1.  Its text is about wounds in a skirmish.
					put("armor", "1_92");
					// Boromir, Defender of Minas Tirith: the other Man; a fellowship ability never used here
					put("boromir", "3_122");

					// Arwen, Daughter of Elrond: strength 6, vitality 3; her text is about Nazgul
					put("arwen", "1_30");
					// Gwemegil: hand weapon, strength +2, "Bearer must be Arwen.  She is damage +1.  Skirmish:
					// Exert Arwen or discard 2 cards from hand to make her strength +1."
					put("gwemegil", "1_47");
					put("elf1", "1_53");        // Lorien Elf: four cards for a hand, never played
					put("elf2", "1_53");
					put("elf3", "1_53");
					put("elf4", "1_53");

					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACardBorneByAHinderedCompanionKeepsAnAbilityThatDoesNotTouchItsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var visitor = scn.GetFreepsCard("visitor");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, visitor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.HinderCard(aragorn);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(7);

		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(visitor));
		assertAttachedTo(visitor, aragorn);
		assertTrue(scn.FreepsActionAvailable(visitor));

		scn.FreepsUseCardAction(visitor);
		assertInZone(Zone.DISCARD, visitor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop1);

		assertInZone(Zone.DISCARD, troop1);
		assertInZone(Zone.SHADOW_CHARACTERS, troop2);
		// 7 - 4
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.IsHindered(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ACardBorneByAHinderedCompanionCannotExertItsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsActionAvailable(bow));
		// "He is an archer."
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();
		// The Bow is neither hindered nor gone; the one thing its ability needs is to exert its bearer
		assertFalse(scn.IsHindered(bow));
		assertAttachedTo(bow, aragorn);
		assertFalse(scn.FreepsActionAvailable(bow));
		// Nor does he get its keyword
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.RestoreCard(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(bow));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(troop1));
		scn.FreepsUseCardAction(bow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2));
		scn.FreepsChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	// The rules' own example.  With Arwen hindered the "exert Arwen" half of Gwemegil's cost cannot be paid, so
	// the other half is all there is and nothing is asked about which; and its effect finds nobody to strengthen.
	@Test
	public void ACardBorneByAHinderedCompanionIsStillUsedAtACostThatSparesItsBearerAndThenGivesHimNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var elf3 = scn.GetFreepsCard("elf3");
		var elf4 = scn.GetFreepsCard("elf4");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(arwen, gwemegil);

		scn.StartGame();
		scn.MoveCardsToHand(elf1, elf2, elf3, elf4);
		scn.MoveMinionsToTable(troop);
		scn.HinderCard(arwen);

		// The skirmish is Aragorn's: Arwen, hindered, could not be in one
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsHindered(arwen));
		assertFalse(scn.IsHindered(gwemegil));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(gwemegil));

		scn.FreepsUseCardAction(gwemegil);
		// No "Exert Arwen or discard": straight to which two cards
		assertFalse(scn.FreepsChoiceAvailable("Exert Arwen"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(elf1, elf2, elf3, elf4));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(elf1, elf2);

		assertInZone(Zone.DISCARD, elf1, elf2);
		assertInZone(Zone.HAND, elf3, elf4);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// "To make nobody strength +1": brought back by the rig, she has what Gwemegil prints and no more.
		// 6 + 2
		scn.ShadowPass();
		scn.RestoreCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));

		// The control: with Arwen restored the same ability asks which cost, exerts her, and strengthens her
		scn.FreepsRefreshDecision();
		scn.FreepsUseCardAction(gwemegil);
		assertTrue(scn.FreepsChoiceAvailable("Exert Arwen"));
		assertTrue(scn.FreepsChoiceAvailable("Discard 2 cards from hand"));
		scn.FreepsChoose("Exert Arwen");
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(9, scn.GetStrength(arwen));
		assertInZone(Zone.HAND, elf3, elf4);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NoItemIsPlayedOnOrTransferredToAHinderedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		// Aragorn is the only Man
		assertTrue(scn.FreepsPlayAvailable(armor));

		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsPlayAvailable(armor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second Man beside him: Armor can be played again, and goes to Boromir with nothing asked
		scn.MoveCompanionsToTable(boromir);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(armor));
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, boromir);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship phase's transfer: the hindered Aragorn is nobody to hand it to
		assertFalse(scn.FreepsTransferAvailable(armor));

		scn.RestoreCard(aragorn);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsTransferAvailable(armor));

		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsTransferAvailable(armor));

		scn.RestoreCard(aragorn);
		scn.FreepsRefreshDecision();
		scn.FreepsTransferCard(armor);
		assertAttachedTo(armor, aragorn);
		// 1 + 1 for the transfer
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
