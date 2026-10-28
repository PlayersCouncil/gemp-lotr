package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_301_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marauder", "7_301");
					put("marauder2", "7_301");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; his text answers a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability used only on purpose
					put("pillager", "7_275");   // Gorgoroth Pillager: [sauron] Orc, cost 4, site 5; his text works only while he is stacked
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] minion who is no Orc (Uruk-hai); damage +1 only
					put("hound", "7_192");      // Morgul Hound: an Orc who is not [sauron] ([wraith]); his text needs 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcMarauderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Orc Marauder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: While you can spot 3 other [sauron] Orcs, this minion is strength +5.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("marauder");

		assertEquals("Orc Marauder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus5OnceAThirdOtherSauronOrcIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var pillager = scn.GetShadowCard("pillager");
		var sentry = scn.GetShadowCard("sentry");
		var hound = scn.GetShadowCard("hound");

		scn.MoveCardsToHand(pillager);
		scn.StartGame();
		scn.MoveMinionsToTable(marauder, chieftain, soldier, sentry, hound);

		// Two other [sauron] Orcs, beside a [sauron] Uruk-hai and a [wraith] Orc: printed strength.
		assertEquals(11, scn.GetStrength(marauder));
		assertEquals(7, scn.GetStrength(chieftain));
		assertInHand(pillager);

		scn.FreepsPass();
		// 4 for the Pillager + 2 roaming at site 2
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertEquals(11, scn.GetStrength(marauder));
		scn.ShadowPlayCard(pillager);

		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		assertEquals(16, scn.GetStrength(marauder));
		// Only the Marauder is made stronger.
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(pillager));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondMarauderIsAnOtherSauronOrcForTheFirstAndNeitherCountsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marauder = scn.GetShadowCard("marauder");
		var marauder2 = scn.GetShadowCard("marauder2");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(marauder, chieftain, soldier);

		// Three [sauron] Orcs in all, the Marauder among them: only two OTHER ones.
		assertEquals(11, scn.GetStrength(marauder));

		scn.MoveMinionsToTable(marauder2);

		// Each Marauder now spots the other and the two Orcs.
		assertEquals(16, scn.GetStrength(marauder));
		assertEquals(16, scn.GetStrength(marauder2));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
