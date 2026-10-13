package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_355_Tests
{

	// The Fellowship sites, with Silverlode Banks as site 7 in place of Anduin Confluence.  No minion is laid, so
	// each move goes from the Shadow phase straight to the regroup phase.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, no ranger; a skirmish ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion, no ranger; an archery ability only
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, no companion; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man ranger; a maneuver ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf ranger; strength +3 against a Nazgul only
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site7", "1_355");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SilverlodeBanksStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Silverlode Banks
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 7
		 * Shadow Number: 6
		 * Direction: Right
		 * Game Text: <b>River</b>. When the fellowship moves to Silverlode Banks without a ranger, every companion must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(7);

		assertEquals("Silverlode Banks", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(7, card.getBlueprint().getSiteNumber());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void MovingToSilverlodeBanksWithoutARangerExertsEveryCompanionAndMovingOnExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var boromir = scn.GetFreepsCard("boromir");
		var legolas = scn.GetFreepsCard("legolas");
		var bounder = scn.GetFreepsCard("bounder");
		// The other player's ranger, in his own fellowship: not in the one that moves.
		var shadowAragorn = scn.GetShadowCard("aragorn");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(boromir, legolas, shadowAragorn);
		scn.MoveAlliesToTable(bounder);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(shadowAragorn));

		scn.FreepsPass();

		// Every companion of the fellowship exerts: the Ring-bearer, a [gondor] Man and an Elf, none a ranger.
		// The ally is no companion; the other player's companion is not of this fellowship.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(shadowAragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from Silverlode Banks, to another river site, still without a ranger.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAragornNobodyExertsMovingToSilverlodeBanks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(aragorn, boromir);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));

		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithArwenNobodyExertsMovingToSilverlodeBanks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		// A ranger who is an [elven] Elf, where Aragorn is a [gondor] Man.
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCompanionsToTable(arwen, legolas);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsPass();

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
