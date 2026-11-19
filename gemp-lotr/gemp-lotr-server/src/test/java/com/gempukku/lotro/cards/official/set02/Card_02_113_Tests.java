package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("book", "2_113");
					put("retired", "1_284");    // Bilbo, Retired Adventurer: [shire] Hobbit ally; a fellowship ability used only on purpose
					put("melancholy", "12_119"); // Bilbo, Melancholy Hobbit: [shire] Hobbit companion; a start-of-skirmish trigger only
					put("merry", "1_303");      // Merry, From O'er the Brandywine: a [shire] Hobbit companion who is no Bilbo
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					// Thorin's Harp: a [dwarven] tale condition in the support area, cost 0; a maneuver ability never used
					put("harp", "18_3");
					// Scroll of Isildur: a [gondor] tale artifact in the support area, cost 1; to play, spot a [gondor] Man
					put("scroll", "9_36");
					// Banner of the White Tree: a [gondor] tale event, Maneuver, cost 0; exert a [gondor] companion
					put("banner", "3_39");
					// Thror's Map: a [shire] possession in the support area that is no tale; an ability used only on purpose
					put("map", "1_318");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, for a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRuleOfFourScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("book", "2_113");
					put("retired", "1_284");    // Bilbo, Retired Adventurer: [shire] Hobbit ally; a fellowship ability used only on purpose
					put("harp", "18_3");        // Thorin's Harp: a [dwarven] tale condition in the support area, cost 0
					put("delving", "1_6");      // Delving: "Fellowship: Exert a Dwarf companion to draw 3 cards."
					put("gimli", "1_13");       // Gimli, Son of Gloin: the Dwarf companion who exerts for Delving
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: "Fellowship: Exert Elrond to draw a card."
					// Axe Strike: a [dwarven] skirmish event, only ever a card in the deck or hand
					put("axe1", "1_3");
					put("axe2", "1_3");
					put("axe3", "1_3");
					put("axe4", "1_3");
					put("axe5", "1_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RedBookofWestmarchStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Red Book of Westmarch
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Plays to your support area.<br>Each time you play a tale, you may spot Bilbo to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("book");

		assertEquals("Red Book of Westmarch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ItsOwnPlayIsATalePlayedAndWithTheBilboWhoIsAnAllyDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var retired = scn.GetFreepsCard("retired");
		scn.MoveAlliesToTable(retired);
		scn.MoveCardsToHand(book);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, retired);
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(book);

		// The Red Book is in play before anything answers its play (CR 4.2 "playing a card"), and it is a tale.
		assertInZone(Zone.SUPPORT, book);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(book));
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTalePlayedWithTheBilboWhoIsACompanionDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var melancholy = scn.GetFreepsCard("melancholy");
		var harp = scn.GetFreepsCard("harp");
		var scroll = scn.GetFreepsCard("scroll");
		scn.MoveCardsToSupportArea(book);
		scn.MoveCompanionsToTable(melancholy, scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToHand(harp, scroll);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, book);
		assertInZone(Zone.FREE_CHARACTERS, melancholy);
		assertEquals(2, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		// A [dwarven] tale condition.
		scn.FreepsPlayCard(harp);
		assertInZone(Zone.SUPPORT, harp);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(book));
		scn.FreepsAcceptOptionalTrigger();
		// 2 - the Harp + 1
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());

		// A [gondor] tale artifact: the same offer again.
		scn.FreepsPlayCard(scroll);
		assertInZone(Zone.SUPPORT, scroll);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(book));
		scn.FreepsAcceptOptionalTrigger();
		// 2 - the Scroll + 1
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ATaleEventPlayedInTheManeuverPhaseDrawsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var retired = scn.GetFreepsCard("retired");
		var aragorn = scn.GetFreepsCard("aragorn");
		var banner = scn.GetFreepsCard("banner");
		scn.MoveCardsToSupportArea(book);
		scn.MoveAlliesToTable(retired);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(banner);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(banner);
		// Aragorn, the only [gondor] companion, exerts for the Banner.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(book));
		scn.FreepsAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, banner);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningTheOfferDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var retired = scn.GetFreepsCard("retired");
		var harp = scn.GetFreepsCard("harp");
		scn.MoveCardsToSupportArea(book);
		scn.MoveAlliesToTable(retired);
		scn.MoveCardsToHand(harp);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(harp);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(book));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, harp);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShirePossessionThatIsNoTaleOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var retired = scn.GetFreepsCard("retired");
		var map = scn.GetFreepsCard("map");
		scn.MoveCardsToSupportArea(book);
		scn.MoveAlliesToTable(retired);
		scn.MoveCardsToHand(map);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(map);

		assertInZone(Zone.SUPPORT, map);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAHobbitWhoIsNoBilboATalePlayedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var book = scn.GetFreepsCard("book");
		var merry = scn.GetFreepsCard("merry");
		var harp = scn.GetFreepsCard("harp");
		scn.MoveCardsToSupportArea(book);
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(harp);

		scn.StartGame();

		// Frodo and Merry, [shire] Hobbits, are in play; no Bilbo is.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsPlayCard(harp);

		assertInZone(Zone.SUPPORT, harp);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithFourCardsDrawnThisFellowshipPhaseATalePlayedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRuleOfFourScenario();

		var book = scn.GetFreepsCard("book");
		var retired = scn.GetFreepsCard("retired");
		var harp = scn.GetFreepsCard("harp");
		var delving = scn.GetFreepsCard("delving");
		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToSupportArea(book);
		scn.MoveAlliesToTable(retired, elrond);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(harp, delving);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(elrond));

		// Three cards drawn by Delving (Gimli the only Dwarf companion exerts), and a fourth by Elrond.
		scn.FreepsPlayCard(delving);
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		// 2 - Delving + 3 + 1, and the last card of the deck is there to be drawn.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());

		// The Rule of 4 is spent: the draw would draw nothing, and it is not offered (Q-0262).
		scn.FreepsPlayCard(harp);
		assertInZone(Zone.SUPPORT, harp);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
