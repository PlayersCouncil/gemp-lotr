package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_09_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uri", "9_11");
					put("guile", "9_8");       // Ring of Guile: a Dwarf's ring, strength +1, "Bearer is damage +1"; its Maneuver ability is used only on purpose
					put("battleaxe", "7_9");   // Gimli's Battle Axe, Trusted Weapon: a Dwarf's hand weapon, strength +2; damage +1 while a threat can be spotted, strength +1 more at 2 threats
					put("gimli", "1_13");      // Gimli, Son of Gloin: a Dwarf companion printed Damage +1, strength 6; his own skirmish ability is used only on purpose
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only

					put("savage1", "1_151");   // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UriStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Úri, Dwarven Lord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 5
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> If Úri is damage +X, exert him to make him strength +X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uri");

		assertEquals("Úri", card.getBlueprint().getTitle());
		assertEquals("Dwarven Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityExertsUriToMakeHimStrengthPlusHisDamageBonusAndNoOtherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uri = scn.GetFreepsCard("uri");
		var guile = scn.GetFreepsCard("guile");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(uri, gimli);
		scn.AttachCardsTo(uri, guile);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Damage +1 from the Ring, but the assignment phase is not a skirmish.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(uri));

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(uri, savage);

		assertEquals(0, scn.GetWoundsOn(uri));
		// 5 + 1 for the Ring of Guile
		assertEquals(6, scn.GetStrength(uri));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(uri));
		scn.FreepsUseCardAction(uri);

		// Damage +1, so +1: 7.  Gimli, the other Dwarf with a damage bonus, is left as he was.
		assertEquals(1, scn.GetWoundsOn(uri));
		assertEquals(7, scn.GetStrength(uri));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtDamagePlus2HeIsMadeStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uri = scn.GetFreepsCard("uri");
		var guile = scn.GetFreepsCard("guile");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		scn.MoveCompanionsToTable(uri);
		scn.AttachCardsTo(uri, guile, battleaxe);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.AddThreats(1);
		scn.MoveMinionsToTable(savage);

		assertEquals(1, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(uri, savage);

		assertEquals(0, scn.GetWoundsOn(uri));
		// 5 + 1 for the Ring + 2 for the Battle Axe; one threat makes him damage +1 more, and no stronger
		assertEquals(8, scn.GetStrength(uri));
		scn.FreepsUseCardAction(uri);

		// Damage +2 (the Ring, and the Battle Axe while a threat is spotted): +2.
		assertEquals(1, scn.GetWoundsOn(uri));
		assertEquals(10, scn.GetStrength(uri));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoDamageBonusOfHisOwnTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uri = scn.GetFreepsCard("uri");
		var battleaxe = scn.GetFreepsCard("battleaxe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(uri, gimli);
		scn.AttachCardsTo(uri, battleaxe);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		assertEquals(0, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(uri, savage);

		// With no threat the Battle Axe gives him no damage bonus; Gimli, another Dwarf beside him, has his own.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(uri));
		assertEquals(0, scn.GetWoundsOn(uri));

		// A threat gives him damage +1, and the ability is offered.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(uri));
	}

	@Test
	public void ExhaustedHeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uri = scn.GetFreepsCard("uri");
		var guile = scn.GetFreepsCard("guile");
		scn.MoveCompanionsToTable(uri);
		scn.AttachCardsTo(uri, guile);
		scn.AddWoundsToChar(uri, 3);

		var savage = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);

		// Vitality 4 with 3 wounds: exhausted.
		assertEquals(3, scn.GetWoundsOn(uri));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(uri, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(uri));
		assertEquals(3, scn.GetWoundsOn(uri));
	}

	@Test
	public void UsedUnassignedInAnotherSkirmishTheBonusLastsThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uri = scn.GetFreepsCard("uri");
		var guile = scn.GetFreepsCard("guile");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(uri, gimli, aragorn);
		scn.AttachCardsTo(uri, guile);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(aragorn);

		// Uri is in no skirmish; his text names none.
		assertFalse(scn.IsCharAssigned(uri));
		assertEquals(6, scn.GetStrength(uri));
		assertTrue(scn.FreepsActionAvailable(uri));
		scn.FreepsUseCardAction(uri);
		assertEquals(1, scn.GetWoundsOn(uri));
		assertEquals(7, scn.GetStrength(uri));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);

		// Each skirmish is a phase of its own: in the next one he is back at 6.
		assertEquals(6, scn.GetStrength(uri));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
