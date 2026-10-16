package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_182_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "1_182");
					put("spear2", "1_182");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
					put("troop2", "1_177");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its (2) only when played
					put("cavetroll", "1_165");  // Cave Troll of Moria: a [moria] minion who is a Troll; its other text is about playing it
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7; answers a weapon played on it
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc; "Skirmish: Exert this minion to wound a character he is skirmishing"
					put("pinned", "1_192");     // Pinned Down: a [moria] condition in the support area; its text needs a [moria] archer

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion, strength 4, vitality 2; its text is about playing it
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit companion, strength 3 (+2 only with a weapon)
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ALLY, strength 4, vitality 2; takes part in skirmishes by his own maneuver ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Spear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [moria] minion.
		 * 	If bearer kills a companion in a skirmish, add 1 burden (or 2 burdens if that companion was a Hobbit).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("spear");

		assertEquals("Goblin Spear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAMoriaMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		var cavetroll = scn.GetShadowCard("cavetroll");
		var chieftain = scn.GetShadowCard("chieftain");
		var pinned = scn.GetShadowCard("pinned");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCardsToHand(spear);
		scn.MoveMinionsToTable(runner, cavetroll, chieftain);
		scn.MoveCardsToSupportArea(pinned);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);
		// [moria] minions of two races; not an Orc of another culture, nor a companion. Pinned Down, a [moria] card that is
		// no minion, is not offered either, but that tells nothing of the definition: a possession goes on characters only, and
		// no [moria] companion or ally is printed.
		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, cavetroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chieftain, pinned, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(cavetroll);

		assertAttachedTo(spear, cavetroll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerKillingACompanionWhoIsNoHobbitAddsOneBurdenAndAnotherMinionsKillAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		// Aragorn has one vitality left: the wound for losing kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop, troop2);
		scn.AttachCardsTo(troop, spear);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertAttachedTo(spear, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, troop2}, new PhysicalCardImpl[]{aragorn, troop});
		assertEquals(0, scn.GetBurdens());

		// The other Goblin Patrol Troop, with no Spear, overwhelms Dwarf Guard: 13 against 4.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertEquals(0, scn.GetBurdens());

		// The bearer beats Aragorn without overwhelming him (15 against 8), and the wound kills him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The Free Peoples player orders the rule's death and the Spear's trigger.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerKillingAHobbitCompanionOfAnyCultureAddsTwoBurdens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var spear2 = scn.GetShadowCard("spear2");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2);
		scn.AttachCardsTo(troop, spear);
		scn.AttachCardsTo(troop2, spear2);
		assertAttachedTo(spear, troop);
		assertAttachedTo(spear2, troop2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop}, new PhysicalCardImpl[]{pippin, troop2});
		assertEquals(0, scn.GetBurdens());

		// 15 against 3: a [shire] Hobbit overwhelmed.
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, merry);
		assertEquals(2, scn.GetBurdens());

		// A [gondor] Hobbit is a Hobbit as well.
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, pippin);
		assertEquals(4, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerWinningWithoutKillingACompanionOrKillingAnAllyAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var spear2 = scn.GetShadowCard("spear2");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2);
		scn.AttachCardsTo(troop, spear);
		scn.AttachCardsTo(troop2, spear2);
		assertAttachedTo(spear, troop);
		assertAttachedTo(spear2, troop2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{thrarin, troop2});
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// 15 against 8: Aragorn loses, takes the wound and lives.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInPlay(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetBurdens());

		// 15 against 4: the ally is overwhelmed and killed, and he is no companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertNotInZone(Zone.SUPPORT, thrarin);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionKilledInTheBearersSkirmishByAnotherMinionsWoundAddsNoBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();
		// Gimli has one vitality left.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(runner, soldier);
		scn.AttachCardsTo(runner, spear);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertAttachedTo(spear, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		// The Orc Soldier is left over, and the Shadow player puts him on Gimli too.
		scn.ShadowAssignToMinions(gimli, soldier);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runner));
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, soldier));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		// The Orc Soldier's own wound kills Gimli, before he has lost to the bearer: the bearer's text did not kill
		// him, nor did his losing (CR 4.2 Section Three's "kills", Q-0328: B).
		scn.ShadowUseCardAction(soldier);
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertInZone(Zone.DEAD, gimli);
		assertEquals(0, scn.GetBurdens());
		// With its only companion gone the skirmish is over, and so are the skirmishes.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
