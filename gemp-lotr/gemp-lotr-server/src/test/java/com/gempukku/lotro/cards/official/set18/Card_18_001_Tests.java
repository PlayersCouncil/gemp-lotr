package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_001_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "18_1");
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("scrabbler", "2_64");     // Goblin Scrabbler: [moria] Orc, strength 4, vitality 1; its play trigger is never met (cheated in)
					put("hound", "7_192");        // Morgul Hound: [wraith] Orc, strength 5, vitality 2; its text needs 2 wounds on the Ring-bearer
					put("tracking", "15_171");    // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3, hunter 1 only
					put("herdsman", "15_83");     // Hunting Herdsman: [men] Man, strength 8, vitality 2, hunter 1; +3 for each follower (none here)
					put("pursuer", "4_190");      // Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its other text is about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gimli, Sprinter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Damage +1</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br>Each time Gimli wins a skirmish, you may add a threat to wound a minion (or wound a hunter minion twice).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Sprinter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WinningASkirmishGimliMayAddAThreatToWoundAnyOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var scrabbler = scn.GetShadowCard("scrabbler");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, scrabbler);
		assertEquals(0, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		// The Troop and the Scrabbler are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		// 6 + 1 for hunter against a non-hunter, against 5.
		assertEquals(7, scn.GetStrength(gimli));
		scn.PassCurrentPhaseActions();

		// The loser's wounds are on before the offer: 1 + 1 for Gimli's damage bonus.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());

		// Any minion, in the skirmish just fought or in none, of either race and culture; an exhausted one too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop, scrabbler));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// No hunter: one wound.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scrabbler));
		assertInZone(Zone.SHADOW_CHARACTERS, savage, troop, scrabbler);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWoundIsAWoundAndKillsAnExhaustedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var scrabbler = scn.GetShadowCard("scrabbler");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, scrabbler);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsAcceptOptionalTrigger();
		// Vitality 1: the Scrabbler could not exert, and is offered all the same.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, scrabbler));
		scn.FreepsChooseCard(scrabbler);

		assertInDiscard(scrabbler);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHunterMinionChosenIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var hound = scn.GetShadowCard("hound");
		var tracking = scn.GetShadowCard("tracking");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, tracking, herdsman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, hound);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		// The Hound, of vitality 2, is killed by its 2 wounds: only the two hunters are left.
		assertInDiscard(hound);
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());

		// A hunter of each culture and race.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tracking, herdsman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(tracking);

		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneHunterMinionInPlayItIsWoundedTwiceUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, tracking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(tracking));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());

		// A hunter minion can be wounded twice, so that is what is done (CR rulings on a parenthesis, Q-0035):
		// the Tracking Uruk is the only candidate, and takes both wounds with no question asked.
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoHunterMinionsInPlayOnlyTheyAreOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var tracking = scn.GetShadowCard("tracking");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, tracking, herdsman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());

		assertTrue(scn.FreepsHasCardChoiceAvailable(tracking, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, troop));
		scn.FreepsChooseCard(herdsman);

		// Vitality 2: the Herdsman is killed by the second wound.
		assertInDiscard(herdsman);
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNoThreatAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreatsAtTheLimitTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		// Two companions, Frodo and Gimli: no more than 2 threats.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishGimliIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		// 7 against 13: he loses, and is not overwhelmed (13 is less than 14).
		assertEquals(7, scn.GetStrength(gimli));
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningAgainInTheFierceRoundItIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetThreats());

		// The fierce round: Gimli against the Pursuer again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		// The Pursuer, of vitality 3, is killed by the loss; the Troop is still there to wound.
		assertInDiscard(pursuer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionLeftTheThreatIsAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(hound);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, hound);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();

		// The Hound, of vitality 2, is killed by its 2 wounds, and no minion is left.
		assertInDiscard(hound);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
