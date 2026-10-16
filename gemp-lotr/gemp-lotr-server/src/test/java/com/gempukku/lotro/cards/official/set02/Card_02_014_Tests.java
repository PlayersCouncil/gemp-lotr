package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_014_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tale", "2_14");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion of another title, vitality 2; "To play, spot a Dwarf"
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally (allies do not move); a fellowship ability only
				}},
				// Sites 4 (Balin's Tomb) and 5 (The Bridge of Khazad-dum) are underground; 3 and 6 are not.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TillDurinWakesAgainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Till Durin Wakes Again
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale</b>. Plays to your support area.<br>Each time a Dwarf companion moves from an underground site, you may heal that Dwarf.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tale");

		assertEquals("Till Durin Wakes Again", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachMoveFromAnUndergroundSiteHealsTheDwarfCompanionsAndNoOthers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(tale);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(grimir, 1);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(grimir));

		// The fellowship moves from Balin's Tomb (underground) to The Bridge of Khazad-dum.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tale));
		scn.FreepsChooseOptionalTrigger(tale);

		// Each Dwarf companion that moved may be healed; the companion who is no Dwarf and the Dwarf ally may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, grimir));
		scn.FreepsChooseCards(gimli, guard);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(grimir));

		// A second move this turn, again from an underground site (The Bridge), to Galadriel's Glade.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tale));
		scn.FreepsChooseOptionalTrigger(tale);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MovingToAnUndergroundSiteOffersNothingAndDecliningAMoveFromOneLeavesTheWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var gimli = scn.GetFreepsCard("gimli");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(tale);
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(2, scn.GetWoundsOn(gimli));

		// From Rivendell Valley (not underground) to Balin's Tomb (underground): nothing is offered.
		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tale));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetWoundsOn(gimli));

		// From Balin's Tomb in the regroup phase: offered, and declined.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tale));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoDwarfCompanionWoundedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tale = scn.GetFreepsCard("tale");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(tale);
		// Wounded: the companion who is no Dwarf and the Dwarf ally, but no Dwarf companion.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(grimir, 1);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(grimir));

		scn.FreepsPass();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tale));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(grimir));
	}
}
