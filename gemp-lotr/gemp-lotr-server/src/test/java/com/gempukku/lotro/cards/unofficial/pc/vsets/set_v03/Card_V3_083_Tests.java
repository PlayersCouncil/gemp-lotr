package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_083_Tests
{

	// Gondor Calls For Aid! (103_83) Tests

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gcfa", "103_83");
					put("beacon1", "103_35");
					put("beacon2", "103_35");
					put("beacon3", "103_35");
					put("beacon4", "103_35");
					put("beacon5", "103_35");
					put("beacon6", "103_35");
					put("strider", "11_54");
					put("amondin", "103_28");

					put("aragorn", "1_89");       // Gondor Man
					put("eowyn", "5_122");        // Rohan Man
					put("boromir", "1_97");       // Gondor Man

					// Culture-restricted items for testing
					put("athelas", "1_94");       // Bearer must be Gondor Man
					put("ridermount", "4_287");   // Plays on Rohan companion
					put("brego", "4_263");        // Plays on Man (spots Rohan Man)

					// Invalid items (not playable on Men)
					put("sting", "1_313");        // Shire possession (Hobbit)
					put("sapling", "9_35");       // Gondor artifact (support area)

					put("runner", "1_178");

					put("uruk", "1_143");
					put("power", "51_136");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorCallsForAidStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gondor Calls For Aid!
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Beacon. To play, hinder 4 beacons.
		* 	This cannot be discarded or hindered.
		* 	Your [Gondor] Men are considered [Rohan] Men.  Your [Rohan] Men are considered [Gondor] Men.
		* 	Maneuver: Hinder a beacon to play an item from your discard pile on your Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gcfa");

		assertEquals("Gondor Calls For Aid!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

//
// Extra Cost tests - requires hindering 4 beacons
//

	@Test
	public void GCFACannotPlayWithoutFourBeaconsToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(gcfa);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1, beacon2, beacon3); // Only 3 beacons

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(gcfa));
	}

	@Test
	public void GCFACanPlayByHinderingFourBeacons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var beacon4 = scn.GetFreepsCard("beacon4");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(gcfa);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(beacon1, beacon2, beacon3, beacon4);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(gcfa));

		scn.FreepsPlayCard(gcfa);
		scn.FreepsChooseCards(beacon1, beacon2, beacon3, beacon4);

		assertInZone(Zone.SUPPORT, gcfa);
		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
		assertTrue(scn.IsHindered(beacon3));
		assertTrue(scn.IsHindered(beacon4));
	}

//
// Culture modifier tests - Gondor Men considered Rohan, Rohan Men considered Gondor
//

	@Test
	public void GCFAAllowsGondorItemOnRohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var athelas = scn.GetFreepsCard("athelas"); // Bearer must be Gondor Man
		var eowyn = scn.GetFreepsCard("eowyn");     // Rohan Man
		var aragorn = scn.GetFreepsCard("aragorn"); // Gondor Man for comparison
		scn.MoveCardsToHand(athelas);
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.MoveCardsToSupportArea(gcfa);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(athelas));
		scn.FreepsPlayCard(athelas);

		// Should be able to choose both Aragorn (native Gondor) and Eowyn (considered Gondor)
		assertTrue(scn.FreepsCanChooseCharacter(aragorn));
		assertTrue(scn.FreepsCanChooseCharacter(eowyn));
	}

	@Test
	public void GCFAAllowsRohanItemOnGondorMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var ridermount = scn.GetFreepsCard("ridermount"); // Plays on Rohan companion
		var aragorn = scn.GetFreepsCard("aragorn");       // Gondor Man
		var eowyn = scn.GetFreepsCard("eowyn");           // Rohan Man for comparison
		scn.MoveCardsToHand(ridermount);
		scn.MoveCompanionsToTable(aragorn, eowyn);
		scn.MoveCardsToSupportArea(gcfa);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(ridermount));
		scn.FreepsPlayCard(ridermount);

		// Should be able to choose both Eowyn (native Rohan) and Aragorn (considered Rohan)
		assertTrue(scn.FreepsCanChooseCharacter(eowyn));
		assertTrue(scn.FreepsCanChooseCharacter(aragorn));
	}


