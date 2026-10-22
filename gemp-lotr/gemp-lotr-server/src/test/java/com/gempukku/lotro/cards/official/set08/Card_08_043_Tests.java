package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "8_43");
					put("oath", "8_41");        // Oathbreaker: [gondor] Wraith companion, vitality 3; to play, add a threat
					// Dead Man of Dunharrow: [gondor] Wraith companion, vitality 3; to play, add a threat; exerts twice as
					// he is played
					put("deadman", "10_27");
					put("deadman2", "10_27");
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, vitality 3; Knight is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShadowHostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Shadow Host
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 9
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Enduring</b>. To play, spot 2 exhausted [gondor] Wraiths and add 2 threats.<br>While Shadow Host is exhausted, it is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("host");

		assertEquals("Shadow Host", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void OneExhaustedGondorWraithIsNotEnoughUntilASecondIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var oath = scn.GetFreepsCard("oath");
		var deadman = scn.GetFreepsCard("deadman");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(deadman, oath, gknight);
		// One exhausted [gondor] Wraith; beside him a [gondor] Wraith who is not exhausted (vitality 3, 1 wound)
		// and an exhausted [gondor] Man who is no Wraith.
		scn.AddWoundsToChar(deadman, 2);
		scn.AddWoundsToChar(oath, 1);
		scn.AddWoundsToChar(gknight, 2);
		scn.MoveCardsToHand(host, deadman2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertEquals(1, scn.GetWoundsOn(oath));
		assertEquals(2, scn.GetWoundsOn(gknight));
		assertEquals(0, scn.GetThreats());
		assertInZone(Zone.HAND, host, deadman2);
		assertFalse(scn.FreepsPlayAvailable(host));

		// The second Dead Man adds his own threat and exerts himself twice as he is played: exhausted.
		scn.FreepsPlayCard(deadman2);
		assertInZone(Zone.FREE_CHARACTERS, deadman2);
		assertEquals(2, scn.GetWoundsOn(deadman2));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(host));

		// Five companions leave room for two more threats.
		scn.FreepsPlayCard(host);
		assertInZone(Zone.FREE_CHARACTERS, host);
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileOnlyOneMoreThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var oath = scn.GetFreepsCard("oath");
		var deadman = scn.GetFreepsCard("deadman");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(oath, deadman);
		// Two exhausted [gondor] Wraiths of different titles.
		scn.AddWoundsToChar(oath, 2);
		scn.AddWoundsToChar(deadman, 2);
		scn.MoveCardsToHand(host, gknight);

		scn.StartGame();
		// Frodo and the two Wraiths: three companions, so with 2 threats only one more can be added (Shadow Host is
		// not in play while his cost is paid).
		scn.AddThreats(2);
		// The choice of fellowship actions was sent before the threats were added.
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());
		assertEquals(2, scn.GetWoundsOn(oath));
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertInZone(Zone.HAND, host, gknight);
		assertFalse(scn.FreepsPlayAvailable(host));

		scn.FreepsPlayCard(gknight);
		assertInZone(Zone.FREE_CHARACTERS, gknight);
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(host));

		scn.FreepsPlayCard(host);
		assertInZone(Zone.FREE_CHARACTERS, host);
		assertEquals(4, scn.GetThreats());
		// Spotting exerts nobody.
		assertEquals(2, scn.GetWoundsOn(oath));
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExhaustedByASkirmishWoundShadowHostIsDefenderPlusOneAndAnotherExhaustedWraithIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var oath = scn.GetFreepsCard("oath");
		scn.MoveCompanionsToTable(host, oath);
		scn.AddWoundsToChar(host, 1);
		scn.AddWoundsToChar(oath, 2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		assertEquals(1, scn.GetWoundsOn(host));
		assertEquals(2, scn.GetWoundsOn(oath));
		// Vitality 3 with 1 wound: not exhausted.
		assertEquals(0, scn.GetKeywordCount(host, Keyword.DEFENDER));
		// Oathbreaker is exhausted, but he is not Shadow Host.
		assertEquals(0, scn.GetKeywordCount(oath, Keyword.DEFENDER));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(host, troop);
		scn.PassSkirmishActions();

		// Strength 9 + 2 (enduring, 1 wound) loses to 13: a second wound, and Shadow Host is exhausted.
		assertEquals(2, scn.GetWoundsOn(host));
		assertEquals(1, scn.GetKeywordCount(host, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(oath, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
