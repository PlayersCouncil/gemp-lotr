package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_032_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("board", "7_32");
					put("gandalf", "0_64");     // Gandalf, Stormcrow: vitality 4, no text
					// Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4; +2 only beside 3 Ents
					put("avenger", "6_27");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, cost 6, vitality 3, no text
					put("troop2", "1_177");
					put("threat", "1_197");     // Threat of the Unknown: [moria] Shadow event, cost 0, "Exert your [moria] Orc to draw 2 cards"
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, Damage +1 and no other text
					put("bred", "1_121");       // Bred for Battle: [isengard] Skirmish event, cost 0, "Exert an Uruk-hai to make it strength +3"
					put("deftness", "1_293");   // Halfling Deftness: [shire] Skirmish event, "Exert a Hobbit to make him strength +3"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBoardIsSetStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: The Board Is Set
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Spell</b>.<br>If an event is played, exert Gandalf to make that opponent remove (2) or cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("board");

		assertEquals("The Board Is Set", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfAndTheOpponentMayRemoveTwoToKeepHisEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var threat = scn.GetShadowCard("threat");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(board);
		scn.MoveCardsToHand(threat);
		assertInHand(board, threat);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		// Exactly 2: "remove (2)" can be done in full.
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(threat);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(board));

		scn.FreepsPlayCard(board);
		// The one Gandalf exerts; the [gandalf] companion who is no Gandalf does not.
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// The opponent who played the event chooses, and both can be done.
		assertTrue(scn.ShadowChoiceAvailable("Remove (2)"));
		assertTrue(scn.ShadowChoiceAvailable("Cancel"));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		scn.ShadowChoose("Remove (2)");

		// 2 removed, and the event goes on: 2 cards drawn.
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowHandCount());
		assertInDiscard(threat);
		assertInDiscard(board);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedForAMinionPlayedAndTheOpponentMayHaveHisEventCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var threat = scn.GetShadowCard("threat");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(board);
		scn.MoveCardsToHand(troop2, threat);
		assertInHand(board, troop2, threat);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(troop2);
		// A Shadow card that is no event: not offered, though Gandalf can exert.
		assertInZone(Zone.SHADOW_CHARACTERS, troop2);
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertInHand(board);
		assertTrue(scn.AwaitingShadowPhaseActions());

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(threat);
		// Two [moria] Orcs now: the Shadow player chooses which exerts.
		scn.ShadowChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsPlayAvailable(board));

		scn.FreepsPlayCard(board);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowChoiceAvailable("Remove (2)"));
		scn.ShadowChoose("Cancel");

		// Cancelled: nothing drawn, the twilight left alone, and the Orc's exertion paid for it stays.
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(troop));
		assertInDiscard(threat);
		assertInDiscard(board);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnswersAShadowSkirmishEventOfAnotherCultureToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var bred = scn.GetShadowCard("bred");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(board);
		scn.MoveCardsToHand(bred);
		assertInHand(board, bred);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, savage);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(5, scn.GetStrength(savage));
		scn.ShadowPlayCard(bred);
		// The event's cost: the Uruk-hai exerts.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(board));

		scn.FreepsPlayCard(board);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowChoiceAvailable("Cancel"));
		scn.ShadowChoose("Cancel");

		// Cancelled: no +3, and the exertion paid for it stays.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(bred);
		assertInDiscard(board);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithLessThanTwoTwilightTheEventIsCancelledUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var gandalf = scn.GetFreepsCard("gandalf");
		var troop = scn.GetShadowCard("troop");
		var threat = scn.GetShadowCard("threat");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(board);
		scn.MoveCardsToHand(threat);
		assertInHand(board, threat);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		// One short of (2): only the cancel can be done in full.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(threat);
		scn.FreepsPlayCard(board);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.ShadowChoiceAvailable("Remove (2)"));
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetShadowHandCount());
		assertInDiscard(threat);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenGandalfCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var troop = scn.GetShadowCard("troop");
		var threat = scn.GetShadowCard("threat");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(board);
		scn.MoveCardsToHand(threat);
		// Vitality 4 with 3 wounds: exhausted.  The [gandalf] companion beside him could exert, and is no Gandalf.
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInHand(board, threat);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetShadowHandCount());
		scn.ShadowPlayCard(threat);

		// Not offered: the event is carried out, 2 cards drawn.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertInHand(board);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInDiscard(threat);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedForAnEventOfTheFreePeoplesPlayersOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var board = scn.GetFreepsCard("board");
		var gandalf = scn.GetFreepsCard("gandalf");
		var deftness = scn.GetFreepsCard("deftness");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(board, deftness);
		assertInHand(board, deftness);

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

		// "That opponent": the event was the Free Peoples player's own, so there is nobody to make remove (2) or
		// cancel it.  Not offered, though Gandalf can exert: had it been, the Free Peoples player would be asked
		// before the Shadow player goes on.  The +3 lands.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertEquals(7, scn.GetStrength(frodo));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertInHand(board);
		assertInDiscard(deftness);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
