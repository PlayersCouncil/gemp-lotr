package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_220_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("war", "7_220");
					put("mongrel", "7_194");    // Morgul Mongrel: a [wraith] Orc, strength 7, vitality 2; its text needs 3 wounds on the Ring-bearer
					put("soldier", "7_199");    // Morgul Soldier: a [wraith] Orc of another title, strength 6; "Nazgûl are strength +1"
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, no Orc, strength 9 (+1 from the Soldier)
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7; its text answers a weapon played on it
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WarLongPlannedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: War Long Planned
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [wraith] Orc strength +2 (or +4 if you spot 2 wounds on the Ring-bearer).
		*/

		var scn = GetScenario();

		var war = scn.GetFreepsCard("war");

		assertEquals("War Long Planned", war.getBlueprint().getTitle());
		assertNull(war.getBlueprint().getSubtitle());
		assertFalse(war.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, war.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, war.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, war.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(war, Timeword.SKIRMISH));
		assertEquals(1, war.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAWraithOrcStrengthPlus2WhileTheRingBearerHasOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var mongrel = scn.GetShadowCard("mongrel");
		var soldier = scn.GetShadowCard("soldier");
		var lemenya = scn.GetShadowCard("lemenya");
		var chieftain = scn.GetShadowCard("chieftain");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(war);
		// One wound on the Ring-bearer, and two on another Ring-bound companion, which do not count.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertInHand(war);

		scn.StartGame();
		scn.MoveMinionsToTable(mongrel, soldier, lemenya, chieftain);
		scn.SetTwilight(3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, mongrel);
		// The Soldier, Lemenya and the Chieftain stay unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, mongrel));
		assertEquals(7, scn.GetStrength(mongrel));
		assertEquals(6, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(chieftain));
		assertInHand(war);
		assertTrue(scn.ShadowPlayAvailable(war));

		scn.ShadowPlayCard(war);
		// [wraith] Orcs of two titles, in the skirmish and in none; not a [wraith] Nazgûl, nor a [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mongrel, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(mongrel);

		assertEquals(9, scn.GetStrength(mongrel));
		assertEquals(6, scn.GetStrength(soldier));
		assertEquals(10, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(chieftain));
		assertInDiscard(war);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoWoundsOnTheRingBearerTheOrcIsStrengthPlus4UntilItsSkirmishPhaseEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var mongrel = scn.GetShadowCard("mongrel");
		var soldier = scn.GetShadowCard("soldier");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(war);
		scn.AddWoundsToChar(frodo, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.StartGame();
		scn.MoveMinionsToTable(mongrel, soldier);
		scn.SetTwilight(3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, mongrel}, new PhysicalCardImpl[]{gimli, soldier});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The Soldier, who waits for his own skirmish, is made strength +4 during Aragorn's.
		assertEquals(6, scn.GetStrength(soldier));
		scn.ShadowPlayCard(war);
		scn.ShadowChooseCard(soldier);
		assertEquals(10, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 8 against 7: Aragorn wins.  In the next skirmish phase the bonus is gone.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(mongrel));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(6, scn.GetStrength(soldier));
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier));
		assertEquals(6, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var war = scn.GetShadowCard("war");
		var mongrel = scn.GetShadowCard("mongrel");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(war);
		assertInHand(war);

		scn.StartGame();
		scn.MoveMinionsToTable(mongrel);
		scn.SetTwilight(3);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertInHand(war);
		assertFalse(scn.ShadowPlayAvailable(war));
		assertEquals(7, scn.GetStrength(mongrel));
	}
}
