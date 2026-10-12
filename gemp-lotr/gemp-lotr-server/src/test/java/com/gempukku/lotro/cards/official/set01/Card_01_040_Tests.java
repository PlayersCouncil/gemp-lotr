package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_040_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "1_40");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, home site 3
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, home site 3
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally, home site 6
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Elrond, Lord of Rivendell
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Ally
		 * Subtype: Elf
		 * Home: 3
		 * Strength: 8
		 * Vitality: 4
		 * Game Text: To play, spot Gandalf or an Elf.
		 * At the start of each of your turns, heal every ally whose home is site 3.
		 * Fellowship: Exert Elrond to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Lord of Rivendell", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(elrond, gandalf);

		scn.StartGame();

		// Only Frodo is in play: a companion, but neither Gandalf nor an Elf.
		assertFalse(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(gandalf);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCardsToHand(elrond, orophin);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(elrond));

		// An Elf who is an ally: the text asks for an Elf, not for an Elf companion.
		scn.FreepsPlayCard(orophin);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StartOfTurnHealsEveryAllyWhoseHomeIsSite3AndNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var silinde = scn.GetFreepsCard("silinde");
		var thrarin = scn.GetFreepsCard("thrarin");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(elrond, silinde, thrarin, orophin);

		scn.AddWoundsToChar(elrond, 2);
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(orophin, 1);

		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(orophin));

		// The first turn starts here.
		scn.StartGame();

		// Healing removes one wound, so Elrond keeps one of his two.
		assertEquals(1, scn.GetWoundsOn(elrond));
		// An Elf and a Dwarf, both at home at site 3
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		// An Elf whose home is site 6
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealsAtTheStartOfItsOwnersTurnsAndNotTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveAlliesToTable(elrond, silinde);

		scn.StartGame();

		// Wounded during the owner's first turn, after its start.
		scn.AddWoundsToChar(silinde, 1);
		assertEquals(1, scn.GetWoundsOn(silinde));

		// The opponent's turn starts: not one of "your turns".
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(1, scn.GetWoundsOn(silinde));

		// The owner's second turn starts.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsElrondToDrawACardUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsUseCardAction(elrond);
		scn.FreepsUseCardAction(elrond);
		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert.
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertEquals(3, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
