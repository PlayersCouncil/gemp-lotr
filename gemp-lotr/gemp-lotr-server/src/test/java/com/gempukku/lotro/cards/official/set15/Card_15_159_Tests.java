package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_159_Tests
{

	// The Fellowship-block path: each player holds one site of each number, so a card that plays "the next site"
	// lays the player's own site of that number, replacing the other player's where it lies.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "15_159");
					put("tracking", "15_171");    // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3; Hunter 1 is its whole text
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard, vitality 4; each companion
					// is resistance -1, and a skirmish ability never used
					put("saruman", "17_116");
					put("guard", "10_81");        // Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2; Damage +1 is its whole text
					put("nelya", "1_233");        // Úlairë Nelya, Lieutenant of Morgul: Shadow: exert her and spot an opponent's site to replace it with your site of the same number

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a ranger, strength 8, vitality 4; a maneuver ability never used
					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability never used
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard; his text is about his strength
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup: spot a ranger to play the fellowship's next site
					put("leader", "12_34");       // Traveled Leader: Maneuver or Regroup: spot a [gandalf] Wizard to replace a site in the fellowship's current region
					put("leader2", "12_34");
					put("leader3", "12_34");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Mere of Dead Faces 11_246 ("Regroup: Exert your Gollum or your Sméagol to play the fellowship's next site")
	// stands in each adventure deck in place of site 5, and the test puts it on the path as the site the fellowship
	// moves to from site 4.
	protected VirtualTableScenario GetMereScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rush", "15_159");
					put("tracking", "15_171");    // Tracking Uruk: [uruk-hai] Uruk-hai; Hunter 1 is its whole text
					put("smeagol", "5_29");       // Sméagol, Slinker: pays the site's exertion; a skirmish ability never used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site5", "11_246");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DefensiveRushStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Defensive Rush
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot an [uruk-hai] minion, the Free Peoples player cannot replace a site.
		 * 	At the start of each skirmish, you may remove (3) to heal an [uruk-hai] minion (or heal an [uruk-hai] minion twice if you control a site).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rush");

		assertEquals("Defensive Rush", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileAnUrukHaiMinionCanBeSpottedTheNextSiteIsNotReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder);
		// The other player's Rivendell Valley lies at site 3, as it does where he has played his next site himself.
		// The minion is laid in the fellowship phase to be spotted there (Q-0228).
		scn.MoveCardToAdventurePath(shadowSite3);
		scn.MoveCardsToSupportArea(rush);
		scn.MoveMinionsToTable(tracking);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertInZone(Zone.SUPPORT, rush);
		assertInZone(Zone.SHADOW_CHARACTERS, tracking);
		assertTrue(scn.FreepsPlayAvailable(pathfinder));

		scn.FreepsPlayCard(pathfinder);

		// The event is played, and the other player's site stays.
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertInZone(Zone.DISCARD, pathfinder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoUrukHaiMinionToSpotTheNextSiteIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rush = scn.GetShadowCard("rush");
		var guard = scn.GetShadowCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder);
		scn.MoveCardToAdventurePath(shadowSite3);
		scn.MoveCardsToSupportArea(rush);
		// An Uruk-hai minion who is not [uruk-hai]; Defensive Rush itself is an [uruk-hai] card that is no minion.
		scn.MoveMinionsToTable(guard);
		scn.FreepsRefreshDecision();

		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.SUPPORT, rush);
		assertInZone(Zone.SHADOW_CHARACTERS, guard);

		scn.FreepsPlayCard(pathfinder);

		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASiteInTheNextRegionIsNotReplacedEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rush = scn.GetShadowCard("rush");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(pathfinder);
		// The fellowship is at site 3, in region 1; the other player's Balin's Tomb lies at site 4, in region 2.
		// Saruman is an [uruk-hai] minion who is no Uruk-hai.
		scn.MoveCardToAdventurePath(shadowSite4);
		scn.MoveCardsToSupportArea(rush);
		scn.MoveMinionsToTable(saruman);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman);

		scn.FreepsPlayCard(pathfinder);

		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, site4);
		assertInZone(Zone.DISCARD, pathfinder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheCurrentSiteAndASiteBehindItAreNotReplacedUntilTheMinionIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var gandalf = scn.GetFreepsCard("gandalf");
		var leader = scn.GetFreepsCard("leader");
		var leader2 = scn.GetFreepsCard("leader2");
		var leader3 = scn.GetFreepsCard("leader3");
		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		var shadowSite2 = scn.GetShadowSite(2);
		var shadowSite3 = scn.GetShadowSite(3);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(leader, leader2, leader3);
		// No minion is played, so the turn goes from the move to site 3 straight to the regroup phase.  Sites 2
		// and 3 were laid by the moves, and are the other player's; the Uruk came through the turn.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(rush);
		scn.MoveMinionsToTable(tracking);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite2, scn.GetSite(2));
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site2, site3);
		assertTrue(scn.FreepsPlayAvailable(leader));

		// The event still asks for a site of the region; the site behind the fellowship is not replaced.
		scn.FreepsPlayCard(leader);
		scn.FreepsChooseCard(shadowSite2);
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, site2);
		assertInZone(Zone.DISCARD, leader);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// Nor is the fellowship's own site.
		scn.ShadowPass();
		scn.FreepsPlayCard(leader2);
		scn.FreepsChooseCard(shadowSite3);
		assertSame(shadowSite3, scn.GetSite(3));
		assertSame(shadowSite3, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertInZone(Zone.DISCARD, leader2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// With the Uruk gone, the same event replaces a site.
		scn.MoveCardsToDiscard(tracking);
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPlayCard(leader3);
		scn.FreepsChooseCard(shadowSite2);

		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertSame(shadowSite3, scn.GetSite(3));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ASiteIsNotReplacedByASitesOwnAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMereScenario();

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var smeagol = scn.GetFreepsCard("smeagol");
		var mere = scn.GetShadowSite("site5");
		var site6 = scn.GetFreepsSite(6);
		var shadowSite6 = scn.GetShadowSite(6);
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardToAdventurePath(mere);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// At Mere of Dead Faces, with the other player's Galadriel's Glade lying at site 6.
		scn.MoveCardToAdventurePath(shadowSite6);
		scn.MoveCardsToSupportArea(rush);
		scn.MoveMinionsToTable(tracking);
		scn.FreepsRefreshDecision();

		assertSame(mere, scn.GetCurrentSite());
		assertSame(shadowSite6, scn.GetSite(6));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsActionAvailable(mere));

		scn.FreepsUseCardAction(mere);

		// Sméagol has exerted, and nothing is replaced.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertSame(shadowSite6, scn.GetSite(6));
		assertInZone(Zone.ADVENTURE_DECK, site6);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// With the Uruk gone, the site's ability replaces it.
		scn.MoveCardsToDiscard(tracking);
		scn.ShadowPass();
		scn.FreepsUseCardAction(mere);

		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertSame(site6, scn.GetSite(6));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite6);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayStillReplaceASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var nelya = scn.GetShadowCard("nelya");
		var site1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(rush);
		scn.MoveMinionsToTable(tracking, nelya);
		scn.ShadowRefreshDecision();

		// The fellowship is at site 2, and site 1 behind it is the Free Peoples player's own.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(site1, scn.GetSite(1));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, site1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfASkirmishRemovesThreeToHealAnUrukHaiMinionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, saruman, guard);
		scn.AddWoundsToChar(tracking, 2);
		scn.AddWoundsToChar(saruman, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 1);
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowAcceptOptionalTrigger();

		// The (3) is removed.  A wounded [uruk-hai] minion, in the skirmish or not, of either race; not the [sauron]
		// Uruk-hai, and not the companion.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracking, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(tracking);

		// One wound healed of two: no site is controlled.
		assertEquals(1, scn.GetWoundsOn(tracking));
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoWoundedUrukHaiMinionTheThreeAreRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, guard);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowAcceptOptionalTrigger();

		// Only the [sauron] Uruk-hai is wounded: the cost is paid and nobody is healed.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItIsNotOfferedAtTheStartOfTheArcheryOrAssignmentPhaseButAtTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.AddWoundsToChar(tracking, 1);
		assertEquals(1, scn.GetWoundsOn(tracking));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetWoundsOn(tracking));
		assertEquals(3, scn.GetTwilight());

		// The archery phase starts, and the Free Peoples player is asked first: no offer was made.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(tracking));

		// Nor at the start of the assignment phase.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(tracking));

		// At the start of the skirmish it is.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoTwilightInThePoolItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.AddWoundsToChar(tracking, 1);
		assertEquals(1, scn.GetWoundsOn(tracking));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.SetTwilight(2);

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(tracking));

		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(rush));
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheOfferRemovesNothingAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking);
		scn.AddWoundsToChar(tracking, 1);
		assertEquals(1, scn.GetWoundsOn(tracking));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(tracking));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ItIsOfferedAgainAtTheStartOfTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.MoveMinionsToTable(tracking, guard);
		scn.AddWoundsToChar(tracking, 2);
		assertEquals(2, scn.GetWoundsOn(tracking));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, tracking}, new PhysicalCardImpl[]{gimli, guard});
		scn.SetTwilight(6);

		assertEquals(6, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(tracking));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(tracking));

		// Tracking Uruk (14 + 1) beats Aragorn, who takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The second skirmish starts: offered again, and the Uruk who fought already is healed.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhileTheShadowPlayerControlsASiteTheMinionIsHealedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var saruman = scn.GetShadowCard("saruman");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(tracking, saruman, guard);
		scn.AddWoundsToChar(tracking, 2);
		scn.AddWoundsToChar(saruman, 2);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowAcceptOptionalTrigger();

		// No question of once or twice: the parenthesis is taken.  The [sauron] Uruk-hai is still no candidate.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracking, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, aragorn));
		scn.ShadowChooseCard(tracking);

		assertEquals(0, scn.GetWoundsOn(tracking));
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ASiteTheFreePeoplesPlayerControlsLeavesItAtOneHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsTakeControlOfSite();
		assertEquals(1, scn.GetFreepsControlledSiteCount());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(tracking);
		scn.AddWoundsToChar(tracking, 2);
		assertEquals(2, scn.GetWoundsOn(tracking));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(tracking));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhileTheShadowPlayerControlsASiteAMinionWithOneWoundIsOfferedBesideOneWithTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rush = scn.GetShadowCard("rush");
		var tracking = scn.GetShadowCard("tracking");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToSupportArea(rush);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(tracking, saruman);
		scn.AddWoundsToChar(tracking, 2);
		scn.AddWoundsToChar(saruman, 1);
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(1, scn.GetWoundsOn(saruman));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tracking);
		scn.ShadowDeclineAssignments();
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertEquals(1, scn.GetWoundsOn(saruman));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(rush));
		scn.ShadowAcceptOptionalTrigger();

		// Ketura (Q-0402: A): "there's nothing wrong with choosing to heal an uruk twice that only has one wound."
		// The parenthesis has a condition (a site controlled), so every [uruk-hai] minion is a candidate of the
		// twice-heal, unlike New Chapter's "(or heal Sam twice)" (Q-0392).  Saruman, on one wound, is taken.
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(tracking, saruman));
		scn.ShadowChooseCard(saruman);

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(2, scn.GetWoundsOn(tracking));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
