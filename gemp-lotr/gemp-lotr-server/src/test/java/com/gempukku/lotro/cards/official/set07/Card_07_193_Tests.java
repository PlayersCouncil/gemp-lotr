package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_193_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lackey", "7_193");
					put("lackey2", "7_193");
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
	public void MorgulLackeyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Lackey
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion to make a Nazgûl or [wraith] Orc strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lackey");

		assertEquals("Morgul Lackey", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsTheLackeyToMakeANazgulOrAWraithOrcStrengthPlusOneForThatSkirmishUntilItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lackey = scn.GetShadowCard("lackey");
		var lackey2 = scn.GetShadowCard("lackey2");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var troop = scn.GetShadowCard("troop");
		var ferny = scn.GetShadowCard("ferny");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(lackey, lackey2, lemenya, khamul, creeper, troop, ferny);

		// "Skirmish:" is not the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lackey));
		scn.ShadowPass();

		// The Lackeys are left out of the skirmishes.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, creeper}, new PhysicalCardImpl[]{aragorn, lemenya});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(lackey));
		assertEquals(0, scn.GetWoundsOn(lackey));
		assertEquals(0, scn.GetWoundsOn(lackey2));
		assertEquals(8, scn.GetStrength(creeper));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(6, scn.GetStrength(lackey));

		scn.ShadowUseCardAction(lackey);

		// A Nazgûl of either culture and a [wraith] Orc, the Lackey itself among them, in the skirmish or out of
		// it.  Not the [moria] Orc, not the [wraith] Man, and no companion.  The other Lackey is not the one exerted.
		assertEquals(1, scn.GetWoundsOn(lackey));
		assertEquals(0, scn.GetWoundsOn(lackey2));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul, creeper, lackey, lackey2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, ferny, gimli, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(creeper);
		assertEquals(9, scn.GetStrength(creeper));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(6, scn.GetStrength(lackey));

		// Vitality 2 with 1 wound: exhausted, so it can no longer exert.  The other copy still can.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lackey));
		assertTrue(scn.ShadowActionAvailable(lackey2));

		scn.ShadowUseCardAction(lackey2);
		scn.ShadowChooseCard(lemenya);
		assertEquals(1, scn.GetWoundsOn(lackey2));
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
