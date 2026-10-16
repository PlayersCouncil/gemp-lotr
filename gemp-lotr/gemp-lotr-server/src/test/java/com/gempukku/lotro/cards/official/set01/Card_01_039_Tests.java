package com.gempukku.lotro.cards.official.set01;

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

public class Card_01_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("song", "1_39");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; strength +3 only against a Nazgul
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, cost 2; an archery ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, no Elf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElfsongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Elf-song
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot an Elf to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("song");

		assertEquals("Elf-song", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingAnElfCompanionRemovesOneBurdenAndExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(song);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(song));

		scn.FreepsPlayCard(song);
		// One of the two burdens, not both; a spot is no exertion.
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndAnElfAllyIsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Aragorn and Frodo are companions, and neither is an Elf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(song, orophin);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.FreepsPlayAvailable(song));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(orophin);
		assertInZone(Zone.SUPPORT, orophin);
		assertTrue(scn.FreepsPlayAvailable(song));

		scn.FreepsPlayCard(song);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertInZone(Zone.DISCARD, song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBurdenItMayStillBePlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetFreepsCard("song");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(song);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(song));

		scn.FreepsPlayCard(song);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, song);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
