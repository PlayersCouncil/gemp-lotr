package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_142_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stroke", "15_142");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Guma, Plains Farmer: [rohan] Man ally, home 4T; his fellowship ability is used only on purpose
					put("guma", "4_277");
					// Merry, Master Holbytla: a [rohan] Hobbit companion; a Man only while 3 [rohan] companions (or
					// Éowyn) can be spotted, which is never so where he is laid; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "0_65");     // Boromir, Steward's Heir: [gondor] Man companion; keywords only
					// Possessions with no text that reaches a test: Heavy Chain ([rohan] armor on a Man), Coat of Mail
					// ([gondor] armor on a Man), Hobbit Sword ([shire] hand weapon on a Hobbit), Sword of Gondor ([gondor]
					// hand weapon on a [gondor] Man; damage +1 only against a Man or a roaming minion)
					put("chain", "4_278");
					put("coat", "1_101");
					put("hsword", "1_299");
					put("gsword", "4_134");
					// Firefoot: [rohan] mount on a [rohan] Man; its triggers need a [rohan] token, and Éomer as bearer
					put("firefoot", "13_126");
					// Protecting the Hall: [rohan] condition on a [rohan] Man (no possession); its ability is used only on purpose
					put("hall", "11_149");
					// The Witch-king, Lord of Angmar with The Pale Blade (a hand weapon) and Black Steed (a mount,
					// discarded only at an underground site): a minion bearing two possessions
					put("witchking", "1_237");
					put("blade", "1_221");
					put("steed", "1_208");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its only text is on its play
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwiftStrokeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Swift Stroke
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot your [rohan] Man bearing a possession to discard all possessions from one character.
		*/

		var scn = GetScenario();

		var stroke = scn.GetFreepsCard("stroke");

		assertEquals("Swift Stroke", stroke.getBlueprint().getTitle());
		assertNull(stroke.getBlueprint().getSubtitle());
		assertFalse(stroke.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, stroke.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, stroke.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, stroke.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(stroke, Timeword.SKIRMISH));
		assertEquals(1, stroke.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsEveryPossessionOfAMinionInAnotherSkirmishAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stroke = scn.GetFreepsCard("stroke");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var chain = scn.GetFreepsCard("chain");
		var firefoot = scn.GetFreepsCard("firefoot");
		var hall = scn.GetFreepsCard("hall");
		var coat = scn.GetFreepsCard("coat");
		var witchking = scn.GetShadowCard("witchking");
		var blade = scn.GetShadowCard("blade");
		var steed = scn.GetShadowCard("steed");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, chain, firefoot, hall);
		scn.AttachCardsTo(guma, coat);
		scn.MoveCardsToHand(stroke);

		scn.StartGame();

		assertAttachedTo(chain, guard);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(hall, guard);
		assertAttachedTo(coat, guma);
		assertInHand(stroke);

		scn.MoveMinionsToTable(witchking, runner);
		scn.AttachCardsTo(witchking, blade, steed);
		assertAttachedTo(blade, witchking);
		assertAttachedTo(steed, witchking);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.ShadowAssignToMinions(frodo, witchking);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.FreepsPlayAvailable(stroke));
		scn.FreepsPlayCard(stroke);
		// "One character": a companion, an ally and a minion, the minion in a skirmish still to come.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma, witchking));
		scn.FreepsChooseCard(witchking);

		// Both of the Witch-king's possessions, a hand weapon and a mount.
		assertInDiscard(blade, steed);
		// Nobody else's.
		assertAttachedTo(chain, guard);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(hall, guard);
		assertAttachedTo(coat, guma);
		assertInDiscard(stroke);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheSpottedManMayBeTheOneWhosePossessionsGoAndHisConditionStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stroke = scn.GetFreepsCard("stroke");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var firefoot = scn.GetFreepsCard("firefoot");
		var hall = scn.GetFreepsCard("hall");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, chain, firefoot, hall);
		scn.MoveCardsToHand(stroke);

		scn.StartGame();

		assertAttachedTo(chain, guard);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(hall, guard);

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);

		scn.FreepsPlayCard(stroke);
		scn.FreepsChooseCard(guard);

		// His armor and his mount; Protecting the Hall is a condition, no possession.
		assertInDiscard(chain, firefoot);
		assertAttachedTo(hall, guard);
		assertInDiscard(stroke);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARohanAllyBearingAPossessionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stroke = scn.GetFreepsCard("stroke");
		var guard = scn.GetFreepsCard("guard");
		var guma = scn.GetFreepsCard("guma");
		var coat = scn.GetFreepsCard("coat");
		var hall = scn.GetFreepsCard("hall");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(guma);
		// The companion bears no possession; the ally does.
		scn.AttachCardsTo(guard, hall);
		scn.AttachCardsTo(guma, coat);
		scn.MoveCardsToHand(stroke);

		scn.StartGame();

		assertAttachedTo(hall, guard);
		assertAttachedTo(coat, guma);

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.FreepsPlayAvailable(stroke));
		scn.FreepsPlayCard(stroke);
		scn.FreepsChooseCard(guma);

		assertInDiscard(coat);
		assertAttachedTo(hall, guard);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutARohanManBearingAPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stroke = scn.GetFreepsCard("stroke");
		var guard = scn.GetFreepsCard("guard");
		var hall = scn.GetFreepsCard("hall");
		var merry = scn.GetFreepsCard("merry");
		var hsword = scn.GetFreepsCard("hsword");
		var boromir = scn.GetFreepsCard("boromir");
		var gsword = scn.GetFreepsCard("gsword");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard, merry, boromir);
		// A [rohan] Man bearing a card that is no possession; a [rohan] Hobbit and a [gondor] Man, each bearing one.
		scn.AttachCardsTo(guard, hall);
		scn.AttachCardsTo(merry, hsword);
		scn.AttachCardsTo(boromir, gsword);
		scn.MoveCardsToHand(stroke);

		scn.StartGame();

		assertAttachedTo(hall, guard);
		assertAttachedTo(hsword, merry);
		assertAttachedTo(gsword, boromir);
		assertInHand(stroke);

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);

		assertInHand(stroke);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stroke));
	}
}
