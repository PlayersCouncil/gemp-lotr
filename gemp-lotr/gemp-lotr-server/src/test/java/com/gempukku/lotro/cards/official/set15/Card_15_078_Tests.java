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

public class Card_15_078_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hillman", "15_78");
					put("hillman2", "15_78");
					put("bree", "11_90");       // Man of Bree: [men] Man; strength +1 for each card in the Free Peoples hand
					// Saruman, Coldly Still: a [men] minion who is no Man (a Wizard); his texts want a [men] possession
					put("saruman", "17_48");
					// Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion; a maneuver ability that liberates a site
					// he controls (none) and a regroup ability
					put("ted", "18_75");
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man, no text
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] companion; fellowship and response texts only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used on purpose
					put("sword", "1_112");      // Ranger's Sword: [gondor] possession on Aragorn, damage +1: a card that is no character
					put("bounder", "1_286");    // Bounder: [shire] ally; a skirmish ability only
					put("stairs", "1_24");      // Stairs of Khazad-dum: [dwarven] support-area condition, the only [dwarven] card
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] Shadow card; a regroup ability only
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
					put("hillman", "15_78");
					put("knight", "5_35");      // Gondorian Knight: a [gondor] companion with no text
					put("guard", "1_7");        // Dwarf Guard: a [dwarven] companion; with Frodo, 3 Free Peoples cultures
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EngrossedHillmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Engrossed Hillman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: At the start of the maneuver phase, if you can spot another [men] Man and 8 characters (or if you can spot 3 Free Peoples cultures), you may take control of a site.
		*/

		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");

		assertEquals("Engrossed Hillman", hillman.getBlueprint().getTitle());
		assertNull(hillman.getBlueprint().getSubtitle());
		assertFalse(hillman.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, hillman.getBlueprint().getSide());
		assertEquals(Culture.MEN, hillman.getBlueprint().getCulture());
		assertEquals(CardType.MINION, hillman.getBlueprint().getCardType());
		assertEquals(Race.MAN, hillman.getBlueprint().getRace());
		assertEquals(4, hillman.getBlueprint().getTwilightCost());
		assertEquals(10, hillman.getBlueprint().getStrength());
		assertEquals(3, hillman.getBlueprint().getVitality());
		assertEquals(4, hillman.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithASiteThatCanBeControlledTheStartOfTheManeuverPhaseOffersItAndAYesTakesIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var hillman = scn.GetShadowCard("hillman");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight, guard);
		scn.MoveMinionsToTable(hillman);

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoSiteThatCanBeControlledTheStartOfTheManeuverPhaseOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSiteControlScenario();

		var hillman = scn.GetShadowCard("hillman");
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 is the only site every marker has passed, and player 1 took it on player 2's turn: there is
		// nothing a yes could take (Q-0034).
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(knight, guard);
		scn.MoveMinionsToTable(hillman);

		assertTrue(scn.FreepsControls(site1));

		// The Hillman is in play, and [shire], [gondor] and [dwarven] can be spotted.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());
	}

	@Test
	public void WithAnotherMenManAndEightCharactersTheOfferIsMadeAndAYesTakesASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");
		var bree = scn.GetShadowCard("bree");
		var brigand = scn.GetShadowCard("brigand");
		var saruman = scn.GetShadowCard("saruman");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");

		scn.StartGame();
		// Player 1's second turn: both markers are on site 2, so site 1 is behind every marker.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveMinionsToTable(hillman, bree, brigand, saruman);
		assertInPlay(sam, aragorn, bounder, bree, brigand, saruman);
		assertFalse(scn.IsSiteControlled(site1));

		scn.SkipToPhase(Phase.MANEUVER);
		// 8 characters: Frodo, Sam, Aragorn, the Bounder (an ally) and four minions, the Hillman and Man of Bree among
		// them; only [shire] and [gondor] are spotted.
		assertEquals(2, scn.GetFreepsCultureCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman));
		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithSevenCharactersAndACardThatIsNoCharacterNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");
		var bree = scn.GetShadowCard("bree");
		var brigand = scn.GetShadowCard("brigand");
		var saruman = scn.GetShadowCard("saruman");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveMinionsToTable(hillman, bree, brigand, saruman);
		assertAttachedTo(sword, aragorn);
		assertInPlay(sam, aragorn, bree, brigand, saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		// 7 characters, the Ranger's Sword beside them; still [shire] and [gondor] alone.
		assertEquals(2, scn.GetFreepsCultureCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void WithoutAnotherMenManEightCharactersOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		var saruman = scn.GetShadowCard("saruman");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		// [men] minions who are no Men (a Wizard and a Hobbit) and a [dunland] Man; the Hillman himself is no other.
		scn.MoveMinionsToTable(hillman, ted, brigand, saruman);
		assertInPlay(sam, aragorn, bounder, ted, brigand, saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetFreepsCultureCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void TwoHillmenSpotEachOtherAndEachOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");
		var hillman2 = scn.GetShadowCard("hillman2");
		var brigand = scn.GetShadowCard("brigand");
		var saruman = scn.GetShadowCard("saruman");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveMinionsToTable(hillman, hillman2, brigand, saruman);
		assertInPlay(sam, aragorn, bounder, hillman2, brigand, saruman);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman2));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsSiteControlled(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Nothing is offered as the next phase begins.
		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void TwoFreePeoplesCulturesBesideAGollumCardAndAnInactiveFreePeoplesCardOfferNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		// The Shadow player's own [dwarven] condition is inactive on the Free Peoples player's turn.
		var shadowStairs = scn.GetShadowCard("stairs");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(shadowStairs);
		scn.MoveMinionsToTable(hillman, gollum);
		assertInZone(Zone.SUPPORT, shadowStairs);
		assertInPlay(aragorn, gollum);

		scn.SkipToPhase(Phase.MANEUVER);
		// [shire] and [gondor]; a [gollum] Shadow card spots no Free Peoples culture (CR 4.2 "spot").
		assertEquals(2, scn.GetFreepsCultureCount());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hillman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.IsSiteControlled(site1));
	}

	@Test
	public void ThreeCulturesOneOfThemOnAConditionOfferItAgainAtTheSecondManeuverPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hillman = scn.GetShadowCard("hillman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");

		scn.StartGame();
		// Both markers on site 3: sites 1 and 2 are behind every marker.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveMinionsToTable(hillman);
		assertInPlay(aragorn, stairs);
		assertEquals(0, scn.GetShadowControlledSiteCount());

		scn.SkipToPhase(Phase.MANEUVER);
		// [shire] Frodo, [gondor] Aragorn and the [dwarven] Stairs, a condition; the Hillman is the only [men] Man.
		assertEquals(3, scn.GetFreepsCultureCount());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));

		// The Hillman is left unassigned; the fellowship moves on again from the regroup phase.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hillman));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
