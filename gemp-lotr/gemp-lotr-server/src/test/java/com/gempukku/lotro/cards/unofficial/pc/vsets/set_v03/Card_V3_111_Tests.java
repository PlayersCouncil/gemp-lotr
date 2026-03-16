package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V3_111_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("getup", "103_111");
					put("merry", "1_302");
					put("sam", "1_311");
					put("pippin", "1_306");
					put("aragorn", "1_89");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GetUpMisterFrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Get Up, Mister Frodo
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot Frodo to restore a [shire] card.  If you cannot spot more than 4 companions, you may add (3) to reconcile your hand and heal up to 2 [shire] companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("getup");

		assertEquals("Get Up, Mister Frodo", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void GetUpMisterFrodoRestoresAShireCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getup = scn.GetFreepsCard("getup");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(sam, merry, pippin);
		scn.MoveCardsToHand(getup);
		scn.MoveMinionsToTable(runner);
		scn.HinderCard(sam, merry);

		scn.StartGame();



		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(getup);

		assertTrue(scn.FreepsHasCardChoicesAvailable(sam, merry));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(pippin));
		scn.FreepsChooseCard(sam);

		assertFalse(scn.IsHindered(sam));
		assertTrue(scn.IsHindered(merry));

		// Decline the optional twilight/reconcile/heal
		scn.FreepsChooseNo();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void GetUpMisterFrodoOptionalAdds3TwilightAndHeals() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getup = scn.GetFreepsCard("getup");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");

		var runner = scn.GetShadowCard("runner");

		// 3 companions (Frodo + Sam + Merry), well under 5
		scn.MoveCompanionsToTable(sam, merry, pippin);
		scn.MoveCardsToHand(getup);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 2);
		scn.AddWoundsToChar(merry, 2);
		scn.AddWoundsToChar(pippin, 2);

		scn.MoveMinionsToTable(runner);
		scn.FreepsPass(); // move to site 3

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(getup);
		int twilight = scn.GetTwilight();

		// Accept optional: add (3), reconcile hand, heal up to 2 Shire companions
		scn.FreepsChooseYes();

		// Twilight should have increased by 3
		assertEquals(twilight + 3, scn.GetTwilight());

		scn.FreepsChooseCards(sam, merry);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetWoundsOn(pippin));
	}

	@Test
	public void GetUpMisterFrodoOptionalNotAvailableWith5PlusCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var getup = scn.GetFreepsCard("getup");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");

		// 5 companions: Frodo + Sam + Merry + Pippin + Aragorn
		scn.MoveCompanionsToTable(sam, merry, pippin, aragorn);
		scn.MoveCardsToHand(getup);
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToPhase(Phase.MANEUVER);

		// Play event, restore Sam
		scn.FreepsPlayCard(getup);

		// With 5+ companions, optional is NOT offered — no twilight change
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
