package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bound", "6_1");
					put("brigand", "4_10");         // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					// Dunlending Footmen: [dunland] Man of another title, strength 11, vitality 2; a cheaper play in a
					// skirmish phase only
					put("footmen", "6_3");
					// Saruman, Rabble-rouser: the [dunland] minion who is no Man; he may not be assigned, his move
					// trigger is kept off by laying him after the move, and his skirmish ability is used only on purpose
					put("saruman", "4_33");
					// Easterling Axeman: a [raider] Man, strength 6, vitality 2; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// No Defense: a [dunland] support-area condition of cost 0 that holds [dunland] tokens by its own
					// Shadow ability (remove (3)); its abilities are used only on purpose
					put("nodefense1", "4_28");
					put("nodefense2", "4_28");
					put("boromir", "1_97");         // Boromir, Son of Denethor: a [gondor] Man to exert for Defend It and Hope
					put("defend1", "4_115");        // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion once
					put("defend2", "4_115");        //   (or twice if that minion is an Uruk-hai)
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, to win a skirmish
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoundByRageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Bound By Rage
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area. When you play this condition, you may spot 2 [dunland] Men to place 2 [dunland] tokens here.<br><b>Response:</b> If a [dunland] Man is about to take a wound, discard this condition or remove a [dunland] token from here to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bound");

		assertEquals("Bound By Rage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBesideTwoDunlandMenItMayTakeTwoTokensHereAndAnotherConditionPlayedAfterTakesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bound = scn.GetShadowCard("bound");
		var brigand = scn.GetShadowCard("brigand");
		var footmen = scn.GetShadowCard("footmen");
		var nodefense1 = scn.GetShadowCard("nodefense1");
		var nodefense2 = scn.GetShadowCard("nodefense2");
		scn.MoveCardsToHand(bound, nodefense2);
		scn.MoveCardsToSupportArea(nodefense1);
		scn.AddTokensToCard(nodefense1, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, footmen);
		scn.SetTwilight(10);

		assertEquals(1, scn.GetCultureTokensOn(nodefense1));
		assertInZone(Zone.HAND, bound, nodefense2);
		scn.FreepsPass();

		assertInZone(Zone.HAND, bound);
		scn.ShadowPlayCard(bound);
		assertInZone(Zone.SUPPORT, bound);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bound));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(bound));
		// "Here": the other [dunland] condition keeps its one token.
		assertEquals(1, scn.GetCultureTokensOn(nodefense1));

		// Another [dunland] condition played once this one is in play sets nothing off.
		scn.ShadowPlayCard(nodefense2);
		assertInZone(Zone.SUPPORT, nodefense2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(nodefense2));
		assertEquals(2, scn.GetCultureTokensOn(bound));
		assertEquals(1, scn.GetCultureTokensOn(nodefense1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferLeavesItWithNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bound = scn.GetShadowCard("bound");
		var brigand = scn.GetShadowCard("brigand");
		var footmen = scn.GetShadowCard("footmen");
		scn.MoveCardsToHand(bound);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, footmen);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(bound);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bound));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, bound);
		assertEquals(0, scn.GetCultureTokensOn(bound));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneDunlandManASarumanAndAnotherCulturesManItIsPlayedWithNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bound = scn.GetShadowCard("bound");
		var brigand = scn.GetShadowCard("brigand");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(bound);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		// Laid after the move, so that Saruman's move trigger is not set off.
		scn.MoveMinionsToTable(brigand, saruman, axeman);
		scn.ShadowRefreshDecision();

		// One [dunland] Man; Saruman is a [dunland] minion who is no Man, the Axeman a Man of another culture.
		assertInZone(Zone.SHADOW_CHARACTERS, brigand, saruman, axeman);
		scn.ShadowPlayCard(bound);
		assertInZone(Zone.SUPPORT, bound);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetCultureTokensOn(bound));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachWoundOnADunlandManIsPreventedForATokenOrForThisConditionWhenBothCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bound = scn.GetShadowCard("bound");
		var brigand = scn.GetShadowCard("brigand");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend1, defend2);
		scn.MoveCardsToSupportArea(bound);
		scn.AddTokensToCard(bound, 2);
		assertInZone(Zone.SUPPORT, bound);
		assertEquals(2, scn.GetCultureTokensOn(bound));

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCultureTokensOn(bound));
		assertEquals(0, scn.GetWoundsOn(brigand));

		// The only minion is chosen unasked.
		scn.FreepsPlayCard(defend1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bound));
		scn.ShadowAcceptOptionalTrigger();
		// Both costs can be paid, so the Shadow player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Discard"));
		assertTrue(scn.ShadowChoiceAvailable("token"));
		scn.ShadowChoose("token");
		assertEquals(1, scn.GetCultureTokensOn(bound));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		scn.ShadowPass();

		// The second wound: this time the condition is discarded.
		scn.FreepsPlayCard(defend2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bound));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowChoiceAvailable("token"));
		scn.ShadowChoose("Discard");
		assertInDiscard(bound);
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTokenHereThisConditionIsDiscardedUnaskedToPreventAWoundOnADunlandManOfAnotherTitle() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bound = scn.GetShadowCard("bound");
		var footmen = scn.GetShadowCard("footmen");
		var nodefense1 = scn.GetShadowCard("nodefense1");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend1);
		scn.MoveCardsToSupportArea(bound, nodefense1);
		scn.AddTokensToCard(nodefense1, 1);
		assertInZone(Zone.SUPPORT, bound, nodefense1);
		assertEquals(0, scn.GetCultureTokensOn(bound));
		assertEquals(1, scn.GetCultureTokensOn(nodefense1));

		scn.StartGame();
		scn.MoveMinionsToTable(footmen);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(bound));
		assertEquals(1, scn.GetCultureTokensOn(nodefense1));
		assertEquals(0, scn.GetWoundsOn(footmen));

		scn.FreepsPlayCard(defend1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bound));
		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(bound);
		assertEquals(0, scn.GetWoundsOn(footmen));
		// "From here": the token on the other [dunland] condition is not taken.
		assertEquals(1, scn.GetCultureTokensOn(nodefense1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ItIsNotOfferedForSarumanAndOfTwoLosersOfOneSkirmishSavesOnlyTheDunlandMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bound = scn.GetShadowCard("bound");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(defend1);
		scn.MoveCardsToSupportArea(bound);
		scn.AddTokensToCard(bound, 2);
		assertEquals(2, scn.GetCultureTokensOn(bound));

		scn.StartGame();
		scn.FreepsPass();
		// Laid after the move, so that Saruman's move trigger is not set off.
		scn.MoveMinionsToTable(brigand, axeman, saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		// Saruman, a [dunland] minion who is no Man, is wounded and nothing is offered.
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPlayCard(defend1);
		// Aragorn is a [gondor] Man too: Boromir is the one exerted.
		scn.FreepsChooseCard(boromir);
		assertTrue(scn.FreepsHasCardChoiceAvailable(saruman, brigand, axeman));
		scn.FreepsChooseCard(saruman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(2, scn.GetCultureTokensOn(bound));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A strength that only arranges the board: 8 + 7 = 15 against 8 + 6.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 7));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowAssignToMinions(aragorn, axeman);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(15, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.PassSkirmishActions();
		// Both losers are about to take their wound together; only the [dunland] Man is saved.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bound));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("token");
		assertEquals(1, scn.GetCultureTokensOn(bound));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
