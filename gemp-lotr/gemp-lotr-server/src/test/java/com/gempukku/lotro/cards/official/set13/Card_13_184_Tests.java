package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_184_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nertea", "13_184");
					put("nertea2", "13_184");      // a second copy, in hand only: unique, so never two in play
					// Úlairë Nertëa, Messenger of Dol Guldur: another card of the title, cost 4; its text answers its own play
					put("messenger", "1_234");
					// Úlairë Lemenya, Lieutenant of Morgul: another [wraith] Nazgûl of cost 4; each companion or ally
					// bearing a [wraith] condition is strength -2 (none is laid)
					put("lemenya", "1_232");
					// Sam, Son of Hamfast: a Ring-bound Hobbit, resistance 5; "Fellowship: Exert Sam to remove a burden"
					put("sam", "1_311");
					// Aragorn, Ranger of the North: an unbound [gondor] Man, resistance 6; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Barliman Butterbur: an ally, and so of resistance 0 (CR 4.2 "resistance"); an ability used only on purpose
					put("barliman", "1_70");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNerteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Úlairë Nertëa, Servant of the Shadow
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>While you can spot a companion who has resistance 3 or less, Úlairë Nertëa is twilight cost -2.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("nertea");

		assertEquals("Úlairë Nertëa", card.getBlueprint().getTitle());
		assertEquals("Servant of the Shadow", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileSamHasResistance3NerteaCostsTwoLessAndNotOnceHisBurdenIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var nertea2 = scn.GetShadowCard("nertea2");
		var messenger = scn.GetShadowCard("messenger");
		var lemenya = scn.GetShadowCard("lemenya");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(nertea, nertea2, messenger, lemenya);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());
		// 5 - 2 burdens; Frodo 10 - 2
		assertEquals(3, scn.GetResistance(sam));
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(2, scn.GetTwilightCost(nertea));
		// A second copy has its own -2 and no more: the two discounts do not reach each other.
		assertEquals(2, scn.GetTwilightCost(nertea2));
		// Another Úlairë Nertëa and another [wraith] Nazgûl in the same hand keep their printed cost: the discount
		// is this card's own, not its title's.
		assertEquals(4, scn.GetTwilightCost(messenger));
		assertEquals(4, scn.GetTwilightCost(lemenya));

		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());
		assertEquals(4, scn.GetResistance(sam));
		assertEquals(4, scn.GetTwilightCost(nertea));
		assertEquals(4, scn.GetTwilightCost(nertea2));
		assertEquals(4, scn.GetTwilightCost(messenger));
		assertEquals(4, scn.GetTwilightCost(lemenya));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void PlayedWhileACompanionHasResistance3ItTakesTwoLessFromThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(3, scn.GetResistance(sam));
		scn.FreepsPass();

		// At site 2 Nertëa (site 3) is roaming: 4 - 2 + 2 roaming. Without the discount it would want 6,
		// and with -3 it would be offered from 3.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(nertea));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(nertea));
		scn.ShadowPlayCard(nertea);

		assertInZone(Zone.SHADOW_CHARACTERS, nertea);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUnboundManAtResistance3CountsWhereAnAllyOfResistance0DoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nertea = scn.GetShadowCard("nertea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(nertea);

		scn.StartGame();
		scn.AddBurdens(2);

		// Every companion above 3; the ally has resistance 0 but is no companion.
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetResistance(frodo));
		assertEquals(0, scn.GetResistance(barliman));
		assertEquals(4, scn.GetTwilightCost(nertea));

		scn.AddBurdens(1);

		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(2, scn.GetTwilightCost(nertea));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
