package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_196_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("predator", "7_196");
					put("predator2", "7_196");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, strength 9; her text is about companions
					// bearing [wraith] conditions
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider], strength 12; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc, strength 8, vitality 2; its text needs 3 burdens
					put("creeper", "8_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text
					// Bill Ferny, Swarthy Sneering Fellow: a [wraith] Man.  "Nazgûl are not roaming. The Free Peoples
					// player may not assign a character to skirmish Bill Ferny. Discard Bill Ferny if underground."
					put("ferny", "2_75");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					// Gimli, Son of Glóin: strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2"
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulPredatorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Predator
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion to make a Nazgûl or [wraith] Orc strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("predator");

		assertEquals("Morgul Predator", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsThePredatorToMakeANazgulOrAWraithOrcStrengthPlusOneForThatSkirmishUntilItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var predator = scn.GetShadowCard("predator");
		var predator2 = scn.GetShadowCard("predator2");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var troop = scn.GetShadowCard("troop");
		var ferny = scn.GetShadowCard("ferny");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(predator, predator2, lemenya, khamul, creeper, troop, ferny);

		// "Skirmish:" is not the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(predator));
		scn.ShadowPass();

		// Both Predators are left out of the skirmishes, with Khamûl, the Troop and Bill Ferny.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, creeper}, new PhysicalCardImpl[]{aragorn, lemenya});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(predator));
		assertEquals(0, scn.GetWoundsOn(predator));
		assertEquals(0, scn.GetWoundsOn(predator2));
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(11, scn.GetStrength(predator));

		scn.ShadowUseCardAction(predator);

		// A Nazgûl of either culture and a [wraith] Orc, the Predator itself among them, in the skirmish or out of
		// it.  Not the [moria] Orc, not the [wraith] Man, and no companion.  The cost is this Predator's own
		// exertion: the other copy is not asked for and not exerted.
		assertEquals(1, scn.GetWoundsOn(predator));
		assertEquals(0, scn.GetWoundsOn(predator2));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul, creeper, predator));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, ferny, gimli, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(creeper);
		assertEquals(9, scn.GetStrength(creeper));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(11, scn.GetStrength(predator));

		scn.FreepsPass();
		scn.ShadowUseCardAction(predator);
		scn.ShadowChooseCard(khamul);
		assertEquals(2, scn.GetWoundsOn(predator));
		assertEquals(0, scn.GetWoundsOn(predator2));
		assertEquals(13, scn.GetStrength(khamul));

		// Vitality 3 with 2 wounds: exhausted, so it can no longer exert.  The other copy pays for its own use.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(predator));
		assertTrue(scn.ShadowActionAvailable(predator2));
		scn.ShadowUseCardAction(predator2);
		scn.ShadowChooseCard(lemenya);
		assertEquals(1, scn.GetWoundsOn(predator2));
		assertEquals(2, scn.GetWoundsOn(predator));
		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(9, scn.GetStrength(creeper));

		// Gimli, 6, loses to the Creeper, 8 + 1.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The next skirmish: each of them is as it was.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
