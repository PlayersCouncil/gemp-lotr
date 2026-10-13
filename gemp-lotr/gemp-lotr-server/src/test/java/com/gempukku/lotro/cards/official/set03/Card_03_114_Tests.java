package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_114");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTwilightScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("trolls", "3_114");
					put("gaffer", "1_291");     // The Gaffer, Sam's Father: Hobbit ally, vitality 2; a fellowship ability only
					put("bounder", "1_286");    // Bounder: Hobbit ally, vitality 2; a skirmish ability only
					put("runner", "1_178");     // Goblin Runner: cost 1, site 4; "When you play this minion, you may add (2)", which the game adds without asking
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThreeMonstrousTrollsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Three Monstrous Trolls
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Tale</b>. Plays to your support area.<br><b>Response:</b> If a Shadow card is about to add any number of twilight tokens, exert a Hobbit ally to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Three Monstrous Trolls", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ResponseExertsAHobbitAllyToPreventAShadowCardAddingTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwilightScenario();

		var trolls = scn.GetFreepsCard("trolls");
		var gaffer = scn.GetFreepsCard("gaffer");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(trolls);
		scn.MoveAlliesToTable(gaffer, bounder);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.FreepsPass();

		// The move added 1 for Frodo and 2 for Ettenmoors: exactly the Runner's price there, 1 + 2 roaming.
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.ShadowPlayAvailable(runner));

		scn.ShadowPlayCard(runner);
		// The Runner is paid for, and his (2) is about to be added.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trolls));

		scn.FreepsAcceptOptionalTrigger();
		// Frodo is a Hobbit and no ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gaffer, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));

		scn.FreepsChooseCard(gaffer);
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(bounder));
		// 0 and not 2: nothing was added.
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SUPPORT, trolls);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
