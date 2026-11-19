package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_06_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "6_78");
					put("army2", "6_78");       // a second copy: its own exertion is not this one's
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: an Easterling who is a Nazgul, strength 12;
					                            // while 3 burdens can be spotted each Easterling is strength +3 (damage +1 at 5)
					put("attacker", "7_141");   // Easterling Attacker: an Easterling Man, strength 6; a Skirmish ability only
					put("sentry", "4_253");     // Southron Sentry: a [raider] Man who is no Easterling, strength 9; a Maneuver ability only
					put("scout", "15_77");      // Easterling Scout: a [men] minion, strength 9, with "Easterling" in his title only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a Maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Easterling Army
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 13
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>. <b>Fierce</b>.
		 * <b>Skirmish:</b> Spot 2 burdens and exert this minion to make an Easterling strength +1.
		 * <b>Skirmish:</b> Spot 4 burdens and exert this minion to make an Easterling strength +2.
		 * <b>Skirmish:</b> Spot 6 burdens and exert this minion to make an Easterling strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Easterling Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(13, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTwoBurdensItExertsToMakeAnEasterlingStrengthPlusOneForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var army = scn.GetShadowCard("army");
		var army2 = scn.GetShadowCard("army2");
		var khamul = scn.GetShadowCard("khamul");
		var attacker = scn.GetShadowCard("attacker");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(army, army2, khamul, attacker, sentry, scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, army}, new PhysicalCardImpl[]{boromir, khamul});
		scn.ShadowDeclineAssignments();

		assertEquals(2, scn.GetBurdens());
		assertEquals(13, scn.GetStrength(army));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(0, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(army2));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(army, "strength +1"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +3"));
		scn.ShadowUseCardAction(army, "strength +1");

		assertEquals(1, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(army2));
		// The Army in this skirmish, its other copy, Khamul (a Nazgul) waiting for his skirmish with Boromir, and the
		// unassigned Attacker are Easterlings.
		assertTrue(scn.ShadowHasCardChoiceAvailable(army, army2, khamul, attacker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, scout, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(13, scn.GetStrength(khamul));
		assertEquals(13, scn.GetStrength(army));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The bonus lasts the skirmish it was given in, and is gone in the next.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtOneBurdenNoAbilityIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var army = scn.GetShadowCard("army");

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(army);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(army));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// One burden: one short of the first ability's 2.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(army, "strength +1"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +3"));
		assertEquals(0, scn.GetWoundsOn(army));
	}

	@Test
	public void AtThreeBurdensTheStrengthPlusTwoAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var army = scn.GetShadowCard("army");

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(army);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);

		assertEquals(3, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(army));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Three burdens: one short of the second ability's 4.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(army, "strength +1"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +3"));
		assertEquals(0, scn.GetWoundsOn(army));
	}

	@Test
	public void AtFourBurdensItExertsToMakeAnEasterlingStrengthPlusTwoForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var army = scn.GetShadowCard("army");
		var army2 = scn.GetShadowCard("army2");
		var khamul = scn.GetShadowCard("khamul");
		var attacker = scn.GetShadowCard("attacker");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		scn.MoveMinionsToTable(army, army2, khamul, attacker, sentry, scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, army}, new PhysicalCardImpl[]{boromir, khamul});
		scn.ShadowDeclineAssignments();

		assertEquals(4, scn.GetBurdens());
		// Khamul makes every Easterling strength +3 at 3 burdens or more.
		assertEquals(16, scn.GetStrength(army));    // 13 + 3
		assertEquals(15, scn.GetStrength(khamul));  // 12 + 3
		assertEquals(9, scn.GetStrength(sentry));
		assertEquals(0, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(army2));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(army, "strength +1"));
		assertTrue(scn.ShadowActionAvailable(army, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +3"));
		scn.ShadowUseCardAction(army, "strength +2");

		assertEquals(1, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(army2));
		// The Army in this skirmish, its other copy, Khamul (a Nazgul) waiting for his skirmish with Boromir, and the
		// unassigned Attacker are Easterlings.
		assertTrue(scn.ShadowHasCardChoiceAvailable(army, army2, khamul, attacker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, scout, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(17, scn.GetStrength(khamul));
		assertEquals(16, scn.GetStrength(army));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The bonus lasts the skirmish it was given in, and is gone in the next.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(15, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtFiveBurdensTheStrengthPlusThreeAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var army = scn.GetShadowCard("army");

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.MoveMinionsToTable(army);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);

		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(army));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Five burdens: one short of the third ability's 6.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(army, "strength +1"));
		assertTrue(scn.ShadowActionAvailable(army, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +3"));
		assertEquals(0, scn.GetWoundsOn(army));
	}

	@Test
	public void AtSixBurdensItExertsToMakeAnEasterlingStrengthPlusThreeForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var army = scn.GetShadowCard("army");
		var army2 = scn.GetShadowCard("army2");
		var khamul = scn.GetShadowCard("khamul");
		var attacker = scn.GetShadowCard("attacker");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.AddBurdens(6);
		assertEquals(6, scn.GetBurdens());
		scn.MoveMinionsToTable(army, army2, khamul, attacker, sentry, scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, army}, new PhysicalCardImpl[]{boromir, attacker});
		scn.ShadowDeclineAssignments();

		assertEquals(6, scn.GetBurdens());
		// Khamul makes every Easterling strength +3 at 3 burdens or more.
		assertEquals(16, scn.GetStrength(army));      // 13 + 3
		assertEquals(9, scn.GetStrength(attacker));   // 6 + 3
		assertEquals(0, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(army2));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(army, "strength +1"));
		assertTrue(scn.ShadowActionAvailable(army, "strength +2"));
		assertTrue(scn.ShadowActionAvailable(army, "strength +3"));
		scn.ShadowUseCardAction(army, "strength +3");

		assertEquals(1, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(army2));
		// The Army in this skirmish, its other copy, Khamul (a Nazgul) waiting for his skirmish with Boromir, and the
		// unassigned Attacker are Easterlings.
		assertTrue(scn.ShadowHasCardChoiceAvailable(army, army2, khamul, attacker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, scout, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(attacker);

		assertEquals(12, scn.GetStrength(attacker));
		assertEquals(16, scn.GetStrength(army));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The bonus lasts the skirmish it was given in, and is gone in the next.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(9, scn.GetStrength(attacker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ExhaustedAtSixBurdensItIsOfferedNoAbilityThoughItsOtherCopyIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var army = scn.GetShadowCard("army");
		var army2 = scn.GetShadowCard("army2");

		scn.StartGame();
		scn.AddBurdens(6);
		assertEquals(6, scn.GetBurdens());
		scn.MoveMinionsToTable(army, army2);
		scn.AddWoundsToChar(army, 2);
		assertEquals(2, scn.GetWoundsOn(army));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, army);
		scn.ShadowDeclineAssignments();

		assertEquals(6, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(army));
		assertEquals(0, scn.GetWoundsOn(army2));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: exhausted, so it cannot exert; the other copy, unwounded, can exert itself.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsExhausted(army));
		assertFalse(scn.ShadowActionAvailable(army, "strength +1"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(army, "strength +3"));
		assertTrue(scn.ShadowActionAvailable(army2, "strength +3"));
		assertEquals(2, scn.GetWoundsOn(army));
	}
}
