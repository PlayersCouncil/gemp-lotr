package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_153_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grima", "54_153");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] companion, strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: unbound [dwarven] companion, strength 6
					put("legolas", "1_50");     // Legolas, Greenleaf: unbound [elven] companion, strength 6
					put("boromir", "1_97");     // Boromir, Son of Denethor: unbound [gondor] companion, strength 7
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion, strength 3
					put("maggot", "1_288");     // Farmer Maggot: [shire] ally, strength 2; a fellowship ability only

					// Shadow conditions, none of which changes a strength.
					put("weary", "4_212");      // Weary: [isengard] condition, plays on a companion or ally
					put("weary2", "4_212");
					put("weary3", "4_212");
					put("evil", "1_283");       // You Bring Great Evil: [sauron] condition, plays on a companion (except an Elf)
					put("evil2", "1_283");

					put("poison", "8_28");      // Spider Poison: a Shadow possession, not a condition
					put("saga", "1_114");       // The Saga of Elendil: a Free Peoples condition on a [gondor] companion (vitality +1)
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc minion, strength 8; a Shadow ability only
					put("enraged", "1_254");    // Mordor Enraged: Shadow condition that plays on a [sauron] minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrimaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Grima, Son of Galmod
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: Lurker.
		 * Each unbound companion (or ally) bearing a Shadow condition is strength -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grima");

		assertEquals("Grima", card.getBlueprint().getTitle());
		assertEquals("Son of Galmod", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.LURKER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedForTwoPlusRoamingAndThenACompanionBearingAShadowConditionIsWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var weary = scn.GetShadowCard("weary");

		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, weary);
		scn.MoveCardsToHand(grima);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 3 companions + 2 for Ettenmoors
		assertEquals(15, scn.GetTwilight());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.ShadowPlayAvailable(grima));

		scn.ShadowPlayCard(grima);
		// 2 to play + 2 roaming (site 3 minion at site 2)
		assertEquals(11, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, grima);
		assertEquals(7, scn.GetStrength(aragorn));
		// Legolas bears nothing.
		assertEquals(6, scn.GetStrength(legolas));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachUnboundCompanionOrAllyBearingAShadowConditionIsStrengthMinus1AndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grima = scn.GetShadowCard("grima");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var taskmaster = scn.GetShadowCard("taskmaster");

		scn.MoveCompanionsToTable(aragorn, gimli, legolas, boromir, sam);
		scn.MoveAlliesToTable(maggot);
		// Aragorn bears two Shadow conditions, of two cultures.
		scn.AttachCardsTo(aragorn, scn.GetShadowCard("weary"), scn.GetShadowCard("evil"));
		scn.AttachCardsTo(gimli, scn.GetShadowCard("evil2"));
		scn.AttachCardsTo(maggot, scn.GetShadowCard("weary2"));
		// Sam is Ring-bound.
		scn.AttachCardsTo(sam, scn.GetShadowCard("weary3"));
		// A Shadow possession, not a condition.
		scn.AttachCardsTo(legolas, scn.GetShadowCard("poison"));
		// A Free Peoples condition.
		scn.AttachCardsTo(boromir, scn.GetFreepsCard("saga"));

		scn.StartGame();
		// A minion bearing a Shadow condition.
		scn.MoveMinionsToTable(taskmaster);
		scn.AttachCardsTo(taskmaster, scn.GetShadowCard("enraged"));

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(2, scn.GetStrength(maggot));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(taskmaster));

		scn.MoveMinionsToTable(grima);

		// -1 once, though Aragorn bears two.
		assertEquals(7, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(gimli));
		assertEquals(1, scn.GetStrength(maggot));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(6, scn.GetStrength(legolas));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(taskmaster));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
