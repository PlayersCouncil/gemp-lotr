package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_355_Tests
{

	// The King path, Ruined Capitol its site 7.  Site 2 is Rohirrim Camp (a text about roaming minions only) and
	// site 6 Minas Tirith Seventh Circle, whose regroup-move heal reaches [elven] companions only.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength text only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, a Free Peoples character who is no companion
					// Durin's Secret: Fellowship event, spot Gandalf, a Dwarf and an Elf to make the move limit for this turn +1
					put("secret", "1_71");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2; Damage +1 is its whole text
					// Orc Chieftain: [sauron] Orc, cost 2; its text answers a weapon played on it
					put("chieftain", "1_266");
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_336");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "7_355");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RuinedCapitolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Ruined Capitol
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>River</b>. <b>Shadow:</b> Spot your minion and 6 companions to make the move limit for this turn -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Ruined Capitol", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void ShadowActionLowersARaisedMoveLimitByOneForTheRestOfTheTurnOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var capitol = scn.GetShadowSite(7);
		var secret = scn.GetFreepsCard("secret");
		// Frodo and these five are six companions; Gimli is the Dwarf and Legolas the Elf that Durin's Secret needs.
		scn.MoveCompanionsToTable("gandalf", "gimli", "legolas", "aragorn", "boromir");

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToHand(secret);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(secret);
		assertEquals(3, scn.GetMoveLimit()); // 2 + 1
		// Laid in the fellowship phase at site 6, to be on the table in the Shadow phase at the Capitol.
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		assertSame(capitol, scn.GetCurrentSite());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(capitol));

		scn.ShadowUseCardAction(capitol);
		// 2 + 1 - 1: a penalty of 2 would read 1.
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Still in force at the end of the turn: one move of two made, so another is asked for, and declined.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		scn.FreepsDeclineReconciliation();

		// The next turn, the other player's, has the move limit of two the rules give: the -1 ended with the turn.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetMoveLimit());
	}

	@Test
	public void IsNotOfferedWithFiveCompanionsAndAnAllyUntilASixthCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var capitol = scn.GetShadowSite(7);
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable("gandalf", "gimli", "legolas", "aragorn");
		scn.MoveAlliesToTable("bounder");

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		assertSame(capitol, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Frodo and four companions, and an ally: five companions.
		assertFalse(scn.ShadowActionAvailable(capitol));

		scn.MoveCompanionsToTable(boromir);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(capitol));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilTheShadowPlayerHasAMinionAndThenEndsTheTurnAfterOneMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var capitol = scn.GetShadowSite(7);
		scn.MoveCompanionsToTable("gandalf", "gimli", "legolas", "aragorn", "boromir");

		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.SetShadowHand(chieftain);
		scn.FreepsPass();

		assertSame(capitol, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Six companions and no minion.
		assertFalse(scn.ShadowActionAvailable(capitol));

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.ShadowActionAvailable(capitol));
		assertEquals(2, scn.GetMoveLimit());

		scn.ShadowUseCardAction(capitol);
		assertEquals(1, scn.GetMoveLimit());

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetMoveCount());
		assertEquals(1, scn.GetMoveLimit());
		scn.PassCurrentPhaseActions();
		// At the move limit the turn ends without the question of another move: the Free Peoples player reconciles.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
