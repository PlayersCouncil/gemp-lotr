package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_192_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "15_192");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The turn-skippers walk a Fellowship-block path.  Isengard Ruined stands in each adventure deck in place of
	// site 9, which the test does not reach, and is put on the path as site 2.  Two followers and their bearers.
	protected VirtualTableScenario GetFollowerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("radagast", "13_38");   // Radagast, Tender of Beasts: follower, Aid (2); its text needs 2 [gandalf] companions
					// Pallando, Far-travelling One: follower, Aid (2); exerts a bearer who is not a [gandalf] companion
					put("pallando", "13_37");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("savage", "1_151");     // Uruk Savage: so that there is a maneuver phase
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("ruined", "15_192");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardRuinedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Isengard Ruined
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 1
		 * Type: Site
		 * Subtype: 
		 * Site Number: *
		 * Game Text: <b>Battleground</b>. Each time a follower is transferred to a character, the first Shadow player may exert a companion twice.
		*/

		var scn = GetScenario();

		//Use this once you have set the deck up properly
		//var card = scn.GetFreepsSite();
		var card = scn.GetFreepsCard("card");

		assertEquals("Isengard Ruined", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	// "Each time a follower is transferred to a character, the first Shadow player may exert a companion
	// twice": a "may" on a trigger is asked and may be declined (Q-0021; Q-0220: G).  The definition had the
	// Shadow player choose a companion each time, with no way to choose none.
	// F-0461.
	@Test
	public void TheShadowPlayerIsAskedEachTimeAndMayDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFollowerScenario();

		var radagast = scn.GetFreepsCard("radagast");
		var pallando = scn.GetFreepsCard("pallando");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToSupportArea(radagast, pallando);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("ruined"));
		scn.FreepsPass();
		var ruined = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Radagast's Aid is paid and he goes to Aragorn: the Shadow player is asked, and declines.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(radagast));
		scn.FreepsChooseOptionalTrigger(radagast);
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(radagast, aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruined));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Pallando's Aid is paid and he goes to Gandalf: asked again, and this time Aragorn exerts twice.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gandalf);
		assertAttachedTo(pallando, gandalf);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruined));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		scn.ShadowChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void IsengardRuinedTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(1, scn.GetTwilight());
	}
}
