package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_091_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "103_91");
					put("eomer", "4_267");
					put("mount", "4_283");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VanguardsLanceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Vanguard's Lance
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [Rohan] companion.
		* 	While bearer is mounted, bearer is strength +1 and each time bearer wins a skirmish, you must exert them to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Vanguard's Lance", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void VanguardsLanceGivesPlus1StrengthWhenMounted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var lance = scn.GetFreepsCard("card");
		var eomer = scn.GetFreepsCard("eomer");
		var mount = scn.GetFreepsCard("mount");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eomer);
		scn.AttachCardsTo(eomer, lance);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Eomer base 7 + lance 2 = 9 without mount
		assertEquals(9, scn.GetStrength(eomer));

		// Add mount, Eomer should get +1 from errata (was +2 before)
		scn.AttachCardsTo(eomer, mount);
		// Eomer base 7 + lance 2 + mounted bonus 1 = 10
		assertEquals(10, scn.GetStrength(eomer));
	}
}
