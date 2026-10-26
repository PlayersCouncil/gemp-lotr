package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("escape", "10_111");

					put("sam", "1_311");           // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability never used here
					put("merry", "1_303");         // Merry, From O'er the Brandywine: [shire] Hobbit companion; strength +2 with a weapon
					// Pippin, Trollslayer: a Hobbit companion who is a [gondor] card; a skirmish ability never used here
					put("trollslayer", "103_31");
					put("bounder", "1_286");       // Bounder: [shire] Hobbit ally; a skirmish ability never used here
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used here
					put("smeagol", "103_116");

					put("savage", "1_151");        // Uruk Savage: [isengard] Uruk-hai; damage +1 and nothing else
					put("troop", "1_177");         // Goblin Patrol Troop: [moria] Orc, no text
					// Ted Sandyman, Chief's Men's Ally: the opponent's Hobbit, a [men] minion; a maneuver and a regroup
					// ability never used here
					put("ted", "18_75");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NarrowEscapeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Narrow Escape
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot 2 Hobbit companions to make a Shadow player discard a minion (or spot 4 Hobbit companions to make that player discard 2 minions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("escape");

		assertEquals("Narrow Escape", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoHobbitCompanionsTheShadowPlayerDiscardsAMinionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(trollslayer);
		scn.MoveCardsToHand(escape);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertInPlay(savage, troop);
		// Frodo and a Hobbit companion of [gondor]
		assertTrue(scn.FreepsPlayAvailable(escape));
		scn.FreepsPlayCard(escape);

		// The Shadow player chooses which, of minions of two cultures and two races.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(savage);

		assertInDiscard(savage);
		assertInPlay(troop);
		assertInDiscard(escape);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneHobbitCompanionItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(escape);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, ted);
		scn.SkipToPhase(Phase.REGROUP);

		// Frodo is the one Hobbit companion.  Beside him: a Hobbit ally, a [shire] companion who is no Hobbit, and
		// the opponent's Hobbit minion.
		assertInPlay(frodo, bounder, smeagol, ted, savage);
		assertInHand(escape);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(escape));
	}

	@Test
	public void WithFourHobbitCompanionsTheShadowPlayerDiscardsTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(sam, merry, trollslayer);
		scn.MoveCardsToHand(escape);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, ted);
		scn.SkipToPhase(Phase.REGROUP);

		assertInPlay(savage, troop, ted);
		scn.FreepsPlayCard(escape);

		// Frodo, Sam, Merry and Pippin: the parenthesis is taken (CR 4.2 "effect"; F-0047), two of three minions.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, troop, ted));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(savage, ted);

		assertInDiscard(savage, ted);
		assertInPlay(troop);
		assertInDiscard(escape);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithThreeHobbitCompanionsOnlyOneMinionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var sam = scn.GetFreepsCard("sam");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(sam, trollslayer, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(escape);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, ted);
		scn.SkipToPhase(Phase.REGROUP);

		assertInPlay(savage, troop, ted);
		scn.FreepsPlayCard(escape);

		// Frodo, Sam and Pippin are three; the Hobbit ally and the [shire] companion who is no Hobbit do not make
		// the fourth, nor does the opponent's Hobbit minion, who may himself be the one discarded.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, troop, ted));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(ted);

		assertInDiscard(ted);
		assertInPlay(savage, troop);
		assertInDiscard(escape);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		scn.MoveCompanionsToTable(trollslayer);
		scn.MoveCardsToHand(escape);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertInHand(escape);
		// Two Hobbit companions and nothing to discard: an event may be played for no effect (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(escape));
		scn.FreepsPlayCard(escape);

		// Nothing is asked of the Shadow player but his regroup actions.
		assertInDiscard(escape);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
