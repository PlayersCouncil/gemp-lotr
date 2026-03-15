package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_050_Tests
{

// ----------------------------------------
// DESERT WIND WHISPER TESTS
// ----------------------------------------

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("whisper", "103_50");     // Desert Wind Whisper
					put("stalker", "103_49");     // Desert Wind Stalker - grants ambush (1)
					put("initiate", "103_47");    // Desert Wind Initiate - Ambush (3)
					put("scout", "103_48");       // Desert Wind Scout - Ambush (2)
					put("orc", "1_271");          // Orc Soldier - not Southron, no ambush

					put("aragorn", "1_89");
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
		* 	When you play this minion, remove (3) or hinder this minion.
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
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}


	@Test
	public void DesertWindWhisperPayTaxOrHinderOnPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(whisper);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();

		int twilightBefore = scn.GetTwilight();

		scn.ShadowPlayCard(whisper);
		scn.ShadowChoose("Remove");

		// 5 to play + 2 roaming + 3 tax = 10 total
		assertEquals(twilightBefore - 10, scn.GetTwilight());
		assertFalse(scn.IsHindered(whisper));
	}

	@Test
	public void DesertWindWhisperConvertsAmbushToStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(whisper);
		scn.MoveMinionsToTable(initiate);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Initiate has Ambush (3), base strength 6
		int initiateBaseBefore = scn.GetStrength(initiate);
		assertEquals(3, scn.GetKeywordCount(initiate, Keyword.AMBUSH));

		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(whisper);
		scn.ShadowChoose("Remove");

		// Initiate now has strength +3 from Ambush (3)
		assertEquals(initiateBaseBefore + 3, scn.GetStrength(initiate));
	}

	@Test
	public void DesertWindWhisperStacksWithStalkerAmbushBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var stalker = scn.GetShadowCard("stalker");
		var initiate = scn.GetShadowCard("initiate");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(whisper, stalker);
		scn.MoveMinionsToTable(initiate);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		// Initiate starts at Ambush (3), strength 6
		int initiateBase = scn.GetStrength(initiate);
		assertEquals(3, scn.GetKeywordCount(initiate, Keyword.AMBUSH));

		scn.SetTwilight(25);
		scn.FreepsPassCurrentPhaseAction();

		// Play Whisper first
		scn.ShadowPlayCard(whisper);
		scn.ShadowChoose("Remove");

		// Initiate: Ambush (3) → Strength +3
		assertEquals(initiateBase + 3, scn.GetStrength(initiate));

		// Play Stalker - grants Ambush (1) to all Southrons
		scn.ShadowPlayCard(stalker);
		scn.ShadowChoose("Remove");

		// Initiate now: Ambush (4) → Strength +4
		assertEquals(4, scn.GetKeywordCount(initiate, Keyword.AMBUSH));
		assertEquals(initiateBase + 4, scn.GetStrength(initiate));

		// Stalker itself: Ambush (1) → Strength +1
		int stalkerBase = 12; // printed strength
		assertEquals(1, scn.GetKeywordCount(stalker, Keyword.AMBUSH));
		assertEquals(stalkerBase + 1, scn.GetStrength(stalker));

		// Whisper itself: Ambush (1) from Stalker → Strength +1
		int whisperBase = 15; // printed strength
		assertEquals(1, scn.GetKeywordCount(whisper, Keyword.AMBUSH));
		assertEquals(whisperBase + 1, scn.GetStrength(whisper));
	}

	@Test
	public void DesertWindWhisperDoesNotAffectMinionsWithoutAmbush() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var whisper = scn.GetShadowCard("whisper");
		var orc = scn.GetShadowCard("orc");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(whisper);
		scn.MoveMinionsToTable(orc);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		int orcStrengthBefore = scn.GetStrength(orc);
		assertFalse(scn.HasKeyword(orc, Keyword.AMBUSH));

		scn.SetTwilight(15);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(whisper);
		scn.ShadowChoose("Remove");

		// Orc has no ambush, no strength bonus
		assertEquals(orcStrengthBefore, scn.GetStrength(orc));
	}
}
