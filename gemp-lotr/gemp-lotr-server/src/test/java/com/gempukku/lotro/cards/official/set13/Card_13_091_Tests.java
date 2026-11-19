package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fires", "13_91");
					put("legion", "11_88");     // Legion of Harad: [men] Man minion, strength 14, toil 2 and no other text
					put("column", "11_72");     // Column of Easterlings: [men] Man minion, strength 8, lurker; a skirmish ability used only on purpose
					put("fearsome", "11_79");   // Fearsome Dunlending: [men] Man minion, strength 9, ambush; a play trigger that is not set off when cheated in
					put("soldier", "4_220");    // Desert Soldier: [raider] Man minion; a start-of-skirmish trigger in his own skirmish only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("goblinrunner", "1_178"); // Goblin Runner: [moria] Orc; "When you play" only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose

					// [men] cards for the discard pile, of each kind, where none of them does anything
					put("stragglers", "13_99"); // Stragglers: condition
					put("fleet", "18_66");      // Fleet of Corsair Ships: possession
					put("overrun", "11_92");    // Overrun: event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FiresBrightlyBurningStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Fires Brightly Burning
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 [men] cards in your discard pile instead of paying the twilight cost for this card.<br>Make your [men] minion strength +2.
		 */

		var scn = GetScenario();

		var fires = scn.GetFreepsCard("fires");

		assertEquals("Fires Brightly Burning", fires.getBlueprint().getTitle());
		assertNull(fires.getBlueprint().getSubtitle());
		assertFalse(fires.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, fires.getBlueprint().getSide());
		assertEquals(Culture.MEN, fires.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fires.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(fires, Timeword.SKIRMISH));
		assertEquals(2, fires.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesYourMenMinionStrengthPlus2UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var fires = scn.GetShadowCard("fires");
		var legion = scn.GetShadowCard("legion");
		var column = scn.GetShadowCard("column");
		var fearsome = scn.GetShadowCard("fearsome");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(fires);

		scn.StartGame();
		assertInHand(fires);
		scn.MoveMinionsToTable(legion, column, fearsome, soldier);
		scn.SkipToAssignments();
		// The Legion in the first skirmish, the Column (a lurker) in a later one; the Dunlending and the Soldier
		// assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, legion}, new PhysicalCardImpl[]{gimli, column});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// No [men] card in the discard pile: the twilight cost is paid, with nothing asked.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fires));
		scn.ShadowPlayCard(fires);
		assertEquals(0, scn.GetTwilight());

		// A [men] minion in this skirmish, in a later one, or in none; not a Man of another culture, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(legion, column, fearsome));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, aragorn, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(8, scn.GetStrength(column));
		scn.ShadowChooseCard(column);

		assertEquals(10, scn.GetStrength(column));
		assertEquals(14, scn.GetStrength(legion));
		assertEquals(9, scn.GetStrength(fearsome));
		assertInDiscard(fires);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Legion beats Aragorn; in the Column's own skirmish, a phase of its own, the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(column));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMenMinionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fires = scn.GetShadowCard("fires");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(fires);

		scn.StartGame();
		assertInHand(fires);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(fires));
		assertEquals(13, scn.GetStrength(troop));
		scn.ShadowPlayCard(fires);

		assertEquals(0, scn.GetTwilight());
		assertEquals(13, scn.GetStrength(troop));
		assertInDiscard(fires);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fires = scn.GetShadowCard("fires");
		var legion = scn.GetShadowCard("legion");
		scn.MoveCardsToHand(fires);

		scn.StartGame();
		assertInHand(fires);
		scn.MoveMinionsToTable(legion);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The pool can pay for it.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(fires));
	}

	@Test
	public void WithTheTwilightAndFourMenCardsBothWaysToPayAreAskedAndTheCardsAreRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var freepsFires = scn.GetFreepsCard("fires");
		scn.MoveCompanionsToTable(aragorn);

		var fires = scn.GetShadowCard("fires");
		var legion = scn.GetShadowCard("legion");
		var column = scn.GetShadowCard("column");
		var fearsome = scn.GetShadowCard("fearsome");
		var stragglers = scn.GetShadowCard("stragglers");
		var fleet = scn.GetShadowCard("fleet");
		var overrun = scn.GetShadowCard("overrun");
		var goblinrunner = scn.GetShadowCard("goblinrunner");
		scn.MoveCardsToHand(fires);
		scn.MoveCardsToDiscard(column, fearsome, stragglers, fleet, overrun, goblinrunner);
		// The other player's [men] card, in his own discard pile: a title the Shadow player's pile does not hold,
		// since a choice from a pile is read by blueprint.
		scn.MoveCardsToDiscard(freepsFires);

		scn.StartGame();
		assertInDiscard(column, fearsome, stragglers, fleet, overrun, goblinrunner, freepsFires);
		scn.MoveMinionsToTable(legion);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertEquals(14, scn.GetStrength(legion));
		scn.ShadowPlayCard(fires);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseYes();

		// Exactly four [men] cards, of any type; not the [moria] Orc, nor the other player's card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(column, fearsome, stragglers, fleet, overrun));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(goblinrunner, freepsFires));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(column, stragglers, fleet, overrun);

		assertInZone(Zone.REMOVED, column, stragglers, fleet, overrun);
		assertInDiscard(fearsome, goblinrunner, freepsFires);
		assertEquals(2, scn.GetTwilight());

		// The lone minion is chosen unasked.
		assertEquals(16, scn.GetStrength(legion));
		assertInDiscard(fires);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheRemovalPaysTheTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fires = scn.GetShadowCard("fires");
		var legion = scn.GetShadowCard("legion");
		var column = scn.GetShadowCard("column");
		var stragglers = scn.GetShadowCard("stragglers");
		var fleet = scn.GetShadowCard("fleet");
		var overrun = scn.GetShadowCard("overrun");
		scn.MoveCardsToHand(fires);
		scn.MoveCardsToDiscard(column, stragglers, fleet, overrun);

		scn.StartGame();
		assertInDiscard(column, stragglers, fleet, overrun);
		scn.MoveMinionsToTable(legion);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fires));
		assertEquals(14, scn.GetStrength(legion));
		scn.ShadowPlayCard(fires);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseNo();

		// The twilight cost is paid in place of the removal; the lone minion is chosen unasked.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(column, stragglers, fleet, overrun, fires);
		assertEquals(16, scn.GetStrength(legion));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolItIsPlayedOnlyWithFourMenCardsToRemoveAndTheyAreRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fires = scn.GetShadowCard("fires");
		var legion = scn.GetShadowCard("legion");
		var column = scn.GetShadowCard("column");
		var stragglers = scn.GetShadowCard("stragglers");
		var fleet = scn.GetShadowCard("fleet");
		var overrun = scn.GetShadowCard("overrun");
		var goblinrunner = scn.GetShadowCard("goblinrunner");
		scn.MoveCardsToHand(fires);
		// Four cards, three of them [men].
		scn.MoveCardsToDiscard(column, stragglers, fleet, goblinrunner);

		scn.StartGame();
		assertInDiscard(column, stragglers, fleet, goblinrunner);
		scn.MoveMinionsToTable(legion);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fires));

		// A fourth [men] card: the removal is the only way to pay, and it is taken with no question.
		scn.MoveCardsToDiscard(overrun);
		assertInDiscard(overrun);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(fires));
		assertEquals(14, scn.GetStrength(legion));
		scn.ShadowPlayCard(fires);

		assertFalse(scn.ShadowDecisionAvailable("instead of paying twilight cost"));
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to remove"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(column, stragglers, fleet, overrun));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(goblinrunner));
		scn.ShadowChooseCards(column, stragglers, fleet, overrun);
		assertInZone(Zone.REMOVED, column, stragglers, fleet, overrun);
		assertInDiscard(goblinrunner, fires);
		assertEquals(0, scn.GetTwilight());
		assertEquals(16, scn.GetStrength(legion));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
