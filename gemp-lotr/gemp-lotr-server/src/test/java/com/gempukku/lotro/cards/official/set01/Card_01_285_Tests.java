package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_285_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pipe", "1_285");
					// Aragorn's Pipe: a [gondor] pipe on a [gondor] companion; its own fellowship ability is never used
					put("aragornspipe", "1_91");
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here
					// Saruman's Hospitality: a [gandalf] pipeweed possession, borne by an unbound companion bearing a
					// pipe; a move trigger only (it needs pipes of 3 cultures)
					put("hospitality", "103_4");
					put("toby", "1_305");          // Old Toby: [shire] pipeweed possession, support area; when played, may draw a card
					put("halfling", "17_106");     // Halfling Leaf: [shire] pipeweed CONDITION, support area; a skirmish ability
					put("sword", "1_299");         // Hobbit Sword: a [shire] possession that is neither pipe nor pipeweed
					// Sam, Son of Hamfast: Hobbit companion; his fellowship ability (exert to remove a burden) is never used
					put("sam", "1_311");
					// Pippin, Trollslayer: a Hobbit companion who is a [gondor] card; a skirmish ability never used here
					put("trollslayer", "103_31");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used here
					put("smeagol", "103_116");
					put("bounder", "1_286");       // Bounder: [shire] Hobbit ally; a skirmish ability never used here
					// Tales: There and Back Again ([shire] condition), Banner of the White Tree ([gondor] event),
					// Thorin's Harp ([dwarven] support-area condition) and Red Book of Westmarch ([shire] possession)
					put("tbaa", "1_317");
					put("banner", "3_39");
					put("harp", "18_3");
					put("redbook", "2_113");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BilbosPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Bilbo's Pipe
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: Bearer must be a Hobbit.<br><b>Fellowship:</b> Discard a pipeweed possession and spot X pipes to shuffle X tales from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pipe");

		assertEquals("Bilbo's Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAHobbitCompanionOrAllyAndNotOnACompanionWhoIsNoHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var frodo = scn.GetRingBearer();
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(trollslayer, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(pipe);

		scn.StartGame();

		assertInHand(pipe);
		assertTrue(scn.FreepsPlayAvailable(pipe));
		scn.FreepsPlayCard(pipe);

		// The Ring-bearer, a Hobbit who is a [gondor] card, and a Hobbit ally
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, trollslayer, bounder));
		// A [shire] companion who is no Hobbit
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(trollslayer);

		assertAttachedTo(pipe, trollslayer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsAPipeweedPossessionAndSpotsPipesToShuffleThatManyTalesIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hospitality = scn.GetFreepsCard("hospitality");
		var toby = scn.GetFreepsCard("toby");
		var halfling = scn.GetFreepsCard("halfling");
		var sword = scn.GetFreepsCard("sword");
		var sam = scn.GetFreepsCard("sam");
		var tbaa = scn.GetFreepsCard("tbaa");
		var banner = scn.GetFreepsCard("banner");
		var harp = scn.GetFreepsCard("harp");
		var redbook = scn.GetFreepsCard("redbook");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn, sam);
		// Two pipes, of two cultures
		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(aragorn, aragornspipe, hospitality);
		scn.AttachCardsTo(sam, sword);
		scn.MoveCardsToSupportArea(toby, halfling);

		scn.StartGame();
		scn.MoveCardsToDiscard(tbaa, banner, harp);
		scn.MoveCardsToHand(redbook);

		assertAttachedTo(pipe, frodo);
		assertAttachedTo(aragornspipe, aragorn);
		assertAttachedTo(hospitality, aragorn);
		assertInZone(Zone.SUPPORT, toby, halfling);
		assertInDiscard(tbaa, banner, harp);
		assertInHand(redbook);
		int deck = scn.GetFreepsDeckCount();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe, "Use");

		// A pipeweed possession of each of two cultures, wherever it lies; not a pipeweed condition, the pipes, or a
		// possession that is no pipeweed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, hospitality));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(halfling, pipe, aragornspipe, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(hospitality);
		assertInDiscard(hospitality);
		assertInZone(Zone.SUPPORT, toby, halfling);

		// X: up to the 2 pipes. Not 3 (the Hobbit Sword, or the pipeweeds there were).
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		// Exactly 2 of the tales in the discard pile, of any kind; not the pipeweed just discarded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(tbaa, banner, harp));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hospitality));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(tbaa, banner);

		assertInZone(Zone.DECK, tbaa, banner);
		assertEquals(deck + 2, scn.GetFreepsDeckCount());
		assertEquals(shuffles + 1, scn.GetFreepsDeckShuffleCount());
		assertInDiscard(harp, hospitality);
		// The tale in hand is not "from your discard pile".
		assertInHand(redbook);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingOneOfTwoPipesShufflesOneTale() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var toby = scn.GetFreepsCard("toby");
		var tbaa = scn.GetFreepsCard("tbaa");
		var harp = scn.GetFreepsCard("harp");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(aragorn, aragornspipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.MoveCardsToDiscard(tbaa, harp);

		assertInZone(Zone.SUPPORT, toby);
		assertInDiscard(tbaa, harp);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsUseCardAction(pipe, "Use");
		// Old Toby is the only pipeweed possession, and is discarded unasked.
		assertInDiscard(toby);

		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		assertTrue(scn.FreepsHasCardChoiceAvailable(tbaa, harp));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(harp);

		assertInZone(Zone.DECK, harp);
		assertInDiscard(tbaa);
		assertEquals(deck + 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SpottingNoPipesStillDiscardsThePipeweedAndShufflesNoTale() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var tbaa = scn.GetFreepsCard("tbaa");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		scn.MoveCardsToDiscard(tbaa);

		assertInZone(Zone.SUPPORT, toby);
		assertInDiscard(tbaa);
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsUseCardAction(pipe);
		assertInDiscard(toby);

		// A player need not spot all he could (CR 4.2 "spot"): none, or the one pipe.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("0");

		assertInDiscard(tbaa);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoTaleInTheDiscardPileThePipeweedIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var sword = scn.GetFreepsCard("sword");
		var redbook = scn.GetFreepsCard("redbook");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		scn.MoveCardsToSupportArea(toby);

		scn.StartGame();
		// A card in the discard pile that is no tale, and a tale in hand
		scn.MoveCardsToDiscard(sword);
		scn.MoveCardsToHand(redbook);

		assertInZone(Zone.SUPPORT, toby);
		assertInDiscard(sword);
		assertInHand(redbook);
		int deck = scn.GetFreepsDeckCount();
		// An action with a cost is offered whenever the cost can be paid (Q-0034).
		assertTrue(scn.FreepsActionAvailable(pipe));

		scn.FreepsUseCardAction(pipe);
		assertInDiscard(toby);
		scn.FreepsChoose("1");

		assertInDiscard(sword, toby);
		assertInHand(redbook);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithoutAPipeweedPossessionAndOfferedOnceOneIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("pipe");
		var toby = scn.GetFreepsCard("toby");
		var halfling = scn.GetFreepsCard("halfling");
		var tbaa = scn.GetFreepsCard("tbaa");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, pipe);
		// A pipeweed that is no possession
		scn.MoveCardsToSupportArea(halfling);
		scn.MoveCardsToHand(toby);

		scn.StartGame();
		scn.MoveCardsToDiscard(tbaa);

		assertInZone(Zone.SUPPORT, halfling);
		assertInHand(toby);
		assertInDiscard(tbaa);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pipe));

		scn.FreepsPlayCard(toby);
		// Old Toby's own draw is declined.
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(pipe));
	}
}
