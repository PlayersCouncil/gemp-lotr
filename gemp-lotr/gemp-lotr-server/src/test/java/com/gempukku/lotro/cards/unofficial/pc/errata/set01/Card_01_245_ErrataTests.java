package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_245_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("measures", "51_245");
					put("orc", "1_266");
					put("runner", "1_178");
					put("target", "1_269");

					put("chaff1", "1_270");
					put("chaff2", "1_270");
					put("chaff3", "1_270");
					put("chaff4", "1_270");
					put("chaff5", "1_270");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesperateMeasuresStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Desperate Measures
		* Unique: False
		* Side: SHADOW
		* Culture: Sauron
		* Twilight Cost: 2
		* Type: event
		* Subtype: Maneuver
		* Game Text: Spot a [sauron] Orc and remove a burden to take a [sauron] card into hand from your discard pile.
		 * The Free Peoples player may discard the top 8 cards of their draw deck to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var measures = scn.GetFreepsCard("measures");

		assertFalse(measures.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, measures.getBlueprint().getSide());
		assertEquals(Culture.SAURON, measures.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, measures.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(measures, Timeword.MANEUVER));
		assertEquals(2, measures.getBlueprint().getTwilightCost());
	}

	@Test
	public void DesperateMeasuresRequiresASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var measures= scn.GetShadowCard("measures");
		var orc= scn.GetShadowCard("runner");
		var target= scn.GetShadowCard("target");
		scn.MoveCardsToHand(measures);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToDiscard(target);

		scn.StartGame();
		scn.AddBurdens(1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(measures));
	}

	@Test
	public void DesperateMeasuresRequiresABurden() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var measures= scn.GetShadowCard("measures");
		var orc= scn.GetShadowCard("orc");
		var target= scn.GetShadowCard("target");
		scn.MoveCardsToHand(measures);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToDiscard(target);

		scn.StartGame();
		scn.RemoveBurdens(1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(measures));
	}

	@Test
	public void DesperateMeasuresTakesASauronCardIntoHandFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var measures= scn.GetShadowCard("measures");
		var orc= scn.GetShadowCard("orc");
		var target= scn.GetShadowCard("target");
		scn.MoveCardsToHand(measures);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToDiscard(target);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());
		assertEquals(Zone.DISCARD, target.getZone());
		assertTrue(scn.ShadowPlayAvailable(measures));

		scn.ShadowPlayCard(measures);

		scn.FreepsChooseNo();

		assertEquals(0, scn.GetBurdens());
		assertEquals(Zone.HAND, target.getZone());
	}

	@Test
	public void DesperateMeasuresCanBePreventedByFreepsDiscarding8TopdeckCards() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var measures= scn.GetShadowCard("measures");
		var orc= scn.GetShadowCard("orc");
		var target= scn.GetShadowCard("target");
		scn.MoveCardsToHand(measures);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToDiscard(target);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetBurdens());
		assertEquals(Zone.SHADOW_CHARACTERS, orc.getZone());
		assertEquals(Zone.DISCARD, target.getZone());
		assertTrue(scn.ShadowPlayAvailable(measures));

		scn.ShadowPlayCard(measures);

		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(9, scn.GetFreepsDeckCount());
		scn.FreepsChooseYes();
		assertEquals(8, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetFreepsDeckCount());

		assertEquals(0, scn.GetBurdens());
		assertEquals(Zone.DISCARD, target.getZone());
	}

	@Test
	public void DesperateMeasuresCanTBePreventedIfFreepsDeckTooSmall() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var measures= scn.GetShadowCard("measures");
		var orc= scn.GetShadowCard("orc");
		var target= scn.GetShadowCard("target");
		scn.MoveCardsToHand(measures);
		scn.MoveMinionsToTable(orc);
		scn.MoveCardsToDiscard(target);

		scn.MoveCardsToFreepsDiscard("chaff1", "chaff2");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(7, scn.GetFreepsDeckCount());
		scn.ShadowPlayCard(measures);

		assertEquals(Zone.HAND, target.getZone());
	}
}
