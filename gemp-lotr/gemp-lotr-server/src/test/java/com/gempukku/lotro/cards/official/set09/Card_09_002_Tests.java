package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("freca", "9_2");
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
					put("freca", "9_2");
					put("brigand", "4_10");     // Dunlending Brigand: the other [dunland] Man, with no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFrecaScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("freca", "9_2");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man with no text
					put("savage", "4_17");      // Dunlending Savage: a [dunland] Man with no text, cost 4
					// Saruman, Rabble-rouser: a [dunland] minion who is a Wizard and no Man.  His move trigger is kept
					// out by laying him after the move; he is only spotted.
					put("saruman", "4_33");
					// Easterling Axeman: a [raider] Man.  Strength +3 and fierce only while 2 burdens can be spotted.
					put("axeman", "4_224");
					// Hides: a [dunland] possession in the support area; its texts answer its own play and a wound
					// on a [dunland] Man, neither of which comes about here.
					put("hides", "4_19");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion with no text
					// Defend It and Hope: [gondor] Maneuver event, "Exert a [gondor] Man to wound a minion once"
					put("defend", "4_115");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a ranger companion
					// Gondor's Vengeance: [gondor] Regroup event, "Exert a ranger companion to discard a minion"
					put("vengeance", "1_106");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrecaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Freca, Hungry Savage
		 * Unique: True
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 1
		 * Site Number: 3
		 * Game Text: When you play Freca, you may spot another [dunland] Man to take control of a site.<br>When Freca is killed, the Free Peoples player may liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("freca");

		assertEquals("Freca", card.getBlueprint().getTitle());
		assertEquals("Hungry Savage", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledPlayingFrecaOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var freca = scn.GetShadowCard("freca");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(brigand);
		scn.MoveCardsToHand(freca);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.ShadowPlayCard(freca);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(freca));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledPlayingFrecaOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var freca = scn.GetShadowCard("freca");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(brigand);
		scn.MoveCardsToHand(freca);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.FreepsControls(site1));

		scn.ShadowPlayCard(freca);
		// Freca is in play beside the other [dunland] Man.
		assertInZone(Zone.SHADOW_CHARACTERS, freca);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(freca));
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void PlayingFrecaBesideADunlendingSavageAsTheOtherDunlandManTakesASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrecaScenario();

		var freca = scn.GetShadowCard("freca");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(freca);
		assertInHand(freca);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.ShadowPlayCard(freca);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(freca));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayingFrecaWithNoOtherDunlandManOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrecaScenario();

		var freca = scn.GetShadowCard("freca");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var hides = scn.GetShadowCard("hides");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCardsToHand(freca);
		scn.SetTwilight(10);
		scn.FreepsPass();
		// A [dunland] minion who is no Man, a Man of another culture, and a [dunland] card that is no Man; Freca
		// himself is no "other".  Laid after the move, so that Saruman's move trigger has nothing to answer.
		scn.MoveCardsToSupportArea(hides);
		scn.MoveMinionsToTable(saruman, axeman);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SUPPORT, hides);
		assertInZone(Zone.SHADOW_CHARACTERS, saruman, axeman);
		assertFalse(scn.IsSiteControlled(site1));

		scn.ShadowPlayCard(freca);
		assertInZone(Zone.SHADOW_CHARACTERS, freca);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(freca));
		assertTrue(scn.AwaitingShadowPhaseActions());

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void DecliningThePlayTriggerTakesNoSiteAndADunlandManPlayedAfterFrecaSetsOffNothingOfHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrecaScenario();

		var freca = scn.GetShadowCard("freca");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(brigand);
		scn.MoveCardsToHand(freca, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertFalse(scn.IsSiteControlled(site1));

		scn.ShadowPlayCard(freca);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(freca));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [dunland] Man played once Freca is in play, with site 1 still there to take: Freca's text
		// answers his own play only.
		scn.ShadowPlayCard(savage);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(freca));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void WhenFrecaIsKilledTheFreePeoplesPlayerMayLiberateASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrecaScenario();

		var freca = scn.GetShadowCard("freca");
		var knight = scn.GetFreepsCard("knight");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.SkipToSite(2);
		// The Shadow player holds site 1 (taken on his own turn) as Freca comes to the table.
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(freca);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.ShadowControls(site1));
		assertEquals(0, scn.GetWoundsOn(knight));

		// The Knight exerts to wound Freca, the only minion, who has vitality 1.
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertInDiscard(freca);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(freca));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(freca));
		scn.FreepsAcceptOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningTheKilledTriggerLeavesTheSiteControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrecaScenario();

		var freca = scn.GetShadowCard("freca");
		var knight = scn.GetFreepsCard("knight");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(freca);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowControls(site1));

		scn.FreepsPlayCard(defend);
		assertInDiscard(freca);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(freca));
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void FrecaDiscardedAndNotKilledOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrecaScenario();

		var freca = scn.GetShadowCard("freca");
		var aragorn = scn.GetFreepsCard("aragorn");
		var vengeance = scn.GetFreepsCard("vengeance");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(vengeance);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(freca);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SHADOW_CHARACTERS, freca);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Aragorn exerts to discard Freca, the only minion: a discard, not a kill (CR 4.2 "killed").
		scn.FreepsPlayCard(vengeance);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(freca);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(freca));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
	}

	@Test
	public void WhenFrecaIsKilledWithNoSiteAnOpponentControlsNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFrecaScenario();

		var freca = scn.GetShadowCard("freca");
		var knight = scn.GetFreepsCard("knight");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(knight);
		scn.MoveCardsToHand(defend);

		scn.StartGame();
		scn.SkipToSite(2);
		// The only controlled site is the Free Peoples player's own, which he cannot liberate (CR 4.2 "liberate").
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(freca);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsControls(site1));

		scn.FreepsPlayCard(defend);
		assertInDiscard(freca);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(freca));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(freca));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		assertTrue(scn.FreepsControls(site1));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}
}
