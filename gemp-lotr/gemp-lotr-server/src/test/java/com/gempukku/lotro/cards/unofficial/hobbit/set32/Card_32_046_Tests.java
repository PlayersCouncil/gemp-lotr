package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_046_Tests
{

	// The Hobbit table, with Dol Guldur as site 5 in place of Mirkwood (which would discard Gandalf as the
	// fellowship moves there): the turn begins at Goblin Town (site 4) and the Shadow phase after the move is at
	// Dol Guldur. No ally laid here has his home at site 5.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gandalf, Friend of Thorin: [gandalf] Wizard companion; a discard bar that needs 2 [dwarven]
					// followers (none is laid), a discount on Thorin and a fellowship ability
					put("gandalf", "30_26");
					// Gandalf, Leader of the Company: "Gandalf cannot be discarded"; a start-of-turn offer, laid after it
					put("gandalf28", "30_28");
					// Ent Avenger: a [gandalf] companion who is no Gandalf; strength +2 while 3 Ents are spotted
					put("avenger", "6_27");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, 7/3; a fellowship ability only
					// Elrond, Elven Lord: [elven] Elf ally, Wise, home 3H; a start-of-turn heal and a regroup ability,
					// neither used
					put("elrond", "30_21");
					// Saruman, The White: [gandalf] Wizard ally, Wise, home 3H; Gandalf is defender +1 (Gandalf is
					// gone by the assignments), a fellowship and a skirmish ability, not used
					put("saruman", "32_24");
					// Dáin Ironfoot: [dwarven] Dwarf ally, not Wise, home 9H; a start-of-regroup offer that wants a
					// [dwarven] follower (none is laid)
					put("dain", "32_2");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, 7/1; no other text
					put("rider", "12_161");     // Black Rider: [wraith] Nazgûl, fierce; strength +1 for each companion
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.HobbitSites);
					put("site5", "32_46");
				}},
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DolGuldurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Dol Guldur
		 * Unique: False
		 * Type: Site
		 * Block: Hobbit
		 * Site Number: 5
		 * Shadow Number: 8
		 * Direction: none
		 * Game Text: <b>Forest</b>. At the start of the Shadow phase, you may discard Gandalf. If you do, Wise allies participate in skirmishes until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Dol Guldur", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.HOBBIT, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertNull(card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void DiscardingGandalfLetsWiseAlliesTakePartInSkirmishesButNotInArcheryFire() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var elrond = scn.GetFreepsCard("elrond");
		var saruman = scn.GetFreepsCard("saruman");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveAlliesToTable(elrond, saruman, dain);
		scn.MoveMinionsToTable(marksman);
		assertInZone(Zone.FREE_CHARACTERS, gandalf, avenger);
		assertInZone(Zone.SUPPORT, elrond, saruman, dain);
		scn.FreepsPass();

		var guldur = scn.GetCurrentSite();
		assertEquals("32_46", guldur.getBlueprintId());
		assertFalse(scn.FreepsCanAssign(elrond));
		assertFalse(scn.FreepsCanAssign(saruman));
		// Offered to the Shadow player of the turn, and not to the Free Peoples player (Q-0417).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guldur));
		scn.ShadowAcceptOptionalTrigger();

		// The one Gandalf is discarded unasked; Ent Avenger, a [gandalf] companion of another title, stays.
		assertInZone(Zone.DISCARD, gandalf);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		// Wise allies of two cultures and races may now be assigned; Dáin, an ally who is not Wise, may not.
		assertTrue(scn.FreepsCanAssign(elrond));
		assertTrue(scn.FreepsCanAssign(saruman));
		assertFalse(scn.FreepsCanAssign(dain));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Skirmishes, not archery fire: the Marksman's wound goes to a companion, never to a Wise ally.
		scn.SkipToArcheryWounds();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbo, avenger));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond, saruman, dain));
		scn.FreepsChooseCard(avenger);
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(elrond));

		scn.SkipToAssignments();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(elrond));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(saruman));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(dain));
		scn.FreepsAssignToMinions(elrond, marksman);
		assertTrue(scn.IsCharAssignedAgainst(elrond, marksman));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void WiseAlliesTakePartThroughTheFierceSkirmishesAndNotInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var elrond = scn.GetFreepsCard("elrond");
		var rider = scn.GetShadowCard("rider");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveAlliesToTable(elrond);
		scn.MoveMinionsToTable(rider, troop);
		assertInZone(Zone.FREE_CHARACTERS, gandalf, balin);
		assertInZone(Zone.SUPPORT, elrond);
		scn.FreepsPass();

		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.DISCARD, gandalf);
		assertTrue(scn.FreepsCanAssign(elrond));

		// Balin takes the Troop; the Black Rider is left for the fierce round.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(balin);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.FreepsCanAssign(elrond));
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(elrond, rider);
		assertTrue(scn.IsCharAssignedAgainst(elrond, rider));
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, rider));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsCanAssign(elrond));
	}

	@Test
	public void DecliningLeavesGandalfAndTheAlliesOutAndTheStartOfTheManeuverPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var elrond = scn.GetFreepsCard("elrond");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(elrond);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInZone(Zone.SUPPORT, elrond);
		scn.FreepsPass();

		var guldur = scn.GetCurrentSite();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(guldur));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertFalse(scn.FreepsCanAssign(elrond));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The start of the maneuver phase, at the same site, with Gandalf still there: nothing.
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertFalse(scn.FreepsCanAssign(elrond));
	}

	@Test
	public void WithNoGandalfItIsNotOfferedAndOnTheNextTurnTheFirstPlayerDiscardsTheOthersGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avenger = scn.GetFreepsCard("avenger");
		var elrond = scn.GetFreepsCard("elrond");
		var hisGandalf = scn.GetShadowCard("gandalf");
		var hisElrond = scn.GetShadowCard("elrond");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(avenger);
		scn.MoveAlliesToTable(elrond);
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertInZone(Zone.SUPPORT, elrond);
		scn.FreepsPass();

		var guldur = scn.GetCurrentSite();
		assertEquals("32_46", guldur.getBlueprintId());
		// Ent Avenger, a [gandalf] companion of another title, does not pay.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, avenger);
		assertFalse(scn.FreepsCanAssign(elrond));

		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship, at site 4, with his Gandalf and his Elrond, moves to the same Dol Guldur;
		// the first player is the Shadow player there.
		scn.MoveCompanionsToTable(hisGandalf);
		scn.MoveAlliesToTable(hisElrond);
		assertInZone(Zone.FREE_CHARACTERS, hisGandalf);
		assertInZone(Zone.SUPPORT, hisElrond);
		scn.ShadowPass();

		assertSame(guldur, scn.GetCurrentSite());
		assertFalse(scn.ShadowCanAssign(hisElrond));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(guldur));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, hisGandalf);
		assertTrue(scn.ShadowCanAssign(hisElrond));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void AGandalfWhoCannotBeDiscardedOpensNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf28 = scn.GetFreepsCard("gandalf28");
		var elrond = scn.GetFreepsCard("elrond");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf28);
		scn.MoveAlliesToTable(elrond);
		assertInZone(Zone.FREE_CHARACTERS, gandalf28);
		assertInZone(Zone.SUPPORT, elrond);
		scn.FreepsPass();

		assertEquals("32_46", scn.GetCurrentSite().getBlueprintId());
		// Gandalf, Leader of the Company "cannot be discarded": no discard, and so nothing for the Wise allies.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.FREE_CHARACTERS, gandalf28);
		assertFalse(scn.FreepsCanAssign(elrond));
	}
}
