package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guarded", "7_101");
					put("guarded2", "7_101");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion of another title; Knight is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("household", "5_83");   // Household Guard: a [rohan] Man companion; valiant is its whole text

					// At site 2 of the Fellowship path, minions of a higher site number are roaming.
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, site 4, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, site 5, damage +1 only
					put("goblinman", "2_42");   // Goblin Man: [isengard] Orc, site 2; a Shadow ability used only on purpose
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, site 2; text only with 2 burdens or wounds
					put("ambition", "1_133");   // Saruman's Ambition: a Shadow condition in the support area; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuardedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Guarded
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 3 [gondor] Men.<br>Each time the fellowship moves, add a threat or discard this condition.<br><b>Regroup:</b> Discard this condition to discard a minion (or all roaming minions).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guarded");

		assertEquals("Guarded", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilThreeGondorMenAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var boromir = scn.GetFreepsCard("boromir");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var household = scn.GetFreepsCard("household");
		scn.MoveCompanionsToTable(boromir, pippin, household);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(guarded, knight);
		assertInHand(guarded, knight);

		scn.StartGame();

		// Two [gondor] Men, a companion and an ally, beside a [gondor] Hobbit and a [rohan] Man.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(guarded));

		scn.FreepsPlayCard(knight);
		assertTrue(scn.FreepsPlayAvailable(guarded));

		scn.FreepsPlayCard(guarded);
		assertInZone(Zone.SUPPORT, guarded);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveOfTheFellowshipAsksForAThreatOrItsDiscardAndAThreatIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var boromir = scn.GetFreepsCard("boromir");
		// Frodo and Boromir: room for two threats.
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(guarded);
		assertInZone(Zone.SUPPORT, guarded);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// Moving from site 1 to site 2.
		scn.FreepsPass();
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, guarded);

		// No minion: on to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, guarded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAMoveItMayBeDiscardedInsteadOfAddingAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(guarded);
		assertInZone(Zone.SUPPORT, guarded);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		scn.FreepsPass();
		assertTrue(scn.FreepsChoiceAvailable("Add a threat"));
		scn.FreepsChooseOption("Discard this condition");

		assertInDiscard(guarded);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheMoveDiscardsItUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		scn.MoveCardsToSupportArea(guarded);
		assertInZone(Zone.SUPPORT, guarded);

		scn.StartGame();
		// Frodo is the only companion: one threat is all there may be.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());

		scn.FreepsPass();

		// A threat cannot be added, so discarding is all that can be done.
		assertFalse(scn.FreepsChoiceAvailable("Add a threat"));
		assertInDiscard(guarded);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCopiesEachAsksOnAMoveAndDiscardingOneLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var guarded2 = scn.GetFreepsCard("guarded2");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(guarded, guarded2);
		assertInZone(Zone.SUPPORT, guarded, guarded2);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());

		// The copy laid first answers first: discarding it discards that copy alone.
		scn.FreepsPass();
		scn.FreepsChooseOption("Discard this condition");
		assertInDiscard(guarded);
		assertInZone(Zone.SUPPORT, guarded2);

		// The second copy asks for itself.
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, guarded2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingInTheRegroupPhaseAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToMovementDecision();
		// Put down after the first move of the turn, so that the regroup decision is the first it sees.
		scn.MoveCardsToSupportArea(guarded);
		assertInZone(Zone.SUPPORT, guarded);
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, guarded);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void WithNoRoamingMinionTheRegroupAbilityDiscardsItAndAChosenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var guarded2 = scn.GetFreepsCard("guarded2");
		var goblinman = scn.GetShadowCard("goblinman");
		var nelya = scn.GetShadowCard("nelya");
		var ambition = scn.GetShadowCard("ambition");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		// Laid after the move from site 1, which would otherwise ask for a threat or a discard.
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(guarded, guarded2, ambition);
		scn.MoveMinionsToTable(goblinman, nelya);
		assertInZone(Zone.SUPPORT, guarded, guarded2, ambition);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman, nelya);
		// Nobody is assigned: both minions are in play when the regroup phase comes.
		scn.SkipToPhase(Phase.REGROUP);

		// At site 2, two minions of site 2 are not roaming.
		assertEquals(2, scn.GetCurrentSite().getBlueprint().getSiteNumber());
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertTrue(scn.FreepsActionAvailable(guarded));

		scn.FreepsUseCardAction(guarded);
		// This copy is the cost, not the other.
		assertInDiscard(guarded);
		assertInZone(Zone.SUPPORT, guarded2);

		// With no roaming minion there is no other way to go: a minion of either culture and race, not a Shadow
		// condition nor a companion.
		assertFalse(scn.FreepsChoiceAvailable("Discard all roaming minions"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(goblinman, nelya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ambition, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(nelya);

		assertInDiscard(nelya);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman);
		assertInZone(Zone.SUPPORT, ambition);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithRoamingMinionsTheRegroupAbilityDiscardsEveryRoamingMinionUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var goblinman = scn.GetShadowCard("goblinman");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(guarded);
		scn.MoveMinionsToTable(troop, savage, goblinman);
		assertInZone(Zone.SUPPORT, guarded);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage, goblinman);
		scn.SkipToPhase(Phase.REGROUP);

		// At site 2: the Troop (site 4) and the Savage (site 5) are roaming; Goblin Man (site 2) is not.
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertTrue(scn.FreepsActionAvailable(guarded));

		scn.FreepsUseCardAction(guarded);

		// The parenthesis has no price and no condition but its targets: with roaming minions to discard, it is
		// what is done, and nothing is asked.
		assertInDiscard(guarded);
		assertInDiscard(troop, savage);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneRoamingMinionTheRegroupAbilityDiscardsThatMinionUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var troop = scn.GetShadowCard("troop");
		var goblinman = scn.GetShadowCard("goblinman");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(guarded);
		scn.MoveMinionsToTable(troop, goblinman, nelya);
		assertInZone(Zone.SUPPORT, guarded);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, goblinman, nelya);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goblinman, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));

		scn.FreepsUseCardAction(guarded);

		// One roaming minion is enough for the parenthesis: the Troop goes, and nobody is chosen.
		assertInDiscard(guarded);
		assertInDiscard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman, nelya);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guarded = scn.GetFreepsCard("guarded");
		var goblinman = scn.GetShadowCard("goblinman");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(guarded);
		scn.MoveMinionsToTable(goblinman);
		assertInZone(Zone.SUPPORT, guarded);
		assertInZone(Zone.SHADOW_CHARACTERS, goblinman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(guarded));
	}
}
