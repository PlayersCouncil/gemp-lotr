package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_127_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("freely", "13_127");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; Valiant is its whole text
					// Théoden, Son of Thengel: [rohan] Man companion; his regroup ability is used only on purpose
					put("theoden", "4_292");
					// Merry, Master Holbytla: a [rohan] Hobbit companion; a Man only while 3 [rohan] companions (or
					// Éowyn) can be spotted, which is never so where he is laid; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; his skirmish ability is not used
					// Guma, Plains Farmer: [rohan] Man ally, home 4T; his fellowship ability is used only on purpose
					put("guma", "4_277");
					// Doom Drove Them: [rohan] support-area condition that adds [rohan] tokens to itself by its
					// fellowship ability; its skirmish ability is used only on purpose
					put("doom", "8_86");

					// [rohan] possessions of two classes
					put("sword", "4_291");      // Sword of Rohan: hand weapon, "Bearer must be a [rohan] Man"
					put("chain", "4_278");      // Heavy Chain: armor, "Bearer must be a Man"
					// Gúthwinë, Eomer's Blade: hand weapon, "Bearer must be a [rohan] companion"; the rest is Éomer's
					put("guthwine", "102_49");
					// Rider's Spear: a [rohan] hand weapon on a [rohan] Man; laid in the Shadow player's own discard pile
					// (a choice from a pile is read by blueprint, so it is a title the Free Peoples pile does not hold)
					put("spear", "4_288");
					// Herugrim: a [rohan] ARTIFACT, on Théoden; damage +1
					put("herugrim", "54_280");
					// Coat of Mail: a [gondor] possession (armor), "Bearer must be a Man"
					put("coat", "1_101");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FreelyAcrossOurLandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Freely Across Our Land
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, add a [rohan] token here.<br><b>Regroup:</b> Discard this from play or remove a token from here to play a [rohan] possession from your discard pile on your [rohan] companion.
		*/

		var scn = GetScenario();

		var freely = scn.GetFreepsCard("freely");

		assertEquals("Freely Across Our Land", freely.getBlueprint().getTitle());
		assertNull(freely.getBlueprint().getSubtitle());
		assertTrue(freely.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, freely.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, freely.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, freely.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(freely, Keyword.SUPPORT_AREA));
		assertEquals(1, freely.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedItTakesARohanTokenAndAnotherConditionPlayedAfterItAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freely = scn.GetFreepsCard("freely");
		var doom = scn.GetFreepsCard("doom");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(freely, doom);

		scn.StartGame();

		assertInHand(freely, doom);
		scn.FreepsPlayCard(freely);
		assertInZone(Zone.SUPPORT, freely);
		assertEquals(1, scn.GetCultureTokensOn(freely));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [rohan] condition played after it: nothing more is added here.
		scn.FreepsPlayCard(doom);
		assertInZone(Zone.SUPPORT, doom);
		assertEquals(1, scn.GetCultureTokensOn(freely));
		assertEquals(0, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovingTheTokenPlaysARohanPossessionFromTheDiscardPileOnARohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freely = scn.GetFreepsCard("freely");
		var doom = scn.GetFreepsCard("doom");
		var guard = scn.GetFreepsCard("guard");
		var theoden = scn.GetFreepsCard("theoden");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		var sword = scn.GetFreepsCard("sword");
		var chain = scn.GetFreepsCard("chain");
		var herugrim = scn.GetFreepsCard("herugrim");
		var coat = scn.GetFreepsCard("coat");
		var shadowSpear = scn.GetShadowCard("spear");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(guard, theoden, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(freely, doom);
		scn.AddTokensToCard(freely, 1);
		scn.AddTokensToCard(doom, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, freely, doom);
		scn.MoveCardsToDiscard(sword, chain, herugrim, coat, shadowSpear);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetCultureTokensOn(freely));
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertInDiscard(sword, chain, herugrim, coat, shadowSpear);
		assertTrue(scn.FreepsActionAvailable(freely));
		scn.FreepsUseCardAction(freely);

		// Both prices can be paid: he is asked which.
		assertTrue(scn.FreepsChoiceAvailable("Discard this"));
		assertTrue(scn.FreepsChoiceAvailable("Remove a token"));
		scn.FreepsChoose("Remove a token");
		// The token from here; not the one on the other [rohan] condition.
		assertEquals(0, scn.GetCultureTokensOn(freely));
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertInZone(Zone.SUPPORT, freely);

		// [rohan] possessions of two classes; not the [rohan] artifact Théoden could bear, the [gondor] possession a
		// Man could bear, nor the other player's Spear in his own discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, chain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herugrim, coat, shadowSpear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chain);

		// Any Man may bear the Chain: it goes on a [rohan] companion, not on the [rohan] ally or the [gondor] one.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, theoden));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(theoden);

		assertAttachedTo(chain, theoden);
		assertInDiscard(sword, herugrim, coat, shadowSpear);
		assertInZone(Zone.SUPPORT, freely, doom);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void DiscardingItPlaysARohanPossessionOnARohanCompanionWhoIsNoMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freely = scn.GetFreepsCard("freely");
		var doom = scn.GetFreepsCard("doom");
		var guard = scn.GetFreepsCard("guard");
		var merry = scn.GetFreepsCard("merry");
		var guthwine = scn.GetFreepsCard("guthwine");
		scn.MoveCompanionsToTable(guard, merry);
		scn.MoveCardsToSupportArea(freely, doom);
		scn.AddTokensToCard(freely, 1);
		scn.AddTokensToCard(doom, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, freely, doom);
		scn.MoveCardsToDiscard(guthwine);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetCultureTokensOn(freely));
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertInDiscard(guthwine);
		assertTrue(scn.FreepsActionAvailable(freely));
		scn.FreepsUseCardAction(freely);

		assertTrue(scn.FreepsChoiceAvailable("Discard this"));
		assertTrue(scn.FreepsChoiceAvailable("Remove a token"));
		scn.FreepsChoose("Discard this");
		assertInDiscard(freely);
		assertInZone(Zone.SUPPORT, doom);
		assertEquals(1, scn.GetCultureTokensOn(doom));

		// Gúthwinë is the one possession there, and goes on a [rohan] companion: the Man or the Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, merry));
		scn.FreepsChooseCard(merry);

		assertAttachedTo(guthwine, merry);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoTokenHereTheConditionIsDiscardedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freely = scn.GetFreepsCard("freely");
		var doom = scn.GetFreepsCard("doom");
		var guard = scn.GetFreepsCard("guard");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(guard);
		// Cheated in, it has no token; the token on the other [rohan] condition is not "here".
		scn.MoveCardsToSupportArea(freely, doom);
		scn.AddTokensToCard(doom, 1);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, freely, doom);
		scn.MoveCardsToDiscard(sword);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetCultureTokensOn(freely));
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertInDiscard(sword);
		assertTrue(scn.FreepsActionAvailable(freely));
		scn.FreepsUseCardAction(freely);

		// Only the discard can be paid, and it is paid unasked; the one possession goes on the one [rohan] Man.
		assertInDiscard(freely);
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertAttachedTo(sword, guard);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoRohanPossessionInTheDiscardPileThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freely = scn.GetFreepsCard("freely");
		var guard = scn.GetFreepsCard("guard");
		var herugrim = scn.GetFreepsCard("herugrim");
		var coat = scn.GetFreepsCard("coat");
		var shadowSpear = scn.GetShadowCard("spear");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(freely);
		scn.AddTokensToCard(freely, 1);

		scn.StartGame();
		// Herugrim (no Théoden to bear it), the [gondor] Coat (which the Guard could bear) and the other player's Spear.
		scn.MoveCardsToDiscard(herugrim, coat, shadowSpear);
		scn.SkipToPhase(Phase.REGROUP);

		// Both prices could be paid, but nothing there can be played: the play is part of what is paid.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(freely));
		assertInDiscard(herugrim, coat, shadowSpear);
		assertFalse(scn.FreepsActionAvailable(freely));
	}

	@Test
	public void NotOfferedWhileOnlyAGondorCompanionAndARohanAllyCouldBearThePossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var freely = scn.GetFreepsCard("freely");
		var boromir = scn.GetFreepsCard("boromir");
		var guma = scn.GetFreepsCard("guma");
		var chain = scn.GetFreepsCard("chain");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(freely);
		scn.AddTokensToCard(freely, 1);

		scn.StartGame();
		scn.MoveCardsToDiscard(chain);
		scn.SkipToPhase(Phase.REGROUP);

		// The Chain wants a Man, and Boromir and Guma are Men; neither is a [rohan] companion.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetCultureTokensOn(freely));
		assertInDiscard(chain);
		assertInZone(Zone.SUPPORT, guma);
		assertFalse(scn.FreepsActionAvailable(freely));
	}
}
