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

public class Card_04_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("feathered", "4_66");

					put("legolas", "11_21");    // Legolas, Companion of the Ring: Elf companion, strength 6, vitality 3; +2 only at a battleground or forest, which no site of this path is
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability only
					// Golradir, Councilor of Imladris: an Elf ally, strength 4, vitality 3, home 3F; a skirmish ability used only on purpose
					put("golradir", "3_20");
					// One Whom Men Would Follow: exert Aragorn to make an ally strength +2 and take part in skirmishes until the regroup phase
					put("onewhom", "1_109");

					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and no other text
					put("savage2", "1_151");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger does nothing when cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FeatheredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Feathered
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a minion skirmishing an Elf strength -2 for each wound on that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("feathered");

		assertEquals("Feathered", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAMinionSkirmishingAnElfStrengthMinus2ForEachWoundOnItUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feathered = scn.GetFreepsCard("feathered");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.MoveCardsToHand(feathered);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, runner);
		scn.AddWoundsToChar(savage1, 2);
		scn.AddWoundsToChar(savage2, 1);
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));

		// Not in the Free Peoples player's assignment actions, the phase before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(feathered));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{legolas, savage1},
				new PhysicalCardImpl[]{arwen, savage2});
		// The Runner, left over, is put on Legolas by the Shadow player: two minions in one Elf's skirmish.
		scn.ShadowAssignToMinions(legolas, runner);

		scn.FreepsResolveSkirmish(legolas);
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.FreepsPlayAvailable(feathered));

		scn.FreepsPlayCard(feathered);
		// Both minions skirmishing Legolas, of two cultures and races; not the wounded Savage assigned to Arwen,
		// whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage1, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage1);

		// 5 - 2 for each of its 2 wounds
		assertEquals(1, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage2));
		assertInDiscard(feathered);

		// Still in force at the next decision of the same skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetStrength(savage1));

		// Legolas, 6, loses the tie to 1 + 5 and lives (2 wounds of 3); in the next skirmish the -4 is gone.
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(legolas));
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(savage1));
	}

	@Test
	public void AMinionSkirmishingAnElfAllyIsReachedAndItsWoundsAreCountedAsTheEventIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feathered = scn.GetFreepsCard("feathered");
		var aragorn = scn.GetFreepsCard("aragorn");
		var golradir = scn.GetFreepsCard("golradir");
		var onewhom = scn.GetFreepsCard("onewhom");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(onewhom, feathered);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.AddWoundsToChar(savage1, 1);
		assertEquals(1, scn.GetWoundsOn(savage1));
		scn.SkipToPhase(Phase.MANEUVER);
		// Golradir, 4 + 2, takes part in skirmishes until the regroup phase.
		scn.FreepsPlayCard(onewhom);
		assertEquals(6, scn.GetStrength(golradir));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(golradir, savage1);
		scn.FreepsResolveSkirmish(golradir);
		assertEquals(5, scn.GetStrength(savage1));

		// The one minion skirmishing an Elf is chosen unasked.
		scn.FreepsPlayCard(feathered);
		// 5 - 2 for its 1 wound
		assertEquals(3, scn.GetStrength(savage1));
		assertInDiscard(feathered);

		// A wound taken after the event was played does not change it: the wounds were counted as it was played.
		scn.AddWoundsToChar(savage1, 1);
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(3, scn.GetStrength(savage1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMinionSkirmishingAnElfTheEventIsStillPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var feathered = scn.GetFreepsCard("feathered");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(feathered);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.AddWoundsToChar(savage1, 1);
		scn.AddWoundsToChar(savage2, 1);
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, savage1},
				new PhysicalCardImpl[]{legolas, savage2});

		// Aragorn's skirmish first: the wounded Savage fights a Man, and the other waits for Legolas.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));

		// Its target finds nothing, and the event is played all the same (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(feathered));
		scn.FreepsPlayCard(feathered);
		assertEquals(5, scn.GetStrength(savage1));
		assertEquals(5, scn.GetStrength(savage2));
		assertInDiscard(feathered);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
