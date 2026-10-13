package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("terrible", "7_50");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4
					// Radagast, The Brown: a [gandalf] Wizard ally who is no Gandalf, vitality 4; a fellowship ability used only on purpose
					put("radagast", "31_17");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] Orc who is no Nazgul, vitality 2; an archery bonus at 3 burdens only
					put("witchking", "8_84");   // The Witch-king, Black Captain: [wraith] Nazgul, vitality 4; a text about Men he skirmishes
					// Ulaire Attea, Khamul of the East: a [raider] Nazgul, vitality 3; bonuses at 3 and 5 burdens only
					put("khamul", "103_61");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TerribleandEvilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Terrible and Evil
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.<br>Exert Gandalf X times to wound a minion X times. If that minion is a Nazgûl, wound it again.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("terrible");

		assertEquals("Terrible and Evil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfTwiceToWoundOneMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terrible = scn.GetFreepsCard("terrible");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		var creeper = scn.GetShadowCard("creeper");
		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terrible);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, creeper, witchking, khamul);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(terrible));
		scn.FreepsPlayCard(terrible);

		// X is at least 1 (CR 4.2 "exert") and at most 3: vitality 4 less the 1 he must keep.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");
		assertEquals(2, scn.GetWoundsOn(gandalf));

		// Minions of three cultures and three races; the companions are not minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, creeper, witchking, khamul));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);

		// Both wounds on the one minion chosen, none on any other.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(creeper));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInZone(Zone.DISCARD, terrible);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ExertsGandalfOnceToWoundAWraithOrcOnceAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terrible = scn.GetFreepsCard("terrible");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var creeper = scn.GetShadowCard("creeper");
		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terrible);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, creeper, witchking, khamul);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(creeper));
		scn.FreepsPlayCard(terrible);
		scn.FreepsChoose("1");
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseCard(creeper);

		// A [wraith] minion who is no Nazgul takes X wounds and not one more: with vitality 2 he is still there.
		assertEquals(1, scn.GetWoundsOn(creeper));
		assertInZone(Zone.SHADOW_CHARACTERS, creeper);
		// "That minion": the Nazgul beside him are not wounded.
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AWraithNazgulChosenIsWoundedOnceMoreThanX() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terrible = scn.GetFreepsCard("terrible");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terrible);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, witchking, khamul);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(witchking));
		scn.FreepsPlayCard(terrible);
		scn.FreepsChoose("2");
		assertEquals(2, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseCard(witchking);

		// 2 for X + 1 again for a Nazgul; vitality 4 keeps him in play.
		assertEquals(3, scn.GetWoundsOn(witchking));
		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		// The other Nazgul is not "that minion".
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ANazgulOfAnotherCultureChosenIsWoundedOnceMoreThanX() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terrible = scn.GetFreepsCard("terrible");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(terrible);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, witchking, khamul);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(khamul));
		scn.FreepsPlayCard(terrible);
		scn.FreepsChoose("1");
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseCard(khamul);

		// A [raider] Nazgul: 1 for X + 1 again; vitality 3 keeps him in play.
		assertEquals(2, scn.GetWoundsOn(khamul));
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileGandalfIsExhaustedThoughOtherGandalfCharactersCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terrible = scn.GetFreepsCard("terrible");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var radagast = scn.GetFreepsCard("radagast");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToSupportArea(radagast);
		scn.MoveCardsToHand(terrible);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(gandalf, 3);
		scn.SkipToPhase(Phase.MANEUVER);

		// Exhausted, Gandalf cannot exert even once; the [gandalf] companion and the [gandalf] Wizard beside him
		// could, but neither is Gandalf.
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(radagast));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(terrible));

		// Able to exert once, he can pay for an X of 1.
		scn.RemoveWoundsFromChar(gandalf, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(terrible));
	}
}
