package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ladder", "5_57");
					put("ladder2", "5_57");     // a second copy: the card is not unique and its token is "here"

					put("troop", "1_143");      // Troop of Uruk-hai: [isengard] Uruk-hai, cost 5, site 5, vitality 4; damage +1, weapons give no bonus against it
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
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
		 * Subtype:
		 * Game Text: <b>Machine</b>. Plays to your support area.<br><b>Shadow:</b> Exert an Uruk-hai to place an [isengard] token on this card.<br><b>Skirmish:</b> Spot an [isengard] token here to heal an Uruk-hai. Discard this condition.
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
	public void ExertsAnUrukHaiToPlaceATokenUntilNoUrukHaiCanExert() throws DecisionResultInvalidException, CardNotFoundException {
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

		scn.ShadowUseCardAction(ladder);
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetWoundsOn(troop));
		// Placing a token is not what discards the Ladder.
		assertInZone(Zone.SUPPORT, ladder);
		assertTrue(scn.ShadowActionAvailable(ladder));

		scn.ShadowUseCardAction(ladder);
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(troop));

		// No limit is printed: a third token in the same phase.
		assertTrue(scn.ShadowActionAvailable(ladder));
		scn.ShadowUseCardAction(ladder);
		assertEquals(3, scn.GetCultureTokensOn(ladder));
		assertEquals(3, scn.GetWoundsOn(troop));

		// Vitality 4 with 3 wounds: the Uruk-hai cannot pay again, and the Orc, who could exert, may not.
		assertEquals(0, scn.GetWoundsOn(forger));
		assertFalse(scn.IsExhausted(forger));
		assertFalse(scn.ShadowActionAvailable(ladder));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiOfAnyCultureMayExertAndTheTokenGoesOnTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
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

		scn.ShadowUseCardAction(ladder2);
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetCultureTokensOn(ladder2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithATokenHereHealsAnUrukHaiOnceAndDiscardsTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
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
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, legion, forger);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(legion, 2);
		scn.AddWoundsToChar(forger, 1);
		scn.ShadowRefreshDecision();

		// The token is placed by the first copy's own Shadow ability. The Savage exerts, to 2 wounds: with the
		// Legion exhausted he is the one Uruk-hai who can, and is not asked for.
		scn.ShadowUseCardAction(ladder);
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetCultureTokensOn(ladder2));
		assertEquals(2, scn.GetWoundsOn(savage));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, legion);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The copy with a token is offered; the copy beside it, with none of its own, is not.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ladder));
		assertFalse(scn.ShadowActionAvailable(ladder2));

		scn.ShadowUseCardAction(ladder);
		// An Uruk-hai of either culture, in the skirmish or out of it; not the [isengard] Orc, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		// One wound of the Savage's two is healed, and nobody else's.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(legion));
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// The copy used is discarded; the other stays.
		assertInZone(Zone.DISCARD, ladder);
		assertInZone(Zone.SUPPORT, ladder2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoWoundedUrukHaiItHealsNobodyAndIsDiscardedAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var savage = scn.GetShadowCard("savage");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, forger);
		// The Ladder places tokens on itself.
		scn.AddTokensToCard(ladder, 1);
		scn.AddWoundsToChar(forger, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The only wounded minion is an Orc. The ability is offered: its second sentence still happens.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowActionAvailable(ladder));
		scn.ShadowUseCardAction(ladder);

		assertEquals(1, scn.GetWoundsOn(forger));
		assertInZone(Zone.DISCARD, ladder);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
