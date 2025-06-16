package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// Wielder of the Flame
public class Card_17_140_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("appetite", "1_294");
					put("enquea", "17_140");
					put("dt", "12_163");
					put("rider", "12_161");

					put("moria1", "1_21");
					put("moria2", "1_21");
					put("moria3", "1_21");
					put("moria4", "1_21");
					put("moria5", "1_21");
					put("moria6", "1_21");

					put("endgame", "10_30");
					put("aragorn", "1_89");
					put("troop", "1_143");
				}}
		);
	}

	@Test
	public void EnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Úlairë Enquëa, Duplicitous Lieutenant
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 11
		 * Vitality: 4
		 * Site: 3
		 * Game Text: Fierce.
		 * Each time the Free Peoples player heals a companion, you may add a burden.
		 * Maneuver: Exert Ulaire Enquea twice to discard a condition (or 2 conditions if you can spot 5 Free Peoples player's conditions).
		 */

		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Duplicitous Lieutenant", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void HealingFromShadowSourcesDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var appetite = scn.GetFreepsCard("appetite");
		scn.MoveCardsToHand(appetite);

		var enquea = scn.GetShadowCard("enquea");
		var rider = scn.GetShadowCard("rider");
		var dt = scn.GetShadowCard("dt");
		scn.MoveMinionsToTable(enquea);
		scn.MoveCardsToHand(rider);
		scn.MoveCardsToHand(dt);

		scn.StartGame();

		scn.RemoveBurdens(1); //To compensate for the bid
		scn.AddWoundsToChar(frodo, 3);

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(appetite);
		scn.FreepsChoose("2");
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowAcceptOptionalTrigger();

		//Two burdens added from enquea's game text from freeps healing
		assertEquals(2, scn.GetBurdens());

		scn.AddWoundsToChar(frodo, 2);
		scn.SetTwilight(10);

		//moving
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(dt);
		assertEquals(2, scn.GetBurdens());
		scn.ShadowPlayCard(rider);
		scn.ShadowAcceptOptionalTrigger();
		//We should have the DT trigger, but no Enquea triggers
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		//We should see +1 burden from Dark Temptation, but no burdens added from Enquea as DT is a shadow card.
		assertEquals(3, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
	}

	@Test
	public void ManeuverActionDiscardsOneOrTwoConditions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var appetite = scn.GetFreepsCard("appetite");

		var moria1 = scn.GetFreepsCard("moria1");
		var moria2 = scn.GetFreepsCard("moria2");
		var moria3 = scn.GetFreepsCard("moria3");
		var moria4 = scn.GetFreepsCard("moria4");
		var moria5 = scn.GetFreepsCard("moria5");
		var moria6 = scn.GetFreepsCard("moria6");

		scn.MoveCardsToSupportArea(moria1);
		scn.MoveCardsToSupportArea(moria2);
		scn.MoveCardsToSupportArea(moria3);
		scn.MoveCardsToSupportArea(moria4);

		PhysicalCardImpl enquea = scn.GetShadowCard("enquea");
		scn.MoveMinionsToTable(enquea);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowActionAvailable("Use Úlairë Enquëa, Duplicitous Lieutenant"));

		scn.ShadowUseCardAction(enquea);
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to discard"));
		scn.ShadowChooseCard(moria4);

		//with only 4 conditions in play, Enquea should only be requesting a single condition to be discarded.
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.RemoveWoundsFromChar(enquea, 2);
		scn.MoveCardsToSupportArea(moria5);
		scn.MoveCardsToSupportArea(moria6);
		scn.FreepsPassCurrentPhaseAction();
		//There should now be 4 - 1 + 2 = 5 freeps conditions in play.

		scn.ShadowUseCardAction(enquea);
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to discard"));
		scn.ShadowChooseCards(moria5, moria6);
	}

	@Test
	public void EndOfTheGameTriggersEnquea() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var endgame = scn.GetFreepsCard("endgame");
		scn.MoveCompanionToTable(aragorn);
		scn.MoveCardsToHand(endgame);

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);

		var enquea = scn.GetShadowCard("enquea");
		scn.MoveMinionsToTable(enquea);

		scn.StartGame();

		scn.RemoveBurdens(1); //To compensate for the bid
		scn.AddWoundsToChar(aragorn, 3);

		//moving
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsResolveSkirmish(aragorn);

		scn.FreepsPlayCard(endgame);

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsResolveActionOrder("Required trigger from");
		scn.FreepsChooseOption("Heal");
		assertEquals(2, scn.GetWoundsOn(aragorn));

		//As a Freeps healing source, we should have the Enquea trigger
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());

	}
}