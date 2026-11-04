package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_139_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spurred", "11_139");

					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, vitality 2; +2 strength at an underground site, which no reading here takes
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] minion who is no Orc (a Half-troll), vitality 2; its assignment trigger is never reached
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, no minion; its text wants 5 burdens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpurredtoBattleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Spurred to Battle
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, exert an [orc] minion.<br>At the start of the Shadow phase, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spurred");

		assertEquals("Spurred to Battle", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayExertsAnOrcMinionOfTheShadowPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spurred = scn.GetShadowCard("spurred");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(spurred);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, halftroll, troop);
		scn.MoveCardsToSupportArea(beckoning);
		assertInHand(spurred);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(spurred));
		scn.ShadowPlayCard(spurred);

		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, spurred);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyOrcMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spurred = scn.GetShadowCard("spurred");
		var denizen = scn.GetShadowCard("denizen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(spurred);

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, troop);
		scn.AddWoundsToChar(denizen, 1);
		// Denizen, vitality 2, is exhausted; the [moria] Troop could exert, but is no [orc] minion.
		assertEquals(1, scn.GetVitality(denizen));
		assertEquals(3, scn.GetVitality(troop));
		assertInHand(spurred);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(spurred));
		assertInHand(spurred);
	}

	@Test
	public void AtTheStartOfTheShadowPhaseAddsOneAndNotAtTheStartOfOtherPhases() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spurred = scn.GetShadowCard("spurred");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(spurred);

		scn.StartGame();
		// In play from the start of the fellowship phase, which adds nothing.
		assertInZone(Zone.SUPPORT, spurred);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 1 companion + 2 for Ettenmoors + 1
		assertEquals(4, scn.GetTwilight());

		scn.MoveMinionsToTable(troop);
		scn.ShadowPass();
		// The start of the maneuver phase adds nothing.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(4, scn.GetTwilight());
	}

	@Test
	public void AddsOneAgainAtTheStartOfTheNextTurnsShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spurred = scn.GetShadowCard("spurred");
		scn.MoveCardsToSupportArea(spurred);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, spurred);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// 1 companion + 2 for Ettenmoors + 1
		assertEquals(4, scn.GetTwilight());

		// Turn 2 is the Shadow player's own, and turn 3 the Free Peoples player's again.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, spurred);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 0 set + 1 companion + 0 for Rivendell Valley + 1
		assertEquals(2, scn.GetTwilight());
	}
}
