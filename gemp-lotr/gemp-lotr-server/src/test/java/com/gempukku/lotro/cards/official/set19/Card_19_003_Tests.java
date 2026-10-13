package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "19_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTwoBearersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("twitching1", "19_3");
					put("twitching2", "19_3");
					// Gimli, Son of Gloin: a [dwarven] companion, strength 6, damage +1; a skirmish ability never used
					put("gimli", "1_13");
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion with no text once in play
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StillTwitchingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Still Twitching
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: Bearer must be a [dwarven] companion.<br>Bearer gains <b>hunter 2</b>. Each time bearer wins a skirmish, you may transfer this condition to another [dwarven] companion.<br>At the end of each turn, if bearer did not win a skirmish this turn, discard this condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Still Twitching", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsDiscardedAtTheEndOfATurnInWhichItsBearerWonNoSkirmishAndStaysWhenHeWonOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoBearersScenario();

		// One copy on each of two [dwarven] companions: Gimli fights and wins, the Dwarf Guard fights nobody.
		var onGimli = scn.GetFreepsCard("twitching1");
		var onGuard = scn.GetFreepsCard("twitching2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, onGimli);
		scn.AttachCardsTo(guard, onGuard);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		scn.PassSkirmishActions();

		// Gimli's 6, and the 2 of hunter 2, beats the Goblin Runner's 5. The transfer his copy then offers
		// is declined; the Guard's copy, whose bearer won nothing, offers none.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(onGimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(onGuard));
		scn.FreepsDeclineOptionalTrigger();
		assertInDiscard(runner);

		// Both are still borne when the fellowship decides not to move again.
		scn.SkipToMovementDecision();
		assertAttachedTo(onGimli, gimli);
		assertAttachedTo(onGuard, guard);

		scn.FreepsChooseToStay();
		scn.PassReconcile(scn.P1());

		// The turn is over. Gimli won a skirmish in it and keeps his copy. The Guard did not, though another
		// [dwarven] companion did: his copy is in the discard pile.
		assertAttachedTo(onGimli, gimli);
		assertInDiscard(onGuard);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
