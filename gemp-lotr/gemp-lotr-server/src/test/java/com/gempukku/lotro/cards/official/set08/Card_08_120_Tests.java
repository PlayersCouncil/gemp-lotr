package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_120_Tests
{

	// The King path, Osgiliath Channel its site 7.  Site 6 is Minas Tirith Seventh Circle (a regroup move from it
	// heals each [elven] companion: there is none); site 8 is Watchers of Cirith Ungol (a skirmish ability for a
	// [wraith] Orc).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Five companions beside Frodo, none with text that acts in a regroup phase.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: no text but its requirement to play
					put("farin", "1_11");       // Farin, Dwarven Emissary: strength +2 while skirmishing an Orc
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally, home 3F; a maneuver ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion; its play trigger is not reached by a cheat
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("poison", "8_28");      // Spider Poison: [gollum] Shadow possession in the support area; a skirmish ability
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OsgiliathChannelStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Osgiliath Channel
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 7K
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>River</b>. <b>Regroup:</b> Spot your minion and 6 companions to make the Free Peoples player
		 * choose to move again this turn.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Osgiliath Channel", card.getBlueprint().getTitle());
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
	public void WithYourMinionAndSixCompanionsTheFreePeoplesPlayerIsMadeToMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli", "guard", "farin");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		// The minion is laid before the regroup phase begins, so that the Free Peoples player's offers are
		// worked out with it in play.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.REGROUP);

		var site = scn.GetCurrentSite();
		assertEquals("Osgiliath Channel", site.getBlueprint().getTitle());
		// The Free Peoples player is asked first, with six companions and a minion in play that is not his.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));
		scn.FreepsPass();

		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		assertTrue(scn.ShadowActionAvailable(site));

		scn.ShadowUseCardAction(site);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		// No "another move" question: the fellowship moves on to site 8.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli", "guard");
		scn.MoveAlliesToTable("thrarin");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Osgiliath Channel", site.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(site));

		// The move is the Free Peoples player's to choose, as usual.
		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void NotOfferedWithNoMinionButAShadowPossessionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli", "guard", "farin");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(poison);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Osgiliath Channel", site.getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(site));
	}

	@Test
	public void NotOfferedOnceTheMoveLimitHasBeenReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli", "guard", "farin");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.FreepsPass();
		// To site 7 by the regroup move from site 6: two moves of two.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Osgiliath Channel", site.getBlueprint().getTitle());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		// Nothing for it to do: the fellowship cannot move again this turn.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(site));
	}

	@Test
	public void TheFreePeoplesPlayerIsMadeToMoveAgainOnceAndIsAskedAsUsualInALaterRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable("aragorn", "boromir", "gimli", "guard", "farin");
		// A move limit of 3, so that after the move the site demands there is a third to be asked about: 2 by
		// rule + 1.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(brigand);
		scn.FreepsPass();

		var site = scn.GetCurrentSite();
		assertEquals("Osgiliath Channel", site.getBlueprint().getTitle());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		scn.ShadowUseCardAction(site);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		// The second move is made without the Free Peoples player being asked.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());

		// The regroup phase at site 8: the limit allows a third move, and it is his to choose.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}
}
