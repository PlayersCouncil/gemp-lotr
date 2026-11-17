package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_231_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("emynmuil", "4_231");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit, not the Ring-bearer, vitality 4; abilities used only on purpose
					put("damrod", "4_114");     // Damrod, Ranger of Ithilien: Ring-bound [gondor] Man, vitality 3; a skirmish ability used only on purpose
					put("boromir", "0_65");     // Boromir, Steward's Heir: Ring-bound [gondor] Man, vitality 3, keywords only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound Man, vitality 4; a maneuver ability used only on purpose
					// Easterling Axeman: [raider] Man, vitality 2; strength +3 and fierce only at 2 burdens (none here)
					put("axeman", "4_224");
					// Southron Explorer: [raider] Man, vitality 3; a regroup ability used only on purpose
					put("explorer", "4_250");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, no Man; his bonuses want 3 burdens (none here)
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion, vitality 1, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("emynmuil", "4_231");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit, not the Ring-bearer, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasternEmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Eastern Emyn Muil
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each Ring-bound companion moving from site 2[T] must exert twice.<br><b>Maneuver:</b> Discard this condition and exert a [raider] Man to exert a Ring-bound companion.
		*/

		var scn = GetScenario();

		var emynmuil = scn.GetFreepsCard("emynmuil");

		assertEquals("Eastern Emyn Muil", emynmuil.getBlueprint().getTitle());
		assertNull(emynmuil.getBlueprint().getSubtitle());
		assertTrue(emynmuil.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, emynmuil.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, emynmuil.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, emynmuil.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(emynmuil, Keyword.SUPPORT_AREA));
		assertEquals(0, emynmuil.getBlueprint().getTwilightCost());
	}

	@Test
	public void MovingFromSite2TEachRingBoundCompanionExertsTwiceAndTheMoveFromSite1TExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emynmuil = scn.GetShadowCard("emynmuil");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var damrod = scn.GetFreepsCard("damrod");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, damrod, boromir, aragorn);
		scn.MoveCardsToSupportArea(emynmuil);
		// Sam has 2 vitality left, so he can exert only once; Boromir (vitality 3) is exhausted.
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, emynmuil);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The move from site 1T to site 2T.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// No minions, so the turn goes on to the regroup phase; the fellowship moves on from site 2T.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		// The Ring-bearer and a Ring-bound Man exert twice.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(damrod));
		// Sam exerts as much as he can (CR 4.2 "effect"), once, and is not killed.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertInZone(Zone.FREE_CHARACTERS, sam);
		// An exhausted Ring-bound companion cannot exert (CR 4.2 "exhausted"): no wound, not killed.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		// An unbound companion does not exert.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, emynmuil);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingFromSite2OfTheFellowshipBlockExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var emynmuil = scn.GetShadowCard("emynmuil");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(emynmuil);
		assertInZone(Zone.SUPPORT, emynmuil);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		// The move from Ettenmoors, site 2 of the Fellowship block, to site 3.
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverActionDiscardsItAndExertsARaiderManToExertARingBoundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emynmuil = scn.GetShadowCard("emynmuil");
		var axeman = scn.GetShadowCard("axeman");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var damrod = scn.GetFreepsCard("damrod");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, damrod, boromir, aragorn);
		scn.MoveCardsToSupportArea(emynmuil);
		// Boromir, a Ring-bound Man of vitality 3, is exhausted.
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, explorer, khamul, brigand);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInZone(Zone.SUPPORT, emynmuil);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(emynmuil));
		scn.ShadowUseCardAction(emynmuil);

		// The cost: a [raider] Man of either keyword, and neither a [raider] Nazgul nor a [dunland] Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand));
		scn.ShadowChooseCard(explorer);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInDiscard(emynmuil);

		// The effect: a Ring-bound companion who can exert, the Ring-bearer, Sam or a Ring-bound Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam, damrod));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, boromir));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(damrod));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithEveryRingBoundCompanionExhaustedTheCostsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emynmuil = scn.GetShadowCard("emynmuil");
		var axeman = scn.GetShadowCard("axeman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(emynmuil);
		// Frodo, vitality 4, exhausted: the one Ring-bound companion.
		scn.AddWoundsToChar(frodo, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SUPPORT, emynmuil);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(emynmuil));
		scn.ShadowUseCardAction(emynmuil);

		// The Axeman, the one [raider] Man, exerts unasked; nobody can be exerted.
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInDiscard(emynmuil);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithTheOnlyRaiderManExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var emynmuil = scn.GetShadowCard("emynmuil");
		var axeman = scn.GetShadowCard("axeman");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(emynmuil);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman, khamul, brigand);
		// The Axeman, vitality 2, exhausted; a [raider] Nazgul and a [dunland] Man could exert.
		scn.AddWoundsToChar(axeman, 1);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertInZone(Zone.SUPPORT, emynmuil);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(emynmuil));
		assertInZone(Zone.SUPPORT, emynmuil);
	}
}
