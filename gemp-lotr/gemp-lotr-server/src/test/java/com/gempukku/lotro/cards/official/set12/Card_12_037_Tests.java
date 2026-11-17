package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_037_Tests
{

	// Site 2 Ettenmoors (Shadow number 2), site 3 Rivendell Valley (0), site 5 The Bridge of Khazad-dum (6).
	// The Ring-bearer is Frodo, Son of Drogo.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("comeaway", "12_37");
					put("comeaway2", "12_37");

					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] Sméagol; to play, add a burden; a skirmish ability only
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] Sméagol; an assignment ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is no Sméagol; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, which is no companion; a skirmish ability only
					put("lord", "1_21");        // Lord of Moria: another Free Peoples condition in the support area; each Dwarf is damage +1

					// Gollum, Nasty Treacherous Creature: a [gollum] minion of another title; a regroup ability only
					put("gollum", "5_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ComeAwayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Come Away
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot Sméagol, the Shadow number of each site is -1.<br>Each time the fellowship moves, wound a companion or discard this condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("comeaway");

		assertEquals("Come Away", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileSmeagolCanBeSpottedEachSiteIsShadowNumberMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comeaway = scn.GetFreepsCard("comeaway");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(comeaway);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, comeaway);
		var ettenmoors = scn.GetFreepsSite(2);
		var shadowEttenmoors = scn.GetShadowSite(2);
		var bridge = scn.GetFreepsSite(5);
		assertEquals(2, scn.GetTwilightCost(ettenmoors));
		assertEquals(2, scn.GetTwilightCost(shadowEttenmoors));
		assertEquals(6, scn.GetTwilightCost(bridge));

		scn.MoveCompanionsToTable(smeagol);

		// Each site, whoever's it is and whether or not it is laid.
		assertEquals(1, scn.GetTwilightCost(ettenmoors));
		assertEquals(1, scn.GetTwilightCost(shadowEttenmoors));
		assertEquals(5, scn.GetTwilightCost(bridge));

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsPass();
		// The wound is chosen, so the condition is still there when the twilight is added.
		scn.FreepsChooseOption("Wound a companion");
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilightCost(scn.GetCurrentSite()));
		// 3 companions (Frodo, Sam, Sméagol) + 1 for Ettenmoors (2 - 1)
		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comeaway = scn.GetFreepsCard("comeaway");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCardsToSupportArea(comeaway);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, comeaway);
		var ettenmoors = scn.GetFreepsSite(2);
		assertEquals(2, scn.GetTwilightCost(ettenmoors));

		// Smeagol, Once Like Us is printed with no accent, and is a Sméagol all the same.
		scn.MoveCompanionsToTable(shiresmeagol);
		assertEquals(1, scn.GetTwilightCost(ettenmoors));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithoutSmeagolTheShadowNumbersArePrintedThoughGollumIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comeaway = scn.GetFreepsCard("comeaway");
		var sam = scn.GetFreepsCard("sam");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(comeaway);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, comeaway);
		var bridge = scn.GetFreepsSite(5);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsPass();
		scn.FreepsChooseOption("Wound a companion");
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetTwilightCost(scn.GetCurrentSite()));
		// 2 companions (Frodo, Sam) + 2 for Ettenmoors
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Gollum, a [gollum] minion, is spotted, and he is no Sméagol.
		scn.MoveMinionsToTable(gollum);
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertEquals(2, scn.GetTwilightCost(scn.GetCurrentSite()));
		assertEquals(6, scn.GetTwilightCost(bridge));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachMoveWoundsACompanionOfThePlayersChoiceWhenThatIsChosenAndAsksAgainOnTheRegroupMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comeaway = scn.GetFreepsCard("comeaway");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(comeaway);
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, comeaway);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsPass();

		// Moving from site 1 to site 2.
		assertEquals(java.util.List.of("Wound a companion", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Wound a companion");
		// The companions of the Free Peoples player, of any culture and race; not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(smeagol);

		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertInZone(Zone.SUPPORT, comeaway);

		// No minions, so the game goes on to the regroup phase, and the fellowship moves again.
		scn.SkipToMovementDecision();
		// Aragorn exhausted (vitality 4): a wound kills him, where an exertion could not be paid.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseToMove();

		assertEquals(java.util.List.of("Wound a companion", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Wound a companion");
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		scn.FreepsChooseCard(aragorn);

		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertInZone(Zone.SUPPORT, comeaway);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ChoosingToDiscardItWoundsNobodyAndLeavesAnotherConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comeaway = scn.GetFreepsCard("comeaway");
		var lord = scn.GetFreepsCard("lord");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToSupportArea(comeaway, lord);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, comeaway, lord);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.FreepsPass();
		assertEquals(java.util.List.of("Wound a companion", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard this condition");

		assertInDiscard(comeaway);
		assertInZone(Zone.SUPPORT, lord);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoCopiesEachAsksOnAMoveAndDiscardingOneLeavesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comeaway = scn.GetFreepsCard("comeaway");
		var comeaway2 = scn.GetFreepsCard("comeaway2");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(comeaway, comeaway2);
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, comeaway, comeaway2);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetFreepsDiscardCount());
		scn.FreepsPass();

		// Each copy puts its own question.
		assertEquals(java.util.List.of("Wound a companion", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Discard this condition");

		// Only the copy that was asked is gone.
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(java.util.List.of("Wound a companion", "Discard this condition"), scn.FreepsGetMultipleChoices());
		scn.FreepsChooseOption("Wound a companion");
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StayingInTheRegroupPhaseAsksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var comeaway = scn.GetFreepsCard("comeaway");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToMovementDecision();
		// Put down after the first move of the turn, so that the regroup decision is the first it sees.
		scn.MoveCardsToSupportArea(comeaway);
		assertInZone(Zone.SUPPORT, comeaway);
		assertEquals(0, scn.GetWoundsOn(sam));

		scn.FreepsChooseToStay();

		assertEquals(0, scn.GetWoundsOn(sam));
		assertInZone(Zone.SUPPORT, comeaway);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
