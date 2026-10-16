package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_276_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fortress", "4_276");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; Valiant and no other text
					put("guard2", "5_83");
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5; a skirmish ability only if mounted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					// Merry, Master Holbytla: the one printed [rohan] character who is no Man, a Hobbit companion of strength 3
					// and vitality 4; enduring (strength +2 for each wound on him).  He is a [rohan] Man as well while 3 [rohan]
					// companions or Eowyn can be spotted; a skirmish ability used only on purpose
					put("merry", "103_85");

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its play trigger does nothing when cheated in
					put("runner2", "1_178");
					put("runner3", "1_178");
					// Uruk Pursuer: strength 5, vitality 3, printed fierce; its bar and its response reach no Hobbit here
					put("pursuer", "4_190");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text

					// Shadow conditions of three cultures, in the support area and on a companion.
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] support area condition; fellowship archery -1 while an Uruk-hai is spotted
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] support area condition; minion archery +1 while a [sauron] Orc is spotted
					put("pinned", "1_192");     // Pinned Down: [moria] support area condition; fellowship archery -1 while a [moria] archer is spotted
					put("weary", "4_212");      // Weary: [isengard] condition on a companion; works only on assignment to an [isengard] tracker
					put("weary2", "4_212");
					put("siege", "5_60");       // Siege Engine: [isengard] machine; Response: if machines are about to be discarded by an opponent, discard this to prevent that
					put("ram", "5_44");         // Battering Ram: [isengard] machine; Shadow and skirmish abilities only

					// Doom Drove Them: a [rohan] Free Peoples condition that carries [rohan] tokens of its own; abilities used only on purpose
					put("doom", "8_86");
					put("poison", "8_28");      // Spider Poison: a Shadow possession on a companion, not a condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fortress", "4_276");
					put("sigewulf", "5_92");    // Sigewulf: [rohan] Man ally, home site 4T, strength 4; a regroup ability that needs 5 valiant Men
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FortressNeverFallenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Fortress Never Fallen
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.
		 * Each time a [rohan] Man wins a skirmish, you may place a [rohan] token on this card.
		 * <b>Regroup:</b> Discard a Shadow condition for each [rohan] token here. Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fortress");

		assertEquals("Fortress Never Fallen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedAndOnlyARohanManWinningASkirmishOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(fortress);
		scn.MoveCompanionsToTable(guard, rider, aragorn, merry);
		// Enduring: 3 + 2 for each of his two wounds is 7, against a Goblin Runner's 5.
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.FreepsPlayCard(fortress);
		assertInZone(Zone.SUPPORT, fortress);

		scn.MoveMinionsToTable(runner1, runner2, runner3, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, runner1},
				new PhysicalCardImpl[]{rider, troop},
				new PhysicalCardImpl[]{aragorn, runner2},
				new PhysicalCardImpl[]{merry, runner3});

		// A [rohan] Man loses: nothing is offered.  The Troop's 13 is more than double the Rider's 5, so he is
		// killed, which leaves two [rohan] companions: Merry is a Hobbit and no Man from here on.
		scn.FreepsResolveSkirmish(rider);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, rider);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fortress));

		// A [gondor] Man wins: nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fortress));

		// A [rohan] Hobbit wins: nothing is offered.
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertFalse(scn.IsRace(merry, Race.MAN));
		scn.FreepsResolveSkirmish(merry);
		assertEquals(7, scn.GetStrength(merry));
		scn.PassSkirmishActions();
		assertInDiscard(runner3);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fortress));

		// A [rohan] Man wins.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fortress));
		assertEquals(0, scn.GetCultureTokensOn(fortress));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(fortress));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTokenIsOfferedEachTimeARohanManWinsAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var pursuer = scn.GetShadowCard("pursuer");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(fortress);
		scn.MoveCompanionsToTable(guard, guard2);

		scn.StartGame();
		// The Pursuer is fierce, so that the Guard beats it twice in the turn without killing it.
		scn.MoveMinionsToTable(pursuer, runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, pursuer},
				new PhysicalCardImpl[]{guard2, runner1});

		// The second Guard, 6, beats the Runner, 5: offered, and declined.
		scn.FreepsResolveSkirmish(guard2);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fortress));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(fortress));

		// The Guard, 6, beats the Pursuer, 5: offered again after the refusal, and taken.
		assertEquals(0, scn.GetWoundsOn(pursuer));
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fortress));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(fortress));

		// The same Man wins the fierce skirmish: a second token.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, pursuer);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fortress));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(fortress));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARohanManAllyWinningAtHomeOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var sigewulf = scn.GetFreepsCard("sigewulf");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(fortress);
		scn.MoveAlliesToTable(sigewulf);
		// Strength 6 against the Goblin Runner's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sigewulf, null, 2));

		scn.StartGame();
		scn.SkipToSite(3);
		// The fellowship moves to site 4, White Rocks: Sigewulf's home.
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sigewulf, runner);
		scn.FreepsResolveSkirmish(sigewulf);
		scn.PassSkirmishActions();

		assertInDiscard(runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fortress));
		assertEquals(0, scn.GetCultureTokensOn(fortress));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(fortress));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupDiscardsAShadowConditionForEachTokenHereAndThenItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var doom = scn.GetFreepsCard("doom");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetShadowCard("armory");
		var bowmen = scn.GetShadowCard("bowmen");
		var weary = scn.GetShadowCard("weary");
		var poison = scn.GetShadowCard("poison");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(fortress, doom);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(armory, bowmen);
		scn.AttachCardsTo(aragorn, weary, poison);
		scn.AddTokensToCard(fortress, 2);
		// Three [rohan] tokens on another card, which are not "here".
		scn.AddTokensToCard(doom, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fortress, doom, armory, bowmen);
		assertAttachedTo(weary, aragorn);
		assertAttachedTo(poison, aragorn);
		assertEquals(2, scn.GetCultureTokensOn(fortress));
		assertEquals(3, scn.GetCultureTokensOn(doom));
		// Not a fellowship action.
		assertFalse(scn.FreepsActionAvailable(fortress));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Goblin Runner, so that there is a maneuver phase: the phase before regroup in which the Free Peoples
		// player is asked for actions.
		scn.MoveMinionsToTable(runner1);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fortress));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(fortress));
		scn.FreepsUseCardAction(fortress);

		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, bowmen, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(doom, poison, fortress));
		// Two tokens here: exactly two, not five.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(armory, weary);

		assertInDiscard(armory, weary, fortress);
		assertInZone(Zone.SUPPORT, bowmen, doom);
		assertEquals(3, scn.GetCultureTokensOn(doom));
		assertAttachedTo(poison, aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AtFiveTokensDiscardsFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var armory = scn.GetShadowCard("armory");
		var bowmen = scn.GetShadowCard("bowmen");
		var pinned = scn.GetShadowCard("pinned");
		var weary = scn.GetShadowCard("weary");
		var weary2 = scn.GetShadowCard("weary2");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToSupportArea(fortress);
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(armory, bowmen, pinned, ram);
		scn.AttachCardsTo(aragorn, weary);
		scn.AttachCardsTo(guard, weary2);
		scn.AddTokensToCard(fortress, 5);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fortress, armory, bowmen, pinned, ram);
		assertAttachedTo(weary, aragorn);
		assertAttachedTo(weary2, guard);
		assertEquals(5, scn.GetCultureTokensOn(fortress));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(fortress);

		// Six Shadow conditions, five tokens: five go, with no limit on the count.
		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, bowmen, pinned, weary, weary2, ram));
		assertEquals(5, scn.FreepsGetChoiceMin());
		assertEquals(5, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(armory, bowmen, pinned, weary, weary2);

		assertInDiscard(armory, bowmen, pinned, weary, weary2, fortress);
		assertInZone(Zone.SUPPORT, ram);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithMoreTokensThanShadowConditionsDiscardsEveryOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var doom = scn.GetFreepsCard("doom");
		var armory = scn.GetShadowCard("armory");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToSupportArea(fortress, doom);
		scn.MoveCardsToSupportArea(armory, bowmen);
		scn.AddTokensToCard(fortress, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fortress, doom, armory, bowmen);
		assertEquals(3, scn.GetCultureTokensOn(fortress));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(fortress);

		assertInDiscard(armory, bowmen, fortress);
		assertInZone(Zone.SUPPORT, doom);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoTokensItStillDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(fortress);
		scn.MoveCardsToSupportArea(armory);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fortress, armory);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetCultureTokensOn(fortress));
		assertTrue(scn.FreepsActionAvailable(fortress));
		scn.FreepsUseCardAction(fortress);

		assertInDiscard(fortress);
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SiegeEngineChosenWithTwoOtherShadowConditionsSavesAllThreeWithOneResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var siege = scn.GetShadowCard("siege");
		var ram = scn.GetShadowCard("ram");
		var armory = scn.GetShadowCard("armory");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToSupportArea(fortress);
		scn.MoveCardsToSupportArea(siege, ram, armory, bowmen);
		scn.AddTokensToCard(fortress, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, fortress, siege, ram, armory, bowmen);
		assertEquals(3, scn.GetCultureTokensOn(fortress));
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(fortress);
		// Three tokens, three Shadow conditions chosen: Siege Engine, another machine and a condition that is none.
		scn.FreepsChooseCards(siege, ram, armory);

		// The three are discarded at the same time, and one response answers them all (CR 4.2 ruling on this card).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(siege));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SUPPORT, ram, armory, bowmen);
		// Siege Engine is discarded to pay for its own response; the Fortress by its own last sentence.
		assertInDiscard(siege, fortress);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(siege));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
