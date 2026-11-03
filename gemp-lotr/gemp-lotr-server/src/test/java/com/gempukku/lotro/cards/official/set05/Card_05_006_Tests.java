package com.gempukku.lotro.cards.official.set05;

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

public class Card_05_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("keep", "5_6");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf
					put("filler", "1_7");       // Dwarf Guard: a card to lie on top of the draw deck and be drawn
					put("lord", "1_21");        // Lord of Moria: [dwarven] condition, to the support area; "Each Dwarf is damage +1"
					put("heart", "1_10");       // Dwarven Heart: [dwarven] condition borne by a Dwarf, cost 0
					// Songs of the Blessed Realm: an [elven] condition, cost 1, no requirement to play; its trigger
					// answers an [elven] tale, of which there is none
					put("songs", "1_61");
					put("axe", "1_9");          // Dwarven Axe: a [dwarven] possession, cost 0, borne by a Dwarf
					// Stairs of Khazad-dum: a [dwarven] condition, cost 0, to the support area; laid in the Shadow
					// player's discard pile, a title the Free Peoples piles do not hold there
					put("stairs", "1_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DefendingtheKeepStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Defending the Keep
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Spot a Dwarf to draw a card or play a [dwarven] condition from your discard pile.
		*/

		var scn = GetScenario();

		var keep = scn.GetFreepsCard("keep");

		assertEquals("Defending the Keep", keep.getBlueprint().getTitle());
		assertNull(keep.getBlueprint().getSubtitle());
		assertFalse(keep.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, keep.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, keep.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, keep.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(keep, Timeword.FELLOWSHIP));
		assertEquals(0, keep.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithBothPossibleItAsksWhichAndDrawsACardWhenThatIsChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keep = scn.GetFreepsCard("keep");
		var gimli = scn.GetFreepsCard("gimli");
		var filler = scn.GetFreepsCard("filler");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(keep);
		scn.MoveCardsToDiscard(lord);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(filler);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, filler);
		assertInDiscard(lord);
		int deck = scn.GetFreepsDeckCount();

		assertTrue(scn.FreepsPlayAvailable(keep));
		scn.FreepsPlayCard(keep);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChoose("Draw");

		assertInHand(filler);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(lord);
		assertInDiscard(keep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarfAllyIsSpottedAndOnlyADwarvenConditionInHisOwnDiscardPileIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keep = scn.GetFreepsCard("keep");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		var heart = scn.GetFreepsCard("heart");
		var songs = scn.GetFreepsCard("songs");
		var axe = scn.GetFreepsCard("axe");
		var stairs = scn.GetShadowCard("stairs");
		// The ally is the only Dwarf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(keep);
		scn.MoveCardsToDiscard(lord, heart, songs, axe, stairs);

		scn.StartGame();
		assertInDiscard(lord, heart, songs, axe, stairs);
		int deck = scn.GetFreepsDeckCount();

		assertTrue(scn.FreepsPlayAvailable(keep));
		scn.FreepsPlayCard(keep);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChoose("Play");

		// The support-area condition and the one a Dwarf bears; not a condition of another culture, a [dwarven]
		// card that is no condition, or a [dwarven] condition in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lord, heart));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(songs, axe, stairs));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(lord);

		assertInZone(Zone.SUPPORT, lord);
		assertInDiscard(heart, songs, axe, stairs);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertInDiscard(keep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheConditionIsPlayedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keep = scn.GetFreepsCard("keep");
		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		var songs = scn.GetFreepsCard("songs");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(keep);
		scn.MoveCardsToDiscard(lord, songs, axe);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInDiscard(lord, songs, axe);

		scn.FreepsPlayCard(keep);

		// The draw cannot be done, so it is no branch: the one [dwarven] condition is played with nothing asked,
		// and the [elven] condition and the [dwarven] possession beside it are not offered.
		assertInZone(Zone.SUPPORT, lord);
		assertInDiscard(songs, axe);
		assertInDiscard(keep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarvenConditionInTheDiscardPileACardIsDrawnUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keep = scn.GetFreepsCard("keep");
		var gimli = scn.GetFreepsCard("gimli");
		var filler = scn.GetFreepsCard("filler");
		var songs = scn.GetFreepsCard("songs");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(keep);
		scn.MoveCardsToDiscard(songs, axe);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(filler);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.DECK, filler);
		assertInDiscard(songs, axe);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(keep);

		assertInHand(filler);
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(songs, axe);
		assertInDiscard(keep);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNothingToDrawOrPlayItIsStillPlayedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keep = scn.GetFreepsCard("keep");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(keep);
		scn.MoveCardsToDiscard(axe);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertInDiscard(axe);

		assertTrue(scn.FreepsPlayAvailable(keep));
		scn.FreepsPlayCard(keep);

		assertInDiscard(keep, axe);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWithoutADwarfInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var keep = scn.GetFreepsCard("keep");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		// A companion who is no Dwarf in play, a [dwarven] condition in play, and a Dwarf in hand.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(keep, gimli);

		scn.StartGame();
		assertInHand(keep, gimli);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(keep));
	}
}
