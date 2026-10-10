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
					put("card", "4_264");
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
		 * Game Text: <b>Response:</b> If your opponent is about to take control of a site, exert Ceorl and a villager to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Ceorl", card.getBlueprint().getTitle());
		assertEquals("Weary Horseman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
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
}
