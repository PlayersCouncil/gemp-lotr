package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_065_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger", "11_65");
					put("ranger2", "11_65");
					// Ranger of Osgiliath: [gondor] Man companion, Ranger, cost 2.  Its regroup ability (discard 2
					// cards from hand) is never used.
					put("osgiliath", "7_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangerofWesternesseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Ranger of Westernesse
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>.<br>When you play this companion, you may play the fellowship's next site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ranger");

		assertEquals("Ranger of Westernesse", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WhenPlayedHeMayPlayTheFellowshipsNextSiteFromHisOwnAdventureDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCardsToHand(ranger);

		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();

		// Only site 1 is on the path; each player's site 2 is in his adventure deck.
		assertNull(scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2, shadowSite2);
		assertEquals(1, scn.GetCurrentSiteNumber());

		scn.FreepsPlayCard(ranger);
		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ranger));
		scn.FreepsAcceptOptionalTrigger();

		// His own site 2 is laid; the fellowship has not moved, and site 3 is not laid.
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, freepsSite2);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertNull(scn.GetSite(3));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HisOwnSiteReplacesTheOtherPlayersSiteLyingNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCardsToHand(ranger);

		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		// CR 4.2 "replacing sites": a card that plays the next site may be used though a site lies there.
		scn.MoveCardToAdventurePath(shadowSite2);
		scn.FreepsRefreshDecision();
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);

		scn.FreepsPlayCard(ranger);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ranger));
		scn.FreepsAcceptOptionalTrigger();

		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedNoSiteIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCardsToHand(ranger);

		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		assertNull(scn.GetSite(2));

		scn.FreepsPlayCard(ranger);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ranger));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertNull(scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2, shadowSite2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithHisOwnSiteAlreadyNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCardsToHand(ranger);

		var freepsSite2 = scn.GetFreepsSite(2);

		scn.StartGame();
		// As where he has played his next site already: on a numbered path he has no other site 2, so the
		// trigger would do nothing.
		scn.MoveCardToAdventurePath(freepsSite2);
		scn.FreepsRefreshDecision();
		assertSame(freepsSite2, scn.GetSite(2));

		scn.FreepsPlayCard(ranger);

		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ranger));
		assertSame(freepsSite2, scn.GetSite(2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnotherRangerPlayedAfterHimSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var osgiliath = scn.GetFreepsCard("osgiliath");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(osgiliath);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertNull(scn.GetSite(2));

		scn.FreepsPlayCard(osgiliath);

		assertInZone(Zone.FREE_CHARACTERS, osgiliath);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ranger));
		assertNull(scn.GetSite(2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedMakesOnlyItsOwnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var ranger2 = scn.GetFreepsCard("ranger2");
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(ranger2);

		var freepsSite2 = scn.GetFreepsSite(2);

		scn.StartGame();
		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertNull(scn.GetSite(2));

		scn.FreepsPlayCard(ranger2);

		// The copy played offers its own trigger; the copy already in play answers nothing.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ranger2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ranger));
		scn.FreepsChooseOptionalTrigger(ranger2);

		assertSame(freepsSite2, scn.GetSite(2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ranger));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ChosenIntoTheStartingFellowshipHeMayPlayTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var freepsSite2 = scn.GetFreepsSite(2);
		var shadowSite2 = scn.GetShadowSite(2);

		// CR 4.2 "starting fellowship": "When you play" text may be used on a starting companion (Q-0305).
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertNull(scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2, shadowSite2);

		scn.FreepsChooseCard(ranger);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ranger));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.FREE_CHARACTERS, ranger);
		assertSame(freepsSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		// Ranger of Osgiliath, cost 2, still fits under the total of 4.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
	}
}
