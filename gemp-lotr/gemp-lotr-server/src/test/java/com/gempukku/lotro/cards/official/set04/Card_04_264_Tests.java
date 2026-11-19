package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_264_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ceorl", "4_264");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ceorl", "4_264");
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] ally, a villager, vitality 2; his ability is a fellowship one
					put("hlafwine", "4_281");   // Hlafwine, Village Farmhand: the other villager, vitality 2; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: no villager; strength 8 and vitality 4, he loses to the Intruder and lives
					put("theoden", "4_292");    // Théoden, Son of Thengel: "Regroup: Spot a [rohan] ally and exert Théoden to liberate a site."
					// White Hand Intruder: [uruk-hai] minion, strength 9, damage +1.  "Each time this minion wins a
					// skirmish, if you do not control a site, you may take control of a site."
					put("intruder", "17_127");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CeorlStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ceorl, Weary Horseman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Response: If your opponent is about to take control of a site, exert Ceorl and a villager to prevent this.
		*/

		var scn = GetScenario();

		var ceorl = scn.GetFreepsCard("ceorl");

		assertEquals("Ceorl", ceorl.getBlueprint().getTitle());
		assertEquals("Weary Horseman", ceorl.getBlueprint().getSubtitle());
		assertTrue(ceorl.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, ceorl.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, ceorl.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, ceorl.getBlueprint().getCardType());
		assertEquals(Race.MAN, ceorl.getBlueprint().getRace());
		assertEquals(2, ceorl.getBlueprint().getTwilightCost());
		assertEquals(6, ceorl.getBlueprint().getStrength());
		assertEquals(3, ceorl.getBlueprint().getVitality());
		assertEquals(6, ceorl.getBlueprint().getResistance());
	}

	@Test
	public void ResponseExertsCeorlAndAVillagerToPreventAnOpponentTakingControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ceorl = scn.GetFreepsCard("ceorl");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var aragorn = scn.GetFreepsCard("aragorn");
		var intruder = scn.GetShadowCard("intruder");
		scn.MoveCompanionsToTable(ceorl, aragorn);
		scn.MoveAlliesToTable(guma, hlafwine);

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker and can be controlled.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(intruder);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The Intruder, 9 against 8, has won: its controller may take control of a site, and says yes.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ceorl));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetWoundsOn(ceorl));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		scn.FreepsAcceptOptionalTrigger();

		// "A villager": either of the two, and nobody else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guma, hlafwine));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ceorl, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);

		assertEquals(1, scn.GetWoundsOn(ceorl));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		// Nothing was taken.
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheResponseDeclinedTheOpponentTakesControlOfTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ceorl = scn.GetFreepsCard("ceorl");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var aragorn = scn.GetFreepsCard("aragorn");
		var intruder = scn.GetShadowCard("intruder");
		scn.MoveCompanionsToTable(ceorl, aragorn);
		scn.MoveAlliesToTable(guma, hlafwine);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(intruder);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ceorl));
		assertFalse(scn.IsSiteControlled(site1));
		scn.FreepsDeclineOptionalTrigger();

		// The same board with Ceorl holding back: site 1, the one site behind both markers, is the Shadow player's.
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(ceorl));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhileCeorlIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ceorl = scn.GetFreepsCard("ceorl");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var aragorn = scn.GetFreepsCard("aragorn");
		var intruder = scn.GetShadowCard("intruder");
		scn.MoveCompanionsToTable(ceorl, aragorn);
		scn.MoveAlliesToTable(guma, hlafwine);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// Vitality 3: two wounds exhaust him, with both villagers still able to exert.
		scn.AddWoundsToChar(ceorl, 2);
		assertEquals(2, scn.GetWoundsOn(ceorl));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		scn.MoveMinionsToTable(intruder);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertFalse(scn.IsSiteControlled(site1));
		scn.ShadowAcceptOptionalTrigger();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ceorl));
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(2, scn.GetWoundsOn(ceorl));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ResponseIsNotOfferedWhileNoVillagerCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ceorl = scn.GetFreepsCard("ceorl");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var aragorn = scn.GetFreepsCard("aragorn");
		var intruder = scn.GetShadowCard("intruder");
		scn.MoveCompanionsToTable(ceorl, aragorn);
		scn.MoveAlliesToTable(guma, hlafwine);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// Vitality 2: one wound exhausts each villager; Ceorl and Aragorn, who is no villager, could still exert.
		scn.AddWoundsToChar(guma, 1);
		scn.AddWoundsToChar(hlafwine, 1);
		assertEquals(0, scn.GetWoundsOn(ceorl));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(hlafwine));
		scn.MoveMinionsToTable(intruder);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, intruder);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(intruder));
		assertFalse(scn.IsSiteControlled(site1));
		scn.ShadowAcceptOptionalTrigger();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ceorl));
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(ceorl));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(hlafwine));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASiteLiberatedIsNoSiteAboutToBeControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var ceorl = scn.GetFreepsCard("ceorl");
		var guma = scn.GetFreepsCard("guma");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCompanionsToTable(ceorl, theoden);
		scn.MoveAlliesToTable(guma, hlafwine);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.IsSiteControlled(site1));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(0, scn.GetWoundsOn(ceorl));
		assertTrue(scn.FreepsActionAvailable(theoden));
		scn.FreepsUseCardAction(theoden);

		assertEquals(1, scn.GetWoundsOn(theoden));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		// No response was offered: the game went on to the Shadow player's regroup actions.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ceorl));
		assertEquals(0, scn.GetWoundsOn(ceorl));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
