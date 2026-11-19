package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("evil", "2_41");

					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, vitality 2, damage +1 and nothing else
					put("retainer", "3_57");    // Isengard Retainer: [isengard] Orc, no Uruk-hai; a regroup ability only used on purpose

					// For the Shadow player's discard pile
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc minion
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion with no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion
					put("fallen", "10_84");     // a second Cirith Ungol Sentry: [sauron] Uruk-hai, the one minion in the pile that is no Orc
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] condition, no minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion, no minion (the Shadow player's own copy)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EvilAfootStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Evil Afoot
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Exert an Uruk-hai and spot X burdens to shuffle X minions from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("evil");

		assertEquals("Evil Afoot", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUrukhaiAndSpotsTwoBurdensToShuffleTwoMinionsFromTheDiscardPileIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var raiding = scn.GetShadowCard("raiding");
		var sentry = scn.GetShadowCard("sentry");
		var retainer = scn.GetShadowCard("retainer");
		var forger = scn.GetShadowCard("forger");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var fallen = scn.GetShadowCard("fallen");
		var ambition = scn.GetShadowCard("ambition");
		var aragorn = scn.GetShadowCard("aragorn");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(raiding, sentry, retainer);
		scn.MoveCardsToDiscard(forger, troop, runner, fallen, ambition, aragorn);
		assertInDiscard(forger, troop, runner, fallen, ambition, aragorn);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);

		// An Uruk-hai of [isengard] and one of [sauron]; the [isengard] Orc is not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiding, sentry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(retainer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(raiding));
		scn.ShadowChooseCard(raiding);
		assertEquals(1, scn.GetWoundsOn(raiding));
		assertEquals(0, scn.GetWoundsOn(sentry));

		// X is the Shadow player's count, up to the 2 burdens there are, though there are 4 minions to shuffle.
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// Minions of three cultures and two races, the Uruk-hai among them; the condition and the companion in the
		// pile are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(forger, troop, runner, fallen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ambition, aragorn));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(fallen, troop);

		assertInZone(Zone.DECK, fallen, troop);
		assertInDiscard(forger, runner, ambition, aragorn, evil);
		assertEquals(deck + 2, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		// The burdens are spotted, not removed.
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingOneBurdenShufflesOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var raiding = scn.GetShadowCard("raiding");
		var forger = scn.GetShadowCard("forger");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(raiding);
		scn.MoveCardsToDiscard(forger, troop);
		assertInDiscard(forger, troop);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPlayCard(evil);
		assertEquals(1, scn.GetWoundsOn(raiding));
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(troop);

		assertInZone(Zone.DECK, troop);
		assertInDiscard(forger, evil);
		assertEquals(deck + 1, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoMinionInTheDiscardPileItIsPlayedAndTheDeckShuffledForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var raiding = scn.GetShadowCard("raiding");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(raiding);
		scn.MoveCardsToDiscard(ambition);
		assertInDiscard(ambition);
		scn.FreepsPass();

		int deck = scn.GetShadowDeckCount();
		int shuffles = scn.GetShadowDeckShuffleCount();
		// An event's target is no requirement.
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);
		assertEquals(1, scn.GetWoundsOn(raiding));
		scn.ShadowChoose("2");

		assertInDiscard(ambition, evil);
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyUrukhaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var evil = scn.GetShadowCard("evil");
		var raiding = scn.GetShadowCard("raiding");
		var retainer = scn.GetShadowCard("retainer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(raiding, retainer);
		scn.AddWoundsToChar(raiding, 2);
		assertEquals(2, scn.GetWoundsOn(raiding));
		scn.MoveCardsToDiscard(troop);
		assertInDiscard(troop);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds; the Isengard Retainer could exert, but is not an Uruk-hai.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(evil));
	}
}
