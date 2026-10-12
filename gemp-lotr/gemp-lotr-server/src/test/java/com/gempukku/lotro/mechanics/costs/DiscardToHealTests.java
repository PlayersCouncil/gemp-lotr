package com.gempukku.lotro.mechanics.costs;

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
 * Discarding a card from hand to heal the character of its title.
 *
 * Comprehensive Rules 4.2, "discard to heal": "As a fellowship action during your fellowship phase, you may spot a
 * unique companion or unique ally with at least one wound and discard a card from your hand with the same card
 * title (it may have a different subtitle) to heal that character."  And "healing": "When you 'heal a
 * character,' you remove one wound."
 *
 * The action belongs to no card's text: the game offers it on the card in hand.  It is not a play, so nothing is
 * added to the twilight pool, and it is the one thing a second copy of a unique character in play is good for,
 * since that copy cannot be played.
 */
public class DiscardToHealTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_303");      // Merry, From O'er the Brandywine: unique, vitality 4; +2 only with a weapon
					put("copy", "1_303");       // the same card again
					put("other", "1_302");      // Merry, Friend to Sam: the title under another subtitle; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: another unique Hobbit; his abilities are used only on purpose
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a card of another title, cost 1

					put("elf1", "1_53");        // Lórien Elf: not unique, vitality 2, cost 1.  "To play, spot an Elf."
					put("elf2", "1_53");

					// Barliman Butterbur: a unique ally, vitality 2.  His fellowship ability exerts him, which a
					// wounded Barliman cannot do
					put("barliman1", "1_70");
					put("barliman2", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACardOfTheSameTitleIsDiscardedFromHandToHealOneWoundFromAUniqueCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var copy = scn.GetFreepsCard("copy");
		var other = scn.GetFreepsCard("other");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, sam);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.MoveCardsToHand(copy, other, pippin);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsHasDiscardToHealAvailable());
		// Either card named Merry will do, whatever its subtitle, and neither can be played while he is in play
		assertTrue(scn.FreepsActionAvailable(copy, "Heal by discard"));
		assertTrue(scn.FreepsActionAvailable(other, "Heal by discard"));
		assertFalse(scn.FreepsPlayAvailable(copy));
		assertFalse(scn.FreepsPlayAvailable(other));
		// A card of another title is only a card to play
		assertFalse(scn.FreepsActionAvailable(pippin, "Heal by discard"));
		assertTrue(scn.FreepsPlayAvailable(pippin));

		scn.FreepsDiscardToHeal(other);

		// One wound and not both; the wounded Sam, whose title it is not, keeps his; nothing was paid
		assertInZone(Zone.DISCARD, other);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.HAND, copy, pippin);
		assertTrue(scn.FreepsActionAvailable(copy, "Heal by discard"));

		scn.FreepsDiscardToHeal(copy);

		assertInZone(Zone.DISCARD, copy);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedForACharacterWithNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var copy = scn.GetFreepsCard("copy");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(merry, sam);
		// A wound on a companion of another title is no wound on Merry
		scn.AddWoundsToChar(sam, 1);
		scn.MoveCardsToHand(copy);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.HAND, copy);

		// The second Merry gives his player nothing to do: no heal, and no play of a unique card already in play
		assertFalse(scn.FreepsHasDiscardToHealAvailable());
		assertFalse(scn.FreepsPlayAvailable(copy));
	}

	@Test
	public void NotOfferedForACharacterWhoIsNotUnique() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		scn.MoveCompanionsToTable(elf1);
		scn.AddWoundsToChar(elf1, 1);
		scn.MoveCardsToHand(elf2);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(elf1));
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsHasDiscardToHealAvailable());

		// All the second Elf can do is be played, and the first keeps his wound
		assertTrue(scn.FreepsPlayAvailable(elf2));
		scn.FreepsPlayCard(elf2);

		assertInZone(Zone.FREE_CHARACTERS, elf1, elf2);
		assertEquals(1, scn.GetWoundsOn(elf1));
		assertEquals(0, scn.GetWoundsOn(elf2));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AUniqueAllyIsHealedTheSameWay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman1 = scn.GetFreepsCard("barliman1");
		var barliman2 = scn.GetFreepsCard("barliman2");
		scn.MoveAlliesToTable(barliman1);
		scn.AddWoundsToChar(barliman1, 1);
		scn.MoveCardsToHand(barliman2);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(barliman1));
		assertTrue(scn.FreepsActionAvailable(barliman2, "Heal by discard"));
		assertFalse(scn.FreepsPlayAvailable(barliman2));

		scn.FreepsDiscardToHeal(barliman2);

		assertInZone(Zone.DISCARD, barliman2);
		assertEquals(0, scn.GetWoundsOn(barliman1));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedOutsideTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var copy = scn.GetFreepsCard("copy");
		scn.MoveCompanionsToTable(merry);
		scn.AddWoundsToChar(merry, 2);
		scn.MoveCardsToHand(copy);

		scn.StartGame();

		// The control: on offer in the fellowship phase
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(copy, "Heal by discard"));

		scn.SkipToPhase(Phase.REGROUP);

		// The same wounds and the same card in hand, in the next phase in which the Free Peoples player is
		// asked for actions
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(merry));
		assertInZone(Zone.HAND, copy);
		assertFalse(scn.FreepsHasDiscardToHealAvailable());
	}
}
