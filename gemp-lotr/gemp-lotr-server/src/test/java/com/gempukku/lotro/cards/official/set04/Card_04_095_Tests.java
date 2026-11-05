package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tunnels", "4_95");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a strength bonus and nothing else
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf, vitality 4
					// Mysterious Wizard: a [gandalf] Skirmish event with the Spell keyword, "Make Gandalf strength +2
					// (or +4 if there are 4 or fewer burdens on the Ring-bearer)"
					put("wizard", "1_78");
					// Let Folly Be Our Cloak: a [gandalf] Fellowship event with no Spell keyword, "Exert X
					// companions to remove (X)"
					put("folly", "1_77");
					put("glamdring", "1_75");   // Glamdring: a [gandalf] possession, no event; bearer must be Gandalf
					put("deftness", "1_293");   // Halfling Deftness: a [shire] Skirmish event, "Exert a Hobbit to make him strength +3"
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IntoDarkTunnelsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Into Dark Tunnels
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Spell</b>.<br><b>Response:</b> If you play a [gandalf] event, exert Gandalf twice to place that event in your hand instead of your discard pile.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tunnels");

		assertEquals("Into Dark Tunnels", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfTwiceToReturnASkirmishSpellToHandButNotAnEventOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tunnels = scn.GetFreepsCard("tunnels");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wizard = scn.GetFreepsCard("wizard");
		var deftness = scn.GetFreepsCard("deftness");
		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(tunnels, wizard, deftness);

		scn.StartGame();
		assertInHand(tunnels, wizard, deftness);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		assertInHand(tunnels, wizard, deftness);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPlayCard(deftness);
		// A [shire] event: its own cost is paid (Frodo, the only Hobbit, exerts) and nothing is offered.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tunnels));
		assertInDiscard(deftness);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// 6, +1 for a Hobbit in the fellowship
		assertEquals(7, scn.GetStrength(gandalf));
		scn.FreepsPlayCard(wizard);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tunnels));
		scn.FreepsPlayCard(tunnels);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInDiscard(tunnels);
		// The spell is carried out (+4, no burdens), and then goes to the hand and not the discard pile.
		assertEquals(11, scn.GetStrength(gandalf));
		assertInHand(wizard);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ReturnsAFellowshipEventThatIsNoSpellButNotAGandalfPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tunnels = scn.GetFreepsCard("tunnels");
		var gandalf = scn.GetFreepsCard("gandalf");
		var folly = scn.GetFreepsCard("folly");
		var glamdring = scn.GetFreepsCard("glamdring");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(tunnels, folly, glamdring);

		scn.StartGame();

		assertInHand(tunnels, folly, glamdring);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(glamdring);
		// A [gandalf] card played that is no event: nothing is offered.
		assertAttachedTo(glamdring, gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tunnels));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPlayCard(folly);
		scn.FreepsChooseCards(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tunnels));

		scn.FreepsPlayCard(tunnels);
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertInDiscard(tunnels);
		// 3 + 3 for Into Dark Tunnels - 1 for the one companion exerted
		assertEquals(5, scn.GetTwilight());
		assertInHand(folly);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenGandalfCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tunnels = scn.GetFreepsCard("tunnels");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var folly = scn.GetFreepsCard("folly");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(tunnels, folly);
		// Vitality 4 with 2 wounds: 2 left, so he can exert once but not twice.
		scn.AddWoundsToChar(gandalf, 2);

		scn.StartGame();
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInHand(tunnels, folly);
		scn.FreepsPlayCard(folly);
		scn.FreepsChooseCards(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		// Ent Avenger, a [gandalf] companion who could exert twice, is no Gandalf.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tunnels));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertInDiscard(folly);
		assertInHand(tunnels);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
