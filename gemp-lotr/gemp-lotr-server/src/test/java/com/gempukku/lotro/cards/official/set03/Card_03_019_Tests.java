package com.gempukku.lotro.cards.official.set03;

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

public class Card_03_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gift", "3_19");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("rohanaragorn", "17_93"); // Aragorn, Defender of Rohan: the one Aragorn not of [gondor] ([rohan]), strength 8, vitality 4; gives hunter only while the Ring-bearer is assigned
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					put("arwenally", "102_6");  // Arwen, Lady of Rivendell: the one Arwen who is an ally; Aragorn +1, and a "may" when he is about to take a wound
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] companion of another title; an archery ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: another [gondor] Man companion, able to exert
					put("chaff1", "1_3");       // Axe Strike: a [dwarven] event, here only as a card in hand
					put("chaff2", "1_3");

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its play trigger is not reached by a cheat
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, strength 6, vitality 2; +3 and fierce only at 2 burdens
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; answers only a weapon played on him
					put("ferocity", "4_151");   // Ferocity: a Shadow event, here only as a card in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GiftoftheEvenstarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Gift of the Evenstar
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, spot Arwen. Bearer must be Aragorn.<br><b>Skirmish:</b> Exert Aragorn or discard 2 cards from hand to make a minion skirmishing Aragorn strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gift");

		assertEquals("Gift of the Evenstar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilArwenIsSpottedThenGoesOnAragorn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.MoveCardsToHand(gift, arwen);

		scn.StartGame();

		// Aragorn is there to bear it, and an [elven] companion of another title is no Arwen.
		assertInZone(Zone.HAND, gift);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gift));

		scn.FreepsPlayCard(arwen);
		assertTrue(scn.FreepsPlayAvailable(gift));
		scn.FreepsPlayCard(gift);

		// Aragorn is the one bearer it may have: it goes on him, not on Arwen, Legolas or Frodo.
		assertAttachedTo(gift, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArwenAsAnAllyIsEnoughToPlayItOnAragornDefenderOfRohan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		var arwen = scn.GetFreepsCard("arwenally");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(arwen);
		scn.MoveCardsToHand(gift);

		scn.StartGame();

		assertInZone(Zone.HAND, gift);
		assertTrue(scn.FreepsPlayAvailable(gift));
		scn.FreepsPlayCard(gift);

		// The [rohan] Aragorn is an Aragorn as well.
		assertAttachedTo(gift, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithArwenAndNoAragornItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(arwen, legolas, boromir);
		scn.MoveCardsToHand(gift);

		scn.StartGame();

		// Arwen is spotted, and Frodo, Arwen, Legolas and a [gondor] Man who is no Aragorn cannot bear it.
		assertInZone(Zone.HAND, gift);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gift));
	}

	@Test
	public void ExertingAragornMakesAMinionSkirmishingHimStrengthMinusOneForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, gift);
		assertAttachedTo(gift, aragorn);
		scn.MoveCardsToHand(scn.GetFreepsCard("chaff1"), scn.GetFreepsCard("chaff2"));

		var runner = scn.GetShadowCard("runner");
		var axeman = scn.GetShadowCard("axeman");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, axeman, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{arwen, chieftain});
		// The leftover minion is the Shadow player's to assign: two minions of other cultures and races skirmish Aragorn.
		scn.ShadowAssignToMinions(aragorn, axeman);

		scn.FreepsResolveSkirmish(aragorn);
		assertAttachedTo(gift, aragorn);
		assertTrue(scn.FreepsActionAvailable(gift));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetFreepsHandCount());
		scn.FreepsUseCardAction(gift);
		// Both costs can be paid, so the player is asked which.
		assertEquals(List.of("Exert Aragorn", "Discard 2 cards from hand"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert Aragorn");

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetFreepsHandCount());

		// The two skirmishing Aragorn; not the Chieftain, assigned to Arwen and waiting.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, axeman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(chieftain));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(6, scn.GetStrength(axeman));
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsChooseCard(axeman);
		assertEquals(5, scn.GetStrength(axeman));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn (8) loses to 5 + 5 and lives; the Axeman lives too. In the next skirmish the -1 is gone.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(arwen);
		assertInPlay(axeman);
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DiscardingTwoCardsFromHandMakesAMinionSkirmishingAragornStrengthMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, gift);
		// Three kinds of card in hand: a [dwarven] event, a Shadow event and a companion.
		var chaff1 = scn.GetFreepsCard("chaff1");
		var ferocity = scn.GetFreepsCard("ferocity");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCardsToHand(chaff1, ferocity, legolas);
		assertInZone(Zone.HAND, chaff1, ferocity, legolas);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsUseCardAction(gift);
		assertEquals(List.of("Exert Aragorn", "Discard 2 cards from hand"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Discard 2 cards from hand");

		assertTrue(scn.FreepsHasCardChoiceAvailable(chaff1, ferocity, legolas));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(ferocity, legolas);

		assertInDiscard(ferocity, legolas);
		assertInZone(Zone.HAND, chaff1);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The one minion skirmishing him is chosen without a question.
		assertEquals(4, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAragornExhaustedTheTwoCardsAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, gift);
		var chaff1 = scn.GetFreepsCard("chaff1");
		var chaff2 = scn.GetFreepsCard("chaff2");
		scn.MoveCardsToHand(chaff1, chaff2);
		assertInZone(Zone.HAND, chaff1, chaff2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsExhausted(aragorn));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.FreepsActionAvailable(gift));
		scn.FreepsUseCardAction(gift);

		// No choice of cost is asked: the discard is the only one that can be paid.
		assertInDiscard(chaff1, chaff2);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneCardInHandAragornDefenderOfRohanExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("rohanaragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, gift);
		var chaff1 = scn.GetFreepsCard("chaff1");
		scn.MoveCardsToHand(chaff1);
		assertInZone(Zone.HAND, chaff1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.HAND, chaff1);
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsUseCardAction(gift);

		// No choice of cost is asked: the exertion is the only one that can be paid, and the [rohan] Aragorn pays it.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.HAND, chaff1);
		assertEquals(4, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithAragornExhaustedAndOneCardInHandItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, gift);
		var chaff1 = scn.GetFreepsCard("chaff1");
		scn.MoveCardsToHand(chaff1);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);

		scn.FreepsResolveSkirmish(aragorn);
		// Another [gondor] Man companion could exert, but the cost is Aragorn's.
		assertTrue(scn.IsExhausted(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gift));
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItIsPaidForAndChangesNoMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gift = scn.GetFreepsCard("gift");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, gift);

		var runner = scn.GetShadowCard("runner");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{arwen, chieftain});

		// Arwen's skirmish first: the Chieftain is skirmishing another companion, and the Runner is assigned to
		// Aragorn but not yet skirmishing him.
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.FreepsActionAvailable(gift));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(runner));
		scn.FreepsUseCardAction(gift);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
