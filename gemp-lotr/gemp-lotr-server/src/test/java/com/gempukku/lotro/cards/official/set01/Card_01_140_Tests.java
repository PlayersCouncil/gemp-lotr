package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_140_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spies", "1_140");
					put("spies2", "1_140");     // a second copy, cheated onto another site, for the end-of-turn discard

					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3; damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability only ever used on purpose
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc, vitality 1; Archer is its whole text
					put("commander", "1_186");  // Guard Commander: [moria] Orc, vitality 2; a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion beside the Ring-bearer, vitality 4
					put("songs", "1_61");       // Songs of the Blessed Realm: Free Peoples condition in the support area, to see that only Spies is discarded at the end of the turn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpiesofSarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Spies of Saruman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, exert an [isengard] minion. Plays on a site.<br>While the fellowship is at this site, skip the archery phase. Discard this condition at the end of the turn.
		 */

		var scn = GetScenario();

		var spies = scn.GetFreepsCard("spies");

		assertEquals("Spies of Saruman", spies.getBlueprint().getTitle());
		assertNull(spies.getBlueprint().getSubtitle());
		assertFalse(spies.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spies.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, spies.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, spies.getBlueprint().getCardType());
		assertEquals(2, spies.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardMinionToPlayOnTheFellowshipsSiteAndTheArcheryPhaseIsSkipped() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		var marksman = scn.GetShadowCard("marksman");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(spies);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant, marksman, commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertTrue(scn.ShadowPlayAvailable(spies));

		scn.ShadowPlayCard(spies);
		// Either site in play; no card that is no site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site2);

		// An [isengard] minion of either race; not a minion of another culture (the Commander has the vitality to
		// exert; the Marksman, at vitality 1, could not have in any case).
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(commander));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(raiders);

		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertAttachedTo(spies, site2);
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.ShadowPass();
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		scn.FreepsPass();
		scn.ShadowPass();

		// The fellowship is at the site bearing Spies: from the maneuver phase straight to the assignment phase,
		// and the Marksman's archery fire never comes.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void OnASiteTheFellowshipIsNotAtTheArcheryPhaseComes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var raiders = scn.GetShadowCard("raiders");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(spies);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, marksman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals(0, scn.GetWoundsOn(raiders));
		scn.ShadowPlayCard(spies);
		scn.ShadowChooseCard(site1);
		assertAttachedTo(spies, site1);
		assertEquals(1, scn.GetWoundsOn(raiders));

		scn.ShadowPass();
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		scn.FreepsPass();
		scn.ShadowPass();

		// The fellowship is at site 2, not at the site bearing Spies.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var raiders = scn.GetShadowCard("raiders");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(spies);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, commander);
		scn.AddWoundsToChar(raiders, 2);
		assertEquals(2, scn.GetWoundsOn(raiders));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Raiding Party has vitality 3: with 2 wounds it cannot exert.  The [moria] minion beside it could.
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(spies));
	}

	@Test
	public void StaysOnItsSiteThroughTheRegroupPhaseAndAMoveAndIsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spies = scn.GetShadowCard("spies");
		var spies2 = scn.GetShadowCard("spies2");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(spies);

		var songs = scn.GetFreepsCard("songs");
		scn.MoveCardsToSupportArea(songs);
		assertInZone(Zone.SUPPORT, songs);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowPlayCard(spies);
		scn.ShadowChooseCard(site2);
		scn.AttachCardsTo(site1, spies2);
		assertAttachedTo(spies, site2);
		assertAttachedTo(spies2, site1);
		assertInZone(Zone.SUPPORT, songs);

		// With no minion left there is no combat, and the turn goes on to the regroup phase.
		scn.MoveCardsToDiscard(raiders);
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(spies, site2);
		assertAttachedTo(spies2, site1);

		// The end of a phase and a move are not the end of the turn.
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(spies, site2);
		assertAttachedTo(spies2, site1);

		scn.SkipToOtherPlayersTurn();
		// Each copy is discarded by its own trigger, and nothing else in play is.
		assertInDiscard(spies, spies2);
		assertInZone(Zone.SUPPORT, songs);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
