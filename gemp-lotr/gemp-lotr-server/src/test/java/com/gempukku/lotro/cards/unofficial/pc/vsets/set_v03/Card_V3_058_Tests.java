package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_058_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("battlelines", "103_58");
					put("soldier", "4_254");      // Southron Soldier: [raider] Southron Man, strength 4, vitality 1; ambush (2) and no other text
					put("explorer", "4_250");     // Southron Explorer: Southron Man, strength 5, vitality 3, no ambush; a regroup ability never used here
					put("warrior", "4_222");      // Desert Warrior: Southron Man, strength 6, vitality 2; may remove (3) at the start of its skirmish to wound
					put("stacked1", "4_222");     // Desert Warriors that are only ever stacked on other cards
					put("stacked2", "4_222");
					put("stacked3", "4_222");
					put("stacked4", "4_222");
					put("lancers", "4_217");      // Desert Lancers: Southron Man; only ever in a discard pile here
					put("legion", "4_218");       // Desert Legion: unique Southron Man, strength 13, vitality 3, ambush (1); the rest of its text needs 4 burdens
					put("plunderer", "8_58");     // Corsair Plunderer: [raider] Man who is no Southron, strength 6, vitality 2; its text is for when it is played
					put("buccaneer", "8_53");     // Corsair Buccaneer: [raider] Man who is no Southron; only ever stacked here
					put("axeman", "4_224");       // Easterling Axeman: [raider] Man who is no Southron; only ever in the discard pile here
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text at all
					put("charger", "103_41");     // Bladetusk Charger: [raider] possession
					put("rearguard", "103_43");   // Bladetusk Rearguard: [raider] artifact. Maneuver: remove (5) to make it a Southron minion of strength 5, vitality 5
					put("ships", "8_65");         // Ships of Great Draught: [raider] possession
					put("sails", "8_50");         // Black Sails of Umbar: [raider] possession
					put("raiders", "4_242");      // Raiders From the East: [raider] condition
					put("larder", "8_23");        // Larder: [gollum] possession
					put("fury", "103_39");        // Savage Fury: [isengard] condition. Maneuver: discard a minion to make your [isengard] minion fierce or relentless

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_97");       // Boromir, Son of Denethor: strength 7, vitality 3
					put("moria", "1_21");         // Lord of Moria: Free Peoples condition in the support area; each Dwarf is damage +1
					put("moria2", "1_21");
					put("stranger", "1_108");     // No Stranger to the Shadows: Free Peoples condition on a ranger; each site's Shadow number is -1
					put("athelas", "1_94");       // Athelas: Free Peoples possession on a [gondor] Man
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShiftingBattlelinesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shifting Battle-lines
		 * Unique: true
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: At the start of each skirmish involving a minion with ambush, you may remove (2) to hinder a
		 * 	Free Peoples condition.
		 * 	Each time your Southron Man dies, you may remove (1) to stack a Southron from your discard pile on a
		 * 	[raider] item that already has a Southron stacked on it.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("battlelines");

		assertEquals("Shifting Battle-lines", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var battlelines = scn.GetShadowCard("battlelines");
		scn.MoveCardsToHand(battlelines);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 1 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.HAND, battlelines);
		assertTrue(scn.ShadowPlayAvailable(battlelines));

		scn.ShadowPlayCard(battlelines);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, battlelines);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfASkirmishInvolvingAMinionWithAmbushMayRemoveTwoToHinderAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stranger = scn.GetFreepsCard("stranger");
		var athelas = scn.GetFreepsCard("athelas");
		var moria = scn.GetFreepsCard("moria");
		var moria2 = scn.GetFreepsCard("moria2");
		scn.MoveCompanionsToTable(aragorn);
		// Free Peoples conditions: one on a companion, two in the support area. And a Free Peoples possession.
		scn.AttachCardsTo(aragorn, stranger, athelas);
		scn.MoveCardsToSupportArea(moria, moria2);

		var battlelines = scn.GetShadowCard("battlelines");
		var fury = scn.GetShadowCard("fury");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(battlelines, fury);

		scn.StartGame();
		// One of the support area conditions is hindered already.
		scn.HinderCard(moria2);
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, soldier);
		// The Soldier's own ambush (2) is not taken up.
		scn.ShadowDeclineOptionalTrigger();

		// 3 set + 2 companions + 1 for site 2, whose shadow number of 2 is -1 for No Stranger to the Shadows
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(moria));

		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));
		assertEquals(6, scn.GetTwilight());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(stranger, moria));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(moria2, athelas, battlelines, fury, aragorn));
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(stranger);

		assertTrue(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(moria));
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(aragorn));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningRemovesAndHindersNothingAndTheNextSkirmishWithAnAmbushMinionIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var stranger = scn.GetFreepsCard("stranger");
		var moria = scn.GetFreepsCard("moria");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, stranger);
		scn.MoveCardsToSupportArea(moria);

		var battlelines = scn.GetShadowCard("battlelines");
		var legion = scn.GetShadowCard("legion");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(battlelines);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, troop);
		// A [moria] Orc that has ambush from elsewhere.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.AMBUSH, 1));
		scn.SetTwilight(3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, legion);
		// The Legion's own ambush (1) is not taken up.
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowAssignToMinions(boromir, troop);

		// 3 set + 3 companions + 1 for site 2, whose shadow number of 2 is -1 for No Stranger to the Shadows
		assertEquals(7, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.IsHindered(stranger));
		assertFalse(scn.IsHindered(moria));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Desert Legion 13 beats Aragorn 8
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.IsCharSkirmishing(troop));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(stranger, moria));

		scn.ShadowChooseCard(moria);

		assertTrue(scn.IsHindered(moria));
		assertFalse(scn.IsHindered(stranger));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfASkirmishWhoseMinionHasNoAmbushThoughAnAmbushMinionWaitsForItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var moria = scn.GetFreepsCard("moria");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(moria);
		// A companion with ambush is no minion with ambush.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, aragorn, null, Keyword.AMBUSH, 2));

		var battlelines = scn.GetShadowCard("battlelines");
		var explorer = scn.GetShadowCard("explorer");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(battlelines);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, soldier);
		scn.SetTwilight(3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, explorer },
				new PhysicalCardImpl[] { boromir, soldier }
		);
		// The Soldier's own ambush (2) is not taken up.
		scn.ShadowDeclineOptionalTrigger();

		// 3 set + 3 companions + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());
		assertFalse(scn.HasKeyword(explorer, Keyword.AMBUSH));
		assertTrue(scn.HasKeyword(soldier, Keyword.AMBUSH));
		assertTrue(scn.IsCharAssigned(soldier));

		// Aragorn and the Explorer, a Southron with no ambush.
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishing(explorer));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Aragorn 8 beats the Explorer's 5
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertFalse(scn.IsHindered(moria));
		assertEquals(8, scn.GetTwilight());

		// The Soldier's skirmish is answered, on the same board.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));
	}

	@Test
	public void NotOfferedWithOneInThePoolAndOfferedWithExactlyTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var moria = scn.GetFreepsCard("moria");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToSupportArea(moria);

		var battlelines = scn.GetShadowCard("battlelines");
		var legion = scn.GetShadowCard("legion");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(battlelines);

		scn.StartGame();
		scn.MoveMinionsToTable(legion, troop);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.AMBUSH, 1));
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, legion);
		// The Legion's own ambush (1) is not taken up.
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowAssignToMinions(boromir, troop);

		scn.SetTwilight(1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.IsCharSkirmishing(legion));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.IsHindered(moria));

		scn.SetTwilight(2);
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));
		scn.ShadowAcceptOptionalTrigger();

		// The one Free Peoples condition there is.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsHindered(moria));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoAmbushMinionsInTheSkirmishItIsOfferedOnceAndWithNoFreePeoplesConditionRemovesTwoForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var athelas = scn.GetFreepsCard("athelas");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, athelas);

		var battlelines = scn.GetShadowCard("battlelines");
		var raiders = scn.GetShadowCard("raiders");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(battlelines, raiders);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, troop);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.AMBUSH, 1));
		scn.SetTwilight(3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, soldier);
		// The Soldier's own ambush (2) is not taken up.
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowAssignToMinions(aragorn, troop);

		// 3 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishing(soldier));
		assertTrue(scn.IsCharSkirmishing(troop));
		// The removal is a cost that can be paid, so the offer is made though no Free Peoples condition is in play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(5, scn.GetTwilight());
		assertFalse(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(raiders));
		assertFalse(scn.IsHindered(battlelines));
		// One skirmish, one offer, however many ambush minions are in it.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhenYourSouthronManDiesMayRemoveOneToStackASouthronFromYourDiscardPileOnARaiderItemWithASouthronStackedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var battlelines = scn.GetShadowCard("battlelines");
		var explorer = scn.GetShadowCard("explorer");
		var lancers = scn.GetShadowCard("lancers");
		var axeman = scn.GetShadowCard("axeman");
		var legion = scn.GetShadowCard("legion");
		var theirLegion = scn.GetFreepsCard("legion");
		var charger = scn.GetShadowCard("charger");
		var rearguard = scn.GetShadowCard("rearguard");
		var ships = scn.GetShadowCard("ships");
		var sails = scn.GetShadowCard("sails");
		var raiders = scn.GetShadowCard("raiders");
		var larder = scn.GetShadowCard("larder");
		scn.MoveCardsToSupportArea(battlelines, charger, rearguard, ships, sails, raiders, larder);
		// A [raider] possession and a [raider] artifact with a Southron stacked on each.
		scn.StackCardsOn(charger, scn.GetShadowCard("stacked1"));
		scn.StackCardsOn(rearguard, scn.GetShadowCard("stacked2"));
		// A [raider] possession with nothing stacked (the Ships), one with a corsair stacked, a [raider]
		// condition with a Southron stacked and a [gollum] possession with a Southron stacked.
		scn.StackCardsOn(sails, scn.GetShadowCard("buccaneer"));
		scn.StackCardsOn(raiders, scn.GetShadowCard("stacked3"));
		scn.StackCardsOn(larder, scn.GetShadowCard("stacked4"));
		// In the discard pile: another Southron and a [raider] Man who is no Southron. In the other player's: a Southron.
		scn.MoveCardsToDiscard(lancers, axeman);
		scn.MoveCardsToDiscard(theirLegion);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AddWoundsToChar(explorer, 2);
		scn.SetTwilight(3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		// The Explorer has no ambush: the start of its skirmish is not answered.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// 3 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.DECK, legion);

		scn.PassSkirmishActions();

		// Aragorn 8 beats the Explorer's 5, and the wound is its third.
		assertInZone(Zone.DISCARD, explorer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));
		assertEquals(7, scn.GetTwilight());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(6, scn.GetTwilight());
		// Where: the game asks this first.
		assertTrue(scn.ShadowHasCardChoiceAvailable(charger, rearguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ships, sails, raiders, larder, battlelines));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertEquals(1, scn.GetStackedCards(rearguard).size());
		scn.ShadowChooseCard(rearguard);

		// What: the Southron that has just died and the one that was in the discard pile before.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, lancers));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman, legion, theirLegion));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(lancers);

		assertInZone(Zone.STACKED, lancers);
		assertEquals(2, scn.GetStackedCards(rearguard).size());
		assertTrue(scn.GetStackedCards(rearguard).contains(lancers));
		assertEquals(1, scn.GetStackedCards(charger).size());
		assertEquals(0, scn.GetStackedCards(ships).size());
		assertInZone(Zone.DISCARD, explorer, axeman, theirLegion);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningStacksNothingAndASecondSouthronManDyingIsOfferedAgainAndMayItselfBeStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var battlelines = scn.GetShadowCard("battlelines");
		var explorer = scn.GetShadowCard("explorer");
		var warrior = scn.GetShadowCard("warrior");
		var charger = scn.GetShadowCard("charger");
		scn.MoveCardsToSupportArea(battlelines, charger);
		scn.StackCardsOn(charger, scn.GetShadowCard("stacked1"));

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, warrior);
		scn.AddWoundsToChar(explorer, 2);
		scn.AddWoundsToChar(warrior, 1);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, explorer },
				new PhysicalCardImpl[] { boromir, warrior }
		);
		// (2) is too little for the Desert Warrior's own text at the start of its skirmish.
		scn.SetTwilight(2);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn 8 beats the Explorer's 5
		assertInZone(Zone.DISCARD, explorer);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetStackedCards(charger).size());
		assertInZone(Zone.DISCARD, explorer);

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// Boromir 7 beats the Desert Warrior's 6
		assertInZone(Zone.DISCARD, warrior);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, warrior));
		// The Southron whose death this answers. The Charger, the one item with a Southron stacked on it, is
		// where it goes.
		scn.ShadowChooseCardBPFromSelection(warrior);

		assertInZone(Zone.STACKED, warrior);
		assertEquals(2, scn.GetStackedCards(charger).size());
		assertTrue(scn.GetStackedCards(charger).contains(warrior));
		assertInZone(Zone.DISCARD, explorer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenASouthronManIsDiscardedFromPlayNorWhenACorsairDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var battlelines = scn.GetShadowCard("battlelines");
		var fury = scn.GetShadowCard("fury");
		var explorer = scn.GetShadowCard("explorer");
		var plunderer = scn.GetShadowCard("plunderer");
		var warrior = scn.GetShadowCard("warrior");
		var charger = scn.GetShadowCard("charger");
		scn.MoveCardsToSupportArea(battlelines, fury, charger);
		scn.StackCardsOn(charger, scn.GetShadowCard("stacked1"));

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, plunderer, warrior);
		scn.AddWoundsToChar(plunderer, 1);
		scn.AddWoundsToChar(warrior, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 3 companions + 2 for the shadow number of site 2
		assertEquals(5, scn.GetTwilight());

		// A Southron Man is discarded from play, by Savage Fury's cost, and not killed.
		scn.ShadowUseCardAction(fury);
		scn.ShadowChooseCard(explorer);

		assertInZone(Zone.DISCARD, explorer);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, plunderer },
				new PhysicalCardImpl[] { boromir, warrior }
		);
		// (2) is too little for the Desert Warrior's own text at the start of its skirmish.
		scn.SetTwilight(2);

		// A [raider] Man who is a corsair is killed: Aragorn 8 beats the Plunderer's 6.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, plunderer);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(1, scn.GetStackedCards(charger).size());
		assertEquals(2, scn.GetTwilight());

		// A Southron Man killed on the same board is answered: Boromir 7 beats the Desert Warrior's 6.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, warrior);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));
	}

	@Test
	public void NotOfferedWhenABladetuskThatIsASouthronMinionAndNoManDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var battlelines = scn.GetShadowCard("battlelines");
		var rearguard = scn.GetShadowCard("rearguard");
		var charger = scn.GetShadowCard("charger");
		var explorer = scn.GetShadowCard("explorer");
		scn.MoveCardsToSupportArea(battlelines, rearguard, charger);
		scn.StackCardsOn(charger, scn.GetShadowCard("stacked1"));

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.SetTwilight(4);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 4 set + 2 companions + 2 for the shadow number of site 2
		assertEquals(8, scn.GetTwilight());

		// Bladetusk Rearguard: remove (5) to make it a Southron minion of strength 5 and vitality 5, with
		// nothing stacked on it.
		scn.ShadowUseCardAction(rearguard);
		assertInZone(Zone.SHADOW_CHARACTERS, rearguard);
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertFalse(scn.IsRace(rearguard, Race.MAN));
		assertEquals(5, scn.GetStrength(rearguard));
		assertEquals(5, scn.GetVitality(rearguard));
		assertEquals(3, scn.GetTwilight());
		scn.AddWoundsToChar(rearguard, 4);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rearguard);
		// The Explorer is left out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn 8 beats the Rearguard's 5, and the wound is its fifth.
		assertInZone(Zone.DISCARD, rearguard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetStackedCards(charger).size());
		assertEquals(3, scn.GetTwilight());
		// The Rearguard was fierce, and is gone: the Explorer has no fierce round to fight.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithAnEmptyPoolAndOfferedWithExactlyOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var battlelines = scn.GetShadowCard("battlelines");
		var explorer = scn.GetShadowCard("explorer");
		var warrior = scn.GetShadowCard("warrior");
		var charger = scn.GetShadowCard("charger");
		scn.MoveCardsToSupportArea(battlelines, charger);
		scn.StackCardsOn(charger, scn.GetShadowCard("stacked1"));

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, warrior);
		scn.AddWoundsToChar(explorer, 2);
		scn.AddWoundsToChar(warrior, 1);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, explorer },
				new PhysicalCardImpl[] { boromir, warrior }
		);
		scn.SetTwilight(0);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, explorer);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(1, scn.GetStackedCards(charger).size());

		scn.SetTwilight(1);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, warrior);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, warrior));

		// The Southron whose death went unanswered is still in the discard pile to be stacked.
		scn.ShadowChooseCardBPFromSelection(explorer);
		assertInZone(Zone.STACKED, explorer);
		assertEquals(2, scn.GetStackedCards(charger).size());
		assertInZone(Zone.DISCARD, warrior);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoItemThatHasASouthronStackedOnItItIsStillOfferedAndRemovesOneForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var battlelines = scn.GetShadowCard("battlelines");
		var explorer = scn.GetShadowCard("explorer");
		var charger = scn.GetShadowCard("charger");
		var sails = scn.GetShadowCard("sails");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(battlelines, charger, sails, raiders);
		// A [raider] item with nothing stacked, one with a corsair stacked, and a [raider] condition with a Southron stacked.
		scn.StackCardsOn(sails, scn.GetShadowCard("buccaneer"));
		scn.StackCardsOn(raiders, scn.GetShadowCard("stacked3"));

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AddWoundsToChar(explorer, 2);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, explorer);
		// The removal is a cost that can be paid, so the offer is made.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(battlelines));
		// 2 companions + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.DISCARD, explorer);
		assertEquals(0, scn.GetStackedCards(charger).size());
		assertEquals(1, scn.GetStackedCards(sails).size());
		assertEquals(1, scn.GetStackedCards(raiders).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
