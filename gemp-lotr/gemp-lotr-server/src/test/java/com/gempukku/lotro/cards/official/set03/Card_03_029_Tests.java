package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("betrayal", "3_29");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					// Saruman, Keeper of Isengard: [isengard] Wizard minion, vitality 4.  He may not take wounds in the
					// archery phase (not the maneuver phase) and may not be assigned; his response saves Uruk-hai only.
					put("saruman", "3_68");
					// Saruman, Rabble-rouser: [dunland] Wizard minion, vitality 4.  His move trigger counts [dunland] Men
					// (none here) and his skirmish ability is used only on purpose.
					put("rabble", "4_33");
					put("uruk", "1_158");       // Uruk-hai Raiding Party: an [isengard] minion who is no Saruman; damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDrawScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("betrayal", "3_29");
					put("scout", "1_191");      // Moria Scout: a [moria] Orc; its trigger is on being played
					put("threat", "1_197");     // Threat of the Unknown: "Shadow: Exert your [moria] Orc to draw 2 cards."
					// Fool of a Took!: [moria] Shadow event.  "Spot a Hobbit (except the Ring-bearer) to reveal cards from
					// the top of your draw deck until you reveal one that is not a [moria] minion. Take the revealed cards
					// into hand."
					put("fool", "1_170");
					put("sam", "1_311");        // Sam, Son of Hamfast: the Hobbit who is not the Ring-bearer
					// Gorgoroth Stormer: [sauron] besieger Orc.  "Shadow: If you have initiative, exert this minion to add a
					// burden and make the Free Peoples player draw a card."
					put("stormer", "8_101");
					// Encirclement: [sauron] support-area condition.  "Regroup: Spot a [sauron] minion to draw a card (or
					// draw 2 cards instead if that minion is a besieger). Discard this condition."
					put("encirclement", "7_268");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BetrayalofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Betrayal of Isengard
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time an opponent draws a card (or takes a card into hand) during the Shadow phase, you may remove (1).<br><b>Maneuver:</b> Exert Gandalf to wound Saruman twice.
		*/

		var scn = GetScenario();

		var betrayal = scn.GetFreepsCard("betrayal");

		assertEquals("Betrayal of Isengard", betrayal.getBlueprint().getTitle());
		assertNull(betrayal.getBlueprint().getSubtitle());
		assertFalse(betrayal.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, betrayal.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, betrayal.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, betrayal.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(betrayal, Keyword.SUPPORT_AREA));
		assertEquals(1, betrayal.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachCardTheShadowPlayerDrawsInTheShadowPhaseOffersToRemoveOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var scout = scn.GetShadowCard("scout");
		var threat = scn.GetShadowCard("threat");
		scn.MoveCardsToSupportArea(betrayal);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SetShadowHand(threat);
		assertInZone(Zone.SUPPORT, betrayal);
		assertInHand(threat);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertTrue(twilight >= 2);
		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(threat);
		assertEquals(1, scn.GetWoundsOn(scout));

		// Two cards drawn: one offer for each.  The first is taken.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(betrayal));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(twilight - 1, scn.GetTwilight());

		// The second is declined, and the pool is left as it was.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(betrayal));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACardTheShadowPlayerTakesIntoHandInTheShadowPhaseOffersToRemoveOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var sam = scn.GetFreepsCard("sam");
		var fool = scn.GetShadowCard("fool");
		var threat = scn.GetShadowCard("threat");
		scn.MoveCardsToSupportArea(betrayal);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SetShadowHand(fool);
		// Threat of the Unknown is no [moria] minion: the reveal stops at it, and it is the one card taken.
		scn.MoveCardsToTopOfDeck(threat);
		assertInZone(Zone.DECK, threat);
		assertEquals(threat, scn.GetShadowTopOfDeck());
		assertInHand(fool);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertTrue(twilight >= 1);
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(fool);
		// The reveal is shown to both players.
		scn.DismissRevealedCards();

		assertInHand(threat);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(betrayal));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDrawingInTheShadowPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var stormer = scn.GetShadowCard("stormer");
		scn.MoveCardsToSupportArea(betrayal);

		scn.StartGame();
		scn.MoveMinionsToTable(stormer);
		assertInZone(Zone.SUPPORT, betrayal);
		scn.FreepsPass();

		// The Free Peoples hand is empty, so the Shadow player has initiative.
		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertTrue(twilight >= 1);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(stormer));

		scn.ShadowUseCardAction(stormer);
		assertEquals(1, scn.GetWoundsOn(stormer));
		assertEquals(1, scn.GetBurdens());
		// A card drawn in the Shadow phase, but by the card's own player and not by an opponent.
		assertEquals(1, scn.GetFreepsHandCount());

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(betrayal));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerDrawingInTheRegroupPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var stormer = scn.GetShadowCard("stormer");
		var encirclement = scn.GetShadowCard("encirclement");
		scn.MoveCardsToSupportArea(betrayal, encirclement);
		assertInZone(Zone.SUPPORT, betrayal, encirclement);

		scn.StartGame();
		// No minion until the regroup phase: the Shadow phase is followed by the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(stormer);
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		int hand = scn.GetShadowHandCount();
		assertTrue(scn.ShadowActionAvailable(encirclement));

		scn.ShadowUseCardAction(encirclement);
		// The Stormer is a besieger: two cards drawn, in the regroup phase.
		assertEquals(hand + 2, scn.GetShadowHandCount());
		assertInDiscard(encirclement);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(betrayal));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheTwilightPoolEmptyACardDrawnInTheShadowPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDrawScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var scout = scn.GetShadowCard("scout");
		var threat = scn.GetShadowCard("threat");
		scn.MoveCardsToSupportArea(betrayal);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		scn.SetShadowHand(threat);
		assertInZone(Zone.SUPPORT, betrayal);
		assertInHand(threat);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(threat);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(2, scn.GetShadowHandCount());

		// Nothing to remove: the offer is not made for either card drawn.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(betrayal));
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsGandalfToWoundSarumanTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var saruman = scn.GetShadowCard("saruman");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(betrayal);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, uruk);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertTrue(scn.FreepsActionAvailable(betrayal));

		scn.FreepsUseCardAction(betrayal);
		// Gandalf pays, not the other [gandalf] companion: no choice is asked.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		// Two wounds on Saruman, none on the [isengard] minion beside him.
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheTwoWoundsKillTheDunlandSarumanWithTwoVitalityLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var gandalf = scn.GetFreepsCard("gandalf");
		var rabble = scn.GetShadowCard("rabble");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(betrayal);

		scn.StartGame();
		scn.MoveMinionsToTable(rabble);
		// Vitality 4 with 2 wounds: two exertions would leave him alive, one wound would leave him alive.
		scn.AddWoundsToChar(rabble, 2);
		assertEquals(2, scn.GetWoundsOn(rabble));
		scn.SkipToPhase(Phase.MANEUVER);

		assertInPlay(rabble);
		assertTrue(scn.FreepsActionAvailable(betrayal));

		scn.FreepsUseCardAction(betrayal);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(rabble);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoSarumanTheAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var gandalf = scn.GetFreepsCard("gandalf");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(betrayal);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertTrue(scn.FreepsActionAvailable(betrayal));

		scn.FreepsUseCardAction(betrayal);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileGandalfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var betrayal = scn.GetFreepsCard("betrayal");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(betrayal);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		// Vitality 4 with 3 wounds: Gandalf is exhausted; the Ent Avenger could exert, but is no Gandalf.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(betrayal));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(saruman));
	}
}
