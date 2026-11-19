package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fist", "6_64");
					put("fist2", "6_64");
					// [isengard] Orcs, each of site number 4. Artisan (cost 4, vitality 3), Journeyman (cost 2,
					// vitality 3) and Tinker (cost 1, vitality 2) share one text: "Regroup: Discard an [isengard]
					// Orc to make the Free Peoples player wound a companion."
					put("artisan", "6_65");
					put("journeyman", "6_67");
					put("tinker", "6_71");
					// Isengard Builder: "Regroup: If stacked on an [isengard] card, discard this card to add (2)."
					put("builder", "6_66");
					// Isengard Tender and Warg-rider: cards for the stack. The Tender's regroup ability, from a stack,
					// discards it and removes (2) to exhaust a companion, and is never used; the Warg-rider has none
					put("tender", "6_70");
					put("warg", "5_67");
					// Isengard Worker: its regroup ability exerts it, and is used only on purpose
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

					// Free Peoples possessions: Dwarven Armor ([dwarven]) on Gimli, a companion; Hobbit Sword
					// ([shire]) on Bilbo, an ally; Old Toby ([shire] pipeweed) in the support area, borne by nobody.
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("armor", "1_8");        // Dwarven Armor: a bar on overwhelming only
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a fellowship ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: no text but its bearer
					put("toby", "1_305");       // Old Toby: a draw on its own play, which a cheat does not set off
					// The Shards of Narsil: a Free Peoples artifact in the support area, an item that is no possession;
					// fellowship abilities only
					put("shards", "3_44");
					// Hides: the Shadow player's own possession, in his support area. Its response prevents a wound to
					// a [dunland] Man, and there is none
					put("hides", "4_19");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IronFistoftheOrcStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Iron Fist of the Orc
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Plays to your support area.<br>Each time a regroup action discards an [isengard] Orc, you may stack that Orc on this card.<br><b>Regroup:</b> Discard 3 cards stacked here and remove (1) to discard a Free Peoples possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fist");

		assertEquals("Iron Fist of the Orc", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
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

		var fist = scn.GetShadowCard("fist");
		var tinker = scn.GetShadowCard("tinker");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var journeyman = scn.GetShadowCard("journeyman");
		scn.MoveCardsToSupportArea(fist);
		// An [isengard] Orc that was in the discard pile before: the Orc stacked is the one just discarded, not
		// this one.
		scn.MoveCardsToDiscard(journeyman);

		scn.StartGame();
		assertInZone(Zone.DISCARD, journeyman);
		scn.MoveMinionsToTable(tinker, savage, runner);
		// Nobody is assigned to them, so all three are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);

		// An [isengard] minion who is no Orc.
		scn.FreepsPlayCard(vengeance);
		scn.FreepsChooseCard(savage);
		assertInZone(Zone.DISCARD, savage);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fist));
		scn.ShadowPass();

		// An Orc of another culture.
		scn.FreepsPlayCard(vengeance2);
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fist));
		scn.ShadowPass();

		// The [isengard] Orc is the one minion left.
		scn.FreepsPlayCard(vengeance3);
		assertInZone(Zone.DISCARD, tinker);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fist));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.STACKED, tinker);
		assertEquals(1, scn.GetStackedCards(fist).size());
		assertTrue(scn.GetStackedCards(fist).contains(tinker));
		assertInZone(Zone.DISCARD, savage, runner, journeyman);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedToPayForARegroupAbilityMayBeStackedOnOneCopyEachTimeItHappens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fist = scn.GetShadowCard("fist");
		var fist2 = scn.GetShadowCard("fist2");
		var artisan = scn.GetShadowCard("artisan");
		var journeyman = scn.GetShadowCard("journeyman");
		scn.MoveCardsToSupportArea(fist, fist2);

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
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fist));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fist2));
		scn.ShadowChooseOptionalTrigger(fist2);

		assertInZone(Zone.STACKED, artisan);
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertEquals(1, scn.GetStackedCards(fist2).size());
		assertTrue(scn.GetStackedCards(fist2).contains(artisan));
		// The Orc is no longer in the discard pile for the other copy to take: the Artisan's ability goes on to
		// its wound.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fist));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// A second regroup ability discards a second Orc: offered again, and declined.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(journeyman));
		scn.ShadowUseCardAction(journeyman);
		assertInZone(Zone.DISCARD, journeyman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fist));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fist2));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, journeyman);
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertEquals(1, scn.GetStackedCards(fist2).size());
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

		var fist = scn.GetShadowCard("fist");
		var tinker = scn.GetShadowCard("tinker");
		scn.MoveCardsToSupportArea(fist);

		scn.StartGame();
		scn.MoveMinionsToTable(tinker);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site number 4 at site 2: the Tinker is roaming, and the one minion.
		scn.FreepsPlayCard(whatare);

		assertInZone(Zone.DISCARD, tinker);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fist));
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnIsengardOrcDiscardedWhenTheTurnEndsIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fist = scn.GetShadowCard("fist");
		var artisan = scn.GetShadowCard("artisan");
		scn.MoveCardsToSupportArea(fist);

		scn.StartGame();
		scn.MoveMinionsToTable(artisan);
		scn.SkipToPhase(Phase.REGROUP);

		assertInZone(Zone.SHADOW_CHARACTERS, artisan);
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseToStay();

		// The rule discards the minions as the turn ends; no regroup action did.
		assertInZone(Zone.DISCARD, artisan);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AnOrcStackedHereThatDiscardsItselfByItsOwnRegroupAbilityIsNotOfferedBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fist = scn.GetShadowCard("fist");
		var builder = scn.GetShadowCard("builder");
		scn.MoveCardsToSupportArea(fist);
		scn.StackCardsOn(fist, builder);

		scn.StartGame();
		assertInZone(Zone.STACKED, builder);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(builder));
		scn.ShadowUseCardAction(builder);

		// It was discarded from a stack, not from play.
		assertInZone(Zone.DISCARD, builder);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fist));
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// CR 4.2 "killed": "Cards that trigger when a character is killed will not trigger when a character is
	// discarded, and vice-versa."
	@Test
	public void AnIsengardOrcKilledByARegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calaglin = scn.GetFreepsCard("calaglin");

		var fist = scn.GetShadowCard("fist");
		var tinker = scn.GetShadowCard("tinker");
		scn.MoveCardsToSupportArea(fist);

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
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fist));
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void DiscardsThreeCardsStackedHereAndRemovesOneToDiscardAFreePeoplesPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		var toby = scn.GetFreepsCard("toby");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bilbo);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(bilbo, sword);
		scn.MoveCardsToSupportArea(toby, shards);

		var fist = scn.GetShadowCard("fist");
		var fist2 = scn.GetShadowCard("fist2");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var tinker = scn.GetShadowCard("tinker");
		var journeyman = scn.GetShadowCard("journeyman");
		var builder = scn.GetShadowCard("builder");
		var artisan = scn.GetShadowCard("artisan");
		var worker = scn.GetShadowCard("worker");
		var hides = scn.GetShadowCard("hides");
		scn.MoveCardsToSupportArea(fist, fist2, hides);
		scn.StackCardsOn(fist, tender, warg, tinker, journeyman);
		scn.StackCardsOn(fist2, builder, artisan, worker);

		scn.StartGame();
		assertInZone(Zone.STACKED, tender, warg, tinker, journeyman);
		assertEquals(4, scn.GetStackedCards(fist).size());
		assertEquals(3, scn.GetStackedCards(fist2).size());
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, bilbo);
		assertInZone(Zone.SUPPORT, toby, shards, hides);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(fist));
		scn.ShadowUseCardAction(fist);

		// Three of the four cards stacked on this copy, and none of those on the other.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tender, warg, tinker, journeyman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(builder, artisan, worker));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(tender, warg, tinker);

		assertInZone(Zone.DISCARD, tender, warg, tinker);
		assertEquals(1, scn.GetStackedCards(fist).size());
		assertTrue(scn.GetStackedCards(fist).contains(journeyman));
		assertEquals(3, scn.GetStackedCards(fist2).size());
		assertEquals(2, scn.GetTwilight());

		// Free Peoples possessions borne by a companion, by an ally and by nobody; not the Free Peoples artifact,
		// nor the Shadow player's own possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, sword, toby));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shards, hides));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(toby);

		assertInZone(Zone.DISCARD, toby);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(sword, bilbo);
		assertInZone(Zone.SUPPORT, shards, hides);
		// The Orcs were discarded from a stack, not from play: neither copy offers to take them back.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesPossessionInPlayTheAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);

		var fist = scn.GetShadowCard("fist");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var tinker = scn.GetShadowCard("tinker");
		var hides = scn.GetShadowCard("hides");
		scn.MoveCardsToSupportArea(fist, hides);
		scn.StackCardsOn(fist, tender, warg, tinker);

		scn.StartGame();
		assertInZone(Zone.STACKED, tender, warg, tinker);
		assertEquals(3, scn.GetStackedCards(fist).size());
		assertInZone(Zone.SUPPORT, shards, hides);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(fist));
		scn.ShadowUseCardAction(fist);

		// The three stacked cards go with no choice, and (1) is removed; there is nothing to discard.
		assertInZone(Zone.DISCARD, tender, warg, tinker);
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, shards, hides);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoCardsStackedHereOrWithNoTwilightToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToSupportArea(toby);

		var fist = scn.GetShadowCard("fist");
		var fist2 = scn.GetShadowCard("fist2");
		var tender = scn.GetShadowCard("tender");
		var warg = scn.GetShadowCard("warg");
		var tinker = scn.GetShadowCard("tinker");
		var builder = scn.GetShadowCard("builder");
		var artisan = scn.GetShadowCard("artisan");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(fist, fist2);
		scn.StackCardsOn(fist, tender, warg);
		scn.StackCardsOn(fist2, builder, artisan, worker);

		scn.StartGame();
		assertEquals(2, scn.GetStackedCards(fist).size());
		assertEquals(3, scn.GetStackedCards(fist2).size());
		assertInZone(Zone.SUPPORT, toby);

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);
		scn.FreepsPass();

		// Two cards stacked on this copy, though three are on the other.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fist));
		assertTrue(scn.ShadowActionAvailable(fist2));

		// Three stacked, and an empty pool.
		scn.StackCardsOn(fist, tinker);
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetStackedCards(fist).size());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(fist));
		assertFalse(scn.ShadowActionAvailable(fist2));

		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(fist));
		assertTrue(scn.ShadowActionAvailable(fist2));
	}
}
