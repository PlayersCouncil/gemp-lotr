package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_242_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "57_242");
					put("merry", "1_302");   // Merry, Friend to Sam (STR 3, VIT 4, Hobbit)
					put("rohan1", "6_95");   // Hrethel, Rider of Rohan (Rohan Man companion)
					put("rohan2", "7_226");  // Enraged Horseman (Rohan Man companion)
					put("lurtz", "1_127");   // Lurtz, Servant of Isengard (STR 13, VIT 3)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerrysSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Merry's Sword
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Merry.<br><b>Maneuver:</b> Exert Merry X times to exert a minion once for each [rohan] companion you spot (limit X).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Merry's Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void ManeuverExertsMinionForEachRohanCompanionSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var rohan1 = scn.GetFreepsCard("rohan1");
		var rohan2 = scn.GetFreepsCard("rohan2");
		var lurtz = scn.GetShadowCard("lurtz");

		scn.MoveCompanionsToTable(merry, rohan1, rohan2);
		scn.AttachCardsTo(merry, sword);
		scn.MoveMinionsToTable(lurtz);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Use Merry's Sword maneuver ability
		scn.FreepsUseCardAction(sword);

		// Choose to spot 2 Rohan companions
		scn.FreepsChoose("2");

		// Merry should have 2 wounds from the exertion cost
		assertEquals(2, scn.GetWoundsOn(merry));

		// Choose Lurtz as the minion to exert
		scn.FreepsChooseCard(lurtz);

		// Lurtz should have 2 wounds from being exerted twice
		assertEquals(2, scn.GetWoundsOn(lurtz));
	}
}
