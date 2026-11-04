package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_224_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "11_224");
					// Úlairë Attëa, The Easterling: another Nazgûl, cost 6.  "When you play Úlairë Attëa, you may wound
					// an ally": no ally is in play where she is played.
					put("attea", "2_82");
					// Morgul Creeper: a [wraith] Orc of vitality 2, a minion to carry a wound; its text needs 3 burdens
					put("creeper", "8_75");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: vitality 3; a skirmish ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: an ally of vitality 2

					// Cards to lie on top of the Shadow player's draw deck.
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] minion
					put("breath", "1_207");     // Black Breath: a [wraith] condition
					put("runner", "1_178");     // Goblin Runner: a [moria] minion
					// and the Shadow player's own Aragorn, a Free Peoples card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Úlairë Otsëa, Seventh of the Nine Riders
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: When you play Úlairë Otsëa, you may spot a companion who has X wounds to reveal the top X cards of your draw deck. Take each [wraith] card revealed this way into hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", card.getBlueprint().getTitle());
		assertEquals("Seventh of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SpottingACompanionWithThreeWoundsRevealsThreeCardsAndTakesTheWraithCardsAmongThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var creeper = scn.GetShadowCard("creeper");
		var nelya = scn.GetShadowCard("nelya");
		var breath = scn.GetShadowCard("breath");
		var runner = scn.GetShadowCard("runner");
		var attea = scn.GetShadowCard("attea");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.MoveMinionsToTable(creeper);
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(creeper, 1);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(creeper));
		// From the top: a [wraith] minion, a [moria] minion, a [wraith] condition, and a [wraith] minion below them.
		scn.MoveCardsToTopOfDeck(attea, breath, runner, nelya);
		assertSame(nelya, scn.GetFromTopOfShadowDeck(1));
		assertSame(runner, scn.GetFromTopOfShadowDeck(2));
		assertSame(breath, scn.GetFromTopOfShadowDeck(3));
		assertSame(attea, scn.GetFromTopOfShadowDeck(4));
		assertInZone(Zone.DECK, nelya, runner, breath, attea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(otsea);
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowChooseOptionalTrigger(otsea);

		// The wounded companions are offered; not the unwounded Frodo, the wounded ally or the wounded minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, thrarin, creeper));
		scn.ShadowChooseCard(aragorn);

		// X = 3: both players are shown the top three cards.
		assertEquals(3, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(3, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(nelya, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(breath, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// The [wraith] minion and condition revealed are taken; the [moria] minion stays on top, and the [wraith]
		// card that was not revealed stays under it.
		assertInHand(nelya, breath);
		assertSame(runner, scn.GetShadowTopOfDeck());
		assertSame(attea, scn.GetFromTopOfShadowDeck(2));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingACompanionWithOneWoundRevealsOneCardAndAFreePeoplesCardStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var nelya = scn.GetShadowCard("nelya");
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));
		// A Free Peoples card on top, and a [wraith] minion under it.
		scn.MoveCardsToTopOfDeck(nelya, shadowAragorn);
		assertSame(shadowAragorn, scn.GetFromTopOfShadowDeck(1));
		assertSame(nelya, scn.GetFromTopOfShadowDeck(2));
		int hand = scn.GetShadowHandCount();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		// Gimli is the only wounded companion, and is spotted unasked.
		scn.ShadowChooseOptionalTrigger(otsea);

		// X = 1: one card shown to each player.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowAragorn, false));
		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();

		// Nothing taken: Otsëa has left the hand, and nothing has come into it.
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertSame(shadowAragorn, scn.GetShadowTopOfDeck());
		assertSame(nelya, scn.GetFromTopOfShadowDeck(2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOfferMayBeDeclinedAndAnotherNazgulPlayedAfterOtseaDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(otsea, attea);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.MoveCardsToTopOfDeck(nelya);
		assertSame(nelya, scn.GetShadowTopOfDeck());
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(otsea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertSame(nelya, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another Nazgûl played after Otsëa: no offer from Otsëa.
		scn.ShadowPlayCard(attea);
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(otsea));
		assertSame(nelya, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCompanionWoundedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var creeper = scn.GetShadowCard("creeper");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.MoveMinionsToTable(creeper);
		// Wounds on an ally and a minion, none on a companion.
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(creeper, 1);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(creeper));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.MoveCardsToTopOfDeck(nelya);
		assertSame(nelya, scn.GetShadowTopOfDeck());
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(otsea);
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(otsea));
		assertSame(nelya, scn.GetShadowTopOfDeck());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheDrawDeckEmptyItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(otsea);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.SetShadowDrawDeck();
		assertEquals(0, scn.GetShadowDeckCount());
		int hand = scn.GetShadowHandCount();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(otsea);

		// The spot pays nothing, and there is nothing to reveal: not offered (Ketura's ruling, Q-0365: A).
		assertInZone(Zone.SHADOW_CHARACTERS, otsea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(otsea));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(hand - 1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
