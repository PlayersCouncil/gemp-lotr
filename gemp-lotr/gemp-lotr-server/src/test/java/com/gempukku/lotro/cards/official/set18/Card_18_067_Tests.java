package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_067_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "18_67");

					put("stragglers", "13_99"); // Stragglers: [men] support-area condition that carries [men] tokens; skirmish ability used only on purpose
					put("howdah", "13_94");     // Howdah: [men] support-area condition that carries [men] tokens; its archery text is not reached
					put("ships", "8_65");       // Ships of Great Draught: [raider] support-area possession that carries [raider] tokens; abilities used only on purpose

					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] Dwarf companion; skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound [gondor] Man companion; skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound companion, vitality 4, exhausted here
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("bounder", "1_286");    // Bounder: a Hobbit ALLY (only companions are unbound)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gríma, Witless Worm
		 * Unique: True
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Regroup:</b> Exert Gríma to add (2).
		 * 	<b>Regroup:</b> Remove a [men] token to add (2).
		 * 	<b>Regroup:</b> Remove (4) to exert an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grima");

		assertEquals("Gríma", card.getBlueprint().getTitle());
		assertEquals("Witless Worm", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void FirstRegroupAbilityExertsGrimaToAddTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(grima));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(grima, "Exert Gríma"));

		scn.ShadowUseCardAction(grima, "Exert Gríma");

		assertEquals(1, scn.GetWoundsOn(grima));
		// 1 + 2
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithGrimaExhaustedAndTheOtherCostsUnpayableNoAbilityIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var ships = scn.GetShadowCard("ships");

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.AddWoundsToChar(grima, 2);
		scn.MoveCardsToSupportArea(ships);
		scn.AddTokensToCard(ships, 2);
		assertEquals(2, scn.GetWoundsOn(grima));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		// Vitality 3 with 2 wounds: exhausted. No [men] token, and (3) in the pool.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(grima));
		assertFalse(scn.ShadowActionAvailable(grima, "Exert Gríma"));
	}

	@Test
	public void SecondRegroupAbilityRemovesAMenTokenFromAnyCardToAddTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var stragglers = scn.GetShadowCard("stragglers");
		var howdah = scn.GetShadowCard("howdah");
		var ships = scn.GetShadowCard("ships");

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.MoveCardsToSupportArea(stragglers, howdah, ships);
		scn.AddTokensToCard(stragglers, 1);
		scn.AddTokensToCard(howdah, 1);
		scn.AddTokensToCard(ships, 2);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(howdah));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(grima, "token to add"));
		scn.ShadowUseCardAction(grima, "token to add");

		// A [men] token on either [men] card; not the [raider] card's tokens.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stragglers, howdah));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ships));
		scn.ShadowChooseCard(stragglers);

		assertEquals(0, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(howdah));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		// 1 + 2
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(grima));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SecondRegroupAbilityIsNotOfferedWithNoMenTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var stragglers = scn.GetShadowCard("stragglers");
		var ships = scn.GetShadowCard("ships");

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.MoveCardsToSupportArea(stragglers, ships);
		scn.AddTokensToCard(ships, 2);
		assertEquals(0, scn.GetCultureTokensOn(stragglers));
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grima, "token to add"));

		// A [men] token on Stragglers, and the decision sent again: offered.
		scn.AddTokensToCard(stragglers, 1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(grima, "token to add"));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ThirdRegroupAbilityRemovesFourToExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(gimli, boromir, aragorn, sam);
		scn.MoveAlliesToTable(bounder);
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowActionAvailable(grima, "unbound companion"));
		scn.ShadowUseCardAction(grima, "unbound companion");

		// 5 - 4
		assertEquals(1, scn.GetTwilight());
		// Unbound companions of two cultures and races; not the Ring-bound Frodo and Sam, not an ally, not an
		// unbound companion who is exhausted.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThirdRegroupAbilityIsNotOfferedWithLessThanFourInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var gimli = scn.GetFreepsCard("gimli");

		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(grima, "unbound companion"));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(grima, "unbound companion"));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoUnboundCompanionAbleToExertTheThirdAbilityIsUsedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(grima);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowActionAvailable(grima, "unbound companion"));
		scn.ShadowUseCardAction(grima, "unbound companion");

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
