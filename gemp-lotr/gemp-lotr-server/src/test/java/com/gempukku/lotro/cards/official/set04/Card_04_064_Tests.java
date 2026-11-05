package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_064_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "4_64");
					put("sword2", "4_64");
					put("legolas", "1_50");    // Legolas, Greenleaf: Elf companion, strength 6, vitality 3; archery ability used only on purpose
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3, no archer (Legolas would bring an archery phase); +3 only against a Nazgul
					put("haldir", "1_48");     // Haldir, Elf of the Golden Wood: Elf companion, no archer; +2 only at sites 6 to 8
					put("orophin", "1_56");    // Orophin, Lorien Bowman: Elf ally; archery ability used only on purpose
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: a Man companion
					put("chaff1", "1_3");      // Axe Strike: a [dwarven] event, here only as a card in hand
					put("chaff2", "1_3");

					put("runner", "1_178");    // Goblin Runner: [moria] Orc, strength 5; its play trigger is not reached by a cheat
					put("axeman", "4_224");    // Easterling Axeman: [raider] Man, strength 6; +3 and fierce only at 2 burdens
					put("troop", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("ferocity", "4_151");  // Ferocity: a Shadow event, here only as a card in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Elven Sword
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Game Text: Bearer must be an Elf.<br><b>Skirmish:</b> Exert bearer or discard 2 cards from hand to make a minion skirmishing bearer strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Elven Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnElfCompanionOrAnElfAllyAndNotOnAMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		assertInZone(Zone.HAND, sword);
		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(orophin);

		assertAttachedTo(sword, orophin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingBearerMakesAMinionSkirmishingBearerStrengthMinusOneForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var sword2 = scn.GetFreepsCard("sword2");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, haldir, aragorn);
		scn.AttachCardsTo(arwen, sword);
		scn.AttachCardsTo(haldir, sword2);
		scn.MoveCardsToHand(scn.GetFreepsCard("chaff1"), scn.GetFreepsCard("chaff2"));

		var runner = scn.GetShadowCard("runner");
		var axeman = scn.GetShadowCard("axeman");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, axeman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, runner},
				new PhysicalCardImpl[]{aragorn, troop});
		// The leftover minion is the Shadow player's to assign: two minions of other cultures and races skirmish Arwen.
		scn.ShadowAssignToMinions(arwen, axeman);

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.FreepsActionAvailable(sword));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(2, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(sword);
		assertEquals(List.of("Exert bearer", "Discard 2 cards from hand"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert bearer");

		// The cost is her bearer's: Arwen exerts, Haldir (who bears the other copy) does not, and no card leaves the hand.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(2, scn.GetFreepsHandCount());

		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, axeman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsChooseCard(axeman);
		assertEquals(5, scn.GetStrength(axeman));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Arwen (6 + 1) loses to 5 + 5 and lives; the Axeman lives too. In the next skirmish the -1 is gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);
		assertInPlay(axeman);
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DiscardingTwoCardsFromHandMakesAMinionSkirmishingBearerStrengthMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, sword);
		// Three kinds of card in hand: a [dwarven] event, a Shadow event and an Elven Sword.
		var chaff1 = scn.GetFreepsCard("chaff1");
		var ferocity = scn.GetFreepsCard("ferocity");
		var sword2 = scn.GetFreepsCard("sword2");
		scn.MoveCardsToHand(chaff1, ferocity, sword2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);

		scn.FreepsResolveSkirmish(arwen);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsUseCardAction(sword);
		assertEquals(List.of("Exert bearer", "Discard 2 cards from hand"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Discard 2 cards from hand");

		assertTrue(scn.FreepsHasCardChoiceAvailable(chaff1, ferocity, sword2));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(ferocity, sword2);

		assertInDiscard(ferocity, sword2);
		assertInZone(Zone.HAND, chaff1);
		assertEquals(0, scn.GetWoundsOn(arwen));
		// The one minion skirmishing her is chosen without a question.
		assertEquals(4, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithBearerExhaustedTheTwoCardsAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, sword);
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		scn.MoveCardsToHand(chaff1, chaff2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsExhausted(arwen));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);

		// No choice of cost is asked: the discard is the only one that can be paid.
		assertInDiscard(chaff1, chaff2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(4, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneCardInHandBearerExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, sword);
		var chaff1 = scn.GetFreepsCard("chaff1");
		scn.MoveCardsToHand(chaff1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);

		scn.FreepsResolveSkirmish(arwen);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInZone(Zone.HAND, chaff1);
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsUseCardAction(sword);

		// No choice of cost is asked: the exertion is the only one that can be paid.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.HAND, chaff1);
		assertEquals(4, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithBearerExhaustedAndOneCardInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, sword);
		var chaff1 = scn.GetFreepsCard("chaff1");
		scn.MoveCardsToHand(chaff1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, runner);

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsExhausted(arwen));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sword));
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItIsPaidForAndChangesNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(arwen, sword);

		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, runner},
				new PhysicalCardImpl[]{aragorn, troop});

		// Aragorn's skirmish first: the Troop is skirmishing another companion, and the Runner is assigned to
		// Arwen but not yet skirmishing her.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.FreepsActionAvailable(sword));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsUseCardAction(sword);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
