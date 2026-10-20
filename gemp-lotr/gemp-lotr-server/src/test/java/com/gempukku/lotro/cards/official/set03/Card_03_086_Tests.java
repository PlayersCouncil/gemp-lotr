package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_086_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("otsea", "3_86");
					put("tip", "1_209");        // Blade Tip: [wraith] support-area condition; its own response moves it to the loser
					put("tip2", "1_209");
					put("breath", "1_207");     // Black Breath: another [wraith] support-area condition; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; strength 3, vitality 4
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				// The Great Ring adds no strength and, while it is not worn, answers no wound.
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void UlaireOtseaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Úlairë Otsëa, Ringwraith in Twilight
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Twilight</b>.<br>Each time Úlairë Otsëa wins a skirmish, you may exert him to transfer Blade Tip from your support area to the Ring-bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("otsea");

		assertEquals("Úlairë Otsëa", card.getBlueprint().getTitle());
		assertEquals("Ringwraith in Twilight", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TWILIGHT));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishHeMayExertToTransferBladeTipFromYourSupportAreaToTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var tip = scn.GetShadowCard("tip");
		var tip2 = scn.GetShadowCard("tip2");
		var breath = scn.GetShadowCard("breath");
		var freepsTip = scn.GetFreepsCard("tip");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tip, tip2, breath);
		// The Free Peoples player's own Blade Tip, in his own support area.
		scn.MoveCardsToSupportArea(freepsTip);
		// 3 + 2: beaten by 9, not overwhelmed, so Sam lives as the losing character.
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 2));
		scn.MoveMinionsToTable(otsea);

		assertInZone(Zone.SUPPORT, tip, tip2, breath, freepsTip);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, otsea);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));

		scn.ShadowChooseOptionalTrigger(otsea);

		// Either Blade Tip in his support area; not Black Breath, and not the other player's.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tip, tip2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(breath, freepsTip));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tip2);

		assertEquals(1, scn.GetWoundsOn(otsea));
		// To the Ring-bearer, not to Sam who lost to him; no bearer is asked for.
		assertAttachedTo(tip2, frodo);
		assertNotAttachedTo(tip2, sam);
		assertInZone(Zone.SUPPORT, tip, breath, freepsTip);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tip));
	}

	@Test
	public void DeclinedAtHisFirstWinItIsOfferedAgainAtHisSecondWinOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var tip = scn.GetShadowCard("tip");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tip);
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 2));
		scn.MoveMinionsToTable(otsea);

		assertInZone(Zone.SUPPORT, tip);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, otsea);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowDeclineOptionalTrigger();

		// Declined: no exertion, and Blade Tip stays where it was.
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertInZone(Zone.SUPPORT, tip);
		assertNotAttachedTo(tip, frodo);

		// A second move, and a second skirmish against Sam.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, otsea);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowChooseOptionalTrigger(otsea);

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertAttachedTo(tip, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishHeIsNotOfferedTheTransfer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var tip = scn.GetShadowCard("tip");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tip);
		// 8 + 2 beats 9, and Otsëa, of vitality 3, lives through the wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));
		scn.MoveMinionsToTable(otsea);

		assertInZone(Zone.SUPPORT, tip);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, otsea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(otsea));
		assertInZone(Zone.SUPPORT, tip);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ExhaustedHeCannotExertAndIsNotOfferedTheTransfer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var tip = scn.GetShadowCard("tip");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tip);
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 2));
		scn.MoveMinionsToTable(otsea);
		// Vitality 3 with 2 wounds: he cannot exert.
		scn.AddWoundsToChar(otsea, 2);

		assertEquals(2, scn.GetWoundsOn(otsea));
		assertInZone(Zone.SUPPORT, tip);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, otsea);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// He won, and Blade Tip's own response is offered before Sam takes his wound (engine pass 54).
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tip));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(otsea));
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertInZone(Zone.SUPPORT, tip);
	}

	@Test
	public void WithNoBladeTipInHisSupportAreaHeExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var tip = scn.GetShadowCard("tip");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam, aragorn);

		scn.StartGame();
		// His only Blade Tip is borne already, as its own response leaves it.
		scn.AttachCardsTo(aragorn, tip);
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 2));
		scn.MoveMinionsToTable(otsea);

		assertAttachedTo(tip, aragorn);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, otsea);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// A trigger with a cost is offered though its effect will do nothing (Q-0034).
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowChooseOptionalTrigger(otsea);

		assertEquals(1, scn.GetWoundsOn(otsea));
		assertAttachedTo(tip, aragorn);
		assertNotAttachedTo(tip, frodo);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARingBearerAlreadyBearingBladeTipIsNotGivenASecondAndHeExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var tip = scn.GetShadowCard("tip");
		var tip2 = scn.GetShadowCard("tip2");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		// One Blade Tip is borne by the Ring-bearer already, as its own response leaves it; the other lies in the support area.
		scn.AttachCardsTo(frodo, tip);
		scn.MoveCardsToSupportArea(tip2);
		scn.ApplyAdHocModifier(new StrengthModifier(null, sam, null, 2));
		scn.MoveMinionsToTable(otsea);

		assertAttachedTo(tip, frodo);
		assertInZone(Zone.SUPPORT, tip2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, otsea);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// A trigger with a cost is offered though its effect cannot be carried out (Q-0034).
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowChooseOptionalTrigger(otsea);

		// He exerts, and the Ring-bearer, who may bear no second, keeps the one he has.
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertAttachedTo(tip, frodo);
		assertInZone(Zone.SUPPORT, tip2);
		assertNotAttachedTo(tip2, frodo);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(tip2));
	}

	@Test
	public void WhenFrodoIsOverwhelmedBladeTipIsTransferredToHimBeforeHeDiesAndSamTakesTheRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var otsea = scn.GetShadowCard("otsea");
		var tip = scn.GetShadowCard("tip");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.MoveCardsToSupportArea(tip);
		scn.MoveMinionsToTable(otsea);

		assertInZone(Zone.SUPPORT, tip);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, otsea);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// 9 against 3 is more than double.  Otsea's win is answered before Frodo is overwhelmed (engine pass 54;
		// until then Frodo was dead, Sam the Ring-bearer, and the Blade Tip went to Sam).
		assertInZone(Zone.FREE_CHARACTERS, frodo);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(otsea));
		scn.ShadowChooseOptionalTrigger(otsea);
		assertEquals(1, scn.GetWoundsOn(otsea));

		// The Blade Tip went to Frodo; then Frodo is overwhelmed and dies, the Blade Tip with him, and Sam's
		// response makes him the Ring-bearer.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(sam));
		scn.FreepsChooseOptionalTrigger(sam);
		assertInZone(Zone.DEAD, frodo);
		assertSame(sam, scn.GetRingBearer());
		assertInDiscard(tip);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
