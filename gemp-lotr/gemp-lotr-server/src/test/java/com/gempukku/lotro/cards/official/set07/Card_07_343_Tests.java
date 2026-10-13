package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_343_Tests
{

	// The King path, Pelennor Plain its site 4.  Site 3 is The Dimholt (a fellowship ability used only on purpose).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability for a Hobbit, used only on purpose

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text, no archer
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; Damage +1 is its whole text
				}},
				new HashMap<>() {{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_343");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PelennorPlainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Pelennor Plain
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 4
		 * Shadow Number: 4
		 * Direction: Right
		 * Game Text: <b>Plains</b>. <b>Archery:</b> Spot your minion and remove 3 burdens to make the minion archery total +3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Pelennor Plain", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void ArcheryActionRemovesThreeBurdensToMakeTheMinionArcheryTotalPlus3ForThatPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plain = scn.GetShadowSite(4);
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(4);
		scn.FreepsPass();

		assertSame(plain, scn.GetCurrentSite());
		// A [moria] Orc alone is the minion spotted.
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		// The Free Peoples player has burdens to remove, but no minion of his own to spot.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(4, scn.GetBurdens());
		assertFalse(scn.FreepsActionAvailable(plain));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(plain));
		assertEquals(0, scn.GetShadowArcheryTotal());

		scn.ShadowUseCardAction(plain);
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// The archery fire: three wounds for the Free Peoples player to place.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The archery phase is over, and the +3 with it.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetBurdens());
	}

	@Test
	public void IsNotOfferedWithOnlyTwoBurdensToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var plain = scn.GetShadowSite(4);
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddBurdens(2);
		scn.FreepsPass();

		assertSame(plain, scn.GetCurrentSite());
		// An [isengard] Uruk-hai alone is the minion spotted.
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(2, scn.GetBurdens());
		assertFalse(scn.ShadowActionAvailable(plain));

		scn.AddBurdens(1);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(plain));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
