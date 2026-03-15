package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strength", "103_59");
					put("southron1", "4_222");   // Desert Warrior (non-unique Raider)
					put("southron2", "4_222");
					put("southron3", "4_222");
					put("southron4", "4_222");
					put("southron5", "4_222");
					put("southron6", "4_222");
					put("southron7", "4_222");
					put("southron8", "4_222");
					put("runner", "1_178");      // Goblin Runner (non-Raider)
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrengthofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Strength of Men
		 * Unique: false
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot 2 [raider] cards to restore 2 [raider] cards (or reconcile your hand
		 *  if you can spot 5 burdens or 6 [raider] cards).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strength");

		assertEquals("Strength of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotPlayWithFewerThan2RaiderCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var southron1 = scn.GetShadowCard("southron1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCardsToHand(strength);
		scn.MoveMinionsToTable(southron1, runner);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		// Only 1 Raider card in play — does not meet spot 2 requirement
		assertFalse(scn.ShadowPlayAvailable(strength));
	}

	@Test
	public void Restores2RaiderCardsBelowThresholds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var southron4 = scn.GetShadowCard("southron4");

		scn.MoveCardsToHand(strength);
		scn.MoveMinionsToTable(southron1, southron2, southron3, southron4);
		scn.HinderCard(southron1, southron2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.IsHindered(southron1));
		assertTrue(scn.IsHindered(southron2));

		// 2 Raider cards, 0 burdens — below both thresholds, restore path
		assertTrue(scn.ShadowPlayAvailable(strength));
		scn.ShadowPlayCard(strength);

		// Only 2 Raider cards on table — auto-selected for restore
		assertFalse(scn.IsHindered(southron1));
		assertFalse(scn.IsHindered(southron2));
	}

	@Test
	public void ReconcilesHandWith5BurdensInsteadOfRestoring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var southron4 = scn.GetShadowCard("southron4");

		scn.MoveCardsToHand(strength);
		scn.MoveMinionsToTable(southron1, southron2, southron3, southron4);
		scn.HinderCard(southron1, southron2);

		scn.StartGame();
		scn.AddBurdens(5);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(strength);

		// With 5 burdens, reconcile fires instead of restore
		assertFalse(scn.IsHindered(southron1));
		assertFalse(scn.IsHindered(southron2));
		assertEquals(6, scn.GetShadowHandCount()); //Drew cards after playing Strength of Men
	}

	@Test
	public void ReconcilesHandWith6RaiderCardsInsteadOfRestoring() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strength = scn.GetShadowCard("strength");
		var southron1 = scn.GetShadowCard("southron1");
		var southron2 = scn.GetShadowCard("southron2");
		var southron3 = scn.GetShadowCard("southron3");
		var southron4 = scn.GetShadowCard("southron4");
		var southron5 = scn.GetShadowCard("southron5");
		var southron6 = scn.GetShadowCard("southron6");
		var southron7 = scn.GetShadowCard("southron7");
		var southron8 = scn.GetShadowCard("southron8");

		scn.MoveCardsToHand(strength);
		scn.MoveMinionsToTable(southron1, southron2, southron3, southron4, southron5, southron6, southron7, southron8);
		scn.HinderCard(southron1, southron2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(strength);

		// With 6 Raider cards, reconcile fires instead of restore
		assertFalse(scn.IsHindered(southron1));
		assertFalse(scn.IsHindered(southron2));
		assertEquals(2, scn.GetShadowHandCount()); //Drew a card after playing Strength of Mensouthron1, southron2,
	}
}
