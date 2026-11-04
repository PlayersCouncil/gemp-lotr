package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_050_Tests
{

	// A Fellowship path with a plains at 2, a forest at 3 and a mountain at 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("passage", "11_50");
					// Sméagol, Poor Creature: [gollum] companion, vitality 4; his skirmish ability is never used
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Smeagol, vitality 4; his assignment ability is never used
					put("shiresmeagol", "103_116");
					// Gollum, Nasty Treacherous Creature: [gollum] minion, vitality 4; his regroup ability is never used
					put("gollum", "5_24");
					// There's Another Way: a [gollum] Free Peoples condition, cost 0, played by spotting Sméagol;
					// its regroup ability is never used
					put("anotherway", "12_40");
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_331");      // Ettenmoors: plains, Shadow number 2
					put("site3", "1_342");      // Rivendell Waterfall: forest, sanctuary, Shadow number 0; no ranger here
					put("site4", "1_348");      // Pass of Caradhras: mountain, Shadow number 2
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A Fellowship path with two rivers in a row: one plain, one a sanctuary.
	protected VirtualTableScenario GetRiverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("passage", "11_50");
					put("smeagol", "6_45");
					// Gollum, Half a Wraith Himself: the [wraith] Gollum, vitality 4; his texts answer a Nazgûl played
					// and the assignment phase, and never come up
					put("wraithgollum", "103_66");
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_330");      // Buckleberry Ferry: river, Shadow number 1
					put("site3", "1_338");      // Ford of Bruinen: river and sanctuary, Shadow number 0
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SafePassageStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Safe Passage
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this condition, name forest, mountain, plains, or river.
		 * Each time the fellowship moves to a site that has the named keyword, you may exert Gollum or Sméagol to remove (4).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("passage");

		assertEquals("Safe Passage", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NamedRiverSmeagolAndThenTheWraithGollumExertToRemove4AtEachMoveToARiver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRiverScenario();

		var frodo = scn.GetRingBearer();
		var passage = scn.GetFreepsCard("passage");
		var smeagol = scn.GetFreepsCard("smeagol");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(passage);

		scn.StartGame();

		scn.FreepsPlayCard(passage);
		// The four keywords are offered and nothing else.
		assertEquals(4, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("forest"));
		assertTrue(scn.FreepsChoiceAvailable("mountain"));
		assertTrue(scn.FreepsChoiceAvailable("plains"));
		assertTrue(scn.FreepsChoiceAvailable("river"));
		scn.FreepsChoose("river");
		assertInZone(Zone.SUPPORT, passage);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPass();

		// Moving to Buckleberry Ferry, a river.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(passage));
		scn.FreepsAcceptOptionalTrigger();
		// Sméagol is the only one who can pay, so nobody is asked.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// 5 - 4, then + 1 for the Ferry + 2 companions
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(wraithgollum);
		scn.SkipToMovementDecision();
		scn.SetTwilight(6);
		assertEquals(6, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(wraithgollum));
		scn.FreepsChooseToMove();

		// Moving to Ford of Bruinen, a river that is also a sanctuary: offered again.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(passage));
		scn.FreepsAcceptOptionalTrigger();
		// Sméagol and the Shadow player's Gollum can each pay; Frodo cannot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, wraithgollum));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(wraithgollum);
		assertEquals(1, scn.GetWoundsOn(wraithgollum));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		// 6 - 4, then + 0 for the Ford + 2 companions
		assertEquals(4, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NamedMountainAMoveToAForestIsNotAnsweredAndTheShadowPlayersGollumMayExertAtTheMountain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var passage = scn.GetFreepsCard("passage");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(passage);

		scn.StartGame();
		scn.SkipToSite(2);

		scn.FreepsPlayCard(passage);
		scn.FreepsChoose("mountain");
		assertInZone(Zone.SUPPORT, passage);

		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsPass();

		// Rivendell Waterfall is a forest and no mountain.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(passage));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// 5 + 0 for the Waterfall + 2 companions
		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(smeagol));

		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToMovementDecision();
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gollum));
		scn.FreepsChooseToMove();

		// Pass of Caradhras is a mountain.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(passage));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, gollum));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gollum);
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// 5 - 4, then + 2 for the Pass + 2 companions
		assertEquals(5, scn.GetTwilight());
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NamedForestTheShireSmeagolExertsAtTheForestAndTheMoveFromItIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var passage = scn.GetFreepsCard("passage");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.MoveCardsToHand(passage);

		scn.StartGame();
		scn.SkipToSite(2);

		scn.FreepsPlayCard(passage);
		scn.FreepsChoose("forest");
		assertInZone(Zone.SUPPORT, passage);

		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(shiresmeagol));
		scn.FreepsPass();

		// Rivendell Waterfall is a forest.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(passage));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(shiresmeagol));
		// 5 - 4, then + 0 for the Waterfall + 2 companions
		assertEquals(3, scn.GetTwilight());
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.REGROUP);
		scn.SkipToMovementDecision();
		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		scn.FreepsChooseToMove();

		// Moving from the forest to the mountain: not answered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(passage));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(1, scn.GetWoundsOn(shiresmeagol));
		// 5 + 2 for the Pass + 2 companions
		assertEquals(9, scn.GetTwilight());
		assertEquals(4, scn.GetCurrentSiteNumber());
	}

	@Test
	public void NamedPlainsTheOfferMayBeDeclinedAndAnotherConditionPlayedAsksForNoName() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var passage = scn.GetFreepsCard("passage");
		var smeagol = scn.GetFreepsCard("smeagol");
		var anotherway = scn.GetFreepsCard("anotherway");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(passage, anotherway);

		scn.StartGame();

		scn.FreepsPlayCard(passage);
		scn.FreepsChoose("plains");
		assertInZone(Zone.SUPPORT, passage);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another condition played after it is asked for no name.
		scn.FreepsPlayCard(anotherway);
		assertInZone(Zone.SUPPORT, anotherway);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SetTwilight(5);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsPass();

		// Ettenmoors is a plains.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(passage));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(smeagol));
		// 5 + 2 for Ettenmoors + 2 companions: nothing removed
		assertEquals(9, scn.GetTwilight());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSmeagolExhaustedAndNoGollumTheMoveToTheNamedSiteOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var passage = scn.GetFreepsCard("passage");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(passage);

		scn.StartGame();

		scn.FreepsPlayCard(passage);
		scn.FreepsChoose("plains");

		// Vitality 4 with 3 wounds: he cannot exert.
		scn.AddWoundsToChar(smeagol, 3);
		scn.SetTwilight(5);
		assertEquals(3, scn.GetWoundsOn(smeagol));
		assertEquals(5, scn.GetTwilight());
		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(passage));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetWoundsOn(smeagol));
		// 5 + 2 for Ettenmoors + 2 companions
		assertEquals(9, scn.GetTwilight());
	}
}
