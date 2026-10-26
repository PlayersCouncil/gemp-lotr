package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brand", "14_5");
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally; a maneuver ability only
					put("dasron", "13_29");     // Dasron, Merchant From Dorwinion: [gandalf] Man companion; needs culture tokens to act
					put("erland", "2_21");      // Erland, Advisor to Brand: [gandalf] Man ally; bars Shadow looks at the hand
					put("avenger", "6_27");     // Ent Avenger: [gandalf] Ent companion, no Man; +2 only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, damage +1 only
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, adds (2) as it is played
					put("runner2", "1_178");
					put("poison", "8_28");      // Spider Poison: [gollum] possession in the support area, no minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Brand, King of Dale
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Each of your Dwarves and other [gandalf] Men is strength +1.<br>While you can spot more minions than companions, the fellowship archery total is +X, where X is the number of minions minus the number of companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brand");

		assertEquals("Brand", card.getBlueprint().getTitle());
		assertEquals("King of Dale", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void EachDwarfAndEachOtherGandalfManIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var dasron = scn.GetFreepsCard("dasron");
		var erland = scn.GetFreepsCard("erland");
		var avenger = scn.GetFreepsCard("avenger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gimli, dasron, avenger, aragorn);
		scn.MoveAlliesToTable(thrarin, erland);

		scn.StartGame();

		// Without Brand: every printed strength (Frodo 3, +1 for The Ruling Ring).
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(thrarin));
		assertEquals(6, scn.GetStrength(dasron));
		assertEquals(5, scn.GetStrength(erland));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));

		scn.MoveCompanionsToTable(brand);

		// A Dwarf companion and a Dwarf ally; a [gandalf] Man companion and a [gandalf] Man ally.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(thrarin));
		assertEquals(7, scn.GetStrength(dasron));
		assertEquals(6, scn.GetStrength(erland));
		// Not Brand himself ("other"), a [gandalf] companion who is no Man, a Man of another culture, or a Hobbit.
		assertEquals(6, scn.GetStrength(brand));
		assertEquals(6, scn.GetStrength(avenger));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipArcheryTotalIsPlusTheMinionsOverTheCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brand = scn.GetFreepsCard("brand");
		var thrarin = scn.GetFreepsCard("thrarin");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(brand);
		// An ally, who is no companion: counted among the companions, he would take one off each count below.
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(runner1, runner2);

		scn.StartGame();
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var poison = scn.GetShadowCard("poison");
		// Two minions against two companions (Brand and Frodo), and a Shadow card in play that is no minion.
		scn.MoveMinionsToTable(troop, savage);
		scn.MoveCardsToSupportArea(poison);
		assertInZone(Zone.SUPPORT, poison);
		// No archer is on the table: the whole total is Brand's.
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Three against two.
		scn.ShadowPlayCard(runner1);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// Four against two.
		scn.ShadowPlayCard(runner2);
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
