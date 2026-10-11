package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_302");        // Merry, Friend to Sam: unbound Hobbit companion, vitality 4, cost 1
					put("pippin", "1_306");       // Pippin, Friend to Frodo: unbound Hobbit companion, vitality 4, cost 1
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound Hobbit companion
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: unbound Man, [gondor]
					// Pippin, Trollslayer: an unbound Hobbit companion who is a [gondor] card, vitality 4; a skirmish
					// ability only. Never in play beside the other Pippin.
					put("trollslayer", "103_31");
					put("bounder", "1_286");      // Bounder: a Hobbit ally; a skirmish ability only

					put("bilbospipe", "1_285");   // Bilbo's Pipe: [shire] pipe, cost 1, bearer must be a Hobbit
					put("aragornspipe", "1_91");  // Aragorn's Pipe: [gondor] pipe, cost 1, bearer must be a [gondor] companion
					put("gandalfspipe", "1_74");  // Gandalf's Pipe: bearer must be Gandalf, who is in none of these tests
					put("treasure", "103_119");   // Treasured Pipe: a pipe kept in hand
					put("frodospipe", "3_107");   // Frodo's Pipe: a pipe kept in the discard pile
					put("toby", "1_305");         // Old Toby: pipeweed possession, cost 1, support area; when played, may draw a card
					put("halfling", "17_106");    // Halfling Leaf: pipeweed condition, cost 1, support area; to play, spot a Hobbit
					put("flotsam", "103_106");    // Flotsam: pipeweed possession, cost 1; bearer must bear a pipe
					put("longbottom", "1_300");   // Longbottom Leaf: a pipeweed kept in hand
					put("hornblower", "17_108");  // Hornblower Leaf: a pipeweed kept in the discard pile
					put("sword", "1_299");        // Hobbit Sword: a [shire] possession that is neither
				}},
				// Bree Gate as site 2 (the move limit is +1 with a ranger); the rest is the rig's Fellowship path
				new HashMap<>() {{
					put("site1", "103_121");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatesofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gates of Isengard
		 * Unique: false
		 * Type: Site
		 * Block: King
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: Fellowship: Exert an unbound Hobbit to play a pipe and pipeweed from your draw deck (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Gates of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ExertsAnUnboundHobbitToPlayAPipeAndThenAPipeweedFromTheDrawDeckOnceInThePhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var aragornspipe = scn.GetFreepsCard("aragornspipe");
		var gandalfspipe = scn.GetFreepsCard("gandalfspipe");
		var pipeInHand = scn.GetFreepsCard("treasure");
		var pipeInDiscard = scn.GetFreepsCard("frodospipe");
		var toby = scn.GetFreepsCard("toby");
		var halfling = scn.GetFreepsCard("halfling");
		var flotsam = scn.GetFreepsCard("flotsam");
		var pipeweedInHand = scn.GetFreepsCard("longbottom");
		var pipeweedInDiscard = scn.GetFreepsCard("hornblower");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCompanionsToTable(merry, trollslayer, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(pipeInHand, pipeweedInHand);
		scn.MoveCardsToDiscard(pipeInDiscard, pipeweedInDiscard);

		scn.StartGame();

		var site = scn.GetCurrentSite();
		assertInZone(Zone.DECK, bilbospipe, aragornspipe, gandalfspipe, toby, halfling, flotsam, sword);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(merry));
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int opponentsShuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.FreepsActionAvailable(site));
		scn.FreepsUseCardAction(site);

		// Unbound Hobbits: a [shire] companion and Pippin, Trollslayer, who is a [gondor] card
		assertEquals(Culture.GONDOR, trollslayer.getBlueprint().getCulture());
		assertTrue(scn.IsRace(trollslayer, Race.HOBBIT));
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, trollslayer));
		// The Ring-bearer, a Ring-bound Hobbit, an unbound companion who is no Hobbit, a Hobbit ally
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, aragorn, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(trollslayer));

		// The pipe. Pipes of two cultures in the draw deck are offered.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbospipe, aragornspipe));
		// A pipe nobody here may bear, the pipeweeds, another possession, a pipe in hand and a pipe in the
		// discard pile
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalfspipe, toby, halfling, flotsam, sword, pipeInHand, pipeInDiscard));
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCardBPFromSelection(bilbospipe);

		// Bilbo's Pipe goes on a Hobbit
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(bilbospipe, merry);
		// 1 for Bilbo's Pipe
		assertEquals(1, scn.GetTwilight());

		// The pipeweed, chosen after the pipe is in play: Flotsam's bearer must bear a pipe, and now one does.
		// A possession and a condition for the support area are offered with it.
		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby, halfling, flotsam));
		// The pipes left in the deck, another possession, a pipeweed in hand and one in the discard pile
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragornspipe, gandalfspipe, sword, pipeweedInHand, pipeweedInDiscard));
		assertEquals(1, scn.FreepsGetChoiceMax());
		// The deck is being looked through a second time
		int shufflesAtTheSecondLook = scn.GetFreepsDeckShuffleCount();
		scn.FreepsChooseCardBPFromSelection(toby);

		// It was played: its own "when you play this possession, you may draw a card" is offered
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(toby));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, toby);
		// 1 for Bilbo's Pipe + 1 for Old Toby
		assertEquals(2, scn.GetTwilight());

		// The deck was looked through, so it has been shuffled, and shuffled again after the second look;
		// the other player's has not
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.GetFreepsDeckShuffleCount() > shufflesAtTheSecondLook);
		assertEquals(opponentsShuffles, scn.GetShadowDeckShuffleCount());
		assertInZone(Zone.HAND, pipeInHand, pipeweedInHand);
		assertInDiscard(pipeInDiscard, pipeweedInDiscard);

		// Once per phase: both unbound Hobbits could exert and a pipe and two pipeweeds are still in the deck
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertInZone(Zone.DECK, aragornspipe, halfling, flotsam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));
	}

	@Test
	public void NotOfferedWhileTheOnlyUnboundHobbitIsExhaustedAndOfferedOnceAnotherIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");

		// Merry cannot exert. Frodo and Sam (Ring-bound), Aragorn (no Hobbit) and the Bounder (an ally) can.
		scn.MoveCompanionsToTable(merry, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(merry, 3);
		scn.MoveCardsToHand(pippin);

		scn.StartGame();

		var site = scn.GetCurrentSite();
		assertTrue(scn.IsExhausted(merry));
		assertFalse(scn.FreepsActionAvailable(site));

		scn.FreepsPlayCard(pippin);

		assertTrue(scn.FreepsActionAvailable(site));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoPipeOrPipeweedInTheDrawDeckTheHobbitStillExertsAndNothingIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");

		scn.MoveCompanionsToTable(merry);
		// Every pipe is in hand and every pipeweed in the discard pile; a Hobbit Sword is in the draw deck
		scn.MoveCardsToHand(scn.GetFreepsCard("bilbospipe"), scn.GetFreepsCard("aragornspipe"),
				scn.GetFreepsCard("gandalfspipe"), scn.GetFreepsCard("treasure"), scn.GetFreepsCard("frodospipe"));
		scn.MoveCardsToDiscard(scn.GetFreepsCard("toby"), scn.GetFreepsCard("halfling"), scn.GetFreepsCard("flotsam"),
				scn.GetFreepsCard("longbottom"), scn.GetFreepsCard("hornblower"));

		scn.StartGame();

		var site = scn.GetCurrentSite();
		assertInZone(Zone.DECK, sword);
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();

		assertTrue(scn.FreepsActionAvailable(site));
		scn.FreepsUseCardAction(site);

		// Merry is the only unbound Hobbit
		assertEquals(1, scn.GetWoundsOn(merry));
		// The deck is shown for the pipe, and again for the pipeweed; there is nothing to choose either time
		scn.FreepsDismissRevealedCards();
		int shufflesAtTheSecondLook = scn.GetFreepsDeckShuffleCount();
		scn.FreepsDismissRevealedCards();

		assertEquals(0, scn.GetTwilight());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetFreepsDiscardCount());
		// Shuffled, and shuffled again after the second look
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.GetFreepsDeckShuffleCount() > shufflesAtTheSecondLook);

		// The use is spent all the same
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(site));
	}

	@Test
	public void HavingLookedThePlayerMayPlayNeitherAndTheExertionStands() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var bilbospipe = scn.GetFreepsCard("bilbospipe");
		var toby = scn.GetFreepsCard("toby");

		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		var site = scn.GetCurrentSite();
		int shuffles = scn.GetFreepsDeckShuffleCount();
		int deck = scn.GetFreepsDeckCount();

		scn.FreepsUseCardAction(site);
		assertEquals(1, scn.GetWoundsOn(merry));

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(bilbospipe));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();

		scn.FreepsDismissRevealedCards();
		assertTrue(scn.FreepsHasCardChoiceAvailable(toby));
		assertEquals(0, scn.FreepsGetChoiceMin());
		int shufflesAtTheSecondLook = scn.GetFreepsDeckShuffleCount();
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.DECK, bilbospipe, toby);
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(merry));
		// Shuffled, and shuffled again after the second look
		assertTrue(scn.GetFreepsDeckShuffleCount() > shuffles);
		assertTrue(scn.GetFreepsDeckShuffleCount() > shufflesAtTheSecondLook);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnTheSecondPlayersTurnItIsOfferedAgainForHisHobbitAndHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		// The first player uses the site in his own fellowship phase, and plays nothing
		var site = scn.GetCurrentSite();
		scn.FreepsUseCardAction(site);
		scn.FreepsDismissRevealedCards();
		scn.FreepsDeclineChoosing();
		scn.FreepsDismissRevealedCards();
		scn.FreepsDeclineChoosing();
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsActionAvailable(site));

		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();

		// The second player's fellowship starts at the same site. His cards are the ones the rig calls the
		// Shadow player's.
		var hisMerry = scn.GetShadowCard("merry");
		var hisPippin = scn.GetShadowCard("pippin");
		var hisPipe = scn.GetShadowCard("bilbospipe");
		var hisToby = scn.GetShadowCard("toby");
		scn.MoveCompanionsToTable(hisMerry, hisPippin);
		// He drew a hand while he was the Shadow player: these go back on his deck
		scn.MoveCardsToTopOfDeck(hisPipe, hisToby);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertSame(site, scn.GetCurrentSite());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		int hisShuffles = scn.GetShadowDeckShuffleCount();
		int firstPlayersShuffles = scn.GetFreepsDeckShuffleCount();
		int twilight = scn.GetTwilight();

		assertTrue(scn.ShadowActionAvailable(site));
		scn.ShadowUseCardAction(site);

		// His own unbound Hobbits, not the first player's
		assertTrue(scn.ShadowHasCardChoiceAvailable(hisMerry, hisPippin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(merry));
		scn.ShadowChooseCard(hisPippin);
		assertEquals(1, scn.GetWoundsOn(hisPippin));
		assertEquals(0, scn.GetWoundsOn(hisMerry));
		assertEquals(1, scn.GetWoundsOn(merry));

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(hisPipe));
		scn.ShadowChooseCardBPFromSelection(hisPipe);
		scn.ShadowChooseCard(hisMerry);
		assertAttachedTo(hisPipe, hisMerry);

		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(hisToby));
		scn.ShadowChooseCardBPFromSelection(hisToby);
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, hisToby);

		// 1 for Bilbo's Pipe + 1 for Old Toby
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.GetShadowDeckShuffleCount() > hisShuffles);
		assertEquals(firstPlayersShuffles, scn.GetFreepsDeckShuffleCount());

		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertFalse(scn.ShadowActionAvailable(site));
	}
}
