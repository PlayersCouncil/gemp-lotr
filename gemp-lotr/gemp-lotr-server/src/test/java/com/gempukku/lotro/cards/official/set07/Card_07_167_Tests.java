package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_167_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("marksmen", "7_167");
					put("marksmen2", "7_167");
					put("explorer", "4_250");   // Southron Explorer: a [raider] Southron Man of cost 2; a regroup ability used only on purpose
					// Easterling Axeman: a [raider] Easterling Man; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul and no Man; his text needs 3 or 5 burdens
					put("khamul", "103_61");
					put("brigand", "4_10");     // Dunlending Brigand: a Man who is not [raider]; no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion of vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronMarksmenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Southron Marksmen
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Southron</b>. <b>Archer</b>. <b>Fierce</b>. To play, spot a [raider] Man.
		 * <b>Archery:</b> Exert this minion and remove a threat to make the minion archery total +1.
		*/

		var scn = GetScenario();

		var marksmen = scn.GetFreepsCard("marksmen");

		assertEquals("Southron Marksmen", marksmen.getBlueprint().getTitle());
		assertNull(marksmen.getBlueprint().getSubtitle());
		assertFalse(marksmen.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, marksmen.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, marksmen.getBlueprint().getCulture());
		assertEquals(CardType.MINION, marksmen.getBlueprint().getCardType());
		assertEquals(Race.MAN, marksmen.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(marksmen, Keyword.SOUTHRON));
		assertTrue(scn.HasKeyword(marksmen, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(marksmen, Keyword.FIERCE));
		assertEquals(5, marksmen.getBlueprint().getTwilightCost());
		assertEquals(10, marksmen.getBlueprint().getStrength());
		assertEquals(3, marksmen.getBlueprint().getVitality());
		assertEquals(4, marksmen.getBlueprint().getSiteNumber());
	}

		@Test
	public void CannotBePlayedBesideARaiderNazgulAndAManOfAnotherCultureUntilASouthronManIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marksmen = scn.GetShadowCard("marksmen");
		var explorer = scn.GetShadowCard("explorer");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(marksmen, explorer);
		assertInHand(marksmen, explorer);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, brigand);
		// Enough for both plays with their roaming penalties at site 2: (2 + 2) + (5 + 2).
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, khamul, brigand);
		// A [raider] minion who is no Man, and a Man who is not [raider]: neither is a [raider] Man.
		assertFalse(scn.ShadowPlayAvailable(marksmen));
		assertTrue(scn.ShadowPlayAvailable(explorer));

		scn.ShadowPlayCard(explorer);
		assertInZone(Zone.SHADOW_CHARACTERS, explorer);
		assertTrue(scn.ShadowPlayAvailable(marksmen));

		scn.ShadowPlayCard(marksmen);
		assertInZone(Zone.SHADOW_CHARACTERS, marksmen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWithAnEasterlingRaiderMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var marksmen = scn.GetShadowCard("marksmen");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(marksmen);
		assertInHand(marksmen);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, axeman);
		assertTrue(scn.ShadowPlayAvailable(marksmen));

		scn.ShadowPlayCard(marksmen);
		assertInZone(Zone.SHADOW_CHARACTERS, marksmen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ArcheryAbilityExertsThisCopyAndRemovesAThreatToMakeTheMinionArcheryTotalPlus1ForThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var marksmen = scn.GetShadowCard("marksmen");
		var marksmen2 = scn.GetShadowCard("marksmen2");

		scn.StartGame();
		scn.MoveMinionsToTable(marksmen, marksmen2);
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// Two archers.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(marksmen));
		assertEquals(0, scn.GetWoundsOn(marksmen2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(marksmen));

		scn.ShadowUseCardAction(marksmen);
		assertEquals(1, scn.GetWoundsOn(marksmen));
		assertEquals(0, scn.GetWoundsOn(marksmen2));
		assertEquals(0, scn.GetThreats());
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The archery fire takes the raised total: three wounds to place.
		scn.PassArcheryActions();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		// Gone with the archery phase.
		assertEquals(2, scn.GetShadowArcheryTotal());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWithoutAThreatOrWithThisCopyExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var marksmen = scn.GetShadowCard("marksmen");
		var marksmen2 = scn.GetShadowCard("marksmen2");

		scn.StartGame();
		scn.MoveMinionsToTable(marksmen, marksmen2);
		// Vitality 3 with 2 wounds: this copy cannot exert.
		scn.AddWoundsToChar(marksmen, 2);
		assertEquals(2, scn.GetWoundsOn(marksmen));
		assertEquals(0, scn.GetWoundsOn(marksmen2));
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		// No threat to remove: neither copy is offered.
		assertEquals(0, scn.GetThreats());
		assertFalse(scn.ShadowActionAvailable(marksmen));
		assertFalse(scn.ShadowActionAvailable(marksmen2));

		// A threat: the copy that can exert is offered, the exhausted one is not.
		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(marksmen));
		assertTrue(scn.ShadowActionAvailable(marksmen2));
	}
}
