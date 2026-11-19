package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_039_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("knight", "8_39");
					put("knight2", "8_39");
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is its whole text

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; his texts are about unbound
					// Hobbits, of whom none is killed here
					put("pursuer", "4_190");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its "when you play" is not met by a cheat
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KnightofDolAmrothStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Knight of Dol Amroth
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>.<br>Each time this companion wins a skirmish, you may wound a minion he is skirmishing.
		*/

		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");

		assertEquals("Knight of Dol Amroth", knight.getBlueprint().getTitle());
		assertNull(knight.getBlueprint().getSubtitle());
		assertFalse(knight.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, knight.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, knight.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, knight.getBlueprint().getCardType());
		assertEquals(Race.MAN, knight.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(knight, Keyword.KNIGHT));
		assertEquals(2, knight.getBlueprint().getTwilightCost());
		assertEquals(6, knight.getBlueprint().getStrength());
		assertEquals(3, knight.getBlueprint().getVitality());
		assertEquals(6, knight.getBlueprint().getResistance());
	}

	@Test
	public void WinningHeMayWoundEitherMinionHeIsSkirmishingAndNotOneWaitingElsewhere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(knight, gknight);

		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Strength enough to beat two minions together (5 + 7 = 12) without overwhelming them (24): 6 + 8 = 14.
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 8));
		scn.MoveMinionsToTable(savage, soldier, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, savage}, new PhysicalCardImpl[]{gknight, troop});
		scn.ShadowAssignToMinions(knight, soldier);

		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		// Each loser takes a wound: the Soldier (vitality 2) is left exhausted.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(knight));
		scn.FreepsChooseOptionalTrigger(knight);

		// Minions of two cultures and races in his skirmish, one of them exhausted; not the Troop, assigned to the
		// Gondorian Knight, and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, knight, gknight, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(soldier);

		// A wound, not an exertion: the exhausted Soldier is killed.
		assertInZone(Zone.DISCARD, soldier);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachTimeHeWinsTheOfferIsMadeAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, pursuer);

		// He, 6, beats the fierce Pursuer, 5, who takes the wound for losing; declined, nothing more happens.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(knight));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(knight));

		// The fierce round: he beats the Pursuer again (2 wounds of 3) and is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight, pursuer);
		scn.FreepsResolveSkirmish(knight);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(knight));
		scn.FreepsChooseOptionalTrigger(knight);

		// The Pursuer, the one minion he is skirmishing, takes the third wound and dies.
		assertInZone(Zone.DISCARD, pursuer);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void IsNotOfferedWhenAnotherCompanionWinsOrHeLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var knight2 = scn.GetFreepsCard("knight2");
		scn.MoveCompanionsToTable(knight, knight2);

		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight2, savage}, new PhysicalCardImpl[]{knight, brigand});

		// The second copy, 6, beats the Savage, 5: only that copy's trigger is offered.
		scn.FreepsResolveSkirmish(knight2);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(knight2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(knight));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// He, 6, loses to the Brigand, 8 (not double his strength), and lives with one wound.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(knight));
		assertInZone(Zone.FREE_CHARACTERS, knight);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void IsNotOfferedWhenTheMinionHeBeatIsKilledByTheLoss() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var knight = scn.GetFreepsCard("knight");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(knight, gknight);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, runner}, new PhysicalCardImpl[]{gknight, troop});

		// The Runner (vitality 1) dies of the wound for losing; the Troop waits for its own skirmish.
		scn.FreepsResolveSkirmish(knight);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
