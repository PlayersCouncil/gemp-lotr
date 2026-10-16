package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("book", "3_1");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion of another title, no text but its requirement to play
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ALLY; a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					// Dwarven Axe: cards with no text that reaches a test, laid as the draw deck
					put("axe1", "1_9");
					put("axe2", "1_9");
					put("axe3", "1_9");
				}},
				// Site 3 Rivendell Valley, 4 Balin's Tomb (its maneuver ability is used only on purpose), 5 The Bridge of Khazad-dum
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("book", "3_1");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion of another title, no text but its requirement to play
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ALLY; a fellowship ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					// Dwarven Axe: cards with no text that reaches a test, laid as the draw deck
					put("axe1", "1_9");
					put("axe2", "1_9");
					put("axe3", "1_9");
				}},
				// Site 4 of the Tower block: White Rocks
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BookofMazarbulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Book of Mazarbul
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: 
		 * Game Text: <b>Tale</b>. Bearer must be a Dwarf.<br>At the start of each fellowship phase when the fellowship is at site 4 or higher, you may draw a card for each Dwarf companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("book");

		assertEquals("Book of Mazarbul", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnADwarfCompanionOrADwarfAllyAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(book);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(book));
		scn.FreepsPlayCard(book);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertAttachedTo(book, grimir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheFellowshipPhaseAtSite4DrawsACardForEachDwarfCompanionAndNotForAnAllyOrAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var troop = scn.GetShadowCard("troop");
		var axe1 = scn.GetFreepsCard("axe1");
		var axe2 = scn.GetFreepsCard("axe2");
		var axe3 = scn.GetFreepsCard("axe3");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, book);

		scn.StartGame();
		assertAttachedTo(book, gimli);
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.SkipToSite(4);
		scn.SetFreepsHand();
		scn.SetFreepsDrawDeck(axe1, axe2, axe3);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(book));

		scn.FreepsAcceptOptionalTrigger();
		// Gimli and the Dwarf Guard; Grimir is an ally and Frodo no Dwarf
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The start of the next phase the Free Peoples player is asked in sets nothing off.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AtSite3NothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, book);

		scn.StartGame();
		assertAttachedTo(book, gimli);

		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedAtSite4NothingIsDrawnAndAtSite5OneDwarfCompanionDrawsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var axe1 = scn.GetFreepsCard("axe1");
		var axe2 = scn.GetFreepsCard("axe2");
		var axe3 = scn.GetFreepsCard("axe3");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(gimli, book);

		scn.StartGame();
		assertAttachedTo(book, gimli);

		scn.SkipToSite(4);
		scn.SetFreepsHand();
		scn.SetFreepsDrawDeck(axe1, axe2, axe3);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(book));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(5);
		scn.SetFreepsHand();
		scn.SetFreepsDrawDeck(axe1, axe2, axe3);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(book));

		scn.FreepsAcceptOptionalTrigger();
		// Gimli alone; Grimir is an ally
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoDwarfCompanionTheDrawIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var grimir = scn.GetFreepsCard("grimir");
		var axe1 = scn.GetFreepsCard("axe1");
		var axe2 = scn.GetFreepsCard("axe2");
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(grimir, book);

		scn.StartGame();
		assertAttachedTo(book, grimir);

		scn.SkipToSite(4);
		scn.SetFreepsHand();
		scn.SetFreepsDrawDeck(axe1, axe2);
		assertEquals(4, scn.GetCurrentSiteNumber());
		// The Dwarf ally bears the Book, and no Dwarf companion is there to count: a draw of none.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite4OfTheTowerBlockNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var book = scn.GetFreepsCard("book");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, book);

		scn.StartGame();
		assertAttachedTo(book, gimli);

		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
