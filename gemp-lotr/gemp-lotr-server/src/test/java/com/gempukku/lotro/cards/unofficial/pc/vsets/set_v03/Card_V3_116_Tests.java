package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("smeagol", "103_116");
					put("sam", "1_311");
					put("aragorn", "1_89");

					put("raider", "1_158");  // Cost 4
					put("hunter", "1_256");  // Cost 5
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SmeagolStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Smeagol, Once Like Us
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: 
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Ring-bound. To play, add a burden.
		* 	Assignment: If you cannot spot a companion with a culture other than [shire], exert Smeagol and hinder him to spot a minion.  Add (2) (and add 2 threats if that minion is twilight cost 5 or more) to hinder that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("smeagol");

		assertEquals("Smeagol", card.getBlueprint().getTitle());
		assertEquals("Once Like Us", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet()); 
	}


// ======== EXTRA COST TO PLAY TEST ========

	@Test
	public void SmeagolAddsABurdenToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());

		scn.FreepsPlayCard(smeagol);

		assertEquals(1, scn.GetBurdens());
	}

// ======== REQUIREMENT TESTS ========

	@Test
	public void SmeagolAbilityNotAvailableWithNonShireCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hunter = scn.GetShadowCard("hunter");

		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveMinionsToTable(hunter);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Aragorn is Gondor, not Shire - ability should not be available
		assertFalse(scn.FreepsActionAvailable(smeagol));
	}

	@Test
	public void SmeagolAbilityAvailableWithOnlyShireCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var hunter = scn.GetShadowCard("hunter");

		// Frodo (RB) + Smeagol + Sam = all Shire
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveMinionsToTable(hunter);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.FreepsActionAvailable(smeagol));
	}

// ======== BASIC EXECUTION TEST ========

	@Test
	public void SmeagolAbilityExertsAndHindersSelfAddsTwilightAndHindersMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var raider = scn.GetShadowCard("raider");  // Cost 4, below threshold

		scn.MoveCompanionsToTable(smeagol);
		scn.MoveMinionsToTable(raider);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		int twilightBefore = scn.GetTwilight();

		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertFalse(scn.IsHindered(smeagol));
		assertFalse(scn.IsHindered(raider));
		assertEquals(0, scn.GetThreats());

		scn.FreepsUseCardAction(smeagol);
		// Single minion, auto-selected

		// Costs paid: Smeagol exerted and hindered
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.IsHindered(smeagol));

		// Effect: twilight added, minion hindered, no threats (cost 4 < 5)
		assertEquals(twilightBefore + 2, scn.GetTwilight());
		assertTrue(scn.IsHindered(raider));
		assertEquals(0, scn.GetThreats());
	}

// ======== THREAT THRESHOLD TESTS ========

	@Test
	public void SmeagolAbilityAdds2ThreatsForMinionCosting5OrMore() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var hunter = scn.GetShadowCard("hunter");  // Cost 5, at threshold

		// Frodo + Smeagol = 2 companions = 2 threat limit
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveMinionsToTable(hunter);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetThreats());

		scn.FreepsUseCardAction(smeagol);

		// With cost 5 minion, should add 2 threats
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsHindered(hunter));
	}

	@Test
	public void SmeagolAbilityFailsOn5CostMinionIfOnlyRoomFor1Threat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var hunter = scn.GetShadowCard("hunter");  // Cost 5

		// Frodo + Smeagol = 2 companions = 2 threat limit
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveMinionsToTable(hunter);
		scn.AddThreats(1);  // Now at 1/2 threats

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(1, scn.GetThreats());

		scn.FreepsUseCardAction(smeagol);

		// With cost 5 minion, couldn't add 2 threats and so Smeagol got hindered but the Hunter did not
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.IsHindered(smeagol));
		assertFalse(scn.IsHindered(hunter));
	}

	@Test
	public void SmeagolAbilityFailsToHinderMinionOn5CostMinionIfAtThreatCap() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var hunter = scn.GetShadowCard("hunter");  // Cost 5

		// Frodo + Smeagol = 2 companions = 2 threat limit
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveMinionsToTable(hunter);
		scn.AddThreats(2);  // At max threats

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetThreats());

		scn.FreepsUseCardAction(smeagol);

		// With cost 5 minion, couldn't add 2 threats and so Smeagol got hindered but the Hunter did not
		assertTrue(scn.IsHindered(smeagol));
		assertFalse(scn.IsHindered(hunter));
	}

	@Test
	public void SmeagolAbilityWorksOn4CostMinionEvenAtThreatCap() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var raider = scn.GetShadowCard("raider");  // Cost 4, below threshold

		scn.MoveCompanionsToTable(smeagol);
		scn.MoveMinionsToTable(raider);
		scn.AddThreats(2);  // At max threats

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(2, scn.GetThreats());

		// Ability should work - no threats needed for cost 4 minion
		assertTrue(scn.FreepsActionAvailable(smeagol));

		scn.FreepsUseCardAction(smeagol);

		// Threats unchanged (no threat cost for <5)
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsHindered(raider));
	}

	@Test
	public void SmeagolAbilityOffersChoiceWhenMultipleMinions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var raider = scn.GetShadowCard("raider");   // Cost 4
		var hunter = scn.GetShadowCard("hunter");   // Cost 5

		scn.MoveCompanionsToTable(smeagol);
		scn.MoveMinionsToTable(raider, hunter);

		scn.StartGame();
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetThreats());

		scn.FreepsUseCardAction(smeagol);

		// Should be asked to choose which minion to target
		assertTrue(scn.FreepsHasCardChoicesAvailable(raider, hunter));

		// Choose the hunter (costs 5)
		scn.FreepsChooseCard(hunter);

		assertEquals(2, scn.GetThreats());
		assertTrue(scn.IsHindered(hunter));
		assertFalse(scn.IsHindered(raider));
	}
}
