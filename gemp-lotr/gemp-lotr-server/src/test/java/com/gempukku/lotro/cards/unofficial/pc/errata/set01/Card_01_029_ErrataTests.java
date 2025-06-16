package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_029_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enmity", "51_29");
					put("arwen", "1_30");
					put("gwemegil", "1_47");
					put("elrond", "1_40");

					put("uruk", "1_145");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AncientEnmityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Ancient Enmity
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Elven
		* Twilight Cost: 0
		* Type: event
		* Subtype: Skirmish
		* Game Text: Make an Elf strength +1. 
		* 	If a minion loses this skirmish to that Elf, you may heal an Elf; any shadow player may discard a card from hand to prevent this.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");

		assertFalse(enmity.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, enmity.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, enmity.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, enmity.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(enmity, Timeword.SKIRMISH));
		assertEquals(0, enmity.getBlueprint().getTwilightCost());
	}

	@Test
	public void AncientEnmityLosingSkirmishDoesNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionToTable(arwen);
		scn.MoveCardsToSupportArea(elrond);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(enmity);

		var uruk = scn.GetShadowCard("uruk");
		var card1 = scn.GetShadowCard("arwen");
		var card2 = scn.GetShadowCard("elrond");
		scn.MoveMinionsToTable(uruk);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, uruk);
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6 + 2, scn.GetStrength(arwen));

		assertTrue(scn.FreepsPlayAvailable(enmity));
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(arwen);
		assertEquals(6 + 2 + 1, scn.GetStrength(arwen));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void AncientEnmityWinningSkirmishHealsAnElf() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionToTable(arwen);
		scn.MoveCardsToSupportArea(elrond);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(enmity);

		var uruk = scn.GetShadowCard("uruk");
		var card1 = scn.GetShadowCard("arwen");
		var card2 = scn.GetShadowCard("elrond");
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToHand(card1, card2);

		scn.StartGame();

		scn.AddWoundsToChar(elrond, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, uruk);
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6 + 2, scn.GetStrength(arwen));

		assertTrue(scn.FreepsPlayAvailable(enmity));
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(arwen);
		assertEquals(6 + 2 + 1, scn.GetStrength(arwen));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(gwemegil);
		assertEquals(6 + 2 + 1 + 1, scn.GetStrength(arwen));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetShadowHandCount());

		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertTrue(scn.FreepsDecisionAvailable("Choose Elf to heal"));
		var choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains("" + arwen.getCardId()));
		assertTrue(choices.contains("" + elrond.getCardId()));
		scn.FreepsChooseCard(elrond);

		assertEquals(1, scn.GetWoundsOn(elrond));
		scn.ShadowChooseNo();
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(arwen));
	}

	@Test
	public void AncientEnmityHealCanBePreventedByShadowDiscardingFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionToTable(arwen);
		scn.MoveCardsToSupportArea(elrond);
		scn.AttachCardsTo(arwen, gwemegil);
		scn.MoveCardsToHand(enmity);

		var uruk = scn.GetShadowCard("uruk");
		var card1 = scn.GetShadowCard("arwen");
		var card2 = scn.GetShadowCard("elrond");
		scn.MoveMinionsToTable(uruk);
		scn.MoveCardsToHand(card1, card2);

		scn.StartGame();

		scn.AddWoundsToChar(elrond, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, uruk);
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6 + 2, scn.GetStrength(arwen));

		assertTrue(scn.FreepsPlayAvailable(enmity));
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(arwen);
		assertEquals(6 + 2 + 1, scn.GetStrength(arwen));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsUseCardAction(gwemegil);
		assertEquals(6 + 2 + 1 + 1, scn.GetStrength(arwen));

		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertTrue(scn.FreepsDecisionAvailable("Choose Elf to heal"));
		scn.FreepsChooseCard(elrond);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(elrond));
		scn.ShadowChooseYes();

		assertEquals(Zone.HAND, card1.getZone());
		assertEquals(Zone.HAND, card2.getZone());
		scn.ShadowChooseCardIDFromSelection(card1);
		assertEquals(Zone.DISCARD, card1.getZone());
		assertEquals(Zone.HAND, card2.getZone());

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(arwen));
	}
}
