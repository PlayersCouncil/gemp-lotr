package com.gempukku.lotro.cards.official.set17;

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

public class Card_17_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sap", "17_25");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion who is no Ent; +1 for each race he spots
					put("guardian", "5_17");    // Forest Guardian: [gandalf] Ent companion, unhasty; an assignment ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion; +2 to himself while 3 Ents can be spotted
					put("treebeard", "4_103");  // Treebeard, Earthborn: [gandalf] Ent ally; a response and a fellowship ability about unbound Hobbits
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally; a fellowship ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion; Knight is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, here a card in hand
					put("merchant", "7_97");    // Gondorian Merchant: an ally, here a card in hand

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheSapisintheBoughStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: The Sap is in the Bough
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Discard a companion from hand to make a [gandalf] companion strength +3 (or +4 if you can spot 3 Ents).
		*/

		var scn = GetScenario();

		var sap = scn.GetFreepsCard("sap");

		assertEquals("The Sap is in the Bough", sap.getBlueprint().getTitle());
		assertNull(sap.getBlueprint().getSubtitle());
		assertFalse(sap.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sap.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, sap.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, sap.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(sap, Timeword.SKIRMISH));
		assertEquals(2, sap.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsACompanionFromHandToMakeAGandalfCompanionStrengthPlusThreeForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var guardian = scn.GetFreepsCard("guardian");
		var knight = scn.GetFreepsCard("knight");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, guardian, knight);
		scn.MoveAlliesToTable(barliman);

		var sap = scn.GetFreepsCard("sap");
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToHand(sap, aragorn, avenger, merchant);
		assertInHand(sap, aragorn, avenger, merchant);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{knight, savage});

		scn.FreepsResolveSkirmish(gandalf);
		int gandalfStrength = scn.GetStrength(gandalf);
		int knightStrength = scn.GetStrength(knight);
		int barlimanStrength = scn.GetStrength(barliman);
		assertEquals(8, scn.GetStrength(guardian));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(sap));

		scn.FreepsPlayCard(sap);
		// Any companion in hand, of either culture and race; not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertInDiscard(aragorn);
		assertInHand(avenger, merchant);

		// A [gandalf] companion, in the skirmish or not, of either race; not a [gandalf] ally, nor a companion of
		// another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, guardian));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(barliman, knight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guardian);

		// One Ent to spot (the Guardian himself): +3.
		assertEquals(11, scn.GetStrength(guardian));    // 8 + 3
		assertEquals(gandalfStrength, scn.GetStrength(gandalf));
		assertEquals(knightStrength, scn.GetStrength(knight));
		assertEquals(barlimanStrength, scn.GetStrength(barliman));
		assertInDiscard(sap);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gone in the next skirmish, a phase of its own.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(guardian));
	}

	@Test
	public void WithThreeEntsToSpotTheCompanionIsStrengthPlusFourForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var guardian = scn.GetFreepsCard("guardian");
		var avenger = scn.GetFreepsCard("avenger");
		var treebeard = scn.GetFreepsCard("treebeard");
		// Three Ents: two companions and an ally.
		scn.MoveCompanionsToTable(gandalf, guardian, avenger);
		scn.MoveAlliesToTable(treebeard);

		var sap = scn.GetFreepsCard("sap");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(sap, aragorn);
		assertInHand(sap, aragorn);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{avenger, savage});

		scn.FreepsResolveSkirmish(gandalf);
		int gandalfStrength = scn.GetStrength(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(sap);
		// Aragorn, the only companion in hand, is discarded unasked; Gandalf is chosen among the [gandalf] companions.
		assertInDiscard(aragorn);
		scn.FreepsChooseCard(gandalf);

		// Gandalf is no Ent himself: the 3 Ents spotted are the others.
		assertEquals(gandalfStrength + 4, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gone in the next skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(avenger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(gandalfStrength, scn.GetStrength(gandalf));
	}

	@Test
	public void WithTwoEntsToSpotTheCompanionIsStrengthPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var guardian = scn.GetFreepsCard("guardian");
		var treebeard = scn.GetFreepsCard("treebeard");
		// Two Ents in play, with Gandalf a third [gandalf] character who is no Ent.
		scn.MoveCompanionsToTable(gandalf, guardian);
		scn.MoveAlliesToTable(treebeard);

		var sap = scn.GetFreepsCard("sap");
		// An Ent in hand, discarded as the cost: it is not in play to be spotted, before or after.
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCardsToHand(sap, avenger);
		assertInHand(sap, avenger);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);

		scn.FreepsResolveSkirmish(gandalf);
		int gandalfStrength = scn.GetStrength(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(sap);
		assertInDiscard(avenger);
		scn.FreepsChooseCard(gandalf);

		assertEquals(gandalfStrength + 3, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableWithNoCompanionInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var sap = scn.GetFreepsCard("sap");
		// An ally in hand, no companion.
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCardsToHand(sap, merchant);
		assertInHand(sap, merchant);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);

		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sap));
	}

	@Test
	public void WithNoGandalfCompanionInPlayItIsPlayedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(barliman);

		var sap = scn.GetFreepsCard("sap");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(sap, aragorn);
		assertInHand(sap, aragorn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, troop);

		scn.FreepsResolveSkirmish(knight);
		assertEquals(6, scn.GetStrength(knight));
		int barlimanStrength = scn.GetStrength(barliman);
		assertTrue(scn.FreepsPlayAvailable(sap));

		scn.FreepsPlayCard(sap);

		// The [gondor] companion and the [gandalf] ally are not made stronger; the cost is paid.
		assertInDiscard(sap, aragorn);
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(barlimanStrength, scn.GetStrength(barliman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
