package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tidings", "13_122");
					put("theoden", "4_292");    // Théoden, Son of Thengel: [rohan] companion, strength 6, vitality 2; a regroup ability used only on purpose
					// Éomer, Sister-son of Théoden: [rohan] companion; a cost discount, and a start-of-turn heal of a
					// [rohan] ally that is withheld with no wounded ally in play
					put("eomer", "4_266");
					// Théodred, Second Marshal of the Mark: [rohan] companion, strength 6, vitality 3; a cost discount, and an
					// optional exertion of the minions at the start of each of his own skirmishes
					put("theodred", "13_138");
					// Éowyn, Sister-daughter of Théoden: [rohan] companion, strength 6, vitality 3; Valiant, and a response
					// only while she is exhausted
					put("eowyn", "4_271");

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("brigand2", "4_10");
					// Orkish Skirmisher: [orc] Orc, strength 9, vitality 3; each time he wins a skirmish, he may exert
					// twice to wound a companion
					put("skirmisher", "18_90");
					put("marksman", "1_176");   // Goblin Marksman: [moria] Orc archer, strength 7, vitality 1; Archer is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BitterTidingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Bitter Tidings
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If Théoden is about to take a wound in a skirmish, you may discard from play Éomer or Théodred to prevent that.<br>If Théodred is about to take a wound in a skirmish, you may discard from play Éomer or Éowyn to prevent that.
		*/

		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");

		assertEquals("Bitter Tidings", tidings.getBlueprint().getTitle());
		assertNull(tidings.getBlueprint().getSubtitle());
		assertFalse(tidings.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, tidings.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, tidings.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, tidings.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(tidings, Timeword.RESPONSE));
		assertEquals(2, tidings.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsEomerOrTheodredToPreventTheWoundTheodenTakesForLosingASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var theodred = scn.GetFreepsCard("theodred");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(theoden, eomer, theodred, eowyn);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, brigand);

		// Théoden, 6, loses to the Brigand, 8 (not double his strength): one wound, about to be taken.
		scn.FreepsResolveSkirmish(theoden);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertInHand(tidings);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tidings));
		scn.FreepsChooseOptionalTrigger(tidings);

		// Éomer and Théodred can pay; Éowyn, of the other sentence's cost, cannot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, theodred));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(eowyn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eomer);

		assertInDiscard(eomer, tidings);
		assertInZone(Zone.FREE_CHARACTERS, theodred, eowyn);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void WithoutEomerTheodredIsDiscardedToPreventTheWoundOnTheoden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(theoden, theodred, eowyn);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, brigand);

		scn.FreepsResolveSkirmish(theoden);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tidings));
		scn.FreepsChooseOptionalTrigger(tidings);

		// Théodred is the only card that can pay, and is discarded unasked.
		assertInDiscard(theodred, tidings);
		assertInZone(Zone.FREE_CHARACTERS, eowyn);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void PreventsAWoundTheodenTakesInAnotherCompanionsSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(theoden, eomer, eowyn);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var skirmisher = scn.GetShadowCard("skirmisher");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{theoden, brigand}, new PhysicalCardImpl[]{eowyn, skirmisher});

		// The Skirmisher, 9, beats Éowyn, 6, and exerts twice to wound Théoden, whose own skirmish is still to come.
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowChooseOptionalTrigger(skirmisher);
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		scn.ShadowChooseCard(theoden);

		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tidings));
		scn.FreepsChooseOptionalTrigger(tidings);

		assertInDiscard(eomer, tidings);
		assertEquals(0, scn.GetWoundsOn(theoden));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedForAnArcheryWoundOnTheodenNorForAWoundOnAnotherRohanCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var theodred = scn.GetFreepsCard("theodred");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(theoden, eomer, theodred, eowyn);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var marksman = scn.GetShadowCard("marksman");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman, brigand);
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(theoden));

		// The Marksman's archery wound, placed by the Free Peoples player on Théoden: the archery phase is no skirmish.
		scn.FreepsChooseCard(theoden);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tidings));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertInHand(tidings);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, brigand}, new PhysicalCardImpl[]{eomer, marksman});

		// Éowyn, a [rohan] companion who is not Théoden, loses to the Brigand: Éomer and Théodred could still pay.
		scn.FreepsResolveSkirmish(eowyn);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tidings));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertInHand(tidings);
		assertInZone(Zone.FREE_CHARACTERS, eomer, theodred);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedForTheodenWithNeitherEomerNorTheodredToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var theoden = scn.GetFreepsCard("theoden");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(theoden, eowyn);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, brigand);

		// Éowyn, another [rohan] companion, could be discarded, but is not one the text names.
		scn.FreepsResolveSkirmish(theoden);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tidings));
		assertEquals(1, scn.GetWoundsOn(theoden));
		assertInHand(tidings);
		assertInZone(Zone.FREE_CHARACTERS, eowyn);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void DiscardsEomerOrEowynToPreventTheWoundTheodredTakesForLosingASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var theodred = scn.GetFreepsCard("theodred");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(theoden, eomer, theodred, eowyn);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theodred, brigand);

		scn.FreepsResolveSkirmish(theodred);
		// Théodred's own start-of-skirmish exertion is not offered: the Brigand, vitality 1, cannot exert.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(theodred));
		scn.PassSkirmishActions();

		// Théodred, 6, loses to the Brigand, 8.
		assertEquals(0, scn.GetWoundsOn(theodred));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tidings));
		scn.FreepsChooseOptionalTrigger(tidings);

		// Éomer and Éowyn can pay; Théoden, of the other sentence's cost, cannot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, eowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(theoden));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(eowyn);

		assertInDiscard(eowyn, tidings);
		assertInZone(Zone.FREE_CHARACTERS, eomer, theoden);
		assertEquals(0, scn.GetWoundsOn(theodred));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void WithoutEowynEomerIsDiscardedToPreventTheWoundOnTheodred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var theodred = scn.GetFreepsCard("theodred");
		scn.MoveCompanionsToTable(theoden, eomer, theodred);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theodred, brigand);

		scn.FreepsResolveSkirmish(theodred);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(theodred));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tidings));
		scn.FreepsChooseOptionalTrigger(tidings);

		// Éomer is the only card that can pay, and is discarded unasked.
		assertInDiscard(eomer, tidings);
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertEquals(0, scn.GetWoundsOn(theodred));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void IsNotOfferedForAnArcheryWoundOnTheodred() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var eomer = scn.GetFreepsCard("eomer");
		var theodred = scn.GetFreepsCard("theodred");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eomer, theodred, eowyn);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var marksman = scn.GetShadowCard("marksman");

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(theodred));

		// The Marksman's archery wound on Théodred, with Éomer and Éowyn able to pay: the archery phase is no skirmish.
		scn.FreepsChooseCard(theodred);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tidings));
		assertEquals(1, scn.GetWoundsOn(theodred));
		assertInHand(tidings);
		assertInZone(Zone.FREE_CHARACTERS, eomer, eowyn);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void IsNotOfferedForTheodredWithOnlyTheodenBesideHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tidings = scn.GetFreepsCard("tidings");
		var theoden = scn.GetFreepsCard("theoden");
		var theodred = scn.GetFreepsCard("theodred");
		scn.MoveCompanionsToTable(theoden, theodred);
		scn.MoveCardsToHand(tidings);
		assertInHand(tidings);

		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theodred, brigand);

		// Théodred loses to the Brigand with only Théoden beside him, who pays for the other sentence alone.
		scn.FreepsResolveSkirmish(theodred);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tidings));
		assertEquals(1, scn.GetWoundsOn(theodred));
		assertInHand(tidings);
		assertInZone(Zone.FREE_CHARACTERS, theoden);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}
}
