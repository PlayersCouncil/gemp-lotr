package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mellon", "15_31");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] Ent companion, cost 4; strength +2 only with 3 Ents
					put("barliman", "1_70");    // Barliman Butterbur: a [gandalf] ally; his Fellowship ability is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; his Maneuver ability is used only on purpose
					put("onod", "17_26");       // Woodland Onod: Ent companion; a skirmish ability used only on purpose
					put("treebeard", "4_104");  // Treebeard, Oldest Living Thing: Ent ally; a Fellowship ability only
					put("salve", "12_32");      // Salve: a [gandalf] condition, its one ability a response to a killing wound
					put("depart", "3_31");      // Depart Silently: a [gandalf] Maneuver event
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
					put("troop2", "1_177");
					put("runner", "1_178");     // Goblin Runner: [moria] minion, its one text on its own play
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] minion; its texts are about unbound Hobbits
					put("destroyer", "17_124"); // White Hand Destroyer: [uruk-hai] minion; hunter only for sites controlled
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MellonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Mellon!
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [gandalf] companions.<br>At the start of the maneuver phase, discard this condition to make each player count the number of cards in his or her hand, discard each of them and draw the same number of cards from the top of his or her draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mellon");

		assertEquals("Mellon!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondGandalfCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mellon = scn.GetFreepsCard("mellon");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(mellon, avenger);

		scn.StartGame();

		// One [gandalf] companion. Barliman is a [gandalf] ally, and Aragorn and Frodo are companions of other
		// cultures.
		assertFalse(scn.FreepsPlayAvailable(mellon));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(avenger);
		assertInZone(Zone.FREE_CHARACTERS, avenger);

		// Gandalf, a Wizard, and the Ent Avenger, an Ent.
		assertTrue(scn.FreepsPlayAvailable(mellon));
		scn.FreepsPlayCard(mellon);
		assertInZone(Zone.SUPPORT, mellon);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseItIsDiscardedAndEachPlayerTradesHisHandForAsManyCardsFromHisDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mellon = scn.GetFreepsCard("mellon");
		var aragorn = scn.GetFreepsCard("aragorn");
		var salve = scn.GetFreepsCard("salve");
		var depart = scn.GetFreepsCard("depart");
		var avenger = scn.GetFreepsCard("avenger");
		var onod = scn.GetFreepsCard("onod");
		var treebeard = scn.GetFreepsCard("treebeard");
		var barliman = scn.GetFreepsCard("barliman");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var runner = scn.GetShadowCard("runner");
		var pursuer = scn.GetShadowCard("pursuer");
		var destroyer = scn.GetShadowCard("destroyer");
		var shadowSalve = scn.GetShadowCard("salve");
		scn.MoveCardsToSupportArea(mellon);

		scn.StartGame();

		// A companion, a condition and an event in the Free Peoples hand; two minions in the Shadow hand.
		scn.SetFreepsHand(aragorn, salve, depart);
		scn.SetFreepsDrawDeck(avenger, onod, treebeard, barliman);
		scn.SetShadowHand(runner, pursuer);
		scn.SetShadowDrawDeck(destroyer, troop2, shadowSalve);
		scn.MoveMinionsToTable(troop);
		assertInZone(Zone.SUPPORT, mellon);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(3, scn.GetShadowDeckCount());

		scn.FreepsPass();

		// The start of the Shadow phase is not the start of the maneuver phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SUPPORT, mellon);
		assertInHand(aragorn, salve, depart, runner, pursuer);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());

		scn.ShadowPass();

		// No "may": nothing is asked. Mellon! is discarded, every card of each hand goes to its owner's discard
		// pile, and each player draws as many as he discarded from the top of his own deck.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInDiscard(mellon, aragorn, salve, depart, runner, pursuer);
		assertInHand(avenger, onod, treebeard, destroyer, troop2);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertInPlay(troop);
	}
}
