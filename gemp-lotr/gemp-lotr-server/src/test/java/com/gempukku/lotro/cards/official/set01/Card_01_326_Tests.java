package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_326_Tests
{

	// The Fellowship path with Westfarthing as its site 1, so the first fellowship phase is played on it.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, a skirmish ability never used
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf companion of cost 2; a skirmish ability only
					put("gimli2", "1_13");      // a second Gimli, who cannot be played while the first is in play
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: a [shire] Hobbit ally of cost 2; a fellowship ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: a [shire] possession a Hobbit may bear, no text
					put("troop", "1_177");      // Goblin Patrol Troop: a minion, a character that is neither companion nor ally; no text
				}},
				new HashMap<>()
				{{
					put("site1", "1_326");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WestfarthingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Westfarthing
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Exert a Hobbit to play a companion or ally; that character's twilight cost is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Westfarthing", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionExertsAHobbitToPlayACompanionForOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		var troop = scn.GetFreepsCard("troop");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(gimli, bilbo, sword, troop);

		scn.StartGame();

		var westfarthing = scn.GetCurrentSite();
		assertEquals("Westfarthing", westfarthing.getBlueprint().getTitle());
		assertInHand(gimli, bilbo, sword, troop);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(westfarthing));

		scn.FreepsUseCardAction(westfarthing);

		// A Hobbit of either culture, companion or ally, may exert; the companions who are no Hobbits may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));

		// A companion and an ally may be played; a possession and a minion may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, bilbo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertInZone(Zone.FREE_CHARACTERS, gimli);
		// 2 printed - 1
		assertEquals(1, scn.GetTwilight());
		assertInHand(bilbo, sword, troop);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Only the character played by the action is cheaper: Bilbo, played the usual way, costs his 2.
		scn.FreepsPlayCard(bilbo);
		assertInZone(Zone.SUPPORT, bilbo);
		// 1 + 2
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyPlayedByTheActionCostsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToHand(gimli, bilbo);

		scn.StartGame();

		var westfarthing = scn.GetCurrentSite();
		assertInHand(gimli, bilbo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsUseCardAction(westfarthing);
		// Frodo is the only Hobbit, and exerts with nobody asked.
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.FreepsChooseCard(bilbo);

		assertInZone(Zone.SUPPORT, bilbo);
		// 2 printed - 1
		assertEquals(1, scn.GetTwilight());
		assertInHand(gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilACompanionOrAllyInHandCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var gimli2 = scn.GetFreepsCard("gimli2");
		var bilbo = scn.GetFreepsCard("bilbo");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(gimli2, sword);

		scn.StartGame();
		scn.MoveCardsToDiscard(bilbo);
		scn.FreepsRefreshDecision();

		var westfarthing = scn.GetCurrentSite();
		// Frodo could exert, but the one companion in hand is a second Gimli, the ally lies in the discard pile,
		// and the Sword is no character.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertInHand(gimli2, sword);
		assertInZone(Zone.DISCARD, bilbo);
		assertFalse(scn.FreepsPlayAvailable(gimli2));
		assertTrue(scn.FreepsPlayAvailable(sword));
		assertFalse(scn.FreepsActionAvailable(westfarthing));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToHand(bilbo);
		scn.FreepsRefreshDecision();
		assertInHand(bilbo);
		assertTrue(scn.FreepsActionAvailable(westfarthing));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithEveryHobbitExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(gimli);

		scn.StartGame();
		// Every Hobbit at one vitality; Aragorn and Smeagol, who are no Hobbits, could still exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(pippin, 3);
		scn.AddWoundsToChar(bounder, 1);
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(pippin));
		assertEquals(1, scn.GetVitality(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertInHand(gimli);
		// The wounds went on under the pending choice of fellowship actions.
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
