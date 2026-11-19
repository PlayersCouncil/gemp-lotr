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

public class Card_11_146_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "11_146");

					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ally, strength 4; a fellowship ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("riders", "11_154");    // Riders of the Mark: a [rohan] Man companion, strength 8, played "any time you could play a skirmish event"

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("brigand", "4_10");     // Dunlending Brigand: strength 8, vitality 1, no text
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					put("pursuer", "4_190");    // Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits
					put("seeker", "4_195");     // Uruk Seeker: fierce, strength 6, vitality 2; its bonus is against a search card only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Éowyn, Shieldmaiden of Rohan
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: Each time Éowyn wins a skirmish, each other [rohan] Man is strength +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", card.getBlueprint().getTitle());
		assertEquals("Shieldmaiden of Rohan", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void EachTimeEowynWinsEachOtherRohanManIsStrengthPlusOneUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eowyn, guard, aragorn);
		scn.MoveAlliesToTable(guma);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, runner}, new PhysicalCardImpl[]{guard, savage});
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();
		// Eowyn's trigger and the Runner's wound answer the win together.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		// 6 against 5: Eowyn wins, and the Runner dies of its wound.
		assertInDiscard(runner);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		// The other [rohan] Men, companion and ally, are +1; Eowyn herself and the [gondor] Man are not.
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(guma));
		assertEquals(8, scn.GetStrength(aragorn));

		// Still in force in the next skirmish...
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(guma));
		scn.PassSkirmishActions();

		// ...and gone once the regroup phase begins.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
	}

	@Test
	public void EowynLosingASkirmishGivesNoBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveAlliesToTable(guma);

		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, brigand}, new PhysicalCardImpl[]{guard, savage});
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
		scn.PassSkirmishActions();

		// 8 against 6, not double: Eowyn loses and lives.
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertInZone(Zone.FREE_CHARACTERS, eowyn);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(4, scn.GetStrength(guma));
	}

	@Test
	public void TwoWinsBeforeTheRegroupPhaseMakeEachOtherRohanManStrengthPlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eowyn, guard);

		var pursuer = scn.GetShadowCard("pursuer");
		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, pursuer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eowyn);
		assertEquals(6, scn.GetStrength(guard));
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// The first win, against the fierce Pursuer (6 against 5).
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(7, scn.GetStrength(guard));

		// The fierce round: Eowyn beats the Pursuer again, and the Guard waits for the Seeker.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, pursuer}, new PhysicalCardImpl[]{guard, seeker});
		scn.FreepsResolveSkirmish(eowyn);
		assertEquals(7, scn.GetStrength(guard));
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		// 6 + 1 + 1
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(eowyn));

		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(6, scn.GetStrength(guard));
	}

	@Test
	public void ARohanManWhoComesIntoPlayAfterTheWinIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var riders = scn.GetFreepsCard("riders");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.MoveCardsToHand(riders);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		assertInHand(riders);
		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, runner}, new PhysicalCardImpl[]{guard, savage});
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertInDiscard(runner);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(7, scn.GetStrength(guard));
		assertInHand(riders);

		// Riders of the Mark comes into play in the next skirmish phase, after the win.
		assertTrue(scn.FreepsPlayAvailable(riders));
		scn.FreepsPlayCard(riders);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(riders));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.FREE_CHARACTERS, riders);
		assertEquals(8, scn.GetStrength(riders));
		assertEquals(7, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
