package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_18_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("harp", "18_3");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] companion
					put("farin", "1_11");       // Farin, Dwarven Emissary: a [dwarven] companion of another title
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a [dwarven] ally, no companion
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: a companion of another culture
					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1, cheated in so its play trigger does nothing
					put("runner2", "1_178");
				}},
				// No site of this path is a mountain; site 2, where the maneuver phase of turn 1 is, is Ettenmoors (plains).
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThorinsHarpStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Thorin's Harp
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Tale</b>.<br><b>Maneuver:</b> Exert a [dwarven] companion to make the fellowship's current site gain <b>mountain</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("harp");

		assertEquals("Thorin's Harp", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ManeuverAbilityExertsADwarvenCompanionToMakeTheCurrentSiteAMountainUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harp = scn.GetFreepsCard("harp");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var haldir = scn.GetFreepsCard("haldir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, farin, haldir);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(harp);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);

		var site1 = scn.GetSite(1);
		var site2 = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(site1, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site2, Keyword.MOUNTAIN));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.FreepsActionAvailable(harp));

		scn.FreepsUseCardAction(harp);
		// Either Dwarf companion may pay; the Dwarf ally, the Elf and the Ring-bearer may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, haldir, frodo));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(haldir));
		// Only the site the fellowship is at gains it, not the site it moved from (site 3 is not yet laid).
		assertTrue(scn.HasKeyword(site2, Keyword.MOUNTAIN));
		assertFalse(scn.HasKeyword(site1, Keyword.MOUNTAIN));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Used again, the site is a mountain already: the Harp is still offered, and the other Dwarf pays.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(harp));
		scn.FreepsUseCardAction(harp);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(farin);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(farin));
		assertTrue(scn.HasKeyword(site2, Keyword.MOUNTAIN));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Still a mountain in the second skirmish, after the first has resolved.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner1}, new PhysicalCardImpl[]{farin, runner2});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(farin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.HasKeyword(site2, Keyword.MOUNTAIN));

		// Gone as the regroup phase starts.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(site2, Keyword.MOUNTAIN));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileTheOnlyDwarvenCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var harp = scn.GetFreepsCard("harp");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var haldir = scn.GetFreepsCard("haldir");
		scn.MoveCompanionsToTable(gimli, haldir);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(harp);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		// Vitality 3 with 2 wounds: Gimli cannot exert. The Dwarf ally and the Elf could.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(haldir));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(harp));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
	}
}
