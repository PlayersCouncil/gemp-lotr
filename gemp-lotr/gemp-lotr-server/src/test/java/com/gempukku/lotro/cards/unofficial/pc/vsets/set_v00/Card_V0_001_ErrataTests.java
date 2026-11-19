package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v00;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V0_001_ErrataTests
{
	// The card's own block: the Shadow player lays each next site, and any site laid as site 3 or site 6 is a
	// sanctuary.  Each player's adventure deck has a copy of Rath Dínen.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("guard", "5_83");         // Household Guard: a [rohan] Man companion, vitality 3; valiant only
					put("sam", "1_311");          // Sam, Son of Hamfast: vitality 4; his abilities are never used here
					// Guma, Plains Farmer, a [rohan] Man ally of vitality 2: "Fellowship: Exert Guma to heal a
					// [rohan] Man."  A heal that is not the sanctuary's.
					put("guma", "4_277");
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");       // Sirannon Ruins: River, and no text
					put("glade", "11_239");       // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("rath", "100_1");
					put("undeep", "11_250");      // North Undeep: the minion archery total is -3; there are no minions
					put("uplands", "11_257");     // Rohan Uplands: [uruk-hai] minions take no archery wounds; there are none
					put("mounds", "11_258");      // Slag Mounds: adds a burden when a companion is killed; none is
					// Expanding Marshland.  Never laid in these tests, and never named.
					put("site7", "11_238");
					put("site8", "11_238");
					put("site9", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void RathDinenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V0
		 * Name: Rath Dínen
		 * Unique: false
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: When sanctuary healing at this site, the Free Peoples player heals only 3 wounds instead
		 * 	of 5.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("rath");

		assertEquals("Rath Dínen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void AsSite3RathDinenHealsThreeWoundsAtTheStartOfTheTurnAndGumaStillHealsAFourth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(guma);

		// The copy the other player lays.
		var rath = scn.GetShadowSite("rath");

		// Two moves in the first turn, to Fangorn Glade and on to Rath Dínen as site 3; then the other
		// player's turn, and the first player's second turn begins at Rath Dínen.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("glade"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(rath);
		// Six wounds: one more than the rule's 5 heals could reach.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(sam, 2);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(rath, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(sam));

		// Three heals, each of them the player's to take or leave: Aragorn twice and the Guard once.
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 3"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard, sam));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, sam));
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));

		// No fourth heal, with three wounds still on the companions.
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(sam));

		// Guma's heal is not the sanctuary's, and a fourth wound is healed at Rath Dínen this turn.
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.FreepsActionAvailable(guma));
		scn.FreepsUseCardAction(guma);
		assertEquals(1, scn.GetWoundsOn(guma));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		scn.FreepsChooseCard(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AsSite6WithAnotherCopyLeftBehindRathDinenOffersThreeHealsToTakeOrDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		// The Ring-bearer (vitality 4) is a companion like the others for the sanctuary's healing.
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, guard);

		// The other player's copy is laid as site 3; the fellowship's own copy will be site 6.
		var rathBehind = scn.GetShadowSite("rath");
		var rath = scn.GetFreepsSite("rath");

		// First turn: to Fangorn Glade and on to the first Rath Dínen as site 3.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("glade"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(rathBehind);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Second turn: nobody is wounded, so nothing is asked at Rath Dínen; on to sites 4 and 5.
		assertEquals(rathBehind, scn.GetCurrentSite());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("undeep"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("uplands"));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Third turn: the fellowship's own copy lies ahead as site 6 (as it would had the other player gone
		// there first), and the fellowship moves to it and stays.
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.MoveCardToAdventurePath(rath);
		scn.FreepsPass();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(frodo, 2);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(rath, scn.GetCurrentSite());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(rathBehind, scn.GetSite(3));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(frodo));

		// One heal is taken.
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard, frodo));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		// Exactly two more are there to decline: three in all, though two copies of Rath Dínen are on the path.
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		scn.FreepsDeclineChoosing();
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		scn.FreepsDeclineChoosing();

		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(frodo));
	}

	@Test
	public void AsSite2RathDinenIsNoSanctuaryAndNoHealingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, guard, sam);

		var rath = scn.GetShadowSite("rath");

		// One move in the first turn, to Rath Dínen as site 2; the first player's second turn begins there.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(rath);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(sam));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(rath, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());

		// The site gives no healing of its own: where it is no sanctuary, nothing is healed.
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(sam));
	}

	@Test
	public void OnceRathDinenIsLeftBehindAnotherSanctuaryHealsFiveWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, guard, sam);

		var rath = scn.GetShadowSite("rath");
		var mounds = scn.GetShadowSite("mounds");

		// First turn: to Fangorn Glade and on to Rath Dínen as site 3.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("glade"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(rath);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Second turn: on to sites 4 and 5.
		assertEquals(rath, scn.GetCurrentSite());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("undeep"));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("uplands"));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Third turn: to Slag Mounds as site 6, a sanctuary with nothing to say about healing.
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(mounds);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(sam, 2);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(mounds, scn.GetCurrentSite());
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(rath, scn.GetSite(3));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(sam));

		// The rule's five heals: two for Aragorn, one for the Guard,
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 5"));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		// and the fourth and fifth, which Rath Dínen would not have given: one for Sam, one more for the Guard.
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, sam));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, sam));
		scn.FreepsChooseCard(guard);
		assertEquals(0, scn.GetWoundsOn(guard));

		// No sixth, with a wound still on Sam.
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(sam));
	}
}
