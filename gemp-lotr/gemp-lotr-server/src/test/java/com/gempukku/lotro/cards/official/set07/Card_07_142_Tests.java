package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_142_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blademaster", "7_142");
					put("blademaster2", "7_142");
					// Goblin Runner: a [moria] Orc, strength 5, vitality 1; its only text is a "when you play" trigger
					put("runner", "1_178");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a Maneuver ability
					// Gimli, Son of Glóin: strength 6, vitality 3, damage +1; "Skirmish: Exert Gimli to make him strength +2"
					put("gimli", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EasterlingBlademasterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Easterling Blademaster
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: <b>Easterling</b>.<br><b>Skirmish:</b> Exert this minion to make him <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blademaster");

		assertEquals("Easterling Blademaster", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.EASTERLING));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsHimToMakeHimDamagePlusOneForThatSkirmishAndNotTheOtherCopyBesideHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blademaster = scn.GetShadowCard("blademaster");
		var blademaster2 = scn.GetShadowCard("blademaster2");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(blademaster, blademaster2, runner);

		// "Skirmish:" is not the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blademaster));
		assertEquals(0, scn.GetWoundsOn(blademaster));
		scn.ShadowPass();

		// Both copies on Aragorn, the second by the Shadow player's assignment of the minion left over.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, blademaster}, new PhysicalCardImpl[]{gimli, runner});
		scn.ShadowAssignToMinions(aragorn, blademaster2);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(blademaster));
		assertEquals(0, scn.GetWoundsOn(blademaster));
		assertEquals(0, scn.GetWoundsOn(blademaster2));
		assertEquals(0, scn.GetKeywordCount(blademaster, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(blademaster2, Keyword.DAMAGE));

		scn.ShadowUseCardAction(blademaster);

		// He exerts and is damage +1; the other copy in the same skirmish is neither.
		assertEquals(1, scn.GetWoundsOn(blademaster));
		assertEquals(1, scn.GetKeywordCount(blademaster, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(blademaster2));
		assertEquals(0, scn.GetKeywordCount(blademaster2, Keyword.DAMAGE));

		// Vitality 2 with 1 wound: exhausted, so he can no longer exert; the other copy still can.  The bonus holds
		// through the skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blademaster));
		assertTrue(scn.ShadowActionAvailable(blademaster2));
		assertEquals(1, scn.GetKeywordCount(blademaster, Keyword.DAMAGE));
		scn.ShadowPass();

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetKeywordCount(blademaster, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(blademaster));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsOfferedToHimInNoSkirmishAndMakesHimDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blademaster = scn.GetShadowCard("blademaster");
		var runner = scn.GetShadowCard("runner");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(blademaster, runner);

		// Only the Runner is assigned, to Gimli; the Blademaster is in no skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(blademaster));

		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(blademaster));
		assertEquals(0, scn.GetWoundsOn(blademaster));
		assertEquals(0, scn.GetKeywordCount(blademaster, Keyword.DAMAGE));

		scn.ShadowUseCardAction(blademaster);

		assertEquals(1, scn.GetWoundsOn(blademaster));
		assertEquals(1, scn.GetKeywordCount(blademaster, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
