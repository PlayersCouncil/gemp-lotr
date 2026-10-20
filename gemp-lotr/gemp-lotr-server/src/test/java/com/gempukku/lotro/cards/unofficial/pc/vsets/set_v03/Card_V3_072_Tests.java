package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_072_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "103_72");
					put("warrior1", "4_165");   // Orthanc Warrior: [isengard] Uruk-hai, cost 2, site 5; damage +1 and nothing else
					put("warrior2", "4_165");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, cost 2, site 3; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5, strength 5; damage +1 and nothing else
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, cost 2; never leaves the draw deck
					put("whelp", "7_202");      // Morgul Whelp: [ringwraith] Orc, cost 1; its text needs a wound on the Ring-bearer
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, cost 1; adds (2) when played
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, cost 3
					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, cost 4, site 5; damage +1 and nothing else
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: [ringwraith] Nazgul, cost 4, site 3; a Shadow ability never used here
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: [ringwraith] Nazgul, cost 4, site 3, strength 9; weakens only bearers of [ringwraith] conditions
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, cost 6, strength 13; no text
					put("detachment", "7_191"); // Morgul Detachment: [ringwraith] Orc, cost 7, site 4; fierce and damage +1 beside a Nazgul
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: Nazgul, cost 8
					put("power1", "1_211");     // Drawn to Its Power: [ringwraith] condition, cost 2, to the support area; answers only a companion's death
					put("power2", "1_211");
					put("host", "1_187");       // Host of Thousands: Shadow event, cost 0: play a [moria] Orc from your discard pile
					put("tentacle", "19_19");   // Reaching Tentacle: [moria] minion, cost 2, site 4; nothing on being played; discarded when not at a marsh

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Attea, Bestowed with Dominion
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 7
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		 * 	Each time you play a minion from hand, you may exert Ulaire Attea to play another minion with the
		 * 	same twilight cost from your discard pile.
		 * 	Skirmish: Hinder another minion to make Ulaire Attea strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Bestowed with Dominion", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(7, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlaysForSevenAndTwoForRoamingAndAsksNothingWithNoOtherMinionOfCostSevenInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var troop = scn.GetShadowCard("troop");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCardsToHand(attea);
		// Minions of cost 6 and 8.
		scn.MoveCardsToDiscard(troop, witchking);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(attea));

		scn.ShadowPlayCard(attea);

		// 7 to play + 2 roaming: her site number is 3 and the fellowship is at site 2.
		assertEquals(twilight - 9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(0, scn.GetWoundsOn(attea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(troop, witchking);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HerOwnPlayFromHandSetsOffHerTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var detachment = scn.GetShadowCard("detachment");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(attea);
		scn.MoveCardsToDiscard(detachment, troop);

		scn.StartGame();
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(attea);

		// She is a minion played from hand, and she is in play when her play is answered.
		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(attea));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));

		scn.ShadowChooseOptionalTrigger(attea);

		// The only other minion of cost 7 in the discard pile is played without a choice.
		assertEquals(1, scn.GetWoundsOn(attea));
		assertInZone(Zone.SHADOW_CHARACTERS, detachment);
		assertInDiscard(troop);
		// 7 to play + 2 roaming: the Detachment's site number is 4.
		assertEquals(twilight - 9, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingAMinionFromHandMayExertAtteaToPlayAnotherMinionOfThatCostFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var marksman = scn.GetShadowCard("marksman");
		var raiding = scn.GetShadowCard("raiding");
		var power2 = scn.GetShadowCard("power2");
		// A second minion of cost 2 stays in hand, and a third in the draw deck.
		scn.MoveCardsToHand(warrior1, savage);
		// In the discard pile: two minions of cost 2, one of them a second copy of the minion to be played;
		// minions of cost 1, 3 and 4; a card of cost 2 that is no minion.
		scn.MoveCardsToDiscard(warrior2, brigand, runner, marksman, raiding, power2);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(30);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertInZone(Zone.DECK, soldier);

		scn.ShadowPlayCard(warrior1);

		// 2 to play + 2 roaming
		assertEquals(twilight - 4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(attea));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));

		scn.ShadowChooseOptionalTrigger(attea);

		assertEquals(1, scn.GetWoundsOn(attea));
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior2, brigand));
		// Cost 4 is what the Warrior came to with the roaming penalty: its twilight cost is still 2.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, marksman, raiding, power2, savage, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCardBPFromSelection(brigand);

		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInDiscard(warrior2);
		// 2 to play + 2 roaming: the Brigand's site number is 3.
		assertEquals(twilight - 4 - 4, scn.GetTwilight());
		// The Brigand came from the discard pile, not from hand: nothing more is asked, though another minion
		// of cost 2 is still in the discard pile and Attea could exert again.
		assertEquals(1, scn.GetWoundsOn(attea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMinionPlayedFromHandAsksAgainByItsOwnCostUntilAtteaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var lemenya = scn.GetShadowCard("lemenya");
		var raiding = scn.GetShadowCard("raiding");
		var otsea = scn.GetShadowCard("otsea");
		var marksman = scn.GetShadowCard("marksman");
		// Minions of two cultures and two costs to play from hand.
		scn.MoveCardsToHand(warrior1, lemenya, savage);
		scn.MoveCardsToDiscard(warrior2, brigand, raiding, otsea, marksman);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(40);
		scn.FreepsPass();

		scn.ShadowPlayCard(warrior1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowChooseOptionalTrigger(attea);
		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior2, brigand));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiding, otsea, marksman));
		scn.ShadowChooseCardBPFromSelection(brigand);
		assertEquals(1, scn.GetWoundsOn(attea));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);

		// A [ringwraith] minion of cost 4: now it is the minions of cost 4 that are offered.
		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowChooseOptionalTrigger(attea);
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiding, otsea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warrior2, marksman));
		scn.ShadowChooseCardBPFromSelection(raiding);
		assertEquals(2, scn.GetWoundsOn(attea));
		assertInZone(Zone.SHADOW_CHARACTERS, raiding);
		assertInDiscard(otsea, warrior2);

		// Vitality 3 with 2 wounds: she cannot exert, so a third minion from hand asks nothing, though a
		// minion of its cost is in the discard pile.
		assertTrue(scn.IsExhausted(attea));
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInDiscard(warrior2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesAtteaThePoolAndTheDiscardPileAsTheyWere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(warrior1);
		scn.MoveCardsToDiscard(warrior2, brigand);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(warrior1);

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(attea));
		assertInDiscard(warrior2, brigand);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardThatIsNoMinionPlayedFromHandAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var power1 = scn.GetShadowCard("power1");
		var warrior1 = scn.GetShadowCard("warrior1");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(power1, warrior1);
		scn.MoveCardsToDiscard(brigand);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(30);
		scn.FreepsPass();

		// A condition of cost 2, with a minion of cost 2 in the discard pile.
		scn.ShadowPlayCard(power1);

		assertInZone(Zone.SUPPORT, power1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A minion of cost 2 on the same table does ask.
		scn.ShadowPlayCard(warrior1);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
	}

	@Test
	public void AMinionPlayedFromTheDiscardPileByAnotherCardAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var host = scn.GetShadowCard("host");
		var runner = scn.GetShadowCard("runner");
		var whelp = scn.GetShadowCard("whelp");
		scn.MoveCardsToHand(host);
		// The Runner is the only [moria] Orc for the event; the Whelp is a second minion of cost 1.
		scn.MoveCardsToDiscard(runner, whelp);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(30);
		scn.FreepsPass();

		scn.ShadowPlayCard(host);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInDiscard(whelp);
		assertEquals(0, scn.GetWoundsOn(attea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotAskedWithNoMinionOfThatCostInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var warrior1 = scn.GetShadowCard("warrior1");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");
		var marksman = scn.GetShadowCard("marksman");
		var power2 = scn.GetShadowCard("power2");
		// Minions of cost 2 in hand and in the draw deck; in the discard pile minions of cost 1 and 3 and a
		// condition of cost 2.
		scn.MoveCardsToHand(warrior1, savage);
		scn.MoveCardsToDiscard(runner, marksman, power2);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(30);
		scn.FreepsPass();

		assertInZone(Zone.DECK, soldier);

		scn.ShadowPlayCard(warrior1);

		assertInZone(Zone.SHADOW_CHARACTERS, warrior1);
		assertEquals(0, scn.GetWoundsOn(attea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionDiscardedAsItIsPlayedIsNotItselfTheOtherMinionOfThatCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var tentacle = scn.GetShadowCard("tentacle");
		var runner = scn.GetShadowCard("runner");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(tentacle);
		// No minion of cost 2 in the discard pile.
		scn.MoveCardsToDiscard(runner, marksman);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(30);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();

		// Site 2 is no marsh: the Tentacle is discarded as soon as it is in play.
		scn.ShadowPlayCard(tentacle);

		// 2 to play + 2 roaming
		assertEquals(twilight - 4, scn.GetTwilight());
		assertInDiscard(tentacle);
		// The only minion of cost 2 in the discard pile is the one just played, which is not another minion.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotAskedWhenThePoolCannotPayForTheMinionInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var warrior1 = scn.GetShadowCard("warrior1");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(warrior1, savage);
		scn.MoveCardsToDiscard(brigand);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(8);
		scn.FreepsPass();

		// 8 set + 1 companion + 2 for the site
		assertEquals(11, scn.GetTwilight());

		// 2 to play + 2 roaming leaves 7: enough for the Brigand, who costs 2 + 2 roaming.
		scn.ShadowPlayCard(warrior1);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowDeclineOptionalTrigger();

		// 2 to play + 2 roaming leaves 3: one short of the Brigand.
		scn.ShadowPlayCard(savage);
		assertEquals(3, scn.GetTwilight());
		assertInDiscard(brigand);
		assertEquals(0, scn.GetWoundsOn(attea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityHindersAnotherMinionForStrengthPlusOneEachTimeWhileOneIsLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var troop = scn.GetShadowCard("troop");
		var lemenya = scn.GetShadowCard("lemenya");
		var savage = scn.GetShadowCard("savage");
		var power1 = scn.GetShadowCard("power1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// Beside Attea: a [moria] Orc, a [ringwraith] Nazgul, and a minion that is hindered already.
		scn.MoveMinionsToTable(attea, troop, lemenya, savage);
		scn.HinderCard(savage);
		// A Shadow card in play that is no minion.
		scn.MoveCardsToSupportArea(power1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(attea));
		assertFalse(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(lemenya));
		assertTrue(scn.ShadowActionAvailable(attea));

		scn.ShadowUseCardAction(attea);

		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, lemenya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(attea, savage, power1, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(troop);

		assertTrue(scn.IsHindered(troop));
		assertFalse(scn.IsHindered(lemenya));
		assertFalse(scn.IsHindered(attea));
		assertEquals(14, scn.GetStrength(attea));

		// No limit: a second use hinders the one minion left to hinder, without a choice.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(attea));
		scn.ShadowUseCardAction(attea);

		assertTrue(scn.IsHindered(lemenya));
		assertEquals(15, scn.GetStrength(attea));

		// With no other minion left unhindered the cost cannot be paid.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(attea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void StrengthBonusLastsForTheSkirmishItIsUsedInAndIsGoneInHerFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(13, scn.GetStrength(attea));
		scn.ShadowUseCardAction(attea);
		scn.ShadowChooseCard(troop);
		assertEquals(14, scn.GetStrength(attea));

		// Still so when the Shadow player is next asked, the last decision before the skirmish resolves.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(14, scn.GetStrength(attea));

		// 14 against Aragorn's 8: he loses and is not overwhelmed.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishing(attea));
		assertEquals(13, scn.GetStrength(attea));
		// The hindering was the cost and is no part of what ends.
		assertTrue(scn.IsHindered(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheAbilityMayBeUsedInASkirmishAtteaIsNotIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.IsCharSkirmishing(attea));
		assertEquals(13, scn.GetStrength(attea));
		assertTrue(scn.ShadowActionAvailable(attea));

		scn.ShadowUseCardAction(attea);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage));
		scn.ShadowChooseCard(troop);

		assertTrue(scn.IsHindered(troop));
		assertEquals(14, scn.GetStrength(attea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
