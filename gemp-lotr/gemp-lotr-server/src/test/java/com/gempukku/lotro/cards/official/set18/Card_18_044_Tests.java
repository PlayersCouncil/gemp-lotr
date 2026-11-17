package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defenses", "18_44");
					put("defenses2", "18_44");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, strength 8; a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger, strength 6; +3 only while skirmishing a Nazgul
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion who is no ranger, strength 6; Knight is his whole text

					put("path", "8_42");        // A Path Appointed: [gondor] support-area condition that takes [gondor] tokens; abilities used only on purpose
					put("path2", "8_42");       // a second copy, with no token on it
					put("houses", "11_61");     // Houses of Healing: [gondor] support-area condition that takes [gondor] tokens; its fellowship ability used only on purpose
					put("stout", "4_57");       // Stout and Strong: [dwarven] support-area condition that takes [dwarven] tokens; its skirmish ability used only on purpose

					put("nelya", "2_84");       // Ulaire Nelya: [wraith] Nazgul, strength 10; fierce and damage +1 only at 2 burdens or 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DefensesLongHeldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Defenses Long Held
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a ranger strength +2 and you may reinforce a [gondor] token.
		*/

		var scn = GetScenario();

		var defenses = scn.GetFreepsCard("defenses");

		assertEquals("Defenses Long Held", defenses.getBlueprint().getTitle());
		assertNull(defenses.getBlueprint().getSubtitle());
		assertFalse(defenses.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, defenses.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, defenses.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, defenses.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(defenses, Timeword.SKIRMISH));
		assertEquals(1, defenses.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAChosenRangerStrengthPlus2ForThatSkirmishAndMayReinforceAGondorToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses = scn.GetFreepsCard("defenses");
		var defenses2 = scn.GetFreepsCard("defenses2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var path = scn.GetFreepsCard("path");
		var path2 = scn.GetFreepsCard("path2");
		var houses = scn.GetFreepsCard("houses");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn, arwen, knight);
		scn.MoveCardsToSupportArea(path, path2, houses, stout);
		scn.MoveCardsToHand(defenses, defenses2);
		assertInHand(defenses, defenses2);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		scn.AddTokensToCard(houses, 2);
		scn.AddTokensToCard(stout, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(path2));
		assertEquals(2, scn.GetCultureTokensOn(houses));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase before the skirmishes: not playable.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(defenses));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.IsCharAssigned(arwen));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.FreepsPlayAvailable(defenses));

		scn.FreepsPlayCard(defenses);
		// The rangers, a [gondor] Man in the skirmish and an [elven] Elf in none; not the Knight, who is no ranger,
		// nor Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo, nelya));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(knight));

		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		// The cards with a [gondor] token; not the copy with none, nor the [dwarven] condition with its own token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(path, houses));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(path2, stout));
		scn.FreepsChooseCard(path);
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(path2));
		assertEquals(2, scn.GetCultureTokensOn(houses));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInDiscard(defenses);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.ShadowPass();
		scn.FreepsPlayCard(defenses2);
		scn.FreepsChooseCard(arwen);
		// Arwen, skirmishing nothing: +2.  The reinforce is declined and no token moves.
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseNo();
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertEquals(0, scn.GetCultureTokensOn(path2));
		assertEquals(2, scn.GetCultureTokensOn(houses));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInDiscard(defenses2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Once the skirmish is over the bonuses are gone (CR 4.2: an event's effect lasts the phase, each skirmish
		// a phase of its own).
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
	}

	@Test
	public void WithNoRangerTheEventIsPlayedForNothingButTheTokenMayStillBeReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses = scn.GetFreepsCard("defenses");
		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToSupportArea(path);
		scn.MoveCardsToHand(defenses);
		assertInHand(defenses);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.AddTokensToCard(path, 1);
		assertEquals(1, scn.GetCultureTokensOn(path));
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, nelya);
		scn.FreepsResolveSkirmish(knight);

		// The Ring-bearer and a Man who is no ranger are the only companions: nobody for the strength.
		var frodoStrength = scn.GetStrength(frodo);
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(defenses));

		scn.FreepsPlayCard(defenses);
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(knight));
		// The reinforce does not hang on the ranger: asked, and with one card to take it the token goes there.
		assertTrue(scn.FreepsDecisionAvailable("reinforce"));
		scn.FreepsChooseYes();
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertInDiscard(defenses);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorTokenInPlayTheReinforceIsNotAskedAndTheRangerIsStillStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var defenses = scn.GetFreepsCard("defenses");
		var aragorn = scn.GetFreepsCard("aragorn");
		var path = scn.GetFreepsCard("path");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(path, stout);
		scn.MoveCardsToHand(defenses);
		assertInHand(defenses);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		// A [gondor] card with no token, and a token of another culture: nothing a [gondor] reinforce can take.
		scn.AddTokensToCard(stout, 1);
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		scn.FreepsPlayCard(defenses);
		// The lone ranger is chosen unasked.
		assertEquals(10, scn.GetStrength(aragorn));
		assertFalse(scn.FreepsDecisionAvailable("reinforce"));
		assertEquals(0, scn.GetCultureTokensOn(path));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInDiscard(defenses);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
