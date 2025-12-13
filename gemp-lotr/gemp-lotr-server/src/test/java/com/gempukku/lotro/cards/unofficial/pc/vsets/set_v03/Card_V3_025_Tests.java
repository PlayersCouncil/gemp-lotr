package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gate", "103_25");
					put("gate2", "103_25");
					put("gate3", "103_25"); // Third copy for uniqueness test
					put("knight1", "8_39"); // Knight of Dol Amroth
					put("knight2", "8_39");
					put("knight3", "8_39");
					put("aragorn", "1_89");

					put("runner1", "1_178");
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void InnerGateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Inner Gate
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Strength: -3
		 * Game Text: Fortification.
		* 	Maneuver: If this is in your support area, exert and hinder 2 unbound knights to transfer this to a minion.  Hinder that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gate");

		assertEquals("Inner Gate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-3, card.getBlueprint().getStrength());
	}


	@Test
	public void InnerGateTransfersToMinionAndHindersIt() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(gate);
		scn.MoveMinionsToTable(runner1, runner2);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(knight1));
		assertEquals(0, scn.GetWoundsOn(knight2));
		assertFalse(scn.IsHindered(knight1));
		assertFalse(scn.IsHindered(knight2));
		assertFalse(scn.IsHindered(runner1));
		assertInZone(Zone.SUPPORT, gate);

		assertTrue(scn.FreepsActionAvailable(gate));
		scn.FreepsUseCardAction(gate);

		// Knights exerted and hindered
		assertEquals(1, scn.GetWoundsOn(knight1));
		assertEquals(1, scn.GetWoundsOn(knight2));
		assertTrue(scn.IsHindered(knight1));
		assertTrue(scn.IsHindered(knight2));

		// Should have choice between runners
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		scn.FreepsChooseCard(runner2);

		assertAttachedTo(gate, runner2);
		assertTrue(scn.IsHindered(runner2));
		assertFalse(scn.IsHindered(runner1));
	}

	@Test
	public void InnerGateNotAvailableWithoutTwoHinderableKnights() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var runner1 = scn.GetShadowCard("runner1");
		// Only 1 knight

		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToSupportArea(gate);
		scn.MoveMinionsToTable(runner1);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Cannot use - need 2 knights
		assertFalse(scn.FreepsActionAvailable(gate));
	}

	@Test
	public void InnerGateNotAvailableIfKnightsAlreadyHindered() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var runner1 = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(gate);
		scn.MoveMinionsToTable(runner1);

		scn.StartGame();

		// Pre-hinder one knight
		scn.HinderCard(knight1);

		scn.SkipToPhase(Phase.MANEUVER);

		// Cannot use - only 1 hinderable knight (knight2), need 2
		assertFalse(scn.FreepsActionAvailable(gate));
	}

	@Test
	public void InnerGateNotAvailableIfAlreadyAttached() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var knight3 = scn.GetFreepsCard("knight3");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.MoveCompanionsToTable(knight1, knight2, knight3);
		scn.MoveMinionsToTable(runner1, runner2);
		// Gate already attached to runner1
		scn.AttachCardsTo(runner1, gate);

		scn.StartGame();
		scn.SkipToPhase(Phase.MANEUVER);

		// Cannot use - gate is not in support area
		assertFalse(scn.FreepsActionAvailable(gate));

		//Base strength 5, -3 from gate
		assertEquals(2, scn.GetStrength(runner1));
	}

	@Test
	public void InnerGateNotAvailableIfKnightsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate = scn.GetFreepsCard("gate");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var runner1 = scn.GetShadowCard("runner1");

		scn.MoveCompanionsToTable(knight1, knight2);
		scn.MoveCardsToSupportArea(gate);
		scn.MoveMinionsToTable(runner1);

		scn.StartGame();

		// Exhaust knight1 (Knight of Dol Amroth has vitality 3, so 2 wounds)
		scn.AddWoundsToChar(knight1, 2);
		assertEquals(1, scn.GetVitality(knight1));

		scn.SkipToPhase(Phase.MANEUVER);

		// Cannot use - knight1 cannot exert, only 1 valid knight
		assertFalse(scn.FreepsActionAvailable(gate));
	}

	@Test
	public void InnerGateTwoDotUniquenessAllowsTwoCopiesButNotThree() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var gate1 = scn.GetFreepsCard("gate");
		var gate2 = scn.GetFreepsCard("gate2");
		var gate3 = scn.GetFreepsCard("gate3");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(gate1, gate2, gate3);

		scn.StartGame();

		// First copy can be played
		assertTrue(scn.FreepsPlayAvailable(gate1));
		scn.FreepsPlayCard(gate1);
		assertInZone(Zone.SUPPORT, gate1);

		// Second copy can be played (2-dot uniqueness)
		assertTrue(scn.FreepsPlayAvailable(gate2));
		scn.FreepsPlayCard(gate2);
		assertInZone(Zone.SUPPORT, gate2);

		// Third copy CANNOT be played (exceeds 2-dot limit)
		assertFalse(scn.FreepsPlayAvailable(gate3));
		assertInZone(Zone.HAND, gate3);
	}
}
