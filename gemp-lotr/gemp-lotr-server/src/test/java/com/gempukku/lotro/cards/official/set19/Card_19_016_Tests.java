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

public class Card_19_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "19_16");
					// Rohirrim Traitor: an [isengard] Man of site 3, cost 2; a strength bonus only while a companion
					// bears an [isengard] condition.
					put("traitor", "6_72");
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					put("troop2", "1_177");
					// Morgul Slayer: a [sauron] Orc, strength 7.  "Regroup: Exert this minion to wound a companion
					// (except the Ring-bearer)."
					put("slayer", "3_93");

					// Aragorn, Ranger of the North: an unbound [gondor] Man, vitality 4; a maneuver ability never used.
					put("aragorn", "1_89");
					// Huorn: an unbound [gandalf] Tree, strength 6 and +1 for each Ent, vitality 4.  "Response: If this
					// companion is about to take a wound, exert an Ent to prevent that."
					put("huorn1", "9_25");
					put("huorn2", "9_25");
					// Ent Avenger: an unbound [gandalf] Ent, strength 6, vitality 4; a strength bonus only while 3
					// Ents can be spotted.  The Ent the Huorns exert.
					put("avenger", "6_27");
					// Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability never used.
					put("sam", "1_311");
					// Bounder: a Hobbit ally whose home is site 2, where these Shadow and assignment phases are; a
					// skirmish ability never used.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Gríma, Servant of Another Master
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: When you play Gríma, you may remove (4) to exert an unbound companion. That companion cannot use special abilities until the regroup phase.<br><b>Assignment:</b> Exert Gríma to assign him to an unbound companion. That companion may exert to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grima");

		assertEquals("Gríma", card.getBlueprint().getTitle());
		assertEquals("Servant of Another Master", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedHeMayRemoveFourToExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var huorn = scn.GetFreepsCard("huorn1");
		var avenger = scn.GetFreepsCard("avenger");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(aragorn, huorn, avenger, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(grima);

		scn.StartGame();
		// Vitality 4 with 3 wounds: the Ent Avenger cannot exert.
		scn.AddWoundsToChar(avenger, 3);
		assertEquals(3, scn.GetWoundsOn(avenger));
		assertInZone(Zone.HAND, grima);
		// 1 set + 2 for Ettenmoors + 5 for the companions.
		scn.SetTwilight(1);
		scn.FreepsPass();
		assertEquals(8, scn.GetTwilight());

		scn.ShadowPlayCard(grima);
		// 2 for Gríma + 2 for roaming at site 2: 4 is left, as the cost asks.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());

		// Two unbound companions of other cultures and races.  Not the exhausted one, not Sam (Ring-bound), not
		// Frodo, and not the ally.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, huorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(avenger, sam, frodo, bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(huorn));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertEquals(3, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferRemovesNothingAndExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var huorn = scn.GetFreepsCard("huorn1");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(aragorn, huorn);
		scn.MoveCardsToHand(grima);

		scn.StartGame();
		assertInZone(Zone.HAND, grima);
		// 4 set + 2 for Ettenmoors + 3 for the companions; 4 to play Gríma at site 2, 5 left.
		scn.SetTwilight(4);
		scn.FreepsPass();

		scn.ShadowPlayCard(grima);
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithLessThanFourLeftAfterHisPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var huorn = scn.GetFreepsCard("huorn1");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(aragorn, huorn);
		scn.MoveCardsToHand(grima);

		scn.StartGame();
		assertInZone(Zone.HAND, grima);
		// 2 set + 2 for Ettenmoors + 3 for the companions; 4 to play Gríma at site 2, 3 left.
		scn.SetTwilight(2);
		scn.FreepsPass();

		scn.ShadowPlayCard(grima);
		assertInZone(Zone.SHADOW_CHARACTERS, grima);
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheOfferIsStillMadeAndPaysForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(grima);

		scn.StartGame();
		assertInZone(Zone.HAND, grima);
		// 4 set + 2 for Ettenmoors + 2 for Frodo and Sam; 4 to play Gríma at site 2, 4 left.
		scn.SetTwilight(4);
		scn.FreepsPass();

		scn.ShadowPlayCard(grima);
		assertEquals(4, scn.GetTwilight());
		// The trigger has a cost that can be paid, so it is offered with nobody to exert.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherIsengardManPlayedAfterHimMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var grima = scn.GetShadowCard("grima");
		var traitor = scn.GetShadowCard("traitor");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(grima, traitor);

		scn.StartGame();
		assertInZone(Zone.HAND, grima, traitor);
		// 8 set + 2 for Ettenmoors + 2 for the companions: 4 for Gríma, 4 for the Traitor (2 and 2 for roaming),
		// and 4 left that the cost could take.
		scn.SetTwilight(8);
		scn.FreepsPass();

		scn.ShadowPlayCard(grima);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(grima));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(8, scn.GetTwilight());

		scn.ShadowPlayCard(traitor);
		assertInZone(Zone.SHADOW_CHARACTERS, grima, traitor);
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCompanionExertedCannotUseSpecialAbilitiesUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var huorn1 = scn.GetFreepsCard("huorn1");
		var huorn2 = scn.GetFreepsCard("huorn2");
		var avenger = scn.GetFreepsCard("avenger");
		var grima = scn.GetShadowCard("grima");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCompanionsToTable(huorn1, huorn2, avenger);
		scn.MoveCardsToHand(grima);

		scn.StartGame();
		assertInZone(Zone.HAND, grima);
		// 2 set + 2 for Ettenmoors + 4 for the companions; 4 to play Gríma at site 2, 4 left.
		scn.SetTwilight(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, troop2, slayer);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, troop1, troop2, slayer);

		scn.ShadowPlayCard(grima);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(huorn1, huorn2, avenger));
		scn.ShadowChooseCard(huorn1);
		assertEquals(1, scn.GetWoundsOn(huorn1));

		// Each Huorn is 6 + 1 for the Ent: 7 against a Troop's 13, a loss and not an overwhelm.  The Ent Avenger
		// loses to the Slayer.  Gríma is left out.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{huorn2, troop1}, new PhysicalCardImpl[]{huorn1, troop2},
				new PhysicalCardImpl[]{avenger, slayer});
		scn.ShadowDeclineAssignments();

		// The Huorn not chosen may still use his Response as he is about to take the wound for losing.
		assertEquals(0, scn.GetWoundsOn(huorn2));
		scn.FreepsResolveSkirmish(huorn2);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(huorn2));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(huorn2));

		// In the next skirmish the Huorn Gríma exerted is not offered his.
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.FreepsResolveSkirmish(huorn1);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(huorn1));
		assertEquals(2, scn.GetWoundsOn(huorn1));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(avenger);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(avenger));

		// In the regroup phase the bar is gone: the Slayer's wound on him is answered by his Response, and the
		// Ent exerts for it.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(slayer));
		scn.ShadowUseCardAction(slayer);
		assertEquals(1, scn.GetWoundsOn(slayer));
		scn.ShadowChooseCard(huorn1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(huorn1));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(huorn1));
		assertEquals(2, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AssignmentAbilityExertsHimToAssignHimToAnUnboundCompanionWhoMayExertToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var huorn = scn.GetFreepsCard("huorn1");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var grima = scn.GetShadowCard("grima");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(aragorn, huorn, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Site 2 is the Bounder's home, so he could be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsCanAssign(bounder));
		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowActionAvailable(grima));

		scn.ShadowUseCardAction(grima);
		assertEquals(1, scn.GetWoundsOn(grima));
		// Two unbound companions.  Not Sam (Ring-bound), not Frodo, not the ally.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, huorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, frodo, bounder));
		scn.ShadowChooseCard(huorn);

		// The Huorn exerts to prevent it: nobody is assigned, and Gríma's exertion stands.
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseYes();
		assertEquals(1, scn.GetWoundsOn(huorn));
		assertFalse(scn.IsCharAssigned(huorn));
		assertFalse(scn.IsCharAssigned(grima));
		assertEquals(1, scn.GetWoundsOn(grima));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// Used again, and declined: Gríma himself is assigned to Aragorn, who does not exert; the Troop is not.
		scn.FreepsPass();
		scn.ShadowUseCardAction(grima);
		assertEquals(2, scn.GetWoundsOn(grima));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to exert"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsCharAssignedAgainst(aragorn, grima));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsCharAssigned(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedWhileGrimaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(grima, 2);
		assertEquals(2, scn.GetWoundsOn(grima));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.CanBeAssignedViaAction(aragorn));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grima));
	}

	@Test
	public void AnExhaustedCompanionChosenIsNotAskedAndIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var huorn = scn.GetFreepsCard("huorn1");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(aragorn, huorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		// Vitality 4 with 3 wounds: Aragorn cannot exert.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		scn.ShadowUseCardAction(grima);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, huorn));
		scn.ShadowChooseCard(aragorn);

		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, grima));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionTheAbilityExertsHimForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var grima = scn.GetShadowCard("grima");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The ability has a cost that can be paid, so it is offered with nobody to assign him to.
		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.ShadowActionAvailable(grima));
		scn.ShadowUseCardAction(grima);

		assertEquals(1, scn.GetWoundsOn(grima));
		assertFalse(scn.FreepsDecisionAvailable("Would you like to exert"));
		assertFalse(scn.IsCharAssigned(grima));
		assertFalse(scn.IsCharAssigned(frodo));
		assertFalse(scn.IsCharAssigned(sam));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
