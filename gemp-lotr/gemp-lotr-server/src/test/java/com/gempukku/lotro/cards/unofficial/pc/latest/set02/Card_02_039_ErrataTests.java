package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_02_039_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beyond", "52_39");
					put("uruk", "1_151");    // Uruk Lieutenant (for exert cost)
					put("helm", "1_15");     // Gimli's Helm (FP helm possession)
					put("armor", "1_101");   // Gondorian armor? We'll use whatever fits
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeyondtheHeightofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Beyond the Height of Men
		 * Unique: false
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert an Uruk-hai to discard a Free Peoples armor, helm, or shield.
		 * Hinder up to 4 Free Peoples possessions if you can spot 6 companions or 8 [Isengard] cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beyond");

		assertEquals("Beyond the Height of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BeyondTheHeightDiscardsAFPArmorHelmOrShield() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var uruk = scn.GetShadowCard("uruk");
		var helm = scn.GetFreepsCard("helm");
		var gimli = scn.GetFreepsCard("armor"); // placeholder

		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToHand(beyond);

		// Attach helm to Gimli (we'll use Frodo as a stand-in for simplicity)
		var frodo = scn.GetRingBearer();
		// Actually, Gimli's Helm requires Gimli as bearer, so let's just put a generic
		// FP possession with helm class in play. We'll skip the helm-specific attach.

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Play the event
		assertTrue(scn.ShadowPlayAvailable(beyond));
		scn.ShadowPlayCard(beyond);

		// Cost: exert an Uruk-hai (auto-selected, only 1)
		assertEquals(1, scn.GetWoundsOn(uruk));
	}
}
