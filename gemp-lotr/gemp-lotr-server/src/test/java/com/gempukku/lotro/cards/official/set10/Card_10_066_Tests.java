package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("outnumbered", "10_66");

					put("hound", "7_192");          // Morgul Hound: [wraith] Orc, strength 5; strength +4 and fierce while the Ring-bearer has 2 wounds
					put("mongrel", "7_194");        // Morgul Mongrel: [wraith] Orc of another title, strength 7; fierce and damage +1 at 3 wounds on the Ring-bearer
					put("lemenya", "1_232");        // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] minion who is no Orc; his text needs a [wraith] condition on a companion
					put("chieftain", "1_266");      // Orc Chieftain: an Orc who is not [wraith] ([sauron]); a trigger on a weapon played on him only

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");         // Boromir, Son of Denethor: strength 7; a skirmish ability used only on purpose
					put("sam", "1_311");            // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TenTimesOutnumberedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Ten Times Outnumbered
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [wraith] Orc strength +2 for each wound on the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("outnumbered");

		assertEquals("Ten Times Outnumbered", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAWraithOrcStrengthPlus2ForTheOneWoundOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var outnumbered = scn.GetShadowCard("outnumbered");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(hound, mongrel, lemenya, chieftain);
		scn.MoveCardsToHand(outnumbered);
		// One wound on the Ring-bearer; Sam's two wounds and Frodo's burdens are no wounds on the Ring-bearer.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 2);
		scn.AddBurdens(2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		assertInHand(outnumbered);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hound);
		// The Mongrel, Lemenya and the Chieftain are left out of the fighting.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.ShadowPlayAvailable(outnumbered));
		scn.ShadowPlayCard(outnumbered);

		// A [wraith] Orc in the skirmish or out of it; not the [wraith] minion who is no Orc, nor the [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, mongrel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		// 5 + 2 for the one wound on Frodo.
		assertEquals(7, scn.GetStrength(hound));
		assertEquals(7, scn.GetStrength(mongrel));
		assertInDiscard(outnumbered);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoWoundsAnOrcWhoseSkirmishIsToComeIsPlus4UntilThisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var outnumbered = scn.GetShadowCard("outnumbered");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");

		scn.StartGame();
		scn.MoveMinionsToTable(hound, mongrel);
		scn.MoveCardsToHand(outnumbered);
		scn.AddWoundsToChar(frodo, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, hound}, new PhysicalCardImpl[]{boromir, mongrel});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, hound));
		assertEquals(7, scn.GetStrength(mongrel));
		scn.ShadowPlayCard(outnumbered);
		scn.ShadowChooseCard(mongrel);

		// 7 + 2 for each of the two wounds on Frodo.
		assertEquals(11, scn.GetStrength(mongrel));

		// Still in force at the next decision of this skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(mongrel));

		// The Hound's 5 + 4 beats Aragorn's 8; then the Mongrel's own skirmish, a phase of its own.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, mongrel));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var outnumbered = scn.GetShadowCard("outnumbered");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(hound);
		scn.MoveCardsToHand(outnumbered);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(outnumbered);

		// The Shadow player's assignment actions, next door to the skirmish.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(5);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(outnumbered));
		assertInHand(outnumbered);
		assertEquals(5, scn.GetStrength(hound));
	}
}
