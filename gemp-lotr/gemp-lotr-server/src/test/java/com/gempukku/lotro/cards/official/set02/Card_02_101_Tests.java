package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("filibert", "2_101");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: Hobbit companion, strength 3, vitality 4; text only about [shire] tales
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit companion, not the Ring-bearer; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Hobbit, strength 8
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: Hobbit ally, home site 3; a fellowship ability

					put("pursuer", "4_190");    // Uruk Pursuer: printed fierce, strength 5, vitality 3; its texts are about unbound Hobbits discarded or killed
					put("pursuer2", "4_190");
					put("enquea", "103_74");    // Ulaire Enquea, Magnified through Suffering: printed fierce and relentless, strength 12; his trigger needs a wounded character at the start of his skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship block format, where a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("filibert", "2_101");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: Hobbit companion, strength 3, vitality 4; text only about [shire] tales
					put("pursuer", "4_190");    // Uruk Pursuer: printed fierce, strength 5, vitality 3; its texts are about unbound Hobbits discarded or killed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void FilibertBolgerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Filibert Bolger, Wily Rascal
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 1
		 * Vitality: 2
		 * Site Number: 1F
		 * Game Text: <b>Skirmish:</b> Exert a Hobbit companion twice to cancel a fierce skirmish involving that Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("filibert");

		assertEquals("Filibert Bolger", card.getBlueprint().getTitle());
		assertEquals("Wily Rascal", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void ExertsAHobbitTwiceToCancelHisFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(filibert);
		scn.MoveCompanionsToTable(pippin, aragorn);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, pursuer);
		scn.FreepsResolveSkirmish(pippin);

		// The regular skirmish against a fierce minion: a Hobbit companion can pay, so the ability is offered.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(filibert));
		scn.PassSkirmishActions();
		// Strength 3 against 5: Pippin loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, pursuer);
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, pursuer));
		assertTrue(scn.FreepsActionAvailable(filibert));

		scn.FreepsUseCardAction(filibert);
		// The Hobbit companions who can exert twice: Pippin and the Ring-bearer; not Aragorn, and not Filibert,
		// a Hobbit ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, filibert));
		scn.FreepsChooseCard(pippin);
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(filibert));

		// Cancelled: no skirmish damage, so Pippin (vitality 4) lives with his 3 wounds, and the Pursuer is unhurt.
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CancelsNothingInTheFierceSkirmishOfAnAllyOrANonHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveAlliesToTable(filibert, bilbo);
		scn.MoveCompanionsToTable(pippin, aragorn);

		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");

		// Bilbo's home is site 3, where the skirmishes of the second turn are fought.
		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(pursuer, pursuer2);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, pursuer},
				new PhysicalCardImpl[]{bilbo, pursuer2});

		// Pippin, outside these skirmishes, can pay, so the ability is offered in each.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuer));
		assertTrue(scn.FreepsActionAvailable(filibert));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, pursuer2));
		assertTrue(scn.FreepsActionAvailable(filibert));

		// Taken, it exerts Pippin twice and Bilbo's skirmish goes on.  Bilbo (vitality 3) could exert twice but is
		// a Hobbit ally, not a companion; Aragorn is a companion and no Hobbit.
		scn.FreepsUseCardAction(filibert);
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, aragorn));
		scn.FreepsChooseCard(pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, pursuer2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileNoHobbitCompanionCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(filibert);
		scn.MoveCompanionsToTable(pippin, sam, aragorn);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// Vitality 4 with 2 wounds: one exertion left, not two.
		scn.AddWoundsToChar(pippin, 2);
		scn.AddWoundsToChar(frodo, 2);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, pursuer);
		scn.FreepsResolveSkirmish(pippin);

		// Sam can pay, so the ability is offered, though Pippin, in the fierce skirmish, cannot.
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, pursuer));
		assertTrue(scn.FreepsActionAvailable(filibert));

		// Vitality 4 with 2 wounds: now no Hobbit companion can exert twice, beside an unwounded Aragorn.
		scn.AddWoundsToChar(sam, 2);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(filibert));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotCancelARelentlessSkirmishEvenAgainstAFierceMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveAlliesToTable(filibert);
		scn.MoveCompanionsToTable(pippin, sam);

		var enquea = scn.GetShadowCard("enquea");

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);

		// No regular skirmish.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// The fierce skirmish against him is cancelled.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, enquea);
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.InFierceRound());
		scn.FreepsUseCardAction(filibert);
		scn.FreepsChooseCard(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertFalse(scn.IsCharSkirmishing(sam));
		assertEquals(0, scn.GetWoundsOn(enquea));

		// The relentless skirmish against the same fierce minion is not.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, enquea);
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.InRelentlessRound());
		assertTrue(scn.HasKeyword(enquea, Keyword.FIERCE));
		assertTrue(scn.FreepsActionAvailable(filibert));

		scn.FreepsUseCardAction(filibert);
		// Sam (2 wounds) cannot exert twice.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam));
		scn.FreepsChooseCard(pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, enquea));

		// Not cancelled: Pippin (strength 3 against 12) is overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, pippin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnyHobbitCompanionMayPayAndOnlyAFierceSkirmishOfThatHobbitIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveAlliesToTable(filibert);
		scn.MoveCompanionsToTable(pippin, sam, aragorn);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, pursuer);
		scn.FreepsResolveSkirmish(pippin);

		// The regular skirmish: the cost can be paid, so the ability is offered, though it will cancel nothing.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.FreepsActionAvailable(filibert));
		scn.FreepsUseCardAction(filibert);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, sam, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, filibert));

		scn.FreepsChooseCard(pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, pursuer));

		scn.PassSkirmishActions();
		// Not cancelled: Pippin (strength 3 against 5) loses and takes a third wound.
		assertEquals(3, scn.GetWoundsOn(pippin));

		// In the fierce skirmish, Sam pays, and Aragorn's skirmish is not his.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, pursuer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsUseCardAction(filibert);
		// Pippin (3 wounds) cannot exert twice.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin));
		scn.FreepsChooseCard(sam);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuer));
		scn.PassSkirmishActions();
		// Aragorn (8 against 5) wins: the skirmish was fought.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheFellowshipBlockFormatTheRingBearerPaysToCancelHisOwnFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveAlliesToTable(filibert);
		scn.MoveCompanionsToTable(pippin);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		// No regular skirmish.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(frodo, pursuer);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, pursuer));

		scn.FreepsUseCardAction(filibert);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		scn.FreepsChooseCard(frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));

		// Cancelled: Frodo is out of the skirmish, and the Pursuer is unhurt.
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathTheRingBearerPaysAndHisFierceSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveAlliesToTable(filibert);
		scn.MoveCompanionsToTable(pippin);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		// No regular skirmish.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(frodo, pursuer);
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, pursuer));

		scn.FreepsUseCardAction(filibert);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin));
		scn.FreepsChooseCard(frodo);
		assertEquals(2, scn.GetWoundsOn(frodo));

		// The Ring-bearer's skirmish cannot be cancelled here: it goes on.
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, pursuer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
