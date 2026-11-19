package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_085_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("terrible", "3_85");
					// Gandalf, Friend of the Shirefolk: vitality 4; his text is a strength bonus only
					put("gandalf", "1_72");
					// Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4
					put("avenger", "6_27");
					put("avenger2", "6_27");
					// Úlairë Lemenya: a [wraith] Nazgûl; her text is about companions bearing a [wraith] condition
					put("lemenya", "1_232");
					// Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, fierce; texts that need burdens
					put("khamul", "103_61");
					// Morgul Hound: a [wraith] Orc, no Nazgûl; +4 and fierce only at 2 wounds on the Ring-bearer
					put("hound", "7_192");
					// [gandalf] cards for the hand: an event, an ally and (avenger2) a companion
					put("sleep", "1_84");       // Sleep, Caradhras: [gandalf] Fellowship event
					put("albert", "1_69");      // Albert Dreary: [gandalf] ally
					put("valor", "1_103");      // Elendil's Valor: a [gondor] Maneuver event, no [gandalf] card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TooGreatandTerribleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Too Great and Terrible
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a Nazgûl to wound Gandalf twice. The Free Peoples player may discard 2 [gandalf] cards from hand to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("terrible");

		assertEquals("Too Great and Terrible", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithANazgulSpottedGandalfIsWoundedTwiceWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var sleep = scn.GetFreepsCard("sleep");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(gandalf, avenger);

		var terrible = scn.GetShadowCard("terrible");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SetFreepsHand(sleep, albert);
		scn.MoveCardsToHand(terrible);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.ShadowPlayAvailable(terrible));
		scn.ShadowPlayCard(terrible);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		assertEquals(2, scn.GetWoundsOn(gandalf));
		// The [gandalf] companion who is no Gandalf is untouched.
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInZone(Zone.HAND, sleep, albert);
		assertInZone(Zone.DISCARD, terrible);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ThePreventionDiscardsTwoGandalfCardsOfTheFreePeoplesPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var albert = scn.GetFreepsCard("albert");
		var avenger2 = scn.GetFreepsCard("avenger2");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(gandalf);

		var terrible = scn.GetShadowCard("terrible");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SetFreepsHand(sleep, albert, avenger2, valor);
		scn.MoveCardsToHand(terrible);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.ShadowPlayCard(terrible);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();

		// An event, an ally and a companion of [gandalf] are offered; the [gondor] event is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, albert, avenger2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(valor));
		assertEquals(2, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(sleep, avenger2);

		assertInZone(Zone.DISCARD, sleep, avenger2);
		assertInZone(Zone.HAND, albert, valor);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, terrible);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOneGandalfCardInHandThePreventionIsNotOfferedAndAnExhaustedGandalfIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(gandalf);

		var terrible = scn.GetShadowCard("terrible");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SetFreepsHand(sleep, valor);
		scn.MoveCardsToHand(terrible);
		scn.MoveMinionsToTable(lemenya);
		// Vitality 4 with 3 wounds: he cannot exert, but he can be wounded.
		scn.AddWoundsToChar(gandalf, 3);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.ShadowPlayCard(terrible);

		// Two cards in hand, one of them [gandalf]: the price cannot be paid, and nothing is asked.
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.DEAD, gandalf);
		assertInZone(Zone.HAND, sleep, valor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoNazgulAndIsPlayedOnceANazgulOfAnotherCultureIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var terrible = scn.GetShadowCard("terrible");
		var hound = scn.GetShadowCard("hound");
		var khamul = scn.GetShadowCard("khamul");

		scn.StartGame();
		scn.SetFreepsHand();
		scn.MoveCardsToHand(terrible);
		scn.MoveMinionsToTable(hound);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [wraith] minion who is no Nazgûl.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(terrible));

		// Khamûl is a [raider] Nazgûl.
		scn.MoveMinionsToTable(khamul);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(terrible));

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.ShadowPlayCard(terrible);

		// With no card in his hand the Free Peoples player cannot prevent it.
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.DISCARD, terrible);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoGandalfInPlayThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var avenger = scn.GetFreepsCard("avenger");
		var sleep = scn.GetFreepsCard("sleep");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(avenger);

		var terrible = scn.GetShadowCard("terrible");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SetFreepsHand(sleep, albert);
		scn.MoveCardsToHand(terrible);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Gandalf is no requirement: the event is played for nothing.
		assertTrue(scn.ShadowPlayAvailable(terrible));
		scn.ShadowPlayCard(terrible);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.HAND, sleep, albert);
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInZone(Zone.DISCARD, terrible);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
