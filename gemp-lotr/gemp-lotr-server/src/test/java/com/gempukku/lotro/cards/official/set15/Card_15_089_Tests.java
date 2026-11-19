package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_089_Tests
{

	// The Fellowship-block path: sites 1 to 3 are region 1 and sites 4 to 6 region 2.  Each player holds one site
	// of each number, so a card that plays "the next site" lays the player's own site of that number, replacing
	// the other player's where it lies.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reload", "15_89");
					put("covetous", "12_59");     // Covetous Easterling: a [men] Man; damage +2 only while skirmishing a Man
					put("bold", "15_73");         // Bold Easterling: a [men] Man; a skirmish ability never used
					put("ted", "18_75");          // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities never used
					put("nelya", "1_233");        // Úlairë Nelya, Lieutenant of Morgul: Shadow: exert her and spot an opponent's site to replace it with your site of the same number

					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a ranger, and a [gondor] Man; a maneuver ability never used
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard; his text is about his strength
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer
					put("barliman", "1_70");      // Barliman Butterbur: an ally; a fellowship ability never used
					put("pathfinder", "1_110");   // Pathfinder: Fellowship or Regroup: spot a ranger to play the fellowship's next site
					put("leader", "12_34");       // Traveled Leader: Maneuver or Regroup: spot a [gandalf] Wizard to replace a site in the fellowship's current region
					put("leader2", "12_34");
					put("leader3", "12_34");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Mere of Dead Faces 11_246 ("Regroup: Exert your Gollum or your Sméagol to play the fellowship's next site")
	// stands in each adventure deck in place of site 5, and the tests put it on the path as the site the
	// fellowship moves to from site 4, in region 2.
	protected VirtualTableScenario GetMereScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reload", "15_89");
					put("covetous", "12_59");     // Covetous Easterling: a [men] Man; damage +2 only while skirmishing a Man
					put("smeagol", "5_29");       // Sméagol, Slinker: pays the site's exertion; a skirmish ability never used
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site5", "11_246");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RapidReloadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Rapid Reload
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: While you can spot a [men] Man, the Free Peoples player cannot replace a site in the current region.
		 * 	<b>Archery:</b> Spot a [men] Man and remove (3) to exert an unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("reload");

		assertEquals("Rapid Reload", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileAMenManCanBeSpottedTheNextSiteOfTheCurrentRegionIsNotReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reload = scn.GetShadowCard("reload");
		var covetous = scn.GetShadowCard("covetous");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder);
		// The other player's Rivendell Valley lies at site 3, in the fellowship's region, as it does where he has
		// played his next site himself.  The minion is laid in the fellowship phase to be spotted there (Q-0228).
		scn.MoveCardToAdventurePath(shadowSite3);
		scn.MoveCardsToSupportArea(reload);
		scn.MoveMinionsToTable(covetous);
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertInZone(Zone.SUPPORT, reload);
		assertInZone(Zone.SHADOW_CHARACTERS, covetous);
		assertTrue(scn.FreepsPlayAvailable(pathfinder));

		scn.FreepsPlayCard(pathfinder);

		// The event is played, and the other player's site stays.
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertInZone(Zone.DISCARD, pathfinder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoMenManToSpotTheNextSiteIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reload = scn.GetShadowCard("reload");
		var ted = scn.GetShadowCard("ted");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var site3 = scn.GetFreepsSite(3);
		var shadowSite3 = scn.GetShadowSite(3);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(pathfinder);
		scn.MoveCardToAdventurePath(shadowSite3);
		scn.MoveCardsToSupportArea(reload);
		// A [men] minion who is no Man; Aragorn is a Man who is not [men], and Rapid Reload a [men] card that is
		// no Man.
		scn.MoveMinionsToTable(ted);
		scn.FreepsRefreshDecision();

		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.SUPPORT, reload);
		assertInZone(Zone.SHADOW_CHARACTERS, ted);

		scn.FreepsPlayCard(pathfinder);

		assertSame(site3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertInZone(Zone.ADVENTURE_DECK, shadowSite3);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheNextSiteIsReplacedWhereItLiesInTheNextRegion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reload = scn.GetShadowCard("reload");
		var bold = scn.GetShadowCard("bold");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pathfinder = scn.GetFreepsCard("pathfinder");
		var site4 = scn.GetFreepsSite(4);
		var shadowSite4 = scn.GetShadowSite(4);
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetFreepsHand(pathfinder);
		// The fellowship is at site 3, in region 1; the other player's Balin's Tomb lies at site 4, in region 2.
		scn.MoveCardToAdventurePath(shadowSite4);
		scn.MoveCardsToSupportArea(reload);
		scn.MoveMinionsToTable(bold);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite4, scn.GetSite(4));
		assertInZone(Zone.SHADOW_CHARACTERS, bold);

		scn.FreepsPlayCard(pathfinder);

		assertSame(site4, scn.GetSite(4));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite4);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheCurrentSiteAndASiteBehindItAreNotReplacedUntilTheManIsGone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reload = scn.GetShadowCard("reload");
		var bold = scn.GetShadowCard("bold");
		var gandalf = scn.GetFreepsCard("gandalf");
		var leader = scn.GetFreepsCard("leader");
		var leader2 = scn.GetFreepsCard("leader2");
		var leader3 = scn.GetFreepsCard("leader3");
		var site2 = scn.GetFreepsSite(2);
		var site3 = scn.GetFreepsSite(3);
		var shadowSite2 = scn.GetShadowSite(2);
		var shadowSite3 = scn.GetShadowSite(3);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetFreepsHand(leader, leader2, leader3);
		// No minion is played, so the turn goes from the move to site 3 straight to the regroup phase.  Sites 2
		// and 3 were laid by the moves, and are the other player's; the Easterling came through the turn.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(reload);
		scn.MoveMinionsToTable(bold);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(shadowSite2, scn.GetSite(2));
		assertSame(shadowSite3, scn.GetSite(3));
		assertInZone(Zone.ADVENTURE_DECK, site2, site3);
		assertTrue(scn.FreepsPlayAvailable(leader));

		// The event still asks for a site of the region; the site behind the fellowship is not replaced.
		scn.FreepsPlayCard(leader);
		scn.FreepsChooseCard(shadowSite2);
		assertSame(shadowSite2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, site2);
		assertInZone(Zone.DISCARD, leader);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// Nor is the fellowship's own site.
		scn.ShadowPass();
		scn.FreepsPlayCard(leader2);
		scn.FreepsChooseCard(shadowSite3);
		assertSame(shadowSite3, scn.GetSite(3));
		assertSame(shadowSite3, scn.GetCurrentSite());
		assertInZone(Zone.ADVENTURE_DECK, site3);
		assertInZone(Zone.DISCARD, leader2);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// With the Easterling gone, the same event replaces a site of the region.
		scn.MoveCardsToDiscard(bold);
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPlayCard(leader3);
		scn.FreepsChooseCard(shadowSite2);

		assertSame(site2, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite2);
		assertSame(shadowSite3, scn.GetSite(3));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ASiteOfTheRegionIsNotReplacedByASitesOwnAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMereScenario();

		var reload = scn.GetShadowCard("reload");
		var covetous = scn.GetShadowCard("covetous");
		var smeagol = scn.GetFreepsCard("smeagol");
		var mere = scn.GetShadowSite("site5");
		var site6 = scn.GetFreepsSite(6);
		var shadowSite6 = scn.GetShadowSite(6);
		scn.MoveCompanionsToTable(smeagol);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardToAdventurePath(mere);
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// At Mere of Dead Faces, site 5 of region 2, with the other player's Galadriel's Glade lying at site 6.
		scn.MoveCardToAdventurePath(shadowSite6);
		scn.MoveCardsToSupportArea(reload);
		scn.MoveMinionsToTable(covetous);
		scn.FreepsRefreshDecision();

		assertSame(mere, scn.GetCurrentSite());
		assertSame(shadowSite6, scn.GetSite(6));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsActionAvailable(mere));

		scn.FreepsUseCardAction(mere);

		// Sméagol has exerted, and nothing is replaced.
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertSame(shadowSite6, scn.GetSite(6));
		assertInZone(Zone.ADVENTURE_DECK, site6);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		// With the Easterling gone, the site's ability replaces it.
		scn.MoveCardsToDiscard(covetous);
		scn.ShadowPass();
		scn.FreepsUseCardAction(mere);

		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertSame(site6, scn.GetSite(6));
		assertInZone(Zone.ADVENTURE_DECK, shadowSite6);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayStillReplaceASiteOfTheCurrentRegion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reload = scn.GetShadowCard("reload");
		var covetous = scn.GetShadowCard("covetous");
		var nelya = scn.GetShadowCard("nelya");
		var site1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(reload);
		scn.MoveMinionsToTable(covetous, nelya);
		scn.ShadowRefreshDecision();

		// The fellowship is at site 2, and site 1 behind it is the Free Peoples player's own.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(site1, scn.GetSite(1));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertTrue(scn.ShadowActionAvailable(nelya));

		scn.ShadowUseCardAction(nelya);

		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite1, scn.GetSite(1));
		assertInZone(Zone.ADVENTURE_DECK, site1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ArcheryAbilityRemoves3ToExertAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reload = scn.GetShadowCard("reload");
		var covetous = scn.GetShadowCard("covetous");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gandalf, sam);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(reload);
		scn.MoveMinionsToTable(covetous);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertTrue(scn.ShadowActionAvailable(reload));

		scn.ShadowUseCardAction(reload);
		assertEquals(0, scn.GetTwilight());
		// An unbound companion: not a Ring-bound one, not an ally, not a minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gandalf));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, frodo, barliman, covetous));

		scn.ShadowChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWithNoMenManToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reload = scn.GetShadowCard("reload");
		var ted = scn.GetShadowCard("ted");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(reload);
		// A [men] minion who is no Man; Aragorn is a Man who is not [men].
		scn.MoveMinionsToTable(ted);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(reload));
	}

	@Test
	public void ArcheryAbilityCanBeUsedAgainUntilThePoolHoldsLessThan3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reload = scn.GetShadowCard("reload");
		var bold = scn.GetShadowCard("bold");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveCardsToSupportArea(reload);
		scn.MoveMinionsToTable(bold);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowActionAvailable(reload));

		scn.ShadowUseCardAction(reload);
		scn.ShadowChooseCard(gandalf);
		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// With (2) in the pool the cost cannot be paid.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(reload));
	}
}
