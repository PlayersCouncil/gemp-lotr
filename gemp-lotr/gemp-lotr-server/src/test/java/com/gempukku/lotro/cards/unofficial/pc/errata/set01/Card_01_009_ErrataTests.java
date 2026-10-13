package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_009_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "51_9");
					put("axe2", "51_9");

					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no damage bonus
					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, home site 3, strength 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a Man

					put("savage", "1_151");       // Uruk Savage: [isengard], strength 5, vitality 3
					put("savage2", "1_151");
					put("soldier", "1_271");      // Orc Soldier: [sauron], strength 7, vitality 2
					put("runner", "1_178");       // Goblin Runner: [moria], strength 5, vitality 1

					put("numenor", "5_37");       // Men of Numenor: Skirmish event, wound a minion bearing a [gondor] fortification
					put("wall", "5_33");          // City Wall: [gondor] fortification; its bearer is strength -3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A table whose Shadow draw deck is four cards once both Savages are down: the Shadow player's own copies of
	// the Axe, the Guard, Gimli and the Runner.
	protected VirtualTableScenario GetSmallDeckScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "51_9");
					put("guard", "1_7");          // Dwarf Guard: strength 4, no damage bonus
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, damage +1
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("savage2", "1_151");
					put("runner", "1_178");       // Goblin Runner: only ever a card in the deck here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dwarven Axe
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a Dwarf.
		 * Each time a minion loses a skirmish to bearer, you may make a minion strength -1 until the regroup
		 * phase.  The Shadow player may discard 2 cards from the bottom of their draw deck to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Dwarven Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnADwarfCompanionOrAllyWhoIsThenStrengthPlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToHand(axe);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();

		assertEquals(3, scn.GetStrength(grimir));
		assertTrue(scn.FreepsPlayAvailable(axe));

		scn.FreepsPlayCard(axe);
		// A Dwarf companion and a Dwarf ally; not Aragorn, a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));

		scn.FreepsChooseCard(grimir);
		assertAttachedTo(axe, grimir);
		assertEquals(5, scn.GetStrength(grimir));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMinionLosingToBearerLetsAnyMinionBeMadeStrengthMinus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var otherAxe = scn.GetFreepsCard("axe2");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(guard, axe);
		// A second Axe, on a Dwarf who fights nobody this turn: it has nothing to say about the Guard's win.
		scn.AttachCardsTo(grimir, otherAxe);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{gimli, savage2});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(savage));

		// 6 against 5: the Savage loses to the Guard, and takes the wound for losing.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage));

		// The minion that lost, the minion waiting for Gimli, and the minion nobody is fighting.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, savage2, soldier));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, gimli, grimir));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		int shadowDeck = scn.GetShadowDeckCount();
		scn.FreepsChooseCard(savage2);
		assertTrue(scn.ShadowDecisionAvailable("discard the bottom 2 cards"));
		assertEquals(5, scn.GetStrength(savage2));

		scn.ShadowChooseNo();
		assertEquals(4, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Still weakened in the skirmish that follows.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(4, scn.GetStrength(savage2));

		// The second Savage loses to Gimli, who bears no Axe: 2 wounds for his damage +1 and nothing more.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetStrength(savage2));
	}

	@Test
	public void ChoosingNoMinionLeavesEveryMinionAsItWasAndTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		int shadowDeck = scn.GetShadowDeckCount();
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, savage2));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineChoosing();
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(savage2));
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NothingIsAskedWhenBearerLosesTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var soldier = scn.GetShadowCard("soldier");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier}, new PhysicalCardImpl[]{gimli, savage});
		scn.FreepsResolveSkirmish(guard);

		// 6 against 7: the Guard loses to the Soldier.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(7, scn.GetStrength(soldier));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NothingIsAskedWhenBearersWinKillsTheOnlyMinionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var gimli = scn.GetFreepsCard("gimli");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);

		int shadowDeck = scn.GetShadowDeckCount();
		assertEquals(8, scn.GetStrength(gimli));

		// 8 against 5; the Runner has 1 vitality. Once the wounds are placed there is no minion left to weaken.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscardTheBottom2CardsOfHisDrawDeckToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmallDeckScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var shadowAxe = scn.GetShadowCard("axe");
		var shadowGuard = scn.GetShadowCard("guard");
		var shadowGimli = scn.GetShadowCard("gimli");
		var shadowRunner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.MoveCardsToBottomOfDeck(shadowGimli, shadowRunner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		int freepsDeck = scn.GetFreepsDeckCount();
		assertEquals(4, scn.GetShadowDeckCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.DECK, shadowAxe, shadowGuard, shadowGimli, shadowRunner);

		scn.FreepsChooseCard(savage2);
		assertTrue(scn.ShadowDecisionAvailable("discard the bottom 2 cards"));

		scn.ShadowChooseYes();
		assertInZone(Zone.DISCARD, shadowGimli, shadowRunner);
		assertInZone(Zone.DECK, shadowAxe, shadowGuard);
		assertEquals(2, scn.GetShadowDeckCount());
		assertEquals(2, scn.GetShadowDiscardCount());
		assertEquals(freepsDeck, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		// Prevented: the Savage is as strong as ever.
		assertEquals(5, scn.GetStrength(savage2));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TheShadowPlayerIsNotAskedWithOneCardInHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmallDeckScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		// Three of the Shadow player's four cards leave his draw deck; his Runner stays.
		scn.MoveCardsToShadowDiscard("axe", "guard", "gimli");
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(1, scn.GetShadowDeckCount());
		assertEquals(5, scn.GetStrength(savage2));

		scn.FreepsChooseCard(savage2);
		assertFalse(scn.ShadowDecisionAvailable("discard the bottom 2 cards"));
		assertEquals(4, scn.GetStrength(savage2));
		assertEquals(1, scn.GetShadowDeckCount());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AFierceMinionThatLosesToBearerTwiceLetsAMinionBeWeakenedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, axe);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.MINION, null, Keyword.FIERCE));

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(guard);

		// The first Savage loses to the Guard, 6 to 5.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		scn.FreepsChooseCard(savage2);
		scn.ShadowChooseNo();
		assertEquals(4, scn.GetStrength(savage2));

		// Gimli, 6, beats the second Savage, 4: 2 wounds of its 3.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(savage2));

		// The fierce skirmishes: the first Savage loses to the Guard a second time.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{gimli, savage2});
		scn.FreepsResolveSkirmish(guard);
		assertEquals(4, scn.GetStrength(savage2));

		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, savage2));

		scn.FreepsChooseCard(savage2);
		scn.ShadowChooseNo();
		// 5 - 1 - 1
		assertEquals(3, scn.GetStrength(savage2));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void AMinionKilledDuringTheSkirmishWhileAnotherFightsOnHasNotLostToBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, axe);
		scn.MoveCardsToHand(numenor);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, soldier);
		// The Runner bears a fortification, so that Men of Numenor can wound it.
		scn.AttachCardsTo(runner, wall);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.ShadowAssignToMinions(guard, soldier);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.IsCharSkirmishingAgainst(guard, runner));
		assertTrue(scn.IsCharSkirmishingAgainst(guard, soldier));

		// The Runner dies in the middle of the skirmish and the Soldier fights on: the Runner neither won nor lost.
		scn.FreepsPlayCard(numenor);
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 6 against 7: the Guard loses to the Soldier, and no minion has lost a skirmish to him.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLastMinionKilledDuringTheSkirmishHasLostToBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		var runner = scn.GetShadowCard("runner");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, axe);
		scn.MoveCardsToHand(numenor);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, soldier);
		// The Runner bears a fortification, so that Men of Numenor can wound it.
		scn.AttachCardsTo(runner, wall);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner}, new PhysicalCardImpl[]{gimli, soldier});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(7, scn.GetStrength(soldier));

		// The Guard's only opponent dies before strengths are compared: the skirmish is over and the Runner lost it.
		scn.FreepsPlayCard(numenor);
		assertInZone(Zone.DISCARD, runner);
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.FreepsHasCardChoiceAvailable(soldier));

		scn.FreepsChooseCard(soldier);
		scn.ShadowChooseNo();
		assertEquals(6, scn.GetStrength(soldier));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void TwoMinionsLosingOneSkirmishToBearerAreTwoTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");
		var guard = scn.GetFreepsCard("guard");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCompanionsToTable(guard, gimli);
		scn.AttachCardsTo(guard, axe);
		// 4 + 2 for the Axe + 5: the Guard beats two Savages, 11 to 10.
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard, null, 5));

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, savage}, new PhysicalCardImpl[]{gimli, soldier});
		scn.ShadowAssignToMinions(guard, savage2);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(11, scn.GetStrength(guard));
		assertEquals(7, scn.GetStrength(soldier));

		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(savage2));

		// Once for the first Savage ...
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, savage2, soldier));
		scn.FreepsChooseCard(soldier);
		scn.ShadowChooseNo();
		assertEquals(6, scn.GetStrength(soldier));

		// ... and once for the second.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, savage2, soldier));
		scn.FreepsChooseCard(soldier);
		scn.ShadowChooseNo();
		assertEquals(5, scn.GetStrength(soldier));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

}
