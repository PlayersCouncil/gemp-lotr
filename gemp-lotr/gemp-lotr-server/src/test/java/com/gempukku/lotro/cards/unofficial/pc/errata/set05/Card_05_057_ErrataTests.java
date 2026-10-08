package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_057_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ladder", "55_57");
					put("ladder2", "55_57");    // a second copy: the card is not unique and its tokens are "here"

					put("troop", "1_143");      // Troop of Uruk-hai: [isengard] Uruk-hai, cost 5, site 5, strength 9, vitality 4; damage +1, weapons give no bonus against it
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("savage2", "1_151");
					put("legion", "17_129");    // White Hand Legion: [uruk-hai] Uruk-hai, strength 17, vitality 3; damage +1, to play remove 2 threats (cheated in)
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, vitality 2; "Shadow: Exert this minion to add (1)", used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ScalingLadderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Scaling Ladder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Machine.
		 * Shadow: Exert an Uruk-hai to place an [isengard] token here (limit 2 per phase).
		 * Skirmish: For each [isengard] token here, heal an Uruk-hai. Discard this condition.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ladder");

		assertEquals("Scaling Ladder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysToTheSupportAreaForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		scn.MoveCardsToHand(ladder);

		scn.StartGame();
		scn.FreepsPass();

		// 1 for Frodo + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(ladder));
		scn.ShadowPlayCard(ladder);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, ladder);
		assertNull(ladder.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsAnUrukHaiToPlaceATokenTwiceAPhaseAndAtTheLimitExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var troop = scn.GetShadowCard("troop");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(ladder);
		scn.MoveCardsToHand(troop);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(forger);
		scn.ShadowRefreshDecision();

		// No Uruk-hai to exert, only an [isengard] Orc.
		assertFalse(scn.ShadowActionAvailable(ladder));
		scn.ShadowPlayCard(troop);

		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(ladder));
		assertEquals("false", scn.ShadowActionNoEffect(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowActionAvailable(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(troop));

		// At its limit the ability is still offered, as the Uruk-hai can still exert, and is flagged as
		// doing nothing (Ketura on Q-0041).
		assertTrue(scn.ShadowActionAvailable(ladder));
		assertEquals("limit", scn.ShadowActionNoEffect(ladder));

		scn.ShadowUseCardAction(ladder);
		// The Uruk-hai exerts and no token is placed.
		assertEquals(3, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetCultureTokensOn(ladder));

		// Vitality 4 with 3 wounds: the Uruk-hai cannot pay again, and the Orc may not.
		assertEquals(0, scn.GetWoundsOn(forger));
		assertFalse(scn.ShadowActionAvailable(ladder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnyUrukHaiNotAnIsengardOrcMayExertAndEachCopyPlacesOnItselfUpToItsOwnLimit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var ladder2 = scn.GetShadowCard("ladder2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(ladder, ladder2);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, savage, legion, forger);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(troop, 3);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(ladder);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legion);
		assertEquals(1, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetCultureTokensOn(ladder2));

		scn.ShadowUseCardAction(ladder);
		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetCultureTokensOn(ladder2));

		// The limit is this copy's: the other places tokens of its own.
		assertEquals("limit", scn.ShadowActionNoEffect(ladder));
		assertEquals("false", scn.ShadowActionNoEffect(ladder2));
		scn.ShadowUseCardAction(ladder2);
		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetCultureTokensOn(ladder2));

		// Not in the maneuver phase, with an Uruk-hai able to exert and the second copy below its limit.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.IsExhausted(legion));
		assertFalse(scn.ShadowActionAvailable(ladder2));
		assertFalse(scn.ShadowActionAvailable(ladder));
	}

	@Test
	public void TheLimitRenewsInTheNextShadowPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, savage2);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(ladder);
		scn.ShadowChooseCard(savage);
		scn.ShadowUseCardAction(ladder);
		scn.ShadowChooseCard(savage2);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals("limit", scn.ShadowActionNoEffect(ladder));

		// The fellowship moves again in the same turn, to a second Shadow phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals("false", scn.ShadowActionNoEffect(ladder));
		scn.ShadowUseCardAction(ladder);
		scn.ShadowChooseCard(savage);
		assertEquals(3, scn.GetCultureTokensOn(ladder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishHealsAnUrukHaiOnceForEachTokenHereSpreadOrTwiceOverAndDiscardsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var ladder2 = scn.GetShadowCard("ladder2");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder, ladder2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, legion, forger);
		scn.AddTokensToCard(ladder, 3);
		scn.AddTokensToCard(ladder2, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(legion, 2);
		scn.AddWoundsToChar(forger, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(ladder));
		scn.ShadowUseCardAction(ladder);
		// Three tokens, three heals, one Uruk-hai at a time: either culture, not the [isengard] Orc nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(legion));
		// The same Uruk-hai may be chosen again.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion));
		scn.ShadowChooseCard(savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		// The third heal has one wounded Uruk-hai left to go to, and is made without asking.
		assertEquals(1, scn.GetWoundsOn(legion));

		// Then it is discarded; the other copy and its token are not touched, and nothing else is healed.
		assertInZone(Zone.DISCARD, ladder);
		assertInZone(Zone.SUPPORT, ladder2);
		assertEquals(1, scn.GetCultureTokensOn(ladder2));
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneTokenHealsOnceAndWithNoneOnlyDiscardsItselfAndNotInRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var ladder2 = scn.GetShadowCard("ladder2");
		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder, ladder2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.AddTokensToCard(ladder, 1);
		scn.AddWoundsToChar(savage, 2);
		scn.AddWoundsToChar(savage2, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage2);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// No token on the second copy: it heals nothing and is discarded.
		assertTrue(scn.ShadowActionAvailable(ladder2));
		scn.ShadowUseCardAction(ladder2);
		assertInZone(Zone.DISCARD, ladder2);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(savage2));

		// One token: one heal, of the Uruk-hai chosen.
		scn.FreepsPass();
		scn.ShadowUseCardAction(ladder);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, savage2));
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertInZone(Zone.DISCARD, ladder);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheSkirmishAbilityIsNotOfferedInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddTokensToCard(ladder, 1);
		scn.AddWoundsToChar(savage, 1);
		// Aragorn, 8, beats the Savage, 5, without killing it (vitality 3, 2 wounds after the skirmish).
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(ladder));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(ladder));
	}
}
