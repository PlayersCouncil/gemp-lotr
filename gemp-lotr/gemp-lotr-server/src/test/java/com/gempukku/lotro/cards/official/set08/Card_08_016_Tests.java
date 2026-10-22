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

public class Card_08_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tarry", "8_16");
					put("tarry2", "8_16");

					put("gandalf", "1_72");    // Gandalf, Friend of the Shirefolk: Frodo signet; a standing strength bonus only
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] Man, Gandalf signet; a maneuver ability used only on purpose
					put("gimli", "1_13");      // Gimli, Son of Gloin: [dwarven] Dwarf, Gandalf signet; a skirmish ability used only on purpose
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Aragorn signet; +3 only against a Nazgul
					put("merry", "1_302");     // Merry, Friend to Sam: Frodo signet; a skirmish ability used only on purpose
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer, vitality 4
					put("thrarin", "1_27");    // Thrarin, Dwarven Smith: a Dwarf ally; Maneuver: exert to take part in skirmishes
					put("ent", "6_27");        // Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4

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

	@Test
	public void LetUsNotTarryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Let Us Not Tarry
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert Gandalf to make a minion skirmishing an unbound companion strength -1 for each Gandalf signet you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tarry");

		assertEquals("Let Us Not Tarry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToMakeAMinionSkirmishingAnUnboundCompanionStrengthMinus1ForEachGandalfSignetSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tarry = scn.GetFreepsCard("tarry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var merry = scn.GetFreepsCard("merry");
		// Gandalf signets: Aragorn and Gimli (2).  Frodo signets: Gandalf, Merry and Frodo (3).  Aragorn signet: Arwen (1).
		scn.MoveCompanionsToTable(gandalf, aragorn, gimli, arwen, merry);
		scn.MoveCardsToHand(tarry);

		var scout = scn.GetShadowCard("scout");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, savage, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scout}, new PhysicalCardImpl[]{gimli, soldier});
		// The Savage, left over, is put into Aragorn's skirmish beside the Scout.
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.FreepsPlayAvailable(tarry));

		scn.FreepsPlayCard(tarry);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		// The player names how many Gandalf signets he spots, from 0 to the 2 there are, filled in with 2.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(Integer.valueOf(2), scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// Both minions skirmishing Aragorn, an Orc of [moria] and an Uruk-hai of [isengard]; not the Soldier, who is
		// assigned to Gimli but whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(scout);

		assertEquals(4, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertInDiscard(tarry);
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
	public void InADwarfsSkirmishNamingOneSignetOfTwoMakesTheMinionStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tarry = scn.GetFreepsCard("tarry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gandalf, aragorn, gimli);
		scn.MoveCardsToHand(tarry);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, soldier);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(tarry);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.FreepsGetChoiceMax());
		// Fewer than there are.  The Soldier is the one minion skirmishing, and is chosen unasked.
		scn.FreepsChoose("1");

		assertEquals(6, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingARingBoundCompanionOrAnAllyIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tarry = scn.GetFreepsCard("tarry");
		var tarry2 = scn.GetFreepsCard("tarry2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		// Gimli is there for his Gandalf signet, so that a number above 0 can be named.
		scn.MoveCompanionsToTable(gandalf, gimli, sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(tarry, tarry2);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);

		// Thrarin takes part in the skirmishes by his own ability.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{thrarin, savage2});

		// Sam is Ring-bound: no minion skirmishing him is a candidate.  The event is played and Gandalf exerted for
		// nothing.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsPlayAvailable(tarry));
		scn.FreepsPlayCard(tarry);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.FreepsChoose("1");
		assertInDiscard(tarry);
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Thrarin is an ally, no companion: the same.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.FreepsPlayAvailable(tarry2));
		scn.FreepsPlayCard(tarry2);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		scn.FreepsChoose("1");
		assertInDiscard(tarry2);
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tarry = scn.GetFreepsCard("tarry");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ent = scn.GetFreepsCard("ent");
		// Ent Avenger, a [gandalf] companion who could exert, is no Gandalf.
		scn.MoveCompanionsToTable(gandalf, aragorn, ent);
		scn.MoveCardsToHand(tarry);
		scn.AddWoundsToChar(gandalf, 3);

		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(ent));
		assertInHand(tarry);
		scn.MoveMinionsToTable(scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, scout);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(tarry));
		assertInHand(tarry);
	}
}
