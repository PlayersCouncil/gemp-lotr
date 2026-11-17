package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("captain", "7_96");
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: a third companion, so that threats fit; a maneuver ability used only on purpose

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
	public void GondorianCaptainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gondorian Captain
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>.<br><b>Fellowship:</b> Discard a fortification to remove a threat.
		*/

		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");

		assertEquals("Gondorian Captain", captain.getBlueprint().getTitle());
		assertNull(captain.getBlueprint().getSubtitle());
		assertFalse(captain.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, captain.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, captain.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, captain.getBlueprint().getCardType());
		assertEquals(Race.MAN, captain.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(captain, Keyword.KNIGHT));
		assertEquals(2, captain.getBlueprint().getTwilightCost());
		assertEquals(6, captain.getBlueprint().getStrength());
		assertEquals(3, captain.getBlueprint().getVitality());
		assertEquals(6, captain.getBlueprint().getResistance());
		assertNull(captain.getBlueprint().getSignet());
	}

	@Test
	public void FellowshipActionDiscardsAFortificationOfEitherTypeFromPlayToRemoveOneThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");
		var level = scn.GetFreepsCard("level");
		var wall = scn.GetFreepsCard("wall");
		var stronghold = scn.GetFreepsCard("stronghold");
		var walls = scn.GetFreepsCard("walls");
		scn.MoveCompanionsToTable(captain, scn.GetFreepsCard("aragorn"));
		scn.MoveCardsToSupportArea(level, stronghold, walls);
		scn.MoveCardsToHand(wall);
		assertInZone(Zone.SUPPORT, level, stronghold, walls);
		assertInHand(wall);

		scn.StartGame();

		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(captain));

		scn.FreepsUseCardAction(captain);
		// Fortifications of either culture and type in play; not a [gondor] condition that is no fortification, and
		// not a fortification in hand ("discard" is from play, CR 4.2).
		assertTrue(scn.FreepsHasCardChoiceAvailable(level, stronghold));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(walls, wall));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(stronghold);
		assertInDiscard(stronghold);
		assertInZone(Zone.SUPPORT, level, walls);
		assertInHand(wall);
		// One threat removed, not all.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyAFortificationInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");
		var wall = scn.GetFreepsCard("wall");
		var walls = scn.GetFreepsCard("walls");
		scn.MoveCompanionsToTable(captain);
		scn.MoveCardsToSupportArea(walls);
		scn.MoveCardsToHand(wall);
		assertInZone(Zone.SUPPORT, walls);
		assertInHand(wall);

		scn.StartGame();

		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(captain));

		// The same fortification in the support area pays the cost.
		scn.MoveCardsToSupportArea(wall);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.FreepsActionAvailable(captain));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfferedInTheFellowshipPhaseAndNotInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");
		var level = scn.GetFreepsCard("level");
		scn.MoveCompanionsToTable(captain);
		scn.MoveCardsToSupportArea(level);
		assertInZone(Zone.SUPPORT, level);

		scn.StartGame();

		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(captain));

		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(captain));
		assertInZone(Zone.SUPPORT, level);
		assertEquals(1, scn.GetThreats());
	}

	@Test
	public void WithNoThreatItIsStillOfferedAndTheFortificationIsDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var captain = scn.GetFreepsCard("captain");
		var level = scn.GetFreepsCard("level");
		scn.MoveCompanionsToTable(captain);
		scn.MoveCardsToSupportArea(level);
		assertInZone(Zone.SUPPORT, level);

		scn.StartGame();

		// An ability with a cost is offered though its effect has nothing to act on (Q-0034).
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsActionAvailable(captain));

		// The one fortification goes unasked.
		scn.FreepsUseCardAction(captain);
		assertInDiscard(level);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
