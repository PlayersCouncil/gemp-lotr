package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_303_Tests
{

	// The Towers path, whose sites 1 to 4 are the card's "sites 1[T] to 4[T]" (site 2 Eastfold 4_331, site 4 White
	// Rocks 4_346, site 5 Deeping Wall 4_348: abilities used only on purpose).  Gimli is the Ring-bearer, so that
	// the Frodo who bears the cloak is not, and his skirmish may be cancelled in any format.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "4_303");
					put("frodo", "13_149");     // Frodo, Frenzied Fighter: [shire] Hobbit, Ring-bound, vitality 4; never the Ring-bearer; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, Ring-bound, vitality 4; a fellowship ability used only on purpose

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.GimliRB,  // Gimli, Bearer of Grudges: his burden trigger answers only his own skirmishes
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path: site 2 is Ettenmoors 1_331, whose skirmish ability is used only on purpose.
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "4_303");
					put("frodo", "13_149");     // Frodo, Frenzied Fighter: never the Ring-bearer
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	// Frodo is the Ring-bearer, in the default multipath format, where a skirmish involving him cannot be cancelled.
	protected VirtualTableScenario GetRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "4_303");
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers block format, where a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetTowersBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "4_303");
					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"ttt_block"
		);
	}

	@Test
	public void FrodosCloakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Frodo's Cloak
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Cloak
		 * Game Text: Bearer must be Frodo.
		 * 	<b>Skirmish:</b> At sites 1[T] to 4[T], add a burden and discard this possession to cancel a skirmish involving Frodo. At any other site, discard this possession to remove a burden and heal Frodo.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cloak");

		assertEquals("Frodo's Cloak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.CLOAK));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAFrodoAndGoesOnAFrodoWhoIsNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(cloak, frodo);

		scn.StartGame();

		// Sam, a [shire] Ring-bound Hobbit, and Gimli, the Ring-bearer: no Frodo, no play.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(cloak));

		scn.FreepsPlayCard(frodo);
		assertTrue(scn.FreepsPlayAvailable(cloak));
		scn.FreepsPlayCard(cloak);

		// Only one Frodo can be in play, so the cloak goes on him with nobody else asked about.
		assertAttachedTo(cloak, frodo);
		assertNotAttachedTo(cloak, sam);
		assertNotAttachedTo(cloak, gimli);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSite2ItAddsABurdenAndIsDiscardedToCancelFrodosSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetFreepsCard("frodo");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(frodo);
		scn.AttachCardsTo(frodo, cloak);
		assertAttachedTo(cloak, frodo);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Site 2, the first site at which a skirmish is fought, stands for the low end of "1[T] to 4[T]".
		assertEquals(2, scn.GetCurrentSiteNumber());
		// The assignment phase, next door to the skirmish phase.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cloak));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertAttachedTo(cloak, frodo);
		assertTrue(scn.FreepsActionAvailable(cloak));
		// One action only: the sentence for "any other site" is not on offer here (using the cloak takes the first
		// of its actions, so the result alone could not tell).
		assertEquals(1, scn.FreepsGetActionCount());

		scn.FreepsUseCardAction(cloak);

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(cloak);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertFalse(scn.IsCharSkirmishing(troop1));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtSite4UsedInAnotherCompanionsSkirmishItIsPaidForAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(frodo, sam);
		scn.AttachCardsTo(frodo, cloak);
		assertAttachedTo(cloak, frodo);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		// Site 4, the high end of "1[T] to 4[T]".
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, troop1}, new PhysicalCardImpl[]{frodo, troop2});
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop1));
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(cloak));
		// One action only: site 4 is the last of "sites 1[T] to 4[T]", and the sentence for "any other site" is
		// not on offer beside it.
		assertEquals(1, scn.FreepsGetActionCount());

		scn.FreepsUseCardAction(cloak);

		// Its costs are paid, and Sam's skirmish is no skirmish involving Frodo: it goes on, and Frodo's own,
		// still waiting, is not touched.  At site 4 the other sentence does not apply: no burden removed, no heal.
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(cloak);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop1));
		assertTrue(scn.IsCharAssignedAgainst(frodo, troop2));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtSite5ItIsDiscardedToRemoveABurdenAndHealFrodoAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetFreepsCard("frodo");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(frodo, sam);
		scn.AttachCardsTo(frodo, cloak);
		assertAttachedTo(cloak, frodo);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(sam, 1);
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 5, the first "other site".
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(cloak));

		scn.FreepsUseCardAction(cloak);

		assertInDiscard(cloak);
		assertEquals(1, scn.GetBurdens());
		// One wound off Frodo; none off Sam, nor off Gimli, the Ring-bearer.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(gimli));
		// Nothing is cancelled at site 5.
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtSite2OfTheFellowshipPathItRemovesABurdenAndHealsFrodoAndCancelsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetFreepsCard("frodo");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(frodo);
		scn.AttachCardsTo(frodo, cloak);
		assertAttachedTo(cloak, frodo);

		scn.StartGame();
		scn.AddBurdens(2);
		scn.AddWoundsToChar(frodo, 2);
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// Site 2 of the Fellowship block is no site 2[T] (CR 4.2 "block symbol").
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(2, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(cloak);

		assertInDiscard(cloak);
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InMultipathItIsPaidForAndTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRingBearerScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");
		scn.AttachCardsTo(frodo, cloak);
		assertAttachedTo(cloak, frodo);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(cloak));

		scn.FreepsUseCardAction(cloak);

		// CR 4.2 "Ring-bearer": with Frodo the Ring-bearer, the cloak cannot cancel his skirmish.  Its costs are paid.
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(cloak);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InTheTowersBlockFormatPlayedOnTheRingBearerItCancelsHisSkirmishAtSite4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersBlockScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCardsToHand(cloak);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(cloak));
		scn.FreepsPlayCard(cloak);
		assertAttachedTo(cloak, frodo);

		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsUseCardAction(cloak);

		assertEquals(1, scn.GetBurdens());
		assertInDiscard(cloak);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
