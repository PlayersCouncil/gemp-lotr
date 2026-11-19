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

public class Card_04_237_Tests
{

	// Every printed site 7, 8 and 9 of the Towers block has a Shadow number of 6 or more, and a move adds it and one
	// for each companion, so the pool is under 7 after such a move only with Shadow numbers lowered: Frodo and Sam,
	// A Talent for Not Being Seen (-1 while 2 Hobbit companions move) and Bill the Pony on Sam (-1).
	// The Towers path, with Hornburg Armory as site 6 and Wizard's Vale (Shadow number 6) as site 8.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wilderness", "4_237");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Hobbit companion
					put("talent", "1_316");     // A Talent for Not Being Seen: "Each time the fellowship moves, spot 2 Hobbit companions to make the Shadow number -1"
					put("bill", "3_106");       // Bill the Pony: "The Shadow number of each site is -1"; discarded at an underground site
				}},
				new HashMap<>()
				{{
					put("site1", "4_325");
					put("site2", "4_331");
					put("site3", "4_337");
					put("site4", "4_346");
					put("site5", "4_348");
					put("site6", "4_354");      // Hornburg Armory: Shadow number 3, a sanctuary that is not underground (Bill the Pony stays)
					put("site7", "102_71");     // Helm's Tower: Towers site 7, Shadow number 6; its text needs 6 companions
					put("site8", "4_359");      // Wizard's Vale: Towers site 8, Shadow number 6; its text is at the start of a Shadow phase
					put("site9", "4_363");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King path: site 7 is Osgiliath Channel 8_120, a King-block site 7 of Shadow number 6.
	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wilderness", "4_237");
					put("sam", "1_311");
					put("talent", "1_316");
					put("bill", "3_106");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wilderness", "4_237");
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, a Southron; strength 5, vitality 3; a regroup ability used only on purpose
					put("explorer2", "4_250");  // a second Southron Explorer, a [raider] Man assigned to no skirmish
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man, an Easterling; strength 6; stronger and fierce only at 2 burdens
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] minion who is a Nazgul; strength 12, fierce; his text needs 3 burdens
					put("brigand", "4_10");     // Dunlending Brigand: a Man minion who is [dunland]; strength 8, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the other player's Man; strength 8
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IthilienWildernessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ithilien Wilderness
		 * Unique: True
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>At the end of each phase during which the fellowship has moved to 7[T], 8[T], or 9[T] and the twilight pool has fewer than 7 twilight tokens, you may add (8).<br><b>Skirmish:</b> Discard this condition to make a [raider] Man strength +2.
		*/

		var scn = GetSkirmishScenario();

		var wilderness = scn.GetFreepsCard("wilderness");

		assertEquals("Ithilien Wilderness", wilderness.getBlueprint().getTitle());
		assertNull(wilderness.getBlueprint().getSubtitle());
		assertTrue(wilderness.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, wilderness.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, wilderness.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, wilderness.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(wilderness, Keyword.SUPPORT_AREA));
		assertEquals(0, wilderness.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtTheEndOfTheFellowshipPhaseThatMovedToSite7TWithSixTwilightItAddsEight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var wilderness = scn.GetShadowCard("wilderness");
		var sam = scn.GetFreepsCard("sam");
		var talent = scn.GetFreepsCard("talent");
		var bill = scn.GetFreepsCard("bill");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(wilderness, talent);
		scn.AttachCardsTo(sam, bill);
		assertInZone(Zone.SUPPORT, wilderness);
		assertInZone(Zone.SUPPORT, talent);
		assertInZone(Zone.ATTACHED, bill);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		// 0 + 6 for Helm's Tower - 1 - 1 + 2 companions.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wilderness));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(14, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, wilderness);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSevenTwilightAtTheEndOfThePhaseItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var wilderness = scn.GetShadowCard("wilderness");
		var sam = scn.GetFreepsCard("sam");
		var talent = scn.GetFreepsCard("talent");
		var bill = scn.GetFreepsCard("bill");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(wilderness, talent);
		scn.AttachCardsTo(sam, bill);
		assertInZone(Zone.SUPPORT, wilderness);
		scn.SetTwilight(1);
		assertEquals(1, scn.GetTwilight());

		// 1 + 6 - 1 - 1 + 2.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wilderness));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DeclinedItIsNotOfferedAfterAShadowPhaseWithNoMoveAndIsOfferedAgainAfterARegroupMoveToSite8T() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var wilderness = scn.GetShadowCard("wilderness");
		var sam = scn.GetFreepsCard("sam");
		var talent = scn.GetFreepsCard("talent");
		var bill = scn.GetFreepsCard("bill");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(wilderness, talent);
		scn.AttachCardsTo(sam, bill);
		assertInZone(Zone.SUPPORT, wilderness);
		scn.SetTwilight(0);

		scn.FreepsPass();
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wilderness));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(6, scn.GetTwilight());

		// The Shadow phase ends with 6 in the pool at site 7T, but the fellowship did not move during it.
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPass();
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wilderness));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// A second move, in the regroup phase: 0 + 6 for Wizard's Vale - 1 - 1 + 2.
		scn.SkipToMovementDecision();
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsChooseToMove();
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(wilderness));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedAtTheEndOfARegroupPhaseInWhichTheFellowshipStayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var wilderness = scn.GetShadowCard("wilderness");
		var sam = scn.GetFreepsCard("sam");
		var talent = scn.GetFreepsCard("talent");
		var bill = scn.GetFreepsCard("bill");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(wilderness, talent);
		scn.AttachCardsTo(sam, bill);
		assertInZone(Zone.SUPPORT, wilderness);
		scn.SetTwilight(0);

		scn.FreepsPass();
		assertEquals(6, scn.GetTwilight());
		scn.ShadowDeclineOptionalTrigger();
		scn.ShadowPass();

		// The fellowship moved this turn, and is at site 7T with 6 in the pool, but not during this regroup phase.
		scn.SkipToMovementDecision();
		assertEquals(6, scn.GetTwilight());
		scn.FreepsChooseToStay();
		assertEquals(7, scn.FreepsGetSiteNumber());
		assertTrue(scn.FreepsDecisionAvailable("Reconcile"));
		scn.FreepsDeclineReconciliation();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wilderness));
		// Player 2's turn begins.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void AMoveToSite6TIsNotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var wilderness = scn.GetShadowCard("wilderness");
		var sam = scn.GetFreepsCard("sam");
		var talent = scn.GetFreepsCard("talent");
		var bill = scn.GetFreepsCard("bill");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveCardsToSupportArea(wilderness, talent);
		scn.AttachCardsTo(sam, bill);
		assertInZone(Zone.SUPPORT, wilderness);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		// 0 + 3 for Hornburg Armory - 1 - 1 + 2: under 7, at a Towers site, but site 6.
		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wilderness));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMoveToASite7OfTheKingBlockIsNotEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var wilderness = scn.GetShadowCard("wilderness");
		var sam = scn.GetFreepsCard("sam");
		var talent = scn.GetFreepsCard("talent");
		var bill = scn.GetFreepsCard("bill");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveCardsToSupportArea(wilderness, talent);
		scn.AttachCardsTo(sam, bill);
		assertInZone(Zone.SUPPORT, wilderness);
		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());

		// 0 + 6 for Osgiliath Channel - 1 - 1 + 2: under 7, at a site 7, but of the King block.
		scn.FreepsPass();
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(6, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(wilderness));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsItToMakeARaiderManStrengthPlus2ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var wilderness = scn.GetShadowCard("wilderness");
		var explorer = scn.GetShadowCard("explorer");
		var axeman = scn.GetShadowCard("axeman");
		var explorer2 = scn.GetShadowCard("explorer2");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(wilderness);
		scn.MoveMinionsToTable(explorer, axeman, khamul, brigand, explorer2);
		assertInZone(Zone.SUPPORT, wilderness);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not an assignment action.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wilderness));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, explorer }, new PhysicalCardImpl[] { gimli, axeman });
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(wilderness));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(6, scn.GetStrength(axeman));

		assertInZone(Zone.SUPPORT, wilderness);
		scn.ShadowUseCardAction(wilderness);
		assertInZone(Zone.DISCARD, wilderness);
		// The Southron in this skirmish, the Easterling waiting for the next and a second Southron assigned to no
		// skirmish; not the [raider] Nazgul, the [dunland] Man or the other player's Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, axeman, explorer2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(axeman);
		assertEquals(8, scn.GetStrength(axeman));
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(5, scn.GetStrength(explorer2));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));

		// Aragorn (8) beats the Southron (5). In the Easterling's own skirmish the bonus is gone.
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(6, scn.GetStrength(axeman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityWithNoRaiderManInPlayDiscardsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var wilderness = scn.GetShadowCard("wilderness");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveCardsToSupportArea(wilderness);
		scn.MoveMinionsToTable(khamul, brigand);
		assertInZone(Zone.SUPPORT, wilderness);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, brigand }, new PhysicalCardImpl[] { gimli, khamul });
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// Its cost is the condition itself, so it is offered with no [raider] Man to make stronger.
		assertInZone(Zone.SUPPORT, wilderness);
		assertTrue(scn.ShadowActionAvailable(wilderness));
		scn.ShadowUseCardAction(wilderness);

		assertInZone(Zone.DISCARD, wilderness);
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
