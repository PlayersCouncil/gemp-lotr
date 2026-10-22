package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_08_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gunners", "8_55");

					put("numenorean", "8_49");  // Black Numenorean: corsair printed fierce; a skirmish ability used only on purpose
					put("boatswain", "10_37");  // Corsair Boatswain: corsair; a Shadow ability used only on purpose
					put("southron", "4_254");   // Southron Soldier: [raider] Man, no corsair; Southron and ambush only
					put("champion", "13_84");   // Corsair Champion: [men] Man with "Corsair" in the title and not the keyword;
					                            // fierce only while every companion is wounded

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, to skirmish the Gunners

					// Cards that carry culture tokens; each is given tokens by its own text.
					put("ships", "8_65");       // Ships of Great Draught: [raider] possession in the support area
					put("raiders", "4_242");    // Raiders From the East: [raider] condition in the support area
					put("galley", "8_59");      // Corsair War Galley: [raider] possession in the support area; initiative only at 6 [raider] tokens
					put("rank", "10_96");       // Rank and File: [sauron] condition in the support area, with [sauron] tokens
					put("leaders", "7_112");    // Noble Leaders: the Free Peoples player's [gondor] condition, with [gondor] tokens

					// Free Peoples cards for the hand: four keep the initiative, three give it to the Shadow.
					put("stairs1", "1_24");     // Stairs of Khazad-dum: [dwarven] condition, no text that reaches these boards
					put("stairs2", "1_24");
					put("stairs3", "1_24");
					put("stairs4", "1_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairGunnersStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Gunners
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Corsair</b>.<br><b>Skirmish:</b> If you have initiative, remove a [raider] token to make a corsair <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gunners");

		assertEquals("Corsair Gunners", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.CORSAIR));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityRemovesARaiderTokenToMakeACorsairFierceUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gunners = scn.GetShadowCard("gunners");
		var numenorean = scn.GetShadowCard("numenorean");
		var boatswain = scn.GetShadowCard("boatswain");
		var southron = scn.GetShadowCard("southron");
		var champion = scn.GetShadowCard("champion");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ships = scn.GetShadowCard("ships");
		var raiders = scn.GetShadowCard("raiders");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		var leaders = scn.GetFreepsCard("leaders");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ships, raiders, galley, rank, leaders);

		scn.StartGame();
		scn.MoveCardsToHand(scn.GetFreepsCard("stairs1"), scn.GetFreepsCard("stairs2"), scn.GetFreepsCard("stairs3"));
		scn.AddTokensToCard(ships, 2);
		scn.AddTokensToCard(raiders, 1);
		scn.AddTokensToCard(rank, 2);
		scn.AddTokensToCard(leaders, 2);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(leaders));
		scn.MoveMinionsToTable(gunners, numenorean, boatswain, southron, champion);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gunners);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Three cards in the Free Peoples hand: the Shadow has initiative.
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.HasKeyword(boatswain, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(gunners));
		scn.ShadowUseCardAction(gunners);

		// A [raider] token, from any card that has one: not the [raider] card with none, nor the cards whose
		// tokens are of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, raiders));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(galley, rank, leaders));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ships);
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(1, scn.GetCultureTokensOn(raiders));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		assertEquals(2, scn.GetCultureTokensOn(leaders));

		// A corsair: the Gunners in the skirmish, one assigned to nobody, and one printed fierce already; not the
		// [raider] Man who is none, nor the Man with the word in his title only.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gunners, boatswain, numenorean));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(southron, champion));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(boatswain);

		assertTrue(scn.HasKeyword(boatswain, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(gunners, Keyword.FIERCE));

		// Still fierce once the skirmish is over: the fierce round.
		scn.PassSkirmishActions();
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(boatswain, Keyword.FIERCE));

		// Gone in the regroup phase; the printed keyword stays.
		scn.PassFierceAssignmentActions();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(boatswain, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(numenorean, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileTheFreePeoplesPlayerHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gunners = scn.GetShadowCard("gunners");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ships);

		scn.StartGame();
		scn.MoveCardsToHand(scn.GetFreepsCard("stairs1"), scn.GetFreepsCard("stairs2"),
				scn.GetFreepsCard("stairs3"), scn.GetFreepsCard("stairs4"));
		scn.AddTokensToCard(ships, 2);
		assertEquals(2, scn.GetCultureTokensOn(ships));
		scn.MoveMinionsToTable(gunners);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gunners);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Four cards in the Free Peoples hand: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gunners));
		assertEquals(2, scn.GetCultureTokensOn(ships));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoRaiderTokenToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gunners = scn.GetShadowCard("gunners");
		var aragorn = scn.GetFreepsCard("aragorn");
		var galley = scn.GetShadowCard("galley");
		var rank = scn.GetShadowCard("rank");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(galley, rank);

		scn.StartGame();
		scn.MoveCardsToHand(scn.GetFreepsCard("stairs1"), scn.GetFreepsCard("stairs2"), scn.GetFreepsCard("stairs3"));
		scn.AddTokensToCard(rank, 2);
		assertEquals(0, scn.GetCultureTokensOn(galley));
		assertEquals(2, scn.GetCultureTokensOn(rank));
		scn.MoveMinionsToTable(gunners);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, gunners);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gunners));
		assertEquals(2, scn.GetCultureTokensOn(rank));
	}
}
