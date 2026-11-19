package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_364_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "7_364");
					put("boromir", "1_97");     // Boromir, Son of Denethor: another companion, strength 7; a skirmish ability used only on purpose
					put("dguard", "1_7");       // Dwarf Guard: a fourth companion, so that four threats can lie; no text but its requirement to play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Aragorn, Driven by Need
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: While you cannot spot 4 threats, Aragorn is strength +1 for each threat.
		 * 	<b>Regroup:</b> Exert Aragorn twice to remove 3 threats.
		 */

		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", aragorn.getBlueprint().getTitle());
		assertEquals("Driven by Need", aragorn.getBlueprint().getSubtitle());
		assertTrue(aragorn.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, aragorn.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, aragorn.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, aragorn.getBlueprint().getCardType());
		assertEquals(Race.MAN, aragorn.getBlueprint().getRace());
		assertEquals(4, aragorn.getBlueprint().getTwilightCost());
		assertEquals(7, aragorn.getBlueprint().getStrength());
		assertEquals(4, aragorn.getBlueprint().getVitality());
		assertEquals(6, aragorn.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, aragorn.getBlueprint().getSignet());
	}

	@Test
	public void StrengthIsPlusOneForEachThreatWhileFewerThanFourCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var dguard = scn.GetFreepsCard("dguard");
		// Four companions, so that four threats can lie.
		scn.MoveCompanionsToTable(aragorn, boromir, dguard);

		scn.StartGame();
		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		scn.AddThreats(2);
		assertEquals(3, scn.GetThreats());
		// 7 + 3 threats
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));

		// Four threats can be spotted: no bonus at all.
		scn.AddThreats(1);
		assertEquals(4, scn.GetThreats());
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
	}

	@Test
	public void RegroupAbilityExertsHimTwiceToRemoveThreeThreatsAndHisBonusComesBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var dguard = scn.GetFreepsCard("dguard");
		scn.MoveCompanionsToTable(aragorn, boromir, dguard);

		scn.StartGame();
		// One wound: 3 vitality left, just enough to exert twice.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddThreats(4);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetThreats());
		assertEquals(7, scn.GetStrength(aragorn));

		// No minion is played, so the turn goes from the move to site 2 straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(aragorn));

		scn.FreepsUseCardAction(aragorn);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetThreats());
		// Fewer than 4 threats again: 7 + 1 threat
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithTwoThreatsTheRegroupAbilityRemovesBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// 7 + 2 threats
		assertEquals(9, scn.GetStrength(aragorn));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsActionAvailable(aragorn));

		scn.FreepsUseCardAction(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetThreats());
		assertEquals(7, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileHeCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var dguard = scn.GetFreepsCard("dguard");
		scn.MoveCompanionsToTable(aragorn, boromir, dguard);

		scn.StartGame();
		// Two wounds: 2 vitality left, so he can exert once and not twice.
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddThreats(4);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetThreats());
	}
}
