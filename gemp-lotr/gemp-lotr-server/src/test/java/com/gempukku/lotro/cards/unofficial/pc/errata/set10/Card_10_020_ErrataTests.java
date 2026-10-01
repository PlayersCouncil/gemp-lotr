package com.gempukku.lotro.cards.unofficial.pc.errata.set10;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_10_020_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strike", "60_20");
					put("original", "10_20");   // the unerrata'd Final Strike, for comparison
					put("gollum", "10_21");     // Gollum, Mad Thing

					put("runner", "1_178");     // Goblin Runner, to play something else in the same phase

					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FinalStrikeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Final Strike
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Any site 9 is a <b>mountain</b>.<br><b>Shadow:</b> If the fellowship is at any site 9, play Gollum
		 * from your discard pile (limit once per phase).<br><b>Response:</b> If the Free Peoples player uses a maneuver
		 * or archery special ability, exert Gollum to cancel its effect.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strike");

		assertEquals("Final Strike", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertTrue(card.getBlueprint().getGameText().contains("(limit once per phase)"));
	}

	/**
	 * Takes the game to site 8, puts the given Final Strike in the Shadow player's support area and Gollum in their
	 * discard pile, then moves to site 9 (which has no fellowship phase), stopping at the Shadow phase.
	 */
	private void MoveToSite9ShadowPhase(VirtualTableScenario scn, com.gempukku.lotro.game.PhysicalCardImpl strike,
			com.gempukku.lotro.game.PhysicalCardImpl gollum) throws DecisionResultInvalidException {
		scn.SkipToSite(8);
		scn.SetTwilight(20);
		scn.MoveCardsToSupportArea(strike);
		scn.MoveCardsToDiscard(gollum);
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
	}

	@Test
	public void PlaysGollumFromDiscardAtSite9OncePerPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetShadowCard("strike");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		MoveToSite9ShadowPhase(scn, strike, gollum);

		assertTrue(scn.ShadowActionAvailable(strike));
		scn.ShadowUseCardAction(strike);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		// Gollum goes back to the discard pile in the same phase (the "recycle"); playing another card refreshes
		// the Shadow player's options
		scn.MoveCardsToDiscard(gollum);
		scn.ShadowPlayCard(runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.ShadowActionAvailable(strike));
	}

	@Test
	public void OriginalFinalStrikeCouldRepeatTheRecycleInOnePhase() throws DecisionResultInvalidException, CardNotFoundException {
		// Control for the test above: the limit is what the errata adds
		var scn = GetScenario();

		var original = scn.GetShadowCard("original");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		MoveToSite9ShadowPhase(scn, original, gollum);

		scn.ShadowUseCardAction(original);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		scn.MoveCardsToDiscard(gollum);
		scn.ShadowPlayCard(runner);
		assertTrue(scn.ShadowActionAvailable(original));
	}

	@Test
	public void RecycleNotAvailableAwayFromSite9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetShadowCard("strike");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(strike);
		scn.MoveCardsToDiscard(gollum);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertFalse(scn.ShadowActionAvailable(strike));
	}

	@Test
	public void Site9IsAMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetShadowCard("strike");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		MoveToSite9ShadowPhase(scn, strike, gollum);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.MOUNTAIN));
	}

	@Test
	public void PcFormatsUseTheErrataAndOldDeckListsConvert() {
		// PC formats with errata set 60 map the original to the errata; a deck list saved with 10_20 is converted
		// when it is validated or joins a game, and the errata itself is legal there
		for (String code : new String[] { "pc_movie", "pc_king_block", "pc_expanded" }) {
			var format = new com.gempukku.lotro.framework.DeckValidationScenario(code).getFormat();
			assertEquals(code, "60_20", format.getErrataCardMap().get("10_20"));
			assertEquals(code, "60_20", format.applyErrata("10_20"));
			assertNull(code, format.validateCard("60_20"));
		}
		// Decipher formats keep the original card and do not accept the PC errata
		var movie = new com.gempukku.lotro.framework.DeckValidationScenario("movie").getFormat();
		assertEquals("10_20", movie.applyErrata("10_20"));
		assertNull(movie.validateCard("10_20"));
		assertNotNull(movie.validateCard("60_20"));
	}
}
