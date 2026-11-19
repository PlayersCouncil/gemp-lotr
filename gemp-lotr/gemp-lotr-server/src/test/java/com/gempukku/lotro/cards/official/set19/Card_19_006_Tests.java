package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "19_6");
					put("uruviel", "15_26");    // Uruviel, Woodland Maid: an [elven] follower; her text answers only a transfer
					put("woodhall", "18_17");   // Woodhall Elf, Exile: an [elven] follower; its text answers only a transfer
					put("radagast", "13_38");   // Radagast, Tender of Beasts: a [gandalf] follower; his text wants 2 [gandalf] companions
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a third companion; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5; its play trigger is not reached by a cheat
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, Damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					// Dwarf Guards with no text that reaches play: the hand laid for the reconcile, and draw deck to fill it from.
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
					put("guard5", "1_7");
					put("guard6", "1_7");
					put("guard7", "1_7");
					put("guard8", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Arwen, Royal Maiden
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>.<br>While you can spot 2 [elven] followers, each minion skirmishing Arwen is strength -2.<br><b>Maneuver:</b> Add 3 threats to reconcile your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Royal Maiden", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WithTwoElvenFollowersEachMinionSkirmishingArwenIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var frodo = scn.GetRingBearer();
		var uruviel = scn.GetFreepsCard("uruviel");
		var woodhall = scn.GetFreepsCard("woodhall");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		// Two [elven] followers: one in the support area, one borne by Frodo.
		scn.MoveCardsToSupportArea(uruviel);
		scn.AttachCardsTo(frodo, woodhall);
		assertInZone(Zone.SUPPORT, uruviel);
		assertAttachedTo(woodhall, frodo);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, runner}, new PhysicalCardImpl[]{frodo, troop});
		// The Uruk-hai, left over, is put on Arwen by the Shadow player: two minions in her skirmish.
		scn.ShadowAssignToMinions(arwen, savage);

		// Assigned to her, but not yet skirmishing her.
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsResolveSkirmish(arwen);
		assertEquals(3, scn.GetStrength(runner));
		assertEquals(3, scn.GetStrength(savage));
		// Assigned to Frodo, whose skirmish is still to come.
		assertEquals(13, scn.GetStrength(troop));

		// 6 against 3 + 3: Arwen loses, and lives through the Uruk-hai's extra wound.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(arwen));
		// In a skirmish of another companion, the minion keeps its strength.
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneElvenFollowerTheMinionSkirmishingArwenKeepsItsStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var uruviel = scn.GetFreepsCard("uruviel");
		var radagast = scn.GetFreepsCard("radagast");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen);
		// One [elven] follower, a follower of another culture, and Arwen herself, an [elven] card that is no follower.
		scn.MoveCardsToSupportArea(uruviel, radagast);
		assertInZone(Zone.SUPPORT, uruviel, radagast);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ManeuverAbilityAddsThreeThreatsToReconcileHerPlayersHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var guard3 = scn.GetFreepsCard("guard3");
		// Three companions: room for three threats.
		scn.MoveCompanionsToTable(arwen, aragorn);

		scn.StartGame();
		scn.SetFreepsHand(guard1, guard2, guard3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		int freepsDeck = scn.GetFreepsDeckCount();
		int shadowHand = scn.GetShadowHandCount();

		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(arwen));

		scn.FreepsUseCardAction(arwen);
		assertEquals(3, scn.GetThreats());

		// The reconcile: one card may be discarded first ...
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(guard1);
		assertInDiscard(guard1);

		// ... and the hand is drawn up from two cards to eight.
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(freepsDeck - 6, scn.GetFreepsDeckCount());
		assertInHand(guard2, guard3);
		// "Your hand": the Shadow player's is left alone.
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhenThreeThreatsCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, aragorn);

		scn.StartGame();
		// Three companions and one threat: two more could be added, three cannot.
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arwen));
		// Aragorn's own maneuver ability is offered beside it.
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(1, scn.GetThreats());
	}
}
