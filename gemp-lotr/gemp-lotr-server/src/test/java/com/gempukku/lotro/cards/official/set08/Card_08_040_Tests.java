package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mount", "8_40");
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, knight, strength 6, vitality 3; Knight is its whole text
					put("gknight2", "5_35");
					// Pippin, Trollslayer: a knight who is no Man (a [gondor] Hobbit); a skirmish ability used only on
					// purpose
					put("pippin", "103_31");
					// Ranger of Osgiliath: [gondor] Man companion who is no knight; a regroup ability used only on purpose
					put("ranger", "7_116");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; his texts are about unbound
					// Hobbits, of whom none is killed here
					put("pursuer", "4_190");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void KnightsMountStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Knight's Mount
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 1
		 * Game Text: Bearer must be a knight.<br>Each time bearer wins a skirmish, you may wound a minion who is not assigned to a skirmish.
		*/

		var scn = GetScenario();

		var mount = scn.GetFreepsCard("mount");

		assertEquals("Knight's Mount", mount.getBlueprint().getTitle());
		assertNull(mount.getBlueprint().getSubtitle());
		assertFalse(mount.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, mount.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, mount.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, mount.getBlueprint().getCardType());
		assertTrue(mount.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(1, mount.getBlueprint().getTwilightCost());
		assertEquals(1, mount.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAKnightOfEitherRaceAndNotOnAManWhoIsNoKnight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mount = scn.GetFreepsCard("mount");
		var gknight = scn.GetFreepsCard("gknight");
		var pippin = scn.GetFreepsCard("pippin");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(gknight, pippin, ranger);
		scn.MoveCardsToHand(mount);

		scn.StartGame();
		assertInHand(mount);
		assertTrue(scn.FreepsPlayAvailable(mount));
		scn.FreepsPlayCard(mount);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gknight, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ranger, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(mount, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WinningBearerMayWoundAMinionWhoIsNotAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var mount = scn.GetFreepsCard("mount");
		var gknight = scn.GetFreepsCard("gknight");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(gknight, ranger);
		scn.AttachCardsTo(gknight, mount);

		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertAttachedTo(mount, gknight);
		scn.MoveMinionsToTable(savage, soldier, troop, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gknight, savage}, new PhysicalCardImpl[]{ranger, soldier});
		// The Troop and the Brigand are left unassigned.
		scn.ShadowDeclineAssignments();

		// The bearer, 6 + 1, beats the Savage, 5, who lives with the wound for losing.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mount));
		scn.FreepsChooseOptionalTrigger(mount);

		// The two unassigned minions, of two cultures and races, the Brigand (vitality 1) exhausted; not the Savage
		// in the bearer's skirmish, not the Soldier assigned to the Ranger, and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, soldier, gknight, ranger, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(brigand);

		// A wound, not an exertion: the Brigand is killed.
		assertInZone(Zone.DISCARD, brigand);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachTimeBearerWinsTheOfferIsMadeAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetFreepsCard("mount");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(gknight);
		scn.AttachCardsTo(gknight, mount);

		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(mount, gknight);
		scn.MoveMinionsToTable(pursuer, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gknight, pursuer);
		scn.ShadowDeclineAssignments();

		// The bearer, 7, beats the fierce Pursuer, 5; declined, the unassigned Troop is not wounded.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mount));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(troop));

		// The fierce round: the Troop, not fierce, stays unassigned; the bearer wins again and is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gknight, pursuer);
		scn.FreepsResolveSkirmish(gknight);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(mount));
		scn.FreepsChooseOptionalTrigger(mount);

		// The Troop, the one minion not assigned, takes the wound.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void IsNotOfferedWhenAKnightWithoutItWinsOrBearerLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetFreepsCard("mount");
		var gknight = scn.GetFreepsCard("gknight");
		var gknight2 = scn.GetFreepsCard("gknight2");
		scn.MoveCompanionsToTable(gknight, gknight2);
		scn.AttachCardsTo(gknight, mount);

		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(mount, gknight);
		scn.MoveMinionsToTable(savage, brigand, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gknight2, savage}, new PhysicalCardImpl[]{gknight, brigand});
		scn.ShadowDeclineAssignments();

		// A knight who does not bear it, 6, beats the Savage, 5, with the Troop unassigned.
		scn.FreepsResolveSkirmish(gknight2);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The bearer, 7, loses to the Brigand, 8, and lives with one wound.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gknight));
		assertInZone(Zone.FREE_CHARACTERS, gknight);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void IsNotOfferedWhenEveryMinionIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mount = scn.GetFreepsCard("mount");
		var gknight = scn.GetFreepsCard("gknight");
		var ranger = scn.GetFreepsCard("ranger");
		scn.MoveCompanionsToTable(gknight, ranger);
		scn.AttachCardsTo(gknight, mount);

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(mount, gknight);
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gknight, savage}, new PhysicalCardImpl[]{ranger, troop});

		// The Savage lives in the bearer's skirmish and the Troop waits for the Ranger's: no minion is unassigned.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
