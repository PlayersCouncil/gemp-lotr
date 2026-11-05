package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_026_Tests
{

	// The Hobbit table: the Hobbit path (site 5 is Mirkwood 30_53, "When the fellowship moves to Mirkwood,
	// discard Gandalf") and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gandalf", "30_26");
					put("bifur", "30_5");       // Bifur, Inarticulate: [dwarven] follower, a Dwarf; a fierce-win trigger only
					put("roac", "32_8");        // Roäc, Son of Carc: [dwarven] follower, a Bird; a maneuver ability only
					put("gwaihir", "31_15");    // Gwaihir, Lord of the Eagles: a [gandalf] follower; a skirmish ability only
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] Dwarf companion, cost 3; a fellowship ability used only on purpose
					put("kili", "30_12");       // Kili, Brother of Fili: [dwarven] Dwarf companion; an archery ability only
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession on a [dwarven] character; damage +1 at a mountain only
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, cost 2
					put("thrain", "32_9");      // Thráin, Father of Thorin: a [dwarven] follower of cost 2 whose subtitle names Thorin
					// The Misadventure of Mr. Underhill: "The twilight cost of each companion played to a site which is
					// not a sanctuary is +2" (Bag End is none), so that a discount of 2 on a companion of cost 2 shows
					put("misadventure", "1_129");
					put("despair", "7_148");    // Fierce in Despair: Regroup event, cost 7, "Spot 2 [raider] Men to discard a companion (except the Ring-bearer)"
					put("axeman1", "4_224");    // Easterling Axeman: [raider] Man minion; +3 and fierce only at 2 burdens
					put("axeman2", "4_224");
					// Dark Sorcery: [wraith] condition, "The first sentence of Gandalf's game text does not apply"
					put("sorcery", "33_61");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GandalfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Gandalf, Friend of Thorin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Wizard
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Wise</b>.<br>While you can spot 2 [dwarven] followers, Gandalf cannot be discarded.<br>The twilight cost of Thorin is -2.<br><b>Fellowship:</b> Discard a [dwarven] follower and add a doubt to draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gandalf");

		assertEquals("Gandalf", card.getBlueprint().getTitle());
		assertEquals("Friend of Thorin", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.WISE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void WithTwoDwarvenFollowersInPlayGandalfIsNotDiscardedOnTheMoveToMirkwood() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, balin);
		// Two [dwarven] followers that differ in race and in where they stand: a Dwarf in the support area and
		// a Bird borne by Balin.
		scn.MoveCardsToSupportArea(bifur);
		scn.AttachCardsTo(balin, roac);
		assertInZone(Zone.SUPPORT, bifur);
		assertAttachedTo(roac, balin);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneDwarvenFollowerBesideAGandalfFollowerAndADwarvenCompanionGandalfIsDiscardedOnTheMoveToMirkwood() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var axe = scn.GetFreepsCard("axe");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, balin);
		// One [dwarven] follower, and beside it what nearly counts: a follower of another culture, a [dwarven]
		// companion and a [dwarven] possession.
		scn.MoveCardsToSupportArea(bifur);
		scn.AttachCardsTo(balin, gwaihir, axe);
		assertInZone(Zone.SUPPORT, bifur);
		assertAttachedTo(gwaihir, balin);
		assertAttachedTo(axe, balin);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);

		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, gandalf);
		assertInZone(Zone.FREE_CHARACTERS, balin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoDwarvenFollowersAShadowDiscardOfACompanionOffersTheDwarvesAndNotGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var kili = scn.GetFreepsCard("kili");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var despair = scn.GetShadowCard("despair");
		scn.MoveCardsToHand(despair);

		scn.StartGame();
		scn.MoveCompanionsToTable(gandalf, balin, kili);
		scn.MoveCardsToSupportArea(bifur);
		scn.AttachCardsTo(kili, roac);

		scn.SkipToPhase(Phase.REGROUP);
		// Two [raider] Men for Fierce in Despair to spot, standing in the regroup phase as minions that lived
		// through the skirmishes do.
		scn.MoveMinionsToTable(scn.GetShadowCard("axeman1"), scn.GetShadowCard("axeman2"));
		scn.SetTwilight(7);
		assertInZone(Zone.SUPPORT, bifur);
		assertAttachedTo(roac, kili);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(despair));
		scn.ShadowPlayCard(despair);
		// The Ring-bearer is excepted by the event, and Gandalf by his own text.
		assertTrue(scn.ShadowHasCardChoiceAvailable(balin, kili));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf, scn.GetRingBearer()));
		scn.ShadowChooseCard(balin);

		assertInZone(Zone.DISCARD, balin);
		assertInZone(Zone.FREE_CHARACTERS, gandalf, kili);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheTwilightCostOfThorinIsMinus2AndNoOtherCardsIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var thrain = scn.GetFreepsCard("thrain");
		scn.MoveShadowCardsToSupportArea("misadventure");
		scn.MoveCardsToHand(thorin, balin, thrain);

		scn.StartGame();
		// Without Gandalf: printed cost + 2 for The Misadventure of Mr. Underhill on each companion.
		assertEquals(4, scn.GetTwilightCost(thorin));
		assertEquals(5, scn.GetTwilightCost(balin));
		assertEquals(2, scn.GetTwilightCost(thrain));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();
		// 2 printed + 2 Misadventure - 2 Gandalf
		assertEquals(2, scn.GetTwilightCost(thorin));
		assertEquals(5, scn.GetTwilightCost(balin));
		assertEquals(2, scn.GetTwilightCost(thrain));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(thorin);
		assertInZone(Zone.FREE_CHARACTERS, thorin);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionDiscardsADwarvenFollowerAndAddsADoubtToDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveCardsToSupportArea(bifur, gwaihir);
		scn.AttachCardsTo(balin, roac, axe);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bifur, gwaihir);
		assertAttachedTo(roac, balin);
		assertAttachedTo(axe, balin);
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		assertTrue(scn.FreepsActionAvailable(gandalf));
		scn.FreepsUseCardAction(gandalf);
		// Both [dwarven] followers, a Dwarf in the support area and a Bird borne by Balin; not the [gandalf]
		// follower nor the [dwarven] possession.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gwaihir, axe));
		scn.FreepsChooseCard(roac);

		assertInZone(Zone.DISCARD, roac);
		assertInZone(Zone.SUPPORT, bifur, gwaihir);
		assertAttachedTo(axe, balin);
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWithNoDwarvenFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveCardsToSupportArea(gwaihir);
		scn.AttachCardsTo(balin, axe);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gwaihir);
		assertAttachedTo(axe, balin);

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gandalf));
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyByDarkSorceryHeIsDiscardedAtMirkwoodAndHisOtherSentencesStillWork() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var thorin = scn.GetFreepsCard("thorin");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCompanionsToTable(gandalf, balin);
		scn.MoveCardsToSupportArea(bifur);
		scn.AttachCardsTo(balin, roac);
		scn.MoveShadowCardsToSupportArea("sorcery");
		scn.MoveCardsToHand(thorin);
		scn.FreepsRefreshDecision();
		assertInZone(Zone.SUPPORT, bifur);
		assertAttachedTo(roac, balin);

		// His third sentence and his Fellowship action are untouched by Dark Sorcery.
		assertEquals(0, scn.GetTwilightCost(thorin));
		assertTrue(scn.FreepsActionAvailable(gandalf));

		scn.FreepsPass();

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertInZone(Zone.DISCARD, gandalf);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
