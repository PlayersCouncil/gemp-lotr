package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_035_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "57_35");
					put("hope2", "57_35");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus and nothing else
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: Defender +1 and no other text; bears Shadow cards here
					put("card1", "1_53");       // Lórien Elf: cards in hand to discard
					put("card2", "1_53");
					put("card3", "1_53");
					put("walls", "7_111");      // Man the Walls: a Free Peoples ([gondor]) condition in the support area

					put("tidings", "7_130");    // Dark Tidings: [raider] condition, support area, a regroup ability
					put("tidings2", "7_130");
					put("beetles", "10_65");    // Swarming Like Beetles: [wraith] condition, support area, a maneuver ability
					put("wielding", "11_105");  // Wielding the Ring: [men] condition borne by a companion, no other text
					put("heavy", "7_60");       // Heavy Burden: [gollum] condition, support area, a regroup ability; hindered here
					put("poison", "8_28");      // Spider Poison: a Shadow possession borne by a companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FoolsHopeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Fool's Hope
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Spot Gandalf and discard X cards from hand to make an opponent choose to discard X Shadow conditions or make you hinder X Shadow conditions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope");

		assertEquals("Fool's Hope", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var card1 = scn.GetFreepsCard("card1");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(hope, gandalf, card1);

		scn.StartGame();
		scn.MoveCardsToSupportArea(scn.GetShadowCard("tidings"));
		scn.FreepsRefreshDecision();

		// A [gandalf] companion of another title.
		assertFalse(scn.FreepsPlayAvailable(hope));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(hope));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingTwoCardsTheOpponentMayChooseToDiscardTwoShadowConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var walls = scn.GetFreepsCard("walls");
		var tidings = scn.GetShadowCard("tidings");
		var beetles = scn.GetShadowCard("beetles");
		var wielding = scn.GetShadowCard("wielding");
		var heavy = scn.GetShadowCard("heavy");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(walls);
		scn.MoveCardsToHand(hope, card1, card2, card3);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tidings, beetles, heavy);
		scn.AttachCardsTo(aragorn, wielding, poison);
		scn.FreepsRefreshDecision();
		scn.HinderCard(heavy);

		assertTrue(scn.FreepsPlayAvailable(hope));
		scn.FreepsPlayCard(hope);

		assertTrue(scn.FreepsHasCardChoiceAvailable(card1, card2, card3));
		scn.FreepsChooseCards(card1, card2);
		assertInDiscard(card1, card2);
		assertInHand(card3);

		// The opponent chooses between the two.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Discard");

		// He chooses which: Shadow conditions of three cultures, in the support area and on a companion.  Not his
		// Shadow possession, not a Free Peoples condition, not a hindered Shadow condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tidings, beetles, wielding));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(heavy, poison, walls));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(tidings, wielding);

		assertInDiscard(tidings, wielding, hope);
		assertInZone(Zone.SUPPORT, beetles, heavy);
		assertTrue(scn.IsHindered(heavy));
		assertFalse(scn.IsHindered(beetles));
		assertEquals(aragorn, poison.getAttachedTo());
		assertInZone(Zone.SUPPORT, walls);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingOneCardTheOpponentMayInsteadMakeYouHinderOneShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var walls = scn.GetFreepsCard("walls");
		var tidings = scn.GetShadowCard("tidings");
		var beetles = scn.GetShadowCard("beetles");
		var wielding = scn.GetShadowCard("wielding");
		var heavy = scn.GetShadowCard("heavy");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(walls);
		scn.MoveCardsToHand(hope, card1, card2);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tidings, beetles, heavy);
		scn.AttachCardsTo(aragorn, wielding, poison);
		scn.FreepsRefreshDecision();
		scn.HinderCard(heavy);

		int shadowDiscard = scn.GetShadowDiscardCount();
		scn.FreepsPlayCard(hope);
		scn.FreepsChooseCards(card1);
		assertInDiscard(card1);
		assertInHand(card2);

		scn.ShadowChooseOption("hinder");

		// The Free Peoples player chooses which.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tidings, beetles, wielding));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(heavy, poison, walls));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(wielding);

		assertTrue(scn.IsHindered(wielding));
		assertEquals(aragorn, wielding.getAttachedTo());
		assertFalse(scn.IsHindered(tidings));
		assertFalse(scn.IsHindered(beetles));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(walls));
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheNumberOfCardsDiscardedSetsTheNumberOfConditionsOnEitherBranch() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var hope2 = scn.GetFreepsCard("hope2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var tidings = scn.GetShadowCard("tidings");
		var tidings2 = scn.GetShadowCard("tidings2");
		var beetles = scn.GetShadowCard("beetles");
		var wielding = scn.GetShadowCard("wielding");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(hope, hope2, card1, card2, card3);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tidings, tidings2, beetles);
		scn.AttachCardsTo(aragorn, wielding);
		scn.FreepsRefreshDecision();

		// One card discarded: one Shadow condition to discard.
		scn.FreepsPlayCard(hope);
		scn.FreepsChooseCards(card1);
		scn.ShadowChooseOption("Discard");
		assertTrue(scn.ShadowHasCardChoiceAvailable(tidings, tidings2, beetles, wielding));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(tidings);
		assertInDiscard(card1, tidings);
		assertInZone(Zone.SUPPORT, tidings2, beetles);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Two cards discarded: two Shadow conditions to hinder.
		scn.FreepsPlayCard(hope2);
		scn.FreepsChooseCards(card2, card3);
		scn.ShadowChooseOption("hinder");
		assertTrue(scn.FreepsHasCardChoiceAvailable(tidings2, beetles, wielding));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tidings));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(tidings2, wielding);
		assertTrue(scn.IsHindered(tidings2));
		assertTrue(scn.IsHindered(wielding));
		assertFalse(scn.IsHindered(beetles));
		assertInDiscard(card2, card3, hope2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoShadowConditionTheEventIsPlayedAndTheOpponentIsAskedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var card1 = scn.GetFreepsCard("card1");
		var heavy = scn.GetShadowCard("heavy");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(hope, card1);

		scn.StartGame();
		// A hindered Shadow condition and a Shadow possession: nothing either branch could act on.
		scn.MoveCardsToSupportArea(heavy);
		scn.HinderCard(heavy);
		scn.AttachCardsTo(aragorn, poison);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(hope));
		scn.FreepsPlayCard(hope);
		scn.FreepsChooseCards(card1);

		assertInDiscard(card1, hope);
		assertTrue(scn.IsHindered(heavy));
		assertEquals(aragorn, poison.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingNoCardsTheEventIsPlayedAndTheOpponentIsAskedNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var gandalf = scn.GetFreepsCard("gandalf");
		var tidings = scn.GetShadowCard("tidings");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(hope);

		scn.StartGame();
		// A Shadow condition that either branch could act on, if X were more than zero.
		scn.MoveCardsToSupportArea(tidings);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(hope));
		scn.FreepsPlayCard(hope);

		// Nothing in hand to discard, so X is 0: discarding 0 or hindering 0 is nothing to choose between.
		assertInDiscard(hope);
		assertInZone(Zone.SUPPORT, tidings);
		assertFalse(scn.IsHindered(tidings));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
