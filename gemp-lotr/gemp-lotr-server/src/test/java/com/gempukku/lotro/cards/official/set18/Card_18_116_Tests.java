package com.gempukku.lotro.cards.official.set18;

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

public class Card_18_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "18_116");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetChoiceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "18_116");
					put("aggressor", "13_168"); // Uruk Aggressor: [uruk-hai] minion, strength 8, vitality 2, damage +1; its text is about its cost
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
					put("worker", "15_135");    // Rohan Worker: a follower; its texts answer a transfer and its bearer's win
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBranchScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "18_116");
					put("aggressor", "13_168"); // Uruk Aggressor: [uruk-hai] Uruk-hai, strength 8, vitality 2, damage +1; its text is about its cost
					put("aggressor2", "13_168");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is no Uruk-hai (a Wizard), strength 8,
					// vitality 4; each companion is resistance -1, and a skirmish ability never used here
					put("saruman", "17_116");
					put("cuguard", "10_81");    // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; Damage +1 is its whole text
					// White Hand Captain: [uruk-hai] Uruk-hai, fierce, strength 12, vitality 3; each hunter character is
					// strength +1 while it is assigned, and nobody here is a hunter
					put("captain", "17_123");

					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("sword", "1_299");      // Hobbit Sword: a Free Peoples possession, on Frodo
					// Followers. Rohan Worker: its texts answer a transfer and its bearer's win (cheated on, and its bearer
					// loses). Rohan Stable Master: its text makes its bearer mounted. Saruman, Servant of Sauron: a
					// Shadow follower, in the Shadow support area; its text answers its bearer's win.
					put("worker", "15_135");
					put("stablemaster", "15_134");
					put("servant", "17_38");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FuryOfTheEvilArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Fury of the Evil Army
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: 
		 * Strength: 1
		 * Game Text: Bearer must be an [uruk-hai] minion.<br>Each time bearer wins a skirmish, you may choose one: discard a follower from play; heal an [uruk-hai] minion; or control a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury");

		assertEquals("Fury of the Evil Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void WithNoFollowerNoWoundedUrukHaiMinionAndNoSiteThatCanBeControlledBearerWinningASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2.  Site 1 is the only site every marker has passed,
		// and player 1 took it on player 2's turn.
		scn.SkipToSite(2);
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertTrue(scn.FreepsControls(site1));

		scn.PassCurrentPhaseActions();
		// 8 + 1 for the Fury against 6: the Knight loses, and takes 2 wounds for damage +1.
		assertEquals(2, scn.GetWoundsOn(knight));
		// None of the three can be done, so there is nothing a yes could choose (Q-0034).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fury));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void WithOnlyASiteThatCanBeControlledBearerWinningASkirmishOffersTheChoiceAndAYesTakesTheSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertFalse(scn.IsSiteControlled(site1));

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();

		// The one thing of the three that can be done is done, and which is not asked.
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyAWoundedUrukHaiMinionBearerWinningASkirmishOffersTheChoiceAndAYesHealsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");

		scn.StartGame();
		scn.SkipToSite(2);
		// No site can be controlled: player 1 holds the only one every marker has passed.
		scn.FreepsTakeControlOfSite();
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);
		scn.AddWoundsToChar(aggressor, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(1, scn.GetWoundsOn(aggressor));

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyAFollowerInPlayBearerWinningASkirmishOffersTheChoiceAndAYesDiscardsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");
		var worker = scn.GetFreepsCard("worker");

		scn.StartGame();
		scn.SkipToSite(2);
		// No site can be controlled: player 1 holds the only one every marker has passed.
		scn.FreepsTakeControlOfSite();
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);

		scn.SkipToAssignments();
		// Laid after the maneuver phase, where its Aid would have been offered.
		scn.MoveCardsToSupportArea(worker);
		assertInZone(Zone.SUPPORT, worker);
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		assertEquals(0, scn.GetWoundsOn(aggressor));
		assertInZone(Zone.SUPPORT, worker);

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();

		assertInDiscard(worker);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerMustBeAnUrukHaiCultureMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBranchScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var saruman = scn.GetShadowCard("saruman");
		var cuguard = scn.GetShadowCard("cuguard");
		scn.MoveCardsToHand(fury);
		assertInZone(Zone.HAND, fury);

		scn.StartGame();
		scn.MoveMinionsToTable(aggressor, saruman, cuguard);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fury));
		scn.ShadowPlayCard(fury);
		// An Uruk-hai and a Wizard of the [uruk-hai] culture; not an Uruk-hai of the [sauron] culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cuguard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(saruman);

		assertAttachedTo(fury, saruman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithEveryBranchOpenTheChoiceIsAskedAndAHealHealsTheUrukHaiCultureMinionChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBranchScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var saruman = scn.GetShadowCard("saruman");
		var cuguard = scn.GetShadowCard("cuguard");
		var knight = scn.GetFreepsCard("knight");
		var worker = scn.GetFreepsCard("worker");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 can be controlled.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor, saruman, cuguard);
		scn.AttachCardsTo(aggressor, fury);
		scn.AddWoundsToChar(aggressor, 1);
		scn.AddWoundsToChar(saruman, 2);
		scn.AddWoundsToChar(cuguard, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(cuguard));

		scn.SkipToAssignments();
		// Laid after the maneuver phase, where its Aid would have been offered.
		scn.MoveCardsToSupportArea(worker);
		assertInZone(Zone.SUPPORT, worker);
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		assertFalse(scn.IsSiteControlled(site1));

		scn.PassCurrentPhaseActions();
		// 8 + 1 for the Fury against 6.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();

		// A follower, a wounded [uruk-hai] minion and a site: all three are asked.
		assertEquals(3, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Discard a follower"));
		assertTrue(scn.ShadowChoiceAvailable("Heal"));
		assertTrue(scn.ShadowChoiceAvailable("Control a site"));
		scn.ShadowChoose("Heal");

		// The bearer and Saruman, a Wizard; not the wounded Uruk-hai of the [sauron] culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aggressor, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cuguard));
		scn.ShadowChooseCard(saruman);

		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertEquals(1, scn.GetWoundsOn(cuguard));
		assertInZone(Zone.SUPPORT, worker);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFollowerOfEitherSideMayBeDiscardedFromPlayAndNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBranchScenario();

		var frodo = scn.GetRingBearer();
		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var servant = scn.GetShadowCard("servant");
		var knight = scn.GetFreepsCard("knight");
		var sword = scn.GetFreepsCard("sword");
		var worker = scn.GetFreepsCard("worker");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.AttachCardsTo(frodo, sword);
		assertAttachedTo(sword, frodo);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);

		scn.SkipToAssignments();
		// Laid after the maneuver phase, where their Aids would have been offered: one borne by a companion, one in
		// each player's support area.
		scn.AttachCardsTo(knight, worker);
		scn.MoveCardsToSupportArea(stablemaster, servant);
		assertAttachedTo(worker, knight);
		assertInZone(Zone.SUPPORT, stablemaster, servant);
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);

		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(knight));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("Discard a follower");

		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, stablemaster, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, fury, knight));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worker);

		assertInDiscard(worker);
		assertInZone(Zone.SUPPORT, stablemaster, servant);
		assertAttachedTo(sword, frodo);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBranchScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var knight = scn.GetFreepsCard("knight");
		var worker = scn.GetFreepsCard("worker");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight);
		scn.MoveMinionsToTable(aggressor);
		scn.AttachCardsTo(aggressor, fury);
		scn.AddWoundsToChar(aggressor, 1);
		assertEquals(1, scn.GetWoundsOn(aggressor));

		scn.SkipToAssignments();
		scn.MoveCardsToSupportArea(worker);
		assertInZone(Zone.SUPPORT, worker);
		scn.FreepsAssignToMinions(knight, aggressor);
		scn.FreepsResolveSkirmish(knight);
		assertFalse(scn.IsSiteControlled(site1));

		scn.PassCurrentPhaseActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, worker);
		assertEquals(1, scn.GetWoundsOn(aggressor));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnotherUrukHaiCultureMinionWinsNorWhenBearerLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBranchScenario();

		var fury = scn.GetShadowCard("fury");
		var aggressor = scn.GetShadowCard("aggressor");
		var saruman = scn.GetShadowCard("saruman");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.StartGame();
		// Site 1 can be controlled throughout.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.MoveMinionsToTable(aggressor, saruman);
		scn.AttachCardsTo(saruman, fury);
		assertAttachedTo(fury, saruman);
		// Aragorn at 8 + 2 beats Saruman at 8 + 1, who lives through the one wound with his 4 vitality.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, aggressor}, new PhysicalCardImpl[]{aragorn, saruman});
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		// The Uruk Aggressor, which does not bear it, wins.
		assertEquals(2, scn.GetWoundsOn(knight));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fury));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(saruman));
		scn.PassCurrentPhaseActions();

		// The bearer loses, and lives.
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fury));
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsTheOfferIsMadeAgainInTheFierceSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBranchScenario();

		var fury = scn.GetShadowCard("fury");
		var captain = scn.GetShadowCard("captain");
		var knight = scn.GetFreepsCard("knight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var worker = scn.GetFreepsCard("worker");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight, aragorn);
		scn.MoveMinionsToTable(captain);
		scn.AttachCardsTo(captain, fury);
		assertAttachedTo(fury, captain);

		scn.SkipToAssignments();
		scn.MoveCardsToSupportArea(worker);
		assertInZone(Zone.SUPPORT, worker);
		scn.FreepsAssignToMinions(aragorn, captain);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();

		// 12 + 1 against 8: the Captain wins, and a follower and a site are there to be taken.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("Control a site");
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, worker);

		// The fierce skirmish: the Captain wins again, against the Knight.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(knight, captain);
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fury));
		scn.ShadowAcceptOptionalTrigger();
		// No site is left to control, so the follower goes with nothing asked.
		assertInDiscard(worker);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
