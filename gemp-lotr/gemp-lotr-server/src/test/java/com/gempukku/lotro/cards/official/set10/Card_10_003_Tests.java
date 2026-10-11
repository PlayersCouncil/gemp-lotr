package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("more", "10_3");
					put("more2", "10_3");

					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf, strength 6, damage +1; "Skirmish: Exert Gimli to make him strength +2."
					put("lord", "1_21");        // Lord of Moria: support-area condition, "Each Dwarf is damage +1."
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally, strength 4, home site 3; a maneuver ability used only on purpose
					put("ring", "9_8");         // Ring of Guile: artifact on a Dwarf, strength +1, bearer is damage +1; a maneuver ability used only on purpose
					put("huorn", "9_25");       // Huorn: a [gandalf] companion, strength 6, who is damage +1 and no Dwarf; a response that needs an Ent
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability used only on purpose

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text is about being played
					put("scrabbler", "2_64");   // Goblin Scrabbler: [moria] Orc, strength 4, vitality 1; its texts are about playing minions
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; "Damage +1." is its whole text
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoreYettoComeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: More Yet to Come
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a Dwarf kills a minion in a skirmish and that minion did not take all wounds caused by that Dwarf's damage bonus, assign those remaining wounds to minions not assigned to a skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("more");

		assertEquals("More Yet to Come", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheDamageWoundAKilledMinionDidNotTakeGoesToAMinionNotAssignedToASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var more = scn.GetFreepsCard("more");
		var more2 = scn.GetFreepsCard("more2");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(more, more2);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage, patrol, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{gimli, runner},
				new PhysicalCardImpl[]{aragorn, brigand});
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertFalse(scn.FreepsPlayAvailable(more));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Gimli, 6, beats the Runner, 5: the wound for losing kills it, and the wound of his damage +1 is left over.
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertTrue(scn.FreepsPlayAvailable(more));
		assertTrue(scn.FreepsPlayAvailable(more2));
		scn.FreepsPlayCard(more);

		// The two minions nobody was assigned to, and not the one waiting to fight Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, patrol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertEquals(0, scn.GetWoundsOn(patrol));
		scn.FreepsChooseCard(patrol);

		assertEquals(1, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInDiscard(more);

		// That wound is assigned now: the second copy has nothing left to assign.
		assertInHand(more2);
		assertFalse(scn.FreepsPlayAvailable(more2));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedWhenTheKilledMinionTookEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var more = scn.GetFreepsCard("more");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(more);

		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, patrol);
		// Vitality 3 with 1 wound: the Savage has room for the wound for losing and for the wound of damage +1.
		scn.AddWoundsToChar(savage, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		assertInDiscard(savage);
		assertInHand(more);
		assertFalse(scn.FreepsPlayAvailable(more));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenACompanionWhoIsNoDwarfKillsTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var huorn = scn.GetFreepsCard("huorn");
		var more = scn.GetFreepsCard("more");
		scn.MoveCompanionsToTable(huorn);
		scn.MoveCardsToHand(more);

		var runner = scn.GetShadowCard("runner");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, patrol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(huorn, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(huorn);
		// Huorn, 6 and damage +1, kills the Runner as Gimli would, with the same wound left over.
		assertEquals(6, scn.GetStrength(huorn));
		assertEquals(1, scn.GetKeywordCount(huorn, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		assertInDiscard(runner);
		assertInHand(more);
		assertFalse(scn.FreepsPlayAvailable(more));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheDwarfOverwhelmsTheMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var more = scn.GetFreepsCard("more");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(more);

		var runner = scn.GetShadowCard("runner");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, patrol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		// 6 + 2 + 2 is twice the Runner's 5: an overwhelmed minion takes no wounds, it simply dies.
		scn.FreepsUseCardAction(gimli);
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		assertEquals(10, scn.GetStrength(gimli));
		assertEquals(5, scn.GetStrength(runner));
		scn.ShadowPass();
		scn.FreepsPass();

		assertInDiscard(runner);
		assertInHand(more);
		assertFalse(scn.FreepsPlayAvailable(more));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfWhoIsDamagePlusTwoHasTwoWoundsToAssignAndMayPutBothOnOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		var more = scn.GetFreepsCard("more");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(more);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage, patrol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		assertInDiscard(runner);
		assertTrue(scn.FreepsPlayAvailable(more));
		scn.FreepsPlayCard(more);

		// One wound at a time, and nothing says they go to different minions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, patrol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(patrol);
		assertEquals(1, scn.GetWoundsOn(patrol));

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, patrol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(patrol);
		assertEquals(2, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(savage));

		assertInDiscard(more);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyTheWoundsTheMinionDidNotTakeAreAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var lord = scn.GetFreepsCard("lord");
		var more = scn.GetFreepsCard("more");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(lord);
		scn.MoveCardsToHand(more);

		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, patrol, brigand);
		// Vitality 3 with 1 wound: of the three wounds of a damage +2 win the Savage takes two.
		scn.AddWoundsToChar(savage, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(2, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		assertInDiscard(savage);
		assertTrue(scn.FreepsPlayAvailable(more));
		scn.FreepsPlayCard(more);

		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, brigand));
		scn.FreepsChooseCard(patrol);

		// One wound was left over, not the two of his bonus.
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInDiscard(more);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyFightingAtHisHomeSiteAssignsHisLeftOverWoundToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var ring = scn.GetFreepsCard("ring");
		var more = scn.GetFreepsCard("more");
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(thrarin, ring);
		scn.MoveCardsToHand(more);

		var scrabbler = scn.GetShadowCard("scrabbler");
		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");

		scn.StartGame();
		// Thrarin's home is site 3: he may be assigned there.
		scn.SkipToSite(2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(scrabbler, savage, patrol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, scrabbler);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thrarin);
		// 4 + 1 for the Ring of Guile against the Scrabbler's 4.
		assertEquals(5, scn.GetStrength(thrarin));
		assertEquals(4, scn.GetStrength(scrabbler));
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		assertInDiscard(scrabbler);
		assertTrue(scn.FreepsPlayAvailable(more));
		scn.FreepsPlayCard(more);

		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, patrol));
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(patrol));
		assertInDiscard(more);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	@Ignore("F-0475: assigning the Runner's remaining wound also takes away the wound of damage +1 that the other minion in the same skirmish was still to take; the text speaks only of the wounds that minion did not take (CR 4.2 \"damage bonus\": each losing character takes the additional wounds)")
	public void AnotherMinionInTheSameSkirmishStillTakesItsOwnDamageBonusWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var more = scn.GetFreepsCard("more");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(more);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var patrol = scn.GetShadowCard("patrol");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage, patrol, brigand);
		// Gimli, 6 + 6, beats the Runner and the Savage, 5 + 5, without doubling them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 6));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.PassSkirmishActions();

		// Two wounds for each loser: the first has killed the Runner, who took one of his two, and wounded the Savage.
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(more));
		scn.FreepsPlayCard(more);

		// The minions nobody was assigned to, and not the Savage, who is in this very skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(patrol, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(patrol);
		assertEquals(1, scn.GetWoundsOn(patrol));
		assertEquals(0, scn.GetWoundsOn(brigand));

		// Only the Runner's wound was left over. The Savage's own wound of Gimli's damage bonus is still his to take.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInDiscard(more);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
