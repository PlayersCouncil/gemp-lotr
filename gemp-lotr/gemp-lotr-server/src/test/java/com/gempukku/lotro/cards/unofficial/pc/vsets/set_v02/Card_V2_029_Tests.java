package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Card_V2_029_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("saruman", "102_29");
					put("worker", "3_62");

					put("legolas", "1_50");
					put("gimli", "1_13");
					put("axe", "1_14");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Saruman, Mind of Metal and Wheels
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Saruman may not be assigned to a skirmish.
		* 	Companions skirmishing [Isengard] Orcs lose all <b>damage</b> bonuses.
		* 	Response: If an [isengard] Orc is about to take a wound, exert Saruman to prevent that wound. 
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Mind of Metal and Wheels", card.getBlueprint().getSubtitle());
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
	public void SarumanCannotBeAssignedToSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		scn.ShadowMoveCharToTable(saruman);
		scn.ShadowMoveCharToTable("worker");

		scn.StartGame();
		scn.SkipToAssignments();
		//Worker but not Saruman
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());
	}

	@Test
	public void Saruman_CAN_TakeArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		scn.ShadowMoveCharToTable(saruman);

		scn.FreepsMoveCharToTable("legolas");

		scn.StartGame();
		scn.SkipToArcheryWounds();

		assertEquals(1, scn.GetWoundsOn(saruman));
	}

	@Test
	public void SarumanNegatesAllDamageBonusesOnCompanionsSkirmishingIsenorcs() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");
		scn.ShadowMoveCharToTable(saruman, worker);

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.FreepsMoveCharToTable(gimli);
		scn.FreepsAttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.SkipToAssignments();

		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsAssignToMinions(gimli, worker);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(worker));
	}

	@Test
	public void SarumanCanExertToPreventWoundsToIsenorcs() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var worker = scn.GetShadowCard("worker");
		scn.ShadowMoveCharToTable(saruman, worker);

		scn.FreepsMoveCharToTable("legolas");

		scn.StartGame();
		scn.SkipToArcheryWounds();

		scn.ShadowChooseCard(worker); //archery wound
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(worker));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(worker));
	}
}
