package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sam, Son of Hamfast: Ring-bound [shire] Hobbit, strength 3, vitality 4.
					// "Fellowship: Exert Sam to remove a burden."
					put("sam", "1_311");
					// Light Shining Faintly: Fellowship event, cost 0.  "Add a burden to heal a Ring-bound
					// companion twice."  With nobody wounded it adds the burden and does nothing else.
					put("light", "4_309");
				}},
				VirtualTableScenario.FellowshipSites,
				"103_110",                          // Frodo, Bereft of Hope is the Ring-bearer
				VirtualTableScenario.RulingRing     // strength +1 to its bearer
		);
	}

	protected VirtualTableScenario GetGaladrielRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frodo", "103_110");
					put("sam", "1_311");        // Sam, Son of Hamfast: a companion of cost 2, never used here
				}},
				VirtualTableScenario.FellowshipSites,
				// Galadriel, Bearer of Wisdom: her only text changes her own resistance
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Frodo, Bereft of Hope
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: Ring-bearer (resistance 7).  Ring-bound.
		 * 	Frodo is strength +1 for each burden.
		 */

		var scn = GetScenario();

		var frodo = scn.GetRingBearer();

		assertEquals("Frodo", frodo.getBlueprint().getTitle());
		assertEquals("Bereft of Hope", frodo.getBlueprint().getSubtitle());
		assertTrue(frodo.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, frodo.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, frodo.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, frodo.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, frodo.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(frodo, Keyword.RING_BOUND));
		assertEquals(0, frodo.getBlueprint().getTwilightCost());
		assertEquals(2, frodo.getBlueprint().getStrength());
		assertEquals(4, frodo.getBlueprint().getVitality());
		// Ring-bearer (resistance 7): he may begin the game bearing the Ring, at resistance 7.
		assertEquals(7, frodo.getBlueprint().getResistance());
		assertTrue(frodo.getBlueprint().canStartWithRing());
	}

	@Test
	public void FrodoIsStrengthPlusOneForEachBurdenAndSamBesideHimIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var sam = scn.GetFreepsCard("sam");
		var light = scn.GetFreepsCard("light");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(light);

		scn.StartGame();

		// He began the game as the Ring-bearer, which is the only way this card comes into play.
		assertEquals("Bereft of Hope", frodo.getBlueprint().getSubtitle());
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertAttachedTo(ring, frodo);
		assertEquals(7, scn.GetResistance(frodo));

		// No burden: 2 printed + 1 for the Ring.
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));

		// A burden added by play.  Nobody is wounded, so the event's healing does nothing.
		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);
		assertEquals(1, scn.GetBurdens());
		// 2 printed + 1 for the Ring + 1 burden
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));

		scn.AddBurdens(2);
		assertEquals(3, scn.GetBurdens());
		// 2 printed + 1 for the Ring + 3 burdens
		assertEquals(6, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));

		// A wound on Frodo and threats are not burdens, and add nothing.
		scn.AddWoundsToChar(frodo, 1);
		scn.AddThreats(2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetThreats());
		assertEquals(6, scn.GetStrength(frodo));

		// A burden removed by play: Sam exerts to remove one.
		assertTrue(scn.FreepsActionAvailable(sam));
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetBurdens());
		// 2 printed + 1 for the Ring + 2 burdens
		assertEquals(5, scn.GetStrength(frodo));
		// Sam is a Ring-bound [shire] Hobbit too, and no burden has ever made him stronger.
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedFromHandWhileAnotherCharacterIsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielRingBearerScenario();

		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToHand(frodo, sam);

		scn.StartGame();

		assertEquals("Galadriel", scn.GetRingBearer().getBlueprint().getTitle());
		assertInHand(frodo, sam);
		// Another companion in the same hand may be played; this Frodo, who costs nothing, may not.
		assertTrue(scn.FreepsPlayAvailable(sam));
		assertFalse(scn.FreepsPlayAvailable(frodo));

		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertFalse(scn.FreepsPlayAvailable(frodo));
		assertInHand(frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
