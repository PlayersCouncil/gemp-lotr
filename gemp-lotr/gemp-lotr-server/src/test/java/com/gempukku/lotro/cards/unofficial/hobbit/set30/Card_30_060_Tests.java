package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_060_Tests
{

	// The Hobbit table, with Rhudaur as site 3 in place of Rivendell: the turn begins at The Troll Hoard (site 2)
	// and the Shadow phase after the fellowship's move is at Rhudaur, the Shadow player's own copy.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Wicked Spider: [spider] Spider minion printed fierce, vitality 2.  A skirmish ability that
					// discards an Orc, never used.
					put("wicked", "31_55");
					// Gangsquad Spies: [men] Man minion printed fierce, vitality 3.  The site number of each [men]
					// Man is -3; a maneuver ability never reached.
					put("spies", "33_26");
					// Orkish Aggressor: [gundabad] Orc minion who is not fierce, vitality 2.  An assignment
					// ability never reached.
					put("aggressor", "30_39");
					// Hatred Rekindled: [gundabad] condition, cost 3, support area; a Shadow ability never used.
					put("hatred", "30_34");
					// Jail: [sauron] condition, cost 2, support area; a regroup ability never reached.
					put("jail", "32_39");
					// Warg: [gundabad] possession (a mount), cost 2, bearer a [gundabad] Orc, which the Aggressor is.
					put("warg", "31_27");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site3", "30_60");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void RhudaurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Rhudaur
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 3
		 * Shadow Number: 1
		 * Direction: none
		 * Game Text: <b>Plains</b>. <b>Sanctuary</b>. <b>Shadow:</b> Exert your fierce minion to play a condition from your draw deck (limit once per turn).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Rhudaur", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void ShadowAbilityExertsAFierceMinionToPlayAConditionFromTheDrawDeckOnceThisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wicked = scn.GetShadowCard("wicked");
		var spies = scn.GetShadowCard("spies");
		var aggressor = scn.GetShadowCard("aggressor");
		var hatred = scn.GetShadowCard("hatred");
		var jail = scn.GetShadowCard("jail");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		// The Troll Hoard makes no offer at the start of this turn: no hand weapon in the deck (F-0800).
		scn.SkipToSite(2);
		// The turns on the way drew the small deck into the hand: these go back.
		scn.MoveCardsToTopOfDeck(hatred, jail, warg);
		scn.MoveMinionsToTable(wicked, spies, aggressor);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, hatred, jail, warg);
		assertEquals(0, scn.GetWoundsOn(wicked));
		assertEquals(0, scn.GetWoundsOn(spies));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals("30_60", scn.GetCurrentSite().getBlueprintId());
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(scn.GetCurrentSite()));

		scn.ShadowUseCardAction(scn.GetCurrentSite());
		// Fierce minions of two cultures and races; the Aggressor, who could exert, is not fierce.
		assertTrue(scn.ShadowHasCardChoiceAvailable(wicked, spies));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aggressor));
		scn.ShadowChooseCard(wicked);

		assertEquals(1, scn.GetWoundsOn(wicked));
		assertEquals(0, scn.GetWoundsOn(spies));
		assertEquals(0, scn.GetWoundsOn(aggressor));

		scn.ShadowDismissRevealedCards();
		// Conditions of two cultures; the Warg, which the Aggressor could bear, is no condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hatred, jail));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warg));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(jail);

		assertInZone(Zone.SUPPORT, jail);
		assertInZone(Zone.DECK, hatred, warg);
		// Shuffled as the condition leaves the deck that was looked through, and again once the look is over;
		// the other player's deck is not.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());

		// Once this turn: the Spies could still exert and Hatred Rekindled is still in the deck.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
	}

	@Test
	public void TheLimitIsSpentForTheTurnAndOnTheNextTurnTheOtherPlayerUsesTheSameRhudaur() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wicked = scn.GetShadowCard("wicked");
		var spies = scn.GetShadowCard("spies");
		var hatred = scn.GetShadowCard("hatred");
		var jail = scn.GetShadowCard("jail");
		var myWicked = scn.GetFreepsCard("wicked");
		var myJail = scn.GetFreepsCard("jail");

		scn.StartGame();
		// The Troll Hoard makes no offer at the start of this turn: no hand weapon in the deck (F-0800).
		scn.SkipToSite(2);
		scn.MoveCardsToTopOfDeck(hatred, jail);
		scn.MoveMinionsToTable(wicked, spies);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, hatred, jail);
		scn.SkipToPhase(Phase.SHADOW);

		var rhudaur = scn.GetCurrentSite();
		assertEquals("30_60", rhudaur.getBlueprintId());
		scn.ShadowUseCardAction(rhudaur);
		scn.ShadowChooseCard(spies);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(jail);
		assertInZone(Zone.SUPPORT, jail);
		assertEquals(1, scn.GetWoundsOn(spies));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rhudaur));

		// The minions are taken off so that the rest of the turn has no skirmishes for Bilbo to lose.
		scn.MoveCardsToDiscard(wicked, spies);
		// Nor to the second player at the start of his turn.
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship, at site 2, moves to the same Rhudaur; the first player is the Shadow
		// player there, with his own fierce minion and a condition on top of his deck.
		scn.MoveCardsToTopOfDeck(myJail);
		scn.MoveMinionsToTable(myWicked);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, myJail);
		assertEquals(0, scn.GetWoundsOn(myWicked));
		scn.ShadowPass();

		assertSame(rhudaur, scn.GetCurrentSite());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertTrue(scn.FreepsActionAvailable(rhudaur));

		scn.FreepsUseCardAction(rhudaur);
		assertEquals(1, scn.GetWoundsOn(myWicked));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(myJail);

		assertInZone(Zone.SUPPORT, myJail);
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void WithNoConditionInTheDrawDeckTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wicked = scn.GetShadowCard("wicked");
		var aggressor = scn.GetShadowCard("aggressor");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		// The Troll Hoard makes no offer at the start of this turn: no hand weapon in the deck (F-0800).
		scn.SkipToSite(2);
		scn.SetShadowDrawDeck(warg);
		scn.MoveMinionsToTable(wicked, aggressor);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, warg);
		assertEquals(0, scn.GetWoundsOn(wicked));
		scn.SkipToPhase(Phase.SHADOW);

		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// A possession the Aggressor could bear, and no condition.  A Hobbit-set play from the draw deck with nothing it could play is withheld (Q-0019; the orchestrator on Q-0406 and Q-0418):
		// the Spider does not exert for a look that finds nothing.
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
		assertEquals(0, scn.GetWoundsOn(wicked));
		assertInZone(Zone.DECK, warg);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheOnlyFierceMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wicked = scn.GetShadowCard("wicked");
		var aggressor = scn.GetShadowCard("aggressor");
		var jail = scn.GetShadowCard("jail");

		scn.StartGame();
		// The Troll Hoard makes no offer at the start of this turn: no hand weapon in the deck (F-0800).
		scn.SkipToSite(2);
		scn.MoveCardsToTopOfDeck(jail);
		scn.MoveMinionsToTable(wicked, aggressor);
		// Vitality 2 with 1 wound: the Spider cannot exert.  The Aggressor can, and is not fierce.
		scn.AddWoundsToChar(wicked, 1);
		scn.SetTwilight(10);
		assertInZone(Zone.DECK, jail);
		assertEquals(1, scn.GetWoundsOn(wicked));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		scn.SkipToPhase(Phase.SHADOW);

		assertEquals("30_60", scn.GetCurrentSite().getBlueprintId());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
	}
}
