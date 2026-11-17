package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ingold", "7_106");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a second wounded companion, left alone; a maneuver ability used only on purpose

					// First Level: [gondor] fortification condition for the support area; its maneuver transfer is used only on purpose
					put("level", "7_92");
					// City Wall: [gondor] fortification condition, here a card in hand; its skirmish transfer is used only on purpose
					put("wall", "5_33");
					// Stronghold of My Fathers: [rohan] fortification POSSESSION for the support area; a response when a
					// [rohan] companion is about to take a wound
					put("stronghold", "102_48");
					put("walls", "7_111");          // Man the Walls: [gondor] support condition that is no fortification

					put("troop", "1_177");          // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IngoldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Ingold
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>.<br><b>Fellowship:</b> Discard a fortification to heal Ingold twice.
		*/

		var scn = GetScenario();

		var ingold = scn.GetFreepsCard("ingold");

		assertEquals("Ingold", ingold.getBlueprint().getTitle());
		assertNull(ingold.getBlueprint().getSubtitle());
		assertTrue(ingold.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, ingold.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, ingold.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, ingold.getBlueprint().getCardType());
		assertEquals(Race.MAN, ingold.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(ingold, Keyword.KNIGHT));
		assertEquals(2, ingold.getBlueprint().getTwilightCost());
		assertEquals(6, ingold.getBlueprint().getStrength());
		assertEquals(3, ingold.getBlueprint().getVitality());
		assertEquals(6, ingold.getBlueprint().getResistance());
		assertNull(ingold.getBlueprint().getSignet());
	}

	@Test
	public void FellowshipActionDiscardsAFortificationOfEitherTypeFromPlayToHealIngoldTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ingold = scn.GetFreepsCard("ingold");
		var aragorn = scn.GetFreepsCard("aragorn");
		var level = scn.GetFreepsCard("level");
		var wall = scn.GetFreepsCard("wall");
		var stronghold = scn.GetFreepsCard("stronghold");
		var walls = scn.GetFreepsCard("walls");
		scn.MoveCompanionsToTable(ingold, aragorn);
		scn.MoveCardsToSupportArea(level, stronghold, walls);
		scn.MoveCardsToHand(wall);
		scn.AddWoundsToChar(ingold, 2);
		scn.AddWoundsToChar(aragorn, 1);
		assertInZone(Zone.SUPPORT, level, stronghold, walls);
		assertInHand(wall);
		assertEquals(2, scn.GetWoundsOn(ingold));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ingold));

		scn.FreepsUseCardAction(ingold);
		// Fortifications of either culture and type in play; not a [gondor] condition that is no fortification, and
		// not a fortification in hand ("discard" is from play, CR 4.2).
		assertTrue(scn.FreepsHasCardChoiceAvailable(level, stronghold));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(walls, wall));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(stronghold);
		assertInDiscard(stronghold);
		assertInZone(Zone.SUPPORT, level, walls);
		assertInHand(wall);
		// Both wounds healed (twice, not once), and only Ingold's.
		assertEquals(0, scn.GetWoundsOn(ingold));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneWoundBothHealsLeaveHimUnwoundedAndUnwoundedHeIsStillOfferedItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ingold = scn.GetFreepsCard("ingold");
		var level = scn.GetFreepsCard("level");
		var stronghold = scn.GetFreepsCard("stronghold");
		scn.MoveCompanionsToTable(ingold);
		scn.MoveCardsToSupportArea(level);
		scn.AddWoundsToChar(ingold, 1);
		assertInZone(Zone.SUPPORT, level);
		assertEquals(1, scn.GetWoundsOn(ingold));

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ingold));
		// The one fortification goes unasked.
		scn.FreepsUseCardAction(ingold);
		assertInDiscard(level);
		assertEquals(0, scn.GetWoundsOn(ingold));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An ability with a cost is offered though its effect has nothing to act on (Q-0034).
		scn.MoveCardsToSupportArea(stronghold);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, stronghold);
		assertTrue(scn.FreepsActionAvailable(ingold));
		scn.FreepsUseCardAction(ingold);
		assertInDiscard(stronghold);
		assertEquals(0, scn.GetWoundsOn(ingold));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAFortificationInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ingold = scn.GetFreepsCard("ingold");
		var wall = scn.GetFreepsCard("wall");
		var walls = scn.GetFreepsCard("walls");
		scn.MoveCompanionsToTable(ingold);
		scn.MoveCardsToSupportArea(walls);
		scn.MoveCardsToHand(wall);
		scn.AddWoundsToChar(ingold, 2);
		assertInZone(Zone.SUPPORT, walls);
		assertInHand(wall);
		assertEquals(2, scn.GetWoundsOn(ingold));

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ingold));

		// The same fortification in the support area pays the cost.
		scn.MoveCardsToSupportArea(wall);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.FreepsActionAvailable(ingold));
		assertEquals(2, scn.GetWoundsOn(ingold));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedInTheFellowshipPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ingold = scn.GetFreepsCard("ingold");
		var level = scn.GetFreepsCard("level");
		scn.MoveCompanionsToTable(ingold);
		scn.MoveCardsToSupportArea(level);
		scn.AddWoundsToChar(ingold, 1);
		assertInZone(Zone.SUPPORT, level);
		assertEquals(1, scn.GetWoundsOn(ingold));

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ingold));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ingold));
		assertInZone(Zone.SUPPORT, level);
		assertEquals(1, scn.GetWoundsOn(ingold));
	}
}
