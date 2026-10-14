package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_028_Tests
{

	// Faramir is the Ring-bearer.  The Great Ring answers no wound unless it is worn, and is worn only by its own
	// maneuver action.  The Towers path: site 2 is Eastfold 4_331, site 3 Barrows of Edoras 4_337.
	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion; Knight is his whole text
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit, no Man (cheated in past his spot); his
					// skirmish ability is used only on purpose
					put("pippin", "103_31");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is his whole text
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("pursuer", "4_190");    // Uruk Pursuer: strength 5, vitality 3, printed fierce; his texts are about unbound Hobbits
					// Ruffian: [men] Man minion, site 2, strength 8 (+4 or +2 by region); no other text
					put("ruffian", "18_72");
					// Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, site 2; fierce and damage +1 only at 2
					// burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					put("goatleaf", "18_68");   // Harry Goatleaf: [men] Man minion, site 2; no text but his region bonus
					// Armor: a [gondor] possession on a Man; its whole text is that bearer takes no more than 1 wound
					// during each skirmish phase
					put("armor", "1_92");
				}},
				VirtualTableScenario.TowersSites,
				"17_28",                        // Faramir, Bearer of Quality is the Ring-bearer
				VirtualTableScenario.GreatRing
		);
	}

	// Frodo is the Ring-bearer, and Faramir an ordinary companion.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "17_28");
					put("knight", "5_35");
					put("savage", "1_151");
					put("savage2", "1_151");
					put("ruffian", "18_72");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Faramir, Bearer of Quality
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Can be the Ring-bearer: True
		 * Game Text: While Faramir is the Ring-bearer, at the start of each skirmish involving him, add 2 burdens or 2 threats.
		 * 	<b>Skirmish:</b> While Faramir is the Ring- bearer, you may wound Faramir to make a minion skirmishing a [gondor] Man roaming until the end of turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", card.getBlueprint().getTitle());
		assertEquals("Bearer of Quality", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(card.getBlueprint().canStartWithRing());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AsRingBearerAtTheStartOfHisSkirmishFaramirMayAdd2Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, savage);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		scn.FreepsResolveSkirmish(faramir);

		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("threats"));
		scn.FreepsChooseOption("burdens");

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AsRingBearerAtTheStartOfHisSkirmishFaramirMayAdd2Threats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, savage);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		// Two companions: two threats can be added.
		assertEquals(2, scn.GetThreatLimit());
		scn.FreepsResolveSkirmish(faramir);

		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("threats"));
		scn.FreepsChooseOption("threats");

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhenTwoThreatsCannotBeAddedFaramirAdds2Burdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.AddThreats(1);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, savage);

		// Two companions and one threat: only one more threat can be added.
		assertEquals(2, scn.GetThreatLimit());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		scn.FreepsResolveSkirmish(faramir);

		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BurdensOrThreatsAreAddedAtTheStartOfEachSkirmishInvolvingFaramir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, pursuer);
		scn.FreepsResolveSkirmish(faramir);
		scn.FreepsChooseOption("burdens");
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// Faramir (7) beats the Pursuer (5), who survives one wound and is fierce.
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(faramir, pursuer);
		scn.FreepsResolveSkirmish(faramir);

		assertTrue(scn.FreepsChoiceAvailable("burdens"));
		assertTrue(scn.FreepsChoiceAvailable("threats"));
		scn.FreepsChooseOption("threats");
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NothingIsAddedAtTheStartOfASkirmishNotInvolvingFaramir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, savage);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.IsCharSkirmishingAgainst(knight, savage));
		assertFalse(scn.IsCharAssigned(faramir));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NothingIsAddedAtTheStartOfASkirmishWhileFaramirIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{faramir, savage}, new PhysicalCardImpl[]{frodo, savage2});

		// His own skirmish, but he is not the Ring-bearer.
		scn.FreepsResolveSkirmish(faramir);

		assertTrue(scn.IsCharSkirmishingAgainst(faramir, savage));
		assertNotSame(faramir, scn.GetRingBearer());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Ring-bearer's skirmish, which Faramir is not in.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, savage2));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WoundingFaramirMakesAMinionSkirmishingAnotherGondorManRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var ruffian = scn.GetShadowCard("ruffian");
		var nelya = scn.GetShadowCard("nelya");
		var goatleaf = scn.GetShadowCard("goatleaf");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian, nelya, goatleaf);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, ruffian}, new PhysicalCardImpl[]{faramir, goatleaf});
		scn.ShadowAssignToMinions(knight, nelya);
		scn.FreepsResolveSkirmish(knight);

		// At Eastfold (site 2) no minion of site 2 is roaming.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertFalse(scn.HasKeyword(ruffian, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goatleaf, Keyword.ROAMING));
		assertTrue(scn.FreepsActionAvailable(faramir));

		scn.FreepsUseCardAction(faramir);

		assertEquals(1, scn.GetWoundsOn(faramir));
		// The two minions in this skirmish, of two cultures and races, and not the one assigned to Faramir, whose
		// skirmish is to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ruffian, nelya));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(goatleaf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(nelya);

		assertTrue(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(ruffian, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goatleaf, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	@Ignore("F-0507: a cost wound turned into a burden by The One Ring counts as unpaid, and the minion is not made roaming")
	public void WearingTheRingFaramirPaysTheWoundAsABurdenAndTheMinionIsStillMadeRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var ruffian = scn.GetShadowCard("ruffian");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian);
		scn.SkipToPhase(Phase.MANEUVER);
		// The Great Ring's own maneuver action: worn until the regroup phase.
		scn.FreepsUseCardAction(scn.GetRing());
		assertTrue(scn.RBWearingOneRing());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ruffian);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.HasKeyword(ruffian, Keyword.ROAMING));

		scn.FreepsUseCardAction(faramir);

		// The wound he is about to take is a burden instead: the cost is paid in that currency, and an exertion
		// would have put a wound on him.
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.HasKeyword(ruffian, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ASecondWoundHeCannotTakeInTheSamePhaseKeepsTheActionFromBeingOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var ruffian = scn.GetShadowCard("ruffian");
		var nelya = scn.GetShadowCard("nelya");
		var goatleaf = scn.GetShadowCard("goatleaf");
		scn.MoveCompanionsToTable(knight, guard);
		scn.AttachCardsTo(faramir, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian, nelya, goatleaf);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, ruffian}, new PhysicalCardImpl[]{guard, goatleaf});
		scn.ShadowAssignToMinions(knight, nelya);
		scn.FreepsResolveSkirmish(knight);

		// Two minions in the Knight's skirmish, so that a second use would still have one to make roaming.
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsActionAvailable(faramir));
		scn.FreepsUseCardAction(faramir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(ruffian, nelya));
		scn.FreepsChooseCard(ruffian);

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertTrue(scn.HasKeyword(ruffian, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));

		// Armor lets him take no second wound in this skirmish phase: the cost cannot be paid, and he is not
		// exhausted (vitality 3, 1 wound).
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(faramir));
		assertEquals(1, scn.GetWoundsOn(faramir));

		// The next skirmish is a phase of its own: the same Armor, and the cost can be paid again (the Guard is a
		// [rohan] Man, so no minion is made roaming).
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.FreepsActionAvailable(faramir));
		scn.FreepsUseCardAction(faramir);

		assertEquals(2, scn.GetWoundsOn(faramir));
		assertFalse(scn.HasKeyword(goatleaf, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InHisOwnSkirmishFaramirMakesTheMinionSkirmishingHimRoaming()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var goatleaf = scn.GetShadowCard("goatleaf");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(goatleaf);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, goatleaf);
		scn.FreepsResolveSkirmish(faramir);
		scn.FreepsChooseOption("threats");

		assertEquals(0, scn.GetWoundsOn(faramir));
		assertFalse(scn.HasKeyword(goatleaf, Keyword.ROAMING));

		scn.FreepsUseCardAction(faramir);

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertTrue(scn.HasKeyword(goatleaf, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheSkirmishOfAGondorHobbitOrOfARohanManFaramirIsWoundedAndNoMinionMadeRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		var ruffian = scn.GetShadowCard("ruffian");
		var goatleaf = scn.GetShadowCard("goatleaf");
		scn.MoveCompanionsToTable(pippin, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian, goatleaf);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, ruffian}, new PhysicalCardImpl[]{guard, goatleaf});

		// A [gondor] companion who is no Man: the action has a cost, and is offered.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsActionAvailable(faramir));
		scn.FreepsUseCardAction(faramir);

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.HasKeyword(ruffian, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(goatleaf, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A Man who is not [gondor].
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.FreepsActionAvailable(faramir));
		scn.FreepsUseCardAction(faramir);

		assertEquals(2, scn.GetWoundsOn(faramir));
		assertFalse(scn.HasKeyword(goatleaf, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Exhausted (vitality 3, 2 wounds) he could not exert, but he can still be wounded: the action is offered.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(faramir));
	}

	@Test
	public void WhileFaramirIsNotTheRingBearerTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var knight = scn.GetFreepsCard("knight");
		var ruffian = scn.GetShadowCard("ruffian");
		scn.MoveCompanionsToTable(faramir, knight);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ruffian);
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertFalse(scn.FreepsActionAvailable(faramir));
	}

	@Test
	public void TheMinionStaysRoamingThroughASecondMoveUntilTheTurnEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var faramir = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var ruffian = scn.GetShadowCard("ruffian");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, ruffian);
		scn.FreepsResolveSkirmish(knight);

		assertFalse(scn.HasKeyword(ruffian, Keyword.ROAMING));
		// The Ruffian is the only minion in this skirmish, and is chosen without a question.
		scn.FreepsUseCardAction(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertTrue(scn.HasKeyword(ruffian, Keyword.ROAMING));

		// The Ruffian beats the Knight and lives on into the regroup phase.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertInZone(Zone.SHADOW_CHARACTERS, ruffian);
		assertTrue(scn.HasKeyword(ruffian, Keyword.ROAMING));

		// The second move, to site 3: the same turn, where a minion of site 2 is not roaming of itself.
		scn.FreepsChooseToMove();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(ruffian, Keyword.ROAMING));

		// The turn ends, and every minion is discarded as it does.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.HasKeyword(ruffian, Keyword.ROAMING));
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(scn.P2());
		scn.PassReconcile(scn.P1());

		assertInDiscard(ruffian);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
