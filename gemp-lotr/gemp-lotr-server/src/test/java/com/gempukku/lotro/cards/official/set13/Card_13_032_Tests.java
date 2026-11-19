package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("less", "13_32");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion
					put("ent", "6_27");         // Ent Avenger: [gandalf] Ent companion, strength 6
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally
					put("worker", "15_135");    // Rohan Worker: a follower with no printed strength or resistance
					put("pipe", "1_74");        // Gandalf's Pipe: a possession that is no follower, bearer must be Gandalf

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForaWhileLessDarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: For a While Less Dark
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>.<br>Make a [gandalf] companion strength +2 (or +4 if that companion bears a follower).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("less");

		assertEquals("For a While Less Dark", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AGandalfCompanionWhoseSkirmishIsStillToComeIsMadeStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var less = scn.GetFreepsCard("less");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(gandalf, ent, aragorn);
		scn.MoveAlliesToTable(barliman);
		// Another companion bears the follower: "that companion" is the one chosen.
		scn.AttachCardsTo(gandalf, worker);
		scn.MoveCardsToHand(less);
		assertAttachedTo(worker, gandalf);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, runner1}, new PhysicalCardImpl[]{ent, runner2});
		scn.FreepsResolveSkirmish(gandalf);

		// In Gandalf's skirmish: Ent Avenger, whose skirmish is still to come, may be chosen.
		assertEquals(6, scn.GetStrength(ent));
		int gandalfStrength = scn.GetStrength(gandalf);
		assertTrue(scn.FreepsPlayAvailable(less));
		scn.FreepsPlayCard(less);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, ent));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(ent);
		assertEquals(8, scn.GetStrength(ent));
		assertEquals(gandalfStrength, scn.GetStrength(gandalf));
		assertInDiscard(less);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(ent);
		assertEquals(6, scn.GetStrength(ent));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionBearingAFollowerIsMadeStrengthPlus4AndTheEventIsNotPlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var less = scn.GetFreepsCard("less");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(gandalf, ent);
		scn.AttachCardsTo(gandalf, worker);
		scn.MoveCardsToHand(less);
		assertAttachedTo(worker, gandalf);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(less));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, runner1);
		scn.FreepsResolveSkirmish(gandalf);

		int before = scn.GetStrength(gandalf);
		scn.FreepsPlayCard(less);
		scn.FreepsChooseCard(gandalf);
		assertEquals(before + 4, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(ent));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACompanionBearingAPossessionThatIsNoFollowerIsMadeStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var less = scn.GetFreepsCard("less");
		var gandalf = scn.GetFreepsCard("gandalf");
		var ent = scn.GetFreepsCard("ent");
		var worker = scn.GetFreepsCard("worker");
		var pipe = scn.GetFreepsCard("pipe");
		scn.MoveCompanionsToTable(gandalf, ent);
		scn.AttachCardsTo(gandalf, pipe);
		// Ent Avenger bears a follower, Gandalf does not.
		scn.AttachCardsTo(ent, worker);
		scn.MoveCardsToHand(less);
		assertAttachedTo(pipe, gandalf);
		assertAttachedTo(worker, ent);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, runner1);
		scn.FreepsResolveSkirmish(gandalf);

		int before = scn.GetStrength(gandalf);
		scn.FreepsPlayCard(less);
		scn.FreepsChooseCard(gandalf);
		assertEquals(before + 2, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(ent));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoGandalfCompanionInPlayTheEventIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var less = scn.GetFreepsCard("less");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(less);
		assertInHand(less);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(less));
		scn.FreepsPlayCard(less);
		assertInDiscard(less);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
