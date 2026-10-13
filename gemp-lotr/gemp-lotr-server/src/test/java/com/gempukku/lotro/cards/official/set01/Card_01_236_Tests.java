package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_236_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "1_236");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound [shire] Hobbit companion who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally with home site 2, so at home and assignable in the assignment phase of turn 1; skirmish ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text, a second minion beside her
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Úlairë Toldëa, Messenger of Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Assignment:</b> Spot 4 burdens to assign a companion (except the Ring-bearer) to skirmish Úlairë Toldëa.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", card.getBlueprint().getTitle());
		assertEquals("Messenger of Morgul", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtFourBurdensAssignsAChosenCompanionOtherThanTheRingBearerToSkirmishHer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(bounder);

		var toldea = scn.GetShadowCard("toldea");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(toldea, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(toldea));
		scn.ShadowUseCardAction(toldea);
		// Bounder is at home on site 2 and could be assigned, but he is an ally and no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		// She is assigned to him, and the other minion to nobody.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, toldea));
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(bounder));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Once she is assigned, the ability is not offered again, though Sam is still unassigned.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(toldea));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedAtThreeBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.AddBurdens(3);
		// A wound on the Ring-bearer beside the burdens: a token on him that is no burden.
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(toldea);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(3, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(toldea));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheRingBearerIsTheOnlyCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");

		scn.StartGame();
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(toldea);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(4, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(toldea));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
