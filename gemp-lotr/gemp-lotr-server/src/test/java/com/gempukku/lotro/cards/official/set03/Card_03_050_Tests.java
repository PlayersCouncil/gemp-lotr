package com.gempukku.lotro.cards.official.set03;

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

public class Card_03_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("protect", "3_50");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Aragorn, vitality 4; a maneuver ability used only on purpose
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: [rohan] Aragorn, vitality 4; hunter for [rohan] companions only while the Ring-bearer is assigned
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] companion who is no Aragorn; a skirmish ability only

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 and nothing else
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc; a Shadow ability used only on purpose
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] condition, no minion; it lowers the archery total, and there is no archery here
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CanYouProtectMeFromYourselfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Can You Protect Me From Yourself?
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot an [isengard] minion to exhaust Aragorn. The Free Peoples player may add 2 burdens to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("protect");

		assertEquals("Can You Protect Me From Yourself?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnIsengardUrukHaiExhaustsAragornWhenTheFreePeoplesPlayerDeclines() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(protect);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowPlayAvailable(protect));

		scn.ShadowPlayCard(protect);
		assertTrue(scn.FreepsDecisionAvailable("to prevent"));
		scn.FreepsChooseNo();

		// Vitality 4: exhausted is 3 wounds, which the 1 already there and two exertions make.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(protect);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void SpottingAnIsengardOrcTheRohanAragornIsSparedByTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var rohanaragorn = scn.GetFreepsCard("rohanaragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCompanionsToTable(rohanaragorn, boromir);
		scn.MoveCardsToHand(protect);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(rohanaragorn));
		assertTrue(scn.ShadowPlayAvailable(protect));

		scn.ShadowPlayCard(protect);
		assertTrue(scn.FreepsDecisionAvailable("to prevent"));
		scn.FreepsChooseYes();

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(rohanaragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertInDiscard(protect);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnIsengardMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(protect);

		scn.StartGame();
		// An [isengard] card that is no minion, and a minion of another culture.
		scn.MoveCardsToSupportArea(armory);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(protect));

		scn.MoveMinionsToTable(savage);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(protect));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithAragornAlreadyExhaustedTheFreePeoplesPlayerIsNotAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(protect);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(protect));

		scn.ShadowPlayCard(protect);
		assertFalse(scn.FreepsDecisionAvailable("to prevent"));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(protect);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoAragornItIsPlayedForNothingAndExhaustsNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var protect = scn.GetShadowCard("protect");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(protect);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(protect));

		scn.ShadowPlayCard(protect);
		assertFalse(scn.FreepsDecisionAvailable("to prevent"));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(protect);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
