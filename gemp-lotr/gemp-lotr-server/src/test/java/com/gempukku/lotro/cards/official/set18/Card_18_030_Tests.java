package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enemy", "18_30");
					put("sam", "1_311");          // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; vitality 4
					put("gimli", "1_13");         // Gimli, Son of Glóin: an unbound companion; a skirmish ability used only on purpose
					// Larder: [gollum] Shadow possession in the support area that holds [gollum] tokens (its regroup
					// ability adds them); its skirmish ability is used only on purpose
					put("larder1", "8_23");
					put("larder2", "8_23");
					// Ships of Great Draught: [raider] Shadow possession in the support area that holds [raider] tokens;
					// its abilities are used only on purpose
					put("ships", "8_65");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnemyinYourMidstStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Enemy in Your Midst
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: Heal a Ring-bound companion to reinforce a [gollum] token.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enemy");

		assertEquals("Enemy in Your Midst", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void HealsARingBoundCompanionToReinforceAGollumToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var larder1 = scn.GetShadowCard("larder1");
		var larder2 = scn.GetShadowCard("larder2");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(enemy);
		scn.MoveCardsToSupportArea(larder1, larder2, ships);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, gimli);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddTokensToCard(larder1, 1);
		scn.AddTokensToCard(larder2, 1);
		scn.AddTokensToCard(ships, 1);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(larder1));
		assertEquals(1, scn.GetCultureTokensOn(larder2));
		assertEquals(1, scn.GetCultureTokensOn(ships));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(enemy));
		scn.ShadowPlayCard(enemy);

		// The Ring-bearer and the other Ring-bound companion; not the unbound companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Either card with a [gollum] token; not the card with a [raider] token.
		assertTrue(scn.ShadowHasCardChoiceAvailable(larder1, larder2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ships));
		scn.ShadowChooseCard(larder2);
		assertEquals(1, scn.GetCultureTokensOn(larder1));
		assertEquals(2, scn.GetCultureTokensOn(larder2));
		assertEquals(1, scn.GetCultureTokensOn(ships));

		assertInDiscard(enemy);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoWoundedRingBoundCompanionItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var larder1 = scn.GetShadowCard("larder1");
		scn.MoveCardsToHand(enemy);
		scn.MoveCardsToSupportArea(larder1);

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(sam, gimli);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 1);
		scn.AddTokensToCard(larder1, 1);

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetCultureTokensOn(larder1));
		assertInHand(enemy);

		scn.FreepsPass();

		// Only the unbound companion is wounded: the heal cannot be paid.
		assertInHand(enemy);
		assertFalse(scn.ShadowPlayAvailable(enemy));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoGollumTokenItIsPlayedAndTheHealIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enemy = scn.GetShadowCard("enemy");
		var larder1 = scn.GetShadowCard("larder1");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(enemy);
		scn.MoveCardsToSupportArea(larder1, ships);

		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 2);
		scn.AddTokensToCard(ships, 1);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetCultureTokensOn(larder1));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertInZone(Zone.SUPPORT, larder1, ships);

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(enemy));
		scn.ShadowPlayCard(enemy);

		// Sam is the one wounded Ring-bound companion, healed unasked; no card has a [gollum] token to reinforce.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetCultureTokensOn(larder1));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertInDiscard(enemy);
		assertInZone(Zone.SUPPORT, larder1, ships);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
