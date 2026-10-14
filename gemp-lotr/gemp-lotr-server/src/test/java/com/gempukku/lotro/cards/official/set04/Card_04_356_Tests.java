package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_356_Tests
{

	// The Towers path, Hornburg Causeway its site 7.  Site 8 is Ring of Isengard 4_358, with no archery text.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "4_316");        // Sam, Samwise the Brave: a Ring-bound companion; two responses used only on purpose
					put("aragorn", "4_109");    // Aragorn, Heir of Elendil: an unbound [gondor] Man; defender +1 only
					put("legolas", "1_51");     // Legolas, Prince of Mirkwood: an unbound [elven] Elf; an archer (the fellowship archery total)
					put("gimli", "1_13");       // Gimli, Son of Gloin: an unbound [dwarven] Dwarf; an ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: an unbound [gandalf] Wizard; a strength bonus only
					put("boromir", "1_97");     // Boromir, Son of Denethor: an unbound [gondor] Man; an ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, no companion; an ability used only on purpose
					put("marksman", "1_176");   // Goblin Marksman: a minion archer and nothing else
				}},
				new HashMap<>() {{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "4_356");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornburgCausewayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hornburg Causeway
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>Battleground</b>. The minion archery total is +2 for each unbound companion over 3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Hornburg Causeway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void TheFourthAndFifthUnboundCompanionsEachAddTwoAndRingBoundCompanionsAndAnAllyAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(sam, aragorn, legolas, gimli);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCardsToHand(gandalf, boromir);

		assertEquals("Hornburg Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		// Five companions, three of them unbound: a count of all companions over 3 would read 4.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.FreepsPlayCard(boromir);
		assertInZone(Zone.FREE_CHARACTERS, boromir);
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InTheArcheryPhaseAFourthUnboundCompanionAddsTwoToTheMinionArchers() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, gandalf);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.FreepsPass();

		assertEquals("Hornburg Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);

		// 1 for the Marksman + 2 for the fourth unbound companion
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftHornburgCausewayFiveUnboundCompanionsAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, legolas, gimli, gandalf, boromir);

		scn.StartGame();
		scn.SkipToSite(7);

		assertEquals("Hornburg Causeway", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(4, scn.GetShadowArcheryTotal());

		scn.SkipToSite(8);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, aragorn, legolas, gimli, gandalf, boromir);
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
