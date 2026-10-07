package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "103_76");
					put("nertea", "1_234");       // Ulaire Nertea, Messenger of Dol Guldur: [wraith] Nazgul, strength 9, not fierce; text about being played
					put("attea", "103_61");       // Ulaire Attea, Khamul of the East: a [raider] Nazgul, strength 12, fierce; his text needs 3 burdens
					put("mongrel", "7_194");      // Morgul Mongrel: a [wraith] Orc, strength 7; his text needs 3 wounds on the Ring-bearer
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a ranger, for Pathfinder
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup: spot a ranger to play the fellowship's next site
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Nelya, Glorified to Conquer
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: Fierce.
		 * 	Skirmish: Exert this minion to make a Nazgul strength +1 for each of your sites on the adventure path
		 * 	(limit +2, or limit +5 if you can spot another Nazgul).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nelya");

		assertEquals("Ulaire Nelya", card.getBlueprint().getTitle());
		assertEquals("Glorified to Conquer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForSixAtSiteTwoWhereSheIsNotRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(nelya);

		scn.StartGame();
		scn.SetTwilight(4);
		scn.FreepsPass();

		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 4 set + 1 for Frodo + 2 for the shadow number of site 2
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.HAND, nelya);
		assertTrue(scn.ShadowPlayAvailable(nelya));

		scn.ShadowPlayCard(nelya);

		// 7 - 6 to play; a site 2 minion is not roaming at site 2
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertEquals(11, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionExertsHerToMakeAnyOneNazgulStrengthPlusOneForEachOfHerOwnersSitesOnThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var nertea = scn.GetShadowCard("nertea");
		var attea = scn.GetShadowCard("attea");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(nelya, nertea, attea, mongrel);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The fellowship is at site 3. Site 1 is the Free Peoples player's; sites 2 and 3 were laid by the
		// Shadow player as the fellowship first reached them.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, scn.GetFreepsSite(1));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(2));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(3));

		// It is a skirmish action: not offered in the assignment phase.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nelya));
		scn.ShadowPass();

		// No Nazgul is in the skirmish: the Nazgul need not be.
		scn.FreepsAssignToMinions(aragorn, mongrel);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(11, scn.GetStrength(nelya));
		assertEquals(9, scn.GetStrength(nertea));
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		// Any Nazgul, herself and the [raider] one included; not the [wraith] Orc, though he is the one skirmishing.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, nertea, attea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mongrel));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(attea);

		// 12 + 2 for sites 2 and 3: not 3 for every site on the path
		assertEquals(14, scn.GetStrength(attea));
		assertEquals(11, scn.GetStrength(nelya));
		assertEquals(9, scn.GetStrength(nertea));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneSiteOfHerOwnersOnThePathTheBonusIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		// No other Nazgul: the limit is +2, which one site does not reach.
		scn.MoveMinionsToTable(nelya, mongrel);
		scn.SkipToAssignments();

		// The first move, to site 2: the only site the Shadow player has laid.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, scn.GetFreepsSite(1));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(2));

		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(11, scn.GetStrength(nelya));

		// She is the only Nazgul, so she is the one made stronger.
		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		// 11 + 1 for site 2: not 2 for both sites on the path
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyTheFreePeoplesPlayersSitesOnThePathSheExertsAndAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var nelya = scn.GetShadowCard("nelya");
		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		scn.StartGame();
		// Pathfinder lays the Free Peoples player's own site 2 before the fellowship moves there.
		scn.FreepsPlayCard(pathfinder);
		scn.MoveMinionsToTable(nelya, nertea);
		scn.SkipToAssignments();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, scn.GetFreepsSite(1));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetFreepsSite(2));
		assertInZone(Zone.ADVENTURE_DECK, scn.GetShadowSite(2));

		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(11, scn.GetStrength(nelya));
		// It has a cost, so it is offered though it will add nothing.
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(11, scn.GetStrength(nelya));
		assertEquals(9, scn.GetStrength(nertea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ASiteHerOwnerControlsIsNoLongerOnTheAdventurePathAndIsNotCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(nelya, attea);
		scn.SkipToAssignments();

		// At site 4 the Shadow player's sites 2, 3 and 4 are on the path.
		assertEquals(4, scn.GetCurrentSiteNumber());
		var site1 = scn.GetFreepsSite(1);
		var site2 = scn.GetShadowSite(2);
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(3));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(4));

		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(nelya);

		// With Attea to spot the limit is +5: 11 + 3 for sites 2, 3 and 4
		assertEquals(14, scn.GetStrength(nelya));
		assertEquals(1, scn.GetWoundsOn(nelya));

		scn.PassSkirmishActions();
		// Nelya 14 beats Aragorn 8
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());

		// The Shadow player takes control of site 1, the Free Peoples player's, and then of his own site 2.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertInZone(Zone.SUPPORT, site1);
		assertInZone(Zone.SUPPORT, site2);

		// The fierce skirmish.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(11, scn.GetStrength(nelya));

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(nelya);

		// 11 + 2 for sites 3 and 4: the two controlled sites, his own and his opponent's, count for nothing
		assertEquals(13, scn.GetStrength(nelya));
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(12, scn.GetStrength(attea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherNazgulNeitherAControlledSiteNorTheFreePeoplesPlayersCurrentSiteIsCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var nelya = scn.GetShadowCard("nelya");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pathfinder);

		scn.StartGame();
		scn.SkipToSite(3);
		// Pathfinder lays the Free Peoples player's own site 4 before the fellowship moves there.
		assertTrue(scn.FreepsPlayAvailable(pathfinder));
		scn.FreepsPlayCard(pathfinder);
		scn.MoveMinionsToTable(nelya, mongrel);
		scn.SkipToAssignments();

		assertEquals(4, scn.GetCurrentSiteNumber());
		var site1 = scn.GetFreepsSite(1);
		var site2 = scn.GetShadowSite(2);
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(3));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetFreepsSite(4));

		// The Shadow player takes control of site 1 and then of his own site 2: his site 3 alone is left on the path.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertInZone(Zone.SUPPORT, site2);

		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(11, scn.GetStrength(nelya));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.ShadowUseCardAction(nelya);

		// 11 + 1 for site 3: not 2 with the site he controls, which the limit of +2 would allow
		assertEquals(12, scn.GetStrength(nelya));
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnotherNazgulToSpotTheBonusStopsAtFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(nelya, attea);
		scn.SkipToAssignments();

		// At site 7 the Shadow player's sites 2 to 7 are on the path: six of them.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, scn.GetFreepsSite(1));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(2));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(7));

		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(attea);

		// The [raider] Nazgul is another Nazgul: 12 + 5, the limit, with six sites to count
		assertEquals(17, scn.GetStrength(attea));
		assertEquals(11, scn.GetStrength(nelya));
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOtherNazgulTheBonusStopsAtTwoLastsForThatSkirmishAndCanBeHadAgainInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		// Another [wraith] minion, but no other Nazgul.
		scn.MoveMinionsToTable(nelya, mongrel);
		scn.SkipToAssignments();

		// At site 4 the Shadow player's sites 2, 3 and 4 are on the path.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(2));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(3));
		assertInZone(Zone.ADVENTURE_PATH, scn.GetShadowSite(4));

		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(11, scn.GetStrength(nelya));

		// She is the only Nazgul, so she is the one made stronger.
		scn.ShadowUseCardAction(nelya);

		// 11 + 2, the limit, with three sites to count
		assertEquals(13, scn.GetStrength(nelya));
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(7, scn.GetStrength(mongrel));

		// Still so when the Shadow player is next asked, in the same skirmish.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(nelya));
		scn.ShadowPass();

		// Nelya 13 beats Aragorn 8. The skirmish is over and so is the bonus.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertEquals(11, scn.GetStrength(nelya));

		// The fierce skirmish is another skirmish phase, with a limit of its own.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(11, scn.GetStrength(nelya));
		scn.FreepsPass();

		scn.ShadowUseCardAction(nelya);

		assertEquals(13, scn.GetStrength(nelya));
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Ignore("F-0135: the limit caps each use and not the skirmish phase: a second use at limit +2 adds 2 more")
	@Test
	public void WithNoOtherNazgulASecondUseInTheSameSkirmishExertsHerAndAddsNothingBeyondTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var mongrel = scn.GetShadowCard("mongrel");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(nelya, mongrel);
		scn.SkipToAssignments();

		// At site 4 the Shadow player's sites 2, 3 and 4 are on the path.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowUseCardAction(nelya);
		assertEquals(13, scn.GetStrength(nelya));
		assertEquals(1, scn.GetWoundsOn(nelya));

		// The limit is for the whole skirmish phase. The ability has a cost, so it is offered all the same.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);

		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(13, scn.GetStrength(nelya));
		assertEquals(7, scn.GetStrength(mongrel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Ignore("F-0135: the limit caps each use and not the skirmish phase: a second use at limit +5 adds 3 more, for 6 in all")
	@Test
	public void WithAnotherNazgulTwoUsesInOneSkirmishGiveNoMoreThanFiveBetweenThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(nelya, attea);
		scn.SkipToAssignments();

		// At site 4 the Shadow player's sites 2, 3 and 4 are on the path.
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(nelya);
		// 11 + 3
		assertEquals(14, scn.GetStrength(nelya));
		assertEquals(12, scn.GetStrength(attea));

		scn.FreepsPass();
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(attea);

		// Three more would make six: the limit of +5 leaves 2, whichever Nazgul is chosen.
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertEquals(14, scn.GetStrength(attea));
		assertEquals(14, scn.GetStrength(nelya));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var nelya = scn.GetShadowCard("nelya");
		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, nertea);
		scn.AddWoundsToChar(nelya, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Vitality 3 with 1 wound: she can exert once more.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCard(nelya);
		assertEquals(2, scn.GetWoundsOn(nelya));
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: exhausted.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(nelya));
	}
}
