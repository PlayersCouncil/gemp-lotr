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
	protected VirtualTableScenario GetFreepsScenario(Consumer<VirtualTableScenario> bidHandler) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// No card but the Ring-bearer is needed
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_18", null,
				bidHandler
		);
	}

	protected VirtualTableScenario GetShadowScenario(Consumer<VirtualTableScenario> bidHandler) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// No card but the Ring-bearer is needed
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_18",
				bidHandler
		);
	}

	protected VirtualTableScenario GetStandardScenario(Consumer<VirtualTableScenario> bidHandler) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				// No card but the Ring-bearer is needed
				new HashMap<>(),
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, null,
				bidHandler
		);
	}

	@Test
	public void MinimumBidStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_18
		 * Type: MetaSite
		 * Intensity: 6
		 * Game Text: You must bid at least 6 burdens.
		 */

		var scn = GetFreepsScenario(
				s -> {
					s.FreepsChoose("6");
					s.ShadowChoose("0");
					s.FreepsChoose("Go first");
				}
		);

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_18", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(6, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsMustBidAtLeast6WithModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_18: "You must bid at least 6 burdens."
		// Freeps has the modifier, so their minimum bid should be 6: no bid below it can be made.

		GetFreepsScenario(
				scn -> {
					// Freeps should be at the bidding decision
					assertTrue(scn.FreepsDecisionAvailable("bid"));

					// Bidding 6 should succeed
					assertEquals(6, scn.FreepsGetChoiceMin());
					scn.FreepsChoose("6");

					// Shadow can bid 0
					assertEquals(0, scn.ShadowGetChoiceMin());
					scn.ShadowChoose("0");

					// Freeps wins, chooses seat
					scn.FreepsChoose("Go first");

					// Each bid is that player's burdens
					assertEquals(6, scn.FreepsGetBurdens());
					assertEquals(0, scn.ShadowGetBurdens());
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
					scn.FreepsChoose("0");

					// Shadow bidding 6 should succeed
					assertEquals(6, scn.ShadowGetChoiceMin());
					scn.ShadowChoose("6");

					// Shadow wins, chooses seat
					scn.ShadowChoose("Go first");

					// Each bid is that player's burdens
					assertEquals(0, scn.FreepsGetBurdens());
					assertEquals(6, scn.ShadowGetBurdens());
					assertTrue(scn.ShadowDecisionAvailable("mulligan"));
				}
		);
	}

	@Test
	public void BiddingWorksNormallyWithoutModifier() throws DecisionResultInvalidException, CardNotFoundException {
		// Without the modifier, both players can bid 0.

		GetStandardScenario(
				scn -> {
					assertTrue(scn.FreepsDecisionAvailable("bid"));

					// The same bids the rig makes by default: 1 and 0, each from a minimum of 0
					assertEquals(0, scn.FreepsGetChoiceMin());
					scn.FreepsChoose("1");

					assertEquals(0, scn.ShadowGetChoiceMin());
					scn.ShadowChoose("0");

					scn.FreepsChoose("Go first");

					assertEquals(1, scn.FreepsGetBurdens());
					assertEquals(0, scn.ShadowGetBurdens());
					assertTrue(scn.FreepsDecisionAvailable("mulligan"));
				}
		);
	}

	@Test
	public void TheOwnerMayBidMoreThan6() throws DecisionResultInvalidException, CardNotFoundException {
		// "At least": 6 is a floor under the bid and not the bid itself.

		GetFreepsScenario(
				scn -> {
					assertEquals(6, scn.FreepsGetChoiceMin());
					scn.FreepsChoose("7");

					scn.ShadowChoose("0");

					// The higher bidder is the one asked where he sits
					scn.FreepsChoose("Go first");

					// The 7 burdens bid are the owner's; a bid cut down to 6 would read 6
					assertEquals(7, scn.FreepsGetBurdens());
					assertEquals(0, scn.ShadowGetBurdens());
					assertTrue(scn.FreepsDecisionAvailable("mulligan"));
				}
		);
	}
}
