package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("eyes", "101_41");

					// Ulaire Nelya, Ringwraith in Twilight: a unique twilight Nazgul, cost 5, site 2.  Fierce and
					// damage +1 while there are 2 burdens or 2 wounds on the Ring-bearer; it never fights here.
					put("nelya", "2_84");
					// Ringwraith in Twilight: a twilight Nazgul that is not unique, cost 4, site 3.  Its trigger
					// is at the start of the maneuver phase, which no test here reaches with it in play.
					put("rit", "101_40");
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul that is not twilight.  His text weakens
					// companions bearing a [ringwraith] condition; nobody bears one.
					put("lemenya", "1_232");
					// Corpse Lights: a twilight minion that is not a Nazgul ([sauron] Wraith).  A skirmish
					// ability, never used here.
					put("lights", "6_99");

					// Sam, Son of Hamfast: Ring-bound as Frodo is, and not the Ring-bearer.  Vitality 4.  A fellowship
					// ability and a response to Frodo dying, neither used here.
					put("sam", "1_311");

					// Goblin Patrol Troop: no text.  They fill out the Shadow player's draw deck, who reconciles
					// to 8 cards before a second move and must still have more cards left than he is to draw.
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				// Frodo, Son of Drogo: vitality 4, so exhausted with 3 wounds.  A fellowship ability, never used here.
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheirEyesFellUponHimStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Their Eyes Fell Upon Him
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot a twilight Nazgul.
		 * 	Each time the fellowship moves, you may spot a wound on the Ring-bearer to draw a card (or 2 cards if
		 * 	the Ring-bearer is exhausted).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eyes");

		assertEquals("Their Eyes Fell Upon Him", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilATwilightNazgulIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetShadowCard("eyes");
		var nelya = scn.GetShadowCard("nelya");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		// A twilight Nazgul in hand is not one to spot.
		scn.MoveCardsToHand(eyes, nelya);

		scn.StartGame();
		// A Nazgul that is not twilight and a twilight minion that is not a Nazgul.
		scn.MoveMinionsToTable(lemenya, lights);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(eyes));
		assertTrue(scn.ShadowPlayAvailable(nelya));

		scn.ShadowPlayCard(nelya);
		// 13 - 5; a site 2 minion is not roaming at site 2
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(eyes));

		scn.ShadowPlayCard(eyes);
		assertInZone(Zone.SUPPORT, eyes);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ATwilightNazgulThatIsNotUniqueIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eyes = scn.GetShadowCard("eyes");
		var rit = scn.GetShadowCard("rit");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCardsToHand(eyes, rit);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, lights);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(13, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(eyes));

		scn.ShadowPlayCard(rit);
		// 13 - 4 to play - 2 roaming (a site 3 minion played at site 2)
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(eyes));

		scn.ShadowPlayCard(eyes);
		assertInZone(Zone.SUPPORT, eyes);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingDrawsACardWhenTheRingBearerHasAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eyes = scn.GetShadowCard("eyes");
		scn.MoveCardsToSupportArea(eyes);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(12, scn.GetFreepsDeckCount());

		scn.FreepsPass();

		assertEquals(1, scn.GetCurrentSiteNumber()); // the marker moves after "the fellowship moves" is answered
		// "You may": the draw is offered, and nothing is drawn until it is taken.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(eyes));
		assertEquals(0, scn.GetShadowHandCount());
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(10, scn.GetShadowDeckCount());
		// "You" is the Shadow player whose condition it is.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(12, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheDrawIsOfferedAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var eyes = scn.GetShadowCard("eyes");
		scn.MoveCardsToSupportArea(eyes);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());

		scn.FreepsPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(eyes));
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingDrawsNothingWhenTheWoundsAreOnAnotherCompanionAndTheRingBearerHasOnlyABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		var eyes = scn.GetShadowCard("eyes");
		scn.MoveCardsToSupportArea(eyes);

		scn.StartGame();
		// Sam is the nearest companion to the Ring-bearer who is not: a Ring-bound Hobbit.
		scn.AddWoundsToChar(sam, 2);
		scn.AddBurdens(1);

		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());

		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondMoveDrawsAgainAndTwoCardsOnceTheRingBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		var eyes = scn.GetShadowCard("eyes");
		scn.MoveCardsToSupportArea(eyes);

		scn.StartGame();
		// Frodo is one wound short of exhausted; Sam beside him is exhausted, and is Ring-bound but not the
		// Ring-bearer.
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 3);

		assertFalse(scn.IsExhausted(frodo));
		assertTrue(scn.IsExhausted(sam));
		assertTrue(scn.HasKeyword(sam, Keyword.RING_BOUND));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(11, scn.GetShadowDeckCount());

		scn.FreepsPass();

		assertEquals(1, scn.GetCurrentSiteNumber()); // the marker moves after "the fellowship moves" is answered
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(eyes));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(10, scn.GetShadowDeckCount());

		// No minion is played, so the regroup phase follows; the Shadow player reconciles to 8 cards.
		scn.SkipToMovementDecision();
		assertEquals(8, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());

		scn.AddWoundsToChar(frodo, 1);
		assertTrue(scn.IsExhausted(frodo));

		scn.FreepsChooseToMove();

		assertEquals(2, scn.GetCurrentSiteNumber()); // still the site left
		// Offered again on the second move, and still nothing drawn until it is taken.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(eyes));
		assertEquals(8, scn.GetShadowHandCount());
		scn.ShadowAcceptOptionalTrigger();

		// Two cards, with a third left in the draw deck undrawn.
		assertEquals(10, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
