package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_026_Tests
{

	protected VirtualTableScenario GetFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("speak", "2_26");
					put("gandalf", "1_72");
					put("fodder1", "1_73");
					put("fodder2", "1_74");
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "30_50"); //HDG Troll Hoard, underground
					put("site3", "1_337");
					put("site4", "1_343");
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

	protected VirtualTableScenario GetShadowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("speak", "2_26");
					put("gandalf", "1_72");
				}},
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void SpeakFriendandEnterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Speak "Friend" and Enter
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Fellowship</b> <i>or</i> <b>Regroup:</b> Spot Gandalf to play the fellowship's next site (replacing opponent's site if necessary). Draw a card if you play an underground site.
		*/

		var scn = GetFellowshipScenario();

		var card = scn.GetFreepsCard("speak");

		assertEquals("Speak \"Friend\" and Enter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpeakFriendandEnterRequiresGandalfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(speak, gandalf);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(speak));

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(speak));
	}

	@Test
	public void SpeakFriendandEnterDrawsACardIfSiteIsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		scn.FreepsPlayCard(speak);
		// Played Speak Friend, drew a card to replace it
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
	}

	@Test
	public void SpeakFriendandEnterCanBeUsedInFellowshipPhaseToPlayNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site2 = scn.GetFreepsSite(2);

		scn.StartGame();

		assertEquals(Zone.ADVENTURE_DECK, site2.getZone());

		scn.FreepsPlayCard(speak);
		assertEquals(Zone.ADVENTURE_PATH, site2.getZone());
	}

	@Test
	public void SpeakFriendandEnterCanBeUsedInRegroupPhaseToPlayNextSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site3 = scn.GetFreepsSite(3);

		scn.StartGame();

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(Zone.ADVENTURE_DECK, site3.getZone());
		assertTrue(scn.FreepsPlayAvailable(speak));

		scn.FreepsPlayCard(speak);
		assertEquals(Zone.ADVENTURE_PATH, site3.getZone());
	}

	@Test
	public void SpeakFriendandEnterReplacesOpponentSite() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetFellowshipScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site2 = scn.GetFreepsSite(2);
		var shadow2 = scn.GetShadowSite(2);

		scn.StartGame();

		scn.MoveCardToAdventurePath(shadow2);

		assertEquals(Zone.ADVENTURE_DECK, site2.getZone());
		assertEquals(Zone.ADVENTURE_PATH, shadow2.getZone());

		scn.FreepsPlayCard(speak);
		assertEquals(Zone.ADVENTURE_PATH, site2.getZone());
		assertEquals(Zone.ADVENTURE_DECK, shadow2.getZone());
	}

	@Test
	public void SpeakFriendandEnterCanBeUsedInFellowshipPhaseToPlayNextSiteOnShadowsPath() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetShadowsScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site1 = scn.GetFreepsSite("site1");
		var site2 = scn.GetFreepsSite("site2");

		scn.StartGame(site1);

		assertEquals(Zone.ADVENTURE_DECK, site2.getZone());

		scn.FreepsPlayCard(speak);
		assertTrue(scn.FreepsDecisionAvailable("Choose site to play"));
		scn.FreepsChooseCardBPFromSelection(site2);
		assertEquals(Zone.ADVENTURE_PATH, site2.getZone());
	}

	@Test
	public void SpeakFriendandEnterCanBeUsedInRegroupPhaseToPlayNextSiteOnShadowsPath() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetShadowsScenario();

		var speak = scn.GetFreepsCard("speak");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable("gandalf");

		var site1 = scn.GetFreepsSite("site1");
		var site3 = scn.GetFreepsSite("site3");

		var site2 = scn.GetFreepsSite("site2");

		scn.StartGame(site1);
		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowChooseCardBPFromSelection(site2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(Zone.ADVENTURE_DECK, site3.getZone());
		assertTrue(scn.FreepsPlayAvailable(speak));

		scn.FreepsPlayCard(speak);
		assertTrue(scn.FreepsDecisionAvailable("Choose site to play"));
		scn.FreepsChooseCardBPFromSelection(site3);
		assertEquals(Zone.ADVENTURE_PATH, site3.getZone());
	}
}
