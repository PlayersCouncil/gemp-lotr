package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_251_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("host", "1_251");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; adds (2) only when a weapon is played on him
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, an Orc who is not [sauron]; no text

					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer; fellowship and response abilities only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability not used
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3; a skirmish ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; his text is his own strength
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; Knight is his whole text
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, a character who is no companion; a fellowship ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AHostAvailsLittleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: A Host Avails Little
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Search</b>.<br><b>Maneuver:</b> Spot a [sauron] Orc and 6 companions to wound a companion (except the Ring-bearer). Do this once for each companion over 5.
		*/

		var scn = GetScenario();

		var host = scn.GetFreepsCard("host");

		assertEquals("A Host Avails Little", host.getBlueprint().getTitle());
		assertNull(host.getBlueprint().getSubtitle());
		assertFalse(host.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, host.getBlueprint().getSide());
		assertEquals(Culture.SAURON, host.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, host.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(host, Keyword.SEARCH));
		assertTrue(scn.HasTimeword(host, Timeword.MANEUVER));
		assertEquals(3, host.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithSixCompanionsWoundsOneCompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var soldier = scn.GetShadowCard("soldier");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(host);
		// Frodo and five more: six companions, one over 5.  The ally Grimir is no companion.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, boromir, gandalf);
		scn.MoveAlliesToTable(grimir);
		// Vitality 3 with 2 wounds: exhausted, yet a wound can still be placed on him.
		scn.AddWoundsToChar(gimli, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.IsExhausted(gimli));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(host));

		scn.ShadowPlayCard(host);
		// Every companion but the Ring-bearer, the exhausted Gimli among them; no ally, no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn, gimli, boromir, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, grimir, soldier));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		// A wound and not an exertion: it kills the exhausted Gimli.  One wound only, and nobody else is touched.
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertInDiscard(host);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSevenCompanionsWoundsTwiceAndMayWoundTheSameCompanionAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var chieftain = scn.GetShadowCard("chieftain");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var knight = scn.GetFreepsCard("knight");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(host);
		// Frodo and six more: seven companions, two over 5.  Counted with the ally, it would be three.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, boromir, gandalf, knight);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.ShadowPlayCard(host);
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn, gimli, boromir, gandalf, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, grimir, chieftain));
		scn.ShadowChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));

		// The second wound: Sam, already wounded once, may be chosen again; the Ring-bearer still may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, aragorn, gimli, boromir, gandalf, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, grimir, chieftain));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertInDiscard(host);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithFiveCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var soldier = scn.GetShadowCard("soldier");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToHand(host);
		// Frodo and four more: five companions; the ally makes six characters, but is no companion.
		scn.MoveCompanionsToTable(sam, aragorn, gimli, boromir);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		assertInHand(host);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 2 for Ettenmoors + 5 companions: the event's (3) can be paid.
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(host));
		assertInHand(host);
	}

	@Test
	public void CannotBePlayedWithSixCompanionsAndNoSauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var host = scn.GetShadowCard("host");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(host);
		scn.MoveCompanionsToTable(sam, aragorn, gimli, boromir, gandalf);

		scn.StartGame();
		// A [sauron] minion who is no Orc, and an Orc who is not [sauron].
		scn.MoveMinionsToTable(sentry, troop);
		assertInHand(host);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// 2 for Ettenmoors + 6 companions: the event's (3) can be paid.
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(host));
		assertInHand(host);
	}
}
