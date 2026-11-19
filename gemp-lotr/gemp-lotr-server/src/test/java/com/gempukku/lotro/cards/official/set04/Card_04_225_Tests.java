package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_225_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "4_225");
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: an Easterling who is a Nazgul, strength 12;
					                            // while 3 burdens can be spotted each Easterling is strength +3 (damage +1 at 5)
					put("infantry", "4_227");   // Easterling Infantry: an Easterling Man, strength 8; fierce and damage +1 at 3 burdens
					put("sentry", "4_253");     // Southron Sentry: a [raider] Man who is no Easterling, strength 9; a maneuver ability only
					put("scout", "15_77");      // Easterling Scout: a [men] minion, strength 9, with "Easterling" in his title only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7; his text is about being overwhelmed
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Easterling Captain
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>. <b>Fierce</b>.<br><b>Skirmish:</b> Spot 2 burdens and remove (2) to make an Easterling strength +2.<br><b>Skirmish:</b> Spot 4 burdens and remove (2) to make an Easterling strength +3.<br><b>Skirmish:</b> Spot 6 burdens and remove (2) to make an Easterling strength +4.
		*/

		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");

		assertEquals("Easterling Captain", captain.getBlueprint().getTitle());
		assertNull(captain.getBlueprint().getSubtitle());
		assertTrue(captain.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, captain.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, captain.getBlueprint().getCulture());
		assertEquals(CardType.MINION, captain.getBlueprint().getCardType());
		assertEquals(Race.MAN, captain.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(captain, Keyword.EASTERLING));
		assertTrue(scn.HasKeyword(captain, Keyword.FIERCE));
		assertEquals(5, captain.getBlueprint().getTwilightCost());
		assertEquals(11, captain.getBlueprint().getStrength());
		assertEquals(3, captain.getBlueprint().getVitality());
		assertEquals(4, captain.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTwoBurdensItRemovesTwoToMakeAnEasterlingStrengthPlusTwoForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var captain = scn.GetShadowCard("captain");
		var khamul = scn.GetShadowCard("khamul");
		var infantry = scn.GetShadowCard("infantry");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(captain, khamul, infantry, sentry, scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, captain}, new PhysicalCardImpl[]{boromir, infantry});
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(2);

		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(captain));
		assertEquals(12, scn.GetStrength(khamul));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Exactly 2 burdens: only the first ability is open.
		assertTrue(scn.ShadowActionAvailable(captain, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(captain, "strength +3"));
		assertFalse(scn.ShadowActionAvailable(captain, "strength +4"));
		scn.ShadowUseCardAction(captain, "strength +2");

		assertEquals(0, scn.GetTwilight());
		// The Captain in this skirmish, and Khamul (a Nazgul) and the Infantry (a Man) outside it, are Easterlings.
		assertTrue(scn.ShadowHasCardChoiceAvailable(captain, khamul, infantry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, scout, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertEquals(14, scn.GetStrength(khamul));
		assertEquals(11, scn.GetStrength(captain));
		assertEquals(8, scn.GetStrength(infantry));
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

		var captain = scn.GetShadowCard("captain");

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(captain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.SetTwilight(2);

		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(captain));
		assertEquals(2, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(captain));
	}

	@Test
	public void AtSixBurdensWithOneTwilightNoAbilityIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var captain = scn.GetShadowCard("captain");

		scn.StartGame();
		scn.AddBurdens(6);
		assertEquals(6, scn.GetBurdens());
		scn.MoveMinionsToTable(captain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.SetTwilight(1);

		assertEquals(6, scn.GetBurdens());
		assertEquals(1, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(captain));
		assertEquals(1, scn.GetTwilight());
		assertEquals(11, scn.GetStrength(captain));
	}

	@Test
	public void AtThreeBurdensTheStrengthPlusThreeAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var captain = scn.GetShadowCard("captain");

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(captain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.SetTwilight(2);

		assertEquals(3, scn.GetBurdens());
		assertEquals(2, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(captain, "strength +2"));
		assertFalse(scn.ShadowActionAvailable(captain, "strength +3"));
		assertFalse(scn.ShadowActionAvailable(captain, "strength +4"));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtFourBurdensItRemovesTwoToMakeAnEasterlingStrengthPlusThreeForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var captain = scn.GetShadowCard("captain");
		var khamul = scn.GetShadowCard("khamul");
		var infantry = scn.GetShadowCard("infantry");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.AddBurdens(4);
		assertEquals(4, scn.GetBurdens());
		scn.MoveMinionsToTable(captain, khamul, infantry, sentry, scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, captain}, new PhysicalCardImpl[]{boromir, infantry});
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(2);

		// Khamul makes every Easterling strength +3 at 3 burdens or more.
		assertEquals(4, scn.GetBurdens());
		assertEquals(2, scn.GetTwilight());
		assertEquals(14, scn.GetStrength(captain));    // 11 + 3
		assertEquals(15, scn.GetStrength(khamul));     // 12 + 3
		assertEquals(9, scn.GetStrength(sentry));

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Exactly 4 burdens: the first two abilities are open, the third is not.
		assertTrue(scn.ShadowActionAvailable(captain, "strength +2"));
		assertTrue(scn.ShadowActionAvailable(captain, "strength +3"));
		assertFalse(scn.ShadowActionAvailable(captain, "strength +4"));
		scn.ShadowUseCardAction(captain, "strength +3");

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(captain, khamul, infantry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, scout, aragorn));
		scn.ShadowChooseCard(khamul);

		assertEquals(18, scn.GetStrength(khamul));     // 12 + 3 + 3
		assertEquals(14, scn.GetStrength(captain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(15, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtFiveBurdensTheStrengthPlusFourAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var captain = scn.GetShadowCard("captain");

		scn.StartGame();
		scn.AddBurdens(5);
		assertEquals(5, scn.GetBurdens());
		scn.MoveMinionsToTable(captain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.SetTwilight(2);

		assertEquals(5, scn.GetBurdens());
		assertEquals(2, scn.GetTwilight());

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(captain, "strength +2"));
		assertTrue(scn.ShadowActionAvailable(captain, "strength +3"));
		assertFalse(scn.ShadowActionAvailable(captain, "strength +4"));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtSixBurdensItRemovesTwoToMakeAnEasterlingStrengthPlusFourForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var captain = scn.GetShadowCard("captain");
		var khamul = scn.GetShadowCard("khamul");
		var infantry = scn.GetShadowCard("infantry");
		var sentry = scn.GetShadowCard("sentry");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.AddBurdens(6);
		assertEquals(6, scn.GetBurdens());
		scn.MoveMinionsToTable(captain, khamul, infantry, sentry, scout);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, captain}, new PhysicalCardImpl[]{boromir, infantry});
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(2);

		// Khamul makes every Easterling strength +3 at 3 burdens or more.
		assertEquals(6, scn.GetBurdens());
		assertEquals(2, scn.GetTwilight());
		assertEquals(14, scn.GetStrength(captain));    // 11 + 3
		assertEquals(11, scn.GetStrength(infantry));   // 8 + 3

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(captain, "strength +2"));
		assertTrue(scn.ShadowActionAvailable(captain, "strength +3"));
		assertTrue(scn.ShadowActionAvailable(captain, "strength +4"));
		scn.ShadowUseCardAction(captain, "strength +4");

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(captain, khamul, infantry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, scout, aragorn));
		// The Infantry, waiting for his own skirmish with Boromir.
		scn.ShadowChooseCard(infantry);

		assertEquals(15, scn.GetStrength(infantry));   // 8 + 3 + 4
		assertEquals(14, scn.GetStrength(captain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Not carried into his own skirmish, which comes after.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(11, scn.GetStrength(infantry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
