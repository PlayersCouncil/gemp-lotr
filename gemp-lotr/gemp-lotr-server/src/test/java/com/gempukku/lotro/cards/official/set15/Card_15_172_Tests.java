package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_172_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ugluk", "15_172");

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4, no hunter; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3, no hunter
					put("faramir", "18_48");      // Faramir, Captain of Ithilien: a companion with hunter 2
					put("bounder", "1_286");      // Bounder: a Hobbit ally, home site 2 of the Fellowship path, vitality 2

					put("raiding", "1_158");      // Uruk-hai Raiding Party: a minion with no hunter, strength 9, vitality 3
					put("brigand", "4_10");       // Dunlending Brigand: strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UglukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uglúk, Ugly Fellow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Fierce. Hunter 3. (While skirmishing a non-hunter character, this character is strength +3.)
		 * 	At the start of each skirmish involving Uglúk, each non-hunter skirmishing Uglúk must exert.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ugluk");

		assertEquals("Uglúk", card.getBlueprint().getTitle());
		assertEquals("Ugly Fellow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(3, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfHisSkirmishOnlyTheNonHunterSkirmishingHimExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var ugluk = scn.GetShadowCard("ugluk");
		var raiding = scn.GetShadowCard("raiding");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk, raiding, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, ugluk}, new PhysicalCardImpl[]{gimli, brigand});
		// The Raiding Party is left over: the Shadow player puts it in Ugluk's skirmish too.
		scn.ShadowAssignToMinions(aragorn, raiding);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(raiding));
		assertEquals(0, scn.GetWoundsOn(ugluk));

		// A skirmish Ugluk is not in: nobody exerts.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(raiding));

		scn.PassSkirmishActions();
		// 6 against 8: Gimli loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(aragorn);
		// Aragorn exerts; Gimli, in a skirmish already over, and the Raiding Party, on Ugluk's own side of his
		// skirmish, do not.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(raiding));
		assertEquals(0, scn.GetWoundsOn(ugluk));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AHunterSkirmishingHimDoesNotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var ugluk = scn.GetShadowCard("ugluk");
		scn.MoveCompanionsToTable(faramir);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, ugluk);

		assertEquals(2, scn.GetKeywordCount(faramir, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(faramir));

		scn.FreepsResolveSkirmish(faramir);
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingHimExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var ugluk = scn.GetShadowCard("ugluk");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// The fellowship moves to site 2, Bounder's home, where he may be assigned.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(ugluk);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, ugluk);

		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsResolveSkirmish(bounder);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void HisFierceSkirmishMakesTheCompanionExertAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var ugluk = scn.GetShadowCard("ugluk");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(ugluk);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, ugluk);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.PassSkirmishActions();
		// 8 against 9 + 3 for hunter: Aragorn loses, one more wound.
		assertEquals(2, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, ugluk);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.InFierceRound());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
