package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_311_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "4_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8; a maneuver ability never used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: unbound companion; a standing strength bonus only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally whose home is site 3, where he may skirmish

					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; answers only a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability never used
					put("warband", "1_272");    // Orc War Band: [sauron] Orc; a skirmish ability never used
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, no Orc; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Merry, Unquenchable Hobbit
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Skirmish:</b> If Merry is not assigned to a skirmish, discard him to remove a [sauron] Orc from a
		 * 	skirmish involving an unbound companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Unquenchable Hobbit", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void UnassignedMerryDiscardsHimselfToRemoveASauronOrcFromTheUnboundCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(merry, aragorn, gandalf);

		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var warband = scn.GetShadowCard("warband");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, soldier, warband, guard, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{gandalf, warband});
		// The Shadow player puts the other three on Aragorn as well.
		scn.ShadowAssignToMinions(aragorn, soldier, guard, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, soldier));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, guard));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		// The War Band waits for Gandalf's skirmish, still to come.
		assertTrue(scn.IsCharAssigned(warband));
		assertFalse(scn.IsCharSkirmishing(warband));
		assertFalse(scn.IsCharAssigned(merry));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertInZone(Zone.DISCARD, merry);

		assertTrue(scn.FreepsHasCardChoiceAvailable(chieftain, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, troop, warband));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(soldier);

		// Out of the skirmish, and still in play.
		assertFalse(scn.IsCharSkirmishing(soldier));
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, guard));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.IsCharAssigned(warband));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileMerryIsAssignedToASkirmishNorInHisOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry, aragorn);

		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{merry, soldier});
		scn.ShadowDeclineAssignments();

		// Aragorn's skirmish against a [sauron] Orc: Merry waits for his own, while Frodo is assigned to nothing.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertTrue(scn.IsCharAssigned(merry));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();

		// Nor in his own skirmish, though he is an unbound companion skirmishing a [sauron] Orc.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		assertInZone(Zone.FREE_CHARACTERS, merry);
	}

	@Test
	public void InARingBoundCompanionsSkirmishMerryIsDiscardedAndTheOrcStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(merry, sam);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, chieftain);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.IsCharSkirmishingAgainst(sam, chieftain));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));

		// The discard is the cost: paid, with no [sauron] Orc in a skirmish involving an unbound companion.
		scn.FreepsUseCardAction(merry);
		assertInZone(Zone.DISCARD, merry);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAnAllysSkirmishMerryIsDiscardedAndTheOrcStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(elrond);

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(chieftain);

		// At Rivendell Valley, site 3, Elrond may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, chieftain);
		scn.FreepsResolveSkirmish(elrond);

		assertTrue(scn.IsCharSkirmishingAgainst(elrond, chieftain));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertInZone(Zone.DISCARD, merry);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
