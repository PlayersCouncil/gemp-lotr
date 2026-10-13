package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "4_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDeadPileScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wellmet", "4_106");
					// Elrond, Lord of Rivendell: the Elf, an ally; his start-of-turn heal reaches allies of
					// site 3 only, and his fellowship ability is not used
					put("elrond", "1_40");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Man
					put("gimli", "5_7");        // Gimli, Skilled Defender: the Dwarf; a trigger on winning a skirmish only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: cost 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WellMetIndeedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Well Met Indeed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot an Elf, a [gondor] Man, and a Dwarf to play Gandalf from your dead pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Well Met Indeed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsAnElfAGondorManAndADwarfToPlayGandalfFromTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDeadPileScenario();

		var wellmet = scn.GetFreepsCard("wellmet");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToDeadPile(gandalf);
		scn.MoveCardsToHand(wellmet);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DEAD, gandalf);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertTrue(scn.FreepsPlayAvailable(wellmet));

		scn.FreepsPlayCard(wellmet);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetFreepsDeadCount());
		// 5 for the event + 4 for Gandalf.
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.DISCARD, wellmet);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
