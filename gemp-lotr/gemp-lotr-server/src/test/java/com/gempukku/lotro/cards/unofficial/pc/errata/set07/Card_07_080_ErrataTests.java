package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.PossessionClass;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_080_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anduril", "57_80");
					put("aragorn", "1_89");
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndurilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Anduril, King's Blade
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Artifact
		 * Subtype: Hand weapon
		 * Game Text: Bearer must be Aragorn. He is strength +2 for each minion he is skirmishing.
		 * 	Assignment: Add a threat to make Aragorn <b>defender +1</b> (limit +1).
		*/

		var scn = GetScenario();

		var anduril = scn.GetFreepsCard("anduril");

		assertEquals("Andúril", anduril.getBlueprint().getTitle());
		assertEquals("King's Blade", anduril.getBlueprint().getSubtitle());
		assertTrue(anduril.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, anduril.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, anduril.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, anduril.getBlueprint().getCardType());
		assertTrue(anduril.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(3, anduril.getBlueprint().getTwilightCost());
		// Errata removed the base +2 strength
		assertEquals(0, anduril.getBlueprint().getStrength());
	}

	@Test
	public void StrengthBonusAppliesDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var runner1 = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();

		// Aragorn base str 8, Anduril has no base str in errata
		assertEquals(8, scn.GetStrength(aragorn));

		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Outside skirmish: no bonus
		assertEquals(8, scn.GetStrength(aragorn));

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, runner1);

		// During skirmish with 1 minion: 8 + 2 = 10
		assertEquals(10, scn.GetStrength(aragorn));

		scn.PassSkirmishActions();
		// Aragorn (str 10) beats Runner (str 5); no wounds on Aragorn
	}

	@Test
	public void AssignmentActionGrantsDefenderAndCostsThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var anduril = scn.GetFreepsCard("anduril");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, anduril);

		scn.StartGame();

		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Before action: no defender, no threats
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetThreats());

		// Use Anduril's assignment action
		assertTrue(scn.FreepsActionAvailable(anduril));
		scn.FreepsUseCardAction(anduril);

		// After action: defender +1, threat +1
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(1, scn.GetThreats());

		// Action should no longer be available (limitPerPhase: 1)
		assertFalse(scn.FreepsActionAvailable(anduril));

		// Defender +1 means Aragorn can be assigned to 2 minions (1 base + 1 defender)
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, runner1, runner2);

		// During skirmish with 2 minions: 8 + 2*2 = 12
		assertEquals(12, scn.GetStrength(aragorn));

		scn.PassSkirmishActions();
		// Aragorn (str 12) beats both Runners (str 5 each)
	}
}
