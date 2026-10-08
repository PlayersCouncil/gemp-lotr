package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_192_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("regular", "54_192");
					put("regular2", "54_192");

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
		 * Game Text: Damage +1.
		 * Shadow: Exert this minion to play an Uruk-hai; its twilight cost is -1 for each other Uruk-hai you can
		 * spot (limit -2).
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
	public void PlayedForThreePlusRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		scn.MoveCardsToHand(regular);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(regular));

		scn.ShadowPlayCard(regular);
		// 3 to play + 2 roaming (site 5 minion at site 2)
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, regular);
		assertTrue(scn.AwaitingShadowPhaseActions());
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

		// An [isengard] Orc in hand, and an Uruk-hai in the discard pile.
		scn.MoveCardsToHand(forger);
		scn.MoveCardsToDiscard(scn.GetShadowCard("warrior"));

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

		scn.StartGame();
		scn.MoveMinionsToTable(regular);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(regular, 1);
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

		scn.StartGame();
		// One other Uruk-hai, of the [uruk-hai] culture, and an [isengard] Orc that is not counted.
		scn.MoveMinionsToTable(regular, attacker, forger);
		scn.SetTwilight(0);
		scn.FreepsPass();

		// 0 set + 1 companion + 2 for Ettenmoors: exactly 2 to play + 2 roaming - 1.
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
	public void TheReductionStopsAtTwoAndEachCopyHasItsOwnLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var regular2 = scn.GetShadowCard("regular2");
		var attacker = scn.GetShadowCard("attacker");
		var searcher = scn.GetShadowCard("searcher");
		var warrior = scn.GetShadowCard("warrior");

		scn.MoveCardsToHand(searcher, warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(regular, regular2, attacker);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());

		// Two other Uruk-hai: the second Regular and the Attacker.
		scn.ShadowUseCardAction(regular);
		assertTrue(scn.ShadowHasCardChoiceAvailable(searcher, warrior));
		scn.ShadowChooseCard(searcher);
		// 4 to play + 2 roaming - 2
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, searcher);

		// Three other Uruk-hai now: the first Regular, the Attacker and the Searcher.  The limit is -2, and this
		// copy's limit is its own.
		scn.ShadowUseCardAction(regular2);
		// 2 to play + 2 roaming - 2
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertEquals(1, scn.GetWoundsOn(regular));
		assertEquals(1, scn.GetWoundsOn(regular2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void UsedTwiceInOnePhaseTheSameCopyTakesTwoOffEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var attacker = scn.GetShadowCard("attacker");
		var warrior = scn.GetShadowCard("warrior");
		var searcher = scn.GetShadowCard("searcher");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.MoveCardsToHand(searcher, warrior2);

		scn.StartGame();
		scn.MoveMinionsToTable(regular, attacker, warrior);
		// Vitality 3, so that this Regular can exert twice.
		scn.ApplyAdHocModifier(new VitalityModifier(null, regular, null, 1));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(13, scn.GetTwilight());

		scn.ShadowUseCardAction(regular);
		scn.ShadowChooseCard(searcher);
		// 4 to play + 2 roaming - 2
		assertEquals(9, scn.GetTwilight());

		assertTrue(scn.ShadowActionAvailable(regular));
		scn.ShadowUseCardAction(regular);
		// 2 to play + 2 roaming - 2: the limit is on each use, as built.
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior2);
		assertEquals(2, scn.GetWoundsOn(regular));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("F-0245: whether (limit -2) is the card's for the whole phase (CR 4.2 limit) or a cap on each use")
	public void UsedTwiceInOnePhaseTheSameCopyTakesNoMoreThanTwoOffInAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regular = scn.GetShadowCard("regular");
		var attacker = scn.GetShadowCard("attacker");
		var warrior = scn.GetShadowCard("warrior");
		var searcher = scn.GetShadowCard("searcher");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.MoveCardsToHand(searcher, warrior2);

		scn.StartGame();
		scn.MoveMinionsToTable(regular, attacker, warrior);
		// Vitality 3, so that this Regular can exert twice.
		scn.ApplyAdHocModifier(new VitalityModifier(null, regular, null, 1));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(13, scn.GetTwilight());

		scn.ShadowUseCardAction(regular);
		scn.ShadowChooseCard(searcher);
		// 4 to play + 2 roaming - 2
		assertEquals(9, scn.GetTwilight());

		scn.ShadowUseCardAction(regular);
		// 2 to play + 2 roaming: this copy's -2 is spent for the phase.
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, warrior2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
