package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_046_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pandemonium", "17_46");
					put("hillsman", "17_50");   // Stampeding Hillsman: [men] Man, cost 2, site 4; a regroup ability only
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard), cost 4, site 4.
					// "Each [men] minion bearing a possession is strength +2"; a skirmish ability never used here.
					put("saruman", "17_48");
					put("savage", "17_53");     // Stampeding Savage: [men] Man, cost 3; a maneuver ability only
					put("armor", "17_41");      // Ceremonial Armor: [men] possession in the support area, which stacks [men] minions
					put("oath", "17_66");       // Wildman's Oath: [men] possession in the support area, which stacks [men] minions
					// Pass of the Spider: a [gollum] possession in the support area, which stacks any minion from play
					// ("Skirmish: If Shelob is skirmishing, stack another minion from play here ...").
					put("pass", "103_12");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PandemoniumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Pandemonium
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Play a [men] minion stacked on a [men] possession as if from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("pandemonium");

		assertEquals("Pandemonium", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysAMenMinionStackedOnAMenPossessionAndNotOneStackedElsewhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pandemonium = scn.GetShadowCard("pandemonium");
		var hillsman = scn.GetShadowCard("hillsman");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var armor = scn.GetShadowCard("armor");
		var oath = scn.GetShadowCard("oath");
		var pass = scn.GetShadowCard("pass");
		scn.MoveCardsToHand(pandemonium);
		scn.MoveCardsToSupportArea(armor, oath, pass);
		scn.StackCardsOn(armor, hillsman);
		scn.StackCardsOn(oath, saruman);
		scn.StackCardsOn(pass, savage);
		assertInHand(pandemonium);
		assertInZone(Zone.STACKED, hillsman, saruman, savage);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(pandemonium));
		scn.ShadowPlayCard(pandemonium);

		// A [men] Man and a [men] Wizard, each on a [men] possession of its own; not a [men] Man stacked on a
		// possession of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hillsman, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertInZone(Zone.STACKED, hillsman, savage);
		assertTrue(scn.GetStackedCards(armor).contains(hillsman));
		assertTrue(scn.GetStackedCards(pass).contains(savage));
		assertInDiscard(pandemonium);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithTheMenMinionInTheDiscardPileAndNoneStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pandemonium = scn.GetShadowCard("pandemonium");
		var hillsman = scn.GetShadowCard("hillsman");
		var armor = scn.GetShadowCard("armor");
		scn.MoveCardsToHand(pandemonium);
		scn.MoveCardsToSupportArea(armor);
		scn.MoveCardsToDiscard(hillsman);
		assertInHand(pandemonium);
		assertInZone(Zone.SUPPORT, armor);
		assertInDiscard(hillsman);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(pandemonium));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Once the Hillsman lies on the Armor, it can be played.
		scn.StackCardsOn(armor, hillsman);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(pandemonium));
	}

	@Test
	public void TheMinionIsPaidForAsIfFromHandAndOneTwilightShortItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pandemonium = scn.GetShadowCard("pandemonium");
		var hillsman = scn.GetShadowCard("hillsman");
		var armor = scn.GetShadowCard("armor");
		scn.MoveCardsToHand(pandemonium);
		scn.MoveCardsToSupportArea(armor);
		scn.StackCardsOn(armor, hillsman);
		assertInHand(pandemonium);
		assertInZone(Zone.STACKED, hillsman);

		scn.StartGame();
		scn.FreepsPass();

		// The Hillsman costs 2, and 2 more for roaming at site 2 (his site is 4).
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(pandemonium));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(pandemonium));
		scn.ShadowPlayCard(pandemonium);

		assertInZone(Zone.SHADOW_CHARACTERS, hillsman);
		assertInDiscard(pandemonium);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
