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

public class Card_07_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear1", "7_108");
					put("spear2", "7_108");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability only
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6; Knight is its whole text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, strength 6; a maneuver ability never used
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is a Hobbit
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; a "when you play" only, never reached
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KnightsSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Knight's Spear
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [gondor] Man.
		 * 	<b>Skirmish:</b> If you cannot spot 2 threats, add a threat to make bearer strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear1");

		assertEquals("Knight's Spear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnAGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear1 = scn.GetFreepsCard("spear1");
		var spear2 = scn.GetFreepsCard("spear2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(spear1, spear2);
		assertInHand(spear1, spear2);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(spear1));
		scn.FreepsPlayCard(spear1);
		// A [gondor] companion and the [gondor] ally, both Men; not a [gondor] Hobbit, a Man of Rohan or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(spear1, aragorn);

		// Aragorn now holds a hand weapon, so the Merchant is the one bearer left for the second copy.
		scn.FreepsPlayCard(spear2);
		assertAttachedTo(spear2, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityAddsAThreatToMakeBearerStrengthPlusOneUntilTwoThreatsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear1 = scn.GetFreepsCard("spear1");
		var spear2 = scn.GetFreepsCard("spear2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		// Four companions, so room for four threats; a copy on each of two [gondor] Men.
		scn.MoveCompanionsToTable(aragorn, knight, guard);
		scn.AttachCardsTo(aragorn, spear1);
		scn.AttachCardsTo(knight, spear2);
		assertAttachedTo(spear1, aragorn);
		assertAttachedTo(spear2, knight);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Two burdens: tokens that are no threats.
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, troop}, new PhysicalCardImpl[]{guard, savage});
		// The Knight's skirmish; Aragorn is in none.
		scn.FreepsResolveSkirmish(knight);

		assertFalse(scn.IsCharAssigned(aragorn));
		assertEquals(0, scn.GetThreats());
		assertEquals(10, scn.GetStrength(aragorn));     // 8 + 2
		assertEquals(8, scn.GetStrength(knight));       // 6 + 2
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(spear1));

		// At 0 threats: a threat added, and Aragorn, who bears this copy, is +1; the Knight is not.
		scn.FreepsUseCardAction(spear1);
		assertEquals(1, scn.GetThreats());
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(knight));
		assertEquals(2, scn.GetBurdens());

		// At 1 threat, the most "cannot spot 2" allows: the Knight's copy, and the Knight alone is +1.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(spear2));
		scn.FreepsUseCardAction(spear2);
		assertEquals(2, scn.GetThreats());
		assertEquals(9, scn.GetStrength(knight));
		assertEquals(11, scn.GetStrength(aragorn));

		// At 2 threats, with room for two more: neither copy is offered.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(spear1));
		assertFalse(scn.FreepsActionAvailable(spear2));
		assertEquals(11, scn.GetStrength(aragorn));

		// The bonuses last the skirmish phase they were made in: in the next skirmish they are gone.
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(knight));
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(spear1));
	}

	@Test
	public void BorneByTheAllyItIsUsedOnceAndThenNotOfferedWhileAThreatCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear1 = scn.GetFreepsCard("spear1");
		var frodo = scn.GetRingBearer();
		var merchant = scn.GetFreepsCard("merchant");
		// Frodo the one companion: one threat is all there is room for.
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, spear1);
		assertAttachedTo(spear1, merchant);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(0, scn.GetThreats());
		assertEquals(8, scn.GetStrength(merchant));     // 6 + 2
		assertEquals(4, scn.GetStrength(frodo));        // 3 + 1 for The Ruling Ring
		assertTrue(scn.FreepsActionAvailable(spear1));

		// The bearer is the ally, and he is the one made +1.
		scn.FreepsUseCardAction(spear1);
		assertEquals(1, scn.GetThreats());
		assertEquals(9, scn.GetStrength(merchant));
		assertEquals(4, scn.GetStrength(frodo));

		// One threat and one companion: "cannot spot 2" still holds, but no threat can be added.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(spear1));
		assertEquals(1, scn.GetThreats());
	}
}
