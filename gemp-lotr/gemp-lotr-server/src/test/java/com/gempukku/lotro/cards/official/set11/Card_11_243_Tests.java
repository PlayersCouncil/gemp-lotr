package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_243_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
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
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
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
		 * Game Text: <b>Plains</b>. Until the regroup phase, each minion skirmishing a [rohan] companion loses
		 * <b>fierce</b> and cannot gain fierce.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		var card = scn.GetFreepsSite("Harrowdale");

		assertEquals("Harrowdale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	/**
	 * As per Legacy Ruling #4, Harrowdale's text is to be interpreted as only working while the fellowship is at that site.
	 * Thus, if you start at Harrowdale and move away from it, the text does not "follow" the fellowship.
	 * <a href="https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_4">Wiki LR#4</a>
	 */

	@Test
	public void MovingFromHarrowdale_DOESNOT_RemoveFierceAgainstSkirmishingRohirrim() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();
		var harrowdale = scn.GetFreepsSite("Harrowdale");
		var shadowSite2 = scn.GetShadowSite("Crags of Emyn Muil");
		var shadowSite3 = scn.GetShadowSite("Buckland Homestead");

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionToTable(eomer, aragorn);

		var pursuer = scn.GetShadowCard("pursuer"); // 5/3
		var seeker = scn.GetShadowCard("seeker"); // 6/2
		scn.MoveMinionsToTable(pursuer, seeker);

		scn.StartGame(harrowdale);

		// Move during fellowship
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseCardBPFromSelection(shadowSite2);

		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  seeker},
				new PhysicalCardImpl[] { aragorn, pursuer }
		);

		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		//Fierce was not removed because Harrowdale only works while on it
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();

		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.FreepsAssignToMinions(aragorn, pursuer);
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
		scn.MoveCompanionToTable(eomer, aragorn);

		var pursuer = scn.GetShadowCard("pursuer"); // 5/3
		var seeker = scn.GetShadowCard("seeker"); // 6/2
		scn.MoveMinionsToTable(pursuer, seeker);

		scn.StartGame(freepsSite1);

		// Move during fellowship
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseCardBPFromSelection(harrowdale);

		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  seeker},
				new PhysicalCardImpl[] { aragorn, pursuer }
		);

		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertFalse(scn.HasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		scn.PassCurrentPhaseActions();

		assertFalse(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.FreepsAssignToMinions(aragorn, pursuer);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		//Fierce suppression should have worn off
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(shadowSite3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  pursuer},
				new PhysicalCardImpl[] { aragorn,  seeker}
		);

		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();
	}
}
