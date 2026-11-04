package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import com.gempukku.lotro.common.Phase;
import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;

import static org.junit.Assert.*;

public class Card_15_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shot", "15_21");
					put("elf", "1_53");          // Lorien Elf: an Elf companion with no keyword (cheated in past "To play, spot an Elf")
					put("celeborn", "1_34");     // Celeborn, Lord of Lorien: an Elf ally; a Fellowship heal used only on purpose
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a companion who is no Elf
					put("runner", "1_178");      // Goblin Runner: [moria] Orc minion, twilight cost 1, vitality 1
					put("brigand", "4_10");      // Dunlending Brigand: [dunland] Man minion, twilight cost 2, vitality 1, no text
					put("inquisitor", "1_268");  // Orc Inquisitor: [sauron] Orc minion, twilight cost 3, vitality 3 (its play trigger never fires: cheated in)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MightyShotStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mighty Shot
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot an Elf to make the fellowship archery total +1 for each minion with a twilight cost of 2 or less that you can spot.
		*/

		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");

		assertEquals("Mighty Shot", shot.getBlueprint().getTitle());
		assertNull(shot.getBlueprint().getSubtitle());
		assertFalse(shot.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, shot.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, shot.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, shot.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(shot, Timeword.ARCHERY));
		assertEquals(2, shot.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElfCompanionMakesTheArcheryTotalPlusOneForEachMinionOfCostTwoOrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(shot);
		assertInHand(shot);

		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		var inquisitor = scn.GetShadowCard("inquisitor");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, brigand, inquisitor);
		scn.SkipToPhase(Phase.ARCHERY);

		// No archer on either side.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(shot));
		scn.FreepsPlayCard(shot);

		// The Runner (cost 1) and the Brigand (cost 2) count; the Inquisitor (cost 3) does not.  "That you can
		// spot": every such minion is counted, and nobody is asked which.
		assertInDiscard(shot);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoElfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(shot);
		assertInHand(shot);

		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, brigand);
		scn.SkipToPhase(Phase.ARCHERY);

		// Frodo and Aragorn are no Elves.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(shot));
		assertInHand(shot);
	}

	@Test
	public void AnElfAllyIsSpottedAndOneCheapMinionAddsOneUntilTheEndOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(shot);
		assertInHand(shot);

		var runner = scn.GetShadowCard("runner");
		var inquisitor = scn.GetShadowCard("inquisitor");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, inquisitor);
		scn.SkipToPhase(Phase.ARCHERY);

		// The only Elf is an ally.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(shot));
		scn.FreepsPlayCard(shot);
		// The Runner alone counts.
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// Still +1 at the end of the archery phase; the one wound is the Shadow player's to place.
		scn.ShadowPass();
		assertEquals(1, scn.GetFreepsArcheryTotal());
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(inquisitor));
		scn.ShadowChooseCard(inquisitor);
		assertEquals(1, scn.GetWoundsOn(inquisitor));

		// Gone in the assignment phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void WithNoMinionOfCostTwoOrLessItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shot = scn.GetFreepsCard("shot");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(elf);
		scn.MoveCardsToHand(shot);
		assertInHand(shot);

		var inquisitor = scn.GetShadowCard("inquisitor");

		scn.StartGame();
		scn.MoveMinionsToTable(inquisitor);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(shot));
		scn.FreepsPlayCard(shot);

		assertInDiscard(shot);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
