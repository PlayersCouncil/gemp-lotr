package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flees", "18_132");

					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] Nazgul, cost 4, site 3; his text needs a
					// [wraith] condition on a companion
					put("nazgul", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgul who is [raider]; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] minion who is an Orc, cost 3; its text needs 3 burdens
					put("creeper", "8_75");

					// [wraith] minions of cost 5, an Orc and a Nazgul, and one of cost 8. Their texts are a
					// skirmish ability, a skirmish ability and a strength of his own.
					put("orc", "7_196");        // Morgul Predator
					put("cantea", "1_230");     // Ulaire Cantea, Lieutenant of Dol Guldur
					put("twk", "1_237");        // The Witch-king, Lord of Angmar

					// [wraith] minions of cost 2. Morgul Hound: an Orc, site 4; its text needs 2 wounds on the
					// Ring-bearer. Bill Ferny: a Man, site 2; "Nazgul are not roaming", and his other texts are
					// about assigning him and about underground sites.
					put("hound", "7_192");
					put("ferny", "2_75");
					put("soldier", "7_199");    // Morgul Soldier: a [wraith] Orc of cost 2 ("Nazgul are strength +1")
					put("lackey", "7_193");     // Morgul Lackey: a [wraith] Orc of cost 2; a skirmish ability
					put("whelp", "7_202");      // Morgul Whelp: a [wraith] Orc of cost 1; its text needs a wound on the Ring-bearer
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai of cost 2; Damage +1 is its whole text
					// Drawn to Its Power: a [wraith] support-area condition of cost 2 with nothing to spot; its
					// text answers a companion killed in a Nazgul's skirmish
					put("power", "1_211");

					// Followers. None has a text that is used here: each answers its own transfer, or gives
					// its bearer something.
					put("follower0", "18_47");  // Elendil's Army: [gondor], cost 0
					put("follower1", "18_17");  // Woodhall Elf, Exile: [elven], cost 1
					put("follower2", "15_26");  // Uruviel, Woodland Maid: [elven], cost 2
					put("follower3", "18_7");   // Celebring, Elven-smith: [elven], cost 3
					put("master", "15_134");    // Rohan Stable Master: [rohan], cost 2; bearer is considered mounted
					// Saruman, Servant of Sauron: an [isengard] follower, the Shadow player's own, cost 1
					put("saruman", "17_38");

					put("knight", "5_35");      // Gondorian Knight: a companion of cost 2; Knight is his whole text
					// Ithilien Trap: a Free Peoples support-area condition of cost 2; its texts are used only on
					// purpose
					put("trap", "4_126");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AllLifeFleesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: All Life Flees
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: To play, spot a Nazgûl.
		 * 	Remove (X) to choose one: discard a follower from play that has a twilight cost of X; or play from deck
		 * 	a [wraith] minion that has a twilight cost of X.
		 */

		var scn = GetScenario();

		var flees = scn.GetFreepsCard("flees");

		assertEquals("All Life Flees", flees.getBlueprint().getTitle());
		assertNull(flees.getBlueprint().getSubtitle());
		assertFalse(flees.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, flees.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, flees.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, flees.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(flees, Timeword.SHADOW));
		assertEquals(2, flees.getBlueprint().getTwilightCost());
	}

	@Test
	public void RequiresNazgulToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flees = scn.GetShadowCard("flees");
		var nazgul = scn.GetShadowCard("nazgul");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(flees, nazgul);

		scn.StartGame();
		// A [wraith] minion who is no Nazgul.
		scn.MoveMinionsToTable(creeper);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(nazgul));
		assertFalse(scn.ShadowPlayAvailable(flees));

		scn.ShadowPlayCard(nazgul);
		assertInZone(Zone.SHADOW_CHARACTERS, nazgul);
		assertTrue(scn.ShadowPlayAvailable(flees));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanDiscardFollowerCostingX() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flees = scn.GetShadowCard("flees");
		var nazgul = scn.GetShadowCard("nazgul");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(flees);
		scn.MoveCardsToSupportArea(saruman);

		var knight = scn.GetFreepsCard("knight");
		var trap = scn.GetFreepsCard("trap");
		var follower0 = scn.GetFreepsCard("follower0");
		var follower1 = scn.GetFreepsCard("follower1");
		var follower2 = scn.GetFreepsCard("follower2");
		var follower3 = scn.GetFreepsCard("follower3");
		var master = scn.GetFreepsCard("master");
		scn.MoveCompanionsToTable(knight);
		// Followers of cost 0, 1 and 3 and one of cost 2 in the support area, beside a condition of cost 2;
		// another follower of cost 2 on a companion of cost 2.
		scn.MoveCardsToSupportArea(follower0, follower1, master, follower3, trap);
		scn.AttachCardsTo(knight, follower2);

		scn.StartGame();
		scn.MoveMinionsToTable(nazgul);
		scn.FreepsPass();
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowPlayAvailable(flees));

		scn.ShadowPlayCard(flees);
		// The event's own 2 is paid, and X may be anything the pool still holds.
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChoose("2");
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Discard a follower");

		// The two followers of cost 2, of two cultures, one of them borne.
		assertTrue(scn.ShadowHasCardChoiceAvailable(follower2, master));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(follower0, follower1, follower3, saruman, trap, knight));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(follower2);
		assertInZone(Zone.DISCARD, follower2);
		assertInZone(Zone.SUPPORT, master);
		assertInZone(Zone.SUPPORT, follower1);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, flees);
		// No draw deck was looked through.
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFollowerOfEitherPlayerThatCostsOneIsDiscardedForXOfOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flees = scn.GetShadowCard("flees");
		var khamul = scn.GetShadowCard("khamul");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(flees);
		scn.MoveCardsToSupportArea(saruman);

		var follower1 = scn.GetFreepsCard("follower1");
		var follower2 = scn.GetFreepsCard("follower2");
		scn.MoveCardsToSupportArea(follower1, follower2);

		scn.StartGame();
		// The Nazgul spotted is not a [wraith] card.
		scn.MoveMinionsToTable(khamul);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(flees));
		scn.ShadowPlayCard(flees);
		assertEquals(3, scn.GetTwilight());

		scn.ShadowChoose("1");
		assertEquals(2, scn.GetTwilight());
		scn.ShadowChooseOption("Discard a follower");

		// The Shadow player's own follower and the Free Peoples player's, each of cost 1.
		assertTrue(scn.ShadowHasCardChoiceAvailable(saruman, follower1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(follower2));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(saruman);
		assertInZone(Zone.DISCARD, saruman);
		assertInZone(Zone.SUPPORT, follower1);
		assertInZone(Zone.SUPPORT, follower2);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanDiscardFollowerCosting0() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flees = scn.GetShadowCard("flees");
		var nazgul = scn.GetShadowCard("nazgul");
		scn.MoveCardsToHand(flees);

		var follower0 = scn.GetFreepsCard("follower0");
		var follower1 = scn.GetFreepsCard("follower1");
		var follower2 = scn.GetFreepsCard("follower2");
		var follower3 = scn.GetFreepsCard("follower3");
		scn.MoveCardsToSupportArea(follower0, follower1, follower2, follower3);

		scn.StartGame();
		scn.MoveMinionsToTable(nazgul);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(flees);
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChoose("0");
		assertEquals(1, scn.GetTwilight());
		scn.ShadowChooseOption("Discard a follower");

		// The one follower that costs nothing is discarded without a choice.
		assertInZone(Zone.DISCARD, follower0);
		assertInZone(Zone.SUPPORT, follower1);
		assertInZone(Zone.SUPPORT, follower2);
		assertInZone(Zone.SUPPORT, follower3);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysAWraithMinionThatCostsXFromTheDrawDeckAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flees = scn.GetShadowCard("flees");
		var nazgul = scn.GetShadowCard("nazgul");
		var hound = scn.GetShadowCard("hound");
		var ferny = scn.GetShadowCard("ferny");
		var whelp = scn.GetShadowCard("whelp");
		var creeper = scn.GetShadowCard("creeper");
		var savage = scn.GetShadowCard("savage");
		var power = scn.GetShadowCard("power");
		var soldier = scn.GetShadowCard("soldier");
		var lackey = scn.GetShadowCard("lackey");
		// [wraith] minions of cost 2 that are not in the draw deck: one in hand, one in the discard pile.
		scn.MoveCardsToHand(flees, soldier);
		scn.MoveCardsToDiscard(lackey);

		var follower2 = scn.GetFreepsCard("follower2");
		scn.MoveCardsToSupportArea(follower2);

		scn.StartGame();
		scn.MoveMinionsToTable(nazgul);
		scn.FreepsPass();
		scn.SetTwilight(12);
		scn.ShadowRefreshDecision();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(flees);
		assertEquals(10, scn.GetTwilight());
		scn.ShadowChoose("2");
		assertEquals(8, scn.GetTwilight());

		// A follower of cost 2 is in play, so there are two things to choose between.
		assertTrue(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Play a");

		scn.ShadowDismissRevealedCards();
		// A [wraith] Orc and a [wraith] Man of cost 2. Not a [wraith] minion of cost 1 or 3, an [isengard]
		// minion of cost 2, a [wraith] condition of cost 2, nor the [wraith] minions of cost 2 in the hand and
		// the discard pile. The player need not play one.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, ferny));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(whelp, creeper, savage, power, soldier, lackey));
		assertEquals(0, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCardBPFromSelection(hound);
		// The minion is paid for as any minion played: 2 to play + 2 roaming (site 4 minion at site 2).
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, hound);
		assertInZone(Zone.DECK, ferny);
		assertInZone(Zone.SUPPORT, follower2);
		assertInZone(Zone.DISCARD, flees);
		assertTrue(scn.GetShadowDeckShuffleCount() > shadowShuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanPlayWraithMinionFromDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flees = scn.GetShadowCard("flees");
		var nazgul = scn.GetShadowCard("nazgul");
		var orc = scn.GetShadowCard("orc");
		var cantea = scn.GetShadowCard("cantea");
		var twk = scn.GetShadowCard("twk");
		scn.MoveCardsToHand(flees);

		// Followers of cost 0 to 3: none of cost 5.
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("follower0"), scn.GetFreepsCard("follower1"),
				scn.GetFreepsCard("follower2"), scn.GetFreepsCard("follower3"));

		scn.StartGame();
		scn.MoveMinionsToTable(nazgul);
		scn.FreepsPass();
		scn.SetTwilight(22);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(flees);
		assertEquals(20, scn.GetTwilight());
		assertInZone(Zone.DECK, orc);
		assertInZone(Zone.DECK, cantea);

		// With no follower of cost 5 to discard there is nothing to choose between: the deck is looked through.
		scn.ShadowChoose("5");
		assertEquals(15, scn.GetTwilight());
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowDismissRevealedCards();

		// A [wraith] Orc and a [wraith] Nazgul of cost 5, and not The Witch-king at 8.
		assertTrue(scn.ShadowHasCardChoiceAvailable(orc, cantea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(twk));
		assertEquals(2, scn.ShadowGetSelectableCount());
		scn.ShadowChooseCardBPFromSelection(cantea);

		// 5 to play + 2 roaming (site 3 minion at site 2)
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, cantea);
		assertInZone(Zone.DECK, orc);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTooLittleLeftForTheMinionTheDeckIsStillLookedThroughAndShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var flees = scn.GetShadowCard("flees");
		var nazgul = scn.GetShadowCard("nazgul");
		var creeper = scn.GetShadowCard("creeper");
		scn.MoveCardsToHand(flees);

		scn.StartGame();
		scn.MoveMinionsToTable(nazgul);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		int shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(flees);
		// Everything left goes: a [wraith] minion of cost 3 lies in the deck and there is nothing to pay for it.
		scn.ShadowChoose("3");
		assertEquals(0, scn.GetTwilight());

		// The deck is shown all the same, and nothing in it can be chosen.
		scn.ShadowDismissRevealedCards();
		assertInZone(Zone.DECK, creeper);
		assertInZone(Zone.DISCARD, flees);
		assertTrue(scn.GetShadowDeckShuffleCount() > shadowShuffles);
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
