package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_206_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_206");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [wraith] minions that each beat a Household Guard without killing him.
	protected VirtualTableScenario GetTwoWinsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronghold", "7_206");
					put("lemenya", "1_232");    // Ulaire Lemenya: [wraith] Nazgul, strength 9; her text is about [wraith] conditions on companions
					put("creeper", "8_75");     // Morgul Creeper: [wraith] Orc, strength 8; archery only at 3 burdens
					put("guard1", "5_83");      // Household Guard: strength 6, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongholdofbrMinasMorgulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Stronghold of Minas Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If your [wraith] minion wins a skirmish, place a [wraith] token here (limit 1 per site).<br><b>Regroup:</b> Spot 3 [wraith] tokens here to exhaust the Ring-bearer. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Stronghold of Minas Morgul", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "If your [wraith] minion wins a skirmish, place a [wraith] token here (limit 1 per site)": once for each
	// site in a turn (Q-0084, F-0146).  The definition counted by a regroup-phase limit; it is now the engine's
	// own count by site.  No board was found that tells the two apart: this test holds the limit, and passed
	// against the definition as it was.
	// F-0454.
	@Test
	public void OneTokenIsPlacedAtEachSiteHoweverManySkirmishesAreWonThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoWinsScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);

		var stronghold = scn.GetShadowCard("stronghold");
		var lemenya = scn.GetShadowCard("lemenya");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToSupportArea(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, creeper);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, lemenya}, new PhysicalCardImpl[]{guard2, creeper});

		// The first win at site 2: offered, and a token placed.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stronghold));

		// The second win at the same site: not offered.
		scn.FreepsResolveSkirmish(guard2);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		assertEquals(1, scn.GetCultureTokensOn(stronghold));

		// A second move in the same turn, and a win at site 3: offered again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(guard1, lemenya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(stronghold));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void StrongholdofbrMinasMorgulTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
