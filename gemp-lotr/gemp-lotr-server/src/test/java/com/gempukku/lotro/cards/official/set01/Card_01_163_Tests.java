package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_163_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chieftain", "1_163");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; a "when you play" add (2), harmless cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, cost 6; no text
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, strength 7, cost 3; Archer and nothing else
					put("troll", "1_165");      // Cave Troll of Moria: a [moria] minion who is a Troll, strength 15; text on playing it only
					put("orcchief", "1_266");   // Orc Chieftain: a [sauron] Orc, strength 7; text on a weapon played on him only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AncientChieftainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Ancient Chieftain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: For each other [moria] Orc you can spot, Ancient Chieftain is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chieftain");

		assertEquals("Ancient Chieftain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlusOneForEachOtherMoriaOrcAndRisesAsMoreArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var marksman = scn.GetShadowCard("marksman");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");
		scn.MoveCardsToHand(troop, marksman);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, runner, troll, orcchief);
		scn.SetTwilight(20);

		// One other [moria] Orc, the Runner: 9 + 1.  The [moria] Troll and the [sauron] Orc are not counted.
		assertEquals(10, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(troop);

		// A second, of another title: 9 + 2, counted as it comes into play and with nothing asked.
		assertEquals(11, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(marksman);

		// A third: 9 + 3.  The others are not made stronger by him.
		assertEquals(12, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(marksman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherMoriaOrcHeIsNotMadeStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chieftain = scn.GetShadowCard("chieftain");
		var troll = scn.GetShadowCard("troll");
		var orcchief = scn.GetShadowCard("orcchief");

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, troll, orcchief);

		// A [moria] minion who is no Orc and an Orc who is not [moria]; he does not count himself: 9.
		assertEquals(9, scn.GetStrength(chieftain));
		assertEquals(15, scn.GetStrength(troll));
		assertEquals(7, scn.GetStrength(orcchief));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
