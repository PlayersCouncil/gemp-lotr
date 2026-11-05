package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_039_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The Band is cheated in during the fellowship phase of turn 1; the totals are read as they stand.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("band", "33_39");
					put("aggressor", "30_39");   // Orkish Aggressor: assigns itself to Bilbo; the Free Peoples player may add a doubt to discard it
					put("thorin", "30_19");      // Thorin, Oakenshield: [dwarven] companion, vitality 4, who carries wounds that are not Bilbo's
					put("master", "32_20");      // The Master, Master of Lake-Town: an ally with no text that reaches the maneuver phase
					put("negotiator", "31_43");  // Skillful Negotiator: Maneuver event, "spot an ally and exert Bilbo"
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinBandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Goblin Band
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.<br>While you can spot 2 doubts or 2 wounds on Bilbo, the minion archery total is +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("band");

		assertEquals("Goblin Band", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ASecondDoubtAddsOneToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var aggressor = scn.GetShadowCard("aggressor");
		var thorin = scn.GetFreepsCard("thorin");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);

		scn.StartGame();
		scn.MoveMinionsToTable(band, aggressor);
		scn.AddBurdens(1);
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(thorin, 2);
		scn.FreepsRefreshDecision();

		// 1 doubt and 1 wound on Bilbo (no sum of the two), and 2 wounds on another companion: the archer's 1 alone.
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// The Band's archery wound goes on Thorin.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(thorin);
		assertEquals(3, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(aggressor));
		scn.ShadowUseCardAction(aggressor);
		assertTrue(scn.FreepsDecisionAvailable("burden"));
		scn.FreepsChooseYes();

		assertInZone(Zone.DISCARD, aggressor);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(bilbo));
		// 1 for the archer + 1 while 2 doubts can be spotted.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ASecondWoundOnBilboAddsOneToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var band = scn.GetShadowCard("band");
		var thorin = scn.GetFreepsCard("thorin");
		var master = scn.GetFreepsCard("master");
		var negotiator = scn.GetFreepsCard("negotiator");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveCardsToHand(negotiator);

		scn.StartGame();
		scn.MoveAlliesToTable(master);
		scn.MoveMinionsToTable(band);
		scn.AddBurdens(1);
		scn.AddWoundsToChar(bilbo, 1);
		scn.AddWoundsToChar(thorin, 2);
		scn.FreepsRefreshDecision();
		assertInHand(negotiator);

		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(2, scn.GetWoundsOn(thorin));
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.FreepsPlayCard(negotiator);

		// The event's cost exerts Bilbo: his second wound.
		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(thorin));
		// 1 for the archer + 1 while 2 wounds on Bilbo can be spotted.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
