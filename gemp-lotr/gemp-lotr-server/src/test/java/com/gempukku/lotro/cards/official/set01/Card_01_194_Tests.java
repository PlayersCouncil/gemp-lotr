package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_194_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("relentless", "1_194");

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its "when you play" is not set off by a cheat
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll (no Orc), fierce; its play text is not reached
					put("relics", "1_195");     // Relics of Moria: [moria] condition in the support area; a Shadow ability used only on purpose
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc minion; a skirmish ability used only on purpose

					// Hobbit Stealth: [shire] Skirmish event, Stealth; "At sites 1 to 5, cancel a skirmish involving a Hobbit"
					put("stealth", "1_298");
					// Depart Silently: [gandalf] Maneuver event, Stealth; "At sites 1 to 5, spot Gandalf to prevent Hobbits
					// from being assigned to skirmishes until the regroup phase"
					put("depart", "3_31");
					// Halfling Deftness: [shire] Skirmish event with no Stealth; "Exert a Hobbit to make him strength +3"
					put("deftness", "1_293");
					// Nice Imitation: [shire] condition, Stealth, plays to the support area; a response to a Hobbit's
					// wound, which nothing here sets off
					put("imitation", "2_106");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit, strength 3, vitality 4; a skirmish ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: for Depart Silently's spot; a strength modifier only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RelentlessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Relentless
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Search</b>.
		 * <b>Response:</b> If a stealth event is played, spot 3 [moria] minions to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("relentless");

		assertEquals("Relentless", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingThreeMoriaMinionsCancelsAStealthEventAndTheSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relentless = scn.GetShadowCard("relentless");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var stealth = scn.GetFreepsCard("stealth");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(relentless, stealth);

		scn.StartGame();
		// Two [moria] Orcs and a [moria] Troll: exactly three [moria] minions.
		scn.MoveMinionsToTable(runner, troop, troll);
		assertInHand(relentless, stealth);

		// Site 2.  Merry against all three.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.ShadowAssignToMinions(merry, troop, troll);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.FreepsPlayCard(stealth);
		assertTrue(scn.ShadowPlayAvailable(relentless));

		scn.ShadowPlayCard(relentless);

		// A spot is no cost: no minion is exerted.
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));
		// Hobbit Stealth is cancelled (CR 4.2 "cancel": the event is discarded): Merry's skirmish is still being fought.
		assertInDiscard(stealth, relentless);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CancelsAStealthEventOfAnotherCultureAndPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relentless = scn.GetShadowCard("relentless");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var depart = scn.GetFreepsCard("depart");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(merry, gandalf);
		scn.MoveCardsToHand(relentless, depart);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop, troll);
		assertInHand(relentless, depart);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Site 2: Depart Silently would keep every Hobbit from being assigned until the regroup phase.
		scn.FreepsPlayCard(depart);
		assertTrue(scn.ShadowPlayAvailable(relentless));

		scn.ShadowPlayCard(relentless);
		assertInDiscard(depart, relentless);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The event is cancelled: Merry can be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner}, new PhysicalCardImpl[]{gandalf, troop});
		scn.ShadowAssignToMinions(gandalf, troll);
		assertTrue(scn.IsCharAssignedAgainst(merry, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void DepartSilentlyKeepsMerryFromBeingAssignedWhenRelentlessIsOfferedAndNotPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relentless = scn.GetShadowCard("relentless");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var depart = scn.GetFreepsCard("depart");
		var merry = scn.GetFreepsCard("merry");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(merry, gandalf);
		scn.MoveCardsToHand(relentless, depart);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop, troll);
		assertInHand(relentless, depart);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The control for the test above: the same board with Relentless offered and left in hand, so that
		// Depart Silently stands and Merry cannot be assigned, which is what cancelling it lifts.
		scn.FreepsPlayCard(depart);
		assertTrue(scn.ShadowPlayAvailable(relentless));
		scn.ShadowPass();
		assertInHand(relentless);
		assertInDiscard(depart);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssign(merry));
		assertTrue(scn.FreepsCanAssign(gandalf));
		assertTrue(scn.FreepsDecisionAvailable("Assign minions"));
	}

	@Test
	public void NotOfferedWithTwoMoriaMinionsBesideAMoriaConditionAndASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relentless = scn.GetShadowCard("relentless");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var relics = scn.GetShadowCard("relics");
		var soldier = scn.GetShadowCard("soldier");
		var stealth = scn.GetFreepsCard("stealth");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(relentless, stealth);
		// A [moria] card that is no minion.
		scn.MoveCardsToSupportArea(relics);

		scn.StartGame();
		// Two [moria] minions, and a minion of another culture.
		scn.MoveMinionsToTable(runner, troop, soldier);
		assertInHand(relentless, stealth);
		assertInZone(Zone.SUPPORT, relics);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.ShadowAssignToMinions(merry, troop, soldier);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetWoundsOn(merry));

		// Hobbit Stealth cancels Merry's skirmish: the Shadow player is asked nothing, nobody is wounded and the
		// turn goes on to the regroup phase.
		scn.FreepsPlayCard(stealth);
		assertInDiscard(stealth);
		assertInHand(relentless);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForAnEventThatIsNoStealthEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relentless = scn.GetShadowCard("relentless");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var deftness = scn.GetFreepsCard("deftness");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(relentless, deftness);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop, troll);
		assertInHand(relentless, deftness);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.ShadowAssignToMinions(merry, troop, troll);
		scn.FreepsResolveSkirmish(merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetStrength(merry));

		// A [shire] skirmish event with no Stealth: carried out at once, and the Shadow player is asked for
		// skirmish actions with Relentless not among them.
		scn.FreepsPlayCard(deftness);
		// Frodo is a Hobbit too: Merry is chosen to exert.
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(6, scn.GetStrength(merry));
		assertInDiscard(deftness);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(relentless));
		assertInHand(relentless);
	}

	@Test
	public void NotOfferedForAStealthCardThatIsNoEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var relentless = scn.GetShadowCard("relentless");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var troll = scn.GetShadowCard("troll");
		var imitation = scn.GetFreepsCard("imitation");
		scn.MoveCardsToHand(relentless, imitation);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop, troll);
		assertInHand(relentless, imitation);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Stealth condition: it goes to the support area and the Free Peoples player is asked for his next
		// fellowship action; the Shadow player, holding Relentless with three [moria] minions in play, was asked
		// nothing.
		scn.FreepsPlayCard(imitation);
		assertInZone(Zone.SUPPORT, imitation);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(relentless);
	}
}
