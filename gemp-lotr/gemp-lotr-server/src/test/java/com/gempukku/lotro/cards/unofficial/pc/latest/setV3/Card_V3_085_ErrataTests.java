package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_085_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_85");
					put("eowyn", "4_270");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Merry, Master Holbytla
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Valiant. Enduring.
		* 	While you can spot 3 [rohan] companions (or Eowyn), Merry is considered a [Rohan] Man.
		* 	Each time another [rohan] Man exerts, you may exert Merry to heal that Man.
		* 	Skirmish: If he is not assigned to a skirmish, hinder Merry to immediately resolve a skirmish involving a [rohan] Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Master Holbytla", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void MerryBecomesRohanManWhenSpottingEowyn() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("card");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		// Without Eowyn, Merry should be a Hobbit but not a Man
		assertFalse(scn.IsRace(merry, Race.MAN));

		// Play Eowyn to satisfy the "or Eowyn" condition
		scn.MoveCompanionsToTable(eowyn);

		// Now Merry should also be considered a Man
		assertTrue(scn.IsRace(merry, Race.MAN));
	}
}
