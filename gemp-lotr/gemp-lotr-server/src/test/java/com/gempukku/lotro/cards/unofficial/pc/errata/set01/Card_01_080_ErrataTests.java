package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_080_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ottar", "51_80");
					put("ottar2", "51_80");     // a second copy, as a Free Peoples card to hold in hand
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: companion, cost 4
					put("barliman", "1_70");    // Barliman Butterbur, Prancing Pony Proprietor: [gandalf] Man ally, not Gandalf
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion with no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("troop4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OttarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Ottar, Man of Laketown
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Man
		 * Home: 3
		 * Strength: 2
		 * Vitality: 4
		 * Game Text: To play, spot Gandalf.
		 * Fellowship: Exert Ottar and discard a card from hand to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ottar");

		assertEquals("Ottar", card.getBlueprint().getTitle());
		assertEquals("Man of Laketown", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 3)));
	}

	@Test
	public void CannotBePlayedUntilGandalfIsSpottedThenIsPlayedForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToHand(ottar, gandalf);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();

		// Barliman is a [gandalf] card but not Gandalf.
		assertFalse(scn.FreepsPlayAvailable(ottar));

		scn.FreepsPlayCard(gandalf);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(ottar));

		scn.FreepsPlayCard(ottar);
		assertInZone(Zone.SUPPORT, ottar);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionExertsOttarAndDiscardsAChosenCardFromHandToDrawUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var barliman = scn.GetFreepsCard("barliman");
		var ottar2 = scn.GetFreepsCard("ottar2");
		var troop = scn.GetFreepsCard("troop");
		var troop2 = scn.GetFreepsCard("troop2");
		scn.MoveAlliesToTable(ottar, barliman);
		scn.MoveCardsToHand(ottar2, troop);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(troop2);
		scn.FreepsRefreshDecision();

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsActionAvailable(ottar));

		scn.FreepsUseCardAction(ottar);
		// Any card in hand may go, a Free Peoples card or a Shadow card; exactly one must.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ottar2, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertInDiscard(troop);
		assertInHand(ottar2, troop2);
		assertEquals(2, scn.GetFreepsHandCount()); // 2 - 1 discarded + 1 drawn
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsUseCardAction(ottar);
		scn.FreepsChooseCard(ottar2);
		scn.FreepsUseCardAction(ottar);
		scn.FreepsChooseCard(troop2);

		// Vitality 4 with 3 wounds: exhausted, so he can no longer exert, though there are cards in hand.
		assertEquals(3, scn.GetWoundsOn(ottar));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetFreepsDiscardCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertFalse(scn.FreepsActionAvailable(ottar));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBeUsedWithNoCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		scn.MoveAlliesToTable(ottar);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(ottar));
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.GetFreepsDeckCount() > 0);
		assertFalse(scn.FreepsActionAvailable(ottar));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBeUsedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var ottar2 = scn.GetFreepsCard("ottar2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveAlliesToTable(ottar);
		scn.MoveCardsToHand(ottar2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(ottar));

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(ottar));
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(ottar));
	}
}
