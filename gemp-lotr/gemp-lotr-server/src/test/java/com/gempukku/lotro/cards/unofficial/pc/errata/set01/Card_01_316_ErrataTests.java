package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_316_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("talent", "51_316");
					put("talent2", "51_316");
					put("talent3", "51_316");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength +2 while he bears a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: his text protects [shire] tales
					// Bounder: [shire] Hobbit ally; a skirmish ability that is never used here
					put("bounder", "1_286");
					// Hobbit Sword: a Hobbit's hand weapon, no text beyond its strength bonus
					put("sword", "1_299");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: a [shire] Hobbit companion
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetOtherCulturesScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("talent", "51_316");
					put("talent2", "51_316");
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion; his text answers another [rohan] Man exerting
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; his skirmish ability is only used on purpose
					// Bounder: [shire] Hobbit ally; a skirmish ability that is never used here
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,     // Frodo, Son of Drogo: a [shire] Hobbit companion
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TalentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: A Talent for Not Being Seen
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Game Text: Stealth. Bearer must be Merry or Pippin. Limit 1 per character.
		 * Each site's Shadow number is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("talent");

		assertEquals("A Talent for Not Being Seen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertFalse(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
	}

	@Test
	public void PlaysForNothingOnMerryOrPippinAndNoOtherHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var talent = scn.GetFreepsCard("talent");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(talent);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(talent));
		scn.FreepsPlayCard(talent);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, bounder));
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(talent, pippin);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowNumberIsOneLessOnceItIsPlayedButNotBelowZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var talent = scn.GetFreepsCard("talent");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(talent);
		scn.FreepsRefreshDecision();

		// Ettenmoors, Shadow number 2.
		var ettenmoors = scn.GetSite(2);
		assertEquals(2, scn.GetTwilightCost(ettenmoors));

		scn.FreepsPlayCard(talent);
		assertAttachedTo(talent, merry);
		assertEquals(1, scn.GetTwilightCost(ettenmoors));

		scn.SetTwilight(0);
		scn.FreepsPass();

		// Rivendell Valley's Shadow number is 0; 0 - 1 is counted as 0 (CR 4.2 "modifier").
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(3)));
		// 2 companions + 0 for the site
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnePerCharacterAndTwoCopiesMakeEverySiteTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var talent = scn.GetFreepsCard("talent");
		var talent2 = scn.GetFreepsCard("talent2");
		var talent3 = scn.GetFreepsCard("talent3");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.AttachCardsTo(merry, talent);
		// Pippin bears another card already, which is no bar to this one.
		scn.AttachCardsTo(pippin, sword);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToHand(talent2, talent3);
		scn.FreepsRefreshDecision();

		// Ettenmoors (2), behind the fellowship, is reduced by the copy Merry bears.
		var ettenmoors = scn.GetSite(2);
		assertEquals(1, scn.GetTwilightCost(ettenmoors));

		// Merry bears one already, so Pippin (who bears a sword) is the only bearer and nobody is asked.
		assertTrue(scn.FreepsPlayAvailable(talent2));
		scn.FreepsPlayCard(talent2);
		assertAttachedTo(talent2, pippin);
		assertNotAttachedTo(talent2, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// 2 - 2
		assertEquals(0, scn.GetTwilightCost(ettenmoors));
		// Merry and Pippin each bear one: there is no bearer for a third.
		assertFalse(scn.FreepsPlayAvailable(talent3));

		scn.SetTwilight(0);
		scn.FreepsPass();

		// Balin's Tomb, Shadow number 3.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilightCost(scn.GetSite(4)));
		// 3 companions + 3 for the site - 2
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysOnTheRohanMerryAndTheGondorPippinAsOnTheShireOnes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetOtherCulturesScenario();

		var frodo = scn.GetRingBearer();
		var talent = scn.GetFreepsCard("talent");
		var talent2 = scn.GetFreepsCard("talent2");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(talent, talent2);

		scn.StartGame();

		// Neither is a [shire] card; the title is all the text names.
		assertEquals(Culture.ROHAN, merry.getBlueprint().getCulture());
		assertEquals(Culture.GONDOR, pippin.getBlueprint().getCulture());

		assertTrue(scn.FreepsPlayAvailable(talent));
		scn.FreepsPlayCard(talent);
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, bounder));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(talent, merry);

		// Merry bears one already, so the [gondor] Pippin is the only bearer left and nobody is asked.
		assertTrue(scn.FreepsPlayAvailable(talent2));
		scn.FreepsPlayCard(talent2);
		assertAttachedTo(talent2, pippin);
		assertNotAttachedTo(talent2, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
