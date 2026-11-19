package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("patrol", "13_104");
					put("goblin", "11_137");    // Scurrying Goblin: [orc] Orc, strength 5, vitality 1, cost 1; adds (2) only when played at an underground site
					put("halftroll", "14_12");  // Half-troll of Far Harad: [orc] Half-troll, strength 8; an optional start-of-assignment trigger that needs (2)
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; "When you play" only, and he is cheated in
					put("enemy", "13_106");     // Enemy Upon Enemy: [orc] support-area condition; abilities used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability used only on purpose
					put("defend", "4_115");     // Defend It and Hope: Maneuver event, exert a [gondor] Man to wound a minion once
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChamberPatrolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Chamber Patrol
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] minion.<br>While the fellowship is in region 1, [orc] Orcs cannot take wounds.<br>While the fellowship is in region 2, the current site gains <b>underground</b>.<br>While the fellowship is in region 3, each [orc] Orc is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("patrol");

		assertEquals("Chamber Patrol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var goblin = scn.GetShadowCard("goblin");
		var runner = scn.GetShadowCard("runner");
		var enemy = scn.GetShadowCard("enemy");
		scn.MoveCardsToHand(patrol, goblin);
		scn.MoveCardsToSupportArea(enemy);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, enemy);
		scn.MoveMinionsToTable(runner);
		assertInPlay(runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [moria] Orc and an [orc] condition: no [orc] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(patrol));

		scn.ShadowPlayCard(goblin);
		assertInPlay(goblin);
		assertTrue(scn.ShadowPlayAvailable(patrol));
		scn.ShadowPlayCard(patrol);
		assertInPlay(patrol);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnOrcMinionWhoIsNoOrcIsSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(patrol);

		scn.StartGame();
		scn.MoveMinionsToTable(halftroll);
		assertInPlay(halftroll);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(patrol));
		scn.ShadowPlayCard(patrol);
		assertInPlay(patrol);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InRegion1OrcOrcsCannotBeWoundedAndOtherMinionsCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var patrol = scn.GetShadowCard("patrol");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(patrol, goblin, halftroll, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 3: the last site of region 1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertTrue(scn.FreepsPlayAvailable(defend));
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Neither [orc] Orc; the [orc] minion who is no Orc and the Orc of another culture are.
		assertTrue(scn.FreepsHasCardChoiceAvailable(halftroll, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(patrol, goblin));
		scn.FreepsChooseCard(halftroll);
		assertEquals(1, scn.GetWoundsOn(halftroll));

		// Nor is the site underground here, nor the Orcs stronger.
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertEquals(11, scn.GetStrength(patrol));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InRegion2AnOrcOrcCanBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var patrol = scn.GetShadowCard("patrol");
		var goblin = scn.GetShadowCard("goblin");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(patrol, goblin);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(patrol));
		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, goblin));
		scn.FreepsChooseCard(patrol);
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InRegion2TheCurrentSiteAloneGainsUnderground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.SkipToSite(6);

		// Galadriel's Glade, site 6, is not underground by itself; nor is Rivendell Valley, site 3, behind it.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(scn.GetSite(3), Keyword.UNDERGROUND));

		scn.MoveMinionsToTable(patrol);
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(scn.GetSite(3), Keyword.UNDERGROUND));
		// Not region 3: no strength bonus.
		assertEquals(11, scn.GetStrength(patrol));
	}

	@Test
	public void InRegion3EachOrcOrcIsStrengthPlus2AndTheSiteGainsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var patrol = scn.GetShadowCard("patrol");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(goblin, halftroll, runner);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(goblin));

		scn.MoveMinionsToTable(patrol);
		assertEquals(13, scn.GetStrength(patrol));
		assertEquals(7, scn.GetStrength(goblin));
		// The [orc] minion who is no Orc and the Orc of another culture are untouched.
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(5, scn.GetStrength(runner));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
	}
}
