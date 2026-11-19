package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_031_Tests
{

	// The Ring-bearer is Frodo, Son of Drogo, who is kept out of the skirmishes.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("allsave", "7_31");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: strength 8, vitality 4, no text and no damage bonus
					// Ent Avenger: a [gandalf] companion who is no Gandalf, strength 6; +2 only beside 3 Ents
					put("avenger", "6_27");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					// Cards that only fill the Free Peoples hand, for initiative (four or more in hand)
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AllSaveOneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: All Save One
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make Gandalf strength +2 (or +4 and <b>damage +1</b> if you have initiative).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("allsave");

		assertEquals("All Save One", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FromAHandOfFourWithoutInitiativeGandalfIsStrengthPlusTwoUntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allsave = scn.GetFreepsCard("allsave");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		// Four cards in hand, the event among them: three beside it.
		scn.MoveCardsToHand(allsave, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"));
		assertEquals(4, scn.GetFreepsHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		// The phase before the skirmishes: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(allsave));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(allsave));

		scn.FreepsPlayCard(allsave);

		// The event does not count itself toward initiative (CR 4.2 "playing a card"): +2 and no damage bonus.
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertInDiscard(allsave);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 10 against 13: Gandalf loses and takes a wound, and the bonus is gone with his skirmish phase.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithInitiativeGandalfIsStrengthPlusFourAndDamagePlusOneUntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allsave = scn.GetFreepsCard("allsave");
		var gandalf = scn.GetFreepsCard("gandalf");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(gandalf);
		// Five cards in hand, the event among them: four beside it.
		scn.MoveCardsToHand(allsave, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"),
				scn.GetFreepsCard("filler3"), scn.GetFreepsCard("filler4"));
		assertEquals(5, scn.GetFreepsHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, brigand);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		scn.FreepsPlayCard(allsave);

		assertEquals(12, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertInDiscard(allsave);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gandalf wins; both the bonus and the damage bonus are gone with his skirmish phase.
		scn.PassSkirmishActions();
		assertInDiscard(brigand);
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishWithInitiativeItGivesGandalfAllAndNotTheSkirmishingGandalfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allsave = scn.GetFreepsCard("allsave");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(gandalf, avenger);
		// Five cards in hand, the event among them: four beside it, so the damage bonus is given as well.
		scn.MoveCardsToHand(allsave, scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"),
				scn.GetFreepsCard("filler3"), scn.GetFreepsCard("filler4"));
		assertEquals(5, scn.GetFreepsHandCount());

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, brigand);
		scn.FreepsResolveSkirmish(avenger);

		assertTrue(scn.FreepsHasInitiative());
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));

		scn.FreepsPlayCard(allsave);

		// "Make Gandalf": no skirmish is named, and the [gandalf] companion who is fighting is not Gandalf.  No choice
		// is asked, and neither the strength nor the damage bonus goes to the Avenger.
		assertEquals(12, scn.GetStrength(gandalf));
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertInDiscard(allsave);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var allsave = scn.GetFreepsCard("allsave");
		var avenger = scn.GetFreepsCard("avenger");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(allsave);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(avenger, brigand);
		scn.FreepsResolveSkirmish(avenger);

		assertEquals(6, scn.GetStrength(avenger));
		// Its target is no requirement of its play (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(allsave));

		scn.FreepsPlayCard(allsave);

		assertEquals(6, scn.GetStrength(avenger));
		assertInDiscard(allsave);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
