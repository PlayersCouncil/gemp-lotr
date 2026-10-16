package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("intimidate", "1_76");
					put("intimidate2", "1_76");
					put("intimidate3", "1_76");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: a fellowship ability never used here
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; stronger only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally, vitality 2; a maneuver ability never used here
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, damage +1 and nothing else
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; adds (2) when played, which it never is here
					put("picket", "6_101");     // Gate Picket: maneuver, exerts twice to wound each companion and ally not of a culture the Free Peoples player names
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntimidateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Intimidate
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: 
		 * Game Text: <b>Spell</b>.<br><b>Response:</b> If a companion is about to take a wound, spot Gandalf to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("intimidate");

		assertEquals("Intimidate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PreventsOneOfTheTwoWoundsOfALostSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intimidate = scn.GetFreepsCard("intimidate");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(intimidate);

		scn.StartGame();
		assertInHand(intimidate);
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, party);
		scn.FreepsPass();
		scn.ShadowPass();

		// Strength 8 loses to 9, and damage +1 makes it two wounds.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(intimidate);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(intimidate));
		scn.FreepsChooseOptionalTrigger(intimidate);

		// That wound is prevented and the other is taken.
		assertInDiscard(intimidate);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OfEveryoneWoundedByGatePicketOnlyTheCompanionsAreOfferedIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intimidate = scn.GetFreepsCard("intimidate");
		var intimidate2 = scn.GetFreepsCard("intimidate2");
		var intimidate3 = scn.GetFreepsCard("intimidate3");
		var gandalf = scn.GetFreepsCard("gandalf");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		var picket = scn.GetShadowCard("picket");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(intimidate, intimidate2, intimidate3);

		scn.StartGame();
		assertInHand(intimidate, intimidate2, intimidate3);
		scn.MoveMinionsToTable(picket);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.ShadowUseCardAction(picket);
		// No culture is named, so every companion and ally is wounded, one at a time.
		scn.FreepsChooseCards();

		// The Ring-bearer, a [shire] Hobbit, and Gandalf, a [gandalf] Wizard: each wound is answered.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(intimidate));
		scn.FreepsChooseOptionalTrigger(intimidate);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(intimidate2));
		scn.FreepsChooseOptionalTrigger(intimidate2);

		// The ally's wound lands with the third copy still in hand and nothing asked.
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertInDiscard(intimidate, intimidate2);
		assertInHand(intimidate3);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWithAGandalfCompanionWhoIsNotGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intimidate = scn.GetFreepsCard("intimidate");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		var picket = scn.GetShadowCard("picket");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(intimidate);

		scn.StartGame();
		assertInHand(intimidate);
		scn.MoveMinionsToTable(picket);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowUseCardAction(picket);
		scn.FreepsChooseCards();

		// Two companions wounded, and no offer for either.
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInHand(intimidate);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAMinionIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var intimidate = scn.GetFreepsCard("intimidate");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(intimidate);

		scn.StartGame();
		assertInHand(intimidate);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		scn.FreepsPass();
		scn.ShadowPass();

		// Strength 8 beats 5: the Runner's wound kills it, with nothing asked.
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(intimidate);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
