package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_172_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer", "1_172");
					put("archer2", "1_172");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, site 4, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its (2) only when played
					put("cavetroll", "1_165");  // Cave Troll of Moria, Scourge of the Black Pit: [moria] Troll, no Orc; its other text is about playing it
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; its text answers a weapon played on it
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinArcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Archer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.
		 * 	While you can spot another [moria] Orc, the fellowship archery total is -6.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("archer");

		assertEquals("Goblin Archer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void FellowshipArcheryTotalIsSixLessOnceAnotherMoriaOrcIsPlayedBesideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(archer, troop);

		scn.StartGame();
		// A fellowship archery total high enough that 6 less is told from 5 or 7 less.
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 7));
		scn.SetTwilight(12);
		scn.FreepsPass();

		// 12 set + 1 companion + 2 for the site
		assertEquals(15, scn.GetTwilight());
		assertEquals(7, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(archer);
		// 5 to play + 2 roaming
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, archer);
		// It is a [moria] Orc itself, and not another one.
		assertEquals(7, scn.GetFreepsArcheryTotal());

		scn.ShadowPlayCard(troop);
		// 6 to play + 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		// The minion archery total is its one archer's, as it was.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyAnotherMoriaOrcThatCanBeSpottedLowersTheTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var runner = scn.GetShadowCard("runner");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 7));
		scn.FreepsPass();

		scn.MoveMinionsToTable(archer);
		assertEquals(7, scn.GetFreepsArcheryTotal());

		// A [moria] minion who is no Orc, and an Orc who is not [moria].
		scn.MoveMinionsToTable(cavetroll, chieftain);
		assertEquals(7, scn.GetFreepsArcheryTotal());

		scn.MoveMinionsToTable(runner);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// A hindered card cannot be spotted.
		scn.HinderCard(runner);
		assertEquals(7, scn.GetFreepsArcheryTotal());
		scn.RestoreCard(runner);
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoGoblinArchersEachSpotTheOtherAndEachTakesSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var archer2 = scn.GetShadowCard("archer2");

		scn.StartGame();
		// High enough that 12 less does not reach zero.
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 13));
		scn.FreepsPass();

		scn.MoveMinionsToTable(archer);
		assertEquals(13, scn.GetFreepsArcheryTotal());

		scn.MoveMinionsToTable(archer2);
		// 13 - 6 - 6
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
