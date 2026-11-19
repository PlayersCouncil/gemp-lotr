package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_219_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "11_219");
					// Aragorn, Ranger of the North: a [gondor] Man companion, resistance 6, vitality 4; a maneuver ability.
					put("aragorn", "1_89");
					// Sam, Son of Hamfast: a [shire] Hobbit companion, resistance 5; a fellowship ability used only on
					// purpose, and a response to Frodo's death.
					put("sam", "1_311");
					// Elrond, Lord of Rivendell: an [elven] ally whose home is site 3 (resistance 0); heals allies whose
					// home is site 3 at the start of his owner's turns, and a fellowship ability.
					put("elrond", "1_40");
					// Uruk Pursuer: an [isengard] Uruk-hai, fierce and a tracker; its texts are about unbound Hobbits
					// discarded or killed.
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Úlairë Attëa, Second of the Nine Riders
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.
		 * Each time Úlairë Attëa is assigned to a fierce skirmish involving a character who has resistance 4 or less, you may exert Úlairë Attëa to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Second of the Nine Riders", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void NotOfferedInTheFirstSkirmishAndOfferedAtEachFierceAssignmentToACompanionOfResistance4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(attea);
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, attea);

		scn.SkipToAssignments();
		// 6 - 2 burdens
		assertEquals(4, scn.GetResistance(aragorn));
		assertFalse(scn.InFierceRound());

		// The first assignment is no fierce skirmish.
		scn.FreepsAssignToMinions(aragorn, attea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attea));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 12 against 8: Aragorn loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(attea));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(attea));
		assertEquals(3, scn.GetBurdens());

		// The fierce skirmish: Aragorn loses again.  Then the fellowship moves on and a second fierce round comes.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		scn.SkipToAssignments();
		// 6 - 3 burdens
		assertEquals(3, scn.GetResistance(aragorn));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(attea));
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NotOfferedAssignedToACompanionOfResistance5WhileAnotherMinionFacesOneOfResistance4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(attea, pursuer);
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.SHADOW_CHARACTERS, attea, pursuer);

		scn.SkipToAssignments();
		// 6 - 1 and 5 - 1
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(sam));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, attea}, new PhysicalCardImpl[]{sam, pursuer});
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attea));
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void OfferedAssignedToAnAllyAtHisHomeAndDecliningExertsNobodyAndAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(attea);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(attea));

		// On to Rivendell Valley, site 3, Elrond's home.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		// An ally's resistance is 0.
		assertEquals(0, scn.GetResistance(elrond));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(elrond, attea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void OfferedWhenTheShadowPlayerAssignsHimToACompanionOfResistance4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddBurdens(1);
		scn.MoveMinionsToTable(attea);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(attea));

		scn.SkipToAssignments();
		// 5 - 1
		assertEquals(4, scn.GetResistance(sam));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(sam, attea);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(attea));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(attea));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.MoveMinionsToTable(attea);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(attea, 2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(attea));

		scn.SkipToAssignments();
		assertEquals(4, scn.GetResistance(aragorn));
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(attea));
		assertEquals(2, scn.GetWoundsOn(attea));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
