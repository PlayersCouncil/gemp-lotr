package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_04_333_Tests
{

	// The Towers path with Plains of Rohan Camp as its site 2.  Site 1 is Eastemnet Gullies (its Fellowship action plays
	// Legolas, who is in no deck here) and site 3 Barrows of Edoras.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a Maneuver ability only
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3; valiant only
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, vitality 2; a Fellowship ability only
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_333");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_352");
					put("site7", "102_71");
					put("site8", "4_358");
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PlainsofRohanCampStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Plains of Rohan Camp
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 2T
		 * Shadow Number: 1
		 * Direction: Left
		 * Game Text: <b>Plains</b>. At the start of each fellowship phase, heal up to 3 wounds from companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(2);

		assertEquals("Plains of Rohan Camp", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(2, card.getBlueprint().getSiteNumber());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void EachFellowshipPhaseBegunAtTheCampHealsUpToThreeWoundsFromTheMoversCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var shadowFrodo = scn.GetShadowRingBearer();
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveAlliesToTable(guma);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(guma, 1);
		scn.AddWoundsToChar(shadowFrodo, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(shadowFrodo));

		// Arriving at the Camp at the end of the fellowship phase heals nobody, nor does the start of the regroup
		// phase there.
		scn.FreepsPass();
		assertEquals("Plains of Rohan Camp", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(frodo));

		// The second player's turn begins at his site 1, with the Camp ahead of him on the path: nothing for him.
		scn.SkipToOtherPlayersTurn();
		assertEquals(1, scn.ShadowGetSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(1, scn.GetWoundsOn(shadowFrodo));

		// The first player's next turn begins at the Camp.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());

		// Companions only: Guma is an ally, and the other player's Frodo is no companion of this fellowship.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, shadowFrodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		// The text counts wounds, not companions: two may come off Aragorn.
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Three wounds healed, and no fourth is asked for: the Guard keeps his.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(guma));
		assertEquals(1, scn.GetWoundsOn(shadowFrodo));

		// "Each fellowship phase": the second player's turn begins at the Camp too, and his own companion is healed.
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowFrodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, guma));
		scn.ShadowChooseCard(shadowFrodo);
		assertEquals(0, scn.GetWoundsOn(shadowFrodo));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheHealsMayBeDeclinedAndTheWoundsStay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsPass();
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(2, scn.FreepsGetSiteNumber());

		// "Up to 3": each heal is asked for in turn, and each may be left untaken.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineChoosing();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsDeclineChoosing();
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsDeclineChoosing();

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
