package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_227_Tests
{

	// The card's own block: the Shadow player lays each next site.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Companions of three cultures and races. None has an archery ability.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability never used here
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, cost 3; a skirmish ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit; a fellowship ability never used here
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit, cost 1; protects [shire] tales
					put("knight", "5_35");      // Gondorian Knight: a companion whose whole text is Knight; laid in the dead pile
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, who is no companion; a fellowship ability only

					put("marksman", "1_176");   // Goblin Marksman: Archer is its whole text (minion archery total 1)
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("site2", "13_185");     // Abandoned Mine Shaft: adds a burden when left in the regroup phase outside region 1; never left here
					put("anduin", "11_227");
					// Never laid in these tests.
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void AnduinBanksStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Anduin Banks
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>River</b>. The minion archery total is +2 for each companion in the fellowship over 4.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("anduin");

		assertEquals("Anduin Banks", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void TheFifthAndSixthCompanionsEachAddTwoAndAnAllyAndADeadCompanionAddNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var knight = scn.GetFreepsCard("knight");
		var grimir = scn.GetFreepsCard("grimir");
		// Frodo and three more are four companions; the ally is a fifth Free Peoples character, and a fifth
		// companion lies in the dead pile.
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToDeadPile(knight);
		scn.MoveCardsToHand(boromir, pippin);

		// Anduin Banks is laid as site 1.
		scn.StartGame(scn.GetFreepsSite("anduin"));

		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.FreepsPlayCard(boromir);

		// 1 companion over 4, x 2
		assertEquals(2, scn.GetShadowArcheryTotal());

		scn.FreepsPlayCard(pippin);

		// 2 companions over 4, x 2
		assertEquals(4, scn.GetShadowArcheryTotal());
		// The fellowship archery total is not touched.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AsSiteTwoFiveCompanionsAddTwoWoundsOfMinionArcheryFire() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var grimir = scn.GetFreepsCard("grimir");
		var marksman = scn.GetShadowCard("marksman");
		// Five companions with Frodo, and an ally.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("anduin"));
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals("Anduin Banks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 1 for the Marksman + 1 companion over 4 x 2
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Free Peoples player places three wounds.
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftAnduinBanksFiveCompanionsAddNothingToTheMinionArcheryTotal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var marksman = scn.GetShadowCard("marksman");
		// Frodo and four more are five companions.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam);

		// Anduin Banks is laid as site 1.
		scn.StartGame(scn.GetFreepsSite("anduin"));

		var banks = scn.GetCurrentSite();
		assertEquals("Anduin Banks", banks.getBlueprint().getTitle());
		// 1 companion over 4, x 2
		assertEquals(2, scn.GetShadowArcheryTotal());

		// The fellowship moves on to Abandoned Mine Shaft, site 2; Anduin Banks lies behind it.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		scn.MoveMinionsToTable(marksman);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals("Abandoned Mine Shaft", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(banks, scn.GetSite(1));
		// The same five companions: only the Marksman's 1.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
