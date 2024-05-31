package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_023_ErrataTests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("nobody", "51_23");
					put("gimli", "1_13");

					put("uruk", "1_154");
					put("target", "1_125");
					put("deck1", "1_126");
					put("deck2", "1_127");
					put("deck3", "1_128");
					put("deck4", "1_129");
					put("deck5", "1_130");
					put("deck6", "1_131");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void NobodyTossesaDwarfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Nobody Tosses a Dwarf
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Dwarven
		* Twilight Cost: 0
		* Type: event
		* Subtype: Response
		* Game Text: If a Dwarf wins a skirmish, discard a Shadow card.  Its owner may discard 3 cards from the bottom of their draw deck to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");

		assertFalse(nobody.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, nobody.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, nobody.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, nobody.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(nobody, Timeword.RESPONSE));
		assertEquals(0, nobody.getBlueprint().getTwilightCost());
	}

	@Test
	public void NobodyDoesNotTriggerIfSkirmishLost() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(nobody);
		scn.FreepsMoveCharToTable(gimli);

		var uruk = scn.GetShadowCard("uruk");
		var target = scn.GetShadowCard("target");
		scn.ShadowMoveCharToTable(uruk);
		scn.ShadowMoveCardToSupportArea(target);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
	}

	@Test
	public void NobodyTriggerDiscardsTargetShadowCard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(nobody);
		scn.FreepsMoveCharToTable(gimli);

		var uruk = scn.GetShadowCard("uruk");
		var target = scn.GetShadowCard("target");
		scn.ShadowMoveCharToTable(uruk);
		scn.ShadowMoveCardToSupportArea(target);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);
		//pump gimli to beat the uruk
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(Zone.SUPPORT, target.getZone());
		scn.ShadowChooseNo();
		assertEquals(Zone.DISCARD, target.getZone());
	}

	@Test
	public void NobodyTriggerCanBePreventedByShadowDiscardingBottomDeck() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nobody = scn.GetFreepsCard("nobody");
		var gimli = scn.GetFreepsCard("gimli");
		scn.FreepsMoveCardToHand(nobody);
		scn.FreepsMoveCharToTable(gimli);

		var uruk = scn.GetShadowCard("uruk");
		var target = scn.GetShadowCard("target");
		scn.ShadowMoveCharToTable(uruk);
		scn.ShadowMoveCardToSupportArea(target);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.FreepsResolveSkirmish(gimli);
		//pump gimli to beat the uruk
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		var bot1 = scn.GetFromBottomOfShadowDeck(1);
		var bot2 = scn.GetFromBottomOfShadowDeck(2);
		var bot3 = scn.GetFromBottomOfShadowDeck(3);
		var bot4 = scn.GetFromBottomOfShadowDeck(4);

		assertEquals(Zone.DECK, bot1.getZone());
		assertEquals(Zone.DECK, bot2.getZone());
		assertEquals(Zone.DECK, bot3.getZone());
		assertEquals(Zone.DECK, bot4.getZone());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(Zone.SUPPORT, target.getZone());
		scn.ShadowChooseYes();
		assertEquals(Zone.SUPPORT, target.getZone());

		assertEquals(Zone.DISCARD, bot1.getZone());
		assertEquals(Zone.DISCARD, bot2.getZone());
		assertEquals(Zone.DISCARD, bot3.getZone());
		assertEquals(Zone.DECK, bot4.getZone());
	}
}
