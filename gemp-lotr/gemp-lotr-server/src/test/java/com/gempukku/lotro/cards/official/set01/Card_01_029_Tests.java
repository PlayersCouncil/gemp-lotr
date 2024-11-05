package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_029_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("enmity", "1_29");
					put("arwen", "1_30");
					put("gwemegil", "1_47");
					put("elrond", "1_40");

					put("uruk", "1_145");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void AncientEnmityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Ancient Enmity
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an Elf strength +1.
		 * 	If a minion loses this skirmish to that Elf, that minion's owner discards 2 cards at random from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enmity");

		assertEquals("Ancient Enmity", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.hasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AncientEnmityLosingSkirmishDoesNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.FreepsMoveCharToTable(arwen);
		scn.FreepsMoveCardToSupportArea(elrond);
		scn.FreepsAttachCardsTo(arwen, gwemegil);
		scn.FreepsMoveCardToHand(enmity);

		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCharToTable(uruk);
		scn.ShadowMoveCardToHand("arwen", "elrond", "enmity");

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, uruk);
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6 + 2, scn.GetStrength(arwen));

		assertTrue(scn.FreepsPlayAvailable(enmity));
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(arwen);
		assertEquals(6 + 2 + 1, scn.GetStrength(arwen));

		assertEquals(3, scn.GetShadowHandCount());
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();
		assertEquals(3, scn.GetShadowHandCount());
	}

	@Test
	public void AncientEnmityWinningSkirmishDiscards2CardsRandomlyFromShadowHand() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var arwen = scn.GetFreepsCard("arwen");
		var gwemegil = scn.GetFreepsCard("gwemegil");
		var elrond = scn.GetFreepsCard("elrond");
		scn.FreepsMoveCharToTable(arwen);
		scn.FreepsMoveCardToSupportArea(elrond);
		scn.FreepsAttachCardsTo(arwen, gwemegil);
		scn.FreepsMoveCardToHand(enmity);

		var uruk = scn.GetShadowCard("uruk");
		scn.ShadowMoveCharToTable(uruk);
		scn.ShadowMoveCardToHand("arwen", "elrond", "enmity");

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

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowPassCurrentPhaseAction();
		scn.FreepsPassCurrentPhaseAction();

		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(2 + 1, scn.GetShadowDiscardCount()); //2 hand cards + dead uruk
	}
}
