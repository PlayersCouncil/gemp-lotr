package com.gempukku.lotro.cards.official.set01;

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

public class Card_01_221_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("paleblade", "1_221");
					put("twk", "1_237");       // The Witch-king, Lord of Angmar: fierce; +2 strength for each other Nazgul
					put("enquea", "1_231");    // Ulaire Enquea, Lieutenant of Morgul: another Nazgul, fierce
					put("ned", "1_220");       // Not Easily Destroyed: [wraith] condition, support area

					put("aragorn", "1_89");    // Aragorn, Ranger of the North
					put("saga", "1_114");      // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; bearer vitality +1
					put("guard", "1_7");       // Dwarf Guard
					put("armor", "1_8");       // Dwarven Armor: a Free Peoples possession, borne by a Dwarf
					put("stairs", "1_24");     // Stairs of Khazad-dum: [dwarven] condition, support area; a Dwarf fighting a [moria] minion is +1
					put("lord", "1_21");       // Lord of Moria: [dwarven] condition, support area; each Dwarf is damage +1
					put("mallorn", "1_54");    // Mallorn-trees: [elven] condition, support area; a fellowship ability only
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
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be The Witch-king.
		 * 	He is <b>damage +1</b>.
		 * 	<b>Response:</b> If The Witch-king wins a skirmish, exert him to discard a Free Peoples condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("paleblade");

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

		var paleblade = scn.GetShadowCard("paleblade");
		var twk = scn.GetShadowCard("twk");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCardsToHand(paleblade, twk);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(23, scn.GetTwilight()); // 20 set + 1 companion + 2 for Ettenmoors
		// Enquea is a Nazgul, and is not The Witch-king.
		assertFalse(scn.ShadowPlayAvailable(paleblade));

		scn.ShadowPlayCard(twk);
		assertEquals(13, scn.GetTwilight()); // 8 + 2 roaming
		assertTrue(scn.ShadowPlayAvailable(paleblade));
		assertEquals(16, scn.GetStrength(twk)); // 14 + 2 for Enquea
		assertEquals(0, scn.GetKeywordCount(twk, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(enquea));
		assertEquals(0, scn.GetKeywordCount(enquea, Keyword.DAMAGE));

		// Enquea is no bearer, so the blade goes to the Witch-king without a choice.
		scn.ShadowPlayCard(paleblade);
		assertAttachedTo(paleblade, twk);
		assertEquals(11, scn.GetTwilight());
		assertEquals(19, scn.GetStrength(twk));
		assertEquals(1, scn.GetKeywordCount(twk, Keyword.DAMAGE));
		assertEquals(11, scn.GetStrength(enquea));
		assertEquals(0, scn.GetKeywordCount(enquea, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThePaleBladeCanBeTriggeredMultipleTimesFromSameVictory() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var stairs = scn.GetFreepsCard("stairs");
		var lord = scn.GetFreepsCard("lord");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(guard, armor);
		scn.MoveCardsToSupportArea(stairs, lord, mallorn);

		var paleblade = scn.GetShadowCard("paleblade");
		var twk = scn.GetShadowCard("twk");
		var ned = scn.GetShadowCard("ned");
		scn.MoveCardsToSupportArea(ned);

		scn.StartGame();
		scn.MoveMinionsToTable(twk);
		scn.AttachCardsTo(twk, paleblade);
		// Aragorn at 11 against 17 loses without being overwhelmed, and survives the two wounds.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, twk);
		scn.PassSkirmishActions();

		// One wound for losing and one for the Witch-king's damage +1.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(twk));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(paleblade));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(twk));

		// Free Peoples conditions of three cultures, in the support area and on a companion; not the Shadow
		// player's own condition, and not a Free Peoples possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stairs, lord, mallorn, saga));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ned, armor));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saga);
		assertInDiscard(saga);
		assertInZone(Zone.SUPPORT, stairs, lord, mallorn, ned);
		assertAttachedTo(armor, guard);

		// The same win may be answered again, for as long as he can exert.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(paleblade));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(twk));
		assertTrue(scn.ShadowHasCardChoiceAvailable(stairs, lord, mallorn));
		scn.ShadowChooseCard(stairs);
		assertInDiscard(stairs);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(paleblade));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(twk));
		scn.ShadowChooseCard(lord);
		assertInDiscard(lord);

		// Vitality 4 with 3 wounds: he cannot exert again, though a condition is left to discard.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, mallorn, ned);
		assertEquals(3, scn.GetWoundsOn(twk));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnotherNazgulWinningOrTheWitchKingLosingIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(stairs);

		var paleblade = scn.GetShadowCard("paleblade");
		var twk = scn.GetShadowCard("twk");
		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(twk, enquea);
		scn.AttachCardsTo(twk, paleblade);
		// Aragorn at 20 beats the Witch-king at 19 (14 + 2 for Enquea + 3).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 12));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, twk },
				new PhysicalCardImpl[] { guard, enquea }
		);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(twk));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		// Enquea at 11 overwhelms the Dwarf Guard at 4, and the Witch-king, with 1 wound of 4, could exert.
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(twk));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
