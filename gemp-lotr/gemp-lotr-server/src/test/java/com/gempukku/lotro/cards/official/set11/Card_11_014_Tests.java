package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("equipped", "11_14");
					put("heart", "1_10");       // Dwarven Heart: a [dwarven] condition, to discard from hand
					put("halls", "1_18");       // Halls of My Home: a [dwarven] event, to discard from hand
					put("armor", "1_92");       // Armor: a [gondor] possession ("Bearer must be a Man"), the card in hand that is not [dwarven]
					put("axe", "1_9");          // Dwarven Axe: [dwarven] possession, hand weapon, "Bearer must be a Dwarf"
					put("bracers", "2_3");      // Dwarven Bracers: [dwarven] possession, bracers, "Bearer must be a Dwarf"
					put("ring", "9_6");         // Ring of Accretion: a [dwarven] ARTIFACT, "Bearer must be a Dwarf"
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession any companion may bear, once an Elf is spotted
					put("gimli", "11_8");       // Gimli, Lively Combatant: [dwarven] Dwarf companion, resistance 7
					put("grimir", "11_10");     // Grimir, Dwarven Emissary: [dwarven] Dwarf companion, resistance 7; a skirmish response only
					put("farin", "11_7");       // Farin, Emissary of Erebor: [dwarven] Dwarf companion, resistance 6; muster only at a battleground or underground
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally (resistance 0); a maneuver ability used only on purpose
					put("elf", "1_53");         // Lorien Elf: an Elf companion, so that the Elven Cloak could be played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WellequippedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Well-equipped
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: Discard a [dwarven] card from hand to play a [dwarven] possession from your draw deck on a [dwarven] companion who has resistance 5 or more.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("equipped");

		assertEquals("Well-equipped", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsADwarvenCardToPlayADwarvenPossessionFromTheDeckOnADwarvenCompanionOfResistance5OrMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var equipped = scn.GetFreepsCard("equipped");
		var heart = scn.GetFreepsCard("heart");
		var halls = scn.GetFreepsCard("halls");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");
		var bracers = scn.GetFreepsCard("bracers");
		var ring = scn.GetFreepsCard("ring");
		var cloak = scn.GetFreepsCard("cloak");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(gimli, grimir, farin, elf);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(equipped, heart, halls, armor);

		scn.StartGame();
		// Two burdens take 2 off every companion's resistance: Gimli and Grimir 7 - 2, Farin 6 - 2.
		scn.AddBurdens(2);
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(5, scn.GetResistance(grimir));
		assertEquals(4, scn.GetResistance(farin));
		assertEquals(0, scn.GetResistance(thrarin));
		assertInHand(equipped, heart, halls, armor);
		assertInZone(Zone.DECK, axe, bracers, ring, cloak);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(equipped));
		scn.FreepsPlayCard(equipped);

		// The cost: a [dwarven] card of any type, and not the [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(heart, halls));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(heart);
		assertInDiscard(heart);
		assertInHand(halls, armor);

		// The deck is looked through: [dwarven] possessions of two classes are offered, a [dwarven] artifact and an
		// [elven] possession are not.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, bracers));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring, cloak));
		scn.FreepsChooseCardBPFromSelection(bracers);

		// The bearer: a [dwarven] companion at resistance 5, not one at 4, not a [dwarven] ally, not another companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(farin, thrarin, elf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(bracers, grimir);
		assertInZone(Zone.DECK, axe, ring, cloak);
		assertInDiscard(equipped);
		// One shuffle as the possession leaves the deck that was looked through, one after.
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutADwarvenCardInHandToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var equipped = scn.GetFreepsCard("equipped");
		var armor = scn.GetFreepsCard("armor");
		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(equipped, armor);

		scn.StartGame();
		assertInHand(equipped, armor);
		assertInZone(Zone.DECK, axe);
		assertEquals(7, scn.GetResistance(gimli));

		// The only other card in hand is [gondor], and the event cannot discard itself.
		assertFalse(scn.FreepsPlayAvailable(equipped));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheDwarvenPossessionsInTheDiscardPileItIsPlayedForNothingAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var equipped = scn.GetFreepsCard("equipped");
		var heart = scn.GetFreepsCard("heart");
		var axe = scn.GetFreepsCard("axe");
		var bracers = scn.GetFreepsCard("bracers");
		var ring = scn.GetFreepsCard("ring");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(equipped, heart);
		scn.MoveCardsToDiscard(axe, bracers);

		scn.StartGame();
		assertInHand(equipped, heart);
		assertInDiscard(axe, bracers);
		assertInZone(Zone.DECK, ring);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(equipped));
		scn.FreepsPlayCard(equipped);
		// The one [dwarven] card in hand is discarded unasked.
		assertInDiscard(heart);

		// The deck is shown, and nothing in it can be played.
		scn.FreepsDismissRevealedCards();

		assertInDiscard(axe, bracers);
		assertInZone(Zone.DECK, ring);
		assertInDiscard(equipped);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenCompanionOfResistance5OrMoreNoPossessionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var equipped = scn.GetFreepsCard("equipped");
		var heart = scn.GetFreepsCard("heart");
		var axe = scn.GetFreepsCard("axe");
		var bracers = scn.GetFreepsCard("bracers");
		var farin = scn.GetFreepsCard("farin");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(farin);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(equipped, heart);

		scn.StartGame();
		scn.AddBurdens(2);
		// 6 - 2: one short.  Thrarin, an ally, has resistance 0.
		assertEquals(4, scn.GetResistance(farin));
		assertInHand(equipped, heart);
		assertInZone(Zone.DECK, axe, bracers);
		int shuffles = scn.GetFreepsDeckShuffleCount();

		assertTrue(scn.FreepsPlayAvailable(equipped));
		scn.FreepsPlayCard(equipped);
		assertInDiscard(heart);

		scn.FreepsDismissRevealedCards();

		assertInZone(Zone.DECK, axe, bracers);
		assertEquals(0, scn.GetAttachedCards(farin).size());
		assertEquals(0, scn.GetAttachedCards(thrarin).size());
		assertInDiscard(equipped);
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
