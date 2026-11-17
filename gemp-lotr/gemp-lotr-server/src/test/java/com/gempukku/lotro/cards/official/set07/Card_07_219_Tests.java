package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_07_219_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toldea", "7_219");
					put("nelya", "7_216");      // Úlairë Nelya, Black-Mantled Wraith: a [wraith] Nazgûl; a regroup ability used only on purpose
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl; his text needs 3 burdens
					put("nertea", "7_217");     // Úlairë Nertëa, Black-Mantled Wraith: a second Nazgûl, cost 4, kept in hand
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc, no Nazgûl; its text needs 2 wounds on the Ring-bearer
					// Chaotic Clash: "Each time you play a minion, it is twilight cost +1", so that Toldëa costs 7 and a discount
					// of 6 reads 1, where one of 7 would read 0.  Its strength text needs an [orc] minion, and there is none.
					put("clash", "17_68");
					put("stairs", "1_24");      // Stairs of Khazad-dûm: a [dwarven] condition of cost 0, played to take the hand from 4 to 3
					put("hand1", "1_13");
					put("hand2", "1_13");
					put("hand3", "1_13");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireToldeaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Toldëa, Wraith on Wings
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>While you have initiative and can spot a Nazgûl, Úlairë Toldëa's twilight cost is -6.
		*/

		var scn = GetScenario();

		var toldea = scn.GetFreepsCard("toldea");

		assertEquals("Úlairë Toldëa", toldea.getBlueprint().getTitle());
		assertEquals("Wraith on Wings", toldea.getBlueprint().getSubtitle());
		assertTrue(toldea.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, toldea.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, toldea.getBlueprint().getCulture());
		assertEquals(CardType.MINION, toldea.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, toldea.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(toldea, Keyword.FIERCE));
		assertEquals(6, toldea.getBlueprint().getTwilightCost());
		assertEquals(12, toldea.getBlueprint().getStrength());
		assertEquals(3, toldea.getBlueprint().getVitality());
		assertEquals(3, toldea.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithANazgulInPlayHerCostIsSixLessOnceTheShadowHasInitiative() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var nertea = scn.GetShadowCard("nertea");
		var nelya = scn.GetShadowCard("nelya");
		var stairs = scn.GetFreepsCard("stairs");
		scn.MoveCardsToHand(stairs, scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));
		scn.MoveCardsToSupportArea(scn.GetShadowCard("clash"));

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.MoveCardsToHand(toldea, nertea);
		scn.FreepsRefreshDecision();

		// Four cards in the Free Peoples hand: he has initiative, and Toldëa costs her 6 + 1 for the Clash.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertEquals(7, scn.GetTwilightCost(toldea));
		assertEquals(5, scn.GetTwilightCost(nertea));

		// The hand goes from 4 to 3: the Shadow has initiative, and she is 7 - 6.
		scn.FreepsPlayCard(stairs);
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(1, scn.GetTwilightCost(toldea));
		// Her text is about her own cost only: the other Nazgûl in hand costs his 4 + 1.
		assertEquals(5, scn.GetTwilightCost(nertea));

		scn.SetTwilight(2);
		scn.FreepsPass();
		// 2 set + 1 companion + 2 for Ettenmoors.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(toldea);
		// 1 for her (7 - 6) + 2 roaming (a site 3 minion at site 2).
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, toldea);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheShadowsInitiativeSheCostsHerFullNumberUntilANazgulIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var toldea = scn.GetShadowCard("toldea");
		var nertea = scn.GetShadowCard("nertea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToSupportArea(scn.GetShadowCard("clash"));

		scn.StartGame();
		// A [wraith] minion who is no Nazgûl in play, and a Nazgûl in hand, who is not spotted there.
		scn.MoveMinionsToTable(hound);
		scn.MoveCardsToHand(toldea, nertea);
		scn.FreepsPass();

		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(7, scn.GetTwilightCost(toldea));

		// A Nazgûl of another culture than hers.
		scn.MoveMinionsToTable(khamul);
		assertEquals(1, scn.GetTwilightCost(toldea));
		assertEquals(5, scn.GetTwilightCost(nertea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
