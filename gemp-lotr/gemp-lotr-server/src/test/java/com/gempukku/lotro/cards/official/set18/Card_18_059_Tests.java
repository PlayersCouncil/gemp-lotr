package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("watcher", "18_59");

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, resistance 6; Knight is its whole text
					// Tradesman From Lebennin: [gondor] Man companion, strength 6, resistance 5; his start-of-fellowship
					// reinforce is never offered here, with no [gondor] token in play
					put("tradesman", "13_77");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, resistance 6; Valiant is its whole text
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit), resistance 6; a skirmish
					// ability used only on purpose
					put("pippin", "103_31");

					put("savage1", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WatcheratSarnFordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Watcher at Sarn Ford, Ranger of the North
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 8
		 * Game Text: <b>Ranger</b>.<br><b>Skirmish:</b> Exert a [gondor] Man with a resistance 6 or more to make that Man strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("watcher");

		assertEquals("Watcher at Sarn Ford", card.getBlueprint().getTitle());
		assertEquals("Ranger of the North", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityExertsAGondorManOfResistance6OrMoreToMakeThatManStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var watcher = scn.GetFreepsCard("watcher");
		var knight = scn.GetFreepsCard("knight");
		var tradesman = scn.GetFreepsCard("tradesman");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(watcher, knight, tradesman, guard, pippin);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		// The Knight's skirmish; the Watcher is in none.
		scn.FreepsAssignToMinions(knight, savage1);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetResistance(watcher));
		assertEquals(6, scn.GetResistance(knight));
		assertEquals(5, scn.GetResistance(tradesman));
		assertEquals(6, scn.GetResistance(guard));
		assertEquals(6, scn.GetResistance(pippin));
		assertEquals(5, scn.GetStrength(watcher));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(0, scn.GetWoundsOn(watcher));
		assertEquals(0, scn.GetWoundsOn(knight));

		assertTrue(scn.FreepsActionAvailable(watcher));
		scn.FreepsUseCardAction(watcher);

		// [gondor] Men of resistance 8 and of exactly 6, in the skirmish and out of it; not a [gondor] Man of
		// resistance 5, a [rohan] Man or a [gondor] Hobbit of resistance 6, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(watcher, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tradesman, guard, pippin, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);

		// The Man who exerted is the one made strength +3; the Watcher is not.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(watcher));
		assertEquals(9, scn.GetStrength(knight));
		assertEquals(5, scn.GetStrength(watcher));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ExertingHimselfInHisOwnSkirmishMakesHimStrengthPlus3UntilThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetFreepsCard("watcher");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(watcher, knight);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{watcher, savage1}, new PhysicalCardImpl[]{knight, savage2});
		scn.FreepsResolveSkirmish(watcher);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(watcher));
		assertEquals(0, scn.GetWoundsOn(watcher));

		scn.FreepsUseCardAction(watcher);
		scn.FreepsChooseCard(watcher);
		assertEquals(1, scn.GetWoundsOn(watcher));
		// Still in force at the next decision of his skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(watcher));

		// 8 against the Savage's 5: he wins, and in the Knight's skirmish, a phase of its own, he is 5 again.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(watcher));
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(watcher));
	}

	@Test
	public void IsNotOfferedWhileEveryGondorManOfResistance6OrMoreIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var watcher = scn.GetFreepsCard("watcher");
		var knight = scn.GetFreepsCard("knight");
		var tradesman = scn.GetFreepsCard("tradesman");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(watcher, knight, tradesman, guard, pippin);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		// Vitality 3 with 2 wounds: both exhausted. The Tradesman (resistance 5), the [rohan] Guard and the Hobbit
		// could exert, but do not match.
		scn.AddWoundsToChar(watcher, 2);
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(watcher));
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(tradesman, savage1);
		scn.FreepsResolveSkirmish(tradesman);

		assertEquals(2, scn.GetWoundsOn(watcher));
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(tradesman));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(watcher));

		// One wound off the Knight, and it is offered.
		scn.RemoveWoundsFromChar(knight, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(watcher));
	}
}
