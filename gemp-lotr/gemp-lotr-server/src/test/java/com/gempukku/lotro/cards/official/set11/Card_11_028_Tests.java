package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_028_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("art", "11_28");
					put("art2", "11_28");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; a strength bonus only
					// Saruman, The White: a [gandalf] Wizard ally (a Hobbit-set card, the one Free Peoples Wizard who is no
					// companion beside Radagast 31_17); a Gandalf is defender +1 beside him, and his abilities are used
					// only on purpose
					put("saruman", "32_24");
					// Ent Avenger: a [gandalf] companion who is no Wizard; a strength bonus only beside 3 Ents
					put("avenger", "6_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] card; a maneuver ability only

					put("mysterious", "1_78");  // Mysterious Wizard: a [gandalf] spell event (Skirmish)
					put("salve", "12_32");      // Salve: a [gandalf] spell condition, played on a companion
					put("power", "1_136");      // Saruman's Power: an [isengard] spell event (Shadow)
					put("durins", "1_71");      // Durin's Secret: a [gandalf] Fellowship event that is no spell

					// Footman of Dunland: a [men] Man minion, a card that is no spell which play can stack here (Ted Sandyman
					// 18_75: "Regroup: Exert Ted Sandyman to stack a [men] Man in play on a possession"); stacked, it is not active,
					// and is not the Free Peoples player's to take (Q-0364: C)
					put("footman", "11_82");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheArtofGandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The Art of Gandalf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: To play, spot a [gandalf] Wizard.
		 * 	<b>Regroup:</b> Stack a spell from hand here.
		 * 	<b>Fellowship:</b> Discard a [gandalf] card from hand to take a card stacked here into hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("art");

		assertEquals("The Art of Gandalf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAGandalfWizardIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable("avenger");
		scn.MoveCardsToHand(art, gandalf);

		scn.StartGame();
		assertInHand(art, gandalf);

		// A [gandalf] companion who is no Wizard is not enough.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(art));

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		assertTrue(scn.FreepsPlayAvailable(art));
		scn.FreepsPlayCard(art);
		assertInZone(Zone.SUPPORT, art);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedWithAGandalfWizardAllyAsTheOnlyWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");
		scn.MoveAlliesToTable("saruman");
		scn.MoveCardsToHand(art);

		scn.StartGame();
		assertInHand(art);

		assertTrue(scn.FreepsPlayAvailable(art));
		scn.FreepsPlayCard(art);
		assertInZone(Zone.SUPPORT, art);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityStacksASpellOfAnyCultureOrTypeFromHandHereAndNotOnTheOtherCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");
		var art2 = scn.GetFreepsCard("art2");
		var mysterious = scn.GetFreepsCard("mysterious");
		var salve = scn.GetFreepsCard("salve");
		var power = scn.GetFreepsCard("power");
		var durins = scn.GetFreepsCard("durins");
		scn.MoveCardsToSupportArea(art, art2);
		scn.MoveCardsToHand(mysterious, salve, power, durins);
		assertInZone(Zone.SUPPORT, art, art2);
		assertInHand(mysterious, salve, power, durins);
		assertEquals(0, scn.GetStackedCards(art).size());
		assertEquals(0, scn.GetStackedCards(art2).size());

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(art));
		scn.FreepsUseCardAction(art);

		// A [gandalf] spell event and condition, and an [isengard] spell; not a [gandalf] event that is no spell.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(mysterious, salve, power));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(durins));
		scn.FreepsChooseCard(salve);

		assertInZone(Zone.STACKED, salve);
		assertEquals(1, scn.GetStackedCards(art).size());
		assertSame(salve, scn.GetStackedCards(art).get(0));
		// "Here": the other copy has nothing stacked on it.
		assertEquals(0, scn.GetStackedCards(art2).size());
		assertEquals(3, scn.GetFreepsHandCount());
		assertInHand(mysterious, power, durins);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithNoSpellInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");
		var durins = scn.GetFreepsCard("durins");
		scn.MoveCardsToSupportArea(art);
		scn.MoveCardsToHand(durins);
		assertInZone(Zone.SUPPORT, art);
		assertInHand(durins);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(art));
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");
		var mysterious = scn.GetFreepsCard("mysterious");
		scn.MoveCompanionsToTable("aragorn");
		scn.MoveCardsToSupportArea(art);
		scn.MoveCardsToHand(mysterious);
		assertInZone(Zone.SUPPORT, art);
		assertInHand(mysterious);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInHand(mysterious);
		assertFalse(scn.FreepsActionAvailable(art));
	}

	@Test
	public void FellowshipAbilityDiscardsAGandalfCardFromHandToTakeACardStackedHereIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");
		var art2 = scn.GetFreepsCard("art2");
		var mysterious = scn.GetFreepsCard("mysterious");
		var salve = scn.GetFreepsCard("salve");
		var power = scn.GetFreepsCard("power");
		var durins = scn.GetFreepsCard("durins");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCardsToSupportArea(art, art2);
		scn.StackCardsOn(art, mysterious, power, footman);
		scn.StackCardsOn(art2, salve);
		scn.MoveCardsToHand(durins, avenger, aragorn);
		assertEquals(3, scn.GetStackedCards(art).size());
		assertEquals(1, scn.GetStackedCards(art2).size());
		assertInZone(Zone.STACKED, mysterious, power, footman, salve);
		assertInHand(durins, avenger, aragorn);

		scn.StartGame();

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(art));
		scn.FreepsUseCardAction(art);

		// The cost: a [gandalf] event or companion, not a [gondor] card.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(durins, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(durins);
		assertInDiscard(durins);
		assertEquals(1, scn.GetFreepsDiscardCount());

		// The take: his own cards stacked here, a spell of either culture; not the Shadow player's Man (Q-0364: C),
		// nor the one on the other copy.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(mysterious, power));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(footman, salve));
		scn.FreepsChooseCard(power);

		assertInHand(power, avenger, aragorn);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetStackedCards(art).size());
		assertInZone(Zone.STACKED, mysterious, footman);
		assertEquals(1, scn.GetStackedCards(art2).size());
		assertSame(salve, scn.GetStackedCards(art2).get(0));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipAbilityIsNotOfferedWithNoGandalfCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");
		var mysterious = scn.GetFreepsCard("mysterious");
		var durins = scn.GetFreepsCard("durins");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(art);
		scn.StackCardsOn(art, mysterious);
		// [gandalf] cards in play and in the discard pile; in hand, only a [gondor] card.
		scn.MoveCompanionsToTable("gandalf");
		scn.MoveCardsToDiscard(durins);
		scn.MoveCardsToHand(aragorn);
		assertEquals(1, scn.GetStackedCards(art).size());
		assertInDiscard(durins);
		assertInHand(aragorn);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(art));
		assertEquals(1, scn.GetStackedCards(art).size());
	}

	@Test
	public void WithNothingStackedHereTheFellowshipAbilityIsPaidForAndTakesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var art = scn.GetFreepsCard("art");
		var art2 = scn.GetFreepsCard("art2");
		var salve = scn.GetFreepsCard("salve");
		var durins = scn.GetFreepsCard("durins");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(art, art2);
		scn.StackCardsOn(art2, salve);
		scn.MoveCardsToHand(durins, aragorn);
		assertEquals(0, scn.GetStackedCards(art).size());
		assertEquals(1, scn.GetStackedCards(art2).size());
		assertInHand(durins, aragorn);

		scn.StartGame();

		// An ability with a cost is offered whatever its effect can do.
		assertTrue(scn.FreepsActionAvailable(art));
		scn.FreepsUseCardAction(art);

		// The one [gandalf] card in hand is discarded unasked, and nothing is taken.
		assertInDiscard(durins);
		assertInHand(aragorn);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetStackedCards(art2).size());
		assertSame(salve, scn.GetStackedCards(art2).get(0));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
