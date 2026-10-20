package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_024_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rabble", "4_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRabbleScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rabble", "4_24");
					put("rabble2", "4_24");
					// Hides: a [dunland] possession in the support area.  "Response: If a [dunland] Man is about to
					// take a wound, remove (2) or discard this possession to prevent that wound."
					put("hides", "4_19");
					// Anger: [dunland] Skirmish event, "Spot 3 [dunland] minions or a site you control to make a
					// [dunland] Man fierce until the regroup phase."
					put("anger", "4_3");
					// Pass of the Spider: a [gollum] possession in the Shadow support area.  "Skirmish: If Shelob
					// is skirmishing, stack another minion from play here ...": how a Rabble comes to be stacked
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
	public void HillmanRabbleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hillman Rabble
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: Each time this minion wins a skirmish, you may stack it on a site you control.<br><b>Shadow:</b> If stacked on a site you control, play this minion. Its twilight cost is -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rabble");

		assertEquals("Hillman Rabble", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishOffersToStackItOnAChosenSiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");
		var rabble2 = scn.GetShadowCard("rabble2");
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
		scn.MoveMinionsToTable(rabble, rabble2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, rabble);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		assertEquals(0, scn.GetWoundsOn(knight));

		// 10 against 6: the Rabble wins.
		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rabble));
		// "This minion": the other Rabble won nothing and offers nothing.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rabble2));
		scn.ShadowAcceptOptionalTrigger();

		// "A site you control": the Shadow player's two, and not the one the other player controls.
		assertEquals(2, scn.ShadowGetCardChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(site3);
		assertInZone(Zone.STACKED, rabble);
		assertEquals(1, scn.GetStackedCards(site3).size());
		assertTrue(scn.GetStackedCards(site3).contains(rabble));
		assertEquals(0, scn.GetStackedCards(site2).size());
		assertEquals(0, scn.GetStackedCards(site1).size());
		// "It": the other Rabble is still on the table.
		assertInZone(Zone.SHADOW_CHARACTERS, rabble2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningLeavesItInPlayAndStacksNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveMinionsToTable(rabble);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, rabble);
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rabble));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SHADOW_CHARACTERS, rabble);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningASkirmishWhileOnlyTheOtherPlayerControlsASiteOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// Taken by the first player on a turn when he was the Shadow player: a site is controlled, but there is
		// none of the Rabble's owner's to stack it on.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(rabble);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, rabble);
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		assertEquals(1, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rabble));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, rabble);
		assertEquals(0, scn.GetStackedCards(site1).size());
	}

	@Test
	public void LosingASkirmishAndLivingOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");
		var hides = scn.GetShadowCard("hides");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(rabble);
		assertInZone(Zone.SUPPORT, hides);
		// The Knight at 11 beats the Rabble's 10 without overwhelming it (20).
		scn.ApplyAdHocModifier(new StrengthModifier(null, knight, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, rabble);
		scn.FreepsResolveSkirmish(knight);

		// An empty pool leaves discarding Hides as its one cost.
		scn.SetTwilight(0);
		assertEquals(0, scn.GetWoundsOn(rabble));

		scn.PassCurrentPhaseActions();
		// Hides saves the Rabble from the wound for losing, so that it is still in play to answer anything.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rabble));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(hides);
		assertInZone(Zone.SHADOW_CHARACTERS, rabble);
		assertEquals(0, scn.GetWoundsOn(rabble));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rabble));
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DeclinedAtItsFirstWinItIsOfferedAgainAtAWinInTheFierceRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");
		var anger = scn.GetShadowCard("anger");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(anger);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveMinionsToTable(rabble);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, rabble);
		scn.FreepsResolveSkirmish(knight);

		// Anger, spotting a site its player controls, makes the Rabble fierce.
		scn.FreepsPass();
		scn.ShadowPlayCard(anger);
		assertTrue(scn.HasKeyword(rabble, Keyword.FIERCE));

		scn.PassCurrentPhaseActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rabble));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, rabble);

		// "Each time": the fierce skirmish, it wins again and is asked again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight, rabble);
		scn.FreepsResolveSkirmish(knight);
		scn.PassFierceSkirmishActions();
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rabble));
		scn.ShadowAcceptOptionalTrigger();

		// The one site its player controls is taken unasked.
		assertInZone(Zone.STACKED, rabble);
		assertTrue(scn.GetStackedCards(site1).contains(rabble));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void StackedOnASiteYouControlItIsPlayedInTheShadowPhaseForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.StackCardsOn(site1, rabble);
		assertInZone(Zone.STACKED, rabble);

		// 0 set + 1 companion + 0 for Rivendell Valley
		scn.SetTwilight(0);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetTwilight());

		// 4 - 2, and no roaming at site 3: 2.  One short of that it is not offered (a -3 would be).
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(rabble));

		// With 2 it is (a -1 would not be), and 2 is paid (a -3 would leave 1).
		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(rabble));

		scn.ShadowUseCardAction(rabble);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, rabble);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionPlaysEachRabbleFromTheSiteForTwoLessAndLeavesTheOtherStacked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");
		var rabble2 = scn.GetShadowCard("rabble2");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.StackCardsOn(site1, rabble, rabble2);
		assertInZone(Zone.STACKED, rabble, rabble2);

		// 2 set + 1 companion + 0 for Rivendell Valley
		scn.SetTwilight(2);
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(rabble));
		assertTrue(scn.ShadowActionAvailable(rabble2));

		// 4 - 2, and no roaming at site 3
		scn.ShadowUseCardAction(rabble);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, rabble);
		// "This minion": the other Rabble is still on the site.
		assertInZone(Zone.STACKED, rabble2);
		assertEquals(1, scn.GetStackedCards(site1).size());
		assertTrue(scn.GetStackedCards(site1).contains(rabble2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowActionIsNotOfferedFromAShadowPossessionNorInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");
		var rabble2 = scn.GetShadowCard("rabble2");
		var pass = scn.GetShadowCard("pass");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(site1, rabble);
		scn.StackCardsOn(pass, rabble2);
		assertInZone(Zone.STACKED, rabble, rabble2);

		// 9 set + 1 companion + 0 for Rivendell Valley: enough for both
		scn.SetTwilight(9);
		scn.FreepsPass();
		// A minion on the table, so that there is a maneuver phase.
		scn.MoveMinionsToTable(troop);
		scn.ShadowRefreshDecision();
		assertEquals(10, scn.GetTwilight());

		// "If stacked on a site you control": the one on the site, and not the one on the possession.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(rabble));
		assertFalse(scn.ShadowActionAvailable(rabble2));

		// A Shadow action: on the same site with the same pool, not offered in the maneuver phase.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(10, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(rabble));
		assertInZone(Zone.STACKED, rabble, rabble2);
	}

	@Test
	public void ShadowActionIsNotOfferedForARabbleStackedOnASiteTheOtherPlayerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");
		var theirRabble = scn.GetFreepsCard("rabble");

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		// Site 1 is the first player's, taken on a turn when he was the Shadow player, with a Rabble of his own
		// stacked on it; site 2 is the second player's, who is the Shadow player now.
		scn.FreepsTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		scn.StackCardsOn(site1, theirRabble);
		scn.StackCardsOn(site2, rabble);
		assertInZone(Zone.STACKED, rabble, theirRabble);

		// Enough in the pool for either Rabble: 9 set + 1 companion + 3 for Balin's Tomb
		scn.SetTwilight(9);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(13, scn.GetTwilight());

		// "A site you control": the Rabble on the Shadow player's own site is offered; the other player's, on the
		// site that player controls, is not.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(rabble));
		assertFalse(scn.ShadowActionAvailable(theirRabble));
		assertInZone(Zone.STACKED, rabble, theirRabble);
	}

	@Test
	public void PlayedFromHandItCostsFourThoughASiteIsControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRabbleScenario();

		var rabble = scn.GetShadowCard("rabble");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.MoveCardsToHand(rabble);
		assertInHand(rabble);

		// 3 set + 1 companion + 0 for Rivendell Valley
		scn.SetTwilight(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(rabble));

		// 4 to play, no roaming at site 3.  The -2 belongs to the play from a controlled site: a discount that
		// reached the hand would leave 2.
		scn.ShadowPlayCard(rabble);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, rabble);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
