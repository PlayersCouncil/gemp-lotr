package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_057_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo: a [shire] Hobbit companion of vitality 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, vitality 4
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion with no text in play
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability and nothing else
					// Hobbit Farmer: [shire] Hobbit ally.  "While you can spot your site 1, this ally has the game
					// text of that site."
					put("farmer", "1_295");

					// [shire] conditions.  Nice Imitation: cost 1, plays to the support area, and only answers a wound
					// to a Hobbit.  There and Back Again: cost 1, bearer must be a Hobbit companion.
					put("imitation", "2_106");
					put("tale", "1_317");
					// Stone Trolls: a [shire] condition that cannot be played unless Bilbo is spotted
					put("trolls", "1_314");
					put("promise", "2_112");    // A Promise: a [shire] condition, kept in hand
					put("monstrous", "3_114");  // Three Monstrous Trolls: a [shire] condition, kept in the discard pile

					put("sword", "1_299");      // Hobbit Sword: a [shire] possession
					put("deftness", "1_293");   // Halfling Deftness: a [shire] event
					// Gandalf's Wisdom: a [gandalf] condition that costs nothing and has no requirement
					put("wisdom", "2_23");
				}},
				new HashMap<>()
				{{
					put("site1", "101_57");
					put("site2", "1_331");      // Ettenmoors: shadow number 2
					put("site3", "1_341");      // Rivendell Valley: shadow number 0
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BagEndStudyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Bag End Study
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: When the fellowship moves from here, you may exert a [shire] companion to play a [shire]
		 * 	condition from your draw deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Bag End Study", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void WhenTheFellowshipMovesFromHereAShireCompanionMayExertToPlayAShireConditionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var imitation = scn.GetFreepsCard("imitation");
		var tale = scn.GetFreepsCard("tale");
		var trolls = scn.GetFreepsCard("trolls");
		var promise = scn.GetFreepsCard("promise");
		var monstrous = scn.GetFreepsCard("monstrous");
		var sword = scn.GetFreepsCard("sword");
		var deftness = scn.GetFreepsCard("deftness");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCompanionsToTable(merry, guard, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(promise);
		scn.MoveCardsToDiscard(monstrous);

		scn.StartGame();
		// A Dwarf who is a [shire] card, and a [gondor] Man who is a Hobbit.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, guard, null, Culture.SHIRE));
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.HOBBIT));

		var study = scn.GetCurrentSite();
		assertEquals("Bag End Study", study.getBlueprint().getTitle());
		assertInZone(Zone.DECK, imitation, tale, trolls, sword, deftness, wisdom);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();
		// Nothing is offered at the start of the turn or during the fellowship phase.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilight());

		scn.FreepsChooseOptionalTrigger(study);
		// The Ring-bearer, another Hobbit, and the Dwarf who is a [shire] card.  Not the Man who is a Hobbit and
		// no [shire] card, and not the [shire] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(merry));

		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// Looking through the draw deck shows the whole of it first.
		scn.FreepsDismissRevealedCards();
		// The two [shire] conditions in the draw deck that can be played.  Not the one that needs Bilbo, not the
		// [shire] possession or event, not the [gandalf] condition, and not the [shire] conditions in hand and
		// in the discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(imitation, tale));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trolls, sword, deftness, wisdom, promise, monstrous));

		scn.FreepsChooseCardBPFromSelection(imitation);
		assertInZone(Zone.SUPPORT, imitation);
		assertInZone(Zone.DECK, tale, trolls, sword, deftness, wisdom);
		assertInHand(promise);
		assertInDiscard(monstrous);
		// The deck was looked through, so it is shuffled.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());

		// Then the move goes on.  1 for Nice Imitation + 4 companions + 2 for Ettenmoors
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysAConditionThatGoesOnABearerAfterTheOnlyShireCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var tale = scn.GetFreepsCard("tale");

		scn.StartGame();

		var study = scn.GetCurrentSite();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DECK, tale);

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));

		// Frodo is the only [shire] companion, so he exerts without being chosen.
		scn.FreepsChooseOptionalTrigger(study);
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(tale);

		// There and Back Again goes on a Hobbit companion, and Frodo is the only one.
		assertAttachedTo(tale, frodo);
		// The search shuffles the deck, and a card played from the deck onto a bearer shuffles it as it leaves.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		// 1 for There and Back Again + 1 companion + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesEveryoneUnexertedAndTheDrawDeckUntouched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var imitation = scn.GetFreepsCard("imitation");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		var study = scn.GetCurrentSite();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentShuffles = scn.GetShadowDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertInZone(Zone.DECK, imitation, tale);
		assertEquals(deck, scn.GetFreepsDeckCount());
		// The deck was never looked through, so it is not shuffled.
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(opponentShuffles, scn.GetShadowDeckShuffleCount());
		// 2 companions + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenEveryShireCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var imitation = scn.GetFreepsCard("imitation");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Frodo and Merry have vitality 4.  Aragorn, who is no [shire] card, and the Bounder, who is no
		// companion, could exert and are not asked to.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(merry, 3);

		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(1, scn.GetCurrentSiteNumber());

		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInZone(Zone.DECK, imitation);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		// 3 companions + 2 for Ettenmoors
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoShireConditionInTheDrawDeckTheCompanionStillExertsAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCompanionsToTable(merry);
		// Every [shire] condition is out of the draw deck.  A [shire] possession and a [gandalf] condition are
		// still in it.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("imitation"), scn.GetFreepsCard("tale"), scn.GetFreepsCard("trolls"),
				scn.GetFreepsCard("promise"), scn.GetFreepsCard("monstrous"));

		scn.StartGame();

		var study = scn.GetCurrentSite();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertInZone(Zone.DECK, sword, wisdom);

		// There is no penalty for looking and not finding, so the player may still try.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));

		scn.FreepsChooseOptionalTrigger(study);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DECK, sword, wisdom);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		// 2 companions + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HavingLookedThePlayerMayPlayNothingAndTheExertionStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var imitation = scn.GetFreepsCard("imitation");
		var tale = scn.GetFreepsCard("tale");

		scn.StartGame();

		var study = scn.GetCurrentSite();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPass();
		scn.FreepsChooseOptionalTrigger(study);
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(imitation, tale));
		// A player who has looked through his draw deck may choose not to play what he finds.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsDeclineChoosing();
		assertInZone(Zone.DECK, imitation, tale);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetWoundsOn(frodo));
		// He did look, so the deck is shuffled.
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		// 1 companion + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheFellowshipMovesOnFromTheNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var farmer = scn.GetFreepsCard("farmer");
		var imitation = scn.GetFreepsCard("imitation");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		var study = scn.GetCurrentSite();
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Nobody played a minion, so the turn goes on to the regroup phase and the choice of a second move.
		scn.SkipToMovementDecision();
		// The Shadow player has drawn his hand by now; the cards this is about go back on the decks.
		scn.MoveCardsToTopOfDeck(imitation, tale);
		// A Hobbit Farmer has the text of his owner's site 1 wherever the fellowship is, so the Study's text is
		// in force at Ettenmoors too.  "Here" is still the Study.
		scn.MoveAlliesToTable(farmer);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, study);

		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertInZone(Zone.DECK, imitation, tale);
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnItIsThatPlayerWhoMayExertAndWhoseDrawDeckIsSearched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.StartGame();

		var study = scn.GetCurrentSite();
		scn.FreepsPass();
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship starts at the same site 1.  His cards are the ones the rig calls the
		// Shadow player's.
		var frodo = scn.GetShadowRingBearer();
		var merry = scn.GetShadowCard("merry");
		var imitation = scn.GetShadowCard("imitation");
		var tale = scn.GetShadowCard("tale");
		scn.MoveCompanionsToTable(merry);
		// He drew a hand while he was the Shadow player; the conditions go back on his deck.
		scn.MoveCardsToTopOfDeck(imitation, tale);

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertSame(study, scn.GetCurrentSite());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		int shuffles = scn.GetShadowDeckShuffleCount();
		int firstPlayersShuffles = scn.GetFreepsDeckShuffleCount();
		int twilight = scn.GetTwilight();

		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(study));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.ShadowChooseOptionalTrigger(study);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, merry));
		scn.ShadowChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(imitation, tale));
		scn.ShadowChooseCardBPFromSelection(imitation);
		assertInZone(Zone.SUPPORT, imitation);
		assertInZone(Zone.DECK, tale);
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play (F-0313)
		assertEquals(firstPlayersShuffles, scn.GetFreepsDeckShuffleCount());

		// 1 for Nice Imitation + 2 companions + 2 for Ettenmoors
		assertEquals(twilight + 5, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void AHobbitFarmerHasTheStudysTextSoASecondCompanionMayExertForASecondCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var farmer = scn.GetFreepsCard("farmer");
		var imitation = scn.GetFreepsCard("imitation");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(farmer);

		scn.StartGame();

		var study = scn.GetCurrentSite();
		assertInZone(Zone.DECK, imitation, tale);

		// The site is the Free Peoples player's own site 1, so his Hobbit Farmer has its game text: the move
		// sets off the site's offer and the Farmer's.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(farmer));

		scn.FreepsChooseOptionalTrigger(farmer);
		// The Farmer is a [shire] ally and cannot be the one who exerts.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(farmer));
		scn.FreepsChooseCard(merry);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(imitation);
		assertInZone(Zone.SUPPORT, imitation);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetCurrentSiteNumber());

		// The site's own offer is still to be answered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(farmer));

		scn.FreepsChooseOptionalTrigger(study);
		scn.FreepsChooseCard(frodo);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(tale);
		// There and Back Again goes on a Hobbit companion: Frodo or Merry.
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(tale, frodo);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(farmer));

		// 1 for Nice Imitation + 1 for There and Back Again + 2 companions + 2 for Ettenmoors
		assertEquals(6, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
