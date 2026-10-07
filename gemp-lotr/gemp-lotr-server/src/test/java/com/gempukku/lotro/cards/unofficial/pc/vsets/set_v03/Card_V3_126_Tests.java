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

public class Card_V3_126_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo (strength 3, vitality 4, resistance 10), who bears The Ruling
	// Ring: strength +1 printed, and a response that puts the Ring on when he is about to take a wound in a
	// skirmish.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");        // Aragorn, Ranger of the North: strength 8, vitality 4; "Maneuver: Exert Aragorn to make him defender +1."
					put("gimli", "1_13");          // Gimli, Son of Gloin: a Dwarf, strength 6, damage +1 in his own text
					put("bilbo", "1_284");         // Bilbo, Retired Adventurer: [shire] ally, strength 2
					put("orophin", "1_56");        // Orophin, Lorien Bowman: Elf ally, an archer in his own text

					// Free Peoples items
					put("anduril", "7_79");        // Anduril, Flame of the West: artifact, strength +2 and vitality +1 printed; "He is damage +1"
					put("bow", "1_90");            // Aragorn's Bow: possession; "He is an archer."
					put("elvenBow", "1_41");       // Elven Bow: possession; "Bearer is an archer."
					put("scroll", "9_36");         // Scroll of Isildur: artifact in the support area, a tale; the Ring-bearer is resistance +1 for each [gondor] artifact
					put("horn", "5_8");            // Horn of Helm: possession in the support area; "Maneuver: Exert a Dwarf companion to ..."
					put("axe", "1_9");             // Dwarven Axe: possession, strength +2 printed; a minion that loses to its bearer costs its owner the top card of his deck
					// A Free Peoples card a companion bears that is no item
					put("alliance", "1_49");       // The Last Alliance of Elves and Men: condition, a tale; bearer is strength +1 for each Elf

					put("witchking", "1_237");     // The Witch-king, Lord of Angmar: [wraith] Nazgul, strength 14
					put("paleBlade", "1_221");     // The Pale Blade: Shadow possession, strength +3 printed; "He is damage +1."
					put("savage", "1_151");        // Uruk Savage: [isengard] Uruk-hai, strength 5
					put("runner1", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("troop", "1_177");         // Goblin Patrol Troop: [moria] Orc, strength 13, no text
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
					put("site8", "7_356");         // Cross Roads: a Shadow ability that needs 2 [raider] Men
					put("site9", "103_126");       // Cracks of Doom
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same Ring-bearer with The One Ring, The Binding Ring 9_1: strength +1 and vitality +1 printed;
	// "Maneuver: Exert bearer to wear The One Ring until the regroup phase. While wearing The One Ring, each
	// time the Ring-bearer is about to take a wound, add a burden instead."
	protected VirtualTableScenario GetBindingRingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("savage", "1_151");        // Uruk Savage: strength 5, vitality 3, damage +1
					// Saruman's Reach: Shadow maneuver event; exert an Uruk-hai to make the opponent exert 2
					// companions or make the Ring-bearer put on The One Ring until the regroup phase.
					put("reach1", "1_137");
					put("reach2", "1_137");
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
					put("site8", "7_356");         // Cross Roads: a Shadow ability that needs 2 [raider] Men
					put("site9", "103_126");       // Cracks of Doom
				}},
				VirtualTableScenario.FOTRFrodo,
				"9_1"
		);
	}

	@Test
	public void CracksofDoomStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Cracks of Doom
		 * Unique: False
		 * Type: Site
		 * Block: King
		 * Site Number: 9
		 * Shadow Number: 9
		 * Direction: Left
		 * Game Text: <b>Mountain</b>. <b>Underground</b>. Game text on Free Peoples items and The One Ring does
		 * 	not apply. Each minion is strength +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(9);

		assertEquals("Cracks of Doom", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.KING, card.getBlueprint().getSiteBlock());
		assertEquals(9, card.getBlueprint().getSiteNumber());
		assertEquals(9, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.MOUNTAIN));
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void EachMinionIsOneStrongerHereAndNobodyElseIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bilbo = scn.GetFreepsCard("bilbo");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveMinionsToTable(savage, runner);

		// At site 8 everyone has the strength printed.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bilbo));
		// 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));

		scn.FreepsPass();
		assertEquals("Cracks of Doom", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Minions of two cultures: 5 + 1
		assertEquals(6, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(runner));
		// A minion that comes into play after the fellowship arrived: 13 + 1
		scn.MoveMinionsToTable(troop);
		assertEquals(14, scn.GetStrength(troop));

		// A companion and an ally are as they were, and the Ring-bearer keeps the +1 printed on the Ring.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetStrength(bilbo));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void HereTheTextOfFreePeoplesItemsGivesNothingAndEveryOtherCardsTextStillApplies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var orophin = scn.GetFreepsCard("orophin");
		var anduril = scn.GetFreepsCard("anduril");
		var bow = scn.GetFreepsCard("bow");
		var elvenBow = scn.GetFreepsCard("elvenBow");
		var scroll = scn.GetFreepsCard("scroll");
		var alliance = scn.GetFreepsCard("alliance");

		var witchking = scn.GetShadowCard("witchking");
		var paleBlade = scn.GetShadowCard("paleBlade");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bilbo, orophin);
		// An artifact and a possession on a companion (the rig puts the Bow beside Anduril, whose text
		// would not let him bear it), a possession on an ally (the rig puts the Elven Bow on a Hobbit) and
		// an artifact in the support area. With them a condition on the same companion.
		scn.AttachCardsTo(aragorn, anduril, bow, alliance);
		scn.AttachCardsTo(bilbo, elvenBow);
		scn.MoveCardsToSupportArea(scroll);
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, paleBlade);

		// Site 8: every text applies.
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(aragorn, Keyword.DAMAGE));        // Anduril
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));        // Aragorn's Bow
		assertTrue(scn.HasKeyword(bilbo, Keyword.ARCHER));          // Elven Bow
		assertTrue(scn.HasKeyword(scroll, Keyword.TALE));           // the Scroll's own keyword
		// 10 + 2 for the Scroll: Anduril and the Scroll itself are [gondor] artifacts
		assertEquals(12, scn.GetResistance(frodo));
		// 8 + 2 printed on Anduril + 1 for The Last Alliance (Orophin is the one Elf)
		assertEquals(11, scn.GetStrength(aragorn));
		// 4 + 1 printed on Anduril
		assertEquals(5, scn.GetVitality(aragorn));
		assertTrue(scn.HasKeyword(alliance, Keyword.TALE));
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(witchking, Keyword.DAMAGE));      // The Pale Blade
		// 14 + 3 printed on The Pale Blade
		assertEquals(17, scn.GetStrength(witchking));

		scn.FreepsPass();
		assertEquals("Cracks of Doom", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The text of the Free Peoples items gives nothing: borne by a companion, borne by an ally, in the
		// support area; artifact and possession.
		assertFalse(scn.HasKeyword(aragorn, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(bilbo, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(scroll, Keyword.TALE));
		assertEquals(10, scn.GetResistance(frodo));

		// What is printed on an item beside its text is still given, and so is the condition's +1:
		// 8 + 2 + 1, and 4 + 1.
		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetVitality(aragorn));
		assertTrue(scn.HasKeyword(alliance, Keyword.TALE));
		// Characters keep their own text.
		assertTrue(scn.HasKeyword(gimli, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(orophin, Keyword.ARCHER));
		// A Shadow item keeps its text: 14 + 3 for the Blade + 1 for the site.
		assertTrue(scn.HasKeyword(witchking, Keyword.DAMAGE));
		assertEquals(18, scn.GetStrength(witchking));

		// Nothing left play on the way.
		assertAttachedTo(anduril, aragorn);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(elvenBow, bilbo);
		assertInZone(Zone.SUPPORT, scroll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnItemsSpecialAbilityIsOfferedAtSite8AndNotHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var horn = scn.GetFreepsCard("horn");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(horn);
		scn.MoveMinionsToTable(troop);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(horn));
		assertTrue(scn.FreepsActionAvailable(aragorn));

		// Nobody is assigned, and the fellowship moves again with the minion still in play.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Cracks of Doom", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// Gimli could exert for it, and Aragorn's own maneuver ability is on offer.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, horn);
		assertFalse(scn.FreepsActionAvailable(horn));
		assertTrue(scn.FreepsActionAvailable(aragorn));
	}

	@Test
	public void AnItemsTriggerHappensAtSite8AndNotHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		// Two cards of the Shadow player's to lie on top of his draw deck.
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveMinionsToTable(runner1);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		scn.MoveCardsToTopOfDeck(troop);
		assertInZone(Zone.DECK, troop);
		// 6 + 2 printed on the Axe, against 5
		assertEquals(8, scn.GetStrength(gimli));
		scn.FreepsAssignAndResolve(gimli, runner1);
		scn.PassSkirmishActions();
		// The wounds for losing and the Axe's trigger answer the same loss: the Free Peoples player
		// says which comes first.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();

		// The Runner lost to the Axe's bearer: its owner discards the top card of his draw deck.
		assertInDiscard(runner1);
		assertInDiscard(troop);

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Cracks of Doom", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveMinionsToTable(runner2);
		scn.MoveCardsToTopOfDeck(savage);

		scn.SkipToAssignments();
		// Still 6 + 2 printed on the Axe, against 5 + 1 for the site
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(runner2));
		assertEquals(savage, scn.GetShadowTopOfDeck());
		scn.FreepsAssignAndResolve(gimli, runner2);
		scn.PassSkirmishActions();

		// This Runner lost to him too: nothing answers the loss but its wounds, and nothing was discarded
		// from the deck.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertInDiscard(runner2);
		assertInZone(Zone.DECK, savage);
		assertEquals(savage, scn.GetShadowTopOfDeck());
		// The Ring-bearer has come through the skirmishes at site 9.
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void TheRulingRingsResponseIsOfferedAtSite8AndNotHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var runner = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(runner);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SkipToAssignments();
		// 3 + 1 printed on the Ring, against 5
		assertEquals(4, scn.GetStrength(frodo));
		scn.FreepsAssignAndResolve(frodo, runner);
		scn.PassSkirmishActions();

		// About to take the wound for losing, he is offered the Ring.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Cracks of Doom", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.SkipToAssignments();
		// Still 3 + 1 printed on the Ring, against 5 + 1 for the site
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(runner));
		scn.FreepsAssignAndResolve(frodo, runner);
		scn.PassSkirmishActions();

		// Nothing was offered: he took the wound, and the game went on to its end.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.RBWearingOneRing());
		assertTrue(scn.GameIsFinished());
	}

	@Test
	public void TheBindingRingsManeuverAbilityIsOfferedAtSite8AndNotHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBindingRingScenario();

		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(savage);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(ring));

		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals("Cracks of Doom", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// He could exert for it: no wound, and still the vitality 4 + 1 printed on the Ring.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(5, scn.GetVitality(frodo));
		assertFalse(scn.FreepsActionAvailable(ring));
	}

	@Test
	public void WoundsToARingBearerWearingTheRingBecomeBurdensAtSite8AndStayWoundsHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBindingRingScenario();

		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var reach1 = scn.GetShadowCard("reach1");
		var reach2 = scn.GetShadowCard("reach2");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(savage);
		scn.MoveCardsToHand(reach1);

		scn.FreepsPass();
		assertEquals(8, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// Frodo is alone and cannot exert 2 companions: Saruman's Reach puts the Ring on him.
		assertFalse(scn.RBWearingOneRing());
		scn.ShadowPlayCard(reach1);
		assertTrue(scn.RBWearingOneRing());

		scn.PassManeuverActions();
		scn.PassArcheryActions();
		scn.PassAssignmentActions();
		// 3 + 1 printed on the Ring against 5: Frodo loses, and the Savage is damage +1.
		scn.FreepsAssignAndResolve(frodo, savage);
		scn.PassSkirmishActions();

		// Site 8: each of the 2 wounds became a burden.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());

		scn.SkipToMovementDecision();
		assertFalse(scn.RBWearingOneRing());
		scn.FreepsChooseToMove();
		assertEquals("Cracks of Doom", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveCardsToHand(reach2);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(reach2);
		// He wears the Ring here as well: that is the event's doing and not the Ring's text.
		assertTrue(scn.RBWearingOneRing());

		scn.PassManeuverActions();
		scn.PassArcheryActions();
		scn.PassAssignmentActions();
		// 3 + 1 printed on the Ring against 5 + 1 for the site: he loses and is not overwhelmed.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(6, scn.GetStrength(savage));
		scn.FreepsAssignAndResolve(frodo, savage);
		scn.PassSkirmishActions();

		// Here the 2 wounds are wounds, and no burden was added.
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.GameIsFinished());
	}
}
