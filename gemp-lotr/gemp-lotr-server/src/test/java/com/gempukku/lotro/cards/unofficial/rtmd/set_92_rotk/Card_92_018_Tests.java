package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_018_Tests
{
	private final HashMap<String, String> cards = new HashMap<>() {{
		// Aragorn, Ranger of the North (1_89): Gondor companion, strength 8, vitality 4
		put("aragorn", "1_89");
		// Gimli, Dwarf of Erebor (1_13): Dwarven companion, strength 6, vitality 3
		put("gimli", "1_13");
		// Merry, Friend to Sam (1_302): Hobbit companion
		put("merry", "1_302");
	}};

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_18", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_18"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_18
		 * Type: MetaSite
		 * Game Text: Each time your fellowship moves, discard 1 non-Hobbit companion (or 2 if at site 7 or higher).
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_18", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
	}

	@Test
	public void DiscardsOneNonHobbitOnFellowshipMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(aragorn, merry);

		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsChooseAny(); //Timing conflict between Bree Gate and the mod

		// Moving from site 1 to site 2 triggers the effect — discard 1 non-hobbit
		// Merry is a hobbit so not a valid target; Aragorn auto-selected
		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.FREE_CHARACTERS, merry);
	}

	@Test
	public void DiscardsOnRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(aragorn, gimli, merry);

		scn.StartGame();
		scn.FreepsPass();
		scn.FreepsChooseAny(); //Timing conflict between Bree Gate and the mod

		//Freeps has to choose who to discard
		assertTrue(scn.FreepsHasCardChoicesAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(merry));

		scn.FreepsChooseCard(aragorn);

		// First move (site 1→2): discard 1, aragorn auto-selected
		assertInZone(Zone.DISCARD, aragorn);

		//Now the regroup move
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// Second move (site 2→3): discard 1, gimli auto-selected
		assertInZone(Zone.DISCARD, gimli);
		assertInZone(Zone.FREE_CHARACTERS, merry);
	}

	@Test
	public void DiscardsOneAtSite6() throws DecisionResultInvalidException, CardNotFoundException {
		// Moving from site 6 (at site 6, below 7 threshold), should discard 1
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// No companions to discard on the initial move
		scn.SkipToSite(6);

		// Now cheat aragorn in and move from site 5→6
		scn.MoveCompanionsToTable(aragorn);
		scn.FreepsPassCurrentPhaseAction();
		// Move triggers, site 5 < 7 so discard 1
		assertInZone(Zone.DISCARD, aragorn);
	}

	@Test
	public void DiscardsTwoAtSite7OrHigher() throws DecisionResultInvalidException, CardNotFoundException {
		// At site 7, moving should discard 2 non-hobbit companions
		var scn = GetFreepsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		// No companions to discard on moves
		scn.SkipToSite(7);

		// Now cheat in 2 non-hobbit companions and move from site 7→8
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.FreepsPassCurrentPhaseAction();

		// Move triggers, site 7 >= 7 so discard 2

		assertInZone(Zone.DISCARD, aragorn);
		assertInZone(Zone.DISCARD, gimli);
	}

	@Test
	public void DoesNotTriggerWhenOwnedByShadow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.SHADOW);

		// No discard triggered — shadow owns the modifier
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
	}
}
