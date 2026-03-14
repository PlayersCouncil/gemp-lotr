package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_106_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_106");
					put("pipe", "1_285");
					put("pipeweed1", "1_300");
					put("pipeweed2", "1_305");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FlotsamStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Flotsam
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Game Text: Pipeweed. Bearer must bear a pipe.
		* 	When you play this possession, you may shuffle up to 2 pipes or pipeweed from your discard pile into your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Flotsam", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FlotsamHasNoSkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		// Errata removes the skirmish ability, leaving only the play trigger
		var scn = GetScenario();

		var flotsam = scn.GetFreepsCard("card");
		var pipe = scn.GetFreepsCard("pipe");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();

		// Attach pipe to Frodo, then attach flotsam to Frodo (who bears a pipe)
		scn.AttachCardsTo(frodo, pipe);
		scn.AttachCardsTo(frodo, flotsam);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Skip to skirmish phase and verify Flotsam has no action available
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		// During skirmish, Flotsam should NOT have an available action (errata removed it)
		assertFalse(scn.FreepsActionAvailable(flotsam));
	}
}
