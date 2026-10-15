package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breath", "1_25");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home site 3, strength 4, vitality 2; a maneuver ability used only on purpose
					put("ring", "9_8");         // Ring of Guile: on a Dwarf, strength +1 and damage +1; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4

					put("runner", "1_178");     // Goblin Runner: Orc, strength 5, vitality 1
					put("scrabbler", "2_64");   // Goblin Scrabbler: Orc, strength 4, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StillDrawsBreathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Still Draws Breath
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a Dwarf wins a skirmish, heal that Dwarf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("breath");

		assertEquals("Still Draws Breath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AWoundedDwarfCompanionWhoWinsASkirmishIsHealedAndNoOtherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(breath);
		scn.AddWoundsToChar(gimli, 2);
		// Another wounded Dwarf, who wins nothing.
		scn.AddWoundsToChar(grimir, 1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		assertFalse(scn.FreepsPlayAvailable(breath));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gimli, 6, beats the Runner, 5.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertTrue(scn.FreepsPlayAvailable(breath));

		scn.FreepsPlayCard(breath);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertInDiscard(breath);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyWhoWinsIsHealedAndACompanionWhoIsNoDwarfIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var thrarin = scn.GetFreepsCard("thrarin");
		var ring = scn.GetFreepsCard("ring");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, ring);
		// Kept out of the deck the turns draw from, and taken into hand once they are played.
		scn.MoveCardsToDiscard(breath);

		var runner = scn.GetShadowCard("runner");
		var scrabbler = scn.GetShadowCard("scrabbler");

		scn.StartGame();
		// Thrarin's home is site 3: the fellowship moves there from site 2.
		scn.SkipToSite(2);
		scn.MoveCardsToHand(breath);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.MoveMinionsToTable(runner, scrabbler);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{thrarin, scrabbler});

		// Aragorn, 8, a wounded companion who is no Dwarf, beats the Runner: nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsPlayAvailable(breath));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Thrarin, 4 + 1 with the Ring, beats the Scrabbler, 4.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInDiscard(scrabbler);
		assertTrue(scn.FreepsPlayAvailable(breath));

		scn.FreepsPlayCard(breath);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(breath);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfWhoLosesASkirmishIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(breath);
		scn.AddWoundsToChar(gimli, 1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInHand(breath);
		scn.MoveMinionsToTable(runner);
		// The Runner, 5 + 2, beats Gimli, 6, who lives through the wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 2));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsPlayAvailable(breath));
		assertInHand(breath);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUnwoundedDwarfWhoWinsMayStillBeAnsweredAndNothingIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(breath);
		// The Dwarf who does not fight is the wounded one.
		scn.AddWoundsToChar(grimir, 1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		// An event is played whatever its effect will do.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertTrue(scn.FreepsPlayAvailable(breath));

		scn.FreepsPlayCard(breath);
		assertInDiscard(breath);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
