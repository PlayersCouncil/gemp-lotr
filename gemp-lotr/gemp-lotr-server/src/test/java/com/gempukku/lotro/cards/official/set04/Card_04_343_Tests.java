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

public class Card_04_343_Tests
{

	// The Towers path with Ered Nimrais as its site 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2; an ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1; an ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; "Bearer must be a Hobbit" only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, cost 6, site 4; no text
					put("walkers", "1_79");     // The Nine Walkers: [gandalf] support condition; a companion whose race you cannot spot is -2
					put("smeagol", "5_29");     // Smeagol, Slinker: [gollum] companion of no race, cost 0; to play, add a burden
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_343");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EredNimraisStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ered Nimrais
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 4
		 * Shadow Number: 2
		 * Direction: Right
		 * Game Text: <b>Mountain</b>. The twilight cost of each companion and ally played at Ered Nimrais is +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Ered Nimrais", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void ACompanionAndAnAllyPlayedAtEredNimraisCostThreeMoreAndAPossessionItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(gimli, bounder, sword);

		assertEquals("Ered Nimrais", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		// 2 + 3, 1 + 3; the possession its printed 1
		assertEquals(5, scn.GetTwilightCost(gimli));
		assertEquals(4, scn.GetTwilightCost(bounder));
		assertEquals(1, scn.GetTwilightCost(sword));
		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(twilight + 5, scn.GetTwilight());

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertEquals(twilight + 5 + 4, scn.GetTwilight());

		// Frodo and the Hobbit ally may each bear it.
		scn.FreepsPlayCard(sword);
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(sword, frodo);
		assertEquals(twilight + 5 + 4 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMinionPlayedAtEredNimraisCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(gimli, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals("Ered Nimrais", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The minion its printed 6 (site 4: no roaming), while a companion in hand reads 2 + 3.
		assertEquals(6, scn.GetTwilightCost(troop));
		assertEquals(5, scn.GetTwilightCost(gimli));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(troop);

		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(twilight - 6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOtherPlayersCompanionPlayedAtEredNimraisOnHisOwnTurnCostsThreeMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadowGimli = scn.GetShadowCard("gimli");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();
		// Player 1's fellowship has moved on to site 5; player 2's comes to his turn at site 4.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCardsToHand(shadowGimli);

		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals("Ered Nimrais", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		// 2 + 3
		assertEquals(5, scn.GetTwilightCost(shadowGimli));
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(shadowGimli);

		assertInZone(Zone.FREE_CHARACTERS, shadowGimli);
		assertEquals(twilight + 5, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromEredNimraisACompanionCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToHand(gimli);

		// The turn begins with the fellowship at Ered Nimrais: 2 + 3 while it is there.
		assertEquals("Ered Nimrais", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(5, scn.GetTwilightCost(gimli));

		scn.SkipToSite(5);
		scn.MoveCardsToHand(gimli);

		// Site 5, Deeping Wall, with Ered Nimrais behind the fellowship.
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInHand(gimli);
		assertEquals(2, scn.GetTwilightCost(gimli));
		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SmeagolPlayedAtEredNimraisBesideTheNineWalkersCostsThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetFreepsCard("gimli");
		var walkers = scn.GetFreepsCard("walkers");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardsToSupportArea(walkers);
		scn.MoveCardsToHand(smeagol, gimli);
		scn.FreepsRefreshDecision();

		assertEquals("Ered Nimrais", scn.GetCurrentSite().getBlueprint().getTitle());
		assertInZone(Zone.SUPPORT, walkers);
		// CR 4.2's ruling on Ered Nimrais: 3. The Nine Walkers is at work on another companion: Gimli, whose race
		// nobody in play has, is 2 + 3 - 2.
		assertEquals(3, scn.GetTwilightCost(smeagol));
		assertEquals(3, scn.GetTwilightCost(gimli));
		int twilight = scn.GetTwilight();
		int burdens = scn.GetBurdens();

		scn.FreepsPlayCard(smeagol);

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(twilight + 3, scn.GetTwilight());
		assertEquals(burdens + 1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
