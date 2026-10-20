package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("looter", "4_11");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetStackedScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("looter", "4_11");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetLooterScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("looter", "4_11");
					put("looter2", "4_11");
					// Hides: a [dunland] possession in the support area.  "Response: If a [dunland] Man is about to
					// take a wound, remove (2) or discard this possession to prevent that wound."
					put("hides", "4_19");
					// Anger: [dunland] Skirmish event, "Spot 3 [dunland] minions or a site you control to make a
					// [dunland] Man fierce until the regroup phase."
					put("anger", "4_3");
					// Pass of the Spider: a [gollum] possession in the Shadow support area.  "Skirmish: If Shelob
					// is skirmishing, stack another minion from play here ...": how a Looter comes to be stacked
					// on a Shadow card that is no site.  Its trigger answers a Free Peoples condition's special
					// ability; none is in these tests.
					put("pass", "103_12");
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingLooterStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Dunlending Looter
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: When this minion wins a skirmish, you may stack him on a site you control.<br><b>Shadow:</b> If stacked on a site you control, play this minion. His twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("looter");

		assertEquals("Dunlending Looter", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StackedOnASiteYouControlHeIsPlayedInTheShadowPhaseForTwoLessAndStillPaysRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStackedScenario();

		// Player 2's first turn, so that a fellowship stands at site 2 with both markers off site 1.
		// Player 1 is the Shadow player there, and the Looter is his.
		// The sum is the old at test's (playFromControlledSite).  No game has him stacked this early: he is
		// stacked by winning a skirmish, and his owner's next Shadow phase is at site 3 or later, where he
		// does not roam (Q-0230: keep or strike the roaming half).
		var looter = scn.GetFreepsCard("looter");

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));

		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		scn.StackCardsOn(site1, looter);
		assertTrue(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertInZone(Zone.STACKED, looter);

		// 3 for the Looter + 2 roaming (his site number is 3, the fellowship is at site 2) - 2 by his own
		// text: 3.  One short of that he is not offered.
		scn.SetTwilight(2);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		assertFalse(scn.FreepsActionAvailable(looter));

		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(looter));

		scn.FreepsUseCardAction(looter);
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.IsSiteControlled(site1));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void WinningASkirmishOffersToStackHimOnAChosenSiteHisOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLooterScenario();

		var looter = scn.GetShadowCard("looter");
		var looter2 = scn.GetShadowCard("looter2");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		// Site 1 is the first player's, taken on a turn when he was the Shadow player; sites 2 and 3 are the
		// second player's, who is the Shadow player now.
		scn.FreepsTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));
		scn.MoveMinionsToTable(looter, looter2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, looter);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));

		// 9 against 6: the Looter wins.
		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter));
		// "This minion": the other Looter won nothing and offers nothing.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter2));
		scn.ShadowAcceptOptionalTrigger();

		// "A site you control": the Shadow player's two, and not the one the other player controls.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(site3);
		assertInZone(Zone.STACKED, looter);
		assertEquals(1, scn.GetStackedCards(site3).size());
		assertTrue(scn.GetStackedCards(site3).contains(looter));
		assertEquals(0, scn.GetStackedCards(site2).size());
		assertEquals(0, scn.GetStackedCards(site1).size());
		// "Him": the other Looter is still on the table.
		assertInZone(Zone.SHADOW_CHARACTERS, looter2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesHimInPlayAndStacksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLooterScenario();

		var looter = scn.GetShadowCard("looter");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveMinionsToTable(looter);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, looter);
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningASkirmishWhileOnlyTheOtherPlayerControlsASiteOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLooterScenario();

		var looter = scn.GetShadowCard("looter");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// Taken by the first player on a turn when he was the Shadow player: a site is controlled, but there is
		// none of the Looter's owner's to stack him on.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(looter);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, looter);
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertEquals(0, scn.GetStackedCards(site1).size());
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLooterScenario();

		var looter = scn.GetShadowCard("looter");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(looter);
		assertInZone(Zone.SUPPORT, hides);
		// The Knight at 10 beats the Looter's 9 without overwhelming him (18).
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, looter);
		scn.FreepsResolveSkirmish(knight);

		// An empty pool leaves discarding Hides as its one cost.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(looter));

		scn.PassCurrentPhaseActions();
		// Hides saves the Looter from the wound for losing, so that he is still in play to answer anything.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(hides);
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertEquals(0, scn.GetWoundsOn(looter));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(looter));
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedAtHisFirstWinHeIsOfferedAgainAtAWinInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLooterScenario();

		var looter = scn.GetShadowCard("looter");
		var anger = scn.GetShadowCard("anger");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(anger);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveMinionsToTable(looter);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, looter);
		scn.FreepsResolveSkirmish(knight);

		// Anger, spotting a site its player controls, makes the Looter fierce.
		scn.FreepsPass();
		scn.ShadowPlayCard(anger);
		assertTrue(scn.HasKeyword(looter, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, looter);

		// The fierce skirmish: he wins again, and is asked again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight, looter);
		scn.FreepsResolveSkirmish(knight);
		scn.PassFierceSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(looter));
		scn.ShadowAcceptOptionalTrigger();

		// The one site he controls is taken unasked.
		assertInZone(Zone.STACKED, looter);
		assertTrue(scn.GetStackedCards(site1).contains(looter));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowActionPlaysEachLooterFromTheSiteForTwoLessAndLeavesTheOtherStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLooterScenario();

		var looter = scn.GetShadowCard("looter");
		var looter2 = scn.GetShadowCard("looter2");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.StackCardsOn(site1, looter, looter2);
		assertInZone(Zone.STACKED, looter, looter2);

		// 2 set + 1 companion + 0 for Rivendell Valley
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(looter));
		assertTrue(scn.ShadowActionAvailable(looter2));

		// 3 - 2, and no roaming at site 3
		scn.ShadowUseCardAction(looter);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		// "This minion": the other Looter is still on the site.
		assertInZone(Zone.STACKED, looter2);
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertTrue(scn.GetStackedCards(site1).contains(looter2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedFromAShadowPossessionNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLooterScenario();

		var looter = scn.GetShadowCard("looter");
		var looter2 = scn.GetShadowCard("looter2");
		var pass = scn.GetShadowCard("pass");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(site1, looter);
		scn.StackCardsOn(pass, looter2);
		assertInZone(Zone.STACKED, looter, looter2);

		// 9 set + 1 companion + 0 for Rivendell Valley: enough for both
		scn.SetTwilight(9);
		scn.FreepsPass();
		// A minion on the table, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertEquals(10, scn.GetTwilight());

		// "If stacked on a site you control": the one on the site, and not the one on the possession.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(looter));
		assertFalse(scn.ShadowActionAvailable(looter2));

		// A Shadow action: on the same site with the same pool, not offered in the maneuver phase.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(looter));
		assertInZone(Zone.STACKED, looter, looter2);
	}

	@Test
	public void PlayedFromHandHeCostsThreeThoughASiteIsControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLooterScenario();

		var looter = scn.GetShadowCard("looter");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToHand(looter);
		assertInHand(looter);

		// 2 set + 1 companion + 0 for Rivendell Valley
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(looter));

		// 3 to play, no roaming at site 3.  The -2 belongs to the play from a controlled site: a discount that
		// reached the hand would leave 2.
		scn.ShadowPlayCard(looter);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, looter);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
