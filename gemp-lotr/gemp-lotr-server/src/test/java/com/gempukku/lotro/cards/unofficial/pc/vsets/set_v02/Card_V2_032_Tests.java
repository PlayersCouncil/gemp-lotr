package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_032_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alliance", "102_32");
					put("alliance2", "102_32");

					// [isengard] trackers, all Uruk-hai of site 5.  Each has text that only matters in a skirmish.
					put("seeker", "4_195");      // Uruk Seeker: cost 2, strength 6
					put("runner", "4_193");      // Uruk Runner: cost 3, strength 8
					put("plains", "4_189");      // Uruk Plains Runner: cost 2, strength 7

					// [sauron] trackers, both Orcs of site 6.  Their texts turn on a site number of 1 or less,
					// which nothing here brings about.
					put("spotter", "102_60");    // Lugburz Spotter: cost 2, strength 6
					put("spotter2", "102_60");
					put("lugburz", "102_63");    // Orc of Lugburz: cost 3, strength 9

					// Desert Wind Initiate: a [raider] tracker and a Man, cost 1, strength 7, site 4.  Never played
					// from hand, so its "when you play" never happens; its other text is about Southrons and ambush.
					// Every [sauron] tracker in print is an Orc and every [isengard] tracker an Uruk-hai, so where
					// it matters the Initiate is made both: a tracker of either race and of neither culture.
					put("initiate", "103_47");
					put("initiate2", "103_47");

					put("savage", "1_151");      // Uruk Savage: an [isengard] Uruk-hai and no tracker, cost 2, strength 5, site 5
					// Orc Chieftain: a [sauron] Orc and no tracker, cost 2, strength 7, site 6.  Adds (2) when a
					// weapon is played on him.
					put("chieftain", "1_266");

					// Down to the Last Child: another [isengard] condition.  Does nothing without tokens.
					put("lastchild", "4_148");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UneasyAllianceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Uneasy Alliance
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Search.
		 * While you can spot an [Isengard] tracker and a [Sauron] tracker, trackers are strength +1.
		 * Shadow: Discard a [sauron] tracker from hand to play an [isengard] tracker from your discard pile. Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("alliance");

		assertEquals("Uneasy Alliance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SEARCH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForOneAndTrackersAreStrengthPlus1OnlyOnceASauronTrackerJoinsTheIsengardOnes() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var seeker = scn.GetShadowCard("seeker");
		var runner = scn.GetShadowCard("runner");
		var initiate = scn.GetShadowCard("initiate");
		var spotter = scn.GetShadowCard("spotter");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToHand(alliance, spotter);
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateShadowFilter("name(Desert Wind Initiate)"), null, Race.ORC));

		scn.StartGame();

		// Two [isengard] trackers, a [raider] tracker who is an Orc, and a [sauron] Orc who is no tracker.
		scn.MoveMinionsToTable(seeker, runner, initiate, savage, chieftain);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 for Frodo + 2 for the site
		assertEquals(5, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(seeker));
		assertEquals(8, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(initiate));
		assertTrue(scn.ShadowPlayAvailable(alliance));

		scn.ShadowPlayCard(alliance);

		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, alliance);
		assertNull(alliance.getAttachedTo());
		// No [sauron] tracker yet.
		assertEquals(6, scn.GetStrength(seeker));
		assertEquals(8, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(initiate));

		scn.ShadowPlayCard(spotter);

		// 2 to play + 2 roaming at site 2
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, spotter);
		assertEquals(7, scn.GetStrength(seeker));
		assertEquals(9, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(spotter));
		// "Trackers", of any culture.
		assertEquals(8, scn.GetStrength(initiate));
		// And nobody who is not one.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SauronTrackersAloneGetNothingUntilAnIsengardTrackerIsPlayed() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var seeker = scn.GetShadowCard("seeker");
		var spotter = scn.GetShadowCard("spotter");
		var lugburz = scn.GetShadowCard("lugburz");
		var initiate = scn.GetShadowCard("initiate");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(alliance);
		scn.MoveCardsToHand(seeker);
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateShadowFilter("name(Desert Wind Initiate)"), null, Race.URUK_HAI));

		scn.StartGame();

		// Two [sauron] trackers, a [raider] tracker who is an Uruk-hai, and an [isengard] Uruk-hai who is no
		// tracker.
		scn.MoveMinionsToTable(spotter, lugburz, initiate, savage);
		scn.SetTwilight(1);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(spotter));
		assertEquals(9, scn.GetStrength(lugburz));
		assertEquals(7, scn.GetStrength(initiate));

		scn.ShadowPlayCard(seeker);

		// 2 to play + 2 roaming at site 2
		assertEquals(0, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(spotter));
		assertEquals(10, scn.GetStrength(lugburz));
		assertEquals(8, scn.GetStrength(initiate));
		assertEquals(7, scn.GetStrength(seeker));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondCopyMakesTrackersStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var alliance2 = scn.GetShadowCard("alliance2");
		var seeker = scn.GetShadowCard("seeker");
		var spotter = scn.GetShadowCard("spotter");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(alliance);
		scn.MoveCardsToHand(alliance2);

		scn.StartGame();

		scn.MoveMinionsToTable(seeker, spotter, savage);
		scn.FreepsPass();

		// 1 for Frodo + 2 for the site
		assertEquals(3, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(seeker));
		assertEquals(7, scn.GetStrength(spotter));

		scn.ShadowPlayCard(alliance2);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, alliance, alliance2);
		// Copies of a card that is not unique add up.
		assertEquals(8, scn.GetStrength(seeker));
		assertEquals(8, scn.GetStrength(spotter));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionDiscardsASauronTrackerFromHandToPlayAnIsengardTrackerFromTheDiscardPileAndDiscardsThatCopy() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var alliance2 = scn.GetShadowCard("alliance2");
		var lastChild = scn.GetShadowCard("lastchild");
		var seeker = scn.GetShadowCard("seeker");
		var runner = scn.GetShadowCard("runner");
		var plains = scn.GetShadowCard("plains");
		var spotter = scn.GetShadowCard("spotter");
		var spotter2 = scn.GetShadowCard("spotter2");
		var lugburz = scn.GetShadowCard("lugburz");
		var initiate = scn.GetShadowCard("initiate");
		var initiate2 = scn.GetShadowCard("initiate2");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var theirPlains = scn.GetFreepsCard("plains");
		scn.MoveCardsToSupportArea(alliance, alliance2, lastChild);
		// In hand: two [sauron] trackers, an [isengard] tracker, a [raider] tracker who is an Orc, and a
		// [sauron] Orc who is no tracker.
		scn.MoveCardsToHand(spotter, lugburz, plains, initiate2, chieftain);
		// In the discard pile: two [isengard] trackers, an [isengard] Uruk-hai who is no tracker and a
		// [raider] tracker who is an Uruk-hai.  The other player's discard pile holds an [isengard] tracker
		// of his own.
		scn.MoveCardsToDiscard(seeker, runner, savage, initiate, theirPlains);
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateShadowFilter("name(Desert Wind Initiate)"), null, Race.ORC));
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateShadowFilter("name(Desert Wind Initiate)"), null, Race.URUK_HAI));

		scn.StartGame();

		// A [sauron] tracker in play is not one in hand.
		scn.MoveMinionsToTable(spotter2);
		scn.SetTwilight(7);
		scn.FreepsPass();

		// 7 set + 1 for Frodo + 2 for the site: enough for any card in the discard pile.
		assertEquals(10, scn.GetTwilight());
		assertEquals(5, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(alliance));
		assertTrue(scn.ShadowActionAvailable(alliance2));

		scn.ShadowUseCardAction(alliance);

		assertTrue(scn.ShadowHasCardChoiceAvailable(spotter, lugburz));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(plains, initiate2, chieftain, spotter2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lugburz);

		assertInZone(Zone.DISCARD, lugburz);
		assertEquals(4, scn.GetShadowHandCount());
		// The Uruk Plains Runner stands for the one in hand and for the one in the other player's discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(seeker, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, initiate, lugburz, plains));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(runner);

		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		// 3 to play + 2 roaming at site 2
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.DISCARD, seeker, savage, initiate);
		assertInZone(Zone.HAND, spotter, plains, initiate2, chieftain);
		// The copy that was used, and no other condition.
		assertInZone(Zone.DISCARD, alliance);
		assertInZone(Zone.SUPPORT, alliance2, lastChild);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutASauronTrackerInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var seeker = scn.GetShadowCard("seeker");
		var plains = scn.GetShadowCard("plains");
		var spotter = scn.GetShadowCard("spotter");
		var lugburz = scn.GetShadowCard("lugburz");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(alliance);
		// An [isengard] tracker and a [sauron] Orc who is no tracker in hand; [sauron] trackers in play and in
		// the discard pile.
		scn.MoveCardsToHand(plains, chieftain);
		scn.MoveCardsToDiscard(seeker, lugburz);

		scn.StartGame();

		scn.MoveMinionsToTable(spotter);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(alliance));

		// The Orc of Lugburz is put in hand, and the Chieftain played so that the game looks again.
		scn.MoveCardsToHand(lugburz);
		scn.ShadowPlayCard(chieftain);

		// 2 to play + 2 roaming
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(alliance));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenThePoolIsOneShortOfTheOnlyIsengardTrackerInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var runner = scn.GetShadowCard("runner");
		var spotter = scn.GetShadowCard("spotter");
		var spotter2 = scn.GetShadowCard("spotter2");
		var savage = scn.GetShadowCard("savage");
		var initiate = scn.GetShadowCard("initiate");
		scn.MoveCardsToSupportArea(alliance);
		scn.MoveCardsToHand(spotter);
		// The Uruk Runner needs 3 + 2 roaming.  The Uruk Savage and the second Lugburz Spotter need 2 + 2 and
		// the Initiate, a tracker made an Uruk-hai, 1 + 2: the pool has that, and none is an [isengard] tracker.
		scn.MoveCardsToDiscard(runner, savage, spotter2, initiate);
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateShadowFilter("name(Desert Wind Initiate)"), null, Race.URUK_HAI));

		scn.StartGame();

		scn.SetTwilight(1);
		scn.FreepsPass();

		// 1 set + 1 for Frodo + 2 for the site
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(alliance));
		assertTrue(scn.ShadowPlayAvailable(spotter));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsOfferedWhenThePoolIsExactlyEnoughForTheOnlyIsengardTrackerInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var alliance = scn.GetShadowCard("alliance");
		var runner = scn.GetShadowCard("runner");
		var spotter = scn.GetShadowCard("spotter");
		var spotter2 = scn.GetShadowCard("spotter2");
		var lugburz = scn.GetShadowCard("lugburz");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(alliance);
		scn.MoveCardsToHand(spotter);
		scn.MoveCardsToDiscard(runner, savage, spotter2);

		scn.StartGame();

		scn.MoveMinionsToTable(lugburz);
		scn.SetTwilight(2);
		scn.FreepsPass();

		// 2 set + 1 for Frodo + 2 for the site
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(alliance));

		// One [sauron] tracker in hand and one [isengard] tracker in the discard pile: nothing to choose.
		scn.ShadowUseCardAction(alliance);

		assertInZone(Zone.DISCARD, spotter);
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, alliance, savage, spotter2);
		// With the condition gone the two trackers on the table are as printed.
		assertEquals(8, scn.GetStrength(runner));
		assertEquals(9, scn.GetStrength(lugburz));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
