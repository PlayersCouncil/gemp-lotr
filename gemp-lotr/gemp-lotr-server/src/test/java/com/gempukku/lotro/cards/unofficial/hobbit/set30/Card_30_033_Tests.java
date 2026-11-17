package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_033_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fimbul", "30_33");
					// Orkish Aggressor: [gundabad] Orc, vitality 2; an Assignment ability only
					put("aggressor", "30_39");
					// Orkish Veteran: [sauron] Orc, vitality 2; a Regroup ability, and archer only at site 9
					put("veteran", "32_43");
					// Bill Ferny Sr.: a [gundabad] minion who is a Man, not an Orc; assignment bars and a Regroup
					// ability that kills a Hobbit ally of site 1
					put("ferny", "33_22");
					// Wicked Spider: [spider] Spider minion, fierce; a Skirmish ability only
					put("spider", "31_55");
					// Borders of the Forest: [mirkwood] condition, no minion; an end-of-Shadow-phase trigger and an
					// Assignment ability, neither reached from the draw deck
					put("borders", "33_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void FimbulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Fimbul, Orkish Assassin
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Damage +1</b>. To play, exert an Orc.
		 * 	<b>Shadow:</b> Discard Fimbul from play to play a minion from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fimbul");

		assertEquals("Fimbul", card.getBlueprint().getTitle());
		assertEquals("Orkish Assassin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedByExertingAnOrcOfEitherCultureAndNotAMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fimbul = scn.GetShadowCard("fimbul");
		var aggressor = scn.GetShadowCard("aggressor");
		var veteran = scn.GetShadowCard("veteran");
		var ferny = scn.GetShadowCard("ferny");

		scn.StartGame();
		scn.MoveCardsToHand(fimbul);
		scn.MoveMinionsToTable(aggressor, veteran, ferny);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.HAND, fimbul);
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fimbul));

		scn.ShadowPlayCard(fimbul);
		// Orcs of two cultures; not the [gundabad] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ferny));
		scn.ShadowChooseCard(veteran);

		assertEquals(1, scn.GetWoundsOn(veteran));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertInZone(Zone.SHADOW_CHARACTERS, fimbul);
		assertEquals(0, scn.GetWoundsOn(fimbul));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fimbul = scn.GetShadowCard("fimbul");
		var aggressor = scn.GetShadowCard("aggressor");
		var ferny = scn.GetShadowCard("ferny");

		scn.StartGame();
		scn.MoveCardsToHand(fimbul);
		scn.MoveMinionsToTable(aggressor, ferny);
		// Vitality 2: one wound exhausts him.  Bill Ferny Sr. could exert, but is no Orc.
		scn.AddWoundsToChar(aggressor, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(ferny));
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.HAND, fimbul);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fimbul));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(ferny));
	}

	@Test
	public void ShadowActionDiscardsFimbulToPlayAMinionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fimbul = scn.GetShadowCard("fimbul");
		var aggressor = scn.GetShadowCard("aggressor");
		var spider = scn.GetShadowCard("spider");
		var borders = scn.GetShadowCard("borders");

		scn.StartGame();
		scn.MoveMinionsToTable(fimbul);
		scn.SetShadowDrawDeck(aggressor, spider, borders);
		scn.SkipToPhase(Phase.SHADOW);
		// Enough for either minion: 2 and 2 roaming for the Orc of site 3, 2 and 2 roaming for the Spider of site 5.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, fimbul);
		assertInZone(Zone.DECK, aggressor, spider, borders);
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(fimbul));

		scn.ShadowUseCardAction(fimbul);
		assertInZone(Zone.DISCARD, fimbul);
		scn.ShadowDismissRevealedCards();
		// Minions of two cultures and races; not the condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, spider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(borders));
		// A play from a draw deck is chosen from 0 (Q-0019).
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(spider);

		assertInZone(Zone.SHADOW_CHARACTERS, spider);
		assertInZone(Zone.DECK, aggressor, borders);
		assertInZone(Zone.DISCARD, fimbul);
		// Shuffled as the Spider leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMinionInTheDrawDeckTheShadowActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fimbul = scn.GetShadowCard("fimbul");
		var borders = scn.GetShadowCard("borders");

		scn.StartGame();
		scn.MoveMinionsToTable(fimbul);
		scn.SetShadowDrawDeck(borders);
		scn.SkipToPhase(Phase.SHADOW);
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();

		assertInZone(Zone.SHADOW_CHARACTERS, fimbul);
		assertInZone(Zone.DECK, borders);
		int shuffles = scn.GetShadowDeckShuffleCount();
		// A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// Fimbul is not discarded for a look that finds nothing.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fimbul));
		assertInZone(Zone.SHADOW_CHARACTERS, fimbul);
		assertInZone(Zone.DECK, borders);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
	}

	@Test
	public void ShadowActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fimbul = scn.GetShadowCard("fimbul");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.MoveMinionsToTable(fimbul);
		scn.SetShadowDrawDeck(aggressor);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertInZone(Zone.SHADOW_CHARACTERS, fimbul);
		assertInZone(Zone.DECK, aggressor);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fimbul));
	}
}
