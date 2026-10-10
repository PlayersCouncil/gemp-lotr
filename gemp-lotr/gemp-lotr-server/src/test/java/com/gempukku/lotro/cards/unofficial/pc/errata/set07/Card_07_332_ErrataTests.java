package com.gempukku.lotro.cards.unofficial.pc.errata.set07;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_332_ErrataTests
{
	// The King block path with this Rohirrim Road as site 1, where every test starts.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3, Valiant and nothing else
					put("deor", "7_222");       // Deor: [rohan] Man ally, vitality 2; a skirmish ability that needs initiative
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability
					// Merry, Master Holbytla: the one printed [rohan] character who is no Man, a Hobbit companion of vitality 4.
					// He is a [rohan] Man as well while 3 [rohan] companions or Eowyn can be spotted; each time another
					// [rohan] Man exerts he may exert to heal that Man (declined); a skirmish ability used only on purpose
					put("merry", "103_85");

					// Cards whose cost is read in hand.
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] Shadow condition, cost 1, support area
					put("sword", "1_218");      // Nazgul Sword: [wraith] Shadow possession, cost 1
					put("runner", "1_178");     // Goblin Runner: [moria] minion, cost 1
					put("bred", "1_121");       // Bred for Battle: [isengard] Shadow event, cost 0
					put("store", "4_294");      // Weapon Store: [rohan] Free Peoples condition, cost 1
					put("rohansword", "4_291"); // Sword of Rohan: [rohan] Free Peoples possession

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] minion, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, strength 5, vitality 3, damage +1

					put("catapult", "8_32");    // Catapult: [gondor] support possession; Maneuver: discard 2 cards from hand
												// to reveal the Shadow player's top card; he discards a Shadow card of that cost
					put("pinned", "1_192");     // Pinned Down: [moria] Shadow condition, cost 1, support area
					put("strength", "1_278");   // Strength Born of Fear: [sauron] Shadow condition, cost 1, support area
					put("coming", "1_196");     // They Are Coming: [moria] Shadow condition, cost 3, support area
				}},
				new HashMap<>()
				{{
					put("site1", "57_332");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RohirrimRoadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Rohirrim Road
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 1K
		 * Shadow Number: 0
		 * Direction: Left
		 * Game Text: <b>Fellowship:</b> Exert a [Rohan] Man to make the twilight cost of each Shadow condition and
		 * possession +2 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(1);

		assertEquals("Rohirrim Road", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(1, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void ExertingARohanManMakesEachShadowConditionAndPossessionCost2More() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var deor = scn.GetFreepsCard("deor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var frodo = scn.GetRingBearer();
		var armory = scn.GetShadowCard("armory");
		var sword = scn.GetShadowCard("sword");
		var runner = scn.GetShadowCard("runner");
		var bred = scn.GetShadowCard("bred");
		var freepsArmory = scn.GetFreepsCard("armory");
		var store = scn.GetFreepsCard("store");
		var rohansword = scn.GetFreepsCard("rohansword");

		// Merry is of the [rohan] culture but no Man: the Guard and he are two [rohan] companions, not three.
		scn.MoveCompanionsToTable(guard, aragorn, merry);
		scn.MoveAlliesToTable(deor);
		scn.MoveCardsToHand(armory, sword, runner, bred, freepsArmory, store, rohansword);

		scn.StartGame();

		var road = scn.GetCurrentSite();
		int storeCost = scn.GetTwilightCost(store);
		int rohanswordCost = scn.GetTwilightCost(rohansword);
		assertEquals(1, scn.GetTwilightCost(armory));
		assertEquals(1, scn.GetTwilightCost(sword));
		assertEquals(1, scn.GetTwilightCost(freepsArmory));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(0, scn.GetTwilightCost(bred));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsActionAvailable(road));

		scn.FreepsUseCardAction(road);
		// A [rohan] Man companion or ally; not a [gondor] Man, nor a [rohan] Hobbit, nor the Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, deor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merry, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(deor);
		// Merry's own text answers a [rohan] Man exerting: he is not exerted to heal Deor.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(deor));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(merry));

		// Shadow conditions and possessions of two cultures, whoever holds them, cost 2 more.
		assertEquals(3, scn.GetTwilightCost(armory));
		assertEquals(3, scn.GetTwilightCost(sword));
		assertEquals(3, scn.GetTwilightCost(freepsArmory));
		// Not a Shadow minion or event, nor a Free Peoples condition or possession.
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(0, scn.GetTwilightCost(bred));
		assertEquals(storeCost, scn.GetTwilightCost(store));
		assertEquals(rohanswordCost, scn.GetTwilightCost(rohansword));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShadowPlayerPays2MoreToPlayAShadowCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var armory = scn.GetShadowCard("armory");

		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(armory);

		scn.StartGame();

		var road = scn.GetCurrentSite();
		scn.FreepsUseCardAction(road);
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(armory));
		scn.ShadowPlayCard(armory);
		// 1 printed + 2
		assertEquals(twilight - 3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCostIsStill2MoreInTheSkirmishPhaseAndBackAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetShadowCard("armory");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.MoveCompanionsToTable(guard, aragorn);
		scn.MoveCardsToHand(armory);

		scn.StartGame();

		var road = scn.GetCurrentSite();
		assertEquals(1, scn.GetTwilightCost(armory));
		scn.FreepsUseCardAction(road);
		assertEquals(3, scn.GetTwilightCost(armory));

		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop}, new PhysicalCardImpl[]{aragorn, savage});

		// Aragorn's skirmish first: strength 8 against 5, Uruk Savage takes a wound and lives.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetTwilightCost(armory));
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// The second skirmish, the last thing before the regroup phase.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetTwilightCost(armory));
		scn.PassCurrentPhaseActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetTwilightCost(armory));
	}

	@Test
	public void NotOfferedWhenTheOnlyRohanManIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");

		scn.MoveCompanionsToTable(guard, aragorn, merry);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(guard, 2);

		scn.StartGame();

		var road = scn.GetCurrentSite();
		// A fresh [gondor] Man and a fresh [rohan] Hobbit (Merry is no Man beside one other [rohan] companion)
		// cannot pay.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertFalse(scn.FreepsActionAvailable(road));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARevealedConditionIsNotComingIntoPlaySoItsCostIsNotModified() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var catapult = scn.GetFreepsCard("catapult");
		var armory = scn.GetShadowCard("armory");
		var pinned = scn.GetShadowCard("pinned");
		var strength = scn.GetShadowCard("strength");
		var coming = scn.GetShadowCard("coming");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(guard);
		// Catapult needs 2 [gondor] knights to be played; it is put in play directly.
		scn.MoveCardsToSupportArea(catapult);
		scn.MoveCardsToSupportArea(pinned, strength, coming);
		scn.MoveCardsToHand(scn.GetFreepsCard("runner"), scn.GetFreepsCard("bred"));

		scn.StartGame();

		var road = scn.GetCurrentSite();
		scn.FreepsUseCardAction(road);
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		// Uruk-hai Armory, printed cost 1, is the card to be revealed; in hand it would cost 3.
		scn.MoveCardsToTopOfDeck(armory);

		assertTrue(scn.FreepsActionAvailable(catapult));
		scn.FreepsUseCardAction(catapult);
		scn.DismissRevealedCards();

		// The Shadow cards in play that cost 1 are offered, the one that costs 3 is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(pinned, strength));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(coming));
		scn.ShadowChooseCard(pinned);
		assertInDiscard(pinned);
		assertInZone(Zone.SUPPORT, strength, coming);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
