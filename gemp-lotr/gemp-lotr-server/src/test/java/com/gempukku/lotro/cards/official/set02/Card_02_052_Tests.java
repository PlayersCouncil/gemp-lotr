package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_052_Tests
{

	// The Fellowship-block path: site 4, Balin's Tomb, is underground; site 2, Ettenmoors, is a plains.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "2_52");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, cost 6, no text
					put("spearman", "2_65");      // Goblin Spearman: [moria] Orc, cost 2; damage +2 only with a Goblin Spear
					put("runner", "1_178");       // Goblin Runner: [moria] Orc; only ever cheated into play
					// Cave Troll of Moria: a [moria] Troll, cost 10, -3 at an underground site; to play, spot a [moria] Orc
					put("cavetroll", "1_165");
					put("chieftain", "1_266");    // Orc Chieftain: [sauron] Orc, cost 2, site 6
					put("pursuer", "2_62");       // Goblin Pursuer: [moria] Orc, cost 2; +3 only after two moves in a turn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path: its site 3, The Dimholt, is underground (the Balrog is discarded anywhere else) and is
	// one short of the site number 4 of every [moria] Orc, so an Orc played there pays the roaming penalty.
	protected VirtualTableScenario GetDimholtScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "2_52");
					// Goblin Sneak: a [moria] Orc of cost 1, site number 4.  "When you play this minion, you may
					// place a [moria] Orc from your discard pile beneath your draw deck."
					put("sneak", "1_181");
					// Goblin Runner: a [moria] Orc of cost 1, site number 4; the other Orc in the discard pile.
					// (Played, it adds (2); it is not played here.)
					put("runner", "1_178");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBalrogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: The Balrog, Flame of Udûn
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 14
		 * Type: Minion
		 * Subtype: Balrog
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 4
		 * Game Text: Damage +1. Fierce. To play, spot a [moria] Orc. Discard The Balrog if not underground.
		 * Shadow: Exert The Balrog and remove (2) to play a [moria] Orc from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balrog");

		assertEquals("The Balrog", card.getBlueprint().getTitle());
		assertEquals("Flame of Udûn", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.BALROG, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(14, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityIsWithheldUntilThePoolPaysTheTwoItRemovesAndTheOrcAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDimholtScenario();

		var balrog = scn.GetShadowCard("balrog");
		var sneak = scn.GetShadowCard("sneak");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		// The move to The Dimholt, site 3: underground.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveMinionsToTable(balrog);
		scn.MoveCardsToDiscard(sneak, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);
		assertEquals(0, scn.GetWoundsOn(balrog));

		// Either Orc costs 1 + 2 for roaming at site 3 = 3 from the discard pile.  A pool of 4 pays the (2)
		// the ability removes, and it pays the Orc, but not one after the other: 2 + 3 = 5.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(balrog));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(balrog));

		scn.ShadowUseCardAction(balrog);
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sneak, runner));

		scn.ShadowChooseCards(sneak);
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertInZone(Zone.DISCARD, runner);
		// 5 - 2 removed - 1 for the Sneak - 2 for roaming
		assertEquals(0, scn.GetTwilight());
		// The Sneak's own offer, to put the Runner beneath the draw deck.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sneak));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAMoriaOrcIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(balrog, troop);

		scn.StartGame();
		scn.SkipToSite(3);
		// The move to Balin's Tomb, site 4: underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		// A [moria] minion who is no Orc, and an Orc who is not [moria].
		scn.MoveMinionsToTable(cavetroll, chieftain);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(balrog));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPlayCard(troop);
		assertTrue(scn.ShadowPlayAvailable(balrog));

		scn.ShadowPlayCard(balrog);
		// Underground, it stays.
		assertInZone(Zone.SHADOW_CHARACTERS, balrog, troop, cavetroll, chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedAwayFromAnUndergroundSiteItIsDiscardedAndTheOrcStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var spearman = scn.GetShadowCard("spearman");
		scn.MoveCardsToHand(balrog);

		scn.StartGame();
		// The move to Ettenmoors, site 2: a plains.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// A [moria] Orc of another title to spot.
		scn.MoveMinionsToTable(spearman);
		// 14 + 2 for roaming
		scn.SetTwilight(16);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(balrog));

		scn.ShadowPlayCard(balrog);
		assertInDiscard(balrog);
		assertInZone(Zone.SHADOW_CHARACTERS, spearman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityPlaysOnlyAMoriaOrcFromItsOwnersDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var spearman = scn.GetShadowCard("spearman");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");
		// A [moria] Orc in the other player's discard pile, of a title in none of the Shadow player's piles.
		var pursuer = scn.GetFreepsCard("pursuer");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		// The Runner is the [moria] Orc that lets the Cave Troll be played.
		scn.MoveMinionsToTable(balrog, runner);
		scn.MoveCardsToDiscard(troop, spearman, cavetroll, chieftain, pursuer);
		// Each card in the pile is paid for after the (2): the Troop 6, the Spearman 2, the Cave Troll 10 - 3
		// underground = 7, the Orc Chieftain 2 + 2 roaming = 4.
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertTrue(scn.ShadowActionAvailable(balrog));

		scn.ShadowUseCardAction(balrog);
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, spearman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cavetroll, chieftain, pursuer));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCards(spearman);
		assertInZone(Zone.SHADOW_CHARACTERS, spearman);
		assertInDiscard(troop, cavetroll, chieftain, pursuer);
		// 10 - 2 removed - 2 for the Spearman
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheBalrogIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.MoveMinionsToTable(balrog);
		// Vitality 5 with 4 wounds.
		scn.AddWoundsToChar(balrog, 4);
		scn.MoveCardsToDiscard(troop);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertFalse(scn.ShadowActionAvailable(balrog));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedUntilAMoriaOrcIsInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		scn.MoveMinionsToTable(balrog, runner);
		// Payable, but no [moria] Orc: the play is part of the cost (Q-0062).
		scn.MoveCardsToDiscard(cavetroll, chieftain);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(balrog));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.MoveCardsToDiscard(troop);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(balrog));
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
