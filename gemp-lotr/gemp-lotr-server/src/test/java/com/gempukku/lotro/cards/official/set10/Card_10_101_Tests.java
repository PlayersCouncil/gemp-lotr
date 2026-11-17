package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "10_101");
					put("troll2", "10_101");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc; no text
					put("strength", "1_278");   // Strength Born of Fear: [sauron] support-area condition; a skirmish ability only
					put("scimitar", "1_269");   // Orc Scimitar: [sauron] possession on a [sauron] Orc; a skirmish ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("sword", "1_112");      // Ranger's Sword: [gondor] possession on Aragorn; damage +1
					put("toby", "1_305");       // Old Toby: [shire] support-area possession; a draw on its own play
					put("shards", "3_44");      // The Shards of Narsil: [gondor] support-area artifact; fellowship abilities only
					put("song", "8_112");       // Song of the Shire: [shire] support-area condition; fellowship and skirmish abilities
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TrollofCirithGorgorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Troll of Cirith Gorgor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 6
		 * Game Text: <b>Damage +1</b>. <b>Fierce</b>. To play, spot a [sauron] minion.<br>This minion is strength +1 for each possession you can spot.
		*/

		var scn = GetScenario();

		var troll = scn.GetFreepsCard("troll");

		assertEquals("Troll of Cirith Gorgor", troll.getBlueprint().getTitle());
		assertNull(troll.getBlueprint().getSubtitle());
		assertFalse(troll.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, troll.getBlueprint().getSide());
		assertEquals(Culture.SAURON, troll.getBlueprint().getCulture());
		assertEquals(CardType.MINION, troll.getBlueprint().getCardType());
		assertEquals(Race.TROLL, troll.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(troll, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(troll, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(troll, Keyword.FIERCE));
		assertEquals(7, troll.getBlueprint().getTwilightCost());
		assertEquals(14, troll.getBlueprint().getStrength());
		assertEquals(4, troll.getBlueprint().getVitality());
		assertEquals(6, troll.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilASauronMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var strength = scn.GetShadowCard("strength");
		scn.MoveCardsToHand(troll, soldier);
		assertInHand(troll, soldier);

		scn.StartGame();
		// A [sauron] card that is no minion, and a minion who is not [sauron].
		scn.MoveCardsToSupportArea(strength);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, strength);
		assertInPlay(troop);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(soldier);
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.ShadowPlayAvailable(troll));

		scn.ShadowPlayCard(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronUrukHaiLetsItBePlayedAndWithNoPossessionInPlayItIsStrength14() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(troll);
		assertInHand(troll);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		assertInPlay(guard);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(troll));
		scn.ShadowPlayCard(troll);

		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		// No possession anywhere: no bonus.
		assertEquals(14, scn.GetStrength(troll));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APossessionPlayedOnACompanionMakesItStrengthPlusOneAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		scn.StartGame();
		// Minions laid early, to be read before and after the Free Peoples player's play (Q-0228).
		scn.MoveMinionsToTable(troll, guard);
		assertEquals(14, scn.GetStrength(troll));
		assertEquals(9, scn.GetStrength(guard));

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);

		assertEquals(15, scn.GetStrength(troll));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CountsEveryPossessionOfEitherSideBorneOrNotAndNoArtifactOrCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var soldier = scn.GetShadowCard("soldier");
		var scimitar = scn.GetShadowCard("scimitar");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var shards = scn.GetFreepsCard("shards");
		var song = scn.GetFreepsCard("song");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(toby, shards, song);

		scn.StartGame();
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, toby, shards, song);
		scn.MoveMinionsToTable(troll, troll2, soldier, guard);
		scn.AttachCardsTo(soldier, scimitar);
		assertAttachedTo(scimitar, soldier);

		// 14 + 3: Ranger's Sword on Aragorn, Old Toby in the support area and the Shadow player's own Orc Scimitar.
		// Not the artifact nor the condition.  Free Peoples possessions alone would read 16, the Shadow player's
		// alone 15, borne ones alone 16, and every item 18.  A second copy has its own bonus, and neither adds to
		// the other's.
		assertEquals(17, scn.GetStrength(troll));
		assertEquals(17, scn.GetStrength(troll2));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
