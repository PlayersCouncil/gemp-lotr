package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_033_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("surprise", "15_33");
					// Gandalf, Returned: strength 7, resistance 7, so that his resistance is neither his strength
					// nor any other companion's; his texts answer his own play and his own win
					put("gandalf", "17_17");
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf
					put("merry", "1_302");      // Merry, Friend to Sam: unbound [shire] Hobbit, strength 3, resistance 6
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man, strength 8, resistance 6
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					// Goblin Pursuer: [moria] Orc, strength 6, vitality 1; +3 only after a second move
					put("pursuer", "2_62");
					put("pursuer2", "2_62");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OneLastSurpriseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: One Last Surprise
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Spell</b>. Spot Gandalf and an unbound companion skirmishing a minion to use Gandalf's resistance to resolve that skirmish instead of that companion's strength.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("surprise");

		assertEquals("One Last Surprise", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGandalfsResistanceOf7AHobbitOfStrength3WinsAgainstAMinionOfStrength6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surprise = scn.GetFreepsCard("surprise");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.MoveCardsToHand(surprise);
		assertInZone(Zone.HAND, surprise);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, pursuer);

		// Merry's 3 loses to 6, and so would his own resistance of 6 (a tie goes to the Shadow). Gandalf, in no
		// skirmish, has resistance 7.
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(6, scn.GetResistance(merry));
		assertEquals(6, scn.GetStrength(pursuer));
		assertEquals(7, scn.GetResistance(gandalf));
		assertTrue(scn.FreepsPlayAvailable(surprise));
		scn.FreepsPlayCard(surprise);
		assertInDiscard(surprise);

		scn.PassCurrentPhaseActions();
		// 7 against 6: Merry wins and is not wounded, and the Goblin Pursuer dies of its one wound.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertInDiscard(pursuer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GandalfsResistanceIsReadAsItStandsSoWithABurdenAragornLosesASkirmishHisStrengthWouldWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surprise = scn.GetFreepsCard("surprise");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(surprise);
		assertInZone(Zone.HAND, surprise);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, pursuer);

		// One burden: Gandalf's resistance is 6, a tie with the Goblin Pursuer's 6. Aragorn's strength of 8,
		// Gandalf's of 7 and Gandalf's printed resistance of 7 would each win.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetResistance(gandalf));
		assertEquals(6, scn.GetStrength(pursuer));
		scn.FreepsPlayCard(surprise);
		assertInDiscard(surprise);

		scn.PassCurrentPhaseActions();
		// A tie goes to the Shadow: Aragorn loses and takes a wound, and the Goblin Pursuer is untouched.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWithAGandalfCompanionWhoIsNoGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surprise = scn.GetFreepsCard("surprise");
		var avenger = scn.GetFreepsCard("avenger");
		var merry = scn.GetFreepsCard("merry");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(avenger, merry);
		scn.MoveCardsToHand(surprise);
		assertInZone(Zone.HAND, surprise);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, pursuer);

		// An unbound companion skirmishing a minion, and a [gandalf] companion, but no Gandalf.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(surprise));
	}

	@Test
	public void IsNotPlayableInARingBoundCompanionsSkirmishWhileAnUnboundCompanionWaitsForHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surprise = scn.GetFreepsCard("surprise");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var pursuer = scn.GetShadowCard("pursuer");
		var pursuer2 = scn.GetShadowCard("pursuer2");
		scn.MoveCompanionsToTable(gandalf, merry, sam);
		scn.MoveCardsToHand(surprise);
		assertInZone(Zone.HAND, surprise);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, pursuer2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, pursuer}, new PhysicalCardImpl[]{merry, pursuer2});
		scn.FreepsResolveSkirmish(sam);

		// Sam is skirmishing a minion but is Ring-bound; Merry is unbound but his skirmish has not begun.
		assertTrue(scn.IsCharAssignedAgainst(merry, pursuer2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(surprise));
	}

	/**
	 * Ketura's ruling (Q-0398: B): "Spot Gandalf and an unbound companion" is two cards, so Gandalf alone in a
	 * skirmish is not "an unbound companion skirmishing a minion" and the event is not played there.
	 */
	@Test
	public void IsNotPlayableInGandalfsOwnSkirmishSinceGandalfIsNotTheUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var surprise = scn.GetFreepsCard("surprise");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.MoveCardsToHand(surprise);
		assertInZone(Zone.HAND, surprise);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, pursuer);

		// Gandalf, an unbound companion, is the only companion skirmishing a minion; Merry, unbound, is in no
		// skirmish. Read as one card, Gandalf would meet both spots.
		assertTrue(scn.IsCharSkirmishing(gandalf));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(surprise));
	}
}
