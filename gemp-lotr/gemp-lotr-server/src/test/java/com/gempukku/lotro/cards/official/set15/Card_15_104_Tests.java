package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shrieker", "15_104");
					put("retribution", "12_101"); // Retribution: [orc] support-area condition, cost 0, no requirement; a win trigger and a Shadow ability used only on purpose
					put("blow", "15_115");        // Pummeling Blow: [orc] condition, cost 0, played on the fellowship's current site by exerting an [orc] minion; Trolls +1 only
					put("goaded", "12_67");       // Goaded to War: a [men] condition; adds (1) only as the fellowship moves to a battleground or plains
					put("spear", "12_96");        // Orc Spear: an [orc] possession (hand weapon) for an [orc] minion; its (2) only when played
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, cost 3; +2 only at an underground site
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: a companion; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackLandShriekerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Black Land Shrieker
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 20
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] condition.<br>Black Land Shrieker is twilight cost -1 for each threat you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shrieker");

		assertEquals("Black Land Shrieker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(20, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ToPlayItNeedsAnOrcConditionAndAMenConditionOrAnOrcPossessionWillNotDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shrieker = scn.GetShadowCard("shrieker");
		var retribution = scn.GetShadowCard("retribution");
		var goaded = scn.GetShadowCard("goaded");
		var denizen = scn.GetShadowCard("denizen");
		var spear = scn.GetShadowCard("spear");

		scn.StartGame();
		scn.MoveCardsToHand(shrieker, retribution);
		assertInZone(Zone.HAND, shrieker);
		assertInZone(Zone.HAND, retribution);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A [men] condition, an [orc] possession on an [orc] Orc: no [orc] condition to spot.
		scn.MoveCardsToSupportArea(goaded);
		scn.MoveMinionsToTable(denizen);
		scn.AttachCardsTo(denizen, spear);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, goaded);
		assertInZone(Zone.ATTACHED, spear);

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shrieker));

		scn.ShadowPlayCard(retribution);
		assertInZone(Zone.SUPPORT, retribution);
		assertTrue(scn.ShadowPlayAvailable(shrieker));
		scn.ShadowPlayCard(shrieker);
		assertInZone(Zone.SHADOW_CHARACTERS, shrieker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ToPlayAnOrcConditionOnTheSiteWillDo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shrieker = scn.GetShadowCard("shrieker");
		var blow = scn.GetShadowCard("blow");
		var denizen = scn.GetShadowCard("denizen");

		scn.StartGame();
		scn.MoveCardsToHand(shrieker, blow);
		assertInZone(Zone.HAND, shrieker);
		assertInZone(Zone.HAND, blow);
		scn.MoveMinionsToTable(denizen);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shrieker));

		// Pummeling Blow exerts the Denizen and goes on the fellowship's current site.
		assertEquals(0, scn.GetWoundsOn(denizen));
		scn.ShadowPlayCard(blow);
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertInZone(Zone.ATTACHED, blow);

		assertTrue(scn.ShadowPlayAvailable(shrieker));
		scn.ShadowPlayCard(shrieker);
		assertInZone(Zone.SHADOW_CHARACTERS, shrieker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ItsTwilightCostIsOneLessForEachThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// Three companions: room for three threats.
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var shrieker = scn.GetShadowCard("shrieker");
		var denizen = scn.GetShadowCard("denizen");
		var retribution = scn.GetShadowCard("retribution");

		scn.StartGame();
		// Both markers on site 3: the Shadow phase is at site 4, where these site 4 minions do not roam.
		scn.SkipToSite(3);
		scn.MoveCardsToHand(shrieker, denizen);
		scn.MoveCardsToSupportArea(retribution);
		assertInZone(Zone.HAND, shrieker);
		assertInZone(Zone.HAND, denizen);
		assertInZone(Zone.SUPPORT, retribution);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		// No threat: the printed 8, and the Denizen its printed 3.
		assertEquals(0, scn.GetThreats());
		assertEquals(8, scn.GetTwilightCost(shrieker));
		assertEquals(3, scn.GetTwilightCost(denizen));

		scn.AddThreats(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(7, scn.GetTwilightCost(shrieker));
		assertEquals(3, scn.GetTwilightCost(denizen));

		scn.AddThreats(2);
		assertEquals(3, scn.GetThreats());
		// 8 - 3
		assertEquals(5, scn.GetTwilightCost(shrieker));
		assertEquals(3, scn.GetTwilightCost(denizen));

		// One short of 5 it cannot be played; at 5 it can, and takes the whole pool.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shrieker));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(shrieker));
		scn.ShadowPlayCard(shrieker);

		assertInZone(Zone.SHADOW_CHARACTERS, shrieker);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
