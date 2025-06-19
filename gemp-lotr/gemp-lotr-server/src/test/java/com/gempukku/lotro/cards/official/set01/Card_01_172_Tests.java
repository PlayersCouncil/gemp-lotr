package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_172_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("archer", "1_172");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
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
		 * Game Text: <b>Archer</b>.<br>While you can spot another [moria] Orc, the fellowship archery total is -6.
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
	public void GoblinArcherSpotsAnotherMoriaOrcToMakeFellowshipArcheryTotalMinus6() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		var runner = scn.GetShadowCard("runner");

		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToHand(archer);

		scn.StartGame();
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 7));
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(7, scn.GetFreepsArcheryTotal());
		scn.MoveCompanionsToTable(archer);
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void GoblinArcherDoesNotAlterFellowshipArcheryTotalIfNoOtherMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var archer = scn.GetShadowCard("archer");
		scn.MoveMinionsToTable(archer);

		scn.StartGame();
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 7));
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(7, scn.GetFreepsArcheryTotal());
	}
}
