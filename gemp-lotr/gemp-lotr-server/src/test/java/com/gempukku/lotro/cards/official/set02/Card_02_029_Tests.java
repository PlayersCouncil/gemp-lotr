package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("staff", "2_29");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, vitality 4
					// Radagast, The Brown (The Hobbit): a [gandalf] Wizard ALLY, the one kind of printed card that tells
					// "a Wizard" from "a Wizard companion"; his fellowship ability is used only on purpose.
					put("radagast", "31_17");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Wizard
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2; +3 and fierce only at 2 burdens
					put("saruman", "4_173");    // Saruman, Black Traitor: [isengard] Wizard minion who may not be assigned to a skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WizardStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Wizard Staff
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Staff
		 * Strength: +1
		 * Game Text: Bearer must be a Wizard.
		 * <b>Skirmish:</b> Exert bearer twice to make a minion strength -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("staff");

		assertEquals("Wizard Staff", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAWizardCompanionOrAllyAndTheAllyWhoBearsItExertsForIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var radagast = scn.GetFreepsCard("radagast");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, avenger, aragorn);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToHand(staff);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(staff));
		scn.FreepsPlayCard(staff);
		// Gandalf and the Wizard ally; not the [gandalf] Ent, the Man or the Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, radagast));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(radagast);
		assertAttachedTo(staff, radagast);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(staff));
		scn.FreepsUseCardAction(staff);

		// The bearer pays, and Gandalf, who does not bear it, does not.
		assertEquals(2, scn.GetWoundsOn(radagast));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(10, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionExertsBearerTwiceToMakeAMinionStrengthMinus3ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(gandalf, staff);

		var troop = scn.GetShadowCard("troop");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, axeman, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, axeman}, new PhysicalCardImpl[]{gandalf, troop});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, axeman));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.FreepsActionAvailable(staff));
		scn.FreepsUseCardAction(staff);
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// The minion in this skirmish, one assigned to a later skirmish, and Saruman, who is assigned to none; no
		// companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axeman, troop, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(10, scn.GetStrength(troop));
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of this skirmish.
		scn.ShadowPass();
		assertEquals(10, scn.GetStrength(troop));

		scn.FreepsPass();
		// Aragorn (8) beats the Axeman (6), who lives through one wound.
		assertEquals(1, scn.GetWoundsOn(axeman));
		scn.FreepsResolveSkirmish(gandalf);
		// Each skirmish is a phase of its own: in the next, the Troop is back to 13.
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhenBearerCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var staff = scn.GetFreepsCard("staff");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 4: with 2 wounds he has 2 left and can exert only once.
		scn.AddWoundsToChar(gandalf, 2);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, troop));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(staff));
	}
}
