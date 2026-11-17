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

public class Card_04_217_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lancers", "4_217");
					put("explorer", "4_250");   // Southron Explorer: [raider] Southron Man, cost 2; a regroup ability used only on purpose
					put("axeman", "4_224");     // Easterling Axeman: [raider] Easterling Man, no Southron; +3 and fierce only at 2 burdens
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession; abilities used only on purpose
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] artifact; Maneuver: remove (5) to become a fierce Southron minion, no Man, strength 5, vitality 5

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesertLancersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Desert Lancers
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. To play, spot a [raider] Man.<br><b>Regroup:</b> Exert Desert Lancers twice to take control of a site.
		*/

		var scn = GetScenario();

		var lancers = scn.GetFreepsCard("lancers");

		assertEquals("Desert Lancers", lancers.getBlueprint().getTitle());
		assertNull(lancers.getBlueprint().getSubtitle());
		assertTrue(lancers.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, lancers.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, lancers.getBlueprint().getCulture());
		assertEquals(CardType.MINION, lancers.getBlueprint().getCardType());
		assertEquals(Race.MAN, lancers.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(lancers, Keyword.SOUTHRON));
		assertEquals(5, lancers.getBlueprint().getTwilightCost());
		assertEquals(11, lancers.getBlueprint().getStrength());
		assertEquals(3, lancers.getBlueprint().getVitality());
		assertEquals(4, lancers.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lancers = scn.GetShadowCard("lancers");
		var explorer = scn.GetShadowCard("explorer");
		var brigand = scn.GetShadowCard("brigand");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(lancers, explorer);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		assertInHand(lancers);
		assertInZone(Zone.SUPPORT, ships);
		// A Man of another culture, and a [raider] card that is no Man.
		scn.MoveMinionsToTable(brigand);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(lancers);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(lancers));

		scn.ShadowPlayCard(explorer);
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertTrue(scn.ShadowPlayAvailable(lancers));
		scn.ShadowPlayCard(lancers);

		assertInZone(Zone.SHADOW_CHARACTERS, lancers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderManWhoIsNoSouthronIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lancers = scn.GetShadowCard("lancers");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(lancers);

		scn.StartGame();
		assertInHand(lancers);
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertInHand(lancers);
		assertTrue(scn.ShadowPlayAvailable(lancers));
		scn.ShadowPlayCard(lancers);

		assertInZone(Zone.SHADOW_CHARACTERS, lancers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ARaiderMinionWhoIsNoManIsNotSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lancers = scn.GetShadowCard("lancers");
		var explorer = scn.GetShadowCard("explorer");
		var brigand = scn.GetShadowCard("brigand");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToHand(lancers, explorer);
		scn.MoveCardsToSupportArea(rearguard);

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		assertInHand(lancers);
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
		assertInHand(lancers);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(lancers));

		// A [raider] Man played beside it, and the Lancers can be played.
		scn.ShadowPlayCard(explorer);
		assertTrue(scn.ShadowPlayAvailable(lancers));
		scn.ShadowPlayCard(lancers);
		assertInZone(Zone.SHADOW_CHARACTERS, lancers);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsItTwiceToTakeControlOfTheLowestSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lancers = scn.GetShadowCard("lancers");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(lancers);
		// The fellowship moves on to site 4; sites 1 and 2 are behind every marker.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lancers);
		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn (8) loses to the Lancers (11) and lives.
		scn.PassSkirmishActions();
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(0, scn.GetWoundsOn(lancers));
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(lancers));

		scn.ShadowUseCardAction(lancers);

		assertEquals(2, scn.GetWoundsOn(lancers));
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneWoundItCannotExertTwiceAndTheAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lancers = scn.GetShadowCard("lancers");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(lancers);
		scn.AddWoundsToChar(lancers, 1);
		assertEquals(1, scn.GetWoundsOn(lancers));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lancers);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsPass();

		// Vitality 3 with 1 wound: 2 left, and exerting twice needs 3. A site can still be taken.
		assertEquals(1, scn.GetWoundsOn(lancers));
		assertFalse(scn.IsSiteControlled(scn.GetSite(1)));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(lancers));
	}

	@Test
	public void WithNoSiteThatCanBeTakenTheAbilityIsStillOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lancers = scn.GetShadowCard("lancers");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(lancers);
		// Turn 1: the Shadow player's marker is still on site 1, so no site is behind every marker.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lancers);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals(0, scn.GetWoundsOn(lancers));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(lancers));

		scn.ShadowUseCardAction(lancers);

		assertEquals(2, scn.GetWoundsOn(lancers));
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(scn.GetSite(2)));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
