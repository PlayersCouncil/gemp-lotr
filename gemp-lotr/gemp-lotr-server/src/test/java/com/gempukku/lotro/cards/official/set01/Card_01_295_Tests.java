package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_295_Tests
{

	// Site 1 is Farmer Maggot's Fields: "Fellowship: Exert a Hobbit to play Merry or Pippin from your draw deck."
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farmer", "1_295");
					put("farmer2", "1_295");
					put("merry", "4_310");      // Merry, Learned Guide: Hobbit companion, cost 1; a skirmish ability only
					put("pippin", "4_314");     // Pippin, Woolly-footed Rascal: Hobbit companion, cost 1; a skirmish ability only
					put("bounder", "1_286");    // Bounder: another Hobbit ally (home site 2); a skirmish ability only
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_321");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 1 is Green Hill Country: "The twilight cost of each Hobbit is -1."  Site 2 is Weatherhills: "Each
	// Nazgul's twilight cost is -1."
	protected VirtualTableScenario GetDiscountScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farmer", "1_295");
					put("sam", "1_311");        // Sam, Son of Hamfast: Hobbit companion, cost 2; a fellowship ability used only on purpose
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul, cost 4, held in hand for her cost to be read
					put("lemenya", "1_232");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "1_323");
					put("site2", "1_335");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 1 is Bag End Study: "When the fellowship moves from here, you may exert a [shire] companion to play a
	// [shire] condition from your draw deck."
	protected VirtualTableScenario GetStudyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farmer", "1_295");
					put("merry", "4_310");        // Merry, Learned Guide: [shire] companion, vitality 4; a skirmish ability only
					// [shire] conditions that go to the support area.  A Promise: cost 0, and nothing without Sam.
					// The Shire Countryside: cost 1, "To play, spot 2 [shire] companions"; it answers a burden
					// removed.  Nice Imitation: cost 1; it answers a wound to a Hobbit.
					put("promise", "2_112");
					put("countryside", "3_113");
					put("imitation", "2_106");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site1", "101_57");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Site 1 is Eastemnet Gullies, of the Tower block: "Fellowship: Exert 2 unbound companions to play Legolas
	// from your draw deck."
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("farmer", "1_295");
					put("merry", "4_310");      // Merry, Learned Guide: an unbound companion, vitality 4
					put("pippin", "4_314");     // Pippin, Woolly-footed Rascal: an unbound companion, vitality 4
					put("legolas", "1_50");     // Legolas, Greenleaf: in the draw deck, the card the site's text plays
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FarmerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Hobbit Farmer
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: While you can spot your site 1, this ally has the game text of that site.<br><b>Fellowship:</b> Exert this ally and spot opponent's site 1 to replace it with your site 1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("farmer");

		assertEquals("Hobbit Farmer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void IsPlayedFromHandForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farmer = scn.GetFreepsCard("farmer");
		scn.MoveCardsToHand(farmer);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(farmer));

		scn.FreepsPlayCard(farmer);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, farmer);
		assertEquals(2, scn.GetStrength(farmer));
		assertEquals(2, scn.GetVitality(farmer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtHisOwnersSite1EachFarmerOffersTheSitesActionAsHisOwnAndItDoesWhatTheSitesDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var farmer = scn.GetFreepsCard("farmer");
		var farmer2 = scn.GetFreepsCard("farmer2");
		var bounder = scn.GetFreepsCard("bounder");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var fields = scn.GetFreepsSite(1);
		scn.MoveAlliesToTable(farmer, farmer2, bounder);

		scn.StartGame();

		assertSame(fields, scn.GetCurrentSite());
		assertInZone(Zone.DECK, merry, pippin);
		// The site makes its own offer and each Farmer makes one of his own.  A Bounder, another Hobbit ally,
		// has no such text.
		assertTrue(scn.FreepsActionAvailable(fields));
		assertTrue(scn.FreepsActionAvailable(farmer));
		assertTrue(scn.FreepsActionAvailable(farmer2));
		assertFalse(scn.FreepsActionAvailable(bounder));

		// "Exert a Hobbit to play Merry or Pippin from your draw deck."
		scn.FreepsUseCardAction(farmer);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, farmer, farmer2, bounder));
		scn.FreepsChooseCard(frodo);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(merry);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(farmer));
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.DECK, pippin);
		// 1 for Merry
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnTheOpponentsSite1HeHasNoTextUntilHisActionExertsHimToReplaceItWithHisOwners() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// The second player's Farmers: the site his fellowship starts on is the first player's.
		var farmer = scn.GetShadowCard("farmer");
		var tired = scn.GetShadowCard("farmer2");
		var fields = scn.GetFreepsSite(1);
		var hisFields = scn.GetShadowSite(1);
		var hisSite2 = scn.GetShadowSite(2);

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.MoveAlliesToTable(farmer, tired);
		// One Farmer is exhausted and cannot pay for the action of his own.
		scn.AddWoundsToChar(tired, 1);
		scn.ShadowRefreshDecision();

		assertEquals(1, scn.GetCurrentSiteNumber());
		assertSame(fields, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, hisFields);
		// His own site 2 is on the path, laid when the first player moved: it is no site 1.
		assertSame(hisSite2, scn.GetSite(2));
		// The site's text is the mover's to use, whoever's card it is; the Farmers have none of it.  The
		// exhausted Farmer offers nothing at all, and the other only his own action.
		assertTrue(scn.ShadowActionAvailable(fields));
		assertFalse(scn.ShadowActionAvailable(tired));
		assertTrue(scn.ShadowActionAvailable(farmer));
		assertEquals(0, scn.GetWoundsOn(farmer));

		scn.ShadowUseCardAction(farmer);
		assertEquals(1, scn.GetWoundsOn(farmer));
		assertEquals(1, scn.GetWoundsOn(tired));
		assertSame(hisFields, scn.GetSite(1));
		assertSame(hisFields, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, fields);

		// The site is his owner's now.  Both Farmers are exhausted, and each offers the site's action, which
		// another Hobbit may exert for.
		assertTrue(scn.ShadowActionAvailable(hisFields));
		assertTrue(scn.ShadowActionAvailable(tired));
		assertTrue(scn.ShadowActionAvailable(farmer));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void ReplacingTheOpponentsSite1FromFurtherDownThePathGivesHimItsTextAtOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var farmer = scn.GetShadowCard("farmer");
		var tired = scn.GetShadowCard("farmer2");
		var fields = scn.GetFreepsSite(1);
		var hisFields = scn.GetShadowSite(1);

		scn.StartGame();
		// The second player's second turn: his fellowship starts it at site 2.
		scn.SkipToSite(2);
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.MoveAlliesToTable(farmer, tired);
		scn.AddWoundsToChar(tired, 1);
		scn.ShadowRefreshDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(fields, scn.GetSite(1));
		assertFalse(scn.ShadowActionAvailable(tired));
		assertTrue(scn.ShadowActionAvailable(farmer));

		scn.ShadowUseCardAction(farmer);
		assertEquals(1, scn.GetWoundsOn(farmer));
		assertSame(hisFields, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, fields);
		assertEquals(2, scn.GetCurrentSiteNumber());

		// Each Farmer has the text of the site behind the fellowship, which the site itself does not offer to
		// a fellowship that is not there.
		assertTrue(scn.ShadowActionAvailable(tired));
		assertTrue(scn.ShadowActionAvailable(farmer));
		assertFalse(scn.ShadowActionAvailable(hisFields));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void HisCopyOfGreenHillCountryStacksWithTheSiteAtSite1AndHoldsAloneFurtherDownThePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscountScenario();

		var farmer = scn.GetFreepsCard("farmer");
		var sam = scn.GetFreepsCard("sam");
		var hills = scn.GetFreepsSite(1);
		var opponentsSite2 = scn.GetShadowSite(2);
		scn.MoveCardsToHand(farmer, sam);

		scn.StartGame();

		assertSame(hills, scn.GetCurrentSite());
		// 2 - 1 for Green Hill Country
		assertEquals(1, scn.GetTwilightCost(sam));

		// The Farmer is a Hobbit himself: 1 - 1.
		scn.FreepsPlayCard(farmer);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, farmer);
		// 2 - 1 for Green Hill Country - 1 for the Farmer's copy of its text
		assertEquals(0, scn.GetTwilightCost(sam));
		// Site 1 is his owner's: he could exert, and has no action of his own to offer.
		assertEquals(0, scn.GetWoundsOn(farmer));
		assertFalse(scn.FreepsActionAvailable(farmer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToSite(2);
		scn.SetFreepsHand(sam);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(hills, scn.GetSite(1));
		// A site's own text is off once the fellowship has left it; the Farmer's copy is his wherever the
		// fellowship is: 2 - 1.
		assertEquals(1, scn.GetTwilightCost(sam));
		// The opponent's site 2 is under the fellowship, and is no site 1.
		assertSame(opponentsSite2, scn.GetCurrentSite());
		assertEquals(0, scn.GetWoundsOn(farmer));
		assertFalse(scn.FreepsActionAvailable(farmer));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(sam);
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOpponentsFarmerTakesTheTextFromHimAndHisOwnActionBringsItBack() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDiscountScenario();

		var farmer = scn.GetFreepsCard("farmer");
		var sam = scn.GetFreepsCard("sam");
		var hisFarmer = scn.GetShadowCard("farmer");
		var hisSam = scn.GetShadowCard("sam");
		var hisLemenya = scn.GetShadowCard("lemenya");
		var hills = scn.GetFreepsSite(1);
		var hisHills = scn.GetShadowSite(1);
		var hisWeatherhills = scn.GetShadowSite(2);
		scn.MoveAlliesToTable(farmer);
		scn.MoveCardsToHand(sam);

		scn.StartGame();
		// 2 - 1 for Green Hill Country - 1 for the Farmer
		assertEquals(0, scn.GetTwilightCost(sam));

		// The second player's first turn.  His fellowship stands on the first player's Green Hill Country,
		// whose text is in force for him; his Farmer has none of it: 2 - 1.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.MoveAlliesToTable(hisFarmer);
		scn.SetShadowHand(hisSam, hisLemenya);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetCurrentSiteNumber());
		assertSame(hills, scn.GetCurrentSite());
		assertEquals(1, scn.GetTwilightCost(hisSam));
		// His own Weatherhills lies ahead as site 2, laid when the first player moved.  It is no site 1, and
		// his Farmer has no text of it: a Nazgul in his hand costs her printed 4.
		assertSame(hisWeatherhills, scn.GetSite(2));
		assertEquals(4, scn.GetTwilightCost(hisLemenya));
		assertEquals(0, scn.GetWoundsOn(hisFarmer));
		assertTrue(scn.ShadowActionAvailable(hisFarmer));

		scn.ShadowUseCardAction(hisFarmer);
		assertEquals(1, scn.GetWoundsOn(hisFarmer));
		assertSame(hisHills, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, hills);
		// 2 - 1 for his own Green Hill Country - 1 for his Farmer
		assertEquals(0, scn.GetTwilightCost(hisSam));
		assertEquals(4, scn.GetTwilightCost(hisLemenya));

		// The first player's second turn, at site 2.  Site 1 is the opponent's now: his Farmer has no text of
		// it, and Sam costs his printed 2.
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		scn.SetFreepsHand(sam);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(hisHills, scn.GetSite(1));
		assertEquals(2, scn.GetTwilightCost(sam));
		assertEquals(0, scn.GetWoundsOn(farmer));
		assertTrue(scn.FreepsActionAvailable(farmer));

		scn.FreepsUseCardAction(farmer);
		assertEquals(1, scn.GetWoundsOn(farmer));
		assertSame(hills, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, hisHills);
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 2 - 1 for the Farmer's copy
		assertEquals(1, scn.GetTwilightCost(sam));
		assertFalse(scn.FreepsActionAvailable(farmer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HasNoTextOfHisOwnersSite1OfTheTowerBlock() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var farmer = scn.GetFreepsCard("farmer");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var legolas = scn.GetFreepsCard("legolas");
		var gullies = scn.GetFreepsSite(1);
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveAlliesToTable(farmer);

		scn.StartGame();

		// His owner's Eastemnet Gullies is site 1 of the Tower block, and offers its own action: two unbound
		// companions can exert and Legolas is in the draw deck.  "Your site 1" on a card of the Fellowship
		// block is site 1 of that block: the Farmer has no text.
		assertSame(gullies, scn.GetCurrentSite());
		assertEquals(SitesBlock.TWO_TOWERS, gullies.getBlueprint().getSiteBlock());
		assertEquals(1, gullies.getBlueprint().getSiteNumber());
		assertInZone(Zone.DECK, legolas);
		assertTrue(scn.FreepsActionAvailable(gullies));
		assertFalse(scn.FreepsActionAvailable(farmer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnOpponentsSite1OfTheTowerBlockIsNoSite1ForHisFarmerToReplace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		// The second player's Farmer: the site his fellowship starts on is the first player's Eastemnet Gullies.
		var farmer = scn.GetShadowCard("farmer");
		var gullies = scn.GetFreepsSite(1);
		var hisGullies = scn.GetShadowSite(1);

		scn.StartGame();
		scn.SkipToOtherPlayersTurn();
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
		scn.MoveAlliesToTable(farmer);
		scn.ShadowRefreshDecision();

		// "Opponent's site 1" on a card of the Fellowship block is site 1 of that block, as "your site 1" is: the
		// Farmer can exert, and the opponent's Gullies is site 1 of the Tower block, so he offers nothing.
		assertSame(gullies, scn.GetCurrentSite());
		assertEquals(SitesBlock.TWO_TOWERS, gullies.getBlueprint().getSiteBlock());
		assertEquals(1, gullies.getBlueprint().getSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, hisGullies);
		assertEquals(0, scn.GetWoundsOn(farmer));
		assertFalse(scn.ShadowActionAvailable(farmer));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheMoveFromHisOwnersBagEndStudyIsAnsweredOnceForTheSiteAndOnceForHisCopyAndTheMoveOnFromSite2NotAtAll() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStudyScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var farmer = scn.GetFreepsCard("farmer");
		var promise = scn.GetFreepsCard("promise");
		var countryside = scn.GetFreepsCard("countryside");
		var imitation = scn.GetFreepsCard("imitation");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(farmer);

		scn.StartGame();

		var study = scn.GetCurrentSite();
		assertSame(scn.GetFreepsSite(1), study);
		assertInZone(Zone.DECK, promise, countryside, imitation);

		// "When the fellowship moves from here": the site answers, and so does the Farmer who has its text.
		scn.FreepsPass();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(farmer));

		scn.FreepsChooseOptionalTrigger(farmer);
		scn.FreepsChooseCard(merry);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(promise);
		assertInZone(Zone.SUPPORT, promise);
		assertEquals(1, scn.GetWoundsOn(merry));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(study));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(farmer));
		scn.FreepsChooseOptionalTrigger(study);
		scn.FreepsChooseCard(frodo);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(countryside);
		assertInZone(Zone.SUPPORT, countryside);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(farmer));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on from site 2.  The Farmer still has the Study's text, and "here" in it is
		// still the Study: nothing is offered, though both companions could exert and a [shire] condition is
		// in the draw deck.
		scn.SkipToMovementDecision();
		assertInZone(Zone.DECK, imitation);
		assertInZone(Zone.ADVENTURE_PATH, study);
		scn.FreepsChooseToMove();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertInZone(Zone.DECK, imitation);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
