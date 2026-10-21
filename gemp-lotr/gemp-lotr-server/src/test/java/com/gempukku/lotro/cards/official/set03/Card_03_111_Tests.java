package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("noakes", "3_111");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					// Rohirrim Guard: a [rohan] companion, "Skirmish: Discard a card from hand to exert a minion
					// skirmishing this companion."
					put("guard", "7_246");
					// Halfling Leaf: a Free Peoples condition (support area); a skirmish ability only (to play, spot a
					// Hobbit: cheated in)
					put("halfling", "17_106");
					// Cards for the Free Peoples hand: Bounder, a Hobbit ally, never played
					put("card1", "1_286");
					put("card2", "1_286");
					put("card3", "1_286");
					put("card4", "1_286");

					// Hand of Sauron: [sauron] Maneuver event, "Exert a [sauron] minion to make the Free Peoples player
					// discard a card at random from his or her hand."
					put("hand1", "3_90");
					put("hand2", "3_90");
					// Orc Inquisitor: [sauron] Orc minion, vitality 3; "When you play this minion, you may make the Free
					// Peoples player discard a card at random from hand."
					put("inquisitor", "1_268");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text
					// Banner of the Eye: a [sauron] possession, borne by a [sauron] Orc; its trigger wants a skirmish won
					put("banner", "6_98");
					// Wreathed in Shadow: a Shadow condition in the support area; an archery ability only (to play, spot
					// a Nazgul: cheated in)
					put("wreathed", "1_238");
					// Desperate Defense of the Ring: a [sauron] condition on a companion; "Each time bearer is assigned
					// to a skirmish, the Free Peoples player chooses to either discard 3 cards from hand or add a burden."
					put("defense", "1_244");
					// Gollum, Old Villain: a [gollum] minion, "Skirmish: Discard a card from hand to make Gollum strength
					// +1 (or +2 if skirmishing a Ring-bound companion)."
					put("gollum", "6_40");
					// Tower Lieutenant: a [sauron] Orc minion, strength 10; "Each time this minion wins a skirmish, the
					// Free Peoples player discards 2 cards at random from hand."
					put("lieutenant", "1_280");
					// Shadow's Reach: [sauron] Shadow event, "Exert a [sauron] Orc to discard a Free Peoples condition."
					put("reach", "1_277");
					put("troop1", "1_177");     // Goblin Patrol Troop: cards for the Shadow hand
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Midgewater Moors as site 2: "Each time a minion is played, the Free Peoples player
	// discards a card from hand."
	protected VirtualTableScenario GetMidgewaterScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("noakes", "3_111");
					put("card1", "1_286");      // Bounder: cards for the Free Peoples hand, never played
					put("card2", "1_286");
					// Goblin Runner: a [moria] Orc minion, cost 1; "When you play this minion, you may add (2)."
					put("runner", "1_178");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_333");
					put("site3", "1_341");
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
	public void OldNoakesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Old Noakes, Purveyor of Wisdoms
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 1
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: Each time a Shadow card makes you discard a card from hand, you may also discard a minion or Shadow condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("noakes");

		assertEquals("Old Noakes", card.getBlueprint().getTitle());
		assertEquals("Purveyor of Wisdoms", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void EachShadowEventThatMakesHimDiscardFromHandOffersTheDiscardOfAMinionOrShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noakes = scn.GetFreepsCard("noakes");
		var frodo = scn.GetRingBearer();
		var halfling = scn.GetFreepsCard("halfling");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var brigand = scn.GetShadowCard("brigand");
		var banner = scn.GetShadowCard("banner");
		var wreathed = scn.GetShadowCard("wreathed");
		var defense = scn.GetShadowCard("defense");
		scn.MoveAlliesToTable(noakes);
		scn.MoveCardsToSupportArea(halfling);
		scn.MoveCardsToHand(card1, card2, card3);
		scn.MoveCardsToHand(hand1, hand2);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor, brigand);
		scn.AttachCardsTo(inquisitor, banner);
		scn.MoveCardsToSupportArea(wreathed);
		scn.AttachCardsTo(frodo, defense);

		assertInZone(Zone.SUPPORT, halfling, wreathed);
		assertAttachedTo(banner, inquisitor);
		assertAttachedTo(defense, frodo);
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor, brigand);
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(hand1);
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(noakes));

		scn.FreepsChooseOptionalTrigger(noakes);
		// Minions of two cultures and races, and Shadow conditions in the support area and on a companion; a Shadow
		// possession and a Free Peoples condition are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(inquisitor, brigand, wreathed, defense));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(banner, halfling));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(brigand);

		assertInZone(Zone.DISCARD, brigand);
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertAttachedTo(banner, inquisitor);
		assertAttachedTo(defense, frodo);
		assertInZone(Zone.SUPPORT, wreathed, halfling);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A second Hand of Sauron: offered again.
		scn.FreepsPass();
		scn.ShadowPlayCard(hand2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(noakes));

		scn.FreepsChooseOptionalTrigger(noakes);
		assertTrue(scn.FreepsHasCardChoiceAvailable(inquisitor, wreathed, defense));
		scn.FreepsChooseCard(wreathed);

		assertInZone(Zone.DISCARD, wreathed);
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertAttachedTo(defense, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AMinionThatMakesHimDiscardFromHandOffersItAndDecliningLeavesEverythingAsItWasButADiscardFromPlayDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noakes = scn.GetFreepsCard("noakes");
		var halfling = scn.GetFreepsCard("halfling");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var wreathed = scn.GetShadowCard("wreathed");
		var reach = scn.GetShadowCard("reach");
		scn.MoveAlliesToTable(noakes);
		scn.MoveCardsToSupportArea(halfling);
		scn.MoveCardsToHand(card1, card2);
		scn.MoveCardsToHand(inquisitor, reach);
		assertInZone(Zone.SUPPORT, halfling);

		scn.StartGame();
		scn.MoveCardsToSupportArea(wreathed);
		scn.SetTwilight(10);

		assertInZone(Zone.SUPPORT, wreathed);
		assertInZone(Zone.HAND, inquisitor);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsPass();
		scn.ShadowPlayCard(inquisitor);
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(noakes));

		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertInZone(Zone.SUPPORT, wreathed);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Shadow's Reach discards one of his cards from play, not from hand: not offered.
		scn.ShadowPlayCard(reach);
		assertInZone(Zone.DISCARD, halfling);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(noakes));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedForADiscardFromHandByAFreePeoplesCardOrFromTheShadowPlayersOwnHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noakes = scn.GetFreepsCard("noakes");
		var guard = scn.GetFreepsCard("guard");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var gollum = scn.GetShadowCard("gollum");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveAlliesToTable(noakes);
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(card1, card2);
		scn.MoveCardsToHand(troop1, troop2);
		assertInZone(Zone.HAND, card1, card2, troop1, troop2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, gollum);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsActionAvailable(guard));

		// The Rohirrim Guard, a Free Peoples card, has him discard from hand: not offered, though a minion is there.
		scn.FreepsUseCardAction(guard);
		scn.FreepsChooseCard(card1);
		assertInZone(Zone.DISCARD, card1);
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(noakes));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gollum, a Shadow card, has the Shadow player discard from his own hand: not offered either.
		scn.ShadowUseCardAction(gollum);
		scn.ShadowChooseCard(troop1);
		assertInZone(Zone.DISCARD, troop1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(noakes));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedForTheDiscardASiteMakesHimMake() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMidgewaterScenario();

		var noakes = scn.GetFreepsCard("noakes");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveAlliesToTable(noakes);
		scn.MoveCardsToHand(card1, card2);
		scn.MoveCardsToHand(runner);
		assertInZone(Zone.HAND, card1, card2);

		scn.StartGame();
		// The Runner is roaming at site 2: 1 + 2.
		scn.SetTwilight(3);

		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, runner);

		// At Midgewater Moors, a site and no Shadow card, a minion played has him discard a card from hand.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.ShadowPlayCard(runner);
		// The site's discard is put first, then the Runner's (2).
		scn.FreepsResolveActionOrder("Midgewater Moors");
		scn.FreepsChooseCard(card1);

		assertInZone(Zone.DISCARD, card1);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(noakes));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TwoCardsDiscardedAtRandomAtOnceOfferItTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noakes = scn.GetFreepsCard("noakes");
		var aragorn = scn.GetFreepsCard("aragorn");
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var lieutenant = scn.GetShadowCard("lieutenant");
		var wreathed = scn.GetShadowCard("wreathed");
		scn.MoveAlliesToTable(noakes);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(card1, card2, card3);

		scn.StartGame();
		scn.MoveMinionsToTable(lieutenant);
		scn.MoveCardsToSupportArea(wreathed);

		assertInZone(Zone.SUPPORT, wreathed);
		assertEquals(3, scn.GetFreepsHandCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lieutenant);
		scn.FreepsResolveSkirmish(aragorn);
		// Strength 10 against 8: the Lieutenant wins, and two cards go at random.
		scn.PassCurrentPhaseActions();
		// The Lieutenant's discard is put ahead of the skirmish damage.
		scn.FreepsResolveActionOrder("Tower Lieutenant");

		// The cards go one at a time (CR 4.2 "discard"), and he is asked after each.
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(noakes));
		scn.FreepsDeclineOptionalTrigger();

		// One offer for each card discarded.
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(noakes));
		scn.FreepsChooseOptionalTrigger(noakes);
		assertTrue(scn.FreepsHasCardChoiceAvailable(lieutenant, wreathed));
		scn.FreepsChooseCard(wreathed);

		assertInZone(Zone.DISCARD, wreathed);
		assertInZone(Zone.SHADOW_CHARACTERS, lieutenant);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(noakes));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void ADiscardFromHandChosenUnderAShadowCardThatGivesAChoiceDoesNotOfferIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noakes = scn.GetFreepsCard("noakes");
		var frodo = scn.GetRingBearer();
		var card1 = scn.GetFreepsCard("card1");
		var card2 = scn.GetFreepsCard("card2");
		var card3 = scn.GetFreepsCard("card3");
		var card4 = scn.GetFreepsCard("card4");
		var hand1 = scn.GetShadowCard("hand1");
		var inquisitor = scn.GetShadowCard("inquisitor");
		var defense = scn.GetShadowCard("defense");
		scn.MoveAlliesToTable(noakes);
		scn.MoveCardsToHand(card1, card2, card3, card4);
		scn.MoveCardsToHand(hand1);

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor);
		scn.AttachCardsTo(frodo, defense);

		assertAttachedTo(defense, frodo);
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());

		// The control: a discard Hand of Sauron makes him make is answered.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(hand1);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(noakes));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertAttachedTo(defense, frodo);

		// Desperate Defense of the Ring gives him the choice, and he chooses the discard: not offered.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, inquisitor);
		scn.FreepsChoose("Discard 3 cards from hand");
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(noakes));
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertAttachedTo(defense, frodo);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
