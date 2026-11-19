package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_020_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cones", "33_20");
					// A second copy, which the first copy's use leaves in play
					put("cones2", "33_20");
					// Gandalf, The Grey: [gandalf] Wizard companion; his wound on a [gandalf] event is never set off
					// (no event is played), his Fellowship ability never used
					put("gandalf", "30_27");
					// Ent Avenger: a [gandalf] companion who is no Gandalf (the Hobbit sets print none); strength +2
					// only beside 3 Ents
					put("avenger", "6_27");
					// Saruman, The White: a [gandalf] Wise Wizard ally who is no Gandalf; his abilities are never used
					put("saruman", "32_24");
					// Free Peoples skirmish events of two cultures, in hand only.
					put("takeup", "30_31");     // Take up arms. Fight!: [gandalf]
					put("fury", "30_3");        // Battle Fury: [dwarven]
					// Noble intentions: a [dwarven] skirmish event, in the other player's hand only
					put("noble", "30_14");
					// Rise of the Necromancer: a [sauron] skirmish event, a Shadow one, in hand only
					put("rise", "33_51");
					// He gives me courage: a [gandalf] maneuver event, in hand only
					put("courage", "30_30");
					// Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("troop", "1_177");
					// Bill Ferny Sr.: [gundabad] Man minion, vitality 2; may not be assigned by the Free Peoples
					// player; a Regroup ability never reached
					put("ferny", "33_22");
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BurningFirConesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Burning Fir Cones
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: <b>Archery:</b> Spot Gandalf and add (3) to reveal your hand. Wound a minion for each Free Peoples skirmish event revealed. Discard this possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cones");

		assertEquals("Burning Fir Cones", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ArcheryActionAdds3RevealsTheHandAndWoundsAMinionForEachOfTwoFreePeoplesSkirmishEvents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cones = scn.GetFreepsCard("cones");
		var cones2 = scn.GetFreepsCard("cones2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var takeup = scn.GetFreepsCard("takeup");
		var fury = scn.GetFreepsCard("fury");
		var rise = scn.GetFreepsCard("rise");
		var courage = scn.GetFreepsCard("courage");
		var noble = scn.GetShadowCard("noble");
		var troop = scn.GetShadowCard("troop");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(cones, cones2);
		// Two Free Peoples skirmish events, beside a Shadow skirmish event and a Free Peoples maneuver event.
		scn.MoveCardsToHand(takeup, fury, rise, courage);
		// A Free Peoples skirmish event in the other player's hand is not in "your hand".
		scn.MoveCardsToHand(noble);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, ferny);
		scn.SkipToPhase(Phase.ARCHERY);
		assertInZone(Zone.SUPPORT, cones, cones2);
		assertInZone(Zone.HAND, takeup, fury, rise, courage, noble);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		int twilight = scn.GetTwilight();

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(cones));
		scn.FreepsUseCardAction(cones);
		assertEquals(twilight + 3, scn.GetTwilight());

		// The hand is shown to the other player, and its owner is shown nothing.
		assertEquals(4, scn.ShadowGetTotalRevealedCardCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(takeup, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(rise, false));
		assertTrue(scn.ShadowHasCardChoiceAvailable(courage, false));
		assertEquals(0, scn.FreepsGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		// Two wounds, each on a minion of the player's choice, the same one twice (CR 4.2 "for each").
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, ferny));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gandalf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, ferny));
		scn.FreepsChooseCard(troop);

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		// The copy used is discarded, and the other stays.
		assertInDiscard(cones);
		assertInZone(Zone.SUPPORT, cones2);
		assertInZone(Zone.HAND, takeup, fury, rise, courage, noble);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithOneFreePeoplesSkirmishEventOneWoundKillsAnExhaustedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cones = scn.GetFreepsCard("cones");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fury = scn.GetFreepsCard("fury");
		var courage = scn.GetFreepsCard("courage");
		var troop = scn.GetShadowCard("troop");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(cones);
		scn.MoveCardsToHand(fury, courage);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, cones);
		scn.MoveMinionsToTable(troop, ferny);
		// Vitality 2 with 1 wound: one wound more kills him.
		scn.AddWoundsToChar(ferny, 1);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(ferny));
		assertInZone(Zone.HAND, fury, courage);

		scn.FreepsUseCardAction(cones);
		scn.ShadowDismissRevealedCards();
		// The exhausted minion is offered for the wound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, ferny));
		scn.FreepsChooseCard(ferny);

		assertInDiscard(ferny);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(cones);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesSkirmishEventInHandItIsPaidForAndWoundsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cones = scn.GetFreepsCard("cones");
		var gandalf = scn.GetFreepsCard("gandalf");
		var rise = scn.GetFreepsCard("rise");
		var courage = scn.GetFreepsCard("courage");
		var troop = scn.GetShadowCard("troop");
		var ferny = scn.GetShadowCard("ferny");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(cones);
		scn.MoveCardsToHand(rise, courage);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, cones);
		scn.MoveMinionsToTable(troop, ferny);
		scn.SkipToPhase(Phase.ARCHERY);
		assertInZone(Zone.HAND, rise, courage);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(ferny));
		int twilight = scn.GetTwilight();

		assertTrue(scn.FreepsActionAvailable(cones));
		scn.FreepsUseCardAction(cones);
		assertEquals(twilight + 3, scn.GetTwilight());
		assertEquals(2, scn.ShadowGetTotalRevealedCardCount());
		scn.ShadowDismissRevealedCards();

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(ferny));
		assertInDiscard(cones);
		assertInZone(Zone.HAND, rise, courage);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryActionIsNotOfferedWithoutGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cones = scn.GetFreepsCard("cones");
		var avenger = scn.GetFreepsCard("avenger");
		var saruman = scn.GetFreepsCard("saruman");
		var fury = scn.GetFreepsCard("fury");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveAlliesToTable(saruman);
		scn.MoveCardsToSupportArea(cones);
		scn.MoveCardsToHand(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);
		assertInZone(Zone.SUPPORT, cones);
		assertInZone(Zone.HAND, fury);

		// A [gandalf] companion and a [gandalf] Wizard, neither of them Gandalf.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(cones));
	}
}
