package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("quick", "4_53");
					put("quick2", "4_53");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; his skirmish ability is used only on purpose
					put("guard", "1_7");        // Dwarf Guard: a Dwarf companion of another title, no text in play
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, a fellowship ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, no archer
					// Legolas, Companion of the Ring: an Elf companion of another title, no archer; +2 at a battleground or
					// forest, which no site of this path is.
					put("legolas", "11_21");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, no archer; a fellowship ability only

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so exhausted as it stands
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void QuickAsMayBeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Quick As May Be
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot a Dwarf companion and an Elf companion to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("quick");

		assertEquals("Quick As May Be", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingADwarfCompanionAndAnElfCompanionWoundsAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quick = scn.GetFreepsCard("quick");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var grimir = scn.GetFreepsCard("grimir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(quick);
		scn.MoveCompanionsToTable(gimli, arwen);
		scn.MoveAlliesToTable(grimir, celeborn);

		scn.StartGame();
		assertInHand(quick);
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsPlayAvailable(quick));

		scn.FreepsPlayCard(quick);
		// Minions of two cultures and races; no companion or ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, arwen, frodo, grimir, celeborn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		// One wound, on the minion chosen; the spotted companions are not exerted.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInDiscard(quick);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnExhaustedMinionIsWoundedAndKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quick = scn.GetFreepsCard("quick");
		var guard = scn.GetFreepsCard("guard");
		var legolas = scn.GetFreepsCard("legolas");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(quick);
		// A Dwarf companion and an Elf companion of other titles than in the test above.
		scn.MoveCompanionsToTable(guard, legolas);

		scn.StartGame();
		assertInHand(quick);
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsPlayAvailable(quick));

		scn.FreepsPlayCard(quick);
		// The Runner, vitality 1, is exhausted and still offered: a wound, not an exertion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, savage));
		scn.FreepsChooseCard(runner);

		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(quick);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWhenTheOnlyDwarfIsAnAllyAndPlayableOnceADwarfCompanionIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quick = scn.GetFreepsCard("quick");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(quick);
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		assertInHand(quick);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// An Elf companion, and a Dwarf who is an ally.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(quick));

		scn.MoveCompanionsToTable(gimli);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(quick));
	}

	@Test
	public void NotPlayableWhenTheOnlyElfIsAnAllyAndPlayableOnceAnElfCompanionIsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quick = scn.GetFreepsCard("quick");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(quick);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(celeborn);

		scn.StartGame();
		assertInHand(quick);
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// A Dwarf companion, and an Elf who is an ally.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(quick));

		scn.MoveCompanionsToTable(arwen);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(quick));
	}
	@Test
	public void WithNoMinionLeftInPlayItIsStillPlayedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var quick = scn.GetFreepsCard("quick");
		var quick2 = scn.GetFreepsCard("quick2");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(quick, quick2);
		scn.MoveCompanionsToTable(gimli, arwen);

		scn.StartGame();
		assertInHand(quick, quick2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// The first copy kills the only minion, the Runner (vitality 1), chosen unasked.
		scn.FreepsPlayCard(quick);
		assertInDiscard(runner);
		scn.ShadowPass();

		// The maneuver phase goes on with no minion in play. The minion is the event's target, not a requirement.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(quick2));
		scn.FreepsPlayCard(quick2);
		assertInDiscard(quick2);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(scn.GetRingBearer()));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
