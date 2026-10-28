package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_194_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mongrel", "7_194");
					put("mongrel2", "7_194");
					// Úlairë Lemenya, Lieutenant of Morgul: another minion, neither fierce nor damage +X; her text
					// reaches only a character bearing a [wraith] condition
					put("lemenya", "1_232");
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					// Legolas, Greenleaf: a companion with the Frodo signet, for Frodo's own "Exert another companion
					// who has the Frodo signet to heal Frodo"
					put("legolas", "1_50");
					// Dear Friends: [shire] Fellowship event, cost 0; "Exert a companion to place a Free Peoples card
					// from your discard pile beneath your draw deck", with an empty discard pile here
					put("friends", "2_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulMongrelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Mongrel
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot 3 wounds on the Ring-bearer, this minion is <b>fierce</b> and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mongrel");

		assertEquals("Morgul Mongrel", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AThirdWoundOnTheRingBearerMakesEachMongrelFierceAndDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var friends = scn.GetFreepsCard("friends");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(friends);

		var mongrel = scn.GetShadowCard("mongrel");
		var mongrel2 = scn.GetShadowCard("mongrel2");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		// Minions laid in the fellowship phase, to be read before and after the wounds change.
		scn.MoveMinionsToTable(mongrel, mongrel2, lemenya);
		scn.AddWoundsToChar(frodo, 2);
		// Three wounds on a companion who is not the Ring-bearer: Sam, of vitality 4, is exhausted.
		scn.AddWoundsToChar(sam, 3);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertFalse(scn.HasKeyword(mongrel, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(mongrel, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(mongrel2, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(mongrel2, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(mongrel));

		// Sam is exhausted, so Frodo exerts for Dear Friends unasked: a third wound token on the Ring-bearer.
		scn.FreepsPlayCard(friends);

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.HasKeyword(mongrel, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(mongrel, Keyword.DAMAGE));
		// Each copy for itself: neither is damage +2.
		assertTrue(scn.HasKeyword(mongrel2, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(mongrel2, Keyword.DAMAGE));
		// The text gives no strength.
		assertEquals(7, scn.GetStrength(mongrel));
		// The text is about the Mongrel itself: the minion beside it gains neither.
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealingTheRingBearerBackToTwoWoundsTakesFierceAndTheDamageBonusAway() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var mongrel = scn.GetShadowCard("mongrel");

		scn.StartGame();
		scn.MoveMinionsToTable(mongrel);
		scn.AddWoundsToChar(frodo, 3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.HasKeyword(mongrel, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(mongrel, Keyword.DAMAGE));

		// Frodo: "Exert another companion who has the Frodo signet to heal Frodo."  Legolas is the only one.
		scn.FreepsUseCardAction(frodo);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertFalse(scn.HasKeyword(mongrel, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(mongrel, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
