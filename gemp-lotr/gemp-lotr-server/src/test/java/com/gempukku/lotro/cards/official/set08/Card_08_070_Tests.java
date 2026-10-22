package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_070_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flail", "8_70");
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: [wraith] Nazgul, vitality 4; fierce, +2 for each other Nazgul
					put("cantea", "1_230");     // Úlairë Cantëa, Lieutenant of Dol Guldur: another [wraith] Nazgul, vitality 3; a skirmish ability used only on purpose
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgul; his text needs 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgul; its text needs 2 wounds on the Ring-bearer
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("scimitar", "1_180");   // Goblin Scimitar: a possession (hand weapon) on a [moria] Orc; a play trigger only
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; a play trigger only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an ARTIFACT hand weapon on a [gondor] Man; its response is used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: a [gondor] possession, ranged weapon, on Aragorn; makes him an archer
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession, cloak, on a companion; minion archery total -1
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("mail", "1_101");       // Coat of Mail: a [gondor] possession, armor, on a Man
					put("merry", "1_302");      // Merry, Friend to Sam: a skirmish ability used only on purpose
					put("hobbitsword", "1_299"); // Hobbit Sword: a possession (hand weapon) on a Hobbit, no other text
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally, home 1F
					put("follow", "1_109");     // One Whom Men Would Follow: exert Aragorn to have an ally take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackFlailStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Black Flail
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +3
		 * Game Text: Bearer must be a Nazgûl.
		 * <b>Skirmish:</b> If bearer is The Witch-king, exert him and spot a possession borne by a character he is skirmishing to discard that possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("flail");

		assertEquals("Black Flail", card.getBlueprint().getTitle());
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
	public void PlaysOnANazgulOfEitherCultureAndOnNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flail = scn.GetShadowCard("flail");
		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();

		scn.MoveMinionsToTable(witchking, khamul, hound);
		scn.MoveCardsToHand(flail);
		scn.SetTwilight(10);
		assertInHand(flail);

		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(flail));
		scn.ShadowPlayCard(flail);

		// A [wraith] Nazgul and a [raider] one; not the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(witchking, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertAttachedTo(flail, khamul);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionExertsTheWitchKingToDiscardAPossessionBorneByTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flail = scn.GetShadowCard("flail");
		var witchking = scn.GetShadowCard("witchking");
		var scout = scn.GetShadowCard("scout");
		var scimitar = scn.GetShadowCard("scimitar");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var bow = scn.GetFreepsCard("bow");
		var cloak = scn.GetFreepsCard("cloak");
		var merry = scn.GetFreepsCard("merry");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, narsil, cloak);
		scn.AttachCardsTo(merry, hobbitsword);

		scn.StartGame();

		scn.MoveMinionsToTable(witchking, scout, runner);
		scn.AttachCardsTo(witchking, flail);
		scn.AttachCardsTo(scout, scimitar);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertAttachedTo(flail, witchking);
		assertAttachedTo(scimitar, scout);

		scn.SkipToAssignments();
		// Merry's skirmish has not begun when the Witch-king acts.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, witchking}, new PhysicalCardImpl[]{merry, runner});
		// The Scout is a second minion in Aragorn's skirmish, bearing a possession of his own.
		scn.ShadowAssignToMinions(aragorn, scout);
		// The bow makes Aragorn an archer: laid after the archery phase.
		scn.AttachCardsTo(aragorn, bow);
		assertAttachedTo(bow, aragorn);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, witchking));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowActionAvailable(flail));
		scn.ShadowUseCardAction(flail);

		assertEquals(1, scn.GetWoundsOn(witchking));
		// Two possessions of other cultures and classes on the companion he skirmishes; not the artifact beside
		// them, a possession on a companion whose skirmish is still to come, one on a minion in the same skirmish,
		// or this Flail itself.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, cloak));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(narsil, hobbitsword, scimitar, flail));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(cloak);

		assertInDiscard(cloak);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertAttachedTo(scimitar, scout);
		assertAttachedTo(flail, witchking);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void APossessionBorneByAnAllyHeIsSkirmishingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flail = scn.GetShadowCard("flail");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(maggot, hobbitsword);
		scn.MoveCardsToHand(follow);

		scn.StartGame();

		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, flail);
		assertAttachedTo(hobbitsword, maggot);
		assertAttachedTo(flail, witchking);

		// Set-up: Maggot takes part in skirmishes until the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(maggot, witchking);
		scn.FreepsResolveSkirmish(maggot);
		assertTrue(scn.IsCharSkirmishingAgainst(maggot, witchking));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowActionAvailable(flail));
		scn.ShadowUseCardAction(flail);

		assertEquals(1, scn.GetWoundsOn(witchking));
		assertInDiscard(hobbitsword);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheBearerIsANazgulWhoIsNotTheWitchKing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flail = scn.GetShadowCard("flail");
		var witchking = scn.GetShadowCard("witchking");
		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();

		scn.MoveMinionsToTable(witchking, cantea);
		scn.AttachCardsTo(cantea, flail);
		assertAttachedTo(flail, cantea);
		assertAttachedTo(cloak, aragorn);

		scn.SkipToAssignments();
		// The Witch-king is in the same skirmish, skirmishing Aragorn too, but does not bear the Flail.
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.ShadowAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, cantea));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, witchking));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flail));
		assertAttachedTo(cloak, aragorn);
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertEquals(0, scn.GetWoundsOn(witchking));
	}

	@Test
	public void NotOfferedInAnotherSkirmishWithNoPossessionOnACharacterHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flail = scn.GetShadowCard("flail");
		var witchking = scn.GetShadowCard("witchking");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, cloak);
		scn.AttachCardsTo(boromir, mail);

		scn.StartGame();

		scn.MoveMinionsToTable(witchking, runner);
		scn.AttachCardsTo(witchking, flail);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(mail, boromir);
		assertAttachedTo(flail, witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, witchking}, new PhysicalCardImpl[]{boromir, runner});
		// Boromir's skirmish is fought first; Aragorn is assigned to the Witch-king and waits for his.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner));
		scn.FreepsPass();

		// The Witch-king could exert, but the spot is part of the price: nobody is skirmishing him, so neither the
		// armor of the companion fighting now nor the cloak of the companion assigned to him can be spotted.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flail));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertAttachedTo(mail, boromir);
		assertAttachedTo(cloak, aragorn);
	}

	@Test
	public void NotOfferedWhileTheWitchKingIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flail = scn.GetShadowCard("flail");
		var witchking = scn.GetShadowCard("witchking");
		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();

		// Cantëa, another Nazgul, is unwounded and could pay an exertion if the cost were not the bearer's own.
		scn.MoveMinionsToTable(witchking, cantea);
		scn.AttachCardsTo(witchking, flail);
		// Vitality 4: with 3 wounds he cannot exert.
		scn.AddWoundsToChar(witchking, 3);
		assertEquals(3, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertAttachedTo(cloak, aragorn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		// Cantëa is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, witchking));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flail));
		assertAttachedTo(cloak, aragorn);
		assertEquals(3, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(cantea));
	}
}
