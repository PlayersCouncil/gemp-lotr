package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("haste", "103_6");
					put("gandalf", "1_364");
					put("shadowfax", "4_100");
					put("wielder", "2_28"); // Maneuver event with no exert cost
					put("terrible", "7_50"); // Maneuver event with an exert cost
					put("forfrodo", "103_107"); // Skirmish event that removes self from game
					put("sam", "1_311");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShowThemtheMeaningofHasteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Show Them the Meaning of Haste
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Maneuver</b> <i>or</i> <b>Skirmish</b>: Exert Gandalf thrice (or spot Shadowfax and exert Gandalf once) to reveal another Free Peoples event from your hand.  Play that event 3 times, then remove it from the game.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("haste");

		assertEquals("Show Them the Meaning of Haste", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ShowThemtheMeaningofHastePlaysANonExertingEventThrice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var wielder = scn.GetFreepsCard("wielder");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(haste, wielder);
		scn.MoveCompanionsToTable(gandalf);

		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(wielder));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.FreepsPlayAvailable(haste));
		assertInZone(Zone.HAND, wielder);

		scn.FreepsPlayCard(haste);
		assertFalse(scn.HasKeyword(gandalf, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		//Wielder was automatically chosen as the only valid target
		scn.ShadowDismissRevealedCards();

		scn.ShadowChooseNo(); //Remove (3) to prevent giving companion Defender +1
		scn.FreepsChooseCard(gandalf);
		scn.ShadowChooseNo(); //Remove (3) to prevent giving companion Defender +1
		scn.FreepsChooseCard(gandalf);
		scn.ShadowChooseNo(); //Remove (3) to prevent giving companion Defender +1
		scn.FreepsChooseCard(gandalf);

		assertEquals(3, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertInZone(Zone.REMOVED, wielder);
	}

	@Test
	public void ShowThemtheMeaningofHastePlaysEventThriceEvenWhenLaterIterationsFailPayment() throws DecisionResultInvalidException, CardNotFoundException {

		//Pre-game setup
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var terrible = scn.GetFreepsCard("terrible");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCardsToHand(haste, terrible);
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, shadowfax);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();
		
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsPlayAvailable(terrible));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.FreepsPlayAvailable(haste));
		assertInZone(Zone.HAND, terrible);
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(haste);
		//Exerted once since Shadowfax can be spotted
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		//Terrible and Evil was automatically chosen as the only valid target
		scn.ShadowDismissRevealedCards();

		//Exerting X times on first play
		scn.FreepsChoose("1");
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, runner);

		//Exerting X times on second play
		scn.FreepsChoose("1");
		assertEquals(3, scn.GetWoundsOn(gandalf));

		//Third play fails to pay costs due to gandalf having no remaining vitality
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		assertInZone(Zone.REMOVED, terrible);
	}

	@Test
	public void ShowThemtheMeaningofHastePlaysEventThriceEvenWhenEventRemovesItselfFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var haste = scn.GetFreepsCard("haste");
		var forfrodo = scn.GetFreepsCard("forfrodo");
		var sam = scn.GetFreepsCard("sam");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCardsToHand(haste, forfrodo);
		scn.MoveCompanionsToTable(gandalf, sam);
		scn.AttachCardsTo(gandalf, shadowfax);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(sam, runner);

		assertTrue(scn.FreepsPlayAvailable(forfrodo));
		assertTrue(scn.FreepsPlayAvailable(haste));
		assertInZone(Zone.HAND, forfrodo);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetStrength(sam));

		scn.FreepsPlayCard(haste);
		//Exerted once since Shadowfax can be spotted
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);

		//ForFrodo was automatically chosen as the only valid target
		scn.ShadowDismissRevealedCards();

		//For Frodo should have been played 3 times, each time exerting Sam as the only
		// valid target, and giving him +1 strength (as there is only 1 burden in play)
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(6, scn.GetStrength(sam));

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		assertInZone(Zone.REMOVED, forfrodo);
	}
}
