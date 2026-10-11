package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Damage +X, the damage bonus.
 *
 * Comprehensive Rules 4.2, "damage bonus" (and "resolve that skirmish"): "When the winning side in a skirmish
 * phase has one or more characters with the keyword damage +1, then each losing character takes one additional
 * wound for each damage +1.  (Damage +2 adds two wounds, and so on.)  This is called a damage bonus, which may be
 * added to or removed by various effects."  Its example: "Aragorn wins a skirmish while facing two Orcs.  If
 * Aragorn has damage +1, then each Orc takes two wounds.  But if both Orcs have damage +1 and they win the
 * skirmish, then Aragorn takes three wounds instead."
 *
 * "When a character with a damage bonus is given an additional damage bonus by a card or special ability, the
 * bonuses are added together."
 *
 * "When an effect says that a character 'loses all damage bonuses,' that character cannot gain a damage bonus
 * due to some other effect."
 *
 * A side that is overwhelmed takes no wounds at all, whatever the winners' bonuses: that is in OverwhelmTests.
 */
public class DamageBonusTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability, not used here
					// Ranger's Sword: hand weapon, strength +2.  "Bearer must be Aragorn.  He is damage +1."
					put("sword", "1_112");
					// Gimli, Son of Glóin: strength 6, vitality 3.  "Damage +1.  Skirmish: Exert Gimli to make
					// him strength +2", which is not used here.
					put("gimli", "1_13");
					// Lord of Moria: [dwarven] support-area condition.  "Each Dwarf is damage +1."
					put("lord", "1_21");
					put("sam", "1_311");        // Sam, Son of Hamfast: a second [shire] companion; his texts are not used here
					// Seek and Hide: [shire] Skirmish event, cost 1.  "Spot 2 [shire] companions to make a
					// minion lose all damage bonuses."
					put("seek", "3_112");

					// Uruk Savage: strength 5, vitality 3.  "Damage +1" is its whole text.
					put("savage1", "1_151");
					put("savage2", "1_151");
					// Uruk-hai Raiding Party: strength 9, vitality 3.  "Damage +1" is its whole text.
					put("party", "1_158");
					// Lurtz's Battle Cry: [isengard] Skirmish event, cost 1.  "Make an Uruk-hai strength -1
					// and damage +1."
					put("cry1", "1_128");
					put("cry2", "1_128");
					// Easterling Berserker: strength 11, vitality 3.  "While this minion is unwounded, he is
					// damage +2."  His skirmish ability wounds 2 other [raider] Men, and there are none.
					put("berserker", "10_40");
					// Gollum, Nasty Treacherous Creature: strength 5, vitality 4; a regroup ability, not used here
					put("gollum", "5_24");
					// Isengard Worker: Orc, strength 5, vitality 3; a regroup ability, not used here
					put("worker", "3_62");
					// Goblin Archer: Orc, strength 4, vitality 3.  Archer; its text needs another [moria] Orc.
					put("archer", "1_172");
					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("runner1", "1_178");
					put("runner2", "1_178");
					// Moria Axe: hand weapon, strength +2.  "Bearer must be a [moria] Orc.  Bearer is damage
					// +1.  Skirmish: Exert bearer to make him strength +2", which a Runner cannot pay.
					put("axe1", "1_190");
					put("axe2", "1_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AWinnerWithDamagePlus1PlacesOneWoundMoreThanAWinnerWithNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{gimli, savage2});

		// Aragorn, 8, beats a Savage, 5, and has no damage bonus: one wound for losing.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage1));

		// Gimli, 6, beats the other Savage, 5, and is damage +1: the wound for losing and one more.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertEquals(1, scn.GetWoundsOn(savage1));

		// The Savages are damage +1 themselves and lost: a loser's bonus places nothing.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AWinnerWithDamagePlus2PlacesTwoWoundsMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var berserker = scn.GetShadowCard("berserker");

		scn.StartGame();
		scn.MoveMinionsToTable(berserker);
		scn.SkipToAssignments();

		assertEquals(2, scn.GetKeywordCount(berserker, Keyword.DAMAGE));

		// The Berserker, 11, beats Aragorn, 8, without overwhelming him.
		scn.FreepsAssignToMinions(aragorn, berserker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// "Damage +2 adds two wounds": three in all, and Aragorn, vitality 4, lives.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(0, scn.GetWoundsOn(berserker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AragornWithDamagePlus1BeatsTwoOrcsAndEachOrcTakesTwoWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var worker = scn.GetShadowCard("worker");
		var archer = scn.GetShadowCard("archer");

		scn.StartGame();
		scn.MoveMinionsToTable(worker);
		// The Archer goes down once the archery phase is past, so that no archery wound is owed.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(archer);
		scn.PassAssignmentActions();

		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));

		// The Free Peoples player takes the Worker and the Shadow player adds the Archer, who was left over.
		scn.FreepsAssignToMinions(aragorn, worker);
		scn.ShadowAssignToMinions(aragorn, archer);

		// The rules' own example.  10 against 5 + 4: Aragorn wins, and each Orc takes two wounds.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(worker));
		assertEquals(2, scn.GetWoundsOn(archer));
		assertInZone(Zone.SHADOW_CHARACTERS, worker, archer);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoOrcsWithDamagePlus1EachBeatAragornAndHeTakesThreeWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var axe1 = scn.GetShadowCard("axe1");
		var axe2 = scn.GetShadowCard("axe2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.AttachCardsTo(runner1, axe1);
		scn.AttachCardsTo(runner2, axe2);
		scn.SkipToAssignments();

		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(runner1));
		assertEquals(7, scn.GetStrength(runner2));
		assertEquals(1, scn.GetKeywordCount(runner1, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(runner2, Keyword.DAMAGE));

		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.ShadowAssignToMinions(aragorn, runner2);

		// The rules' own example, its second half.  7 + 7 against 10: the Orcs win, one wound for losing
		// and one more for each Orc's damage +1.  Aragorn's own bonus places nothing, as he lost.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertInZone(Zone.SHADOW_CHARACTERS, runner1, runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADamageBonusGivenByAnotherCardIsAddedToTheCharactersOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(lord);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();

		// Gimli's printed damage +1 and Lord of Moria's
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));

		// Gimli, 6, beats Gollum, 5: three wounds, and Gollum, vitality 4, lives.
		scn.FreepsAssignToMinions(gimli, gollum);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADamageBonusGivenForTheSkirmishIsAddedToTheCharactersOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var party = scn.GetShadowCard("party");
		var cry1 = scn.GetShadowCard("cry1");
		scn.MoveCardsToHand(cry1);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, party);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(1, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(9, scn.GetStrength(party));

		// The Raiding Party is the only Uruk-hai, so nothing is asked.
		scn.FreepsPass();
		scn.ShadowPlayCard(cry1);
		assertEquals(2, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(party));

		// 8 against 8: the Shadow wins a tie.  One wound for losing and two for the bonuses.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACharacterWhoLosesAllDamageBonusesHasNoneAndCannotGainOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var seek = scn.GetFreepsCard("seek");
		// Frodo and Sam are the two [shire] companions
		scn.MoveCompanionsToTable(gimli, sam);
		scn.MoveCardsToHand(seek);

		var party = scn.GetShadowCard("party");
		var cry1 = scn.GetShadowCard("cry1");
		var cry2 = scn.GetShadowCard("cry2");
		scn.MoveCardsToHand(cry1, cry2);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, party);
		scn.FreepsResolveSkirmish(gimli);

		// A bonus of its own and one given to it
		scn.FreepsPass();
		scn.ShadowPlayCard(cry1);
		assertEquals(2, scn.GetKeywordCount(party, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(party));

		// Both are lost.  The Raiding Party is the only minion, so nothing is asked.
		scn.FreepsPlayCard(seek);
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));

		// "That character cannot gain a damage bonus due to some other effect": the second Battle Cry takes
		// its strength and gives it nothing.
		scn.ShadowPlayCard(cry2);
		assertInZone(Zone.DISCARD, cry2);
		assertEquals(7, scn.GetStrength(party));
		assertEquals(0, scn.GetKeywordCount(party, Keyword.DAMAGE));

		// 7 against Gimli's 6: he loses and takes the one wound for losing.  Any bonus would have killed
		// him at once, vitality 3.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
