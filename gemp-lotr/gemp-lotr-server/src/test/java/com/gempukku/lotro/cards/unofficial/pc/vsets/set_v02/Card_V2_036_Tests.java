package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_036_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pit1", "102_36");
					put("pit2", "102_36");

					// The four [isengard] mounts there are.  Each says "Bearer must be a warg-rider."
					put("warg", "5_65");            // Warg: costs 3, strength +3
					put("warwarg", "5_64");         // War-warg: costs 4
					put("sharkuswarg", "5_59");     // Sharku's Warg: costs 5, unique
					put("hound", "102_24");         // Hound of Isengard: costs 2; when played, may take an [isengard] skirmish event from the discard pile, and there is none
					// A mount that is not [isengard], and an [isengard] possession that is not a mount.
					put("steed", "1_208");          // Black Steed: [wraith] mount, costs 1, bearer must be a Nazgul
					put("sword", "1_160");          // Uruk-hai Sword: [isengard] hand weapon, costs 1, bearer must be an Uruk-hai

					put("rider1", "5_67");          // Warg-rider: [isengard] Orc, vitality 2; fierce while not exhausted
					put("rider2", "5_67");
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, vitality 3; Damage +1 is its whole text
					put("forger", "3_56");          // Isengard Forger: [isengard] Orc, vitality 2; "Shadow: Exert this minion to add (1)."
					put("lemenya", "1_232");        // Ulaire Lemenya: a [wraith] Nazgul, vitality 2; his text is about [wraith] conditions
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WolfPitStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Wolf Pit
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: When you play this possession, you may stack two [isengard] mounts here from your draw deck or
		 * discard pile.
		 * Shadow: Exert an [Isengard] minion to play an [Isengard] mount stacked here as if from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pit1");

		assertEquals("Wolf Pit", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilightAndDecliningStacksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit1");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var rider = scn.GetShadowCard("rider1");
		scn.MoveCardsToHand(pit);
		scn.MoveCardsToDiscard(warg);

		scn.StartGame();
		scn.MoveMinionsToTable(rider);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 5 set + 1 companion + 2 for the site.
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.HAND, pit);
		assertTrue(scn.ShadowPlayAvailable(pit));
		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(pit);
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, pit);
		assertNotAttachedTo(pit, rider);

		// A mount in each pile, and the stacking is a "may".
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetStackedCards(pit).size());
		assertInZone(Zone.DISCARD, warg);
		assertInZone(Zone.DECK, warwarg);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StacksTwoIsengardMountsFromTheDrawDeckOnTheCopyJustPlayedAndTheDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var sharkuswarg = scn.GetShadowCard("sharkuswarg");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToSupportArea(pit2);
		scn.MoveCardsToHand(pit1);
		// Three [isengard] mounts stay in the draw deck, with a [wraith] mount and an [isengard] hand weapon.
		// One [isengard] mount in the discard pile, so that there is a pile to choose.
		scn.MoveCardsToDiscard(warg);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();
		assertInZone(Zone.DECK, warwarg, sharkuswarg, hound, steed, sword);

		scn.ShadowPlayCard(pit1);
		// The copy that was played offers, and the copy that was already in play does not.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pit2));
		scn.ShadowChooseOptionalTrigger(pit1);

		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(warwarg, sharkuswarg, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, sword, warg));
		scn.ShadowChooseCardBPFromSelection(warwarg);
		assertInZone(Zone.STACKED, warwarg);
		assertTrue(scn.GetStackedCards(pit1).contains(warwarg));
		assertEquals(shadowShuffles + 1, scn.GetShadowDeckShuffleCount());

		// The second mount, from the draw deck again.
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(sharkuswarg, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warwarg, steed, sword, warg));
		scn.ShadowChooseCardBPFromSelection(hound);
		assertInZone(Zone.STACKED, hound);

		// Two and no more, on the copy that was played; the third mount is left in the deck.
		assertEquals(2, scn.GetStackedCards(pit1).size());
		assertTrue(scn.GetStackedCards(pit1).contains(hound));
		assertEquals(0, scn.GetStackedCards(pit2).size());
		assertInZone(Zone.DECK, sharkuswarg, steed, sword);
		assertInZone(Zone.DISCARD, warg);
		// The deck was looked through twice, once for each mount.
		assertEquals(shadowShuffles + 2, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StacksTwoIsengardMountsFromTheDiscardPileAndNoDeckIsShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit1");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var sharkuswarg = scn.GetShadowCard("sharkuswarg");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(pit);
		// Three [isengard] mounts in the discard pile with a [wraith] mount and an [isengard] hand weapon;
		// Sharku's Warg stays in the draw deck, so that there is a pile to choose.
		scn.MoveCardsToDiscard(warg, warwarg, hound, steed, sword);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		int shadowShuffles = scn.GetShadowDeckShuffleCount();
		int freepsShuffles = scn.GetFreepsDeckShuffleCount();

		scn.ShadowPlayCard(pit);
		scn.ShadowAcceptOptionalTrigger();

		scn.ShadowChoose("discard pile");
		assertTrue(scn.ShadowHasCardChoiceAvailable(warg, warwarg, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, sword, sharkuswarg));
		// Each of the two is a "may" of its own: one at a time, and either may be left.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(warg);
		assertInZone(Zone.STACKED, warg);
		assertTrue(scn.GetStackedCards(pit).contains(warg));

		scn.ShadowChoose("discard pile");
		assertTrue(scn.ShadowHasCardChoiceAvailable(warwarg, hound));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(warg, steed, sword, sharkuswarg));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCardBPFromSelection(hound);

		assertEquals(2, scn.GetStackedCards(pit).size());
		assertTrue(scn.GetStackedCards(pit).contains(hound));
		assertInZone(Zone.DISCARD, warwarg, steed, sword);
		assertInZone(Zone.DECK, sharkuswarg);
		assertEquals(shadowShuffles, scn.GetShadowDeckShuffleCount());
		assertEquals(freepsShuffles, scn.GetFreepsDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OneMountMayComeFromTheDrawDeckAndTheOtherFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit1");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(pit);
		scn.MoveCardsToDiscard(warg, hound);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(pit);
		scn.ShadowAcceptOptionalTrigger();

		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertInZone(Zone.DECK, warwarg);
		scn.ShadowChooseCardBPFromSelection(warwarg);
		assertInZone(Zone.STACKED, warwarg);

		scn.ShadowChoose("discard pile");
		assertTrue(scn.ShadowHasCardChoiceAvailable(warg, hound));
		assertInZone(Zone.DISCARD, warg);
		scn.ShadowChooseCardBPFromSelection(warg);
		assertInZone(Zone.STACKED, warg);

		assertEquals(2, scn.GetStackedCards(pit).size());
		assertTrue(scn.GetStackedCards(pit).contains(warwarg));
		assertTrue(scn.GetStackedCards(pit).contains(warg));
		assertInZone(Zone.DISCARD, hound);
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASingleMountInTheDiscardPileIsStackedAloneWithNoPileToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit1");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var sharkuswarg = scn.GetShadowCard("sharkuswarg");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		// The other three [isengard] mounts are in hand, which is neither pile; the draw deck holds a [wraith]
		// mount and an [isengard] hand weapon and nothing that can be stacked.
		scn.MoveCardsToHand(pit, warwarg, sharkuswarg, hound);
		scn.MoveCardsToDiscard(warg);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		int shuffles = scn.GetShadowDeckShuffleCount();
		assertInZone(Zone.DECK, steed, sword);

		scn.ShadowPlayCard(pit);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pit));
		scn.ShadowAcceptOptionalTrigger();

		// Straight to the discard pile.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(warg));
		scn.ShadowChooseCardBPFromSelection(warg);

		// There is no second mount to stack, and the ones in hand are not offered.
		assertEquals(1, scn.GetStackedCards(pit).size());
		assertTrue(scn.GetStackedCards(pit).contains(warg));
		assertInZone(Zone.HAND, warwarg, sharkuswarg, hound);
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("F-0068: once the draw deck is being looked through a mount must be taken (the choice has a minimum of 1)")
	public void LookingThroughTheDrawDeckDoesNotForceAMountToBeStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit1");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var sharkuswarg = scn.GetShadowCard("sharkuswarg");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(pit);
		scn.MoveCardsToDiscard(warg);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		int shuffles = scn.GetShadowDeckShuffleCount();

		scn.ShadowPlayCard(pit);
		scn.ShadowAcceptOptionalTrigger();

		// The first look: a mount may be left where it is.  One is taken.
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(warwarg, sharkuswarg, hound));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(warwarg);
		assertEquals(1, scn.GetStackedCards(pit).size());

		// The second look: two mounts are there, and neither is taken.
		scn.ShadowChoose("draw deck");
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(sharkuswarg, hound));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowDeclineChoosing();

		assertEquals(1, scn.GetStackedCards(pit).size());
		assertInZone(Zone.DECK, sharkuswarg, hound);
		// Looked through twice, so shuffled twice, whatever was taken.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("F-0069: the stacking is offered with no [isengard] mount in the draw deck or the discard pile, and accepting does nothing")
	public void IsNotOfferedWithNoIsengardMountInTheDrawDeckOrTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit1");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var sharkuswarg = scn.GetShadowCard("sharkuswarg");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		// Every [isengard] mount is in hand.  A [wraith] mount is in the discard pile and an [isengard] hand
		// weapon in the draw deck.
		scn.MoveCardsToHand(pit, warg, warwarg, sharkuswarg, hound);
		scn.MoveCardsToDiscard(steed);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertInZone(Zone.DECK, sword);
		scn.ShadowPlayCard(pit);
		assertInZone(Zone.SUPPORT, pit);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(pit));
		assertEquals(0, scn.GetStackedCards(pit).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	@Ignore("F-0070: with [isengard] mounts in the draw deck only, the discard pile is offered as a choice, and choosing it ends the stacking with nothing stacked")
	public void WithMountsInTheDrawDeckOnlyTheDeckIsLookedThroughWithNoPileToChoose() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit1");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var sharkuswarg = scn.GetShadowCard("sharkuswarg");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		// All four [isengard] mounts are in the draw deck.  The discard pile holds a [wraith] mount and an
		// [isengard] hand weapon and nothing that can be stacked.
		scn.MoveCardsToHand(pit);
		scn.MoveCardsToDiscard(steed, sword);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.FreepsPass();

		scn.ShadowPlayCard(pit);
		scn.ShadowAcceptOptionalTrigger();

		// Straight to the draw deck.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.ShadowHasCardChoiceAvailable(warg, warwarg, sharkuswarg, hound));
		scn.ShadowChooseCardBPFromSelection(warg);

		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		scn.ShadowDismissRevealedCards();
		scn.ShadowChooseCardBPFromSelection(hound);

		assertEquals(2, scn.GetStackedCards(pit).size());
		assertInZone(Zone.DISCARD, steed, sword);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionExertsAChosenIsengardMinionToPlayAChosenIsengardMountStackedHereForItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var warg = scn.GetShadowCard("warg");
		var warwarg = scn.GetShadowCard("warwarg");
		var hound = scn.GetShadowCard("hound");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		var rider1 = scn.GetShadowCard("rider1");
		var rider2 = scn.GetShadowCard("rider2");
		var savage = scn.GetShadowCard("savage");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(pit1, pit2);
		// On this copy: two [isengard] mounts, a [wraith] mount and an [isengard] hand weapon.  The last two
		// each have a bearer on the table and are cheap, so nothing but what they are keeps them out.
		scn.StackCardsOn(pit1, warg, warwarg, steed, sword);
		// On the other copy: an [isengard] mount.
		scn.StackCardsOn(pit2, hound);

		scn.StartGame();
		scn.MoveMinionsToTable(rider1, rider2, savage, lemenya);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 1 companion + 2 for the site.
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(pit1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(5, scn.GetStrength(rider2));

		scn.ShadowUseCardAction(pit1);
		// The exertion: two [isengard] Orcs and an [isengard] Uruk-hai, and not the [wraith] Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider1, rider2, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		// The mount.
		assertTrue(scn.ShadowHasCardChoiceAvailable(warg, warwarg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(steed, sword, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(warg);

		// As if from hand: onto a warg-rider of the player's choosing, for the Warg's own cost of 3.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider1, rider2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, lemenya));
		scn.ShadowChooseCard(rider2);
		assertAttachedTo(warg, rider2);
		assertEquals(5 + 3, scn.GetStrength(rider2));
		assertEquals(13 - 3, scn.GetTwilight());
		assertEquals(3, scn.GetStackedCards(pit1).size());
		assertFalse(scn.GetStackedCards(pit1).contains(warg));
		assertEquals(1, scn.GetStackedCards(pit2).size());
		assertEquals(0, scn.GetWoundsOn(rider1));
		assertEquals(0, scn.GetWoundsOn(rider2));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(pit1));

		// Not in the maneuver phase, with a War-warg still stacked here, a warg-rider to bear it and 10 in the
		// pool.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pit1));
	}

	@Test
	public void ShadowActionIsOfferedOnlyOnceAnIsengardMountStackedHereCanBePaidForAndPlaysNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit1 = scn.GetShadowCard("pit1");
		var pit2 = scn.GetShadowCard("pit2");
		var warwarg = scn.GetShadowCard("warwarg");
		var sharkuswarg = scn.GetShadowCard("sharkuswarg");
		var steed = scn.GetShadowCard("steed");
		var sword = scn.GetShadowCard("sword");
		var rider = scn.GetShadowCard("rider1");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(pit1, pit2);
		// War-warg costs 4 and Sharku's Warg 5.
		scn.StackCardsOn(pit1, warwarg, sharkuswarg);
		// Neither of these is an [isengard] mount; each costs 1 and has a bearer on the table.
		scn.StackCardsOn(pit2, steed, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, savage, forger, lemenya);
		scn.FreepsPass();

		// 1 companion + 2 for the site: one short of the War-warg.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(pit1));
		assertFalse(scn.ShadowActionAvailable(pit2));

		// "Shadow: Exert this minion to add (1)."
		scn.ShadowUseCardAction(forger);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(pit1));
		assertFalse(scn.ShadowActionAvailable(pit2));

		// The Forger is exhausted; the warg-rider or Uruk Savage can exert.
		scn.ShadowUseCardAction(pit1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, lemenya));
		scn.ShadowChooseCard(savage);

		// Only the War-warg can be paid for and only the warg-rider can bear it, so there is nothing to
		// choose: Sharku's Warg stays where it is.
		assertAttachedTo(warwarg, rider);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.STACKED, sharkuswarg);
		assertEquals(1, scn.GetStackedCards(pit1).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedOnceNoIsengardMinionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pit = scn.GetShadowCard("pit1");
		var warg = scn.GetShadowCard("warg");
		var rider = scn.GetShadowCard("rider1");
		var forger = scn.GetShadowCard("forger");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToSupportArea(pit);
		scn.StackCardsOn(pit, warg);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, forger, lemenya);
		// The warg-rider (vitality 2) cannot exert, and can still bear the Warg.  Isengard Forger has one
		// exertion in him; the Nazgul could exert and is not [isengard].
		scn.AddWoundsToChar(rider, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(pit));

		// The Forger spends his exertion on his own ability.
		scn.ShadowUseCardAction(forger);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertFalse(scn.ShadowActionAvailable(pit));
		assertEquals(1, scn.GetStackedCards(pit).size());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
