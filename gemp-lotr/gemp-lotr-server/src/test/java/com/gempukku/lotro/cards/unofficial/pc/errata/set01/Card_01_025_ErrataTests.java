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

public class Card_01_025_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breath", "51_25");

					put("gimli", "1_13");         // Gimli, Son of Gloin: Dwarf companion, strength 6, vitality 3, damage +1
					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion, strength 5 (7 against an Orc), vitality 3
					put("guard", "1_7");          // Dwarf Guard: Dwarf companion, strength 4, vitality 2, no text in play
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally, home site 3, strength 3, vitality 3
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("armor", "1_8");          // Dwarven Armor: a Free Peoples card to hold in hand
					put("heart", "1_10");         // Dwarven Heart: another

					put("runner", "1_178");       // Goblin Runner: Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("savage", "1_151");       // Uruk Savage: Uruk-hai, strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StillDrawsBreathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Still Draws Breath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Tale.</b>
		 * Each time a Dwarf wins a skirmish, you may discard a card from hand to heal that Dwarf.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("breath");

		assertEquals("Still Draws Breath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AWoundedDwarfWhoWinsASkirmishMayDiscardACardFromHandToHealHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		// The player's own copy of a Shadow card, in his hand: "a card" is any card.
		var ownRunner = scn.GetFreepsCard("runner");
		var heart = scn.GetFreepsCard("heart");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(breath);
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AddWoundsToChar(gimli, 2);
		// Another wounded Dwarf, who wins nothing.
		scn.AddWoundsToChar(guard, 1);
		scn.MoveCardsToHand(armor, ownRunner);
		scn.MoveCardsToDiscard(heart);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(breath));

		// Gimli, 6, beats the Runner, 5.
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(gimli));

		scn.FreepsChooseOptionalTrigger(breath);
		assertTrue(scn.FreepsHasCardChoiceAvailable(armor, ownRunner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(heart, breath));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(ownRunner);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, ownRunner);
		assertInZone(Zone.HAND, armor);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOfferMayBeDeclinedAndThenNoCardIsDiscardedAndNoWoundHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(breath);
		scn.MoveCompanionsToTable(gimli);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveCardsToHand(armor, heart);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, armor);
		assertInZone(Zone.HAND, heart);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithAnEmptyHandAndIsOfferedWithOneCardInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var armor = scn.GetFreepsCard("armor");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(breath);
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(farin, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{farin, runner2});
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetFreepsHandCount());

		// Gimli wins with nothing in hand to discard.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Farin, 7 against an Orc, wins with one card in hand: it is the card discarded.
		scn.MoveCardsToHand(armor);
		scn.FreepsResolveSkirmish(farin);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));
		assertEquals(1, scn.GetWoundsOn(farin));

		scn.FreepsChooseOptionalTrigger(breath);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, armor);
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfWhoLosesAndAManWhoWinsAreNotOfferedTheHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(breath);
		scn.MoveCompanionsToTable(guard, aragorn);
		// A wounded companion who is no Dwarf.
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(armor, heart);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, runner}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(guard));

		// The Guard, 4, loses to the Runner, 5, and is wounded for it.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Aragorn, 8, beats the other Runner.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfAllyAndADwarfCompanionAreEachHealedByTheirOwnWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var grimir = scn.GetFreepsCard("grimir");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(breath);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCompanionsToTable(gimli);
		// Grimir, 3 + 3, can beat a Runner, 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, grimir, null, 3));

		scn.StartGame();
		// Grimir's home is site 3: the fellowship moves there from site 2.
		scn.SkipToSite(2);
		scn.AddWoundsToChar(grimir, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.MoveCardsToHand(armor, heart);
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{grimir, runner}, new PhysicalCardImpl[]{gimli, runner2});
		scn.FreepsResolveSkirmish(grimir);
		int hand = scn.GetFreepsHandCount();

		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));

		scn.FreepsChooseOptionalTrigger(breath);
		scn.FreepsChooseCard(armor);
		assertInZone(Zone.DISCARD, armor);
		assertEquals(hand - 1, scn.GetFreepsHandCount());
		// The ally who won is healed; Gimli, wounded and waiting for his own skirmish, is not.
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Gimli, 6, beats the other Runner.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));

		scn.FreepsChooseOptionalTrigger(breath);
		scn.FreepsChooseCard(heart);
		assertInZone(Zone.DISCARD, heart);
		assertEquals(hand - 2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarfWhoBeatsAFierceMinionTwiceIsOfferedTheHealEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var ownRunner = scn.GetFreepsCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(breath);
		scn.MoveCompanionsToTable(gimli);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveCardsToHand(armor, heart, ownRunner);
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, CardType.MINION, null, Keyword.FIERCE));

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, savage);

		// Gimli, 6, beats the Savage, 5: 2 wounds of its 3, with his damage +1.
		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));

		scn.FreepsChooseOptionalTrigger(breath);
		scn.FreepsChooseCard(armor);
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The fierce skirmish: he beats it again.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(gimli, savage);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(breath));

		scn.PassFierceSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));

		scn.FreepsChooseOptionalTrigger(breath);
		assertTrue(scn.FreepsHasCardChoiceAvailable(heart, ownRunner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor));

		scn.FreepsChooseCard(heart);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, armor);
		assertInZone(Zone.DISCARD, heart);
		assertInZone(Zone.HAND, ownRunner);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OneSkirmishWonAgainstTwoMinionsIsOneOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(breath);
		scn.MoveCompanionsToTable(gimli);
		scn.AddWoundsToChar(gimli, 2);
		// Gimli, 6 + 5, can beat two Runners, 5 + 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, gimli, null, 5));
		scn.MoveCardsToHand(armor, heart);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		// The Shadow player sends the second Runner into the same skirmish.
		scn.FreepsAssignToMinions(gimli, runner);
		scn.ShadowAssignToMinions(gimli, runner2);
		scn.FreepsResolveSkirmish(gimli);

		scn.PassSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));

		scn.FreepsChooseOptionalTrigger(breath);
		scn.FreepsChooseCard(armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, heart);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUnwoundedDwarfWhoWinsIsStillOfferedTheDiscardAndNothingIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var breath = scn.GetFreepsCard("breath");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var armor = scn.GetFreepsCard("armor");
		var heart = scn.GetFreepsCard("heart");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(breath);
		scn.MoveCompanionsToTable(gimli, guard);
		// The Dwarf who does not fight is the wounded one.
		scn.AddWoundsToChar(guard, 1);
		scn.MoveCardsToHand(armor, heart);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, runner);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gimli));
		// A cost may be paid for its own sake, whatever the effect will do.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(breath));

		scn.FreepsChooseOptionalTrigger(breath);
		scn.FreepsChooseCard(heart);
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.DISCARD, heart);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
