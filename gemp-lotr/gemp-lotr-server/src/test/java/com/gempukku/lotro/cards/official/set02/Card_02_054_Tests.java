package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("darkfire", "2_54");
					put("balrog", "12_79");      // The Balrog, The Terror of Khazad-dum: his text acts only underground
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] minion with no text, no Balrog
					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: vitality 4; his text is a strength count
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a companion who is no Gandalf
					put("avenger", "6_27");      // Ent Avenger: a [gandalf] companion who is no Gandalf
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkFireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Dark Fire
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Strength: -2
		 * Game Text: To play, spot The Balrog. Plays on Gandalf.
		 * Each time the fellowship moves, Gandalf must exert.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("darkfire");

		assertEquals("Dark Fire", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(-2, card.getBlueprint().getStrength());
	}

	@Test
	public void WithTheBalrogSpottedItIsPlayedOnGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkfire = scn.GetShadowCard("darkfire");
		var balrog = scn.GetShadowCard("balrog");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(darkfire);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(darkfire));
		// Gandalf is the only bearer it takes, so it goes on him unasked; Aragorn is no Gandalf.
		scn.ShadowPlayCard(darkfire);
		assertAttachedTo(darkfire, gandalf);
		assertNotAttachedTo(darkfire, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAMoriaMinionWhoIsNotTheBalrog() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkfire = scn.GetShadowCard("darkfire");
		var troop = scn.GetShadowCard("troop");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(darkfire);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// Gandalf is there to bear it and the pool could pay: only The Balrog is missing.
		assertFalse(scn.ShadowPlayAvailable(darkfire));
	}

	@Test
	public void CannotBePlayedOnAGandalfCompanionWhoIsNotGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkfire = scn.GetShadowCard("darkfire");
		var balrog = scn.GetShadowCard("balrog");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(avenger, aragorn);
		scn.MoveCardsToHand(darkfire);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.SetTwilight(10);

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The Balrog is spotted and the pool could pay: no Gandalf to bear it.
		assertFalse(scn.ShadowPlayAvailable(darkfire));
	}

	@Test
	public void EachTimeTheFellowshipMovesGandalfExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkfire = scn.GetShadowCard("darkfire");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, darkfire);
		assertAttachedTo(darkfire, gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The move to site 2: Gandalf exerts, nobody else, and nothing is asked.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The regroup move to site 3: again.
		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnExhaustedGandalfDoesNotExertWhenTheFellowshipMoves() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkfire = scn.GetShadowCard("darkfire");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gandalf, aragorn);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, darkfire);
		// Vitality 4 with 3 wounds: exhausted.  "Must exert" then does nothing (CR 4.2 "exhausted"); a wound would kill him.
		scn.AddWoundsToChar(gandalf, 3);
		assertAttachedTo(darkfire, gandalf);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GandalfDoesNotExertWhenTheFellowshipStays()throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darkfire = scn.GetShadowCard("darkfire");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, darkfire);
		assertAttachedTo(darkfire, gandalf);
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// The fellowship stays at site 2 at the end of the regroup phase: no move, no exertion.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
