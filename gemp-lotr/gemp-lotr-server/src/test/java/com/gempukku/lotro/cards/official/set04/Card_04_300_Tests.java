package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_300_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("escape", "4_300");
					put("escape2", "4_300");
					put("escape3", "4_300");
					put("escape4", "4_300");
					put("merry", "1_302");        // Merry, Friend to Sam: [shire] Hobbit companion, unbound; strength 3, vitality 4
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit companion, unbound; cheated in
					put("businessman", "13_157"); // Westfarthing Businessman: [shire] Hobbit companion, unbound; +1 per [shire] card with a token (none)
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound companion who is no Hobbit
					put("bounder", "1_286");      // Bounder: a Hobbit ally, who is no companion and so not unbound
					put("tale", "1_317");         // There and Back Again: another [shire] condition on a Hobbit companion
					put("runner", "1_178");       // Goblin Runner: strength 5; cheated in, so its text never comes up
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EscapeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Escape
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Condition
		 * Vitality: +1
		 * Game Text: Stealth. Bearer must be an unbound Hobbit. Limit 1 per character.
		 * Skirmish: Exert bearer twice to cancel a skirmish involving him. Any Shadow player may remove (1) to
		 * prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("escape");

		assertEquals("Escape", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.STEALTH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void BearerMustBeAnUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var escape = scn.GetFreepsCard("escape");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(escape);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(escape));
		scn.FreepsPlayCard(escape);

		// Unbound Hobbits of two cultures; not the Ring-bearer or Sam (Ring-bound Hobbits), not Aragorn (no Hobbit),
		// not the Hobbit ally (no companion, so neither bound nor unbound).
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, aragorn, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertAttachedTo(escape, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void LimitOnePerCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var escape2 = scn.GetFreepsCard("escape2");
		var escape3 = scn.GetFreepsCard("escape3");
		var escape4 = scn.GetFreepsCard("escape4");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var businessman = scn.GetFreepsCard("businessman");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCompanionsToTable(merry, pippin, businessman);
		scn.AttachCardsTo(merry, escape);
		scn.AttachCardsTo(pippin, tale);
		scn.MoveCardsToHand(escape2, escape3, escape4);

		scn.StartGame();

		assertAttachedTo(escape, merry);
		assertAttachedTo(tale, pippin);
		scn.FreepsPlayCard(escape2);
		// Merry has an Escape; Pippin's [shire] condition is another card, and the Businessman bears nothing.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, businessman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		scn.FreepsChooseCard(pippin);
		assertAttachedTo(escape2, pippin);

		// The Businessman is the one unbound Hobbit left without one.
		scn.FreepsPlayCard(escape3);
		assertAttachedTo(escape3, businessman);

		// Every unbound Hobbit bears an Escape: the fourth cannot be played.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(escape4));
		assertInHand(escape4);
	}

	@Test
	public void SkirmishAbilityExertsBearerTwiceToCancelHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, escape);
		// Vitality 4 + 1: two wounds leave him three, enough to exert twice.
		scn.AddWoundsToChar(merry, 2);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.FreepsResolveSkirmish(merry);
		scn.SetTwilight(3);

		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsActionAvailable(escape));
		scn.FreepsUseCardAction(escape);
		assertEquals(4, scn.GetWoundsOn(merry));

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Cancelled: no winner and no loser, so Merry takes no wound for losing to the stronger Runner.
		assertEquals(3, scn.GetTwilight());
		assertEquals(4, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayRemoveOneToPreventTheCancelAndTheExertionsStayPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, escape);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.FreepsResolveSkirmish(merry);
		scn.SetTwilight(3);

		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsUseCardAction(escape);
		assertEquals(2, scn.GetWoundsOn(merry));

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		assertEquals(2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(merry));
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner));

		// The skirmish goes on: Merry's 3 against the Runner's 5, and he loses.
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyTwilightPoolTheShadowPlayerIsNotAskedAndTheSkirmishIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, escape);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.FreepsResolveSkirmish(merry);
		scn.SetTwilight(0);

		scn.FreepsUseCardAction(escape);
		assertEquals(2, scn.GetWoundsOn(merry));

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(merry));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneExertionLeftTheBearerCannotUseIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, escape);
		// Vitality 4 + 1 with three wounds: two left, so he can exert once and not twice.
		scn.AddWoundsToChar(merry, 3);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(3, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(escape));
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItCancelsNothingAndNobodyIsAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var escape = scn.GetFreepsCard("escape");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.AttachCardsTo(merry, escape);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner}, new PhysicalCardImpl[]{pippin, runner2});
		// Pippin's skirmish first: Merry is assigned, and not yet in a skirmish.
		scn.FreepsResolveSkirmish(pippin);
		scn.SetTwilight(3);

		// The cost can be paid; the skirmish is part of the effect.
		assertTrue(scn.FreepsActionAvailable(escape));
		scn.FreepsUseCardAction(escape);
		assertEquals(2, scn.GetWoundsOn(merry));

		// No skirmish involving Merry to cancel: the Shadow player is asked nothing, and Pippin's goes on.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runner2));

		// Pippin's 3 against the Runner's 5: he loses and takes a wound.
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertTrue(scn.IsCharAssignedAgainst(merry, runner));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
