package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_205_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vigilant", "11_205");
					put("vigilant2", "11_205");
					// Murderous Uruk: an [uruk-hai] Uruk-hai minion; damage +1 and muster (a regroup trigger) only.
					put("murderous", "11_195");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard and no Uruk-hai; each
					// companion is resistance -1, and a skirmish ability used only on purpose.
					put("saruman", "17_116");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 and no other text: an Uruk-hai of another culture.
					put("guard", "10_81");
					// Dark Alliance: an [uruk-hai] condition in the support area (no minion); a response used only on purpose.
					put("alliance", "12_141");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VigilantUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Vigilant Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>. <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br>This minion is strength +2 for each [uruk-hai] minion not assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vigilant");

		assertEquals("Vigilant Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CountsEachUrukHaiMinionNotAssignedAndNoOtherCardAsTheAssignmentsAreMadeAndTheSkirmishesEnd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var vigilant = scn.GetShadowCard("vigilant");
		var murderous = scn.GetShadowCard("murderous");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		var alliance = scn.GetShadowCard("alliance");

		scn.StartGame();
		scn.MoveMinionsToTable(vigilant, murderous, saruman, guard);
		scn.MoveCardsToSupportArea(alliance);

		// 6 + 2 x 3: itself, the Murderous Uruk and Saruman; not the [sauron] Guard, nor Dark Alliance, an
		// [uruk-hai] card that is no minion, though neither is assigned.
		assertEquals(12, scn.GetStrength(vigilant));
		// The bonus is this minion's alone.
		assertEquals(8, scn.GetStrength(murderous));
		assertEquals(8, scn.GetStrength(saruman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, vigilant }, new PhysicalCardImpl[] { boromir, murderous });
		scn.ShadowDeclineAssignments();

		assertTrue(scn.IsCharAssignedAgainst(aragorn, vigilant));
		assertTrue(scn.IsCharAssignedAgainst(boromir, murderous));
		assertFalse(scn.IsCharAssigned(saruman));
		// 6 + 2: Saruman alone of the [uruk-hai] minions is not assigned.
		assertEquals(8, scn.GetStrength(vigilant));

		// The lurker's skirmish comes last: Boromir's first.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, murderous));
		assertEquals(8, scn.GetStrength(vigilant));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.PassSkirmishActions();
		// Boromir, 7 against 8, loses and lives with 2 wounds; that skirmish is over and the Murderous Uruk is no
		// longer assigned (CR 4.2 "skirmish phase(s)").
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, vigilant));
		// 6 + 2 x 2: Saruman and the Murderous Uruk; itself, in its own skirmish, still assigned.
		assertEquals(10, scn.GetStrength(vigilant));
		assertEquals(8, scn.GetStrength(murderous));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachCopyIsStrengthenedForItselfAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vigilant = scn.GetShadowCard("vigilant");
		var vigilant2 = scn.GetShadowCard("vigilant2");

		scn.StartGame();
		scn.MoveMinionsToTable(vigilant, vigilant2);

		// 6 + 2 x 2 (both copies) each: a second copy's bonus adds nothing to the first.
		assertEquals(10, scn.GetStrength(vigilant));
		assertEquals(10, scn.GetStrength(vigilant2));
	}
}
