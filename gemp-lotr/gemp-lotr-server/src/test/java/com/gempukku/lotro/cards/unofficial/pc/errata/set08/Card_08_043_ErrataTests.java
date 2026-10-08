package com.gempukku.lotro.cards.unofficial.pc.errata.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_08_043_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "58_43");
					put("oath", "8_41");        // Oathbreaker: [gondor] Wraith companion; to play, add a threat
					put("deadman", "10_27");    // Dead Man of Dunharrow: [gondor] Wraith companion; to play, add a threat; exerts twice when played
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, no Wraith
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion with no text but its play requirement
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
		 * Race: Wraith
		 * Strength: 9
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Enduring. To play, spot 2 [gondor] Wraiths and add 2 threats.
		 * While Shadow Host is exhausted, it is defender +1.
		 */

		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");

		assertEquals("Shadow Host", host.getBlueprint().getTitle());
		assertNull(host.getBlueprint().getSubtitle());
		assertTrue(host.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, host.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, host.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, host.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, host.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(host, Keyword.ENDURING));
		assertEquals(5, host.getBlueprint().getTwilightCost());
		assertEquals(9, host.getBlueprint().getStrength());
		assertEquals(3, host.getBlueprint().getVitality());
		assertEquals(6, host.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, host.getBlueprint().getSignet());
	}

	@Test
	public void PlayedForFiveAndTwoThreatsOnSpottingTwoGondorWraithsWhetherExhaustedOrNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var oath = scn.GetFreepsCard("oath");
		var deadman = scn.GetFreepsCard("deadman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(oath, deadman, aragorn);
		// One Wraith exhausted, the other not.
		scn.AddWoundsToChar(deadman, 2);
		scn.MoveCardsToHand(host);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(host));

		scn.FreepsPlayCard(host);

		assertInZone(Zone.FREE_CHARACTERS, host);
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetThreats());
		// Spotting exerts nobody.
		assertEquals(0, scn.GetWoundsOn(oath));
		assertEquals(2, scn.GetWoundsOn(deadman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneGondorWraithBesideAGondorManIsNotEnoughUntilASecondWraithIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var oath = scn.GetFreepsCard("oath");
		var deadman = scn.GetFreepsCard("deadman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(oath, aragorn, guard);
		scn.MoveCardsToHand(host, deadman);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(host));

		scn.FreepsPlayCard(deadman);

		// Dead Man of Dunharrow added his own threat: 1, with room for 2 more among 5 companions.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.FreepsPlayAvailable(host));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileOnlyOneMoreThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");
		var oath = scn.GetFreepsCard("oath");
		var deadman = scn.GetFreepsCard("deadman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(oath, deadman);
		scn.MoveCardsToHand(host, aragorn);
		scn.AddThreats(2);

		scn.StartGame();

		// 3 companions (Frodo and the two Wraiths) and 2 threats: a third may be added, a fourth may not.
		assertEquals(2, scn.GetThreats());
		assertFalse(scn.FreepsPlayAvailable(host));

		scn.FreepsPlayCard(aragorn);

		// 4 companions: 2 more threats fit.
		assertTrue(scn.FreepsPlayAvailable(host));
		scn.FreepsPlayCard(host);

		assertInZone(Zone.FREE_CHARACTERS, host);
		assertEquals(4, scn.GetThreats());
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