//
// Maneuver ability tests - hinder X beacons to play X items from discard
//

	@Test
	public void GCFAManeuverAbilityPlaysOneItemFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var ridermount = scn.GetFreepsCard("ridermount");
		var eowyn = scn.GetFreepsCard("eowyn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(gcfa, beacon1);
		scn.MoveCardsToDiscard(ridermount);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.DISCARD, ridermount);
		assertFalse(scn.IsHindered(beacon1));

		assertTrue(scn.FreepsActionAvailable(gcfa));
		scn.FreepsUseCardAction(gcfa);
		// Only valid beacon is hindered

		// Choose item from discard, only 1 to choose
		//scn.FreepsChooseCard(ridermount);
		// Choose bearer - Eowyn is native Rohan, auto-selected

		assertAttachedTo(ridermount, eowyn);
		assertEquals(eowyn, ridermount.getAttachedTo());
		assertTrue(scn.IsHindered(beacon1));
	}

	@Test
	public void GCFAManeuverAbilityPlaysMultipleItemsFromDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var beacon2 = scn.GetFreepsCard("beacon2");
		var beacon3 = scn.GetFreepsCard("beacon3");
		var ridermount = scn.GetFreepsCard("ridermount");
		var athelas = scn.GetFreepsCard("athelas");
		var sting = scn.GetFreepsCard("sting");           // Invalid - plays on Hobbit
		var sapling = scn.GetFreepsCard("sapling");
		var brego = scn.GetFreepsCard("brego");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.MoveCardsToSupportArea(gcfa, beacon1, beacon2, beacon3);
		scn.MoveCardsToDiscard(ridermount, athelas, brego, sting, sapling);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(gcfa);
		scn.FreepsChooseCards(beacon1, beacon2, beacon3); // Hinder 3 beacons

		assertTrue(scn.FreepsHasCardChoicesAvailable(ridermount, brego, athelas));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(sting, sapling));

		// First item
		scn.FreepsChooseCard(brego);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsDeclineOptionalTrigger(); //Brego's text

		// Second item
		scn.FreepsChooseCard(athelas);
		scn.FreepsChooseCard(aragorn);

		// Third item, automatically chosen and went on eowyn due to being the only options
		//scn.FreepsChooseCard(ridermount);
		//scn.FreepsChooseCard(eowyn); // Brego can go on Eowyn (Man)

		assertAttachedTo(brego, aragorn);
		assertAttachedTo(athelas, aragorn);
		assertAttachedTo(ridermount, eowyn);

		assertTrue(scn.IsHindered(beacon1));
		assertTrue(scn.IsHindered(beacon2));
		assertTrue(scn.IsHindered(beacon3));
	}


	@Test
	public void GCFAManeuverAbilityNotAvailableWithoutBeaconToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var ridermount = scn.GetFreepsCard("ridermount");
		var eowyn = scn.GetFreepsCard("eowyn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(gcfa); // No other beacons, GCFA can't be hindered
		scn.MoveCardsToDiscard(ridermount);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		// No beacons available to hinder (GCFA can't be hindered)
		assertFalse(scn.FreepsActionAvailable(gcfa));
	}

	@Test
	public void GCFAManeuverAbilityNotAvailableWithoutItemInDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gcfa = scn.GetFreepsCard("gcfa");
		var beacon1 = scn.GetFreepsCard("beacon1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(gcfa, beacon1);
		// No items in discard
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		// No playable items in discard
		assertFalse(scn.FreepsActionAvailable(gcfa));
	}

	@Test
	public void GondorCallsForAidCannotBeDiscardedOrHinderedBySarumansPower() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var aid = scn.GetFreepsCard("gcfa");
		var amondin = scn.GetFreepsCard("amondin");
		scn.MoveCardsToSupportArea(aid, amondin);

		var uruk = scn.GetShadowCard("uruk");
		//Errata'd Saruman's Power, which discards 1 condition and then hinders all others
		var power = scn.GetShadowCard("power");
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, aid);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(power));

		scn.ShadowPlayCard(power);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, aid);
	}

	@Test
	public void GondorCallsForAidCannotBeHinderedByOtherBeacons() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var aid = scn.GetFreepsCard("gcfa");
		var amondin = scn.GetFreepsCard("amondin");
		var strider = scn.GetFreepsCard("strider");
		scn.MoveCardsToSupportArea(aid, amondin);
		scn.MoveCompanionsToTable(strider);

		var uruk = scn.GetShadowCard("uruk");
		//Errata'd Saruman's Power, which discards 1 condition and then hinders all others
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();
		scn.FreepsPass();
		//site / strider timing tie
		scn.FreepsChoose("Strider");

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(strider, uruk);
		scn.FreepsPass();
		scn.ShadowPass();

		assertInZone(Zone.SUPPORT, aid);
		assertFalse(scn.IsHindered(aid));
		assertInZone(Zone.SUPPORT, amondin);
		assertFalse(scn.IsHindered(amondin));

		//Strider is being wounded, Flame of Amon Din permits hindering a beacon to prevent it
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();

		assertFalse(scn.FreepsHasCardChoicesAvailable(aid));
		assertFalse(scn.FreepsHasCardChoicesAvailable(amondin));
		assertFalse(scn.IsHindered(aid));
		assertTrue(scn.IsHindered(amondin));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
