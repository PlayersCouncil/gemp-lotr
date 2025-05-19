package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_035_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wall", "102_35");
					put("uruk", "1_143"); //costs 5
					put("uruk2", "1_143"); //costs 5
					put("savage", "1_151"); //costs 2
					put("evil", "2_41");

					put("unheeded", "8_115");
					put("pippin", "1_306");
					put("greenleaf", "1_50");
					put("sting", "1_313");
					put("orcbane", "2_109");
					put("lindenroot", "5_19");
					put("gilgalad", "9_15");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukShieldWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Uruk Shield Wall
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Machine. Each time an Uruk-hai costing X is about to take a wound (except during a skirmish),
		 * prevent that and exert it. X is the number of [isengard] tokens you can spot here.
		 * Maneuver: Exert an Uruk-hai to add an [isengard] token here.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wall");

		assertEquals("Uruk Shield Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void UrukShieldWallBlocksWoundsDuringShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var uruk = scn.GetShadowCard("uruk");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToHand(evil);
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 5);
		scn.AddWoundsToChar(uruk, 2);

		var unheeded = scn.GetFreepsCard("unheeded");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(unheeded);
		scn.MoveCompanionToTable(pippin);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(evil);
		assertEquals(3, scn.GetWoundsOn(uruk));
		assertEquals(1, scn.GetVitality(uruk));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.ShadowChoose("1");

		assertEquals(Zone.SHADOW_CHARACTERS, uruk.getZone());
		assertEquals(3, scn.GetWoundsOn(uruk));
	}

	@Test
	public void UrukShieldWallBlocksWoundsAndExertsDuringManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var uruk = scn.GetShadowCard("uruk");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToHand(evil);
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 5);
		scn.AddWoundsToChar(uruk, 2);

		var sting = scn.GetFreepsCard("sting");
		var orcbane = scn.GetFreepsCard("orcbane");
		var unheeded = scn.GetFreepsCard("unheeded");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(unheeded, sting, orcbane);
		scn.MoveCompanionToTable(pippin);

		scn.StartGame();
		scn.FreepsPlayCard(sting);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(orcbane);
		scn.FreepsChoose("1");
		//Since Unheeded triggered, we know our uruk got exerted instead of wounded
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(Zone.SHADOW_CHARACTERS, uruk.getZone());
		assertEquals(3, scn.GetWoundsOn(uruk));
	}

	@Test
	public void UrukShieldWallBlocksWoundsAndExertsDuringArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var uruk = scn.GetShadowCard("uruk");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToHand(evil);
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 5);
		scn.AddWoundsToChar(uruk, 2);

		var greenleaf = scn.GetFreepsCard("greenleaf");
		var unheeded = scn.GetFreepsCard("unheeded");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(unheeded);
		scn.MoveCompanionToTable(pippin);
		scn.MoveCompanionToTable(greenleaf);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsUseCardAction(greenleaf);

		//Since Unheeded triggered, we know our uruk got exerted instead of wounded
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());

		assertEquals(Zone.SHADOW_CHARACTERS, uruk.getZone());
		assertEquals(3, scn.GetWoundsOn(uruk));
	}

	@Test
	public void UrukShieldWall_DOESNOT_BlockDuringSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var uruk = scn.GetShadowCard("uruk");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToHand(evil);
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 5);
		scn.AddWoundsToChar(uruk, 3);

		var lindenroot = scn.GetFreepsCard("lindenroot");
		var unheeded = scn.GetFreepsCard("unheeded");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCardsToHand(unheeded);
		scn.MoveCompanionToTable(pippin);
		scn.MoveCompanionToTable(lindenroot);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(lindenroot);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		scn.FreepsAssignToMinions(lindenroot, uruk);
		scn.FreepsResolveSkirmish(lindenroot);

		scn.FreepsUseCardAction(lindenroot);

		assertEquals(Zone.DISCARD, uruk.getZone());
	}

	@Test
	public void UrukShieldWallBlocksWoundsAndExertsDuringRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall");
		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
		var savage = scn.GetShadowCard("savage");
		var evil = scn.GetShadowCard("evil");
		scn.MoveCardsToHand(evil);
		scn.MoveMinionsToTable(uruk, uruk2, savage);
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 5);
		scn.AddWoundsToChar(uruk, 3);
		scn.AddWoundsToChar(uruk2, 3);
		scn.AddWoundsToChar(savage, 2);

		//In addition to regroup, we are testing that:
		// A: multi-wound abilities don't somehow bypass shield wall
		// B: lethal damage doesn't bypass shield wall
		var gilgalad = scn.GetFreepsCard("gilgalad");
		//not bothering with unheeded since the multi-wound part is what's important
		scn.MoveCompanionToTable(gilgalad);

		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertEquals(Zone.SHADOW_CHARACTERS, uruk.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, uruk2.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, savage.getZone());
		scn.FreepsUseCardAction(gilgalad);

		assertEquals(Zone.SHADOW_CHARACTERS, uruk.getZone());
		assertEquals(Zone.SHADOW_CHARACTERS, uruk2.getZone());
		//The only unprotected uruk
		assertEquals(Zone.DISCARD, savage.getZone());
	}
}
