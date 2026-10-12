package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_256_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sang", "57_256");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3; a skirmish ability
					put("deor", "7_222");       // Deor: [rohan] Man ally, vitality 2; a skirmish ability that needs initiative
					put("steed", "7_253");      // Swift Steed: [rohan] mount, bearer must be a [rohan] Man; acts only in skirmishes
					put("steed2", "7_253");
					put("arod", "13_1");        // Arod: [dwarven] mount, bearer must be a Dwarf; acts only in skirmishes or when played
					put("sword", "4_291");      // Sword of Rohan: [rohan] hand weapon, not a mount; acts only in skirmishes

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheySangasTheySlewStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: They Sang as They Slew
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Exert X mounted companions to wound X minions and remove X burdens.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sang");

		assertEquals("They Sang as They Slew", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTwoMountedCompanionsToWoundTwoMinionsAndRemoveTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var gimli = scn.GetFreepsCard("gimli");
		var deor = scn.GetFreepsCard("deor");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.MoveCardsToHand(sang);
		scn.MoveCompanionsToTable(guard, guard2, gimli);
		scn.MoveAlliesToTable(deor);
		// Two mounted companions of different cultures and races; a mounted ally; a companion bearing a
		// possession that is not a mount.
		scn.AttachCardsTo(guard, scn.GetFreepsCard("steed"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("arod"));
		scn.AttachCardsTo(deor, scn.GetFreepsCard("steed2"));
		scn.AttachCardsTo(guard2, scn.GetFreepsCard("sword"));

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop, savage, savage2);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertTrue(scn.FreepsPlayAvailable(sang));

		scn.FreepsPlayCard(sang);

		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deor, guard2, frodo));
		scn.FreepsChooseCards(guard, gimli);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(0, scn.GetWoundsOn(guard2));

		// Two different minions, of any culture; no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, savage2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(troop, savage);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(savage2));

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(sang);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ExertingOneMountedCompanionWoundsOneMinionAndRemovesOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(sang);
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, scn.GetFreepsCard("steed"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("arod"));

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetBurdens());
		scn.FreepsPlayCard(sang);
		scn.FreepsChooseCards(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneMinionTwoExertionsWoundItOnceAndStillRemoveTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(sang);
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, scn.GetFreepsCard("steed"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("arod"));

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(sang);
		scn.FreepsChooseCards(guard, gimli);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// "Wound X minions" wounds X different minions; with one minion there is one wound to place.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyMountedCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var gimli = scn.GetFreepsCard("gimli");
		var deor = scn.GetFreepsCard("deor");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(sang);
		scn.MoveCompanionsToTable(guard, guard2, gimli);
		scn.MoveAlliesToTable(deor);
		scn.AttachCardsTo(guard, scn.GetFreepsCard("steed"));
		scn.AttachCardsTo(deor, scn.GetFreepsCard("steed2"));
		scn.AttachCardsTo(guard2, scn.GetFreepsCard("sword"));
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(guard, 2);

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		// Fresh companions who are not mounted and a fresh mounted ally cannot pay, and X may not be zero.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertFalse(scn.FreepsPlayAvailable(sang));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtLeastOneMountedCompanionMustExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(sang);
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, scn.GetFreepsCard("steed"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("arod"));

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(sang);
		// A cost of exerting X characters cannot be paid by exerting none (CR 4.2 "exert").
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(sang);
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, scn.GetFreepsCard("steed"));

		scn.StartGame();
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sang));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(sang));
	}

	@Test
	public void XIsLimitedOnlyByTheMountedCompanionsAbleToExertWhateverThereIsToWoundOrRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCardsToHand(sang);
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, scn.GetFreepsCard("steed"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("arod"));

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(sang);
		// One burden and one minion are all a second exertion could act on, but the text puts no cap on X: both
		// mounted companions can exert, so both may.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard, gimli);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(gimli));
		// Only one minion to wound once and one burden to remove.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void XCanReachTheMinionsInPlayWhenTheyOutnumberTheBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCardsToHand(sang);
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, scn.GetFreepsCard("steed"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("arod"));

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetBurdens());
		scn.FreepsPlayCard(sang);
		// One burden but two minions: both mounted companions may exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, gimli));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard, gimli);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Both minions must be wounded, so the game chooses them.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
