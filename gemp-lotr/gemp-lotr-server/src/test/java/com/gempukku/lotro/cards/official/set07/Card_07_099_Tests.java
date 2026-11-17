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

public class Card_07_099_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gate", "7_99");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, strength 6, Knight and no other text
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] Man companion, strength 6; a regroup ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit, strength 3; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally; a maneuver ability used only on purpose
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, Valiant and no other text

					// [gondor] fortifications, each with a maneuver transfer used only on purpose
					put("second", "7_118");     // Second Level
					put("level", "7_92");       // First Level, strength -1 on a minion that bears it
					put("slits", "5_80");       // Arrow-slits: a [rohan] fortification; its maneuver ability needs 2 [rohan] Men
					put("rally", "5_38");       // Rally Point: a [gondor] condition that is no fortification; a regroup ability

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text, vitality 3
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, Damage +1 and no other text
					put("savage2", "1_151");
					put("runner1", "1_178");    // Goblin Runner: a [moria] Orc, strength 5, vitality 1, so exhausted as he stands
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatGateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Great Gate
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [gondor] companion strength +2 and, if you can spot 2 [gondor] fortifications, exert a minion that companion is skirmishing.
		*/

		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");

		assertEquals("Great Gate", gate.getBlueprint().getTitle());
		assertNull(gate.getBlueprint().getSubtitle());
		assertFalse(gate.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, gate.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, gate.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, gate.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(gate, Timeword.SKIRMISH));
		assertEquals(1, gate.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithTwoGondorFortificationsItStrengthensAGondorCompanionAndExertsAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var guard = scn.GetFreepsCard("guard");
		var second = scn.GetFreepsCard("second");
		var level = scn.GetFreepsCard("level");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(second);
		scn.MoveCardsToHand(gate);
		assertInZone(Zone.SUPPORT, second);
		assertInHand(gate);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, savage2, runner1);
		// One [gondor] fortification in the support area and one borne by a minion: both are spotted.
		scn.AttachCardsTo(savage2, level);
		assertAttachedTo(level, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{frodo, savage2});
		// Two more minions put on the Knight by the Shadow player.
		scn.ShadowAssignToMinions(knight, savage, runner1);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertTrue(scn.FreepsPlayAvailable(gate));

		scn.FreepsPlayCard(gate);
		// [gondor] companions, skirmishing or not, of two races; not the [gondor] ally, the [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);

		assertEquals(8, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(pippin));
		// Either minion he is skirmishing that can exert; not the exhausted Runner, nor the minion waiting for Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner1, savage2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertInDiscard(gate);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneGondorFortificationNoMinionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight = scn.GetFreepsCard("knight");
		var second = scn.GetFreepsCard("second");
		var slits = scn.GetFreepsCard("slits");
		var rally = scn.GetFreepsCard("rally");
		scn.MoveCompanionsToTable(knight);
		// One [gondor] fortification, beside a [rohan] fortification and a [gondor] condition that is no fortification.
		scn.MoveCardsToSupportArea(second, slits, rally);
		scn.MoveCardsToHand(gate);
		assertInZone(Zone.SUPPORT, second, slits, rally);
		assertInHand(gate);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.FreepsPlayCard(gate);
		assertEquals(8, scn.GetStrength(knight));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(gate);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACompanionWhoseSkirmishIsStillToComeIsStrengthenedAndNoMinionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var second = scn.GetFreepsCard("second");
		var level = scn.GetFreepsCard("level");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveCardsToSupportArea(second, level);
		scn.MoveCardsToHand(gate);
		assertInZone(Zone.SUPPORT, second, level);
		assertInHand(gate);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{ranger, savage});
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(6, scn.GetStrength(ranger));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The Ranger is assigned to the Savage, but his skirmish has not begun: he is skirmishing nobody.
		scn.FreepsPlayCard(gate);
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, ranger));
		scn.FreepsChooseCard(ranger);

		assertEquals(8, scn.GetStrength(ranger));
		assertEquals(6, scn.GetStrength(knight));
		// Neither the minion of the skirmish under way nor the one waiting for the Ranger.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(gate);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndItsBonusIsGoneInTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(knight, ranger);
		scn.MoveCardsToHand(gate);
		assertInHand(gate);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gate));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, runner1}, new PhysicalCardImpl[]{ranger, runner2});
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(gate));

		scn.FreepsPlayCard(gate);
		scn.FreepsChooseCard(knight);
		assertEquals(8, scn.GetStrength(knight));

		// The Knight, at 8, beats the Runner; in the next skirmish he is back to 6.
		scn.PassCurrentPhaseActions();
		assertInDiscard(runner1);
		scn.FreepsResolveSkirmish(ranger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(knight));
	}

	@Test
	public void WithNoGondorCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var second = scn.GetFreepsCard("second");
		var level = scn.GetFreepsCard("level");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(second, level);
		scn.MoveCardsToHand(gate);
		assertInZone(Zone.SUPPORT, second, level);
		assertInHand(gate);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		// An event's target is no requirement (CR 4.2 "effect").
		assertTrue(scn.FreepsPlayAvailable(gate));

		scn.FreepsPlayCard(gate);
		assertInDiscard(gate);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
