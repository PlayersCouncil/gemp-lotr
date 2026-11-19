package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_057_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The skirmishes and
	// the regroup phase of turn 1 are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sack", "31_57");
					put("sack2", "31_57");
					// Jail: another [sauron] support-area condition of the Shadow player's; its regroup ability is never used
					put("jail", "32_39");

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] Dwarf companion, 7/3; a maneuver ability used only on purpose
					// Dain Ironfoot, Lord of the Iron Hills: a [dwarven] Dwarf ally; a start-of-regroup offer that wants
					// a [dwarven] follower to discard, which no test lays
					put("dain", "32_2");

					// William, Troll of Ettenmoors: [troll] Troll, 11/4; his play cost is never paid (cheated in) and his
					// trigger answers a follower's transfer, which no test makes
					put("william", "31_61");
					put("demolition", "32_31"); // Demolition Troll: [moria] Troll, 10/3; a skirmish ability used only on purpose
					// Goblin Runner: [moria] Orc, 5/1; its play trigger is not reached (cheated in)
					put("runner", "31_30");
					// Dark Bats: [moria] Creature, 4/2: beats Bilbo (3) and leaves him alive; its Shadow ability is not used
					put("bats", "32_30");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void CaughtInASackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Caught In A Sack
		 * Unique: False
		 * Side: Shadow
		 * Culture: Troll
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Discard this condition if Bilbo wins a skirmish.
		 * 	<b>Regroup:</b> Discard a minion (or exert a Troll) to transfer this condition to a [dwarven] companion. Bearer cannot be assigned to a skirmish by any player.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sack");

		assertEquals("Caught In A Sack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.TROLL, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardedWhenBilboWinsASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var sack = scn.GetShadowCard("sack");
		var jail = scn.GetShadowCard("jail");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(sack, jail);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		// Bilbo 3 + 3 beats the Runner's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 3));
		assertInZone(Zone.SUPPORT, sack, jail);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassCurrentPhaseActions();
		// The rule's kill of the Runner and this card's discard are put in order: the rule first.
		assertTrue(scn.FreepsDecisionAvailable("Required responses"));
		assertInZone(Zone.SUPPORT, sack);
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DISCARD, sack);
		// Another condition of the same player's stays where it was.
		assertInZone(Zone.SUPPORT, jail);
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.DISCARD, runner);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void StaysWhenAnotherCompanionWinsAndWhenBilboLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var sack = scn.GetShadowCard("sack");
		var balin = scn.GetFreepsCard("balin");
		var runner = scn.GetShadowCard("runner");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(sack);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, bats);
		assertInZone(Zone.SUPPORT, sack);
		assertEquals(0, scn.GetWoundsOn(bilbo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner}, new PhysicalCardImpl[]{bilbo, bats});

		// Balin, 7, beats the Runner, 5.
		scn.FreepsResolveSkirmish(balin);
		scn.PassCurrentPhaseActions();
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SUPPORT, sack);

		// The Bats, 4, beat Bilbo, 3, who takes the wound and lives.
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertInZone(Zone.SUPPORT, sack);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupDiscardingAMinionTransfersItToADwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var sack = scn.GetShadowCard("sack");
		var sack2 = scn.GetShadowCard("sack2");
		var jail = scn.GetShadowCard("jail");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var william = scn.GetShadowCard("william");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(sack, sack2, jail);

		scn.StartGame();
		scn.MoveMinionsToTable(william, runner);
		assertInZone(Zone.SUPPORT, sack, sack2, jail);

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(sack));

		scn.ShadowUseCardAction(sack);
		// Either price can be paid, so the player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Discard a minion"));
		assertTrue(scn.ShadowChoiceAvailable("Exert a Troll"));
		scn.ShadowChoose("Discard a minion");
		// A minion of either kind, and no other Shadow card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(william, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(jail, sack, sack2));
		scn.ShadowChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, william);

		// A [dwarven] companion: neither Bilbo nor a [dwarven] ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(balin, dwalin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bilbo, dain));
		scn.ShadowChooseCard(dwalin);
		assertAttachedTo(sack, dwalin);
		// The copy whose action was used goes; the other copy stays.
		assertInZone(Zone.SUPPORT, sack2, jail);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupExertingATrollTransfersIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var balin = scn.GetFreepsCard("balin");
		var william = scn.GetShadowCard("william");
		var demolition = scn.GetShadowCard("demolition");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(sack);

		scn.StartGame();
		scn.MoveMinionsToTable(william, demolition, runner);
		assertInZone(Zone.SUPPORT, sack);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(william));
		assertEquals(0, scn.GetWoundsOn(demolition));
		assertEquals(0, scn.GetWoundsOn(runner));

		scn.ShadowUseCardAction(sack);
		scn.ShadowChoose("Exert a Troll");
		// A Troll of either culture, and not the Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(william, demolition));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner));
		scn.ShadowChooseCard(demolition);

		assertEquals(1, scn.GetWoundsOn(demolition));
		assertEquals(0, scn.GetWoundsOn(william));
		assertInZone(Zone.SHADOW_CHARACTERS, william, demolition, runner);
		// Balin is the one [dwarven] companion: the condition goes to him unasked.
		assertAttachedTo(sack, balin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheOnlyTrollExhaustedAMinionIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var balin = scn.GetFreepsCard("balin");
		var william = scn.GetShadowCard("william");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(sack);

		scn.StartGame();
		scn.MoveMinionsToTable(william, runner);
		scn.SkipToPhase(Phase.REGROUP);
		// Vitality 4 with 3 wounds: William cannot exert.
		scn.AddWoundsToChar(william, 3);
		assertEquals(3, scn.GetWoundsOn(william));
		assertInZone(Zone.SUPPORT, sack);
		scn.FreepsPass();
		assertEquals(3, scn.GetWoundsOn(william));
		assertTrue(scn.ShadowActionAvailable(sack));

		scn.ShadowUseCardAction(sack);
		// No question of which price: a minion to discard is asked for at once, William among them.
		assertFalse(scn.ShadowChoiceAvailable("Exert a Troll"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(william, runner));
		scn.ShadowChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertEquals(3, scn.GetWoundsOn(william));
		assertAttachedTo(sack, balin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWithNoMinionToPayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(sack);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, sack);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sack));
		assertInZone(Zone.SUPPORT, sack);
	}

	@Test
	public void WithNoDwarvenCompanionTheActionIsPaidForAndTheConditionStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var dain = scn.GetFreepsCard("dain");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveAlliesToTable(dain);
		scn.MoveCardsToSupportArea(sack);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		assertInZone(Zone.SUPPORT, sack);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(sack));

		scn.ShadowUseCardAction(sack);
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, troop));
		scn.ShadowChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.SUPPORT, sack);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ItMayBeMovedOnFromOneDwarvenCompanionToAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.AttachCardsTo(balin, sack);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		assertAttachedTo(sack, balin);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		// As built, it may be moved on from the Dwarf who bears it (Q-0022).
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(sack));
		scn.ShadowUseCardAction(sack);
		// The Runner is the one minion and Dwalin the one Dwarf it can go to: both are taken unasked.
		assertInZone(Zone.DISCARD, runner);
		assertAttachedTo(sack, dwalin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerCannotBeAssignedByEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sack = scn.GetShadowCard("sack");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(balin, dwalin);
		scn.AttachCardsTo(balin, sack);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		assertAttachedTo(sack, balin);

		scn.SkipToAssignments();
		assertFalse(scn.FreepsCanAssignAgainst(balin, runner));
		assertFalse(scn.FreepsCanAssignAgainst(balin, troop));
		assertFalse(scn.ShadowCanAssignAgainst(balin, runner));
		assertFalse(scn.ShadowCanAssignAgainst(balin, troop));
		// Dwalin, who does not bear it, may be assigned by either player.
		assertTrue(scn.FreepsCanAssignAgainst(dwalin, runner));
		assertTrue(scn.ShadowCanAssignAgainst(dwalin, runner));

		scn.FreepsAssignToMinions(dwalin, runner);
		assertTrue(scn.IsCharAssignedAgainst(dwalin, runner));
		// The Troop left over is the Shadow player's to assign, and not to Balin.
		assertFalse(scn.ShadowCanAssignAgainst(balin, troop));
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
	}
}
