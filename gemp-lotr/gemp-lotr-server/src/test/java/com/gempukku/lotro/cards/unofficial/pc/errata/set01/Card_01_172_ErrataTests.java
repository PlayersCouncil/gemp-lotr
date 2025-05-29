package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_172_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer", "51_172");
					put("marksman", "1_176");
					put("commander", "2_49");
					put("bowman", "2_60");
					put("troop", "2_67");
					put("runner", "1_178");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void GoblinArcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Archer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Archer</b>.
		* 	The fellowship archery total is -1 for each [moria] archer you can spot.
		* 	Assignment: Exert this minion to prevent [moria] archers from being assigned to skirmishes.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("archer");

		assertEquals("Goblin Archer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ARCHER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void GoblinArcherMakesFellowshipArcheryTotalMinus1PerMoriaArcher() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var marksman = scn.GetShadowCard("marksman");
		var commander = scn.GetShadowCard("commander");
		var bowman = scn.GetShadowCard("bowman");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(marksman, commander, bowman, troop, runner);
		scn.MoveCardsToHand(archer);

		scn.StartGame();
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 6));
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(6, scn.GetFreepsArcheryTotal());
		scn.MoveCompanionToTable(archer);
		// -5 for the 5 archers, the runner doesn't count
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void GoblinArcherAssignmentAbilityPreventsMoriaArchersFromSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var marksman = scn.GetShadowCard("marksman");
		var commander = scn.GetShadowCard("commander");
		var bowman = scn.GetShadowCard("bowman");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(archer, marksman, commander, bowman, troop, runner);

		var frodo = scn.GetRingBearer();

		//so frodo doesn't die to all the arrows
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.SHADOW, -6));

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable(archer));
		assertEquals(0, scn.GetWoundsOn(archer));
		scn.ShadowUseCardAction(archer);

		assertEquals(1, scn.GetWoundsOn(archer));
		scn.PassAssignmentActions();

		//Only the runner is now assignable
		assertEquals(1, scn.FreepsGetShadowAssignmentTargetCount());

	}
}
