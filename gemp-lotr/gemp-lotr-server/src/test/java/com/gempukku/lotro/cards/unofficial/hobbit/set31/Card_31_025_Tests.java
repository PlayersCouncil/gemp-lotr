package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_025_Tests
{
	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot. The skirmishes are fought at The Troll Hoard (site 2) on turn 1. The Warg's bearer, Orkish Aggressor
	// (6, +2 for the Warg: 8), beats Gandalf (7) and leaves him alive.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "31_25");

					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, strength 6; its Assignment ability not used
					put("goblin", "32_27");     // Assaulting Goblin: [moria] Orc, strength 7; a response at a battleground
					// Minions that are no Orcs.
					put("bats", "32_30");       // Dark Bats: [moria] Creature; a Shadow ability, not used
					put("spider", "31_55");     // Wicked Spider: [spider] Spider; a Skirmish ability, not used

					// [gandalf] cards of four kinds.
					put("gandalf", "30_27");    // Gandalf, The Grey: [gandalf] companion, strength 7, vitality 4; his texts not reached
					put("staff", "31_18");      // Wizard Staff: [gandalf] artifact on a Wizard; a start-of-turn offer (laid after it)
					put("radagast", "31_17");   // Radagast, The Brown: [gandalf] ally; a Fellowship ability, not used
					put("beorn", "31_13");      // Beorn, Skin-Changer: [gandalf] follower; his Aid declined by the skippers

					// [dwarven] companions to exert, and a [dwarven] ally who may not.
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, vitality 3; a Fellowship ability
					put("nori", "30_15");       // Nori, Of the Blue Mountains: [dwarven] companion, vitality 3; a Maneuver ability
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion, vitality 3; a Maneuver ability
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] ally; his regroup offer wants a [dwarven] follower
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void SavageWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Savage Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gundabad
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 2
		 * Vitality: 1
		 * Game Text: Bearer must be an Orc.<br>Bearer is <b>fierce</b>.<br>Each time bearer wins a skirmish, the
		 * 	Free Peoples player must exert a [dwarven] companion for each 2 [gandalf] cards you spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("Savage Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GUNDABAD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAnOrcAndMakesHimFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var aggressor = scn.GetShadowCard("aggressor");
		var goblin = scn.GetShadowCard("goblin");
		var bats = scn.GetShadowCard("bats");
		var spider = scn.GetShadowCard("spider");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor, goblin, bats, spider);
		scn.SetShadowHand(warg);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, goblin, bats, spider);
		assertInHand(warg);
		assertFalse(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));

		assertTrue(scn.ShadowPlayAvailable(warg));
		scn.ShadowPlayCard(warg);
		// Orcs of two cultures; not a Creature, nor a Spider.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, goblin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, spider));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aggressor);
		assertAttachedTo(warg, aggressor);

		// The bearer is fierce, and the other Orc is not.
		assertTrue(scn.HasKeyword(aggressor, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(goblin, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachWinMakesTheFreePeoplesPlayerExertADwarvenCompanionForEach2GandalfCardsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var aggressor = scn.GetShadowCard("aggressor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var radagast = scn.GetFreepsCard("radagast");
		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		var dwalin = scn.GetFreepsCard("dwalin");
		var dain = scn.GetFreepsCard("dain");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gandalf, balin, nori, dwalin);
		scn.MoveAlliesToTable(radagast, dain);
		scn.MoveCardsToSupportArea(beorn);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, staff);
		// Dwalin, vitality 3, exhausted.
		scn.AddWoundsToChar(dwalin, 2);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.SUPPORT, beorn, radagast, dain);
		assertEquals(2, scn.GetWoundsOn(dwalin));
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		assertAttachedTo(warg, aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		// 8 against 7: Gandalf wounded.
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// The Shadow player spots from 0 to the four [gandalf] cards: Gandalf, his Staff, Radagast and Beorn (not
		// Bilbo, a [shire] card, nor the Dwarves); all of them is the number filled in.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		assertEquals(Integer.valueOf(4), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("4");

		// Two exertions, each a [dwarven] companion of the Free Peoples player's choice: not Bilbo, Gandalf or Dain,
		// nor Dwalin, exhausted.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, nori));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, gandalf, dain, dwalin));
		scn.FreepsChooseCard(nori);
		scn.FreepsChooseCard(balin);
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(1, scn.GetWoundsOn(balin));
		assertEquals(2, scn.GetWoundsOn(dwalin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(dain));

		// The Warg's bearer is fierce: he wins the fierce skirmish against Gandalf too, and the text is answered
		// again.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		scn.ShadowChoose("4");
		scn.FreepsChooseCard(nori);
		// Nori is now exhausted: the second exertion goes to Balin unasked.
		assertEquals(2, scn.GetWoundsOn(nori));
		assertEquals(2, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThreeGandalfCardsSpottedOfFourMakeOneExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var aggressor = scn.GetShadowCard("aggressor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var radagast = scn.GetFreepsCard("radagast");
		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(gandalf, balin, nori);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToSupportArea(beorn);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.SUPPORT, beorn, radagast);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		assertAttachedTo(warg, aggressor);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Three spotted of four: one exertion, not two (3 / 2, rounded down).
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, nori));
		scn.FreepsChooseCard(nori);
		assertEquals(1, scn.GetWoundsOn(nori));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheSameDwarvenCompanionMayBeExertedForEachCount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var aggressor = scn.GetShadowCard("aggressor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var radagast = scn.GetFreepsCard("radagast");
		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToSupportArea(beorn);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.SUPPORT, beorn, radagast);
		assertEquals(0, scn.GetWoundsOn(balin));
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		scn.ShadowChoose("4");

		// Balin, the one [dwarven] companion, is exerted for each of the two counts (CR 4.2 "for each").
		assertEquals(2, scn.GetWoundsOn(balin));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ALostSkirmishMakesNobodyExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var warg = scn.GetShadowCard("warg");
		var aggressor = scn.GetShadowCard("aggressor");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var radagast = scn.GetFreepsCard("radagast");
		var beorn = scn.GetFreepsCard("beorn");
		var balin = scn.GetFreepsCard("balin");
		var nori = scn.GetFreepsCard("nori");
		scn.MoveCompanionsToTable(gandalf, balin, nori);
		scn.MoveAlliesToTable(radagast);
		scn.MoveCardsToSupportArea(beorn);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, staff);
		assertAttachedTo(staff, gandalf);
		scn.FreepsPass();
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, warg);
		assertAttachedTo(warg, aggressor);
		// Gandalf made strong enough to beat the bearer (8) and not to kill him (vitality 2 + 1 for the Warg).
		scn.ApplyAdHocModifier(new StrengthModifier(null, gandalf, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, aggressor);
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		// 10 against 8: the bearer loses and takes a wound; nobody is asked to spot or to exert.
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(nori));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
