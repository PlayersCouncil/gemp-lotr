package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "12_47");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; his texts are used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] companion whose whole text is Knight
					put("sting", "5_116");      // Sting, Baggins Heirloom: "Skirmish: Exert Frodo to ...", a wound on the Ring-bearer in the middle of a skirmish
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its "when you play" never fires, as it is cheated in
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Faramir, Dúnadan of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Ranger</b>.<br><b>Skirmish:</b> Exert Faramir to make him strength +1 for each wound on the Ring-bearer.
		*/

		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", faramir.getBlueprint().getTitle());
		assertEquals("Dúnadan of Gondor", faramir.getBlueprint().getSubtitle());
		assertTrue(faramir.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, faramir.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, faramir.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, faramir.getBlueprint().getCardType());
		assertEquals(Race.MAN, faramir.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(faramir, Keyword.RANGER));
		assertEquals(3, faramir.getBlueprint().getTwilightCost());
		assertEquals(7, faramir.getBlueprint().getStrength());
		assertEquals(3, faramir.getBlueprint().getVitality());
		assertEquals(7, faramir.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityExertsFaramirToCountOnlyTheRingBearersWoundsForTheRestOfHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		var sam = scn.GetFreepsCard("sam");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(faramir, sam, knight);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		// Sam is Ring-bound but not the Ring-bearer: his wound is not counted.
		scn.AddWoundsToChar(sam, 1);
		scn.MoveMinionsToTable(runner1, runner2);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, runner1}, new PhysicalCardImpl[]{knight, runner2});
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, runner1));
		assertEquals(7, scn.GetStrength(faramir));

		assertTrue(scn.FreepsActionAvailable(faramir));
		scn.FreepsUseCardAction(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		// 7 + 2 for Frodo's two wounds; Sam's wound, or Faramir's own exertion, counted would read 10.
		assertEquals(9, scn.GetStrength(faramir));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Faramir's 9 beats the Runner's 5. In the next skirmish, a phase of its own, the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, runner2));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedOutsideHisOwnSkirmishAndCountsThreeWoundsAsPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(faramir, knight);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(runner1);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner1);
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.IsCharSkirmishingAgainst(knight, runner1));
		assertFalse(scn.IsCharSkirmishing(faramir));
		assertEquals(7, scn.GetStrength(faramir));

		// Faramir is in no skirmish, and the text names none.
		assertTrue(scn.FreepsActionAvailable(faramir));
		scn.FreepsUseCardAction(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		// 7 + 3 for Frodo's three wounds
		assertEquals(10, scn.GetStrength(faramir));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsStillOfferedAndPaidWhenTheRingBearerHasNoWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, runner1);
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, runner1));
		assertEquals(7, scn.GetStrength(faramir));

		// The text asks nothing of the Ring-bearer's wounds: the exertion is paid for a bonus of +0.
		assertTrue(scn.FreepsActionAvailable(faramir));
		scn.FreepsUseCardAction(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(7, scn.GetStrength(faramir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileFaramirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(faramir);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		// Vitality 3 with 2 wounds: exhausted, so he cannot exert.
		scn.AddWoundsToChar(faramir, 2);
		scn.MoveMinionsToTable(runner1);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(faramir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, runner1);
		scn.FreepsResolveSkirmish(faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, runner1));

		assertFalse(scn.FreepsActionAvailable(faramir));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AWoundOnTheRingBearerAfterTheUseDoesNotRaiseTheBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var faramir = scn.GetFreepsCard("faramir");
		var sting = scn.GetFreepsCard("sting");
		scn.MoveCompanionsToTable(faramir);
		scn.AttachCardsTo(frodo, sting);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(runner1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, runner1);
		scn.FreepsResolveSkirmish(faramir);

		scn.FreepsUseCardAction(faramir);
		// 7 + 1 for Frodo's one wound
		assertEquals(8, scn.GetStrength(faramir));
		scn.ShadowPass();

		// Sting exerts Frodo in Faramir's skirmish: a second wound on the Ring-bearer, after the bonus was made.
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(2, scn.GetWoundsOn(frodo));
		// The count was taken as the ability was used: still 8, not 9.
		assertEquals(8, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
