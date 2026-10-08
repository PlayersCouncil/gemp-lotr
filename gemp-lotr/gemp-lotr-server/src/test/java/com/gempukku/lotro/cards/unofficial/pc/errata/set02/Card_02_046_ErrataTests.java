package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_046_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "52_46");
					put("lurtz", "1_127");      // Lurtz, Servant of Isengard: another unique Uruk-hai; his maneuver ability is only used on purpose
					put("shagrat", "10_99");    // Shagrat, Captain of Cirith Ungol: unique [sauron] Uruk-hai, Damage +1; his trigger is on losing a skirmish
					put("assassin", "1_131");   // Orthanc Assassin: unique [isengard] Uruk-hai, cost 5, site 5; Archer and Damage +1 are its whole text
					put("savage", "1_151");     // Uruk Savage: non-unique [isengard] Uruk-hai, Damage +1 is its whole text
					put("savage2", "1_151");
					put("grima", "4_153");      // Gríma, Son of Galmód: unique [isengard] Man; weakens companions bearing Shadow conditions (none here)
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, cost 2, site 5, Damage +1 is its whole text
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, cost 2, site 6, Damage +1 is its whole text
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc; Shadow: exert it to add (1)
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
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Shadow:</b> Remove (1) and exert a unique Uruk-hai to play an Uruk-hai from your discard pile.
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
	public void PlayedFromHandForThreePlusRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		scn.MoveCardsToHand(captain);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(captain));
		scn.ShadowPlayCard(captain);

		assertInZone(Zone.SHADOW_CHARACTERS, captain);
		// 3 to play + 2 roaming (site 5 minion at site 2)
		assertEquals(twilight - 5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RemovesOneAndExertsAUniqueUrukHaiToPlayAnUrukHaiFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var lurtz = scn.GetShadowCard("lurtz");
		var shagrat = scn.GetShadowCard("shagrat");
		var assassin = scn.GetShadowCard("assassin");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var grima = scn.GetShadowCard("grima");
		var warrior = scn.GetShadowCard("warrior");
		var sentry = scn.GetShadowCard("sentry");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToDiscard(warrior, sentry, assassin, forger);
		scn.MoveCardsToHand(savage2);
		// An Uruk-hai in the other player's discard pile.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("savage"));

		scn.StartGame();
		scn.MoveMinionsToTable(captain, lurtz, shagrat, savage, grima);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(captain));
		scn.ShadowUseCardAction(captain);

		// The unique Uruk-hai of either culture may exert; a non-unique Uruk-hai and a unique minion of another race may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(captain, lurtz, shagrat));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, grima));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lurtz);

		assertEquals(1, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(captain));
		assertEquals(0, scn.GetWoundsOn(shagrat));

		// Uruk-hai of two cultures in the discard pile, a unique one among them, are offered; an Orc there, an
		// Uruk-hai in hand and one in the opponent's discard pile are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, sentry, assassin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, savage2));
		scn.ShadowChooseCardBPFromSelection(sentry);

		assertInZone(Zone.SHADOW_CHARACTERS, sentry);
		assertInDiscard(warrior, assassin, forger);
		assertInHand(savage2);
		// 1 removed + 2 to play Cirith Ungol Sentry + 2 roaming (site 6 minion at site 2)
		assertEquals(twilight - 5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithExactlyEnoughTwilightTheCaptainExertsHimselfToPlayAnIsengardUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToDiscard(warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		// 2 set + 1 for the companion + 2 for site 2
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(captain));
		scn.ShadowUseCardAction(captain);

		// The Captain is the only unique Uruk-hai, and so the one exerted.
		assertEquals(1, scn.GetWoundsOn(captain));
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		// 1 removed + 2 to play Orthanc Warrior + 2 roaming
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithTwilightForTheRemovalButOneShortOfAlsoPlayingTheUrukHai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToDiscard(warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		// 1 set + 1 for the companion + 2 for site 2: one short of 1 removed + 2 + 2 roaming
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(captain));
		assertEquals(0, scn.GetWoundsOn(captain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheOnlyUniqueUrukHaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCardsToDiscard(warrior);

		scn.StartGame();
		scn.MoveMinionsToTable(captain, savage);
		// Vitality 2 with 1 wound: the Captain cannot exert; the non-unique Uruk Savage beside him could.
		scn.AddWoundsToChar(captain, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(captain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithNoUrukHaiInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetShadowCard("captain");
		var savage2 = scn.GetShadowCard("savage2");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToDiscard(forger);
		scn.MoveCardsToHand(savage2);

		scn.StartGame();
		scn.MoveMinionsToTable(captain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(captain));
		assertEquals(0, scn.GetWoundsOn(captain));
		assertTrue(scn.AwaitingShadowPhaseActions());
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
