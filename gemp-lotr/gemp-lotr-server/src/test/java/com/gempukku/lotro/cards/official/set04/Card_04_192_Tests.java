package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_192_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("regular", "4_192");
					put("regular2", "4_192");
					put("regular3", "4_192");
					put("regular4", "4_192");

					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, cost 2, site 5, Damage +1 and nothing else
					put("warrior2", "4_165");
					put("searcher", "4_194");   // Uruk Searcher: [isengard] Uruk-hai, cost 4, site 5; text only while skirmishing
					put("attacker", "18_124");  // White Hand Attacker: [uruk-hai] Uruk-hai, cost 3, site 5; text only at a battleground
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, cost 2, site 4; a Shadow ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukRegularStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Regular
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Shadow:</b> Exert this minion to play an Uruk-hai; its twilight cost is -1 for each other Uruk-hai you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("regular");

		assertEquals("Uruk Regular", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionToPlayAnUrukHaiFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var warrior = scn.GetShadowCard("warrior");
		var attacker = scn.GetShadowCard("attacker");
		var forger = scn.GetShadowCard("forger");
		var discarded = scn.GetShadowCard("warrior2");

		// Uruk-hai of two cultures in hand, and an [isengard] Orc; another Uruk-hai in the discard pile.
		scn.MoveCardsToHand(warrior, attacker, forger);
		scn.MoveCardsToDiscard(discarded);
		assertInHand(warrior, attacker, forger);
		assertInDiscard(discarded);

		scn.StartGame();
		scn.MoveMinionsToTable(regular);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(regular));
		assertTrue(scn.ShadowActionAvailable(regular));

		scn.ShadowUseCardAction(regular);
		assertEquals(1, scn.GetWoundsOn(regular));
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, attacker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, discarded));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(warrior);

		// 2 to play + 2 roaming, and nothing off: the Regular is the only Uruk-hai in play and is not counted.
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertInHand(attacker, forger);
		assertInDiscard(discarded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoUrukHaiInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var forger = scn.GetShadowCard("forger");
		var discarded = scn.GetShadowCard("warrior");

		// An [isengard] Orc in hand, and an Uruk-hai in the discard pile.
		scn.MoveCardsToHand(forger);
		scn.MoveCardsToDiscard(discarded);
		assertInHand(forger);
		assertInDiscard(discarded);

		scn.StartGame();
		scn.MoveMinionsToTable(regular);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(regular));
		assertTrue(scn.ShadowPlayAvailable(forger));
		assertFalse(scn.ShadowActionAvailable(regular));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhenThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var warrior = scn.GetShadowCard("warrior");

		scn.MoveCardsToHand(warrior);
		assertInHand(warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(regular);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(regular, 1);
		assertEquals(1, scn.GetWoundsOn(regular));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(warrior));
		assertFalse(scn.ShadowActionAvailable(regular));
	}

	@Test
	public void TwilightCostIsOneLessForEachOtherUrukHaiAndIsCountedInTheOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var attacker = scn.GetShadowCard("attacker");
		var forger = scn.GetShadowCard("forger");
		var warrior = scn.GetShadowCard("warrior");

		scn.MoveCardsToHand(warrior);
		assertInHand(warrior);

		scn.StartGame();
		// One other Uruk-hai, of the [uruk-hai] culture, and an [isengard] Orc that is not counted.
		scn.MoveMinionsToTable(regular, attacker, forger);
		scn.FreepsPass();

		// One short of 2 to play + 2 roaming - 1 for the Attacker.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(regular));

		// Exactly 2 to play + 2 roaming - 1.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(regular));

		// The warrior is the only Uruk-hai in hand, and is chosen without asking.
		scn.ShadowUseCardAction(regular);
		assertEquals(1, scn.GetWoundsOn(regular));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourRegularsInPlayTheSearcherCostsOneAsTheRulingSays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var regular2 = scn.GetShadowCard("regular2");
		var regular3 = scn.GetShadowCard("regular3");
		var regular4 = scn.GetShadowCard("regular4");
		var searcher = scn.GetShadowCard("searcher");
		var warrior = scn.GetShadowCard("warrior");

		scn.MoveCardsToHand(searcher, warrior);
		assertInHand(searcher, warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(regular, regular2, regular3, regular4);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());

		scn.ShadowUseCardAction(regular);
		assertTrue(scn.ShadowHasCardChoiceAvailable(searcher, warrior));
		scn.ShadowChooseCard(searcher);

		// CR 4.2's example: three other Regulars, so the Searcher's cost of 4 is 1; + 2 roaming.  The Warrior in
		// hand is not spotted.
		assertEquals(10, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, searcher);
		// Only the Regular whose ability was used exerted.
		assertEquals(1, scn.GetWoundsOn(regular));
		assertEquals(0, scn.GetWoundsOn(regular2));
		assertEquals(0, scn.GetWoundsOn(regular3));
		assertEquals(0, scn.GetWoundsOn(regular4));

		// The reduction was that one play's: the Warrior played from hand afterwards pays 2 + 2 roaming in full,
		// with five Uruk-hai in play.
		scn.ShadowPlayCard(warrior);
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
