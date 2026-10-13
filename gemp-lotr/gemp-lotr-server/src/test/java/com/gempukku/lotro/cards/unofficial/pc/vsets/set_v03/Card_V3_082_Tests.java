package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_082_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("death", "103_82");
					put("death2", "103_82");
					put("rider", "4_286");        // Rider of Rohan: [rohan] companion, strength 5; his ability needs a mount
					put("guard", "5_83");         // Household Guard: [rohan] companion, strength 6; valiant and no text
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] companion, strength 8
					put("aldor", "4_262");        // Aldor, Soldier of Edoras: [rohan] ally, strength 3; an archery ability that needs a villager
					put("lordofmoria", "1_21");   // Lord of Moria: Free Peoples condition in the support area

					put("swarms1", "1_183");      // Goblin Swarms: [moria] condition, cost 1, support area
					put("swarms2", "1_183");
					put("swarms3", "1_183");
					put("ships", "8_65");         // Ships of Great Draught: [raider] possession, cost 2, support area
					put("hollowing", "3_54");     // Hollowing of Isengard: [isengard] condition, cost 4, support area
					put("stone", "9_47");         // Ithil Stone: [sauron] artifact, cost 0, support area; may draw at the start of the Shadow phase
					put("fear", "103_64");        // Cover of Darkness, Omen of Fear: [ringwraith] condition, cost 2, two dots; its ability needs 3 [ringwraith] minions
					put("dread", "103_94");       // Cover of Darkness, Omen of Dread: [sauron] condition, cost 2, two dots; answers a burden or a threat being added
					put("poison", "8_28");        // Spider Poison: Shadow possession, cost 1, that goes from the support area onto a companion; idle until its bearer is assigned
					put("poison2", "8_28");
					// Weary: [isengard] condition, cost 1. Plays on a companion or ally; answers its bearer being
					// assigned to an [isengard] tracker and nothing else
					put("weary1", "4_212");
					put("weary2", "4_212");
					put("runner1", "1_178");      // Goblin Runners, to be stacked
					put("runner2", "1_178");
					put("runner3", "1_178");
					put("runner4", "1_178");
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3
					put("savage2", "1_151");      // a second one, for a second skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeathTakeUsAllStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Death Take Us All
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: The Shadow player may hinder any of their own support cards.
		 * 	Spot an unhindered Shadow support card.  Make a [rohan] companion strength +1 for each of the
		 * 	following: that card's twilight cost, cards stacked on that card, culture tokens on that card,
		 * 	copies of that card you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("death");

		assertEquals("Death Take Us All", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesARohanCompanionStrongerByCostStackedCardsTokensAndCopiesOfTheSpottedCardForTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var death2 = scn.GetFreepsCard("death2");
		var frodo = scn.GetRingBearer();
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aldor = scn.GetFreepsCard("aldor");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var swarms1 = scn.GetShadowCard("swarms1");
		var swarms2 = scn.GetShadowCard("swarms2");
		var swarms3 = scn.GetShadowCard("swarms3");
		var ships = scn.GetShadowCard("ships");
		var hollowing = scn.GetShadowCard("hollowing");
		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(rider, guard, aragorn);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToSupportArea(lordofmoria);
		scn.MoveCardsToHand(death, death2);
		scn.MoveCardsToSupportArea(swarms1, swarms2, ships, hollowing);
		// Every part of the sum is a different number, and the Ships have cards and tokens of their own.
		// The three cards stacked on the Swarms are two minions and a condition, a third Goblin Swarms.
		scn.StackCardsOn(swarms1, scn.GetShadowCard("runner1"), scn.GetShadowCard("runner2"), swarms3);
		scn.StackCardsOn(ships, scn.GetShadowCard("runner4"));
		scn.AddTokensToCard(swarms1, 5);
		scn.AddTokensToCard(ships, 4);
		// A Shadow card that is in no support area.
		scn.AttachCardsTo(guard, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);

		// A skirmish event: not in the assignment phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(death));

		scn.PassCurrentPhaseActions();
		// Two skirmishes, the Rider's first.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, savage}, new PhysicalCardImpl[]{aragorn, savage2});
		scn.FreepsResolveSkirmish(rider);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(death));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(aldor));
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(death);

		assertEquals(pool + 3, scn.GetTwilight());

		// First the Shadow player may hinder support cards of his own: any number, or none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swarms1, swarms2, ships, hollowing));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, poison, lordofmoria, swarms3));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowDeclineChoosing();

		assertFalse(scn.IsHindered(swarms1));
		assertFalse(scn.IsHindered(swarms2));
		assertFalse(scn.IsHindered(ships));
		assertFalse(scn.IsHindered(hollowing));

		// Then a Shadow support card is spotted.
		assertTrue(scn.FreepsHasCardChoiceAvailable(swarms1, swarms2, ships, hollowing));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(savage, poison, lordofmoria, swarms3));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(swarms1);

		// Then a [rohan] companion, in the skirmish or out of it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, aldor, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(rider);

		// 5 + 1 for the Swarms' twilight cost + 3 cards stacked on it + 5 tokens on it + 2 Goblin Swarms that can
		// be spotted (the third is stacked, and a stacked card cannot be spotted)
		assertEquals(16, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(3, scn.GetStrength(aldor));
		assertInDiscard(death);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The skirmish ends (16 against 5: the Savage is overwhelmed) and so does the bonus: in the next skirmish
		// of the same turn, Aragorn's, the Rider is 5 again. A bonus that lasted until the regroup phase would
		// still be on him here.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(rider));

		// Aragorn wins at 8 against 5; the event cannot be played in the regroup phase either.
		scn.FreepsPass();
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(death2));
	}

	@Test
	public void CardsTheShadowPlayerHindersCannotBeSpottedAndAHinderedCopyIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var swarms1 = scn.GetShadowCard("swarms1");
		var swarms2 = scn.GetShadowCard("swarms2");
		var swarms3 = scn.GetShadowCard("swarms3");
		var ships = scn.GetShadowCard("ships");
		var hollowing = scn.GetShadowCard("hollowing");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(rider, guard);
		scn.MoveCardsToHand(death);
		scn.MoveCardsToSupportArea(swarms1, swarms2, swarms3, ships, hollowing);
		scn.StackCardsOn(swarms1, scn.GetShadowCard("runner1"), scn.GetShadowCard("runner2"), scn.GetShadowCard("runner3"));
		scn.AddTokensToCard(swarms1, 5);

		scn.StartGame();
		// One copy is hindered before the event is played.
		scn.HinderCard(swarms3);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rider, savage);

		assertEquals(6, scn.GetStrength(guard));

		scn.FreepsPlayCard(death);

		// A card already hindered is not offered for hindering again.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swarms1, swarms2, ships, hollowing));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(swarms3));
		assertEquals(4, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(swarms2, hollowing);

		assertTrue(scn.IsHindered(swarms2));
		assertTrue(scn.IsHindered(hollowing));
		assertFalse(scn.IsHindered(swarms1));
		assertFalse(scn.IsHindered(ships));

		assertTrue(scn.FreepsHasCardChoiceAvailable(swarms1, ships));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(swarms2, swarms3, hollowing));

		scn.FreepsChooseCard(swarms1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, guard));
		scn.FreepsChooseCard(guard);

		// 6 + 1 for the twilight cost + 3 stacked + 5 tokens + 1 Goblin Swarms that can be spotted (the other two are hindered)
		assertEquals(16, scn.GetStrength(guard));
		assertEquals(5, scn.GetStrength(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithEverySupportCardHinderedByTheShadowPlayerNothingIsSpottedAndNobodyIsStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var ships = scn.GetShadowCard("ships");
		var hollowing = scn.GetShadowCard("hollowing");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(rider, guard);
		scn.MoveCardsToHand(death);
		scn.MoveCardsToSupportArea(ships, hollowing);
		scn.AddTokensToCard(ships, 4);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rider, savage);

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(death);

		assertTrue(scn.ShadowHasCardChoiceAvailable(ships, hollowing));
		scn.ShadowChooseCards(ships, hollowing);

		assertTrue(scn.IsHindered(ships));
		assertTrue(scn.IsHindered(hollowing));
		// The event was paid for and is spent, and has nothing to spot.
		assertEquals(pool + 3, scn.GetTwilight());
		assertInDiscard(death);
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(guard));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoShadowSupportCardInPlayItCanStillBePlayedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(rider, guard);
		scn.MoveCardsToHand(death);
		// The nearest things to a Shadow support card: a Free Peoples card in a support area, a Shadow card
		// borne by a companion, and a minion.
		scn.MoveCardsToSupportArea(lordofmoria);
		scn.AttachCardsTo(guard, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rider, savage);

		// Its spot comes after the Shadow player's hindering and is no requirement for playing it.
		assertTrue(scn.FreepsPlayAvailable(death));
		int pool = scn.GetTwilight();

		scn.FreepsPlayCard(death);

		assertEquals(pool + 3, scn.GetTwilight());
		assertInDiscard(death);
		assertFalse(scn.IsHindered(poison));
		assertEquals(5, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(guard));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACardThatCostsNothingAndHasNothingOnItStillCountsAsOneCopyOfItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var stone = scn.GetShadowCard("stone");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rider, savage);
		// Put down here, past the Shadow phase its own trigger answers: an artifact of cost 0.
		scn.MoveCardsToSupportArea(stone);

		assertEquals(5, scn.GetStrength(rider));

		scn.FreepsPlayCard(death);
		assertTrue(scn.ShadowHasCardChoiceAvailable(stone));
		scn.ShadowDeclineChoosing();

		// The Stone is the only card to spot and the Rider the only [rohan] companion: neither is asked.
		// 5 + 0 for the twilight cost + 0 stacked + 0 tokens + 1 Ithil Stone that can be spotted
		assertEquals(6, scn.GetStrength(rider));
		assertInDiscard(death);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACopyBorneByACompanionIsCountedThoughItIsInNoSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var poison = scn.GetShadowCard("poison");
		var poison2 = scn.GetShadowCard("poison2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(rider, guard);
		scn.MoveCardsToHand(death);
		// Two Spider Poisons: one still in the support area, one already on a companion.
		scn.MoveCardsToSupportArea(poison);
		scn.AttachCardsTo(guard, poison2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rider, savage);

		assertEquals(5, scn.GetStrength(rider));

		scn.FreepsPlayCard(death);
		assertTrue(scn.ShadowHasCardChoiceAvailable(poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison2));
		scn.ShadowDeclineChoosing();

		// The Poison in the support area is the only card that can be spotted, so the companion is asked next.
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider, guard));
		scn.FreepsChooseCard(rider);

		// 5 + 1 for the twilight cost + 0 stacked + 0 tokens + 2 Spider Poisons that can be spotted, wherever they are
		assertEquals(8, scn.GetStrength(rider));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AShadowCardAnAllyBearsIsASupportCardForBothPlayersChoicesAndOneACompanionBearsIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var aldor = scn.GetFreepsCard("aldor");
		var swarms1 = scn.GetShadowCard("swarms1");
		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(rider, guard);
		scn.MoveAlliesToTable(aldor);
		scn.MoveCardsToHand(death);
		scn.MoveCardsToSupportArea(swarms1);
		// The same Shadow condition on an ally, who is in a support area himself, and on a companion.
		scn.AttachCardsTo(aldor, weary1);
		scn.AttachCardsTo(guard, weary2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignAndResolve(rider, savage);

		assertEquals(5, scn.GetStrength(rider));
		scn.FreepsPlayCard(death);

		// What a card in a support area bears is in that support area: the Weary on the ally is one of the
		// Shadow player's support cards, and the one on the Household Guard is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swarms1, weary1));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(weary2, aldor));
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowDeclineChoosing();

		assertTrue(scn.FreepsHasCardChoiceAvailable(swarms1, weary1));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(weary2, aldor));
		scn.FreepsChooseCard(weary1);
		scn.FreepsChooseCard(rider);

		// 5 + 1 for Weary's twilight cost + 2 Wearies that can be spotted; nothing is stacked on it and it has no
		// tokens.
		assertEquals(8, scn.GetStrength(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACardOfTheSameTitleUnderAnotherSubtitleIsCountedAsACopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var death = scn.GetFreepsCard("death");
		var rider = scn.GetFreepsCard("rider");
		var fear = scn.GetShadowCard("fear");
		var dread = scn.GetShadowCard("dread");
		var ships = scn.GetShadowCard("ships");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(death);
		// Two Covers of Darkness, which two dots allow, with different subtitles.
		scn.MoveCardsToSupportArea(fear, dread, ships);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rider, savage);

		assertEquals(5, scn.GetStrength(rider));

		scn.FreepsPlayCard(death);
		scn.ShadowDeclineChoosing();
		assertTrue(scn.FreepsHasCardChoiceAvailable(fear, dread, ships));
		scn.FreepsChooseCard(fear);

		// 5 + 2 for the twilight cost + 0 stacked + 0 tokens + 2 Covers of Darkness that can be spotted
		assertEquals(9, scn.GetStrength(rider));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
