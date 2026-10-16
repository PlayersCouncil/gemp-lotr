package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flee1", "2_33");
					put("flee2", "2_33");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability
					// Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Gimli, Son of Glóin: a [dwarven] companion, strength 6, no archer; a skirmish ability used only on purpose
					put("gimli", "1_13");
					// Gondorian Merchant: a [gondor] ally, strength 6; a maneuver ability used only on purpose
					put("merchant", "7_97");
					// One Whom Men Would Follow: "Maneuver: Exert Aragorn to spot an ally. Until the regroup phase, that
					// ally is strength +2 and participates in archery fire and skirmishes."
					put("follow", "1_109");

					// Corpse Lights: a [sauron] Wraith, strength 7, vitality 2; a skirmish ability used only on purpose
					put("lights", "6_99");
					// Orc Swordsman: a [sauron] Orc, vitality 3; strength +2 while he bears a weapon
					put("swordsman", "3_98");
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text
					put("troop2", "1_177");
					// Uruk Fighter: an [isengard] Uruk-hai, vitality 2; his maneuver ability needs 5 companions
					put("fighter", "1_146");
					// Goblin Runner: a [moria] Orc, strength 5, vitality 1; his play trigger is not reached, as he is cheated in
					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FleeinTerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Flee in Terror
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a [gondor] companion wins a skirmish, discard an exhausted Orc. That minion's owner may remove (3) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flee1");

		assertEquals("Flee in Terror", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAGondorCompanionWinsItDiscardsTheChosenExhaustedOrcUnlessItsOwnerRemoves3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flee1 = scn.GetFreepsCard("flee1");
		var flee2 = scn.GetFreepsCard("flee2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lights = scn.GetShadowCard("lights");
		var swordsman = scn.GetShadowCard("swordsman");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var fighter = scn.GetShadowCard("fighter");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(flee1, flee2);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, swordsman, troop1, troop2, fighter);
		// Exhausted: the Swordsman and the first Troop (vitality 3, 2 wounds), and the Fighter, an Uruk-hai
		// (vitality 2, 1 wound).  The second Troop has 1 wound and is not exhausted.
		scn.AddWoundsToChar(swordsman, 2);
		scn.AddWoundsToChar(troop1, 2);
		scn.AddWoundsToChar(troop2, 1);
		scn.AddWoundsToChar(fighter, 1);
		assertEquals(2, scn.GetWoundsOn(swordsman));
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(1, scn.GetWoundsOn(fighter));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		// Aragorn 8 against Corpse Lights' 7: he wins, and Corpse Lights is left exhausted by the wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(lights));
		assertTrue(scn.FreepsPlayAvailable(flee1));

		scn.FreepsPlayCard(flee1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(swordsman, troop1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(fighter, troop2, lights, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(swordsman);

		// The Swordsman's owner declines to pay: he is discarded, and the pool is as it was.
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();
		assertInDiscard(flee1, swordsman);
		assertInZone(Zone.SHADOW_CHARACTERS, troop1, troop2, fighter, lights);
		assertEquals(5, scn.GetTwilight());

		// A second copy answers the same win: this time the owner removes (3), and the Troop stays.
		assertTrue(scn.FreepsPlayAvailable(flee2));
		scn.FreepsPlayCard(flee2);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(flee2);
		assertInZone(Zone.SHADOW_CHARACTERS, troop1);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithLessThan3InThePoolTheOwnerIsNotAskedAndTheOrcIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flee = scn.GetFreepsCard("flee1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lights = scn.GetShadowCard("lights");
		var swordsman = scn.GetShadowCard("swordsman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(flee);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, swordsman);
		scn.AddWoundsToChar(swordsman, 2);
		assertEquals(2, scn.GetWoundsOn(swordsman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		assertEquals(2, scn.GetTwilight());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsPlayAvailable(flee));
		// The Swordsman, the one exhausted Orc, is chosen unasked.
		scn.FreepsPlayCard(flee);

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertInDiscard(flee, swordsman);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoExhaustedOrcItIsPlayedForNothingAndNobodyIsAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flee = scn.GetFreepsCard("flee1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lights = scn.GetShadowCard("lights");
		var troop = scn.GetShadowCard("troop2");
		var fighter = scn.GetShadowCard("fighter");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(flee);

		scn.StartGame();
		scn.MoveMinionsToTable(lights, troop, fighter);
		// An Orc with a wound who is not exhausted, and an exhausted Uruk-hai.
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(fighter, 1);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(fighter));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lights);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());

		scn.PassSkirmishActions();
		assertTrue(scn.FreepsPlayAvailable(flee));
		scn.FreepsPlayCard(flee);

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertInDiscard(flee);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, fighter, lights);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheGondorCompanionLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flee = scn.GetFreepsCard("flee1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop1");
		var swordsman = scn.GetShadowCard("swordsman");
		var runner = scn.GetShadowCard("runner1");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(flee);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, swordsman, runner);
		scn.AddWoundsToChar(swordsman, 2);
		assertEquals(2, scn.GetWoundsOn(swordsman));
		scn.SkipToAssignments();
		// Gimli's skirmish waits, so that the moment after Aragorn's is the choice of the next skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{gimli, runner});
		scn.ShadowDeclineAssignments();

		// The Troop's 13 against Aragorn's 8: he loses, and lives with 1 wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsPlayAvailable(flee));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertInZone(Zone.SHADOW_CHARACTERS, swordsman);
	}

	@Test
	public void OfferedWhenAGondorHobbitWinsAndNotWhenACompanionOfAnotherCultureDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flee = scn.GetFreepsCard("flee1");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var swordsman = scn.GetShadowCard("swordsman");
		scn.MoveCompanionsToTable(pippin, gimli);
		scn.MoveCardsToHand(flee);

		scn.StartGame();
		// Pippin 3 + 5: enough to beat a Runner.
		scn.ApplyAdHocModifier(new StrengthModifier(null, pippin, null, 5));
		scn.MoveMinionsToTable(runner1, runner2, swordsman);
		scn.AddWoundsToChar(swordsman, 2);
		assertEquals(2, scn.GetWoundsOn(swordsman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner1}, new PhysicalCardImpl[]{pippin, runner2});
		scn.ShadowDeclineAssignments();

		// Gimli 6 against the Runner's 5: a [dwarven] companion wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertFalse(scn.FreepsPlayAvailable(flee));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Pippin 8 against the other Runner's 5: a [gondor] Hobbit wins.
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertTrue(scn.FreepsPlayAvailable(flee));
		scn.SetTwilight(0);
		scn.FreepsPlayCard(flee);
		assertInDiscard(flee, swordsman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAGondorAllyWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flee = scn.GetFreepsCard("flee1");
		var follow = scn.GetFreepsCard("follow");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var swordsman = scn.GetShadowCard("swordsman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(flee, follow);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2, swordsman);
		scn.AddWoundsToChar(swordsman, 2);
		assertEquals(2, scn.GetWoundsOn(swordsman));
		scn.SkipToPhase(Phase.MANEUVER);
		// The Merchant, strength 6 + 2, takes part in skirmishes until the regroup phase.
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.PassManeuverActions();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merchant, runner1}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.ShadowDeclineAssignments();

		// The Merchant 8 against the Runner's 5: a [gondor] ally wins.
		scn.FreepsResolveSkirmish(merchant);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertFalse(scn.FreepsPlayAvailable(flee));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertInZone(Zone.SHADOW_CHARACTERS, swordsman);
	}
}
