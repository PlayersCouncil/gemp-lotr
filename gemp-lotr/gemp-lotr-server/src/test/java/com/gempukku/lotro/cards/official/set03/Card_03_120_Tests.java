package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_03_120_Tests
{

	// Wastes of Emyn Muil is site 9; the rest is the usual Fellowship path.  Cards are laid after SkipToSite(8),
	// since Anduin Confluence on the way discards allies.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, vitality 3; a skirmish ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf, no archer; strength against a Nazgul only
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: an ally, no archer; a fellowship ability only

					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, vitality 2; a Shadow ability only
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, vitality 3; a regroup ability only
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, no Orc; damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
				}},
				new HashMap<>() {{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "3_120");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WastesofEmynMuilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Wastes of Emyn Muil
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Maneuver:</b> Spot 4 companions and exert your [isengard] Orc to make the Free Peoples player wound a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Wastes of Emyn Muil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ManeuverActionExertsAnIsengardOrcOfTheShadowPlayersToMakeTheFreePeoplesPlayerWoundACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var bilbo = scn.GetFreepsCard("bilbo");
		var forger = scn.GetShadowCard("forger");
		var worker = scn.GetShadowCard("worker");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		// Exactly 4 companions, of four cultures and races, and an ally.
		scn.MoveCompanionsToTable(aragorn, gimli, arwen);
		scn.MoveAlliesToTable(bilbo);
		scn.FreepsPass();

		var wastes = scn.GetCurrentSite();
		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals("Wastes of Emyn Muil", wastes.getBlueprint().getTitle());
		scn.MoveMinionsToTable(forger, worker, savage, troop);
		// Gimli, vitality 3, is exhausted.
		scn.AddWoundsToChar(gimli, 2);
		assertEquals(1, scn.GetVitality(gimli));
		scn.SkipToPhase(Phase.MANEUVER);

		// "Your [isengard] Orc": the Free Peoples player, who has none, is not offered it with the Orcs on the table.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(wastes));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(wastes));
		scn.ShadowUseCardAction(wastes);

		// Either [isengard] Orc may exert; an [isengard] Uruk-hai and a [moria] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(forger, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(forger);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(0, scn.GetWoundsOn(worker));

		// The Free Peoples player chooses: any companion, the exhausted Gimli among them; not the ally, nor a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, worker, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithThreeCompanionsAndAnAllyTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var bilbo = scn.GetFreepsCard("bilbo");
		var forger = scn.GetShadowCard("forger");

		scn.StartGame();
		scn.SkipToSite(8);
		// Frodo, Aragorn and Arwen; Bilbo is no companion.
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveAlliesToTable(bilbo);
		scn.FreepsPass();

		var wastes = scn.GetCurrentSite();
		assertEquals("Wastes of Emyn Muil", wastes.getBlueprint().getTitle());
		scn.MoveMinionsToTable(forger);
		assertEquals(0, scn.GetWoundsOn(forger));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wastes));
	}

	@Test
	public void WithHisOnlyIsengardOrcExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var arwen = scn.GetFreepsCard("arwen");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn, gimli, arwen);
		scn.FreepsPass();

		var wastes = scn.GetCurrentSite();
		assertEquals("Wastes of Emyn Muil", wastes.getBlueprint().getTitle());
		// The Forger, vitality 2, cannot exert; the Savage and the Troop, who could, are no [isengard] Orcs.
		scn.MoveMinionsToTable(forger, savage, troop);
		scn.AddWoundsToChar(forger, 1);
		assertEquals(1, scn.GetVitality(forger));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wastes));
	}
}
