package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "8_8");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDwarvesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wish", "8_8");
					// Thrarin, Dwarven Smith: Dwarf ally, vitality 2; his one ability is a maneuver ability
					put("thrarin", "1_27");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability that is never used
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, vitality 2; no text once in play
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion, vitality 3; an archery ability that is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WishForOurKinfolkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Wish For Our Kinfolk
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Fellowship:</b> Exert a Dwarf companion and either an [elven] companion or a [gondor] Man to add a [dwarven] token here.<br><b>Skirmish:</b> Remove a [dwarven] token here to make a companion <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Wish For Our Kinfolk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipActionIsPaidByADwarfCompanionAndNeverByADwarfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDwarvesScenario();

		var wish = scn.GetFreepsCard("wish");
		var thrarin = scn.GetFreepsCard("thrarin");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToSupportArea(wish);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();

		// Thrarin is a Dwarf who can exert and Legolas an [elven] companion who can: the one thing missing from
		// "exert a Dwarf companion" is that the Dwarf be a companion.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsActionAvailable(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCompanionsToTable(gimli, guard);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetCultureTokensOn(wish));
		assertTrue(scn.FreepsActionAvailable(wish));
		scn.FreepsUseCardAction(wish);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrarin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// Legolas is the only [elven] companion and there is no [gondor] Man, so he exerts with nothing asked.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetCultureTokensOn(wish));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
