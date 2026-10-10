package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_94");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHealOrRemoveScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("athelas", "1_94");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Man who bears it, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: a second companion, vitality 3
					put("bounder", "1_286");    // Bounder: [shire] ally, vitality 2: a wounded character who is no companion
					put("saga", "1_114");       // The Saga of Elendil: a Free Peoples condition on a [gondor] companion (vitality +1)

					put("breath", "1_207");     // Black Breath: Shadow condition; wounds may not be removed from its bearer
					put("defense", "1_244");    // Desperate Defense of the Ring: Shadow condition on a companion; acts only when he is assigned
					put("tip", "1_209");        // Blade Tip: Shadow condition in the support area, on no companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AthelasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Athelas
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: 
		 * Game Text: Bearer must be a [gondor] Man.<br><b>Fellowship:</b> Discard this possession to heal a companion or to remove a Shadow condition from a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Athelas", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithNobodyWoundedAndNoShadowConditionItIsDiscardedAndNothingIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealOrRemoveScenario();

		var athelas = scn.GetFreepsCard("athelas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var tip = scn.GetShadowCard("tip");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		// A wound on an ally and a Shadow condition on no companion: nothing for either half to act on.
		scn.AddWoundsToChar(bounder, 1);
		scn.MoveCardsToSupportArea(tip);

		assertTrue(scn.FreepsActionAvailable(athelas));

		scn.FreepsUseCardAction(athelas);
		assertInZone(Zone.DISCARD, athelas);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertInZone(Zone.SUPPORT, tip);
	}

	@Test
	public void WithOnlyAWoundedCompanionItHealsWithoutAskingWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealOrRemoveScenario();

		var athelas = scn.GetFreepsCard("athelas");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var tip = scn.GetShadowCard("tip");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.MoveCardsToSupportArea(tip);

		scn.FreepsUseCardAction(athelas);
		assertInZone(Zone.DISCARD, athelas);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		// A companion to heal: either wounded companion, not the wounded ally, not the unwounded Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertInZone(Zone.SUPPORT, tip);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOnlyAShadowConditionOnACompanionItDiscardsItWithoutAskingWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealOrRemoveScenario();

		var athelas = scn.GetFreepsCard("athelas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var saga = scn.GetFreepsCard("saga");
		var breath = scn.GetShadowCard("breath");
		var defense = scn.GetShadowCard("defense");
		var tip = scn.GetShadowCard("tip");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(aragorn, athelas, saga);

		scn.StartGame();
		// No companion is wounded; the ally is.
		scn.AddWoundsToChar(bounder, 1);
		scn.AttachCardsTo(gimli, breath);
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveCardsToSupportArea(tip);

		assertEquals(0, scn.GetShadowDiscardCount());

		scn.FreepsUseCardAction(athelas);
		assertInZone(Zone.DISCARD, athelas);
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		// A Shadow condition on a companion: either of the two, not the Shadow condition in the support area,
		// not the Free Peoples condition Aragorn bears.
		assertTrue(scn.FreepsHasCardChoiceAvailable(breath, defense));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(tip, saga));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(breath);
		assertInZone(Zone.DISCARD, breath);
		assertEquals(1, scn.GetShadowDiscardCount());
		assertAttachedTo(defense, aragorn);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, tip);
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAWoundedCompanionAndAShadowConditionThePlayerIsAskedWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealOrRemoveScenario();

		var athelas = scn.GetFreepsCard("athelas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		// The wound and the Black Breath are on different companions: Black Breath's bearer may not be healed.
		scn.AddWoundsToChar(gimli, 2);
		scn.AttachCardsTo(aragorn, breath);

		scn.FreepsUseCardAction(athelas);
		assertInZone(Zone.DISCARD, athelas);
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Heal a companion"));
		assertTrue(scn.FreepsChoiceAvailable("Remove a Shadow condition from a companion"));

		scn.FreepsChoose("Heal a companion");
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertAttachedTo(breath, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithAWoundedCompanionAndAShadowConditionThePlayerAskedWhichMayRemoveTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHealOrRemoveScenario();

		var athelas = scn.GetFreepsCard("athelas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, athelas);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.AttachCardsTo(aragorn, breath);

		scn.FreepsUseCardAction(athelas);
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));

		scn.FreepsChoose("Remove a Shadow condition from a companion");
		assertInZone(Zone.DISCARD, breath, athelas);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
