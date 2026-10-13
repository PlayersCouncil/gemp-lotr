package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Unique cards and cards that are not unique.
 *
 * Comprehensive Rules 4.2, "unique": "Many character, possession, artifact, and condition cards represent a thing
 * that there is only one of.  Such a card has a dot before the card title and is unique.  When a unique card is in
 * play and currently active, you cannot play another card that has the same title."  And: "Two cards represent the
 * same thing if they have the same card title (even if their subtitles or collector's info are different) ...  Two
 * cards can have the same card title even if they are in different languages."
 *
 * Comprehensive Rules 4.2, "dead pile": "When you have a unique companion or ally in your dead pile, you cannot
 * play another copy of that card, or any other card with the same title.  You may play another copy of a
 * non-unique card that is in your dead pile."
 *
 * Comprehensive Rules 5.0, "non-unique": "All cards that do not have a single dot before their card title are
 * non-unique.  This means that all players may have multiple copies of those cards in play at one time.  If a card
 * has 2 dots it is restricted to 2 copies in play".  Three dots are three copies (War-beacon 103_35, Ominous Sky
 * 103_97).
 *
 * Comprehensive Rules 5.0, "hinder": "If a hindered card is unique, you cannot play another copy of that card with
 * the same title.  ...  If you have a hindered Aragorn, and play a second Aragorn, this would be an inconsistent
 * game state once the Free Peoples player reconciles, as there would be 2 Aragorns in play.  Thus, the play is
 * disallowed."
 *
 * The Players' Council writes some titles without their accents (Ulaire Enquea for Úlairë Enquëa); the game
 * compares titles with the accents taken off, so that the two are one title.
 *
 * None of the cards' own texts is used in any test here.
 */
