package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
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
					// Éomer, Third Marshal of Riddermark: [rohan] Man companion, strength 7, vitality 3;
					// strength +2 for each wound on a minion he is skirmishing
					put("eomer", "4_267");
					// Merry, Master Holbytla: the [rohan] companion who is a Hobbit (a Man too only beside
					// Éowyn or with 3 [rohan] companions), strength 3, vitality 4
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion, strength 8
					// Guma, Plains Farmer: a [rohan] Man who is an ally, strength 4, vitality 2
					put("guma", "4_277");
					// One Whom Men Would Follow: Maneuver event, "Exert Aragorn to spot an ally. Until the
					// regroup phase, that ally is strength +2 and participates in archery fire and skirmishes."
					put("follow", "1_109");

					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, printed fierce
					put("pursuer", "4_190");
					put("pursuer2", "4_190");
					// Uruk Seeker: [isengard] Uruk-hai, strength 6, vitality 2, printed fierce
					put("seeker", "4_195");
					// Black Rider: [wraith] Nazgul, vitality 3, printed fierce; strength 8 and +1 for each companion
					put("rider", "12_161");
					// Easterling Axeman: [raider] Man, strength 6, vitality 2.  "While you can spot 2 burdens,
					// this minion is strength +3 and fierce."
					put("axeman1", "4_224");
					put("axeman2", "4_224");
				}},
				new HashMap<>() {{
					put("site1", "11_239");         // Fangorn Glade
					put("harrowdale", "11_243");
					// Crags of Emyn Muil: its text needs region 2
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					// Buckland Homestead: "Shadow: Play a Nazgul to discard a condition from play."
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
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Plains</b>. Until the regroup phase, each minion skirmishing a [rohan] companion loses
		 * <b>fierce</b> and cannot gain fierce.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("Harrowdale");

		assertEquals("Harrowdale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
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

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eomer, aragorn);

		var pursuer = scn.GetShadowCard("pursuer"); // 5/3
		var seeker = scn.GetShadowCard("seeker"); // 6/2
		scn.MoveMinionsToTable(pursuer, seeker);

		scn.StartGame(harrowdale);
		assertSame(harrowdale, scn.GetCurrentSite());

		// Move during fellowship
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowSite2);
		assertSame(shadowSite2, scn.GetCurrentSite());

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
		assertTrue(scn.IsCharSkirmishingAgainst(seeker, eomer));
		//Fierce was not removed because Harrowdale only works while on it
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		scn.PassCurrentPhaseActions();

		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		// Both are to be defended against a second time, the one that fought the [rohan] companion included.
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(seeker));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(pursuer));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  seeker},
				new PhysicalCardImpl[] { aragorn, pursuer }
		);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void MovingToHarrowdaleRemovesFierceAgainstSkirmishingRohirrim() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();
		var harrowdale = scn.GetShadowSite("Harrowdale");
		var freepsSite1 = scn.GetFreepsSite("Crags of Emyn Muil");
		var shadowSite3 = scn.GetShadowSite("Buckland Homestead");

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eomer, aragorn);

		var pursuer = scn.GetShadowCard("pursuer"); // 5/3
		var seeker = scn.GetShadowCard("seeker"); // 6/2
		scn.MoveMinionsToTable(pursuer, seeker);

		scn.StartGame(freepsSite1);

		// Move during fellowship
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(harrowdale);
		assertSame(harrowdale, scn.GetCurrentSite());

		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  seeker},
				new PhysicalCardImpl[] { aragorn, pursuer }
		);

		// Assigned to the [rohan] companion, with the skirmish not yet begun: still fierce.
		assertTrue(scn.IsCharAssignedAgainst(seeker, eomer));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.IsCharSkirmishingAgainst(seeker, eomer));
		assertFalse(scn.HasKeyword(seeker, Keyword.FIERCE));
		// The minion waiting for the [gondor] companion is untouched.
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer, aragorn));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		// A skirmish later the loss still holds.
		assertFalse(scn.HasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.InFierceRound());
		scn.PassCurrentPhaseActions();

		assertFalse(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		// Only the minion that kept fierce is to be defended against a second time.
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(seeker));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(pursuer));
		scn.FreepsAssignToMinions(aragorn, pursuer);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		//Fierce suppression should have worn off
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(shadowSite3);
		assertSame(shadowSite3, scn.GetCurrentSite());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,  pursuer},
				new PhysicalCardImpl[] { aragorn,  seeker}
		);

		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		// At the next site of the same turn a minion skirmishing the [rohan] companion keeps fierce.
		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer, eomer));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachMinionSkirmishingARohanCompanionLosesFierceAndOneSkirmishingARohanAllyOrAnotherCompanionKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		var harrowdale = scn.GetShadowSite("Harrowdale");

		var eomer = scn.GetFreepsCard("eomer");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guma = scn.GetFreepsCard("guma");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(eomer, merry, aragorn);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToHand(follow);

		var rider = scn.GetShadowCard("rider");
		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveMinionsToTable(rider, pursuer, pursuer2, seeker);

		scn.StartGame(scn.GetFreepsSite("Crags of Emyn Muil"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(harrowdale);
		assertSame(harrowdale, scn.GetCurrentSite());

		// Guma is made to fight: Aragorn exerts, and Guma is the only ally.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer,    rider },
				new PhysicalCardImpl[] {   merry,  pursuer },
				new PhysicalCardImpl[] {    guma, pursuer2 },
				new PhysicalCardImpl[] { aragorn,   seeker }
		);
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer2, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));

		// A Nazgul against a [rohan] Man.
		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.IsCharSkirmishingAgainst(rider, eomer));
		assertFalse(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		// An Uruk-hai against a [rohan] companion who is a Hobbit.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer, merry));
		assertFalse(scn.HasKeyword(pursuer, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		// The same Uruk-hai against a [rohan] Man who is an ally.
		scn.FreepsResolveSkirmish(guma);
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer2, guma));
		assertTrue(scn.HasKeyword(pursuer2, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();
		assertTrue(scn.HasKeyword(pursuer2, Keyword.FIERCE));

		// And against a companion of another culture.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(seeker, aragorn));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(pursuer2));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(seeker));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(rider));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(pursuer));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// In the regroup phase all four are fierce again.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer2, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
	}

	@Test
	public void BothMinionsSkirmishingTheSameRohanCompanionLoseFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		var harrowdale = scn.GetShadowSite("Harrowdale");

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eomer, aragorn);

		var pursuer = scn.GetShadowCard("pursuer");
		var seeker = scn.GetShadowCard("seeker");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveMinionsToTable(pursuer, seeker, pursuer2);

		scn.StartGame(scn.GetFreepsSite("Crags of Emyn Muil"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(harrowdale);
		assertSame(harrowdale, scn.GetCurrentSite());

		scn.SkipToAssignments();
		// The Free Peoples player assigns one minion to each companion, and the Shadow player puts the Seeker
		// on Éomer as well: 5 and 6 together against his 7, so he loses, but not by double (14), and lives.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer, pursuer },
				new PhysicalCardImpl[] { aragorn, pursuer2 }
		);
		scn.ShadowAssignToMinions(eomer, seeker);
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer2, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer, eomer));
		assertTrue(scn.IsCharSkirmishingAgainst(seeker, eomer));
		// Each minion of the skirmish loses it, not one of them.
		assertFalse(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(seeker, Keyword.FIERCE));
		// The minion waiting for the [gondor] companion is untouched.
		assertTrue(scn.HasKeyword(pursuer2, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(pursuer2, aragorn));
		assertTrue(scn.HasKeyword(pursuer2, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(seeker, Keyword.FIERCE));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(pursuer2));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(pursuer));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(seeker));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(seeker, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(pursuer2, Keyword.FIERCE));
	}

	@Test
	public void AMinionThatSkirmishedARohanCompanionCannotGainFierceFromItsOwnTextUntilTheRegroupPhase()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();
		var harrowdale = scn.GetShadowSite("Harrowdale");

		var eomer = scn.GetFreepsCard("eomer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eomer, aragorn);

		var axeman1 = scn.GetShadowCard("axeman1");
		var axeman2 = scn.GetShadowCard("axeman2");
		scn.MoveMinionsToTable(axeman1, axeman2);

		scn.StartGame(scn.GetFreepsSite("Crags of Emyn Muil"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(harrowdale);
		assertSame(harrowdale, scn.GetCurrentSite());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] {   eomer, axeman1 },
				new PhysicalCardImpl[] { aragorn, axeman2 }
		);
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(axeman2, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(eomer);
		assertTrue(scn.IsCharSkirmishingAgainst(axeman1, eomer));
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Two burdens arrive after that skirmish: each Axeman's own text now makes him strength +3 and
		// fierce.  The one that fought the [rohan] companion has the strength and cannot gain fierce.
		scn.AddBurdens(2);
		assertEquals(9, scn.GetStrength(axeman1));
		assertEquals(9, scn.GetStrength(axeman2));
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		assertFalse(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(axeman1));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(axeman2));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The regroup phase: the burdens are still 2, and he is fierce.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.HasKeyword(axeman1, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(axeman2, Keyword.FIERCE));
	}
}
