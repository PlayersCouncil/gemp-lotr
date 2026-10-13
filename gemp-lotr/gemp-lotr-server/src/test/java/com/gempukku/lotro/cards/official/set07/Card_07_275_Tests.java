package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_275_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager1", "7_275");
					put("pillager2", "7_275");
					put("troop", "7_279");      // Gorgoroth Troop: unique besieger Orc, strength 14; a skirmish ability used only on purpose
					put("garrison", "7_273");   // Gorgoroth Garrison: besieger Orc; "Regroup: Discard 2 cards from hand to stack a [sauron] Orc on a site you control (or discard 1 card from hand if that Orc is a besieger)."
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc who is no besieger; his text answers a weapon played on him
					put("uruk", "4_180");       // Uruk Besieger: an [isengard] Uruk-hai with the word in his title and not the keyword; damage +1, and abilities that stack him and play him from a site
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support-area condition that play stacks a [sauron] Orc on
					put("runner", "1_178");     // Goblin Runner: a card in hand to discard for the Garrison
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Pillager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br>While this minion is stacked on a site you control, besiegers are <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pillager1");

		assertEquals("Gorgoroth Pillager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedFromHandForFourAndTwoForRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager1");
		scn.MoveCardsToHand(pillager);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for site 2
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(pillager));

		scn.ShadowPlayCard(pillager);

		// 4 + 2 for roaming: site number 5 at site 2
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, pillager);
		// In play and not stacked, its own text does nothing for it.
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BesiegersBecomeFierceWhenAPillagerIsStackedOnASiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager1");
		var other = scn.GetShadowCard("pillager2");
		var garrison = scn.GetShadowCard("garrison");
		var chieftain = scn.GetShadowCard("chieftain");
		var uruk = scn.GetShadowCard("uruk");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.SetShadowHand(runner);
		scn.MoveMinionsToTable(pillager, other, garrison, chieftain, uruk);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowControls(site1));
		// A Pillager in play makes nobody fierce, though its owner controls a site.
		assertFalse(scn.HasKeyword(pillager, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(other, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(garrison, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(garrison, "Stack a besieger"));

		// The one card in hand is discarded, and one site is controlled: only the Orc to stack is chosen.
		scn.ShadowUseCardAction(garrison, "Stack a besieger");
		scn.ShadowChooseCard(pillager);

		assertInZone(Zone.STACKED, pillager);
		assertTrue(scn.GetStackedCards(site1).contains(pillager));
		// The besiegers left in play are fierce, the second Pillager among them.
		assertTrue(scn.HasKeyword(other, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(garrison, Keyword.FIERCE));
		// A [sauron] Orc who is no besieger and an Uruk-hai who is one in name only are not.
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(uruk, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StackedOnAConditionItMakesNoBesiegerFierceAndOnAControlledSiteItDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager1");
		var other = scn.GetShadowCard("pillager2");
		var troop = scn.GetShadowCard("troop");
		var chieftain = scn.GetShadowCard("chieftain");
		var teeth = scn.GetShadowCard("teeth");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, pillager);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, chieftain);

		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.STACKED, pillager);
		assertTrue(scn.GetStackedCards(teeth).contains(pillager));
		// Stacked, but on no site.
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		scn.StackCardsOn(site1, other);

		assertTrue(scn.GetStackedCards(site1).contains(other));
		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void APillagerStackedOnItsOwnersSiteDoesNothingOnItsOwnersOwnTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theirPillager = scn.GetFreepsCard("pillager1");
		var pillager = scn.GetShadowCard("pillager1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// Each player controls a site, and the player whose fellowship is moving has a Pillager of his own on his.
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.StackCardsOn(site2, theirPillager);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);

		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertTrue(scn.GetStackedCards(site2).contains(theirPillager));
		// His Shadow cards do nothing on his own turn: the other player's besieger is not fierce for it.
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		scn.StackCardsOn(site1, pillager);

		assertTrue(scn.HasKeyword(troop, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
