package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_347_Tests
{

	// The Towers-block path, with Deep of Helm as its site 5.
	protected VirtualTableScenario GetDeepOfHelmScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Uruk-hai Raiding Party: Uruk-hai, cost 4, site 5; damage +1 and nothing else
					put("party1", "1_158");
					put("party2", "1_158");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_347");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path, with the candidates of "Uruk-hai" beside a Raiding Party.
	protected VirtualTableScenario GetCandidatesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Uruk-hai Raiding Party: [isengard] Uruk-hai, cost 4, site 5; damage +1 and nothing else
					put("party", "1_158");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, cost 3, site 6; damage +1 and nothing else
					put("guard", "10_81");
					// Isengard Retainer: an [isengard] Orc, no Uruk-hai, cost 4, site 4; a regroup ability never used
					put("retainer", "3_57");
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_347");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeepofHelmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Deep of Helm
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 5
		 * Shadow Number: 6
		 * Direction: Left
		 * Game Text: <b>Battleground</b>. <b>Plains</b>. The twilight cost of the first Uruk-hai played at Deep of Helm each turn is -3.
		*/

		var scn = GetDeepOfHelmScenario();

		var card = scn.GetFreepsSite(5);

		assertEquals("Deep of Helm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(5, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void OnlyTheFirstUrukHaiPlayedHereInATurnCostsThreeLessWhateverThePhaseAndTheFirstOfTheNextTurnDoesAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeepOfHelmScenario();

		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		var freepsParty = scn.GetFreepsCard("party1");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(party1, party2);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Deep of Helm", scn.GetCurrentSite().getBlueprint().getTitle());
		int twilight = scn.GetTwilight();
		// 4 - 3 for the first Uruk-hai played here this turn
		assertEquals(1, scn.GetTwilightCost(party1));
		assertEquals(1, scn.GetTwilightCost(party2));

		scn.ShadowPlayCard(party1);

		assertInZone(Zone.SHADOW_CHARACTERS, party1);
		assertEquals(twilight - 1, scn.GetTwilight());
		// The second Uruk-hai costs what is printed on it,
		assertEquals(4, scn.GetTwilightCost(party2));

		// and still does in the later phases of the same turn: "each turn", not each phase.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(4, scn.GetTwilightCost(party2));
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(4, scn.GetTwilightCost(party2));

		// The next turn is the other player's: his fellowship comes to Deep of Helm from site 4, and the first
		// Uruk-hai played there that turn, by the player whose turn the last one was, costs 3 less again.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(freepsParty);
		scn.ShadowPass();

		assertEquals("Deep of Helm", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetTwilightCost(freepsParty));
		twilight = scn.GetTwilight();

		scn.FreepsPlayCard(freepsParty);

		assertInZone(Zone.SHADOW_CHARACTERS, freepsParty);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void AnUrukHaiPlayedAtTheSiteBeforeInTheSameTurnIsNotTheFirstPlayedAtDeepOfHelm() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeepOfHelmScenario();

		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(party1, party2);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Site 4, White Rocks: an Uruk-hai is played there at its printed cost.
		assertEquals(4, (int) scn.GetCurrentSite().getSiteNumber());
		assertEquals(4, scn.GetTwilightCost(party1));
		scn.ShadowPlayCard(party1);
		assertInZone(Zone.SHADOW_CHARACTERS, party1);

		// The fellowship moves again, to Deep of Helm, in the same turn.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("Deep of Helm", scn.GetCurrentSite().getBlueprint().getTitle());
		int twilight = scn.GetTwilight();
		// That Uruk-hai was not played at Deep of Helm: the next is the first played here this turn. 4 - 3
		assertEquals(1, scn.GetTwilightCost(party2));

		scn.ShadowPlayCard(party2);

		assertInZone(Zone.SHADOW_CHARACTERS, party2);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnotherCultureIsThreeCheaperAndAnIsengardOrcIsNeitherDiscountedNorCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCandidatesScenario();

		var party = scn.GetShadowCard("party");
		var guard = scn.GetShadowCard("guard");
		var retainer = scn.GetShadowCard("retainer");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(party, guard, retainer);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Deep of Helm", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		// 4 - 3, 3 - 3; the Orc its printed 4
		assertEquals(1, scn.GetTwilightCost(party));
		assertEquals(0, scn.GetTwilightCost(guard));
		assertEquals(4, scn.GetTwilightCost(retainer));

		scn.ShadowPlayCard(retainer);
		// 4 to play, and no roaming: his site number is 4
		assertEquals(twilight - 4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, retainer);
		// No Uruk-hai has been played here yet.
		assertEquals(1, scn.GetTwilightCost(party));
		assertEquals(0, scn.GetTwilightCost(guard));

		scn.ShadowPlayCard(guard);
		// 0 to play + 2 roaming (site number 6, played at site 5)
		assertEquals(twilight - 4 - 2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		// The [sauron] Uruk-hai was the first played here this turn.
		assertEquals(4, scn.GetTwilightCost(party));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromDeepOfHelmTheFirstUrukHaiCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetCandidatesScenario();

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetShadowHand(party);

		// The turn begins with the fellowship at Deep of Helm: 4 - 3 while it is there.
		assertEquals("Deep of Helm", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetTwilightCost(party));

		scn.SetTwilight(10);
		scn.FreepsPass();

		// Site 6, Caves of Aglarond, with Deep of Helm behind the fellowship.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertEquals(4, scn.GetTwilightCost(party));

		scn.ShadowPlayCard(party);
		// 4 to play, and no roaming: its site number is 5
		assertEquals(twilight - 4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, party);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
