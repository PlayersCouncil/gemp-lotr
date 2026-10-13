package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breeder", "18_87");
					put("breeder2", "18_87");
					put("breeder3", "18_87");
					// Tracking Uruk: [uruk-hai] Uruk-hai, not unique, cost 5, site 5; Hunter 1 is its whole text
					put("tracker", "15_171");
					put("tracker2", "15_171");
					// Ugluk, Ugly Fellow: unique [uruk-hai] Uruk-hai, cost 4, site 5; fierce, hunter 3; his text
					// answers the start of a skirmish he is in
					put("ugluk", "15_172");
					// Uruk Decimator: [uruk-hai] Uruk-hai, cost 4, site 5, with no keyword; stronger at a
					// battleground and damage +1 only while it bears a possession
					put("decimator", "12_150");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishBreederStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Breeder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>
		 * 	<br><b>Shadow:</b> Exert this minion to play an [uruk-hai] hunter at twilight cost -2.
		*/

		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");

		assertEquals("Orkish Breeder", breeder.getBlueprint().getTitle());
		assertNull(breeder.getBlueprint().getSubtitle());
		assertFalse(breeder.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, breeder.getBlueprint().getSide());
		assertEquals(Culture.ORC, breeder.getBlueprint().getCulture());
		assertEquals(CardType.MINION, breeder.getBlueprint().getCardType());
		assertEquals(Race.ORC, breeder.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(breeder, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(breeder, Keyword.HUNTER));
		assertEquals(4, breeder.getBlueprint().getTwilightCost());
		assertEquals(9, breeder.getBlueprint().getStrength());
		assertEquals(3, breeder.getBlueprint().getVitality());
		assertEquals(4, breeder.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsThisMinionToPlayAnUrukHaiHunterFromHandForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");
		var breeder2 = scn.GetShadowCard("breeder2");
		var breeder3 = scn.GetShadowCard("breeder3");
		var tracker = scn.GetShadowCard("tracker");
		var ugluk = scn.GetShadowCard("ugluk");
		var decimator = scn.GetShadowCard("decimator");
		var discarded = scn.GetShadowCard("tracker2");

		// Two [uruk-hai] hunters in hand, an [uruk-hai] minion who is no hunter and an [orc] hunter; another
		// [uruk-hai] hunter in the discard pile.
		scn.MoveCardsToHand(tracker, ugluk, decimator, breeder3);
		scn.MoveCardsToDiscard(discarded);

		scn.StartGame();
		scn.MoveMinionsToTable(breeder, breeder2);
		scn.FreepsPass();

		// At two less the Tracking Uruk is 5 - 2 + 2 roaming (site 5 minion at site 2) = 5 and Ugluk
		// 4 - 2 + 2 = 4: with 3 in the pool the ability could play neither.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(breeder));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(breeder));
		assertEquals(0, scn.GetWoundsOn(breeder2));
		assertTrue(scn.ShadowActionAvailable(breeder));
		// Nothing is off a card played from hand in the usual way: each of them wants 6 or 7 with its roaming.
		assertFalse(scn.ShadowPlayAvailable(tracker));
		assertFalse(scn.ShadowPlayAvailable(ugluk));
		assertFalse(scn.ShadowPlayAvailable(decimator));
		assertFalse(scn.ShadowPlayAvailable(breeder3));

		scn.ShadowUseCardAction(breeder);
		assertEquals(1, scn.GetWoundsOn(breeder));
		assertEquals(0, scn.GetWoundsOn(breeder2));
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracker, ugluk));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(decimator, breeder3, discarded));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tracker);

		// 5 to play - 2 + 2 roaming (site 5 minion at site 2)
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, tracker);
		assertInHand(ugluk, decimator, breeder3);
		assertInDiscard(discarded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedOneTokenShortOfTheReducedCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");
		var ugluk = scn.GetShadowCard("ugluk");
		scn.MoveCardsToHand(ugluk);

		scn.StartGame();
		scn.MoveMinionsToTable(breeder);
		scn.FreepsPass();

		// Ugluk is 4 to play - 2 + 2 roaming (site 5 minion at site 2): 4.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(breeder));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(breeder));

		// The only [uruk-hai] hunter in hand is played without a choice.
		scn.ShadowUseCardAction(breeder);
		assertEquals(1, scn.GetWoundsOn(breeder));
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, ugluk);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoUrukHaiHunterInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");
		var breeder3 = scn.GetShadowCard("breeder3");
		var decimator = scn.GetShadowCard("decimator");

		// An [uruk-hai] minion who is no hunter and an [orc] hunter in hand; an [uruk-hai] hunter in the
		// discard pile, and others in the draw deck.
		scn.MoveCardsToHand(decimator, breeder3);
		scn.MoveCardsToDiscard(scn.GetShadowCard("tracker"));

		scn.StartGame();
		scn.MoveMinionsToTable(breeder);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(breeder));
		assertTrue(scn.ShadowPlayAvailable(decimator));
		assertTrue(scn.ShadowPlayAvailable(breeder3));
		assertFalse(scn.ShadowActionAvailable(breeder));
	}

	@Test
	public void ShadowAbilityIsNotOfferedOnACopyThatIsExhaustedAndIsOnTheCopyBesideIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breeder = scn.GetShadowCard("breeder");
		var breeder2 = scn.GetShadowCard("breeder2");
		var tracker = scn.GetShadowCard("tracker");
		scn.MoveCardsToHand(tracker);

		scn.StartGame();
		scn.MoveMinionsToTable(breeder, breeder2);
		// Vitality 3: two wounds leave the first copy exhausted.
		scn.AddWoundsToChar(breeder, 2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(tracker));
		assertFalse(scn.ShadowActionAvailable(breeder));
		assertTrue(scn.ShadowActionAvailable(breeder2));

		scn.ShadowUseCardAction(breeder2);
		assertEquals(2, scn.GetWoundsOn(breeder));
		assertEquals(1, scn.GetWoundsOn(breeder2));
		assertInZone(Zone.SHADOW_CHARACTERS, tracker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
