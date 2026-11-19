package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_316_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("talent", "1_316");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion; strength +2 with a weapon
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; enduring, a skirmish ability never used
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, no companion; a skirmish ability never used
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit; a maneuver ability never used

					put("scout", "17_84");      // Orkish Scout: Shadow: exert to add (X), X the current site's Shadow number
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ATalentforNotBeingSeenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: A Talent for Not Being Seen
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Stealth</b>. To play, exert a Hobbit. Plays to your support area.
		 * 		Each time the fellowship moves, spot 2 Hobbit companions to make the Shadow number -1 (or spot 4 to make it -2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("talent");

		assertEquals("A Talent for Not Being Seen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ToPlayAHobbitExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var talent = scn.GetFreepsCard("talent");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(talent);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.FreepsPlayAvailable(talent));

		scn.FreepsPlayCard(talent);

		// A Hobbit of either culture, companion or ally, may exert; the companions who are no Hobbits may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInZone(Zone.SUPPORT, talent);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithEveryHobbitExhaustedItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var talent = scn.GetFreepsCard("talent");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(pippin, aragorn, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(talent);

		scn.StartGame();
		// Every Hobbit at one vitality; Aragorn and Smeagol, who are no Hobbits, could still exert.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(pippin, 3);
		scn.AddWoundsToChar(bounder, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetVitality(frodo));
		assertEquals(1, scn.GetVitality(pippin));
		assertEquals(1, scn.GetVitality(bounder));

		assertFalse(scn.FreepsPlayAvailable(talent));
		assertInZone(Zone.HAND, talent);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoHobbitCompanionsTheMoveIsMadeAtAShadowNumberOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var talent = scn.GetFreepsCard("talent");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo and a [gondor] Hobbit are the two Hobbit companions.
		scn.MoveCompanionsToTable(pippin, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(talent);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		// To Ettenmoors, Shadow number 2.
		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 companions + (2 - 1) for the site
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneHobbitCompanionTheShadowNumberIsUnchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var talent = scn.GetFreepsCard("talent");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		// Frodo is the only Hobbit companion: a Hobbit ally and two companions who are no Hobbits beside him.
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(talent);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 3 companions + 2 for the site
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourHobbitCompanionsTheMoveIsMadeAtAShadowNumberTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var talent = scn.GetFreepsCard("talent");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(merry, pippin, sam);
		scn.MoveCardsToSupportArea(talent);

		scn.StartGame();
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		int pool = scn.GetTwilight();

		// To Balin's Tomb, Shadow number 3: a site that is not lowered to 0 by 2, so that 2 less is told from 3 less.
		scn.FreepsPass();

		assertEquals(4, scn.GetCurrentSiteNumber());
		// 4 companions + (3 - 2) for the site
		assertEquals(pool + 5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeHobbitCompanionsTheShadowNumberIsOnlyOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var talent = scn.GetFreepsCard("talent");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		// Three Hobbit companions, with a Hobbit ally and a companion who is no Hobbit to make five of neither kind.
		scn.MoveCompanionsToTable(merry, pippin, smeagol);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(talent);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		// 4 companions + (2 - 1) for the site
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveOfATurnIsMadeAtAShadowNumberOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var talent = scn.GetFreepsCard("talent");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(talent);

		scn.StartGame();
		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		int pool = scn.GetTwilight();

		// To The Bridge of Khazad-dum, Shadow number 6 (no twilight for a region on an ordered path).
		scn.FreepsPass();
		assertEquals(5, scn.GetCurrentSiteNumber());
		// 2 companions + (6 - 1) for the site
		assertEquals(pool + 7, scn.GetTwilight());
		pool = scn.GetTwilight();

		// To Galadriel's Glade, Shadow number 3, in the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(6, scn.GetCurrentSiteNumber());
		// 2 companions + (3 - 1) for the site
		assertEquals(pool + 4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void InTheShadowPhaseAfterTheMoveTheSiteIsAtItsPrintedShadowNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var talent = scn.GetFreepsCard("talent");
		var pippin = scn.GetFreepsCard("pippin");
		var scout = scn.GetShadowCard("scout");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(talent);

		scn.StartGame();
		scn.MoveMinionsToTable(scout);
		assertEquals(0, scn.GetTwilight());

		// To Ettenmoors, Shadow number 2.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 2 companions + (2 - 1) for the site
		assertEquals(3, scn.GetTwilight());

		// The -1 was for the move, which ended with the fellowship phase (Q-0018).
		assertEquals(2, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertTrue(scn.ShadowActionAvailable(scout));
		scn.ShadowUseCardAction(scout);
		// 3 + 2, the Scout's X being the current site's Shadow number
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
