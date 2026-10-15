package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_134_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chill", "1_134");
					put("chill2", "1_134");

					put("snows", "1_138");      // Saruman's Snows: another [isengard] weather condition played on a site, with no limit of its own
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
					put("servant", "3_58");     // Isengard Servant: [isengard] Orc, vitality 3; a regroup ability used only on purpose
					put("commander", "1_186");  // Guard Commander: [moria] Orc, vitality 2; a skirmish ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman: a Hobbit MINION ([men]), vitality 4; his abilities are used only on purpose

					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; cheated in
					put("merry", "1_302");      // Merry, Friend to Sam: a [shire] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit
					put("bounder", "1_286");    // Bounder: a Hobbit ALLY, vitality 2; a skirmish ability used only on purpose
					put("storm", "1_130");      // No Ordinary Storm: [isengard] condition in the support area that answers only weathers played; the other condition at the end of the turn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetEndOfTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("chill", "1_134");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumansChillStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Saruman's Chill
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Spell</b>. <b>Weather</b>. To play, exert an [isengard] minion. Plays on a site. Limit 1 per site.<br>Each Hobbit who moves from this site must exert. Discard this condition at the end of the turn.
		*/

		var scn = GetScenario();

		var chill = scn.GetFreepsCard("chill");

		assertEquals("Saruman's Chill", chill.getBlueprint().getTitle());
		assertNull(chill.getBlueprint().getSubtitle());
		assertFalse(chill.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, chill.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, chill.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, chill.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(chill, Keyword.SPELL));
		assertTrue(scn.HasKeyword(chill, Keyword.WEATHER));
		// CR 4.2's one ruling on the card: "The cost of this condition is 1. Some copies of this card are misprinted."
		assertEquals(1, chill.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnIsengardMinionToPlayOnTheSiteChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetShadowCard("chill");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(chill);

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
		assertTrue(scn.ShadowPlayAvailable(chill));

		scn.ShadowPlayCard(chill);
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
		assertAttachedTo(chill, site2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlyIsengardMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetShadowCard("chill");
		var raiders = scn.GetShadowCard("raiders");
		var commander = scn.GetShadowCard("commander");
		scn.MoveCardsToHand(chill);

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
		assertFalse(scn.ShadowPlayAvailable(chill));
	}

	@Test
	public void ASecondCopyIsNotOfferedTheSiteThatBearsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetShadowCard("chill");
		var chill2 = scn.GetShadowCard("chill2");
		var snows = scn.GetShadowCard("snows");
		var raiders = scn.GetShadowCard("raiders");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToHand(chill, chill2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(raiders, servant);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Sites 1 to 3 are in play: site 3 will bear a Chill, site 2 another weather condition, site 1 nothing.
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.AttachCardsTo(site2, snows);
		scn.ShadowRefreshDecision();
		assertAttachedTo(snows, site2);
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(servant));

		scn.ShadowPlayCard(chill);
		scn.ShadowChooseCard(site3);
		scn.ShadowChooseCard(raiders);
		assertAttachedTo(chill, site3);
		assertEquals(1, scn.GetWoundsOn(raiders));

		assertTrue(scn.ShadowPlayAvailable(chill2));
		scn.ShadowPlayCard(chill2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site3));
		scn.ShadowChooseCard(site2);
		scn.ShadowChooseCard(servant);

		assertAttachedTo(chill2, site2);
		assertAttachedTo(snows, site2);
		assertAttachedTo(chill, site3);
		assertEquals(1, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachHobbitCompanionExertsAsTheFellowshipMovesFromItsSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetShadowCard("chill");
		var chill2 = scn.GetShadowCard("chill2");
		var raiders = scn.GetShadowCard("raiders");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCardsToHand(chill);

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(pippin, aragorn, merry);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, ted);
		// Merry has 3 of his 4 vitality gone: exhausted, so he cannot exert (a wound would still be taken).
		scn.AddWoundsToChar(merry, 3);
		assertEquals(3, scn.GetWoundsOn(merry));
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowPlayCard(chill);
		scn.ShadowChooseCard(site2);
		assertAttachedTo(chill, site2);
		// A second copy on the site behind: it is no copy of the site moved from, and adds no exertion.
		scn.AttachCardsTo(site1, chill2);
		assertAttachedTo(chill2, site1);

		// Nobody is assigned, so there is no skirmish, and both minions stay in play into the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.SHADOW_CHARACTERS, ted);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(3, scn.GetWoundsOn(merry));

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		// Each Hobbit companion of either culture, once; not a companion who is no Hobbit, nor the Hobbit
		// ally or the Hobbit minion, who do not move with the fellowship, nor the exhausted Hobbit, who cannot exert.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(ted));
		assertEquals(3, scn.GetWoundsOn(merry));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnASiteTheFellowshipDoesNotMoveFromNoHobbitExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetShadowCard("chill");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToHand(chill);

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SetTwilight(10);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		scn.ShadowPlayCard(chill);
		scn.ShadowChooseCard(site1);
		assertAttachedTo(chill, site1);

		// With no minion left there is no combat, and the turn goes on to the regroup phase.
		scn.MoveCardsToDiscard(raiders);
		scn.SkipToMovementDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));

		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertAttachedTo(chill, site1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyThisConditionIsDiscardedAndOnlyAtTheEndOfTheTurnNotAfterTheMove() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var chill = scn.GetShadowCard("chill");
		var chill2 = scn.GetShadowCard("chill2");
		var storm = scn.GetShadowCard("storm");
		scn.MoveCardsToSupportArea(storm);
		assertInZone(Zone.SUPPORT, storm);

		scn.StartGame();
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.AttachCardsTo(site2, chill);
		scn.AttachCardsTo(site1, chill2);
		assertAttachedTo(chill, site2);
		assertAttachedTo(chill2, site1);
		assertInZone(Zone.SUPPORT, storm);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		// The end of a phase and a move are not the end of the turn.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertAttachedTo(chill, site2);
		assertAttachedTo(chill2, site1);

		scn.SkipToOtherPlayersTurn();
		// Each copy goes by its own trigger; the other condition in play stays.
		assertInDiscard(chill, chill2);
		assertInZone(Zone.SUPPORT, storm);
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void IsDiscardedAtTheEndOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEndOfTurnScenario();

		var chill = scn.GetShadowCard("chill");

		scn.StartGame();
		scn.FreepsPass();
		// Laid on the site the fellowship has just come to, so that no Hobbit moves from it this turn.
		var site = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.AttachCardsTo(site, chill);

		// The last thing asked in the turn: the Chill has stayed through every phase of it.
		scn.SkipToMovementDecision();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertAttachedTo(chill, site);
		assertTrue(scn.FreepsDecisionAvailable("another move"));

		scn.FreepsChooseToStay();
		// The first thing asked in the next turn, which is the other player's.
		assertInDiscard(chill);
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}
}
