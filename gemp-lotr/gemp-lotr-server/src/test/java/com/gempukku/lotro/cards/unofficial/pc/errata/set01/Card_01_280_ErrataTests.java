package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_280_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lt", "51_280");

					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7, vitality 2; a regroup ability only

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound companion who is not the Ring-bearer, strength 3, vitality 4
					put("guard1", "1_7");       // Dwarf Guard: companion, strength 4, vitality 2, no text in play
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an ally, so a character who is no companion; her text needs your site 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TowerLieutenantStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Tower Lieutenant
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Each time this minion wins a skirmish, you may exhaust a companion (except the Ring-bearer).
		 * The Free Peoples player may discard 2 cards at random from hand to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lt");

		assertEquals("Tower Lieutenant", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishItMayExhaustACompanionOtherThanTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var guard1 = scn.GetFreepsCard("guard1");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCardsToHand(lt);
		scn.MoveCompanionsToTable(aragorn, sam, guard1);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToFreepsHand("guard2", "guard3");

		scn.StartGame();
		// Vitality 2 with 1 wound: exhausted already.
		scn.AddWoundsToChar(guard1, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();
		int twilight = scn.GetTwilight();

		scn.ShadowPlayCard(lt);
		// 4 to play + 2 roaming (a site 6 minion at site 2)
		assertEquals(twilight - 6, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, lt);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		// 10 against 8: the Lieutenant wins.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lt));

		scn.ShadowChooseOptionalTrigger(lt);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, guard1, silinde));
		scn.ShadowChooseCard(sam);
		// Exactly two cards in hand: enough to be asked.
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();

		// Vitality 4: exhausted at 3 wounds.
		assertEquals(3, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayDiscardTwoCardsAtRandomFromHandToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToFreepsHand("guard1", "guard2", "guard3");

		scn.StartGame();
		scn.MoveMinionsToTable(lt);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		int shadowHand = scn.GetShadowHandCount();
		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Aragorn is the only companion who can be exhausted, so he is taken without a choice.
		scn.ShadowChooseOptionalTrigger(lt);
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsDecisionAvailable("prevent"));

		scn.FreepsChooseYes();
		// Two of his three Guards, at random: he is not asked which.
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneCardInHandTheFreePeoplesPlayerIsNotAskedAndTheCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToFreepsHand("guard1");

		scn.StartGame();
		scn.MoveMinionsToTable(lt);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowChooseOptionalTrigger(lt);
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToExhaust() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToFreepsHand("guard1", "guard2");

		scn.StartGame();
		scn.MoveMinionsToTable(lt);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lt));

		scn.ShadowDeclineOptionalTrigger();
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoCompanionWhoCanBeExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(aragorn, guard1);
		scn.MoveCardsToFreepsHand("guard2", "guard3");

		scn.StartGame();
		// Aragorn and the Guard exhausted, and Frodo is the Ring-bearer.  (Aragorn's own wound for losing comes
		// after the win is answered: engine pass 54.)
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(guard1, 1);
		scn.MoveMinionsToTable(lt);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(lt));
		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertEquals(2, scn.GetFreepsHandCount());
		// The wound for losing kills him.
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ItsLossAndAnotherMinionsWinDoNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		var slayer = scn.GetShadowCard("slayer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard1 = scn.GetFreepsCard("guard1");
		scn.MoveCompanionsToTable(aragorn, guard1);

		scn.StartGame();
		scn.MoveMinionsToTable(lt, slayer);
		// Aragorn at 13 beats the Lieutenant's 10.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lt}, new PhysicalCardImpl[]{guard1, slayer});

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(lt));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The Slayer, 7, beats the Guard, 4.
		scn.FreepsResolveSkirmish(guard1);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachWinIsAnsweredAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lt = scn.GetShadowCard("lt");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(lt);
		// Made fierce, to win a second skirmish in the turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, lt, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// No cards in the Free Peoples hand: nothing is asked of him.
		scn.ShadowChooseOptionalTrigger(lt);
		scn.ShadowChooseCard(sam);
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lt);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(lt));

		// Sam is exhausted now, so Aragorn is the only one left to choose.
		scn.ShadowChooseOptionalTrigger(lt);
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
