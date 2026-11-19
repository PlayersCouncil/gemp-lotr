package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_066_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faramir", "13_66");
					put("knight1", "5_35");     // Gondorian Knight: [gondor] Man companion of cost 2, Knight and no other text
					put("knight2", "5_35");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability only
					put("denethor", "13_64");   // Denethor, Last Ruling Steward: a resistance modifier and a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil: [gondor] ARTIFACT hand weapon on a [gondor] Man; a response to a wound
					put("brother", "4_111");    // Boromir, My Brother: [gondor] CONDITION, "Bearer must be Faramir"; a skirmish ability only
					put("chain", "4_278");      // Heavy Chain: [rohan] possession (armor), "Bearer must be a Man"; an overwhelm bar
					put("shield", "1_107");     // Great Shield: [gondor] possession (shield), "Bearer must be a Man"; archery total -1
					put("sword", "7_98");       // Gondorian Sword: [gondor] possession (hand weapon) on a [gondor] Man; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FaramirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Faramir, Prince of Ithilien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: While you can spot Boromir, Faramir is twilight cost -1.<br>While you can spot Denethor, Faramir is twilight cost -1.<br>While Faramir bears a [gondor] possession, he is <b>defender +1</b>.
		*/

		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");

		assertEquals("Faramir", faramir.getBlueprint().getTitle());
		assertEquals("Prince of Ithilien", faramir.getBlueprint().getSubtitle());
		assertTrue(faramir.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, faramir.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, faramir.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, faramir.getBlueprint().getCardType());
		assertEquals(Race.MAN, faramir.getBlueprint().getRace());
		assertEquals(3, faramir.getBlueprint().getTwilightCost());
		assertEquals(7, faramir.getBlueprint().getStrength());
		assertEquals(3, faramir.getBlueprint().getVitality());
		assertEquals(7, faramir.getBlueprint().getResistance());
	}

	@Test
	public void WhileBoromirCanBeSpottedFaramirIsTwilightCostMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var knight1 = scn.GetFreepsCard("knight1");
		var knight2 = scn.GetFreepsCard("knight2");
		var boromir = scn.GetFreepsCard("boromir");
		// A [gondor] Man companion who is no Boromir, in play.
		scn.MoveCompanionsToTable(knight1);
		scn.MoveCardsToHand(faramir, knight2, boromir);

		scn.StartGame();
		assertEquals(3, scn.GetTwilightCost(faramir));
		assertEquals(2, scn.GetTwilightCost(knight2));

		scn.FreepsPlayCard(boromir);
		assertInPlay(boromir);
		assertEquals(2, scn.GetTwilightCost(faramir));
		// Another companion in hand keeps its cost.
		assertEquals(2, scn.GetTwilightCost(knight2));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileDenethorCanBeSpottedFaramirIsTwilightCostMinusOneAndWithBoromirTooMinusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var knight2 = scn.GetFreepsCard("knight2");
		var boromir = scn.GetFreepsCard("boromir");
		var denethor = scn.GetFreepsCard("denethor");
		scn.MoveCompanionsToTable(denethor);
		scn.MoveCardsToHand(faramir, knight2);

		scn.StartGame();
		assertEquals(2, scn.GetTwilightCost(faramir));
		assertEquals(2, scn.GetTwilightCost(knight2));

		scn.MoveCompanionsToTable(boromir);
		assertEquals(1, scn.GetTwilightCost(faramir));
		assertEquals(2, scn.GetTwilightCost(knight2));

		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(faramir);
		// 3 printed - 1 for Boromir - 1 for Denethor
		assertInPlay(faramir);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileFaramirBearsAGondorPossessionHeIsDefenderPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var knight1 = scn.GetFreepsCard("knight1");
		var narsil = scn.GetFreepsCard("narsil");
		var brother = scn.GetFreepsCard("brother");
		var chain = scn.GetFreepsCard("chain");
		var sword = scn.GetFreepsCard("sword");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(faramir, knight1);
		// A [gondor] artifact, a [gondor] condition and a [rohan] possession on Faramir; a [gondor] possession on
		// another companion.
		scn.AttachCardsTo(faramir, narsil, brother, chain);
		scn.AttachCardsTo(knight1, sword);
		scn.MoveCardsToHand(shield);

		scn.StartGame();
		assertAttachedTo(narsil, faramir);
		assertAttachedTo(brother, faramir);
		assertAttachedTo(chain, faramir);
		assertAttachedTo(sword, knight1);
		assertEquals(0, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(knight1, Keyword.DEFENDER));

		scn.FreepsPlayCard(shield);
		scn.FreepsChooseCard(faramir);
		assertAttachedTo(shield, faramir);
		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(knight1, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGondorHandWeaponPlayedOnFaramirMakesHimDefenderPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var faramir = scn.GetFreepsCard("faramir");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(faramir);
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		assertEquals(0, scn.GetKeywordCount(faramir, Keyword.DEFENDER));

		// Faramir is the only [gondor] Man, so the Sword goes on him unasked.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, faramir);
		assertEquals(1, scn.GetKeywordCount(faramir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
