package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_164_Tests
{

	// A Shadows-block path, on which whoever plays a site chooses it from his adventure deck.  The sites these tests
	// lay: Fangorn Glade (a forest: twilight for Free Peoples weapons when the fellowship moves to it), Crags of Emyn
	// Muil (a mountain: its text is about region 2), Sirannon Ruins (a river, no text), and two put in for this card,
	// Slag Mounds (a battleground: "Each time a companion is killed, add a burden") and Mere of Dead Faces (a marsh:
	// "Regroup: Exert your Gollum or your Smeagol to play the fellowship's next site").
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fearless", "13_164");
					put("fearless2", "13_164");
					put("outrider", "13_172");    // Uruk Outrider: [uruk-hai] Uruk-hai; damage +1 for each character in the dead pile
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard, cost 4, site number 4.
					// "Each companion is resistance -1"; his skirmish ability is never used here.
					put("saruman", "17_116");
					put("savage", "1_151");       // Uruk Savage: an Uruk-hai of the [isengard] culture; Damage +1 is its whole text
					// Weapons of Control: [uruk-hai] support-area condition, not unique.  "When you play this, add an
					// [uruk-hai] token here for each wounded companion."  Its skirmish ability is never used here.
					put("weapons", "13_177");
					// Deathly Roar: [uruk-hai] support-area condition.  "Each time the Free Peoples player plays a
					// possession or artifact, place an [uruk-hai] token here."  Its other text answers a skirmish won.
					put("roar", "17_113");
					// Poised for Assault: [uruk-hai] support-area condition; a token for each skirmish a companion
					// loses to an [uruk-hai] minion.  Here it has none.
					put("poised", "15_166");
					// Ships of Great Draught: [raider] support-area possession that takes [raider] tokens by its
					// own Shadow ability.
					put("ships", "8_65");
					put("bowmen", "1_264");       // Orc Bowmen: [sauron] support-area condition, inert without a [sauron] Orc

					put("lordOfMoria", "1_21");   // Lord of Moria: [dwarven] support-area condition, inert without a Dwarf
					put("toby", "1_305");         // Old Toby: a [shire] possession in the support area; its text is about being played
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] companion to bear the tale
					put("saga", "1_114");         // The Saga of Elendil: [gondor] condition borne by a [gondor] companion
					// Speak "Friend" and Enter: "Fellowship or Regroup: Spot Gandalf to play the fellowship's next
					// site.  Draw a card if you play an underground site."  No site played here is underground.
					put("speak", "2_26");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk
					put("smeagol", "5_29");       // Smeagol, Slinker: his skirmish ability is never used here
					// There's Another Way: [gollum] support-area condition.  "Smeagol is strength +1.  Regroup:
					// Discard this condition to replace the fellowship's current site with a site from your
					// adventure deck."
					put("way", "12_40");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.ShadowsSites);
					put("site8", "11_258");       // Slag Mounds
					put("site9", "11_246");       // Mere of Dead Faces
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void FearlessApproachStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Fearless Approach
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [uruk-hai] minion.<br>When you play this, if you spot a battleground site on the adventure path, reinforce 3 [uruk-hai] tokens.<br>Each time the Free Peoples player plays the fellowship's next site, you may discard a condition from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fearless");

		assertEquals("Fearless Approach", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiCultureMinionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var weapons = scn.GetShadowCard("weapons");
		scn.MoveCardsToHand(fearless, saruman);

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		// An Uruk-hai who is not of the [uruk-hai] culture, and an [uruk-hai] card that is no minion.
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToSupportArea(weapons);
		scn.AddTokensToCard(weapons, 1);
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fearless));

		// Saruman is an [uruk-hai] minion and no Uruk-hai.  4, and 2 more for roaming at site 2.
		scn.ShadowPlayCard(saruman);
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(fearless));

		scn.ShadowPlayCard(fearless);
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fearless);
		assertNotAttachedTo(fearless, saruman);
		// Neither site on the path is a battleground: nothing was reinforced.
		assertEquals(1, scn.GetCultureTokensOn(weapons));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingWithBattlegroundOnAdventurePathReinforces3UrukHaiTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var fearless2 = scn.GetShadowCard("fearless2");
		var outrider = scn.GetShadowCard("outrider");
		var weapons = scn.GetShadowCard("weapons");
		var roar = scn.GetShadowCard("roar");
		var poised = scn.GetShadowCard("poised");
		var ships = scn.GetShadowCard("ships");
		scn.MoveCardsToHand(fearless);

		// The fellowship starts at Slag Mounds and moves on to Sirannon Ruins: the battleground lies behind it.
		var mounds = scn.GetFreepsSite("site8");
		scn.StartGame(mounds);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.MoveMinionsToTable(outrider);
		// Two [uruk-hai] cards with an [uruk-hai] token each, one with none, a card with a [raider] token, and
		// another copy of the card itself.
		scn.MoveCardsToSupportArea(weapons, roar, poised, ships, fearless2);
		scn.AddTokensToCard(weapons, 1);
		scn.AddTokensToCard(roar, 1);
		scn.AddTokensToCard(ships, 1);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(scn.GetSite(1), Keyword.BATTLEGROUND));
		assertInZone(Zone.ADVENTURE_PATH, mounds);

		scn.ShadowPlayCard(fearless);

		// No "may": the first of three, on a card that already has an [uruk-hai] token.
		assertTrue(scn.ShadowHasCardChoiceAvailable(weapons, roar));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poised, ships, fearless, fearless2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(weapons);
		assertEquals(2, scn.GetCultureTokensOn(weapons));

		// The second may go to the same card ...
		assertTrue(scn.ShadowHasCardChoiceAvailable(weapons, roar));
		scn.ShadowChooseCard(weapons);

		// ... and the third to another.
		assertTrue(scn.ShadowHasCardChoiceAvailable(weapons, roar));
		scn.ShadowChooseCard(roar);

		assertEquals(3, scn.GetCultureTokensOn(weapons));
		assertEquals(2, scn.GetCultureTokensOn(roar));
		assertEquals(0, scn.GetCultureTokensOn(poised));
		assertEquals(1, scn.GetCultureTokensOn(ships));
		assertEquals(0, scn.GetCultureTokensOn(fearless));
		// The copy that was already in play was not played: three tokens in all, and nothing more is asked.
		assertEquals(0, scn.GetCultureTokensOn(fearless2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ReinforcesWhereTheBattlegroundIsTheSiteTheFellowshipIsAt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var outrider = scn.GetShadowCard("outrider");
		var weapons = scn.GetShadowCard("weapons");
		scn.MoveCardsToHand(fearless);

		// From Fangorn Glade to the Shadow player's Slag Mounds.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site8"));
		scn.MoveMinionsToTable(outrider);
		scn.MoveCardsToSupportArea(weapons);
		scn.AddTokensToCard(weapons, 1);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.BATTLEGROUND));

		scn.ShadowPlayCard(fearless);

		// One card has a token: all three go on it, with nothing to choose.
		assertEquals(4, scn.GetCultureTokensOn(weapons));
		assertEquals(0, scn.GetCultureTokensOn(fearless));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingWithoutBattlegroundOnAdventurePathDoesNotReinforce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var outrider = scn.GetShadowCard("outrider");
		var weapons = scn.GetShadowCard("weapons");
		scn.MoveCardsToHand(fearless);

		// From Fangorn Glade to Sirannon Ruins.
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.MoveMinionsToTable(outrider);
		scn.MoveCardsToSupportArea(weapons);
		scn.AddTokensToCard(weapons, 1);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetSite(1), Keyword.BATTLEGROUND));
		// Each player's Slag Mounds is in his adventure deck: a battleground, and not on the adventure path.
		assertInZone(Zone.ADVENTURE_DECK, scn.GetFreepsSite("site8"), scn.GetShadowSite("site8"));
		assertEquals(1, scn.GetCultureTokensOn(weapons));

		scn.ShadowPlayCard(fearless);

		assertInZone(Zone.SUPPORT, fearless);
		assertEquals(1, scn.GetCultureTokensOn(weapons));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABattlegroundTheShadowPlayerControlsIsNotOnTheAdventurePath() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var outrider = scn.GetShadowCard("outrider");
		var weapons = scn.GetShadowCard("weapons");
		scn.MoveCardsToHand(fearless);

		// The first player's first turn takes him from Slag Mounds to Sirannon Ruins, and the second player's
		// turn takes that fellowship there too.
		var mounds = scn.GetFreepsSite("site8");
		scn.StartGame(mounds);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site5"));
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// Both markers are past Slag Mounds: the second player takes control of it.
		assertEquals(2, scn.FreepsGetSiteNumber());
		assertEquals(2, scn.ShadowGetSiteNumber());
		scn.ShadowTakeControlOfSite(mounds);
		assertTrue(scn.ShadowControls(mounds));

		// On to Crags of Emyn Muil.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(scn.GetShadowSite("site3"));
		scn.MoveMinionsToTable(outrider);
		scn.MoveCardsToSupportArea(weapons);
		scn.AddTokensToCard(weapons, 1);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetCurrentSiteNumber());
		// Still a battleground, and in his support area (CR 4.2 "control": "no longer a site on the adventure path").
		assertTrue(scn.HasKeyword(mounds, Keyword.BATTLEGROUND));
		assertInZone(Zone.SUPPORT, mounds);
		assertFalse(scn.HasKeyword(scn.GetSite(2), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.ShadowPlayCard(fearless);

		assertInZone(Zone.SUPPORT, fearless);
		assertEquals(1, scn.GetCultureTokensOn(weapons));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreePeoplesPlayingNextSiteLetsShadowDiscardACondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, saga);
		scn.MoveCardsToSupportArea(lordOfMoria, toby);

		var fearless = scn.GetShadowCard("fearless");
		var bowmen = scn.GetShadowCard("bowmen");
		var ships = scn.GetShadowCard("ships");

		var crags = scn.GetFreepsSite("site3");
		scn.StartGame(scn.GetFreepsSite("site1"));
		// The second player's cards, as an earlier turn of his would have left them.
		scn.MoveCardsToSupportArea(fearless, bowmen, ships);
		scn.FreepsRefreshDecision();

		assertInZone(Zone.ADVENTURE_DECK, crags);

		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(crags);
		assertInZone(Zone.ADVENTURE_PATH, crags);
		assertSame(crags, scn.GetSite(2));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fearless));
		scn.ShadowAcceptOptionalTrigger();

		// A condition of either side, in a support area or borne, the card itself among them.  A possession of
		// either side is none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lordOfMoria, saga, bowmen, fearless));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(toby, ships));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(lordOfMoria);

		assertInDiscard(lordOfMoria);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, bowmen, fearless, ships, toby);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FreePeoplesPlayingNextSiteTriggerIsOptional() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(lordOfMoria);

		var fearless = scn.GetShadowCard("fearless");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(fearless);
		scn.FreepsRefreshDecision();

		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(scn.GetFreepsSite("site3"));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fearless));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, lordOfMoria, fearless);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ShadowPlayingNextSiteDuringMovementDoesNotTrigger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCardsToSupportArea(lordOfMoria);

		var fearless = scn.GetShadowCard("fearless");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(fearless);

		// The move: the Shadow player plays the fellowship's next site, as the rules have it.
		scn.FreepsPass();
		var ruins = scn.GetShadowSite("site5");
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertSame(ruins, scn.GetCurrentSite());

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, lordOfMoria, fearless);
	}

	@Test
	public void EachCopyOffersEachTimeAndASitesOwnTextPlayingTheNextSiteIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var speak = scn.GetFreepsCard("speak");
		var gandalf = scn.GetFreepsCard("gandalf");
		var smeagol = scn.GetFreepsCard("smeagol");
		var lordOfMoria = scn.GetFreepsCard("lordOfMoria");
		scn.MoveCardsToHand(speak);
		scn.MoveCompanionsToTable(gandalf, smeagol);
		scn.MoveCardsToSupportArea(lordOfMoria);

		var fearless = scn.GetShadowCard("fearless");
		var fearless2 = scn.GetShadowCard("fearless2");
		var bowmen = scn.GetShadowCard("bowmen");

		var mere = scn.GetFreepsSite("site9");
		var crags = scn.GetFreepsSite("site3");
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(fearless, fearless2, bowmen);
		scn.FreepsRefreshDecision();

		// The fellowship phase: Speak "Friend" and Enter lays Mere of Dead Faces as site 2.  Each copy offers.
		scn.FreepsPlayCard(speak);
		scn.FreepsChooseCardBPFromSelection(mere);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fearless));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fearless2));
		scn.ShadowChooseOptionalTrigger(fearless);
		scn.ShadowChooseCard(lordOfMoria);
		assertInDiscard(lordOfMoria);
		// The other copy's offer is still to be answered, and is declined.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fearless2));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, bowmen, fearless, fearless2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The fellowship moves to the site already there: nobody plays one, and nothing is offered.
		scn.FreepsPass();
		assertSame(mere, scn.GetCurrentSite());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The regroup phase, by the site's own text: Smeagol exerts and the Free Peoples player lays site 3.
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsUseCardAction(mere);
		scn.FreepsChooseCardBPFromSelection(crags);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertSame(crags, scn.GetSite(3));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fearless));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fearless2));
		scn.ShadowChooseOptionalTrigger(fearless2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(bowmen, fearless, fearless2));
		scn.ShadowChooseCard(bowmen);
		assertInDiscard(bowmen);
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, fearless, fearless2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenTheFreePeoplesPlayerReplacesTheSiteTheFellowshipIsAt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var smeagol = scn.GetFreepsCard("smeagol");
		var way = scn.GetFreepsCard("way");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(way);

		var fearless = scn.GetShadowCard("fearless");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.MoveCardsToSupportArea(fearless);
		scn.FreepsPass();
		var ruins = scn.GetShadowSite("site5");
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.SkipToPhase(Phase.REGROUP);
		assertSame(ruins, scn.GetCurrentSite());

		// There's Another Way: the Free Peoples player plays a site of his own, and it is site 2, where the
		// fellowship is, not the next.
		var crags = scn.GetFreepsSite("site3");
		scn.FreepsUseCardAction(way);
		scn.FreepsChooseCardBPFromSelection(crags);
		assertInDiscard(way);
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, fearless);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
