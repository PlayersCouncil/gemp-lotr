package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pledge", "11_64");
					put("pledge2", "11_64");
					// Guard of the White Tree: [gondor] Man companion, strength 7, resistance 5.  Its skirmish ability
					// discards a [gondor] condition from the support area, and none is laid.
					put("guard", "13_67");
					// Gondorian Knight: [gondor] Man companion, strength 6, resistance 6, Knight and nothing else.
					put("knight", "5_35");
					// Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, resistance 6.  Enduring, so he
					// is kept unwounded; his skirmish ability (exert him twice) is never used.
					put("pippin", "103_31");
					// Gondorian Merchant: the [gondor] ally, strength 6.  His ability needs initiative and two
					// cards discarded, and is never used.
					put("merchant", "7_97");
					// Arwen, Daughter of Elrond: an [elven] companion, strength 6 (+3 only against a Nazgûl).
					put("arwen", "1_30");

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PledgeofLoyaltyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Pledge of Loyalty
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [gondor] companion strength +2 (or +3 if he or she has resistance 4 or more).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pledge");

		assertEquals("Pledge of Loyalty", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGondorCompanionStrengthPlusTwoBelowResistance4AndPlusThreeAtExactly4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pledge = scn.GetFreepsCard("pledge");
		var pledge2 = scn.GetFreepsCard("pledge2");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, knight, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(pledge, pledge2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Each burden lowers every companion's resistance by 1: the Guard 5 - 2 = 3, the Knight and Pippin
		// 6 - 2 = 4.
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(3, scn.GetResistance(guard));
		assertEquals(4, scn.GetResistance(knight));
		assertEquals(4, scn.GetResistance(pippin));
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(pledge));

		scn.FreepsPlayCard(pledge);

		// A [gondor] companion of either race, in the skirmish or out of it; not the [gondor] ally and not a
		// companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, knight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// The Guard, at resistance 3, is +2, though the Knight beside him has resistance 4.
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(4, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, pledge);

		scn.ShadowPass();
		scn.FreepsPlayCard(pledge2);
		scn.FreepsChooseCard(knight);

		// The Knight, at resistance exactly 4, is +3.
		assertEquals(9, scn.GetStrength(knight));
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(pippin));
		assertInZone(Zone.DISCARD, pledge2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheBonusLastsOnlyTheSkirmishItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pledge = scn.GetFreepsCard("pledge");
		var guard = scn.GetFreepsCard("guard");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(guard, knight);
		scn.MoveCardsToHand(pledge);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, runner}, new PhysicalCardImpl[]{guard, troop});
		scn.FreepsResolveSkirmish(knight);

		assertEquals(5, scn.GetResistance(guard));
		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Guard, whose skirmish is still to come, is chosen during the Knight's.
		scn.FreepsPlayCard(pledge);
		scn.FreepsChooseCard(guard);
		assertEquals(10, scn.GetStrength(guard));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(guard));

		// The Knight, 6, beats the Runner, 5, which dies.
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, runner);

		// The Guard's own skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pledge = scn.GetFreepsCard("pledge");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(pledge);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(pledge));

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(pledge));
	}

	@Test
	public void WithNoGondorCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pledge = scn.GetFreepsCard("pledge");
		var arwen = scn.GetFreepsCard("arwen");
		var merchant = scn.GetFreepsCard("merchant");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(pledge);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// CR 4.2 "effect": an event may be played though its effect finds nothing.
		assertTrue(scn.FreepsPlayAvailable(pledge));

		scn.FreepsPlayCard(pledge);

		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(merchant));
		assertEquals(4, scn.GetStrength(frodo));
		assertInZone(Zone.DISCARD, pledge);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
