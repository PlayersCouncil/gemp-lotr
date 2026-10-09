package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * The filter word transferableTo(<filter>): a card in play that a card's effect could transfer now to one of the
 * cards the inner filter accepts.
 *
 * Comprehensive Rules 4.2, "transfer": "An artifact or possession may be transferred only to a character who may
 * bear it", and "If an effect can transfer a card to another 'eligible bearer,' you must obey that card's
 * requirements on both what may bear it and when it may be transferred."  Comprehensive Rules 4.2, "bearer": a
 * character bears one item of a class.
 *
 * So the word accepts a card only when nothing says it cannot be transferred, and when one of the cards the inner
 * filter accepts is a bearer its own text allows, is not the bearer it has, has room for an item of its class, and
 * is not kept from bearing it by another card.  It is what the Transfer effect with "checkTarget: true" goes on to
 * ask, and it is for the requirement beside such an effect: by Ketura's rule that an optional action with no cost
 * that would do nothing is not offered (Q-0034), "you may transfer ... to another eligible bearer" is withheld when
 * nothing could move (F-0324, Race Text 94_4; Ketura, 2026-10-09: "a 'transferable to' filter is perfectly
 * kosher").
 *
 * Shown with a card of the tests' own, Test Porter 0_8023: "Fellowship: If a possession could be transferred to a
 * Man, choose up to 1 such possession and add (1)."  Its requirement and its choice are both written
 * possession,transferableTo(man).
 */
public class TransferableToFilterTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Test Porter (the tests' own card): "Fellowship: If a possession could be transferred to a Man,
					// choose up to 1 such possession and add (1)."
					put("porter", "0_8023");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability that is never used
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man; Knight is its whole text
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf; a skirmish ability that is never used
					put("guard", "1_7");        // Dwarf Guard: a Dwarf with no text once in play
					// Warrior of Rohan: a [rohan] Man.  "Possessions cannot be transferred from this companion to
					// another character."  His fellowship ability plays a possession from hand, and no hand holds one.
					put("warrior", "17_104");
					put("armor1", "1_92");      // Armor: a possession (armor).  "Bearer must be a Man."
					put("armor2", "1_92");
					put("cloak", "1_42");       // Elven Cloak: a possession (cloak).  "Bearer must be a companion."
					put("rsword", "1_112");     // Ranger's Sword: a possession (hand weapon).  "Bearer must be Aragorn."
					put("darmor", "1_8");       // Dwarven Armor: a possession (armor).  "Bearer must be a Dwarf."
					put("bow", "7_94");         // Gondor Bow: a possession (ranged weapon).  "Bearer must be a [gondor] Man."
					// Athelas: a possession of no class.  "Bearer must be a [gondor] Man."  Its fellowship ability
					// discards it, and is never used.
					put("athelas", "1_94");
					// Anduril, Flame of the West: an artifact (hand weapon).  "Bearer must be Aragorn. ... He ...
					// cannot bear other weapons."  Its own ability needs a site 2 or a site 5.
					put("anduril", "7_79");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void APossessionIsAcceptedWhenACardTheFilterAcceptsMayBearItByItsOwnText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var porter = scn.GetFreepsCard("porter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor1");
		var cloak = scn.GetFreepsCard("cloak");
		var rsword = scn.GetFreepsCard("rsword");
		var darmor = scn.GetFreepsCard("darmor");
		scn.MoveCompanionsToTable(aragorn, knight, gimli, guard);
		scn.MoveCardsToSupportArea(porter);
		scn.AttachCardsTo(aragorn, armor, cloak, rsword);
		scn.AttachCardsTo(gimli, darmor);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(porter));
		scn.FreepsUseCardAction(porter);

		// The Knight is a Man and a companion: Armor ("a Man") and the Cloak ("a companion") could go to him.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, cloak));
		// Ranger's Sword is Aragorn's alone.  Dwarven Armor could go to the Dwarf Guard, and he is no Man: an
		// eligible bearer whom the inner filter does not accept is no reason to accept the card.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rsword, darmor));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBearerItHasIsNoPlaceForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var porter = scn.GetFreepsCard("porter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(porter);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();

		// Aragorn is a [gondor] Man, and Athelas has no class to fill his place for it: he is refused only because
		// he bears it already.  Gimli and Frodo are no Men.
		assertFalse(scn.FreepsActionAvailable(porter));

		scn.MoveCompanionsToTable(knight);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(porter));
		scn.FreepsUseCardAction(porter);
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas));
		scn.FreepsChooseCard(athelas);

		// The word asks and moves nothing.
		assertEquals(aragorn, athelas.getAttachedTo());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABearerWithAnItemOfItsClassIsNoPlaceForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var porter = scn.GetFreepsCard("porter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var armor = scn.GetFreepsCard("armor1");
		var knightsArmor = scn.GetFreepsCard("armor2");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(porter);
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(porter));

		// One armor to a character (CR 4.2 "bearer"): each Man now has his own, and neither Armor can move.
		scn.AttachCardsTo(knight, knightsArmor);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(porter));
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void APossessionThatCannotBeTransferredIsRefusedThoughAnotherCouldBearIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var porter = scn.GetFreepsCard("porter");
		var warrior = scn.GetFreepsCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var armor = scn.GetFreepsCard("armor1");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(warrior, aragorn, knight);
		scn.MoveCardsToSupportArea(porter);
		scn.AttachCardsTo(warrior, armor);

		scn.StartGame();

		// Aragorn and the Knight are Men with no armor, and the Warrior's possessions cannot be transferred from him.
		assertFalse(scn.FreepsActionAvailable(porter));

		// A possession may still be transferred TO him: Aragorn's Cloak could go to the Warrior or the Knight.
		scn.AttachCardsTo(aragorn, cloak);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(porter));
		scn.FreepsUseCardAction(porter);
		assertTrue(scn.FreepsHasCardChoiceAvailable(cloak));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor));
		scn.FreepsChooseCard(cloak);

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABearerWhomAnotherCardKeepsFromBearingItIsNoPlaceForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var porter = scn.GetFreepsCard("porter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var bow = scn.GetFreepsCard("bow");
		var anduril = scn.GetFreepsCard("anduril");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(porter);
		scn.AttachCardsTo(knight, bow);

		scn.StartGame();

		// Aragorn is the other [gondor] Man, and bears no ranged weapon.
		assertTrue(scn.FreepsActionAvailable(porter));

		// "He ... cannot bear other weapons."
		scn.AttachCardsTo(aragorn, anduril);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsActionAvailable(porter));
		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void AHinderedBearerIsNoPlaceForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var porter = scn.GetFreepsCard("porter");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var armor = scn.GetFreepsCard("armor1");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(porter);
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(porter));

		scn.HinderCard(knight);
		scn.FreepsRefreshDecision();

		assertTrue(scn.IsHindered(knight));
		assertFalse(scn.FreepsActionAvailable(porter));

		scn.RestoreCard(knight);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(porter));
	}
}
