package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "7_58");
					put("gollum2", "7_58");
					// Morgul Whelp: a [wraith] Orc minion of cost 1, with a standing strength bonus only; it lies in the
					// discard pile beside Gollum
					put("whelp", "7_202");
					// Goblin Runner: a minion on the table, so that there is a maneuver phase; a play trigger only
					put("runner", "1_178");
					// Cards for the Free Peoples hand: with 4 there he has initiative, with 3 the Shadow player has.
					put("strike", "1_3");       // Axe Strike
					put("strike2", "1_3");
					put("lorien", "1_53");      // Lórien Elf
					put("guard", "1_7");        // Dwarf Guard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "7_58");
					// Three cards for the Free Peoples hand: with fewer than 4 there, the Shadow player has initiative.
					put("strike", "1_3");       // Axe Strike
					put("lorien", "1_53");      // Lórien Elf
					put("guard", "1_7");        // Dwarf Guard
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GollumStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gollum, Plotting Deceiver
		 * Unique: True
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: 
		 * Strength: 5
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: If you have initiative during the Shadow phase, you may play this minion from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gollum");

		assertEquals("Gollum", card.getBlueprint().getTitle());
		assertEquals("Plotting Deceiver", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithInitiativeInTheShadowPhaseGollumIsPlayedFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardPileScenario();

		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToDiscard(gollum);
		scn.MoveCardsToHand(scn.GetFreepsCard("strike"), scn.GetFreepsCard("lorien"), scn.GetFreepsCard("guard"));

		scn.StartGame();
		assertInZone(Zone.DISCARD, gollum);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, gollum);
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyGollumHimselfIsPlayedFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var whelp = scn.GetShadowCard("whelp");
		scn.MoveCardsToDiscard(gollum, whelp);
		scn.MoveCardsToHand(scn.GetFreepsCard("strike"), scn.GetFreepsCard("lorien"), scn.GetFreepsCard("guard"));

		scn.StartGame();
		assertInZone(Zone.DISCARD, gollum, whelp);
		// 1 set + 1 companion + 2 for the site: 4, enough for Gollum (2 + 2 roaming) or the Whelp (1 + 2 roaming).
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, gollum, whelp);
		assertFalse(scn.ShadowActionAvailable(whelp));
		assertTrue(scn.ShadowActionAvailable(gollum));

		scn.ShadowUseCardAction(gollum);
		// No choice of minion is asked: Gollum is in play and the Whelp is still in the discard pile.
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertInZone(Zone.DISCARD, whelp);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithoutInitiativeGollumIsNotPlayedFromTheDiscardPileThoughACopyInHandIsPlayable() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var gollum2 = scn.GetShadowCard("gollum2");
		scn.MoveCardsToDiscard(gollum);
		scn.MoveCardsToHand(gollum2);
		scn.MoveCardsToHand(scn.GetFreepsCard("strike"), scn.GetFreepsCard("strike2"), scn.GetFreepsCard("lorien"),
				scn.GetFreepsCard("guard"));

		scn.StartGame();
		assertInZone(Zone.DISCARD, gollum);
		assertInHand(gollum2);
		// 1 set + 1 companion + 2 for the site: 4, enough for Gollum (2 + 2 roaming).
		scn.SetTwilight(1);
		scn.FreepsPass();

		// Four cards in the Free Peoples hand: he has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, gollum);
		assertInHand(gollum2);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
		// The initiative is asked of the play from the discard pile alone.
		assertTrue(scn.ShadowPlayAvailable(gollum2));
	}

	@Test
	public void WithInitiativeGollumIsNotPlayedFromTheDiscardPileInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToDiscard(gollum);
		scn.MoveCardsToHand(scn.GetFreepsCard("strike"), scn.GetFreepsCard("lorien"), scn.GetFreepsCard("guard"));

		scn.StartGame();
		assertInZone(Zone.DISCARD, gollum);
		scn.MoveMinionsToTable(runner);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertInZone(Zone.DISCARD, gollum);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gollum));
	}
}
