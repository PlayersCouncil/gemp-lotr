package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_188_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_188");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Shadows path: the Shadow player lays Breeding Pit of Isengard as site 2.
	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("herdsman1", "15_83");  // Hunting Herdsman: a [men] hunter minion, (3), site 4; strength +3 for each follower
					put("herdsman2", "15_83");
					// Bold Easterling: site 4, strength 8, vitality 1. "Skirmish: If this minion is skirmishing,
					// discard this minion to play a [men] minion from your discard pile."
					put("easterling", "15_73");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");
					put("site2", "15_188");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void BreedingpitofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Breeding pit of Isengard
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 0
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Underground</b>. The twilight cost of the first hunter minion played each Shadow phase is -2.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite();
		var card = scn.GetFreepsCard("card");

		assertEquals("Breeding pit of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void BreedingpitofIsengardTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void AHunterMinionPlayedInAnotherPhaseDoesNotTakeTheDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var herdsman1 = scn.GetShadowCard("herdsman1");
		var herdsman2 = scn.GetShadowCard("herdsman2");
		var easterling = scn.GetShadowCard("easterling");
		scn.MoveCardsToHand(herdsman1);
		scn.MoveCardsToDiscard(herdsman2);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));

		assertEquals(2, scn.GetCurrentSiteNumber());

		// The Shadow phase: 3 to play + 2 roaming - 2 for the first hunter minion.
		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(herdsman1);
		assertEquals(twilight - 3, scn.GetTwilight());

		scn.MoveMinionsToTable(easterling);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, easterling);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		twilight = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(easterling));
		scn.ShadowUseCardAction(easterling);
		// The Easterling is in the discard pile now beside the other Herdsman.
		scn.ShadowChooseCardBPFromSelection(herdsman2);

		assertInZone(Zone.DISCARD, easterling);
		assertInZone(Zone.SHADOW_CHARACTERS, herdsman2);
		// 3 to play + 2 roaming, and no discount: no hunter minion was played before it in this phase, which is
		// not a Shadow phase.
		assertEquals(twilight - 5, scn.GetTwilight());
		// The skirmish is over with its only minion gone; the Herdsman played is fierce until the regroup phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
