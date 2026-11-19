package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_268_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("encirclement", "7_268");
					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc minion, cost 2, no besieger; his skirmish ability is used only on purpose
					put("few", "1_260");            // The Number Must Be Few: [sauron] support condition, cost 2; its text needs 7 companions
					put("despair", "1_243");        // Despair: [sauron] Maneuver event, cost 2, only ever held in hand here
					put("grond", "8_103");          // Grond, Hammer of the Underworld: [sauron] possession (engine), cost 3, only ever held in hand here
					put("hound", "7_192");          // Morgul Hound: a [wraith] Orc minion, cost 2; +4 and fierce only at 2 wounds on the Ring-bearer
					put("pillager", "7_275");       // Gorgoroth Pillager: besieger Orc; its text works only while stacked on a site its owner controls
					put("troll", "8_102");          // Great Hill Troll: besieger Troll; its trigger is for its own play at sites 5K to 9K
					put("explorer", "4_250");       // Southron Explorer: [raider] Man; "Regroup: Exert this minion and remove (5) to take control of a site"
					put("filler1", "1_177");        // Goblin Patrol Troop, cards lying in a draw deck
					put("filler2", "1_177");
					put("filler3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EncirclementStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Encirclement
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: For each 2 sites you control, the twilight cost of each of your [sauron] cards is -1.<br><b>Regroup:</b> Spot a [sauron] minion to draw a card (or draw 2 cards instead if that minion is a besieger). Discard this condition.
		*/

		var scn = GetScenario();

		var encirclement = scn.GetFreepsCard("encirclement");

		assertEquals("Encirclement", encirclement.getBlueprint().getTitle());
		assertNull(encirclement.getBlueprint().getSubtitle());
		assertTrue(encirclement.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, encirclement.getBlueprint().getSide());
		assertEquals(Culture.SAURON, encirclement.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, encirclement.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(encirclement, Keyword.SUPPORT_AREA));
		assertEquals(2, encirclement.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachOfYourSauronCardsIsTwilightCostMinus1ForEach2SitesYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var encirclement = scn.GetShadowCard("encirclement");
		var soldier = scn.GetShadowCard("soldier");
		var few = scn.GetShadowCard("few");
		var despair = scn.GetShadowCard("despair");
		var grond = scn.GetShadowCard("grond");
		var hound = scn.GetShadowCard("hound");
		var freepsSoldier = scn.GetFreepsCard("soldier");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(encirclement);
		scn.MoveCardsToHand(soldier, few, despair, grond, hound, freepsSoldier);
		assertInZone(Zone.SUPPORT, encirclement);
		assertInHand(soldier, few, despair, grond, hound, freepsSoldier);

		// One site his and one the Free Peoples player's: one site he controls, no discount.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertEquals(2, scn.GetTwilightCost(soldier));
		assertEquals(2, scn.GetTwilightCost(few));
		assertEquals(2, scn.GetTwilightCost(despair));
		assertEquals(3, scn.GetTwilightCost(grond));

		// Two sites: -1 on a [sauron] minion, condition, event and possession of his.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(3)));
		assertEquals(1, scn.GetTwilightCost(soldier));
		assertEquals(1, scn.GetTwilightCost(few));
		assertEquals(1, scn.GetTwilightCost(despair));
		assertEquals(2, scn.GetTwilightCost(grond));
		// Not a [wraith] card of his, nor the Free Peoples player's own [sauron] card.
		assertEquals(2, scn.GetTwilightCost(hound));
		assertEquals(2, scn.GetTwilightCost(freepsSoldier));

		// Three sites: still -1.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(4)));
		assertEquals(1, scn.GetTwilightCost(soldier));
		assertEquals(2, scn.GetTwilightCost(grond));

		// Four sites: -2.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(5)));
		assertEquals(0, scn.GetTwilightCost(soldier));
		assertEquals(0, scn.GetTwilightCost(few));
		assertEquals(0, scn.GetTwilightCost(despair));
		assertEquals(1, scn.GetTwilightCost(grond));
		assertEquals(2, scn.GetTwilightCost(hound));
		assertEquals(2, scn.GetTwilightCost(freepsSoldier));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASecondSiteTakenByLegalPlayBringsTheDiscountIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var encirclement = scn.GetShadowCard("encirclement");
		var soldier = scn.GetShadowCard("soldier");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveCardsToSupportArea(encirclement);
		scn.MoveCardsToHand(soldier);
		scn.SetTwilight(10);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(explorer);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, encirclement);
		assertInHand(soldier);
		assertTrue(scn.ShadowControls(scn.GetSite(1)));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(2, scn.GetTwilightCost(soldier));
		assertFalse(scn.ShadowControls(scn.GetSite(2)));

		scn.ShadowUseCardAction(explorer);
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		assertEquals(1, scn.GetTwilightCost(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SpottingASauronMinionWhoIsNoBesiegerDrawsOneCardAndDiscardsTheCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var encirclement = scn.GetShadowCard("encirclement");
		var soldier = scn.GetShadowCard("soldier");
		var hound = scn.GetShadowCard("hound");
		var filler1 = scn.GetShadowCard("filler1");

		scn.StartGame();
		scn.MoveCardsToSupportArea(encirclement);
		scn.SetShadowDrawDeck(filler1, scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"));
		scn.MoveMinionsToTable(soldier, hound);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, encirclement);
		assertEquals(3, scn.GetShadowDeckCount());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(encirclement));

		scn.ShadowUseCardAction(encirclement);
		assertEquals(1, scn.GetShadowHandCount());
		assertInHand(filler1);
		assertEquals(2, scn.GetShadowDeckCount());
		assertInDiscard(encirclement);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SpottingABesiegerTrollDrawsTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var encirclement = scn.GetShadowCard("encirclement");
		var troll = scn.GetShadowCard("troll");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveCardsToSupportArea(encirclement);
		scn.SetShadowDrawDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"));
		scn.MoveMinionsToTable(troll, hound);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, encirclement);
		assertEquals(3, scn.GetShadowDeckCount());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowUseCardAction(encirclement);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertInDiscard(encirclement);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithABesiegerOrcBesideAnOrcWhoIsNoneTwoCardsAreDrawnUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var encirclement = scn.GetShadowCard("encirclement");
		var pillager = scn.GetShadowCard("pillager");
		var soldier = scn.GetShadowCard("soldier");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveCardsToSupportArea(encirclement);
		scn.SetShadowDrawDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"), scn.GetShadowCard("filler3"));
		scn.MoveMinionsToTable(pillager, soldier, hound);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, encirclement);
		assertEquals(3, scn.GetShadowDeckCount());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetShadowHandCount());

		// A besieger can be spotted, so the branch in parentheses is the one taken, and the player is not asked
		// which minion he spots (Ketura on F-0047: "a branch in parenthesis is mandatory to choose if it's available").
		scn.ShadowUseCardAction(encirclement);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetShadowDeckCount());
		assertInDiscard(encirclement);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSauronMinionTheRegroupAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var encirclement = scn.GetShadowCard("encirclement");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveCardsToSupportArea(encirclement);
		scn.SetShadowDrawDeck(scn.GetShadowCard("filler1"), scn.GetShadowCard("filler2"));
		scn.MoveMinionsToTable(hound);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, encirclement);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		// A [wraith] minion, and Encirclement itself a [sauron] card that is no minion.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(encirclement));
	}
}
