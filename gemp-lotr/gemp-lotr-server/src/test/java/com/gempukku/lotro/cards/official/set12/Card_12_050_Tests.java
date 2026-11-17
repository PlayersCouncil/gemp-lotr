package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_050_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guardian", "12_50");
					put("guardian2", "12_50");
					put("guardian3", "12_50");
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; his texts are used only on purpose
					put("ranger", "7_116");     // Ranger of Osgiliath: a [gondor] Man companion, strength 6; a regroup ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man companion, strength 6, Knight his whole text
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3; his skirmish ability is used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: an unbound [dwarven] Dwarf companion; a skirmish ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] ally; a maneuver ability used only on purpose
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally whose home is site 3, where he may skirmish
					put("runnerA", "1_178");    // Goblin Runner: strength 5, vitality 1; cheated in, so its "when you play" never fires
					put("runnerB", "1_178");
					put("runnerC", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GuardianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Guardian
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Toil 2</b>. <helper>(For each [gondor] character you exert when playing this, its twilight cost is -2.)</helper><br><b>Skirmish:</b> If a [gondor] companion is not assigned to a skirmish, discard this condition to have him or her replace an unbound companion skirmishing a minion.
		*/

		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");

		assertEquals("Guardian", guardian.getBlueprint().getTitle());
		assertNull(guardian.getBlueprint().getSubtitle());
		assertFalse(guardian.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, guardian.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, guardian.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, guardian.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(guardian, Keyword.TOIL));
		assertEquals(2, scn.GetKeywordCount(guardian, Keyword.TOIL));
		assertTrue(scn.HasKeyword(guardian, Keyword.SUPPORT_AREA));
		assertEquals(4, guardian.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingOneGondorCompanionForToilTakesTwoOffItsCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guardian = scn.GetFreepsCard("guardian");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(ranger, knight, gimli);
		scn.MoveCardsToHand(guardian);

		scn.StartGame();
		assertInHand(guardian);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(knight));

		assertTrue(scn.FreepsPlayAvailable(guardian));
		scn.FreepsPlayCard(guardian);
		// Toil: only a [gondor] character may be exerted, and none need be.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger, knight));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(0, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(ranger);

		assertEquals(1, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertInZone(Zone.SUPPORT, guardian);
		// 4 - 2 for the one exertion
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsThisCopyToPutAnUnassignedGondorCompanionInPlaceOfTheUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guardian = scn.GetFreepsCard("guardian");
		var guardian2 = scn.GetFreepsCard("guardian2");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(ranger, knight, pippin, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(guardian, guardian2);

		var runnerA = scn.GetShadowCard("runnerA");
		var runnerB = scn.GetShadowCard("runnerB");
		var runnerC = scn.GetShadowCard("runnerC");

		scn.StartGame();
		scn.MoveMinionsToTable(runnerA, runnerB, runnerC);
		assertInZone(Zone.SUPPORT, guardian, guardian2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ranger, runnerA}, new PhysicalCardImpl[]{gimli, runnerB},
				new PhysicalCardImpl[]{knight, runnerC});

		// The Ranger's skirmish first: his 6 beats the Runner's 5.
		scn.FreepsResolveSkirmish(ranger);
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runnerB));
		assertTrue(scn.FreepsActionAvailable(guardian));
		scn.FreepsUseCardAction(guardian);
		assertInZone(Zone.DISCARD, guardian);
		assertInZone(Zone.SUPPORT, guardian2);

		// The Ranger, whose skirmish is over, and Pippin, never assigned: two [gondor] companions of two races.
		// Not the Knight, assigned and waiting; not the [gondor] ally; not Frodo or Gimli, who are no [gondor] cards.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ranger, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, merchant, frodo, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runnerB));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.IsCharAssignedAgainst(knight, runnerC));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// The skirmish goes on with Pippin: his 3 loses to the Runner's 5, and the wound is his, not Gimli's.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void SkirmishAbilityIsOfferedWhenAHobbitIsTheOnlyUnassignedGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(knight, pippin, gimli);
		scn.MoveCardsToSupportArea(guardian);

		var runnerA = scn.GetShadowCard("runnerA");
		var runnerB = scn.GetShadowCard("runnerB");

		scn.StartGame();
		scn.MoveMinionsToTable(runnerA, runnerB);
		assertInZone(Zone.SUPPORT, guardian);

		scn.SkipToAssignments();
		// The Knight, a Man, is assigned and waiting; Pippin, a Hobbit, is the one [gondor] companion left unassigned.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runnerA}, new PhysicalCardImpl[]{knight, runnerB});
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runnerA));
		assertFalse(scn.IsCharAssigned(pippin));

		assertTrue(scn.FreepsActionAvailable(guardian));
		scn.FreepsUseCardAction(guardian);
		assertInZone(Zone.DISCARD, guardian);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runnerA));
		assertFalse(scn.IsCharSkirmishing(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileEveryGondorCompanionIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian");
		var knight = scn.GetFreepsCard("knight");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(guardian);

		var runnerB = scn.GetShadowCard("runnerB");
		var runnerC = scn.GetShadowCard("runnerC");

		scn.StartGame();
		scn.MoveMinionsToTable(runnerB, runnerC);
		assertInZone(Zone.SUPPORT, guardian);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runnerB}, new PhysicalCardImpl[]{knight, runnerC});

		// Unassigned are Frodo, who is no [gondor] card, and the [gondor] ally, who is no companion; the Knight
		// waits for his own skirmish.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, runnerB));
		assertTrue(scn.IsCharAssignedAgainst(knight, runnerC));
		assertFalse(scn.FreepsActionAvailable(guardian));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnAllyOrARingBoundCompanionSkirmishingTheUseReplacesNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guardian = scn.GetFreepsCard("guardian");
		var guardian2 = scn.GetFreepsCard("guardian2");
		var guardian3 = scn.GetFreepsCard("guardian3");
		var sam = scn.GetFreepsCard("sam");
		var knight = scn.GetFreepsCard("knight");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(knight, sam);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(guardian, guardian2, guardian3);

		var runnerA = scn.GetShadowCard("runnerA");
		var runnerB = scn.GetShadowCard("runnerB");
		var runnerC = scn.GetShadowCard("runnerC");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runnerA, runnerB, runnerC);
		assertInZone(Zone.SUPPORT, guardian, guardian2, guardian3);

		// At Rivendell Valley, site 3, Elrond may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elrond, runnerA}, new PhysicalCardImpl[]{frodo, runnerB},
				new PhysicalCardImpl[]{sam, runnerC});

		// An ally skirmishing: the Knight is unassigned, so it is offered and paid for, and nobody is replaced.
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, runnerA));
		assertTrue(scn.FreepsActionAvailable(guardian));
		scn.FreepsUseCardAction(guardian);
		assertInZone(Zone.DISCARD, guardian);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, runnerA));
		assertFalse(scn.IsCharSkirmishing(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Elrond's 8 beats the Runner's 5.
		scn.PassSkirmishActions();

		// The Ring-bearer skirmishing, who is Ring-bound: the second copy is paid for, and nobody is replaced.
		scn.FreepsResolveSkirmish(frodo);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runnerB));
		assertTrue(scn.FreepsActionAvailable(guardian2));
		scn.FreepsUseCardAction(guardian2);
		assertInZone(Zone.DISCARD, guardian2);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, runnerB));
		assertFalse(scn.IsCharSkirmishing(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Frodo's 4 loses to the Runner's 5; the Ruling Ring offers to answer his wound, and is declined.
		scn.PassSkirmishActions();
		scn.FreepsDeclineOptionalTrigger();

		// A Ring-bound companion who is not the Ring-bearer: the third copy is paid for, and nobody is replaced.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runnerC));
		assertTrue(scn.FreepsActionAvailable(guardian3));
		scn.FreepsUseCardAction(guardian3);
		assertInZone(Zone.DISCARD, guardian3);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runnerC));
		assertFalse(scn.IsCharSkirmishing(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
