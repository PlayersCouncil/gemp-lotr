package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_177_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "7_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTransferScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "7_177");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl, strength 9; a companion bearing a [wraith] condition is strength -2
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a Nazgûl, strength 10; her text needs 2 burdens or 2 wounds on the Ring-bearer
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1; no Nazgûl
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3
					put("farin", "1_11");       // Farin, Dwarven Emissary: strength 5, vitality 3; stronger only against an Orc
					// Four cards for the Free Peoples player's hand, which give him initiative
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "7_177");
					put("blade2", "7_177");
					put("breath", "1_207");     // Black Breath: another [wraith] condition with the same transfer; burdens and wounds may not be removed from bearer
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, strength 10; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, strength 12, fierce; nothing below 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl, strength 8; nothing below 3 burdens
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3, not overwhelmed unless his strength is tripled
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: strength 6, +3 while skirmishing a Nazgûl
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, strength 8, home site 3
					// Goblin Patrol Troop: no text.  Four in the Free Peoples hand give him the initiative.
					put("hand1", "1_177");
					put("hand2", "1_177");
					put("hand3", "1_177");
					put("hand4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FeelHisBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Feel His Blade
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Transfer this condition from your support area to a character skirmishing a Nazgûl.<br>During the turn this card is transferred, the Shadow has initiative, regardless of the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Feel His Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  The ability costs
	// nothing; with nobody skirmishing a Nazgûl, and once the condition has left the support area, there is
	// nothing for it to do.  It was offered all the same, and using it gave the Shadow initiative for the turn
	// though nothing was transferred.
	@Test
	public void TheTransferIsOfferedOnlyFromTheSupportAreaAndOnlyWhileSomebodyIsSkirmishingANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(aragorn, gimli, farin);
		scn.MoveCardsToHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));

		var blade = scn.GetShadowCard("blade");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(blade);
		assertInZone(Zone.SUPPORT, blade);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{farin, savage}, new PhysicalCardImpl[]{aragorn, lemenya},
				new PhysicalCardImpl[]{gimli, nelya});

		// Farin's skirmish is against an Uruk-hai: nobody is skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(farin);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		scn.ShadowPass();

		// Aragorn's is against Úlairë Lemenya.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(blade));
		scn.ShadowUseCardAction(blade);
		assertAttachedTo(blade, aragorn);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		scn.PassSkirmishActions();

		// Gimli's is against Úlairë Nelya: somebody is skirmishing a Nazgûl, and the condition is no longer in the
		// support area it is to be transferred from.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));
		assertAttachedTo(blade, aragorn);
	}

	@Test
	public void InASkirmishAgainstANazgulACopyInYourSupportAreaIsTransferredToTheCompanionFightingIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var blade2 = scn.GetShadowCard("blade2");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, boromir, arwen);

		scn.StartGame();
		scn.MoveCardsToSupportArea(blade, blade2);
		assertInZone(Zone.SUPPORT, blade, blade2);
		scn.MoveMinionsToTable(nelya, khamul, creeper);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, creeper},
				new PhysicalCardImpl[]{arwen, khamul});

		// First, Boromir against the Morgul Creeper, a [wraith] minion who is no Nazgûl.  Aragorn and Arwen are
		// assigned to Nazgûl, but their skirmishes have not begun: nobody is skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));
		assertFalse(scn.ShadowActionAvailable(blade2));
		scn.ShadowPass();

		// 8 against 7: Boromir loses.
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Now Aragorn against Nelya: each copy offers itself.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(blade));
		assertTrue(scn.ShadowActionAvailable(blade2));

		scn.ShadowUseCardAction(blade);

		// Aragorn is the one character skirmishing a Nazgûl, so nothing is asked; Arwen, still waiting for Khamûl,
		// is not given it.
		assertAttachedTo(blade, aragorn);
		assertNotAttachedTo(blade, arwen);
		assertInZone(Zone.SUPPORT, blade2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The copy on Aragorn is no longer in your support area and offers nothing; the other still does.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(blade));
		assertTrue(scn.ShadowActionAvailable(blade2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingANazgulIsGivenIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var khamul = scn.GetShadowCard("khamul");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(blade);
		assertInZone(Zone.SUPPORT, blade);
		scn.FreepsPass();
		// Site 3: Elrond's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, khamul);
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();
		assertInZone(Zone.SUPPORT, blade);
		assertTrue(scn.ShadowActionAvailable(blade));

		scn.ShadowUseCardAction(blade);

		assertAttachedTo(blade, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnotherConditionTransferredLeavesTheFreePeoplesInitiativeAndThisOneTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var breath = scn.GetShadowCard("breath");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(blade, breath);
		assertInZone(Zone.SUPPORT, blade, breath);
		scn.MoveMinionsToTable(nelya);

		// In the support area, untransferred, it leaves the Free Peoples player his initiative.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Black Breath, another [wraith] condition, is transferred to Aragorn the same way: nothing changes.
		scn.ShadowUseCardAction(breath);
		assertAttachedTo(breath, aragorn);
		assertInZone(Zone.SUPPORT, blade);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		// Then this card: the Shadow has the initiative, the Free Peoples hand still 4.
		scn.FreepsPass();
		scn.ShadowUseCardAction(blade);
		assertAttachedTo(blade, aragorn);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheShadowHasTheInitiativeForTheRestOfThatTurnAndNotOnLaterTurns() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(blade);
		assertInZone(Zone.SUPPORT, blade);
		scn.MoveMinionsToTable(nelya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.FreepsHasInitiative());
		scn.ShadowUseCardAction(blade);
		assertAttachedTo(blade, aragorn);
		assertTrue(scn.ShadowHasInitiative());

		// Nelya (10) beats Aragorn (8), who lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The regroup phase, the Free Peoples hand still 4.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());

		// After a second move, in the Shadow phase at site 3, it is still so.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.FreepsHasInitiative());

		// The next turn is player 2's: his fellowship, with a full hand, has the initiative.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertTrue(scn.GetShadowHandCount() >= 4);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());

		// Player 1's next turn: the card is still on Aragorn, but was not transferred this turn.
		scn.SkipToOtherPlayersTurn();
		// Site 3 is a sanctuary, and Aragorn is wounded.
		scn.FreepsDeclineSanctuaryHealing();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertAttachedTo(blade, aragorn);
		assertTrue(scn.GetFreepsHandCount() >= 4);
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
	}

	@Test
	public void OnceTheCardLeavesPlayWithItsBearerTheFreePeoplesPlayerHasTheInitiativeAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var blade = scn.GetShadowCard("blade");
		var nelya = scn.GetShadowCard("nelya");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(blade);
		assertInZone(Zone.SUPPORT, blade);
		scn.MoveMinionsToTable(nelya);
		// Vitality 3: one more wound kills him.
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, nelya);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		scn.ShadowUseCardAction(blade);
		assertAttachedTo(blade, boromir);
		assertTrue(scn.ShadowHasInitiative());

		// Nelya (10) beats Boromir (7) and the wound kills him; the card he bears goes with him.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, boromir);
		assertInDiscard(blade);

		// The same turn, the Free Peoples hand still 4: a card out of play gives the Shadow nothing.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertFalse(scn.ShadowHasInitiative());
	}
}
