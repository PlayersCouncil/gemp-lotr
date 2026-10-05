package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("purpose", "101_31");

					// Three kinds of tentacle, all [moria] minions of site 4.
					// Vile Tentacle: cost 2.  Weaker away from a marsh; its Shadow ability is never used here.
					put("vile", "101_36");
					// Reaching Tentacle: cost 2.  Discarded if not at a marsh; stronger for each tentacle.
					put("reaching", "19_19");
					// Strong Tentacle: cost 3.  The same text as Reaching Tentacle, and nothing when played.
					put("strong", "19_20");

					// Goblin Patrol Troop: a [moria] minion with no text, and not a tentacle.
					put("troop", "1_177");
					// Goblin Scimitar: a [moria] possession.  Only ever in a discard pile here.
					put("scimitar", "1_180");
					// Uruk Savage: an [isengard] minion of site 5; damage +1 is its whole text.
					put("savage", "1_151");
					// Goblin Runner: a [moria] minion.  Only ever in a discard pile or a draw deck here.
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuidedbyOnePurposeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Guided by One Purpose
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Spot 3 tentacles to add a burden.  Then you may make the Free Peoples player discard a
		 * tentacle to shuffle a [moria] card from your discard pile into your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("purpose");

		assertEquals("Guided by One Purpose", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithTwoTentaclesInPlayAndCanOnceAThirdIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var purpose = scn.GetShadowCard("purpose");
		var vile = scn.GetShadowCard("vile");
		var reaching = scn.GetShadowCard("reaching");
		var strong = scn.GetShadowCard("strong");
		var troop = scn.GetShadowCard("troop");
		// The third tentacle is in hand, where it cannot be spotted.
		scn.MoveCardsToHand(purpose, strong);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		// Two tentacles, and a third [moria] minion that is not one.
		scn.MoveMinionsToTable(vile, reaching, troop);
		scn.SetTwilight(3);
		scn.FreepsPass();

		// 3 set + 1 companion + 2 for site 2
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, vile, reaching, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(purpose));

		scn.ShadowPlayCard(strong);

		// 3 to play + 2 roaming: a minion of site 4 at site 2.  What is left pays for the event.
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, strong);
		assertTrue(scn.ShadowPlayAvailable(purpose));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AddsABurdenThenTheFreePeoplesPlayerDiscardsATentacleToShuffleAMoriaCardFromDiscardIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var purpose = scn.GetShadowCard("purpose");
		var vile = scn.GetShadowCard("vile");
		var reaching = scn.GetShadowCard("reaching");
		var strong = scn.GetShadowCard("strong");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var freepsRunner = scn.GetFreepsCard("runner");
		scn.MoveCardsToHand(purpose);
		// In the Shadow player's discard pile: a [moria] card and a Shadow card of another culture.
		scn.MoveCardsToDiscard(scimitar, savage);
		// A [moria] card in the other player's discard pile.  The Shadow player's own copy stays in his draw deck.
		scn.MoveCardsToDiscard(freepsRunner);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		// Exactly three tentacles, and a [moria] minion that is not one.
		scn.MoveMinionsToTable(vile, reaching, strong, troop);
		scn.FreepsPass();

		// 1 companion + 2 for site 2
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetShadowDiscardCount());
		// Only the Shadow player's Goblin Runner is left in his draw deck.
		assertEquals(1, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, runner);
		assertInZone(Zone.HAND, purpose);
		assertTrue(scn.ShadowPlayAvailable(purpose));

		scn.ShadowPlayCard(purpose);

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertNotInZone(Zone.HAND, purpose);
		// The rest is the Shadow player's to take or leave.
		assertTrue(scn.ShadowDecisionAvailable("Would you like to make the Free Peoples player discard a tentacle"));
		assertInZone(Zone.SHADOW_CHARACTERS, vile, reaching, strong);
		scn.ShadowChooseYes();

		// It is the Free Peoples player who picks, from the tentacles and nothing else.
		assertTrue(scn.FreepsHasCardChoiceAvailable(vile, reaching, strong));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		scn.FreepsChooseCard(reaching);

		assertInZone(Zone.DISCARD, reaching);
		assertInZone(Zone.SHADOW_CHARACTERS, vile, strong, troop);

		// The [moria] cards in the Shadow player's discard pile, the tentacle just discarded among them.  Not the
		// [isengard] minion, not the Free Peoples player's Goblin Runner, and not the event, which is not in the
		// discard pile until it has finished being played.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scimitar, reaching));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, freepsRunner, purpose));
		int shuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.ShadowChooseCardBPFromSelection(scimitar);

		assertInZone(Zone.DECK, scimitar);
		assertEquals(2, scn.GetShadowDeckCount());
		// Shuffled in, and into his own deck only.
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		// The Uruk Savage, the Reaching Tentacle and now the event.
		assertInZone(Zone.DISCARD, savage, reaching, purpose, freepsRunner);
		assertEquals(3, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TentacleOfAnotherCultureCountsAsATentacleAndNotAsAMoriaCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var purpose = scn.GetShadowCard("purpose");
		var vile = scn.GetShadowCard("vile");
		var reaching = scn.GetShadowCard("reaching");
		var savage = scn.GetShadowCard("savage");
		var scimitar = scn.GetShadowCard("scimitar");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(purpose);
		scn.MoveCardsToDiscard(scimitar, runner);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		scn.MoveMinionsToTable(vile, reaching, savage);
		// Every tentacle there is is a [moria] card.  The Uruk Savage is made a third tentacle, so that the text's
		// "tentacle" and its "[moria] card" can be told apart.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.TENTACLE));
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(purpose));

		scn.ShadowPlayCard(purpose);

		assertEquals(1, scn.GetBurdens());
		scn.ShadowChooseYes();

		assertTrue(scn.FreepsHasCardChoiceAvailable(vile, reaching, savage));
		scn.FreepsChooseCard(savage);

		assertInZone(Zone.DISCARD, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, vile, reaching);

		// The discarded Uruk Savage is in the same discard pile as the two [moria] cards and is not one of them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scimitar, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		scn.ShadowChooseCardBPFromSelection(runner);

		assertInZone(Zone.DECK, runner);
		assertInZone(Zone.DISCARD, scimitar, savage, purpose);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningAfterTheBurdenDiscardsNoTentacleAndShufflesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var purpose = scn.GetShadowCard("purpose");
		var vile = scn.GetShadowCard("vile");
		var reaching = scn.GetShadowCard("reaching");
		var strong = scn.GetShadowCard("strong");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCardsToHand(purpose);
		scn.MoveCardsToDiscard(scimitar);

		scn.StartGame();
		// Every site is a marsh, so that the tentacles which are discarded away from one stay in play.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.SITE, null, Keyword.MARSH));
		scn.MoveMinionsToTable(vile, reaching, strong);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetShadowDiscardCount());
		// The Goblin Patrol Troop, the Uruk Savage and the Goblin Runner.
		assertEquals(3, scn.GetShadowDeckCount());

		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPlayCard(purpose);

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowDecisionAvailable("Would you like to make the Free Peoples player discard a tentacle"));
		scn.ShadowChooseNo();

		// The burden stays; nobody is asked anything more.
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, vile, reaching, strong);
		assertInZone(Zone.DISCARD, scimitar, purpose);
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(3, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
