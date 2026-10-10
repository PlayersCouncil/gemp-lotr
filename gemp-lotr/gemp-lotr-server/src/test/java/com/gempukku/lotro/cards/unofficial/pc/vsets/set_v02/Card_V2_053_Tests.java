package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dawn", "102_53");

					put("guard", "5_83");       // Household Guard: valiant [rohan] Man, vitality 3; no other text
					put("guard2", "5_83");
					put("guardian", "54_76");   // Lorien Guardian: valiant [elven] Elf, vitality 3; a regroup ability
					put("elite", "4_265");      // Elite Rider: [rohan] Man who is not valiant; stronger against a wounded minion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], not valiant; Maneuver: exert him to make him defender +1
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] ally, a Man, vitality 2; a regroup ability
					// Northern Signal-fire, Flame of Calenhad: a [rohan] possession in the support area.  "Maneuver:
					// Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of your other Men
					// gain each of the unloaded keywords (except race) on that Man."
					put("flame", "103_86");

					put("valor", "1_103");      // Elendil's Valor: Free Peoples event. Maneuver: make a [gondor] companion defender +1
					put("valor2", "1_103");
					put("spear", "4_288");      // Rider's Spear: Free Peoples possession
					put("dwarf", "1_7");        // Dwarf Guard: Free Peoples companion
					put("mark", "12_114");      // For the Mark: regroup event; "You may exert a [rohan] Man who has resistance 3
					put("mark2", "12_114");     //   or more to play this event during the maneuver phase."
					put("stronghold", "102_48"); // Stronghold of My Fathers: Response: if a [rohan] companion is about to take a wound, discard this to prevent it

					put("runner", "1_178");     // Goblin Runner: Shadow minion
					put("poison", "8_28");      // Spider Poison: Shadow possession
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3; damage +1
					put("enquea", "1_231");     // Ulaire Enquea: Maneuver: spot 6 companions and exert him to wound a companion
					put("terror", "1_226");     // Their Power Is in Terror: Shadow event. Maneuver: exert a Nazgul to wound every ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AndtheRedDawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: And the Red Dawn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Maneuver: Spot 3 valiant companions to discard your hand. For each Shadow card discarded, exert a minion. For each Free Peoples card discarded, heal a valiant or [rohan] companion. You cannot make any more Maneuver actions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dawn");

		assertEquals("And the Red Dawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForTwoToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		scn.MoveCardsToHand(dawn);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(dawn));

		scn.FreepsPlayCard(dawn);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, dawn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedUntilAThirdValiantCompanionCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guardian = scn.GetFreepsCard("guardian");
		var dwarf = scn.GetFreepsCard("dwarf");
		var herdsman = scn.GetFreepsCard("herdsman");
		var flame = scn.GetFreepsCard("flame");
		var spear = scn.GetFreepsCard("spear");
		// Two valiant companions, of two cultures; a Dwarf Guard and Frodo are two companions who are not valiant.
		scn.MoveCompanionsToTable(guard, guardian, dwarf);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(dawn, flame);
		scn.MoveCardsToHand(spear);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(herdsman, Keyword.VALIANT));
		assertFalse(scn.FreepsActionAvailable(dawn));

		// A valiant character who is no companion: no ally is valiant in print.  The Flame of Calenhad has the
		// Household Guard, the only unbound Man, exert, and until the regroup phase the Herdsman, the player's
		// other Man, has his keyword.  Two valiant companions and a valiant ally are not three valiant companions.
		scn.FreepsUseCardAction(flame);
		scn.ShadowPass();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.HasKeyword(herdsman, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(dwarf, Keyword.VALIANT));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dawn));

		// A third valiant companion arrives, and the action is there.
		scn.MoveCompanionsToTable(guard2);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsActionAvailable(dawn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DiscardsTheWholeHandExertingAMinionForEachShadowCardAndHealingAValiantOrRohanCompanionForEachFreePeoplesCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var dawn = scn.GetFreepsCard("dawn");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guardian = scn.GetFreepsCard("guardian");
		var elite = scn.GetFreepsCard("elite");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herdsman = scn.GetFreepsCard("herdsman");
		// Exactly three valiant companions: two Household Guards and the Lorien Guardian.
		scn.MoveCompanionsToTable(guard, guard2, guardian, elite, aragorn);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(dawn);

		// Two Shadow cards and three Free Peoples cards, of five types between them.
		var runner = scn.GetFreepsCard("runner");
		var poison = scn.GetFreepsCard("poison");
		var valor = scn.GetFreepsCard("valor");
		var spear = scn.GetFreepsCard("spear");
		var dwarf = scn.GetFreepsCard("dwarf");
		scn.MoveCardsToHand(runner, poison, valor, spear, dwarf);
		scn.MoveCardsToHand(scn.GetShadowCard("runner"), scn.GetShadowCard("poison"));

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// The ally is [rohan] and wounded, so that only his being an ally keeps him from being healed.
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(guardian, 2);
		scn.AddWoundsToChar(elite, 1);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(herdsman, 1);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(dawn));

		scn.FreepsUseCardAction(dawn);

		// The game has the hand discarded a card at a time, in the order its owner chooses, and does what
		// each card calls for as it goes.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, poison, valor, spear, dwarf));
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner);

		// A Shadow card: exert a minion. Either minion, and no companion or ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, aragorn, herdsman));
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));

		// A second Shadow card, of another type: the same minion may be exerted again.
		scn.FreepsChooseCard(poison);
		assertInDiscard(poison);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(troop);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));

		// A Free Peoples card: heal a valiant or [rohan] companion. The wounded ones are a valiant [rohan] Man,
		// a valiant Elf and a [rohan] Man who is not valiant; not Aragorn or Frodo, who are neither, nor the ally.
		scn.FreepsChooseCard(valor);
		assertInDiscard(valor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guardian, elite));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo, herdsman, troop));
		assertEquals(3, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guardian);
		assertEquals(1, scn.GetWoundsOn(guardian));

		// The same companion may be healed again.
		scn.FreepsChooseCard(spear);
		assertInDiscard(spear);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guardian, elite));
		scn.FreepsChooseCard(guardian);
		assertEquals(0, scn.GetWoundsOn(guardian));

		// The last card needs no choosing. The Guardian has no wound left to heal.
		assertInDiscard(dwarf);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, elite));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guardian, aragorn, frodo, herdsman));
		scn.FreepsChooseCard(elite);
		assertEquals(0, scn.GetWoundsOn(elite));

		// Two exertions for two Shadow cards and three heals for three Free Peoples cards, and no more.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(herdsman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOnlyAFreePeoplesCardInHandOneCompanionIsHealedAndNoMinionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guardian = scn.GetFreepsCard("guardian");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(guard, guard2, guardian);
		scn.MoveCardsToSupportArea(dawn);
		scn.MoveCardsToHand(spear);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(guardian, 2);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(dawn);

		assertInDiscard(spear);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guardian));
		scn.FreepsChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithOnlyAShadowCardInHandOneMinionIsExertedAndNobodyIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guardian = scn.GetFreepsCard("guardian");
		var runner = scn.GetFreepsCard("runner");
		scn.MoveCompanionsToTable(guard, guard2, guardian);
		scn.MoveCardsToSupportArea(dawn);
		scn.MoveCardsToHand(runner);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AddWoundsToChar(guard, 2);
		scn.AddWoundsToChar(guardian, 2);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(dawn);

		assertInDiscard(runner);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(guardian));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AfterItIsUsedItsPlayerCanMakeNoMoreManeuverActionsWhileTheShadowPlayerCanAndAResponseIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var stronghold = scn.GetFreepsCard("stronghold");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guardian = scn.GetFreepsCard("guardian");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elite = scn.GetFreepsCard("elite");
		var valor = scn.GetFreepsCard("valor");
		var valor2 = scn.GetFreepsCard("valor2");
		var mark = scn.GetFreepsCard("mark");
		var mark2 = scn.GetFreepsCard("mark2");
		// Six companions with Frodo, which is what Ulaire Enquea's ability needs; three of them valiant.
		scn.MoveCompanionsToTable(guard, guard2, guardian, aragorn, elite);
		scn.MoveCardsToSupportArea(dawn, stronghold);
		scn.MoveCardsToHand(valor, mark);

		var enquea = scn.GetShadowCard("enquea");
		var terror = scn.GetShadowCard("terror");
		scn.MoveCardsToHand(terror);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.MANEUVER);

		// Before: a companion's Maneuver ability, a Maneuver event, a regroup event that says it may be played
		// during the maneuver phase, and the condition itself are all on offer.
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(valor));
		assertTrue(scn.FreepsPlayAvailable(mark));
		assertTrue(scn.FreepsActionAvailable(dawn));

		// The two cards in hand are discarded, and nobody has a wound to heal.
		scn.FreepsUseCardAction(dawn);
		scn.FreepsChooseCard(valor);
		assertInDiscard(valor, mark);

		// Other copies of the events come to hand, so that there are events to refuse.
		scn.MoveCardsToHand(valor2, mark2);

		// "You": the other player still has his Maneuver actions, a minion's ability and an event from hand.
		assertInHand(terror);
		assertTrue(scn.ShadowPlayAvailable(terror));
		assertTrue(scn.ShadowActionAvailable(enquea));
		scn.ShadowUseCardAction(enquea);
		scn.ShadowChooseCard(guard);

		// A response is not a Maneuver action: this player may still answer the wound.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stronghold));
		scn.FreepsChooseOptionalTrigger(stronghold);
		assertInDiscard(stronghold);
		assertEquals(0, scn.GetWoundsOn(guard));

		// Asked again in the same maneuver phase, this player has none of the four.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertInHand(valor2, mark2);
		assertFalse(scn.FreepsPlayAvailable(valor2));
		assertFalse(scn.FreepsPlayAvailable(mark2));
		assertFalse(scn.FreepsActionAvailable(dawn));
	}

	@Test
	public void ManeuverActionsMayBeMadeAgainInTheNextManeuverPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dawn = scn.GetFreepsCard("dawn");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var guardian = scn.GetFreepsCard("guardian");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(guard, guard2, guardian, aragorn);
		scn.MoveCardsToSupportArea(dawn);
		scn.MoveCardsToHand(spear);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(aragorn));
		scn.FreepsUseCardAction(dawn);
		assertInDiscard(spear);
		scn.ShadowPass();

		// Still the same maneuver phase, with this player being asked.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aragorn));
		assertFalse(scn.FreepsActionAvailable(dawn));

		// Nobody is assigned to the Troop, and the fellowship moves on to site 3 with it still in play.
		// A Maneuver event comes to hand on the way.
		scn.SkipToMovementDecision();
		scn.MoveCardsToHand(valor);
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(valor));
		assertTrue(scn.FreepsActionAvailable(dawn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
