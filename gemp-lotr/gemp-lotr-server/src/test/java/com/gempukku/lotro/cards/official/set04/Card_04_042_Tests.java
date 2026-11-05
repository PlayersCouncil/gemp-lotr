package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("company", "4_42");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, home 3F, strength 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: Orc, strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground (its Shadow number counts mounted companions).
	// Thrarin's home is site 3 of the Fellowship path, so on this one he is at no site.  No Dwarf ally has a home
	// that is a battleground: each is of site 3 of the Fellowship path.  Aragorn fights the skirmish.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("company", "4_42");
					put("company2", "4_42");
					put("farin", "1_11");       // Farin, Dwarven Emissary: Dwarf companion, strength 5, resistance 6; +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home 3F, strength 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BestCompanyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Best Company
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Dwarf strength +2 (or +4 if at a battleground).
		*/

		var scn = GetScenario();

		var company = scn.GetFreepsCard("company");

		assertEquals("Best Company", company.getBlueprint().getTitle());
		assertNull(company.getBlueprint().getSubtitle());
		assertFalse(company.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, company.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, company.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, company.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(company, Timeword.SKIRMISH));
		assertEquals(0, company.getBlueprint().getTwilightCost());
	}

	@Test
	public void AwayFromABattlegroundADwarfIsStrengthPlus2ForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var company = scn.GetFreepsCard("company");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(company);
		assertInHand(company);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Ettenmoors, site 2: a plains, not a battleground.
		assertEquals("Ettenmoors", scn.GetCurrentSite().getBlueprint().getTitle());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		// Not a Skirmish phase: not playable among the assignment actions.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(company));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertTrue(scn.FreepsPlayAvailable(company));

		scn.FreepsPlayCard(company);
		// A Dwarf in the skirmish and a Dwarf in none are offered; companions who are no Dwarf are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		// 6 + 2
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(grimir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(company);

		// Still +2 at the next decision of the skirmish.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));

		// Gimli beats the Runner; in the next skirmish, a phase of its own, he is 6 again.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWithNoHomeSiteOnThePathIsPlus2WithTheFellowshipAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var company = scn.GetFreepsCard("company");
		var company2 = scn.GetFreepsCard("company2");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(farin, aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(company, company2);
		assertInHand(company, company2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(5, scn.GetStrength(farin));
		assertEquals(4, scn.GetStrength(thrarin));

		// The companion is with the fellowship, at the battleground: 5 + 4.
		scn.FreepsPlayCard(company);
		scn.FreepsChooseCard(farin);
		assertEquals(9, scn.GetStrength(farin));

		// Thrarin's home is on another block's path: on this one he is at no site (CR 4.2 "ally"): 4 + 2.
		scn.ShadowPass();
		scn.FreepsPlayCard(company2);
		scn.FreepsChooseCard(thrarin);
		assertEquals(6, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
