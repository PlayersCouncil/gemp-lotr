package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_020_Tests
{

	// The Hobbit table: the Hobbit path (site 3 Rivendell, site 4 Goblin Town, underground, site 5 Mirkwood, a
	// forest), Bilbo, Reliable Companion 30_46 as the Ring-bearer (strength 3, vitality 4; not overwhelmed by
	// Gollum unless his strength is tripled) and no card in the Ring slot.  A doubt is the HDG's burden.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "31_20");
					put("thorin", "30_19");      // Thorin, Oakenshield: [dwarven] companion, strength 7, vitality 4; a fellowship ability, not used
					put("balin", "30_2");        // Balin, Brother of Dwalin: [dwarven] companion, strength 7, vitality 3; a fellowship ability, not used
					// Orkish Aggressor: [gundabad] Orc, site 3, strength 6.  "Assignment: Assign this minion to
					// skirmish Bilbo. The Free Peoples player may add a doubt to discard this minion."
					put("aggressor", "30_39");
					// Yazneg, Orkish Assassin: [gundabad] Orc, strength 5, vitality 2, which beats Bilbo without
					// overwhelming him; a maneuver ability, not used.
					put("yazneg", "30_41");
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Gollum, Small Slimy Creature
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: (none: Gollum has no race)
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Each time Gollum wins a skirmish, you may add a doubt.<br><b>Assignment:</b> If Gollum is not
		 * roaming, exert Gollum and assign him to Bilbo to make Gollum strength +1 (or +3 at an underground site)
		 * until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Small Slimy Creature", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachTimeGollumWinsASkirmishTheShadowPlayerMayAddADoubt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// 5 against 3.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.FreepsGetBurdens());

		// A second move, and a second skirmish of his against Bilbo the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(bilbo));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(gollum));
		// Declined: no doubt.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void NotOfferedWhenGollumLosesOrAnotherMinionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var gollum = scn.GetShadowCard("gollum");
		var yazneg = scn.GetShadowCard("yazneg");

		scn.MoveCompanionsToTable(thorin);
		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum, yazneg);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum, yazneg);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{thorin, gollum}, new PhysicalCardImpl[]{bilbo, yazneg});
		assertEquals(0, scn.FreepsGetBurdens());

		// Gollum (5) loses to Thorin (7, damage +1) and lives, vitality 4 with 2 wounds.
		assertEquals(0, scn.GetWoundsOn(gollum));
		scn.FreepsResolveSkirmish(thorin);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gollum));

		// Yazneg (5) beats Bilbo (3).
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(bilbo);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(gollum));
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void AssignmentActionExertsGollumToAssignHimToBilboAndMakeHimStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(balin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Mirkwood, a forest: no underground site.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);

		assertEquals(1, scn.GetWoundsOn(gollum));
		// Bilbo, and not the other companion there to be assigned.
		assertTrue(scn.IsCharAssignedAgainst(bilbo, gollum));
		assertFalse(scn.IsCharAssigned(balin));
		assertEquals(6, scn.GetStrength(gollum));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtAnUndergroundSiteGollumIsStrengthPlusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Goblin Town, underground, and Gollum's own site number.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.UNDERGROUND));
		assertFalse(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);

		assertTrue(scn.IsCharAssignedAgainst(bilbo, gollum));
		assertEquals(8, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileGollumIsRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.SkipToSite(2);
		// The Troll Hoard's play of a hand weapon at the start of the fellowship phase, declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();
		// Gollum played at Rivendell, site 3, below his site number 4: he roams there.
		scn.SetShadowHand(gollum);
		scn.MoveMinionsToTable(aggressor);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInHand(gollum);
		assertTrue(scn.ShadowPlayAvailable(gollum));
		scn.ShadowPlayCard(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// The Aggressor (site 3) does not roam, and could pay an exertion.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(aggressor, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		assertTrue(scn.ShadowActionAvailable(aggressor));
	}

	@Test
	public void NotOfferedWhileGollumIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gollum, 3);
		assertEquals(3, scn.GetWoundsOn(gollum));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(gollum, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
	}

	@Test
	public void NotOfferedOnceBilboIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var aggressor = scn.GetShadowCard("aggressor");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(balin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum, aggressor);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.ShadowActionAvailable(gollum));
		// The Aggressor's own Assignment ability takes Bilbo first; the doubt to discard it is declined.
		scn.ShadowUseCardAction(aggressor);
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(bilbo, aggressor));
		assertFalse(scn.IsCharAssigned(balin));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
	}

	@Test
	public void TheStrengthBonusLastsUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(balin);
		scn.FreepsPass();
		scn.MoveMinionsToTable(gollum, troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowUseCardAction(gollum);
		assertEquals(6, scn.GetStrength(gollum));
		scn.PassCurrentPhaseActions();
		// Balin (7) against the Troop (13): wounded, not overwhelmed.
		scn.FreepsAssignToMinions(balin, troop);

		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(balin));

		// In Gollum's skirmish, after another has resolved: still +1.
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(6, scn.GetStrength(gollum));
		scn.PassSkirmishActions();
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(5, scn.GetStrength(gollum));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
