package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_156_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charging", "15_156");
					put("charging2", "15_156");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4, no hunter; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("pursuer", "4_190");    // Uruk Pursuer: printed fierce, strength 5; its texts read only an unbound Hobbit discarded or killed
					put("seeker", "4_195");     // Uruk Seeker: printed fierce, strength 6; +2 only against a bearer of a search card
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ChargingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Charging Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>During a fierce skirmish involving this minion, it is strength +3 and gains <b>hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper>
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("charging");

		assertEquals("Charging Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus3AndHunter1InHisFierceSkirmishAndNotInHisRegularSkirmishOrAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var charging = scn.GetShadowCard("charging");

		scn.StartGame();
		scn.MoveMinionsToTable(charging);
		assertEquals(7, scn.GetStrength(charging));
		assertFalse(scn.HasKeyword(charging, Keyword.HUNTER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, charging);
		scn.FreepsResolveSkirmish(aragorn);

		// His regular skirmish: no bonus, no hunter.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, charging));
		assertEquals(7, scn.GetStrength(charging));
		assertFalse(scn.HasKeyword(charging, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(charging));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// 8 against 7: the Uruk loses, takes a wound and lives (vitality 3).
		assertEquals(1, scn.GetWoundsOn(charging));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, charging);
		scn.FreepsResolveSkirmish(aragorn);

		// His fierce skirmish: hunter 1, and 7 + 3 + 1 for hunter 1 against Aragorn, who is no hunter.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, charging));
		assertEquals(1, scn.GetKeywordCount(charging, Keyword.HUNTER));
		assertEquals(11, scn.GetStrength(charging));
		scn.PassSkirmishActions();
		// 11 against 8: Aragorn loses.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce skirmish over, he is back to 7 and no hunter.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(7, scn.GetStrength(charging));
		assertFalse(scn.HasKeyword(charging, Keyword.HUNTER));
	}

	@Test
	public void OnlyTheMinionsOwnFierceSkirmishCountsAndTheBonusIsHisAloneAndDoesNotStackWithASecondCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var charging = scn.GetShadowCard("charging");
		var charging2 = scn.GetShadowCard("charging2");
		var pursuer = scn.GetShadowCard("pursuer");
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(charging, charging2, pursuer, seeker);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, charging},
				new PhysicalCardImpl[]{gimli, seeker});
		scn.ShadowAssignToMinions(aragorn, charging2, pursuer);

		// Another minion's fierce skirmish: neither copy is in it, and neither has the bonus nor hunter.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, seeker));
		assertEquals(7, scn.GetStrength(charging));
		assertEquals(7, scn.GetStrength(charging2));
		assertFalse(scn.HasKeyword(charging, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(charging2, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(seeker));
		assertFalse(scn.HasKeyword(seeker, Keyword.HUNTER));
		scn.PassSkirmishActions();

		// Their own fierce skirmish: each copy is 7 + 3 + 1 for hunter 1 (not + 6, nor hunter 2), and the other
		// fierce minion in it keeps its 5 and gains no hunter.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, charging));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, charging2));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuer));
		assertEquals(1, scn.GetKeywordCount(charging, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(charging2, Keyword.HUNTER));
		assertEquals(11, scn.GetStrength(charging));
		assertEquals(11, scn.GetStrength(charging2));
		assertFalse(scn.HasKeyword(pursuer, Keyword.HUNTER));
		assertEquals(5, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
