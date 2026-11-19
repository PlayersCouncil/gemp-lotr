package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_237_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "1_237");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, cost 4, site 3, strength 9; her text
					// reaches only companions and allies bearing a [wraith] condition
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, cost 6, site 3, strength 12; his text
					// wants 3 burdens
					put("attea", "103_61");
					// Morgul Creeper and Morgul Hound: [wraith] Orcs, no Nazgul; their texts want burdens or wounds
					// on the Ring-bearer
					put("creeper", "8_75");
					put("hound", "7_192");
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc; its (2) only when played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Witch-king, Lord of Angmar
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>For each other Nazgûl you can spot, The Witch-king is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Lord of Angmar", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlus2ForEachOtherNazgulPlayedAndNothingForOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var witchking = scn.GetShadowCard("witchking");
		var lemenya = scn.GetShadowCard("lemenya");
		var attea = scn.GetShadowCard("attea");
		var creeper = scn.GetShadowCard("creeper");
		var hound = scn.GetShadowCard("hound");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SetShadowHand(lemenya, attea);
		scn.FreepsPass();
		scn.MoveMinionsToTable(witchking, creeper, hound, runner);
		// Lemenya 4 + 2 roaming, Attea 6 + 2 roaming at site 2
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Three other minions, two of them [wraith], and two Nazgul in hand: none of them counts.
		assertInZone(Zone.HAND, lemenya, attea);
		assertEquals(14, scn.GetStrength(witchking));

		scn.ShadowPlayCard(lemenya);
		assertEquals(16, scn.GetStrength(witchking));
		assertEquals(9, scn.GetStrength(lemenya));

		// A Nazgul of another culture counts as well.  With both: 18.  Himself counted would read 20, [wraith] Nazgul
		// only 16, every other [wraith] minion 20, every other minion 22, +1 for each 16.
		scn.ShadowPlayCard(attea);
		assertEquals(18, scn.GetStrength(witchking));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(12, scn.GetStrength(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
