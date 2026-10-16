package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_088_Tests
{

	// With no minion in play the game goes from the Shadow phase straight to the regroup phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guide", "1_88");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] ranger
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] ranger
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man who is no ranger

					put("wrath", "4_261");      // Wrath of Harad: [raider] support condition; while a [raider] Man is spotted, each Free Peoples event that spots or exerts a ranger gains the cost "wound a companion"
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2, site 4; strength +3 and fierce only at 2 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnAbleGuideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: An Able Guide
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Spot a ranger to remove (4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guide");

		assertEquals("An Able Guide", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAGondorRangerRemovesFourTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guide = scn.GetFreepsCard("guide");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(guide);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(6);

		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(guide));

		scn.FreepsPlayCard(guide);

		// 6 - 4: four removed, not all of it.
		assertEquals(2, scn.GetTwilight());
		// The ranger is spotted, not exerted.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, guide);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnElvenRangerIsSpottedAndWithFewerThanFourInThePoolAllOfItIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guide = scn.GetFreepsCard("guide");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(guide);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(guide));

		scn.FreepsPlayCard(guide);

		// CR 4.2 "effect": as much as can be removed is removed.
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInZone(Zone.DISCARD, guide);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoRangerToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guide = scn.GetFreepsCard("guide");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(guide);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(6);
		scn.FreepsRefreshDecision();

		// A [gondor] Man who is no ranger, and twilight to remove.
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(guide));
	}

	@Test
	public void UnderWrathOfHaradItAlsoCostsAWoundOnACompanionOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guide = scn.GetFreepsCard("guide");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(guide);

		var wrath = scn.GetShadowCard("wrath");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(wrath);
		assertInZone(Zone.SUPPORT, wrath);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(axeman);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(6);
		scn.FreepsRefreshDecision();

		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SUPPORT, wrath);
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(guide));

		scn.FreepsPlayCard(guide);
		// The event spots a ranger, so Wrath of Harad, with a [raider] Man spotted, adds "wound a companion" to its
		// cost: a cost of the Free Peoples player's event, which he pays and so chooses (CR 4.2 "cost").
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir));
		scn.FreepsChooseCard(boromir);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, guide);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
