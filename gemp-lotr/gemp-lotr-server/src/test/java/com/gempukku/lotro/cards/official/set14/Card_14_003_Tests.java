package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrohir", "14_3");
					put("elladan", "14_2");     // Elladan, Son of Elrond: the other twin; his text lowers costs, which no test here reads
					put("legolas", "1_50");     // Legolas, Greenleaf: an [elven] Elf companion of another title, cost 2; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Elf; a maneuver ability only

					// Spring Forth Nimbly: [elven] Skirmish event, "Make an Elf strength +1 for each card that has an
					// [elven] token on it": with no token in play it gives +0
					put("nimbly", "13_24");
					put("nimbly2", "13_24");
					// An Honorable Charge: [rohan] Skirmish event, "Make an unbound companion strength +1 for each [rohan]
					// Man you spot (limit +3)": with no [rohan] Man it gives +0
					put("charge", "4_282");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, strength 7, damage +1
					put("gollum", "7_59");      // Gollum, Vile Creature: strength 5; his Skirmish ability is never used
					// Controlled By The Ring: on Gollum; "Skirmish: If Gollum is not assigned to a skirmish, discard this
					// condition to have him replace a minion skirmishing a companion"
					put("controlled", "15_41");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrohirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Elrohir, Son of Elrond
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Elladan is twilight cost -2.<br>Each time you play an [elven] event during a skirmish involving Elrohir or Elladan, each minion in that skirmish is strength -2.
		*/

		var scn = GetScenario();

		var elrohir = scn.GetFreepsCard("elrohir");

		assertEquals("Elrohir", elrohir.getBlueprint().getTitle());
		assertEquals("Son of Elrond", elrohir.getBlueprint().getSubtitle());
		assertTrue(elrohir.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, elrohir.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, elrohir.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, elrohir.getBlueprint().getCardType());
		assertEquals(Race.ELF, elrohir.getBlueprint().getRace());
		assertEquals(3, elrohir.getBlueprint().getTwilightCost());
		assertEquals(7, elrohir.getBlueprint().getStrength());
		assertEquals(3, elrohir.getBlueprint().getVitality());
		assertEquals(6, elrohir.getBlueprint().getResistance());
	}

	@Test
	public void WhileElrohirIsInPlayElladanCostsTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrohir = scn.GetFreepsCard("elrohir");
		var elladan = scn.GetFreepsCard("elladan");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(elrohir, elladan, legolas);

		scn.StartGame();
		assertInHand(elrohir, elladan, legolas);
		assertEquals(3, scn.GetTwilightCost(elladan));
		assertEquals(2, scn.GetTwilightCost(legolas));

		scn.FreepsPlayCard(elrohir);
		assertInZone(Zone.FREE_CHARACTERS, elrohir);

		// Legolas, another Elf companion, is not Elladan and keeps his printed cost.
		assertEquals(1, scn.GetTwilightCost(elladan));
		assertEquals(2, scn.GetTwilightCost(legolas));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(elladan);
		// 3 printed - 2
		assertEquals(pool + 1, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, elladan);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachElvenEventPlayedInHisSkirmishMakesEachMinionThereStrengthMinus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrohir = scn.GetFreepsCard("elrohir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nimbly = scn.GetFreepsCard("nimbly");
		var nimbly2 = scn.GetFreepsCard("nimbly2");
		scn.MoveCompanionsToTable(elrohir, aragorn);
		scn.MoveCardsToHand(nimbly, nimbly2);

		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, party, warrior);
		// Enough to outlast 9 + 5 after the two events, so that both minions live on into the next skirmish.
		scn.ApplyAdHocModifier(new StrengthModifier(null, elrohir, null, 10));
		assertInHand(nimbly, nimbly2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elrohir, troop}, new PhysicalCardImpl[]{aragorn, warrior});
		scn.ShadowAssignToMinions(elrohir, party);

		scn.FreepsResolveSkirmish(elrohir);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(party));
		assertEquals(7, scn.GetStrength(warrior));

		scn.FreepsPlayCard(nimbly);
		// Both minions of his skirmish, of two cultures and races; the Warrior waits in Aragorn's skirmish.
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(party));
		assertEquals(7, scn.GetStrength(warrior));

		scn.ShadowPass();
		scn.FreepsPlayCard(nimbly2);
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(party));
		assertEquals(7, scn.GetStrength(warrior));

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(9, scn.GetStrength(troop));
		scn.PassCurrentPhaseActions();

		// Each skirmish is a phase of its own: in Aragorn's, the minions Elrohir beat are back to their printed strength.
		scn.FreepsResolveSkirmish(aragorn);
		assertInPlay(troop, party);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(party));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NeitherAnElvenEventInAnotherSkirmishNorAnotherCulturesEventInHisWeakensAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrohir = scn.GetFreepsCard("elrohir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nimbly = scn.GetFreepsCard("nimbly");
		var charge = scn.GetFreepsCard("charge");
		scn.MoveCompanionsToTable(elrohir, aragorn);
		scn.MoveCardsToHand(nimbly, charge);

		var troop = scn.GetShadowCard("troop");
		var warrior = scn.GetShadowCard("warrior");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, warrior);
		assertInHand(nimbly, charge);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elrohir, troop}, new PhysicalCardImpl[]{aragorn, warrior});

		// Aragorn's skirmish first: Elrohir is assigned, and his skirmish has not begun.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPlayCard(nimbly);
		assertInDiscard(nimbly);
		assertEquals(7, scn.GetStrength(warrior));
		assertEquals(13, scn.GetStrength(troop));
		scn.PassCurrentPhaseActions();

		scn.FreepsResolveSkirmish(elrohir);
		scn.FreepsPlayCard(charge);
		scn.FreepsChooseCard(elrohir);
		assertInDiscard(charge);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnElvenEventInElladansSkirmishWeakensTheMinionsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrohir = scn.GetFreepsCard("elrohir");
		var elladan = scn.GetFreepsCard("elladan");
		var nimbly = scn.GetFreepsCard("nimbly");
		scn.MoveCompanionsToTable(elrohir, elladan);
		scn.MoveCardsToHand(nimbly);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		assertInHand(nimbly);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elladan, troop);

		scn.FreepsResolveSkirmish(elladan);
		assertFalse(scn.IsCharAssigned(elrohir));
		assertEquals(13, scn.GetStrength(troop));
		scn.FreepsPlayCard(nimbly);
		scn.FreepsChooseCard(elladan);
		assertEquals(11, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionThatComesIntoHisSkirmishAfterTheEventIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrohir = scn.GetFreepsCard("elrohir");
		var nimbly = scn.GetFreepsCard("nimbly");
		scn.MoveCompanionsToTable(elrohir);
		scn.MoveCardsToHand(nimbly);

		var troop = scn.GetShadowCard("troop");
		var gollum = scn.GetShadowCard("gollum");
		var controlled = scn.GetShadowCard("controlled");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, gollum);
		scn.AttachCardsTo(gollum, controlled);
		assertAttachedTo(controlled, gollum);
		assertInHand(nimbly);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrohir, troop);
		// Gollum is left over, and stays unassigned.
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(elrohir);
		scn.FreepsPlayCard(nimbly);
		assertEquals(11, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(gollum));

		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowUseCardAction(controlled);
		assertTrue(scn.IsCharSkirmishingAgainst(elrohir, gollum));
		assertFalse(scn.IsCharSkirmishing(troop));

		// The minions it reached were the ones in the skirmish when the event was played: Gollum keeps his 5, and the
		// Troop keeps its -2 to the end of the skirmish.
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(11, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
