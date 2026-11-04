package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ResistanceModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_138_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("theodred", "13_138");
					// Éowyn, Shieldmaiden of Rohan: [rohan] Man companion, resistance 7, cost 2; a trigger on her own
					// skirmish wins, and she fights none here
					put("eowyn", "11_146");
					// Éomer, Éored Leader: [rohan] Man companion, resistance 7; his cost reduction and maneuver
					// ability do not come up here
					put("eomer", "12_112");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3, resistance 6; Valiant is its whole text
					// Faramir, Dúnadan of Gondor: [gondor] Man companion, resistance 7; a skirmish ability only
					put("faramir", "12_47");
					// Merry, Master Holbytla: a [rohan] Hobbit companion, resistance 6; a Man only while 3 [rohan]
					// companions (or Éowyn) can be spotted, never so where he is laid; his texts are used only on purpose
					put("merry", "103_85");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, strength 11, vitality 3; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1 (always exhausted); its only text is on its play
					// Siege Troop: [sauron] Orc, strength 20, vitality 4; a regroup ability used only on purpose
					put("siegetroop", "8_106");
					// Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its other texts answer unbound Hobbits
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheodredStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Théodred, Second Marshal of the Mark
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While you can spot a [rohan] Man who has resistance of 7 or more, Théodred is twilight cost -1.<br>At the start of each skirmish involving Théodred, you may exert each minion he is skirmishing.
		*/

		var scn = GetScenario();

		var theodred = scn.GetFreepsCard("theodred");

		assertEquals("Théodred", theodred.getBlueprint().getTitle());
		assertEquals("Second Marshal of the Mark", theodred.getBlueprint().getSubtitle());
		assertTrue(theodred.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, theodred.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, theodred.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, theodred.getBlueprint().getCardType());
		assertEquals(Race.MAN, theodred.getBlueprint().getRace());
		assertEquals(2, theodred.getBlueprint().getTwilightCost());
		assertEquals(6, theodred.getBlueprint().getStrength());
		assertEquals(3, theodred.getBlueprint().getVitality());
		assertEquals(6, theodred.getBlueprint().getResistance());
	}

	@Test
	public void OnceARohanManOfResistance7IsPlayedTheodredCosts1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theodred = scn.GetFreepsCard("theodred");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCardsToHand(theodred, eowyn, guard);

		scn.StartGame();

		assertInHand(theodred);
		assertInHand(eowyn);
		assertInHand(guard);
		assertEquals(0, scn.GetTwilight());
		// Nothing to spot: his printed 2, beside the Guard's 2.
		assertEquals(2, scn.GetTwilightCost(theodred));
		assertEquals(2, scn.GetTwilightCost(guard));

		scn.FreepsPlayCard(eowyn);
		assertEquals(7, scn.GetResistance(eowyn));
		assertEquals(2, scn.GetTwilight());
		// The discount is Théodred's alone: the Guard, another [rohan] companion in hand, still costs 2.
		assertEquals(1, scn.GetTwilightCost(theodred));
		assertEquals(2, scn.GetTwilightCost(guard));

		scn.FreepsPlayCard(theodred);
		// 2 for Éowyn + 1 for Théodred.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, theodred);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ABurdenThatTakesTheRohanManToResistance6TakesTheDiscountWithIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theodred = scn.GetFreepsCard("theodred");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToHand(theodred);

		scn.StartGame();

		assertInHand(theodred);
		assertEquals(0, scn.GetBurdens());
		// Another title of [rohan] Man at exactly 7.
		assertEquals(7, scn.GetResistance(eomer));
		assertEquals(1, scn.GetTwilightCost(theodred));

		// Each burden lowers every companion's resistance: Éomer at 6, one short.
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetResistance(eomer));
		assertEquals(2, scn.GetTwilightCost(theodred));
	}

	@Test
	public void AGondorManOrARohanHobbitOfResistance7OrARohanManOf6GivesNoDiscount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theodred = scn.GetFreepsCard("theodred");
		var faramir = scn.GetFreepsCard("faramir");
		var merry = scn.GetFreepsCard("merry");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		// Two [rohan] companions, the Guard and Merry, and no Éowyn: Merry is no Man.
		scn.MoveCompanionsToTable(faramir, merry, guard);
		scn.MoveCardsToHand(theodred);

		scn.StartGame();
		// No [rohan] Hobbit of resistance 7 is in print: Merry's 6 is raised by one, so that only "Man" keeps him out.
		scn.ApplyAdHocModifier(new ResistanceModifier(null, merry, null, 1));

		assertInHand(theodred);
		assertEquals(7, scn.GetResistance(faramir));
		assertEquals(7, scn.GetResistance(merry));
		assertEquals(6, scn.GetResistance(guard));
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(2, scn.GetTwilightCost(theodred));
	}

	@Test
	public void AtTheStartOfHisSkirmishHeMayExertEachMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theodred = scn.GetFreepsCard("theodred");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(theodred, guard);

		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");
		var runner = scn.GetShadowCard("runner");
		var siegetroop = scn.GetShadowCard("siegetroop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, assassin, runner, siegetroop);
		scn.SkipToAssignments();
		// Three minions on Théodred, two of them put there by the Shadow player, the Runner exhausted; the Siege
		// Troop waits for the Guard.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{theodred, savage}, new PhysicalCardImpl[]{guard, siegetroop});
		scn.ShadowAssignToMinions(theodred, assassin, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(0, scn.GetWoundsOn(siegetroop));

		scn.FreepsResolveSkirmish(theodred);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theodred));
		scn.FreepsAcceptOptionalTrigger();

		// Each minion he is skirmishing that can exert does; the Runner cannot, and is not wounded either.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(siegetroop));
		assertEquals(0, scn.GetWoundsOn(theodred));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedAtTheStartOfAnotherCompanionsSkirmishAndDeclinedAtHisOwnNothingExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theodred = scn.GetFreepsCard("theodred");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(theodred, guard);

		var savage = scn.GetShadowCard("savage");
		var siegetroop = scn.GetShadowCard("siegetroop");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, siegetroop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{theodred, savage}, new PhysicalCardImpl[]{guard, siegetroop});

		// The Guard's skirmish first, Théodred assigned and waiting for his.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(theodred));
		assertEquals(0, scn.GetWoundsOn(siegetroop));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The Troop (20) overwhelms the Guard (6).
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(theodred);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theodred));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhenEveryMinionHeIsSkirmishingIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theodred = scn.GetFreepsCard("theodred");
		scn.MoveCompanionsToTable(theodred);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theodred, runner);
		scn.FreepsResolveSkirmish(theodred);

		// The Runner, vitality 1, cannot exert: the offer would do nothing.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(theodred));
		assertEquals(0, scn.GetWoundsOn(runner));
	}

	@Test
	public void TheFierceSkirmishIsAnotherSkirmishInvolvingHimAndOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theodred = scn.GetFreepsCard("theodred");
		scn.MoveCompanionsToTable(theodred);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// Strength for the Pursuer, so that it wins both skirmishes and takes no wound for losing: 5 + 3 against 6.
		scn.ApplyAdHocModifier(new StrengthModifier(null, pursuer, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theodred, pursuer);
		scn.FreepsResolveSkirmish(theodred);
		assertEquals(8, scn.GetStrength(pursuer));
		assertEquals(0, scn.GetWoundsOn(pursuer));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theodred));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// Théodred loses, takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(theodred));
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(theodred, pursuer);
		scn.FreepsResolveSkirmish(theodred);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(theodred));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
