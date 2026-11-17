package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_164_Tests
{

	// The Fellowship path, with Rivendell Waterfall 1_342 (Forest, Sanctuary) as site 3 and Balin's Tomb 1_343
	// (Underground only) at site 4 as it always is. Three Shadows-block sites are laid as site 4 by
	// MoveCardToAdventurePath where a test needs them; sites 7 to 9 are never reached.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("echo", "12_164");
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] Nazgûl, site 3; her text reaches bearers of [wraith] conditions only
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, fierce; the rest needs 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl; nothing below 3 burdens
					put("steed", "1_208");      // Black Steed: [wraith] mount, cost 1, bearer must be a Nazgûl; strength +2 at a plains, discarded at an underground site
					put("mordor", "12_172");    // Steed of Mordor: [wraith] mount, cost 2, bearer must be a Nazgûl; muster, and a draw when he wins
					put("blade", "12_168");     // Nazgûl Blade: [wraith] hand weapon, cost 1, bearer must be a Nazgûl: a possession that is no mount
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site3", "1_342");      // Rivendell Waterfall: Forest, Sanctuary; a ranger there raises the move limit, which no test reads
					remove("site7");
					remove("site8");
					remove("site9");
					put("woody", "11_266");     // Woody-End: Forest only; a Shadow ability (remove a burden) used only on purpose
					put("slag", "11_258");      // Slag Mounds: Battleground only; adds a burden as a companion is killed
					put("pelennor", "12_190");  // Northern Pelennor: Battleground, Plains; a Shadow ability about lurkers
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EchoofHoovesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Echo of Hooves
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Toil 2</b>. <helper>(For each [wraith] character you exert when playing this, its twilight cost is -2.)</helper><br>Spot your Nazgûl at a battleground or forest site to play a mount on him from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("echo");

		assertEquals("Echo of Hooves", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(card, Keyword.TOIL));
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtAForestEitherOfYourNazgulMayBeGivenAMountFromYourDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echo = scn.GetShadowCard("echo");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var steed = scn.GetShadowCard("steed");
		var mordor = scn.GetShadowCard("mordor");
		var blade = scn.GetShadowCard("blade");
		var woody = scn.GetShadowSite("woody");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(woody);
		scn.MoveCardsToTopOfDeck(steed, mordor, blade);
		scn.MoveCardsToHand(echo);
		scn.MoveMinionsToTable(creeper);
		assertInZone(Zone.DECK, steed, mordor, blade);
		assertInHand(echo);
		scn.SetTwilight(20);

		scn.FreepsPass();
		assertEquals(woody, scn.GetCurrentSite());
		// At a forest, with a pool that pays, but no Nazgûl: the Creeper is a [wraith] Orc.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(echo));

		scn.MoveMinionsToTable(lemenya, khamul);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(echo));
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		scn.ShadowPlayCard(echo);
		// Toil: the pool pays in full, so the [wraith] characters are offered to exert, and nobody is.
		scn.ShadowDeclineChoosing();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(creeper));

		// The deck is shown, then either mount may be chosen, and not the Blade, which is no mount.
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(steed, mordor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(blade));
		scn.ShadowChooseCardBPFromSelection(mordor);
		// Either Nazgûl may bear it, the [raider] one as well; not the Creeper.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(creeper));
		scn.ShadowChooseCard(khamul);

		assertAttachedTo(mordor, khamul);
		assertNotAttachedTo(mordor, lemenya);
		assertInZone(Zone.DECK, steed, blade);
		assertInDiscard(echo);
		// The deck looked through is shuffled: once as the mount leaves it, once as the look is over.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtAForestThatIsASanctuaryYourNazgulIsGivenAMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echo = scn.GetShadowCard("echo");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var blade = scn.GetShadowCard("blade");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToTopOfDeck(steed, blade);
		scn.MoveCardsToHand(echo);
		scn.MoveMinionsToTable(lemenya);
		assertInZone(Zone.DECK, steed, blade);
		scn.SetTwilight(20);

		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("Rivendell Waterfall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.ShadowPlayAvailable(echo));
		scn.ShadowPlayCard(echo);
		scn.ShadowDeclineChoosing();
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(steed);

		// One Nazgûl: the steed goes on her with nothing asked.
		assertAttachedTo(steed, lemenya);
		assertInZone(Zone.DECK, blade);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtABattlegroundYourNazgulIsGivenAMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echo = scn.GetShadowCard("echo");
		var khamul = scn.GetShadowCard("khamul");
		var steed = scn.GetShadowCard("steed");
		var blade = scn.GetShadowCard("blade");
		var slag = scn.GetShadowSite("slag");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(slag);
		scn.MoveCardsToTopOfDeck(steed, blade);
		scn.MoveCardsToHand(echo);
		scn.MoveMinionsToTable(khamul);
		assertInZone(Zone.DECK, steed, blade);
		scn.SetTwilight(20);

		scn.FreepsPass();
		assertEquals(slag, scn.GetCurrentSite());
		assertTrue(scn.ShadowPlayAvailable(echo));
		// Khamûl is no [wraith] character: there is nobody to exert for toil, and nothing is asked for it.
		scn.ShadowPlayCard(echo);
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(steed);

		assertAttachedTo(steed, khamul);
		assertInZone(Zone.DECK, blade);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtABattlegroundThatIsPlainsYourNazgulIsGivenAMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echo = scn.GetShadowCard("echo");
		var lemenya = scn.GetShadowCard("lemenya");
		var mordor = scn.GetShadowCard("mordor");
		var blade = scn.GetShadowCard("blade");
		var pelennor = scn.GetShadowSite("pelennor");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(pelennor);
		scn.MoveCardsToTopOfDeck(mordor, blade);
		scn.MoveCardsToHand(echo);
		scn.MoveMinionsToTable(lemenya);
		assertInZone(Zone.DECK, mordor, blade);
		scn.SetTwilight(20);

		scn.FreepsPass();
		assertEquals(pelennor, scn.GetCurrentSite());
		assertTrue(scn.ShadowPlayAvailable(echo));
		scn.ShadowPlayCard(echo);
		scn.ShadowDeclineChoosing();
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(mordor);

		assertAttachedTo(mordor, lemenya);
		assertInZone(Zone.DECK, blade);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtASiteThatIsNeitherBattlegroundNorForestItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echo = scn.GetShadowCard("echo");
		var lemenya = scn.GetShadowCard("lemenya");
		var mordor = scn.GetShadowCard("mordor");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToTopOfDeck(mordor);
		scn.MoveCardsToHand(echo);
		scn.MoveMinionsToTable(lemenya);
		assertInZone(Zone.DECK, mordor);
		assertInHand(echo);
		scn.SetTwilight(20);

		scn.FreepsPass();
		// Balin's Tomb: underground only.  A Nazgûl, a mount in the deck and a pool that pays.
		assertEquals("Balin's Tomb", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(echo));
	}

	@Test
	public void WithNoMountInTheDrawDeckItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var echo = scn.GetShadowCard("echo");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var mordor = scn.GetShadowCard("mordor");
		var blade = scn.GetShadowCard("blade");
		var woody = scn.GetShadowSite("woody");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(woody);
		// Every mount the deck could hold is in the discard pile; the Blade is in the deck.
		scn.MoveCardsToDiscard(steed, mordor);
		scn.MoveCardsToTopOfDeck(blade);
		scn.MoveCardsToHand(echo);
		scn.MoveMinionsToTable(lemenya);
		assertInDiscard(steed, mordor);
		assertInZone(Zone.DECK, blade);
		scn.SetTwilight(20);

		scn.FreepsPass();
		assertEquals(woody, scn.GetCurrentSite());
		// No penalty for finding nothing in the deck (CR 4.2 "draw deck"): the event is played.
		assertTrue(scn.ShadowPlayAvailable(echo));
		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		scn.ShadowPlayCard(echo);
		scn.ShadowDeclineChoosing();
		// The deck is shown and nothing in it can be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(blade, false));
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DECK, blade);
		assertEquals(0, scn.GetAttachedCards(lemenya).size());
		assertInDiscard(echo);
		// The look alone shuffles the deck once.
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
