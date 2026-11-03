package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "5_18");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; a strength bonus only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man for Defend It and Hope; a maneuver ability used only on purpose
					put("defend", "4_115");     // Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion once (twice an Uruk-hai)

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("scout", "1_191");      // Moria Scout: [moria] Orc, vitality 2; its play text is not reached cheated in
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its play text is not reached cheated in
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FuryoftheWhiteRiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Fury of the White Rider
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.<br><b>Maneuver:</b> Spot 3 twilight tokens and exert Gandalf to wound a minion twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury");

		assertEquals("Fury of the White Rider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithThreeTwilightTokensItExertsGandalfToWoundAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fury = scn.GetFreepsCard("fury");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fury);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		// Exactly the 3 the text asks to spot.
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, fury);
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(fury));
		scn.FreepsPlayCard(fury);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		// Minions of two cultures and races are offered, and one with a single vitality left too (a wound, not an
		// exertion); the companions are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Two wounds on the one minion chosen.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage, runner);
		assertInDiscard(fury);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TwoWoundsKillAMinionOfVitalityTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fury);

		var troop = scn.GetShadowCard("troop");
		var scout = scn.GetShadowCard("scout");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, scout);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, fury);
		assertEquals(0, scn.GetWoundsOn(scout));
		scn.FreepsPlayCard(fury);
		scn.FreepsChooseCard(scout);

		// Exerting twice would have needed 3 vitality; two wounds kill him.
		assertInDiscard(scout);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithTwoTwilightTokensItCannotBePlayedUntilAThirdIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(fury);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// One short; the event's own cost of 3, added as it is played, does not count toward it.
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, fury);
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fury));

		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(fury));
	}

	@Test
	public void WithGandalfExhaustedItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(fury);
		// Vitality 4: exhausted.  The [gandalf] Ent beside him could exert, but is no Gandalf.
		scn.AddWoundsToChar(gandalf, 3);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, fury);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(fury));
	}

	@Test
	public void WithNoMinionLeftItIsStillPlayedAndOnlyExertsGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(fury, defend);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.HAND, fury, defend);
		// The only minion is killed first, by another maneuver event (cost 0).
		scn.FreepsPlayCard(defend);
		assertInDiscard(runner);
		scn.ShadowPass();

		assertInZone(Zone.HAND, fury);
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(fury));
		scn.FreepsPlayCard(fury);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(fury);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
