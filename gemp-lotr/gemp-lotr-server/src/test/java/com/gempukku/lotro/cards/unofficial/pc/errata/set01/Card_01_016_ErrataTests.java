package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_016_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("kingdom", "51_16");
					put("gimli", "1_13");
					put("guard", "51_7");

					put("runner1", "1_178");
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void GreatestKingdomofMyPeopleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Greatest Kingdom of My People
		* Unique: True
		* Side: FREE_PEOPLE
		* Culture: Dwarven
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: <b>Tale.</b>  To play, exert a Dwarf. 
		* 	The first time your opponent plays an Orc each turn, you may take a [dwarven] kingdom into hand from your discard pile.  The Shadow player may discard the bottom 2 cards of their draw deck to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");

		assertTrue(kingdom.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, kingdom.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, kingdom.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, kingdom.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(kingdom, Keyword.SUPPORT_AREA));
		assertTrue(scn.hasKeyword(kingdom, Keyword.TALE));
		assertEquals(1, kingdom.getBlueprint().getTwilightCost());
	}

	@Test
	public void GreatestKingdomExertsADwarfToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.FreepsMoveCharToTable(gimli);
		scn.FreepsMoveCardToHand(kingdom, guard);

		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(kingdom));
		scn.FreepsPlayCard(guard);
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsPlayCard(kingdom);
		assertEquals(1, scn.GetWoundsOn(guard));
	}

	@Test
	public void GreatestKingdomTriggerOptionallyPermitsRetrievalOf1DwarvenCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.FreepsMoveCardToSupportArea(kingdom);
		scn.FreepsMoveCardToDiscard(gimli, guard);
		scn.FreepsMoveCardToDiscard("runner1", "runner2");

		var runner1 = scn.GetShadowCard("runner1");
		scn.ShadowMoveCardToHand(runner1);

		scn.SetTwilight(20);
		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetFreepsCardChoiceCount());
		var choices = scn.FreepsGetBPChoices();
		assertTrue(choices.contains(gimli.getBlueprintId()));
		assertTrue(choices.contains(guard.getBlueprintId()));
		assertEquals(Zone.DISCARD, gimli.getZone());

		scn.FreepsChooseCardBPFromSelection(gimli);
		scn.ShadowChooseNo();
		assertEquals(Zone.HAND, gimli.getZone());
	}

	@Test
	public void GreatestKingdomTriggerCanBePreventedByShadowDiscardingFromDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.FreepsMoveCardToSupportArea(kingdom);
		scn.FreepsMoveCardToDiscard(gimli, guard);
		scn.FreepsMoveCardToDiscard("runner1", "runner2");

		var runner1 = scn.GetShadowCard("runner1");
		scn.ShadowMoveCardToHand(runner1);

		scn.SetTwilight(20);
		scn.StartGame();

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetFreepsCardChoiceCount());

		assertEquals(Zone.DISCARD, gimli.getZone());
		scn.FreepsChooseCardBPFromSelection(gimli);
		var bottom1 = scn.GetFromBottomOfShadowDeck(1);
		var bottom2 = scn.GetFromBottomOfShadowDeck(2);
		var bottom3 = scn.GetFromBottomOfShadowDeck(3);
		assertEquals(Zone.DECK, bottom1.getZone());
		assertEquals(Zone.DECK, bottom2.getZone());
		assertEquals(Zone.DECK, bottom3.getZone());

		scn.ShadowChooseYes();
		assertEquals(Zone.DISCARD, gimli.getZone());
		assertEquals(Zone.DISCARD, bottom1.getZone());
		assertEquals(Zone.DISCARD, bottom2.getZone());
		assertEquals(Zone.DECK, bottom3.getZone());
	}

	@Test
	public void GreatestKingdomTriggersOnlyOnceIfShadowPrevents() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.FreepsMoveCardToSupportArea(kingdom);
		scn.FreepsMoveCardToDiscard(gimli, guard);
		scn.FreepsMoveCardToDiscard("runner1", "runner2");

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.ShadowMoveCardToHand(runner1, runner2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(gimli);

		scn.ShadowChooseYes();

		scn.ShadowPlayCard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void GreatestKingdomTriggersOnlyOnceIfShadowDoesNotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var kingdom = scn.GetFreepsCard("kingdom");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		scn.FreepsMoveCardToSupportArea(kingdom);
		scn.FreepsMoveCardToDiscard(gimli, guard);
		scn.FreepsMoveCardToDiscard("runner1", "runner2");

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.ShadowMoveCardToHand(runner1, runner2);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPassCurrentPhaseAction();

		scn.ShadowPlayCard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCardBPFromSelection(gimli);

		scn.ShadowChooseNo();

		scn.ShadowPlayCard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}
}
