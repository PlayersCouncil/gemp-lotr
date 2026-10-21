package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "2_46");
					put("lurtz", "1_127");      // Lurtz, Servant of Isengard: another unique Uruk-hai, vitality 3; his maneuver ability is only used on purpose
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, cost 2, site 5, Damage +1 is its whole text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, cost 2, site 6, Damage +1 is its whole text
					put("forger", "3_56");      // Isengard Forger: an [isengard] Orc, cost 2; Shadow: exert it to add (1), used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, Damage +1 is its whole text (kept in hand)
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, cost 4, Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Uruk Captain, and an Uruk Savage in the discard pile.
	protected VirtualTableScenario GetCaptainScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "2_46");
					put("savage", "1_151");     // Uruk Savage: Uruk-hai, cost 2, site number 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Uruk Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: Damage +1.
		 * Shadow: Remove (1) and exert Uruk Captain to play an Uruk-hai from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("captain");

		assertEquals("Uruk Captain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void RemovesOneAndExertsUrukCaptainToPlayAnUrukHaiOfEitherCultureFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var lurtz = scn.GetShadowCard("lurtz");
		var warrior = scn.GetShadowCard("warrior");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToDiscard(warrior, sentry, forger);
		scn.MoveCardsToHand(savage);
		// An Uruk-hai in the other player's discard pile, of a title in none of the Shadow player's piles.
		var raiders = scn.GetFreepsCard("raiders");
		scn.MoveCardsToDiscard(raiders);

		scn.StartGame();
		scn.MoveMinionsToTable(captain, lurtz);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(captain));
		assertEquals(0, scn.GetWoundsOn(captain));
		scn.ShadowUseCardAction(captain);

		// Uruk Captain exerts himself: nobody is asked which Uruk-hai exerts, and Lurtz, a unique Uruk-hai
		// beside him, does not.
		assertEquals(1, scn.GetWoundsOn(captain));
		assertEquals(0, scn.GetWoundsOn(lurtz));

		// Uruk-hai of two cultures in the discard pile are offered; an Orc there, an Uruk-hai in hand and one in
		// the opponent's discard pile are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, savage, raiders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(sentry);

		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertInDiscard(warrior, forger, raiders);
		assertInHand(savage);
		// 13 - (1 removed + 2 to play Cirith Ungol Sentry + 2 roaming: a site 6 minion at site 2)
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	// "Remove (1) and exert Uruk Captain to play an Uruk-hai from your discard pile": a play from a pile as an
	// effect is treated as a cost, so the pool must pay for the removal and the Uruk-hai together (F-0046,
	// F-0141).  The definition did not count the (1) it removes: with exactly the Uruk-hai's cost in the pool the
	// ability was offered, took the token, exerted the Captain and played nothing.
	// F-0451.
	@Test
	public void ThePoolMustPayForTheRemovalAndTheUrukHaiTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCaptainScenario();

		var captain = scn.GetShadowCard("captain");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(captain);

		// At site 2 the Savage is roaming: 2 and 2 more.  (4) in the pool pays for him or for the removal.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(captain));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(captain));
		assertEquals(0, scn.GetWoundsOn(captain));
		scn.ShadowUseCardAction(captain);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(captain));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhileUrukCaptainIsExhaustedThoughAnotherUniqueUrukHaiCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var lurtz = scn.GetShadowCard("lurtz");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToDiscard(warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(captain, lurtz);
		// Vitality 2 with 1 wound: the Captain cannot exert; Lurtz beside him could.
		scn.AddWoundsToChar(captain, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(captain));
		assertInDiscard(warrior);
	}

	@Test
	public void NotOfferedWithNoUrukHaiInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToDiscard(forger);
		scn.MoveCardsToHand(savage);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("raiders"));

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(captain));
		assertEquals(0, scn.GetWoundsOn(captain));
	}

	@Test
	public void NotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToDiscard(warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(captain));
	}
}
