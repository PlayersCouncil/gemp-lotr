package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_30_010_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 ([shire] Hobbit) as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fili", "30_10");
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion; a fellowship ability only
					// Dain Ironfoot, Lord of the Iron Hills: [dwarven] Dwarf ally; a bonus for Thorin and a regroup offer
					// that wants a [dwarven] follower to discard
					put("dain", "32_2");

					// Orkish Pursuer: "Regroup: If this minion is not mounted, exert him to make the Free Peoples
					// player wound a companion." (and a doubt when he wins a skirmish, which no test reaches)
					put("pursuer1", "32_42");
					put("pursuer2", "32_42");
					put("assassin", "2_93");    // Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void FiliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Fili, Brother of Kili
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Damage +1</b>.
		 * 	<b>Response:</b> If a [dwarven] companion is about to take a wound, exert Fili and add (1) to prevent that wound.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fili");

		assertEquals("Fili", card.getBlueprint().getTitle());
		assertEquals("Brother of Kili", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void ExertsAndAddsOneToPreventAWoundToADwarvenCompanionHimselfIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var fili = scn.GetFreepsCard("fili");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(fili, balin);

		var pursuer1 = scn.GetShadowCard("pursuer1");
		var pursuer2 = scn.GetShadowCard("pursuer2");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer1, pursuer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.ShadowUseCardAction(pursuer1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(fili, balin, bilbo));
		scn.FreepsChooseCard(balin);

		// Another [dwarven] companion.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fili));
		scn.FreepsChooseOptionalTrigger(fili);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(1, scn.GetWoundsOn(fili));
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Fili himself: the exertion is no wound, and the wound is prevented.
		scn.FreepsPass();
		scn.ShadowUseCardAction(pursuer2);
		scn.FreepsChooseCard(fili);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fili));
		scn.FreepsChooseOptionalTrigger(fili);
		assertEquals(2, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileFiliIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fili = scn.GetFreepsCard("fili");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(fili, balin);

		var pursuer1 = scn.GetShadowCard("pursuer1");

		scn.StartGame();
		scn.AddWoundsToChar(fili, 2);
		assertEquals(2, scn.GetWoundsOn(fili));
		scn.MoveMinionsToTable(pursuer1);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		// Vitality 3 with 2 wounds: he cannot exert.
		assertEquals(2, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(balin));
		scn.ShadowUseCardAction(pursuer1);
		scn.FreepsChooseCard(balin);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fili));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(fili));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAWoundToADwarvenAllyOrToACompanionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var fili = scn.GetFreepsCard("fili");
		var balin = scn.GetFreepsCard("balin");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(fili, balin);
		scn.MoveAlliesToTable(dain);

		var assassin = scn.GetShadowCard("assassin");
		var pursuer1 = scn.GetShadowCard("pursuer1");

		scn.StartGame();
		scn.MoveMinionsToTable(assassin, pursuer1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [dwarven] ally: Dain is the only ally, so the Tower Assassin wounds him.
		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(fili));
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(dain));
		assertEquals(0, scn.GetWoundsOn(fili));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A companion of another culture: Bilbo, the [shire] Ring-bearer.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(bilbo));
		scn.ShadowUseCardAction(pursuer1);
		scn.FreepsChooseCard(bilbo);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fili));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(fili));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
