package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_160_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");

					put("sword", "51_160");
					put("uruk", "1_154");
					put("chaff1", "1_155");
					put("chaff2", "1_156");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukhaiSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 1
		* Title: Uruk-hai Sword
		* Unique: False
		* Side: SHADOW
		* Culture: Isengard
		* Twilight Cost: 1
		* Type: possession
		* Subtype: Hand Weapon
		* Strength: 2
		* Game Text: Bearer must be an Uruk-hai. 
		* 	Each time bearer wins a skirmish, the Free Peoples player must exert a companion or discard the top 2 cards of their draw deck.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertFalse(sword.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sword.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void UrukhaiSwordPlaysOnUruk() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);
		var sword = scn.GetShadowCard("sword");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPassCurrentPhaseAction();

		assertTrue(scn.ShadowPlayAvailable(sword));
		assertEquals(Zone.HAND, sword.getZone());
		scn.ShadowPlayCard(sword);
		assertEquals(Zone.ATTACHED, sword.getZone());
		assertEquals(uruk, sword.getAttachedTo());
	}

	@Test
	public void UrukhaiSwordSkirmishWinCanCauseExert() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionToTable(aragorn);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);
		var sword = scn.GetShadowCard("sword");
		scn.AttachCardsTo(uruk, sword);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveActionOrder("sword");

		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseOption("exert");
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void UrukhaiSwordSkirmishWinCanCauseTopDeckDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionToTable(aragorn);

		var uruk = scn.GetShadowCard("uruk");
		scn.MoveMinionsToTable(uruk);
		var sword = scn.GetShadowCard("sword");
		scn.AttachCardsTo(uruk, sword);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveActionOrder("skirmish");

		assertTrue(scn.FreepsAnyDecisionsAvailable());
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsChooseOption("discard");
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(2, scn.GetFreepsDiscardCount());
	}
}
