package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("soldier", "15_138");
					put("soldier2", "15_138");  // a second copy: not unique
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6; Valiant is his whole text
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, cost 1; a fellowship ability used only on purpose
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or Eowyn can be spotted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, cost 4; a maneuver ability used only on purpose
					// Faramir, Captain of Ithilien: [gondor] Man companion, ranger, hunter 2 (a hunter who is no minion); a
					// maneuver heal used only on purpose
					put("faramir", "18_48");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, no hunter; Damage +1 is its whole text
					put("butcher", "17_122");   // White Hand Butcher: [uruk-hai] Uruk-hai, cost 4; Hunter 6 is its whole text
					// Hunting Herdsman: [men] Man, cost 3, hunter 1; +3 for each follower you can spot, and there is none
					put("herdsman", "15_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimSoldierStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Rohirrim Soldier
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Valiant</b>. While you can spot a [rohan] Man, this companion's twilight cost is -1.<br>This companion is strength +1 for each hunter minion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("soldier");

		assertEquals("Rohirrim Soldier", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void WithARohanManCompanionToSpotHeIsPlayedForTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(soldier, aragorn);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();

		assertEquals(2, scn.GetTwilightCost(soldier));    // 3 - 1
		// Another companion in the hand, beside the [rohan] Man he spots, costs what it is printed at.
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(soldier);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, soldier);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanHobbitAndAGondorManDoNotLowerHisCostAndARohanManAllyPlayedThenDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(soldier, guma);
		scn.MoveCompanionsToTable(merry, aragorn);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertEquals(3, scn.GetTwilightCost(soldier));

		scn.FreepsPlayCard(guma);
		assertInZone(Zone.SUPPORT, guma);
		assertEquals(2, scn.GetTwilightCost(soldier));    // 3 - 1
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(soldier);

		assertEquals(pool + 2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, soldier);
		// Two [rohan] companions: Merry is still no Man.
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StrengthPlus1ForEachHunterMinionAndForNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var soldier = scn.GetFreepsCard("soldier");
		var soldier2 = scn.GetFreepsCard("soldier2");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(soldier, soldier2, faramir);

		var savage = scn.GetShadowCard("savage");
		var butcher = scn.GetShadowCard("butcher");
		var herdsman = scn.GetShadowCard("herdsman");
		scn.MoveCardsToHand(savage, butcher, herdsman);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// A hunter who is no minion (Faramir): no count.
		assertTrue(scn.HasKeyword(faramir, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(soldier));
		assertEquals(6, scn.GetStrength(soldier2));

		// A minion who is no hunter: no count.
		scn.ShadowPlayCard(savage);
		assertFalse(scn.HasKeyword(savage, Keyword.HUNTER));
		assertEquals(6, scn.GetStrength(soldier));
		assertEquals(6, scn.GetStrength(soldier2));

		scn.ShadowPlayCard(butcher);
		// One hunter minion: +1, though its hunter number is 6.  Each copy has its own +1, not the other's too.
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(7, scn.GetStrength(soldier2));
		assertEquals(7, scn.GetStrength(faramir));

		scn.ShadowPlayCard(herdsman);
		// Two hunter minions, of two cultures and races: +2.
		assertEquals(8, scn.GetStrength(soldier));
		assertEquals(8, scn.GetStrength(soldier2));
		assertEquals(7, scn.GetStrength(faramir));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
