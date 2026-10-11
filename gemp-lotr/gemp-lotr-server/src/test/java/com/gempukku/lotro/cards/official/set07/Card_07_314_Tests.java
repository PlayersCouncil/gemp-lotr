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

public class Card_07_314_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_314");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two [sauron] minions that each beat a Household Guard without killing him.
	protected VirtualTableScenario GetTwoWinsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stronghold", "7_314");
					put("chieftain1", "1_266"); // Orc Chieftain: [sauron] Orc, strength 7; his text answers a weapon played on him
					put("chieftain2", "1_266");
					put("guard1", "5_83");      // Household Guard: strength 6, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrongholdofCirithUngolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Stronghold of Cirith Ungol
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If your [sauron] minion wins a skirmish, place a [sauron] token here (limit 1 per site).<br><b>Regroup:</b> Add (2) for each token here. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Stronghold of Cirith Ungol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "If your [sauron] minion wins a skirmish, place a [sauron] token here (limit 1 per site)": once for each
	// site in a turn (Q-0084, F-0146).  The definition counted by a regroup-phase limit; it is now the engine's
	// own count by site.  No board was found that tells the two apart: this test holds the limit, and passed
	// against the definition as it was.
	// F-0455.
	@Test
	public void OneTokenIsPlacedAtEachSiteHoweverManySkirmishesAreWonThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoWinsScenario();

		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);

		var stronghold = scn.GetShadowCard("stronghold");
		var chieftain1 = scn.GetShadowCard("chieftain1");
		var chieftain2 = scn.GetShadowCard("chieftain2");
		scn.MoveCardsToSupportArea(stronghold);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain1, chieftain2);
		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, chieftain1}, new PhysicalCardImpl[]{guard2, chieftain2});

		// The first win at site 2: offered, and a token placed.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
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
		scn.FreepsAssignToMinions(guard1, chieftain1);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard1);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(guard1));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(stronghold));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(stronghold));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void StrongholdofCirithUngolTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
