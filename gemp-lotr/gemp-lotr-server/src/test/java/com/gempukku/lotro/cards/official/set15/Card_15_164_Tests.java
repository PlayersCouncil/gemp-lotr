package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_164_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mauhur", "15_164");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; Damage +1 is its whole text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, no hunter; a maneuver ability used only on purpose
					// Sam, Son of Hamfast: Ring-bound, no hunter, and not the Ring-bearer; a fellowship ability and a
					// response to Frodo's death
					put("sam", "1_311");
					// Faramir, Captain of Ithilien: [gondor] Man companion, ranger and hunter 2; a maneuver ability
					// used only on purpose
					put("faramir", "18_48");
					// Bounder: [shire] Hobbit ally whose home is site 2 of this path, where he may be assigned to a
					// skirmish; a skirmish ability used only on purpose
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MauhurStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mauhúr, Relentless Hunter
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>. <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br><b>Assignment:</b> Exert Mauhúr to assign him to a non-hunter character (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mauhur");

		assertEquals("Mauhúr", card.getBlueprint().getTitle());
		assertEquals("Relentless Hunter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AssignmentAbilityExertsMauhurToAssignHimToANonHunterCharacterWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var faramir = scn.GetFreepsCard("faramir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, sam, faramir);
		scn.MoveAlliesToTable(bounder);

		var mauhur = scn.GetShadowCard("mauhur");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(mauhur, savage);
		// The assignment phase of turn 1, at site 2: Bounder's home.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(mauhur));
		assertFalse(scn.IsCharAssigned(bounder));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(savage));
		assertTrue(scn.ShadowActionAvailable(mauhur));

		scn.ShadowUseCardAction(mauhur);
		assertEquals(1, scn.GetWoundsOn(mauhur));
		// A companion, a Ring-bound companion and an ally, none a hunter. Not the Ring-bearer, and not Faramir,
		// who is a hunter. The ally may be assigned to a skirmish at this site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam, bounder));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, faramir));
		assertTrue(scn.FreepsCanAssign(bounder));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(bounder);

		assertTrue(scn.IsCharAssignedAgainst(bounder, mauhur));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertFalse(scn.IsCharAssigned(sam));
		assertFalse(scn.IsCharAssigned(savage));
		assertEquals(1, scn.GetWoundsOn(mauhur));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AssignmentAbilityIsNotOfferedOnceMauhurCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var mauhur = scn.GetShadowCard("mauhur");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(mauhur, savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Vitality 2: with one wound he is exhausted.
		scn.AddWoundsToChar(mauhur, 1);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mauhur));
		assertEquals(1, scn.GetWoundsOn(mauhur));
		assertFalse(scn.IsCharAssigned(aragorn));
	}
}
