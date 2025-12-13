package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("norearly", "103_37");
					put("uruk1", "1_151"); // Uruk Savage
					put("uruk2", "1_151");
					put("isengardcard", "3_54"); // Hollowing of Isengard - for discard from hand

					put("gandalf", "1_72"); // Gandalf, Friend of the Shirefolk
					put("aragorn", "1_89");
					put("legolas", "1_50");
					put("gimli", "1_13");
					put("boromir", "1_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorisHeEarlyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Nor is He Early
		 * Unique: true
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [isengard] minions.
		* 	Response: If a Free Peoples character is played, add (3).  If you can spot 5 or more companions (or if that character is [Gandalf]), you may discard an [isengard] card from hand to hinder that character.  Hinder this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("norearly");

		assertEquals("Nor is He Early", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NorIsHeEarlyRequiresTwoIsengardMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var norearly = scn.GetShadowCard("norearly");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");

		scn.MoveCardsToHand(norearly, uruk2);
		scn.MoveMinionsToTable(uruk1);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.ShadowPlayAvailable(norearly));
		scn.ShadowPlayCard(uruk2);

		assertTrue(scn.ShadowPlayAvailable(norearly));
		scn.ShadowPlayCard(norearly);

		assertInZone(Zone.SUPPORT, norearly);
	}

	@Test
	public void NorIsHeEarlyResponseAddsTwilightAndHindersSelf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var norearly = scn.GetShadowCard("norearly");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(norearly);
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		int twilightBefore = scn.GetTwilight();
		assertFalse(scn.IsHindered(norearly));

		scn.FreepsPlayCard(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable("Nor Is He Early"));
		scn.ShadowAcceptOptionalTrigger();

		// +3 from response, +4 from Aragorn's cost
		assertEquals(twilightBefore + 3 + 4, scn.GetTwilight());
		assertTrue(scn.IsHindered(norearly));
	}

	@Test
	public void NorIsHeEarlyCanHinderGandalfCharacterByDiscardingFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var norearly = scn.GetShadowCard("norearly");
		var isengardcard = scn.GetShadowCard("isengardcard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.MoveCardsToSupportArea(norearly);
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.MoveCardsToHand(isengardcard);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		assertFalse(scn.IsHindered(gandalf));

		scn.FreepsPlayCard(gandalf);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		scn.ShadowChooseYes();

		assertInZone(Zone.DISCARD, isengardcard);
		assertTrue(scn.IsHindered(gandalf));
		assertTrue(scn.IsHindered(norearly));
	}

	@Test
	public void NorIsHeEarlyCanHinderAnyCharacterWith5PlusCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var norearly = scn.GetShadowCard("norearly");
		var isengardcard = scn.GetShadowCard("isengardcard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");

		scn.MoveCardsToSupportArea(norearly);
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.MoveCardsToHand(isengardcard);
		scn.MoveCompanionsToTable(legolas, gimli, boromir);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		// 4 companions currently, playing Aragorn makes 5
		scn.FreepsPlayCard(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowDecisionAvailable("Would you like to discard"));
		scn.ShadowChooseYes();

		assertInZone(Zone.DISCARD, isengardcard);
		assertTrue(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(norearly));
	}

	@Test
	public void NorIsHeEarlyCannotHinderNonGandalfWithLessThan5Companions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var norearly = scn.GetShadowCard("norearly");
		var isengardcard = scn.GetShadowCard("isengardcard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCardsToSupportArea(norearly);
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.MoveCardsToHand(isengardcard);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();

		scn.FreepsPlayCard(aragorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// Not offered - less than 5 companions, not Gandalf
		assertFalse(scn.ShadowDecisionAvailable("Would you like to discard"));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.IsHindered(norearly));
	}

	@Test
	public void NorIsHeEarlyCanDeclineToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var norearly = scn.GetShadowCard("norearly");
		var isengardcard = scn.GetShadowCard("isengardcard");
		var uruk1 = scn.GetShadowCard("uruk1");
		var uruk2 = scn.GetShadowCard("uruk2");
		var gandalf = scn.GetFreepsCard("gandalf");

		scn.MoveCardsToSupportArea(norearly);
		scn.MoveMinionsToTable(uruk1, uruk2);
		scn.MoveCardsToHand(isengardcard);
		scn.MoveCardsToHand(gandalf);

		scn.StartGame();

		int twilightBefore = scn.GetTwilight();

		scn.FreepsPlayCard(gandalf);

		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(gandalf));
		assertFalse(scn.IsHindered(norearly));
		assertInZone(Zone.HAND, isengardcard);

		//No additional (3) from activating the card
		assertEquals(twilightBefore + 4, scn.GetTwilight());
	}
}
