package com.gempukku.lotro.cards.unofficial.pc.errata.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_276_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fortress", "54_276");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6; Valiant and no other text
					put("rider", "4_286");      // Rider of Rohan: [rohan] Man companion, strength 5; a skirmish ability only if mounted
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, strength 6, damage +1

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its play trigger does nothing when cheated in
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and no other text

					// Shadow conditions of three cultures, in the support area and on a companion.
					put("armory", "1_157");     // Uruk-hai Armory: [isengard] support area condition; fellowship archery -1 while an Uruk-hai is spotted
					put("bowmen", "1_264");     // Orc Bowmen: [sauron] support area condition; minion archery +1 while a [sauron] Orc is spotted
					put("pinned", "1_192");     // Pinned Down: [moria] support area condition; fellowship archery -1 while a [moria] archer is spotted
					put("weary", "4_212");      // Weary: [isengard] condition on a companion; works only on assignment to an [isengard] tracker
					put("weary2", "4_212");
					put("siege", "5_60");       // Siege Engine: [isengard] machine; Response: if machines are about to be discarded by an opponent, discard this to prevent that
					put("ram", "5_44");         // Battering Ram: [isengard] machine; Shadow and skirmish abilities only

					put("songs", "1_61");       // Songs of the Blessed Realm: a Free Peoples condition in the support area
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
					put("fortress", "54_276");
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
		 * Game Text: Fortification.
		 * Each time a [Rohan] Man wins a skirmish, you may place a [Rohan] token on this card.
		 * Regroup: Discard a Shadow condition
		 * for each [Rohan] token here (limit 4).
		 * Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fortress");

		assertEquals("Fortress Never Fallen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.FORTIFICATION));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForTwoAndOnlyARohanManWinningASkirmishOffersAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var guard = scn.GetFreepsCard("guard");
		var rider = scn.GetFreepsCard("rider");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(fortress);
		scn.MoveCompanionsToTable(guard, rider, aragorn, gimli);
		// Gimli is [rohan] as well as [dwarven] here: a [rohan] character who is no Man.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, gimli, null, Culture.ROHAN));

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(fortress);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fortress);

		scn.MoveMinionsToTable(runner1, runner2, runner3, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, runner1},
				new PhysicalCardImpl[]{rider, savage},
				new PhysicalCardImpl[]{aragorn, runner2},
				new PhysicalCardImpl[]{gimli, runner3});

		// A [rohan] Man loses (a tie goes to the minion): nothing is offered.
		scn.FreepsResolveSkirmish(rider);
		scn.PassSkirmishActions();
		// 1 wound + damage +1
		assertEquals(2, scn.GetWoundsOn(rider));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fortress));

		// A [gondor] Man wins: nothing is offered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(fortress));

		// A [rohan] Dwarf wins: nothing is offered.
		scn.FreepsResolveSkirmish(gimli);
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
		var rider = scn.GetFreepsCard("rider");
		var savage = scn.GetShadowCard("savage");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(fortress);
		scn.MoveCompanionsToTable(guard, rider);
		// Strength 6 against the Goblin Runner's 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, rider, null, 1));

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner1);
		// The Savage made fierce, so that the Guard beats it twice in the turn without killing it.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, savage},
				new PhysicalCardImpl[]{rider, runner1});

		scn.FreepsResolveSkirmish(rider);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fortress));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(fortress));

		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(fortress));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(fortress));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
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
	public void RegroupDiscardsAShadowConditionForEachTokenAndThenItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var songs = scn.GetFreepsCard("songs");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armory = scn.GetShadowCard("armory");
		var bowmen = scn.GetShadowCard("bowmen");
		var weary = scn.GetShadowCard("weary");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCardsToSupportArea(fortress, songs);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(armory, bowmen);
		scn.AttachCardsTo(aragorn, weary, poison);
		scn.AddTokensToCard(fortress, 2);

		scn.StartGame();
		assertFalse(scn.FreepsActionAvailable(fortress));

		// No minions: the game goes from the Shadow phase straight to regroup.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(fortress));
		scn.FreepsUseCardAction(fortress);

		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, bowmen, weary));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(songs, poison, fortress));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(armory, weary);

		assertInDiscard(armory, weary, fortress);
		assertInZone(Zone.SUPPORT, bowmen, songs);
		assertAttachedTo(poison, aragorn);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AtFiveTokensDiscardsNoMoreThanFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var armory = scn.GetShadowCard("armory");
		var bowmen = scn.GetShadowCard("bowmen");
		var pinned = scn.GetShadowCard("pinned");
		var weary = scn.GetShadowCard("weary");
		var weary2 = scn.GetShadowCard("weary2");
		scn.MoveCardsToSupportArea(fortress);
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(armory, bowmen, pinned);
		scn.AttachCardsTo(aragorn, weary);
		scn.AttachCardsTo(guard, weary2);
		scn.AddTokensToCard(fortress, 5);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(fortress);

		assertTrue(scn.FreepsHasCardChoiceAvailable(armory, bowmen, pinned, weary, weary2));
		assertEquals(4, scn.FreepsGetChoiceMin());
		assertEquals(4, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(armory, bowmen, pinned, weary);

		assertInDiscard(armory, bowmen, pinned, weary, fortress);
		assertAttachedTo(weary2, guard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithMoreTokensThanShadowConditionsDiscardsEveryOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var songs = scn.GetFreepsCard("songs");
		var armory = scn.GetShadowCard("armory");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCardsToSupportArea(fortress, songs);
		scn.MoveCardsToSupportArea(armory, bowmen);
		scn.AddTokensToCard(fortress, 3);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(fortress);

		assertInDiscard(armory, bowmen, fortress);
		assertInZone(Zone.SUPPORT, songs);
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
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetCultureTokensOn(fortress));
		assertTrue(scn.FreepsActionAvailable(fortress));
		scn.FreepsUseCardAction(fortress);

		assertInDiscard(fortress);
		assertInZone(Zone.SUPPORT, armory);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void SiegeEngineSavesEveryMachineDiscardedWithOneResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortress = scn.GetFreepsCard("fortress");
		var siege = scn.GetShadowCard("siege");
		var ram = scn.GetShadowCard("ram");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToSupportArea(fortress);
		scn.MoveCardsToSupportArea(siege, ram, armory);
		scn.AddTokensToCard(fortress, 2);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsUseCardAction(fortress);
		scn.FreepsChooseCards(siege, ram);

		// One response answers both discards, which happen together (CR 4.2 ruling on this card).
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(siege));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.SUPPORT, ram, armory);
		// Siege Engine is discarded to pay for its own response.
		assertInDiscard(siege, fortress);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(siege));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
