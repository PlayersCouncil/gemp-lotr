package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_091_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "17_91");
					put("warg2", "17_91");

					// [orc] mounts and other possessions, for the hand and the draw deck
					put("vicious", "17_92");      // Vicious Warg: [orc] mount, on an [orc] Orc of strength 9 or less; bearer is fierce
					put("gear", "17_90");         // Rider's Gear: an [orc] possession (armor) that is no mount, on an [orc] Orc
					put("gothmog", "10_89");      // Gothmog's Warg: a [sauron] mount, on a [sauron] Orc; strength +1 per site controlled

					// Minions
					put("blackpit", "11_117");    // Denizen of the Black Pit: [orc] Orc, strength 10 (+2 at an underground site)
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, strength 9 (+2 at an underground site)
					put("mountainorc", "11_129"); // Mountain Orc: [orc] Orc, strength 9; its trigger is on being assigned
					put("goblin", "11_137");      // Scurrying Goblin: [orc] Orc, strength 5; its trigger is on being played
					put("dreg", "12_92");         // Orc Dreg: [orc] Orc, strength 10; its trigger is on being assigned
					put("strategist", "12_97");   // Orc Strategist: [orc] Orc, strength 11; lurker, a maneuver ability
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8
					put("runner", "1_178");       // Goblin Runner: a [moria] Orc, strength 5; its trigger is on being played
					put("soldier", "1_271");      // Orc Soldier: a [sauron] Orc, strength 7; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThreateningWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Threatening Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +2
		 * Vitality: +1
		 * Game Text: Bearer must be an [orc] Orc with strength 10 or less. Bearer is <b>fierce</b>.<br>When you play this, you may spot 2 [orc] Orcs that are not bearing mounts to play an [orc] mount from your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("Threatening Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlayedOnlyOnAnOrcOrcOfStrength10OrLessWhoIsThenFierceAndTheDeclinedOfferLeavesTheDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var vicious = scn.GetShadowCard("vicious");
		var dreg = scn.GetShadowCard("dreg");
		var mountainorc = scn.GetShadowCard("mountainorc");
		var strategist = scn.GetShadowCard("strategist");
		var blackpit = scn.GetShadowCard("blackpit");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(warg);
		scn.SetTwilight(10);
		// The fellowship moves to Balin's Tomb, site 4: an underground site.
		scn.FreepsPass();
		scn.MoveMinionsToTable(dreg, mountainorc, strategist, blackpit, halftroll, runner);
		scn.SetShadowDrawDeck(vicious);
		assertInZone(Zone.DECK, vicious);
		scn.ShadowRefreshDecision();
		assertEquals(4, scn.GetCurrentSiteNumber());

		assertEquals(10, scn.GetStrength(dreg));
		assertEquals(9, scn.GetStrength(mountainorc));
		assertEquals(11, scn.GetStrength(strategist));
		// Printed 10, but 12 here: it is the strength in play that is read.
		assertEquals(12, scn.GetStrength(blackpit));
		assertFalse(scn.HasKeyword(dreg, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(mountainorc, Keyword.FIERCE));

		assertTrue(scn.ShadowPlayAvailable(warg));
		scn.ShadowPlayCard(warg);
		// [orc] Orcs of two titles at 10 or less are offered; an [orc] Orc of 11 or of 12, an [orc] minion who
		// is no Orc and an Orc who is not [orc] are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(dreg, mountainorc));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(strategist, blackpit, halftroll, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(dreg);

		assertAttachedTo(warg, dreg);
		assertTrue(scn.HasKeyword(dreg, Keyword.FIERCE));
		// Another [orc] Orc who does not bear it is not made fierce.
		assertFalse(scn.HasKeyword(mountainorc, Keyword.FIERCE));

		// Mountain Orc, Orc Strategist and Denizen of the Black Pit bear no mount: the offer is made.
		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DECK, vicious);
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(shuffles, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SpottingTwoOrcOrcsWithNoMountItPlaysAnOrcMountFromTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		var vicious = scn.GetShadowCard("vicious");
		var gear = scn.GetShadowCard("gear");
		var gothmog = scn.GetShadowCard("gothmog");
		var blackpit = scn.GetShadowCard("blackpit");
		var denizen = scn.GetShadowCard("denizen");
		var mountainorc = scn.GetShadowCard("mountainorc");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		// Orc Soldier is a [sauron] Orc, so Gothmog's Warg could be played: only its culture keeps it out.
		scn.MoveMinionsToTable(blackpit, denizen, mountainorc, soldier);
		scn.SetShadowDrawDeck(vicious, warg2, gear, gothmog);
		assertInZone(Zone.DECK, vicious, warg2, gear, gothmog);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(warg);
		scn.ShadowChooseCard(blackpit);
		assertAttachedTo(warg, blackpit);

		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();
		// The whole deck is shown first.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();
		// [orc] mounts of two titles may be chosen; an [orc] possession that is no mount and a mount of
		// another culture may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(vicious, warg2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gear, gothmog));
		scn.ShadowChooseCardBPFromSelection(vicious);

		// Vicious Warg goes on an [orc] Orc of strength 9 or less: the bearer is chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, mountainorc));
		scn.ShadowChooseCard(denizen);

		assertAttachedTo(vicious, denizen);
		assertInZone(Zone.DECK, warg2, gear, gothmog);
		assertEquals(3, scn.GetShadowDeckCount());
		// One shuffle as the card leaves the deck that was looked through, and the look's own after the play.
		assertEquals(shuffles + 2, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneOrcOrcBesideTheBearerThatBearsNoMountTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var vicious = scn.GetShadowCard("vicious");
		var blackpit = scn.GetShadowCard("blackpit");
		var denizen = scn.GetShadowCard("denizen");
		var goblin = scn.GetShadowCard("goblin");
		var halftroll = scn.GetShadowCard("halftroll");
		var soldier = scn.GetShadowCard("soldier");
		var warg2 = scn.GetShadowCard("warg2");
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		// Denizen of Khazad-dum is the one [orc] Orc bearing no mount.  Beside him: an [orc] Orc bearing a mount,
		// an [orc] minion who is no Orc and an Orc who is not [orc].
		scn.MoveMinionsToTable(blackpit, denizen, goblin, halftroll, soldier);
		scn.AttachCardsTo(goblin, vicious);
		assertAttachedTo(vicious, goblin);
		// An [orc] mount in the deck that Denizen of Khazad-dum could bear: only the spot keeps the offer back.
		scn.SetShadowDrawDeck(warg2);
		assertInZone(Zone.DECK, warg2);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(warg);
		scn.ShadowChooseCard(blackpit);
		assertAttachedTo(warg, blackpit);

		// Its own bearer now bears a mount, so only one [orc] Orc can be spotted.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherMountPlayedAfterItIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		var blackpit = scn.GetShadowCard("blackpit");
		var denizen = scn.GetShadowCard("denizen");
		var mountainorc = scn.GetShadowCard("mountainorc");
		var goblin = scn.GetShadowCard("goblin");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCardsToHand(warg2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(blackpit, denizen, mountainorc, goblin);
		scn.AttachCardsTo(blackpit, warg);
		assertAttachedTo(warg, blackpit);
		scn.SetShadowDrawDeck(vicious);
		assertInZone(Zone.DECK, vicious);
		scn.ShadowRefreshDecision();

		// A second Threatening Warg, played on Denizen of Khazad-dum, leaves two [orc] Orcs with no mount.
		scn.ShadowPlayCard(warg2);
		scn.ShadowChooseCard(denizen);
		assertAttachedTo(warg2, denizen);

		// The copy just played makes its own offer; the one already in play does not answer that play.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.DECK, vicious);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOrcMountInTheDrawDeckTheOfferIsTakenForNothingAndTheDeckShuffled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var gear = scn.GetShadowCard("gear");
		var gothmog = scn.GetShadowCard("gothmog");
		var blackpit = scn.GetShadowCard("blackpit");
		var denizen = scn.GetShadowCard("denizen");
		var mountainorc = scn.GetShadowCard("mountainorc");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(blackpit, denizen, mountainorc, soldier);
		scn.SetShadowDrawDeck(gear, gothmog);
		assertInZone(Zone.DECK, gear, gothmog);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(warg);
		scn.ShadowChooseCard(blackpit);
		assertAttachedTo(warg, blackpit);

		int shuffles = scn.GetShadowDeckShuffleCount();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warg));
		scn.ShadowAcceptOptionalTrigger();
		// The deck is shown, and nothing in it may be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gear, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gothmog, false));
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DECK, gear, gothmog);
		assertEquals(2, scn.GetShadowDeckCount());
		// The look's own shuffle, and no other.
		assertEquals(shuffles + 1, scn.GetShadowDeckShuffleCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
