package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_101_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("filibert", "52_101");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: Hobbit companion; text only about [shire] tales
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit companion, not the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is not a Hobbit
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: Hobbit ally, home site 3; a fellowship ability

					// Each test makes the minions it uses fierce.
					put("ambusher", "1_261");   // Orc Ambusher: strength 5, vitality 2; tracker, and a response to regroup events
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
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
		 * Game Text: <b>Skirmish:</b> Exert a Hobbit companion twice (except the Ring-bearer) to cancel a fierce
		 * skirmish involving that Hobbit.
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
	public void ExertsAHobbitTwiceToCancelHisFierceSkirmishButNotHisRegularOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(filibert);
		scn.MoveCompanionsToTable(pippin, aragorn);

		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, ambusher, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, ambusher);
		scn.FreepsResolveSkirmish(pippin);

		// The regular skirmish against a fierce minion is not a fierce skirmish: Pippin can pay, so the ability is
		// offered, flagged as cancelling nothing.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(filibert));
		assertEquals("effect", scn.FreepsActionNoEffect(filibert));
		scn.PassSkirmishActions();
		// Strength 3 against 5: Pippin loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(pippin));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, ambusher);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, ambusher));
		assertTrue(scn.FreepsActionAvailable(filibert));
		assertEquals("false", scn.FreepsActionNoEffect(filibert));

		// Pippin is the one Hobbit companion who is not the Ring-bearer, so he is chosen without asking.
		scn.FreepsUseCardAction(filibert);
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(filibert));

		// Cancelled: no skirmish damage, so Pippin (vitality 4) lives with his 3 wounds, and the ambusher is unhurt.
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CancelsNothingInTheFierceSkirmishOfTheRingBearerAnAllyOrANonHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToSupportArea(filibert, bilbo);
		scn.MoveCompanionsToTable(pippin, aragorn);

		var ambusher = scn.GetShadowCard("ambusher");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		// Bilbo's home is site 3, where the skirmishes of the second turn are fought.
		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(ambusher, troop, troop2);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, ambusher, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop2, null, Keyword.FIERCE));

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, ambusher},
				new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{bilbo, troop2});

		// Pippin, outside these skirmishes, can pay, so the ability is offered in each, flagged as cancelling nothing.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, ambusher));
		assertTrue(scn.FreepsActionAvailable(filibert));
		assertEquals("effect", scn.FreepsActionNoEffect(filibert));
		scn.PassSkirmishActions();
		scn.FreepsDeclineOptionalTrigger(); // The Ruling Ring

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.FreepsActionAvailable(filibert));
		assertEquals("effect", scn.FreepsActionNoEffect(filibert));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, troop2));
		assertTrue(scn.FreepsActionAvailable(filibert));
		assertEquals("effect", scn.FreepsActionNoEffect(filibert));

		// Taken, it exerts Pippin twice and Bilbo's skirmish goes on.
		scn.FreepsUseCardAction(filibert);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.IsCharSkirmishingAgainst(bilbo, troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void OfferedOnlyWhileAHobbitCompanionOtherThanTheRingBearerCanExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(filibert);
		scn.MoveCompanionsToTable(pippin, sam, aragorn);

		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, ambusher, null, Keyword.FIERCE));
		// Vitality 4 with 2 wounds: one exertion left, not two.
		scn.AddWoundsToChar(pippin, 2);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, ambusher);
		scn.FreepsResolveSkirmish(pippin);

		// Sam can pay, so the ability is offered; Pippin, in the fierce skirmish, cannot, so it would cancel nothing.
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, ambusher));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(filibert));
		assertEquals("effect", scn.FreepsActionNoEffect(filibert));

		// Vitality 4 with 2 wounds: now no Hobbit companion but the Ring-bearer can exert twice.
		scn.AddWoundsToChar(sam, 2);
		scn.FreepsRefreshDecision();
		assertFalse(scn.FreepsActionAvailable(filibert));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotCancelARelentlessSkirmishEvenAgainstAFierceMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(filibert);
		scn.MoveCompanionsToTable(pippin, aragorn);

		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, ambusher, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, ambusher, null, Keyword.RELENTLESS));

		// No regular skirmishes; Aragorn wins the fierce one, and the Ambusher (vitality 2) survives the wound.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, ambusher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(ambusher));

		// The relentless assignment phase.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(pippin, ambusher);
		scn.FreepsResolveSkirmish(pippin);

		// Pippin can pay, so the ability is offered, flagged as cancelling nothing.
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, ambusher));
		assertTrue(scn.HasKeyword(ambusher, Keyword.FIERCE));
		assertTrue(scn.FreepsActionAvailable(filibert));
		assertEquals("effect", scn.FreepsActionNoEffect(filibert));

		scn.FreepsUseCardAction(filibert);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, ambusher));

		// Not cancelled: Pippin (strength 3 against 5) loses and takes a third wound.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnyHobbitCompanionButTheRingBearerMayPayAndOnlyAFierceSkirmishOfThatHobbitIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var filibert = scn.GetFreepsCard("filibert");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(filibert);
		scn.MoveCompanionsToTable(pippin, sam, aragorn);

		var ambusher = scn.GetShadowCard("ambusher");

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, ambusher, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, ambusher);
		scn.FreepsResolveSkirmish(pippin);

		// The regular skirmish: the cost can be paid, so the ability is offered, though it will cancel nothing.
		assertTrue(scn.FreepsActionAvailable(filibert));
		assertEquals("effect", scn.FreepsActionNoEffect(filibert));
		scn.FreepsUseCardAction(filibert);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(filibert));

		scn.FreepsChooseCard(pippin);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, ambusher));

		scn.PassSkirmishActions();
		// Not cancelled: Pippin (strength 3 against 5) loses and takes a third wound.
		assertEquals(3, scn.GetWoundsOn(pippin));

		// In the fierce skirmish, Sam pays, and Pippin's skirmish is not his.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, ambusher);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals("effect", scn.FreepsActionNoEffect(filibert));
		// Pippin (3 wounds) cannot exert twice, so Sam is chosen without asking.
		scn.FreepsUseCardAction(filibert);
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ambusher));
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
