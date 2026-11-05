package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mithrandir", "4_98");
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: strength 7, vitality 4; a fellowship ability used only on purpose
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf, strength 6, vitality 4

					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2; +3 only while 2 burdens can be spotted
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; a play trigger that does nothing when cheated in
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7, vitality 1, nothing else
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MithrandirMithrandirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Mithrandir, Mithrandir!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.<br><b>Maneuver:</b> Exert Gandalf to wound each minion who has strength of 6 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mithrandir");

		assertEquals("Mithrandir, Mithrandir!", card.getBlueprint().getTitle());
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
	public void ExertsGandalfToWoundEachMinionOfStrength6OrLessAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mithrandir = scn.GetFreepsCard("mithrandir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(mithrandir);

		var axeman = scn.GetShadowCard("axeman");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(axeman, savage, runner, marksman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// No burdens: the Axeman stands at his printed 6, exactly the threshold; the Marksman is one over it.
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(marksman));
		// Companions of strength 6 or less, which are no minions.
		assertEquals(6, scn.GetStrength(avenger));
		assertTrue(scn.GetStrength(frodo) <= 6);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsPlayAvailable(mithrandir));

		// Gandalf, the one Gandalf, exerts unasked.
		scn.FreepsPlayCard(mithrandir);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// A minion of each culture and race at 6 or less is wounded; the Runner, with one vitality, dies of it.
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(marksman));
		assertInZone(Zone.SHADOW_CHARACTERS, marksman);
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(mithrandir);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionOfStrength6OrLessItIsPlayedAndOnlyGandalfExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mithrandir = scn.GetFreepsCard("mithrandir");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(mithrandir);

		var marksman = scn.GetShadowCard("marksman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(marksman, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(7, scn.GetStrength(marksman));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(mithrandir));

		scn.FreepsPlayCard(mithrandir);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(marksman));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(mithrandir);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileGandalfIsExhaustedThoughAnotherGandalfCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mithrandir = scn.GetFreepsCard("mithrandir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(mithrandir);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(gandalf, 3);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertInHand(mithrandir);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Vitality 4 with 3 wounds: Gandalf cannot exert. The Ent Avenger, a [gandalf] companion, could, but he is
		// no Gandalf; a minion of strength 5 is there to wound.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(5, scn.GetStrength(savage));
		assertFalse(scn.FreepsPlayAvailable(mithrandir));
		assertInHand(mithrandir);
	}
}
