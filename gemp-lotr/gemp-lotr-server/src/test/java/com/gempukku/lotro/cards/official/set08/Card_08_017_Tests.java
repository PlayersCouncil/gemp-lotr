package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("steed", "8_17");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: his text is about his own strength
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf; an archery ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					// Thrarin, Dwarven Smith: a [dwarven] ally; "Maneuver: Exert Thrarin to allow him to participate in
					// archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
					// Ent Avenger: a [gandalf] companion who is not Gandalf; strength +2 while 3 Ents can be spotted
					put("avenger", "6_27");
					// Shadowfax, Unequaled Steed: "Bearer must be Gandalf.  Each time Gandalf is about to take a
					// wound, you may add a burden to prevent that"; no wound is dealt in these tests
					put("shadowfax", "11_40");
					// Horse of Rohan: a mount that is no Shadowfax; "Bearer must be a Man, Elf, or Wizard"; its
					// start-of-skirmish exertion reaches only a skirmish its bearer is in
					put("horse", "4_283");
					// Glamdring: a [gandalf] possession that is no Shadowfax; "Bearer must be Gandalf"; damage +1 and
					// an ability used only on purpose
					put("glamdring", "1_75");

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MightySteedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Mighty Steed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: If Gandalf is not assigned to a skirmish, spot Shadowfax or exert Gandalf to have Gandalf replace an unbound companion in a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("steed");

		assertEquals("Mighty Steed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToHaveHimReplaceTheUnboundCompanionInTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(steed);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop1));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(steed));

		// No Shadowfax to spot: the exertion is the only way to pay, and is taken unasked.
		scn.FreepsPlayCard(steed);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop1));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertInDiscard(steed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfIsAssignedToASkirmishStillToCome() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(steed);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		assertInHand(steed);
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{gandalf, troop2});
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn is an unbound companion in the skirmish and Gandalf can exert, but Gandalf is assigned.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop1));
		assertTrue(scn.IsCharAssignedAgainst(gandalf, troop2));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(steed);
		assertFalse(scn.FreepsPlayAvailable(steed));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithGandalfExhaustedSpottingShadowfaxPaysAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var legolas = scn.GetFreepsCard("legolas");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(gandalf, legolas);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(steed);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		// Vitality 4 with 3 wounds: Gandalf cannot exert.
		scn.AddWoundsToChar(gandalf, 3);
		assertAttachedTo(shadowfax, gandalf);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(legolas, troop1);
		scn.FreepsResolveSkirmish(legolas);

		assertAttachedTo(shadowfax, gandalf);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(legolas, troop1));
		assertTrue(scn.FreepsPlayAvailable(steed));

		scn.FreepsPlayCard(steed);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop1));
		assertFalse(scn.IsCharSkirmishing(legolas));
		assertInDiscard(steed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithBothWaysToPayThePlayerIsAskedAndSpottingShadowfaxExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(steed);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop1);
		scn.FreepsResolveSkirmish(aragorn);

		assertAttachedTo(shadowfax, gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(steed);
		assertTrue(scn.FreepsGetMultipleChoices().contains("Spot Shadowfax"));
		assertTrue(scn.FreepsGetMultipleChoices().contains("Exert Gandalf"));
		scn.FreepsChoose("Spot Shadowfax");

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop1));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithGandalfExhaustedAndNoShadowfax() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var horse = scn.GetFreepsCard("horse");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(gandalf, aragorn, avenger);
		// A mount and a [gandalf] possession on Gandalf, neither of them Shadowfax.
		scn.AttachCardsTo(gandalf, horse, glamdring);
		scn.MoveCardsToHand(steed);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 3);
		assertAttachedTo(horse, gandalf);
		assertAttachedTo(glamdring, gandalf);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInHand(steed);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop1);
		scn.FreepsResolveSkirmish(aragorn);

		assertAttachedTo(horse, gandalf);
		assertAttachedTo(glamdring, gandalf);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		// The Ent Avenger is a [gandalf] companion who could exert, but is no Gandalf.
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop1));
		assertInHand(steed);
		assertFalse(scn.FreepsPlayAvailable(steed));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithARingBoundCompanionInTheSkirmishItReplacesNobodyAndLeavesACompanionAssignedLaterAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(gandalf, aragorn, sam);
		scn.MoveCardsToHand(steed);

		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, troop1}, new PhysicalCardImpl[]{aragorn, troop2});
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop2));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		// Sam is Ring-bound, and Aragorn, unbound, is not in a skirmish yet: the event is played for nothing.
		scn.FreepsPlayCard(steed);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop1));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, troop2));
		assertFalse(scn.IsCharSkirmishing(gandalf));
		assertFalse(scn.IsCharAssigned(gandalf));
		assertInDiscard(steed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnAllyInTheSkirmishItReplacesNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var steed = scn.GetFreepsCard("steed");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(steed);

		var troop1 = scn.GetShadowCard("troop1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, troop1);
		scn.FreepsResolveSkirmish(thrarin);

		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, troop1));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(steed);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.IsCharSkirmishingAgainst(thrarin, troop1));
		assertFalse(scn.IsCharSkirmishing(gandalf));
		assertInDiscard(steed);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
