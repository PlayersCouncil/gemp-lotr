package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "4_74");
					put("aragorn", "1_89");    // Aragorn: [gondor] Man companion, unbound, strength 8, vitality 4
					put("gimli", "1_13");      // Gimli, Son of Gloin: damage +1; his own ability is used only on purpose
					put("farin", "1_11");      // Farin: a [dwarven] Dwarf companion who is no Gimli; +2 only against an Orc
					put("sam", "1_311");       // Sam: a Ring-bound companion who is not the Ring-bearer, vitality 4
					put("thrarin", "1_27");    // Thrarin: Dwarf ally; Maneuver: exert to take part in skirmishes

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
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Legolas, Elven Comrade
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Archer</b>.
		 * 	<b>Skirmish:</b> Exert Legolas to make a minion skirmishing an unbound companion strength -1 (or -2 if that companion is Gimli).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Elven Comrade", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void SkirmishAbilityExertsLegolasToMakeAMinionSkirmishingAnUnboundCompanionStrengthMinus1UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		var scout = scn.GetShadowCard("scout");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, savage, soldier);

		// Legolas is an archer: the Shadow player puts his archery wound on the Savage (vitality 3).
		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(savage);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, scout}, new PhysicalCardImpl[]{legolas, soldier});
		// The Savage, left over, is put into Aragorn's skirmish beside the Scout.
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(6, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.FreepsActionAvailable(legolas));

		// Legolas is not in this skirmish: the text names none of his own.
		scn.FreepsUseCardAction(legolas);
		// Both minions skirmishing Aragorn, an Orc of [moria] and an Uruk-hai of [isengard]; not the Soldier, who is
		// assigned to Legolas but whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(soldier, aragorn, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(scout);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(5, scn.GetStrength(scout));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still -1 at the Free Peoples player's next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(scout));
		scn.FreepsPass();

		// Gone once the next skirmish has begun: each skirmish is a phase of its own.
		scn.FreepsResolveSkirmish(legolas);
		assertEquals(6, scn.GetStrength(scout));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingARingBoundCompanionOrAnAllyIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(legolas, sam);
		scn.MoveAlliesToTable(thrarin);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);

		// Thrarin takes part in the skirmishes by his own ability.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(savage);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{thrarin, savage2});

		// Sam is Ring-bound: no minion skirmishing him is a candidate.  The ability costs an exertion, so it is
		// offered, and the exertion is paid for nothing.
		scn.FreepsResolveSkirmish(sam);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Thrarin is an ally, no companion: the same.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.FreepsActionAvailable(legolas));
		scn.FreepsUseCardAction(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingGimliIsStrengthMinus2AndOneSkirmishingAnotherDwarfMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(legolas, gimli, farin);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);

		scn.SkipToArcheryWounds();
		scn.ShadowChooseCard(savage2);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{farin, savage2});

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(5, scn.GetStrength(savage));
		scn.FreepsUseCardAction(legolas);
		// The Savage is the one minion skirmishing an unbound companion, and that companion is Gimli: -2.
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(3, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Farin is a Dwarf companion and no Gimli: -1.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(farin);
		assertEquals(5, scn.GetStrength(savage2));
		scn.FreepsUseCardAction(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(4, scn.GetStrength(savage2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenLegolasIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Vitality 3 with 2 wounds: Legolas cannot exert.
		scn.AddWoundsToChar(legolas, 2);
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(5, scn.GetStrength(savage));
	}
}
