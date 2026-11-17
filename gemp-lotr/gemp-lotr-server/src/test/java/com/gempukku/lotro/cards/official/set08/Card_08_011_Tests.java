package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eldar", "8_11");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion; an archery ability used only on purpose
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally; an archery ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, no Elf; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, no Elf; a maneuver ability used only on purpose
					// Left in the draw deck, so that it holds more than 3 cards
					put("guard1", "1_7");       // Dwarf Guard: no text once in play
					put("guard2", "1_7");
					put("guard3", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LifeoftheEldarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Life of the Eldar
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: To play, spot 2 Elves.<br>Discard an Elf to draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eldar");

		assertEquals("Life of the Eldar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElfCompanionAndAnElfAllyItDiscardsAnElfToDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eldar = scn.GetFreepsCard("eldar");
		var legolas = scn.GetFreepsCard("legolas");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(eldar);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertInHand(eldar);
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 3);
		// Exactly 2 Elves, a companion and an ally.
		assertTrue(scn.FreepsPlayAvailable(eldar));
		scn.FreepsPlayCard(eldar);

		// Either Elf may be discarded, and one must be; Gimli, no Elf, is not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(orophin);

		assertInDiscard(orophin);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertInDiscard(eldar);
		// The event leaves the hand and 3 cards come in.
		assertEquals(hand - 1 + 3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneElfAndCompanionsWhoAreNoElves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eldar = scn.GetFreepsCard("eldar");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, gimli, aragorn);
		scn.MoveCardsToHand(eldar);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertInHand(eldar);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(eldar));
	}
}
