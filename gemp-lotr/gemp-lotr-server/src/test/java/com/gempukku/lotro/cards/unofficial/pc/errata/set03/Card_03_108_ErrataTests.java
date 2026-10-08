package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_108_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pan", "53_108");
					put("pan2", "53_108");

					put("sam", "1_311");        // Sam, Son of Hamfast: Hobbit companion, strength 3, vitality 4
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: Hobbit ally, home 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, vitality 4

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, vitality 3; Shadow: exert to add (1)
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1
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
		 * Twilight Cost: 1
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
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysForOneOnAHobbitCompanionOrAllyAndMakesBearerStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pan = scn.GetFreepsCard("pan");
		var sam = scn.GetFreepsCard("sam");
		var bilbo = scn.GetFreepsCard("bilbo");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(pan);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bilbo);
		// Aragorn is of the [shire] culture but no Hobbit.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.SHIRE));

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsPlayAvailable(pan));

		scn.FreepsPlayCard(pan);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);

		assertAttachedTo(pan, sam);
		assertEquals(1, scn.GetTwilight());
		assertEquals(4, scn.GetStrength(sam));
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
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, pan);
		// Another Hobbit with a pan of his own, to show it is the bearer of the pan used who exerts.
		scn.AttachCardsTo(frodo, pan2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2, taskmaster, raiders);
		// The Uruk-hai is of the [moria] culture, as the Goblin Patrol Troop is, but no Orc.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, raiders, null, Culture.MORIA));

		scn.SkipToPhase(Phase.MANEUVER);
		// A skirmish action: not in the maneuver phase.
		assertFalse(scn.FreepsActionAvailable(pan));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, troop},
				new PhysicalCardImpl[]{aragorn, troop2});
		// The Shadow player puts his other two minions on Sam.
		scn.ShadowAssignToMinions(sam, taskmaster, raiders);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertTrue(scn.FreepsActionAvailable(pan));

		scn.FreepsUseCardAction(pan);
		// Orcs of two cultures skirmishing Sam are offered; the Uruk-hai skirmishing him is not, and neither is
		// the Orc assigned to Aragorn, whose skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, taskmaster));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(raiders, troop2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(taskmaster);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(raiders));
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
		assertEquals(0, scn.GetWoundsOn(raiders));
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
