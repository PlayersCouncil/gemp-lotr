package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("delving", "1_6");

					put("gimli", "1_13");       // Gimli, Son of Gloin: unique Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("farin", "1_11");       // Farin, Dwarven Emissary: unique Dwarf companion, vitality 3; strength +2 against an Orc only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, vitality 4; a maneuver ability used only on purpose

					put("deck1", "1_3");        // Axe Strike, a skirmish event: five cards to draw, never played
					put("deck2", "1_3");
					put("deck3", "1_3");
					put("deck4", "1_3");
					put("deck5", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DelvingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Delving
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Exert a Dwarf companion to draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("delving");

		assertEquals("Delving", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAChosenDwarfCompanionToDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delving = scn.GetFreepsCard("delving");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(delving);
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertInHand(delving);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		assertTrue(scn.FreepsPlayAvailable(delving));
		scn.FreepsPlayCard(delving);
		// Dwarf companions of two titles; not a Dwarf ally, nor a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(farin);

		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Three drawn of five, the event gone from hand: 1 - 1 + 3.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertInDiscard(delving);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyDwarfCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var delving = scn.GetFreepsCard("delving");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(delving);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		// Vitality 3: two wounds leave Gimli unable to exert.  A Dwarf ally and a Man companion could exert.
		scn.AddWoundsToChar(gimli, 2);
		scn.FreepsRefreshDecision();

		assertInHand(delving);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(delving));
	}
}
