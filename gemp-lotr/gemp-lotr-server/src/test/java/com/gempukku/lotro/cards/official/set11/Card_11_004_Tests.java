package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("battle", "11_4");
					// Dwarven Warrior: Dwarf companion, strength 6, vitality 3; his text answers his own play only.
					put("warrior", "12_7");
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4; "To play, spot a Dwarf" is its whole text
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8; a maneuver ability used only on purpose
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2.  "Skirmish: Exert this minion to wound a
					// character he is skirmishing."
					put("soldier", "1_271");
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BattletotheLastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Battle to the Last
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a Dwarf. For each wound on that Dwarf, make him strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("battle");

		assertEquals("Battle to the Last", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheSpottedDwarfStrengthPlus2AndDamagePlus1ForHisOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var battle = scn.GetFreepsCard("battle");
		var warrior = scn.GetFreepsCard("warrior");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(warrior, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.AddWoundsToChar(warrior, 1);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(warrior));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(warrior, soldier);
		scn.FreepsResolveSkirmish(warrior);

		assertEquals(6, scn.GetStrength(warrior));
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(thrarin));

		// A Dwarf in the skirmish and a Dwarf ally in none are offered; a wounded Man and the Ring-bearer are not.
		assertTrue(scn.FreepsPlayAvailable(battle));
		scn.FreepsPlayCard(battle);
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior));
		assertTrue(scn.FreepsHasCardChoiceAvailable(thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(warrior);

		// 6 + 2 for his one wound
		assertEquals(8, scn.GetStrength(warrior));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertInDiscard(battle);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The wounds are counted as the event is played: a wound after it adds nothing (CR 4.2 "event").
		scn.ShadowUseCardAction(soldier);
		assertEquals(2, scn.GetWoundsOn(warrior));
		assertEquals(8, scn.GetStrength(warrior));
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoWoundsTheDwarfIsStrengthPlus4AndDamagePlus2UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var warrior = scn.GetFreepsCard("warrior");
		var guard = scn.GetFreepsCard("guard");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(warrior, guard);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.AddWoundsToChar(warrior, 2);
		assertEquals(2, scn.GetWoundsOn(warrior));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{warrior, runner1}, new PhysicalCardImpl[]{guard, runner2});
		scn.FreepsResolveSkirmish(warrior);

		scn.FreepsPlayCard(battle);
		// The Guard, a Dwarf too, is the other candidate.  6 + 2 for each of his two wounds:
		scn.FreepsChooseCard(warrior);
		assertEquals(10, scn.GetStrength(warrior));
		assertEquals(2, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of the same skirmish.
		scn.ShadowPass();
		assertEquals(10, scn.GetStrength(warrior));
		assertEquals(2, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		scn.FreepsPass();

		// The Guard's skirmish is a phase of its own: the bonus is gone.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		scn.FreepsResolveSkirmish(guard);
		assertEquals(2, scn.GetWoundsOn(warrior));
		assertEquals(6, scn.GetStrength(warrior));
		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoDwarfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battle = scn.GetFreepsCard("battle");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(battle);

		scn.StartGame();
		assertInHand(battle);
		scn.MoveMinionsToTable(runner1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);

		// A wounded Man in the skirmish and no Dwarf in play.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(battle));
		assertInHand(battle);
	}
}
