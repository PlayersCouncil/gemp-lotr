package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.cards.GenericCardTestHelper;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_053_Tests
{

	protected GenericCardTestHelper GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new GenericCardTestHelper(
				new HashMap<>()
				{{
					put("red", "102_53");
					put("velf", "55_10");
					put("varagorn", "102_16");
					put("vrohan", "7_224");
					put("rohan", "12_107");

					//Maneuver ability and event
					put("albert", "1_69");
					put("valor", "1_103");

					put("freeps1", "1_70");
					put("freeps2", "1_71");
					put("freeps3", "1_72");
					put("freeps4", "1_73");
					put("freeps5", "1_74");

					put("uruk1", "5_45");
					put("uruk2", "5_46");
					put("uruk3", "5_47");
				}},
				GenericCardTestHelper.FellowshipSites,
				GenericCardTestHelper.FOTRFrodo,
				GenericCardTestHelper.RulingRing
		);
	}

	@Test
	public void AndtheRedDawnStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: And the Red Dawn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Maneuver: Spot 3 valiant companions to discard your hand. For each Shadow card discarded, exert a minion. For each Free Peoples card discarded, heal a valiant or [rohan] companion. You cannot make any more Maneuver actions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("red");

		assertEquals("And the Red Dawn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.hasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AndtheRedDawnCannotBePlayedWithout3ValiantCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var red = scn.GetFreepsCard("red");

		var velf = scn.GetFreepsCard("velf");
		var varagorn = scn.GetFreepsCard("varagorn");
		var vrohan = scn.GetFreepsCard("vrohan");
		var rohan = scn.GetFreepsCard("rohan");
		var valor = scn.GetFreepsCard("valor");

		scn.FreepsMoveCharToTable(velf, varagorn, rohan);
		scn.FreepsMoveCardToHand("albert");
		scn.FreepsMoveCardToHand(vrohan, valor);
		scn.FreepsMoveCardToSupportArea(red);

		scn.ShadowMoveCharToTable("uruk1");

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsActionAvailable(red));
		scn.FreepsMoveCharToTable(vrohan);
		scn.FreepsPlayCard(valor);
		scn.ShadowPassCurrentPhaseAction();

		assertTrue(scn.FreepsActionAvailable(red));
	}

	@Test
	public void AndtheRedDawnSpots3ValiantCompanionsToDiscardHandAndHaveEffects() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var red = scn.GetFreepsCard("red");

		scn.FreepsMoveCardToSupportArea(red);

		var frodo = scn.GetRingBearer();

		var velf = scn.GetFreepsCard("velf");
		var varagorn = scn.GetFreepsCard("varagorn");
		var vrohan = scn.GetFreepsCard("vrohan");
		var rohan = scn.GetFreepsCard("rohan");

		scn.FreepsMoveCharToTable(velf, varagorn, vrohan, rohan);

		scn.AddWoundsToChar(velf, 2);
		scn.AddWoundsToChar(varagorn, 2);
		scn.AddWoundsToChar(vrohan, 2);
		scn.AddWoundsToChar(rohan, 2);
		scn.AddWoundsToChar(frodo, 2);

		var albert = scn.GetFreepsCard("albert");
		var valor = scn.GetFreepsCard("valor");

		scn.FreepsMoveCharToTable(albert);
		scn.FreepsMoveCardToHand(valor);

		var freeps1 = scn.GetFreepsCard("freeps1");
		var freeps2 = scn.GetFreepsCard("freeps2");
		var freeps3 = scn.GetFreepsCard("freeps3");
		var freeps4 = scn.GetFreepsCard("freeps4");
		var freeps5 = scn.GetFreepsCard("freeps5");
		var shadow1 = scn.GetFreepsCard("uruk1");
		var shadow2 = scn.GetFreepsCard("uruk2");
		var shadow3 = scn.GetFreepsCard("uruk3");

		scn.FreepsMoveCardToHand(freeps1, freeps2, freeps3, freeps4, freeps5, shadow1, shadow2, shadow3);

		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var uruk3 = scn.GetShadowCard("uruk3");

		scn.ShadowMoveCharToTable(uruk1, uruk2, uruk3);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(red));
		assertTrue(scn.FreepsPlayAvailable(valor));
		assertTrue(scn.FreepsActionAvailable(albert));
		scn.FreepsMoveCardToDiscard(valor);

		assertEquals(Zone.HAND,  freeps1.getZone());
		assertEquals(Zone.HAND,  freeps2.getZone());
		assertEquals(Zone.HAND,  freeps3.getZone());
		assertEquals(Zone.HAND,  freeps4.getZone());
		assertEquals(Zone.HAND,  freeps5.getZone());
		assertEquals(Zone.HAND,  shadow1.getZone());
		assertEquals(Zone.HAND,  shadow2.getZone());
		assertEquals(Zone.HAND,  shadow3.getZone());

		assertEquals(2, scn.GetWoundsOn(velf));
		assertEquals(2, scn.GetWoundsOn(varagorn));
		assertEquals(2, scn.GetWoundsOn(vrohan));
		assertEquals(2, scn.GetWoundsOn(rohan));
		assertEquals(2, scn.GetWoundsOn(frodo));

		assertEquals(0, scn.GetWoundsOn(uruk1));
		assertEquals(0, scn.GetWoundsOn(uruk2));
		assertEquals(0, scn.GetWoundsOn(uruk3));

		scn.FreepsUseCardAction(red);

		//Should have 3 exertions for 3 discarded shadow cards
		//1
		assertTrue(scn.FreepsDecisionAvailable("choose cards from hand to discard"));
		scn.FreepsChooseCard(shadow1);
		assertTrue(scn.FreepsDecisionAvailable("exert"));
		var choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains(String.valueOf(uruk1.getCardId())));
		assertTrue(choices.contains(String.valueOf(uruk2.getCardId())));
		assertTrue(choices.contains(String.valueOf(uruk3.getCardId())));
		scn.FreepsChooseCard(uruk1);
		assertEquals(1, scn.GetWoundsOn(uruk1));

		//2
		assertTrue(scn.FreepsDecisionAvailable("choose cards from hand to discard"));
		scn.FreepsChooseCard(shadow2);
		assertTrue(scn.FreepsDecisionAvailable("exert"));
		choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains(String.valueOf(uruk1.getCardId())));
		assertTrue(choices.contains(String.valueOf(uruk2.getCardId())));
		assertTrue(choices.contains(String.valueOf(uruk3.getCardId())));
		scn.FreepsChooseCard(uruk2);
		assertEquals(1, scn.GetWoundsOn(uruk2));

		//3
		assertTrue(scn.FreepsDecisionAvailable("choose cards from hand to discard"));
		scn.FreepsChooseCard(shadow3);
		assertTrue(scn.FreepsDecisionAvailable("exert"));
		choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains(String.valueOf(uruk1.getCardId())));
		assertTrue(choices.contains(String.valueOf(uruk2.getCardId())));
		assertTrue(choices.contains(String.valueOf(uruk3.getCardId())));
		scn.FreepsChooseCard(uruk3);
		assertEquals(1, scn.GetWoundsOn(uruk3));


		//Should have 5 heals for 5 discarded freeps cards
		//1
		assertTrue(scn.FreepsDecisionAvailable("choose cards from hand to discard"));
		scn.FreepsChooseCard(freeps1);
		assertTrue(scn.FreepsDecisionAvailable("heal"));
		choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains(String.valueOf(velf.getCardId())));
		assertTrue(choices.contains(String.valueOf(varagorn.getCardId())));
		assertTrue(choices.contains(String.valueOf(vrohan.getCardId())));
		assertTrue(choices.contains(String.valueOf(rohan.getCardId())));
		assertFalse(choices.contains(String.valueOf(frodo.getCardId())));
		scn.FreepsChooseCard(velf);
		assertEquals(1, scn.GetWoundsOn(velf));

		//2
		assertTrue(scn.FreepsDecisionAvailable("choose cards from hand to discard"));
		scn.FreepsChooseCard(freeps2);
		assertTrue(scn.FreepsDecisionAvailable("heal"));
		choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains(String.valueOf(velf.getCardId())));
		assertTrue(choices.contains(String.valueOf(varagorn.getCardId())));
		assertTrue(choices.contains(String.valueOf(vrohan.getCardId())));
		assertTrue(choices.contains(String.valueOf(rohan.getCardId())));
		assertFalse(choices.contains(String.valueOf(frodo.getCardId())));
		scn.FreepsChooseCard(varagorn);
		assertEquals(1, scn.GetWoundsOn(varagorn));

		//3
		assertTrue(scn.FreepsDecisionAvailable("choose cards from hand to discard"));
		scn.FreepsChooseCard(freeps3);
		assertTrue(scn.FreepsDecisionAvailable("heal"));
		choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains(String.valueOf(velf.getCardId())));
		assertTrue(choices.contains(String.valueOf(varagorn.getCardId())));
		assertTrue(choices.contains(String.valueOf(vrohan.getCardId())));
		assertTrue(choices.contains(String.valueOf(rohan.getCardId())));
		assertFalse(choices.contains(String.valueOf(frodo.getCardId())));
		scn.FreepsChooseCard(vrohan);
		assertEquals(1, scn.GetWoundsOn(vrohan));

		//4
		assertTrue(scn.FreepsDecisionAvailable("choose cards from hand to discard"));
		scn.FreepsChooseCard(freeps4);
		assertTrue(scn.FreepsDecisionAvailable("heal"));
		choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains(String.valueOf(velf.getCardId())));
		assertTrue(choices.contains(String.valueOf(varagorn.getCardId())));
		assertTrue(choices.contains(String.valueOf(vrohan.getCardId())));
		assertTrue(choices.contains(String.valueOf(rohan.getCardId())));
		assertFalse(choices.contains(String.valueOf(frodo.getCardId())));
		scn.FreepsChooseCard(rohan);
		assertEquals(1, scn.GetWoundsOn(rohan));

		//5
		//last card discard is automatic
		assertTrue(scn.FreepsDecisionAvailable("heal"));
		choices = scn.FreepsGetCardChoices();
		assertTrue(choices.contains(String.valueOf(velf.getCardId())));
		assertTrue(choices.contains(String.valueOf(varagorn.getCardId())));
		assertTrue(choices.contains(String.valueOf(vrohan.getCardId())));
		assertTrue(choices.contains(String.valueOf(rohan.getCardId())));
		assertFalse(choices.contains(String.valueOf(frodo.getCardId())));
		scn.FreepsChooseCard(velf);
		assertEquals(0, scn.GetWoundsOn(velf));

		scn.FreepsMoveCardToHand(valor);
		scn.ShadowPassCurrentPhaseAction();
		assertTrue(scn.FreepsDecisionAvailable("maneuver"));
		assertFalse(scn.FreepsPlayAvailable(valor));
		assertFalse(scn.FreepsPlayAvailable(valor));
		assertFalse(scn.FreepsActionAvailable(red));

		assertEquals(Zone.DISCARD,  freeps1.getZone());
		assertEquals(Zone.DISCARD,  freeps2.getZone());
		assertEquals(Zone.DISCARD,  freeps3.getZone());
		assertEquals(Zone.DISCARD,  freeps4.getZone());
		assertEquals(Zone.DISCARD,  freeps5.getZone());
		assertEquals(Zone.DISCARD,  shadow1.getZone());
		assertEquals(Zone.DISCARD,  shadow2.getZone());
		assertEquals(Zone.DISCARD,  shadow3.getZone());
	}
}
