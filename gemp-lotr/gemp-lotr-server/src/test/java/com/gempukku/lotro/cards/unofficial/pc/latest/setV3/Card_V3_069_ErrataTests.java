package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_069_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_69");
					put("nazgul", "1_232"); // Ulaire Enquea
					put("guard", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoManMayHinderMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: No Man May Hinder Me
		 * Unique: false
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver/Skirmish
		 * Game Text: Maneuver or Skirmish: Restore a Nazgul.
		 *   Then you may exert The Witch-king twice to hinder all Free Peoples possessions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("No Man May Hinder Me", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		// Errata added Maneuver timeword (was just Skirmish before)
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NoManMayHinderMeCanBePlayedInManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetShadowCard("card");
		var nazgul = scn.GetShadowCard("nazgul");
		scn.MoveCardsToHand(card);
		scn.MoveMinionsToTable(nazgul);

		// Hinder the Nazgul so Restore has a visible effect
		scn.HinderCard(nazgul);

		scn.StartGame();
		scn.SetTwilight(10);

		// Skip to maneuver phase
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// The card should be playable in the Maneuver phase (errata added Maneuver timeword)
		assertTrue(scn.ShadowPlayAvailable(card));

		assertTrue(scn.IsHindered(nazgul));
		scn.ShadowPlayCard(card);
		// Choose the Nazgul to restore (errata changed from all(Nazgul) to choose(Nazgul))
		scn.ShadowChooseCard(nazgul);

		// Nazgul should be restored (no longer hindered)
		assertFalse(scn.IsHindered(nazgul));
	}
}
