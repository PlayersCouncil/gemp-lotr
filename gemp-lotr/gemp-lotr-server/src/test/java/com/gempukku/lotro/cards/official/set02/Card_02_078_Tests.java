package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("found", "2_78");

					// Úlairë Nelya, Ringwraith in Twilight: a twilight Nazgûl; her text needs 2 burdens or 2 wounds
					// on the Ring-bearer, and there are none here
					put("nelya", "2_84");
					// Úlairë Attëa, The Easterling: a twilight Nazgûl of another title; his text answers his own play
					put("attea", "2_82");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl who is not twilight; his text reaches
					// only a companion or ally bearing a [wraith] condition
					put("lemenya", "1_232");
					// Wisp of Pale Sheen: a twilight [sauron] Wraith who is no Nazgûl; a Regroup ability only
					put("wisp", "6_108");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ItWantstobeFoundStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: It Wants to be Found
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a twilight Nazgûl to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("found");

		assertEquals("It Wants to be Found", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingATwilightNazgulAddsABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var found = scn.GetShadowCard("found");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(found);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.FreepsGetBurdens());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowPlayAvailable(found));

		scn.ShadowPlayCard(found);

		// The burden goes on the Free Peoples player's Ring-bearer; the Nazgûl is only spotted, not exerted.
		assertEquals(1, scn.FreepsGetBurdens());
		assertEquals(0, scn.ShadowGetBurdens());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertInDiscard(found);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ATwilightNazgulOfAnotherTitleIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var found = scn.GetShadowCard("found");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCardsToHand(found);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowPlayAvailable(found));

		scn.ShadowPlayCard(found);

		assertEquals(1, scn.FreepsGetBurdens());
		assertInDiscard(found);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithANazgulWhoIsNotTwilightOrATwilightMinionWhoIsNoNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var found = scn.GetShadowCard("found");
		var lemenya = scn.GetShadowCard("lemenya");
		var wisp = scn.GetShadowCard("wisp");
		scn.MoveCardsToHand(found);
		assertInHand(found);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, wisp);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// The pool holds 3 (1 companion + 2 for site 2), enough for the event's 2.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(found));
		assertEquals(0, scn.FreepsGetBurdens());
		assertInHand(found);
	}
}
