package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_227_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("threshold", "1_227");

					put("otsea", "1_235");      // Úlairë Otsëa: [wraith] Nazgûl, strength 9, vitality 3; a Shadow ability used only on purpose
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, fierce; his texts need 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, strength 5; stronger only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThresholdofShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Threshold of Shadow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Nazgûl strength +2 (or +4 if you spot 5 burdens).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("threshold");

		assertEquals("Threshold of Shadow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTheChosenNazgulStrengthPlus2UntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threshold = scn.GetShadowCard("threshold");
		var otsea = scn.GetShadowCard("otsea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(threshold);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, khamul, hound);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, otsea}, new PhysicalCardImpl[]{frodo, hound});
		// Khamûl is left over and stays unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(9, scn.GetStrength(otsea));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(threshold));

		scn.ShadowPlayCard(threshold);
		// A Nazgûl in the skirmish, and one of another culture in none, may be chosen; the [wraith] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(khamul);
		assertEquals(14, scn.GetStrength(khamul));
		assertEquals(9, scn.GetStrength(otsea));
		assertEquals(5, scn.GetStrength(hound));
		assertInDiscard(threshold);
		// Still in force at the next decision of the same skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(14, scn.GetStrength(khamul));

		scn.PassSkirmishActions();
		// Otsëa (9) beat Aragorn (8); the next skirmish is a phase of its own, and the bonus is gone.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(12, scn.GetStrength(khamul));
	}

	@Test
	public void IsNotPlayableInTheShadowPlayersAssignmentActions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threshold = scn.GetShadowCard("threshold");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCardsToHand(threshold);
		assertInHand(threshold);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(threshold));
		assertEquals(9, scn.GetStrength(otsea));
		assertInHand(threshold);
	}

	@Test
	public void AtFiveBurdensTheNazgulIsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threshold = scn.GetShadowCard("threshold");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCardsToHand(threshold);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.MoveMinionsToTable(otsea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(otsea));

		// Otsëa is the one Nazgûl: he is chosen unasked.
		scn.ShadowPlayCard(threshold);
		assertEquals(13, scn.GetStrength(otsea));
		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtFourBurdensTheNazgulIsOnlyStrengthPlus2ThoughTheShadowPlayersOwnRingBearerHasMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var threshold = scn.GetShadowCard("threshold");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCardsToHand(threshold);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// The Shadow player's own Ring-bearer is given 2 burdens on his turn: counted with the Free Peoples
		// player's 4, they would make the 5.
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(2);
		scn.SkipToOtherPlayersTurn();
		scn.AddBurdens(4);
		assertEquals(4, scn.FreepsGetBurdens());
		assertEquals(2, scn.ShadowGetBurdens());
		scn.MoveMinionsToTable(otsea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(otsea));

		scn.ShadowPlayCard(threshold);
		assertEquals(11, scn.GetStrength(otsea));
		assertEquals(4, scn.FreepsGetBurdens());
		assertEquals(2, scn.ShadowGetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
