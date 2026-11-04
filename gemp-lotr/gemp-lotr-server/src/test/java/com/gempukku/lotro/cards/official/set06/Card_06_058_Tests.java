package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ladder", "6_58");
					put("ladder2", "6_58");     // a second copy: the card is not unique and its tokens are "here"

					put("troop", "1_143");      // Troop of Uruk-hai: [isengard] Uruk-hai, cost 5, site 5, strength 9, vitality 4; damage +1, weapons give no bonus against it
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					put("legion", "17_129");    // White Hand Legion: [uruk-hai] Uruk-hai, strength 17, vitality 3; damage +1, to play remove 2 threats (cheated in)
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, strength 6, vitality 2; "Shadow: Exert this minion to add (1)", used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AssaultLadderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Assault Ladder
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: <b>Machine</b>. Plays to your support area.
		 * <b>Shadow:</b> Exert an Uruk-hai to place an [isengard] token on this card.
		 * <b>Skirmish:</b> Spot 3 [isengard] tokens here to make an Uruk-hai strength +3. Discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("ladder");

		assertEquals("Assault Ladder", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
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
		assertInZone(Zone.SUPPORT, ladder);
		assertInHand(troop);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(forger);
		scn.ShadowRefreshDecision();

		// No Uruk-hai to exert, only an [isengard] Orc.
		assertTrue(scn.AwaitingShadowPhaseActions());
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
		assertInZone(Zone.SUPPORT, ladder, ladder2);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop, savage, legion, forger);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(troop, 3);
		assertEquals(3, scn.GetWoundsOn(troop));
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
	public void WithThreeTokensHereMakesAnUrukHaiStrengthPlus3ForThatSkirmishAndDiscardsTheCopyUsed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var ladder2 = scn.GetShadowCard("ladder2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(ladder, ladder2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, legion, forger);
		// Each Ladder places tokens on itself: three on the first, two on the second, five [isengard] tokens in all.
		scn.AddTokensToCard(ladder, 3);
		scn.AddTokensToCard(ladder2, 2);
		assertInZone(Zone.SUPPORT, ladder, ladder2);
		assertEquals(3, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetCultureTokensOn(ladder2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, legion});
		// The Troop and the Orc are left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The copy with three tokens is offered; the copy beside it, with two of its own, is not.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(ladder));
		assertFalse(scn.ShadowActionAvailable(ladder2));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(17, scn.GetStrength(legion));
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(6, scn.GetStrength(forger));

		scn.ShadowUseCardAction(ladder);
		// An Uruk-hai of either culture, in this skirmish, waiting for its own or in none; not the [isengard] Orc,
		// nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, legion, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(legion);

		assertEquals(20, scn.GetStrength(legion));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(9, scn.GetStrength(troop));
		assertEquals(6, scn.GetStrength(forger));
		// The copy used is discarded; the other stays, its tokens with it.
		assertInZone(Zone.DISCARD, ladder);
		assertInZone(Zone.SUPPORT, ladder2);
		assertEquals(2, scn.GetCultureTokensOn(ladder2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Legion's own skirmish is a phase of its own: the +3 is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(17, scn.GetStrength(legion));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUrukHaiItIsOfferedAndDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ladder = scn.GetShadowCard("ladder");
		var forger = scn.GetShadowCard("forger");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		scn.AddTokensToCard(ladder, 3);
		assertInZone(Zone.SUPPORT, ladder);
		assertEquals(3, scn.GetCultureTokensOn(ladder));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, forger);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// The only minion is an Orc. The ability is offered: its second sentence still happens.
		assertEquals(6, scn.GetStrength(forger));
		assertTrue(scn.ShadowActionAvailable(ladder));
		scn.ShadowUseCardAction(ladder);

		assertEquals(6, scn.GetStrength(forger));
		assertInZone(Zone.DISCARD, ladder);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
