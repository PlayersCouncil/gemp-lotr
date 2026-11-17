package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("embassy", "11_5");
					// Dwarven Warrior: Dwarf companion; his text answers his own play only.
					put("warrior", "12_7");
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, cost 1; a maneuver ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability used only on purpose
					// Mountain Homestead: [dwarven] support-area condition; "While the fellowship is at a mountain site,
					// each Dwarf is strength +2", and no site of this path is a mountain.
					put("homestead", "11_12");
					put("runner", "1_178");     // Goblin Runner: a minion whose text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenEmbassyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Dwarven Embassy
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Dwarf.
		 * Each companion is <b>damage +1</b>.
		 * Discard this condition at the end of your turn.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("embassy");

		assertEquals("Dwarven Embassy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilADwarfIsSpottedAndADwarfAllyWillDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var embassy = scn.GetFreepsCard("embassy");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(embassy, thrarin);

		scn.StartGame();
		assertInHand(embassy, thrarin);

		// A Hobbit and a Man, and no Dwarf.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(embassy));

		scn.FreepsPlayCard(thrarin);
		assertInZone(Zone.SUPPORT, thrarin);
		assertTrue(scn.FreepsPlayAvailable(embassy));

		scn.FreepsPlayCard(embassy);
		assertInZone(Zone.SUPPORT, embassy);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedOnADwarfCompanionsWordEachCompanionIsDamagePlus1AndNoAllyOrMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var embassy = scn.GetFreepsCard("embassy");
		var warrior = scn.GetFreepsCard("warrior");
		var thrarin = scn.GetFreepsCard("thrarin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(warrior, aragorn);
		scn.MoveCardsToHand(embassy);

		scn.StartGame();
		assertInHand(embassy);
		// Laid early: the minion stands beside the companions later in the turn.
		scn.MoveMinionsToTable(runner);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));

		// The Dwarven Warrior is the only Dwarf to spot.
		scn.FreepsPlayCard(embassy);
		assertInZone(Zone.SUPPORT, embassy);

		// A Dwarf, a Man and a Hobbit, of three cultures.
		assertEquals(1, scn.GetKeywordCount(warrior, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		// An ally and a minion are no companions.  The ally is laid only now, so that the Warrior alone was spotted.
		scn.MoveAlliesToTable(thrarin);
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(runner, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardedAtTheEndOfTheTurnAndNotWhenTheRegroupPhaseEndsInAMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var embassy = scn.GetFreepsCard("embassy");
		var homestead = scn.GetFreepsCard("homestead");
		var warrior = scn.GetFreepsCard("warrior");
		scn.MoveCompanionsToTable(warrior);
		scn.MoveCardsToSupportArea(embassy, homestead);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, embassy, homestead);

		// No minion: from the Shadow phase the game goes to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SUPPORT, embassy, homestead);
		scn.FreepsChooseToMove();

		// The regroup phase is over and the turn is not: the Embassy is still in play, and companions still damage +1.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertInZone(Zone.SUPPORT, embassy, homestead);
		assertEquals(1, scn.GetKeywordCount(frodo, Keyword.DAMAGE));

		// The end of the turn: the Embassy is discarded, the other condition stays.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertInDiscard(embassy);
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
	}
}
