package com.gempukku.lotro.mechanics.costs;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * What a transfer costs.
 *
 * Comprehensive Rules 4.2, "transfer": "You may transfer an artifact or possession between your Free Peoples
 * characters during your fellowship phase by paying the twilight cost for that artifact or possession again", and
 * "Transferring a card is not playing that card, even though you must pay its twilight cost."
 *
 * Comprehensive Rules 4.2, "twilight cost": "Cards already in play that affect another card's twilight cost have
 * that effect only when the card to be modified is coming into play."
 *
 * Ketura, 2026-10-06 (Q-0053): "transferring 'is not playing' and so specifically does not count as 'coming into
 * play' for the purposes of having the twilight cost altered."  So a transfer is paid for at the card's own
 * twilight cost, whatever other cards in play would add to or take from the cost of playing it.
 *
 * Ketura, 2026-10-06 (Q-0074), on a card whose own text changes its cost: "Based on a strict reading of the
 * twilight cost rule, transfers should not take self-discounts into account."  So a transfer costs the number
 * printed on the card and nothing else.
 */
public class TransferCostTests
{
	// Site 1 of the King block sites every scenario gets by default is Edoras Hall (7_330): "The twilight cost
	// of each [rohan] possession is -1."
	protected VirtualTableScenario GetEdorasHallScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider1", "4_286");     // Rider of Rohan: [rohan] Man, cost 2; a skirmish ability that is never used
					put("rider2", "4_286");
					put("spear1", "4_288");     // Rider's Spear: [rohan] possession, hand weapon, cost 1, for a [rohan] Man
					put("spear2", "4_288");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf, vitality 3; an archery ability that is never used
					put("arwen", "3_7");        // Arwen, Elven Rider: an Elf; a response about the Ring-bearer's wounds
					// Asfaloth: [elven] possession, mount, cost 2.  "Bearer must be an Elf.  When played on Arwen,
					// Asfaloth's twilight cost is -2."
					put("asfaloth", "1_31");
					// Heavy Axe: [dwarven] possession, hand weapon, cost 4.  "Bearer must be a Dwarf.  This is twilight
					// cost -1 for each [dwarven] card that has a culture token on it."
					put("axe1", "15_7");
					put("axe2", "15_7");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf; a skirmish ability that is never used
					put("guard", "1_7");        // Dwarf Guard: a Dwarf with no text once in play
					// Chamber of Records: a [dwarven] condition for the support area that holds [dwarven] tokens.
					put("chamber", "15_3");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Rohirrim Road (7_332), the other King block site 1: "Fellowship: Exert an Elf to make the twilight cost of
	// each condition and possession +2 until the end of the turn."
	protected VirtualTableScenario GetRohirrimRoadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rider1", "4_286");     // Rider of Rohan: [rohan] Man, cost 2; a skirmish ability that is never used
					put("rider2", "4_286");
					put("spear1", "4_288");     // Rider's Spear: [rohan] possession, hand weapon, cost 1, for a [rohan] Man
					put("spear2", "4_288");
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf, vitality 3; an archery ability that is never used
					put("arwen", "3_7");        // Arwen, Elven Rider: an Elf; a response about the Ring-bearer's wounds
					// Asfaloth: [elven] possession, mount, cost 2.  "Bearer must be an Elf.  When played on Arwen,
					// Asfaloth's twilight cost is -2."
					put("asfaloth", "1_31");
					// Heavy Axe: [dwarven] possession, hand weapon, cost 4.  "Bearer must be a Dwarf.  This is twilight
					// cost -1 for each [dwarven] card that has a culture token on it."
					put("axe1", "15_7");
					put("axe2", "15_7");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf; a skirmish ability that is never used
					put("guard", "1_7");        // Dwarf Guard: a Dwarf with no text once in play
					// Chamber of Records: a [dwarven] condition for the support area that holds [dwarven] tokens.
					put("chamber", "15_3");
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

	@Test
	public void APossessionMadeDearerByASiteIsPlayedAtTheDearerCostAndTransferredAtItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohirrimRoadScenario();

		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var spear1 = scn.GetFreepsCard("spear1");
		var spear2 = scn.GetFreepsCard("spear2");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(rider1, rider2, legolas);
		scn.AttachCardsTo(rider1, spear1);
		scn.MoveCardsToHand(spear2);

