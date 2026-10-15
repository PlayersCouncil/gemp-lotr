package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_203_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blades", "1_203");
					put("blades2", "1_203");
					put("lemenya", "1_232");    // Ulaire Lemenya: a [wraith] Nazgul, strength 9, vitality 2; text on companions bearing [wraith] conditions only
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, vitality 3, fierce; the rest needs 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5, vitality 2; the rest needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: damage +1, strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AllBladesPerishStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: All Blades Perish
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a Nazgûl is about to take a wound, prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blades");

		assertEquals("All Blades Perish", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void OfThreeLosersOfOneSkirmishOnlyTheNazgulAreOfferedAndEachCopyPreventsOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blades = scn.GetShadowCard("blades");
		var blades2 = scn.GetShadowCard("blades2");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(blades, blades2);

		scn.StartGame();
		assertInHand(blades, blades2);
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		// Aragorn is made strong enough to beat all three (9 + 12 + 5 = 26) without overwhelming them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 20));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowAssignToMinions(aragorn, khamul, hound);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(28, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();

		// The three losers are about to be wounded together.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blades));
		scn.ShadowChooseOptionalTrigger(blades);

		// Nazgul of two cultures; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);
		assertInDiscard(blades);

		// The second copy is offered for the same wounds, and takes the other Nazgul's, the one left, unasked.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blades2));
		scn.ShadowChooseOptionalTrigger(blades2);

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(1, scn.GetWoundsOn(hound));
		assertInDiscard(blades, blades2);
		// Khamul is fierce: the fierce assignments follow.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsOfferedForARaiderNazgulWoundedAloneAndPreventsHisWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blades = scn.GetShadowCard("blades");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(blades);

		scn.StartGame();
		assertInHand(blades);
		scn.MoveMinionsToTable(khamul);
		// Aragorn (8) is made strong enough to beat Khamul (12) without overwhelming him.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 8));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, khamul);
		assertEquals(16, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(khamul));
		scn.PassSkirmishActions();

		// A [raider] Nazgul, the only one about to take a wound: no [wraith] Nazgul is there to be the one that
		// sets the response off.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blades));
		scn.ShadowChooseOptionalTrigger(blades);

		assertInDiscard(blades);
		assertEquals(0, scn.GetWoundsOn(khamul));
		// Khamul is fierce: the fierce assignments follow.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OneCopyPreventsOnlyTheFirstOfTwoWounds()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blades = scn.GetShadowCard("blades");
		var lemenya = scn.GetShadowCard("lemenya");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(blades);

		scn.StartGame();
		assertInHand(blades);
		scn.MoveMinionsToTable(lemenya);
		// Gimli (6, damage +1) is made strong enough to beat Lemenya (9) without overwhelming her.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, lemenya);
		assertEquals(10, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.PassSkirmishActions();

		// Two wounds for a loss to damage +1: the first is prevented and the second, with no copy left, is taken.
		// Without the event Lemenya, of vitality 2, would die.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blades));
		scn.ShadowChooseOptionalTrigger(blades);

		assertInDiscard(blades);
		assertInPlay(lemenya);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAWraithMinionWhoIsNoNazgulIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blades = scn.GetShadowCard("blades");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(blades);

		scn.StartGame();
		assertInHand(blades);
		scn.MoveMinionsToTable(hound);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, hound);
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.PassSkirmishActions();

		// Strength 8 beats 5: the [wraith] Orc takes its wound, with nothing asked.
		assertEquals(1, scn.GetWoundsOn(hound));
		assertInHand(blades);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
