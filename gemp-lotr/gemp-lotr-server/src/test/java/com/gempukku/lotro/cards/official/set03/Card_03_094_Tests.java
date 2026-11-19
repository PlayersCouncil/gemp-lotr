package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("butcher1", "3_94");
					put("butcher2", "3_94");
					put("defenses", "1_274");   // Sauron's Defenses: [sauron] condition, support area; acts only with 3 companions dead
					put("evil", "1_283");       // You Bring Great Evil: [sauron] condition borne by a companion; acts only at a sanctuary
					put("few", "1_260");        // The Number Must Be Few: [sauron] condition, support area, cost 2, no play requirement; acts only at 7 companions
					put("pinned", "1_192");     // Pinned Down: [moria] condition, support area; acts only with a [moria] archer
					put("mallorn", "1_54");     // Mallorn-trees: [elven] Free Peoples condition, support area; a fellowship action only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; text on a weapon PLAYED on him only
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] possession on a [sauron] Orc; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion to bear You Bring Great Evil; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcButcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Butcher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: For each [sauron] condition you can spot, this minion is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("butcher1");

		assertEquals("Orc Butcher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCopyIsStrengthPlusOneForEachSauronConditionInPlayAndForNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(mallorn);

		var butcher1 = scn.GetShadowCard("butcher1");
		var butcher2 = scn.GetShadowCard("butcher2");
		var defenses = scn.GetShadowCard("defenses");
		var evil = scn.GetShadowCard("evil");
		var pinned = scn.GetShadowCard("pinned");
		var chieftain = scn.GetShadowCard("chieftain");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.MoveMinionsToTable(butcher1, butcher2, chieftain);
		scn.AttachCardsTo(chieftain, scimitar);
		scn.AttachCardsTo(aragorn, evil);
		scn.MoveCardsToSupportArea(defenses, pinned);

		assertInZone(Zone.SUPPORT, defenses, pinned, mallorn);
		assertAttachedTo(evil, aragorn);
		assertAttachedTo(scimitar, chieftain);

		// Two [sauron] conditions, one in the support area and one borne by a companion: 6 + 2.
		// Counting every condition (the [moria] one and the Free Peoples one too) would read 10, and every
		// [sauron] card (the possession and the three Orcs too) 12.  Each copy is made stronger by its own
		// text alone: one that reached every Butcher, or every minion, would read 10.
		assertEquals(8, scn.GetStrength(butcher1));
		assertEquals(8, scn.GetStrength(butcher2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoSauronConditionHeIsNotMadeStrongerUntilOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCardsToSupportArea(mallorn);

		var butcher = scn.GetShadowCard("butcher1");
		var few = scn.GetShadowCard("few");
		var pinned = scn.GetShadowCard("pinned");
		var chieftain = scn.GetShadowCard("chieftain");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(few);

		scn.StartGame();
		scn.MoveMinionsToTable(butcher, chieftain);
		scn.AttachCardsTo(chieftain, scimitar);
		scn.MoveCardsToSupportArea(pinned);
		scn.SetTwilight(10);

		assertInZone(Zone.SUPPORT, pinned, mallorn);
		assertAttachedTo(scimitar, chieftain);
		assertInHand(few);

		// A [moria] condition, a Free Peoples condition and a [sauron] possession: none of them counts.
		assertEquals(6, scn.GetStrength(butcher));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(few);

		// A [sauron] condition played: 6 + 1, with nothing asked.
		assertInZone(Zone.SUPPORT, few);
		assertEquals(7, scn.GetStrength(butcher));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
