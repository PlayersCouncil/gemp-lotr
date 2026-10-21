package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "19_8");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3, Damage +1 and no other text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion, cost 1; "When you play this minion, you may add (2)"
					put("bred", "1_121");       // Bred for Battle: [isengard] Skirmish event, "Exert an Uruk-hai to make it strength +3"
					put("deftness", "1_293");   // Halfling Deftness: [shire] Skirmish event, "Exert a Hobbit to make him strength +3"
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("threat", "1_197");     // Threat of the Unknown: [moria] Shadow event, "Exert your [moria] Orc to draw 2 cards"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Gandalf, Wise Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: <b>Fellowship</b>.<br><b>Response:</b> If an event is played, exert Gandalf twice to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Wise Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FELLOWSHIP));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertsTwiceToCancelAShadowEventWhoseCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var bred = scn.GetShadowCard("bred");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(bred);
		// Vitality 4 with 1 wound: 3 left, just enough to exert twice.
		scn.AddWoundsToChar(gandalf, 1);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInHand(bred);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(5, scn.GetStrength(savage));
		scn.ShadowPlayCard(bred);
		// The event's cost: the Uruk-hai exerts.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(gandalf));

		scn.FreepsUseCardAction(gandalf);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		// Cancelled: no +3, the exertion paid for it stays, and the event is discarded.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(bred);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void CancelsAnEventOfHisOwnPlayerToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var deftness = scn.GetFreepsCard("deftness");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(deftness);
		assertInHand(deftness);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Frodo 3, +1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		scn.FreepsPlayCard(deftness);
		// The event's cost: Frodo, the only Hobbit, exerts.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(gandalf));

		scn.FreepsUseCardAction(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInDiscard(deftness);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenGandalfCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var bred = scn.GetShadowCard("bred");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(bred);
		// Vitality 4 with 2 wounds: 2 left, so he can exert once but not twice.
		scn.AddWoundsToChar(gandalf, 2);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInHand(bred);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(savage));
		scn.ShadowPlayCard(bred);
		// No response is asked for: the event is carried out and the skirmish goes on.
		assertFalse(scn.FreepsActionAvailable(gandalf));
		assertEquals(8, scn.GetStrength(savage));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInDiscard(bred);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACardThatIsNoEventIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		var threat = scn.GetShadowCard("threat");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(runner, threat);
		assertInHand(runner, threat);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(runner);
		// A minion played: Gandalf, able to exert twice, is not offered.  Had he been, the Free Peoples player
		// would be asked before the Shadow player goes on.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The control, in the same phase: an event played is answered.
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(threat);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsActionAvailable(gandalf));

		scn.FreepsUseCardAction(gandalf);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		// No 2 cards drawn.
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(threat);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
