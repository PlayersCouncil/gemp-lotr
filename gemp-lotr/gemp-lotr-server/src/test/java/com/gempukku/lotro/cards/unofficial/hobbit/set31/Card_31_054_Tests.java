package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_054_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  Its forests are The Troll Hoard 30_50 (site 2, also underground) and Mirkwood 30_53 (site 5); Goblin
	// Town 30_52 (site 4) is underground and no forest.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nest", "31_54");
					// Spiders of two cultures.
					put("fat", "31_51");         // Fat Spider: [spider] Spider, cost 3 (-1 for each doubt; none here), site 5; a Regroup ability, not used
					put("fat2", "31_51");
					put("shelob", "8_26");       // Shelob, Last Child of Ungoliant: [gollum] Spider, cost 6, site 8
					// Orcs of two cultures.
					put("veteran", "32_43");     // Orkish Veteran: [sauron] Orc, site 4; archer at site 9 only, a Regroup ability, not used
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc, site 3; an Assignment ability, not used
					put("malevolent", "33_47");  // Malevolent Orc: [sauron] Orc, cost 2; an end-of-Shadow-phase trigger, never in play
					// A [spider] card that is no Spider: a unique condition whose texts do not reach these boards.
					put("river", "31_50");       // Enchanted River
					// A Spider for the other player's own discard pile, of a blueprint the Shadow player's piles do not hold.
					put("lob", "31_52");         // Lazy Lob: [spider] Spider
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SpiderNestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Spider Nest
		 * Unique: False
		 * Side: Shadow
		 * Culture: Spider
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a Spider.<br><b>Assignment:</b> Discard an Orc from play to play a Spider from
		 * your draw deck or discard pile. Its twilight cost is -2 (or -4 at a forest).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nest");

		assertEquals("Spider Nest", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SPIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASpiderIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var fat = scn.GetShadowCard("fat");
		var veteran = scn.GetShadowCard("veteran");
		var river = scn.GetShadowCard("river");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToHand(nest, fat);
		// A [spider] card that is no Spider, and a minion that is no Spider.
		scn.MoveCardsToSupportArea(river);
		scn.MoveMinionsToTable(veteran);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInHand(nest, fat);
		assertInZone(Zone.SUPPORT, river);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(nest));

		scn.ShadowPlayCard(fat);
		assertInZone(Zone.SHADOW_CHARACTERS, fat);

		assertTrue(scn.ShadowPlayAvailable(nest));
		scn.ShadowPlayCard(nest);
		assertInZone(Zone.SUPPORT, nest);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAGollumSpider() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToHand(nest);
		scn.MoveMinionsToTable(shelob);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();
		assertInHand(nest);
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);

		assertTrue(scn.ShadowPlayAvailable(nest));
		scn.ShadowPlayCard(nest);
		assertInZone(Zone.SUPPORT, nest);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignmentDiscardsAnOrcToPlayASpiderFromTheDrawDeckFourCheaperAtAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var fat = scn.GetShadowCard("fat");
		var fat2 = scn.GetShadowCard("fat2");
		var shelob = scn.GetShadowCard("shelob");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var malevolent = scn.GetShadowCard("malevolent");
		var river = scn.GetShadowCard("river");
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		// Orcs of two cultures, and a Spider that is no Orc.
		scn.MoveMinionsToTable(veteran, aggressor, fat);
		// In the deck, a Spider, an Orc and a [spider] card that is no Spider; a Spider in the discard pile, so that
		// the pile is chosen.
		scn.SetShadowDrawDeck(shelob, malevolent, river);
		scn.MoveCardsToDiscard(fat2);
		assertInZone(Zone.SUPPORT, nest);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran, aggressor, fat);
		assertInZone(Zone.DECK, shelob, malevolent, river);
		assertInZone(Zone.DISCARD, fat2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();
		int shuffles = scn.GetShadowDeckShuffleCount();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertTrue(scn.ShadowActionAvailable(nest));
		scn.ShadowUseCardAction(nest);
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(fat));
		scn.ShadowChooseCard(veteran);
		assertInZone(Zone.DISCARD, veteran);

		assertTrue(scn.ShadowChoiceAvailable("draw deck"));
		assertTrue(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(malevolent, river));
		scn.ShadowChooseCardBPFromSelection(shelob);

		assertInZone(Zone.SHADOW_CHARACTERS, shelob, aggressor, fat);
		assertInZone(Zone.DECK, malevolent, river);
		assertInZone(Zone.DISCARD, fat2, veteran);
		// At The Troll Hoard, a forest: 10 - (6 + 2 roaming - 4)
		assertEquals(6, scn.GetTwilight());
		// Shuffled as the card leaves the deck looked through, and again after the play.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void PlaysASpiderFromTheDiscardPileTwoCheaperAwayFromAForest() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var fat = scn.GetShadowCard("fat");
		var fat2 = scn.GetShadowCard("fat2");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		var malevolent = scn.GetShadowCard("malevolent");
		var river = scn.GetShadowCard("river");
		var freepsLob = scn.GetFreepsCard("lob");
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		scn.SkipToSite(3);
		assertInZone(Zone.SUPPORT, nest);
		scn.MoveMinionsToTable(aggressor);
		scn.SetShadowDrawDeck(fat2);
		// Spiders of two cultures in the discard pile, an Orc and a [spider] card that is no Spider beside them, and
		// a Spider in the other player's own discard pile.
		scn.MoveCardsToDiscard(fat, shelob, malevolent, river, freepsLob);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, fat2);
		assertInZone(Zone.DISCARD, fat, shelob, malevolent, river, freepsLob);
		// The fellowship moves on to Goblin Town (site 4), underground and no forest.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowUseCardAction(nest);
		// The one Orc is discarded unasked.
		assertInZone(Zone.DISCARD, aggressor);
		scn.ShadowChoose("discard pile");
		assertTrue(scn.ShadowHasCardChoiceAvailable(fat, shelob));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(malevolent, river, freepsLob));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(shelob);

		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertInZone(Zone.DECK, fat2);
		assertInZone(Zone.DISCARD, fat, malevolent, river, freepsLob, aggressor);
		// 10 - (6 + 2 roaming - 2)
		assertEquals(4, scn.GetTwilight());
		// No draw deck was looked through.
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AtMirkwoodASpiderIsFourCheaperAndExactlyEnoughTwilightPlaysIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var fat2 = scn.GetShadowCard("fat2");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		scn.SkipToSite(4);
		assertInZone(Zone.SUPPORT, nest);
		scn.MoveMinionsToTable(aggressor);
		scn.SetShadowDrawDeck(fat2);
		scn.MoveCardsToDiscard(shelob);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, fat2);
		assertInZone(Zone.DISCARD, shelob);
		// The fellowship moves on to Mirkwood (site 5), a forest that is not underground.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Shelob (site 8) still roams: 6 + 2 - 4.
		scn.SetTwilight(4);
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());

		assertTrue(scn.ShadowActionAvailable(nest));
		scn.ShadowUseCardAction(nest);
		assertInZone(Zone.DISCARD, aggressor);
		scn.ShadowChoose("discard pile");

		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertInZone(Zone.DECK, fat2);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithASpiderOnlyInTheDrawDeckItIsPlayedFromThereUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var fat = scn.GetShadowCard("fat");
		var aggressor = scn.GetShadowCard("aggressor");
		var malevolent = scn.GetShadowCard("malevolent");
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SetShadowDrawDeck(fat);   // lint-ok: fat2, the twin, is set aside by this cheat and is in no pile
		scn.MoveCardsToDiscard(malevolent);
		assertInZone(Zone.SUPPORT, nest);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, fat);
		assertInZone(Zone.DISCARD, malevolent);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(nest);
		assertInZone(Zone.DISCARD, aggressor);
		// No choice of pile: the discard pile holds no Spider.
		assertFalse(scn.ShadowChoiceAvailable("discard pile"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(fat);

		assertInZone(Zone.SHADOW_CHARACTERS, fat);
		// The cost 3 - 4 reads 0 (a twilight cost never reads below 0), and the roaming penalty is paid on top of it:
		// 10 - (0 + 2).
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithASpiderOnlyInTheDiscardPileItIsPlayedFromThereUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		var malevolent = scn.GetShadowCard("malevolent");
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SetShadowDrawDeck(malevolent);
		scn.MoveCardsToDiscard(shelob);
		assertInZone(Zone.SUPPORT, nest);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, malevolent);
		assertInZone(Zone.DISCARD, shelob);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowUseCardAction(nest);
		assertInZone(Zone.DISCARD, aggressor);
		// No choice of pile, the draw deck holding no Spider, and the one Spider there is played unasked.
		assertFalse(scn.ShadowChoiceAvailable("draw deck"));
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertInZone(Zone.DECK, malevolent);
		// 10 - (6 + 2 roaming - 4)
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoOrcInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var fat = scn.GetShadowCard("fat");
		var fat2 = scn.GetShadowCard("fat2");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		scn.MoveMinionsToTable(fat);
		scn.SetShadowDrawDeck(fat2);
		scn.MoveCardsToDiscard(shelob);
		assertInZone(Zone.SUPPORT, nest);
		assertInZone(Zone.SHADOW_CHARACTERS, fat);
		assertInZone(Zone.DECK, fat2);
		assertInZone(Zone.DISCARD, shelob);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nest));
	}

	@Test
	public void IsNotOfferedWithNoSpiderInEitherPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var aggressor = scn.GetShadowCard("aggressor");
		var malevolent = scn.GetShadowCard("malevolent");
		var river = scn.GetShadowCard("river");
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SetShadowDrawDeck(malevolent);
		scn.MoveCardsToDiscard(river);
		assertInZone(Zone.SUPPORT, nest);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, malevolent);
		assertInZone(Zone.DISCARD, river);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nest));
	}

	@Test
	public void IsNotOfferedWithOneTwilightTooFewForTheOnlySpider() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nest = scn.GetShadowCard("nest");
		var shelob = scn.GetShadowCard("shelob");
		var aggressor = scn.GetShadowCard("aggressor");
		var malevolent = scn.GetShadowCard("malevolent");
		scn.MoveCardsToSupportArea(nest);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor);
		scn.SetShadowDrawDeck(malevolent);
		scn.MoveCardsToDiscard(shelob);
		assertInZone(Zone.SUPPORT, nest);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DECK, malevolent);
		assertInZone(Zone.DISCARD, shelob);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Shelob at The Troll Hoard: 6 + 2 roaming - 4 = 4.
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nest));
	}
}
