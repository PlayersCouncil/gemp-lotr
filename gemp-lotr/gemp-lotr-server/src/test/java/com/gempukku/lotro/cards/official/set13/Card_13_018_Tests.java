package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "13_18");
					put("elfcard", "1_53");     // Lorien Elf: an [elven] companion, for the top of the draw deck
					put("count", "4_69");       // Final Count: an [elven] condition, for the top of the draw deck
					put("armor", "1_8");        // Dwarven Armor: a [dwarven] possession, for the top of the draw deck
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Legolas, Of the Grey Company
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Archer</b>.<br>At the start of each archery phase, you may reveal the top card of your draw deck. If it is an [elven] card, wound a minion.
		*/

		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", legolas.getBlueprint().getTitle());
		assertEquals("Of the Grey Company", legolas.getBlueprint().getSubtitle());
		assertTrue(legolas.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, legolas.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, legolas.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, legolas.getBlueprint().getCardType());
		assertEquals(Race.ELF, legolas.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(2, legolas.getBlueprint().getTwilightCost());
		assertEquals(6, legolas.getBlueprint().getStrength());
		assertEquals(3, legolas.getBlueprint().getVitality());
		assertEquals(6, legolas.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfTheArcheryPhaseAnElvenCardRevealedLetsHimWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var elfcard = scn.GetFreepsCard("elfcard");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		// The Lorien Elf on top, the Dwarven Armor under it.
		scn.MoveCardsToTopOfDeck(armor, elfcard);
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();
		scn.MoveMinionsToTable(troop, savage);
		// Vitality 3 with 2 wounds: the Savage is exhausted, and a wound kills it.
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(savage));

		// The start of the maneuver phase, the phase before: nothing is offered.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();

		// The top card is shown to both players.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elfcard, false));
		assertTrue(scn.FreepsHasCardChoiceAvailable(elfcard, false));
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		scn.DismissRevealedCards();

		// An [elven] card: a minion is wounded, either of them; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// A wound, not an exertion: the exhausted Savage is killed.  The card revealed stays on top.
		assertInDiscard(savage);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ACardThatIsNotElvenIsRevealedAndNoMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var armor = scn.GetFreepsCard("armor");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(armor);
		assertSame(armor, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();
		scn.MoveMinionsToTable(troop, savage);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, false));
		scn.DismissRevealedCards();

		// A [dwarven] card: nothing is asked and nobody is wounded; the card stays on top.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertSame(armor, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void DeclinedNothingIsShownAndItIsOfferedAgainAtTheNextArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var count = scn.GetFreepsCard("count");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(count);
		assertSame(count, scn.GetFreepsTopOfDeck());
		int deck = scn.GetFreepsDeckCount();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.ShadowGetTotalRevealedCardCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// Legolas is an archer: the fellowship's archery wound goes on the one minion.  No skirmish is fought.
		scn.PassArcheryActions();
		assertEquals(1, scn.GetWoundsOn(troop));
		scn.PassAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowRefreshDecision();

		// The next archery phase, after the second move: offered again.  Final Count, an [elven] condition, is
		// on top; the Troop, the only minion, is wounded unasked.
		scn.SkipToPhase(Phase.ARCHERY);
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(count, false));
		scn.DismissRevealedCards();
		assertEquals(2, scn.GetWoundsOn(troop));
		assertSame(count, scn.GetFreepsTopOfDeck());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheRevealIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SetFreepsDrawDeck();
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// Nothing to reveal: not offered (Ketura's ruling, Q-0348, as Glorfindel 9_16's Q-0148).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void WithOneCardLeftInTheDrawDeckTheRevealIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var elfcard = scn.GetFreepsCard("elfcard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.SetFreepsDrawDeck(elfcard);
		assertEquals(1, scn.GetFreepsDeckCount());
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// The last card is enough: offered, revealed, and the one minion wounded.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(elfcard, false));
		scn.DismissRevealedCards();
		assertEquals(1, scn.GetWoundsOn(troop));
		assertSame(elfcard, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
