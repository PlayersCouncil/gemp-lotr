package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_083_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("force", "11_83");
					put("bree", "11_90");       // Man of Bree: a [men] Man minion of vitality 1; strength +1 for each card in the Free Peoples hand
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; maneuver and regroup abilities used only on purpose
					put("companies", "12_58");  // Countless Companies: a [men] condition in the support area, no minion; nothing in these tests sets off its text
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man minion; strength +3 and fierce only while 2 burdens are spotted
					put("defend", "4_115");     // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion once
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the [gondor] Man who exerts for it
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ForceofHaradStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Force of Harad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: When you cannot spot another [men] minion, discard this minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("force");

		assertEquals("Force of Harad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedWithNoOtherMenMinionItIsDiscardedThoughAMenConditionAndAManOfAnotherCultureAreInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var force = scn.GetShadowCard("force");
		var companies = scn.GetShadowCard("companies");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(force);
		scn.MoveCardsToSupportArea(companies);

		scn.StartGame();
		scn.MoveMinionsToTable(axeman);
		scn.SetTwilight(10);
		assertInHand(force);
		assertInPlay(companies, axeman);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(force));
		scn.ShadowPlayCard(force);

		// "Another": the Force itself does not count, and neither the [men] condition nor the [raider] Man does.
		assertInDiscard(force);
		assertInPlay(companies, axeman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedBesideAMenMinionWhoIsNoManItStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var force = scn.GetShadowCard("force");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(force);

		scn.StartGame();
		scn.MoveMinionsToTable(ted);
		scn.SetTwilight(10);
		assertInHand(force);
		assertInPlay(ted);
		scn.FreepsPass();

		scn.ShadowPlayCard(force);

		assertInZone(Zone.SHADOW_CHARACTERS, force, ted);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsDiscardedWhenTheOtherMenMinionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var force = scn.GetShadowCard("force");
		var bree = scn.GetShadowCard("bree");

		scn.StartGame();
		scn.MoveMinionsToTable(force, bree);
		scn.SkipToPhase(Phase.MANEUVER);

		// While the Man of Bree is in play, the Force stays.
		assertInZone(Zone.SHADOW_CHARACTERS, force, bree);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(force, bree));
		scn.FreepsChooseCard(bree);

		// The Man of Bree, vitality 1, is killed; with no other [men] minion to spot, the Force goes too.
		assertInDiscard(bree, force);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
