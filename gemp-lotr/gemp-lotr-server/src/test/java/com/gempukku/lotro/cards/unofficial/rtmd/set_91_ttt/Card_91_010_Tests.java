package com.gempukku.lotro.cards.unofficial.rtmd.set_91_ttt;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_91_010_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion, strength 6, vitality 3
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion; a skirmish ability used only on purpose
					put("enquea", "1_231");     // Ulaire Enquea: a [wraith] Nazgul, strength 11, vitality 4, fierce
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					// Axe Strike, a Skirmish event (make a Dwarf strength +2): cards to discard from hand
					put("filler1", "1_3");
					put("filler2", "1_3");
					put("filler3", "1_3");
					put("filler4", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"91_10", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion, strength 6, vitality 3
					put("bounder", "1_286");    // Bounder: a Hobbit ally, who is no companion; a skirmish ability used only on purpose
					put("enquea", "1_231");     // Ulaire Enquea: a [wraith] Nazgul, strength 11, vitality 4, fierce
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					// Axe Strike, a Skirmish event (make a Dwarf strength +2): cards to discard from hand
					put("filler1", "1_3");
					put("filler2", "1_3");
					put("filler3", "1_3");
					put("filler4", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "91_10"
		);
	}

	@Test
	public void SkirmishPumpStatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 91
		 * Name: Race Text 91_10
		 * Type: MetaSite
		 * Intensity: -2
		 * Game Text: Skirmish: Discard 3 cards from hand to make your companion or minion strength +1.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 91_10", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-2, card.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsCanPumpCompanionDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_10: "Skirmish: Discard 3 cards from hand to make your companion or minion strength +1."

		var scn = GetFreepsScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToFreepsHand("filler1", "filler2", "filler3");

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, enquea);

		// Now in skirmish
		int gimliStr = scn.GetStrength(gimli);
		int frodoStr = scn.GetStrength(frodo);
		assertEquals(6, gimliStr);
		assertEquals(3, scn.GetFreepsHandCount());
		int discards = scn.GetFreepsDiscardCount();

		// Use the modifier's activated ability
		assertTrue(scn.FreepsActionAvailable(freepsMod));
		scn.FreepsUseCardAction(freepsMod);

		// Should have discarded 3 cards
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(discards + 3, scn.GetFreepsDiscardCount());

		// His companions, in the skirmish or out of it; not his ally, and not the other player's minion
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, enquea));
		assertEquals(1, scn.FreepsGetChoiceMin());

		// Choose Gimli as the target
		scn.FreepsChooseCard(gimli);

		// Gimli should be strength +1
		assertEquals(gimliStr + 1, scn.GetStrength(gimli));
		assertEquals(frodoStr, scn.GetStrength(frodo));

		//Shadow should not be able to use it (he has no card in hand here)
		assertFalse(scn.ShadowActionAvailable(freepsMod));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The bonus lasts for this skirmish and no longer: Gimli loses to the Nazgul, who is fierce,
		// and the assignments of the fierce round come next
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(gimliStr, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotUseWithoutThreeCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		// 91_10: Should not be usable without 3 cards in hand.

		var scn = GetFreepsScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(enquea);
		// Only 2 filler cards in hand, not enough
		scn.MoveCardsToFreepsHand("filler1", "filler2");

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, enquea);

		// In skirmish, the action should not be available with only 2 cards in hand
		assertEquals(2, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(freepsMod));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ShadowOwnerMakesHisMinionStrongerAndIsNotOfferedTheOpponentsCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		var enquea = scn.GetShadowCard("enquea");
		var savage = scn.GetShadowCard("savage");
		var filler1 = scn.GetShadowCard("filler1");
		var filler2 = scn.GetShadowCard("filler2");
		var filler3 = scn.GetShadowCard("filler3");
		var filler4 = scn.GetShadowCard("filler4");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(enquea, savage);
		scn.MoveCardsToHand(filler1, filler2, filler3, filler4);

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		// The Uruk Savage is left out of the skirmishes
		scn.FreepsAssignToMinions(gimli, enquea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsPass();

		assertEquals(4, scn.GetShadowHandCount());
		int discards = scn.GetShadowDiscardCount();
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(11, scn.GetStrength(enquea));
		assertTrue(scn.ShadowActionAvailable(shadowMod));
		scn.ShadowUseCardAction(shadowMod);

		// Three of the four cards in hand, of his choosing
		assertEquals(3, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(filler1, filler2, filler4);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, filler3);
		assertInDiscard(filler1, filler2, filler4);
		assertEquals(discards + 3, scn.GetShadowDiscardCount());

		// His minions, in the skirmish or out of it; not the other player's companions
		assertTrue(scn.ShadowHasCardChoiceAvailable(enquea, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, frodo));
		scn.ShadowChooseCard(savage);

		assertEquals(6, scn.GetStrength(savage));
		assertEquals(11, scn.GetStrength(enquea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheShadowPlayerIsNotOfferedTheFreePeoplesOwnersAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var freepsMod = scn.GetFreepsCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToFreepsHand("filler1", "filler2", "filler3");
		scn.MoveCardsToShadowHand("filler1", "filler2", "filler3");

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, enquea);

		// The owner is offered it
		assertTrue(scn.FreepsActionAvailable(freepsMod));
		scn.FreepsPass();

		// The Shadow player has three cards to discard and a minion of his own, but the modifier is not his
		assertEquals(3, scn.GetShadowHandCount());
		assertFalse(scn.ShadowActionAvailable(freepsMod));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerIsNotOfferedTheShadowOwnersAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var shadowMod = scn.GetShadowCard("mod");
		var gimli = scn.GetFreepsCard("gimli");
		var enquea = scn.GetShadowCard("enquea");

		scn.MoveCompanionsToTable(gimli);
		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToFreepsHand("filler1", "filler2", "filler3");
		scn.MoveCardsToShadowHand("filler1", "filler2", "filler3");

		scn.StartGame();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, enquea);

		// The Free Peoples player has three cards to discard and companions of his own, but the modifier is not his
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(shadowMod));
		scn.FreepsPass();

		// The owner is offered it
		assertTrue(scn.ShadowActionAvailable(shadowMod));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
