package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_340_Tests
{

	// The Towers path, Streets of Edoras its site 3.  SkipToSite(2) and a skip on reach it in the second turn.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deor", "7_222");       // Deor: unique [rohan] Man ally, home 3T, villager; a skirmish ability that needs initiative
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, not unique, home 3T; a regroup ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home 3T, vitality 1; a maneuver ability that needs initiative
					put("celeborn", "1_34");    // Celeborn: Elf ally, home 6F (at home nowhere on this path), vitality 3; a fellowship ability only
					put("aragorn", "17_93");    // Aragorn, Defender of Rohan: a [rohan] companion; hunter for [rohan] companions while the Ring-bearer is assigned
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a companion to bear the Horn; text only about overwhelming
					put("horn", "3_42");        // Horn of Boromir: Maneuver: exert Boromir and spot an ally; until the regroup phase that ally is strength +3 and participates
					put("comedown", "4_146");   // Come Down: Archery: spot 2 [isengard] archers to make allies take wounds from archery fire instead of companions
					put("assassin", "1_131");   // Orthanc Assassin: [isengard] archer, damage +1 and nothing else
					put("crossbow", "4_182");   // Uruk Crossbow Troop: [isengard] archer; its when-played token does nothing when cheated in
					put("marksman", "1_176");   // Goblin Marksman: archer, and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				new HashMap<>() {{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_340");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same cards on the usual Towers path, whose site 3T is Barrows of Edoras 4_337: a sanctuary that bars nobody.
	protected VirtualTableScenario GetBarrowsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deor", "7_222");
					put("herdsman", "7_247");
					put("merchant", "7_97");
					put("aragorn", "17_93");
					put("marksman", "1_176");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StreetsofEdorasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Streets of Edoras
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Sanctuary</b>. Each [rohan] ally may not participate in archery fire or skirmishes at Streets of Edoras.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Streets of Edoras", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
	}

	@Test
	public void AtStreetsOfEdorasRohanAlliesAtHomeAreOfferedNeitherAnArcheryWoundNorAnAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var deor = scn.GetFreepsCard("deor");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merchant = scn.GetFreepsCard("merchant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(deor, herdsman, merchant);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassArcheryActions();

		// All three allies are at home.  The [gondor] one takes part, and so does the [rohan] companion; the two
		// [rohan] allies do not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deor, herdsman));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(0, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(merchant));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(deor));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(herdsman));
		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(aragorn));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(merchant));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(deor));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(herdsman));
	}

	@Test
	public void AtBarrowsOfEdorasTheSameRohanAlliesAtHomeAreOfferedArcheryWoundsAndAssignment() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBarrowsScenario();

		var frodo = scn.GetRingBearer();
		var deor = scn.GetFreepsCard("deor");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merchant = scn.GetFreepsCard("merchant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(deor, herdsman, merchant);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals("Barrows of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(deor));
		scn.PassArcheryActions();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, merchant, deor, herdsman));
		scn.FreepsChooseCard(deor);
		assertEquals(1, scn.GetWoundsOn(deor));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(deor));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(herdsman));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(merchant));
		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(deor));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(herdsman));
	}

	@Test
	public void WithComeDownInForceARohanAllyAtStreetsOfEdorasTakesNoArcheryWoundAndTheOtherAlliesDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var deor = scn.GetFreepsCard("deor");
		var merchant = scn.GetFreepsCard("merchant");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var comedown = scn.GetShadowCard("comedown");
		var assassin = scn.GetShadowCard("assassin");
		var crossbow = scn.GetShadowCard("crossbow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(deor, merchant, celeborn);
		scn.MoveCardsToSupportArea(comedown);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(assassin, crossbow);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(comedown));
		scn.ShadowUseCardAction(comedown);
		scn.PassArcheryActions();

		// Two archers, two wounds.  Come Down sends them to allies, at home or away; the [rohan] ally is passed over
		// as the companions are (Q-0147).
		assertTrue(scn.FreepsHasCardChoiceAvailable(merchant, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deor, frodo, aragorn));
		scn.FreepsChooseCard(celeborn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merchant, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deor, frodo, aragorn));
		scn.FreepsChooseCard(celeborn);

		assertEquals(2, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void HornOfBoromirMakesARohanAllyStrongerAtStreetsOfEdorasButHeTakesNoPart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var deor = scn.GetFreepsCard("deor");
		var boromir = scn.GetFreepsCard("boromir");
		var horn = scn.GetFreepsCard("horn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, horn);
		scn.MoveAlliesToTable(deor);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Streets of Edoras", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(4, scn.GetStrength(deor));

		// Deor is the one ally, and is taken without asking.
		scn.FreepsUseCardAction(horn);
		assertEquals(1, scn.GetWoundsOn(boromir));
		// The rest of the Horn still reaches him (Q-0150): strength 4 + 3.
		assertEquals(7, scn.GetStrength(deor));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(7, scn.GetStrength(deor));
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(boromir));
		assertFalse(scn.FreepsHasAssignmentTargetAvailable(deor));
		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(boromir));
		assertFalse(scn.ShadowHasAssignmentTargetAvailable(deor));
	}

	@Test
	public void OnceTheFellowshipHasLeftStreetsOfEdorasHornOfBoromirHasARohanAllyTakePart() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var deor = scn.GetFreepsCard("deor");
		var boromir = scn.GetFreepsCard("boromir");
		var horn = scn.GetFreepsCard("horn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, horn);
		scn.MoveAlliesToTable(deor);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Streets of Edoras is behind the fellowship now, and Deor away from his home.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsUseCardAction(horn);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(7, scn.GetStrength(deor));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(frodo));
		assertTrue(scn.FreepsHasAssignmentTargetAvailable(deor));
		scn.FreepsDeclineAssignments();

		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));
		assertTrue(scn.ShadowHasAssignmentTargetAvailable(deor));
	}
}
