package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_148_Tests
{

	// A Shadows-block path, on which whoever plays a site chooses it from his adventure deck: each player holds a
	// copy of Nurn.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Chasing Uruk: [uruk-hai] Uruk-hai, strength 10, fierce, hunter 1.  Its own text answers a
					// skirmish it wins, and none is fought here.
					put("chasing", "15_157");
					put("tracking", "15_171");    // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14; hunter 1 is its whole text
					// Hunting Herdsman: [men] Man, strength 8, hunter 1; strength +3 for each follower, and there is none
					put("herdsman", "15_83");
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text at all, so no hunter
					// Southron Explorer: strength 5, vitality 3, no hunter.  "Regroup: Exert this minion and remove
					// (5) to take control of a site."
					put("explorer", "4_250");
					// Faramir, Captain of Ithilien: a hunter who is a companion, strength 7; his maneuver ability is
					// used only on purpose
					put("faramir", "18_48");
				}},
				new HashMap<>() {{
					put("site1", "11_239");       // Fangorn Glade
					put("site2", "13_185");       // Abandoned Mine Shaft
					put("site3", "11_234");       // Crags of Emyn Muil: its text needs region 2
					put("site4", "17_148");
					put("site5", "18_138");       // Sirannon Ruins: no text
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
	public void NurnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Nurn
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: <b>Plains</b>. While you control this site, each of your hunter minions is strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("Nurn");

		assertEquals("Nurn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void OnceAPlayerTakesControlOfNurnEachOfHisHunterMinionsIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tracking = scn.GetShadowCard("tracking");
		var herdsman = scn.GetShadowCard("herdsman");
		var troop = scn.GetShadowCard("troop");
		var explorer = scn.GetShadowCard("explorer");

		// The first player starts on his own Nurn and moves on to Sirannon Ruins; the second player's turn takes
		// that fellowship there too.
		var nurn = scn.GetFreepsSite("site4");
		var otherNurn = scn.GetShadowSite("site4");
		scn.StartGame(nurn);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());

		// The first player's second turn: he moves to site 3, and the Shadow player lays his own copy of Nurn there.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(otherNurn);
		scn.MoveMinionsToTable(tracking, herdsman, troop, explorer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// One Nurn lies behind both markers and the fellowship stands on the other; nobody controls either.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertSame(otherNurn, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_PATH, nurn);
		assertFalse(scn.IsSiteControlled(nurn));
		assertFalse(scn.IsSiteControlled(otherNurn));
		assertEquals(14, scn.GetStrength(tracking));
		assertEquals(8, scn.GetStrength(herdsman));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(explorer));

		// The Southron Explorer takes control of the lowest site behind every marker: site 1, the first player's Nurn.
		assertTrue(scn.ShadowActionAvailable(explorer));
		scn.ShadowUseCardAction(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowControls(nurn));
		assertInZone(Zone.SUPPORT, nurn);
		assertFalse(scn.IsSiteControlled(otherNurn));

		// Each of his hunter minions is +1, once: the copy of Nurn on the path adds nothing.  The minions that
		// are no hunters are as they were.
		assertEquals(14 + 1, scn.GetStrength(tracking));
		assertEquals(8 + 1, scn.GetStrength(herdsman));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NurnGrantsStrengthToHunterMinionsOwnedByControllerAndNotOtherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hunter1 = scn.GetShadowCard("chasing");
		var hunter2 = scn.GetFreepsCard("chasing");
		var faramir = scn.GetShadowCard("faramir");
		scn.MoveCardsToHand(hunter1);

		// The first player starts on his own Nurn and moves on to Sirannon Ruins; the second player's turn takes
		// that fellowship there too.
		var nurn = scn.GetFreepsSite("site4");
		scn.StartGame(nurn);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Both markers are past Nurn: the second player takes control of it.
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		scn.ShadowTakeControlOfSite(nurn);
		assertTrue(scn.ShadowControls(nurn));
		assertInZone(Zone.SUPPORT, nurn);

		// The first player's second turn, on to Crags of Emyn Muil.
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(hunter1);

		// Base 10 strength, +1 from controlling Nurn
		assertEquals(10 + 1, scn.GetStrength(hunter1));

		// Now we flip sides and confirm that player 1 does not get a boost from Nurn while player 2 controls it
		// (the second player is the one moving a fellowship, so the names are backwards).
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowControls(nurn));
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(hunter2);
		scn.SetTwilight(10);
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		scn.FreepsPlayCard(hunter2);

		// Base 10 strength, no bonus from the opponent controlling Nurn
		assertInZone(Zone.SHADOW_CHARACTERS, hunter2);
		assertEquals(10, scn.GetStrength(hunter2));
		// The controller's own hunter, who is a companion and no minion, gains nothing either.
		assertTrue(scn.HasKeyword(faramir, Keyword.HUNTER));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}
}
