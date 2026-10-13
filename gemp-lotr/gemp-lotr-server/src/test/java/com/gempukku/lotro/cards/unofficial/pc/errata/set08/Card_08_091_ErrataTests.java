package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_091_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "58_91");

					put("eowyn", "5_122");      // Eowyn: unique [rohan] Man companion; a maneuver ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, cost 2, Valiant and nothing else
					put("aldor", "4_262");      // Aldor: [rohan] ally, the only [rohan] character that is no companion; an archery ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and nothing else

					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc minion, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, no text
					put("ladder", "6_58");      // Assault Ladder: [isengard] support condition, abilities used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Rohirrim Army
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot 2 [rohan] companions.
		 * You may play Rohirrim Army any time you could play a regroup event.
		 * When you play Rohirrim Army, you may discard a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Rohirrim Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void CannotBePlayedUntilASecondRohanCompanionIsPlayedAndThenPlaysForFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var aldor = scn.GetFreepsCard("aldor");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(eowyn, knight);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(army, guard);

		scn.StartGame();

		// One [rohan] companion, a [rohan] ally and a [gondor] companion: not enough.
		assertFalse(scn.FreepsPlayAvailable(army));

		scn.FreepsPlayCard(guard);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(army));

		scn.FreepsPlayCard(army);
		// 2 for Household Guard + 5
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, army);
		// No minion in play, so there is nothing to discard and nothing is asked.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBePlayedInTheRegroupPhaseAndDiscardsAChosenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var patrol = scn.GetShadowCard("patrol");
		var brigand = scn.GetShadowCard("brigand");
		var ladder = scn.GetShadowCard("ladder");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveCardsToHand(army);

		scn.StartGame();
		// No minions, so the game goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(patrol, brigand);
		scn.MoveCardsToSupportArea(ladder);
		scn.SetTwilight(1);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(army));
		scn.FreepsPlayCard(army);

		// 1 + 5
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, army);
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, brigand));
		// Not a Shadow support condition, nor a character who is no minion (a companion, the Army itself).
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ladder, eowyn, guard, army));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(brigand);

		assertInDiscard(brigand);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertInZone(Zone.SUPPORT, ladder);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void MayDeclineToDiscardAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var patrol = scn.GetShadowCard("patrol");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveCardsToHand(army);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(patrol, brigand);

		scn.FreepsPlayCard(army);
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, brigand));
		scn.FreepsDeclineChoosing();

		assertInZone(Zone.FREE_CHARACTERS, army);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol, brigand);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveCardsToHand(army);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(army));
	}

	@Test
	public void PlayingAnotherRohanCompanionWithTheArmyInPlayDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCompanionsToTable(army, eowyn);
		scn.MoveCardsToHand(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);

		scn.FreepsPlayCard(guard);

		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
