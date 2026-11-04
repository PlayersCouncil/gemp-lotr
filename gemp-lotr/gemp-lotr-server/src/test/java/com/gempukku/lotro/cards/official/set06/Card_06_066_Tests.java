package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("builder", "6_66");
					put("builder2", "6_66");
					// Iron Fist of the Orc: an [isengard] condition that Orcs a regroup action discards are stacked on
					put("fist", "6_64");
					// Library of Orthanc: an [isengard] artifact, another type of [isengard] card, that takes cards stacked
					put("library", "9_39");
					// Web: a [gollum] possession whose regroup ability stacks the Shadow player's Orc on it
					put("web", "8_30");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardBuilderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Isengard Builder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> If stacked on an [isengard] card, discard this card to add (2).
		*/

		var scn = GetScenario();

		var builder = scn.GetFreepsCard("builder");

		assertEquals("Isengard Builder", builder.getBlueprint().getTitle());
		assertNull(builder.getBlueprint().getSubtitle());
		assertFalse(builder.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, builder.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, builder.getBlueprint().getCulture());
		assertEquals(CardType.MINION, builder.getBlueprint().getCardType());
		assertEquals(Race.ORC, builder.getBlueprint().getRace());
		assertEquals(3, builder.getBlueprint().getTwilightCost());
		assertEquals(7, builder.getBlueprint().getStrength());
		assertEquals(3, builder.getBlueprint().getVitality());
		assertEquals(4, builder.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityDiscardsTheBuilderFromAnIsengardCardToAdd2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var builder = scn.GetShadowCard("builder");
		var builder2 = scn.GetShadowCard("builder2");
		var fist = scn.GetShadowCard("fist");
		var library = scn.GetShadowCard("library");
		scn.MoveCardsToSupportArea(fist, library);
		scn.StackCardsOn(fist, builder);
		scn.StackCardsOn(library, builder2);

		scn.StartGame();
		assertInZone(Zone.STACKED, builder, builder2);
		// No minion is in play, so the game goes from the Shadow phase straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(builder));
		assertTrue(scn.ShadowActionAvailable(builder2));
		scn.ShadowUseCardAction(builder);

		// The Builder used goes from Iron Fist to the discard pile; the other copy stays on the Library.
		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, builder);
		assertInZone(Zone.STACKED, builder2);
		assertEquals(0, scn.GetStackedCards(fist).size());
		assertEquals(1, scn.GetStackedCards(library).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();

		// Stacked on an [isengard] artifact it works as well.
		assertTrue(scn.ShadowActionAvailable(builder2));
		scn.ShadowUseCardAction(builder2);
		assertEquals(pool + 4, scn.GetTwilight());
		assertInZone(Zone.DISCARD, builder2);
		assertEquals(0, scn.GetStackedCards(library).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AbilityIsNotOfferedStackedOnACardOfAnotherCultureNorInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var builder = scn.GetShadowCard("builder");
		var builder2 = scn.GetShadowCard("builder2");
		var web = scn.GetShadowCard("web");
		scn.MoveCardsToSupportArea(web);
		scn.StackCardsOn(web, builder);

		scn.StartGame();
		assertInZone(Zone.STACKED, builder);
		// The other Builder is a minion in play, left unassigned so that it lives into the regroup phase.
		scn.MoveMinionsToTable(builder2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertInZone(Zone.STACKED, builder);
		assertInZone(Zone.SHADOW_CHARACTERS, builder2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(builder));
		assertFalse(scn.ShadowActionAvailable(builder2));
	}
}
