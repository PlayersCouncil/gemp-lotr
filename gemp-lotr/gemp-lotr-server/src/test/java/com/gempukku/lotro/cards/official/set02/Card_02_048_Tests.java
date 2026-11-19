package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("storm", "2_48");
					put("storm2", "2_48");

					put("snows", "1_138");      // Saruman's Snows: another [isengard] weather condition played on a site, with no limit of its own
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability used only on purpose
					put("commander", "1_186");  // Guard Commander: [moria] Orc, vitality 2; a skirmish ability used only on purpose

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used only on purpose
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] ALLY, vitality 2; a skirmish ability used only on purpose
					put("nostorm", "1_130");    // No Ordinary Storm: [isengard] condition in the support area that answers only weathers played; the other condition at the end of the turn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WizardStormStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Wizard Storm
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Spell</b>. <b>Weather</b>. To play, exert an [isengard] minion. Plays on a site. Limit 1 per site.<br>When the fellowship moves from this site, every companion must exert. Discard this condition at the end of the turn.
		*/

		var scn = GetScenario();

		var storm = scn.GetFreepsCard("storm");

		assertEquals("Wizard Storm", storm.getBlueprint().getTitle());
		assertNull(storm.getBlueprint().getSubtitle());
		assertFalse(storm.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, storm.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, storm.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, storm.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(storm, Keyword.SPELL));
		assertTrue(scn.HasKeyword(storm, Keyword.WEATHER));
		assertEquals(2, storm.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardMinionToPlayOnTheSiteChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetShadowCard("storm");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(storm);
		assertInHand(storm);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant, commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertTrue(scn.ShadowPlayAvailable(storm));

		scn.ShadowPlayCard(storm);
		// Either site in play; no card that is no site.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(raiders, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(site2);

		// An [isengard] minion of either race; not a minion of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiders, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(commander));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(servant);

		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertAttachedTo(storm, site2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetShadowCard("storm");
		var raiders = scn.GetShadowCard("raiders");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(storm);
		assertInHand(storm);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, commander);
		scn.AddWoundsToChar(raiders, 2);
		assertEquals(2, scn.GetWoundsOn(raiders));
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Raiding Party has vitality 3: with 2 wounds it cannot exert.  The [moria] minion beside it could.
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(storm));
	}

	@Test
	public void ASecondCopyIsNotOfferedTheSiteThatBearsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetShadowCard("storm");
		var storm2 = scn.GetShadowCard("storm2");
		var snows = scn.GetShadowCard("snows");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(storm, storm2);
		assertInHand(storm, storm2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(raiders, servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Sites 1 to 3 are in play: site 3 will bear a Wizard Storm, site 2 another weather condition, site 1 nothing.
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.AttachCardsTo(site2, snows);
		scn.ShadowRefreshDecision();
		assertAttachedTo(snows, site2);
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));

		scn.ShadowPlayCard(storm);
		scn.ShadowChooseCard(site3);
		scn.ShadowChooseCard(raiders);
		assertAttachedTo(storm, site3);
		assertEquals(1, scn.GetWoundsOn(raiders));

		assertTrue(scn.ShadowPlayAvailable(storm2));
		scn.ShadowPlayCard(storm2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3));
		scn.ShadowChooseCard(site2);
		scn.ShadowChooseCard(servant);

		assertAttachedTo(storm2, site2);
		assertAttachedTo(snows, site2);
		assertAttachedTo(storm, site3);
		assertEquals(1, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EveryCompanionExertsAsTheFellowshipMovesFromItsSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetShadowCard("storm");
		var storm2 = scn.GetShadowCard("storm2");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(storm);
		assertInHand(storm);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		// Merry has 3 of his 4 vitality gone: exhausted, so he cannot exert (a wound would still be taken).
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowPlayCard(storm);
		scn.ShadowChooseCard(site2);
		assertAttachedTo(storm, site2);
		// The Raiding Party paid for it: one of its 3 vitality gone.
		assertEquals(1, scn.GetWoundsOn(raiders));
		// A second copy on the site behind: it is no copy of the site moved from, and adds no exertion.
		scn.AttachCardsTo(site1, storm2);
		assertAttachedTo(storm2, site1);

		// Nobody is assigned, so there is no skirmish, and the minion stays in play into the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, raiders);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(3, scn.GetWoundsOn(merry));

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		// Each companion of either culture and race, once; not the ally, who does not move with the fellowship,
		// nor the minion, nor the exhausted companion, who cannot exert.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnASiteTheFellowshipDoesNotMoveFromNoCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetShadowCard("storm");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(storm);
		assertInHand(storm);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		scn.ShadowPlayCard(storm);
		scn.ShadowChooseCard(site1);
		assertAttachedTo(storm, site1);

		// With no minion left there is no combat, and the turn goes on to the regroup phase.
		scn.MoveCardsToDiscard(raiders);
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertAttachedTo(storm, site1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyThisConditionIsDiscardedAndOnlyAtTheEndOfTheTurnNotAfterTheMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetShadowCard("storm");
		var storm2 = scn.GetShadowCard("storm2");
		var nostorm = scn.GetShadowCard("nostorm");
		scn.MoveCardsToSupportArea(nostorm);
		assertInZone(Zone.SUPPORT, nostorm);

		scn.StartGame();
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.AttachCardsTo(site2, storm);
		scn.AttachCardsTo(site1, storm2);
		assertAttachedTo(storm, site2);
		assertAttachedTo(storm2, site1);
		assertInZone(Zone.SUPPORT, nostorm);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		// The end of a phase and a move are not the end of the turn.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(storm, site2);
		assertAttachedTo(storm2, site1);

		scn.SkipToOtherPlayersTurn();
		// Each copy goes by its own trigger; the other condition in play stays.
		assertInDiscard(storm, storm2);
		assertInZone(Zone.SUPPORT, nostorm);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
