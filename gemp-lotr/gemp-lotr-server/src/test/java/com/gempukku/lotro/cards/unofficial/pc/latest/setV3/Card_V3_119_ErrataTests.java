package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_119_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_119");
					put("pipeweed1", "1_300");
					put("pipeweed2", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreasuredPipeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Treasured Pipe
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Pipe
		 * Game Text: To play, spot a pipe or an unbound Hobbit. Bearer must be a companion.
		* 	Fellowship: Discard 2 pipeweeds and spot X pipes to make X companions strength +1 until the regroup phase.  Hinder this pipe.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Treasured Pipe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.PIPE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TreasuredPipeRequires2PipeweedsToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata changes discard cost from 1 pipeweed to 2 pipeweeds
		var scn = GetScenario();

		var pipe = scn.GetFreepsCard("card");
		var pipeweed1 = scn.GetFreepsCard("pipeweed1");
		var pipeweed2 = scn.GetFreepsCard("pipeweed2");
		var frodo = scn.GetRingBearer();

		// Attach pipe to Frodo (who is an unbound Hobbit, satisfying play condition)
		scn.AttachCardsTo(frodo, pipe);

		// Put only 1 pipeweed in support area - should NOT be enough
		scn.MoveCardsToSupportArea(pipeweed1);

		scn.StartGame();

		// With only 1 pipeweed, the ability should not be available
		// because the errata requires discarding 2 pipeweeds
		assertFalse(scn.FreepsActionAvailable(pipe));

		// Add a second pipeweed - now it should be enough
		scn.MoveCardsToSupportArea(pipeweed2);
		assertTrue(scn.FreepsActionAvailable(pipe));
	}
}
