package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alcarin", "5_31");
					put("knight", "5_35");        // Gondorian Knight: [gondor] Man, cost 2, vitality 3; Knight is his whole text
					put("pippin", "103_31");      // Pippin, Trollslayer: a knight who is a Hobbit, cost 2; to play, spot a [gondor] companion
					put("ranger", "7_116");       // Ranger of Osgiliath: [gondor] Man, cost 2, no knight; a regroup ability only

					// Second Level: [gondor] fortification. "Maneuver: Exert a [gondor] Man or spot a ranger to
					// transfer this condition from your support area to a minion."
					put("secondlevel", "7_118");
					put("wall", "5_33");          // City Wall: [gondor] fortification, bearer is strength -3; its transfer is a skirmish ability
					put("sparewall", "5_33");     // a fortification that lies in the support area, on no minion
					// Disarmed: a [gondor] condition a minion bears that is no fortification (strength -1; bearer
					// cannot bear Shadow cards).
					put("disarmed", "18_46");
					// Unexpected Visitor: on a [gondor] companion. Each minion gains "Assignment: Assign this
					// minion to bearer of Unexpected Visitor."
					put("visitor", "7_126");

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("axeman", "4_224");       // Easterling Axeman: [raider] Man, vitality 2; his text needs 2 burdens
					put("runner", "1_178");       // Goblin Runner: harmless when cheated in
					// Morgul Spawn: "While you can spot a Nazgûl, the Free Peoples player must exert a companion
					// to assign this minion to a skirmish."
					put("spawn", "7_200");
					put("lemenya", "1_232");      // Úlairë Lemenya: the Nazgûl to spot; her text is about [wraith] conditions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AlcarinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Alcarin, Warrior of Lamedon
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>. The twilight cost of each other knight in your starting fellowship is -1.<br><b>Assignment:</b> Assign Alcarin to a minion bearing a [gondor] fortification to heal Alcarin.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alcarin");

		assertEquals("Alcarin", card.getBlueprint().getTitle());
		assertEquals("Warrior of Lamedon", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void InTheStartingFellowshipEachOtherKnightCostsOneLessOnceAlcarinIsDown() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var ranger = scn.GetFreepsCard("ranger");

		// While a player chooses his starting fellowship the game keeps the total of what he has played in the
		// twilight pool.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(alcarin, knight, ranger));
		assertEquals(2, scn.GetTwilightCost(knight));
		assertEquals(2, scn.GetTwilightCost(pippin));
		assertEquals(2, scn.GetTwilightCost(ranger));

		scn.FreepsChooseCard(alcarin);

		// His own 3 is not lowered.
		assertInZone(Zone.FREE_CHARACTERS, alcarin);
		assertEquals(3, scn.GetTwilight());
		// A knight, Man or Hobbit, now costs 1 and makes the 4; the Ranger's 3 + 2 would be 5.
		assertEquals(1, scn.GetTwilightCost(knight));
		assertEquals(1, scn.GetTwilightCost(pippin));
		assertEquals(2, scn.GetTwilightCost(ranger));
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger));

		scn.FreepsChooseCard(knight);

		// 3 + 1 is the 4: the other knight's 1 no longer fits, and this player is asked nothing more.
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertInZone(Zone.DECK, pippin);
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		// The other player's knights are no business of this Alcarin: his Knight costs him the printed 2.
		var hisKnight = scn.GetShadowCard("knight");
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
		scn.ShadowChooseCard(hisKnight);
		assertInZone(Zone.FREE_CHARACTERS, hisKnight);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Starting fellowship"));
	}

	@Test
	public void AKnightPlayedAheadOfAlcarinKeepsItsCostAndAlcarinKeepsHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");

		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(alcarin, knight));

		scn.FreepsChooseCard(knight);

		// Alcarin is still in the draw deck and lowers nothing.
		assertInZone(Zone.DECK, alcarin);
		assertEquals(2, scn.GetTwilight());
		// Another 2 fits; Alcarin's own 3 does not, as it would if he were a knight who cost 1 less himself.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(alcarin));
	}

	@Test
	public void PlayedForThreeAndAKnightPlayedInTheFellowshipPhaseKeepsItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCardsToHand(alcarin, knight);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(alcarin));
		scn.FreepsPlayCard(alcarin);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, alcarin);

		// Alcarin is in play, but this is no starting fellowship: 3 + the Knight's printed 2.
		scn.FreepsPlayCard(knight);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AssignsAlcarinToAMinionBearingAGondorFortificationAndHealsHimOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var knight = scn.GetFreepsCard("knight");
		var secondlevel = scn.GetFreepsCard("secondlevel");
		var wall = scn.GetFreepsCard("wall");
		var sparewall = scn.GetFreepsCard("sparewall");
		var disarmed = scn.GetFreepsCard("disarmed");
		scn.MoveCompanionsToTable(alcarin, knight);
		scn.MoveCardsToSupportArea(secondlevel, sparewall);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var axeman = scn.GetShadowCard("axeman");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, axeman, runner);
		scn.AttachCardsTo(troop, wall);
		scn.AttachCardsTo(axeman, disarmed);
		scn.AddWoundsToChar(alcarin, 1);
		scn.AddWoundsToChar(knight, 1);

		// Second Level goes onto the Savage by its own maneuver ability, and Alcarin exerts for it.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(secondlevel);
		scn.FreepsChooseCard(alcarin);
		scn.FreepsChooseCard(savage);
		assertAttachedTo(secondlevel, savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetWoundsOn(alcarin));
		assertFalse(scn.IsCharAssigned(alcarin));
		assertTrue(scn.FreepsActionAvailable(alcarin));

		scn.FreepsUseCardAction(alcarin);

		// The two minions that bear a [gondor] fortification, an Uruk-hai and an Orc; not the Man who bears a
		// [gondor] condition that is none, nor the Runner beside a fortification that nobody bears.
		assertInZone(Zone.SUPPORT, sparewall);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axeman, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(savage);

		assertTrue(scn.IsCharAssignedAgainst(alcarin, savage));
		assertFalse(scn.IsCharAssigned(troop));
		// One wound of his two is healed.
		assertEquals(1, scn.GetWoundsOn(alcarin));
		// The other knight is left as he was.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.IsCharAssigned(knight));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileNoMinionBearsAGondorFortification() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var sparewall = scn.GetFreepsCard("sparewall");
		var disarmed = scn.GetFreepsCard("disarmed");
		scn.MoveCompanionsToTable(alcarin);
		scn.MoveCardsToSupportArea(sparewall);

		var axeman = scn.GetShadowCard("axeman");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, runner);
		scn.AttachCardsTo(axeman, disarmed);
		scn.AddWoundsToChar(alcarin, 1);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// A [gondor] condition on a minion that is no fortification, and a fortification on no minion.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertAttachedTo(disarmed, axeman);
		assertInZone(Zone.SUPPORT, sparewall);
		assertFalse(scn.FreepsActionAvailable(alcarin));
	}

	@Test
	public void NotOfferedOnceAlcarinIsAlreadyAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var wall = scn.GetFreepsCard("wall");
		var visitor = scn.GetFreepsCard("visitor");
		scn.MoveCompanionsToTable(alcarin);
		scn.AttachCardsTo(alcarin, visitor);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.AttachCardsTo(savage, wall);
		scn.AddWoundsToChar(alcarin, 1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(alcarin));
		scn.FreepsPass();

		// The Shadow player assigns the Runner to the bearer of Unexpected Visitor.
		scn.ShadowUseCardAction(runner);
		assertTrue(scn.IsCharAssignedAgainst(alcarin, runner));

		// The Savage still bears a fortification, but Alcarin can no longer be assigned to it.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsCharAssigned(savage));
		assertFalse(scn.FreepsActionAvailable(alcarin));
		assertEquals(1, scn.GetWoundsOn(alcarin));
	}

	@Test
	public void AnUnwoundedAlcarinMayStillAssignHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(alcarin);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.AttachCardsTo(savage, wall);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetWoundsOn(alcarin));
		assertTrue(scn.FreepsActionAvailable(alcarin));
		scn.FreepsUseCardAction(alcarin);

		// The assignment is what he pays, and it can be paid with nothing to heal.
		assertTrue(scn.IsCharAssignedAgainst(alcarin, savage));
		assertFalse(scn.IsCharAssigned(runner));
		assertEquals(0, scn.GetWoundsOn(alcarin));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void AMorgulSpawnBearingAFortificationIsPaidForWithAnExertionWhenAlcarinAssignsHimselfToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var knight = scn.GetFreepsCard("knight");
		var wall = scn.GetFreepsCard("wall");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(alcarin, knight);

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya);
		scn.AttachCardsTo(spawn, wall);
		scn.AddWoundsToChar(alcarin, 2);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(alcarin));
		scn.FreepsUseCardAction(alcarin);

		// The Spawn is the one minion bearing a fortification, so it is the one chosen; its price is asked
		// now. Alcarin, exhausted, cannot pay it himself.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(alcarin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.IsCharAssigned(alcarin));

		scn.FreepsChooseCard(knight);

		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharAssignedAgainst(alcarin, spawn));
		assertFalse(scn.IsCharAssigned(lemenya));
		assertEquals(1, scn.GetWoundsOn(alcarin));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void WithNobodyAbleToExertAMorgulSpawnBearingAFortificationIsNotHisToAssignHimselfTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alcarin = scn.GetFreepsCard("alcarin");
		var wall = scn.GetFreepsCard("wall");
		var sparewall = scn.GetFreepsCard("sparewall");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(alcarin);

		var spawn = scn.GetShadowCard("spawn");
		var lemenya = scn.GetShadowCard("lemenya");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(spawn, lemenya, savage);
		scn.AttachCardsTo(spawn, wall);
		// Vitality 3 with 2 wounds and vitality 4 with 3: neither companion can exert.
		scn.AddWoundsToChar(alcarin, 2);
		scn.AddWoundsToChar(frodo, 3);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertAttachedTo(wall, spawn);
		assertFalse(scn.FreepsActionAvailable(alcarin));

		// With a second fortification on a minion that asks no price, the ability is offered and Alcarin goes
		// to that minion without a choice.
		scn.AttachCardsTo(savage, sparewall);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(alcarin));
		scn.FreepsUseCardAction(alcarin);

		assertTrue(scn.IsCharAssignedAgainst(alcarin, savage));
		assertFalse(scn.IsCharAssigned(spawn));
		assertEquals(1, scn.GetWoundsOn(alcarin));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}
}
