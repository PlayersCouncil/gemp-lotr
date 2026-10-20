package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_259_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warden", "1_259");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; text on a weapon played on him only
					put("ambusher", "1_261");   // Orc Ambusher: [sauron] Orc, strength 5, cost 1; tracker, and a response to a Free Peoples regroup event
					put("inquisitor", "1_268"); // Orc Inquisitor: [sauron] Orc, strength 9, cost 3; his "when you play" needs cards in the Free Peoples hand
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is an Uruk-hai, strength 9; damage +1
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, strength 5; +4 only at 2 wounds on the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulWardenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Morgul Warden
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: For each other [sauron] Orc you can spot, Morgul Warden is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warden");

		assertEquals("Morgul Warden", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlusOneForEachOtherSauronOrcAndRisesAsMoreArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warden = scn.GetShadowCard("warden");
		var chieftain = scn.GetShadowCard("chieftain");
		var ambusher = scn.GetShadowCard("ambusher");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(ambusher, inquisitor);

		scn.StartGame();
		scn.MoveMinionsToTable(warden, chieftain, guard, hound);
		scn.SetTwilight(20);

		// One other [sauron] Orc, the Chieftain: 9 + 1.  The [sauron] Uruk-hai and the [wraith] Orc are not counted.
		assertEquals(10, scn.GetStrength(warden));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(hound));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(ambusher);

		// A second, of another title: 9 + 2, counted as it comes into play and with nothing asked.
		assertEquals(11, scn.GetStrength(warden));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(inquisitor);

		// A third: 9 + 3.  The others are not made stronger by him.
		assertEquals(12, scn.GetStrength(warden));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(ambusher));
		assertEquals(9, scn.GetStrength(inquisitor));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherSauronOrcHeIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warden = scn.GetShadowCard("warden");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(warden, guard, hound);

		// A [sauron] minion who is no Orc and an Orc who is not [sauron]; he does not count himself: 9.
		assertEquals(9, scn.GetStrength(warden));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(hound));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
