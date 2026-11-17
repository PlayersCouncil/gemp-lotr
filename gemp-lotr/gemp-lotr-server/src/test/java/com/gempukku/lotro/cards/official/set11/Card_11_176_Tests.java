package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_176_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unharmed", "11_176");

					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit; a maneuver ability used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, vitality 2; a skirmish ability used only on purpose

					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion, vitality 4; abilities used only on purpose
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai minion, vitality 3; damage +1 and no other text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion; its play trigger is not reached by a cheat
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnharmedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Unharmed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Heal a Hobbit for each minion you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("unharmed");

		assertEquals("Unharmed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithThreeMinionsSpottedHealsUpToThreeHobbitsOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unharmed = scn.GetFreepsCard("unharmed");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var ted = scn.GetShadowCard("ted");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(unharmed);
		assertInHand(unharmed);

		scn.StartGame();
		scn.MoveMinionsToTable(ted, savage, runner);
		scn.SkipToPhase(Phase.REGROUP);

		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.AddWoundsToChar(ted, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(ted));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPlayCard(unharmed);

		// A wounded Hobbit of either culture or side, companion, ally or minion; not the companion or the minion
		// who is no Hobbit. The player spots as many minions as he likes, so each heal may be passed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, savage));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bounder);
		scn.FreepsChooseCard(ted);
		// A third heal for the third minion, an Orc, Uruk-hai and Hobbit of three cultures; passed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		scn.FreepsDeclineChoosing();

		assertInDiscard(unharmed);
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTwoMinionsSpottedOneHobbitMayBeHealedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unharmed = scn.GetFreepsCard("unharmed");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);
		scn.MoveCardsToHand(unharmed);
		assertInHand(unharmed);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.REGROUP);

		scn.AddWoundsToChar(merry, 3);
		scn.AddWoundsToChar(pippin, 1);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.FreepsPlayCard(unharmed);
		// "For each" may reach one character more than once (CR 4.2 "for each").
		scn.FreepsChooseCard(merry);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		scn.FreepsChooseCard(merry);

		// Two minions, two heals; the four companions are not counted.
		assertInDiscard(unharmed);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotPlayableInTheManeuverPhaseAndWithNoWoundedHobbitIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unharmed = scn.GetFreepsCard("unharmed");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(unharmed);
		assertInHand(unharmed);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));

		// The maneuver phase of the same turn, with a minion in play: not playable.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(unharmed));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsPlayAvailable(unharmed));
		scn.FreepsPlayCard(unharmed);

		// Merry is unwounded and Aragorn is no Hobbit: nothing is healed.
		assertInDiscard(unharmed);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
