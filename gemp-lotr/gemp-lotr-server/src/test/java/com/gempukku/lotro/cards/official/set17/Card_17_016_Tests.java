package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_016_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("barliman", "17_16");

					// [gandalf] followers with no strength of their own.
					put("alatar", "13_28");         // Alatar, Final Envoy: Aid (2); a reinforce on transfer, -1 to minions in bearer's skirmish
					put("radagast", "13_38");       // Radagast, Tender of Beasts: Aid (2), resistance +2; acts on minions in bearer's skirmish only
					// Cards on him that fail one word each.
					put("worker", "15_135");        // Rohan Worker: a [rohan] follower, no strength; its texts answer a transfer and a win
					put("watch", "12_35");          // Watch and Wait: a [gandalf] condition on a companion; a win trigger only
					put("arwen", "1_30");           // Arwen, Daughter of Elrond: another companion to bear a follower

					// Cards for the draw deck.
					put("sleep", "1_84");           // Sleep, Caradhras: a [gandalf] event
					put("gandalf", "1_72");         // Gandalf, Friend of the Shirefolk: a [gandalf] companion
					put("lorien", "1_53");          // Lórien Elf: a Free Peoples card of another culture
					put("runner", "1_178");         // Goblin Runner: a Shadow card

					put("savage", "1_151");         // Uruk Savage: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarlimanButterburStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Barliman Butterbur, Red-faced Landlord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Barliman Butterbur is strength +1 for each attached [gandalf] follower.
		 * Maneuver: Exert Barliman Butterbur to reveal the top 2 cards of your deck. Take any [gandalf] cards into hand and discard the rest.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("barliman");

		assertEquals("Barliman Butterbur", card.getBlueprint().getTitle());
		assertEquals("Red-faced Landlord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void AGandalfFollowerTransferredToHimByItsAidMakesHimStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var alatar = scn.GetFreepsCard("alatar");
		scn.MoveCompanionsToTable(barliman);
		scn.MoveCardsToSupportArea(alatar);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		// A [gandalf] follower in the support area is attached to nobody.
		assertInZone(Zone.SUPPORT, alatar);
		assertEquals(4, scn.GetStrength(barliman));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(alatar, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(barliman);
		assertEquals(barliman, alatar.getAttachedTo());
		assertEquals(5, scn.GetStrength(barliman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void HeIsStrengthPlus1ForEachGandalfFollowerAndNotForOtherCardsHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var alatar = scn.GetFreepsCard("alatar");
		var radagast = scn.GetFreepsCard("radagast");
		var worker = scn.GetFreepsCard("worker");
		var watch = scn.GetFreepsCard("watch");
		scn.MoveCompanionsToTable(barliman);

		scn.StartGame();
		assertEquals(4, scn.GetStrength(barliman));

		// A follower of another culture.
		scn.AttachCardsTo(barliman, worker);
		assertEquals(barliman, worker.getAttachedTo());
		assertEquals(4, scn.GetStrength(barliman));

		// A [gandalf] card that is no follower.
		scn.AttachCardsTo(barliman, watch);
		assertEquals(barliman, watch.getAttachedTo());
		assertEquals(4, scn.GetStrength(barliman));

		// One [gandalf] follower, then two.
		scn.AttachCardsTo(barliman, alatar);
		assertEquals(barliman, alatar.getAttachedTo());
		assertEquals(5, scn.GetStrength(barliman));
		scn.AttachCardsTo(barliman, radagast);
		assertEquals(barliman, radagast.getAttachedTo());
		assertEquals(6, scn.GetStrength(barliman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGandalfFollowerAttachedToAnotherCompanionDoesNotCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var arwen = scn.GetFreepsCard("arwen");
		var alatar = scn.GetFreepsCard("alatar");
		scn.MoveCompanionsToTable(barliman, arwen);

		scn.StartGame();
		assertEquals(4, scn.GetStrength(barliman));
		assertEquals(6, scn.GetStrength(arwen));

		// "Each attached [gandalf] follower" is each one attached to Barliman (Ketura's ruling, Q-0372: B): one on
		// Arwen counts for nobody.
		scn.AttachCardsTo(arwen, alatar);
		assertEquals(arwen, alatar.getAttachedTo());
		assertEquals(4, scn.GetStrength(barliman));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityRevealsTheTopTwoCardsAndTakesTheGandalfCardIntoHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var sleep = scn.GetFreepsCard("sleep");
		var lorien = scn.GetFreepsCard("lorien");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));
		// From the top: the [gandalf] event, the Lórien Elf, then the [gandalf] Gandalf, who is not revealed.
		scn.MoveCardsToTopOfDeck(gandalf, lorien, sleep);
		assertEquals(sleep, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, sleep);
		assertInZone(Zone.DECK, lorien);
		assertInZone(Zone.DECK, gandalf);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(barliman));
		int hand = scn.GetFreepsHandCount();
		assertTrue(scn.FreepsActionAvailable(barliman));

		scn.FreepsUseCardAction(barliman);
		assertEquals(1, scn.GetWoundsOn(barliman));
		// Both players are shown the two cards, which neither may choose.
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(sleep, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorien, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sleep, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(lorien, false));
		scn.DismissRevealedCards();

		assertInZone(Zone.HAND, sleep);
		assertInZone(Zone.DISCARD, lorien);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(gandalf, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AGandalfCompanionIsTakenAndAShadowCardDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetFreepsCard("runner");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));
		// From the top: the Free Peoples player's own Goblin Runner, Gandalf, then the Lórien Elf.
		scn.MoveCardsToTopOfDeck(lorien, gandalf, runner);
		assertEquals(runner, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, runner);
		assertInZone(Zone.DECK, gandalf);
		assertInZone(Zone.DECK, lorien);

		scn.SkipToPhase(Phase.MANEUVER);
		int hand = scn.GetFreepsHandCount();
		scn.FreepsUseCardAction(barliman);
		scn.DismissRevealedCards();

		assertInZone(Zone.HAND, gandalf);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(lorien, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EveryGandalfCardAmongTheTwoIsTakenWithNoChoiceAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		var sleep = scn.GetFreepsCard("sleep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var lorien = scn.GetFreepsCard("lorien");
		scn.MoveCompanionsToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));
		// From the top: two [gandalf] cards, then the Lórien Elf, who is not revealed.
		scn.MoveCardsToTopOfDeck(lorien, gandalf, sleep);
		assertEquals(sleep, scn.GetFreepsTopOfDeck());
		assertInZone(Zone.DECK, sleep);
		assertInZone(Zone.DECK, gandalf);
		assertInZone(Zone.DECK, lorien);

		scn.SkipToPhase(Phase.MANEUVER);
		int hand = scn.GetFreepsHandCount();
		int discard = scn.GetFreepsDiscardCount();

		scn.FreepsUseCardAction(barliman);
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		assertInZone(Zone.HAND, sleep);
		assertInZone(Zone.HAND, gandalf);
		assertEquals(hand + 2, scn.GetFreepsHandCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertEquals(lorien, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(barliman);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(barliman, 2);
		assertEquals(2, scn.GetWoundsOn(barliman));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.MANEUVER);
		// The draw deck still holds cards to reveal.
		assertTrue(scn.GetFreepsDeckCount() > 2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(barliman));
	}
}
