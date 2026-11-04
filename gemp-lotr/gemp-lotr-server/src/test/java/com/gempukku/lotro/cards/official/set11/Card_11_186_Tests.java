package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_186_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("furious", "11_186");
					put("scout", "1_191");      // Moria Scout: a [moria] Orc of vitality 2 to bear the Moria Axe; its "when you play" is not met, it is cheated in
					put("tyrannical", "11_204");    // Tyrannical Uruk: an [uruk-hai] Uruk-hai minion, Damage +1 and Toil 1 only
					put("axe", "1_190");        // Moria Axe: a Shadow possession with a Skirmish ability (exert bearer to make him strength +2)
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; Skirmish: exert Gimli to make him strength +2
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; Skirmish: exert Boromir to make a Hobbit strength +3
					put("pan", "3_108");        // Frying Pan: [shire] possession on a Hobbit; Skirmish: exert bearer to wound an Orc he is skirmishing
					put("coat", "13_153");      // Mithril-coat, Dwarf-mail: [shire] ARTIFACT on a Ring-bound Hobbit; Skirmish: exert bearer to ...
					put("saelbeth", "3_25");    // Saelbeth, Elven Councilor: [elven] Elf ALLY; Skirmish: exert Saelbeth to make an Uruk-hai strength -1
					put("sword", "4_64");       // Elven Sword: [elven] possession on an Elf; Skirmish: exert bearer or discard 2 cards from hand to ...
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FuriousUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Furious Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While this minion is assigned to a skirmish, each companion and Free Peoples possession loses its special abilities.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("furious");

		assertEquals("Furious Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileItIsAssignedNoCompanionOrFreePeoplesPossessionMayUseItsSpecialAbilitiesInAnySkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var furious = scn.GetShadowCard("furious");
		var scout = scn.GetShadowCard("scout");
		var axe = scn.GetShadowCard("axe");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var pan = scn.GetFreepsCard("pan");
		var coat = scn.GetFreepsCard("coat");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli, boromir);
		scn.AttachCardsTo(frodo, pan, coat);
		scn.MoveAlliesToTable(saelbeth);
		scn.AttachCardsTo(saelbeth, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(furious, scout);
		scn.AttachCardsTo(scout, axe);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, furious},
				new PhysicalCardImpl[]{boromir, scout});
		// Boromir's skirmish first: the Furious Uruk is still assigned, its own skirmish waiting.
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.IsCharAssigned(furious));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Each companion, the one in this skirmish and the one in the Uruk's own.
		assertFalse(scn.FreepsActionAvailable(boromir));
		assertFalse(scn.FreepsActionAvailable(gimli));
		// Each Free Peoples possession, a companion's and an ally's.
		assertFalse(scn.FreepsActionAvailable(pan));
		assertFalse(scn.FreepsActionAvailable(sword));
		// Neither an artifact nor an ally loses its abilities.
		assertTrue(scn.FreepsActionAvailable(coat));
		assertTrue(scn.FreepsActionAvailable(saelbeth));

		// Nor does a Shadow possession.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(axe));
	}

	@Test
	public void LeftUnassignedItTakesNoSpecialAbilityAway() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var furious = scn.GetShadowCard("furious");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var pan = scn.GetFreepsCard("pan");
		var saelbeth = scn.GetFreepsCard("saelbeth");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli, boromir);
		scn.AttachCardsTo(frodo, pan);
		scn.MoveAlliesToTable(saelbeth);
		scn.AttachCardsTo(saelbeth, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(furious, tyrannical);
		scn.SkipToAssignments();
		// Another [uruk-hai] Uruk-hai is assigned: the bar goes by this minion alone.
		scn.FreepsAssignToMinions(boromir, tyrannical);
		// The Furious Uruk is left over, and the Shadow player does not assign it either.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(boromir);

		assertFalse(scn.IsCharAssigned(furious));
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, tyrannical));
		assertFalse(scn.IsCharSkirmishing(furious));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(boromir));
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsActionAvailable(pan));
		assertTrue(scn.FreepsActionAvailable(sword));
	}
}
