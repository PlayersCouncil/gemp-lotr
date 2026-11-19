package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_224_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elfhelm", "7_224");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, Valiant and no other text

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text answers its own play only
				}},
				// Of the sites laid by site 4 only Ettenmoors, site 2, is a plains.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Towers path whose first three sites are plains (the third a sanctuary as well) and whose site 4 is not.
	protected VirtualTableScenario GetPlainsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elfhelm", "7_224");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, Valiant and no other text

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text answers its own play only
				}},
				new HashMap<>()
				{{
					put("site1", "4_328");      // The Riddermark: plains; a fellowship ability that plays Aragorn
					put("site2", "4_331");      // Eastfold: plains; a Shadow ability that needs 3 [isengard] trackers
					put("site3", "4_342");      // Westemnet Plains: plains and a sanctuary; a bar on [dunland] Men only
					put("site4", "4_346");      // White Rocks: a battleground, no plains; its Shadow number counts mounted companions
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElfhelmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Elfhelm, Marshal of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>.<br><b>Skirmish:</b> Exert Elfhelm to make him strength +1 for each uncontrolled plains site you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elfhelm");

		assertEquals("Elfhelm", card.getBlueprint().getTitle());
		assertEquals("Marshal of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityExertsElfhelmToMakeHimStrengthPlus1ForEachUncontrolledPlainsSiteUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainsScenario();

		var elfhelm = scn.GetFreepsCard("elfhelm");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(elfhelm, guard);

		scn.StartGame();
		scn.SkipToSite(3);
		// The Riddermark, a plains, goes to the Shadow player; Eastfold and Westemnet Plains stay uncontrolled.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));
		assertFalse(scn.ShadowControls(scn.GetSite(3)));
		assertTrue(scn.HasKeyword(scn.GetSite(1), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.SANCTUARY));

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(runner, savage);

		scn.SkipToAssignments();
		// The fellowship is at White Rocks, which is no plains.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elfhelm, runner},
				new PhysicalCardImpl[]{guard, savage});

		scn.FreepsResolveSkirmish(elfhelm);
		assertEquals(0, scn.GetWoundsOn(elfhelm));
		assertEquals(6, scn.GetStrength(elfhelm));
		assertTrue(scn.FreepsActionAvailable(elfhelm));

		scn.FreepsUseCardAction(elfhelm);
		// 6 + 2: Eastfold and Westemnet Plains.
		assertEquals(1, scn.GetWoundsOn(elfhelm));
		assertEquals(8, scn.GetStrength(elfhelm));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 8 against the Runner's 5: the Runner dies of its one wound.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);

		scn.FreepsResolveSkirmish(guard);
		// Each skirmish is a phase of its own: the bonus is gone in the next.
		assertTrue(scn.IsCharSkirmishingAgainst(guard, savage));
		assertEquals(6, scn.GetStrength(elfhelm));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsOfferedInAnotherCompanionsSkirmishAndCountsTheOnePlainsSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elfhelm = scn.GetFreepsCard("elfhelm");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(elfhelm, guard);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));
		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.PLAINS));
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		// Elfhelm is in no skirmish.
		assertFalse(scn.IsCharSkirmishing(elfhelm));
		assertEquals(0, scn.GetWoundsOn(elfhelm));
		assertEquals(6, scn.GetStrength(elfhelm));
		assertTrue(scn.FreepsActionAvailable(elfhelm));

		scn.FreepsUseCardAction(elfhelm);
		// 6 + 1: Ettenmoors, the fellowship's site.  Bag End is no plains.
		assertEquals(1, scn.GetWoundsOn(elfhelm));
		assertEquals(7, scn.GetStrength(elfhelm));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithEveryPlainsSiteControlledTheExertionBuysNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elfhelm = scn.GetFreepsCard("elfhelm");
		scn.MoveCompanionsToTable(elfhelm);

		scn.StartGame();
		scn.SkipToSite(3);
		// Bag End and Ettenmoors, the only plains of the path, go to the Shadow player.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.PLAINS));

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(elfhelm, savage);
		scn.FreepsResolveSkirmish(elfhelm);

		assertEquals(0, scn.GetWoundsOn(elfhelm));
		assertEquals(6, scn.GetStrength(elfhelm));
		assertTrue(scn.FreepsActionAvailable(elfhelm));

		scn.FreepsUseCardAction(elfhelm);
		assertEquals(1, scn.GetWoundsOn(elfhelm));
		assertEquals(6, scn.GetStrength(elfhelm));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileElfhelmIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elfhelm = scn.GetFreepsCard("elfhelm");
		scn.MoveCompanionsToTable(elfhelm);
		scn.AddWoundsToChar(elfhelm, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(elfhelm));

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elfhelm, savage);
		scn.FreepsResolveSkirmish(elfhelm);

		// Vitality 3 with 2 wounds: he cannot exert.
		assertTrue(scn.IsCharSkirmishingAgainst(elfhelm, savage));
		assertEquals(2, scn.GetWoundsOn(elfhelm));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(elfhelm));
	}
}
