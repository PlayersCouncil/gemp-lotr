package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crutch", "102_77");
					put("gandalf", "1_364");
					put("glamdring", "1_75");
					put("glamdring2", "6_31");
					put("betrayal", "3_29");

					put("shotgun", "1_231");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfsStaffStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Gandalf's Staff, Old Man's Crutch
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Staff
		 * Vitality: 1
		 * Game Text: Bearer must be Gandalf.
		* 	Discard any weapon he bears.
		* 	Each time Gandalf is exerted by a Free Peoples card, you may wound a minion (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("crutch");

		assertEquals("Gandalf's Staff", card.getBlueprint().getTitle());
		assertEquals("Old Man's Crutch", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.STAFF));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void GandalfsStaffDiscardsWeaponsOnBearerBothBeforeAndAfterPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var crutch = scn.GetFreepsCard("crutch");
		var glamdring = scn.GetFreepsCard("glamdring");
		var glamdring2 = scn.GetFreepsCard("glamdring2");

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(crutch, glamdring2);
		scn.MoveCompanionToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);

		scn.StartGame();

		assertEquals(Zone.HAND, crutch.getZone());
		assertEquals(Zone.ATTACHED, glamdring.getZone());
		scn.FreepsPlayCard(crutch);
		assertEquals(Zone.ATTACHED, crutch.getZone());
		assertEquals(Zone.DISCARD, glamdring.getZone());

		assertEquals(Zone.HAND, glamdring2.getZone());
		scn.FreepsPlayCard(glamdring2);
		assertEquals(Zone.DISCARD, glamdring2.getZone());
	}

	@Test
	public void GandalfsStaffCanWoundAMinionWhenExertingOncePerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var crutch = scn.GetFreepsCard("crutch");
		var betrayal = scn.GetFreepsCard("betrayal");
		scn.MoveCompanionToTable(gandalf);
		scn.AttachCardsTo(gandalf, crutch);
		scn.MoveCardsToSupportArea(betrayal);

		var shotgun = scn.GetShadowCard("shotgun");
		scn.MoveMinionsToTable(shotgun);

		scn.StartGame();

		scn.AddBurdens(4);

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(shotgun));

		scn.FreepsUseCardAction(betrayal);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(shotgun));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(shotgun));

		scn.ShadowPassCurrentPhaseAction();

		//Staff doesn't trigger a second time
		scn.FreepsUseCardAction(betrayal);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(shotgun));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}
}
