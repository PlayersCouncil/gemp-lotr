package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger", "7_116");

					// Cards for the hand: a companion, a possession and a Shadow minion
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion
					put("armor", "1_92");       // Armor: [gondor] possession
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1 (exhausted); its trigger is on being played

					// Siege Troop: [sauron] Orc, vitality 4; Besieger and a regroup ability used only on purpose
					put("troop", "8_106");
					// Uruk Savage: [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangerofOsgiliathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Ranger of Osgiliath
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>.
		 * <b>Regroup:</b> Discard 2 cards from hand to exhaust a minion.
		*/

		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");

		assertEquals("Ranger of Osgiliath", ranger.getBlueprint().getTitle());
		assertNull(ranger.getBlueprint().getSubtitle());
		assertFalse(ranger.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, ranger.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, ranger.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, ranger.getBlueprint().getCardType());
		assertEquals(Race.MAN, ranger.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(ranger, Keyword.RANGER));
		assertEquals(2, ranger.getBlueprint().getTwilightCost());
		assertEquals(6, ranger.getBlueprint().getStrength());
		assertEquals(3, ranger.getBlueprint().getVitality());
		assertEquals(6, ranger.getBlueprint().getResistance());
	}

	@Test
	public void RegroupAbilityDiscardsTwoCardsOfAnyKindFromHandToExhaustAMinionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetFreepsCard("runner");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(boromir, armor, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		assertInHand(boromir, armor, runner);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(ranger));

		scn.FreepsUseCardAction(ranger);
		// Any 2 cards: a companion, a possession and a Shadow card are all offered, and exactly 2 are taken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, armor, runner));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(boromir, runner);
		assertInDiscard(boromir, runner);
		assertInHand(armor);
		assertEquals(1, scn.GetFreepsHandCount());

		// A minion of either culture and race; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Vitality 4: exhausted is 3 wounds (an exertion twice would be 2).  The Savage is untouched.
		assertEquals(3, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTheOnlyMinionAlreadyExhaustedTheCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		var armor = scn.GetFreepsCard("armor");
		var savage = scn.GetFreepsCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(boromir, armor, savage);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertInHand(boromir, armor, savage);
		scn.SkipToPhase(Phase.REGROUP);

		// The Runner's vitality is 1: exhausted, he cannot be exhausted (or exerted) further.
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsActionAvailable(ranger));

		scn.FreepsUseCardAction(ranger);
		scn.FreepsChooseCards(boromir, armor);

		// The cost is paid, and nothing is exhausted: the Runner is still in play, unwounded (a wound would kill him).
		assertInDiscard(boromir, armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInPlay(runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var armor = scn.GetFreepsCard("armor");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(armor);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertInHand(armor);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ranger));
		assertEquals(0, scn.GetWoundsOn(savage));
	}
}
