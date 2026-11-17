package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_051_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("duty1", "103_51");
					put("duty2", "103_51");       // two dots: a second copy may be in play
					put("explorer", "4_250");     // Southron Explorer: [raider] Man, Southron, strength 5, vitality 3; a regroup ability never used here
					put("victim", "4_250");       // another Explorer, the minion that dies
					put("easterling", "7_141");   // Easterling Attacker: [raider] Man, Easterling, strength 6, vitality 2; a skirmish ability never used here
					put("footman", "11_82");      // Footman of Dunland: [men] Man, strength 7, vitality 3; the fellowship's site gains plains

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, vitality 4
					put("gimli", "1_13");         // Gimli, Son of Gloin: unbound [dwarven] Dwarf, strength 6, vitality 3, damage +1
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					put("bounder", "1_286");      // Bounder: [shire] Hobbit ally, vitality 2; a skirmish ability never used here
					put("vengeance", "1_106");    // Gondor's Vengeance: "Regroup: Exert a ranger companion to discard a minion."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DutyNoLessThanYoursStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Duty No Less Than Yours
		 * Unique: 2
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [raider] minion dies, you may exert a [raider] minion to exert an unbound companion.
		 * 	If you can spot another Duty No Less Than Yours, you may discard it to also hinder that companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("duty1");

		assertEquals("Duty No Less Than Yours", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		// Two dots: no more than two copies in play
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenARaiderMinionDiesARaiderMinionMayExertToExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetShadowCard("duty1");
		var explorer = scn.GetShadowCard("explorer");
		var victim = scn.GetShadowCard("victim");
		var easterling = scn.GetShadowCard("easterling");
		var footman = scn.GetShadowCard("footman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(duty);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, victim, easterling, footman);
		// The victim has 1 vitality left, so losing to Aragorn (8 against 5) kills it.
		scn.AddWoundsToChar(victim, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, victim);

		assertInZone(Zone.SHADOW_CHARACTERS, victim);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(duty));

		scn.PassSkirmishActions();

		assertInDiscard(victim);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(easterling));
		assertEquals(0, scn.GetWoundsOn(footman));

		scn.ShadowAcceptOptionalTrigger();

		// A [raider] minion: the Southron and the Easterling, and not the [men] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, easterling));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(footman));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(easterling);

		assertEquals(1, scn.GetWoundsOn(easterling));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(footman));
		// An unbound companion: not the Ring-bearer, not Ring-bound Sam, not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		// With no other copy to spot nothing more is asked, and nobody is hindered.
		assertFalse(scn.IsHindered(gimli));
		assertInZone(Zone.SUPPORT, duty);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheTriggerExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetShadowCard("duty1");
		var explorer = scn.GetShadowCard("explorer");
		var victim = scn.GetShadowCard("victim");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(duty);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, victim);
		scn.AddWoundsToChar(victim, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, victim);
		scn.PassSkirmishActions();

		assertInDiscard(victim);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertInZone(Zone.SUPPORT, duty);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsOfferedAgainForEachRaiderMinionThatDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetShadowCard("duty1");
		var explorer = scn.GetShadowCard("explorer");
		var victim = scn.GetShadowCard("victim");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(duty);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, victim, easterling);
		// Each of the two has 1 vitality left; the victim, being exhausted, cannot be the minion that exerts.
		scn.AddWoundsToChar(victim, 2);
		scn.AddWoundsToChar(easterling, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, easterling}, new PhysicalCardImpl[]{gimli, victim});
		scn.ShadowDeclineAssignments();

		// Aragorn (8) beats the Easterling (6).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertInDiscard(easterling);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowAcceptOptionalTrigger();
		// The unwounded Explorer is the only [raider] minion able to exert, so it is not asked for.
		assertEquals(1, scn.GetWoundsOn(explorer));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Gimli (6) beats the other Explorer (5).
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertInDiscard(victim);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(explorer));
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAMinionOfAnotherCultureDiesOrWhenARaiderMinionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetShadowCard("duty1");
		var explorer = scn.GetShadowCard("explorer");
		var victim = scn.GetShadowCard("victim");
		var footman = scn.GetShadowCard("footman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var vengeance = scn.GetFreepsCard("vengeance");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(vengeance);
		scn.MoveCardsToSupportArea(duty);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, victim, footman);
		// The Footman, a Man of another culture, has 1 vitality left; Aragorn (8) beats it (7).
		scn.AddWoundsToChar(footman, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, footman);
		scn.PassSkirmishActions();

		// It died with two [raider] minions able to exert and two unbound companions on the table.
		assertInDiscard(footman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(duty));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(vengeance));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Gondor's Vengeance: Aragorn, the only ranger, exerts to discard a minion.
		scn.FreepsPlayCard(vengeance);
		assertTrue(scn.FreepsHasCardChoiceAvailable(explorer, victim));
		scn.FreepsChooseCard(victim);

		// A [raider] minion left play by being discarded, which is not dying.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(victim);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(duty));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenNoRaiderMinionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetShadowCard("duty1");
		var explorer = scn.GetShadowCard("explorer");
		var victim = scn.GetShadowCard("victim");
		var footman = scn.GetShadowCard("footman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(duty);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, victim, footman);
		scn.AddWoundsToChar(victim, 2);
		// The [raider] minion that is left is exhausted; the minion that could exert is of another culture.
		scn.AddWoundsToChar(explorer, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, victim);
		scn.PassSkirmishActions();

		assertInDiscard(victim);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(duty));
		assertEquals(2, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionItIsStillOfferedAndOnlyTheMinionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty = scn.GetShadowCard("duty1");
		var explorer = scn.GetShadowCard("explorer");
		var victim = scn.GetShadowCard("victim");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(duty);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, victim);
		scn.AddWoundsToChar(victim, 2);
		// Sam at strength 3 + 5, so that a Ring-bound companion can kill the Explorer (5).
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, victim);
		scn.PassSkirmishActions();

		// The exertion is a cost that can be paid, so the trigger is offered though it will reach nobody.
		assertInDiscard(victim);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty));
		assertEquals(0, scn.GetWoundsOn(explorer));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOtherCopyMayBeDiscardedToHinderTheCompanionExertedAndEachCopyMakesItsOwnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty1 = scn.GetShadowCard("duty1");
		var duty2 = scn.GetShadowCard("duty2");
		var explorer = scn.GetShadowCard("explorer");
		var victim = scn.GetShadowCard("victim");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(duty1, duty2);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, victim, easterling);
		scn.AddWoundsToChar(victim, 2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, victim);
		scn.PassSkirmishActions();

		assertInDiscard(victim);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty2));

		// The first copy's offer, with the discard declined.
		scn.ShadowChooseOptionalTrigger(duty1);
		scn.ShadowChooseCard(explorer);
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));

		scn.ShadowChooseNo();

		assertInZone(Zone.SUPPORT, duty1);
		assertInZone(Zone.SUPPORT, duty2);
		assertFalse(scn.IsHindered(gimli));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The second copy's offer for the same death, with the discard taken.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(duty1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty2));
		assertEquals(0, scn.GetWoundsOn(easterling));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowChooseOptionalTrigger(duty2);
		scn.ShadowChooseCard(easterling);
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(easterling));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));

		scn.ShadowChooseYes();

		// "It" is the other copy: the one whose trigger was used stays in play.
		assertInDiscard(duty1);
		assertInZone(Zone.SUPPORT, duty2);
		// "That companion" is the one this copy exerted, and he is exerted as well as hindered.
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AHinderedSecondCopyCannotBeSpottedSoNothingIsAskedAfterTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duty1 = scn.GetShadowCard("duty1");
		var duty2 = scn.GetShadowCard("duty2");
		var explorer = scn.GetShadowCard("explorer");
		var victim = scn.GetShadowCard("victim");
		var easterling = scn.GetShadowCard("easterling");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(duty1, duty2);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, victim, easterling);
		scn.AddWoundsToChar(victim, 2);
		scn.HinderCard(duty2);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, victim);
		scn.PassSkirmishActions();

		assertInDiscard(victim);
		assertTrue(scn.IsHindered(duty2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(duty1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(duty2));

		scn.ShadowChooseOptionalTrigger(duty1);
		scn.ShadowChooseCard(explorer);
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(gimli));
		// The other copy is in play but hindered, and a hindered card cannot be spotted by its title.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to discard"));
		assertInZone(Zone.SUPPORT, duty1);
		assertInZone(Zone.SUPPORT, duty2);
		assertTrue(scn.IsHindered(duty2));
		assertFalse(scn.IsHindered(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
