package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_217_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gates", "1_217");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: [wraith] Nazgûl, cost 5, site 2 (not roaming at site 2)
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: [wraith] Nazgûl, cost 8, site 3; text about his strength only
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, cost 6, site 3
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] Nazgûl, cost 4, laid in the discard pile
					put("hound", "7_192");      // Morgul Hound: a [wraith] minion who is an Orc and no Nazgûl, cost 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulGatesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Morgul Gates
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Play a Nazgûl. His twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gates");

		assertEquals("Morgul Gates", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysANazgulFromHandAtTwilightCostTwoLessAndTheNextNazgulPaysInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var nelya = scn.GetShadowCard("nelya");
		var witchking = scn.GetShadowCard("witchking");

		scn.StartGame();
		scn.MoveCardsToHand(gates, nelya, witchking);

		scn.FreepsPass();

		// Shadow phase at site 2. Nelya costs 5: through the Gates 3, and no less.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(gates));
		assertFalse(scn.ShadowPlayAvailable(nelya));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(gates));
		assertFalse(scn.ShadowPlayAvailable(nelya));

		scn.ShadowPlayCard(gates);

		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertInDiscard(gates);
		// 3 - (5 - 2)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// "His" twilight cost: the next Nazgûl played this phase pays his own in full.
		assertEquals(8, scn.GetTwilightCost(witchking));
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(witchking);

		assertInZone(Zone.SHADOW_CHARACTERS, witchking);
		// 10 - (8 + 2 roaming)
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OffersEveryNazgulInHandAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveCardsToHand(gates, nelya, khamul, hound);
		scn.MoveCardsToDiscard(lemenya);

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(gates);

		// Nazgûl of two cultures in hand; neither a [wraith] minion who is no Nazgûl, nor a Nazgûl in the discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertInHand(nelya);
		assertInHand(hound);
		assertInDiscard(lemenya);
		// 20 - (6 - 2 + 2 roaming)
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithNoNazgulInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gates = scn.GetShadowCard("gates");
		var hound = scn.GetShadowCard("hound");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.MoveCardsToHand(gates, hound);
		scn.MoveCardsToDiscard(lemenya);

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// The minion in hand is no Nazgûl, and the Nazgûl is not in hand.
		assertTrue(scn.ShadowPlayAvailable(hound));
		assertFalse(scn.ShadowPlayAvailable(gates));
	}
}
