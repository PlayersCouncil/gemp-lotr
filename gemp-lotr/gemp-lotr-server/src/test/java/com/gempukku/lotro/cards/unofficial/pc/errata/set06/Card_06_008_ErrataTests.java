package com.gempukku.lotro.cards.unofficial.pc.errata.set06;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_008_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("peasants", "56_8");
					put("brigand", "4_10");    // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("footman", "11_82");   // Footman of Dunland: a [men] Man, strength 7, vitality 3; the current site gains plains
					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("ned", "1_220");       // Not Easily Destroyed: [wraith] condition, support area
					put("wielding", "11_105"); // Wielding the Ring: [men] condition borne by a companion, no other text

					put("knight", "5_35");     // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3, Knight
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4
					put("saga", "1_114");      // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; bearer vitality +1
					put("guard", "1_7");       // Dwarf Guard: strength 4, vitality 2
					put("armor", "1_8");       // Dwarven Armor: a Free Peoples possession, borne by a Dwarf
					put("stairs", "1_24");     // Stairs of Khazad-dum: [dwarven] condition, support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TooLongHaveThesePeasantsStoodStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Too Long Have These Peasants Stood
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a [dunland] Man wins a skirmish, hinder all conditions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("peasants");

		assertEquals("Too Long Have These Peasants Stood", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenADunlandManWinsASkirmishHindersEveryConditionOfBothSidesAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(knight, aragorn, guard);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(guard, armor);
		scn.MoveCardsToSupportArea(stairs);

		var peasants = scn.GetShadowCard("peasants");
		var brigand = scn.GetShadowCard("brigand");
		var ned = scn.GetShadowCard("ned");
		var wielding = scn.GetShadowCard("wielding");
		scn.MoveCardsToHand(peasants);
		scn.MoveCardsToSupportArea(ned);
		scn.AttachCardsTo(knight, wielding);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SetTwilight(5);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(knight, brigand);
		scn.PassSkirmishActions();

		// The Brigand 8 against the Knight 6.
		assertEquals(1, scn.GetWoundsOn(knight));
		int twilight = scn.GetTwilight();
		assertFalse(scn.IsHindered(stairs));
		assertFalse(scn.IsHindered(saga));
		assertFalse(scn.IsHindered(ned));
		assertFalse(scn.IsHindered(wielding));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(peasants));
		scn.ShadowChooseOptionalTrigger(peasants);

		assertEquals(twilight - 2, scn.GetTwilight());
		assertInDiscard(peasants);
		assertTrue(scn.IsHindered(stairs));
		assertTrue(scn.IsHindered(saga));
		assertTrue(scn.IsHindered(ned));
		assertTrue(scn.IsHindered(wielding));
		assertFalse(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(scn.GetRing()));
		assertFalse(scn.IsHindered(knight));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(guard));
		assertFalse(scn.IsHindered(brigand));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADunlandManLosingOrAWinnerThatIsNotADunlandManDoesNotOfferIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(knight, aragorn, guard);
		scn.MoveCardsToSupportArea(stairs);

		var peasants = scn.GetShadowCard("peasants");
		var brigand = scn.GetShadowCard("brigand");
		var footman = scn.GetShadowCard("footman");
		var savage = scn.GetShadowCard("savage");
		var ned = scn.GetShadowCard("ned");
		scn.MoveCardsToHand(peasants);
		scn.MoveCardsToSupportArea(ned);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, footman, savage);
		scn.SetTwilight(5);
		// An Uruk-hai given [dunland]: of the culture but no Man.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, savage, null, Culture.DUNLAND));
		// Aragorn 11 beats the Brigand 8.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, footman}, new PhysicalCardImpl[]{guard, savage},
				new PhysicalCardImpl[]{aragorn, brigand});
		int twilight = scn.GetTwilight();
		assertTrue(twilight >= 2); // enough to pay for the event throughout

		// The [men] Footman 7 beats the Knight 6.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(peasants));

		// The [dunland] Uruk Savage 5 beats the Dwarf Guard 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard); // 2 wounds with the Savage's damage +1
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(peasants));

		// The [dunland] Brigand loses to Aragorn.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(brigand);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(peasants));

		assertInHand(peasants);
		assertEquals(twilight, scn.GetTwilight());
		assertFalse(scn.IsHindered(stairs));
		assertFalse(scn.IsHindered(ned));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
