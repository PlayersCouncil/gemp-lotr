package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_118");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path of the rig with Great Chasm as its site 4.
	protected VirtualTableScenario GetSite4Scenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksman1", "1_176");  // Goblin Marksman: a [moria] archer, (3), site 4; Archer is its whole text
					put("marksman2", "1_176");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; killed in a skirmish here
					// Now for Ruin: "Each time a minion is killed, spot 2 valiant companions to add a [rohan] token
					// here. Each time there are 3 [rohan] tokens here, remove all tokens here to play a [rohan]
					// condition or event from your discard pile. The Shadow player may then play a minion from
					// their discard pile."  A way for a minion to be played in a skirmish phase.
					put("ruin", "102_52");
					put("doom", "8_86");        // Doom Drove Them: a [rohan] condition for Now for Ruin to play, (1), with nothing to spot
					put("guard1", "5_83");      // Household Guard: a valiant companion, strength 6, with no other text
					put("guard2", "5_83");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "2_118");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatChasmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Great Chasm
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 4
		 * Type: Site
		 * Subtype: 
		 * Site Number: 4
		 * Game Text: <b>Underground</b>. The twilight cost of the first [moria] archer played each Shadow phase is -2.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite(4);
		var card = scn.GetFreepsCard("card");

		assertEquals("Great Chasm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void GreatChasmTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(4, scn.GetTwilight());
	}

	@Test
	public void AMoriaArcherPlayedInAnotherPhaseDoesNotTakeTheDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSite4Scenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var doom = scn.GetFreepsCard("doom");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.MoveCardsToSupportArea(ruin);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCardsToDiscard(doom);
		scn.MoveCardsToHand(marksman1);
		scn.MoveCardsToDiscard(marksman2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());

		// The Shadow phase: the first [moria] archer costs 3 - 2.
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(marksman1);
		assertEquals(twilight - 1, scn.GetTwilight());

		scn.MoveMinionsToTable(runner);
		// The Marksman's arrow goes to Frodo.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(guard1, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard1);

		assertEquals(2, scn.GetCultureTokensOn(ruin));
		// The Guard (6) kills the Runner (5): a third token, Doom Drove Them is played from the discard pile, and
		// the Shadow player may play a minion from his, in the skirmish phase.
		scn.PassCurrentPhaseActions();

		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.SUPPORT, doom);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasCardChoiceAvailable(marksman2, runner));
		twilight = scn.GetTwilight();
		scn.ShadowChooseCardBPFromSelection(marksman2);

		assertInZone(Zone.SHADOW_CHARACTERS, marksman2);
		// 3 to play, and no discount: no [moria] archer was played before it in this phase, which is not a
		// Shadow phase.
		assertEquals(twilight - 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
