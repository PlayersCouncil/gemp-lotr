package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_242_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "57_242");
					put("merry", "1_302");    // Merry, Friend to Sam (VIT 4, Shire Hobbit)
					put("rohan1", "6_95");    // Hrethel, Rider of Rohan (unique Rohan companion)
					put("rohan2", "7_226");   // Enraged Horseman (non-unique Rohan companion)
					put("rohan3", "7_226");
					put("rohan4", "7_226");
					put("savage", "1_151");   // Uruk Savage (VIT 3, non-unique, non-archer)
					put("raider", "1_158");   // Uruk-hai Raiding Party (VIT 3, non-unique, non-archer)
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
		 * Game Text: Bearer must be Merry.<br><b>Maneuver:</b> Exert Merry X times to exert a minion once
		 *  for each [rohan] companion you spot (limit X).
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
	public void ManeuverExertsMerryXTimesAndWoundsMinionsXTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var merry = scn.GetFreepsCard("merry");
		var rohan1 = scn.GetFreepsCard("rohan1");
		var rohan2 = scn.GetFreepsCard("rohan2");
		var rohan3 = scn.GetFreepsCard("rohan3");
		var rohan4 = scn.GetFreepsCard("rohan4");
		var savage = scn.GetShadowCard("savage");
		var raider = scn.GetShadowCard("raider");

		scn.MoveCompanionsToTable(merry, rohan1, rohan2, rohan3, rohan4);
		scn.AttachCardsTo(merry, sword);
		scn.MoveMinionsToTable(savage, raider);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Freeps uses Merry's Sword maneuver ability
		scn.FreepsUseCardAction(sword);

		// Integer choice: between 1 exertion and 3 spare vitality on Merry
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());

		// Choose 3 (Merry VIT 4 can exert 3 times without dying)
		scn.FreepsChoose("3");

		// Merry should have 3 wounds from the exertion cost (exhausted)
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.IsExhausted(merry));

		// 1st wound: choose which minion to exert — both should be valid
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// 2nd wound: savage now has 1 wound, both still valid
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.IsExhausted(savage));

		// 3rd wound: savage is exhausted so can no longer be exerted, auto-chosen
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(raider));

		// Action complete, control passes to Shadow
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
