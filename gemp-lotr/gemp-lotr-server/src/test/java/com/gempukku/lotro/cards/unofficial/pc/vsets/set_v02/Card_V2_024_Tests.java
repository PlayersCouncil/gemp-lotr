package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_024_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hound1", "102_24");
					put("hound2", "102_24");

					// Warg-riders, both [isengard] Orcs who are fierce while not exhausted.
					put("rider", "5_53");        // Isengard Rider: strength 8, vitality 3
					put("wargrider", "5_67");    // Warg-rider: strength 5, vitality 2
					// Isengard Worker: an [isengard] Orc who is no warg-rider; a regroup ability.
					put("worker", "3_62");

					// [isengard] skirmish events.
					put("bred", "1_121");        // Bred for Battle
					put("servants", "3_70");     // Servants to Saruman
					put("voices", "5_68");       // Wolf-voices
					put("ferocity", "4_151");    // Ferocity
					put("hunt", "1_126");        // Hunt Them Down!: an [isengard] maneuver event
					put("drums", "1_168");       // Drums in the Deep: a [moria] skirmish event
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HoundofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Hound of Isengard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Vitality: +1
		 * Game Text: Bearer must be a warg-rider.
		 * When you play this possession, you may take an [isengard] skirmish event into hand from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hound1");

		assertEquals("Hound of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void IsPlayedForTwoOnAWargRider() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var hound = scn.GetShadowCard("hound1");
		var rider = scn.GetShadowCard("rider");
		var wargrider = scn.GetShadowCard("wargrider");
		var worker = scn.GetShadowCard("worker");
		var bred = scn.GetShadowCard("bred");
		scn.MoveCardsToHand(hound);
		scn.MoveCardsToDiscard(bred);

		scn.StartGame();

		scn.MoveMinionsToTable(rider, wargrider, worker);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(hound));

		scn.ShadowPlayCard(hound);

		// The two warg-riders; not the [isengard] Orc who is none, and not Frodo.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, wargrider));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(worker, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(rider);

		assertAttachedTo(hound, rider);
		assertEquals(5, scn.GetStrength(wargrider));
		assertEquals(2, scn.GetVitality(wargrider));

		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedAnIsengardSkirmishEventMayBeTakenIntoHandFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hound = scn.GetShadowCard("hound1");
		var rider = scn.GetShadowCard("rider");
		var bred = scn.GetShadowCard("bred");
		var servants = scn.GetShadowCard("servants");
		var voices = scn.GetShadowCard("voices");
		var hunt = scn.GetShadowCard("hunt");
		var drums = scn.GetShadowCard("drums");
		var theirFerocity = scn.GetFreepsCard("ferocity");
		scn.MoveCardsToHand(hound);
		// Two [isengard] skirmish events in the discard pile, with an [isengard] event of another phase and
		// a skirmish event of another culture.  A third [isengard] skirmish event is in the other player's
		// discard pile, and a fourth (Wolf-voices) in the draw deck.
		scn.MoveCardsToDiscard(bred, servants, hunt, drums, theirFerocity);

		scn.StartGame();

		scn.MoveMinionsToTable(rider);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.DECK, voices);
		assertInZone(Zone.DISCARD, bred, servants);
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(hound);

		assertAttachedTo(hound, rider);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hound));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(bred, servants));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hunt, drums, theirFerocity, voices));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(bred);

		assertInZone(Zone.HAND, bred);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, servants, hunt, drums, theirFerocity);
		assertInZone(Zone.DECK, voices);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesTheEventInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hound = scn.GetShadowCard("hound1");
		var rider = scn.GetShadowCard("rider");
		var bred = scn.GetShadowCard("bred");
		var servants = scn.GetShadowCard("servants");
		scn.MoveCardsToHand(hound);
		scn.MoveCardsToDiscard(bred, servants);

		scn.StartGame();

		scn.MoveMinionsToTable(rider);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(hound);

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hound));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, bred, servants);
		assertAttachedTo(hound, rider);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedMakesOneOfferOfItsOwnAndTheCopyInPlayMakesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inPlay = scn.GetShadowCard("hound1");
		var played = scn.GetShadowCard("hound2");
		var rider = scn.GetShadowCard("rider");
		var wargrider = scn.GetShadowCard("wargrider");
		var bred = scn.GetShadowCard("bred");
		var servants = scn.GetShadowCard("servants");
		scn.MoveCardsToHand(played);
		scn.MoveCardsToDiscard(bred, servants);

		scn.StartGame();

		scn.MoveMinionsToTable(rider, wargrider);
		scn.AttachCardsTo(wargrider, inPlay);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(played));
		scn.ShadowPlayCard(played);

		// The Warg-rider already has a mount, so the Isengard Rider is the only bearer there is.
		assertAttachedTo(played, rider);
		assertAttachedTo(inPlay, wargrider);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(played));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inPlay));

		scn.ShadowChooseOptionalTrigger(played);
		scn.ShadowChooseCardBPFromSelection(servants);

		// One event for one Hound played: the other event stays, and nothing more is offered.
		assertInZone(Zone.HAND, servants);
		assertInZone(Zone.DISCARD, bred);
		assertEquals(1, scn.GetShadowHandCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoIsengardSkirmishEventInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hound = scn.GetShadowCard("hound1");
		var rider = scn.GetShadowCard("rider");
		var voices = scn.GetShadowCard("voices");
		var hunt = scn.GetShadowCard("hunt");
		var drums = scn.GetShadowCard("drums");
		var theirFerocity = scn.GetFreepsCard("ferocity");
		scn.MoveCardsToHand(hound);
		// What is in reach nearly counts: an [isengard] event of another phase and a skirmish event of another
		// culture in the discard pile, an [isengard] skirmish event in the other player's discard pile, and
		// another in the draw deck.
		scn.MoveCardsToDiscard(hunt, drums, theirFerocity);

		scn.StartGame();

		scn.MoveMinionsToTable(rider);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInZone(Zone.DECK, voices);
		assertTrue(scn.ShadowPlayAvailable(hound));

		scn.ShadowPlayCard(hound);

		// The offer costs nothing and would do nothing, so it is not made.
		assertAttachedTo(hound, rider);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, hunt, drums, theirFerocity);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
