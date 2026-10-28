package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_330_Tests
{

	// The King path, whose site 1 is Edoras Hall.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; Valiant is its whole text
					// Swift Steed: [rohan] possession, mount, printed 2, on a [rohan] Man; a plains text and a skirmish trigger
					put("steed", "7_253");
					// Sturdy Shield: [rohan] possession, shield, printed 2, on a [rohan] Man; read in hand only
					put("shield", "15_141");
					// Throne of the Golden Hall: [rohan] artifact, printed 2; read in hand only
					put("throne", "17_98");
					// Fortress Never Fallen: [rohan] condition, printed 2; read in hand only
					put("fortress", "4_276");
					// Armor of the Citadel: [gondor] possession, armor, printed 2; read in hand only
					put("armor", "11_55");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EdorasHallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Edoras Hall
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 1
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: The twilight cost of each [rohan] possession is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Edoras Hall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void EachRohanPossessionCostsOneLessAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var steed = scn.GetFreepsCard("steed");
		var shield = scn.GetFreepsCard("shield");
		var throne = scn.GetFreepsCard("throne");
		var fortress = scn.GetFreepsCard("fortress");
		var armor = scn.GetFreepsCard("armor");
		var shadowShield = scn.GetShadowCard("shield");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveCardsToHand(steed, shield, throne, fortress, armor, shadowShield);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertInHand(steed, shield, throne, fortress, armor, shadowShield);
		// Each printed 2.  A mount and a shield, and the Shadow player's copy in his own hand: each [rohan] possession.
		assertEquals(1, scn.GetTwilightCost(steed));
		assertEquals(1, scn.GetTwilightCost(shield));
		assertEquals(1, scn.GetTwilightCost(shadowShield));
		// A [rohan] artifact, a [rohan] condition and a [gondor] possession: not touched.
		assertEquals(2, scn.GetTwilightCost(throne));
		assertEquals(2, scn.GetTwilightCost(fortress));
		assertEquals(2, scn.GetTwilightCost(armor));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(steed);
		assertAttachedTo(steed, guard);
		assertEquals(1, scn.GetTwilight()); // 2 printed - 1
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnceTheFellowshipHasLeftEdorasHallARohanPossessionCostsItsPrintedNumber() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var steed = scn.GetFreepsCard("steed");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(steed);
		scn.FreepsRefreshDecision();

		// Edoras Hall is behind the fellowship: its text is not active (CR 4.2 "active").
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInHand(steed);
		assertEquals(2, scn.GetTwilightCost(steed));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(steed);
		assertAttachedTo(steed, guard);
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
