package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "3_69");
					put("savage", "1_151");
					put("shaman", "1_152");

					put("sam", "2_114");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Saruman, Servant of the Eye
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Saruman may not take wounds during the archery phase and may not be assigned to a skirmish.<br><b>Assignment:</b> Exert Saruman to assign an [isengard] minion to a companion (except the Ring-bearer). That companion may exert to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Servant of the Eye", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SarumanCanAssignWithoutErrors() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(saruman, savage);

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionToTable(sam);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(saruman));
		assertTrue(scn.CanBeAssignedViaAction(sam));
		assertTrue(scn.CanBeAssignedViaAction(savage));

		scn.ShadowUseCardAction(saruman);
		scn.FreepsChooseNo();
		scn.PassCurrentPhaseActions();
		assertTrue(scn.IsCharAssigned(sam));
		assertTrue(scn.IsCharAssigned(savage));

		scn.FreepsResolveSkirmish(sam);
	}

	@Test
	public void SarumanCannotAssignMultipleMinionsToSameCompanionEvenWithDefender() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveMinionsToTable(saruman, savage, shaman);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionToTable(sam);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(sam);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(saruman));
		assertTrue(scn.CanBeAssignedViaAction(sam));
		assertTrue(scn.CanBeAssignedViaAction(savage));
		assertTrue(scn.CanBeAssignedViaAction(shaman));

		scn.ShadowUseCardAction(saruman);
		scn.ShadowChooseCard(savage);
		scn.FreepsChooseNo();
		scn.FreepsPassCurrentPhaseAction();
		assertTrue(scn.IsCharAssigned(sam));
		assertTrue(scn.IsCharAssigned(savage));
		assertFalse(scn.IsCharAssigned(shaman));
		assertFalse(scn.CanBeAssignedViaAction(sam));
		assertFalse(scn.CanBeAssignedViaAction(savage));
		assertTrue(scn.CanBeAssignedViaAction(shaman));

		scn.ShadowUseCardAction(saruman);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(frodo, shaman);

		scn.FreepsResolveSkirmish(sam);
	}
}
