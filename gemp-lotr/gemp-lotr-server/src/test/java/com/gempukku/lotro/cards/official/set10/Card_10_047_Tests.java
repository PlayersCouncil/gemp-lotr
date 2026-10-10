package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("call", "10_47");
					put("southron", "4_220");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetThreatScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("call", "10_47");
					// Sauron's Hatred, a [sauron] support-area condition: "Shadow: Remove a threat to play a
					// [sauron] Orc without paying any roaming penalty."
					put("hatred", "7_310");
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, cost 2; its text answers a weapon played on it

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a second companion, so that 2 threats can lie
					// Sting, Elven Long Knife, a hand weapon on Bilbo or Frodo: "Regroup: Exert bearer to remove
					// a threat."
					put("sting", "18_113");
					// Tom Bombadil, The Master, a [shire] ally: "To play, remove 2 burdens or 2 threats."
					put("tom", "9_52");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RallyingCallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Rallying Call
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a [raider] Man.
		 * Threats cannot be removed by Free Peoples cards.
		 * <b>Shadow:</b> Remove (1) and play a Southron to add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("call");

		assertEquals("Rallying Call", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RallyingCallRemoves1AndPlaysASouthronToAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var call = scn.GetShadowCard("call");
		var southron = scn.GetShadowCard("southron");
		scn.MoveCardsToSupportArea(call);
		scn.MoveCardsToHand(southron);

		scn.StartGame();
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(call));
		assertTrue(scn.ShadowPlayAvailable(southron));
		assertEquals(Zone.HAND, southron.getZone());

		scn.ShadowUseCardAction(call);

		//10 twiliight -4 base -2 roaming -1 extra tax from Call
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, southron.getZone());
	}

	@Test
	public void RallyingCallHasEffectEvenWhenFinalTwilightPoolIsZero() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var call = scn.GetShadowCard("call");
		var southron = scn.GetShadowCard("southron");
		scn.MoveCardsToSupportArea(call);
		scn.MoveCardsToHand(southron);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.ShadowActionAvailable(call));
		assertTrue(scn.ShadowPlayAvailable(southron));
		assertEquals(Zone.HAND, southron.getZone());

		scn.ShadowUseCardAction(call);

		//We must ensure that the final tally results in an empty twilight pool
		// as it is here that PlayCardFromHand falters if set up incorrectly
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetThreats());
		assertEquals(Zone.SHADOW_CHARACTERS, southron.getZone());
	}

	@Test
	public void WhileItIsInPlayAFreePeoplesCardRemovesNoThreatAndAShadowCardStillDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var tom = scn.GetFreepsCard("tom");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(tom);

		var call = scn.GetShadowCard("call");
		var hatred = scn.GetShadowCard("hatred");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(call, hatred);
		scn.MoveCardsToHand(chieftain);

		scn.StartGame();
		// Two companions hold the 2 threats.
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		// A Free Peoples card that must remove threats to be played: there are no burdens to remove in their
		// place, and Tom Bombadil is not offered.
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertInZone(Zone.HAND, tom);
		assertFalse(scn.FreepsPlayAvailable(tom));

		// A Shadow card removes one as its cost, and what the threat buys happens.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(hatred));
		scn.ShadowUseCardAction(hatred);
		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		// Nobody fights the Orc: on to the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		// A Free Peoples card whose effect removes a threat: Frodo exerts for Sting, and the threat stays.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithoutItAFreePeoplesCardRemovesAThreatAndOneThatMustRemoveThreatsToBePlayedIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetThreatScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sting = scn.GetFreepsCard("sting");
		var tom = scn.GetFreepsCard("tom");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(tom);

		scn.StartGame();
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();

		// The same board with no Rallying Call in play: Tom Bombadil can pay with the 2 threats.
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsPlayAvailable(tom));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
