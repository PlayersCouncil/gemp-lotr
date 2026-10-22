package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_062_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "10_62");
					put("banner2", "10_62");    // a second Morgul Banner-bearer
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion, strength 5; its (2) is added only when played
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion, strength 8, no text
					// Men of Harad: a [raider] condition in the support area, which is no minion; its ability is not used
					put("harad", "4_238");

					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; bearer must be a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulBannerbearerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Morgul Banner-bearer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While the total number of other minions and twilight tokens is 3 or fewer, this minion is strength +3 and an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Morgul Banner-bearer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithThreeTwilightTokensAndNoOtherMinionItIsStrengthElevenAndAnArcherUntilAPlayAddsAFourth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(sword);

		var banner = scn.GetShadowCard("banner");
		var runner = scn.GetShadowCard("runner");
		var harad = scn.GetShadowCard("harad");

		scn.StartGame();
		// Laid in the fellowship phase so that a Free Peoples play can add the fourth token (Q-0228).
		scn.MoveMinionsToTable(banner);
		scn.MoveCardsToSupportArea(harad);
		scn.MoveCardsToHand(runner);
		scn.SetTwilight(3);

		// Three tokens, and itself, which is not an "other" minion; a Shadow condition in play and a minion in
		// hand, neither of which counts.
		assertInZone(Zone.SUPPORT, harad);
		assertInHand(runner);
		assertEquals(3, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(banner));
		assertTrue(scn.HasKeyword(banner, Keyword.ARCHER));

		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		// 3 + 1 for the sword
		assertEquals(4, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(banner));
		assertFalse(scn.HasKeyword(banner, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OtherMinionsOfAnyCultureAndRaceAreCountedWithTheTwilightTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(banner, runner, brigand);
		scn.SetTwilight(1);

		// An Orc and a Man of two other cultures, and 1 token: 3.
		assertEquals(1, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(banner));
		assertTrue(scn.HasKeyword(banner, Keyword.ARCHER));
		// The other minions are not made stronger or archers.
		assertEquals(8, scn.GetStrength(brigand));
		assertFalse(scn.HasKeyword(brigand, Keyword.ARCHER));
		assertEquals(5, scn.GetStrength(runner));
		assertFalse(scn.HasKeyword(runner, Keyword.ARCHER));

		// The same two minions and 2 tokens: 4.
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(banner));
		assertFalse(scn.HasKeyword(banner, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoBannerBearersEachCountTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var banner2 = scn.GetShadowCard("banner2");

		scn.StartGame();
		scn.MoveMinionsToTable(banner, banner2);
		scn.SetTwilight(2);

		// For each, one other minion and 2 tokens: 3.
		assertEquals(2, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(banner));
		assertTrue(scn.HasKeyword(banner, Keyword.ARCHER));
		assertEquals(11, scn.GetStrength(banner2));
		assertTrue(scn.HasKeyword(banner2, Keyword.ARCHER));

		// One other minion and 3 tokens: 4.
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(banner));
		assertFalse(scn.HasKeyword(banner, Keyword.ARCHER));
		assertEquals(8, scn.GetStrength(banner2));
		assertFalse(scn.HasKeyword(banner2, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
