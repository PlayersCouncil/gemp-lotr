package com.gempukku.lotro.mechanics.sites;

import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Sanctuaries, and the healing done at them.
 *
 * Comprehensive Rules 4.2, "sanctuary": "A sanctuary is any site 3 or site 6 on the adventure path.  Sites
 * preceding the Shadows expansion set mark this with a keyword.
 *
 * At the start of your turn when your fellowship is at a sanctuary, you may heal up to 5 wounds from your
 * companions (not allies).  You may choose to heal 5 different companions once, or one companion twice and
 * another three times, or any other combination.  You don't have to heal any wounds at all – you may choose any
 * number from zero to 5."
 *
 * "Healing": "Generally, your fellowship only heals at a sanctuary you reach on the adventure path."
 *
 * The game asks for the heals one at a time, each a choice of one wounded companion or none, and asks no more
 * once no companion is wounded.
 */
public class SanctuaryTests
{
	// The Ring-bearer is Frodo, Son of Drogo, vitality 4.
	protected VirtualTableScenario GetScenario(HashMap<String, String> sites, String format) throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability, not used here
					put("boromir", "1_96");     // Boromir, Lord of Gondor: vitality 3; his text is about being overwhelmed
					put("gimli", "1_13");       // Gimli, Son of Glóin: vitality 3; a skirmish ability, not used here
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4; a fellowship ability, not used here
					put("guard", "1_7");        // Dwarf Guard: vitality 2; his text is about being played
					put("bounder", "1_286");    // Bounder: ally, home site 2, vitality 2; a skirmish ability, not used here
				}},
				sites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				format
		);
	}

	// Site 3 is Rivendell Valley and site 6 Galadriel's Glade, each printed a sanctuary.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.FellowshipSites, VirtualTableScenario.Multipath);
	}

	// A path of sites from the Shadows set onward, none of which has the keyword printed.  Fangorn Glade
	// (site1), Abandoned Mine Shaft (site2) and Sirannon Ruins (site5) do nothing as the fellowship arrives.
	protected VirtualTableScenario GetShadowsPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return GetScenario(VirtualTableScenario.ShadowsSites, VirtualTableScenario.Shadows);
	}

	@Test
	public void AtTheStartOfATurnAtASanctuaryFiveCompanionsAreHealedOnceEachAndNoSixthWoundAndNoAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var guard = scn.GetFreepsCard("guard");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, sam, guard);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Six wounded companions, one wound more than the sanctuary heals, and a wounded ally
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.SkipToSite(3);

		// The turn at the sanctuary begins with the first of five heals: any wounded companion, or none,
		// and not the ally.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir, gimli, sam, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));

		// A companion with no wound left is no longer among them.
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 4"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, gimli, sam, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, bounder));
		scn.FreepsChooseCard(aragorn);
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 3"));
		scn.FreepsChooseCard(boromir);
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 2"));
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 1"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bounder));
		scn.FreepsChooseCard(sam);

		// "Up to 5 wounds": five companions healed once each, and no sixth heal for the Guard.
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneCompanionMayBeHealedTwiceAndAnotherThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(sam, 2);
		scn.SkipToSite(3);

		// Sam twice: after his first heal he is still among the wounded.
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 5"));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 4"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam));
		scn.FreepsChooseCard(sam);
		assertEquals(0, scn.GetWoundsOn(sam));

		// Aragorn three times
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 1"));
		scn.FreepsChooseCard(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NoWoundNeedBeHealedAtAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToSite(3);

		// "You may choose any number from zero to 5": none
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 5"));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsDeclineSanctuaryHealing();

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SomeOfTheFiveHealsMayBeTakenAndTheRestLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(frodo, 1);
		scn.SkipToSite(3);

		// Two of the five
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(frodo);
		assertTrue(scn.FreepsDecisionAvailable("remaining heals: 3"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsDeclineSanctuaryHealing();

		// The wounds not chosen stay, and they are not healed later in the turn.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SiteThreeAndSiteSixAreSanctuariesAndATurnStartedAtAnotherSiteHealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 3);

		// A turn that starts at site 2 opens on the fellowship phase.
		scn.SkipToSite(2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// Site 3: the heals are offered, and left.
		scn.SkipToSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));
		scn.FreepsDeclineSanctuaryHealing();

		// Sites 4 and 5: nothing is offered.
		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.SkipToSite(5);
		assertEquals(5, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// Site 6: five heals again.
		scn.SkipToSite(6);
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.SANCTUARY));
		assertFalse(scn.HasKeyword(scn.GetSite(2), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.SANCTUARY));
		assertFalse(scn.HasKeyword(scn.GetSite(4), Keyword.SANCTUARY));
		assertFalse(scn.HasKeyword(scn.GetSite(5), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(scn.GetSite(6), Keyword.SANCTUARY));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AFellowshipThatMovesToASanctuaryIsHealedOnlyWhenItsNextTurnStartsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.SkipToSite(2);

		// The fellowship moves from site 2 to the sanctuary: arriving heals nothing, and nothing is asked
		// in the rest of that turn.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The other player's turn starts, at his own site 2: no heal for the fellowship resting at site 3.
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// "At the start of your turn when your fellowship is at a sanctuary"
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachPlayerHealsHisOwnCompanionsAtTheStartOfHisOwnTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var otherAragorn = scn.GetShadowCard("aragorn");
		scn.MoveCompanionsToTable(aragorn, otherAragorn);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(otherAragorn, 2);
		scn.SkipToSite(3);

		// Player 1's turn at the sanctuary: his own Aragorn, and not the other player's.
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(otherAragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(otherAragorn));
		assertFalse(scn.FreepsDecisionAvailable("Sanctuary healing"));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Player 2's fellowship reached site 3 on his last turn, and his own turn now starts there.
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.ShadowGetSiteNumber());
		assertTrue(scn.ShadowDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(otherAragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(otherAragorn);
		scn.ShadowChooseCard(otherAragorn);
		assertEquals(0, scn.GetWoundsOn(otherAragorn));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ASiteOfTheShadowsSetLaidAsSiteThreeIsASanctuaryWithNoKeywordPrinted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsPathScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var shaft = scn.GetShadowSite("site2");
		var ruins = scn.GetShadowSite("site5");

		// Two moves in the first turn: the Shadow player lays Abandoned Mine Shaft as site 2 and Sirannon
		// Ruins, which has no text at all, as site 3.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.AddWoundsToChar(aragorn, 2);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.SANCTUARY));
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(ruins);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(ruins, scn.GetCurrentSite());
		assertTrue(scn.HasKeyword(ruins, Keyword.SANCTUARY));
		assertFalse(scn.HasKeyword(scn.GetSite(2), Keyword.SANCTUARY));
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The other player's turn, and then the first player's next turn starts at the Ruins.
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Sanctuary healing - Choose companion to heal - remaining heals: 5"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn));
		scn.FreepsChooseCard(aragorn);
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
