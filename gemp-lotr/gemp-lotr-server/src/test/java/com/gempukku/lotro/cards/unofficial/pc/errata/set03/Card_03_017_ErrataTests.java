package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_017_ErrataTests
{
	// Both players' adventure decks are built from the same sites, so each holds several site 2s and two site 3s.
	protected VirtualTableScenario GetFellowshipScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "53_17");
					put("celeborn", "1_34");    // Celeborn: Elf ally; a fellowship ability used only on purpose
					put("greenleaf", "1_50");   // Legolas, Greenleaf: Elf companion, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: Man companion
				}},
				new HashMap<>() {{
					put("site1", "1_319");      // Bag End
					put("site2", "1_332");      // Midgewater Marshes: Marsh, shadow number 1; each Hobbit that moves there exerts
					put("site3", "1_337");      // Council Courtyard
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site2F", "1_329");     // Breeland Forest: Fellowship-block forest site 2
					put("site2T", "4_330");     // Derndingle: Towers-block forest site 2, shadow number 2
					put("site3F", "1_342");     // Rivendell Waterfall: forest site 3
					put("siteX", "11_231");     // Caras Galadhon: Shadows-block forest, no site number
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Multipath
		);
	}

	protected VirtualTableScenario GetNoForestScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "53_17");
				}},
				// Site 2 is Ettenmoors, plains, and no site 2 in either deck is a forest.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHobbitonScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "53_17");
				}},
				new HashMap<>() {{
					put("site1", "2_116");      // Hobbiton Woods: Fellowship: exert a Hobbit to play your site 2
					put("site2", "1_329");      // Breeland Forest: forest site 2
					put("site3", "1_337");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_350");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetShadowsPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "53_17");
				}},
				new HashMap<>() {{
					put("site1", "11_239");     // Fangorn Glade: forest
					put("site2", "13_185");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");     // The Angle: forest
					put("site9", "11_231");     // Caras Galadhon: forest
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Galadriel, Lady of the Golden Wood
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Home: 6
		 * Game Text: At the start of each of your turns, you may heal another Elf.
		 * Each time the fellowship is about to move to an opponent's site during the Fellowship phase, you may
		 * exert Galadriel to replace that site with a forest of the same site number from your adventure deck.
		 */

		var scn = GetFellowshipScenario();

		var galadriel = scn.GetFreepsCard("galadriel");

		assertEquals("Galadriel", galadriel.getBlueprint().getTitle());
		assertEquals("Lady of the Golden Wood", galadriel.getBlueprint().getSubtitle());
		assertTrue(galadriel.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, galadriel.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, galadriel.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, galadriel.getBlueprint().getCardType());
		assertEquals(Race.ELF, galadriel.getBlueprint().getRace());
		assertEquals(3, galadriel.getBlueprint().getTwilightCost());
		assertEquals(3, galadriel.getBlueprint().getStrength());
		assertEquals(3, galadriel.getBlueprint().getVitality());
		assertTrue(galadriel.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
	}

	@Test
	public void IsPlayedFromHandForThreeToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoForestScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCardsToHand(galadriel);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(galadriel));
		scn.FreepsPlayCard(galadriel);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtTheStartOfEachOfYourTurnsMayHealAnotherElfCompanionOrAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var celeborn = scn.GetFreepsCard("celeborn");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(greenleaf, aragorn);
		scn.MoveAlliesToTable(galadriel, celeborn);
		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(celeborn, 1);
		scn.AddWoundsToChar(greenleaf, 2);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);

		// Another Elf, companion or ally; not Galadriel herself, and not the wounded Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(celeborn, greenleaf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(galadriel, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(greenleaf);

		assertEquals(1, scn.GetWoundsOn(greenleaf));
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The opponent's turn passes, and at the start of the next of Freeps' turns it is offered again.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site2"));
		scn.FreepsDeclineOptionalTrigger();
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		assertEquals(Phase.BETWEEN_TURNS, scn.GetCurrentPhase());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
	}

	@Test
	public void DecliningTheHealHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		scn.MoveCompanionsToTable(greenleaf);
		scn.MoveAlliesToTable(galadriel);
		scn.AddWoundsToChar(greenleaf, 1);

		scn.StartGame();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(greenleaf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HealIsNotOfferedWhenOnlyGaladrielAndANonElfAreWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var aragorn = scn.GetFreepsCard("aragorn");
		var greenleaf = scn.GetFreepsCard("greenleaf");
		scn.MoveCompanionsToTable(aragorn, greenleaf);
		scn.MoveAlliesToTable(galadriel);
		// Galadriel herself and a Man are wounded; the Elf beside them is not.
		scn.AddWoundsToChar(galadriel, 1);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsToReplaceTheOpponentsSiteWithAForestOfTheSameNumberBeforeTheFellowshipArrives() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var frodo = scn.GetRingBearer();
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		var shadowMarshes = scn.GetShadowSite("site2");
		var freepsMarshes = scn.GetFreepsSite("site2");
		var freepsBreeland = scn.GetFreepsSite("site2F");
		var freepsDerndingle = scn.GetFreepsSite("site2T");
		var freepsWaterfall = scn.GetFreepsSite("site3F");
		var freepsCaras = scn.GetFreepsSite("siteX");

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowMarshes);

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);
		assertEquals(1, scn.GetWoundsOn(galadriel));

		// Forests numbered 2, of the Fellowship block and of the Towers block; not the non-forest site 2, a forest
		// site 3, or a forest with no number.
		assertTrue(scn.FreepsHasBPChoiceAvailable(freepsBreeland.getBlueprintId()));
		assertTrue(scn.FreepsHasBPChoiceAvailable(freepsDerndingle.getBlueprintId()));
		assertFalse(scn.FreepsHasBPChoiceAvailable(freepsMarshes.getBlueprintId()));
		assertFalse(scn.FreepsHasBPChoiceAvailable(freepsWaterfall.getBlueprintId()));
		assertFalse(scn.FreepsHasBPChoiceAvailable(freepsCaras.getBlueprintId()));
		scn.FreepsChooseCardBPFromSelection(freepsDerndingle);

		assertEquals(freepsDerndingle, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_PATH, freepsDerndingle);
		assertInZone(Zone.ADVENTURE_DECK, shadowMarshes);
		// Midgewater Marshes was replaced before the fellowship moved, so Frodo did not exert.
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Derndingle's shadow number 2 + 1 companion
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesTheOpponentsSiteToBeMovedTo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var frodo = scn.GetRingBearer();
		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		var shadowMarshes = scn.GetShadowSite("site2");

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowMarshes);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(shadowMarshes, scn.GetCurrentSite());
		// Midgewater Marshes makes each Hobbit moving there exert.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnExhaustedGaladrielIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		scn.AddWoundsToChar(galadriel, 2);
		var shadowMarshes = scn.GetShadowSite("site2");

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowMarshes);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(shadowMarshes, scn.GetCurrentSite());
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenMovingToTheOpponentsSiteInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		var shadowMarshes = scn.GetShadowSite("site2");
		var shadowWaterfall = scn.GetShadowSite("site3F");

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowMarshes);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(scn.P2());
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(shadowWaterfall);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(shadowWaterfall, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenMovingToYourOwnSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitonScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		var hobbitonWoods = scn.GetFreepsSite(1);
		var freepsBreeland = scn.GetFreepsSite("site2");

		scn.StartGame();
		scn.FreepsUseCardAction(hobbitonWoods);
		assertInZone(Zone.ADVENTURE_PATH, freepsBreeland);

		scn.FreepsPass();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(freepsBreeland, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoForestOfThatNumberInTheAdventureDeckGaladrielStillExertsAndNothingIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoForestScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		var shadowSite2 = scn.GetShadowSite(2);
		var freepsSite2 = scn.GetFreepsSite(2);

		scn.StartGame();
		scn.FreepsPass();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsChooseOptionalTrigger(galadriel);

		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(shadowSite2, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, freepsSite2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OfferedAgainAtTheFellowshipPhaseMoveOfALaterTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		var shadowMarshes = scn.GetShadowSite("site2");
		var shadowCourtyard = scn.GetShadowSite("site3");

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowMarshes);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowCourtyard);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(galadriel));
	}

	@Test
	public void NotOfferedWhenMovingToAnOpponentsShadowsPathSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsPathScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveAlliesToTable(galadriel);
		var freepsSite2 = scn.GetFreepsSite("site2");
		var shadowForest = scn.GetShadowSite("site9");

		scn.FreepsChooseCardBPFromSelection(freepsSite2);
		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shadowForest);

		assertEquals(shadowForest, scn.GetCurrentSite());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(galadriel));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

}
