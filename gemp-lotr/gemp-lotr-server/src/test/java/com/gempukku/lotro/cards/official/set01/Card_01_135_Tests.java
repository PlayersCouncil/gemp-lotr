package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_135_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frost", "1_135");
					put("frost2", "1_135");

					put("chill", "1_134");      // Saruman's Chill: another [isengard] weather condition played on a site, limit 1 per site of its own
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability used only on purpose
					put("commander", "1_186");  // Guard Commander: [moria] Orc, vitality 2; a skirmish ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman: a Hobbit MINION ([men]), strength 3; his abilities are used only on purpose

					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3 (enduring: kept unwounded); cheated in
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit, strength 8
					put("bounder", "1_286");    // Bounder: a Hobbit ALLY, strength 2, home site 2; a skirmish ability used only on purpose
					put("songs", "1_61");       // Songs of the Blessed Realm: Free Peoples condition in the support area, to see that only Frost is discarded at the end of the turn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansFrostStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Frost
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Spell</b>. <b>Weather</b>. To play, exert an [isengard] minion. Plays on a site. Limit 1 per site.<br>Each Hobbit at this site is strength -2. Discard this condition at the end of the turn.
		 */

		var scn = GetScenario();

		var frost = scn.GetFreepsCard("frost");

		assertEquals("Saruman's Frost", frost.getBlueprint().getTitle());
		assertNull(frost.getBlueprint().getSubtitle());
		assertFalse(frost.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, frost.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, frost.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, frost.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(frost, Keyword.SPELL));
		assertTrue(scn.HasKeyword(frost, Keyword.WEATHER));
		assertEquals(2, frost.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardMinionToPlayOnTheFellowshipsSiteAndEachHobbitCompanionThereIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frost = scn.GetShadowCard("frost");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(frost);

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, servant, commander);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));
		assertEquals(0, scn.GetWoundsOn(commander));
		// Frodo 3 + 1 for The Ruling Ring.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(frost));

		scn.ShadowPlayCard(frost);
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
		assertAttachedTo(frost, site2);

		// Each Hobbit companion of either culture; not a companion who is no Hobbit.
		assertEquals(2, scn.GetStrength(frodo));
		assertEquals(1, scn.GetStrength(pippin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnASiteTheFellowshipIsNotAtNoHobbitCompanionIsWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frost = scn.GetShadowCard("frost");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(frost);

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));

		scn.ShadowPlayCard(frost);
		scn.ShadowChooseCard(site1);

		assertAttachedTo(frost, site1);
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHobbitAllyWhoseHomeIsTheSiteAndAHobbitMinionAreStrengthMinus2Too() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frost = scn.GetShadowCard("frost");
		var raiders = scn.GetShadowCard("raiders");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(frost);

		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, ted);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site2 = scn.GetSite(2);
		// The fellowship is at site 2, Bounder's home: he is at it (CR 4.2 "ally"), and so is every minion.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));

		scn.ShadowPlayCard(frost);
		scn.ShadowChooseCard(site2);
		// The Raiding Party, the one [isengard] minion, exerts unasked.
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertAttachedTo(frost, site2);

		assertEquals(0, scn.GetStrength(bounder));
		assertEquals(1, scn.GetStrength(ted));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AHobbitAllyIsWeakenedAtHisHomeSiteWhileTheFellowshipIsAtAnother() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frost = scn.GetShadowCard("frost");
		var raiders = scn.GetShadowCard("raiders");
		var ted = scn.GetShadowCard("ted");

		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(frost);
		scn.MoveMinionsToTable(raiders, ted);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site2 = scn.GetSite(2);
		// The fellowship has moved on to site 3; Bounder is still at site 2, his home.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));

		scn.ShadowPlayCard(frost);
		scn.ShadowChooseCard(site2);
		// The Raiding Party, the one [isengard] minion, exerts unasked.
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertAttachedTo(frost, site2);

		// The Hobbit at site 2 is weakened; the Hobbit companion and the Hobbit minion, at site 3, are not.
		assertEquals(0, scn.GetStrength(bounder));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(ted));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frost = scn.GetShadowCard("frost");
		var raiders = scn.GetShadowCard("raiders");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(frost);

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
		assertFalse(scn.ShadowPlayAvailable(frost));
	}

	@Test
	public void ASecondCopyIsNotOfferedTheSiteThatBearsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frost = scn.GetShadowCard("frost");
		var frost2 = scn.GetShadowCard("frost2");
		var chill = scn.GetShadowCard("chill");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(frost, frost2);
		scn.MoveMinionsToTable(raiders, servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Sites 1 to 3 are in play: site 3 will bear a Frost, site 2 another weather condition, site 1 nothing.
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.AttachCardsTo(site2, chill);
		scn.ShadowRefreshDecision();
		assertAttachedTo(chill, site2);

		scn.ShadowPlayCard(frost);
		scn.ShadowChooseCard(site3);
		scn.ShadowChooseCard(raiders);
		assertAttachedTo(frost, site3);
		assertEquals(1, scn.GetWoundsOn(raiders));

		assertTrue(scn.ShadowPlayAvailable(frost2));
		scn.ShadowPlayCard(frost2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3));
		scn.ShadowChooseCard(site2);
		scn.ShadowChooseCard(servant);

		assertAttachedTo(frost2, site2);
		assertAttachedTo(chill, site2);
		assertAttachedTo(frost, site3);
		assertEquals(1, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void StaysOnItsSiteThroughTheRegroupPhaseAndAMoveAndIsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frost = scn.GetShadowCard("frost");
		var frost2 = scn.GetShadowCard("frost2");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(frost);

		var songs = scn.GetFreepsCard("songs");
		scn.MoveCardsToSupportArea(songs);
		assertInZone(Zone.SUPPORT, songs);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowPlayCard(frost);
		scn.ShadowChooseCard(site2);
		scn.AttachCardsTo(site1, frost2);
		assertAttachedTo(frost, site2);
		assertAttachedTo(frost2, site1);
		assertInZone(Zone.SUPPORT, songs);

		// With no minion left there is no combat, and the turn goes on to the regroup phase.
		scn.MoveCardsToDiscard(raiders);
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(frost, site2);
		assertAttachedTo(frost2, site1);

		// The end of a phase and a move are not the end of the turn.
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(frost, site2);
		assertAttachedTo(frost2, site1);

		scn.SkipToOtherPlayersTurn();
		// Each copy is discarded by its own trigger, and nothing else in play is.
		assertInDiscard(frost, frost2);
		assertInZone(Zone.SUPPORT, songs);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
