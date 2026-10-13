package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_356_Tests
{

	// Anduin Banks is site 8 of the Fellowship sites.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; his maneuver ability is not used
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, an archer (fellowship archery total 1)
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, cost 3; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, cost 4; his text is his own strength
					put("knight", "5_35");      // Gondorian Knight: a companion; Knight is his whole text
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, who is no companion; a fellowship ability only
					put("marksman", "1_176");   // Goblin Marksman: an archer minion (minion archery total 1); Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnduinBanksStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Anduin Banks
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 8
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>River</b>. For each companion in the fellowship over 4, add 2 to the minion archery total.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Anduin Banks", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void WithFiveCompanionsTheMinionArcheryTotalIsTwoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var grimir = scn.GetFreepsCard("grimir");
		var frodo = scn.GetRingBearer();

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, gandalf);
		scn.MoveAlliesToTable(grimir);
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());

		// Five companions with Frodo, one over 4.  An ally and a minion are no companions.
		// 1 for the Marksman + 2
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// The Free Peoples player places three wounds.
		scn.PassArcheryActions();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AddsTwoForEachCompanionOverFourAsTheyArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var knight = scn.GetFreepsCard("knight");
		var grimir = scn.GetFreepsCard("grimir");

		scn.StartGame();
		scn.SkipToSite(8);
		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveCompanionsToTable(aragorn, gimli, legolas);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToDeadPile(knight);
		scn.MoveCardsToHand(boromir, gandalf);

		// Four companions with Frodo, an ally beside them and a fifth companion in the dead pile: nothing is added.
		// Legolas is the fellowship's archer, and that total is not touched.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(boromir);
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(gandalf);
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasMovedOnFromTheBanksNothingIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, gandalf);

		// Five companions with Frodo, at the Banks.
		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetShadowArcheryTotal());

		// The same fellowship at site 9, the Banks behind it: only the Marksman's 1.
		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
