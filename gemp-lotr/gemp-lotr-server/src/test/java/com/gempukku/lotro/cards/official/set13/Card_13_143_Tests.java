package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_143_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bill", "13_143");

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit, not the Ring-bearer; a fellowship ability never used
					put("bilbo", "12_119");     // Bilbo, Melancholy Hobbit: Ring-bound Hobbit companion; a start-of-skirmish offer, no skirmish here
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound Hobbit companion; strength +2 with a weapon
					put("bounder", "1_286");    // Bounder: Hobbit ALLY; a skirmish ability never used
					put("smeagol", "5_29");     // Smeagol, Slinker: a Ring-bound companion who is no Hobbit; a skirmish ability never used

					put("fury1", "12_132");     // Sudden Fury: [shire] support-area condition holding [shire] tokens; a skirmish ability never used
					put("fury2", "12_132");
					put("palaces", "6_10");     // Suspended Palaces: a [dwarven] condition holding [dwarven] tokens; a fellowship ability never used
					put("ale", "18_108");       // Golden Perch Ale: [shire] support-area possession, cost 1; nothing on its own play

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("swarms", "1_183");     // Goblin Swarms: [moria] support-area condition; answers only a [moria] Orc's win
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BillthePonyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Bill the Pony, Dearly-loved
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a Ring-bound Hobbit.<br>When you play Bill the Pony, you may reinforce a [shire] token.<br><b>Regroup:</b> Spot 2 minions and remove 2 [shire] tokens to make a Shadow player discard a minion from play.
		*/

		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");

		assertEquals("Bill the Pony", bill.getBlueprint().getTitle());
		assertEquals("Dearly-loved", bill.getBlueprint().getSubtitle());
		assertTrue(bill.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, bill.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, bill.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, bill.getBlueprint().getCardType());
		assertTrue(bill.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, bill.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARingBoundHobbitAndWithNoShireTokenNoReinforcementIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var merry = scn.GetFreepsCard("merry");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(sam, bilbo, merry, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(palaces);
		scn.AddTokensToCard(palaces, 2);
		scn.MoveCardsToHand(bill);

		scn.StartGame();
		assertInHand(bill);
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		scn.FreepsPlayCard(bill);
		// The Ring-bearer, a Ring-bound Hobbit who is not, and a third; not an unbound Hobbit, a Hobbit ally, or a
		// Ring-bound companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, bounder, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(bill, sam);
		// The only tokens in play are [dwarven] ones: nothing to reinforce, and nothing is offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bill));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenPlayedItMayReinforceAShireTokenAndAnotherShirePossessionPlayedAfterItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var frodo = scn.GetRingBearer();
		var fury1 = scn.GetFreepsCard("fury1");
		var fury2 = scn.GetFreepsCard("fury2");
		var palaces = scn.GetFreepsCard("palaces");
		var ale = scn.GetFreepsCard("ale");
		scn.MoveCardsToSupportArea(fury1, fury2, palaces);
		scn.AddTokensToCard(fury1, 1);
		scn.AddTokensToCard(fury2, 1);
		scn.AddTokensToCard(palaces, 2);
		scn.MoveCardsToHand(bill, ale);

		scn.StartGame();
		assertInHand(bill, ale);
		assertEquals(1, scn.GetCultureTokensOn(fury1));
		assertEquals(1, scn.GetCultureTokensOn(fury2));
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		// Frodo is the one Ring-bound Hobbit: Bill goes on him unasked.
		scn.FreepsPlayCard(bill);
		assertAttachedTo(bill, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bill));
		scn.FreepsAcceptOptionalTrigger();

		// Either card that holds a [shire] token; not the one that holds [dwarven] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury1, fury2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(palaces));
		scn.FreepsChooseCard(fury1);

		assertEquals(2, scn.GetCultureTokensOn(fury1));
		assertEquals(1, scn.GetCultureTokensOn(fury2));
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		// Another [shire] possession played after Bill.
		scn.FreepsPlayCard(ale);
		assertInZone(Zone.SUPPORT, ale);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bill));
		assertEquals(2, scn.GetCultureTokensOn(fury1));
		assertEquals(1, scn.GetCultureTokensOn(fury2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheReinforcementDeclinedLeavesTheTokensAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var frodo = scn.GetRingBearer();
		var fury1 = scn.GetFreepsCard("fury1");
		scn.MoveCardsToSupportArea(fury1);
		scn.AddTokensToCard(fury1, 1);
		scn.MoveCardsToHand(bill);

		scn.StartGame();
		assertInHand(bill);
		assertEquals(1, scn.GetCultureTokensOn(fury1));

		scn.FreepsPlayCard(bill);
		assertAttachedTo(bill, frodo);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bill));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(fury1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityRemovesTwoShireTokensToMakeTheShadowPlayerDiscardAMinionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var frodo = scn.GetRingBearer();
		var fury1 = scn.GetFreepsCard("fury1");
		var fury2 = scn.GetFreepsCard("fury2");
		var palaces = scn.GetFreepsCard("palaces");
		scn.AttachCardsTo(frodo, bill);
		scn.MoveCardsToSupportArea(fury1, fury2, palaces);
		scn.AddTokensToCard(fury1, 1);
		scn.AddTokensToCard(fury2, 1);
		scn.AddTokensToCard(palaces, 2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var swarms = scn.GetShadowCard("swarms");

		scn.StartGame();
		assertAttachedTo(bill, frodo);
		assertEquals(1, scn.GetCultureTokensOn(fury1));
		assertEquals(1, scn.GetCultureTokensOn(fury2));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		scn.MoveMinionsToTable(savage, troop);
		scn.MoveCardsToSupportArea(swarms);
		scn.SkipToPhase(Phase.REGROUP);

		assertInPlay(savage, troop, swarms);
		assertTrue(scn.FreepsActionAvailable(bill));
		scn.FreepsUseCardAction(bill);
		// One [shire] token from each Sudden Fury, asked a card at a time; the [dwarven] tokens are no price.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury1, fury2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(palaces));
		scn.FreepsChooseCard(fury1);

		assertEquals(0, scn.GetCultureTokensOn(fury1));
		assertEquals(0, scn.GetCultureTokensOn(fury2));
		assertEquals(2, scn.GetCultureTokensOn(palaces));

		// The Shadow player chooses, of minions of two cultures and races; not his condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(swarms));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);

		assertInDiscard(troop);
		assertInPlay(savage, swarms);
		assertAttachedTo(bill, frodo);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithOneMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var frodo = scn.GetRingBearer();
		var fury1 = scn.GetFreepsCard("fury1");
		scn.AttachCardsTo(frodo, bill);
		scn.MoveCardsToSupportArea(fury1);
		scn.AddTokensToCard(fury1, 2);

		var savage = scn.GetShadowCard("savage");
		var swarms = scn.GetShadowCard("swarms");

		scn.StartGame();
		assertAttachedTo(bill, frodo);
		assertEquals(2, scn.GetCultureTokensOn(fury1));
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(swarms);
		scn.SkipToPhase(Phase.REGROUP);

		// One minion, and a Shadow condition beside it; the two tokens could be paid.
		assertInPlay(savage, swarms);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bill));
		assertEquals(2, scn.GetCultureTokensOn(fury1));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithOneShireTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetFreepsCard("bill");
		var frodo = scn.GetRingBearer();
		var fury1 = scn.GetFreepsCard("fury1");
		var palaces = scn.GetFreepsCard("palaces");
		scn.AttachCardsTo(frodo, bill);
		scn.MoveCardsToSupportArea(fury1, palaces);
		scn.AddTokensToCard(fury1, 1);
		scn.AddTokensToCard(palaces, 2);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(bill, frodo);
		assertEquals(1, scn.GetCultureTokensOn(fury1));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.REGROUP);

		// Two minions, one [shire] token and two [dwarven] ones.
		assertInPlay(savage, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bill));
		assertEquals(1, scn.GetCultureTokensOn(fury1));
	}
}
