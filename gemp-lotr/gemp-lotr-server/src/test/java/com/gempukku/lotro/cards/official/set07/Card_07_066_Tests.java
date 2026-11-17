package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("safe", "7_66");
					// Sméagol, Poor Creature: the [gollum] Sméagol; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability used only on purpose
					put("smeagol2", "103_116");
					// Aragorn, Ranger of the North: a companion who could exert and is no Sméagol; a maneuver ability used only on purpose
					put("aragorn", "1_89");

					// Cards for the Shadow hand, of four cultures and both sides.
					put("runner1", "1_178");    // Goblin Runner: a [moria] minion
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: an [isengard] minion
					put("strike", "1_3");       // Axe Strike: a [dwarven] Free Peoples event
					put("lorien", "1_53");      // Lórien Elf: an [elven] Free Peoples companion
					// Goblin Patrol Troop: a [moria] minion with no text, left in the Shadow draw deck
					put("troop", "1_177");
					// No Business of Ours: "The Free Peoples player may not look at or reveal cards in any Shadow
					// player's hand."
					put("business", "2_44");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoSafePlacesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: No Safe Places
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Exert Sméagol to reveal an opponent's hand. That opponent must discard a card from hand for each culture revealed.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("safe");

		assertEquals("No Safe Places", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SmeagolExertsTheWholeShadowHandIsShownAndItsOwnerDiscardsOneCardForEachCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var safe = scn.GetFreepsCard("safe");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		// An [elven] card of the Free Peoples player's own hand: not in "an opponent's hand".
		var ownLorien = scn.GetFreepsCard("lorien");
		// Four cards of three cultures: two [moria] minions, an [isengard] minion and a [dwarven] Free Peoples event.
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var savage = scn.GetShadowCard("savage");
		var strike = scn.GetShadowCard("strike");
		// Stays in the Shadow draw deck.
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(safe, ownLorien);
		scn.MoveCardsToHand(runner1, runner2, savage, strike);
		assertInZone(Zone.HAND, runner1, runner2, savage, strike, ownLorien);

		scn.StartGame();
		// No minion is in play, so the game goes from the move straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.FreepsPlayAvailable(safe));

		scn.MarkGameLog();
		scn.FreepsPlayCard(safe);
		// Sméagol is the only Sméagol: he exerts without a choice, and Aragorn does not.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The whole hand is shown to the player of the event; its owner is told by the game log.
		assertEquals(4, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner1, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner2, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, false));
		assertFalse(scn.FreepsHasCardChoiceAvailable(troop, false));
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(runner1, savage, strike));
		assertFalse(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkMentions(troop));

		scn.FreepsDismissRevealedCards();
		// [moria], [isengard] and [dwarven]: three cultures among four cards, so 3 to discard, not 4; the
		// Free Peoples player's own [elven] card is not counted.  Which three is for the Shadow player to say.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2, savage, strike));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ownLorien));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(runner2, savage, strike);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, runner2, savage, strike);
		assertInZone(Zone.HAND, runner1, ownLorien);
		assertInZone(Zone.DISCARD, safe);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SmeagolOnceLikeUsExertsAndAHandOfOneCultureLosesOneCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var safe = scn.GetFreepsCard("safe");
		var smeagol2 = scn.GetFreepsCard("smeagol2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(smeagol2, aragorn);
		scn.MoveCardsToHand(safe);
		scn.MoveCardsToHand(runner1, runner2);
		assertInZone(Zone.HAND, runner1, runner2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(smeagol2));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(safe));

		scn.FreepsPlayCard(safe);
		// The [shire] Sméagol pays as the [gollum] one does.
		assertEquals(1, scn.GetWoundsOn(smeagol2));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.FreepsGetTotalRevealedCardCount());

		scn.FreepsDismissRevealedCards();
		// Two cards of one culture: one card to discard.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner1, runner2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(runner1);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, runner1);
		assertInZone(Zone.HAND, runner2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileSmeagolIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var safe = scn.GetFreepsCard("safe");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		// Sméagol, vitality 4, is exhausted; a healthy companion who is no Sméagol stands beside him.
		scn.AddWoundsToChar(smeagol, 3);
		scn.MoveCardsToHand(safe);
		scn.MoveCardsToHand(runner1);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(safe));
	}

	@Test
	public void CannotBePlayedWithNoSmeagolThoughAnotherCompanionCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var safe = scn.GetFreepsCard("safe");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(safe);
		scn.MoveCardsToHand(runner1);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(safe));
	}

	@Test
	public void AgainstAnEmptyHandItIsStillPlayedAndOnlyItsCostIsPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var safe = scn.GetFreepsCard("safe");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(safe);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsPlayAvailable(safe));

		scn.MarkGameLog();
		scn.FreepsPlayCard(safe);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.DISCARD, safe);
		assertEquals(0, scn.GetShadowDiscardCount());
		// Nobody is shown or asked anything, and the game log says that there was nothing to reveal.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertTrue(scn.GetGameLogSinceMark().toString(), scn.GameLogSinceMarkSays("no cards in " + scn.P2() + "'s hand"));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WhileTheHandMayNotBeRevealedNoCultureIsRevealedAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var safe = scn.GetFreepsCard("safe");
		var smeagol = scn.GetFreepsCard("smeagol");
		var business = scn.GetShadowCard("business");
		var runner1 = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(safe);
		scn.MoveCardsToSupportArea(business);
		scn.MoveCardsToHand(runner1, savage);
		assertInZone(Zone.HAND, runner1, savage);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// The event may be played whatever it will do; the reveal is its effect, and that is what is barred.
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.FreepsPlayAvailable(safe));

		scn.FreepsPlayCard(safe);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.DISCARD, safe);
		// Two cultures are in that hand, and none was revealed.
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.HAND, runner1, savage);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
