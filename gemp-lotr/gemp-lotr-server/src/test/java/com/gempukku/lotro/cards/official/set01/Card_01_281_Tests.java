package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_281_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eye", "1_281");
					put("ambusher", "1_261");  // Orc Ambusher: [sauron] Orc tracker, vitality 2; a regroup-event response
					put("band", "1_270");      // Orc Scouting Band: [sauron] Orc tracker of another title, vitality 2
					put("soldier", "1_271");   // Orc Soldier: [sauron] Orc who is no tracker, vitality 2
					put("pursuer", "4_190");   // Uruk Pursuer: an [isengard] tracker (an Uruk-hai), vitality 3

					put("aragorn", "1_89");    // Aragorn: [gondor] Man companion, vitality 4
					put("gimli", "1_13");      // Gimli: [dwarven] Dwarf companion, vitality 3
					put("grimir", "1_17");     // Grimir: a Dwarf ally, vitality 3: a character who is no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UndertheWatchingEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Under the Watching Eye
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Search</b>. To play, exert a [sauron] tracker. Plays to your support area.
		 * 	Each time the fellowship moves, the Free Peoples player must exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eye");

		assertEquals("Under the Watching Eye", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayTheShadowPlayerExertsASauronTracker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetShadowCard("eye");
		var ambusher = scn.GetShadowCard("ambusher");
		var band = scn.GetShadowCard("band");
		var soldier = scn.GetShadowCard("soldier");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToHand(eye);

		scn.StartGame();
		// Laid before the move, so that the Shadow phase's first choice of actions already sees them.
		scn.MoveMinionsToTable(ambusher, band, soldier, pursuer);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.ShadowPlayAvailable(eye));

		scn.ShadowPlayCard(eye);
		// Two [sauron] trackers of different titles; not a [sauron] Orc who is no tracker, nor a tracker of [isengard].
		assertTrue(scn.ShadowHasCardChoiceAvailable(ambusher, band));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier, pursuer));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(band);

		assertEquals(1, scn.GetWoundsOn(band));
		assertEquals(0, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertInZone(Zone.SUPPORT, eye);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronTrackerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eye = scn.GetShadowCard("eye");
		var ambusher = scn.GetShadowCard("ambusher");
		var soldier = scn.GetShadowCard("soldier");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToHand(eye);

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher, soldier, pursuer);
		// Vitality 2 with 1 wound: the Ambusher is exhausted.  The Soldier and the Pursuer could exert, but neither
		// is a [sauron] tracker.
		scn.AddWoundsToChar(ambusher, 1);
		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertInHand(eye);
		scn.FreepsPass();

		assertEquals(1, scn.GetWoundsOn(ambusher));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertInHand(eye);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(eye));
	}

	@Test
	public void EachMoveMakesTheFreePeoplesPlayerExertACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(grimir);

		var eye = scn.GetShadowCard("eye");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(eye);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, eye);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The move that ends the fellowship phase.  The Free Peoples player chooses: any companion, the Ring-bearer
		// included; not an ally.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move, with a minion of the Shadow phase still on the table: the condition answers it again.
		scn.MoveMinionsToTable(soldier);
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseToMove();

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, soldier));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingAtTheRegroupDecisionExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var eye = scn.GetShadowCard("eye");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(eye);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, eye);
		scn.FreepsPass();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.MoveMinionsToTable(soldier);
		scn.SkipToMovementDecision();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertInZone(Zone.SUPPORT, eye);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The fellowship stays: the turn ends with nobody exerted, and the other player's begins.
		scn.FreepsChooseToStay();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
