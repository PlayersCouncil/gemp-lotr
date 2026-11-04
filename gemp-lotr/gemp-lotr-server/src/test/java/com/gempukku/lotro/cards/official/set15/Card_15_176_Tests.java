package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_176_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("assassin", "15_176");
					put("assassin2", "15_176");

					// Saruman, Agent of the Dark Lord: an [uruk-hai] minion who is no Uruk-hai (a Wizard).  His skirmish
					// ability and his heal on a companion's death are never used.
					put("saruman", "12_144");
					// Tyrannical Uruk: an [uruk-hai] Uruk-hai; damage +1 and toil 1 (a play matter) only.
					put("tyrannical", "11_204");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 only.
					put("guard", "10_81");
					// Poised for Assault and Weapons of Control: [uruk-hai] support-area conditions that hold
					// [uruk-hai] tokens.  Neither's skirmish ability is used, and no companion loses a skirmish.
					put("poised", "15_166");
					put("weapons", "13_177");
					// Always Threatening: an [orc] support-area condition that holds [orc] tokens.  Its ability is
					// never used.
					put("threatening", "13_103");

					// Rohan Worker: a Free Peoples follower with no printed bonus, Aid (1).  Its Aid is declined; its
					// other texts answer a transfer and a skirmish win, neither of which happens.
					put("worker1", "15_135");
					put("worker2", "15_135");
					// Saruman, Servant of Sauron: a Shadow follower, Aid (1) (declined); a skirmish-win trigger only.
					put("sarumanfollower", "17_38");
					// Aragorn, Ranger of the North, with Ranger's Sword: a Free Peoples possession that is no follower.
					put("aragorn", "1_89");
					put("sword", "1_112");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukVillageAssassinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uruk Village Assassin
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>At the start of the maneuver phase, you may spot another [uruk-hai] minion and remove 3 [uruk-hai] tokens to take control of a site or discard a follower.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("assassin");

		assertEquals("Uruk Village Assassin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StartOfManeuverRemoves3TokensFromTwoCardsToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker1 = scn.GetFreepsCard("worker1");
		scn.MoveCardsToSupportArea(worker1);
		assertInZone(Zone.SUPPORT, worker1);

		var assassin = scn.GetShadowCard("assassin");
		var saruman = scn.GetShadowCard("saruman");
		var poised = scn.GetShadowCard("poised");
		var weapons = scn.GetShadowCard("weapons");

		scn.StartGame();
		// Player 1's second turn: both markers on site 2, and site 1 behind them.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(poised, weapons);
		scn.AddTokensToCard(poised, 2);
		scn.AddTokensToCard(weapons, 2);
		scn.MoveMinionsToTable(assassin, saruman);
		assertEquals(2, scn.GetCultureTokensOn(poised));
		assertEquals(2, scn.GetCultureTokensOn(weapons));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertFalse(scn.ShadowControls(site1));
		// The other [uruk-hai] minion is a Wizard.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowChooseOptionalTrigger(assassin);

		// The tokens may come from more than one card.
		scn.ShadowChooseCard(poised);
		scn.ShadowChooseCard(poised);
		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertEquals(1, scn.GetCultureTokensOn(weapons));

		// A site can be taken and a follower discarded: he is asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Take control");

		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, worker1);
		// The follower's Aid is put to the Free Peoples player again.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker1));
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheOtherBranchDiscardsAFollowerOfEitherSideAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var worker1 = scn.GetFreepsCard("worker1");
		var worker2 = scn.GetFreepsCard("worker2");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(worker2);

		var assassin = scn.GetShadowCard("assassin");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var poised = scn.GetShadowCard("poised");
		var sarumanfollower = scn.GetShadowCard("sarumanfollower");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// A follower borne after the skip, which would have sent it back to the support area.
		scn.AttachCardsTo(aragorn, worker1);
		scn.MoveCardsToSupportArea(poised, sarumanfollower);
		scn.AddTokensToCard(poised, 3);
		scn.MoveMinionsToTable(assassin, tyrannical);
		assertAttachedTo(worker1, aragorn);
		assertAttachedTo(sword, aragorn);
		assertInZone(Zone.SUPPORT, worker2, sarumanfollower);
		assertEquals(3, scn.GetCultureTokensOn(poised));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.ShadowChooseOptionalTrigger(assassin);
		assertEquals(0, scn.GetCultureTokensOn(poised));
		scn.ShadowChooseOption("Discard a follower");

		assertTrue(scn.ShadowHasCardChoiceAvailable(worker1, worker2, sarumanfollower));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worker1);

		assertInZone(Zone.DISCARD, worker1);
		assertInZone(Zone.SUPPORT, worker2, sarumanfollower);
		assertAttachedTo(sword, aragorn);
		assertFalse(scn.ShadowControls(site1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		// The Aid of the follower left is put to the Free Peoples player again.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(worker2));
		scn.FreepsDeclineOptionalTrigger();
		// And the Shadow follower's Aid to the Shadow player.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(sarumanfollower));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoSiteToTakeTheFollowerIsDiscardedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker1 = scn.GetFreepsCard("worker1");
		scn.MoveCardsToSupportArea(worker1);

		var assassin = scn.GetShadowCard("assassin");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var poised = scn.GetShadowCard("poised");
		scn.MoveCardsToSupportArea(poised);

		scn.StartGame();
		scn.AddTokensToCard(poised, 3);
		scn.MoveMinionsToTable(assassin, tyrannical);
		assertEquals(3, scn.GetCultureTokensOn(poised));
		assertInZone(Zone.SUPPORT, worker1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		// Turn 1: the Shadow player's marker is on site 1, so no site can be taken.
		assertEquals(1, scn.ShadowGetSiteNumber());
		scn.ShadowChooseOptionalTrigger(assassin);

		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertInZone(Zone.DISCARD, worker1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoFollowerTheSiteIsTakenWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var poised = scn.GetShadowCard("poised");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(poised);
		scn.AddTokensToCard(poised, 3);
		scn.MoveMinionsToTable(assassin, tyrannical);
		assertEquals(3, scn.GetCultureTokensOn(poised));

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowControls(site1));
		scn.ShadowChooseOptionalTrigger(assassin);

		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNeitherASiteNorAFollowerItIsOfferedAndTheTokensArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var poised = scn.GetShadowCard("poised");
		scn.MoveCardsToSupportArea(poised);

		scn.StartGame();
		scn.AddTokensToCard(poised, 3);
		scn.MoveMinionsToTable(assassin, tyrannical);
		assertEquals(3, scn.GetCultureTokensOn(poised));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowChooseOptionalTrigger(assassin);

		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithNoOtherUrukHaiMinionBesideASauronUrukHaiAndAnUrukHaiCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var guard = scn.GetShadowCard("guard");
		var poised = scn.GetShadowCard("poised");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(poised);
		scn.AddTokensToCard(poised, 3);
		scn.MoveMinionsToTable(assassin, guard);
		assertEquals(3, scn.GetCultureTokensOn(poised));

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(3, scn.GetCultureTokensOn(poised));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ASecondAssassinIsAnotherUrukHaiMinionAndEachCopyMakesItsOwnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var assassin2 = scn.GetShadowCard("assassin2");
		var poised = scn.GetShadowCard("poised");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(poised);
		scn.AddTokensToCard(poised, 3);
		scn.MoveMinionsToTable(assassin, assassin2);
		assertEquals(3, scn.GetCultureTokensOn(poised));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin2));
		scn.ShadowChooseOptionalTrigger(assassin2);

		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertTrue(scn.ShadowControls(site1));
		// With the tokens gone the first copy's offer cannot be paid for.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithOnly2UrukHaiTokensBesideTokensOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var poised = scn.GetShadowCard("poised");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(poised, threatening);
		scn.AddTokensToCard(poised, 2);
		scn.AddTokensToCard(threatening, 3);
		scn.MoveMinionsToTable(assassin, tyrannical);
		assertEquals(2, scn.GetCultureTokensOn(poised));
		assertEquals(3, scn.GetCultureTokensOn(threatening));

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(2, scn.GetCultureTokensOn(poised));
		assertEquals(3, scn.GetCultureTokensOn(threatening));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedItLeavesEverythingAsItWasAndIsNotOfferedAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var assassin = scn.GetShadowCard("assassin");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var poised = scn.GetShadowCard("poised");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(poised);
		scn.AddTokensToCard(poised, 6);
		scn.MoveMinionsToTable(assassin, tyrannical);
		assertEquals(6, scn.GetCultureTokensOn(poised));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(6, scn.GetCultureTokensOn(poised));
		assertFalse(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The neighbouring moment: the start of the archery phase.
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(assassin));
		assertEquals(6, scn.GetCultureTokensOn(poised));
	}

	@Test
	public void OfferedAgainAtTheStartOfASecondManeuverPhaseInTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var worker1 = scn.GetFreepsCard("worker1");
		scn.MoveCardsToSupportArea(worker1);

		var assassin = scn.GetShadowCard("assassin");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var poised = scn.GetShadowCard("poised");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToSupportArea(poised);
		scn.AddTokensToCard(poised, 6);
		scn.MoveMinionsToTable(assassin, tyrannical);
		assertEquals(6, scn.GetCultureTokensOn(poised));
		assertInZone(Zone.SUPPORT, worker1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.ShadowChooseOptionalTrigger(assassin);
		scn.ShadowChooseOption("Take control");
		assertEquals(3, scn.GetCultureTokensOn(poised));
		assertTrue(scn.ShadowControls(site1));

		// A second move: the minions stay in play, and a second maneuver phase starts at site 4.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(4, scn.FreepsGetSiteNumber());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(assassin));
		scn.ShadowChooseOptionalTrigger(assassin);

		// No other site is behind every marker now: the follower goes, unasked.
		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertInZone(Zone.DISCARD, worker1);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
