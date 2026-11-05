package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_038_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The phases of
	// turn 1 after the move are at The Troll Hoard (site 2), whose only text is a start-of-turn offer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("acorn", "31_38");
					put("acorn2", "31_38");
					// Thorin, Oakenshield: [dwarven] Dwarf companion; +1 for each [dwarven] follower he bears (none)
					// and a fellowship ability
					put("thorin", "30_19");
					// Fili, Brother of Kili: [dwarven] Dwarf companion who bears Caught In A Sack; a response to a
					// [dwarven] companion about to take a wound, never reached
					put("fili", "30_10");
					// Master Worrywort, Hobbit Farmer: a [shire] Hobbit ally who is no Bilbo
					put("worrywort", "33_53");

					// Hatred Rekindled: a [gundabad] condition in the Shadow support area; a Shadow ability only
					put("hatred", "30_34");
					// Goblin Swarms: a [moria] condition in the Shadow support area; a response to the Shadow
					// player's Orc winning, and a Shadow ability
					put("swarms", "31_34");
					// Caught In A Sack: a [troll] condition borne by a [dwarven] companion; discarded if Bilbo wins
					// a skirmish (he fights none), and a Shadow regroup ability left unused
					put("sack", "31_57");
					// Stairs of Khazad-dûm: a Free Peoples [dwarven] condition in the support area (the Hobbit sets
					// print no Free Peoples condition); +1 to a Dwarf skirmishing a [moria] minion, never reached
					put("stairs", "1_24");
					// Azog's Mace: a [moria] possession, no condition, borne by an Orc who fights nobody
					put("mace", "32_29");
					// Goblin Runner: [moria] Orc, 5/1; a text on being played, cheated in so it does not fire
					put("runner", "31_30");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AnAcornfromBeornshouseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: An Acorn from Beorn's house
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Game Text: Bearer must be Bilbo.
		 * 	Regroup: Discard this possession to remove a doubt and discard a Shadow condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("acorn");

		assertEquals("An Acorn from Beorn's house", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnlyOnBilbo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var acorn = scn.GetFreepsCard("acorn");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var worrywort = scn.GetFreepsCard("worrywort");
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(worrywort);

		scn.StartGame();
		scn.MoveCardsToHand(acorn);
		scn.FreepsRefreshDecision();
		assertInHand(acorn);

		assertTrue(scn.FreepsPlayAvailable(acorn));
		scn.FreepsPlayCard(acorn);

		// Neither the Dwarf companion nor the Hobbit ally may bear it: Bilbo is the one bearer, and nothing is asked.
		assertAttachedTo(acorn, bilbo);
		assertTrue(scn.GetAttachedCards(thorin).isEmpty());
		assertTrue(scn.GetAttachedCards(worrywort).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsTheAcornToRemoveADoubtAndDiscardAShadowConditionInPlayOrBorne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var acorn = scn.GetFreepsCard("acorn");
		var acorn2 = scn.GetFreepsCard("acorn2");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var fili = scn.GetFreepsCard("fili");
		var stairs = scn.GetFreepsCard("stairs");
		var hatred = scn.GetShadowCard("hatred");
		var swarms = scn.GetShadowCard("swarms");
		var sack = scn.GetShadowCard("sack");
		var mace = scn.GetShadowCard("mace");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(thorin, fili);
		scn.AttachCardsTo(bilbo, acorn, acorn2);
		scn.AttachCardsTo(fili, sack);
		scn.MoveCardsToSupportArea(stairs, hatred, swarms);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		assertAttachedTo(acorn, bilbo);
		assertAttachedTo(acorn2, bilbo);
		assertAttachedTo(sack, fili);
		assertInZone(Zone.SUPPORT, stairs, hatred, swarms);
		// The Runner and his Mace are laid in the fellowship phase and left unassigned, so they stand in the regroup phase.
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, mace);
		assertAttachedTo(mace, runner);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(acorn));
		scn.FreepsUseCardAction(acorn);

		// Shadow conditions of three cultures, two in the support area and one a companion bears; not the Free
		// Peoples condition, nor the Shadow possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(hatred, swarms, sack));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stairs, mace));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sack);

		// The copy used is the one discarded; the second copy stays on Bilbo.
		assertInDiscard(acorn, sack);
		assertAttachedTo(acorn2, bilbo);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, stairs, hatred, swarms);
		assertAttachedTo(mace, runner);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var acorn = scn.GetFreepsCard("acorn");
		var bilbo = scn.GetRingBearer();
		var hatred = scn.GetShadowCard("hatred");
		var runner = scn.GetShadowCard("runner");
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveCardsToSupportArea(hatred);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.SUPPORT, hatred);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// A doubt to remove and a Shadow condition to discard, a minion on the table: still not a Maneuver action.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(acorn));

		// The same board in the regroup phase offers it.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(acorn));
	}

	@Test
	public void WithNoShadowConditionTheAcornIsDiscardedAndTheDoubtStillRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var acorn = scn.GetFreepsCard("acorn");
		var bilbo = scn.GetRingBearer();
		var stairs = scn.GetFreepsCard("stairs");
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveCardsToSupportArea(stairs);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.SUPPORT, stairs);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(acorn));
		scn.FreepsUseCardAction(acorn);

		// Only a Free Peoples condition in play: nothing is asked and nothing else is discarded.
		assertInDiscard(acorn);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoDoubtTheAcornStillDiscardsAShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var acorn = scn.GetFreepsCard("acorn");
		var bilbo = scn.GetRingBearer();
		var hatred = scn.GetShadowCard("hatred");
		scn.AttachCardsTo(bilbo, acorn);
		scn.MoveCardsToSupportArea(hatred);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(acorn, bilbo);
		assertInZone(Zone.SUPPORT, hatred);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(acorn));
		scn.FreepsUseCardAction(acorn);

		// The one Shadow condition is discarded unasked.
		assertInDiscard(acorn, hatred);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
