package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_12_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sharp", "12_13");
					put("gimli", "11_8");
					put("book", "3_1");
					put("helm", "1_15");

					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SharpDefenseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Sharp Defense
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a Dwarf strength +2 (or +2 for each possession he bears if he has resistance 4 or more).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sharp");

		assertEquals("Sharp Defense", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SharpDefenseMakesDwarfWith3ResistanceAndNoPossessionsStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(sharp);
		scn.MoveCompanionToTable(gimli);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.AddBurdens(3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(3, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(sharp);
		//Base 6, and since the 4 resistance clause is not met, +2 flat strength
		assertEquals(8, scn.GetStrength(gimli));
	}

	@Test
	public void SharpDefenseMakesDwarfWith4ResistanceAndNoPossessionsStrengthPlus0() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(sharp);
		scn.MoveCompanionToTable(gimli);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.AddBurdens(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(sharp);
		//Base 6, and since the 4 resistance clause is met, +0 for all attached possessions
		assertEquals(6, scn.GetStrength(gimli));
	}

	@Test
	public void SharpDefenseMakesDwarfWith4ResistanceAndNoPossessionsStrengthPlus2ForEachPossessionBorne() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sharp = scn.GetFreepsCard("sharp");
		var gimli = scn.GetFreepsCard("gimli");
		var book = scn.GetFreepsCard("book");
		var helm = scn.GetFreepsCard("helm");
		scn.MoveCardsToHand(sharp);
		scn.MoveCompanionToTable(gimli);
		scn.AttachCardsTo(gimli, book, helm);

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);

		scn.StartGame();

		scn.AddBurdens(2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(4, scn.GetResistance(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(sharp);
		//Base 6 + 2 for book + 2 for helm
		assertEquals(10, scn.GetStrength(gimli));
	}
}
