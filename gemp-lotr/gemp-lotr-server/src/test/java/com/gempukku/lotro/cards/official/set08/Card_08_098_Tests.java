package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("looter", "8_98");
					put("looter2", "8_98");
					put("pillager", "7_275");   // Gorgoroth Pillager: besieger Orc, cost 4, site 5; its text works only while it is stacked
					put("troll", "8_102");      // Great Hill Troll: a besieger who is no Orc, cost 9, site 5; its trigger is for sites 5K to 9K
					put("assassin", "1_262");   // Orc Assassin: a [sauron] Orc who is no besieger; an assignment ability that needs 2 Hobbit companions
					put("uruk", "4_180");       // Uruk Besieger: "Besieger" in the title only; his own regroup action stacks him on a controlled site
					put("soldier", "7_278");    // Gorgoroth Soldier: besieger Orc; a response that needs 2 cards in hand
					put("teeth", "5_110");      // Teeth of Mordor: [sauron] support-area condition that play stacks a [sauron] Orc on
					put("garrison", "7_273");   // Gorgoroth Garrison: besieger Orc, cost 3, site 5; regroup abilities only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothLooterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Gorgoroth Looter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br>When you play this minion, you may remove a burden to play a besieger stacked on a site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("looter");

		assertEquals("Gorgoroth Looter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedItOffersToRemoveABurdenToPlayABesiegerStackedOnASiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var looter = scn.GetShadowCard("looter");
		var looter2 = scn.GetShadowCard("looter2");
		var pillager = scn.GetShadowCard("pillager");
		var troll = scn.GetShadowCard("troll");
		var assassin = scn.GetShadowCard("assassin");
		var uruk = scn.GetShadowCard("uruk");
		var soldier = scn.GetShadowCard("soldier");
		var teeth = scn.GetShadowCard("teeth");
		var garrison = scn.GetShadowCard("garrison");
		var theirPillager = scn.GetFreepsCard("pillager");

		scn.StartGame();
		scn.SkipToSite(4);
		// Site 1 is the other player's, with a besieger of his own on it; sites 2 and 3 are the Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// Besiegers of two titles and races on his two sites, each beside a minion who is none; a besieger stacked
		// on a condition, and one in hand.
		scn.StackCardsOn(site1, theirPillager);
		scn.StackCardsOn(site2, pillager, assassin);
		scn.StackCardsOn(site3, troll, uruk);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, soldier);
		scn.MoveMinionsToTable(looter2);
		scn.SetShadowHand(looter, garrison);
		scn.AddBurdens(2);
		scn.SetTwilight(20);
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.STACKED, theirPillager, pillager, assassin, troll, uruk, soldier);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.STACKED, theirPillager, pillager, assassin, troll, uruk, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(looter);

		// Its own play only: the Looter already in play offers nothing.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter2));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasCardChoiceAvailable(pillager, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(assassin, uruk, soldier, theirPillager, garrison));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troll);

		assertInZone(Zone.SHADOW_CHARACTERS, troll, looter, looter2);
		assertInZone(Zone.STACKED, theirPillager, pillager, assassin, uruk, soldier);
		assertInHand(garrison);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedItLeavesTheBurdenAndTheStackAndABesiegerPlayedAfterItIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var looter = scn.GetShadowCard("looter");
		var pillager = scn.GetShadowCard("pillager");
		var garrison = scn.GetShadowCard("garrison");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.SetShadowHand(looter, garrison);
		scn.AddBurdens(1);
		scn.SetTwilight(20);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, pillager);
		scn.FreepsPass();

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, pillager);
		scn.ShadowPlayCard(looter);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, pillager);
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another besieger played with the Looter in play, a burden and a besieger on the site: nothing is offered.
		scn.ShadowPlayCard(garrison);
		assertInZone(Zone.SHADOW_CHARACTERS, garrison);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, pillager);
	}

	@Test
	public void WithNoBurdenToRemoveItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var looter = scn.GetShadowCard("looter");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.SetShadowHand(looter);
		scn.SetTwilight(20);
		assertInZone(Zone.STACKED, pillager);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.STACKED, pillager);
		scn.ShadowPlayCard(looter);

		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter));
		assertInZone(Zone.STACKED, pillager);
	}

	@Test
	public void WithNoBesiegerStackedOnASiteItsOwnerControlsItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var looter = scn.GetShadowCard("looter");
		var assassin = scn.GetShadowCard("assassin");
		var uruk = scn.GetShadowCard("uruk");
		var soldier = scn.GetShadowCard("soldier");
		var teeth = scn.GetShadowCard("teeth");
		var theirPillager = scn.GetFreepsCard("pillager");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// The other player's besieger on the site he controls; minions who are no besiegers on the Shadow player's
		// site; a besieger on a condition.
		scn.StackCardsOn(site1, theirPillager);
		scn.StackCardsOn(site2, assassin, uruk);
		scn.MoveCardsToSupportArea(teeth);
		scn.StackCardsOn(teeth, soldier);
		scn.SetShadowHand(looter);
		scn.AddBurdens(1);
		scn.SetTwilight(20);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, theirPillager, assassin, uruk, soldier);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, theirPillager, assassin, uruk, soldier);
		scn.ShadowPlayCard(looter);

		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, theirPillager, assassin, uruk, soldier);
	}

	@Test
	public void WithThePoolOneShortOfTheBesiegerOnTheSiteItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var looter = scn.GetShadowCard("looter");
		var looter2 = scn.GetShadowCard("looter2");
		var pillager = scn.GetShadowCard("pillager");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.StackCardsOn(site1, pillager);
		scn.SetShadowHand(looter, looter2);
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, pillager);
		scn.FreepsPass();

		// At site 5 neither minion roams: the Looter costs 3 and the Pillager on the site 4.
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();
		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.STACKED, pillager);
		scn.ShadowPlayCard(looter);

		// 6 - 3: one short of the Pillager.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter));
		assertEquals(1, scn.GetBurdens());

		// One more in the pool, and the second Looter's play is answered.
		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(looter2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter2));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, pillager, looter, looter2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
