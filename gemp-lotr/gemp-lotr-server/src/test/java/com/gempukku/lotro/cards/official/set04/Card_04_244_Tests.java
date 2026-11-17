package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_244_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("regiment", "4_244");
					put("explorer", "4_250");   // Southron Explorer: [raider] Southron Man, cost 2; a regroup ability used only on purpose
					put("axeman", "4_224");     // Easterling Axeman: [raider] Easterling Man, no Southron; +3 and fierce only at 2 burdens
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession; abilities used only on purpose
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact; Maneuver: remove (5) to become a fierce Southron minion, no Man, strength 5, vitality 5

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; not overwhelmed unless his strength is tripled
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RegimentofHaradrimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Regiment of Haradrim
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 16
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. To play, spot a [raider] Man.<br><b>Regroup:</b> Exert Regiment of Haradrim twice to take control of a site.
		*/

		var scn = GetScenario();

		var regiment = scn.GetFreepsCard("regiment");

		assertEquals("Regiment of Haradrim", regiment.getBlueprint().getTitle());
		assertNull(regiment.getBlueprint().getSubtitle());
		assertTrue(regiment.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, regiment.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, regiment.getBlueprint().getCulture());
		assertEquals(CardType.MINION, regiment.getBlueprint().getCardType());
		assertEquals(Race.MAN, regiment.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(regiment, Keyword.SOUTHRON));
		assertEquals(8, regiment.getBlueprint().getTwilightCost());
		assertEquals(16, regiment.getBlueprint().getStrength());
		assertEquals(4, regiment.getBlueprint().getVitality());
		assertEquals(4, regiment.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var explorer = scn.GetShadowCard("explorer");
		var brigand = scn.GetShadowCard("brigand");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(regiment, explorer);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		assertInHand(regiment);
		assertInZone(Zone.SUPPORT, ships);
		// A Man of another culture, and a [raider] card that is no Man.
		scn.MoveMinionsToTable(brigand);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(regiment);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(regiment));

		scn.ShadowPlayCard(explorer);
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertTrue(scn.ShadowPlayAvailable(regiment));
		scn.ShadowPlayCard(regiment);

		assertInZone(Zone.SHADOW_CHARACTERS, regiment);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderManWhoIsNoSouthronIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(regiment);

		scn.StartGame();
		assertInHand(regiment);
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(regiment);
		assertTrue(scn.ShadowPlayAvailable(regiment));
		scn.ShadowPlayCard(regiment);

		assertInZone(Zone.SHADOW_CHARACTERS, regiment);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderMinionWhoIsNoManIsNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var explorer = scn.GetShadowCard("explorer");
		var brigand = scn.GetShadowCard("brigand");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToHand(regiment, explorer);
		scn.MoveCardsToSupportArea(rearguard);

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		assertInHand(regiment);
		assertInZone(Zone.SUPPORT, rearguard);
		// The Brigand, a [dunland] Man, is there so that the game has a maneuver phase.
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		// A [raider] minion now, and no Man, until the end of the turn.
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		// Aragorn (8) loses to the Brigand (8) and lives; Boromir (7) beats the Rearguard (5) in both rounds.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, brigand}, new PhysicalCardImpl[]{boromir, rearguard});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, rearguard);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(rearguard));

		// The fellowship moves again, to a second Shadow phase in the same turn.
		scn.SkipToMovementDecision();
		scn.SetTwilight(20);
		scn.FreepsChooseToMove();

		assertInZone(Zone.SHADOW_CHARACTERS, rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertInHand(regiment);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(regiment));

		// A [raider] Man played beside it, and the Regiment can be played.
		scn.ShadowPlayCard(explorer);
		assertTrue(scn.ShadowPlayAvailable(regiment));
		scn.ShadowPlayCard(regiment);
		assertInZone(Zone.SHADOW_CHARACTERS, regiment);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsItTwiceToTakeControlOfTheLowestSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(regiment);
		// The fellowship moves on to site 4; sites 1 and 2 are behind every marker.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, regiment);
		scn.FreepsResolveSkirmish(boromir);
		// Boromir (7) loses to the Regiment (16) and lives: 16 is no triple of 7.
		scn.PassSkirmishActions();
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(0, scn.GetWoundsOn(regiment));
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(regiment));

		scn.ShadowUseCardAction(regiment);

		assertEquals(2, scn.GetWoundsOn(regiment));
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoWoundsItCannotExertTwiceAndTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(regiment);
		scn.AddWoundsToChar(regiment, 2);
		assertEquals(2, scn.GetWoundsOn(regiment));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, regiment);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsPass();

		// Vitality 4 with 2 wounds: 2 left, and exerting twice needs 3. A site can still be taken.
		assertEquals(2, scn.GetWoundsOn(regiment));
		assertFalse(scn.IsSiteControlled(scn.GetSite(1)));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(regiment));
	}

	@Test
	public void WithNoSiteThatCanBeTakenTheAbilityIsStillOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var regiment = scn.GetShadowCard("regiment");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(regiment);
		// Turn 1: the Shadow player's marker is still on site 1, so no site is behind every marker.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, regiment);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals(0, scn.GetWoundsOn(regiment));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(regiment));

		scn.ShadowUseCardAction(regiment);

		assertEquals(2, scn.GetWoundsOn(regiment));
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(scn.GetSite(2)));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
