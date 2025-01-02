package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_243_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("eomer", "4_267");
					put("aragorn", "1_89");

					put("pursuer", "4_190"); // 5/3
					put("seeker", "4_195"); // 6/2
				}},
				new HashMap<>() {{
					put("site1", "11_239");
					put("harrowdale", "11_243");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing,
				GenericCardTestHelper.Shadows
		);
	}

	@Test
	public void HarrowdaleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Harrowdale
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Plains</b>. Until the regroup phase, each minion skirmishing a [rohan] companion loses <b>fierce</b> and cannot gain fierce.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		var card = scn.GetFreepsSite("Harrowdale");

		assertEquals("Harrowdale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.PLAINS));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void HarrowdaleTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.FreepsMoveCardToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(3, scn.GetTwilight());
	}

	@Test
	public void MovingFromHarrowdaleRemovesFierceAgainstSkirmishingRohirrim() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();
		var harrowdale = scn.GetFreepsSite("Harrowdale");
		var shadowSite2 = scn.GetShadowSite("Crags of Emyn Muil");
		var shadowSite3 = scn.GetShadowSite("Buckland Homestead");

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(eomer, aragorn);

		var pursuer = scn.GetShadowCard("pursuer"); // 5/3
		var seeker = scn.GetShadowCard("seeker"); // 6/2
		scn.ShadowMoveCharToTable(pursuer, seeker);

		scn.StartGame(harrowdale);

		// Move during fellowship
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseCardBPFromSelection(shadowSite2);

		assertEquals(1, ((DefaultActionsEnvironment) scn._game.getActionsEnvironment()).getUntilStartOfPhaseActionProxies(Phase.REGROUP).size());
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  seeker},
				new PhysicalCardImpl[] { aragorn, pursuer }
		);

		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertFalse(scn.hasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();

		assertFalse(scn.hasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		scn.FreepsAssignToMinions(aragorn, pursuer);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		//Fierce suppression should have worn off
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(shadowSite3);

		assertEquals(0, ((DefaultActionsEnvironment) scn._game.getActionsEnvironment()).getUntilStartOfPhaseActionProxies(Phase.REGROUP).size());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  pursuer},
				new PhysicalCardImpl[] { aragorn,  seeker}
		);

		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();
	}

	@Test
	public void MovingToHarrowdaleRemovesFierceAgainstSkirmishingRohirrim() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();
		var harrowdale = scn.GetShadowSite("Harrowdale");
		var freepsSite1 = scn.GetShadowSite("Crags of Emyn Muil");
		var shadowSite3 = scn.GetShadowSite("Buckland Homestead");

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.FreepsMoveCharToTable(eomer, aragorn);

		var pursuer = scn.GetShadowCard("pursuer"); // 5/3
		var seeker = scn.GetShadowCard("seeker"); // 6/2
		scn.ShadowMoveCharToTable(pursuer, seeker);

		scn.StartGame(freepsSite1);

		// Move during fellowship
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseCardBPFromSelection(harrowdale);

		assertEquals(1, ((DefaultActionsEnvironment) scn._game.getActionsEnvironment()).getUntilStartOfPhaseActionProxies(Phase.REGROUP).size());
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  seeker},
				new PhysicalCardImpl[] { aragorn, pursuer }
		);

		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertFalse(scn.hasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();

		assertFalse(scn.hasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		scn.FreepsAssignToMinions(aragorn, pursuer);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		//Fierce suppression should have worn off
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(shadowSite3);

		assertEquals(0, ((DefaultActionsEnvironment) scn._game.getActionsEnvironment()).getUntilStartOfPhaseActionProxies(Phase.REGROUP).size());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  pursuer},
				new PhysicalCardImpl[] { aragorn,  seeker}
		);

		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.hasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.hasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();
	}
}
