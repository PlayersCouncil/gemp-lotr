package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_167_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("signs", "13_167");
					put("entranced", "13_163");  // Entranced Uruk: [uruk-hai] Uruk-hai lurker, strength 9; a bar on discarding its possessions only
					put("entranced2", "13_163");
					put("saruman", "12_144");    // Saruman, Agent of the Dark Lord: [uruk-hai] Wizard lurker, strength 8; a skirmish ability used
					                             // only on purpose, and a heal of himself when a companion is killed
					put("tactician", "13_175");  // Uruk Tactician: [uruk-hai] Uruk-hai who is no lurker, strength 7; a skirmish ability used only on purpose
					put("prowling", "11_136");   // Prowling Orc: [orc] Orc lurker, strength 6; a skirmish ability used only on purpose

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("sam", "1_311");         // Sam, Son of Hamfast: strength 3, vitality 4; a fellowship ability and a response when Frodo dies
					put("gimli", "11_8");        // Gimli, Lively Combatant: strength 6, vitality 3; may draw when he wins a skirmish
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: ally, home site 3, strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SignsofWarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Signs of War
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot 3 [uruk-hai] lurker minions, at the start of each skirmish involving an [uruk-hai] lurker minion, each companion skirmishing that minion must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("signs");

		assertEquals("Signs of War", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithThreeUrukHaiLurkersEachSkirmishInvolvingOneExertsItsCompanionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);

		var signs = scn.GetShadowCard("signs");
		var entranced = scn.GetShadowCard("entranced");
		var entranced2 = scn.GetShadowCard("entranced2");
		var saruman = scn.GetShadowCard("saruman");
		var tactician = scn.GetShadowCard("tactician");
		var prowling = scn.GetShadowCard("prowling");
		scn.MoveCardsToSupportArea(signs);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, signs);
		// Three [uruk-hai] lurkers: two Entranced Uruks and Saruman.
		scn.MoveMinionsToTable(entranced, entranced2, saruman, tactician, prowling);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, tactician}, new PhysicalCardImpl[]{sam, saruman},
				new PhysicalCardImpl[]{gimli, entranced});
		// The minions left over: the Prowling Orc to Aragorn, and a second [uruk-hai] lurker to Gimli.
		scn.ShadowAssignToMinions(new PhysicalCardImpl[]{aragorn, prowling}, new PhysicalCardImpl[]{gimli, entranced2});
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// An [uruk-hai] minion who is no lurker and a lurker of another culture: nobody exerts.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassCurrentPhaseActions();

		// Sam against Saruman, an [uruk-hai] lurker who is no Uruk-hai: Sam exerts as it starts.
		int aragornWounds = scn.GetWoundsOn(aragorn);
		scn.FreepsResolveSkirmish(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(aragornWounds, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassCurrentPhaseActions();

		// Each skirmish: Gimli exerts at the start of his.
		int samWounds = scn.GetWoundsOn(sam);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(samWounds, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoUrukHaiLurkersNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var signs = scn.GetShadowCard("signs");
		var entranced = scn.GetShadowCard("entranced");
		var entranced2 = scn.GetShadowCard("entranced2");
		var tactician = scn.GetShadowCard("tactician");
		var prowling = scn.GetShadowCard("prowling");
		scn.MoveCardsToSupportArea(signs);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, signs);
		// Two [uruk-hai] lurkers, beside an [uruk-hai] minion who is no lurker and a lurker of another culture.
		scn.MoveMinionsToTable(entranced, entranced2, tactician, prowling);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, entranced);
		scn.ShadowDeclineAssignments();
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnExhaustedCompanionIsNotWoundedAndAnAllySkirmishingALurkerDoesNotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);

		var signs = scn.GetShadowCard("signs");
		var entranced = scn.GetShadowCard("entranced");
		var entranced2 = scn.GetShadowCard("entranced2");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(signs);

		scn.StartGame();
		scn.SkipToSite(2);
		assertInZone(Zone.SUPPORT, signs);
		// Gimli, of vitality 3, exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.MoveMinionsToTable(entranced, entranced2, saruman);
		// At site 3, Elrond's home, the ally may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elrond, entranced}, new PhysicalCardImpl[]{gimli, entranced2});
		scn.ShadowDeclineAssignments();

		// An ally is no companion.
		scn.FreepsResolveSkirmish(elrond);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.PassCurrentPhaseActions();

		// Exhausted, Gimli cannot exert: he is neither wounded nor killed.
		scn.FreepsResolveSkirmish(gimli);
		assertInPlay(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
