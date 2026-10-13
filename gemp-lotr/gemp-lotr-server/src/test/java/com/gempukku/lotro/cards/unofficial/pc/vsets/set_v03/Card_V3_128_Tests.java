package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_128_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo, who bears The Ruling Ring. He is kept out of every skirmish and is
	// never given a wound, so the Ring's response is never offered.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("gimli", "1_13");          // Gimli, Son of Gloin: strength 6, vitality 3
					put("guard", "1_7");           // Dwarf Guard: vitality 2, no text in play
					put("bilbo", "1_284");         // Bilbo, Retired Adventurer: [shire] ally, vitality 3; a fellowship ability only
					put("resistance", "7_121");    // Stout Resistance: skirmish event, "Add a threat to make a [gondor] Man strength +3."

					put("bandit", "7_160");        // Southron Bandit: [raider] Man, strength 8, vitality 2. "Maneuver: Exert this minion
					                               // to add (5). The Free Peoples player may add a threat to prevent this."
					put("towers", "7_173");        // War Towers: maneuver event, (1). "Spot a [raider] minion to add a threat. Add an
					                               // additional threat for each companion over 4."
					put("destroyer", "7_190");     // Morgul Destroyer: strength 6, vitality 2. "Skirmish: Remove a threat to make this
					                               // minion strength +3." Its when-played text needs a Nazgul and it is never played.
				}},
				new HashMap<>()
				{{
					put("site1", "7_330");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");        // Watchers of Cirith Ungol: a skirmish ability for a [wraith] Orc, never used
					put("site9", "103_128");       // Orodruin
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrodruinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Orodruin
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Mountain</b>. <b>Battleground</b>. Each time a threat is added, the Free Peoples player must
		 * 	assign all threat wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Orodruin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.BATTLEGROUND));
	}

	@Test
	public void EachThreatAddedHereIsTakenOffAgainAndTheFreePeoplesPlayerPutsItsWoundOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var bandit = scn.GetShadowCard("bandit");
		var towers = scn.GetShadowCard("towers");

		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bilbo);

		scn.StartGame();

		scn.SkipToSite(8);
		scn.MoveMinionsToTable(bandit);
		scn.MoveCardsToHand(towers);

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(bandit));

		// A threat added by a Shadow card, as that card's effect. It is the Free Peoples player who is asked.
		scn.ShadowPlayCard(towers);

		// The threat has been counted and taken off the dead pile before its wound is assigned.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		// Threat wounds go on companions: not on an ally, not on a minion.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, bandit));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(0, scn.GetWoundsOn(bandit));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A second threat in the same phase, this one added by the Free Peoples player to stop the Bandit's (5).
		scn.FreepsPass();
		var twilight = scn.GetTwilight();
		scn.ShadowUseCardAction(bandit);
		assertEquals(1, scn.GetWoundsOn(bandit));
		scn.FreepsChooseYes();

		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, bandit));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		scn.FreepsChooseCard(gimli);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(bandit));
		assertEquals(0, scn.GetThreats());
		// The threat was paid, so the Bandit's (5) was prevented.
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EveryThreatAlreadyOnTheDeadPileBecomesAWoundWhenOneMoreIsAddedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var resistance = scn.GetFreepsCard("resistance");
		var bandit = scn.GetShadowCard("bandit");

		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();

		scn.SkipToSite(8);
		// Two threats that were on the dead pile before the fellowship came here.
		scn.AddThreats(2);
		scn.MoveCardsToHand(resistance);
		scn.MoveMinionsToTable(bandit);

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		// Arriving with threats on the dead pile assigns nothing: none has been added here.
		scn.SkipToAssignments();
		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsAssignToMinions(aragorn, bandit);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(aragorn));

		// A threat added by a Free Peoples card, as its cost: the third of three companions' worth.
		scn.FreepsPlayCard(resistance);

		// All three are off the dead pile, and three wounds are to be assigned, one at a time, wherever the Free
		// Peoples player likes: here all three on one companion.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());
		// The event goes on to do what the threat paid for: 8 + 3.
		assertEquals(11, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ACompanionKilledByTheFirstThreatWoundBringsNoThreatWoundsOfHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var bandit = scn.GetShadowCard("bandit");

		scn.MoveCompanionsToTable(aragorn, gimli, guard);

		scn.StartGame();

		scn.SkipToSite(8);
		scn.AddThreats(2);
		// Vitality 2 with 1 wound: the next wound kills him.
		scn.AddWoundsToChar(guard, 1);
		scn.MoveMinionsToTable(bandit);

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(bandit);
		scn.FreepsChooseYes();

		// 2 threats lying + 1 added = 3 threat wounds, and the threats are already gone.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, guard));
		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DEAD, guard);

		// His death finds no threat on the dead pile. Two wounds are left of the three, and no more.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.FreepsChooseCard(aragorn);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AThreatTheLimitKeepsOffTheDeadPileAndAThreatRemovedAssignNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bandit = scn.GetShadowCard("bandit");
		var destroyer = scn.GetShadowCard("destroyer");
		var towers = scn.GetShadowCard("towers");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.SkipToSite(8);
		// Two companions and two threats: no further threat can be added.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreatLimit());
		scn.MoveMinionsToTable(bandit, destroyer);
		scn.MoveCardsToHand(towers);

		scn.FreepsPass();
		assertEquals(9, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(2, scn.GetThreats());

		// War Towers is played and paid for, and its threat is not added.
		scn.ShadowPlayCard(towers);
		assertInZone(Zone.DISCARD, towers);

		assertEquals(2, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, destroyer);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(6, scn.GetStrength(destroyer));
		// A threat removed, with another left lying.
		scn.ShadowUseCardAction(destroyer);

		assertEquals(9, scn.GetStrength(destroyer));
		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtTheSiteBeforeTheSameThreatStaysOnTheDeadPileAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bandit = scn.GetShadowCard("bandit");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();

		scn.SkipToSite(7);
		scn.MoveMinionsToTable(bandit);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetThreats());

		scn.ShadowUseCardAction(bandit);
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
