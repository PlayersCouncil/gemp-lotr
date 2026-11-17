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

public class Card_04_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "4_59");
					put("blade2", "4_59");

					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					put("haldir", "1_48");     // Haldir, Elf of the Golden Wood: Elf companion, vitality 3; +2 only at sites 6 to 8
					put("lorienelf", "1_53");  // Lorien Elf: Elf companion, vitality 2, no text in play
					put("celeborn", "1_34");   // Celeborn, Lord of Lorien: Elf ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability used only on purpose
					put("gimli", "1_13");      // Gimli, Son of Gloin: [dwarven] Dwarf, unbound; a skirmish ability used only on purpose
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer, vitality 4
					put("thrarin", "1_27");    // Thrarin, Dwarven Smith: a Dwarf ally; Maneuver: exert to take part in skirmishes

					put("scout", "1_191");     // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("savage2", "1_151");
					put("soldier", "1_271");   // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two Elf companions, one of them in a skirmish against an Uruk Savage.
	protected VirtualTableScenario GetTwoElvesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "4_59");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3
					put("elf", "1_53");         // Lorien Elf: Elf companion, strength 4, vitality 2, no text of its own in play
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArrowandBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Arrow and Blade
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Exert X Elf companions to make a minion skirmishing an unbound companion strength -X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Arrow and Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "Exert X Elf companions to make a minion ... strength -X": an X of exertions is at least 1 (Q-0076).  The
	// definition let the choice of Elves be answered with none, so the event could be played with no Elf able to
	// exert.
	// F-0446.
	@Test
	public void AtLeastOneElfCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoElvesScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(arwen, elf);
		scn.MoveCardsToHand(blade);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		// Both Elves exhausted (Frodo, who is no Elf, could exert): the event cannot be played.
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(elf, 1);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(elf));
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blade));

		scn.RemoveWoundsFromChar(arwen, 2);
		scn.RemoveWoundsFromChar(elf, 1);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(elf));
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);

		// One Elf at the least, and Frodo is not among them.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(arwen, elf);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(elf));
		assertEquals(3, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ExertsTwoElfCompanionsToMakeAMinionSkirmishingAnUnboundCompanionStrengthMinus2ForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, haldir, lorienelf, aragorn, gimli);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(blade);
		// Lorien Elf, vitality 2, is exhausted by one wound.
		scn.AddWoundsToChar(lorienelf, 1);

		var scout = scn.GetShadowCard("scout");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		scn.MoveMinionsToTable(scout, savage, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scout}, new PhysicalCardImpl[]{gimli, soldier});
		// The Savage, left over, is put into Aragorn's skirmish beside the Scout.
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.FreepsPlayAvailable(blade));

		scn.FreepsPlayCard(blade);
		// The Elf companions who can exert; not the exhausted one, the Elf ally, nor the companions who are no Elves.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, haldir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienelf, celeborn, aragorn, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(arwen, haldir);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(celeborn));

		// Both minions skirmishing Aragorn, an Orc of [moria] and an Uruk-hai of [isengard]; not the Soldier, who is
		// assigned to Gimli but whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(scout);

		// Two Elves exerted: X is 2.
		assertEquals(4, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertInDiscard(blade);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still -2 at the Free Peoples player's next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(scout));
		scn.FreepsPass();

		// Gone once the next skirmish has begun: each skirmish is a phase of its own.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OneElfExertedOfTwoMakesTheMinionStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, haldir, gimli);
		scn.MoveCardsToHand(blade);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, soldier);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		scn.FreepsPlayCard(blade);
		assertEquals(2, scn.FreepsGetChoiceMax());
		// Fewer than there are.  The Soldier is the one minion skirmishing, and is chosen unasked.
		scn.FreepsChooseCards(arwen);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(6, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingARingBoundCompanionOrAnAllyIsNotWeakenedAndTheElfExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var blade2 = scn.GetFreepsCard("blade2");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(arwen, haldir, aragorn, sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(blade, blade2);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2, soldier);

		// Thrarin takes part in the skirmishes by his own ability.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{thrarin, savage2},
				new PhysicalCardImpl[]{aragorn, soldier});

		// Sam is Ring-bound, and the Soldier skirmishes Aragorn only once Sam's skirmish is over: no minion is a
		// candidate.  The event is played and an Elf exerted for nothing.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);
		scn.FreepsChooseCards(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInDiscard(blade);
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Thrarin is an ally, no companion: the same.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.FreepsPlayAvailable(blade2));
		scn.FreepsPlayCard(blade2);
		scn.FreepsChooseCards(haldir);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertInDiscard(blade2);
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