		scn.StartGame();

		assertEquals("Rohirrim Road", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetTwilight());

		// The transfer comes first, so that the play below is seen to be dearer on the very same board.
		assertTrue(scn.FreepsTransferAvailable(spear1));
		scn.FreepsTransferCard(spear1);
		assertAttachedTo(spear1, rider2);
		// The Spear's own 1: it is not coming into play, and the Road's +2 does not reach it.
		assertEquals(1, scn.GetTwilight());

		// The second Spear is played, and costs its 1 + 2 for the Road.  Rider 2 already bears a hand weapon,
		// so it can only go to Rider 1.
		scn.FreepsPlayCard(spear2);
		assertAttachedTo(spear2, rider1);
		assertEquals(1 + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void APossessionMadeCheaperByASiteIsPlayedAtTheCheaperCostAndTransferredAtItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEdorasHallScenario();

		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var spear1 = scn.GetFreepsCard("spear1");
		var spear2 = scn.GetFreepsCard("spear2");
		scn.MoveCompanionsToTable(rider1, rider2);
		scn.AttachCardsTo(rider1, spear1);
		scn.MoveCardsToHand(spear2);

		scn.StartGame();

		assertEquals("Edoras Hall", scn.GetCurrentSite().getBlueprint().getTitle());
		assertEquals(0, scn.GetTwilight());

		assertTrue(scn.FreepsTransferAvailable(spear1));
		scn.FreepsTransferCard(spear1);
		assertAttachedTo(spear1, rider2);
		// The Spear's own 1: it is not coming into play, and the Hall's -1 does not reach it.
		assertEquals(1, scn.GetTwilight());

		// The second Spear is played, and costs its 1 - 1 for the Hall: nothing is added.
		scn.FreepsPlayCard(spear2);
		assertAttachedTo(spear2, rider1);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardCheaperWhenPlayedOnACharacterIsNotCheaperWhenTransferredToHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEdorasHallScenario();

		var asfaloth = scn.GetFreepsCard("asfaloth");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.MoveCardsToHand(asfaloth);

		scn.StartGame();

		// Played on Arwen, Asfaloth costs its 2 - 2.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(asfaloth);
		scn.FreepsChooseCard(arwen);
		assertAttachedTo(asfaloth, arwen);
		assertEquals(0, scn.GetTwilight());

		// Transferred to Legolas it costs its 2, as it would have cost to play on him.
		scn.FreepsTransferCard(asfaloth);
		assertAttachedTo(asfaloth, legolas);
		assertEquals(2, scn.GetTwilight());

		// Transferred back to Arwen it costs its 2 again: "when played on Arwen", and a transfer is not a play.
		scn.FreepsTransferCard(asfaloth);
		assertAttachedTo(asfaloth, arwen);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACardWhoseOwnTextLowersItsCostIsPlayedAtTheLowerCostAndTransferredAtTheCostPrintedOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEdorasHallScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var axe1 = scn.GetFreepsCard("axe1");
		var axe2 = scn.GetFreepsCard("axe2");
		var chamber = scn.GetFreepsCard("chamber");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, axe1);
		scn.MoveCardsToSupportArea(chamber);
		scn.MoveCardsToHand(axe2);

		scn.StartGame();
		// One [dwarven] card with a culture token on it: a Heavy Axe is twilight cost -1.
		scn.AddTokensToCard(chamber, 1);
		assertEquals(0, scn.GetTwilight());

		// The transfer comes first, so that the play below is seen to be cheaper on the very same board.
		assertTrue(scn.FreepsTransferAvailable(axe1));
		scn.FreepsTransferCard(axe1);
		assertAttachedTo(axe1, guard);
		// The 4 printed on it: it is in play, and its own text about its cost does not reach a transfer.
		assertEquals(4, scn.GetTwilight());

		// The second Axe is played, and costs its 4 - 1.  The Guard already bears a hand weapon, so it can
		// only go to Gimli.
		scn.FreepsPlayCard(axe2);
		assertAttachedTo(axe2, gimli);
		assertEquals(4 + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
