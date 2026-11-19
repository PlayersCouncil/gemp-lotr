package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_019_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hides", "4_19");
					put("hides2", "4_19");

					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					// Saruman, Rabble-rouser: a [dunland] Wizard, strength 8, vitality 4.  Each time the fellowship
					// moves the Free Peoples player exerts a companion for each [dunland] Man: he is laid after the move.
					put("saruman", "4_33");
					// Easterling Axeman: a [raider] Man, strength 6, vitality 2; strength +3 and fierce only at 2 burdens
					put("axeman", "4_224");
					// Saruman's Staff: on Saruman, fierce and damage +1; its maneuver ability lifts his bar on being
					// assigned to a skirmish, so that he is a [dunland] minion in a skirmish
					put("staff", "4_174");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("sword", "1_112");      // Ranger's Sword: on Aragorn, strength +2 and damage +1
					// Defend It and Hope: Maneuver event, cost 0: exert a [gondor] Man to wound a minion once (twice
					// an Uruk-hai, and none is laid here)
					put("defend1", "4_115");
					put("defend2", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HidesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hides
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Plays to your support area. When you play this possession, you may draw a card.
		 * <b>Response:</b> If a [dunland] Man is about to take a wound, remove (2) or discard this possession to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hides");

		assertEquals("Hides", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenPlayedItMayDrawACardAndASecondCopyPlayedMakesOnlyItsOwnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hides = scn.GetShadowCard("hides");
		var hides2 = scn.GetShadowCard("hides2");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(hides, hides2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The Savage lies under the Brigand, which is the next card drawn.
		scn.MoveCardsToTopOfDeck(savage, brigand);
		scn.ShadowRefreshDecision();
		int deck = scn.GetShadowDeckCount();
		assertEquals(2, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(hides));

		scn.ShadowPlayCard(hides);
		assertInZone(Zone.SUPPORT, hides);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		scn.ShadowAcceptOptionalTrigger();

		// The top card is drawn, and no other.
		assertInHand(brigand, hides2);
		assertInZone(Zone.DECK, savage);
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second copy's play is answered by that copy alone; the first, in play, makes no offer of its own.
		scn.ShadowPlayCard(hides2);
		assertInZone(Zone.SUPPORT, hides2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hides));
		scn.ShadowDeclineOptionalTrigger();

		// Declined: nothing drawn.
		assertInZone(Zone.DECK, savage);
		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PreventsAWoundToADunlandManByRemovingTwoOrByDiscardingItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2);

		var hides = scn.GetShadowCard("hides");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(hides);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		assertEquals(0, scn.GetWoundsOn(brigand));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(brigand);

		// The Brigand is about to take the wound.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		scn.ShadowChooseOptionalTrigger(hides);
		assertEquals(List.of("Remove (2)", "Discard this possession"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Remove (2)");

		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, hides);
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// A [dunland] Man of another title, the Savage, is about to take a wound, and Hides goes this time.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(savage);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		scn.ShadowChooseOptionalTrigger(hides);
		assertEquals(List.of("Remove (2)", "Discard this possession"), scn.ShadowGetMultipleChoices());
		scn.ShadowChoose("Discard this possession");

		assertInDiscard(hides);
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithLessThanTwoInThePoolHidesIsDiscardedWithNoQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		var hides = scn.GetShadowCard("hides");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(hides);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(1);

		assertEquals(0, scn.GetWoundsOn(brigand));
		// The Brigand is the only minion: the wound goes to him unasked.
		scn.FreepsPlayCard(defend1);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		scn.ShadowChooseOptionalTrigger(hides);

		// (2) cannot be removed from a pool of 1: the discard is paid, with no choice asked.
		assertInDiscard(hides);
		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenADunlandMinionWhoIsNoManOrAManOfAnotherCultureIsAboutToTakeAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2);

		var hides = scn.GetShadowCard("hides");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToSupportArea(hides);

		scn.StartGame();
		scn.FreepsPass();
		// Laid after the move, so that Saruman's "each time the fellowship moves" asks nothing.
		scn.MoveMinionsToTable(saruman, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);

		// Saruman: [dunland], and a Wizard.  Hides could be paid for both ways, and is not offered.
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(saruman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(saruman));
		scn.ShadowPass();

		// The Axeman: a Man, and [raider].
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(axeman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(axeman));

		assertInZone(Zone.SUPPORT, hides);
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void OnlyTheWoundItAnswersIsPreventedAndTheSecondWoundOfALostSkirmishIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);

		var hides = scn.GetShadowCard("hides");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(hides);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		scn.SkipToAssignments();
		// Aragorn, 8 + 2, beats the Brigand, 8: two wounds for the sword's damage +1.
		scn.FreepsAssignAndResolve(aragorn, brigand);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(brigand));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		scn.ShadowChooseOptionalTrigger(hides);
		scn.ShadowChoose("Discard this possession");
		assertInDiscard(hides);

		// The first wound is prevented; the second finds no Hides, and the Brigand, of vitality 1, dies of it.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(hides));
		assertInDiscard(brigand);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenSeveralMinionsAreWoundedTogetherInALostSkirmishHidesSavesOnlyTheDunlandManChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var hides = scn.GetShadowCard("hides");
		var brigand = scn.GetShadowCard("brigand");
		var savage = scn.GetShadowCard("savage");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		scn.MoveCardsToSupportArea(hides);

		scn.StartGame();
		scn.FreepsPass();
		// Laid after the move, so that Saruman's "each time the fellowship moves" asks nothing.
		scn.MoveMinionsToTable(brigand, savage, axeman, saruman);
		scn.AttachCardsTo(saruman, staff);
		// Aragorn, 8 + 30, beats the four together, 8 + 11 + 6 + (8 + 2 for the Staff) = 35, and is far from
		// doubling them: nobody is overwhelmed, and the four lose together and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 30));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowAssignToMinions(aragorn, savage, axeman, saruman);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(38, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.PassSkirmishActions();

		// Two [dunland] Men are about to take a wound, beside a Man of another culture and a [dunland] Wizard.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(hides));
		scn.ShadowChooseOptionalTrigger(hides);
		scn.ShadowChoose("Discard this possession");
		assertInDiscard(hides);

		// The wound to be prevented is a [dunland] Man's: either of the two, and not the others.
		assertTrue(scn.ShadowHasCardChoiceAvailable(brigand, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(brigand);

		// That wound alone was prevented.  The Savage, of vitality 1, dies of his own; the others take theirs.
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertInZone(Zone.SHADOW_CHARACTERS, brigand);
		assertInDiscard(savage);
		assertEquals(1, scn.GetWoundsOn(axeman));
		assertEquals(1, scn.GetWoundsOn(saruman));
		// The Staff makes Saruman fierce: the fierce assignments follow.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
