package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erland", "11_30");
					// Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; his text is about his own strength
					put("gandalf", "1_72");
					// Ent Avenger: a [gandalf] companion who is not Gandalf (an Ent); +2 strength only beside 3 Ents
					put("avenger", "6_27");
					// Barliman Butterbur: a [gandalf] ally, no companion
					put("barliman", "1_70");
					// Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability only
					put("aragorn", "1_89");

					// Mysterious Wizard: [gandalf] Skirmish event
					put("wizard", "1_78");
					// Show Them the Meaning of Haste: [gandalf] event printed "Maneuver or Skirmish"
					put("haste", "103_6");
					// Risk a Little Light: [gandalf] Fellowship event
					put("risk", "1_82");
					// Long Slow Wrath: a [shire] Skirmish event
					put("wrath", "6_112");
					// The Nine Walkers: a [gandalf] condition, no event
					put("walkers", "1_79");
					// Under the Living Earth: a [gandalf] Skirmish event, laid only in the Shadow player's discard pile
					put("living", "4_105");

					// Goblin Patrol Troop: a minion with no text, to make a skirmish
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErlandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Erland, Dale Counselor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot a [gandalf] companion.<br><b>Skirmish:</b> Exert Erland to take a [gandalf] skirmish event from your discard pile into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("erland");

		assertEquals("Erland", card.getBlueprint().getTitle());
		assertEquals("Dale Counselor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAGandalfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(erland, gandalf);

		scn.StartGame();

		// A [gandalf] ally, and companions of other cultures (Frodo, Aragorn), do not do.
		assertInZone(Zone.HAND, erland, gandalf);
		assertFalse(scn.FreepsPlayAvailable(erland));

		scn.FreepsPlayCard(gandalf);

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.FreepsPlayAvailable(erland));

		scn.FreepsPlayCard(erland);

		assertInZone(Zone.FREE_CHARACTERS, erland);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfCompanionWhoIsNotGandalfAlsoLetsHimBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(erland);

		scn.StartGame();

		assertInZone(Zone.HAND, erland);
		assertTrue(scn.FreepsPlayAvailable(erland));

		scn.FreepsPlayCard(erland);

		assertInZone(Zone.FREE_CHARACTERS, erland);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsErlandToTakeAGandalfSkirmishEventFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var wizard = scn.GetFreepsCard("wizard");
		var haste = scn.GetFreepsCard("haste");
		var risk = scn.GetFreepsCard("risk");
		var wrath = scn.GetFreepsCard("wrath");
		var walkers = scn.GetFreepsCard("walkers");
		var living = scn.GetShadowCard("living");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(erland);
		scn.MoveCardsToDiscard(wizard, haste, risk, wrath, walkers, living);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		// Erland is in no skirmish; his text names none.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(erland));
		assertInZone(Zone.DISCARD, wizard, haste, risk, wrath, walkers, living);
		assertTrue(scn.FreepsActionAvailable(erland));

		scn.FreepsUseCardAction(erland);

		assertEquals(1, scn.GetWoundsOn(erland));
		// Not offered: a [gandalf] Fellowship event, a [shire] skirmish event, a [gandalf] condition, and the
		// Shadow player's own [gandalf] skirmish event in his discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(wizard, haste));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(risk, wrath, walkers, living));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCardBPFromSelection(wizard);

		assertInZone(Zone.HAND, wizard);
		assertInZone(Zone.DISCARD, haste, risk, wrath, walkers, living);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedOnceErlandIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var erland = scn.GetFreepsCard("erland");
		var wizard = scn.GetFreepsCard("wizard");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(erland);
		scn.MoveCardsToDiscard(wizard);

		scn.StartGame();
		scn.AddWoundsToChar(erland, 2);
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		// Vitality 3 with 2 wounds: he cannot exert.
		assertEquals(2, scn.GetWoundsOn(erland));
		assertInZone(Zone.DISCARD, wizard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(erland));
	}
}
