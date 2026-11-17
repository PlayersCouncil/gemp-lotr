package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stout1", "7_121");
					put("stout2", "7_121");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability only
					put("ingold", "7_106");     // Ingold: [gondor] Man companion, strength 6; a fellowship ability only
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, strength 6; a maneuver ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6; valiant only

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; Damage +1 only
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StoutResistanceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Stout Resistance
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Add a threat to make a [gondor] Man strength +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stout1");

		assertEquals("Stout Resistance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsAThreatToMakeAGondorManStrengthPlusThreeUntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var ingold = scn.GetFreepsCard("ingold");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var stout = scn.GetFreepsCard("stout1");
		scn.MoveCompanionsToTable(aragorn, ingold, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(stout);
		assertInHand(stout);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{guard, savage2});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetThreats());
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(stout));

		scn.FreepsPlayCard(stout);
		assertEquals(1, scn.GetThreats());
		// Any [gondor] Man: the companion skirmishing, a companion in no skirmish and the ally; not a [gondor]
		// Hobbit, a [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, ingold, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(ingold));
		assertEquals(6, scn.GetStrength(merchant));
		assertInDiscard(stout);
		// Still +3 at the next decision of the skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(aragorn));

		// The skirmish is a phase of its own: in the Household Guard's, the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishing(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionInNoSkirmishAndTheAllyCanEachBeMadeStrengthPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var ingold = scn.GetFreepsCard("ingold");
		var merchant = scn.GetFreepsCard("merchant");
		var stout1 = scn.GetFreepsCard("stout1");
		var stout2 = scn.GetFreepsCard("stout2");
		scn.MoveCompanionsToTable(aragorn, ingold);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(stout1, stout2);
		assertInHand(stout1, stout2);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetThreats());
		assertFalse(scn.IsCharAssigned(ingold));
		assertEquals(6, scn.GetStrength(ingold));
		assertEquals(6, scn.GetStrength(merchant));

		scn.FreepsPlayCard(stout1);
		scn.FreepsChooseCard(ingold);
		assertEquals(1, scn.GetThreats());
		assertEquals(9, scn.GetStrength(ingold));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.ShadowPass();
		scn.FreepsPlayCard(stout2);
		scn.FreepsChooseCard(merchant);
		assertEquals(2, scn.GetThreats());
		assertEquals(9, scn.GetStrength(merchant));
		assertEquals(9, scn.GetStrength(ingold));
		assertEquals(8, scn.GetStrength(aragorn));
		assertInDiscard(stout1, stout2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGondorManInPlayItAddsTheThreatForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var stout = scn.GetFreepsCard("stout1");
		scn.MoveCompanionsToTable(pippin, guard);
		scn.MoveCardsToHand(stout);
		assertInHand(stout);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage1);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(0, scn.GetThreats());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(pippin));
		// A target is no requirement (CR 4.2 "effect"): the event is played for nothing.
		assertTrue(scn.FreepsPlayAvailable(stout));
		scn.FreepsPlayCard(stout);

		assertEquals(1, scn.GetThreats());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(4, scn.GetStrength(frodo));
		assertInDiscard(stout);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileAsManyThreatsAsCompanionsAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var stout = scn.GetFreepsCard("stout1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(stout);
		assertInHand(stout);

		var savage1 = scn.GetShadowCard("savage1");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		// Frodo and Aragorn: two companions, so two threats is the most there can be.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage1);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stout));

		// One threat fewer, and a threat can be added.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(stout));
		scn.FreepsPlayCard(stout);
		assertEquals(2, scn.GetThreats());
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
