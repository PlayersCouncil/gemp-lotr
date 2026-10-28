package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_215_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "7_215");
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc; its text wants 2 wounds on the Ring-bearer
					put("cantea", "1_230");     // Úlairë Cantëa: a [wraith] Nazgûl; his ability is used only on purpose
					put("twilight", "19_34");   // In Twilight: a [wraith] condition, no minion (borne by Cantëa)
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl; no burdens, so no bonus
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("gimli", "1_13");       // Gimli, Son of Glóin: the companion of the second skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Nelya, Assailing Minion
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: <b>Fierce</b>.<br><b>Skirmish:</b> Discard a [wraith] minion to make Úlairë Nelya strength +2.
		*/

		var scn = GetScenario();

		var nelya = scn.GetFreepsCard("nelya");

		assertEquals("Úlairë Nelya", nelya.getBlueprint().getTitle());
		assertEquals("Assailing Minion", nelya.getBlueprint().getSubtitle());
		assertTrue(nelya.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, nelya.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, nelya.getBlueprint().getCulture());
		assertEquals(CardType.MINION, nelya.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, nelya.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(nelya, Keyword.FIERCE));
		assertEquals(5, nelya.getBlueprint().getTwilightCost());
		assertEquals(10, nelya.getBlueprint().getStrength());
		assertEquals(3, nelya.getBlueprint().getVitality());
		assertEquals(2, nelya.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityDiscardsAWraithMinionToMakeHerStrengthPlusTwoForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var cantea = scn.GetShadowCard("cantea");
		var twilight = scn.GetShadowCard("twilight");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound, cantea, khamul);
		scn.AttachCardsTo(cantea, twilight);
		assertAttachedTo(twilight, cantea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{gimli, cantea});
		// The Hound and Khamûl are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);
		// A [wraith] minion of either race, assigned or not, herself included (the text has no "another");
		// not the [raider] Nazgûl, nor the [wraith] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, cantea, nelya));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, twilight));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);

		assertInDiscard(hound);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya, cantea, khamul);
		assertEquals(12, scn.GetStrength(nelya));
		// The bonus is hers alone.
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Aragorn, 8 against 12, loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);

		// Each skirmish is a phase of its own: in the next one she is back at 10.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(nelya));
	}

	@Test
	public void SkirmishAbilityIsOfferedWhileNelyaIsInNoSkirmishAtAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var cantea = scn.GetShadowCard("cantea");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound, cantea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, cantea);
		// Nelya and the Hound are left unassigned, so Nelya is in no skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(nelya));
		// The text names no skirmish of hers: the strength bonus is hers to take wherever a skirmish is going on.
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(hound);

		assertInDiscard(hound);
		assertEquals(12, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, hound);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nelya));
		assertInZone(Zone.SHADOW_CHARACTERS, nelya, hound);
		assertEquals(10, scn.GetStrength(nelya));
	}
}
