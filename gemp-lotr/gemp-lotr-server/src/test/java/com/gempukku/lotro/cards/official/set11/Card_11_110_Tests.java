package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fate", "11_110");

					put("miscreant", "11_131");  // Orc Miscreant: [orc] Orc; a regroup ability used only on purpose
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll; its assignment trigger is never reached
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, cheated into the support area

					// For the Free Peoples hand.
					put("noble", "1_304");       // Noble Intentions: a [shire] Free Peoples event
					put("stout", "7_121");       // Stout Resistance: a [gondor] Free Peoples event
					put("boromir", "1_96");      // Boromir, Lord of Gondor: a Free Peoples card that is no event
					put("drums", "1_168");       // Drums in the Deep: a [moria] Shadow event
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: left in the Free Peoples draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoundtoitsFateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Bound to its Fate
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot an [orc] minion to reveal the Free Peoples player's hand. The Free Peoples player chooses to either discard a revealed Free Peoples event or add a burden.
		*/

		var scn = GetScenario();

		var fate = scn.GetFreepsCard("fate");

		assertEquals("Bound to its Fate", fate.getBlueprint().getTitle());
		assertNull(fate.getBlueprint().getSubtitle());
		assertFalse(fate.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fate.getBlueprint().getSide());
		assertEquals(Culture.ORC, fate.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fate.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(fate, Timeword.SHADOW));
		assertEquals(1, fate.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetShadowCard("fate");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(fate);
		assertInHand(fate);

		scn.StartGame();
		// A [moria] Orc and an [orc] card that is no minion.
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(beckoning);
		scn.SetTwilight(5);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fate));

		// An [orc] minion who is no Orc.
		scn.MoveMinionsToTable(halftroll);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fate));
	}

	@Test
	public void RevealsTheFreePeoplesHandToTheShadowPlayerAndTheFreePeoplesPlayerMayDiscardARevealedFreePeoplesEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetShadowCard("fate");
		var miscreant = scn.GetShadowCard("miscreant");
		var noble = scn.GetFreepsCard("noble");
		var stout = scn.GetFreepsCard("stout");
		var boromir = scn.GetFreepsCard("boromir");
		var drums = scn.GetFreepsCard("drums");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(fate);
		scn.MoveCardsToHand(noble, stout, boromir, drums);
		assertInHand(fate, noble, stout, boromir, drums);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowPlayAvailable(fate));
		scn.ShadowPlayCard(fate);

		// The whole Free Peoples hand is shown to the Shadow player; a card of the draw deck is not, and the hand's
		// owner is shown nothing.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(noble, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(stout, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(boromir, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(drums, false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(aragorn, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// The Free Peoples player chooses, and both are open to him.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(List.of("Discard a Free Peoples event", "Add a burden"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard a Free Peoples event");

		// Free Peoples events of two cultures are offered; a Free Peoples card that is no event and a Shadow event
		// in the same hand are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(noble, stout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, drums));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(stout);

		assertInDiscard(stout);
		assertInDiscard(fate);
		assertInHand(noble, boromir, drums);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddABurdenInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetShadowCard("fate");
		var miscreant = scn.GetShadowCard("miscreant");
		var noble = scn.GetFreepsCard("noble");
		var stout = scn.GetFreepsCard("stout");
		var boromir = scn.GetFreepsCard("boromir");
		var drums = scn.GetFreepsCard("drums");
		scn.MoveCardsToHand(fate);
		scn.MoveCardsToHand(noble, stout, boromir, drums);
		assertInHand(fate, noble, stout, boromir, drums);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(fate);
		scn.ShadowDismissRevealedCards();

		assertTrue(scn.FreepsChoiceAvailable("Add a burden"));
		scn.FreepsChooseOption("Add a burden");

		assertEquals(1, scn.GetBurdens());
		assertInHand(noble, stout, boromir, drums);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesEventRevealedABurdenIsAddedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fate = scn.GetShadowCard("fate");
		var miscreant = scn.GetShadowCard("miscreant");
		var boromir = scn.GetFreepsCard("boromir");
		var drums = scn.GetFreepsCard("drums");
		scn.MoveCardsToHand(fate);
		scn.MoveCardsToHand(boromir, drums);
		assertInHand(fate, boromir, drums);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(fate);
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// Nothing in the hand can be discarded, so the burden is the only branch that can be done in full.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(1, scn.GetBurdens());
		assertInHand(boromir, drums);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
