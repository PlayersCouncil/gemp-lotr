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

public class Card_13_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("massing", "13_111");
					put("goblinA", "11_137");   // Scurrying Goblin: [orc] Orc, strength 5; its played trigger is at an underground site only
					put("goblinB", "11_137");
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, strength 8; its trigger is at the start of the assignment phase, used only on purpose
					put("olog", "11_108");      // Beastly Olog-hai: [orc] Troll, strength 13; an assignment ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its text is on its own play
					put("depths", "13_120");    // Unforgiving Depths: [orc] condition in the support area; its ability is used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; his abilities are used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MassingStrengthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Massing Strength
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Discard any number of your [orc] minions from play to make an [orc] minion strength +3 for each one discarded.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("massing");

		assertEquals("Massing Strength", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardingTwoOrcMinionsMakesAnOrcMinionStrengthPlus6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var massing = scn.GetShadowCard("massing");
		var goblinA = scn.GetShadowCard("goblinA");
		var goblinB = scn.GetShadowCard("goblinB");
		var halftroll = scn.GetShadowCard("halftroll");
		var olog = scn.GetShadowCard("olog");
		var runner = scn.GetShadowCard("runner");
		var depths = scn.GetShadowCard("depths");
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinA, goblinB, halftroll, olog, runner);
		scn.MoveCardsToSupportArea(depths);
		assertInHand(massing);
		assertInZone(Zone.SUPPORT, depths);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll, olog);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, goblinA);
		// The Goblin Runner joins the same skirmish; the other three minions stay unassigned.
		scn.ShadowAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(goblinA));
		assertEquals(5, scn.GetStrength(goblinB));
		assertEquals(5, scn.GetStrength(runner));
		assertInHand(massing);
		assertTrue(scn.ShadowPlayAvailable(massing));
		scn.ShadowPlayCard(massing);

		// The cost: the Shadow player's [orc] minions in play, assigned or not, of any race; not the [moria] Orc, not
		// the [orc] condition.  Any number, from none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblinA, goblinB, halftroll, olog));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, depths));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(halftroll, olog);
		assertInDiscard(halftroll, olog);

		// The target: an [orc] minion, in the skirmish or not; not the [moria] Orc in the skirmish, not the [orc] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblinA, goblinB));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(runner, depths));
		scn.ShadowChooseCard(goblinA);

		// 5 + 3 for each of the 2 discarded
		assertEquals(11, scn.GetStrength(goblinA));
		assertEquals(5, scn.GetStrength(goblinB));
		assertEquals(5, scn.GetStrength(runner));
		assertInZone(Zone.SUPPORT, depths);
		assertInDiscard(massing);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DiscardingOneMakesAnOrcMinionWaitingForAnotherSkirmishStrengthPlus3ForThisSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var massing = scn.GetShadowCard("massing");
		var goblinA = scn.GetShadowCard("goblinA");
		var goblinB = scn.GetShadowCard("goblinB");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinA, goblinB, halftroll);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, goblinA}, new PhysicalCardImpl[]{sam, goblinB});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(goblinB));
		assertEquals(5, scn.GetStrength(goblinA));
		scn.ShadowPlayCard(massing);
		scn.ShadowChooseCards(halftroll);
		// Goblin B waits for Sam's skirmish, and is not in this one.
		scn.ShadowChooseCard(goblinB);

		// 5 + 3 for the one discarded
		assertEquals(8, scn.GetStrength(goblinB));
		assertEquals(5, scn.GetStrength(goblinA));
		assertInDiscard(halftroll, massing);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Frodo (4) loses to Goblin A (5) and is wounded; then Sam's skirmish, a phase of its own.
		scn.PassCurrentPhaseActions();
		// The Ruling Ring's response to the wound, declined.
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(5, scn.GetStrength(goblinB));
		scn.FreepsResolveSkirmish(sam);
		assertEquals(5, scn.GetStrength(goblinB));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var massing = scn.GetShadowCard("massing");
		var goblinA = scn.GetShadowCard("goblinA");
		var goblinB = scn.GetShadowCard("goblinB");
		scn.MoveCardsToHand(massing);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinA, goblinB);
		assertInHand(massing);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertInHand(massing);
		assertFalse(scn.ShadowPlayAvailable(massing));
	}
}
