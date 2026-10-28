package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_179_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "7_179");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: [wraith] Nazgûl, cost 4, site 3; her text reads [wraith] conditions on companions
					put("nertea", "1_234");     // Úlairë Nertëa, Messenger of Dol Guldur: [wraith] Nazgûl, cost 4, site 3; her play trigger needs 5 companions
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, cost 6, site 3; his text needs 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl, cost 2, site 4; its text needs 2 wounds on the Ring-bearer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that a threat can be laid
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GhastlyHostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Ghastly Host
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Shadow:</b> Spot a Nazgûl and remove a threat to play a Nazgûl. Its twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("host");

		assertEquals("Ghastly Host", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingANazgulAndRemovingAThreatPlaysANazgulFromHandAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(host);
		scn.MoveCardsToHand(nertea, khamul, hound);
		assertInZone(Zone.SUPPORT, host);
		assertInHand(nertea, khamul, hound);
		scn.FreepsPass();
		// Site 2: each of these minions is roaming (+2).
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(1);
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetThreats());
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(host));

		scn.ShadowUseCardAction(host);
		assertEquals(0, scn.GetThreats());
		// A Nazgûl of either culture; not the [wraith] Orc, though the pool could pay for him.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nertea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(khamul);

		assertInPlay(khamul);
		// 6 - (6 - 2 + 2 roaming)
		assertEquals(0, scn.GetTwilight());
		assertInHand(nertea, hound);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheNazgulPlayedCostsTwoLessAndThatPlayAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(host);
		scn.MoveCardsToHand(nertea, khamul);
		assertInZone(Zone.SUPPORT, host);
		assertInHand(nertea, khamul);
		scn.FreepsPass();
		// Site 2: each Nazgûl here is roaming (+2).
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(1);

		// 3 in the pool: Nertëa at 4 - 2 + 2 roaming cannot be paid for, so the ability is not offered.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(host));

		// 4 in the pool: not enough to play her from hand (4 + 2 roaming), enough through this card.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(nertea));
		assertTrue(scn.ShadowActionAvailable(host));

		scn.ShadowUseCardAction(host);
		// Nertëa is the one Nazgûl the pool can pay for: she is played unasked.
		assertInPlay(nertea);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetTwilight());

		// Khamûl played from hand afterwards pays his full 6 + 2 roaming.
		scn.SetTwilight(8);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(khamul);
		assertInPlay(khamul);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilANazgulCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var nertea = scn.GetShadowCard("nertea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(host);
		scn.MoveCardsToHand(nertea);
		assertInZone(Zone.SUPPORT, host);
		assertInHand(nertea);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hound);
		scn.AddThreats(1);
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		// A [wraith] Orc is no Nazgûl.
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(host));

		// Khamûl, a [raider] Nazgûl, is.
		scn.MoveMinionsToTable(khamul);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(host));

		scn.ShadowUseCardAction(host);
		assertInPlay(nertea);
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoThreatToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(host);
		scn.MoveCardsToHand(nertea);
		assertInZone(Zone.SUPPORT, host);
		assertInHand(nertea);
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(host));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(host));
	}

	@Test
	public void IsNotOfferedWithNoNazgulInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var lemenya = scn.GetShadowCard("lemenya");
		var nertea = scn.GetShadowCard("nertea");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"));

		scn.StartGame();
		scn.MoveCardsToSupportArea(host);
		// A Nazgûl in the discard pile, and a [wraith] Orc in hand the pool could pay for.
		scn.MoveCardsToDiscard(nertea);
		scn.MoveCardsToHand(hound);
		assertInZone(Zone.SUPPORT, host);
		assertInDiscard(nertea);
		assertInHand(hound);
		scn.FreepsPass();
		scn.MoveMinionsToTable(lemenya);
		scn.AddThreats(1);
		scn.SetTwilight(6);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(host));

		// With her in hand, it is.
		scn.MoveCardsToHand(nertea);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(host));
		assertEquals(1, scn.GetThreats());
	}
}
