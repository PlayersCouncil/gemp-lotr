package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MoveLimitModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_064_Tests
{

	// The Hobbit table, on the Hobbit path with the Free Peoples Pack's Dale in place of Erebor as site 7.  Site 6
	// is Esgaroth 30_54 (an optional play of an ally at the start of the regroup phase, declined), site 8 Smaug's
	// Den 30_56 (a Shadow ability to play Smaug, not used).  Bilbo, Reliable Companion 30_46 is the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Five [dwarven] Dwarf companions, with Bilbo six.  Balin and Dwalin have abilities of the
					// fellowship and maneuver phases, Fili a response to a wound, Glóin a skirmish ability and Kili
					// an archery one: none of them is used.
					put("balin", "30_2");
					put("dwalin", "30_9");
					put("fili", "30_10");
					put("gloin", "30_11");
					put("kili", "30_12");
					put("bard", "30_22");        // Bard, The Bowman: [esgaroth] ally, a character who is no companion; a regroup ability, not used
					// Minions of two cultures and two races, cheated in during the regroup phase, where minions
					// that lived through the skirmishes stand.
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc; an Assignment ability, not used
					put("troll", "32_31");       // Demolition Troll: [moria] Troll; a Skirmish ability, not used
				}},
				new HashMap<>()
				{{
					put("site1", "30_49");
					put("site2", "30_50");
					put("site3", "30_51");
					put("site4", "30_52");
					put("site5", "30_53");
					put("site6", "30_54");
					put("site7", "30_64");
					put("site8", "30_56");
					put("site9", "30_57");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DaleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Dale
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: none
		 * Game Text: <b>Mountain</b>. <b>Regroup:</b> Spot your minion and 6 companions to make the fellowship move
		 * again this turn (if the move limit allows).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Dale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void WithYourMinionAndSixCompanionsTheFellowshipIsMadeToMoveAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable("balin", "dwalin", "fili", "gloin", "kili");
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(aggressor);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);

		var dale = scn.GetCurrentSite();
		assertEquals("30_64", dale.getBlueprintId());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		// The Free Peoples player is asked first, with six companions and a minion in play that is not his.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dale));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dale));
		scn.ShadowUseCardAction(dale);
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();

		// No "another move" question: the fellowship moves on to site 8.
		assertFalse(scn.FreepsDecisionAvailable("another move"));
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetMoveCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FiveCompanionsAndAnAllyAreTooFewAndTheMoveIsTheFreePeoplesPlayersToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bard = scn.GetFreepsCard("bard");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable("balin", "dwalin", "fili", "gloin");
		scn.MoveAlliesToTable(bard);
		assertInZone(Zone.SUPPORT, bard);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// A Troll this time: any minion of the player's meets "your minion".
		scn.MoveMinionsToTable(troll);
		assertInZone(Zone.SHADOW_CHARACTERS, troll);
		scn.FreepsPass();

		var dale = scn.GetCurrentSite();
		assertEquals("30_64", dale.getBlueprintId());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dale));

		scn.ShadowPass();
		scn.ShadowDeclineReconciliation();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("another move"));
	}

	@Test
	public void SixCompanionsWithNoMinionAreNotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable("balin", "dwalin", "fili", "gloin", "kili");
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		var dale = scn.GetCurrentSite();
		assertEquals("30_64", dale.getBlueprintId());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dale));
	}

	@Test
	public void NotOfferedOnceTheMoveLimitHasBeenReached() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCompanionsToTable("balin", "dwalin", "fili", "gloin", "kili");
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.FreepsPass();
		// To site 7 by the regroup move from site 6: two moves of two.  Esgaroth's play of an ally at the start
		// of that regroup phase is declined.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsDeclineOptionalTrigger();
		scn.PassCurrentPhaseActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		scn.FreepsPass();

		var dale = scn.GetCurrentSite();
		assertEquals("30_64", dale.getBlueprintId());
		assertEquals(2, scn.GetMoveCount());
		assertEquals(2, scn.GetMoveLimit());
		// Nothing for it to do: the fellowship cannot move again this turn.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dale));
	}

	@Test
	public void TheFellowshipIsMadeToMoveAgainOnceAndIsAskedAsUsualInALaterRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balin = scn.GetFreepsCard("balin");
		var aggressor = scn.GetShadowCard("aggressor");
		// A move limit of 3, so that after the move the site demands there is a third to be asked about: 2 by
		// rule + 1.
		scn.ApplyAdHocModifier(new MoveLimitModifier(null, 1));

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable("balin", "dwalin", "fili", "gloin", "kili");
		assertInZone(Zone.FREE_CHARACTERS, balin);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(aggressor);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		scn.FreepsPass();

		var dale = scn.GetCurrentSite();
		assertEquals("30_64", dale.getBlueprintId());
		assertEquals(1, scn.GetMoveCount());
		assertEquals(3, scn.GetMoveLimit());

		scn.ShadowUseCardAction(dale);
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
