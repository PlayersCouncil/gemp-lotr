package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_04_243_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fire", "4_243");
					put("fire2", "4_243");
					put("elite", "4_232");      // Elite Archer: [raider] Southron Man, archer, vitality 2; an archery ability used only on purpose
					put("elite2", "4_232");
					put("axeman", "4_224");     // Easterling Axeman: [raider] Easterling Man who is no archer, vitality 2; +3 and fierce only at 2 burdens
					put("bow", "4_247");        // Southron Bow: bearer ([raider] Man) is an archer; minion archery total +1 while another [raider] Man is spotted
					put("soldier", "4_220");    // Desert Soldier: [raider] Southron Man who is no archer, vitality 2; a start-of-skirmish trigger only
					put("goblinarcher", "1_172"); // Goblin Archer: [moria] Orc archer, vitality 3; its -6 needs another [moria] Orc

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RapidFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Rapid Fire
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Exert a [raider] archer to make the minion archery total +1 for each burden (limit +5).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fire");

		assertEquals("Rapid Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(6, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ArcheryEventExertsARaiderArcherToMakeTheMinionArcheryTotalPlusOneForEachBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var elite = scn.GetShadowCard("elite");
		var axeman = scn.GetShadowCard("axeman");
		var bow = scn.GetShadowCard("bow");
		var soldier = scn.GetShadowCard("soldier");
		var goblinarcher = scn.GetShadowCard("goblinarcher");
		scn.MoveCardsToHand(fire);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(elite, axeman, soldier, goblinarcher);
		scn.AttachCardsTo(axeman, bow);
		scn.AddBurdens(2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(6);

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.HasKeyword(axeman, Keyword.ARCHER));
		assertEquals(0, scn.GetWoundsOn(elite));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(goblinarcher));
		// Three archers, and 1 more from the Bow.
		assertEquals(4, scn.GetShadowArcheryTotal());
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fire));
		scn.ShadowPlayCard(fire);
		// A printed [raider] archer and a [raider] Man made an archer by his bow are offered; a [raider] Man who is no
		// archer and an archer of another culture are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elite, axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, goblinarcher));
		scn.ShadowChooseCard(elite);

		assertEquals(1, scn.GetWoundsOn(elite));
		assertEquals(0, scn.GetWoundsOn(axeman));
		// 4 + 1 for each of 2 burdens.
		assertEquals(6, scn.GetShadowArcheryTotal());
		assertInZone(Zone.DISCARD, fire);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithThreeBurdensTheTotalIsPlusThreeUntilTheArcheryPhaseIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var elite = scn.GetShadowCard("elite");
		scn.MoveCardsToHand(fire);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(elite);
		scn.AddBurdens(3);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(6);

		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetShadowArcheryTotal());
		scn.FreepsPass();

		scn.ShadowPlayCard(fire);
		// The one [raider] archer exerts unasked.
		assertEquals(1, scn.GetWoundsOn(elite));
		// 1 + 3 burdens.
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Four archery wounds, placed by the Free Peoples player.
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(gimli);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Once the archery phase is over, the total is the Elite Archer's 1 again.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithSixBurdensEachCopyAddsNoMoreThanFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var fire2 = scn.GetShadowCard("fire2");
		var elite = scn.GetShadowCard("elite");
		var elite2 = scn.GetShadowCard("elite2");
		scn.MoveCardsToHand(fire, fire2);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(elite, elite2);
		scn.AddBurdens(6);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(12);

		assertEquals(6, scn.GetBurdens());
		assertEquals(2, scn.GetShadowArcheryTotal());
		scn.FreepsPass();

		scn.ShadowPlayCard(fire);
		scn.ShadowChooseCard(elite);
		// 2 + 5, not 2 + 6.
		assertEquals(7, scn.GetShadowArcheryTotal());

		// The limit is each card's own: a second copy in the same phase adds its own 5.
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(fire2));
		scn.ShadowPlayCard(fire2);
		assertEquals(1, scn.GetWoundsOn(elite2));
		assertEquals(12, scn.GetShadowArcheryTotal());
		assertInZone(Zone.DISCARD, fire, fire2);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotPlayableWhileTheOnlyRaiderArcherIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fire = scn.GetShadowCard("fire");
		var elite = scn.GetShadowCard("elite");
		var goblinarcher = scn.GetShadowCard("goblinarcher");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(fire);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(elite, goblinarcher, axeman);
		scn.AddBurdens(2);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(6);
		// Vitality 2: one wound exhausts the Elite Archer.  An archer of another culture and a [raider] Man who is
		// no archer could exert.
		scn.AddWoundsToChar(elite, 1);

		assertEquals(1, scn.GetWoundsOn(elite));
		assertEquals(0, scn.GetWoundsOn(goblinarcher));
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fire));
	}
}
