package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_278_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "7_278");
					put("engineer", "7_272");   // Gorgoroth Engineer: a besieger Orc; his text answers only his play from a stacked site
					put("troll", "8_102");      // Great Hill Troll: a besieger who is a Troll; his trigger is for King-block sites only
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc who is no besieger; his text answers a weapon played on him
					put("hand1", "1_7");        // Dwarf Guard: cards for the Shadow hand to pay with
					put("hand2", "1_7");
					put("hand3", "1_7");
					put("hand4", "1_7");
					put("hand5", "1_7");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] character Foes of Mordor exerts
					put("foes", "1_105");       // Foes of Mordor: "Maneuver: Exert a [gondor] character to wound a [sauron] minion."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GorgorothSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gorgoroth Soldier
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Besieger</b>.<br><b>Response:</b> If a besieger is about to take a wound, discard 2 cards from hand to prevent it.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soldier");

		assertEquals("Gorgoroth Soldier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BESIEGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void DiscardsTwoCardsFromHandToPreventAWoundOnABesiegerOrcOrTrollAndNotOnAnOrcWhoIsNoBesieger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var engineer = scn.GetShadowCard("engineer");
		var troll = scn.GetShadowCard("troll");
		var chieftain = scn.GetShadowCard("chieftain");
		var hand1 = scn.GetShadowCard("hand1");
		var hand2 = scn.GetShadowCard("hand2");
		var hand3 = scn.GetShadowCard("hand3");
		var hand4 = scn.GetShadowCard("hand4");
		var hand5 = scn.GetShadowCard("hand5");

		var aragorn = scn.GetFreepsCard("aragorn");
		var foes = scn.GetFreepsCard("foes");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(foes);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, engineer, troll, chieftain);
		scn.SetShadowHand(hand1, hand2, hand3, hand4, hand5);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(engineer));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(chieftain));

		// A wound on a [sauron] Orc who is no besieger, with cards enough in hand: not answered.
		scn.FreepsUseCardAction(foes);
		scn.FreepsChooseCard(chieftain);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(5, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// A wound on a besieger who is a Troll.
		scn.FreepsUseCardAction(foes);
		scn.FreepsChooseCard(troll);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowChooseOptionalTrigger(soldier);
		assertEquals(2, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(hand1, hand2);
		assertInDiscard(hand1, hand2);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// A wound on the Soldier himself, a besieger Orc.  The Engineer, a besieger no wound is coming to, is
		// left as he was.
		scn.FreepsUseCardAction(foes);
		scn.FreepsChooseCard(soldier);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(soldier));
		scn.ShadowChooseOptionalTrigger(soldier);
		scn.ShadowChooseCards(hand3, hand4);
		assertInDiscard(hand3, hand4);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(engineer));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOneCardInHandTheResponseIsNotOfferedAndTheWoundIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetShadowCard("soldier");
		var hand1 = scn.GetShadowCard("hand1");

		var aragorn = scn.GetFreepsCard("aragorn");
		var foes = scn.GetFreepsCard("foes");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(foes);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SetShadowHand(hand1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.FreepsUseCardAction(foes);
		// The Soldier is the one [sauron] minion: chosen unasked.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(soldier));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
