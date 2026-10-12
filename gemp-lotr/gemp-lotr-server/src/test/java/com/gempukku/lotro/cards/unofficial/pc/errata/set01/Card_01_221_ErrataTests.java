package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

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

public class Card_01_221_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "51_221");
					put("witchking", "1_237"); // The Witch-king, Lord of Angmar: fierce; +2 strength for each other Nazgul
					put("enquea", "1_231");    // Ulaire Enquea, Lieutenant of Morgul: another Nazgul, fierce
					put("ned", "1_220");       // Not Easily Destroyed: [wraith] condition, support area

					put("aragorn", "1_89");    // Aragorn, Ranger of the North
					put("saga", "1_114");      // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; bearer vitality +1
					put("guard", "1_7");       // Dwarf Guard
					put("armor", "1_8");       // Dwarven Armor: a Free Peoples possession, borne by a Dwarf
					put("stairs", "1_24");     // Stairs of Khazad-dum: [dwarven] condition, support area
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePaleBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Pale Blade
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: 3
		 * Game Text: Bearer must be The Witch-king.
		 * 	He is <b>damage +1</b>.
		 * 	Each time The Witch-king wins a skirmish, you may exert him to discard a Free Peoples condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("The Pale Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnTheWitchKingMakingHimStrengthPlus3AndDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCardsToHand(blade, witchking);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(23, scn.GetTwilight()); // 20 set + 1 companion + 2 for Ettenmoors
		assertFalse(scn.ShadowPlayAvailable(blade));

		scn.ShadowPlayCard(witchking);
		assertEquals(13, scn.GetTwilight()); // 8 + 2 roaming
		assertTrue(scn.ShadowPlayAvailable(blade));
		assertEquals(16, scn.GetStrength(witchking)); // 14 + 2 for Enquea
		assertEquals(0, scn.GetKeywordCount(witchking, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(enquea));
		assertEquals(0, scn.GetKeywordCount(enquea, Keyword.DAMAGE));

		// Enquea is no bearer, so the blade goes to the Witch-king without a choice.
		scn.ShadowPlayCard(blade);
		assertAttachedTo(blade, witchking);
		assertEquals(19, scn.GetStrength(witchking));
		assertEquals(1, scn.GetKeywordCount(witchking, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(enquea));
		assertEquals(0, scn.GetKeywordCount(enquea, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeTheWitchKingWinsASkirmishHeMayExertToDiscardAFreePeoplesCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(guard, armor);
		scn.MoveCardsToSupportArea(stairs);

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var ned = scn.GetShadowCard("ned");
		scn.MoveCardsToSupportArea(ned);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);
		// Aragorn at 11 against 17 loses without being overwhelmed, and survives the two wounds.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, witchking);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blade));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(witchking));

		assertTrue(scn.ShadowHasCardChoiceAvailable(stairs, saga));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ned, armor));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(stairs);
		assertInDiscard(stairs);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, ned);
		assertAttachedTo(armor, guard);

		// The Witch-king is fierce: he wins a second skirmish and is asked again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, witchking);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();

		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blade));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(witchking));
		assertInDiscard(saga);
		assertInZone(Zone.SUPPORT, ned);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTriggerCanBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(stairs);

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, witchking);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blade));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions()); // the fierce assignments
	}

	@Test
	public void AnExhaustedWitchKingIsNotOfferedTheTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(stairs);

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);
		scn.AddWoundsToChar(witchking, 3);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, witchking);
		scn.PassSkirmishActions();

		// Vitality 4 with 3 wounds: he wins, but cannot exert.
		assertInZone(Zone.DEAD, guard);
		assertEquals(3, scn.GetWoundsOn(witchking));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(blade));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions()); // the fierce assignments
	}

	@Test
	public void AnotherNazgulWinningOrTheWitchKingLosingDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(stairs);

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, enquea);
		scn.AttachCardsTo(witchking, blade);
		// Aragorn at 20 beats the Witch-king at 19 (14 + 2 for Enquea + 3).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 12));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, witchking },
				new PhysicalCardImpl[] { guard, enquea }
		);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(blade));

		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// Enquea at 11 overwhelms the Dwarf Guard at 4.
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(blade));
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions()); // the fierce assignments
	}
}
