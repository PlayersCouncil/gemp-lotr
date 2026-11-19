package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_154_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier1", "51_154");
					put("soldier2", "51_154");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, cost 2; Damage +1 is its whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: companion, vitality 4; a fellowship ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: companion, vitality 2, no text in play
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an ally, so a character who is no companion; her text needs your site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Uruk Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * When you play this minion, exert a companion.  The Free Peoples player may discard the top 2 cards of their draw deck to prevent this.
		 */

		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier1");

		assertEquals("Uruk Soldier", soldier.getBlueprint().getTitle());
		assertNull(soldier.getBlueprint().getSubtitle());
		assertFalse(soldier.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, soldier.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, soldier.getBlueprint().getCulture());
		assertEquals(CardType.MINION, soldier.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, soldier.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(soldier, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertEquals(2, soldier.getBlueprint().getTwilightCost());
		assertEquals(7, soldier.getBlueprint().getStrength());
		assertEquals(1, soldier.getBlueprint().getVitality());
		assertEquals(5, soldier.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayingItTheShadowPlayerExertsACompanionUnlessPrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		assertTrue(scn.ShadowPlayAvailable(soldier));

		scn.ShadowPlayCard(soldier);
		// 2 to play + 2 roaming (site 5 minion at site 2)
		assertEquals(twilight - 4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);

		// The Shadow player chooses: any companion, the Ring-bearer among them; not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(silinde));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardTheTopTwoCardsOfTheirDrawDeckToPreventTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("guard");
		var top3 = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		assertEquals(top1, scn.GetFreepsTopOfDeck());
		scn.SetTwilight(10);
		scn.FreepsPass();

		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(soldier);
		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		assertInZone(Zone.DISCARD, top1, top2);
		assertEquals(top3, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneCardInTheDrawDeckTheExertionCannotBePrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		// Leave the Free Peoples player's Silinde alone in his draw deck.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("soldier1"), scn.GetFreepsCard("soldier2"), scn.GetFreepsCard("savage"),
				scn.GetFreepsCard("sam"), scn.GetFreepsCard("guard"));
		assertEquals(1, scn.GetFreepsDeckCount());
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(soldier);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, frodo));
		scn.ShadowChooseCard(aragorn);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnExhaustedCompanionCannotBeChosenToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier1");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);
		// Vitality 2: the Guard is exhausted and cannot exert.
		scn.AddWoundsToChar(guard, 1);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(soldier);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard));

		scn.ShadowChooseCard(aragorn);
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCompanionAbleToExertThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier1");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		// Frodo, vitality 4, and the Guard, vitality 2, both exhausted.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(guard, 1);
		scn.MoveCardsToHand(soldier);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		scn.ShadowPlayCard(soldier);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.SHADOW_CHARACTERS, soldier);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyPlayingThisMinionExertsAndEachCopyOnlyForItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var savage = scn.GetShadowCard("savage");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(savage, soldier2);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int deck = scn.GetFreepsDeckCount();

		// Another [isengard] Uruk-hai played beside a Soldier in play: nothing.
		scn.ShadowPlayCard(savage);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second Soldier: one exertion, for itself, and not one for the first.
		scn.ShadowPlayCard(soldier2);
		scn.ShadowChooseCard(aragorn);
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
