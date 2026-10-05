package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("murder", "101_29");

					// Free Peoples conditions of three cultures, none of which does anything on these boards
					put("promise", "2_112");    // A Promise: [shire], support area, cost 0; strengthens Frodo and Sam only while Sam is in play
					put("lord", "1_21");        // Lord of Moria: [dwarven], support area; each Dwarf is damage +1, and there is no Dwarf
					put("wisdom", "2_23");      // Gandalf's Wisdom: [gandalf], support area, cost 0; a discount on items played on Gandalf
					put("elbereth", "2_108");   // O Elbereth! Gilthoniel!: [shire], borne by the Ring-bearer; a skirmish ability never used here
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession with no text

					put("armory", "1_173");     // Goblin Armory: [moria] Shadow condition; triggers on [moria] weapons and [moria] Orcs only
					put("armory2", "1_173");
					put("snows", "1_138");      // Saruman's Snows: [isengard] weather condition that plays on a site
					put("ambition", "1_133");   // Saruman's Ambition: [isengard] condition, support area; cheapens [isengard] events
					put("business", "2_44");    // No Business of Ours: [isengard] condition, support area
					put("caradhras", "1_123");  // Caradhras Has Not Forgiven Us: [isengard] weather event, so not a condition
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, cost 2, site 5; Damage +1 is its whole text
					put("saruman", "12_144");   // Saruman, Agent of the Dark Lord: [uruk-hai] minion, cost 4, site 4, lurker; a skirmish ability and a trigger on a companion's death, neither met here
					// A Saruman that is not a minion. The text says "spot Saruman" and no more, so this one in play counts too.
					put("servant", "17_38");    // Saruman, Servant of Sauron: [isengard] follower, cost 1, played to the support area; not a condition, and its aid cost and its trigger need a bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MurderofCrowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Murder of Crows
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Crow
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: While you can spot 3 Free Peoples conditions, this minion is twilight cost -3.
		 * 	Shadow: Spot 3 Free Peoples conditions (or spot Saruman) to take an [isengard] condition into hand from your draw deck or discard pile (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("murder");

		assertEquals("Murder of Crows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.CROW, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CostsItsPrintedFourWithTwoFreePeoplesConditionsAShadowConditionAndAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var murder = scn.GetShadowCard("murder");
		var frodo = scn.GetRingBearer();
		var promise = scn.GetFreepsCard("promise");
		var elbereth = scn.GetFreepsCard("elbereth");
		var sword = scn.GetFreepsCard("sword");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(murder);
		scn.MoveCardsToSupportArea(promise, armory);
		scn.AttachCardsTo(frodo, elbereth, sword);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		// Two Free Peoples conditions; the Shadow condition and the Free Peoples possession do not make a third.
		assertInZone(Zone.SUPPORT, promise, armory);
		assertAttachedTo(elbereth, frodo);
		assertAttachedTo(sword, frodo);
		assertInZone(Zone.HAND, murder);

		scn.ShadowPlayCard(murder);

		assertInZone(Zone.SHADOW_CHARACTERS, murder);
		// 10 - 4 to play - 2 roaming (a site 4 minion played at site 2)
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CostsThreeLessOnceAThirdFreePeoplesConditionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var murder = scn.GetShadowCard("murder");
		var frodo = scn.GetRingBearer();
		var promise = scn.GetFreepsCard("promise");
		var elbereth = scn.GetFreepsCard("elbereth");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCardsToHand(murder, wisdom);
		scn.MoveCardsToSupportArea(promise);
		scn.AttachCardsTo(frodo, elbereth);

		scn.StartGame();

		assertEquals(4, scn.GetTwilightCost(murder));

		// The third condition: of a third culture, in the support area like the first and unlike the second.
		scn.FreepsPlayCard(wisdom);
		assertInZone(Zone.SUPPORT, wisdom);
		assertEquals(1, scn.GetTwilightCost(murder));

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// Gandalf's Wisdom costs 0. 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(3, scn.GetTwilight());
		// Just enough: 4 - 3 to play + 2 roaming at site 2
		assertTrue(scn.ShadowPlayAvailable(murder));

		scn.ShadowPlayCard(murder);

		assertInZone(Zone.SHADOW_CHARACTERS, murder);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFourthFreePeoplesConditionTakesNoMoreOffTheCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var murder = scn.GetShadowCard("murder");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(murder);
		scn.MoveFreepsCardsToSupportArea("promise", "lord", "wisdom");
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("elbereth"));

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());

		scn.ShadowPlayCard(murder);

		assertInZone(Zone.SHADOW_CHARACTERS, murder);
		// Four conditions are still only "3 spotted": 10 - (4 - 3) to play - 2 roaming at site 2
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingThreeFreePeoplesConditionsTakesAnIsengardConditionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var murder = scn.GetShadowCard("murder");
		var savage = scn.GetShadowCard("savage");
		var snows = scn.GetShadowCard("snows");
		var ambition = scn.GetShadowCard("ambition");
		var business = scn.GetShadowCard("business");
		var caradhras = scn.GetShadowCard("caradhras");
		var armory = scn.GetShadowCard("armory");
		var armory2 = scn.GetShadowCard("armory2");
		var shadowLord = scn.GetShadowCard("lord");
		var frodo = scn.GetRingBearer();
		var promise = scn.GetFreepsCard("promise");
		var elbereth = scn.GetFreepsCard("elbereth");
		var sword = scn.GetFreepsCard("sword");
		var wisdom = scn.GetFreepsCard("wisdom");
		scn.MoveCardsToSupportArea(promise, armory);
		scn.AttachCardsTo(frodo, elbereth, sword);
		scn.MoveCardsToHand(savage);
		// Snows, Ambition, Caradhras, the second Armory and the Shadow player's own Lord of Moria stay in the
		// draw deck; No Business of Ours gives the discard pile an [isengard] condition too.
		scn.MoveCardsToDiscard(business);

		scn.StartGame();
		scn.MoveMinionsToTable(murder);
		scn.SetTwilight(1);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 1 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		// Two Free Peoples conditions, a Shadow condition, a Free Peoples possession and no Saruman
		assertInZone(Zone.SUPPORT, promise, armory);
		assertAttachedTo(elbereth, frodo);
		assertAttachedTo(sword, frodo);
		assertFalse(scn.ShadowActionAvailable(murder));

		// A third Free Peoples condition arrives. Playing the Uruk Savage is only there to have the game ask again.
		scn.MoveCardsToSupportArea(wisdom);
		scn.ShadowPlayCard(savage);
		assertTrue(scn.ShadowActionAvailable(murder));

		assertEquals(0, scn.GetShadowHandCount());
		int deckBefore = scn.GetShadowDeckCount();
		assertInZone(Zone.DECK, snows, ambition);
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowUseCardAction(murder);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();

		// A weather that plays on a site and a condition for the support area
		assertTrue(scn.ShadowHasCardChoiceAvailable(snows, ambition));
		// An [isengard] card that is not a condition, a Shadow condition that is not [isengard],
		// a Free Peoples condition, and the [isengard] condition that is in the other pile
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(caradhras, armory2, shadowLord, business));
		scn.ShadowChooseCardBPFromSelection(snows);

		assertInZone(Zone.HAND, snows);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deckBefore - 1, scn.GetShadowDeckCount());
		assertInZone(Zone.DECK, ambition);
		assertInZone(Zone.DISCARD, business);
		// The draw deck was looked through for the condition, so it is reshuffled once the looking is done.
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingSarumanIsEnoughWithNoFreePeoplesConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var murder = scn.GetShadowCard("murder");
		// "Saruman" is a title: this one is not [isengard], unlike the Crows.
		var saruman = scn.GetShadowCard("saruman");
		var snows = scn.GetShadowCard("snows");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToHand(saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(murder);
		scn.SetTwilight(3);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 3 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(6, scn.GetTwilight());
		// No Free Peoples condition and no Saruman
		assertFalse(scn.ShadowActionAvailable(murder));

		scn.ShadowPlayCard(saruman);
		assertInPlay(saruman);
		// 6 - 4 to play Saruman - 2 roaming at site 2
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(murder));

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		int shuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowUseCardAction(murder);
		// The discard pile has no [isengard] condition to take, so the draw deck is searched without asking which.
		assertFalse(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(snows, ambition));
		scn.ShadowChooseCardBPFromSelection(ambition);

		assertInZone(Zone.HAND, ambition);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, snows);
		// The draw deck was looked through for the condition, so it is reshuffled once the looking is done.
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingAFollowerNamedSarumanIsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var murder = scn.GetShadowCard("murder");
		// "Saruman" is a title and the text asks for no card type: this one is a follower, which sits in the
		// support area and is not a character, but is a card named Saruman in play.
		var servant = scn.GetShadowCard("servant");
		var snows = scn.GetShadowCard("snows");
		var ambition = scn.GetShadowCard("ambition");
		scn.MoveCardsToHand(servant);

		scn.StartGame();
		scn.MoveMinionsToTable(murder);
		scn.SetTwilight(7);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 7 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(10, scn.GetTwilight());
		// No Free Peoples condition, and the only Saruman is in hand
		assertInZone(Zone.HAND, servant);
		assertFalse(scn.ShadowActionAvailable(murder));
		assertTrue(scn.ShadowPlayAvailable(servant));

		scn.ShadowPlayCard(servant);
		assertInZone(Zone.SUPPORT, servant);
		// 10 - 1 to play the follower, which is not a minion and so is never roaming
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(murder));

		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, snows, ambition);
		scn.ShadowUseCardAction(murder);
		// The discard pile has no [isengard] condition to take, so the draw deck is searched without asking which.
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(snows);

		assertInZone(Zone.HAND, snows);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DECK, ambition);
		assertInZone(Zone.SUPPORT, servant);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TakesFromTheDiscardPileOnceInAPhaseAndAgainInTheNextShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var murder = scn.GetShadowCard("murder");
		var snows = scn.GetShadowCard("snows");
		var ambition = scn.GetShadowCard("ambition");
		var business = scn.GetShadowCard("business");
		var caradhras = scn.GetShadowCard("caradhras");
		var armory2 = scn.GetShadowCard("armory2");
		var shadowLord = scn.GetShadowCard("lord");
		var frodo = scn.GetRingBearer();
		scn.MoveFreepsCardsToSupportArea("promise", "wisdom");
		scn.AttachCardsTo(frodo, scn.GetFreepsCard("elbereth"));
		// Ambition and No Business of Ours are the [isengard] conditions in the discard pile; Snows stays in the draw deck.
		scn.MoveCardsToDiscard(ambition, business, caradhras, armory2, shadowLord);

		scn.StartGame();
		scn.MoveMinionsToTable(murder);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowActionAvailable(murder));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(5, scn.GetShadowDiscardCount());
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowUseCardAction(murder);
		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("discard pile");

		assertTrue(scn.ShadowHasCardChoiceAvailable(ambition, business));
		// An [isengard] card that is not a condition, a Shadow condition that is not [isengard],
		// a Free Peoples condition, and the [isengard] condition that is in the other pile
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(caradhras, armory2, shadowLord, snows));
		scn.ShadowChooseCard(business);

		assertInZone(Zone.HAND, business);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(4, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, ambition);
		assertInZone(Zone.DECK, snows);
		// The condition came from the discard pile and the draw deck was never looked through, so it is not reshuffled.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		// The three conditions are still there, and the ability has had its once for this phase.
		assertFalse(scn.ShadowActionAvailable(murder));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Nobody is assigned to the Crows, so they are still in play when the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInPlay(murder);
		assertTrue(scn.ShadowActionAvailable(murder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
