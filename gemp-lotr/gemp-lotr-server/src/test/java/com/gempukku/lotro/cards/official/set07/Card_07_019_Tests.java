package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("study", "7_19");
					// Legolas, Companion of the Ring: an Elf companion; his text needs a battleground or forest
					put("legolas", "11_21");
					// Silinde, Elf of Mirkwood: an Elf ally, in the support area; he copies the text of the player's
					// site 3, Rivendell Valley, whose Fellowship action plays no part here
					put("silinde", "1_60");
					// Aragorn, Ranger of the North: a companion who is no Elf; his Maneuver ability is not used
					put("aragorn", "1_89");
					// Cards for the hand, of several cultures and both sides: Haldir, an [elven] Elf companion;
					// Defiance, an [elven] Skirmish event; Swordarm of the White Tower, a [gondor] Skirmish event;
					// Morgul Hound, a [wraith] minion; Goblin Patrol Troop, a [moria] minion with no text
					put("haldir", "1_48");
					put("defiance", "1_37");
					put("swordarm", "1_116");
					put("hound", "7_192");
					put("troop", "1_177");
					// More cards with no text, so that the draw deck holds enough for a draw up to 8
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
					put("filler7", "1_177");
					put("filler8", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CarefulStudyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Careful Study
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot an Elf and discard 3 cards from hand to reconcile your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("study");

		assertEquals("Careful Study", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElfCompanionItDiscardsThreeChosenCardsFromHandAndReconciles() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var study = scn.GetFreepsCard("study");
		var legolas = scn.GetFreepsCard("legolas");
		var haldir = scn.GetFreepsCard("haldir");
		var defiance = scn.GetFreepsCard("defiance");
		var swordarm = scn.GetFreepsCard("swordarm");
		var hound = scn.GetFreepsCard("hound");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(study, haldir, defiance, swordarm, hound, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertInHand(study, haldir, defiance, swordarm, hound, troop);
		assertEquals(6, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(study));

		scn.FreepsPlayCard(study);
		// Exactly three, of any of the five other cards in hand, whatever their culture or side.
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, defiance, swordarm, hound, troop));
		// The event, in the void while it is played, is not one of them.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(study));
		scn.FreepsChooseCards(haldir, defiance, hound);
		assertInDiscard(haldir, defiance, hound);
		assertInHand(swordarm, troop);

		// The reconcile: its own discard of one is declined, and he draws from two cards to eight.
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 6, scn.GetFreepsDeckCount());
		assertInHand(swordarm, troop);
		assertInDiscard(study);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SpottingAnElfAllyWithExactlyThreeOtherCardsInHandItDiscardsThemAllAndDrawsEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var study = scn.GetFreepsCard("study");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var swordarm = scn.GetFreepsCard("swordarm");
		var hound = scn.GetFreepsCard("hound");
		var troop = scn.GetFreepsCard("troop");
		// The only Elf is an ally; the companion beside him is none.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(study, swordarm, hound, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertInHand(study, swordarm, hound, troop);
		assertEquals(4, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(study));

		scn.FreepsPlayCard(study);
		// The three are all there is to discard; with the hand then empty the reconcile asks nothing.
		assertInDiscard(swordarm, hound, troop);
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 8, scn.GetFreepsDeckCount());
		assertInDiscard(study);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyTwoOtherCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var study = scn.GetFreepsCard("study");
		var legolas = scn.GetFreepsCard("legolas");
		var swordarm = scn.GetFreepsCard("swordarm");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToHand(study, swordarm, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// The event itself cannot be discarded to pay for it (CR 4.2 "playing a card").
		assertInHand(study, swordarm, troop);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(study));
	}

	@Test
	public void CannotBePlayedWithNoElfInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var study = scn.GetFreepsCard("study");
		var aragorn = scn.GetFreepsCard("aragorn");
		var haldir = scn.GetFreepsCard("haldir");
		var swordarm = scn.GetFreepsCard("swordarm");
		var hound = scn.GetFreepsCard("hound");
		var troop = scn.GetFreepsCard("troop");
		// Aragorn and Frodo are no Elves; the Elf Haldir is in hand, not in play.  The cost could be paid.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(study, haldir, swordarm, hound, troop);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		assertInHand(study, haldir, swordarm, hound, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(study));
	}
}
