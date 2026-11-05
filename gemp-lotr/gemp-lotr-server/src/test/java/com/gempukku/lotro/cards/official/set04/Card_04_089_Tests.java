package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "4_89");
					put("gimli", "1_13");       // Gimli, Son of Glóin: a [dwarven] companion, cost 2; a skirmish ability only
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally, cost 0; a fellowship ability used only on purpose
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession, cost 1, that Frodo could bear
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, a character the Free Peoples player cannot play on his turn
					// Ent Avenger: a [gandalf] companion, laid in the Shadow player's own discard pile only, a
					// title none of the Free Peoples player's piles holds
					put("avenger", "6_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Gandalf, Greyhame
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: <b>Fellowship:</b> If the twilight pool has fewer than 2 twilight tokens, add (2) to play a character from your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Greyhame", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, card.getBlueprint().getSignet());
	}

	@Test
	public void AtOneTwilightAddsTwoToPlayACompanionOrAllyFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetFreepsCard("runner");
		var avenger = scn.GetShadowCard("avenger");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(gimli, barliman, sword, runner, avenger);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsRefreshDecision();

		assertInDiscard(gimli, barliman, sword, runner, avenger);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsActionAvailable(gandalf));

		scn.FreepsUseCardAction(gandalf);
		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, barliman));
		// The possession is no character, the minion cannot be played on his own turn, and Ent Avenger
		// lies in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, runner, avenger));

		scn.FreepsChooseCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		// 1 + 2 for Gandalf's own cost + 2 for Gimli
		assertEquals(5, scn.GetTwilight());
		assertInDiscard(barliman, sword, runner, avenger);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTwoTwilightItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(gimli);

		scn.StartGame();
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		assertInDiscard(gimli);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void WithNoCharacterInTheDiscardPileThatCanBePlayedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetFreepsCard("runner");
		var avenger = scn.GetShadowCard("avenger");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToDiscard(sword, runner, avenger);

		scn.StartGame();

		// The play from the discard pile is part of the price (Q-0062): with nothing there it can play, the
		// ability is withheld though the pool is empty.
		assertInDiscard(sword, runner, avenger);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}
}
