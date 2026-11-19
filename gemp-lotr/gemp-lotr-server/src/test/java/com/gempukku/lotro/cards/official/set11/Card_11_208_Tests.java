package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_208_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wings1", "11_208");
					put("wings2", "11_208");
					// Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, strength 9, vitality 2; each companion bearing a [wraith] condition is strength -2
					put("lemenya", "1_232");
					// Morgul Mongrel: a [wraith] Orc, strength 7, vitality 2; fierce and damage +1 only at 3 wounds on the Ring-bearer
					put("mongrel", "7_194");
					// Goblin Patrol Troop: a [moria] Orc with no text, strength 13
					put("troop", "1_177");
					// Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2; no text in play
					put("guard", "1_7");
					// Gimli, Son of Glóin: a Dwarf, to bear Dwarven Armor; his skirmish ability is used only on purpose
					put("gimli", "1_13");
					// The Saga of Elendil: a [gondor] condition borne by a [gondor] companion; its skirmish ability is used only on purpose
					put("saga", "1_114");
					// Stairs of Khazad-dûm: a [dwarven] condition in the support area; it reaches only a Dwarf skirmishing a [moria] minion
					put("stairs", "1_24");
					// Dwarven Armor: a Free Peoples possession, borne by a Dwarf
					put("armor", "1_8");
					// Black Breath: a Shadow ([wraith]) condition, borne by a companion as its own transfer puts it
					put("breath", "1_207");
					// Not Easily Destroyed: a Shadow ([wraith]) condition in the support area; its ability is used only on purpose
					put("ned", "1_220");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkWingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Dark Wings
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time your [wraith] minion wins a skirmish, you may discard this condition to discard a Free Peoples condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("wings1");

		assertEquals("Dark Wings", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenAWraithNazgulWinsTheCopyUsedIsDiscardedToDiscardAFreePeoplesConditionBorneOrNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wings1 = scn.GetShadowCard("wings1");
		var wings2 = scn.GetShadowCard("wings2");
		var lemenya = scn.GetShadowCard("lemenya");
		var breath = scn.GetShadowCard("breath");
		var ned = scn.GetShadowCard("ned");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var saga = scn.GetFreepsCard("saga");
		var stairs = scn.GetFreepsCard("stairs");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToSupportArea(wings1, wings2, ned);
		scn.AttachCardsTo(aragorn, breath);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(breath, aragorn);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.SUPPORT, wings1);
		assertInZone(Zone.SUPPORT, wings2);
		assertInZone(Zone.SUPPORT, ned);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		// Aragorn bears a [wraith] condition: 8 - 2 = 6 against Lemenya's 9, beaten and not overwhelmed.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Each copy makes its own offer.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wings1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wings2));
		scn.ShadowChooseOptionalTrigger(wings1);
		assertInDiscard(wings1);
		assertInZone(Zone.SUPPORT, wings2);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, stairs));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(breath, ned, wings2, armor));
		scn.ShadowChooseCard(stairs);

		assertInDiscard(stairs);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(breath, aragorn);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, ned);
		assertInZone(Zone.SUPPORT, wings2);
		// The other copy's offer, declined: it stays in play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wings2));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, wings2);
		assertAttachedTo(saga, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedAtOneWinItIsOfferedAgainWhenAWraithOrcWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wings = scn.GetShadowCard("wings1");
		var lemenya = scn.GetShadowCard("lemenya");
		var mongrel = scn.GetShadowCard("mongrel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, mongrel);
		scn.MoveCardsToSupportArea(wings);
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.SUPPORT, wings);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{guard, mongrel});

		// Lemenya (9) beats Aragorn (8): offered, and declined.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wings));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, wings);
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The [wraith] Orc (7) beats the Dwarf Guard (4): offered again, and taken.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wings));
		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(wings);
		assertInDiscard(stairs);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheWraithMinionLosesOrWhenAMinionOfAnotherCultureWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wings = scn.GetShadowCard("wings1");
		var lemenya = scn.GetShadowCard("lemenya");
		var troop = scn.GetShadowCard("troop");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, troop);
		scn.MoveCardsToSupportArea(wings);
		// Aragorn at 10 beats Lemenya's 9; she lives with one wound of 2.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.SUPPORT, wings);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, troop});

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wings));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The [moria] Troop (13) beats Gimli (6, and +1 by the Stairs against a [moria] minion: not overwhelmed).
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wings));
		assertInZone(Zone.SUPPORT, wings);
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesConditionInPlayItIsOfferedAndDiscardsItselfForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wings = scn.GetShadowCard("wings1");
		var lemenya = scn.GetShadowCard("lemenya");
		var ned = scn.GetShadowCard("ned");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(gimli, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.MoveCardsToSupportArea(wings, ned);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, wings);
		assertInZone(Zone.SUPPORT, ned);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wings));
		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(wings);
		assertInZone(Zone.SUPPORT, ned);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
