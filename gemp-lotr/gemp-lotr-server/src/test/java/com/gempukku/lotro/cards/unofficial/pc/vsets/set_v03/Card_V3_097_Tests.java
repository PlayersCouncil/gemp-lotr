package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_097_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sky", "103_97");
					put("sky2", "103_97");
					put("sky3", "103_97");
					put("sky4", "103_97");
					put("fear", "103_64");        // Cover of Darkness, Omen of Fear: a [ringwraith] twilight condition, cost 2; to play, hinder 2 twilight conditions
					put("flames", "10_85");       // Flames Within: a [sauron] condition that is not twilight, cost 0, nothing needed to play it
					put("hollowing", "3_54");     // Hollowing of Isengard: an [isengard] condition that is not twilight
					put("armory", "1_173");       // Goblin Armory: a [moria] condition, cost 0, nothing needed to play it
					put("armory2", "1_173");
					put("oath", "17_66");         // Wildman's Oath: a Shadow possession
					put("stairs", "1_24");        // Stairs of Khazad-dum: a Free Peoples condition

					put("soldier", "1_271");      // Orc Soldier: a [sauron] Orc, cost 2, site 6
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc, cost 6, site 4, no text
					put("cavetroll", "1_165");    // Cave Troll of Moria: a [moria] Troll, cost 10, site 4; to play, spot a [moria] Orc
					put("sentry", "10_84");       // Cirith Ungol Sentry: a [sauron] Uruk-hai, cost 2, site 6
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OminousSkyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ominous Sky
		 * Unique: 3
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Twilight. Each time you play an Orc or Troll, add (1) (limit once per phase).
		 * 	<b>Shadow</b> <i>or</i> <b>Regroup</b>: Remove (2) to choose a Shadow condition from your discard pile. Shuffle it into your draw deck (or you may play it if it is twilight).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sky");

		assertEquals("Ominous Sky", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(3, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		scn.MoveCardsToHand(sky);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(sky));

		scn.ShadowPlayCard(sky);

		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, sky);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFirstOrcOrTrollPlayedInEachPhaseAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");
		var cavetroll = scn.GetShadowCard("cavetroll");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToHand(sentry, troop, soldier, cavetroll);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPass();

		// 40 set + 1 companion + 2 for the site
		assertEquals(43, scn.GetTwilight());

		// A [sauron] minion that is an Uruk-hai: 2 + 2 roaming, and nothing added.
		scn.ShadowPlayCard(sentry);
		assertEquals(39, scn.GetTwilight());

		// A [moria] Orc: 6 + 2 roaming, and 1 added.
		scn.ShadowPlayCard(troop);
		assertEquals(32, scn.GetTwilight());
		assertFalse(scn.IsHindered(sky));

		// A second Orc in the same phase: 2 + 2 roaming, and nothing added.
		scn.ShadowPlayCard(soldier);
		assertEquals(28, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves again: a second Shadow phase in the same turn, at site 3.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		var pool = scn.GetTwilight();

		// A [moria] Troll: 10 + 2 roaming, and 1 added.
		scn.ShadowPlayCard(cavetroll);
		assertEquals(pool - 12 + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachCopyAddsItsOwnOneForTheFirstOrcOfThePhaseAndNothingForTheSecond() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(sky, sky2, sky3);
		// The third copy is hindered: its text is not active, and it adds nothing.
		scn.HinderCard(sky3);
		scn.MoveCardsToHand(soldier, troop);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertEquals(23, scn.GetTwilight());

		// A [sauron] Orc: 2 + 2 roaming, and 1 added by each of the two copies that are not hindered.
		scn.ShadowPlayCard(soldier);
		assertEquals(21, scn.GetTwilight());

		// A second Orc: 6 + 2 roaming, and nothing from either.
		scn.ShadowPlayCard(troop);
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionRemovesTwoAndShufflesAChosenShadowConditionThatIsNotTwilightIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		var fear = scn.GetShadowCard("fear");
		var flames = scn.GetShadowCard("flames");
		var hollowing = scn.GetShadowCard("hollowing");
		var stairs = scn.GetShadowCard("stairs");
		var oath = scn.GetShadowCard("oath");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(sky2, fear, flames, hollowing, stairs, oath);
		// A Shadow condition in hand, and its other copy in the other player's discard pile.
		scn.MoveCardsToHand(armory);
		scn.MoveCardsToDiscard(scn.GetFreepsCard("armory"));

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		var freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(sky));

		scn.ShadowUseCardAction(sky);

		assertEquals(8, scn.GetTwilight());
		// Shadow conditions of three cultures, twilight and not. Not offered: a Free Peoples condition and a
		// Shadow possession in the same pile, and a Shadow condition that is somewhere else.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sky2, fear, flames, hollowing));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(stairs, oath, armory));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		// Flames Within is [sauron] and could be played for nothing, but is not twilight: no question is asked.
		scn.ShadowChooseCardBPFromSelection(flames);

		assertInZone(Zone.DECK, flames);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertInDiscard(sky2, fear, hollowing, stairs, oath);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ATwilightConditionChosenMayBePlayedInsteadForItsOwnCostOnTopOfTheTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(sky2);

		scn.StartGame();
		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 set + 1 companion + 2 for the site: exactly the 2 and the 2 the second Sky costs
		assertEquals(4, scn.GetTwilight());
		var shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowUseCardAction(sky);

		// The only Shadow condition in the pile is chosen without asking.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Would you like to play"));
		scn.ShadowChooseYes();

		assertInZone(Zone.SUPPORT, sky2);
		assertEquals(0, scn.GetTwilight());
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingTheTwilightConditionChosenPlaysThatCardAndAsksForNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		var flames = scn.GetShadowCard("flames");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(sky2, flames, armory);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(sky);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sky2, flames, armory));
		scn.ShadowChooseCardBPFromSelection(sky2);

		assertTrue(scn.ShadowDecisionAvailable("Would you like to play"));
		scn.ShadowChooseYes();

		// "Play it": the Sky that was chosen, and nothing else is asked.
		assertInZone(Zone.SUPPORT, sky2);
		assertInDiscard(flames, armory);
		// 10 - 2 - 2 for the Sky
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningToPlayATwilightConditionShufflesItIntoTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		var flames = scn.GetShadowCard("flames");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(sky2, flames);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		var shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowUseCardAction(sky);
		scn.ShadowChooseCardBPFromSelection(sky2);

		assertTrue(scn.ShadowDecisionAvailable("Would you like to play"));
		assertInDiscard(sky2);
		scn.ShadowChooseNo();

		assertInZone(Zone.DECK, sky2);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertInDiscard(flames);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupActionPlaysATwilightConditionOfAnotherCultureForAllItCostsAndIsNoManeuverAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky3 = scn.GetShadowCard("sky3");
		var fear = scn.GetShadowCard("fear");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToSupportArea(sky, sky3);
		scn.MoveCardsToDiscard(fear);

		scn.StartGame();
		// A minion, so that there is a maneuver phase.
		scn.MoveMinionsToTable(soldier);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sky));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		// 10 set + 1 companion + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(sky));

		scn.ShadowUseCardAction(sky);

		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Would you like to play"));
		scn.ShadowChooseYes();

		// Cover of Darkness costs 2 and "To play, hinder 2 twilight conditions": the two Skies, without a choice.
		assertInZone(Zone.SUPPORT, fear);
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.IsHindered(sky));
		assertTrue(scn.IsHindered(sky3));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedWithOneInThePoolAndIsWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var flames = scn.GetShadowCard("flames");
		var armory = scn.GetShadowCard("armory");
		var armory2 = scn.GetShadowCard("armory2");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(flames);
		scn.MoveCardsToHand(armory, armory2);

		scn.StartGame();
		scn.FreepsPass();

		// Each Goblin Armory is played for nothing, to get a new decision with the pool as it has been set.
		scn.SetTwilight(1);
		scn.ShadowPlayCard(armory);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(sky));

		scn.SetTwilight(2);
		scn.ShadowPlayCard(armory2);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(sky));

		scn.ShadowUseCardAction(sky);

		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DECK, flames);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ATwilightConditionThePoolCannotPayForAfterTheTwoIsShuffledInWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		scn.MoveCardsToSupportArea(sky);
		scn.MoveCardsToDiscard(sky2);

		scn.StartGame();
		scn.SetTwilight(0);
		scn.FreepsPass();

		// 1 companion + 2 for the site: the 2, and 1 left where the second Sky costs 2
		assertEquals(3, scn.GetTwilight());
		var shadowShuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowUseCardAction(sky);

		assertFalse(scn.ShadowDecisionAvailable("Would you like to play"));
		assertInZone(Zone.DECK, sky2);
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFourthOminousSkyCannotBePlayedAndIsShuffledInWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var sky2 = scn.GetShadowCard("sky2");
		var sky3 = scn.GetShadowCard("sky3");
		var sky4 = scn.GetShadowCard("sky4");
		scn.MoveCardsToSupportArea(sky, sky2, sky3);
		scn.MoveCardsToDiscard(sky4);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());

		scn.ShadowUseCardAction(sky);

		// Three copies are in play, which is as many as there may be.
		assertFalse(scn.ShadowDecisionAvailable("Would you like to play"));
		assertInZone(Zone.DECK, sky4);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoShadowConditionInTheDiscardPileItIsStillOfferedAndOnlyRemovesTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sky = scn.GetShadowCard("sky");
		var stairs = scn.GetShadowCard("stairs");
		var oath = scn.GetShadowCard("oath");
		scn.MoveCardsToSupportArea(sky);
		// A Free Peoples condition and a Shadow possession.
		scn.MoveCardsToDiscard(stairs, oath);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		var shadowShuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowActionAvailable(sky));

		scn.ShadowUseCardAction(sky);

		assertEquals(8, scn.GetTwilight());
		assertInDiscard(stairs, oath);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
