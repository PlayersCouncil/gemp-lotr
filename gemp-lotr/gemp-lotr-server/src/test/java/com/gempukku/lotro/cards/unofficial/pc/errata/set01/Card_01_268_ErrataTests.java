package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_268_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("inquisitor1", "51_268");
					put("inquisitor2", "51_268");

					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, cost 2, site 6; a regroup ability only, nothing on being played
					put("guard1", "1_7");       // Dwarf Guard: companion, no text in play; here only cards in the Free Peoples hand
					put("guard2", "1_7");
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
		 * Game Text: When you play this minion, you may draw a card.  The Free Peoples player may discard 1 card
		 * at random from hand to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("inquisitor1");

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
	public void WhenPlayedItMayDrawACardIfTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inquisitor = scn.GetShadowCard("inquisitor1");
		scn.MoveCardsToHand(inquisitor);
		scn.MoveCardsToFreepsHand("guard1", "guard2");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		int twilight = scn.GetTwilight();
		int shadowDeck = scn.GetShadowDeckCount();
		var top = scn.GetShadowTopOfDeck();
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());

		scn.ShadowPlayCard(inquisitor);
		// 3 to play + 2 roaming (a site 6 minion at site 2)
		assertEquals(twilight - 5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, inquisitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));

		scn.ShadowChooseOptionalTrigger(inquisitor);
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, top);
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardOneCardAtRandomFromHandToPreventTheDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inquisitor = scn.GetShadowCard("inquisitor1");
		scn.MoveCardsToHand(inquisitor);
		scn.MoveCardsToFreepsHand("guard1", "guard2");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(inquisitor);
		scn.ShadowChooseOptionalTrigger(inquisitor);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.FreepsDecisionAvailable("prevent"));

		scn.FreepsChooseYes();
		// One of his two Guards, at random: he is not asked which.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoCardInHandTheFreePeoplesPlayerIsNotAskedAndTheCardIsDrawn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inquisitor = scn.GetShadowCard("inquisitor1");
		scn.MoveCardsToHand(inquisitor);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());

		scn.ShadowPlayCard(inquisitor);
		scn.ShadowChooseOptionalTrigger(inquisitor);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inquisitor = scn.GetShadowCard("inquisitor1");
		scn.MoveCardsToHand(inquisitor);
		scn.MoveCardsToFreepsHand("guard1", "guard2");

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(inquisitor);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		scn.ShadowDeclineOptionalTrigger();
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheDrawIsOfferedButNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inquisitor = scn.GetShadowCard("inquisitor1");
		scn.MoveCardsToHand(inquisitor);
		scn.MoveCardsToFreepsHand("guard1", "guard2");

		scn.StartGame();
		scn.MoveCardsToShadowDiscard("inquisitor2", "slayer", "guard1", "guard2");
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertEquals(0, scn.GetShadowDeckCount());

		scn.ShadowPlayCard(inquisitor);
		// An offer to draw from an empty deck is still made (Q-0029); the prevention is not asked (Q-0114).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor));
		scn.ShadowChooseOptionalTrigger(inquisitor);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyItsOwnPlaySetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var inquisitor1 = scn.GetShadowCard("inquisitor1");
		var inquisitor2 = scn.GetShadowCard("inquisitor2");
		var slayer = scn.GetShadowCard("slayer");
		scn.MoveCardsToHand(inquisitor2, slayer);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();
		// Put into play, not played: no offer.
		scn.MoveMinionsToTable(inquisitor1);
		scn.ShadowRefreshDecision();
		int shadowDeck = scn.GetShadowDeckCount();

		scn.ShadowPlayCard(slayer);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(inquisitor2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(inquisitor2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(inquisitor1));
		scn.ShadowChooseOptionalTrigger(inquisitor2);
		// No card in the Free Peoples hand, so nothing to prevent with: one card drawn, and no second offer.
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(shadowDeck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
