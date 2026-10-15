package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_268_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inquisitor", "1_268");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetDiscardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inquisitor1", "1_268");
					put("inquisitor2", "1_268");
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, cost 2, site 6; a regroup ability only, nothing on being played
					put("guard1", "1_7");       // Dwarf Guard: here only cards in the Free Peoples hand
					put("guard2", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTwoInquisitorsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inquisitor1", "1_268");
					put("inquisitor2", "1_268");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcInquisitorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Inquisitor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: When you play this minion, you may make the Free Peoples player discard a card at random from hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("inquisitor");

		assertEquals("Orc Inquisitor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithNoCardInTheFreePeoplesHandTheDiscardIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoInquisitorsScenario();

		var inquisitor1 = scn.GetShadowCard("inquisitor1");
		var inquisitor2 = scn.GetShadowCard("inquisitor2");
		// The Free Peoples player's own copy of the minion is the one card in his hand.
		var spare = scn.GetFreepsCard("inquisitor1");
		scn.MoveCardsToHand(inquisitor1, inquisitor2, spare);
		assertInHand(spare);

		scn.StartGame();
		// Each Inquisitor costs 3 + 2 for roaming.
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());

		// With a card in that hand the offer is made, and a yes discards the card.
		scn.ShadowPlayCard(inquisitor1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor1));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(spare);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The hand is empty now.  A yes would do nothing, so nothing is asked.
		scn.ShadowPlayCard(inquisitor2);
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inquisitor2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedItMayMakeTheFreePeoplesPlayerDiscardACardAtRandom() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var inquisitor = scn.GetShadowCard("inquisitor1");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(inquisitor, slayer);
		scn.MoveCardsToFreepsHand("guard1", "guard2");
		assertInHand(slayer);

		scn.StartGame();
		// 3 to play + 2 roaming (a site 6 minion at site 2)
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());

		scn.ShadowPlayCard(inquisitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		scn.ShadowChooseOptionalTrigger(inquisitor);

		// One of his two Guards, at random: he is not asked which.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		// The Shadow player's own hand is not touched.
		assertEquals(1, scn.GetShadowHandCount());
		assertInHand(slayer);
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var inquisitor = scn.GetShadowCard("inquisitor1");
		scn.MoveCardsToHand(inquisitor);
		scn.MoveCardsToFreepsHand("guard1", "guard2");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());

		scn.ShadowPlayCard(inquisitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyItsOwnPlaySetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscardScenario();

		var inquisitor1 = scn.GetShadowCard("inquisitor1");
		var inquisitor2 = scn.GetShadowCard("inquisitor2");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(inquisitor2, slayer);
		scn.MoveCardsToFreepsHand("guard1", "guard2");

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		// Put into play, not played: no offer.
		scn.MoveMinionsToTable(inquisitor1);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor1);
		assertEquals(2, scn.GetFreepsHandCount());

		// Another [sauron] Orc played while an Inquisitor is in play.
		scn.ShadowPlayCard(slayer);
		assertInZone(Zone.SHADOW_CHARACTERS, slayer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetFreepsHandCount());

		// The second copy played offers its own discard, and the first makes no offer.
		scn.ShadowPlayCard(inquisitor2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inquisitor1));
		scn.ShadowChooseOptionalTrigger(inquisitor2);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
