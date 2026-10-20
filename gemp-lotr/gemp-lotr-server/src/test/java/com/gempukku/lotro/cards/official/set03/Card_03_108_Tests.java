package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_108_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pan", "3_108");
					put("pan2", "3_108");

					put("sam", "1_311");        // Sam, Son of Hamfast: Hobbit companion, strength 3, vitality 4
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: Hobbit ally, home 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, vitality 4
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability only
					put("smeagol", "103_116");  // Smeagol, Once Like Us: [shire] companion with no race, Ring-bound; to play, add a burden (cheated in); an assignment ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, vitality 3; Shadow: exert to add (1)
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll, vitality 4, damage +1 and fierce; its other texts are about playing it
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FryingPanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Frying Pan
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Game Text: Bearer must be a Hobbit.
		 * Skirmish: Exert bearer to wound an Orc he or she is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pan");

		assertEquals("Frying Pan", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAHobbitCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pan = scn.GetFreepsCard("pan");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(pan);
		// Smeagol is a [shire] companion but no Hobbit; Pippin is a Hobbit and not [shire].
		scn.MoveCompanionsToTable(sam, pippin, smeagol);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(pan));

		scn.FreepsPlayCard(pan);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, bilbo, pippin, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(pan, sam);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionExertsBearerToWoundAnOrcHeIsSkirmishingAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pan = scn.GetFreepsCard("pan");
		var pan2 = scn.GetFreepsCard("pan2");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var troll = scn.GetShadowCard("troll");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, pan);
		// Another Hobbit with a pan of his own, to show it is the bearer of the pan used who exerts.
		scn.AttachCardsTo(frodo, pan2);

		scn.StartGame();
		// The Cave Troll is of the [moria] culture, as the Goblin Patrol Troop is, but no Orc.
		scn.MoveMinionsToTable(troop, troop2, taskmaster, troll);

		scn.SkipToPhase(Phase.MANEUVER);
		// A skirmish action: not in the maneuver phase, where the Free Peoples player is asked for actions.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pan));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, troop},
				new PhysicalCardImpl[]{aragorn, troop2});
		// The Shadow player puts his other two minions on Sam.
		scn.ShadowAssignToMinions(sam, taskmaster, troll);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.FreepsActionAvailable(pan));

		scn.FreepsUseCardAction(pan);
		// Orcs of two cultures skirmishing Sam are offered; the Troll skirmishing him is not, and neither is
		// the Orc assigned to Aragorn, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, taskmaster));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troll, troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(taskmaster);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Frodo's pan: its bearer is not in the skirmish, so none of these Orcs is one he is skirmishing. An action
		// with a cost that can be paid is still offered (Q-0034); it exerts Frodo and wounds nobody, and no one
		// is asked whom to wound.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsUseCardAction(pan2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhenBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pan = scn.GetFreepsCard("pan");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, pan);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Vitality 4 with 3 wounds: Sam is exhausted. Frodo, another Hobbit, could still exert.
		scn.AddWoundsToChar(sam, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, troop);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop));
		assertFalse(scn.FreepsActionAvailable(pan));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
