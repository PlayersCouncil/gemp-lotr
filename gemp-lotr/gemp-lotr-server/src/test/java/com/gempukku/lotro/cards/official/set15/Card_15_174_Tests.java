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

public class Card_15_174_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("striker", "15_174");
					put("striker2", "15_174");
					// Uruk Besieger: "Regroup: Stack this minion on a site you control."  Only ever stacked here.
					put("besieger", "4_180");

					// Aragorn, Ranger of the North: strength 8, vitality 4.  His maneuver ability is never used.
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukCavernStrikerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uruk Cavern Striker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br><b>Skirmish:</b> Spot a Free Peoples player's site on the adventure path and exert this minion twice to exchange the spotted site with a site you control. You now control the spotted site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("striker");

		assertEquals("Uruk Cavern Striker", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishAbilityExertsThisMinionTwiceAndTakesControlOfTheSpottedFreePeoplesSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var striker = scn.GetShadowCard("striker");
		var striker2 = scn.GetShadowCard("striker2");

		scn.StartGame();
		// Sites 3 and 4 are laid from the Free Peoples player's own adventure deck; sites 2 and 5 are the Shadow
		// player's, laid as the fellowship moves.
		scn.SkipToSite(2);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site3"));
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site4"));
		scn.SkipToSite(5);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		var site5 = scn.GetSite(5);
		assertEquals(scn.P1(), site3.getOwner());
		assertEquals(scn.P1(), site4.getOwner());
		assertEquals(scn.P2(), site2.getOwner());
		assertEquals(scn.P2(), site5.getOwner());
		// Site 1, the Free Peoples player's, is the lowest site behind both markers: the Shadow player took it.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);

		scn.FreepsPass();
		scn.MoveMinionsToTable(striker, striker2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, striker},
				new PhysicalCardImpl[]{scn.GetRingBearer(), striker2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Fellowship at site 6, the Shadow player's marker at site 5: sites 1 to 4 are behind both.
		assertEquals(6, scn.FreepsGetSiteNumber());
		assertEquals(5, scn.ShadowGetSiteNumber());
		assertEquals(0, scn.GetWoundsOn(striker));
		assertEquals(0, scn.GetWoundsOn(striker2));
		assertFalse(scn.ShadowControls(site3));
		assertTrue(scn.ShadowActionAvailable(striker));
		scn.ShadowUseCardAction(striker);

		// The Free Peoples player's sites on the path are offered; the Shadow player's own are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site3, site4));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site2, site5));
		scn.ShadowChooseCard(site3);

		// Exerted twice; the other copy is untouched.
		assertEquals(2, scn.GetWoundsOn(striker));
		assertEquals(0, scn.GetWoundsOn(striker2));
		// He now controls the spotted site, given for the one site he controlled.
		assertTrue(scn.ShadowControls(site3));
		assertInZone(Zone.SUPPORT, site3);
		assertFalse(scn.ShadowControls(site4));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheSiteGivenInExchangeGoesBackOntoTheAdventurePathInTheSpottedSitesPlace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var striker = scn.GetShadowCard("striker");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site3"));
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site4"));
		scn.SkipToSite(5);
		var site1 = scn.GetSite(1);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		scn.FreepsPass();
		scn.MoveMinionsToTable(striker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, striker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertInZone(Zone.ADVENTURE_PATH, site3);
		scn.ShadowUseCardAction(striker);
		scn.ShadowChooseCard(site3);

		// The two sites change places: site 1 goes onto the path where site 3 was, and nobody controls it.
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertSame(site1, scn.GetSite(3));
		assertTrue(scn.ShadowControls(site3));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheFellowshipsCurrentSiteMayBeSpottedAndTheFellowshipIsThenAtTheSiteGiven() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var striker = scn.GetShadowCard("striker");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site3"));
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site4"));
		scn.SkipToSite(5);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		// Site 6 too is the Free Peoples player's own, and the fellowship moves to it.
		var site6 = scn.GetFreepsSite("site6");
		scn.MoveCardToAdventurePath(site6);

		scn.FreepsPass();
		scn.MoveMinionsToTable(striker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, striker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertSame(site6, scn.GetCurrentSite());
		scn.ShadowUseCardAction(striker);

		// Ketura (Q-0403): the marker rule of CR 4.2 "control" is for the standard "take control of a site" action,
		// which this is not, so every Free Peoples site on the path is offered, the current one included.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scn.GetSite(3), scn.GetSite(4), site6));
		scn.ShadowChooseCard(site6);

		assertEquals(2, scn.GetWoundsOn(striker));
		assertTrue(scn.ShadowControls(site6));
		assertInZone(Zone.SUPPORT, site6);
		// "Swapping position of two sites": the fellowship has not moved, and the site given is where it stands.
		assertEquals(6, scn.FreepsGetSiteNumber());
		assertSame(site1, scn.GetCurrentSite());
		assertSame(site1, scn.GetSite(6));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// The game goes on: the skirmish ends and the fellowship moves on to site 7.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(7, scn.FreepsGetSiteNumber());
		assertTrue(scn.ShadowControls(site6));
		assertSame(site1, scn.GetSite(6));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionStackedOnTheSiteGivenEndsStackedOnTheSiteTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var striker = scn.GetShadowCard("striker");
		var besieger = scn.GetShadowCard("besieger");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site3"));
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site4"));
		scn.SkipToSite(5);
		var site1 = scn.GetSite(1);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));
		scn.StackCardsOn(site1, besieger);
		assertInZone(Zone.STACKED, besieger);

		scn.FreepsPass();
		scn.MoveMinionsToTable(striker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, striker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetStackedCards(site1).size());
		scn.ShadowUseCardAction(striker);
		scn.ShadowChooseCard(site3);

		// Ketura (Q-0404): as when a controlled site is replaced (CR 4.2 "site"), the stacked minion moves from the
		// site given to the site taken.
		assertTrue(scn.ShadowControls(site3));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetStackedCards(site1).size());
		assertEquals(1, scn.GetStackedCards(site3).size());
		assertSame(besieger, scn.GetStackedCards(site3).get(0));
		assertInZone(Zone.STACKED, besieger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithNoFreePeoplesSiteOnTheAdventurePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var striker = scn.GetShadowCard("striker");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// The Free Peoples player's site 1 is the Shadow player's to control; site 2 and site 3, laid as the
		// fellowship moves, are the Shadow player's own.
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site1));

		scn.FreepsPass();
		scn.MoveMinionsToTable(striker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, striker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(scn.P2(), scn.GetSite(2).getOwner());
		assertEquals(scn.P2(), scn.GetSite(3).getOwner());
		assertEquals(0, scn.GetWoundsOn(striker));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(striker));
	}

	@Test
	public void NotOfferedWhileThisMinionCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var striker = scn.GetShadowCard("striker");
		var striker2 = scn.GetShadowCard("striker2");

		scn.StartGame();
		// Site 2 is the Free Peoples player's own.
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site2"));
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));

		scn.FreepsPass();
		scn.MoveMinionsToTable(striker, striker2);
		// Vitality 3 with 1 wound: he can exert once, not twice.
		scn.AddWoundsToChar(striker, 1);
		assertEquals(1, scn.GetWoundsOn(striker));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, striker},
				new PhysicalCardImpl[]{scn.GetRingBearer(), striker2});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(scn.P1(), scn.GetSite(2).getOwner());
		assertEquals(1, scn.GetWoundsOn(striker));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(striker));
		// The other copy, unwounded, is offered its own.
		assertTrue(scn.ShadowActionAvailable(striker2));
	}

	@Test
	public void WithNoSiteControlledTheAbilityIsOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var striker = scn.GetShadowCard("striker");

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site2"));
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);

		scn.FreepsPass();
		scn.MoveMinionsToTable(striker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, striker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(striker));
		assertTrue(scn.ShadowActionAvailable(striker));
		scn.ShadowUseCardAction(striker);
		assertTrue(scn.ShadowHasCardChoiceAvailable(site1, site2));
		scn.ShadowChooseCard(site2);

		// Exerted twice, and with no site of his to give nothing is exchanged.
		assertEquals(2, scn.GetWoundsOn(striker));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnlyASiteTheShadowPlayerControlsIsOfferedForTheExchange() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		var striker = scn.GetShadowCard("striker");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site3"));
		scn.SkipToSite(3);
		scn.MoveCardToAdventurePath(scn.GetFreepsSite("site4"));
		scn.SkipToSite(5);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		// Site 1 the Free Peoples player took on a turn of the other player's; sites 2 (the Shadow player's own)
		// and 3 (the Free Peoples player's) are the Shadow player's.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));

		scn.FreepsPass();
		scn.MoveMinionsToTable(striker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, striker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.ShadowControls(site4));
		scn.ShadowUseCardAction(striker);
		// Site 4 is the one Free Peoples site left on the path, and is spotted unasked.
		assertTrue(scn.ShadowHasCardChoiceAvailable(site2, site3));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(site1));
		scn.ShadowChooseCard(site3);

		assertTrue(scn.ShadowControls(site4));
		assertTrue(scn.FreepsControls(site1));
		assertEquals(2, scn.GetWoundsOn(striker));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
