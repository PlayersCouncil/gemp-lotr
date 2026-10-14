package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_189_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("lorien", "1_53");      // Lórien Elf: no text in play
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: +3 against a Nazgûl in a skirmish
					put("gimli", "1_13");       // Gimli, Son of Glóin: a skirmish ability used only on purpose
					// Faramir, Captain of Ithilien: a hunter (Hunter 2) who is a companion, not a minion; a maneuver
					// heal used only on purpose
					put("faramir", "18_48");
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: an ally; a fellowship ability only

					put("tracking", "15_171");  // Tracking Uruk: an [uruk-hai] Uruk-hai, Hunter 1 is its whole text
					// Hunting Herdsman: a [men] Man, Hunter 1; strength +3 for each follower (none here)
					put("herdsman", "15_83");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, and no hunter
				}},
				new HashMap<>()
				{{
					// City Gates is a Shadows-block site; the test lays the Shadow player's copy as the next site.
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("gates", "15_189");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CityGatesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: City Gates
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 1
		 * Direction: Right
		 * Game Text: When the Fellowship moves to this site, add a threat (or 2 if you can spot a hunter minion)
		 * 	for each companion over 4.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("gates");

		assertEquals("City Gates", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void MovingThereWithFiveCompanionsOneOfThemAHunterAddsOneThreatAndMovingOnAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Frodo and four more; Faramir is a hunter, but no minion.
		scn.MoveCompanionsToTable("sam", "aragorn", "lorien", "faramir");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("gates"));

		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();

		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves again, from City Gates to site 3: nothing is added.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingThereWithSixCompanionsAddsTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn", "lorien", "arwen", "gimli");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("gates"));

		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();

		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingThereWithFourCompanionsAndAnAllyAddsNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable("sam", "aragorn", "lorien");
		// An ally is no companion: with him counted there would be five.
		scn.MoveAlliesToTable("celeborn");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("gates"));

		assertEquals(0, scn.GetThreats());
		scn.FreepsPass();

		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnUrukHaiHunterMinionInPlayFiveCompanionsAddTwoThreatsAtARegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCompanionsToTable("sam", "aragorn", "lorien", "arwen");

		scn.StartGame();
		// The first move, to Ettenmoors, and no minion played: the turn goes on to its regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// A minion can be in play at a move only in the regroup phase.
		scn.MoveMinionsToTable(tracking);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("gates"));

		assertEquals(0, scn.GetThreats());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		// One companion over 4, two threats for him.
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMenHunterMinionInPlaySixCompanionsAddFourThreatsAtARegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCompanionsToTable("sam", "aragorn", "lorien", "arwen", "gimli");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(herdsman);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("gates"));

		assertEquals(0, scn.GetThreats());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		// Two companions over 4, two threats for each; six companions allow up to six.
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAMinionWhoIsNoHunterInPlayFiveCompanionsAddOneThreatAtARegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable("sam", "aragorn", "lorien", "faramir");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("gates"));

		assertEquals(0, scn.GetThreats());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("City Gates", scn.GetCurrentSite().getBlueprint().getTitle());
		// A minion, and a hunter companion, but no hunter minion: one threat for the one companion over 4.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
