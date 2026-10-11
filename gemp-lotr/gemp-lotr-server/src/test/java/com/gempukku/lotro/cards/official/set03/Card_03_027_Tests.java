package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_27");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Elrond bearing Vilya, a minion so that there is a maneuver phase, and a Shadow condition to put down later.
	protected VirtualTableScenario GetShadowConditionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vilya", "3_27");
					put("elrond", "1_40");
					put("fear", "1_278");
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VilyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Vilya
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: 1
		 * Game Text: Bearer must be Elrond.<br><b>Maneuver:</b> Exert Elrond twice and spot a Shadow condition to return that condition to its owner's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Vilya", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	// "Exert Elrond twice and spot a Shadow condition to return that condition to its owner's hand": the
	// definition asked for no Shadow condition, and Elrond could exert twice for nothing.
	@Test
	public void TheAbilityWantsAShadowConditionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowConditionScenario();

		var vilya = scn.GetFreepsCard("vilya");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, vilya);

		var fear = scn.GetShadowCard("fear");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Elrond could exert twice, and there is no Shadow condition.
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(vilya));

		// Strength Born of Fear is put down in the Shadow player's support area.
		scn.MoveCardsToSupportArea(fear);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(vilya));
		scn.FreepsUseCardAction(vilya);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertInZone(Zone.HAND, fear);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void VilyaTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
