package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("echoes1", "7_34");
					put("echoes2", "7_34");
					// Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability that is never used
					put("grey", "1_364");
					// Gandalf, Friend of the Shirefolk: strength 6, vitality 4, and +1 for each of Hobbit, Dwarf,
					// Elf and Man in the fellowship: 7 beside Frodo
					put("friend", "1_72");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3; "Damage +1." is its whole text

					// Dwarf Guards, which stay in the draw deck to be drawn.
					put("filler1", "1_7");
					put("filler2", "1_7");
					put("filler3", "1_7");
					put("filler4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EchoesofValinorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Echoes of Valinor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time Gandalf wins a skirmish, you may exert him to draw 2 cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("echoes1");

		assertEquals("Echoes of Valinor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echoes = scn.GetFreepsCard("echoes1");
		scn.MoveCardsToHand(echoes);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(echoes));

		scn.FreepsPlayCard(echoes);

		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, echoes);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenGandalfWinsASkirmishHeMayExertToDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echoes = scn.GetFreepsCard("echoes1");
		var gandalf = scn.GetFreepsCard("grey");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(echoes);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		// 7 against 5.
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(echoes));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheExertionAndTheCardsMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echoes = scn.GetFreepsCard("echoes1");
		var gandalf = scn.GetFreepsCard("friend");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(echoes);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		// 6 + 1 for the Hobbit in the fellowship, against 5.
		assertEquals(7, scn.GetStrength(gandalf));
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(echoes));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedWhenAnotherCompanionOfHisCultureWinsOrGandalfLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echoes = scn.GetFreepsCard("echoes1");
		var gandalf = scn.GetFreepsCard("grey");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToSupportArea(echoes);
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		// Aragorn made a [gandalf] companion as well: of Gandalf's culture, and not Gandalf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.GANDALF));
		scn.MoveMinionsToTable(savage, party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gandalf, party});

		// Aragorn's 8 against the Savage's 5: he wins, and nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Gandalf's 7 against the Raiding Party's 9: he loses, takes 2 for damage +1, and nothing is offered.
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinInATurnMakesTheOfferAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echoes = scn.GetFreepsCard("echoes1");
		var gandalf = scn.GetFreepsCard("grey");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(echoes);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// The Savage made fierce, so that Gandalf fights it twice in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(echoes));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(echoes));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceGandalfCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echoes = scn.GetFreepsCard("echoes1");
		var gandalf = scn.GetFreepsCard("grey");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(echoes);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		// Vitality 4 with 2 wounds: one exertion left.
		scn.AddWoundsToChar(gandalf, 2);
		scn.MoveMinionsToTable(savage);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		scn.ShadowPass();

		// He wins and can exert: offered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(echoes));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		scn.ShadowPass();

		// He wins again, exhausted: he cannot exert, and nothing is offered.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachCopyMakesItsOwnOfferForOneWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echoes1 = scn.GetFreepsCard("echoes1");
		var echoes2 = scn.GetFreepsCard("echoes2");
		var gandalf = scn.GetFreepsCard("grey");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(echoes1, echoes2);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(echoes1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(echoes2));
		scn.FreepsChooseOptionalTrigger(echoes2);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetFreepsHandCount());

		// The other copy still has its offer to make for the same win.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(echoes1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(echoes2));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
