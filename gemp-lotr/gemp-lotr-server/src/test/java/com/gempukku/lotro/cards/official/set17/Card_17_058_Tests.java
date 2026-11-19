package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sneak", "17_58");

					put("armor", "17_41");      // Ceremonial Armor: [men] possession played to the support area; stacks a [men] minion discarded in the maneuver phase
					put("oath", "17_66");       // Wildman's Oath: [men] possession played to the support area; the same trigger, and a skirmish ability never used here
					put("brand", "17_47");      // Primitive Brand: [men] possession, a hand weapon borne by a [men] minion
					// Hides: a [dunland] possession in the support area.  Ted Sandyman 18_75's regroup ability stacks a [men] Man
					// on any possession, so a [men] minion can lie on it in a game.  Its own texts answer its play and [dunland] wounds.
					put("hides", "4_19");
					put("corps", "11_73");      // Corps of Harad: [men] Man, to bear the Brand; fierce only at a battleground
					put("haradrim", "11_74");   // Detachment of Haradrim: a [men] minion to lie stacked on a possession
					put("hillsman", "17_50");   // Stampeding Hillsman: a [men] minion to lie stacked on a possession
					put("hillsman2", "17_50");
					put("saruman", "17_48");    // Saruman, Coldly Still: a [men] minion who is no Man, to lie stacked on a possession
					put("troop", "1_177");      // Goblin Patrol Troop: a minion of cost 6 with no text, read in hand beside the Sneak
					put("pandemonium", "17_46");// Pandemonium: Shadow event, cost 0, plays a [men] minion stacked on a [men] possession as if from hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SunlandSneakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Sunland Sneak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 14
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: This minion is twilight cost -1 for each [men] minion stacked on a [men] possession.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("sneak");

		assertEquals("Sunland Sneak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TwilightCostIsMinusOneForEachMenMinionStackedOnAMenPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var troop = scn.GetShadowCard("troop");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var brand = scn.GetShadowCard("brand");
		var hides = scn.GetShadowCard("hides");
		var corps = scn.GetShadowCard("corps");
		var hillsman = scn.GetShadowCard("hillsman");
		var hillsman2 = scn.GetShadowCard("hillsman2");
		var haradrim = scn.GetShadowCard("haradrim");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(sneak, troop);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, brand);
		scn.MoveCardsToSupportArea(armor, oath, hides);
		assertInHand(sneak, troop);
		assertAttachedTo(brand, corps);
		assertInZone(Zone.SUPPORT, armor, oath, hides);

		// Three [men] possessions with nothing stacked on them.
		assertEquals(0, scn.GetStackedCards(armor).size());
		assertEquals(0, scn.GetStackedCards(oath).size());
		assertEquals(0, scn.GetStackedCards(brand).size());
		assertEquals(6, scn.GetTwilightCost(sneak));
		assertEquals(6, scn.GetTwilightCost(troop));

		// A [men] minion on a [dunland] possession does not count.
		scn.StackCardsOn(hides, hillsman);
		assertEquals(6, scn.GetTwilightCost(sneak));

		scn.StackCardsOn(armor, hillsman2);
		assertEquals(5, scn.GetTwilightCost(sneak));
		// Only the Sneak is made cheaper.
		assertEquals(6, scn.GetTwilightCost(troop));

		// A second minion on the same possession counts as well.
		scn.StackCardsOn(armor, saruman);
		assertEquals(4, scn.GetTwilightCost(sneak));

		// So does one on the Brand, borne by a minion and in no support area.
		scn.StackCardsOn(brand, haradrim);
		assertEquals(3, scn.GetTwilightCost(sneak));
		assertEquals(6, scn.GetTwilightCost(troop));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedFromHandForItsLoweredCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var armor = scn.GetShadowCard("armor");
		var hillsman = scn.GetShadowCard("hillsman");
		var hillsman2 = scn.GetShadowCard("hillsman2");
		var haradrim = scn.GetShadowCard("haradrim");
		scn.MoveCardsToHand(sneak);
		scn.MoveCardsToSupportArea(armor);
		scn.StackCardsOn(armor, hillsman, hillsman2, haradrim);

		scn.StartGame();
		assertInHand(sneak);
		assertInZone(Zone.SUPPORT, armor);
		assertEquals(3, scn.GetStackedCards(armor).size());
		scn.SkipToSite(3);
		scn.FreepsPass();

		// The Shadow phase at Balin's Tomb, site 4: the Sneak is not roaming.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilightCost(sneak));
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(sneak));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(sneak));
		scn.ShadowPlayCard(sneak);

		// 3 in the pool - (6 - 3 minions stacked)
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetStackedCards(armor).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedFromAStackItDoesNotCountItselfForItsOwnDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sneak = scn.GetShadowCard("sneak");
		var armor = scn.GetShadowCard("armor");
		var hillsman = scn.GetShadowCard("hillsman");
		var pandemonium = scn.GetShadowCard("pandemonium");
		scn.MoveCardsToHand(pandemonium);
		scn.MoveCardsToSupportArea(armor);
		scn.StackCardsOn(armor, sneak, hillsman);

		scn.StartGame();
		assertInHand(pandemonium);
		assertInZone(Zone.SUPPORT, armor);
		assertInZone(Zone.STACKED, sneak, hillsman);
		scn.SkipToSite(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(pandemonium);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sneak, hillsman));
		scn.ShadowChooseCard(sneak);

		// As it is played the Sneak goes from the stack into the void (CR 4.2 "playing a card"), and only the
		// Hillsman is left stacked: 5 in the pool - (6 - 1).
		assertInZone(Zone.SHADOW_CHARACTERS, sneak);
		assertInZone(Zone.STACKED, hillsman);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
