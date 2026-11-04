package com.gempukku.lotro.mechanics.costs;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * What a card in play costs: the number printed on it.
 *
 * Comprehensive Rules 4.2, "twilight cost": "Cards already in play that affect another card's twilight cost have
 * that effect only when the card to be modified is coming into play."  Its example: Rohirrim Road makes "the
 * twilight cost of each condition and possession +2 until the end of the turn"; Catapult then reveals a condition
 * from the top of an opponent's deck, and "the condition's twilight cost is not modified by the site text of
 * Rohirrim Road, because the condition is not coming into play".
 *
 * Ketura, 2026-10-06 (F-0109): the rule stands as written.  And on a card whose own text changes its cost (Q-0073,
 * Eomer: "While you can spot a [rohan] Man, Eomer's twilight cost is -1"): "For now, we will follow the rules as
 * written."  So once a card is in play, anything that asks its twilight cost is answered with the number printed
 * on it, whatever another card or its own text says; what it costs to PLAY a card is another matter and is as
 * those texts make it.  (What a transfer costs is in TransferCostTests.)
 */
public class TwilightCostInPlayTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf and no archer; her text is about skirmishing a Nazgul
					// Mighty Shot: "Spot an Elf to make the fellowship archery total +1 for each minion with a twilight
					// cost of 2 or less that you can spot."  An archery event.
					put("shot", "15_21");
					put("savage", "1_151");     // Uruk Savage: [isengard], twilight cost 2, site number 5; "Damage +1." is its whole text
					put("fighter", "1_146");    // Uruk Fighter: [isengard], twilight cost 3, site number 5; a maneuver ability that needs 5 companions
					put("runner", "1_178");     // Goblin Runner: [moria], twilight cost 1; its text is about being played

					// Catapult: a [gondor] possession for the support area.  "Maneuver: Discard 2 cards from hand to reveal
					// the top card of an opponent's draw deck. Choose an opponent who must discard a Shadow card that has a
					// twilight cost that is the same as the twilight cost of the revealed card."
					put("catapult", "8_32");
					put("guard1", "1_7");       // Dwarf Guards: two cards to discard from hand
					put("guard2", "1_7");
					put("seeking", "1_276");    // Seeking Its Master: a Shadow condition for the support area, twilight cost 1
					put("coming", "1_196");     // They Are Coming: a Shadow condition for the support area, twilight cost 3
					put("fear", "1_278");       // Strength Born of Fear: another Shadow condition, twilight cost 1

					// Chaotic Clash: an [orc] condition.  "Each time you play a minion, it is twilight cost +1."  The rest
					// of its text is about being played and about [orc] minions.
					put("clash", "17_68");

					// Eomer, Third Marshal of Riddermark: twilight cost 3.  "While you can spot a [rohan] Man, Eomer's
					// twilight cost is -1."
					put("eomer", "4_267");
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man; a skirmish ability that is never used
					// Surrounded by Wraiths: "Each time the Free Peoples player plays a companion, that companion is
					// exhausted unless the Free Peoples player pays its twilight cost again."
					put("surrounded", "11_218");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rohirrim Road (7_332), a King block site 1: "Fellowship: Exert an Elf to make the twilight cost of each
	// condition and possession +2 until the end of the turn."  Site 2 is King's Tent, shadow number 1.
	protected VirtualTableScenario GetRohirrimRoadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf and no archer; her text is about skirmishing a Nazgul
					// Mighty Shot: "Spot an Elf to make the fellowship archery total +1 for each minion with a twilight
					// cost of 2 or less that you can spot."  An archery event.
					put("shot", "15_21");
					put("savage", "1_151");     // Uruk Savage: [isengard], twilight cost 2, site number 5; "Damage +1." is its whole text
					put("fighter", "1_146");    // Uruk Fighter: [isengard], twilight cost 3, site number 5; a maneuver ability that needs 5 companions
					put("runner", "1_178");     // Goblin Runner: [moria], twilight cost 1; its text is about being played

					// Catapult: a [gondor] possession for the support area.  "Maneuver: Discard 2 cards from hand to reveal
					// the top card of an opponent's draw deck. Choose an opponent who must discard a Shadow card that has a
					// twilight cost that is the same as the twilight cost of the revealed card."
					put("catapult", "8_32");
					put("guard1", "1_7");       // Dwarf Guards: two cards to discard from hand
					put("guard2", "1_7");
					put("seeking", "1_276");    // Seeking Its Master: a Shadow condition for the support area, twilight cost 1
					put("coming", "1_196");     // They Are Coming: a Shadow condition for the support area, twilight cost 3
					put("fear", "1_278");       // Strength Born of Fear: another Shadow condition, twilight cost 1

					// Chaotic Clash: an [orc] condition.  "Each time you play a minion, it is twilight cost +1."  The rest
					// of its text is about being played and about [orc] minions.
					put("clash", "17_68");

					// Eomer, Third Marshal of Riddermark: twilight cost 3.  "While you can spot a [rohan] Man, Eomer's
					// twilight cost is -1."
					put("eomer", "4_267");
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man; a skirmish ability that is never used
					// Surrounded by Wraiths: "Each time the Free Peoples player plays a companion, that companion is
					// exhausted unless the Free Peoples player pays its twilight cost again."
					put("surrounded", "11_218");
				}},
				new HashMap<>()
				{{
					put("site1", "7_332");
					put("site2", "7_335");
					put("site3", "8_117");
					put("site4", "7_342");
					put("site5", "7_345");
					put("site6", "7_350");
					put("site7", "8_120");
					put("site8", "10_120");
					put("site9", "7_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Slopes of Amon Hen (1_361) as site 9, shadow number 9: "The twilight cost of each
	// [isengard] minion is -1." is its whole text.
	protected VirtualTableScenario GetAmonHenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf and no archer; her text is about skirmishing a Nazgul
					// Mighty Shot: "Spot an Elf to make the fellowship archery total +1 for each minion with a twilight
					// cost of 2 or less that you can spot."  An archery event.
					put("shot", "15_21");
					put("savage", "1_151");     // Uruk Savage: [isengard], twilight cost 2, site number 5; "Damage +1." is its whole text
					put("fighter", "1_146");    // Uruk Fighter: [isengard], twilight cost 3, site number 5; a maneuver ability that needs 5 companions
					put("runner", "1_178");     // Goblin Runner: [moria], twilight cost 1; its text is about being played

					// Catapult: a [gondor] possession for the support area.  "Maneuver: Discard 2 cards from hand to reveal
					// the top card of an opponent's draw deck. Choose an opponent who must discard a Shadow card that has a
					// twilight cost that is the same as the twilight cost of the revealed card."
					put("catapult", "8_32");
					put("guard1", "1_7");       // Dwarf Guards: two cards to discard from hand
					put("guard2", "1_7");
					put("seeking", "1_276");    // Seeking Its Master: a Shadow condition for the support area, twilight cost 1
					put("coming", "1_196");     // They Are Coming: a Shadow condition for the support area, twilight cost 3
					put("fear", "1_278");       // Strength Born of Fear: another Shadow condition, twilight cost 1

					// Chaotic Clash: an [orc] condition.  "Each time you play a minion, it is twilight cost +1."  The rest
					// of its text is about being played and about [orc] minions.
					put("clash", "17_68");

					// Eomer, Third Marshal of Riddermark: twilight cost 3.  "While you can spot a [rohan] Man, Eomer's
					// twilight cost is -1."
					put("eomer", "4_267");
					put("rider", "4_286");      // Rider of Rohan: a [rohan] Man; a skirmish ability that is never used
					// Surrounded by Wraiths: "Each time the Free Peoples player plays a companion, that companion is
					// exhausted unless the Free Peoples player pays its twilight cost again."
					put("surrounded", "11_218");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_361");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CatapultUnderRohirrimRoadComparesTheRevealedCardAndTheCardsInPlayAtTheCostsPrintedOnThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohirrimRoadScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var catapult = scn.GetFreepsCard("catapult");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var seeking = scn.GetShadowCard("seeking");
		var coming = scn.GetShadowCard("coming");
		var fear = scn.GetShadowCard("fear");
		var fighter = scn.GetShadowCard("fighter");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(catapult);
		scn.MoveCardsToHand(guard1, guard2);
		scn.MoveCardsToSupportArea(coming);
		scn.MoveCardsToHand(seeking);

		scn.StartGame();

		assertEquals("Rohirrim Road", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(arwen));
		scn.SetTwilight(5);
		scn.FreepsPass();

		// 5 set + 2 companions + 1 for King's Tent
		assertEquals(8, scn.GetTwilight());
		// The Road is in force: a condition coming into play costs its 1 + 2.
		scn.ShadowPlayCard(seeking);
		assertEquals(5, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, seeking);

		scn.MoveMinionsToTable(fighter, runner);
		scn.MoveCardsToTopOfDeck(fear);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsActionAvailable(catapult));
		scn.FreepsUseCardAction(catapult);
		assertInDiscard(guard1, guard2);
		// Strength Born of Fear is revealed: a condition, printed cost 1, that would cost 3 to play this turn.
		scn.DismissRevealedCards();

		// The Shadow player must discard a Shadow card costing 1: Seeking Its Master, a condition in play that
		// has just been paid 3 for, and the Goblin Runner.  Not They Are Coming, a condition costing 3 (1 as
		// revealed + 2 for the Road), nor the Uruk Fighter, a minion costing 3.
		assertTrue(scn.ShadowHasCardChoiceAvailable(seeking, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(coming, fighter));
		scn.ShadowChooseCard(seeking);

		assertInDiscard(seeking);
		assertInZone(Zone.SUPPORT, coming);
		assertInZone(Zone.SHADOW_CHARACTERS, fighter, runner);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMinionWhoseCostASiteLowersIsPlayedForLessAndInPlayIsNotOneCostingLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAmonHenScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var shot = scn.GetFreepsCard("shot");
		var fighter = scn.GetShadowCard("fighter");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToHand(fighter);
		scn.SetTwilight(10);
		// The fellowship moves to site 9, Slopes of Amon Hen.
		scn.FreepsPass();

		assertEquals(9, scn.GetCurrentSiteNumber());
		// 10 set + 2 companions + 9 for the site
		assertEquals(21, scn.GetTwilight());

		// Coming into play, the Fighter costs its 3 - 1 for the site.
		scn.ShadowPlayCard(fighter);
		assertEquals(19, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, fighter);
		assertEquals(3, scn.GetTwilightCost(fighter));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.FreepsPlayCard(shot);

		// "Each minion with a twilight cost of 2 or less": the Savage, printed 2.  The Fighter in play costs
		// the 3 printed on it, though 2 was paid for it.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AMinionPlayedUnderChaoticClashCostsOneMoreAndInPlayCostsWhatIsPrintedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var shot = scn.GetFreepsCard("shot");
		var clash = scn.GetShadowCard("clash");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(shot);
		scn.MoveCardsToSupportArea(clash);
		scn.MoveCardsToHand(savage);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 2 companions + 2 for the site
		assertEquals(14, scn.GetTwilight());

		scn.ShadowPlayCard(savage);

		// 2 + 1 for Chaotic Clash + 2 for roaming: site number 5 at site 2
		assertEquals(9, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(2, scn.GetTwilightCost(savage));

		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		scn.FreepsPlayCard(shot);

		// In play the Savage is a minion with a twilight cost of 2, as printed, and is counted.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ACompanionWhoseOwnTextLowersHisCostIsPlayedForLessAndInPlayCostsWhatIsPrintedOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eomer = scn.GetFreepsCard("eomer");
		var rider = scn.GetFreepsCard("rider");
		var surrounded = scn.GetShadowCard("surrounded");
		scn.MoveCompanionsToTable(rider);
		scn.MoveCardsToHand(eomer);
		scn.MoveCardsToSupportArea(surrounded);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		// In hand, with a [rohan] Man to spot, he would cost 3 - 1 to play.
		assertEquals(2, scn.GetTwilightCost(eomer));

		scn.FreepsPlayCard(eomer);
		assertEquals(2, scn.GetTwilight());

		// Surrounded by Wraiths: pay his twilight cost again or he is exhausted.  He is in play, the Rider is
		// still there to spot, and his cost is the 3 printed on him.
		scn.FreepsChooseYes();

		assertEquals(2 + 3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(eomer));
		assertEquals(3, scn.GetTwilightCost(eomer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
