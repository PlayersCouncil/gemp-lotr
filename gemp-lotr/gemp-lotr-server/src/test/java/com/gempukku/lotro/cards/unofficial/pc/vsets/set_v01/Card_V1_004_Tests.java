
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V1_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("friend", "101_4");
					put("legolas", "1_50");
					put("gimli", "1_13");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElfriendStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: I Name You Elf-friend
		* Side: Free Peoples
		* Culture: dwarven
		* Twilight Cost: 1
		* Type: event
		* Subtype: Maneuver
		* Game Text: Exert an Elf to make Gimli strength +3 and damage +1 until the regroup phase.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl friend = scn.GetFreepsCard("friend");

		assertFalse(friend.getBlueprint().isUnique());
		assertEquals(1, friend.getBlueprint().getTwilightCost());
		assertEquals(CardType.EVENT, friend.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(friend, Timeword.MANEUVER));
		assertEquals(Culture.DWARVEN, friend.getBlueprint().getCulture());
		assertEquals(Side.FREE_PEOPLE, friend.getBlueprint().getSide());
	}

	@Test
	public void ElfriendExertsElfToPumpADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl friend = scn.GetFreepsCard("friend");
		PhysicalCardImpl gimli = scn.GetFreepsCard("gimli");
		PhysicalCardImpl legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(friend);
		scn.MoveCompanionToTable(gimli, legolas);

		scn.MoveMinionsToTable("runner");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable("Name You"));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsPlayCard(friend);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
	}
}
