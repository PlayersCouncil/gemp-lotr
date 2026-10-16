package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_238_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wreathed", "1_238");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, cost 4; his text reaches only a
					// character who bears a [wraith] condition
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] minion who is an Orc and no Nazgûl; strength +4 and fierce only at 2
					// wounds on the Ring-bearer
					put("hound", "7_192");

					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his archery ability is used only on purpose
					// Garrison of Gondor: a second archer companion; defender +1 only with a fortification
					put("garrison", "8_36");
					put("marksman", "1_176");   // Goblin Marksman: an archer minion and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WreathedinShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Wreathed in Shadow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Nazgûl. Plays to your support area.
		 * 		<b>Archery:</b> Remove (1) to make the fellowship archery total -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wreathed");

		assertEquals("Wreathed in Shadow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedBesideAWraithMinionWhoIsNoNazgulUntilANazgulIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wreathed = scn.GetShadowCard("wreathed");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(wreathed, lemenya);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(hound);
		assertInHand(wreathed);
		scn.FreepsPass();

		assertInHand(wreathed);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wreathed));

		scn.ShadowPlayCard(lemenya);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);

		assertTrue(scn.ShadowPlayAvailable(wreathed));
		scn.ShadowPlayCard(wreathed);
		assertInZone(Zone.SUPPORT, wreathed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedWhileARaiderNazgulIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wreathed = scn.GetShadowCard("wreathed");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToHand(wreathed);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul);
		assertInHand(wreathed);
		scn.FreepsPass();

		assertInHand(wreathed);
		assertTrue(scn.ShadowPlayAvailable(wreathed));
		scn.ShadowPlayCard(wreathed);
		assertInZone(Zone.SUPPORT, wreathed);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ArcheryActionRemovesOneToMakeTheFellowshipArcheryTotalMinusOneForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wreathed = scn.GetShadowCard("wreathed");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, garrison);

		scn.StartGame();
		scn.MoveCardsToSupportArea(wreathed);
		scn.MoveMinionsToTable(marksman, troop);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		// Two archer companions; the Marksman is the one archer minion.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(wreathed));

		scn.ShadowUseCardAction(wreathed);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Still in force as the fellowship's archery wounds are placed: one, not two.
		scn.PassArcheryActions();
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(marksman));
		scn.FreepsChooseCard(garrison);
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(marksman));
		assertEquals(1, scn.GetWoundsOn(garrison));

		// Gone with the phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void ArcheryActionIsNotOfferedWithAnEmptyTwilightPool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wreathed = scn.GetShadowCard("wreathed");
		var legolas = scn.GetFreepsCard("legolas");
		var garrison = scn.GetFreepsCard("garrison");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas, garrison);

		scn.StartGame();
		scn.MoveCardsToSupportArea(wreathed);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(0);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wreathed));
	}
}
