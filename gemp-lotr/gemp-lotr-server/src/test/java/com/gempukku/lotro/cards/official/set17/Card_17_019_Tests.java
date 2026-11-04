package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guidance", "17_19");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard; +1 strength for each of Hobbit, Dwarf, Elf, Man companion spotted
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Man; +2 only at 3 Ents
					put("dasron", "13_29");     // Dasron, Merchant From Dorwinion: a [gandalf] Man companion; his trigger needs 2 culture tokens, and there are none
					put("erland", "2_21");      // Erland, Advisor to Brand: a [gandalf] Man ally; his bar on looking at the hand reaches no test
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; his maneuver ability is never used

					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuidanceoftheIstariStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Guidance of the Istari
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a [gandalf] companion strength +1 for each [gandalf] Man you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guidance");

		assertEquals("Guidance of the Istari", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGandalfCompanionStrongerByOneForEachGandalfManSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guidance = scn.GetFreepsCard("guidance");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var dasron = scn.GetFreepsCard("dasron");
		var erland = scn.GetFreepsCard("erland");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, avenger, dasron, aragorn);
		scn.MoveAlliesToTable(erland);
		scn.MoveCardsToHand(guidance);
		assertInHand(guidance);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);
		// 6 + 1 (Frodo, a Hobbit) + 1 (a Man)
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(6, scn.GetStrength(dasron));
		assertTrue(scn.FreepsPlayAvailable(guidance));

		scn.FreepsPlayCard(guidance);
		// The [gandalf] companions of three races; not the [gondor] companion, not the [gandalf] ally.
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, avenger, dasron));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, erland));
		scn.FreepsChooseCard(gandalf);

		// Two [gandalf] Men, the companion Dasron and the ally Erland.  Counting every [gandalf] card would give 4
		// (Gandalf and the Ent Avenger too), every Man 3 (Aragorn too), [gandalf] Man companions alone 1.
		assertEquals(10, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(6, scn.GetStrength(dasron));
		assertInDiscard(guidance);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneGandalfManTheBonusIsOneOnACompanionInAnotherSkirmishAndEndsWithThisOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guidance = scn.GetFreepsCard("guidance");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dasron = scn.GetFreepsCard("dasron");
		var troop = scn.GetShadowCard("troop");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf, dasron);
		scn.MoveCardsToHand(guidance);
		assertInHand(guidance);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, party);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, troop}, new PhysicalCardImpl[]{dasron, party});
		scn.FreepsResolveSkirmish(gandalf);
		assertEquals(8, scn.GetStrength(gandalf));
		assertEquals(6, scn.GetStrength(dasron));

		// Dasron, whose own skirmish is still to come, is the one made stronger; he is the one [gandalf] Man.
		scn.FreepsPlayCard(guidance);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, dasron));
		scn.FreepsChooseCard(dasron);
		assertEquals(7, scn.GetStrength(dasron));
		assertEquals(8, scn.GetStrength(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gandalf 8 against 13 loses; Dasron's skirmish is a skirmish phase of its own, and the bonus is gone there.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.FreepsResolveSkirmish(dasron);
		assertEquals(6, scn.GetStrength(dasron));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guidance = scn.GetFreepsCard("guidance");
		var gandalf = scn.GetFreepsCard("gandalf");
		var dasron = scn.GetFreepsCard("dasron");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, dasron);
		scn.MoveCardsToHand(guidance);
		assertInHand(guidance);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(guidance));
		assertInHand(guidance);
	}

	@Test
	public void WithNoGandalfCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guidance = scn.GetFreepsCard("guidance");
		var erland = scn.GetFreepsCard("erland");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(erland);
		scn.MoveCardsToHand(guidance);
		assertInHand(guidance);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(erland));
		assertTrue(scn.FreepsPlayAvailable(guidance));

		scn.FreepsPlayCard(guidance);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(erland));
		assertInDiscard(guidance);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
