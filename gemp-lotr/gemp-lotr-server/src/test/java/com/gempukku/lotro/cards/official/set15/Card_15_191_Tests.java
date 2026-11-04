package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_191_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: a second [shire] card; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor]; a maneuver ability used only on purpose
					put("lordofmoria", "1_21"); // Lord of Moria: a [dwarven] condition in the support area; each Dwarf is damage +1
					// Uruk-hai Armory: an [isengard] condition in the support area; lowers the fellowship archery total only
					put("armory", "1_157");
					// Pinned Down: a [moria] condition in the support area; lowers the fellowship archery total only
					put("pinned", "1_192");
				}},
				new HashMap<>()
				{{
					// Gate of Mordor is a Shadows-block site; the tests lay the Shadow player's copy as the next site.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("gate", "15_191");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GateofMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Gate of Mordor
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. Each time the fellowship moves to this site, add (1) for each culture you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("gate");

		assertEquals("Gate of Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void MovingThereAddsOneForEachActiveCultureAndMovingOnAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetShadowSite("gate");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var ownPinned = scn.GetFreepsCard("pinned");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable("sam", "aragorn");
		scn.MoveCardsToSupportArea(lordofmoria, ownPinned, armory);

		scn.StartGame();
		scn.MoveCardToAdventurePath(gate);
		assertInZone(Zone.SUPPORT, lordofmoria);
		assertInZone(Zone.SUPPORT, ownPinned);
		assertInZone(Zone.SUPPORT, armory);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertSame(gate, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 3 companions + 0 for the Gate + 4 cultures: [shire] (Frodo and Sam, one culture), [gondor] (Aragorn),
		// [dwarven] (Lord of Moria) and [isengard] (the Shadow player's Armory).  The Free Peoples player's own
		// [moria] Pinned Down is not active on his turn and cannot be spotted.
		assertEquals(7, scn.GetTwilight());

		// No minion is played: the turn goes on to the regroup phase, and the fellowship moves on to site 3.
		scn.SkipToMovementDecision();
		assertEquals(7, scn.GetTwilight());
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 7 + 3 companions + 0 for Rivendell Valley: a move from the Gate adds no cultures.
		assertEquals(10, scn.GetTwilight());
	}

	@Test
	public void TheSecondPlayersFellowshipMovingThereAddsOneForEachCultureActiveOnHisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gate = scn.GetShadowSite("gate");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var firstPlayersPinned = scn.GetFreepsCard("pinned");
		var secondPlayersArmory = scn.GetShadowCard("armory");
		scn.MoveCompanionsToTable("sam", "aragorn");
		scn.MoveCardsToSupportArea(lordofmoria, firstPlayersPinned, secondPlayersArmory);

		scn.StartGame();
		scn.MoveCardToAdventurePath(gate);
		assertInZone(Zone.SUPPORT, lordofmoria);
		assertInZone(Zone.SUPPORT, firstPlayersPinned);
		assertInZone(Zone.SUPPORT, secondPlayersArmory);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertSame(gate, scn.GetCurrentSite());
		// 3 companions + 4 cultures, as on the first player's own turn
		assertEquals(7, scn.GetTwilight());

		// The second player's turn.  His fellowship, his Frodo alone, moves from site 1 to the same Gate.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.SetTwilight(0);
		scn.ShadowPass();

		assertSame(gate, scn.GetCurrentSite());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		// 1 companion + 0 for the Gate + 2 cultures: [shire] (his Frodo) and [moria] (the first player's Pinned Down,
		// a Shadow card active now).  The first player's companions and Lord of Moria are not active on this turn,
		// nor is the second player's own Armory.
		assertEquals(3, scn.GetTwilight());
	}
}
