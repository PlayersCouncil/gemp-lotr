package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_250_Tests
{

	// The card's own block: the Shadow player lays each next site.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "1_50");     // Legolas, Greenleaf: Archer (fellowship archery total 1); his archery ability is not used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: no archer; a maneuver ability never used here
					// Archer is the Marksman's whole text: four of them are a minion archery total of 4.
					put("marksman1", "1_176");  // Goblin Marksman
					put("marksman2", "1_176");
					put("marksman3", "1_176");
					put("marksman4", "1_176");
				}},
				new HashMap<>()
				{{
					put("site1", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("site2", "13_185");     // Abandoned Mine Shaft: adds a burden when left in the regroup phase outside region 1; never left here
					put("undeep", "11_250");
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
	public void NorthUndeepStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: North Undeep
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: <b>River</b>. The minion archery total is -3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite("undeep");

		assertEquals("North Undeep", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.RIVER));
	}

	@Test
	public void AtTheUndeepFourMinionArchersMakeOneWoundAndTheFellowshipArcheryTotalIsUntouched() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		var marksman4 = scn.GetShadowCard("marksman4");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		// The Shadow player lays North Undeep as site 2.
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("undeep"));
		scn.MoveMinionsToTable(marksman1, marksman2, marksman3, marksman4);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals("North Undeep", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 4 Marksmen - 3
		assertEquals(1, scn.GetShadowArcheryTotal());
		// Legolas's 1, not lowered.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The Free Peoples player places the one wound of minion archery fire; the Shadow player then places the
		// fellowship's one, which kills a Marksman (vitality 1).
		scn.PassCurrentPhaseActions();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.ShadowChooseCard(marksman1);
		assertInZone(Zone.DISCARD, marksman1);
		assertInZone(Zone.SHADOW_CHARACTERS, marksman2, marksman3, marksman4);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftTheUndeepFourMinionArchersMakeFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		var marksman4 = scn.GetShadowCard("marksman4");

		// North Undeep is laid as site 1, and the Marksmen are laid in the fellowship phase to read the total there.
		scn.StartGame(scn.GetFreepsSite("undeep"));
		var undeep = scn.GetCurrentSite();
		assertEquals("North Undeep", undeep.getBlueprint().getTitle());
		scn.MoveMinionsToTable(marksman1, marksman2, marksman3, marksman4);
		// 4 Marksmen - 3
		assertEquals(1, scn.GetShadowArcheryTotal());

		// The fellowship moves on to Abandoned Mine Shaft, site 2; North Undeep lies behind it.
		scn.FreepsRefreshDecision();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals("Abandoned Mine Shaft", scn.GetCurrentSite().getBlueprint().getTitle());
		assertSame(undeep, scn.GetSite(1));
		// The same four Marksmen, with nothing taken off.
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
