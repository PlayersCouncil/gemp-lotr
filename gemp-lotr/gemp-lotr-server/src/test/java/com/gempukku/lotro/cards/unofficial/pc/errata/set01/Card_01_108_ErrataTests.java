package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_108_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nostranger", "51_108");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger, a maneuver ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger; her text needs a Nazgul
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man and no ranger, a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoStrangerToTheShadowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: No Stranger to the Shadows
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Game Text: Stealth. Bearer must be a ranger.
		 * Each site's Shadow number is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nostranger");

		assertEquals("No Stranger to the Shadows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysForZeroOnARanger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nostranger = scn.GetFreepsCard("nostranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, arwen, boromir);
		scn.MoveCardsToHand(nostranger);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(nostranger));
		scn.FreepsPlayCard(nostranger);

		// Rangers of two cultures and races; Boromir is a [gondor] Man with no ranger.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertAttachedTo(nostranger, arwen);
		// It costs 0.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowNumberIsOneLessOnceItIsPlayedButNotBelowZero() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nostranger = scn.GetFreepsCard("nostranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(nostranger);
		scn.FreepsRefreshDecision();

		// Ettenmoors, Shadow number 2.
		var ettenmoors = scn.GetSite(2);
		assertEquals(2, scn.GetTwilightCost(ettenmoors));

		scn.FreepsPlayCard(nostranger);
		assertAttachedTo(nostranger, aragorn);
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
	public void EverySiteOnThePathIsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nostranger = scn.GetFreepsCard("nostranger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, nostranger);

		scn.StartGame();
		scn.SkipToSite(3);

		// Ettenmoors (2), behind the fellowship, is reduced as well as the site it is at.
		assertEquals(1, scn.GetTwilightCost(scn.GetSite(2)));
		assertEquals(0, scn.GetTwilightCost(scn.GetSite(3)));

		scn.SetTwilight(0);
		scn.FreepsPass();

		// Balin's Tomb, Shadow number 3.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetTwilightCost(scn.GetSite(4)));
		assertEquals(1, scn.GetTwilightCost(scn.GetSite(2)));
		// 2 companions + 3 for the site - 1
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
