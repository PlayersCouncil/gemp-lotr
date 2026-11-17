package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("miles", "13_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The card's own block: the Shadow player lays each next site, and holds others to replace it with.
	protected VirtualTableScenario GetReplacedSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("miles", "13_21");
					// Legolas, Prince of Mirkwood: an Elf companion and an archer; his text needs a skirmish
					put("legolas", "1_51");
					// Rúmil, Elven Protector: an Elf ally and an archer, home 6F, so he is spotted here and adds
					// nothing to the archery total; his archery ability wounds an Orc, and there is none
					put("rumil", "1_57");
					// Úlairë Nelya, Third of the Nine Riders: "Shadow: Exert Úlairë Nelya to replace the
					// fellowship's current site with a site from your adventure deck."  Fierce, vitality 3.
					put("nelya", "11_222");
					// Traveled Leader: [gandalf] Maneuver or Regroup event, cost 1, "Spot a [gandalf] Wizard to
					// replace a site in the fellowship's current region with a site from your adventure deck."
					put("leader", "12_34");
					// Gandalf, Friend of the Shirefolk: the [gandalf] Wizard; his text is about his strength
					put("gandalf", "1_72");
				}},
				new HashMap<>()
				{{
					put("site1", "18_138");     // Sirannon Ruins: River, and no text
					put("glade", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to; there are none
					put("road", "11_236");      // East Road: each companion is twilight cost +2, and no companion is played
					// Abandoned Mine Shaft: adds a burden only when left in the regroup phase outside region 1
					put("shaft", "13_185");
					// Never laid in these tests, and never named: five copies of Expanding Marshland.
					put("site5", "11_238");
					put("site6", "11_238");
					put("site7", "11_238");
					put("site8", "11_238");
					put("site9", "11_238");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// A numbered path, where a site may lie ahead of the fellowship and a Shadow card may replace it there.
	protected VirtualTableScenario GetSiteAheadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("miles", "13_21");
					// Legolas, Prince of Mirkwood: an Elf companion and an archer; his text needs a skirmish
					put("legolas", "1_51");
					// Rúmil, Elven Protector: an Elf ally and an archer, home 6F, so he is spotted at site 3 and
					// adds nothing to the archery total; his archery ability wounds an Orc, and there is none
					put("rumil", "1_57");
					// Úlairë Nelya, Lieutenant of Morgul: "Shadow: Exert Úlairë Nelya and spot an opponent's site
					// to replace it with your site of the same number."  Fierce, vitality 3.
					put("nelya", "1_233");
					// Arwen, Daughter of Elrond: an Elf companion who is no archer (a ranger); stronger against a Nazgûl only
					put("arwen", "1_30");
					// Garrison of Gondor: a Man companion and an archer (and a knight); defender only beside a fortification
					put("garrison", "8_36");
					// Goblin Runner: a minion for the archery phase of a later turn; its text answers only its own play
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ManyMilesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Many Miles
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot 2 Elf archers to make the fellowship archery total +1 (or +2 if a Shadow player has replaced the fellowship's current site this turn).
		*/

		var scn = GetScenario();

		var miles = scn.GetFreepsCard("miles");

		assertEquals("Many Miles", miles.getBlueprint().getTitle());
		assertNull(miles.getBlueprint().getSubtitle());
		assertFalse(miles.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, miles.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, miles.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, miles.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(miles, Timeword.ARCHERY));
		assertEquals(2, miles.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsOneToTheFellowshipArcheryTotalWhenNobodyHasReplacedTheCurrentSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var miles = scn.GetFreepsCard("miles");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(miles);

		var nelya = scn.GetShadowCard("nelya");
		var glade = scn.GetShadowSite("glade");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(nelya);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(glade, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(nelya));
		// Legolas alone: the ally is away from home.
		assertEquals(1, scn.GetFreepsArcheryTotal());

		assertTrue(scn.FreepsPlayAvailable(miles));
		scn.FreepsPlayCard(miles);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertInDiscard(miles);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// Still in force as the archery wounds are placed (both on the one minion), and gone with the phase.
		scn.PassArcheryActions();
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(1, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void AddsTwoToTheFellowshipArcheryTotalWhenAShadowPlayerHasReplacedTheCurrentSiteThisTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var miles = scn.GetFreepsCard("miles");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(miles);

		var nelya = scn.GetShadowCard("nelya");
		var glade = scn.GetShadowSite("glade");
		var road = scn.GetShadowSite("road");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();
		assertEquals(glade, scn.GetCurrentSite());

		// The Shadow player's Nazgûl replaces Fangorn Glade, under the fellowship, with East Road.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(road);
		assertEquals(road, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(nelya));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		assertTrue(scn.FreepsPlayAvailable(miles));
		scn.FreepsPlayCard(miles);
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertInDiscard(miles);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AddsOneWhenTheSiteAShadowPlayerReplacedThisTurnIsNoLongerTheCurrentSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var miles = scn.GetFreepsCard("miles");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(miles);

		var nelya = scn.GetShadowCard("nelya");
		var glade = scn.GetShadowSite("glade");
		var road = scn.GetShadowSite("road");
		var shaft = scn.GetShadowSite("shaft");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(road);
		assertEquals(road, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());

		// The fellowship moves on in the regroup phase, to a site the Shadow player lays and nobody replaces.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(shaft);
		assertEquals(shaft, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(road, scn.GetSite(2));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// A site was replaced by a Shadow player this turn, but not the site the fellowship is now at.
		assertTrue(scn.FreepsPlayAvailable(miles));
		scn.FreepsPlayCard(miles);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertInDiscard(miles);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AddsTwoAtASiteAShadowPlayerReplacedThisTurnBeforeTheFellowshipCameToIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteAheadScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var miles = scn.GetFreepsCard("miles");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(miles);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Site 3 lies ahead of the fellowship and is the Free Peoples player's own, as it is once the other
		// player's fellowship has gone that way and he laid it for it.
		scn.MoveCardToAdventurePath(scn.GetFreepsSite(3));
		assertEquals(scn.GetFreepsSite(3), scn.GetSite(3));
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		// The Shadow player's Nazgûl replaces it, with the fellowship still at site 2.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(scn.GetSite(3));
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(scn.GetShadowSite(3), scn.GetSite(3));
		assertEquals(2, scn.GetCurrentSiteNumber());

		// The fellowship moves on to it in the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(scn.GetShadowSite(3), scn.GetCurrentSite());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// A Shadow player has replaced this site this turn, wherever the fellowship stood when he did (Ketura
		// on Q-0259).
		assertTrue(scn.FreepsPlayAvailable(miles));
		scn.FreepsPlayCard(miles);
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertInDiscard(miles);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotPlayableWithOneElfArcherBesideAnElfWhoIsNoArcherAndAnArcherWhoIsNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteAheadScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var garrison = scn.GetFreepsCard("garrison");
		var miles = scn.GetFreepsCard("miles");
		scn.MoveCompanionsToTable(legolas, arwen, garrison);
		scn.MoveCardsToHand(miles);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.ARCHERY);

		assertInHand(miles);
		// Legolas and the Garrison, the two archers.
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(miles));
	}

	@Test
	public void AddsOneWhenTheFreePeoplesPlayerHasReplacedTheCurrentSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var gandalf = scn.GetFreepsCard("gandalf");
		var leader = scn.GetFreepsCard("leader");
		var miles = scn.GetFreepsCard("miles");
		scn.MoveCompanionsToTable(legolas, gandalf);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(leader, miles);

		var nelya = scn.GetShadowCard("nelya");
		var glade = scn.GetShadowSite("glade");
		var road = scn.GetFreepsSite("road");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(nelya);
		assertEquals(glade, scn.GetCurrentSite());

		// In the maneuver phase the Free Peoples player replaces the Shadow player's Fangorn Glade, under the
		// fellowship, with his own East Road.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(leader);
		scn.FreepsChooseCard(glade);
		scn.FreepsChooseCardBPFromSelection(road);
		assertEquals(road, scn.GetCurrentSite());
		assertInDiscard(leader);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// The current site was replaced this turn, but by the Free Peoples player, not a Shadow player.
		assertTrue(scn.FreepsPlayAvailable(miles));
		scn.FreepsPlayCard(miles);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertInDiscard(miles);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AddsTwoWhenTheSiteAShadowPlayerReplacedIsReplacedAgainByTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var gandalf = scn.GetFreepsCard("gandalf");
		var leader = scn.GetFreepsCard("leader");
		var miles = scn.GetFreepsCard("miles");
		scn.MoveCompanionsToTable(legolas, gandalf);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(leader, miles);

		var nelya = scn.GetShadowCard("nelya");
		var glade = scn.GetShadowSite("glade");
		var road = scn.GetShadowSite("road");
		var shaft = scn.GetFreepsSite("shaft");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(glade);
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();

		// The Shadow player's Nazgûl replaces Fangorn Glade, under the fellowship, with East Road.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(road);
		assertEquals(road, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(nelya));

		// In the maneuver phase the Free Peoples player replaces East Road with his own Abandoned Mine Shaft.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(leader);
		scn.FreepsChooseCard(road);
		scn.FreepsChooseCardBPFromSelection(shaft);
		assertEquals(shaft, scn.GetCurrentSite());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// The Shadow player's replacement still happened this turn (Ketura on Q-0259).
		assertTrue(scn.FreepsPlayAvailable(miles));
		scn.FreepsPlayCard(miles);
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertInDiscard(miles);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AddsOneAtASiteAShadowPlayerReplacedOnAnEarlierTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteAheadScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var miles = scn.GetFreepsCard("miles");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(rumil);
		scn.MoveCardsToHand(miles);

		var nelya = scn.GetShadowCard("nelya");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Site 3 lies ahead of the fellowship, the Free Peoples player's own; the Shadow player replaces it on
		// the first turn.
		scn.MoveCardToAdventurePath(scn.GetFreepsSite(3));
		scn.MoveMinionsToTable(nelya);
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(scn.GetSite(3));
		assertEquals(scn.GetShadowSite(3), scn.GetSite(3));

		// The fellowship stays at site 2; on its next turn it moves to the site replaced two turns before.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(scn.GetShadowSite(3), scn.GetCurrentSite());
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetFreepsArcheryTotal());

		assertTrue(scn.FreepsPlayAvailable(miles));
		scn.FreepsPlayCard(miles);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertInDiscard(miles);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
