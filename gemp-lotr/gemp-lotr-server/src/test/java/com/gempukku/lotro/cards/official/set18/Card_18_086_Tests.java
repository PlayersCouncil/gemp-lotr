package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "18_86");
					put("goblin", "11_137");    // Scurrying Goblin: an [orc] Orc minion, cost 1; adds (2) only at an underground site
					put("shadow", "12_83");     // The Beckoning Shadow: an [orc] support-area condition; its text wants 5 burdens
					put("patrol", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion with no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no hunter
					put("dunedain", "15_59");   // Dúnedain of the South: a [gondor] Man companion, hunter 1; his trigger is on a win
					put("gimli", "15_5");       // Gimli, Eager Hunter: a [dwarven] Dwarf companion, hunter 3 and damage +1 only
					put("tracker", "15_171");   // Tracking Uruk: an [uruk-hai] minion, hunter 1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishArcherTroopStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Archer Troop
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: To play, spot an [orc] card.
		 * The minion archery total is +1 for each hunter companion you can spot.
		*/

		var scn = GetScenario();

		var troop = scn.GetFreepsCard("troop");

		assertEquals("Orkish Archer Troop", troop.getBlueprint().getTitle());
		assertNull(troop.getBlueprint().getSubtitle());
		assertFalse(troop.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, troop.getBlueprint().getSide());
		assertEquals(Culture.ORC, troop.getBlueprint().getCulture());
		assertEquals(CardType.MINION, troop.getBlueprint().getCardType());
		assertEquals(Race.ORC, troop.getBlueprint().getRace());
		assertEquals(5, troop.getBlueprint().getTwilightCost());
		assertEquals(10, troop.getBlueprint().getStrength());
		assertEquals(3, troop.getBlueprint().getVitality());
		assertEquals(4, troop.getBlueprint().getSiteNumber());
	}

	@Test
	public void CannotBePlayedUntilAnOrcMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var goblin = scn.GetShadowCard("goblin");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCardsToHand(troop, goblin);
		assertInHand(troop);
		assertInHand(goblin);

		scn.StartGame();
		// An Orc of another culture in play.
		scn.MoveMinionsToTable(patrol);
		// 7 set + 1 companion + 2 for Ettenmoors = 10: the Troop (5 + 2 roaming) and the Goblin (1 + 2 roaming) both.
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		// The [moria] Orc is not spotted, nor the [orc] Goblin still in hand.
		assertInHand(troop);
		assertInHand(goblin);
		assertFalse(scn.ShadowPlayAvailable(troop));

		scn.ShadowPlayCard(goblin);
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);

		assertTrue(scn.ShadowPlayAvailable(troop));
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CanBePlayedSpottingAnOrcConditionInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var shadow = scn.GetShadowCard("shadow");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.MoveMinionsToTable(patrol);
		scn.MoveCardsToSupportArea(shadow);
		assertInZone(Zone.SUPPORT, shadow);
		// 4 set + 1 companion + 2 for Ettenmoors = 7: the Troop's 5 + 2 roaming.
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(troop));
		scn.ShadowPlayCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MinionArcheryTotalIsPlus1ForEachHunterCompanionAndForNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var tracker = scn.GetShadowCard("tracker");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dunedain = scn.GetFreepsCard("dunedain");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(dunedain);

		scn.StartGame();
		assertEquals(0, scn.GetShadowArcheryTotal());
		// Laid early, in the fellowship phase (Q-0228), to be read as the fellowship grows.
		scn.MoveMinionsToTable(troop, tracker);
		scn.FreepsRefreshDecision();

		// A companion who is no hunter and a hunter who is no companion: nothing.
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(dunedain);
		assertInZone(Zone.FREE_CHARACTERS, dunedain);
		assertEquals(1, scn.GetShadowArcheryTotal());

		// Gimli is hunter 3, and counts once.
		scn.MoveCompanionsToTable(gimli);
		assertEquals(2, scn.GetShadowArcheryTotal());
		// The fellowship's total is not the one raised.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
