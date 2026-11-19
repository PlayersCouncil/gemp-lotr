package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_044_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("incited", "11_44");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5, vitality 4; his texts are used only on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum; fierce
					put("itsmine", "7_62");     // It's Mine: a [gollum] Shadow condition; a Shadow ability used only on purpose
					put("smeagol", "6_45");     // Sméagol, Poor Creature: a Free Peoples [gollum] card (a companion)
					put("fickle", "103_11");    // Fickle Loyalties: a Free Peoples [gollum] condition, for the Free Peoples player's own pile
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("guard", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, strength 4, vitality 2; no text in play
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; his texts are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; skirmishes by his own maneuver ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IncitedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Incited
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a companion loses a skirmish, you may exert Gollum to take a [gollum] card into hand from your discard pile.
		*/

		var scn = GetScenario();

		var incited = scn.GetFreepsCard("incited");

		assertEquals("Incited", incited.getBlueprint().getTitle());
		assertNull(incited.getBlueprint().getSubtitle());
		assertTrue(incited.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, incited.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, incited.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, incited.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(incited, Keyword.SUPPORT_AREA));
		assertEquals(1, incited.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenACompanionLosesGollumMayExertToTakeAGollumCardOfAnyKindFromHisOwnDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var fickle = scn.GetFreepsCard("fickle");
		scn.MoveCompanionsToTable(guard);
		// A [gollum] card in the Free Peoples player's own pile, of a title in none of the Shadow player's piles.
		scn.MoveCardsToDiscard(fickle);

		var incited = scn.GetShadowCard("incited");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var itsmine = scn.GetShadowCard("itsmine");
		var smeagol = scn.GetShadowCard("smeagol");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToSupportArea(incited);
		scn.MoveCardsToDiscard(shelob, itsmine, smeagol, pursuer);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertInDiscard(shelob, itsmine, smeagol, pursuer, fickle);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, gollum);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();

		// Dwarf Guard (4) loses to Gollum (5).
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(incited));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, itsmine, smeagol));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(pursuer, fickle));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(itsmine);

		assertInHand(itsmine);
		assertEquals(1, scn.GetShadowHandCount());
		assertInDiscard(shelob, smeagol, pursuer, fickle);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheWraithGollumPaysAndWithNoGollumCardInThePileNothingIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		var incited = scn.GetShadowCard("incited");
		var gollum = scn.GetShadowCard("wraithgollum");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToSupportArea(incited);
		scn.MoveCardsToDiscard(pursuer);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertInDiscard(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, gollum);
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetShadowHandCount());
		// The exertion is a cost, so the offer is made with nothing to take.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(incited));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(pursuer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForAWinOrAnAllysLossAndOfferedEachTimeTheSameCompanionLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(thrarin);

		var incited = scn.GetShadowCard("incited");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var itsmine = scn.GetShadowCard("itsmine");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(incited);
		scn.MoveCardsToDiscard(shelob, itsmine);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, pursuer, runner1, runner2);
		assertInDiscard(shelob, itsmine);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{sam, pursuer},
				new PhysicalCardImpl[]{thrarin, runner2});
		scn.ShadowDeclineAssignments();

		// Aragorn (8) wins against a Goblin Runner (5): no offer.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(incited));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Thrarin (4), an ally, loses to the other Goblin Runner (5) and is killed by the wound: no offer.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(incited));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Sam (3) loses to the Uruk Pursuer (5): offered, and declined.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(incited));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(shelob, itsmine);

		// The Pursuer is fierce: Sam loses to it a second time, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, pursuer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(incited));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob, itsmine));
		scn.ShadowChooseCards(shelob);

		assertInHand(shelob);
		assertInDiscard(itsmine);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhileGollumIsExhaustedThoughShelobCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		var incited = scn.GetShadowCard("incited");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var itsmine = scn.GetShadowCard("itsmine");
		scn.MoveCardsToSupportArea(incited);
		scn.MoveCardsToDiscard(itsmine);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gollum, 3);
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertInDiscard(itsmine);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(incited));
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertInDiscard(itsmine);
		// Shelob is fierce: the game goes on to the fierce assignment phase.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
