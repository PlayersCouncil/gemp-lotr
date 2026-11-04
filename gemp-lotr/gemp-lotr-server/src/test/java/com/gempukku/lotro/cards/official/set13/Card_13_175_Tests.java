package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_13_175_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tactician", "13_175");
					put("tactician2", "13_175");
					put("entranced", "13_163"); // Entranced Uruk: [uruk-hai] Uruk-hai lurker, strength 9; a bar on discarding its possessions
					put("saruman", "12_144");   // Saruman, Agent of the Dark Lord: [uruk-hai] Wizard lurker, strength 8; a skirmish ability used only on purpose, a heal on a kill
					put("commander", "13_158"); // Assault Commander: [uruk-hai] Uruk-hai, no lurker, strength 9; a Shadow ability used only on purpose
					put("prowler", "11_136");   // Prowling Orc: [orc] Orc lurker, strength 6; a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, strength 6; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: ally, strength 4; his maneuver ability lets him skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukTacticianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Uruk Tactician
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Skirmish:</b> Exert this minion to make a companion skirmishing an [uruk-hai] lurker minion strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tactician");

		assertEquals("Uruk Tactician", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void MakesTheCompanionSkirmishingAnUrukHaiLurkerStrengthMinus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var tactician = scn.GetShadowCard("tactician");
		var tactician2 = scn.GetShadowCard("tactician2");
		var entranced = scn.GetShadowCard("entranced");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(tactician, tactician2, entranced, saruman);
		scn.SkipToAssignments();
		// Two lurker skirmishes; both Tacticians are assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, entranced}, new PhysicalCardImpl[]{gimli, saruman});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(tactician));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowActionAvailable(tactician));
		scn.ShadowUseCardAction(tactician);

		// Only the companion in this skirmish: Gimli, assigned to a lurker whose skirmish has not begun, is left alone.
		assertEquals(1, scn.GetWoundsOn(tactician));
		assertEquals(0, scn.GetWoundsOn(tactician2));
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// In the next skirmish, a phase of its own, Aragorn's penalty is gone; the [uruk-hai] Wizard lurker's companion is reached.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));
		scn.ShadowUseCardAction(tactician);

		assertEquals(2, scn.GetWoundsOn(tactician));
		assertEquals(4, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPaidForNothingWhereNoCompanionSkirmishesAnUrukHaiLurker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var tactician = scn.GetShadowCard("tactician");
		var tactician2 = scn.GetShadowCard("tactician2");
		var entranced = scn.GetShadowCard("entranced");
		var commander = scn.GetShadowCard("commander");
		var prowler = scn.GetShadowCard("prowler");

		scn.StartGame();
		scn.MoveMinionsToTable(tactician, tactician2, entranced, commander, prowler);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		// An [uruk-hai] minion who is no lurker, a lurker who is no [uruk-hai] minion, and an ally against an [uruk-hai] lurker.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, commander}, new PhysicalCardImpl[]{gimli, prowler},
				new PhysicalCardImpl[]{thrarin, entranced});
		scn.ShadowDeclineAssignments();

		// The Assault Commander's skirmish is not a lurker's, so it comes first.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(tactician));
		assertTrue(scn.ShadowActionAvailable(tactician));
		scn.ShadowUseCardAction(tactician);
		assertEquals(1, scn.GetWoundsOn(tactician));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(tactician));
		scn.ShadowUseCardAction(tactician);
		assertEquals(2, scn.GetWoundsOn(tactician));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(tactician2));
		assertTrue(scn.ShadowActionAvailable(tactician2));
		scn.ShadowUseCardAction(tactician2);
		assertEquals(1, scn.GetWoundsOn(tactician2));
		assertEquals(4, scn.GetStrength(thrarin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var tactician = scn.GetShadowCard("tactician");
		var entranced = scn.GetShadowCard("entranced");

		scn.StartGame();
		scn.MoveMinionsToTable(tactician, entranced);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(tactician, 2);
		assertEquals(2, scn.GetWoundsOn(tactician));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, entranced);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(tactician));
		assertEquals(8, scn.GetStrength(aragorn));
	}
}
