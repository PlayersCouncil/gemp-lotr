package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boatswain", "10_37");
					put("boatswain2", "10_37");

					put("scout", "4_252");      // Southron Scout: a [raider] Man, ambush only; cost 1
					put("gunners", "8_55");     // Corsair Gunners: a [raider] Man, a corsair; a skirmish ability only
					put("champion", "13_84");   // Corsair Champion: a [men] Man, "Corsair" in his title only
					put("beasts", "7_149");     // Great Beasts: [raider] support-area condition; to play, spot a [raider] Man

					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession, cost 2
					put("charger", "103_41");   // Bladetusk Charger: [raider] support-area possession, cost 3
					put("halberd", "7_156");    // Raider Halberd: [raider] possession borne by a [raider] Man, cost 0
					put("rearguard", "103_43"); // Bladetusk Rearguard: [raider] support-area artifact, cost 4
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] support-area possession, cost 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairBoatswainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Corsair Boatswain
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>. To play, spot a [raider] Man.<br><b>Shadow:</b> Exert this minion to play a [raider] support area possession from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boatswain");

		assertEquals("Corsair Boatswain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilARaiderManIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boatswain = scn.GetShadowCard("boatswain");
		var scout = scn.GetShadowCard("scout");
		var champion = scn.GetShadowCard("champion");
		var beasts = scn.GetShadowCard("beasts");
		scn.MoveCardsToHand(boatswain, scout);
		// A [raider] card that is no Man.
		scn.MoveCardsToSupportArea(beasts);

		scn.StartGame();
		// A Man who is not [raider].
		scn.MoveMinionsToTable(champion);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(boatswain));

		scn.ShadowPlayCard(scout);
		assertInZone(Zone.SHADOW_CHARACTERS, scout);

		assertTrue(scn.ShadowPlayAvailable(boatswain));
		scn.ShadowPlayCard(boatswain);
		assertInZone(Zone.SHADOW_CHARACTERS, boatswain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACorsairIsARaiderManToSpotToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boatswain = scn.GetShadowCard("boatswain");
		var gunners = scn.GetShadowCard("gunners");
		scn.MoveCardsToHand(boatswain);

		scn.StartGame();
		scn.MoveMinionsToTable(gunners);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(boatswain));
		scn.ShadowPlayCard(boatswain);
		assertInZone(Zone.SHADOW_CHARACTERS, boatswain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsHimToPlayARaiderSupportAreaPossessionFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boatswain = scn.GetShadowCard("boatswain");
		var boatswain2 = scn.GetShadowCard("boatswain2");
		var ships = scn.GetShadowCard("ships");
		var charger = scn.GetShadowCard("charger");
		var halberd = scn.GetShadowCard("halberd");
		var rearguard = scn.GetShadowCard("rearguard");
		var beasts = scn.GetShadowCard("beasts");
		var fleet = scn.GetShadowCard("fleet");

		scn.StartGame();
		scn.SetShadowDrawDeck(ships, charger, halberd, rearguard, beasts, fleet);
		scn.MoveMinionsToTable(boatswain, boatswain2);
		// Enough for any card in the deck, so that only the wording keeps one out.
		scn.SetTwilight(10);
		scn.FreepsPass();

		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertEquals(6, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetWoundsOn(boatswain));
		assertEquals(0, scn.GetWoundsOn(boatswain2));
		assertTrue(scn.ShadowActionAvailable(boatswain));
		scn.ShadowUseCardAction(boatswain);

		// This copy exerts, and the other does not.
		assertEquals(1, scn.GetWoundsOn(boatswain));
		assertEquals(0, scn.GetWoundsOn(boatswain2));

		scn.ShadowDismissRevealedCards();
		// [raider] support-area possessions of two titles; not a [raider] possession that is borne, a [raider]
		// support-area artifact or condition, or a support-area possession of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, charger));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halberd, rearguard, beasts, fleet));
		// A play from the draw deck may always find nothing (Q-0019).
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(ships);

		assertInZone(Zone.SUPPORT, ships);
		assertEquals(5, scn.GetShadowDeckCount());
		// The deck looked through is shuffled: once as the card leaves it and once more after.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNothingInTheDrawDeckToPlayHeExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boatswain = scn.GetShadowCard("boatswain");
		var halberd = scn.GetShadowCard("halberd");
		var rearguard = scn.GetShadowCard("rearguard");
		var beasts = scn.GetShadowCard("beasts");
		var fleet = scn.GetShadowCard("fleet");

		scn.StartGame();
		scn.SetShadowDrawDeck(halberd, rearguard, beasts, fleet);
		scn.MoveMinionsToTable(boatswain);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(boatswain));
		assertTrue(scn.ShadowActionAvailable(boatswain));
		scn.ShadowUseCardAction(boatswain);

		assertEquals(1, scn.GetWoundsOn(boatswain));
		// Shown, and nothing can be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(halberd, false));
		scn.ShadowDismissRevealedCards();

		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(pool, scn.GetTwilight());
		// Looked through and shuffled once.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boatswain = scn.GetShadowCard("boatswain");
		var boatswain2 = scn.GetShadowCard("boatswain2");
		var ships = scn.GetShadowCard("ships");

		scn.StartGame();
		scn.SetShadowDrawDeck(ships);
		scn.MoveMinionsToTable(boatswain, boatswain2);
		scn.AddWoundsToChar(boatswain, 1);
		assertEquals(1, scn.GetWoundsOn(boatswain));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// Vitality 2 with 1 wound: he cannot exert. The other copy still can.
		assertFalse(scn.ShadowActionAvailable(boatswain));
		assertTrue(scn.ShadowActionAvailable(boatswain2));
	}
}
