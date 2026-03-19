package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class Card_91_018_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
	}};

	protected VirtualTableScenario GetFreepsScenario(Consumer<VirtualTableScenario> bidHandler) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_18", null,
				bidHandler
		);
	}

	protected VirtualTableScenario GetShadowScenario(Consumer<VirtualTableScenario> bidHandler) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_18",
				bidHandler
		);
	}

	protected VirtualTableScenario GetStandardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MinimumBidStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_18
		 * Type: MetaSite
		 * Game Text: You must bid at least 6 burdens.
		 */

		var scn = GetFreepsScenario(
				s -> {
					s.FreepsDecided("6");
					s.ShadowDecided("0");
					s.FreepsDecided("0");
				}
		);

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_18", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void FreepsMustBidAtLeast6WithModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_18: "You must bid at least 6 burdens."
		// Freeps has the modifier, so their minimum bid should be 6.
		// Bidding less than 6 should be rejected.

		GetFreepsScenario(
				scn -> {
					// Freeps should be at the bidding decision
					assertTrue(scn.FreepsDecisionAvailable("bid"));

					// Bidding 6 should succeed
					assertEquals(6, scn.FreepsGetChoiceMin());
					scn.FreepsDecided("6");

					// Shadow can bid 0
					assertEquals(0, scn.ShadowGetChoiceMin());
					scn.ShadowDecided("0");

					// Freeps wins, chooses seat
					scn.FreepsDecided("0");

					assertTrue(scn.FreepsDecisionAvailable("mulligan"));
				}
		);
	}

	@Test
	public void ShadowMustBidAtLeast6WithTheirCopy() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_18: Shadow has the modifier, so Shadow must bid at least 6.

		GetShadowScenario(
				scn -> {
					// Freeps can bid 0
					assertEquals(0, scn.FreepsGetChoiceMin());
					scn.FreepsDecided("0");

					// Shadow bidding 6 should succeed
					assertEquals(6, scn.ShadowGetChoiceMin());
					scn.ShadowDecided( "6");

					// Shadow wins, chooses seat
					scn.ShadowDecided( "0");

					assertTrue(scn.ShadowDecisionAvailable("mulligan"));
				}
		);
	}

	@Test
	public void BiddingWorksNormallyWithoutModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// Without the modifier, both players can bid 0.

		var scn = GetStandardScenario();

		// This uses the default BidAndSeatPlayers() which bids 1/0, so it already passed
		assertTrue(scn.FreepsDecisionAvailable("mulligan"));
	}
}
