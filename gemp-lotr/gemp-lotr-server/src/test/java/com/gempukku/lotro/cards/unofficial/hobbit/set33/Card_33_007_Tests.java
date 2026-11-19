package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_007_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Turn 1 begins at Bag
	// End (site 1); its maneuver phase is at The Troll Hoard (site 2). Each follower in play makes its Aid offer at
	// the start of the maneuver phase where its cost can be paid; those offers are declined.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hammer", "33_7");

					put("balin", "30_2");           // Balin, Brother of Dwalin: [dwarven] companion; his Fellowship ability not used
					put("thorin", "30_19");         // Thorin, Oakenshield: [dwarven] companion; +1 for each [dwarven] follower he bears
					put("grimir", "1_17");          // Grimir, Dwarven Elder: a [dwarven] ally

					// [dwarven] followers of two races
					put("bifur", "30_5");           // Bifur, Inarticulate: Dwarf; Aid - Exert a companion
					put("roac", "32_8");            // Roäc, Son of Carc: Bird; Aid - Exert Thorin
					// Support-area cards it may not attach, and a [dwarven] follower already borne
					put("thrush", "31_42");         // Old Thrush, Messenger Bird: a [shire] follower
					put("chariot", "33_10");        // Iron Hills Chariot: a [dwarven] support-area possession
					put("dori", "30_8");            // Dori, Really a decent fellow: a [dwarven] follower, cheated onto Thorin

					// Cards for the hand
					put("fury", "30_3");            // Battle Fury: a [dwarven] event
					put("ironforged", "32_18");     // Iron-Forged Weapon: an [esgaroth] possession
					put("wizard", "30_23");         // A Wizard Is Never Late: a [gandalf] event

					put("troop", "1_177");          // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void DwarvenWarhammerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Dwarven Warhammer
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [dwarven] companion.
		 * 	<b>Maneuver:</b> Discard 2 cards from hand to attach a [dwarven] follower from your support area to bearer (without paying the aid cost).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hammer");

		assertEquals("Dwarven Warhammer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnADwarvenCompanionAndOnNoOtherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetFreepsCard("hammer");
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var grimir = scn.GetFreepsCard("grimir");
		var bifur = scn.GetFreepsCard("bifur");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, thorin);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToHand(hammer);

		scn.StartGame();

		assertInZone(Zone.HAND, hammer);
		assertTrue(scn.FreepsPlayAvailable(hammer));

		scn.FreepsPlayCard(hammer);
		// Not the [shire] Ring-bearer, a [dwarven] ally or a [dwarven] follower.
		assertTrue(scn.FreepsHasCardChoiceAvailable(balin, thorin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, grimir, bifur));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(balin);

		assertAttachedTo(hammer, balin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionDiscardsTwoCardsToAttachADwarvenFollowerFromTheSupportAreaToBearerWithoutItsAid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetFreepsCard("hammer");
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var bifur = scn.GetFreepsCard("bifur");
		var roac = scn.GetFreepsCard("roac");
		var thrush = scn.GetFreepsCard("thrush");
		var chariot = scn.GetFreepsCard("chariot");
		var dori = scn.GetFreepsCard("dori");
		var fury = scn.GetFreepsCard("fury");
		var ironforged = scn.GetFreepsCard("ironforged");
		var wizard = scn.GetFreepsCard("wizard");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(balin, thorin);
		scn.AttachCardsTo(balin, hammer);
		scn.AttachCardsTo(thorin, dori);
		scn.MoveCardsToSupportArea(bifur, roac, thrush, chariot);
		scn.MoveCardsToHand(fury, ironforged, wizard);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");

		assertAttachedTo(hammer, balin);
		assertAttachedTo(dori, thorin);
		assertInZone(Zone.SUPPORT, bifur, roac, thrush, chariot);
		assertInZone(Zone.HAND, fury, ironforged, wizard);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		int twilight = scn.GetTwilight();
		assertTrue(scn.FreepsActionAvailable(hammer));

		scn.FreepsUseCardAction(hammer);
		// Any 2 cards of the 3 in hand, whatever their culture or type.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, ironforged, wizard));
		scn.FreepsChooseCards(fury, wizard);
		assertInDiscard(fury, wizard);
		assertInZone(Zone.HAND, ironforged);
		assertEquals(1, scn.GetFreepsHandCount());

		// A [dwarven] follower of either race in the support area; not a [shire] follower, a [dwarven] possession, or a
		// [dwarven] follower another companion bears.
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, roac));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(thrush, chariot, dori));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bifur);

		assertAttachedTo(bifur, balin);
		assertNotAttachedTo(bifur, thorin);
		assertAttachedTo(dori, thorin);
		assertInZone(Zone.SUPPORT, roac, thrush, chariot);
		// Bifur's Aid (exert a companion) is not paid.
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(bilbo));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedWithOneCardInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetFreepsCard("hammer");
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var fury = scn.GetFreepsCard("fury");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, hammer);
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToHand(fury);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");

		assertAttachedTo(hammer, balin);
		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.HAND, fury);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());
		assertFalse(scn.FreepsActionAvailable(hammer));
		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.HAND, fury);
	}

	@Test
	public void WithNoDwarvenFollowerInTheSupportAreaTheTwoCardsAreDiscardedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hammer = scn.GetFreepsCard("hammer");
		var balin = scn.GetFreepsCard("balin");
		var thorin = scn.GetFreepsCard("thorin");
		var thrush = scn.GetFreepsCard("thrush");
		var dori = scn.GetFreepsCard("dori");
		var fury = scn.GetFreepsCard("fury");
		var wizard = scn.GetFreepsCard("wizard");
		scn.MoveCompanionsToTable(balin, thorin);
		scn.AttachCardsTo(balin, hammer);
		scn.AttachCardsTo(thorin, dori);
		scn.MoveCardsToSupportArea(thrush);
		scn.MoveCardsToHand(fury, wizard);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");

		assertAttachedTo(hammer, balin);
		assertAttachedTo(dori, thorin);
		assertInZone(Zone.SUPPORT, thrush);
		assertInZone(Zone.HAND, fury, wizard);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(hammer));

		// Two cards in hand: both go, unasked.
		scn.FreepsUseCardAction(hammer);

		assertInDiscard(fury, wizard);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInZone(Zone.SUPPORT, thrush);
		assertAttachedTo(dori, thorin);
		assertTrue(scn.GetAttachedCards(balin).contains(hammer));
		assertEquals(1, scn.GetAttachedCards(balin).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
