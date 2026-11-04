package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "11_53");
					// Gamling, Warrior of Rohan: [rohan] Man companion, strength 6, vitality 3; "Skirmish: Play a
					// [rohan] possession on Gamling to make him strength +2 and damage +1"
					put("gamling", "5_82");
					put("spear", "4_288");      // Rider's Spear: [rohan] hand weapon on a [rohan] Man
					// Swordarm of the White Tower: [gondor] Skirmish event, "Make a [gondor] companion strength +2"
					put("swordarm", "1_116");
					put("swordarm2", "1_116");
					// Swordsman of the Northern Kingdom: [gondor] Skirmish event, "Make a ranger strength +2"
					put("swordsman", "1_117");
					// Stout and Sturdy: [shire] event, "Maneuver or Skirmish: Heal a Hobbit"
					put("sturdy", "1_315");
					// Strength of Kings: [gondor] Response event, "If a skirmish event is played during a skirmish
					// involving a [gondor] Man, cancel that event"
					put("kings", "1_115");
					// Bred for Battle: [isengard] Skirmish event, "Exert an Uruk-hai to make it strength +3"
					put("bred", "1_121");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its (2) is never added, being cheated in
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AragornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Aragorn, Guide and Protector
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 8
		 * Game Text: <b>Ranger</b>.
		 * Each time a Free Peoples skirmish event is played, you may exert Aragorn to wound a minion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aragorn");

		assertEquals("Aragorn", card.getBlueprint().getTitle());
		assertEquals("Guide and Protector", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
	}

	@Test
	public void EachFreePeoplesSkirmishEventInHisSkirmishLetsHimExertToWoundAMinionHeIsSkirmishingUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gamling = scn.GetFreepsCard("gamling");
		var swordarm = scn.GetFreepsCard("swordarm");
		var swordarm2 = scn.GetFreepsCard("swordarm2");
		var swordsman = scn.GetFreepsCard("swordsman");
		var sturdy = scn.GetFreepsCard("sturdy");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, gamling);
		scn.MoveCardsToHand(swordarm, swordarm2, swordsman, sturdy);

		scn.StartGame();
		// A wound for Stout and Sturdy to heal.
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(savage, runner, troop);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gamling, troop});
		// The Goblin Runner left over goes to Aragorn as well.
		scn.ShadowAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// A [gondor] skirmish event: Aragorn is the only [gondor] companion, so he is made stronger unasked.
		scn.FreepsPlayCard(swordarm);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Both minions he is skirmishing are offered, the exhausted Runner among them; Gamling's Troop is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);
		// A wound, not an exertion: the Runner's one vitality goes.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A [shire] event printed "Maneuver or Skirmish", played in the skirmish, is a skirmish event.
		scn.ShadowPass();
		scn.FreepsPlayCard(sturdy);
		// The play is answered before the event's own heal is carried out.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		// The Savage is the only minion he is skirmishing now: it is wounded unasked.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Each time: a third event, the same minion wounded again.
		scn.ShadowPass();
		scn.FreepsPlayCard(swordarm2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Vitality 4 with 3 wounds: he can exert no more, and a fourth event offers nothing.
		scn.ShadowPass();
		scn.FreepsPlayCard(swordsman);
		assertInZone(Zone.DISCARD, swordsman);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(savage));
	}

	@Test
	public void AManeuverPlayAShadowSkirmishEventAResponseEventAndAPossessionPlayedOfferNothingAndTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gamling = scn.GetFreepsCard("gamling");
		var spear = scn.GetFreepsCard("spear");
		var sturdy = scn.GetFreepsCard("sturdy");
		var kings = scn.GetFreepsCard("kings");
		var swordarm = scn.GetFreepsCard("swordarm");
		var bred = scn.GetShadowCard("bred");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, gamling);
		scn.MoveCardsToHand(spear, sturdy, kings, swordarm, bred);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertInZone(Zone.HAND, spear, sturdy, kings, swordarm, bred);
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		// Stout and Sturdy played in the maneuver phase is a maneuver event and no skirmish event.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(sturdy);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// A Free Peoples card that is no event, played in his skirmish.
		scn.FreepsUseCardAction(gamling);
		assertAttachedTo(spear, gamling);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A Shadow skirmish event in his skirmish, answered by a Free Peoples response event.
		scn.ShadowPlayCard(bred);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(kings));
		scn.FreepsPlayCard(kings);
		assertInZone(Zone.DISCARD, kings, bred);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));

		// The control: a Free Peoples skirmish event is answered, and the offer declined leaves all as it was.
		scn.FreepsPlayCard(swordarm);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PlayedInAnotherCompanionsSkirmishTheExertionWoundsNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gamling = scn.GetFreepsCard("gamling");
		var swordarm = scn.GetFreepsCard("swordarm");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, gamling);
		scn.MoveCardsToHand(swordarm);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.FreepsPass();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gamling, runner});
		// Gamling's skirmish first; Aragorn's waits.
		scn.FreepsResolveSkirmish(gamling);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPlayCard(swordarm);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(aragorn));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// He is skirmishing nobody yet: neither the Savage assigned to him nor the Runner fighting Gamling is wounded.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
