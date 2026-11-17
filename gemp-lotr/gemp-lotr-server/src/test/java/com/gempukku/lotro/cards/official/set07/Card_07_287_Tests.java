package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_287_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "7_287");
					put("guard2", "7_287");
					put("swordsman", "3_98");   // Orc Swordsman: [sauron] Orc, vitality 3; strength +2 only while he bears a weapon
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, vitality 3; a Shadow ability used only on purpose
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; its text answers a weapon played on it
					// Cirith Ungol Guard: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 is its whole text
					put("uruk", "10_81");
					put("morgul", "7_199");     // Morgul Soldier: a [wraith] Orc, vitality 2; Nazgul are strength +1 (none in play)

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man and a ranger; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man; a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a skirmish ability used only on purpose
					put("whatarethey", "1_119");  // What Are They?: Maneuver event. Spot a ranger to discard a roaming minion
					put("whatarethey2", "1_119");
					// Defend It and Hope: Maneuver event. Exert a [gondor] Man to wound a minion once (twice an Uruk-hai)
					put("defend", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mordor Guard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Skirmish:</b> Exert 3 [sauron] Orcs to add 3 threats.
		 * 	When this minion is discarded, remove 3 threats.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guard");

		assertEquals("Mordor Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsThreeSauronOrcsOfTheShadowPlayersChoiceToAddThreeThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var guard = scn.GetShadowCard("guard");
		var swordsman = scn.GetShadowCard("swordsman");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var chieftain = scn.GetShadowCard("chieftain");
		var uruk = scn.GetShadowCard("uruk");
		var morgul = scn.GetShadowCard("morgul");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, swordsman, taskmaster, chieftain, uruk, morgul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, swordsman}, new PhysicalCardImpl[]{boromir, guard});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Four companions, so all three threats can be added and a fourth would show.
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(morgul));
		// Guard's own skirmish is still to come: the ability is his to use in another's.
		assertTrue(scn.ShadowActionAvailable(guard));

		scn.ShadowUseCardAction(guard);

		// Every [sauron] Orc, Guard himself, the one skirmishing and the two in no skirmish; not the [sauron]
		// Uruk-hai nor the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, swordsman, taskmaster, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruk, morgul));
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(guard, taskmaster, chieftain);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(morgul));
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithOnlyTwoSauronOrcsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);

		var guard = scn.GetShadowCard("guard");
		var swordsman = scn.GetShadowCard("swordsman");
		var chieftain = scn.GetShadowCard("chieftain");
		var uruk = scn.GetShadowCard("uruk");
		var morgul = scn.GetShadowCard("morgul");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, swordsman, chieftain, uruk, morgul);
		// The Swordsman exhausted: vitality 3 with 2 wounds.
		scn.AddWoundsToChar(swordsman, 2);
		assertEquals(2, scn.GetWoundsOn(swordsman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, swordsman}, new PhysicalCardImpl[]{boromir, guard});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		// Guard and the Chieftain can exert, and so can the [sauron] Uruk-hai and the [wraith] Orc, who are no
		// [sauron] Orcs: three cannot be paid.
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(morgul));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(guard));
	}

	@Test
	public void DiscardedByAFreePeoplesEventGuardRemovesThreeThreatsOnceAndAnotherSauronOrcDiscardedRemovesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var whatarethey = scn.GetFreepsCard("whatarethey");
		var whatarethey2 = scn.GetFreepsCard("whatarethey2");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveCardsToHand(whatarethey, whatarethey2);

		var guard = scn.GetShadowCard("guard");
		var guard2 = scn.GetShadowCard("guard2");
		var swordsman = scn.GetShadowCard("swordsman");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, guard2, swordsman);
		// Four companions hold four threats.
		scn.AddThreats(4);

		scn.SkipToPhase(Phase.MANEUVER);

		// At site 2 every one of them, of site 6, is roaming.
		assertEquals(4, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, guard, guard2, swordsman);

		scn.FreepsPlayCard(whatarethey);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guard2, swordsman));
		scn.FreepsChooseCard(swordsman);

		// Another [sauron] Orc discarded: no threat removed.
		assertInZone(Zone.DISCARD, swordsman);
		assertEquals(4, scn.GetThreats());

		scn.ShadowPass();
		scn.FreepsPlayCard(whatarethey2);
		scn.FreepsChooseCard(guard);

		// Three removed, by the copy discarded alone: the copy still in play does not answer it.
		assertInZone(Zone.DISCARD, guard);
		assertInZone(Zone.SHADOW_CHARACTERS, guard2);
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void KilledGuardIsNotDiscardedAndRemovesNoThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.MoveCardsToHand(defend);

		var guard = scn.GetShadowCard("guard");
		var swordsman = scn.GetShadowCard("swordsman");

		scn.StartGame();
		scn.MoveMinionsToTable(guard, swordsman);
		scn.AddThreats(4);
		// Vitality 3 with 2 wounds: one more kills him.
		scn.AddWoundsToChar(guard, 2);

		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(guard));

		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, swordsman));
		scn.FreepsChooseCard(guard);

		assertInZone(Zone.DISCARD, guard);
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
