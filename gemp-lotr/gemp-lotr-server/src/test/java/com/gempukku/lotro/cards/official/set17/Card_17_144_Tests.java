package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_144_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "17_144");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, vitality 3; fierce, his text needs 3 burdens
					put("khamul", "103_61");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, not overwhelmed unless his strength is tripled
					put("gimli", "1_13");       // Gimli, Son of Glóin: vitality 3
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a sixth companion; his text changes only his strength
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally, neither counted nor exerted
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: The Witch-king, Conqueror of Arthedain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>Each time the Witch-king wins a fierce skirmish, you may exert a companion for each companion you can spot over 4.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Conqueror of Arthedain", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithSixCompanionsAFierceWinOffersTwoExertionsThatMayFallOnOneCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam, gandalf);
		scn.MoveAlliesToTable(barliman);

		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		// Gimli is exhausted: 2 wounds of 3 vitality.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(witchking);

		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// The first round's win is no fierce skirmish: nothing is offered.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();

		// Six companions: two over 4, so two exertions.  The ally is no companion and Gimli cannot exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, sam, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, barliman, boromir));
		// Ketura's ruling (Q-0399): each exertion a choice of a companion or of none.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The same companion may be chosen again for the second.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, sam, gandalf));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Ketura's ruling (Q-0399): "you may exert a companion for each ..." is 0 up to the count after the yes; as
	// Ketura put it of Q-0397's wounds, choosing none forfeits the rest.
	@Test
	public void AfterTheYesNoExertionNeedBeMadeAndTheFirstNoneEndsThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam, gandalf);

		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();

		// Six companions, two exertions owed by the count; none is chosen at the first, and the second is not asked.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli, sam, gandalf));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFiveCompanionsAndAnAllyAFierceWinOffersOneExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);
		scn.MoveAlliesToTable(barliman);

		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman));
		scn.ShadowChooseCard(gimli);

		// One companion over 4 (the ally is not counted): one exertion, and nothing more is asked.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourCompanionsAndAnAllyAFierceWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveAlliesToTable(barliman);

		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		// Four companions and an ally: none over 4, and a yes would exert nobody.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnotherFierceNazgulWinningAsksNothingAndDecliningTheWitchkingsOfferExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);

		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, witchking}, new PhysicalCardImpl[]{aragorn, khamul});
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, witchking}, new PhysicalCardImpl[]{aragorn, khamul});

		// Khamûl, 12 against Aragorn's 8, wins a fierce skirmish too: no offer of the Witch-king's.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceWinAfterASecondMoveIsAnsweredAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);

		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The fellowship moves again; the Witch-king stays in play.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, witchking);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(witchking));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
