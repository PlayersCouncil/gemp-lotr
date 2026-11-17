package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lembas", "18_15");
					put("lembas2", "18_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; stronger only against a Nazgul
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, cost 2; an archery ability only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an [elven] Elf who is an ally; his start-of-turn heal finds nobody wounded
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability never used here

					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase; its play trigger is not reached by a cheat
					// Cards with no text, for the hand and for the draw deck a draw up to 8 takes from
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
					put("filler7", "1_177");
					put("filler8", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LembasBreadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Lembas Bread
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: 
		 * Game Text: To play, spot 2 [elven] companions. Bearer must be a companion.
		 * 	<b>Regroup:</b> Discard this possession from play to reconcile your hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lembas");

		assertEquals("Lembas Bread", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoElvenCompanionsAreSpottedAndThenGoesOnAnyCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lembas = scn.GetFreepsCard("lembas");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(lembas, legolas);
		assertInHand(lembas, legolas);

		scn.StartGame();

		// One [elven] companion, an [elven] ally and a [gondor] companion: not enough.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(lembas));

		// A second [elven] companion, of another title
		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.FreepsPlayAvailable(lembas));
		scn.FreepsPlayCard(lembas);

		// Bearer must be a companion: companions of three cultures and races, the Ring-bearer among them; not the ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, arwen, legolas, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(lembas, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityDiscardsThisPossessionToReconcileTheHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lembas = scn.GetFreepsCard("lembas");
		var lembas2 = scn.GetFreepsCard("lembas2");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(arwen, lembas);
		// The second copy, on another companion, is not the one discarded.
		scn.AttachCardsTo(aragorn, lembas2);
		scn.MoveCardsToHand(filler1, filler2, filler3);
		assertAttachedTo(lembas, arwen);
		assertAttachedTo(lembas2, aragorn);
		assertInHand(filler1, filler2, filler3);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(lembas));
		scn.FreepsUseCardAction(lembas);

		assertInDiscard(lembas);
		assertAttachedTo(lembas2, aragorn);

		// The reconcile: its own discard of one is declined, and he draws from three cards to eight.
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();
		assertEquals(8, scn.GetFreepsHandCount());
		assertEquals(deck - 5, scn.GetFreepsDeckCount());
		assertInHand(filler1, filler2, filler3);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lembas = scn.GetFreepsCard("lembas");
		var arwen = scn.GetFreepsCard("arwen");
		var filler1 = scn.GetFreepsCard("filler1");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, lembas);
		scn.MoveCardsToHand(filler1);
		assertAttachedTo(lembas, arwen);
		assertInHand(filler1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(lembas));
		assertAttachedTo(lembas, arwen);
	}
}
