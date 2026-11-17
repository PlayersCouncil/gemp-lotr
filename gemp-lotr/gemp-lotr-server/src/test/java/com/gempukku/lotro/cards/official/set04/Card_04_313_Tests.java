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

public class Card_04_313_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "4_313");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8; a maneuver ability never used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: unbound companion; a standing strength bonus only
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; abilities used only on purpose
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally whose home is site 3, where he may skirmish

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 only
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; damage +1 only
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai; damage +1 only
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard); each companion
					// is resistance -1, and a skirmish ability never used
					put("saruman", "17_116");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Pippin, Just a Nuisance
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
		 * Game Text: <b>Skirmish:</b> If Pippin is not assigned to a skirmish, discard him to remove an Uruk-hai from a
		 * 	skirmish involving an unbound companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Just a Nuisance", card.getBlueprint().getSubtitle());
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
	public void UnassignedPippinDiscardsHimselfToRemoveAnUrukHaiFromTheUnboundCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(pippin, aragorn, gandalf);

		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var sentry = scn.GetShadowCard("sentry");
		var saruman = scn.GetShadowCard("saruman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard, sentry, saruman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{gandalf, sentry});
		// The Shadow player puts the other three on Aragorn as well.
		scn.ShadowAssignToMinions(aragorn, guard, saruman, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, guard));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, saruman));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		// The Sentry waits for Gandalf's skirmish, still to come.
		assertTrue(scn.IsCharAssigned(sentry));
		assertFalse(scn.IsCharSkirmishing(sentry));
		assertFalse(scn.IsCharAssigned(pippin));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		assertInZone(Zone.DISCARD, pippin);

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(saruman, troop, sentry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// Out of the skirmish, and still in play.
		assertFalse(scn.IsCharSkirmishing(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, saruman));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.IsCharAssigned(sentry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhilePippinIsAssignedToASkirmishNorInHisOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(pippin, aragorn);

		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage},
				new PhysicalCardImpl[]{pippin, guard});
		scn.ShadowDeclineAssignments();

		// Aragorn's skirmish against an Uruk-hai: Pippin waits for his own, while Frodo is assigned to nothing.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, savage));
		assertTrue(scn.IsCharAssigned(pippin));
		assertFalse(scn.IsCharAssigned(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
		scn.PassSkirmishActions();

		// Nor in his own skirmish, though he is an unbound companion skirmishing an Uruk-hai.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
	}

	@Test
	public void InARingBoundCompanionsSkirmishPippinIsDiscardedAndTheUrukHaiStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(pippin, sam);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, savage);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pippin));

		// The discard is the cost: paid, with no Uruk-hai in a skirmish involving an unbound companion.
		scn.FreepsUseCardAction(pippin);
		assertInZone(Zone.DISCARD, pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAnAllysSkirmishPippinIsDiscardedAndTheUrukHaiStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(elrond);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);

		// At Rivendell Valley, site 3, Elrond may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elrond, savage);
		scn.FreepsResolveSkirmish(elrond);

		assertTrue(scn.IsCharSkirmishingAgainst(elrond, savage));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		assertInZone(Zone.DISCARD, pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
