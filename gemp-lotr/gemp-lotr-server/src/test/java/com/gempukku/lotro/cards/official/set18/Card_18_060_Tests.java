package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "18_60");
					put("axe2", "18_60");

					put("corps", "11_73");      // Corps of Harad: [men] Man, strength 10, vitality 3; fierce only at a battleground (none on this path)
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text
					put("stragglers", "13_99"); // Stragglers: [men] support condition that takes a [men] token as it is played
					put("fleet", "18_66");      // Fleet of Corsair Ships: [men] support possession that may take a [men] token as it is played
					put("threatening", "13_103"); // Always Threatening: [orc] support condition that carries [orc] tokens

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairBoardingAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Corsair Boarding Axe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [men] minion.
		 * 	<b>Skirmish:</b> Remove (3) to reinforce a [men] token and make bearer <b>fierce</b> until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe");

		assertEquals("Corsair Boarding Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAMenMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveCardsToHand(axe);
		scn.MoveMinionsToTable(corps, ted, brigand);
		scn.SetTwilight(5);
		assertInZone(Zone.HAND, axe);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(axe));
		scn.ShadowPlayCard(axe);
		// [men] minions of two races are offered; a Man minion of another culture is not, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ted);

		assertAttachedTo(axe, ted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesThreeToReinforceAMenTokenAndMakeBearerFierceAndIsOfferedAgainOnceHeIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var axe2 = scn.GetShadowCard("axe2");
		var corps = scn.GetShadowCard("corps");
		var ted = scn.GetShadowCard("ted");
		var stragglers = scn.GetShadowCard("stragglers");
		var fleet = scn.GetShadowCard("fleet");
		var threatening = scn.GetShadowCard("threatening");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, ted);
		scn.AttachCardsTo(corps, axe);
		scn.AttachCardsTo(ted, axe2);
		scn.MoveCardsToSupportArea(stragglers, fleet, threatening);
		scn.AddTokensToCard(stragglers, 1);
		scn.AddTokensToCard(fleet, 1);
		scn.AddTokensToCard(threatening, 2);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertAttachedTo(axe, corps);
		assertAttachedTo(axe2, ted);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corps}, new PhysicalCardImpl[]{gimli, ted});
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(7);
		scn.FreepsPass();

		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.HasKeyword(corps, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(axe));
		// The text names no skirmish of the bearer's: the copy on Ted Sandyman, assigned but not yet fighting, is offered too.
		assertTrue(scn.ShadowActionAvailable(axe2));
		scn.ShadowUseCardAction(axe);
		// Either [men] card with a token may be reinforced; the [orc] condition's tokens are no [men] tokens.
		assertTrue(scn.ShadowHasCardChoiceAvailable(stragglers, fleet));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(threatening));
		scn.ShadowChooseCard(stragglers);

		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetCultureTokensOn(stragglers));
		assertEquals(1, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));
		// Ted Sandyman bears the other copy, and the copy used makes its own bearer fierce only.
		assertFalse(scn.HasKeyword(ted, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Already fierce, he is still offered fierce while (3) is there to pay (Q-0245).
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(axe));
		scn.ShadowUseCardAction(axe);
		scn.ShadowChooseCard(fleet);
		assertEquals(1, scn.GetTwilight());
		assertEquals(2, scn.GetCultureTokensOn(stragglers));
		assertEquals(2, scn.GetCultureTokensOn(fleet));
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMenTokenInPlayTheAbilityIsStillUsedAndMakesBearerFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var corps = scn.GetShadowCard("corps");
		var fleet = scn.GetShadowCard("fleet");
		var threatening = scn.GetShadowCard("threatening");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, axe);
		// A [men] card with no token, and tokens of another culture.
		scn.MoveCardsToSupportArea(fleet, threatening);
		scn.AddTokensToCard(threatening, 2);
		assertEquals(0, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertAttachedTo(axe, corps);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, corps);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.HasKeyword(corps, Keyword.FIERCE));
		assertTrue(scn.ShadowActionAvailable(axe));
		scn.ShadowUseCardAction(axe);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetCultureTokensOn(fleet));
		assertEquals(2, scn.GetCultureTokensOn(threatening));
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithOnlyTwoTwilightInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var corps = scn.GetShadowCard("corps");
		var stragglers = scn.GetShadowCard("stragglers");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, axe);
		scn.MoveCardsToSupportArea(stragglers);
		scn.AddTokensToCard(stragglers, 1);
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
		assertAttachedTo(axe, corps);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, corps);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(axe));
		assertFalse(scn.HasKeyword(corps, Keyword.FIERCE));
		assertEquals(1, scn.GetCultureTokensOn(stragglers));
	}

	@Test
	public void BearerStaysFierceThroughTheFierceSkirmishAndLosesItAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(corps);
		scn.AttachCardsTo(corps, axe);
		assertAttachedTo(axe, corps);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, corps);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);
		scn.FreepsPass();
		scn.ShadowUseCardAction(axe);
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));

		// 10 + 2 for the Axe against 8: Aragorn loses and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, corps);
		scn.FreepsResolveSkirmish(aragorn);
		// Still fierce in the fierce skirmish, a skirmish after the one it was made in.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, corps));
		assertTrue(scn.HasKeyword(corps, Keyword.FIERCE));

		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.HasKeyword(corps, Keyword.FIERCE));
	}
}
