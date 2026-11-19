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

public class Card_11_214_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "11_214");
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: fierce, strength 14 (+2 for each other Nazgûl), vitality 4
					put("deathless", "6_122");  // The Witch-king, Deathless Lord: fierce, strength 14, vitality 4; his text answers a companion killed, and none is
					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, strength 9; her text reaches only a companion or ally bearing a [wraith] condition
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his texts need 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc; +4 and fierce only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: strength 7, vitality 3; a skirmish ability never used here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThePaleBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: The Pale Blade, Sword of Flame
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +3
		 * Game Text: Bearer must be a Nazgûl.<br>If bearer is The Witch-king, each time he wins a skirmish, you may exert him to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("The Pale Blade", card.getBlueprint().getTitle());
		assertEquals("Sword of Flame", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnANazgulOfEitherCultureAndNotOnAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(blade);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, khamul, hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(blade));
		scn.ShadowPlayCard(blade);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(witchking, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		scn.ShadowChooseCard(khamul);

		assertAttachedTo(blade, khamul);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeTheWitchKingBearingItWinsHeMayExertToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// Strength that keeps each companion from being overwhelmed by the Witch-king's 14 + 3.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 3));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);
		assertAttachedTo(blade, witchking);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.FreepsGetBurdens());
		// 17 against 10: he wins.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blade));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertEquals(1, scn.FreepsGetBurdens());

		// He is fierce, and wins again in the fierce round: offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blade));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(witchking));
		assertEquals(2, scn.FreepsGetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesTheWitchKingUnexertedAndAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var deathless = scn.GetShadowCard("deathless");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));

		scn.StartGame();
		scn.MoveMinionsToTable(deathless);
		scn.AttachCardsTo(deathless, blade);
		assertAttachedTo(blade, deathless);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, deathless);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(deathless));
		assertEquals(0, scn.FreepsGetBurdens());
		// A Witch-king of another title, 17 against 10.
		scn.PassSkirmishActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(blade));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(deathless));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheWitchKingBearingItLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn at 18 beats the Witch-king's 17, who lives through the one wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);
		assertAttachedTo(blade, witchking);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.FreepsGetBurdens());
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(witchking));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(blade));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnotherNazgulBearsItAndWinsNorWhenTheWitchKingWinsWithoutIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// Boromir at 9 is not overwhelmed by the Witch-king's 14 + 2 (for Lemenya).
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 2));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, lemenya);
		scn.AttachCardsTo(lemenya, blade);
		assertAttachedTo(blade, lemenya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, lemenya},
				new PhysicalCardImpl[]{boromir, witchking});

		// Lemenya bears the Blade: 9 + 3 against Aragorn's 8.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(blade));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The Witch-king, who does not bear it, wins: 16 against 9.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(blade));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheWitchKingBearingItIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade);
		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(witchking, 3);
		assertAttachedTo(blade, witchking);
		assertEquals(3, scn.GetWoundsOn(witchking));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.FreepsGetBurdens());
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(blade));
		assertEquals(3, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
