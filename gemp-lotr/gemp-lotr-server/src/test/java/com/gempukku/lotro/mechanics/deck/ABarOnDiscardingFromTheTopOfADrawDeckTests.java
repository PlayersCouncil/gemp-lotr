package com.gempukku.lotro.mechanics.deck;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * A forced discard from the top of a draw deck that a bar protects ("Shadow cards may not discard cards from your
 * hand or from the top of your draw deck", Frodo, Courteous Halfling 4_301 and Frodo, Little Master 19_28) is a
 * cost that cannot be paid, so the action whose cost it is is not offered (CR 4.2 "cost": a cost must be paid in
 * full).  The bar protects the deck's OWNER: the definition word `discardTopCardsFromDeck` asks it of the deck it
 * discards from, in its cost check as in its forecast and its effect, and not of the player whose action it is.
 *
 * Corsair Ruffian 10_39 is the Shadow card, the one printed card with such a cost on another player's deck: "When
 * you play this minion, you may spot another corsair and discard the top card of the Free Peoples player's draw
 * deck to add 2 [raider] tokens to a card that already has a [raider] token on it."  Corsair Boatswain 10_37 is the
 * other corsair; Ships of Great Draught 8_65, a [raider] support-area possession, holds the token.  The discard as an
 * EFFECT of an optional trigger (Uruk Soldier 1_154) is withheld by its forecast, in
 * mechanics/actions/AnOfferThatCostsNothingAndWouldDoNothingIsNotMadeTests.
 */
public class ABarOnDiscardingFromTheTopOfADrawDeckTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ruffian", "10_39");    // Corsair Ruffian
					put("boatswain", "10_37");  // Corsair Boatswain: the other corsair; a Shadow ability, not used
					put("ships", "8_65");       // Ships of Great Draught: a [raider] card to hold the token
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound companion; a skirmish ability only
					put("arwen", "7_16");       // Arwen, Fair Elf Maiden: an unbound companion and no archer
					put("boromir", "1_97");     // Boromir, Son of Denethor: an unbound companion; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion, and so not one of the three
				}},
				VirtualTableScenario.FellowshipSites,
				"4_301",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AShadowCostThatDiscardsTheTopOfABarredDrawDeckCannotBePaidAndItsTriggerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var ruffian = scn.GetShadowCard("ruffian");
		var boatswain = scn.GetShadowCard("boatswain");
		var ships = scn.GetShadowCard("ships");
		// Three unbound companions: Frodo's bar is in force.
		scn.MoveCompanionsToTable(gimli, arwen, boromir);
		scn.MoveCardsToHand(ruffian);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(boatswain);
		scn.AddTokensToCard(ships, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetCultureTokensOn(ships));

		scn.ShadowPlayCard(ruffian);

		// The cost cannot be paid: the trigger is not offered, and the Shadow phase goes on.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ruffian));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(1, scn.GetCultureTokensOn(ships));
	}

	@Test
	public void WithoutTheBarTheTriggerIsOfferedAndItsCostDiscardsTheTopCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var sam = scn.GetFreepsCard("sam");
		var ruffian = scn.GetShadowCard("ruffian");
		var boatswain = scn.GetShadowCard("boatswain");
		var ships = scn.GetShadowCard("ships");
		// Two unbound companions, one short, beside a Ring-bound one: the bar is not in force.
		scn.MoveCompanionsToTable(gimli, arwen, sam);
		scn.MoveCardsToHand(ruffian);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveMinionsToTable(boatswain);
		scn.AddTokensToCard(ships, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(1, scn.GetCultureTokensOn(ships));

		scn.ShadowPlayCard(ruffian);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ruffian));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(3, scn.GetCultureTokensOn(ships));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
