package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gnawing", "6_63");
					put("gnawing2", "6_63");
					// [isengard] Orcs, each of site number 4. Artisan (cost 4, vitality 3), Journeyman (cost 2,
					// vitality 3) and Tinker (cost 1, vitality 2) share one text: "Regroup: Discard an [isengard]
					// Orc to make the Free Peoples player wound a companion."
					put("artisan", "6_65");
					put("journeyman", "6_67");
					put("tinker", "6_71");
					// Isengard Builder: "Regroup: If stacked on an [isengard] card, discard this card to add (2)."
					put("builder", "6_66");
					// Isengard Tender (cost 1) and Warg-rider (cost 1): cards for the stack. The Tender's regroup
					// ability, from a stack, removes (2); the Warg-rider has none
					put("tender", "6_70");
					put("warg", "5_67");
					// Isengard Worker (cost 2): its regroup ability exerts it, and is used only on purpose
					put("worker", "3_62");
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion who is an Uruk-hai
					put("runner", "1_178");     // Goblin Runner: an Orc of [moria]

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger, vitality 4
					// Gondor's Vengeance: Regroup event, cost 0. "Exert a ranger companion to discard a minion."
					put("vengeance", "1_106");
					put("vengeance2", "1_106");
					put("vengeance3", "1_106");
					// What Are They?: Maneuver event, cost 1. "Spot a ranger to discard a roaming minion."
					put("whatare", "1_119");
					// Calaglin, Elf of Lorien: ally. "Regroup: Exert Calaglin to wound a minion."
					put("calaglin", "3_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GnawingBitingHackingBurningStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Gnawing, Biting, Hacking, Burning
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Plays to your support area.<br>Each time a regroup action discards an [isengard] Orc, you may stack that Orc on this card.<br><b>Shadow:</b> Discard 2 cards stacked here and remove (1) to play an [isengard] Orc from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gnawing");

		assertEquals("Gnawing, Biting, Hacking, Burning", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysToTheSupportAreaForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gnawing = scn.GetShadowCard("gnawing");
		scn.MoveCardsToHand(gnawing);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(gnawing));
		scn.ShadowPlayCard(gnawing);

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, gnawing);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedByAFreePeoplesRegroupEventMayBeStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var vengeance = scn.GetFreepsCard("vengeance");
		var vengeance2 = scn.GetFreepsCard("vengeance2");
		var vengeance3 = scn.GetFreepsCard("vengeance3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(vengeance, vengeance2, vengeance3);

		var gnawing = scn.GetShadowCard("gnawing");
		var tinker = scn.GetShadowCard("tinker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var journeyman = scn.GetShadowCard("journeyman");
		scn.MoveCardsToSupportArea(gnawing);
		// An [isengard] Orc that was in the discard pile before: the Orc stacked is the one just discarded, not
		// this one.
		scn.MoveCardsToDiscard(journeyman);

		scn.StartGame();
		scn.MoveMinionsToTable(tinker, savage, runner);
		// Nobody is assigned to them, so all three are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		// An [isengard] minion who is no Orc.
		scn.FreepsPlayCard(vengeance);
		scn.FreepsChooseCard(savage);
		assertInZone(Zone.DISCARD, savage);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		scn.ShadowPass();

		// An Orc of another culture.
		scn.FreepsPlayCard(vengeance2);
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		scn.ShadowPass();

		// The [isengard] Orc is the one minion left.
		scn.FreepsPlayCard(vengeance3);
		assertInZone(Zone.DISCARD, tinker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, tinker);
		assertEquals(1, scn.GetStackedCards(gnawing).size());
		assertTrue(scn.GetStackedCards(gnawing).contains(tinker));
		assertInZone(Zone.DISCARD, savage, runner, journeyman);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedToPayForARegroupAbilityMayBeStackedOnOneCopyEachTimeItHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var gnawing = scn.GetShadowCard("gnawing");
		var gnawing2 = scn.GetShadowCard("gnawing2");
		var artisan = scn.GetShadowCard("artisan");
		var journeyman = scn.GetShadowCard("journeyman");
		scn.MoveCardsToSupportArea(gnawing, gnawing2);

		scn.StartGame();
		scn.MoveMinionsToTable(artisan, journeyman);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(artisan));
		scn.ShadowUseCardAction(artisan);
		// The Artisan pays with itself.
		scn.ShadowChooseCard(artisan);
		assertInZone(Zone.DISCARD, artisan);

		// Each copy offers to take it, and it goes to the copy chosen.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gnawing2));
		scn.ShadowChooseOptionalTrigger(gnawing2);

		assertInZone(Zone.STACKED, artisan);
		assertEquals(0, scn.GetStackedCards(gnawing).size());
		assertEquals(1, scn.GetStackedCards(gnawing2).size());
		assertTrue(scn.GetStackedCards(gnawing2).contains(artisan));
		// The Orc is no longer in the discard pile for the other copy to take: the Artisan's ability goes on to
		// its wound.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// A second regroup ability discards a second Orc: offered again, and declined.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(journeyman));
		scn.ShadowUseCardAction(journeyman);
		assertInZone(Zone.DISCARD, journeyman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gnawing2));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, journeyman);
		assertEquals(0, scn.GetStackedCards(gnawing).size());
		assertEquals(1, scn.GetStackedCards(gnawing2).size());
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedByAManeuverEventIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var whatare = scn.GetFreepsCard("whatare");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(whatare);

		var gnawing = scn.GetShadowCard("gnawing");
		var tinker = scn.GetShadowCard("tinker");
		scn.MoveCardsToSupportArea(gnawing);

		scn.StartGame();
		scn.MoveMinionsToTable(tinker);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site number 4 at site 2: the Tinker is roaming, and the one minion.
		scn.FreepsPlayCard(whatare);

		assertInZone(Zone.DISCARD, tinker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		assertEquals(0, scn.GetStackedCards(gnawing).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedWhenTheTurnEndsIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gnawing = scn.GetShadowCard("gnawing");
		var artisan = scn.GetShadowCard("artisan");
		scn.MoveCardsToSupportArea(gnawing);

		scn.StartGame();
		scn.MoveMinionsToTable(artisan);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, artisan);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToStay();

		// The rule discards the minions as the turn ends; no regroup action did.
		assertInZone(Zone.DISCARD, artisan);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetStackedCards(gnawing).size());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AnOrcStackedHereThatDiscardsItselfByItsOwnRegroupAbilityIsNotOfferedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gnawing = scn.GetShadowCard("gnawing");
		var builder = scn.GetShadowCard("builder");
		scn.MoveCardsToSupportArea(gnawing);
		scn.StackCardsOn(gnawing, builder);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(builder));
		scn.ShadowUseCardAction(builder);

		// It was discarded from a stack, not from play.
		assertInZone(Zone.DISCARD, builder);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		assertEquals(0, scn.GetStackedCards(gnawing).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// CR 4.2 "killed": "Cards that trigger when a character is killed will not trigger when a character is
	// discarded, and vice-versa."
	@Test
	public void AnIsengardOrcKilledByARegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calaglin = scn.GetFreepsCard("calaglin");

		var gnawing = scn.GetShadowCard("gnawing");
		var tinker = scn.GetShadowCard("tinker");
		scn.MoveCardsToSupportArea(gnawing);

		scn.StartGame();
		scn.MoveMinionsToTable(tinker);
		scn.SkipToPhase(Phase.REGROUP);
		// Calaglin is an archer, so he comes down once the archery phase is past.
		scn.MoveAlliesToTable(calaglin);
		scn.AddWoundsToChar(tinker, 1);
		scn.FreepsRefreshDecision();

		// The Tinker has 1 vitality left: Calaglin's wound kills it.
		assertTrue(scn.FreepsActionAvailable(calaglin));
		scn.FreepsUseCardAction(calaglin);

		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertInZone(Zone.DISCARD, tinker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gnawing));
		assertEquals(0, scn.GetStackedCards(gnawing).size());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void DiscardsTwoCardsStackedHereAndRemovesOneToPlayAnIsengardOrcThatWasAlreadyInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gnawing = scn.GetShadowCard("gnawing");
		var gnawing2 = scn.GetShadowCard("gnawing2");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var artisan = scn.GetShadowCard("artisan");
		var builder = scn.GetShadowCard("builder");
		var tinker = scn.GetShadowCard("tinker");
		var journeyman = scn.GetShadowCard("journeyman");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(gnawing, gnawing2);
		scn.StackCardsOn(gnawing, tender, warg, artisan);
		scn.StackCardsOn(gnawing2, builder);
		scn.MoveCardsToDiscard(tinker, journeyman, savage, runner);
		scn.MoveCardsToHand(worker);

		scn.StartGame();
		scn.FreepsPass();
		// 1 for the ability + 4 for the dearer of the two Orcs in the pile, the Journeyman (2 + 2 roaming at
		// site 2).
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		// The copy with one card stacked cannot pay.
		assertTrue(scn.ShadowActionAvailable(gnawing));
		assertFalse(scn.ShadowActionAvailable(gnawing2));
		scn.ShadowUseCardAction(gnawing);

		// Two of the three cards stacked on this copy; not the card on the other copy.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tender, warg, artisan));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(builder));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(tender, warg);

		assertInZone(Zone.DISCARD, tender, warg);
		assertEquals(1, scn.GetStackedCards(gnawing).size());
		assertTrue(scn.GetStackedCards(gnawing).contains(artisan));
		assertEquals(4, scn.GetTwilight());

		// The [isengard] Orcs that were in the discard pile before the ability was used. Not the two just
		// discarded there to pay for it (each costs 1 and could be paid for), not the Uruk-hai, not the Orc of
		// [moria], and not the [isengard] Orc in hand.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tinker, journeyman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(tender, warg, savage, runner, worker));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(tinker);

		assertInZone(Zone.SHADOW_CHARACTERS, tinker);
		// 5 - 1 for the ability - 3 for the Tinker (1 + 2 roaming)
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.DISCARD, journeyman, tender, warg);
		assertInZone(Zone.HAND, worker);
		assertEquals(1, scn.GetStackedCards(gnawing2).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithOneCardStackedHereOrWithoutThePoolForTheRemovalAndTheOrcTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gnawing = scn.GetShadowCard("gnawing");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var tinker = scn.GetShadowCard("tinker");
		scn.MoveCardsToSupportArea(gnawing);
		scn.StackCardsOn(gnawing, tender);
		scn.MoveCardsToDiscard(tinker);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// One card stacked.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gnawing));

		scn.StackCardsOn(gnawing, warg);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gnawing));

		// The Tinker costs 3 here (1 + 2 roaming at site 2): (3) pays for it or for the removal, not for both.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gnawing));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gnawing));
	}

	// CR 4.2 "effect": "If an action plays a card from your hand (or discard pile) as part of its effect, then
	// that card must be in your hand (or discard pile) before you can begin to perform that action."
	@Test
	public void NotOfferedWhenTheOnlyIsengardOrcsAreTheOnesStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gnawing = scn.GetShadowCard("gnawing");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var tinker = scn.GetShadowCard("tinker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(gnawing);
		scn.StackCardsOn(gnawing, tender, warg);
		// An [isengard] Uruk-hai and an Orc of [moria] in the discard pile, each of which the pool could pay for.
		scn.MoveCardsToDiscard(savage, runner);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gnawing));

		scn.MoveCardsToDiscard(tinker);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gnawing));
	}
}
