package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("headman", "6_4");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSiteControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("headman", "6_4");
					put("brigand1", "4_10");    // Dunlending Brigand: [dunland] Man with no text; the two other Men
					put("brigand2", "4_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHeadmanScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("headman1", "6_4");
					put("headman2", "6_4");
					put("brigand1", "4_10");    // Dunlending Brigand: a [dunland] Man with no text
					put("brigand2", "4_10");
					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man with no text
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man.  His move trigger is kept
					// out by laying him after the move; he is only spotted.
					put("saruman", "4_33");
					// Easterling Axeman: a [raider] Man.  Strength +3 and fierce only while 2 burdens can be spotted.
					put("axeman", "4_224");

					// For the Free Peoples hand: a companion, a possession and an event.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					put("armor", "1_8");        // Dwarven Armor
					put("stout", "1_315");      // Stout and Sturdy
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DunlendingHeadmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Dunlending Headman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: When you play this minion, you may spot 2 other [dunland] Men to take control of a site. The Free Peoples player may discard 2 cards from hand to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("headman");

		assertEquals("Dunlending Headman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledPlayingTheHeadmanOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var headman = scn.GetShadowCard("headman");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.MoveCardsToHand(headman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.ShadowPlayCard(headman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(headman));
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples player holds the cards the turns before dealt him, and declines to discard 2 of them.
		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards from hand to prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledPlayingTheHeadmanOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var headman = scn.GetShadowCard("headman");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(brigand1, brigand2);
		scn.MoveCardsToHand(headman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));

		scn.ShadowPlayCard(headman);
		// The Headman is in play beside 2 other [dunland] Men.
		assertInZone(Zone.SHADOW_CHARACTERS, headman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(headman));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void PlayedBesideAnotherHeadmanAndASavageTheFreePeoplesPlayerMayDiscardTwoCardsOfHisChoiceToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHeadmanScenario();

		var headman1 = scn.GetShadowCard("headman1");
		var headman2 = scn.GetShadowCard("headman2");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		var stout = scn.GetFreepsCard("stout");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.SetFreepsHand(aragorn, armor, stout);
		// The two other [dunland] Men, of two titles: another Headman, already in play, and the Savage.
		scn.MoveMinionsToTable(headman2, savage);
		scn.MoveCardsToHand(headman1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInHand(aragorn, armor, stout);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertFalse(scn.IsSiteControlled(site1));

		// The Headman played offers his own trigger; the one already in play offers nothing.
		scn.ShadowPlayCard(headman1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(headman1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(headman2));
		scn.ShadowChooseOptionalTrigger(headman1);

		assertTrue(scn.FreepsDecisionAvailable("discard 2 cards from hand to prevent"));
		scn.FreepsChooseYes();
		// Exactly two, of any kind, of the three he holds.
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, armor, stout));
		scn.FreepsChooseCards(aragorn, stout);

		assertInDiscard(aragorn, stout);
		assertInHand(armor);
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.SHADOW_CHARACTERS, headman1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneCardInHandTheFreePeoplesPlayerIsNotAskedAndTheSiteIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHeadmanScenario();

		var headman = scn.GetShadowCard("headman1");
		var brigand = scn.GetShadowCard("brigand1");
		var savage = scn.GetShadowCard("savage");
		var armor = scn.GetFreepsCard("armor");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		// One card: one short of the two the prevention costs.
		scn.SetFreepsHand(armor);
		scn.MoveMinionsToTable(brigand, savage);
		scn.MoveCardsToHand(headman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertInHand(armor);
		assertFalse(scn.IsSiteControlled(site1));

		scn.ShadowPlayCard(headman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(headman));
		scn.ShadowChooseOptionalTrigger(headman);

		assertFalse(scn.FreepsDecisionAvailable("discard 2 cards from hand to prevent"));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertInHand(armor);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithOneOtherDunlandManBesideADunlandWizardAndARaiderManNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHeadmanScenario();

		var headman = scn.GetShadowCard("headman1");
		var brigand = scn.GetShadowCard("brigand1");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToHand(headman);
		scn.SetTwilight(10);
		scn.FreepsPass();
		// One other [dunland] Man (with the Headman himself, two [dunland] Men), a [dunland] minion who is no Man
		// and a Man of another culture.  Laid after the move, so that Saruman's move trigger has nothing to answer.
		scn.MoveMinionsToTable(brigand, saruman, axeman);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, brigand, saruman, axeman);
		assertFalse(scn.IsSiteControlled(site1));

		scn.ShadowPlayCard(headman);
		assertInZone(Zone.SHADOW_CHARACTERS, headman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(headman));
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void DecliningTakesNoSiteAndADunlandManPlayedAfterTheHeadmanSetsOffNothingOfHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHeadmanScenario();

		var headman = scn.GetShadowCard("headman1");
		var brigand1 = scn.GetShadowCard("brigand1");
		var brigand2 = scn.GetShadowCard("brigand2");
		var savage = scn.GetShadowCard("savage");
		var armor = scn.GetFreepsCard("armor");
		var stout = scn.GetFreepsCard("stout");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.SetFreepsHand(armor, stout);
		scn.MoveMinionsToTable(brigand1, savage);
		scn.MoveCardsToHand(headman, brigand2);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertInHand(armor, stout);
		assertFalse(scn.IsSiteControlled(site1));

		scn.ShadowPlayCard(headman);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(headman));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.FreepsDecisionAvailable("discard 2 cards from hand to prevent"));
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInHand(armor, stout);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [dunland] Man played once the Headman is in play, with site 1 still there to take and three
		// other [dunland] Men to spot: the Headman's text answers his own play only.
		scn.ShadowPlayCard(brigand2);
		assertInZone(Zone.SHADOW_CHARACTERS, brigand2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(headman));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}
}
