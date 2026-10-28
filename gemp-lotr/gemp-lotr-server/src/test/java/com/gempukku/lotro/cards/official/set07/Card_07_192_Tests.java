package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_192_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hound", "7_192");
					put("hound2", "7_192");
					// Úlairë Lemenya, Lieutenant of Morgul: another minion, strength 9, not fierce; her text reaches only
					// a character bearing a [wraith] condition
					put("lemenya", "1_232");
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					// Legolas, Greenleaf: a companion with the Frodo signet, for Frodo's own "Exert another companion
					// who has the Frodo signet to heal Frodo"
					put("legolas", "1_50");
					// Dear Friends: [shire] Fellowship event, cost 0; "Exert a companion to place a Free Peoples card
					// from your discard pile beneath your draw deck"
					put("friends", "2_98");
					put("friends2", "2_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulHoundStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Hound
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: While you can spot 2 wounds on the Ring-bearer, this minion is strength +4 and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hound");

		assertEquals("Morgul Hound", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ASecondWoundOnTheRingBearerMakesEachHoundStrengthPlus4AndFierceAndAThirdKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var friends = scn.GetFreepsCard("friends");
		var friends2 = scn.GetFreepsCard("friends2");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(friends, friends2);

		var hound = scn.GetShadowCard("hound");
		var hound2 = scn.GetShadowCard("hound2");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		// Minions laid in the fellowship phase, to be read before and after the wounds change.
		scn.MoveMinionsToTable(hound, hound2, lemenya);
		scn.AddWoundsToChar(frodo, 1);
		// Two wounds on a companion who is not the Ring-bearer.
		scn.AddWoundsToChar(sam, 2);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(5, scn.GetStrength(hound));
		assertFalse(scn.HasKeyword(hound, Keyword.FIERCE));
		assertEquals(5, scn.GetStrength(hound2));
		assertFalse(scn.HasKeyword(hound2, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(lemenya));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));

		// Frodo exerts for Dear Friends: a second wound token on the Ring-bearer.
		scn.FreepsPlayCard(friends);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		scn.FreepsChooseCard(frodo);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		// 5 + 4 on each copy, each for itself: neither is 13.
		assertEquals(9, scn.GetStrength(hound));
		assertTrue(scn.HasKeyword(hound, Keyword.FIERCE));
		assertEquals(9, scn.GetStrength(hound2));
		assertTrue(scn.HasKeyword(hound2, Keyword.FIERCE));
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		// The text is about the Hound itself: the minion beside it gains nothing.
		assertEquals(9, scn.GetStrength(lemenya));
		assertFalse(scn.HasKeyword(lemenya, Keyword.FIERCE));

		// Two wounds or more: a third keeps it.
		scn.FreepsPlayCard(friends2);
		scn.FreepsChooseCard(frodo);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(9, scn.GetStrength(hound));
		assertTrue(scn.HasKeyword(hound, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealingTheRingBearerBackToOneWoundTakesTheBonusAndFierceAway() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.AddWoundsToChar(frodo, 2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(9, scn.GetStrength(hound));
		assertTrue(scn.HasKeyword(hound, Keyword.FIERCE));

		// Frodo: "Exert another companion who has the Frodo signet to heal Frodo."  Legolas is the only one.
		scn.FreepsUseCardAction(frodo);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetStrength(hound));
		assertFalse(scn.HasKeyword(hound, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
