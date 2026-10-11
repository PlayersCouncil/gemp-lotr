package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_108");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFortificationScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("destructive", "15_108");
					// Parapet: a [rohan] fortification in the support area; its Archery ability needs 2 [rohan] Men
					put("parapet", "5_87");
					put("goblin", "11_137");    // Scurrying Goblin: an [orc] Orc of strength 5, to bear the mount
					// Threatening Warg: an [orc] possession (a mount) on an [orc] Orc of strength 10 or less; its own
					// offer comes only when it is played
					put("warg", "17_91");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DestructiveOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Destructive Orc
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you play Destructive Orc, you may discard a fortification for each [orc] possession and each [orc] artifact you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Destructive Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  "A fortification
	// for each [orc] possession and each [orc] artifact" is no fortification where there is neither.
	@Test
	public void TheDiscardIsNotOfferedWithNoOrcPossessionOrArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFortificationScenario();

		var parapet = scn.GetFreepsCard("parapet");
		scn.MoveCardsToSupportArea(parapet);

		var destructive = scn.GetShadowCard("destructive");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(destructive);

		scn.StartGame();
		// An [orc] minion that bears nothing
		scn.MoveMinionsToTable(goblin);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A fortification is there to discard.
		scn.ShadowPlayCard(destructive);
		assertInZone(Zone.SHADOW_CHARACTERS, destructive);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(destructive));
		assertInZone(Zone.SUPPORT, parapet);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDiscardIsOfferedWithAnOrcPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFortificationScenario();

		var parapet = scn.GetFreepsCard("parapet");
		scn.MoveCardsToSupportArea(parapet);

		var destructive = scn.GetShadowCard("destructive");
		var goblin = scn.GetShadowCard("goblin");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCardsToHand(destructive);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.AttachCardsTo(goblin, warg);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(destructive);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(destructive));
		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(parapet);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void DestructiveOrcTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(6, scn.GetTwilight());
	}
}
