package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_242_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Faithful Companion: a Hobbit companion, cost 2.  His fellowship ability plays Bill the
					// Pony from the draw deck, and no Bill is in it.
					put("sam", "1_310");
					// The Gaffer, Sam's Father: a Hobbit ally, cost 1.  His fellowship ability heals Frodo or Sam.
					put("gaffer", "1_291");
					// Pippin, Trollslayer: a Hobbit companion of [gondor], vitality 4.  To play, spot a [gondor]
					// companion: he is put on the table.  His skirmish ability is not used.
					put("pippin", "103_31");
					// Bounder: a Hobbit ally, vitality 2; a skirmish ability
					put("bounder", "1_286");
					// Merry, Master Holbytla: a Hobbit companion of [rohan], cost 2, nothing needed to play him
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is a Man
					// Goldberry, River-daughter: a [shire] ally with no race, vitality 6; her texts need a river
					put("goldberry", "9_51");
					// Hobbit Sword: a [shire] possession, cost 1.  "Bearer must be a Hobbit."
					put("sword", "1_299");
					// Smeagol, Once Like Us: a [shire] companion with no race, cost 1.  "To play, add a burden."
					put("smeagol", "103_116");
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion of cost 2 who is a Dwarf
					// Ted Sandyman, Chief's Men's Ally: a Hobbit who is a [men] MINION, cost 1, site number 2
					put("ted", "18_75");
				}},
				new HashMap<>() {{
					put("greendragon", "11_242");
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void GreenDragonInnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Green Dragon Inn
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>Dwelling</b>. At the start of your fellowship phase, you may exert 2 Hobbits to play a Hobbit from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("greendragon");

		assertEquals("Green Dragon Inn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.DWELLING));
	}

	@Test
	public void TwoHobbitsExertToPlayAHobbitFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var goldberry = scn.GetFreepsCard("goldberry");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(bounder, goldberry);

		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetFreepsCard("gimli");
		var ted = scn.GetFreepsCard("ted");

		var greendragon = scn.GetFreepsCard("greendragon");

		scn.StartGame(greendragon);

		assertSame(greendragon, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.DECK, sam, gaffer, merry, sword, smeagol, gimli, ted);

		// Enough in the pool that Ted Sandyman, roaming, could be paid for: 1 + 2.
		scn.SetTwilight(5);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.FreepsAcceptOptionalTrigger();

		// Two of the three Hobbits: the Ring-bearer, a companion of another culture and an ally.  A Man and
		// a [shire] ally who is no Hobbit cannot pay.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, goldberry));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.FreepsChooseCards(pippin, bounder);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(goldberry));
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());

		scn.FreepsDismissRevealedCards();

		// A Hobbit companion, a Hobbit ally and a Hobbit of [rohan] may be played.  Not a [shire] possession
		// with "Hobbit" in its title, a [shire] companion who is no Hobbit, a Dwarf, or a Hobbit who is a
		// minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, gaffer, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword, smeagol, gimli, ted));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCardBPFromSelection(sam);

		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertInZone(Zone.DECK, gaffer, merry);
		// 5 + 2 for Sam
		assertEquals(7, scn.GetTwilight());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 2, scn.GetFreepsDeckShuffleCount()); // as the card left the deck, and the definition's own shuffle after the play
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneHobbitAbleToExertNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var goldberry = scn.GetFreepsCard("goldberry");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(goldberry);
		// Pippin is exhausted.  Frodo, Aragorn and Goldberry could each exert, and one of them is a Hobbit.
		scn.AddWoundsToChar(pippin, 3);

		var greendragon = scn.GetFreepsCard("greendragon");

		scn.StartGame(greendragon);

		assertSame(greendragon, scn.GetCurrentSite());
		assertTrue(scn.IsExhausted(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(goldberry));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningExertsNobodyAndTheOtherPlayerIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToTopOfDeck(gaffer);

		var greendragon = scn.GetFreepsCard("greendragon");

		scn.StartGame(greendragon);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();

		scn.FreepsDeclineOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInZone(Zone.DECK, gaffer);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheHobbitsExertAndThePlayerMayStillPlayNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gaffer = scn.GetFreepsCard("gaffer");
		scn.MoveCompanionsToTable(sam);

		var greendragon = scn.GetFreepsCard("greendragon");

		scn.StartGame(greendragon);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		// Frodo and Sam are the only two Hobbits: they exert without a choice.
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(gaffer));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, gaffer);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoHobbitInTheDrawDeckTheOfferIsStillMadeAndTheExertionsBuyALookThatFindsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		// Every Hobbit of the deck that could be played is taken out of it.  Ted Sandyman, the Hobbit Sword,
		// the [shire] Smeagol, Aragorn, Goldberry and Gimli are left.
		scn.MoveCardsToDiscard(scn.GetFreepsCard("sam"), scn.GetFreepsCard("gaffer"), scn.GetFreepsCard("merry"),
				scn.GetFreepsCard("bounder"));

		var greendragon = scn.GetFreepsCard("greendragon");

		scn.StartGame(greendragon);

		assertEquals(6, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		int shuffles = scn.GetFreepsDeckShuffleCount();
		scn.SetTwilight(5);

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		// The deck is shown, and nothing in it may be chosen.
		scn.FreepsDismissRevealedCards();

		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedToThePlayerWhoseFellowshipIsAtTheInnAndNotOnceItHasMovedOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveAlliesToTable(bounder);

		var shadowPippin = scn.GetShadowCard("pippin");
		var shadowGaffer = scn.GetShadowCard("gaffer");
		scn.MoveCompanionsToTable(shadowPippin);

		var greendragon = scn.GetFreepsCard("greendragon");
		var ruins = scn.GetShadowSite("site5");     // Sirannon Ruins: a river, and no other text

		scn.StartGame(greendragon);

		// The first player's turn: he is offered and declines.  The second player, whose own Frodo and Pippin
		// could exert, is offered nothing on a turn that is not his.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(shadowPippin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// He moves on to site 2.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(2, scn.FreepsGetSiteNumber());

		// The second player's turn begins at site 1, which is the first player's card: the offer is the
		// second player's.  His own Frodo and Pippin exert, and he plays an ally from his own draw deck.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertSame(greendragon, scn.GetCurrentSite());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		scn.MoveCardsToTopOfDeck(shadowGaffer);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(shadowPippin));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(shadowPippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(shadowGaffer);
		assertInZone(Zone.SUPPORT, shadowGaffer);
		assertEquals(1, scn.GetTwilight());
		assertNotInZone(Zone.SUPPORT, scn.GetFreepsCard("gaffer"));

		// The first player's second turn begins at site 2, with Frodo, Pippin and the Bounder all able to
		// exert.  The Inn lies behind him, and nothing is offered.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertSame(greendragon, scn.GetSite(1));
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
