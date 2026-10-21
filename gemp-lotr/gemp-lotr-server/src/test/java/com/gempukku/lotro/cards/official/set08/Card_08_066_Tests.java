package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wind", "8_66");

					put("ships1", "8_65");      // Ships of Great Draught: [raider] support-area possession to hold [raider] tokens; its abilities are never used
					put("ships2", "8_65");
					put("raiders", "4_242");    // Raiders From the East: [raider] support-area condition that holds [raider] tokens; its abilities are never used
					put("sails", "8_50");       // Black Sails of Umbar: [raider] support-area possession that holds [raider] tokens; its ability is never used
					put("path", "8_42");        // A Path Appointed: [gondor] support-area condition that holds [gondor] tokens; its abilities are never used
					put("buccaneer", "8_53");   // Corsair Buccaneer: [raider] corsair Man, strength 8; his play trigger is not met here (he is cheated in)
					put("plunderer", "8_58");   // Corsair Plunderer: [raider] corsair Man, strength 6; his play trigger is not met here (he is cheated in)
					put("plunderer2", "8_58");  // a second Corsair Plunderer, left out of every skirmish
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man who is no corsair, strength 6; +3 and fierce only at 2 burdens, and there are none
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability never used here
					put("guard", "5_83");       // Household Guard: [rohan] Man, strength 6, vitality 3; valiant and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WindThatSpedShipsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Wind That Sped Ships
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a corsair strength +1 for each [raider] token you spot (limit +6). If you have initiative, you may place this event on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wind");

		assertEquals("Wind That Sped Ships", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheNumberOfTokensToSpotComesFilledInWithAllUpToTheLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wind = scn.GetShadowCard("wind");
		var ships1 = scn.GetShadowCard("ships1");
		var ships2 = scn.GetShadowCard("ships2");
		var buccaneer = scn.GetShadowCard("buccaneer");
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		scn.MoveCardsToSupportArea(ships1, ships2);
		// Seven [raider] tokens, over two cards: more than the limit of +6.
		scn.AddTokensToCard(ships1, 4);
		scn.AddTokensToCard(ships2, 3);
		scn.MoveMinionsToTable(buccaneer);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, buccaneer);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(buccaneer));
		scn.ShadowPlayCard(wind);

		// The player names how many tokens he spots, 0 to the limit; the box comes filled in with the most
		// (Q-0341: A, as F-0561), and the player accepts it.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(6, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(6), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("6");

		assertEquals(14, scn.GetStrength(buccaneer));
		// The Shadow player has initiative (the Free Peoples hand is empty) and is offered the deck; he declines.
		scn.ShadowChooseNo();
		assertInZone(Zone.DISCARD, wind);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesTheCorsairChosenStrongerByTheRaiderTokensNamedAndWithInitiativeGoesOnTopOfTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(path);
		// Three cards in the Free Peoples hand: the Shadow has initiative, and only just.
		scn.MoveCardsToHand(scn.GetFreepsCard("axeman"), scn.GetFreepsCard("plunderer"), scn.GetFreepsCard("buccaneer"));

		var wind = scn.GetShadowCard("wind");
		var raiders = scn.GetShadowCard("raiders");
		var sails = scn.GetShadowCard("sails");
		var plunderer = scn.GetShadowCard("plunderer");
		var buccaneer = scn.GetShadowCard("buccaneer");
		var plunderer2 = scn.GetShadowCard("plunderer2");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		scn.MoveCardsToSupportArea(raiders, sails);
		// Three [raider] tokens, on a condition and a possession; a [gondor] token beside them.
		scn.AddTokensToCard(raiders, 2);
		scn.AddTokensToCard(sails, 1);
		scn.AddTokensToCard(path, 1);
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(sails));
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(plunderer, buccaneer, plunderer2, axeman);

		scn.SkipToAssignments();
		// The second Plunderer and the Axeman are left out of the skirmishes.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, plunderer}, new PhysicalCardImpl[]{guard, buccaneer});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(6, scn.GetStrength(plunderer));
		assertEquals(8, scn.GetStrength(buccaneer));
		assertEquals(6, scn.GetStrength(plunderer2));
		assertEquals(6, scn.GetStrength(axeman));
		int deckBefore = scn.GetShadowDeckCount();
		assertTrue(scn.ShadowPlayAvailable(wind));
		scn.ShadowPlayCard(wind);

		// The three [raider] tokens are what may be spotted; the [gondor] token is not one of them.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(3), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// A corsair in this skirmish, in one still to come or in none; not the [raider] Man who is no corsair.
		assertTrue(scn.ShadowHasCardChoiceAvailable(plunderer, buccaneer, plunderer2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(plunderer);

		// Two named of the three: 6 + 2.
		assertEquals(8, scn.GetStrength(plunderer));
		assertEquals(8, scn.GetStrength(buccaneer));
		assertEquals(6, scn.GetStrength(plunderer2));
		assertEquals(6, scn.GetStrength(axeman));

		assertTrue(scn.ShadowDecisionAvailable("Would you like to place"));
		scn.ShadowChooseYes();
		assertEquals(wind, scn.GetShadowTopOfDeck());
		assertEquals(deckBefore + 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithoutInitiativeItIsDiscardedAndTheBonusLastsOnlyForTheSkirmishItWasPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		// Four cards in the Free Peoples hand: the Shadow does not have initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("axeman"), scn.GetFreepsCard("plunderer"),
				scn.GetFreepsCard("buccaneer"), scn.GetFreepsCard("raiders"));

		var wind = scn.GetShadowCard("wind");
		var raiders = scn.GetShadowCard("raiders");
		var sails = scn.GetShadowCard("sails");
		var plunderer = scn.GetShadowCard("plunderer");
		var buccaneer = scn.GetShadowCard("buccaneer");
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		scn.MoveCardsToSupportArea(raiders, sails);
		scn.AddTokensToCard(raiders, 2);
		scn.AddTokensToCard(sails, 1);
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		assertEquals(1, scn.GetCultureTokensOn(sails));
		scn.MoveMinionsToTable(plunderer, buccaneer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, plunderer}, new PhysicalCardImpl[]{guard, buccaneer});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		assertEquals(8, scn.GetStrength(buccaneer));
		int deckBefore = scn.GetShadowDeckCount();
		scn.ShadowPlayCard(wind);
		scn.ShadowChoose("3");
		// The Buccaneer's skirmish is still to come.
		scn.ShadowChooseCard(buccaneer);

		// All three named: 8 + 3.
		assertEquals(11, scn.GetStrength(buccaneer));
		// No initiative, so nothing is asked and the event is discarded.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to place"));
		assertInDiscard(wind);
		assertEquals(deckBefore, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn beats the Plunderer, 8 to 6; that skirmish was the phase the Buccaneer's +3 lasted for.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, buccaneer));
		assertEquals(8, scn.GetStrength(buccaneer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndWithNoCorsairPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wind = scn.GetShadowCard("wind");
		var raiders = scn.GetShadowCard("raiders");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(wind);

		scn.StartGame();
		scn.MoveCardsToSupportArea(raiders);
		scn.AddTokensToCard(raiders, 2);
		assertEquals(2, scn.GetCultureTokensOn(raiders));
		scn.MoveMinionsToTable(axeman);

		// The assignment phase, next door to the skirmish: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wind));
		scn.ShadowPass();

		scn.FreepsAssignAndResolve(aragorn, axeman);
		scn.FreepsPass();

		// No corsair on the table: the event is played all the same, and strengthens nobody.
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.ShadowPlayAvailable(wind));
		scn.ShadowPlayCard(wind);
		scn.ShadowChoose("2");
		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(8, scn.GetStrength(aragorn));
		// The Free Peoples hand is empty: the Shadow player has initiative and is asked about the deck.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to place"));
		scn.ShadowChooseNo();
		assertInDiscard(wind);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
