package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deadman", "10_27");
					put("deadman2", "10_27");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, Knight and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeadManofDunharrowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Dead Man of Dunharrow
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Enduring</b>. To play, add a threat.<br>When you play this companion, exert him twice.
		*/

		var scn = GetScenario();

		var deadman = scn.GetFreepsCard("deadman");

		assertEquals("Dead Man of Dunharrow", deadman.getBlueprint().getTitle());
		assertNull(deadman.getBlueprint().getSubtitle());
		assertFalse(deadman.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, deadman.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, deadman.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, deadman.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, deadman.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(deadman, Keyword.ENDURING));
		assertEquals(2, deadman.getBlueprint().getTwilightCost());
		assertEquals(6, deadman.getBlueprint().getStrength());
		assertEquals(3, deadman.getBlueprint().getVitality());
		assertEquals(6, deadman.getBlueprint().getResistance());
	}

	@Test
	public void IsPlayedByAddingAThreatAndExertsHimselfTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deadman = scn.GetFreepsCard("deadman");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(deadman);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsPlayAvailable(deadman));
		scn.FreepsPlayCard(deadman);

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.FREE_CHARACTERS, deadman);
		// Exerted twice: on him, and on no other companion.
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotPlayableWhenAThreatCannotBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCardsToHand(deadman);

		scn.StartGame();

		// The Ring-bearer is the only companion in play, so one threat is as many as there may be; he is not
		// counted as a companion before he is played.
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.HAND, deadman);
		assertFalse(scn.FreepsPlayAvailable(deadman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// With the threat gone he is playable again.
		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(deadman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondCopyPlayedExertsOnlyItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deadman = scn.GetFreepsCard("deadman");
		var deadman2 = scn.GetFreepsCard("deadman2");
		scn.MoveCompanionsToTable(deadman);
		scn.MoveCardsToHand(deadman2);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetThreats());
		scn.FreepsPlayCard(deadman2);

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(deadman2));
		// The copy already in play is not set off by another companion of his kind being played.
		assertEquals(0, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedIntoTheStartingFellowshipHeAddsAThreatAndExertsTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deadman = scn.GetFreepsCard("deadman");

		// CR 4.2 "starting fellowship": "When you play" text is used on a starting companion (Q-0305).
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(deadman));

		scn.FreepsChooseCard(deadman);

		assertInZone(Zone.FREE_CHARACTERS, deadman);
		assertEquals(2, scn.GetWoundsOn(deadman));
		// Gondorian Knight (2) still fits beside him under the total of 4; he is not chosen, and player 2 then
		// chooses. GetThreats reads the current player's threats, so the threat is read once turn 1 has begun.
		assertTrue(scn.FreepsDecisionAvailable("Starting fellowship"));
		scn.FreepsDeclineChoosing();
		scn.ShadowDeclineChoosing();
		scn.StartGame(true, false);

		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
