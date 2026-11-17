package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "15_76");
					// Two [men] Men of different titles, cost 2 each, site 4
					put("bree", "11_90");       // Man of Bree: strength +1 for each card in the Free Peoples hand
					put("rampager", "11_98");   // Rampaging Easterling: lurker; +3 each time another [men] minion wins a skirmish
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard); his texts want a [men] possession
					put("saruman", "17_48");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					// Two Elves for House of Elrond's move trigger
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf, strength 6; +3 against a Nazgul
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: [elven] Elf, strength 5; +2 at sites 6 to 8
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					// House of Elrond: "When the fellowship moves to House of Elrond, the Free Peoples player may spot 2
					// Elves to remove a burden"; site 2 stays Ettenmoors 1_331, "Skirmish: Exert your companion or minion
					// to make that character strength +2"
					put("site3", "3_119");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRevealScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "15_76");
					put("scout1", "15_77");     // Easterling Scout: a [men] Man; its ability is a skirmish one
					put("scout2", "15_77");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DestroyedHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Destroyed Homestead
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, spot 2 [men] Men to randomly reveal a site from the Free Peoples player's adventure deck. Note its card title. Each site on the adventure path with the same card title as the selected site loses its game text.
		*/

		var scn = GetScenario();

		var homestead = scn.GetShadowCard("homestead");

		assertEquals("Destroyed Homestead", homestead.getBlueprint().getTitle());
		assertNull(homestead.getBlueprint().getSubtitle());
		assertFalse(homestead.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, homestead.getBlueprint().getSide());
		assertEquals(Culture.MEN, homestead.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, homestead.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(homestead, Keyword.SUPPORT_AREA));
		assertEquals(3, homestead.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedWithTwoMenMenToSpotOneSiteOfTheFreePeoplesAdventureDeckIsRevealedToBothPlayers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRevealScenario();

		var homestead = scn.GetShadowCard("homestead");
		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		scn.MoveCardsToHand(homestead);

		scn.StartGame();
		scn.MoveMinionsToTable(scout1, scout2);
		// 7, and for the move 1 companion and the 2 of Ettenmoors
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(homestead));

		scn.ShadowPlayCard(homestead);
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, homestead);

		// Each player is shown one card.  Which site is chance: it is one of the eight still in the Free
		// Peoples player's adventure deck, sites 2 to 9, and not his site 1, which is on the adventure path.
		// A card shown is read by its blueprint, and the Shadow player's adventure deck holds the same
		// sites but for his site 2, which is on the path, and his site 1, which is not: a site revealed
		// from the wrong deck would be told from this only when it is site 1, one time in eight.
		assertEquals(1, scn.FreepsGetTotalRevealedCardCount());
		assertEquals(1, scn.ShadowGetTotalRevealedCardCount());
		int shownToFreeps = 0;
		int shownToShadow = 0;
		for (int number = 2; number <= 9; number++) {
			var site = scn.GetFreepsSite(number);
			assertInZone(Zone.ADVENTURE_DECK, site);
			if (scn.FreepsHasCardChoiceAvailable(site, false))
				shownToFreeps++;
			if (scn.ShadowHasCardChoiceAvailable(site, false))
				shownToShadow++;
		}
		assertEquals(1, shownToFreeps);
		assertEquals(1, shownToShadow);
		assertFalse(scn.FreepsHasCardChoiceAvailable(scn.GetSite(1), false));
		assertFalse(scn.ShadowHasCardChoiceAvailable(scn.GetSite(1), false));

		scn.FreepsDismissRevealedCards();
		scn.ShadowDismissRevealedCards();
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSiteOnThePathWithTheRevealedTitleLosesItsGameTextAndASiteOfAnotherTitleKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetShadowCard("homestead");
		var bree = scn.GetShadowCard("bree");
		var rampager = scn.GetShadowCard("rampager");
		var saruman = scn.GetShadowCard("saruman");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var freepsEttenmoors = scn.GetFreepsSite("site2");
		var house = scn.GetShadowSite("site3");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.MoveCardsToHand(homestead);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.SetTwilight(10);
		scn.FreepsPass();
		var ettenmoors = scn.GetCurrentSite();
		// The Free Peoples player's adventure deck holds his Ettenmoors alone: the Shadow player's is on the path.
		scn.SetFreepsAdventureDeck(freepsEttenmoors);
		// Saruman, vitality 4, can pay Ettenmoors' exertion; the two Men have 1 vitality each.
		scn.MoveMinionsToTable(bree, rampager, saruman);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		assertTrue(scn.ShadowHasCardChoiceAvailable(freepsEttenmoors, false));
		scn.DismissRevealedCards();
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, bree);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);
		// Ettenmoors on the path has the title revealed: neither player is offered its skirmish ability.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ettenmoors));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ettenmoors));
		scn.ShadowPass();

		// Arwen (6) beats Man of Bree (4); on to the regroup move to House of Elrond, another title.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertSame(house, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(house));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASiteOfTheRevealedTitleLaidLaterLosesItsGameTextWhileOneOfAnotherTitleKeepsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetShadowCard("homestead");
		var bree = scn.GetShadowCard("bree");
		var rampager = scn.GetShadowCard("rampager");
		var saruman = scn.GetShadowCard("saruman");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var freepsHouse = scn.GetFreepsSite("site3");
		var house = scn.GetShadowSite("site3");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.MoveCardsToHand(homestead);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.SetTwilight(10);
		scn.FreepsPass();
		var ettenmoors = scn.GetCurrentSite();
		scn.SetFreepsAdventureDeck(freepsHouse);
		// Saruman, vitality 4, can pay Ettenmoors' exertion; the two Men have 1 vitality each.
		scn.MoveMinionsToTable(bree, rampager, saruman);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(homestead);
		assertTrue(scn.ShadowHasCardChoiceAvailable(freepsHouse, false));
		scn.DismissRevealedCards();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, bree);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);
		// Ettenmoors is of another title and keeps its text.
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(ettenmoors));
		scn.ShadowPass();

		// The Shadow player's House of Elrond, laid by the move after the play, has the title revealed.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertSame(house, scn.GetCurrentSite());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneMenManToSpotNothingIsRevealedAndNoSiteLosesItsText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetShadowCard("homestead");
		var bree = scn.GetShadowCard("bree");
		var saruman = scn.GetShadowCard("saruman");
		var brigand = scn.GetShadowCard("brigand");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var freepsEttenmoors = scn.GetFreepsSite("site2");
		scn.MoveCompanionsToTable(arwen, haldir);
		scn.MoveCardsToHand(homestead);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		var ettenmoors = scn.GetCurrentSite();
		scn.SetFreepsAdventureDeck(freepsEttenmoors);
		// One [men] Man, beside a [men] minion who is no Man and a Man of another culture.
		scn.MoveMinionsToTable(bree, saruman, brigand);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(homestead);
		assertInZone(Zone.SUPPORT, homestead);
		assertFalse(scn.BeingShownCards(scn.P1()));
		assertFalse(scn.BeingShownCards(scn.P2()));
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, bree);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
	}
}
