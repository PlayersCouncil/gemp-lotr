package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_217_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "7_217");
					// Cards for the Free Peoples hand, which decides who has initiative.
					put("hand1", "1_13");
					put("hand2", "1_13");
					put("hand3", "1_13");
					put("hand4", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Nertëa, Black-Mantled Wraith
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Regroup:</b> If you have initiative, discard Úlairë Nertëa to add a burden.
		*/

		var scn = GetScenario();

		var nertea = scn.GetFreepsCard("nertea");

		assertEquals("Úlairë Nertëa", nertea.getBlueprint().getTitle());
		assertEquals("Black-Mantled Wraith", nertea.getBlueprint().getSubtitle());
		assertTrue(nertea.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, nertea.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, nertea.getBlueprint().getCulture());
		assertEquals(CardType.MINION, nertea.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, nertea.getBlueprint().getRace());
		assertEquals(4, nertea.getBlueprint().getTwilightCost());
		assertEquals(9, nertea.getBlueprint().getStrength());
		assertEquals(2, nertea.getBlueprint().getVitality());
		assertEquals(3, nertea.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithInitiativeRegroupAbilityDiscardsHerToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");

		scn.StartGame();
		// No minion in the Shadow phase: straight on to the regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(nertea);
		// Three cards in the Free Peoples hand: the Shadow has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(nertea));

		scn.ShadowUseCardAction(nertea);
		assertInDiscard(nertea);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithFourCardsInTheFreePeoplesHandRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");

		scn.StartGame();
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(nertea);
		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"),
				scn.GetFreepsCard("hand3"), scn.GetFreepsCard("hand4"));
		scn.FreepsPass();

		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nertea));
		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(0, scn.GetBurdens());
	}
}
