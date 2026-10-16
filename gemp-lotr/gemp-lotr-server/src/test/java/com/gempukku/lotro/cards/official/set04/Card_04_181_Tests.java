package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_181_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chaser", "4_181");
					put("chaser2", "4_181");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
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
	public void UrukChaserStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Uruk Chaser
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Tracker</b>. <b>Fierce</b>.<br>During a fierce skirmish involving this minion, it is strength +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("chaser");

		assertEquals("Uruk Chaser", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsStrengthPlus2InHisFierceSkirmishAndNotInHisRegularSkirmishOrAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var chaser = scn.GetShadowCard("chaser");

		scn.StartGame();
		scn.MoveMinionsToTable(chaser);
		assertEquals(9, scn.GetStrength(chaser));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, chaser);
		scn.FreepsResolveSkirmish(aragorn);

		// His regular skirmish: no bonus.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chaser));
		assertEquals(9, scn.GetStrength(chaser));
		scn.PassSkirmishActions();
		// 8 against 9: Aragorn loses and takes a wound.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, chaser);
		scn.FreepsResolveSkirmish(aragorn);

		// His fierce skirmish: 9 + 2.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chaser));
		assertEquals(11, scn.GetStrength(chaser));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The fierce skirmish over, he is back to 9.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(9, scn.GetStrength(chaser));
	}

	@Test
	public void OnlyTheMinionsOwnFierceSkirmishCountsAndTheBonusIsHisAloneAndDoesNotStackWithASecondCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var chaser = scn.GetShadowCard("chaser");
		var chaser2 = scn.GetShadowCard("chaser2");
		var pursuer = scn.GetShadowCard("pursuer");
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(chaser, chaser2, pursuer, seeker);

		// No regular skirmishes.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chaser},
				new PhysicalCardImpl[]{gimli, seeker});
		scn.ShadowAssignToMinions(aragorn, chaser2, pursuer);

		// Another minion's fierce skirmish: neither copy is in it, and neither has the bonus.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, seeker));
		assertEquals(9, scn.GetStrength(chaser));
		assertEquals(9, scn.GetStrength(chaser2));
		assertEquals(6, scn.GetStrength(seeker));
		scn.PassSkirmishActions();

		// Their own fierce skirmish: each copy is 9 + 2 (not + 4), and the other fierce minion in it keeps its 5.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.InFierceRound());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chaser));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chaser2));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, pursuer));
		assertEquals(11, scn.GetStrength(chaser));
		assertEquals(11, scn.GetStrength(chaser2));
		assertEquals(5, scn.GetStrength(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
