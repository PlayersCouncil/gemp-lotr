package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_028_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("wielder", "2_28");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; his own maneuver ability is not used here
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf; strength +2 only beside 3 Ents
					put("erland", "2_21");      // Erland, Advisor to Brand: a [gandalf] ally; his text is about the Free Peoples hand

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so its text does nothing
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WielderoftheFlameStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Wielder of the Flame
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.
		 * 		<b>Maneuver:</b> Spot Gandalf to make a companion <b>defender +1</b> until the regroup phase. Any Shadow player may remove (3) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wielder");

		assertEquals("Wielder of the Flame", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAChosenCompanionDefenderPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wielder = scn.GetFreepsCard("wielder");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var erland = scn.GetFreepsCard("erland");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(erland);
		scn.MoveCardsToHand(wielder);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToPhase(Phase.MANEUVER);
		// One short of what the Shadow player would need once the event's own (1) is in the pool.
		scn.SetTwilight(1);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(wielder));

		scn.FreepsPlayCard(wielder);
		// 1 in the pool + 1 for the event: too little to prevent with, so nobody is asked.
		assertEquals(2, scn.GetTwilight());
		assertFalse(scn.ShadowDecisionAvailable("prevent"));

		// Any companion, the Ring-bearer and Gandalf among them; not an ally, and not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(erland, runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertEquals(2, scn.GetTwilight());
		assertInDiscard(wielder);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		// Defender +1: Aragorn may take both minions, and Gandalf may not.
		assertTrue(scn.FreepsCanAssignAgainst(aragorn, runner, runner2));
		assertFalse(scn.FreepsCanAssignAgainst(gandalf, runner, runner2));
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{gandalf, runner2});

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Still in force in the second skirmish, with the first one over.
		scn.FreepsResolveSkirmish(gandalf);
		assertTrue(scn.IsCharSkirmishingAgainst(gandalf, runner2));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.PassSkirmishActions();

		// And gone as the regroup phase starts.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
	}

	@Test
	public void WielderAbilityCanBePrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wielder = scn.GetFreepsCard("wielder");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(wielder);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);

		scn.FreepsPlayCard(wielder);
		// 2 in the pool + 1 for the event: exactly the (3) the Shadow player needs.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("prevent"));

		scn.ShadowChooseYes();
		// (3) removed and nobody made defender; the event was played and paid for all the same.
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertInDiscard(wielder);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wielder = scn.GetFreepsCard("wielder");
		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(wielder);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(2);

		scn.FreepsPlayCard(wielder);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("prevent"));

		scn.ShadowChooseNo();
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gandalf));
		scn.FreepsChooseCard(gandalf);

		// Nothing was removed, and Gandalf himself is the defender.
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DEFENDER));
		assertInDiscard(wielder);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAGandalfCompanionWhoIsNotGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wielder = scn.GetFreepsCard("wielder");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(wielder);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Frodo and an Ent of the [gandalf] culture: nobody named Gandalf.
		assertInZone(Zone.HAND, wielder);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(wielder));
	}
}
