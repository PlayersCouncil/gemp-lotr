package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_115_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unheeded", "8_115");
					// Merry, From O'er the Brandywine: an unbound [shire] Hobbit, vitality 4; +2 only while he bears a weapon
					put("merry", "1_303");
					// Pippin, Trollslayer: an unbound [gondor] Hobbit, vitality 4; his skirmish ability is used only on purpose
					put("gondorpippin", "103_31");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit; his abilities are used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally; his skirmish ability is used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is no Hobbit
					// Ted Sandyman, Chief's Men's Ally: a Hobbit minion; his abilities are used only on purpose
					put("ted", "18_75");

					// Orc Taskmaster: [sauron] Orc, vitality 3; "Shadow: Exert this minion to add (1)"
					put("taskmaster", "2_90");
					// Easterling Attacker: [raider] Man, vitality 2; "Skirmish: Exert this minion to make him damage +1"
					put("attacker", "7_141");
					// Orc Soldier: [sauron] Orc, vitality 2; his skirmish ability is used only on purpose
					put("soldier", "1_271");
				}},
				// Site 2 is Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength +2"
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnheededStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Unheeded
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a minion exerts, exert an unbound Hobbit to wound that minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("unheeded");

		assertEquals("Unheeded", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AMinionExertingForItsShadowAbilityIsWoundedByExertingAnUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheeded = scn.GetFreepsCard("unheeded");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(unheeded);

		var taskmaster = scn.GetShadowCard("taskmaster");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster, soldier);
		assertInHand(unheeded);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.ShadowUseCardAction(taskmaster);
		assertEquals(1, scn.GetWoundsOn(taskmaster));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		// Merry is the one unbound Hobbit, so he exerts unasked.
		scn.FreepsChooseOptionalTrigger(unheeded);
		assertEquals(1, scn.GetWoundsOn(merry));

		// That minion, and not the other.
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInDiscard(unheeded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnyUnboundHobbitMayExertAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheeded = scn.GetFreepsCard("unheeded");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("gondorpippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(unheeded);

		var taskmaster = scn.GetShadowCard("taskmaster");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster, ted);
		scn.FreepsPass();

		scn.ShadowUseCardAction(taskmaster);
		scn.FreepsChooseOptionalTrigger(unheeded);

		// The unbound Hobbits of two cultures.  Not the Ring-bound Sam or Ring-bearer, the Hobbit ally, an unbound
		// companion who is no Hobbit, or the Hobbit minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, bounder, aragorn, ted));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionLeftExhaustedByItsExertionIsKilledByTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheeded = scn.GetFreepsCard("unheeded");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(unheeded);

		var attacker = scn.GetShadowCard("attacker");

		scn.StartGame();
		scn.MoveMinionsToTable(attacker);
		assertInHand(unheeded);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, attacker);
		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(attacker));
		scn.ShadowUseCardAction(attacker);
		// Vitality 2 with 1 wound: exhausted.
		assertEquals(1, scn.GetWoundsOn(attacker));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		scn.FreepsChooseOptionalTrigger(unheeded);
		assertEquals(1, scn.GetWoundsOn(merry));

		// A wound and not an exertion: the exhausted minion takes it and is killed.
		assertInZone(Zone.DISCARD, attacker);
		assertInDiscard(unheeded);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACompanionExertingIsNotAnsweredAndAMinionExertingForASiteIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheeded = scn.GetFreepsCard("unheeded");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(unheeded);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, soldier);
		scn.FreepsResolveSkirmish(merry);

		var ettenmoors = scn.GetCurrentSite();
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsUseCardAction(ettenmoors);
		// Frodo or Merry: Merry.
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));

		// A companion exerted, with a minion on the table: not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Shadow player's minion exerts for the same site's text: offered.
		assertEquals(0, scn.GetWoundsOn(soldier));
		scn.ShadowUseCardAction(ettenmoors);
		assertEquals(1, scn.GetWoundsOn(soldier));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		scn.FreepsChooseOptionalTrigger(unheeded);
		assertEquals(2, scn.GetWoundsOn(merry));
		// Vitality 2: the wound kills him.
		assertInZone(Zone.DISCARD, soldier);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhileNoUnboundHobbitCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unheeded = scn.GetFreepsCard("unheeded");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(merry, sam);
		scn.MoveCardsToHand(unheeded);
		// Vitality 4 with 3 wounds: the one unbound Hobbit is exhausted.  Sam, a Ring-bound Hobbit, can exert.
		scn.AddWoundsToChar(merry, 3);

		var taskmaster = scn.GetShadowCard("taskmaster");

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsPass();

		scn.ShadowUseCardAction(taskmaster);
		assertEquals(1, scn.GetWoundsOn(taskmaster));

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
