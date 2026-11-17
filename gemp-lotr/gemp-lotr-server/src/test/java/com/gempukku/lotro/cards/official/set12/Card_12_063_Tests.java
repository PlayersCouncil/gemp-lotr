package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "12_63");
					put("banner2", "12_63");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, vitality 4, resistance 6; his maneuver ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6, damage +1; his skirmish ability is used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound [shire] Hobbit, resistance 5; his abilities are used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; Maneuver: exert him to take part in skirmishes until the regroup phase
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, strength 13, vitality 3
					// Uruk Pursuer: strength 5, vitality 3; its texts are about unbound Hobbits being discarded or
					// killed, and none is
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingBannerbearerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Easterling Banner-bearer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each wound on each companion he is skirmishing (or +2 for each if that companion has resistance 2 or less).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Easterling Banner-bearer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlusOneForEachWoundOnTheCompanionHeIsSkirmishingAndPlusTwoOnceThatCompanionIsAtResistance2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var troop = scn.GetShadowCard("troop");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(banner, troop, pursuer);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(banner, 1);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(banner));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(9, scn.GetStrength(banner));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, banner}, new PhysicalCardImpl[]{gimli, pursuer});
		// The Troop is left over, and the Shadow player puts it on Aragorn beside the Banner-bearer.
		scn.ShadowAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(gimli);

		// Assigned to Aragorn, but his skirmish has not begun: nothing for Aragorn's wounds, nor for Gimli's.
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer, gimli));
		assertFalse(scn.IsCharSkirmishing(banner));
		assertEquals(9, scn.GetStrength(banner));

		// Gimli wins, 6 to 5, and the Pursuer lives through its two wounds.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);

		// 9 + 1 for each of Aragorn's two wounds; nothing for the wounds on the Troop beside him, on himself, or
		// on Gimli in no skirmish.
		assertTrue(scn.IsCharSkirmishingAgainst(banner, aragorn));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(11, scn.GetStrength(banner));

		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(12, scn.GetStrength(banner));

		// Three burdens: Aragorn at resistance 3, still +1 each.
		scn.AddBurdens(3);
		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(12, scn.GetStrength(banner));

		// A fourth: resistance 2, +2 each. 9 + 2 * 3
		scn.AddBurdens(1);
		assertEquals(2, scn.GetResistance(aragorn));
		assertEquals(15, scn.GetStrength(banner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundsOnAnAllyHeIsSkirmishingAreNotCountedAndOnAHobbitCompanionTheyAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var banner2 = scn.GetShadowCard("banner2");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(banner, banner2);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(2, scn.GetWoundsOn(sam));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thrarin, banner}, new PhysicalCardImpl[]{sam, banner2});
		scn.FreepsResolveSkirmish(thrarin);

		// Thrarin is an ally, no companion: his wound counts for nothing (and his resistance of 0 for nothing).
		assertTrue(scn.IsCharSkirmishingAgainst(banner, thrarin));
		assertEquals(9, scn.GetStrength(banner));
		assertEquals(9, scn.GetStrength(banner2));

		// Thrarin is overwhelmed, 4 to 9.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sam);

		// 9 + 1 for each of Sam's two wounds
		assertTrue(scn.IsCharSkirmishingAgainst(banner2, sam));
		assertEquals(5, scn.GetResistance(sam));
		assertEquals(11, scn.GetStrength(banner2));

		// Three burdens: Sam at resistance 2. 9 + 2 * 2
		scn.AddBurdens(3);
		assertEquals(2, scn.GetResistance(sam));
		assertEquals(13, scn.GetStrength(banner2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
