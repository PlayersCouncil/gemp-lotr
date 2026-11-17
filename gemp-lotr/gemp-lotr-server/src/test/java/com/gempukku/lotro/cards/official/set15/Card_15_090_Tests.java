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

public class Card_15_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman1", "15_90");
					put("hillman2", "15_90");
					put("corps", "11_73");      // Corps of Harad: a [men] Man minion, fierce only at a battleground (none on this path)
					// Ted Sandyman: a [men] minion who is a Hobbit, no Man; his abilities are his to use and are not used
					put("ted", "18_75");
					// Easterling Axeman: a [raider] Man; strength +3 and fierce only while 2 burdens can be spotted
					put("axeman", "4_224");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion
					put("gimli", "15_5");       // Gimli, Eager Hunter: a Dwarf companion, no Man
					put("barliman", "1_70");    // Barliman Butterbur: a Man ally, home site 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RaptHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Rapt Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: At the start of the regroup phase, the Free People's player may liberate a site. You may spot 3 Men to prevent this.<br><b>Maneuver:</b> Spot another [men] Man and exert this minion to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hillman1");

		assertEquals("Rapt Hillman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfTheRegroupPhaseTheFreePeoplesPlayerMayLiberateASiteThatTwoMenCannotSave() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var ted = scn.GetShadowCard("ted");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Player 1's second turn: the Shadow player holds site 1, taken on his own turn.
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman, ted);
		assertTrue(scn.ShadowControls(site1));

		// Not at the start of the maneuver phase.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsDecisionAvailable("liberate"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.ShadowControls(site1));

		scn.SkipToPhase(Phase.REGROUP);
		// The question is the Free Peoples player's, not the Shadow player's.
		assertTrue(scn.FreepsDecisionAvailable("liberate"));
		assertFalse(scn.ShadowDecisionAvailable("liberate"));
		assertTrue(scn.ShadowControls(site1));
		scn.FreepsChooseYes();

		// Two Men only, the Hillman and Aragorn: Ted is a [men] Hobbit and Gimli a Dwarf.  The Shadow player cannot
		// spot 3 Men and is not asked to prevent it.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerDecliningLeavesTheSiteControlled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman);
		assertTrue(scn.ShadowControls(site1));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.FreepsDecisionAvailable("liberate"));
		scn.FreepsChooseNo();

		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithThreeMenOfAnyKindTheShadowPlayerMaySpotThemToPreventTheLiberation() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman);
		assertTrue(scn.ShadowControls(site1));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsChooseYes();

		// "3 Men" names no side and no type: the Hillman himself, Aragorn (a companion) and Barliman (an ally).
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerDecliningToPreventLetsTheSiteBeLiberated() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var corps = scn.GetShadowCard("corps");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman, corps);
		assertTrue(scn.ShadowControls(site1));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsChooseYes();

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		assertFalse(scn.IsSiteControlled(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsTheFreePeoplesPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		// The only controlled site is the Free Peoples player's own, which he cannot liberate (CR 4.2 "liberate").
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman);
		assertTrue(scn.FreepsControls(site1));

		scn.SkipToPhase(Phase.REGROUP);
		assertFalse(scn.FreepsDecisionAvailable("liberate"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsControls(site1));
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void ManeuverAbilityExertsHimToTakeControlOfASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		// Player 1's second turn, at site 3: site 1 is behind both markers.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman, corps);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetWoundsOn(hillman));
		assertEquals(0, scn.GetWoundsOn(corps));
		assertTrue(scn.ShadowActionAvailable(hillman));
		scn.ShadowUseCardAction(hillman);

		assertEquals(1, scn.GetWoundsOn(hillman));
		assertEquals(0, scn.GetWoundsOn(corps));
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, site1);
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TwoHillmenSpotEachOtherAndTheOneUsedExertsItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman1 = scn.GetShadowCard("hillman1");
		var hillman2 = scn.GetShadowCard("hillman2");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman1, hillman2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Each copy is "another [men] Man" for the other.
		assertTrue(scn.ShadowActionAvailable(hillman1));
		assertTrue(scn.ShadowActionAvailable(hillman2));
		assertEquals(0, scn.GetWoundsOn(hillman1));
		assertEquals(0, scn.GetWoundsOn(hillman2));
		scn.ShadowUseCardAction(hillman2);

		assertEquals(0, scn.GetWoundsOn(hillman1));
		assertEquals(1, scn.GetWoundsOn(hillman2));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithNoOtherMenMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var ted = scn.GetShadowCard("ted");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		// A [men] minion who is no Man and a Man of another culture; the Hillman is no "other".
		scn.MoveMinionsToTable(hillman, ted, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, hillman, ted, axeman);
		assertEquals(0, scn.GetWoundsOn(hillman));
		assertFalse(scn.ShadowActionAvailable(hillman));
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman, corps);
		// Vitality 2: one wound exhausts him.
		scn.AddWoundsToChar(hillman, 1);
		assertEquals(1, scn.GetWoundsOn(hillman));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(hillman));
		assertFalse(scn.ShadowActionAvailable(hillman));
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void WithNoSiteToTakeTheAbilityIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman1");
		var corps = scn.GetShadowCard("corps");

		scn.StartGame();
		// Player 1's first turn: the other marker is still on site 1, so no site is behind every marker.
		scn.FreepsPass();
		scn.MoveMinionsToTable(hillman, corps);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(hillman));
		assertTrue(scn.ShadowActionAvailable(hillman));
		scn.ShadowUseCardAction(hillman);

		assertEquals(1, scn.GetWoundsOn(hillman));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertFalse(scn.IsSiteControlled(scn.GetSite(1)));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
