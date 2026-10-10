package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_050_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("whisper", "103_50");
					put("stalker", "103_49");   // Desert Wind Stalker: strength 12; Southrons gain ambush (1)
					put("soldier", "4_254");    // Southron Soldier: Southron, ambush (2), strength 4, cost 2, no other text
					put("fighter", "4_251");    // Southron Fighter: Southron, ambush (1), strength 10; a maneuver ability never used here
					put("horror", "14_13");     // Horror of Harad: [orc] Half-troll, ambush (1), strength 9; keywords only (damage +1, fierce, lurker, muster, toil 2)
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text at all
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertWindWhisperStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Desert Wind Whisper
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. Tracker.
		 * 	When you play this minion, remove (4) or hinder this minion.
		 * 	Each minion with ambush (X) is strength +X.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("whisper");

		assertEquals("Desert Wind Whisper", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertFalse(scn.HasKeyword(card, Keyword.AMBUSH));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedOffersTheChoiceAndRemovingFourLeavesItUnhindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(whisper);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());
		assertEquals(4, scn.GetStrength(soldier));
		assertInZone(Zone.HAND, whisper);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(whisper));

		scn.ShadowPlayCard(whisper);

		assertInZone(Zone.SHADOW_CHARACTERS, whisper);
		assertEquals(0, scn.GetShadowHandCount());
		// 11 - 5 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowChoiceAvailable("Remove (4)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChoose("Remove (4)");

		// 4 - 4 for the choice
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.IsHindered(whisper));
		assertInZone(Zone.SHADOW_CHARACTERS, whisper);
		// Strength 4 + 2 for ambush (2)
		assertEquals(6, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedChoosingToHinderRemovesNothingAndHindersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		scn.MoveCardsToHand(whisper);

		scn.StartGame();
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(11, scn.GetTwilight());

		scn.ShadowPlayCard(whisper);

		// 11 - 5 to play - 2 roaming at site 2
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.IsHindered(whisper));
		assertTrue(scn.ShadowChoiceAvailable("Remove (4)"));
		assertTrue(scn.ShadowChoiceAvailable("Hinder"));

		scn.ShadowChoose("Hinder");

		// Nothing more is removed, and the minion is hindered where it stands.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.IsHindered(whisper));
		assertInZone(Zone.SHADOW_CHARACTERS, whisper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileItIsHinderedMinionsWithAmbushGetNoBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(whisper);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertEquals(4, scn.GetStrength(soldier));

		scn.ShadowPlayCard(whisper);

		// In play and not yet hindered: strength 4 + 2 for ambush (2)
		assertFalse(scn.IsHindered(whisper));
		assertEquals(6, scn.GetStrength(soldier));

		scn.ShadowChoose("Hinder");

		assertTrue(scn.IsHindered(whisper));
		// The game text of a hindered card does not apply, so the Soldier is back to its own strength.
		assertEquals(4, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedWithFewerThanFourLeftInThePoolItIsHinderedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		scn.MoveCardsToHand(whisper);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.IsHindered(whisper));

		scn.ShadowPlayCard(whisper);

		// 10 - 5 to play - 2 roaming at site 2 leaves 3, one short of what removing (4) needs.
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowChoiceAvailable("Remove (4)"));
		assertTrue(scn.IsHindered(whisper));
		assertInZone(Zone.SHADOW_CHARACTERS, whisper);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingAnotherMinionWhileItIsInPlayRemovesAndHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.MoveMinionsToTable(whisper);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.IsHindered(whisper));

		scn.ShadowPlayCard(soldier);

		assertInPlay(soldier);
		// 10 - 2 to play the Soldier - 2 roaming at site 2, and nothing more
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.IsHindered(whisper));
		assertFalse(scn.IsHindered(soldier));
		assertFalse(scn.ShadowChoiceAvailable("Remove (4)"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMinionWithAmbushIsStrongerByItsOwnAmbushNumberAndNothingElseIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var soldier = scn.GetShadowCard("soldier");
		var fighter = scn.GetShadowCard("fighter");
		var horror = scn.GetShadowCard("horror");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, fighter, horror, troop);

		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(fighter, Keyword.AMBUSH));
		assertEquals(1, scn.GetKeywordCount(horror, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(troop, Keyword.AMBUSH));

		assertEquals(4, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(fighter));
		assertEquals(9, scn.GetStrength(horror));
		assertEquals(13, scn.GetStrength(troop));

		scn.MoveMinionsToTable(whisper);

		// Strength 4 + 2 for ambush (2)
		assertEquals(6, scn.GetStrength(soldier));
		// Strength 10 + 1 for ambush (1)
		assertEquals(11, scn.GetStrength(fighter));
		// Neither [raider], nor a Man, nor a Southron: strength 9 + 1 for ambush (1)
		assertEquals(10, scn.GetStrength(horror));
		// A minion with no ambush
		assertEquals(13, scn.GetStrength(troop));
		// Whisper has no ambush of its own
		assertEquals(15, scn.GetStrength(whisper));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AmbushGainedFromAnotherCardCountsAndAddsToPrintedAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var stalker = scn.GetShadowCard("stalker");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(whisper, soldier, troop);

		assertEquals(2, scn.GetKeywordCount(soldier, Keyword.AMBUSH));
		assertFalse(scn.HasKeyword(whisper, Keyword.AMBUSH));
		// Strength 4 + 2 for ambush (2)
		assertEquals(6, scn.GetStrength(soldier));
		assertEquals(15, scn.GetStrength(whisper));
		assertEquals(13, scn.GetStrength(troop));

		// Stalker: "Southrons gain ambush (1)."
		scn.MoveMinionsToTable(stalker);

		// Printed ambush (2) and gained ambush (1), taken as adding up (Q-0007): strength 4 + 2 + 1
		assertEquals(7, scn.GetStrength(soldier));
		// Stalker is a Southron itself: strength 12 + 1 for the ambush (1) it gives itself
		assertEquals(1, scn.GetKeywordCount(stalker, Keyword.AMBUSH));
		assertEquals(13, scn.GetStrength(stalker));
		// Whisper is a Southron too, and is a minion with ambush (1) now: strength 15 + 1
		assertEquals(1, scn.GetKeywordCount(whisper, Keyword.AMBUSH));
		assertEquals(16, scn.GetStrength(whisper));
		// Not a Southron: no ambush, no bonus
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