public class UniquenessTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn1", "1_89");    // Aragorn, Ranger of the North: unique, cost 4
					put("aragorn2", "1_89");
					put("exile", "1_365");      // Aragorn, King in Exile: the same title under another subtitle, cost 4
					put("heir", "3_38");        // Aragorn, Heir to the White City: a third subtitle, cost 4
					put("merry", "1_302");      // Merry, Friend to Sam: a unique companion of another title, a Hobbit, cost 1

					put("asfaloth1", "1_31");   // Asfaloth: unique possession, a mount, cost 2; "Bearer must be an Elf"
					put("asfaloth2", "1_31");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: another Elf (on her Asfaloth costs 2 less)
					put("bow", "1_41");         // Elven Bow: a possession that is not unique, cost 1; "Bearer must be an Elf"

					put("guest1", "1_297");     // Hobbit Party Guest: an ally that is not unique, cost 1, home site 1
					put("guest2", "1_297");
					put("guest3", "1_297");
					put("guest4", "1_297");
					put("sword1", "1_299");     // Hobbit Sword: a possession that is not unique, cost 1; "Bearer must be a Hobbit"
					put("sword2", "1_299");

					put("enquea", "1_231");     // Úlairë Enquëa, Lieutenant of Morgul: unique Nazgul, cost 6, site 3
					put("thrall", "60_68");     // Ulaire Enquea, Thrall of The One: the title without its accents; cost 6, site 3
					put("attea", "1_229");      // Úlairë Attëa, Keeper of Dol Guldur: a unique Nazgul of another title; cost 6, site 3

					put("gate1", "103_25");     // Inner Gate: two dots; a Free Peoples condition for the support area, cost 2
					put("gate2", "103_25");
					put("gate3", "103_25");
					put("fury1", "103_39");     // Savage Fury: two dots; a Shadow condition for the support area, cost 1
					put("fury2", "103_39");
					put("fury3", "103_39");

					// War-beacon: three dots; a Free Peoples possession for the support area, cost 1.  "To play, spot an
					// unbound Man."  Its two fellowship abilities are never used.
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");
					put("beacon4", "103_35");
					// Ominous Sky: three dots; a Shadow condition for the support area, cost 2.  It answers an Orc or a
					// Troll played (there is none), and its Shadow ability is never used.  These are the only two
					// cards printed with three dots.
					put("sky1", "103_97");
					put("sky2", "103_97");
					put("sky3", "103_97");
					put("sky4", "103_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhileAUniqueCompanionIsInPlayNoOtherCardOfItsTitleCanBePlayedWhateverItsSubtitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn1 = scn.GetFreepsCard("aragorn1");
		var aragorn2 = scn.GetFreepsCard("aragorn2");
		var exile = scn.GetFreepsCard("exile");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToHand(aragorn1, aragorn2, exile, merry);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(aragorn1));
		assertTrue(scn.FreepsPlayAvailable(aragorn2));
		assertTrue(scn.FreepsPlayAvailable(exile));
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(aragorn1);

		assertInZone(Zone.FREE_CHARACTERS, aragorn1);
		assertEquals(4, scn.GetTwilight());
		// Neither the second copy nor the Aragorn of another subtitle; the unique companion of another title
		// is still to be played, so it is the title that stands in the way and nothing about the hand or the pool.
		assertFalse(scn.FreepsPlayAvailable(aragorn2));
		assertFalse(scn.FreepsPlayAvailable(exile));
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// It is the card in play that bars them: with him gone from play, both can be played again.
		scn.MoveCardsToDiscard(aragorn1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DISCARD, aragorn1);
		assertTrue(scn.FreepsPlayAvailable(aragorn2));
		assertTrue(scn.FreepsPlayAvailable(exile));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherCardOfAHinderedUniqueCardsTitleCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn1");
		var heir = scn.GetFreepsCard("heir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(heir, merry);

		scn.StartGame();

		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.FreepsPlayAvailable(heir));
		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.HinderCard(aragorn);
		scn.FreepsRefreshDecision();

		// The reading is the same as before the hindering, and that is the check: a hindered card has no title
		// for any card to spot, and a game that looked for "an Aragorn in play" in that way would now offer the
		// other one.
		assertTrue(scn.IsHindered(aragorn));
		assertFalse(scn.FreepsPlayAvailable(heir));
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With the hindered card out of play nothing bars him.
		scn.MoveCardsToDiscard(aragorn);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DISCARD, aragorn);
		assertTrue(scn.FreepsPlayAvailable(heir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardOfAUniqueTitleInItsOwnersDeadPileBarsAnotherAndOneThatIsNotUniqueDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn1 = scn.GetFreepsCard("aragorn1");
		var aragorn2 = scn.GetFreepsCard("aragorn2");
		var exile = scn.GetFreepsCard("exile");
		var merry = scn.GetFreepsCard("merry");
		var guest1 = scn.GetFreepsCard("guest1");
		var guest2 = scn.GetFreepsCard("guest2");
		var othersAragorn = scn.GetShadowCard("aragorn1");
		// Neither a discard pile nor the other player's dead pile is "your dead pile".
		scn.MoveCardsToDiscard(aragorn1, guest1);
		scn.MoveCardsToDeadPile(othersAragorn);
		scn.MoveCardsToHand(aragorn2, exile, merry, guest2);

		scn.StartGame();

		assertInZone(Zone.DISCARD, aragorn1, guest1);
		assertInZone(Zone.DEAD, othersAragorn);
		assertTrue(scn.FreepsPlayAvailable(aragorn2));
		assertTrue(scn.FreepsPlayAvailable(exile));
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertTrue(scn.FreepsPlayAvailable(guest2));

		scn.MoveCardsToDeadPile(aragorn1, guest1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DEAD, aragorn1, guest1);
		// The unique companion in his owner's dead pile bars his other copy and the Aragorn of another subtitle.
		assertFalse(scn.FreepsPlayAvailable(aragorn2));
		assertFalse(scn.FreepsPlayAvailable(exile));
		// The unique companion of another title is still to be played, and so is another copy of the ally that is
		// not unique, though a copy of it is in the dead pile too.
		assertTrue(scn.FreepsPlayAvailable(merry));
		assertTrue(scn.FreepsPlayAvailable(guest2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondCopyOfAUniquePossessionCannotBePlayedThoughAnotherBearerCouldTakeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var asfaloth1 = scn.GetFreepsCard("asfaloth1");
		var asfaloth2 = scn.GetFreepsCard("asfaloth2");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.MoveCardsToHand(asfaloth1, asfaloth2, bow);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(asfaloth1));
		assertTrue(scn.FreepsPlayAvailable(asfaloth2));
		assertTrue(scn.FreepsPlayAvailable(bow));
		assertEquals(0, scn.GetTwilight());

		// Either Elf could bear it, so a second copy has somebody to go on.
		scn.FreepsPlayCard(asfaloth1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(legolas);

		assertAttachedTo(asfaloth1, legolas);
		assertEquals(2, scn.GetTwilight());
		// Arwen bears no mount, and the second copy cannot be played on her; another possession for an Elf can.
		assertEquals(0, scn.GetAttachedCards(arwen).size());
		assertFalse(scn.FreepsPlayAvailable(asfaloth2));
		assertTrue(scn.FreepsPlayAvailable(bow));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnyNumberOfCopiesOfACardThatIsNotUniqueMayBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var guest1 = scn.GetFreepsCard("guest1");
		var guest2 = scn.GetFreepsCard("guest2");
		var guest3 = scn.GetFreepsCard("guest3");
		var guest4 = scn.GetFreepsCard("guest4");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(guest1, guest2, guest3, guest4, sword1, sword2);

		scn.StartGame();

		// An ally, four times over.
		assertTrue(scn.FreepsPlayAvailable(guest1));
		scn.FreepsPlayCard(guest1);
		assertTrue(scn.FreepsPlayAvailable(guest2));
		scn.FreepsPlayCard(guest2);
		assertTrue(scn.FreepsPlayAvailable(guest3));
		scn.FreepsPlayCard(guest3);
		assertTrue(scn.FreepsPlayAvailable(guest4));
		scn.FreepsPlayCard(guest4);

		assertInZone(Zone.SUPPORT, guest1);
		assertInZone(Zone.SUPPORT, guest2);
		assertInZone(Zone.SUPPORT, guest3);
		assertInZone(Zone.SUPPORT, guest4);
		assertEquals(4, scn.GetTwilight());

		// A possession, twice.
		assertTrue(scn.FreepsPlayAvailable(sword1));
		scn.FreepsPlayCard(sword1);
		scn.FreepsChooseCard(merry);
		assertAttachedTo(sword1, merry);

		assertTrue(scn.FreepsPlayAvailable(sword2));
		scn.FreepsPlayCard(sword2);
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(sword2, frodo);

		// 4 Guests and 2 Swords at 1 each
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardWithTwoDotsMayHaveTwoCopiesInPlayAndNoThird() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate1 = scn.GetFreepsCard("gate1");
		var gate2 = scn.GetFreepsCard("gate2");
		var gate3 = scn.GetFreepsCard("gate3");
		var fury1 = scn.GetShadowCard("fury1");
		var fury2 = scn.GetShadowCard("fury2");
		var fury3 = scn.GetShadowCard("fury3");
		scn.MoveCardsToHand(gate1, gate2, gate3);
		scn.MoveCardsToHand(fury1, fury2, fury3);

		scn.StartGame();

		// A Free Peoples card.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(gate1));
		scn.FreepsPlayCard(gate1);

		assertTrue(scn.FreepsPlayAvailable(gate2));
		assertTrue(scn.FreepsPlayAvailable(gate3));
		scn.FreepsPlayCard(gate2);

		assertInZone(Zone.SUPPORT, gate1, gate2);
		// 2 Gates at 2 each
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(gate3));
		assertInZone(Zone.HAND, gate3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// It is the two in play that bar the third: with one of them gone from play, it can be played.
		scn.MoveCardsToDiscard(gate1);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.DISCARD, gate1);
		assertTrue(scn.FreepsPlayAvailable(gate3));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Shadow card.
		scn.FreepsPass();

		// 4 + 2 for the Shadow number of site 2 + 1 for the one companion
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fury1));
		scn.ShadowPlayCard(fury1);

		assertTrue(scn.ShadowPlayAvailable(fury2));
		assertTrue(scn.ShadowPlayAvailable(fury3));
		scn.ShadowPlayCard(fury2);

		assertInZone(Zone.SUPPORT, fury1, fury2);
		// 2 Furies at 1 each: the pool would still pay for the third.
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(fury3));
		assertInZone(Zone.HAND, fury3);
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveCardsToDiscard(fury1);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.DISCARD, fury1);
		assertTrue(scn.ShadowPlayAvailable(fury3));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ATitleWrittenWithAndWithoutItsAccentsIsOneTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enquea = scn.GetShadowCard("enquea");
		var thrall = scn.GetShadowCard("thrall");
		var attea = scn.GetShadowCard("attea");

		scn.StartGame();
		scn.SetShadowHand(enquea, thrall, attea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 + 2 for the Shadow number of site 2 + 1 for the one companion
		assertEquals(23, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowPlayAvailable(enquea));
		assertTrue(scn.ShadowPlayAvailable(thrall));
		assertTrue(scn.ShadowPlayAvailable(attea));

		scn.ShadowPlayCard(enquea);

		assertInZone(Zone.SHADOW_CHARACTERS, enquea);
		// 6 to play + 2 roaming (site number 3 at site 2)
		assertEquals(15, scn.GetTwilight());
		// The pool still pays for another Nazgul of cost 6, and the one of another title is offered: it is the
		// title, read without its accents, that withholds the other.
		assertFalse(scn.ShadowPlayAvailable(thrall));
		assertTrue(scn.ShadowPlayAvailable(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardWithThreeDotsMayHaveThreeCopiesInPlayAndNoFourth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn1");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var beacon4 = scn.GetFreepsCard("beacon4");
		var sky1 = scn.GetShadowCard("sky1");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(beacon1, beacon2, beacon3, beacon4);
		scn.MoveCardsToHand(sky1, sky2, sky3, sky4);

		scn.StartGame();

		// A Free Peoples card.  Aragorn is the unbound Man its text needs.
		scn.FreepsPlayCard(beacon1);
		scn.FreepsPlayCard(beacon2);
		assertTrue(scn.FreepsPlayAvailable(beacon3));
		assertTrue(scn.FreepsPlayAvailable(beacon4));
		scn.FreepsPlayCard(beacon3);

		assertInZone(Zone.SUPPORT, beacon1, beacon2, beacon3);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(beacon4));
		assertInZone(Zone.HAND, beacon4);

		scn.MoveCardsToDiscard(beacon1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(beacon4));

		// A Shadow card.  3, 2 for the Shadow number of site 2 and 1 for each of the two companions, and 4 more,
		// so that the pool would pay for the fourth.
		scn.SetTwilight(7);
		scn.FreepsPass();
		assertEquals(11, scn.GetTwilight());

		scn.ShadowPlayCard(sky1);
		scn.ShadowPlayCard(sky2);
		assertTrue(scn.ShadowPlayAvailable(sky3));
		assertTrue(scn.ShadowPlayAvailable(sky4));
		scn.ShadowPlayCard(sky3);

		assertInZone(Zone.SUPPORT, sky1, sky2, sky3);
		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(sky4));
		assertInZone(Zone.HAND, sky4);

		scn.MoveCardsToDiscard(sky1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(sky4));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
