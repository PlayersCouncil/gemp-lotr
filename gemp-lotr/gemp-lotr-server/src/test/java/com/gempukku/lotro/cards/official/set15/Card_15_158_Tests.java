package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_158_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("uruk", "15_158");
					put("uruk2", "15_158");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; damage +1 only
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
	public void CovetousUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Covetous Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>This minion is strength +1 for each possession you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("uruk");

		assertEquals("Covetous Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void APossessionPlayedOnACompanionMakesItStrengthPlusOneAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		scn.StartGame();
		// Minions laid early, to be read before and after the Free Peoples player's play (Q-0228).
		scn.MoveMinionsToTable(uruk, guard);
		// No possession anywhere: no bonus.
		assertEquals(8, scn.GetStrength(uruk));
		assertEquals(9, scn.GetStrength(guard));

		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);

		// 8 + 1 for the one possession.
		assertEquals(9, scn.GetStrength(uruk));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CountsEveryPossessionOfEitherSideBorneOrNotAndNoArtifactOrCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		var uruk2 = scn.GetShadowCard("uruk2");
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
		scn.MoveMinionsToTable(uruk, uruk2, soldier, guard);
		scn.AttachCardsTo(soldier, scimitar);
		assertAttachedTo(scimitar, soldier);

		// 8 + 3: Ranger's Sword on Aragorn, Old Toby in the support area and the Shadow player's own Orc Scimitar.
		// Not the artifact nor the condition.  Free Peoples possessions alone would read 10, the Shadow player's
		// alone 9, borne ones alone 10, and every item 12.  A second copy has its own bonus, and neither adds to
		// the other's.
		assertEquals(11, scn.GetStrength(uruk));
		assertEquals(11, scn.GetStrength(uruk2));
		assertEquals(9, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
