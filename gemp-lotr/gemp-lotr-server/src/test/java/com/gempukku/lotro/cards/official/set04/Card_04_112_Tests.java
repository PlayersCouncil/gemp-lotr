package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gauntlets", "4_112");
					put("boromir", "0_65");       // Boromir, Steward's Heir: [gondor] Man companion, strength 7, vitality 3; keywords only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability only
					put("merchant", "7_97");      // Gondorian Merchant: the [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit, no Man; cheated in past his spot
					put("guard", "5_83");         // Household Guard: a [rohan] Man; Valiant is his whole text

					put("brigand", "4_10");       // Dunlending Brigand: strength 8, vitality 1, no text
					put("brigand2", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Faramir, Bearer of Quality 17_28, a [gondor] Man who may bear The One Ring, as the Ring-bearer.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gauntlets", "4_112");
					put("brigand", "4_10");       // Dunlending Brigand: strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				"17_28",
				VirtualTableScenario.GreatRing,   // no strength bonus, and no answer to a wound while not worn
				VirtualTableScenario.Fellowship
		);
	}

	protected VirtualTableScenario GetMultipathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gauntlets", "4_112");
					put("brigand", "4_10");       // Dunlending Brigand: strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				"17_28",
				VirtualTableScenario.GreatRing,
				VirtualTableScenario.Multipath
		);
	}

	@Test
	public void BoromirsGauntletsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Boromir's Gauntlets
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Gauntlets
		 * Game Text: Bearer must be a [gondor] Man.<br><b>Skirmish:</b> Exert bearer to cancel a skirmish involving bearer. Any Shadow player may remove (1) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gauntlets");

		assertEquals("Boromir's Gauntlets", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.GAUNTLETS));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void BearerMustBeAGondorManCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gauntlets = scn.GetFreepsCard("gauntlets");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(boromir, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(gauntlets);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(gauntlets));
		scn.FreepsPlayCard(gauntlets);

		// A [gondor] Man companion and the [gondor] Man ally; not a [gondor] Hobbit, a [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(gauntlets, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsBearerToCancelHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetFreepsCard("gauntlets");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, gauntlets);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertAttachedTo(gauntlets, boromir);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);
		scn.SetTwilight(3);

		assertAttachedTo(gauntlets, boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(gauntlets));
		scn.FreepsUseCardAction(gauntlets);
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Cancelled: no winner and no loser, so Boromir takes no wound for losing to the stronger Brigand (8 to 7).
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertFalse(scn.IsCharSkirmishing(boromir));
		assertAttachedTo(gauntlets, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayRemoveOneToPreventTheCancelAndTheExertionStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetFreepsCard("gauntlets");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, gauntlets);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertAttachedTo(gauntlets, boromir);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);
		scn.SetTwilight(3);

		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsUseCardAction(gauntlets);
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, brigand));

		// The skirmish goes on: Boromir's 7 against the Brigand's 8, and he loses.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyTwilightPoolTheShadowPlayerIsNotAskedAndTheSkirmishIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetFreepsCard("gauntlets");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, gauntlets);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertAttachedTo(gauntlets, boromir);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);
		scn.SetTwilight(0);

		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsUseCardAction(gauntlets);
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.IsCharSkirmishing(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheBearerExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetFreepsCard("gauntlets");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, gauntlets);
		// Vitality 3 with two wounds: exhausted.
		scn.AddWoundsToChar(boromir, 2);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertAttachedTo(gauntlets, boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(2, scn.GetWoundsOn(boromir));
		assertAttachedTo(gauntlets, boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gauntlets));
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItCancelsNothingAndNobodyIsAskedToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetFreepsCard("gauntlets");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, gauntlets);

		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");

		scn.StartGame();
		assertAttachedTo(gauntlets, boromir);
		scn.MoveMinionsToTable(brigand, brigand2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, brigand}, new PhysicalCardImpl[]{aragorn, brigand2});
		// Aragorn's skirmish first: Boromir is assigned, and not yet in a skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);

		// The exertion can be paid; the skirmish is part of the effect.
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(gauntlets));
		scn.FreepsUseCardAction(gauntlets);
		assertEquals(1, scn.GetWoundsOn(boromir));

		// No skirmish involving Boromir to cancel: the Shadow player is asked nothing, and Aragorn's goes on.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, brigand2));

		// Aragorn's 8 against the Brigand's 8: a tie goes to the Shadow side, and he takes a wound.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(boromir, brigand));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllyWhoBearsThemIsTheOneWhoExertsAndTheCompanionsSkirmishGoesOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetFreepsCard("gauntlets");
		var boromir = scn.GetFreepsCard("boromir");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, gauntlets);
		// Vitality 1 cannot exert; one more by hand, which printed cards that raise vitality can give, lets the ally
		// who bears the Gauntlets pay for them.
		scn.ApplyAdHocModifier(new VitalityModifier(null, merchant, null, 1));

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertAttachedTo(gauntlets, merchant);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, brigand);
		scn.FreepsResolveSkirmish(boromir);
		scn.SetTwilight(3);

		assertAttachedTo(gauntlets, merchant);
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(gauntlets));
		scn.FreepsUseCardAction(gauntlets);

		// The bearer pays; the skirmish is Boromir's and not the bearer's, so nothing is to be prevented.
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, brigand));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheSkirmishOfAGondorManWhoIsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var faramir = scn.GetRingBearer();
		var gauntlets = scn.GetFreepsCard("gauntlets");
		scn.AttachCardsTo(faramir, gauntlets);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertAttachedTo(gauntlets, faramir);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, brigand);
		scn.FreepsResolveSkirmish(faramir);
		// Faramir's own text as Ring-bearer adds 2 burdens unasked at the start of his skirmish: with one companion
		// no 2 threats can be added.
		scn.SetTwilight(3);

		assertAttachedTo(gauntlets, faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, brigand));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertTrue(scn.FreepsActionAvailable(gauntlets));
		scn.FreepsUseCardAction(gauntlets);
		assertEquals(1, scn.GetWoundsOn(faramir));

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Cancelled: the Brigand's 8 would have beaten Faramir's 7.
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.IsCharSkirmishing(faramir));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathTheRingBearerPaysAndHisSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMultipathScenario();

		var faramir = scn.GetRingBearer();
		var gauntlets = scn.GetFreepsCard("gauntlets");
		scn.AttachCardsTo(faramir, gauntlets);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		assertAttachedTo(gauntlets, faramir);
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(faramir, brigand);
		scn.FreepsResolveSkirmish(faramir);
		scn.SetTwilight(3);

		assertAttachedTo(gauntlets, faramir);
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, brigand));
		assertEquals(0, scn.GetWoundsOn(faramir));
		// The exertion can be paid, so the ability is offered; the skirmish is the Ring-bearer's and goes on, and
		// with nothing to cancel the Shadow player is not asked to prevent it.
		assertTrue(scn.FreepsActionAvailable(gauntlets));
		scn.FreepsUseCardAction(gauntlets);

		assertEquals(1, scn.GetWoundsOn(faramir));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(faramir, brigand));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
