package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scouting", "7_68");
					put("scouting2", "7_68");

					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] Sméagol; to play, add a burden; a skirmish ability only
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] Sméagol; an assignment ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is no Sméagol; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("lord", "1_21");        // Lord of Moria: another Free Peoples condition in the support area; each Dwarf is damage +1

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("scout", "1_191");      // Moria Scout: [moria] Orc, vitality 2: can exert once and not twice; its play text is not reached cheated in
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its play text is not reached cheated in
					put("ambition", "1_133");   // Saruman's Ambition: a Shadow condition, which is no minion; a cost bonus and a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScoutingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Scouting
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot Sméagol.<br>Each time the fellowship moves, add a threat or discard this condition.<br><b>Maneuver:</b> Discard Sméagol to discard a minion. An opponent may exert a minion twice to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scouting");

		assertEquals("Scouting", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		// A Ring-bound companion who is no Sméagol.
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(scouting, smeagol);
		assertInHand(scouting, smeagol);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(scouting));

		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(scouting));

		scn.FreepsPlayCard(scouting);
		assertInZone(Zone.SUPPORT, scouting);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(scouting);
		assertInHand(scouting);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(scouting));
		scn.FreepsPlayCard(scouting);
		assertInZone(Zone.SUPPORT, scouting);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMoveOfTheFellowshipAddsAThreatWhenThatIsChosenAndAsksAgainOnTheRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(scouting);
		// Three companions with Frodo, so the threat limit (3) is no obstacle to two threats.
		scn.MoveCompanionsToTable(smeagol, sam);
		assertInZone(Zone.SUPPORT, scouting);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();

		// Moving from site 1 to site 2.
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SUPPORT, scouting);

		// No minions, so the game goes on to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");
		assertEquals(2, scn.GetThreats());
		assertInZone(Zone.SUPPORT, scouting);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToDiscardItLeavesTheThreatsAndAnotherConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("smeagol");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(scouting, lord);
		scn.MoveCompanionsToTable(smeagol);
		assertInZone(Zone.SUPPORT, scouting, lord);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard this condition");

		assertInDiscard(scouting);
		assertInZone(Zone.SUPPORT, lord);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreatsAtTheirLimitTheMoveDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToSupportArea(scouting);
		scn.MoveCompanionsToTable(smeagol);
		assertInZone(Zone.SUPPORT, scouting);

		scn.StartGame();
		// Frodo and Sméagol: a threat limit of 2.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(2, scn.GetThreats());

		scn.FreepsPass();

		// A threat cannot be added, so discarding is all that can be done.
		assertInDiscard(scouting);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCopiesEachAsksOnAMoveAndDiscardingOneLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var scouting2 = scn.GetFreepsCard("scouting2");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToSupportArea(scouting, scouting2);
		scn.MoveCompanionsToTable(smeagol);
		assertInZone(Zone.SUPPORT, scouting, scouting2);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsPass();

		// Each copy puts its own question.
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard this condition");

		// Only the copy that was asked is gone.
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetThreats());
		assertEquals(java.util.List.of("Add a threat", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Add a threat");

		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingInTheRegroupPhaseAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToMovementDecision();
		// Put down after the first move of the turn, so that the regroup decision is the first it sees.
		scn.MoveCardsToSupportArea(scouting);
		assertInZone(Zone.SUPPORT, scouting);
		assertEquals(0, scn.GetThreats());

		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.SUPPORT, scouting);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ManeuverAbilityDiscardsSmeagolToDiscardAMinionOfThePlayersChoiceWhenTheShadowPlayerDoesNotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(smeagol, sam);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var ambition = scn.GetShadowCard("ambition");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.MoveCardsToSupportArea(ambition);
		scn.SkipToPhase(Phase.MANEUVER);
		// Put down after the move from site 1, so that its trigger has nothing to ask.
		scn.MoveCardsToSupportArea(scouting);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, scouting, ambition);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(scouting));
		scn.FreepsUseCardAction(scouting);

		// The cost: Sméagol, the only one there is, to the discard pile; the condition stays.
		assertInDiscard(smeagol);
		assertInZone(Zone.SUPPORT, scouting);

		// The opponent is asked first, and declines.
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Minions of two cultures and races are offered; a Shadow condition and the companions are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ambition, sam, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertInDiscard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, ambition);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShireSmeagolPaysTooAndTheShadowPlayerPreventsTheDiscardByExertingAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(smeagol);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, scout, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.MoveCardsToSupportArea(scouting);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, scouting);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsActionAvailable(scouting));
		scn.FreepsUseCardAction(scouting);

		assertInDiscard(smeagol);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// Only minions able to exert twice: the Scout (vitality 2) could exert once only, the Runner not at all.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scout, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		// Two exertions, and no minion is discarded.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage, scout, runner);
		assertInDiscard(smeagol);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertTwiceThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.MoveCardsToSupportArea(scouting);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, scouting);
		assertInZone(Zone.SHADOW_CHARACTERS, scout, runner);
		scn.FreepsUseCardAction(scouting);

		assertInDiscard(smeagol);
		// Straight to the choice of the minion: the opponent has nothing to ask.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, runner));
		scn.FreepsChooseCard(scout);

		assertInDiscard(scout);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithoutSmeagol() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scouting = scn.GetFreepsCard("scouting");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.MoveCardsToSupportArea(scouting);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.SUPPORT, scouting);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(scouting));
	}
}
