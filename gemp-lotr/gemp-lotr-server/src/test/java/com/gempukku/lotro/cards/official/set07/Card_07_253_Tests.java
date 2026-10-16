package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_253_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_253");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path with Westemnet Hills 4_344 as site 4: a plains, and the home of Guma (4T).  Site 3, the home
	// of the Rohirrim Herdsman (3T), is Barrows of Edoras 4_337, a sanctuary and no plains.  Moving to Westemnet
	// Hills heals each mounted ally; nobody is wounded then.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("steed", "7_253");
					put("steed2", "7_253");
					put("guma", "4_277");       // Guma: [rohan] Man ally, home 4T, strength 4, vitality 2; a fellowship ability used only on purpose
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4, vitality 2; a regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability used only on purpose
					// One Whom Men Would Follow: "Exert Aragorn to spot an ally.  Until the regroup phase, that ally is
					// strength +2 and participates in archery fire and skirmishes."
					put("follow", "1_109");
					put("warrior", "4_165");    // Orthanc Warrior: strength 7, vitality 2; damage +1 is its whole text
					put("warrior2", "4_165");
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site4", "4_344");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwiftSteedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Swift Steed
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Game Text: Bearer must be a [rohan] Man.<br>While at a plains site, bearer takes no more than 1 wound during each skirmish.<br>At the start of each skirmish involving bearer, each minion skirmishing bearer must exert.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Swift Steed", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void SwiftSteedTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}

	@Test
	public void BorneByAnAllyItHoldsHisWoundsOnlyWhileHisHomeSiteIsAPlains() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var steed = scn.GetFreepsCard("steed");
		var steed2 = scn.GetFreepsCard("steed2");
		var guma = scn.GetFreepsCard("guma");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(guma, herdsman);
		scn.AttachCardsTo(guma, steed);
		scn.AttachCardsTo(herdsman, steed2);
		scn.MoveCardsToHand(follow);

		var warrior = scn.GetShadowCard("warrior");
		var warrior2 = scn.GetShadowCard("warrior2");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(warrior, warrior2);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals("Westemnet Hills", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.PLAINS));

		// The Herdsman, away from his home site, is made to take part in skirmishes: 4 + 2.
		scn.FreepsPlayCard(follow);
		scn.FreepsChooseCard(herdsman);
		assertEquals(6, scn.GetStrength(herdsman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guma, warrior},
				new PhysicalCardImpl[]{herdsman, warrior2});

		// Guma is at his home site, the plains the fellowship is at: 7 beats his 4 and its damage +1 would place 2
		// wounds; he takes 1.
		scn.FreepsResolveSkirmish(guma);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guma));
		assertInZone(Zone.SUPPORT, guma);

		// The Herdsman is at his home site, Barrows of Edoras, which is no plains (CR 4.2 "ally"): 7 beats his 6,
		// and the 2 wounds kill him (vitality 2).
		scn.FreepsResolveSkirmish(herdsman);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, herdsman);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
