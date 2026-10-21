package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("slayer", "3_93");
					put("slayer2", "3_93");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound and not the Ring-bearer, vitality 4
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: Man ally, a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulSlayerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Morgul Slayer
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Regroup:</b> Exert this minion to wound a companion (except the Ring-bearer).
		*/

		var scn = GetScenario();

		var slayer = scn.GetFreepsCard("slayer");

		assertEquals("Morgul Slayer", slayer.getBlueprint().getTitle());
		assertNull(slayer.getBlueprint().getSubtitle());
		assertFalse(slayer.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, slayer.getBlueprint().getSide());
		assertEquals(Culture.SAURON, slayer.getBlueprint().getCulture());
		assertEquals(CardType.MINION, slayer.getBlueprint().getCardType());
		assertEquals(Race.ORC, slayer.getBlueprint().getRace());
		assertEquals(2, slayer.getBlueprint().getTwilightCost());
		assertEquals(7, slayer.getBlueprint().getStrength());
		assertEquals(2, slayer.getBlueprint().getVitality());
		assertEquals(6, slayer.getBlueprint().getSiteNumber());
	}

	@Test
	public void RegroupAbilityExertsThisMinionToWoundACompanionWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(albert);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, albert);
		scn.MoveMinionsToTable(slayer, slayer2);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(slayer2));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertTrue(scn.ShadowActionAvailable(slayer));

		scn.ShadowUseCardAction(slayer);
		// This copy exerts, and not the other.
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(slayer2));

		// Companions of three cultures and races, Ring-bound Sam among them; not Frodo the Ring-bearer, nor an ally.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, albert));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedCompanionIsOfferedAndTheWoundKillsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		// Sam, vitality 4, with 3 wounds.
		scn.AddWoundsToChar(sam, 3);
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertTrue(scn.ShadowActionAvailable(slayer));

		scn.ShadowUseCardAction(slayer);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		scn.ShadowChooseCard(sam);

		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var slayer2 = scn.GetShadowCard("slayer2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, slayer2);
		// Vitality 2 with 1 wound: exhausted.  The other copy is fresh.
		scn.AddWoundsToChar(slayer, 1);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(slayer2));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.IsExhausted(slayer));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));
		assertTrue(scn.ShadowActionAvailable(slayer2));
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(slayer));

		scn.ShadowPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(slayer));
	}

	@Test
	public void WithOnlyTheRingBearerAndAnAllyItIsOfferedAndTheMinionExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var slayer = scn.GetShadowCard("slayer");
		var frodo = scn.GetRingBearer();
		var albert = scn.GetFreepsCard("albert");
		scn.MoveAlliesToTable(albert);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, albert);
		scn.MoveMinionsToTable(slayer);
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertTrue(scn.ShadowActionAvailable(slayer));

		scn.ShadowUseCardAction(slayer);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(albert));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
