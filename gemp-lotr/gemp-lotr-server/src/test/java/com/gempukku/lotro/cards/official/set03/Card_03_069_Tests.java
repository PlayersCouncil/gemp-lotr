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

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "3_69");
					// Uruk-hai Raiding Party: an [isengard] minion; Damage +1 is its whole text
					put("raiders", "1_158");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
					put("merry", "1_303");      // Merry, From O'er the Brandywine: his text is about bearing a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: his text is about [shire] tales
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
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

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
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(sam);
		scn.ShadowPass();
		scn.FreepsPass();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(saruman));
		assertTrue(scn.CanBeAssignedViaAction(sam));
		assertTrue(scn.CanBeAssignedViaAction(savage));
		assertTrue(scn.CanBeAssignedViaAction(shaman));

		scn.ShadowUseCardAction(saruman);
		scn.ShadowChooseCard(savage);
		scn.FreepsChooseNo();
		scn.FreepsPass();
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

	@Test
	public void AssignmentAbilityOffersEveryCompanionButTheRingBearerAndASecondUseWithNoMinionLeftAssignsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var saruman = scn.GetShadowCard("saruman");
		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, raiders, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));

		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));

		// The Raiding Party is the one minion that can be taken: Saruman may not be assigned, and the Troop
		// is not [isengard].  So no minion is asked for.
		scn.ShadowChooseCard(merry);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(merry, raiders));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.IsCharAssigned(pippin));
		assertFalse(scn.IsCharAssigned(troop));
		assertFalse(scn.IsCharAssigned(saruman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// No [isengard] minion is left to assign.  The ability has a cost that can be paid, so it is still
		// offered: Saruman exerts for nothing, though Pippin and the Troop are both free.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertFalse(scn.IsCharAssigned(pippin));
		assertFalse(scn.IsCharAssigned(troop));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(merry, raiders));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
