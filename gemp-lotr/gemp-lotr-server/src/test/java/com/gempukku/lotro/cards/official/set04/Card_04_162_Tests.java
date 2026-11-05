package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_162_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("power", "4_162");
					// Gríma, Wormtongue: an [isengard] Man minion; a maneuver ability never used.
					put("grima", "4_154");
					// Rohirrim Traitor: an [isengard] Man minion; +2 while a companion bears an [isengard] condition.
					put("traitor", "6_72");
					// Saruman, Servant of the Eye: the [isengard] Saruman, a Wizard minion; an assignment ability
					// never used.
					put("saruman", "3_69");
					// Saruman, Rabble-rouser: the [dunland] Saruman, a Wizard minion; his move trigger has nothing
					// to count, being laid after the move.
					put("rabblerouser", "4_33");
					// Saruman, Servant of Sauron: the [isengard] follower Saruman, no character; a skirmish-win
					// trigger only.
					put("follower", "17_38");
					// Dunlending Footmen: a [dunland] Man; a cost modifier in skirmishes only.
					put("footmen", "6_3");
					// Uruk Savage: an [isengard] Uruk-hai minion; Damage +1 is its whole text.
					put("savage", "1_151");
					// Uruk-hai Sword: an [isengard] possession; a text for its bearer only.
					put("sword", "1_160");
					// Brought Back Alive: an [isengard] Assignment event.
					put("alive", "4_143");
					// Cirith Ungol Guard: a [sauron] Uruk-hai; Damage +1 is its whole text.
					put("guard", "10_81");
					// Isengard Axe: an [isengard] possession, put in the Free Peoples player's discard pile.
					put("axe", "3_55");
					// Free Peoples cards for the Shadow player's hand: a companion, a possession and an event.
					put("boromir", "1_97");
					put("armor", "1_92");
					put("defiance", "1_37");
					// Shadow cards for the Shadow player's hand: an [isengard] tracker and a [sauron] Orc.
					put("chaser", "4_181");
					put("ambusher", "1_261");
					// Aragorn, Ranger of the North: a companion; a maneuver ability never used.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewPowerRisingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: New Power Rising
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Spot Saruman or an [isengard] Man, reveal your hand, and discard all Free Peoples cards revealed to take an [isengard] card into hand from your discard pile.
		*/

		var scn = GetScenario();

		var power = scn.GetFreepsCard("power");

		assertEquals("New Power Rising", power.getBlueprint().getTitle());
		assertNull(power.getBlueprint().getSubtitle());
		assertFalse(power.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, power.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, power.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, power.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(power, Timeword.REGROUP));
		assertEquals(2, power.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGrimaSpottedRevealsTheHandDiscardsItsFreePeoplesCardsAndTakesAnIsengardCardFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var grima = scn.GetShadowCard("grima");
		var savage = scn.GetShadowCard("savage");
		var sword = scn.GetShadowCard("sword");
		var alive = scn.GetShadowCard("alive");
		var guard = scn.GetShadowCard("guard");
		var axe = scn.GetFreepsCard("axe");
		var boromir = scn.GetShadowCard("boromir");
		var armor = scn.GetShadowCard("armor");
		var defiance = scn.GetShadowCard("defiance");
		var chaser = scn.GetShadowCard("chaser");
		var ambusher = scn.GetShadowCard("ambusher");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(power, boromir, armor, defiance, chaser, ambusher);
		scn.MoveCardsToDiscard(savage, sword, alive, guard, axe);

		scn.StartGame();
		assertInZone(Zone.HAND, power, boromir, armor, defiance, chaser, ambusher);
		assertInZone(Zone.DISCARD, savage, sword, alive, guard, axe);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(grima);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);

		// The whole hand is shown to the Free Peoples player, and nothing to its owner.
		assertEquals(5, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(chaser, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(ambusher, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		scn.FreepsDismissRevealedCards();

		// Every Free Peoples card revealed is discarded; the Shadow cards stay.
		assertInZone(Zone.DISCARD, boromir, armor, defiance);
		assertInZone(Zone.HAND, chaser, ambusher);

		// An [isengard] character, item and event; not the [sauron] card, nor the Free Peoples cards just
		// discarded, nor the [isengard] card in the other player's discard pile, nor this event, still being
		// played.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, sword, alive));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, boromir, armor, defiance, axe, power));
		scn.ShadowChooseCard(sword);
		assertInZone(Zone.HAND, sword);
		assertInZone(Zone.DISCARD, savage, alive, guard, power);
		assertInZone(Zone.DISCARD, axe);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheRohirrimTraitorSpottedAnEmptyHandIsRevealedAndAnIsengardCardTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var traitor = scn.GetShadowCard("traitor");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(power);
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		assertInZone(Zone.HAND, power);
		assertInZone(Zone.DISCARD, savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(traitor);
		scn.FreepsPass();

		// The event is the whole hand: once it is played the reveal shows nothing, and is paid.
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(power));
		scn.MarkGameLog();
		scn.ShadowPlayCard(power);
		assertFalse(scn.FreepsDecisionAvailable("Hand of"));
		assertTrue(scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		// The one [isengard] card in the pile is taken unasked.
		assertInZone(Zone.HAND, savage);
		assertInZone(Zone.DISCARD, power);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheIsengardSarumanSpottedAnIsengardCardIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(power);
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		assertInZone(Zone.HAND, power);
		assertInZone(Zone.DISCARD, savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(saruman);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);
		assertInZone(Zone.HAND, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheDunlandSarumanSpottedAnIsengardCardIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var rabblerouser = scn.GetShadowCard("rabblerouser");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(power);
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		assertInZone(Zone.HAND, power);
		assertInZone(Zone.DISCARD, savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(rabblerouser);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);
		assertInZone(Zone.HAND, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheFollowerSarumanSpottedAnIsengardCardIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var follower = scn.GetShadowCard("follower");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(power);
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		assertInZone(Zone.HAND, power);
		assertInZone(Zone.DISCARD, savage);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(follower);
		assertInZone(Zone.SUPPORT, follower);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);
		assertInZone(Zone.HAND, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoSarumanAndNoIsengardMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var savage = scn.GetShadowCard("savage");
		var footmen = scn.GetShadowCard("footmen");
		var sword = scn.GetShadowCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(power);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();
		assertInZone(Zone.HAND, power);
		assertInZone(Zone.DISCARD, sword);
		scn.SkipToPhase(Phase.REGROUP);
		// An [isengard] Uruk-hai who is no Man, and a Man of [dunland].
		scn.MoveMinionsToTable(savage, footmen);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(power));
	}

	@Test
	public void WithNoIsengardCardInTheDiscardPileTheFreePeoplesCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var power = scn.GetShadowCard("power");
		var grima = scn.GetShadowCard("grima");
		var guard = scn.GetShadowCard("guard");
		var boromir = scn.GetShadowCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(power, boromir);
		scn.MoveCardsToDiscard(guard);

		scn.StartGame();
		assertInZone(Zone.HAND, power, boromir);
		assertInZone(Zone.DISCARD, guard);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(grima);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(power));
		scn.ShadowPlayCard(power);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, false));
		scn.FreepsDismissRevealedCards();
		assertInZone(Zone.DISCARD, boromir, guard, power);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
