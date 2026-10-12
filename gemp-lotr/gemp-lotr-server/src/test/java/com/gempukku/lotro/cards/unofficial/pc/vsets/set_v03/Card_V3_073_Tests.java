package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_073_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cantea", "103_73");
					put("khamul", "103_61");     // Ulaire Attea, Khamul of the East: the one Nazgul of another culture, [raider]; his text needs 3 burdens
					put("rider", "12_161");      // Black Rider: [ringwraith] Nazgul; stronger for each companion
					put("cur", "7_189");         // Morgul Cur: [ringwraith] Orc, strength 8, +2 while a Nazgul can be spotted

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_96");      // Boromir, Lord of Gondor: strength 7, vitality 3
					put("bounder", "1_286");     // Bounder: Hobbit ally, home site 2; a skirmish ability never used here
					put("athelas1", "1_94");     // Athelas: [gondor] possession on a [gondor] Man; a fellowship ability only
					put("athelas2", "1_94");
					put("alliance1", "1_49");    // The Last Alliance of Elves and Men: [elven] condition on a [gondor] Man; strength for each Elf, and there are none
					put("alliance2", "1_49");
					put("narsil", "9_34");       // Narsil, Blade of the Faithful: [gondor] artifact on a [gondor] Man; a response to a wound on its bearer
					put("sword", "1_112");       // Ranger's Sword: possession, bearer must be Aragorn
					put("saga", "1_114");        // The Saga of Elendil: [gondor] condition on a [gondor] companion
					put("hobbitsword", "1_299"); // Hobbit Sword: possession, bearer must be a Hobbit; strength +2
					put("tip1", "1_209");        // Blade Tip: Shadow [ringwraith] condition, in the support area or on a character; wounds its bearer at the start of a fellowship phase
					put("tip2", "1_209");
					put("tip3", "1_209");
					put("poison1", "8_28");      // Spider Poison: Shadow [gollum] possession, in the support area or on a companion, who exerts when assigned
					put("poison2", "8_28");
					put("nazgulsword", "1_218"); // Nazgul Sword: Shadow possession, bearer must be a Nazgul; strength +2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireCanteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Cantea, Exalted with Decay
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Fierce.
		 * 	Skirmish: Exert this minion to hinder all possessions and conditions borne by a character it is
		 * 	skirmishing.  If you can spot another Nazgul, hinder all other copies of those cards you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cantea");

		assertEquals("Úlairë Cantëa", card.getBlueprint().getTitle());
		assertEquals("Exalted with Decay", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void HindersEveryPossessionAndConditionOnTheCharacterSheIsSkirmishingAndNothingElseWithNoOtherNazgulToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var cur = scn.GetShadowCard("cur");
		var rider = scn.GetShadowCard("rider");
		var tip1 = scn.GetShadowCard("tip1");
		var tip2 = scn.GetShadowCard("tip2");
		var poison1 = scn.GetShadowCard("poison1");
		var poison2 = scn.GetShadowCard("poison2");
		var nazgulsword = scn.GetShadowCard("nazgulsword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var alliance1 = scn.GetFreepsCard("alliance1");
		var alliance2 = scn.GetFreepsCard("alliance2");
		var narsil = scn.GetFreepsCard("narsil");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// On Boromir: a possession, a condition and an artifact of his own side.
		scn.AttachCardsTo(boromir, athelas1, alliance1, narsil);
		// On Aragorn: a copy of each of the first two, and a possession Boromir has no copy of.
		scn.AttachCardsTo(aragorn, athelas2, alliance2, sword);

		scn.StartGame();
		// On Boromir as well: a condition and a possession of the Shadow player's, with a copy of each in
		// the Shadow support area.
		scn.AttachCardsTo(boromir, tip1, poison1);
		scn.MoveCardsToSupportArea(tip2, poison2);
		// No Nazgul to spot but Cantea herself: a [ringwraith] Orc, and a Nazgul who is hindered.
		scn.MoveMinionsToTable(cantea, cur, rider);
		scn.HinderCard(rider);
		// A possession Cantea bears herself: she is in the skirmish, but is no character she is skirmishing.
		scn.AttachCardsTo(cantea, nazgulsword);
		scn.SkipToAssignments();
		// Aragorn's skirmish with the Cur has not begun when Boromir's is fought.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, cantea}, new PhysicalCardImpl[]{aragorn, cur});
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(cantea));
		assertFalse(scn.IsHindered(athelas1));
		assertFalse(scn.IsHindered(alliance1));
		assertFalse(scn.IsHindered(tip1));
		assertFalse(scn.IsHindered(poison1));
		assertTrue(scn.ShadowActionAvailable(cantea));

		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		// Every possession and condition Boromir bears, whichever player's it is.
		assertTrue(scn.IsHindered(athelas1));
		assertTrue(scn.IsHindered(alliance1));
		assertTrue(scn.IsHindered(tip1));
		assertTrue(scn.IsHindered(poison1));
		// Not his artifact, not Boromir, and not the possession she bears herself.
		assertFalse(scn.IsHindered(narsil));
		assertFalse(scn.IsHindered(boromir));
		assertFalse(scn.IsHindered(nazgulsword));
		// Nothing on the companion assigned to another minion, copy or not, and no copy in a support area.
		assertFalse(scn.IsHindered(athelas2));
		assertFalse(scn.IsHindered(alliance2));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(tip2));
		assertFalse(scn.IsHindered(poison2));

		// Her cost can be paid once more and no more: vitality 3.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(cantea));
		scn.ShadowUseCardAction(cantea);
		assertEquals(2, scn.GetWoundsOn(cantea));
		assertFalse(scn.IsHindered(narsil));
		assertFalse(scn.IsHindered(athelas2));
		assertFalse(scn.IsHindered(tip2));

		scn.FreepsPass();
		assertTrue(scn.IsExhausted(cantea));
		assertFalse(scn.ShadowActionAvailable(cantea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAnotherNazgulToSpotEveryOtherCopyInPlayIsHinderedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var khamul = scn.GetShadowCard("khamul");
		var cur = scn.GetShadowCard("cur");
		var tip1 = scn.GetShadowCard("tip1");
		var tip2 = scn.GetShadowCard("tip2");
		var tip3 = scn.GetShadowCard("tip3");
		var poison1 = scn.GetShadowCard("poison1");
		var poison2 = scn.GetShadowCard("poison2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");
		var alliance1 = scn.GetFreepsCard("alliance1");
		var alliance2 = scn.GetFreepsCard("alliance2");
		var narsil = scn.GetFreepsCard("narsil");
		var sword = scn.GetFreepsCard("sword");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(boromir, athelas1, alliance1, narsil);
		// On Aragorn: a copy of each of Boromir's two, and a possession and a condition that are copies of nothing.
		scn.AttachCardsTo(aragorn, athelas2, alliance2, sword, saga);

		scn.StartGame();
		// Copies of the Shadow cards Boromir bears: one on Aragorn, two in the Shadow support area.
		scn.AttachCardsTo(boromir, tip1, poison1);
		scn.AttachCardsTo(aragorn, tip2);
		scn.MoveCardsToSupportArea(tip3, poison2);
		// The other Nazgul is of the [raider] culture.
		scn.MoveMinionsToTable(cantea, cur, khamul);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, cantea}, new PhysicalCardImpl[]{aragorn, cur});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(cantea));
		assertFalse(scn.IsHindered(athelas2));
		assertFalse(scn.IsHindered(alliance2));
		assertFalse(scn.IsHindered(tip2));
		assertFalse(scn.IsHindered(tip3));
		assertFalse(scn.IsHindered(poison2));

		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		assertTrue(scn.IsHindered(athelas1));
		assertTrue(scn.IsHindered(alliance1));
		assertTrue(scn.IsHindered(tip1));
		assertTrue(scn.IsHindered(poison1));
		// The copies: on another companion and in a support area, of either side.
		assertTrue(scn.IsHindered(athelas2));
		assertTrue(scn.IsHindered(alliance2));
		assertTrue(scn.IsHindered(tip2));
		assertTrue(scn.IsHindered(tip3));
		assertTrue(scn.IsHindered(poison2));
		// The artifact was not hindered, and Aragorn's other cards are copies of nothing that was.
		assertFalse(scn.IsHindered(narsil));
		assertFalse(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(saga));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySheIsSkirmishingIsACharacterWhoseCardsAreHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var tip1 = scn.GetShadowCard("tip1");
		var bounder = scn.GetFreepsCard("bounder");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(bounder, hobbitsword);

		scn.StartGame();
		scn.AttachCardsTo(bounder, tip1);
		scn.MoveMinionsToTable(cantea);
		scn.SkipToAssignments();
		// Site 2 is the Bounder's home.
		scn.FreepsAssignToMinions(bounder, cantea);
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertFalse(scn.IsHindered(hobbitsword));
		assertFalse(scn.IsHindered(tip1));
		// 2 + 2 for the Hobbit Sword
		assertEquals(4, scn.GetStrength(bounder));

		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		assertTrue(scn.IsHindered(hobbitsword));
		assertTrue(scn.IsHindered(tip1));
		assertEquals(2, scn.GetStrength(bounder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedDuringAnotherMinionsSkirmishSheExertsAndHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var khamul = scn.GetShadowCard("khamul");
		var cur = scn.GetShadowCard("cur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var athelas2 = scn.GetFreepsCard("athelas2");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(boromir, athelas1);
		scn.AttachCardsTo(aragorn, athelas2);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, cur, khamul);
		scn.SkipToAssignments();
		// Boromir fights the Cur first; Aragorn is assigned to Cantea and their skirmish has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, cur}, new PhysicalCardImpl[]{aragorn, cantea});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertFalse(scn.IsCharSkirmishing(cantea));
		assertEquals(0, scn.GetWoundsOn(cantea));
		// The cost can be paid, so the ability is on offer.
		assertTrue(scn.ShadowActionAvailable(cantea));

		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		// Neither the possession on the companion now fighting, nor the one on the companion she is assigned to.
		assertFalse(scn.IsHindered(athelas1));
		assertFalse(scn.IsHindered(athelas2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoPossessionOrConditionOnTheCharacterNothingIsHinderedAnywhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cantea = scn.GetShadowCard("cantea");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var narsil = scn.GetFreepsCard("narsil");
		var athelas1 = scn.GetFreepsCard("athelas1");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, boromir);
		// Boromir bears an artifact and nothing else.
		scn.AttachCardsTo(boromir, narsil);
		scn.AttachCardsTo(aragorn, athelas1, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, khamul);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, cantea);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(cantea));

		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		assertFalse(scn.IsHindered(narsil));
		// Another Nazgul can be spotted, but no card was hindered for these to be copies of.
		assertFalse(scn.IsHindered(athelas1));
		assertFalse(scn.IsHindered(sword));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
