package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "11_23");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetToilScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_23");
					// Legolas, Companion of the Ring: vitality 3; his own text needs a battleground or a forest
					put("legolas", "11_21");
					// Elven Marksmanship: [elven] condition of the support area, cost 6, not unique.  "To play, spot
					// an Elf.  Toil 2."  Its archery ability is not used.
					put("marksmanship1", "11_17");
					put("marksmanship2", "11_17");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Legolas' Bow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be an Elf.<br>Bearer is an <b>archer</b>.<br>If bearer is Legolas, each time you exert him to play an [elven] condition or [elven] event, you may heal him (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Legolas' Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingLegolasForToilToPlayAnElvenConditionOffersTheHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetToilScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var marksmanship1 = scn.GetFreepsCard("marksmanship1");
		var marksmanship2 = scn.GetFreepsCard("marksmanship2");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(marksmanship1, marksmanship2);

		scn.StartGame();
		// One wound to begin with, so that a heal of one wound reads apart from a heal of them all.
		scn.AddWoundsToChar(legolas, 1);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));

		// Played with nobody exerted for toil: the whole cost, and nothing for the Bow to answer.
		scn.FreepsPlayCard(marksmanship1);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.SUPPORT, marksmanship1);
		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Played with Legolas exerted for toil: that exertion is one made to play an [elven] condition, and
		// the Bow answers it while the condition is still on its way to the table.
		scn.FreepsPlayCard(marksmanship2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		scn.FreepsChooseCard(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInZone(Zone.SUPPORT, marksmanship2);
		// 6 + (6 - 2)
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
