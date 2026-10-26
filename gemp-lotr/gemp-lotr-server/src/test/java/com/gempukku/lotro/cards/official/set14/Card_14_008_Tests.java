package com.gempukku.lotro.cards.official.set14;

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

public class Card_14_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("duinhir", "14_8");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant only
					put("firefoot", "13_126");  // Firefoot, Mearas of The Mark: [rohan] mount on a [rohan] Man; its triggers need tokens
					put("armor", "1_92");       // Armor: [gondor] possession of the class armor, on a Man
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally; a maneuver ability only

					put("rider", "12_161");     // Black Rider: [wraith] Nazgul, fierce
					put("steed", "1_208");      // Black Steed: [wraith] mount on a Nazgul; +2 at a plains only
					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, strength 7, vitality 2, damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5
					put("runner2", "1_178");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, no text
					put("brigand2", "4_10");
					put("taskmaster", "2_90");  // Orc Taskmaster: [sauron] Orc, strength 8, vitality 3; a Shadow ability only
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3
					put("poison", "8_28");      // Spider Poison: [gollum] possession in the support area, no minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DuinhirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Duinhir, Tall Man of Blackroot Vale
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: While you can spot a mount, Duinhir is <b>damage +1</b>.<br>Each time Duinhir wins a skirmish, if you can spot more minions than companions, you may exert him to discard a minion that has strength 7 or less.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("duinhir");

		assertEquals("Duinhir", card.getBlueprint().getTitle());
		assertEquals("Tall Man of Blackroot Vale", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void IsDamagePlus1OnceAFreePeoplesMountIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duinhir = scn.GetFreepsCard("duinhir");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var firefoot = scn.GetFreepsCard("firefoot");
		scn.MoveCompanionsToTable(duinhir, guard);
		// A possession of another class in play, and the mount still in hand.
		scn.AttachCardsTo(guard, armor);
		scn.MoveCardsToHand(firefoot);

		scn.StartGame();

		assertAttachedTo(armor, guard);
		assertInHand(firefoot);
		assertEquals(0, scn.GetKeywordCount(duinhir, Keyword.DAMAGE));

		scn.FreepsPlayCard(firefoot);
		assertAttachedTo(firefoot, guard);

		assertTrue(scn.HasKeyword(duinhir, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(duinhir, Keyword.DAMAGE));
		// The mount's own bearer is not made damage +1.
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowMountOnAMinionIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duinhir = scn.GetFreepsCard("duinhir");
		scn.MoveCompanionsToTable(duinhir);

		scn.StartGame();
		var rider = scn.GetShadowCard("rider");
		var steed = scn.GetShadowCard("steed");
		scn.MoveMinionsToTable(rider);
		assertEquals(0, scn.GetKeywordCount(duinhir, Keyword.DAMAGE));

		scn.AttachCardsTo(rider, steed);
		assertAttachedTo(steed, rider);
		assertEquals(1, scn.GetKeywordCount(duinhir, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WinningWithMoreMinionsThanCompanionsHeMayExertToDiscardAMinionOfStrength7OrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duinhir = scn.GetFreepsCard("duinhir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(duinhir);
		// An ally, who is no companion: counted among the companions, he would make 3 against 3.
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		// Three minions against two companions (Frodo and Duinhir).
		scn.MoveMinionsToTable(warrior, runner, brigand);
		// Strength 8 against the Warrior's 7, so that he wins.
		scn.ApplyAdHocModifier(new StrengthModifier(null, duinhir, null, 2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(duinhir, warrior);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(duinhir);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(duinhir));
		assertEquals(0, scn.GetWoundsOn(duinhir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(duinhir));

		// The minion he beat (strength 7, wounded) and one in no skirmish (5, another culture and race);
		// not one of strength 8, and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand, frodo, duinhir, thrarin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);

		assertInDiscard(runner);
		assertInPlay(warrior, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionOfStrength7OrLessTheExertionDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duinhir = scn.GetFreepsCard("duinhir");
		scn.MoveCompanionsToTable(duinhir);

		scn.StartGame();
		var taskmaster = scn.GetShadowCard("taskmaster");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		// Three minions of strength 8 against two companions.
		scn.MoveMinionsToTable(taskmaster, brigand, brigand2);
		// Strength 9 against the Taskmaster's 8.
		scn.ApplyAdHocModifier(new StrengthModifier(null, duinhir, null, 3));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(duinhir, taskmaster);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(duinhir);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(duinhir));
		assertEquals(0, scn.GetWoundsOn(duinhir));
		scn.FreepsAcceptOptionalTrigger();
		// Paid, and nothing to discard.
		assertEquals(1, scn.GetWoundsOn(duinhir));
		assertInPlay(taskmaster, brigand, brigand2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAsManyMinionsAsCompanionsHisWinOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duinhir = scn.GetFreepsCard("duinhir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(duinhir, aragorn);

		scn.StartGame();
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		var poison = scn.GetShadowCard("poison");
		// Three minions against three companions, and a Shadow card in play that is no minion.
		scn.MoveMinionsToTable(warrior, runner, brigand);
		scn.MoveCardsToSupportArea(poison);
		assertInZone(Zone.SUPPORT, poison);
		scn.ApplyAdHocModifier(new StrengthModifier(null, duinhir, null, 2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(duinhir, warrior);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(duinhir);
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(duinhir));
		assertEquals(0, scn.GetWoundsOn(duinhir));
		assertInPlay(runner, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duinhir = scn.GetFreepsCard("duinhir");
		scn.MoveCompanionsToTable(duinhir);

		scn.StartGame();
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveMinionsToTable(warrior, runner, brigand);
		// The Runner at 7 beats Duinhir's 6 and gives him one wound, which he lives through.
		scn.ApplyAdHocModifier(new StrengthModifier(null, runner, null, 2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(duinhir, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(duinhir);
		assertEquals(0, scn.GetWoundsOn(duinhir));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(duinhir));
		assertInPlay(duinhir);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(duinhir));
		assertInPlay(warrior, runner, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExhaustedHeIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duinhir = scn.GetFreepsCard("duinhir");
		scn.MoveCompanionsToTable(duinhir);
		scn.AddWoundsToChar(duinhir, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(duinhir));
		var warrior = scn.GetShadowCard("warrior");
		var runner = scn.GetShadowCard("runner");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveMinionsToTable(warrior, runner, brigand);
		scn.ApplyAdHocModifier(new StrengthModifier(null, duinhir, null, 2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(duinhir, warrior);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(duinhir);
		scn.PassSkirmishActions();

		// Vitality 3 with 2 wounds: he cannot exert.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(duinhir));
		assertEquals(2, scn.GetWoundsOn(duinhir));
		assertInPlay(runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedAtOneWinItIsOfferedAgainAtTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var duinhir = scn.GetFreepsCard("duinhir");
		scn.MoveCompanionsToTable(duinhir);

		scn.StartGame();
		var pursuer = scn.GetShadowCard("pursuer");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		// Three minions against two companions; the fierce Pursuer (strength 5, vitality 3) loses to him twice.
		scn.MoveMinionsToTable(pursuer, runner, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(duinhir, pursuer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(duinhir);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(duinhir));
		scn.FreepsDeclineOptionalTrigger();
		// Declined: no exertion, nothing discarded.
		assertEquals(0, scn.GetWoundsOn(duinhir));
		assertInPlay(pursuer, runner, runner2);
		assertEquals(1, scn.GetWoundsOn(pursuer));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(duinhir, pursuer);
		scn.FreepsResolveSkirmish(duinhir);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(duinhir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(duinhir));
		scn.FreepsChooseCard(runner);
		assertInDiscard(runner);
		assertInPlay(pursuer, runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
