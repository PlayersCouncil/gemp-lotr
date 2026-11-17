package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_039_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The regroup phase of
	// turn 1 is at The Troll Hoard (site 2). Minions laid after StartGame() and left unassigned stand there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("jail", "32_39");
					put("jail2", "32_39");     // a second Jail, for "this condition"
					// Gandalf, Friend of Thorin: a Wise companion, vitality 4; a Fellowship ability only, and a discard
					// bar that wants two [dwarven] followers
					put("gandalf", "30_26");
					put("gandalf3", "30_26");   // a second copy of that Gandalf, for "copies"
					// Gandalf, Leader of Dwarves: a Gandalf of another subtitle; a reconcile draw and Skirmish abilities
					put("gandalf2", "30_25");
					// Elrond, Elven Lord: an [elven] Wise ally, vitality 4; a start-of-turn heal offer and a Regroup
					// ability used only on purpose
					put("elrond", "30_21");
					// Galadriel, Elven Lady: an [elven] Wise ally, vitality 3; a start-of-turn heal of Gandalf and a
					// Skirmish ability used only on purpose
					put("galadriel", "32_13");
					// Lindir, Elven Minstrel: an [elven] ally who is not Wise; a move trigger that heals an [elven] ally
					put("lindir", "33_14");
					put("balin", "30_2");       // Balin, Brother of Dwalin: a companion who is not Wise; a Fellowship ability

					put("runner", "31_30");     // Goblin Runner: [moria] Orc; a play trigger only
					put("ferny", "33_22");      // Bill Ferny Sr.: [gundabad] Man; a Regroup ability (kill an ally of home site 1: there is none)
					// Dark Sorcery: a Shadow card in play that is no minion; its Shadow ability is used only on purpose
					put("sorcery", "33_61");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void JailStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Jail
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Regroup: Discard a minion to stack a Wise character on this condition. A Wise character may exert twice to prevent this. Copies of characters stacked here cannot be played.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("jail");

		assertEquals("Jail", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RegroupActionDiscardsAMinionToStackAWiseCharacterWhenThePreventionIsDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var balin = scn.GetFreepsCard("balin");
		var lindir = scn.GetFreepsCard("lindir");
		scn.MoveCompanionsToTable(gandalf, balin);

		var jail = scn.GetShadowCard("jail");
		var runner = scn.GetShadowCard("runner");
		var ferny = scn.GetShadowCard("ferny");
		var jail2 = scn.GetShadowCard("jail2");
		var sorcery = scn.GetShadowCard("sorcery");
		scn.MoveCardsToSupportArea(jail, jail2, sorcery);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond, lindir);
		scn.MoveMinionsToTable(runner, ferny);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetStackedCards(jail).size());
		assertEquals(0, scn.GetStackedCards(jail2).size());
		assertTrue(scn.ShadowActionAvailable(jail));
		scn.ShadowUseCardAction(jail);

		// The cost: either minion, of two cultures and races; not Dark Sorcery, a Shadow card that is no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, ferny));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sorcery));
		scn.ShadowChooseCard(runner);
		assertInDiscard(runner);

		// A Wise companion and a Wise ally; not Balin, Lindir or Bilbo, who are not Wise.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, elrond));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(balin, lindir, bilbo));
		scn.ShadowChooseCard(gandalf);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetStackedCards(jail).size());
		assertTrue(scn.GetStackedCards(jail).contains(gandalf));
		// On the Jail whose action it was, not the other.
		assertEquals(0, scn.GetStackedCards(jail2).size());
		assertInZone(Zone.STACKED, gandalf);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertInPlay(ferny, elrond, balin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWiseCharacterMayExertTwiceToPreventTheStacking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var galadriel = scn.GetFreepsCard("galadriel");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf, balin);

		var jail = scn.GetShadowCard("jail");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(jail);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond, galadriel);
		// Vitality 3 with a wound: Galadriel cannot exert twice.
		scn.AddWoundsToChar(galadriel, 1);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		scn.ShadowUseCardAction(jail);
		// One minion: the cost is paid with it unasked.
		assertInDiscard(runner);
		scn.ShadowChooseCard(gandalf);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// Any Wise character who can exert twice pays, not only the one to be stacked.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(galadriel, balin, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsChooseCard(elrond);

		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetStackedCards(jail).size());
		assertInDiscard(runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThePreventionIsNotAskedWhenNoWiseCharacterCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gandalf);

		var jail = scn.GetShadowCard("jail");
		scn.MoveCardsToSupportArea(jail);

		scn.StartGame();
		scn.MoveAlliesToTable(elrond);
		// Vitality 4 with two wounds each: neither can exert twice.
		scn.AddWoundsToChar(gandalf, 2);
		scn.AddWoundsToChar(elrond, 2);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(elrond));
		scn.MoveMinionsToTable("runner");
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		scn.ShadowUseCardAction(jail);
		scn.ShadowChooseCard(gandalf);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertTrue(scn.GetStackedCards(jail).contains(gandalf));
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoWiseCharacterTheMinionIsStillDiscardedAndNothingIsStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);

		var jail = scn.GetShadowCard("jail");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(jail);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(jail));
		scn.ShadowUseCardAction(jail);

		assertInDiscard(runner);
		assertEquals(0, scn.GetStackedCards(jail).size());
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInPlay(balin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithoutAMinionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var jail = scn.GetShadowCard("jail");
		var sorcery = scn.GetShadowCard("sorcery");
		scn.MoveCardsToSupportArea(jail, sorcery);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(jail));
		assertInPlay(gandalf);
	}

	@Test
	public void CopiesOfACharacterStackedHereCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gandalf3 = scn.GetFreepsCard("gandalf3");
		var gandalf2 = scn.GetFreepsCard("gandalf2");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToHand(gandalf3, gandalf2, elrond);

		var jail = scn.GetShadowCard("jail");
		scn.MoveCardsToSupportArea(jail);
		scn.StackCardsOn(jail, gandalf);

		scn.StartGame();

		assertTrue(scn.GetStackedCards(jail).contains(gandalf));
		assertInHand(gandalf3, gandalf2, elrond);
		// The same card and a Gandalf of another subtitle (copies go by title); Elrond is no copy.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gandalf3));
		assertFalse(scn.FreepsPlayAvailable(gandalf2));
		assertTrue(scn.FreepsPlayAvailable(elrond));

		// With Gandalf no longer stacked on Jail, both may be played.
		scn.MoveCardsToDiscard(gandalf);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetStackedCards(jail).size());
		assertTrue(scn.FreepsPlayAvailable(gandalf3));
		assertTrue(scn.FreepsPlayAvailable(gandalf2));
	}
}
