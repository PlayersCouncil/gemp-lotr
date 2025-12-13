package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("king", "103_27");
					put("deadman1", "10_27"); // Dead Man of Dunharrow
					put("deadman2", "10_27");
					put("aragorn", "1_89");

					put("slayer", "3_93"); // Morgul Slayer - wounds companion in Regroup
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KingoftheDeadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: King of the Dead, Dead Keeper
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wraith
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: Enduring.  To play, add 2 threats.
		 * 	Each time King of the Dead is killed, heal every Wraith.
		 * 	Assignment: Exert King of the Dead and restore a Wraith to heal that Wraith.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("king");

		assertEquals("King of the Dead", card.getBlueprint().getTitle());
		assertEquals("Dead Keeper", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WRAITH, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void KingOfTheDeadAddsTwoThreatsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(king);

		scn.StartGame();

		assertEquals(0, scn.GetThreats());

		assertTrue(scn.FreepsPlayAvailable(king));
		scn.FreepsPlayCard(king);

		assertInZone(Zone.FREE_CHARACTERS, king);
		assertEquals(2, scn.GetThreats());
	}

	@Test
	public void KingOfTheDeadCannotPlayIfThreatLimitExceeded() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("aragorn");
		// 2 companions (Frodo + Aragorn) = threat limit 2

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(king);

		// Pre-add 1 threat, only 1 room remaining but King needs 2
		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(2, scn.GetThreatLimit());

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(king));
	}

	@Test
	public void KingOfTheDeadHasEnduring() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn, king);

		scn.StartGame();

		// Base strength 7
		assertEquals(7, scn.GetStrength(king));

		// Add 1 wound - enduring gives +2 per wound
		scn.AddWoundsToChar(king, 1);
		assertEquals(9, scn.GetStrength(king));

		// Add another wound
		scn.AddWoundsToChar(king, 1);
		assertEquals(11, scn.GetStrength(king));
	}

	@Test
	public void KingOfTheDeadHealsAllWraithsWhenKilled() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(king, deadman1, deadman2);

		scn.StartGame();

		// Wound the Dead Men
		scn.AddWoundsToChar(deadman1, 2);
		scn.AddWoundsToChar(deadman2, 1);

		// Wound King to 1 vitality (vitality 4, so 3 wounds)
		scn.AddWoundsToChar(king, 3);
		assertEquals(1, scn.GetVitality(king));

		scn.SkipToPhase(Phase.REGROUP);

		scn.MoveMinionsToTable(slayer);

		scn.FreepsPass();

		assertEquals(2, scn.GetWoundsOn(deadman1));
		assertEquals(1, scn.GetWoundsOn(deadman2));

		// Use Slayer to kill King
		scn.ShadowUseCardAction(slayer);
		scn.ShadowChooseCard(king);
		scn.FreepsResolveRuleFirst();

		// King is killed
		assertInZone(Zone.DEAD, king);

		// All Wraiths healed (including the Dead Men)
		assertEquals(1, scn.GetWoundsOn(deadman1)); // Was 2, now 1
		assertEquals(0, scn.GetWoundsOn(deadman2)); // Was 1, now 0
	}

	@Test
	public void KingOfTheDeadAssignmentAbilityRestoresAndHealsWraith() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(king, deadman1);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Wound and hinder Dead Man
		scn.AddWoundsToChar(deadman1, 2);
		scn.HinderCard(deadman1);

		assertEquals(2, scn.GetWoundsOn(deadman1));
		assertTrue(scn.IsHindered(deadman1));
		assertEquals(0, scn.GetWoundsOn(king));

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(king));
		scn.FreepsUseCardAction(king);

		// Dead Man auto-selected as only hindered Wraith to restore

		// King exerted
		assertEquals(1, scn.GetWoundsOn(king));

		// Dead Man restored and healed
		assertFalse(scn.IsHindered(deadman1));
		assertEquals(1, scn.GetWoundsOn(deadman1)); // Was 2, healed 1
	}

	@Test
	public void KingOfTheDeadAssignmentAbilityNotAvailableWithoutHinderedWraith() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(king, deadman1);
		scn.MoveMinionsToTable(runner);
		// Dead Man is NOT hindered

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Cannot use - no hindered Wraith to restore
		assertFalse(scn.FreepsActionAvailable(king));
	}

	@Test
	public void KingOfTheDeadAssignmentAbilityNotAvailableIfExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(king, deadman1);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Exhaust King (vitality 4, so 3 wounds)
		scn.AddWoundsToChar(king, 3);
		assertEquals(1, scn.GetVitality(king));

		// Hinder Dead Man
		scn.HinderCard(deadman1);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Cannot use - King exhausted, cannot exert
		assertFalse(scn.FreepsActionAvailable(king));
	}

	@Test
	public void KingOfTheDeadAssignmentAbilityCanChooseWhichWraithToRestore() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var king = scn.GetFreepsCard("king");
		var deadman1 = scn.GetFreepsCard("deadman1");
		var deadman2 = scn.GetFreepsCard("deadman2");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(king, deadman1, deadman2);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		// Wound and hinder both Dead Men
		scn.AddWoundsToChar(deadman1, 2);
		scn.AddWoundsToChar(deadman2, 1);
		scn.HinderCard(deadman1);
		scn.HinderCard(deadman2);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsUseCardAction(king);

		// Should have choice between both hindered Wraiths
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(deadman1);

		// Dead Man 1 restored and healed
		assertFalse(scn.IsHindered(deadman1));
		assertEquals(1, scn.GetWoundsOn(deadman1));

		// Dead Man 2 still hindered
		assertTrue(scn.IsHindered(deadman2));
		assertEquals(1, scn.GetWoundsOn(deadman2));
	}
}
