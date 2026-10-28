package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_187_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brawler", "7_187");
					put("brawler2", "7_187");
					// Goblin Runner: another minion, strength 5; its text answers only its own play
					put("runner", "1_178");
					// Ulaire Lemenya, Lieutenant of Morgul: the Nazgul the Brute spots; her text reaches only a companion or
					// ally bearing a [wraith] condition
					put("lemenya", "1_232");
					// Morgul Brute: played with a Nazgul to spot, it may add a burden, which the Free Peoples player may
					// prevent by wounding the Ring-bearer (the legal wound that exhausts him)
					put("brute", "7_188");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulBrawlerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Brawler
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: While the Ring-bearer is exhausted, this minion is strength +3, <b>fierce</b> and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var brawler = scn.GetFreepsCard("brawler");

		assertEquals("Morgul Brawler", brawler.getBlueprint().getTitle());
		assertNull(brawler.getBlueprint().getSubtitle());
		assertFalse(brawler.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, brawler.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, brawler.getBlueprint().getCulture());
		assertEquals(CardType.MINION, brawler.getBlueprint().getCardType());
		assertEquals(Race.ORC, brawler.getBlueprint().getRace());
		assertEquals(4, brawler.getBlueprint().getTwilightCost());
		assertEquals(10, brawler.getBlueprint().getStrength());
		assertEquals(1, brawler.getBlueprint().getVitality());
		assertEquals(4, brawler.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileTheRingBearerIsExhaustedEachCopyIsStrengthPlus3FierceAndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo, Son of Drogo, vitality 4: 2 wounds leave him 2, one short of exhausted. Sam is exhausted.
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 3);
		assertEquals(2, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(sam));

		var brawler = scn.GetShadowCard("brawler");
		var brawler2 = scn.GetShadowCard("brawler2");
		var runner = scn.GetShadowCard("runner");
		var lemenya = scn.GetShadowCard("lemenya");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCardsToHand(brute);
		assertInHand(brute);

		scn.StartGame();
		scn.MoveMinionsToTable(brawler, brawler2, runner, lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(10, scn.GetStrength(brawler));
		assertFalse(scn.HasKeyword(brawler, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(brawler, Keyword.DAMAGE));

		// The Brute's burden, prevented by a wound on the Ring-bearer, which exhausts him.
		scn.ShadowPlayCard(brute);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetVitality(frodo));

		assertEquals(13, scn.GetStrength(brawler)); // 10 + 3
		assertTrue(scn.HasKeyword(brawler, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(brawler, Keyword.DAMAGE));
		// The second copy has its own bonus and no more: neither adds to the other.
		assertEquals(13, scn.GetStrength(brawler2));
		assertTrue(scn.HasKeyword(brawler2, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(brawler2, Keyword.DAMAGE));
		// Other minions are untouched.
		assertEquals(5, scn.GetStrength(runner));
		assertFalse(scn.HasKeyword(runner, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(brute));
		assertFalse(scn.HasKeyword(brute, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(brute, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
