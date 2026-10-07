package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_129_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, who bears The Ruling Ring. He is kept out of every skirmish.
	// The format is the rig's default, multipath: a Decipher format, in which the game at site 9 is over as the
	// last skirmish ends and no regroup phase starts there.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, damage +1
					put("sting", "1_313");       // Sting: [shire] possession, bearer must be Frodo; a fellowship or regroup ability only
					put("armor", "1_8");         // Dwarven Armor: [dwarven] possession on a Dwarf; changes no number
					put("bilbo", "1_284");       // Bilbo, Retired Adventurer: [shire] ally; a fellowship ability only
					put("promise", "2_112");     // A Promise: [shire] condition in the support area; needs Sam to do anything
					put("toby", "1_305");        // Old Toby: [shire] possession in the support area; its text is for when it is played

					put("runner1", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; cheated in, so its text does nothing
					put("runner2", "1_178");
					put("scimitar", "1_180");    // Goblin Scimitar: [moria] possession on a [moria] Orc, strength +2
					put("pinned", "1_192");      // Pinned Down: [moria] condition in the support area; needs a [moria] archer
					put("poison", "8_28");       // Spider Poison: [gollum] possession a companion bears; bearer exerts when assigned
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");      // Watchers of Cirith Ungol: a skirmish ability for a [wraith] Orc, never used
					put("site9", "103_129");     // Pinnacle of Doom
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same table in PC-Movie, a Players' Council format: the game at site 9 ends only when the Shadow player
	// is about to reconcile, so a regroup phase starts there. Each player has a Map in his support area.
	protected VirtualTableScenario GetPCMovieScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, damage +1
					put("sting", "1_313");       // Sting: [shire] possession, bearer must be Frodo; a fellowship or regroup ability only
					put("armor", "1_8");         // Dwarven Armor: [dwarven] possession on a Dwarf; changes no number
					put("bilbo", "1_284");       // Bilbo, Retired Adventurer: [shire] ally; a fellowship ability only
					put("promise", "2_112");     // A Promise: [shire] condition in the support area; needs Sam to do anything
					put("toby", "1_305");        // Old Toby: [shire] possession in the support area; its text is for when it is played

					put("runner1", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; cheated in, so its text does nothing
					put("runner2", "1_178");
					put("scimitar", "1_180");    // Goblin Scimitar: [moria] possession on a [moria] Orc, strength +2
					put("pinned", "1_192");      // Pinned Down: [moria] condition in the support area; needs a [moria] archer
					put("poison", "8_28");       // Spider Poison: [gollum] possession a companion bears; bearer exerts when assigned
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");      // Watchers of Cirith Ungol: a skirmish ability for a [wraith] Orc, never used
					put("site9", "103_129");     // Pinnacle of Doom
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.PCMovie
		);
	}

	@Test
	public void PinnacleofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pinnacle of Doom
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Mountain</b>. At the start of each phase, the Shadow player may remove (1) to hinder a card
		 * 	(except companions or The One Ring).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Pinnacle of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
	}

	@Test
	public void AtTheStartOfTheShadowPhaseTheShadowPlayerRemovesOneToHinderAnyCardButACompanionOrTheOneRing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sting = scn.GetFreepsCard("sting");
		var armor = scn.GetFreepsCard("armor");
		var bilbo = scn.GetFreepsCard("bilbo");
		var promise = scn.GetFreepsCard("promise");
		var toby = scn.GetFreepsCard("toby");
		var runner = scn.GetShadowCard("runner1");
		var scimitar = scn.GetShadowCard("scimitar");
		var pinned = scn.GetShadowCard("pinned");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.SkipToSite(8);
		// Free Peoples cards: a possession on the Ring-bearer beside the Ring, a possession on another companion,
		// an ally, a condition and a possession in the support area.
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(promise, toby);
		// The Shadow player's own cards: a minion, a possession it bears, a condition in his support area, and a
		// possession borne by a companion.
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, scimitar);
		scn.MoveCardsToSupportArea(pinned);
		scn.AttachCardsTo(aragorn, poison);
		// A card that is hindered already is not offered for hindering.
		scn.HinderCard(toby);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertEquals("Pinnacle of Doom", site.getBlueprint().getTitle());

		// 3 companions + 9 for the site
		assertEquals(12, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.FreepsAnyDecisionsAvailable());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sting, armor, bilbo, promise, runner, scimitar, pinned, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, gimli, ring, toby, site, scn.GetSite(8)));
		assertEquals(8, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertFalse(scn.IsHindered(armor));

		scn.ShadowChooseCard(armor);
		assertTrue(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(sting));
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(promise));
		assertFalse(scn.IsHindered(runner));
		assertFalse(scn.IsHindered(scimitar));
		assertFalse(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(gimli));
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningLeavesThePoolAndEveryCardAsItWasAndTheOfferIsNotMadeAgainInThatPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var promise = scn.GetFreepsCard("promise");
		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(promise);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();

		// 2 companions + 9 for the site
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(11, scn.GetTwilight());
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(promise));
		assertFalse(scn.IsHindered(runner));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The maneuver phase: offered at its start and declined again.
		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The phase goes on: the offer was for its start, and is not made again when the Shadow player's turn
		// to act in it comes.
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(11, scn.GetTwilight());
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(promise));
		assertFalse(scn.IsHindered(runner));
	}

	@Test
	public void IsNotOfferedWhileThePoolIsEmptyAndIsOfferedWithExactlyOneToRemove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var promise = scn.GetFreepsCard("promise");
		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(promise);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();

		// The Shadow phase, with 2 companions + 9 for the site in the pool: offered, and declined.
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();

		// The maneuver phase starts with nothing in the pool: no offer, though three cards could be hindered.
		scn.SetTwilight(0);
		scn.ShadowPass();
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The archery phase starts with exactly (1): offered, and taking it empties the pool.
		scn.SetTwilight(1);
		scn.PassManeuverActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(bilbo, promise, runner));
		scn.ShadowChooseCard(promise);
		assertTrue(scn.IsHindered(promise));
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(runner));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The assignment phase starts with the pool empty again: no offer.
		scn.PassArcheryActions();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(runner));
	}

	@Test
	public void TheOfferIsMadeAtTheStartOfEveryPhaseAndACardAlreadyHinderedIsNotOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPCMovieScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sting = scn.GetFreepsCard("sting");
		var armor = scn.GetFreepsCard("armor");
		var bilbo = scn.GetFreepsCard("bilbo");
		var promise = scn.GetFreepsCard("promise");
		var toby = scn.GetFreepsCard("toby");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var pinned = scn.GetShadowCard("pinned");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(frodo, sting);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(promise, toby, pinned);
		scn.AttachCardsTo(aragorn, poison);
		scn.MoveMinionsToTable(runner1, runner2);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertEquals("Pinnacle of Doom", site.getBlueprint().getTitle());
		// 3 companions + 9 for the site
		assertEquals(12, scn.GetTwilight());

		// The Shadow phase.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(11, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(poison, sting, armor, bilbo, promise, toby, pinned, runner1, runner2));
		scn.ShadowChooseCard(poison);
		assertTrue(scn.IsHindered(poison));
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();

		// The maneuver phase. What was hindered a phase ago is not offered again.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sting, armor, bilbo, promise, toby, pinned, runner1, runner2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, frodo, aragorn, gimli, ring));
		scn.ShadowChooseCard(sting);
		assertTrue(scn.IsHindered(sting));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.PassManeuverActions();

		// The archery phase.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(9, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, bilbo, promise, toby, pinned, runner1, runner2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, sting));
		scn.ShadowChooseCard(bilbo);
		assertTrue(scn.IsHindered(bilbo));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.PassArcheryActions();

		// The assignment phase: offered, and this once declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(9, scn.GetTwilight());
		assertFalse(scn.IsHindered(promise));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{gimli, runner2});

		// The first skirmish.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, promise, toby, pinned, runner1, runner2));
		scn.ShadowChooseCard(toby);
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Aragorn's 8 beats the Runner's 5 and its one vitality.
		scn.PassSkirmishActions();

		// The second skirmish is a skirmish phase of its own: the offer is made again.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, promise, pinned, runner2));
		scn.ShadowChooseCard(armor);
		assertTrue(scn.IsHindered(armor));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Gimli's 6 beats the other Runner's 5.
		scn.PassSkirmishActions();

		// The regroup phase: in this format the game at site 9 does not end before the Shadow player's
		// reconcile, so the phase starts and the offer is made.
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasCardChoiceAvailable(promise, pinned));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, sting, bilbo, toby, armor, frodo, aragorn, gimli, ring));
		scn.ShadowChooseCard(pinned);
		assertTrue(scn.IsHindered(pinned));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Seven phases, seven offers, six taken: six cards hindered and (6) removed; no companion, and not
		// the Ring.
		assertTrue(scn.IsHindered(poison));
		assertTrue(scn.IsHindered(sting));
		assertTrue(scn.IsHindered(bilbo));
		assertTrue(scn.IsHindered(toby));
		assertTrue(scn.IsHindered(armor));
		assertFalse(scn.IsHindered(promise));
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(gimli));
		assertFalse(scn.IsHindered(ring));
	}

	@Test
	public void WhereTheGameEndsAsTheLastSkirmishEndsNoRegroupPhaseStartsHereAndNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var promise = scn.GetFreepsCard("promise");
		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(promise);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		// 2 companions + 9 for the site
		assertEquals(11, scn.GetTwilight());

		// Offered at the start of the Shadow, maneuver, archery, assignment and skirmish phases, and declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowPass();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();
		scn.PassManeuverActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();
		scn.PassArcheryActions();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowDeclineOptionalTrigger();
		assertFalse(scn.GameIsFinished());

		// Aragorn's 8 beats the Runner's 5. The Ring-bearer has survived every skirmish at site 9: the game
		// is over, with the pool not empty and two cards that could have been hindered.
		scn.PassSkirmishActions();
		assertTrue(scn.GameIsFinished());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(11, scn.GetTwilight());
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(promise));
	}

	@Test
	public void WithNothingInPlayThatCanBeHinderedTheOfferIsStillMadeAndRemovingOneHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();

		scn.StartGame();
		scn.SkipToSite(8);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertEquals("Pinnacle of Doom", site.getBlueprint().getTitle());

		// 1 companion + 9 for the site. In play: the Ring-bearer, The One Ring and the sites.
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(9, scn.GetTwilight());
		assertFalse(scn.IsHindered(frodo));
		assertFalse(scn.IsHindered(ring));
		assertFalse(scn.IsHindered(site));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheSiteBeforeNoOfferIsMadeAndItIsMadeOnceTheFellowshipMovesHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var promise = scn.GetFreepsCard("promise");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(promise);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());

		// Site 8, with 1 companion + 8 for the site in the pool and two cards that could be hindered: the
		// Shadow phase starts with no offer, and so does the regroup phase (no minion, so no phase between).
		assertEquals(9, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The fellowship moves again, to Pinnacle of Doom, and the next phase to start has the offer.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(9, scn.GetCurrentSiteNumber());
		var site = scn.GetCurrentSite();
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.IsHindered(bilbo));
		assertFalse(scn.IsHindered(promise));
	}

	@Test
	public void AMapInASupportAreaIsNotOfferedForHindering() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPCMovieScenario();

		var bilbo = scn.GetFreepsCard("bilbo");
		var promise = scn.GetFreepsCard("promise");
		var pinned = scn.GetShadowCard("pinned");
		var freepsMap = scn.GetFreepsMap();
		var shadowMap = scn.GetShadowMap();

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(promise, pinned);

		scn.FreepsPass();
		var site = scn.GetCurrentSite();
		assertEquals("Pinnacle of Doom", site.getBlueprint().getTitle());

		// Each player's Map lies in his support area, beside a condition of his that can be hindered.
		assertInZone(Zone.SUPPORT, freepsMap);
		assertInZone(Zone.SUPPORT, promise);
		assertInZone(Zone.SUPPORT, shadowMap);
		assertInZone(Zone.SUPPORT, pinned);
		assertFalse(scn.IsHindered(pinned));
		// 1 companion + 9 for the site
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(9, scn.GetTwilight());
		// The text excepts companions and The One Ring and says nothing of Maps. The game leaves a Map out of
		// the choice all the same: it marks the path a deck is built for and is not a card to be hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bilbo, promise, pinned));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(freepsMap, shadowMap));
		assertEquals(3, scn.ShadowGetCardChoiceCount());

		scn.ShadowChooseCard(pinned);
		assertTrue(scn.IsHindered(pinned));
		assertFalse(scn.IsHindered(freepsMap));
		assertFalse(scn.IsHindered(shadowMap));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
