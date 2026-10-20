package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_123_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("caradhras", "1_123");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, vitality 3; a skirmish ability
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, vitality 3; a skirmish ability
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, vitality 4
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, vitality 2, home site 1; a fellowship ability

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability only ever used on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai, vitality 2; damage +1 and nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CaradhrasHasNotForgivenUsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Caradhras Has Not Forgiven Us
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>. <b>Weather</b>.<br><b>Maneuver:</b> Exert an [isengard] minion and spot 5 companions to discard an exhausted companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("caradhras");

		assertEquals("Caradhras Has Not Forgiven Us", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasKeyword(card, Keyword.WEATHER));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFiveCompanionsExertsAnIsengardMinionToDiscardAnExhaustedCompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caradhras = scn.GetShadowCard("caradhras");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var savage = scn.GetShadowCard("savage");
		var servant = scn.GetShadowCard("servant");
		var guard = scn.GetShadowCard("guard");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		// Frodo and four more: exactly five companions.
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(caradhras);

		scn.StartGame();
		// Exhausted: Frodo (4 vitality, 3 wounds), Gimli and Boromir (3 vitality, 2 wounds), Barliman and the
		// Sentry (2 vitality, 1 wound).  Aragorn (4 vitality, 2 wounds) has 2 left and is not exhausted.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(barliman, 1);
		scn.MoveMinionsToTable(savage, servant, guard, sentry, troop);
		scn.AddWoundsToChar(sentry, 1);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetFreepsDeadCount());
		assertTrue(scn.ShadowPlayAvailable(caradhras));

		scn.ShadowPlayCard(caradhras);
		// [isengard] minions of two races; not a [sauron] Uruk-hai or a [moria] Orc, each able to exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(servant);

		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Exhausted companions of two cultures and races; not the exhausted Ring-bearer, a companion who is
		// wounded but not exhausted, an exhausted ally or an exhausted minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, barliman, sentry));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(boromir);

		// Discarded, not killed.
		assertInDiscard(boromir, caradhras);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertInPlay(frodo, aragorn, gimli, sam, barliman, sentry);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithFourCompanionsAndAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caradhras = scn.GetShadowCard("caradhras");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var savage = scn.GetShadowCard("savage");
		// Frodo and three more: four companions, and an ally beside them who is a character but no companion.
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(caradhras);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(caradhras));

		// A fifth companion.
		scn.MoveCompanionsToTable(boromir);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(caradhras));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInPlay(gimli);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caradhras = scn.GetShadowCard("caradhras");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveCardsToHand(caradhras);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(savage, troop);
		scn.AddWoundsToChar(savage, 1);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: Uruk Savage can still exert.
		assertTrue(scn.ShadowPlayAvailable(caradhras));

		scn.AddWoundsToChar(savage, 1);
		scn.ShadowRefreshDecision();

		// Vitality 3 with 2 wounds: exhausted.  The [moria] minion beside it could exert.
		assertFalse(scn.ShadowPlayAvailable(caradhras));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInPlay(gimli);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoExhaustedCompanionButTheRingBearerItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var caradhras = scn.GetShadowCard("caradhras");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir, sam);
		scn.MoveCardsToHand(caradhras);

		scn.StartGame();
		// Frodo exhausted; Aragorn wounded with 2 vitality left.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(aragorn, 2);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.ShadowPlayAvailable(caradhras));

		scn.ShadowPlayCard(caradhras);

		// The one [isengard] minion pays; nothing can be discarded.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(caradhras);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertInPlay(frodo, aragorn, gimli, boromir, sam);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
