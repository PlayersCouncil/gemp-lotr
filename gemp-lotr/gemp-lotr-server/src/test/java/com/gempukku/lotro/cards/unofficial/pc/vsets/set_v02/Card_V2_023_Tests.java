package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_023_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fires", "102_23");

					// [isengard] Orcs of vitality 3.  Each has a regroup ability that is only ever used on purpose.
					put("worker", "3_62");       // Isengard Worker: cost 2
					put("servant", "3_58");      // Isengard Servant: cost 3
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("savage", "1_151");      // Uruk Savage: an [isengard] Uruk-hai, vitality 3, damage +1

					// Saruman, Servant of the Eye: an [isengard] Wizard minion with an assignment ability.
					put("saruman", "3_69");
					// Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; his text is about his strength.
					put("gandalf", "1_72");

					// Down to the Last Child: another [isengard] condition that holds [isengard] tokens.  It gains
					// them when a companion loses to an Uruk-hai and spends them in the Shadow phase.
					put("child", "4_148");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FiresofIndustryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Fires of Industry
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: When you play this condition, spot 2 [Isengard] Orcs or a Wizard to add 2 [isengard] tokens here.
		 * At the start of the regroup phase, you may discard this condition (or remove an [isengard] token here), to heal an [Isengard] Orc.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fires");

		assertEquals("Fires of Industry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForOneToTheSupportAreaAndWithOneIsengardOrcAndNoWizardGetsNoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(fires);

		scn.StartGame();

		// One [isengard] Orc, and beside him an Orc of another culture and an [isengard] minion of another race.
		scn.MoveMinionsToTable(worker, troop, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for the site
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.HAND, fires);
		assertTrue(scn.ShadowPlayAvailable(fires));

		scn.ShadowPlayCard(fires);

		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, fires);
		assertEquals(0, scn.GetCultureTokensOn(fires));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedWithTwoIsengardOrcsToSpotItGetsTwoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var child = scn.GetShadowCard("child");
		scn.MoveCardsToHand(fires, child);

		scn.StartGame();

		scn.MoveMinionsToTable(worker, servant, troop, savage);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(fires));
		assertTrue(scn.ShadowPlayAvailable(fires));

		scn.ShadowPlayCard(fires);

		assertInZone(Zone.SUPPORT, fires);
		assertEquals(2, scn.GetCultureTokensOn(fires));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another condition played beside it adds nothing here.
		scn.ShadowPlayCard(child);
		assertInZone(Zone.SUPPORT, child);
		assertEquals(2, scn.GetCultureTokensOn(fires));
		assertEquals(0, scn.GetCultureTokensOn(child));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedWithSarumanToSpotItGetsTwoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var child = scn.GetShadowCard("child");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(fires);
		// Another [isengard] condition that holds [isengard] tokens is in play already: "here" is not there.
		scn.MoveCardsToSupportArea(child);

		scn.StartGame();

		// One [isengard] Orc is not enough by himself; the Wizard is.
		scn.MoveMinionsToTable(worker, saruman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(fires));
		assertEquals(0, scn.GetCultureTokensOn(child));
		assertTrue(scn.ShadowPlayAvailable(fires));

		scn.ShadowPlayCard(fires);

		// Both tokens go on the card just played, and the player is not asked where.
		assertInZone(Zone.SUPPORT, fires);
		assertEquals(2, scn.GetCultureTokensOn(fires));
		assertEquals(0, scn.GetCultureTokensOn(child));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedWithTheOtherPlayersGandalfToSpotItGetsTwoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var worker = scn.GetShadowCard("worker");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCardsToHand(fires);
		// The text says "a Wizard", of no side, culture or type: the fellowship's Gandalf is one.
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();

		scn.MoveMinionsToTable(worker);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(fires));
		assertTrue(scn.ShadowPlayAvailable(fires));

		scn.ShadowPlayCard(fires);

		assertInZone(Zone.SUPPORT, fires);
		assertEquals(2, scn.GetCultureTokensOn(fires));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfRegroupATokenHereIsRemovedToHealAnIsengardOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var child = scn.GetShadowCard("child");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(fires, child);
		scn.AddTokensToCard(fires, 2);
		scn.AddTokensToCard(child, 2);

		scn.StartGame();

		scn.MoveMinionsToTable(worker, servant, troop, savage);
		scn.AddWoundsToChar(worker, 2);
		scn.AddWoundsToChar(servant, 2);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetCultureTokensOn(fires));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fires));

		scn.ShadowAcceptOptionalTrigger();

		// With a token here the token is what is paid: the player is not asked which, and the condition stays.
		assertEquals(1, scn.GetCultureTokensOn(fires));
		assertInZone(Zone.SUPPORT, fires);
		assertEquals(2, scn.GetCultureTokensOn(child));

		// The two [isengard] Orcs; not the [moria] Orc or the [isengard] Uruk-hai, wounded as they are.
		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(worker);

		assertEquals(1, scn.GetWoundsOn(worker));
		assertEquals(2, scn.GetWoundsOn(servant));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetCultureTokensOn(fires));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtTheStartOfRegroupWithNoTokenHereTheConditionIsDiscardedToHealAnIsengardOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var child = scn.GetShadowCard("child");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(fires, child);
		// [isengard] tokens on the table, but none of them here.
		scn.AddTokensToCard(child, 2);

		scn.StartGame();

		scn.MoveMinionsToTable(worker, servant, troop, savage);
		scn.AddWoundsToChar(worker, 2);
		scn.AddWoundsToChar(servant, 2);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetCultureTokensOn(fires));
		assertInZone(Zone.SUPPORT, fires);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fires));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, fires);
		assertInZone(Zone.SUPPORT, child);
		assertEquals(2, scn.GetCultureTokensOn(child));

		assertTrue(scn.ShadowHasCardChoiceAvailable(worker, servant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(servant);

		assertEquals(2, scn.GetWoundsOn(worker));
		assertEquals(1, scn.GetWoundsOn(servant));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLastTokenHereIsRemovedAndAtTheNextRegroupPhaseTheConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(fires);
		scn.AddTokensToCard(fires, 1);

		scn.StartGame();

		// The only wounded [isengard] Orc, so he is healed without a choice being put.
		scn.MoveMinionsToTable(worker);
		scn.AddWoundsToChar(worker, 2);

		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetCultureTokensOn(fires));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fires));
		scn.ShadowAcceptOptionalTrigger();

		// One token is enough to keep the condition.
		assertEquals(0, scn.GetCultureTokensOn(fires));
		assertInZone(Zone.SUPPORT, fires);
		assertEquals(1, scn.GetWoundsOn(worker));

		// The fellowship moves again, and a second regroup phase begins with no token left here.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetCultureTokensOn(fires));
		assertInZone(Zone.SUPPORT, fires);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fires));
		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, fires);
		assertEquals(0, scn.GetWoundsOn(worker));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsOfferedOnlyAtTheStartOfTheRegroupPhaseAndDecliningChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var worker = scn.GetShadowCard("worker");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToSupportArea(fires);
		scn.AddTokensToCard(fires, 2);

		scn.StartGame();

		scn.MoveMinionsToTable(worker, servant);
		scn.AddWoundsToChar(worker, 2);
		scn.AddWoundsToChar(servant, 2);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fires));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fires));

		// Nobody is assigned on this turn, so the assignment phase is the last before the regroup phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fires));

		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fires));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(2, scn.GetCultureTokensOn(fires));
		assertInZone(Zone.SUPPORT, fires);
		assertEquals(2, scn.GetWoundsOn(worker));
		assertEquals(2, scn.GetWoundsOn(servant));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// The start of the phase is past: nothing is offered again while the phase goes on.
		scn.FreepsPass();
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fires));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsOfferedWithNoIsengardOrcToHealAndAcceptedRemovesATokenAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fires = scn.GetShadowCard("fires");
		var worker = scn.GetShadowCard("worker");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(fires);
		scn.AddTokensToCard(fires, 2);

		scn.StartGame();

		// An [isengard] Orc with no wound, and wounds on an Orc of another culture and an [isengard] Uruk-hai.
		scn.MoveMinionsToTable(worker, troop, savage);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 2);

		scn.SkipToPhase(Phase.REGROUP);

		// The offer has a cost, so it is made whenever the cost can be paid, whatever the heal will do.
		assertEquals(2, scn.GetCultureTokensOn(fires));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fires));

		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetCultureTokensOn(fires));
		assertInZone(Zone.SUPPORT, fires);
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
