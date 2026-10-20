package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_261_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ambusher", "1_261");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRoamingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Orc Ambusher: cost 1, site number 6
					put("ambusher1", "1_261");
					put("ambusher2", "1_261");
					put("ambusher3", "1_261");
					// Cirith Ungol Guard: a [sauron] minion who is no Orc (an Uruk-hai), cost 3, site number 6;
					// Damage +1 is its whole text
					put("guard1", "10_81");
					put("guard2", "10_81");
					put("guard3", "10_81");
					// Uruk Savage: an [isengard] minion, cost 2, site number 5; Damage +1 is its whole text
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetResponseScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ambusher1", "1_261");
					put("ambusher2", "1_261");
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] Uruk-hai; Damage +1 is its whole text

					// Aragorn, Ranger of the North: a [gondor] ranger companion, vitality 4; his maneuver ability is
					// used only on purpose
					put("aragorn", "1_89");
					// Pursuit Just Behind: [gondor] Regroup event, "Exert a ranger companion to wound every minion"
					put("pursuit", "1_111");
					// We Must Go Warily: [gondor] Response event, "If the fellowship moves in the regroup phase,
					// exert a [gondor] companion twice to make each minion's twilight cost +1 until the next
					// regroup phase": a Free Peoples event played in the regroup phase that is no regroup event
					put("warily", "3_48");

					// Sword-wall: [elven] event printed "Skirmish or Regroup", "Spot 3 Elf companions to liberate a
					// site or exert a minion"
					put("swordwall", "4_84");
					// Haldir, Elf of the Golden Wood: Elf companion, strength 5; +2 only at sites 6 to 8.  No archer: an
					// archery wound would exhaust the Ambusher before the skirmish
					put("haldir", "1_48");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					put("elf", "1_53");         // Lorien Elf: Elf companion; "To play, spot an Elf", so he is cheated in

					// Coming for the Ring: [isengard] Shadow Regroup event, "Reveal a card at random from the Free
					// Peoples player's hand. Heal X [isengard] minions ..."; not playable with his hand empty (Q-0336),
					// and with no [isengard] minion in play it heals nobody
					put("coming", "3_51");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcAmbusherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Orc Ambusher
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 5
		 * Vitality: 2
		 * Site Number: 6
		 * Game Text: <b>Tracker</b>.<br>The roaming penalty for each [sauron] minion you play is -1.<br><b>Response:</b> If a Free Peoples regroup event is played, exert this minion to cancel that event.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ambusher");

		assertEquals("Orc Ambusher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void TheNextSauronMinionPaysOneLessForRoamingAndTheAmbusherItselfPaysThePenaltyInFull() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var ambusher1 = scn.GetShadowCard("ambusher1");
		var ambusher2 = scn.GetShadowCard("ambusher2");
		var ambusher3 = scn.GetShadowCard("ambusher3");
		var guard1 = scn.GetShadowCard("guard1");
		var guard2 = scn.GetShadowCard("guard2");
		var guard3 = scn.GetShadowCard("guard3");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(ambusher1, ambusher2, ambusher3, guard1, guard2, guard3, savage);

		scn.StartGame();
		scn.SetTwilight(27);
		scn.FreepsPass();

		// Site 2: every minion here is roaming, and the penalty is 2.
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 27 set + 1 companion + 2 for Ettenmoors
		assertEquals(30, scn.GetTwilight());

		scn.ShadowPlayCard(ambusher1);
		// 1 to play + 2 roaming: its own text is not in force while it is being played
		assertEquals(27, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, ambusher1);

		scn.ShadowPlayCard(guard1);
		// 3 to play + 2 roaming - 1 for the Ambusher.  The penalty is less; the minion is no less roaming.
		assertEquals(23, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard1, Keyword.ROAMING));

		scn.ShadowPlayCard(savage);
		// An [isengard] minion: 2 to play + 2 roaming
		assertEquals(19, scn.GetTwilight());

		scn.ShadowPlayCard(ambusher2);
		// 1 to play + 2 roaming - 1 for the first Ambusher, and nothing for itself
		assertEquals(17, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, ambusher2);

		scn.ShadowPlayCard(guard2);
		// Two Ambushers in play: 3 to play + 2 roaming - 2
		assertEquals(14, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard2, Keyword.ROAMING));

		scn.ShadowPlayCard(ambusher3);
		// 1 to play + 2 roaming - 2
		assertEquals(13, scn.GetTwilight());

		scn.ShadowPlayCard(guard3);
		// Three Ambushers in play take the penalty of 2 to nothing and no lower: 3 to play, not 3 - 1
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.HasKeyword(guard3, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, guard3);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASauronMinionWhoIsNotRoamingPaysHisPrintedCostAndNoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRoamingScenario();

		var ambusher1 = scn.GetShadowCard("ambusher1");
		var guard1 = scn.GetShadowCard("guard1");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.FreepsPass();

		// Site 6, the site number of the Cirith Ungol Guard: he pays no roaming penalty for the Ambusher to
		// make less.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.MoveMinionsToTable(ambusher1);
		scn.SetShadowHand(guard1);

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowPlayAvailable(guard1));

		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(guard1));

		scn.ShadowPlayCard(guard1);
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.HasKeyword(guard1, Keyword.ROAMING));
		assertInZone(Zone.SHADOW_CHARACTERS, guard1);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ExertsToCancelAFreePeoplesRegroupEventWhoseCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var ambusher1 = scn.GetShadowCard("ambusher1");
		var ambusher2 = scn.GetShadowCard("ambusher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuit = scn.GetFreepsCard("pursuit");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pursuit);

		scn.StartGame();
		// With no minion in play the game goes from the Shadow phase straight to regroup; the minions are the
		// ones still standing after the skirmishes.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ambusher1, ambusher2);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(ambusher1));
		assertEquals(0, scn.GetWoundsOn(ambusher2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		scn.FreepsPlayCard(pursuit);
		// Aragorn, the only ranger companion, exerts for the event.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Each Ambusher may answer it.
		assertTrue(scn.ShadowActionAvailable(ambusher1));
		assertTrue(scn.ShadowActionAvailable(ambusher2));

		scn.ShadowUseCardAction(ambusher1);
		// The Ambusher used exerts, and the one beside it does not.  Neither is wounded by the event, which
		// would have put a wound on each; Aragorn's exertion stays paid, and the event is discarded.
		assertEquals(1, scn.GetWoundsOn(ambusher1));
		assertEquals(0, scn.GetWoundsOn(ambusher2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(pursuit);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnEventOfSkirmishOrRegroupPlayedInTheRegroupPhaseIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var ambusher1 = scn.GetShadowCard("ambusher1");
		var guard = scn.GetShadowCard("guard");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		var swordwall = scn.GetFreepsCard("swordwall");
		scn.MoveCompanionsToTable(haldir, arwen, elf);
		scn.MoveCardsToHand(swordwall);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ambusher1, guard);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(ambusher1));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// Played in the regroup phase, Sword-wall is a regroup event.
		scn.FreepsPlayCard(swordwall);
		assertTrue(scn.ShadowActionAvailable(ambusher1));

		scn.ShadowUseCardAction(ambusher1);
		// Its effect is not carried out: the Free Peoples player is asked nothing, and the Guard is not exerted.
		assertEquals(1, scn.GetWoundsOn(ambusher1));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnEventOfSkirmishOrRegroupPlayedInASkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var ambusher1 = scn.GetShadowCard("ambusher1");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		var swordwall = scn.GetFreepsCard("swordwall");
		scn.MoveCompanionsToTable(haldir, arwen, elf);
		scn.MoveCardsToHand(swordwall);

		scn.StartGame();
		scn.MoveMinionsToTable(ambusher1);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(haldir, ambusher1);
		scn.FreepsResolveSkirmish(haldir);

		assertEquals(0, scn.GetWoundsOn(ambusher1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Played in a skirmish, Sword-wall is a skirmish event and no regroup event.  The Ambusher, able to
		// exert, is not offered: the event is carried out at once (with no site to liberate, the minion is
		// exerted, and it is the only one) and the Shadow player is asked for skirmish actions.
		scn.FreepsPlayCard(swordwall);
		assertEquals(1, scn.GetWoundsOn(ambusher1));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AFreePeoplesResponseEventPlayedInTheRegroupPhaseIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var ambusher1 = scn.GetShadowCard("ambusher1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var warily = scn.GetFreepsCard("warily");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(warily);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ambusher1);
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsChooseToMove();
		// Still the regroup phase: the move is under way and We Must Go Warily answers it.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(ambusher1));

		scn.FreepsPlayCard(warily);
		// A response event is no regroup event, wherever it is played: the Ambusher is not offered, the event
		// is carried out and the move goes on to the next Shadow phase.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(ambusher1));
		assertInDiscard(warily);
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowRegroupEventIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var ambusher1 = scn.GetShadowCard("ambusher1");
		var coming = scn.GetShadowCard("coming");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCardsToHand(coming, arwen);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ambusher1);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(ambusher1));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());

		scn.ShadowPlayCard(coming);
		scn.ShadowDismissRevealedCards();
		// No offer stops the play: the event is carried out and the Free Peoples player is asked for regroup
		// actions again.
		assertEquals(0, scn.GetWoundsOn(ambusher1));
		assertInDiscard(coming);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedAmbusherIsNotOfferedAndTheEventIsCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetResponseScenario();

		var ambusher1 = scn.GetShadowCard("ambusher1");
		var ambusher2 = scn.GetShadowCard("ambusher2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pursuit = scn.GetFreepsCard("pursuit");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(pursuit);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(ambusher1, ambusher2);
		scn.FreepsRefreshDecision();
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(ambusher1, 1);

		assertEquals(1, scn.GetWoundsOn(ambusher1));
		assertEquals(0, scn.GetWoundsOn(ambusher2));

		scn.FreepsPlayCard(pursuit);
		// Only the Ambusher that can exert is offered.
		assertFalse(scn.ShadowActionAvailable(ambusher1));
		assertTrue(scn.ShadowActionAvailable(ambusher2));

		scn.ShadowDeclineOptionalTrigger();
		// Every minion is wounded: the exhausted Ambusher dies, the other has its first wound.
		assertInDiscard(ambusher1);
		assertEquals(1, scn.GetWoundsOn(ambusher2));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(pursuit);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
