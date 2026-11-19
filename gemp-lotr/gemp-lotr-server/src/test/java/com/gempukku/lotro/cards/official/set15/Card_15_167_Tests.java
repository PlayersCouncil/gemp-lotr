package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_15_167_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pursuing", "15_167");
					put("pursuing2", "15_167");
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
	public void PursuingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Pursuing Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Fierce</b>.<br>During a fierce skirmish involving this minion, it gains <b>hunter 4</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pursuing");

		assertEquals("Pursuing Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void GainsHunter4InHisFierceSkirmishAndNotInHisRegularSkirmishOrAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var pursuing = scn.GetShadowCard("pursuing");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuing);
		assertEquals(6, scn.GetStrength(pursuing));
		assertFalse(scn.HasKeyword(pursuing, Keyword.HUNTER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pursuing);
		scn.FreepsResolveSkirmish(aragorn);

		// His regular skirmish: no hunter.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuing));
		assertEquals(6, scn.GetStrength(pursuing));
		assertFalse(scn.HasKeyword(pursuing, Keyword.HUNTER));
		assertEquals(0, scn.GetWoundsOn(pursuing));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// 8 against 6: the Uruk loses, takes a wound and lives (vitality 2).
		assertEquals(1, scn.GetWoundsOn(pursuing));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, pursuing);
		scn.FreepsResolveSkirmish(aragorn);

		// His fierce skirmish: hunter 4, so 6 + 4 against Aragorn, who is no hunter.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuing));
		assertEquals(4, scn.GetKeywordCount(pursuing, Keyword.HUNTER));
		assertEquals(10, scn.GetStrength(pursuing));
		scn.PassSkirmishActions();
		// 10 against 8: Aragorn loses.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce skirmish over, he is back to 6 and no hunter.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(pursuing));
		assertFalse(scn.HasKeyword(pursuing, Keyword.HUNTER));
	}

	@Test
	public void OnlyTheMinionsOwnFierceSkirmishCountsAndTheHunterIsHisAloneAndDoesNotStackWithASecondCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var pursuing = scn.GetShadowCard("pursuing");
		var pursuing2 = scn.GetShadowCard("pursuing2");
		var pursuer = scn.GetShadowCard("pursuer");
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuing, pursuing2, pursuer, seeker);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, pursuing},
				new PhysicalCardImpl[]{gimli, seeker});
		scn.ShadowAssignToMinions(aragorn, pursuing2, pursuer);

		// Another minion's fierce skirmish: neither copy is in it, and neither has hunter.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, seeker));
		assertEquals(6, scn.GetStrength(pursuing));
		assertEquals(6, scn.GetStrength(pursuing2));
		assertFalse(scn.HasKeyword(pursuing, Keyword.HUNTER));
		assertFalse(scn.HasKeyword(pursuing2, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(seeker));
		assertFalse(scn.HasKeyword(seeker, Keyword.HUNTER));
		scn.PassSkirmishActions();

		// Their own fierce skirmish: each copy is hunter 4 (not hunter 8), 6 + 4 against Aragorn, and the
		// other fierce minion in it keeps its 5 and gains no hunter.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuing));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuing2));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuer));
		assertEquals(4, scn.GetKeywordCount(pursuing, Keyword.HUNTER));
		assertEquals(4, scn.GetKeywordCount(pursuing2, Keyword.HUNTER));
		assertEquals(10, scn.GetStrength(pursuing));
		assertEquals(10, scn.GetStrength(pursuing2));
		assertFalse(scn.HasKeyword(pursuer, Keyword.HUNTER));
		assertEquals(5, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
