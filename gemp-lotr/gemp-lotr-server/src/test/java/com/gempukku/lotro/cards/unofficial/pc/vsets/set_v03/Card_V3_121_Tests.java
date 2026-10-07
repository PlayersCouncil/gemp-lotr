package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_V3_121_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "1_302");
					put("pipe", "1_285");
					put("pipeweed", "1_305");
				}},
				new HashMap<>() {{
					put("site1", "103_121");
					put("site2", "1_327");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GatesofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Gates of Isengard
		 * Unique: false
		 * Side:
		 * Culture:
		 * Shadow Number: 0
		 * Type: Site
		 * Subtype: Standard
		 * Site Number: 1K
		 * Game Text: Fellowship: Exert an unbound Hobbit to play a pipe and pipeweed from your draw deck (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Gates of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
	}

	@Test
	public void RequiresUnboundHobbitToUse() throws DecisionResultInvalidException, CardNotFoundException {
		// Only Frodo (Ring-bound) on the table — no unbound Hobbit to exert
		var scn = GetScenario();

		var site = scn.GetFreepsSite(1);

		scn.StartGame();

		// Frodo is Ring-bound, not an unbound Hobbit — action should not be available
		assertFalse(scn.FreepsActionAvailable(site));
	}

	@Test
	public void PlaysAPipeAndPipeweedFromDeckThenLimited() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pipe = scn.GetFreepsCard("pipe");
		var pipeweed = scn.GetFreepsCard("pipeweed");
		var site = scn.GetFreepsSite(1);

		// Merry on table; pipe and pipeweed stay in draw deck
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();

		// Action available with an unbound Hobbit on the table
		assertTrue(scn.FreepsActionAvailable(site));
		scn.FreepsUseCardAction(site);
		scn.DismissRevealedCards();

		// Cost: exert an unbound Hobbit — Merry is the only one, auto-chosen
		assertEquals(1, scn.GetWoundsOn(merry));

		// Effect 1: play a pipe from draw deck — Bilbo's Pipe is the only pipe, auto-chosen
		// Bearer selection: Frodo and Merry are both valid Hobbits; choose Merry
		scn.FreepsChooseCardBPFromSelection(pipe);
		scn.FreepsChooseCard(merry);

		// Effect 2: play a pipeweed from draw deck — Old Toby is the only pipeweed, auto-chosen
		// Old Toby has an optional on-play trigger — decline
		scn.DismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(pipeweed);
		scn.FreepsDeclineOptionalTrigger();

		// Verify: pipe attached to Merry, pipeweed in support area
		assertAttachedTo(pipe, merry);
		assertEquals(Zone.SUPPORT, pipeweed.getZone());

		// Limit once per phase — action no longer available
		assertFalse(scn.FreepsActionAvailable(site));
	}
}
