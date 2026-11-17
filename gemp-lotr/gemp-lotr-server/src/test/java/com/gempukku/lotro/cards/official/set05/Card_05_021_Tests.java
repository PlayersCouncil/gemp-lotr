package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bbs", "5_21");

					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] Sméagol; to play, add a burden; a skirmish ability only
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: [shire] Sméagol; an assignment ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is no Sméagol; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("defend", "4_115");     // Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion once (twice an Uruk-hai)

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("scout", "1_191");      // Moria Scout: [moria] Orc, vitality 2: can exert once and not twice; its play text is not reached cheated in
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its play text is not reached cheated in
					put("ambition", "1_133");   // Saruman's Ambition: a Shadow condition, which is no minion; a cost bonus and a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSmeagolRingBearerScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bbs", "5_21");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, so the Ring-bearer's move trigger has no unbound companion to kill
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				// Sméagol, Bearer of Great Secrets 9_30 as Ring-bearer: each move kills an unbound companion (none here);
				// a regroup ability only.
				VirtualTableScenario.SmeagolRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeBackSoonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Be Back Soon
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Discard Sméagol to discard a minion. An opponent may exert a minion twice to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bbs");

		assertEquals("Be Back Soon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsSmeagolToDiscardAMinionOfThePlayersChoiceWhenTheShadowPlayerDoesNotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bbs = scn.GetFreepsCard("bbs");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToHand(bbs);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var ambition = scn.GetShadowCard("ambition");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.MoveCardsToSupportArea(ambition);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, bbs);
		assertInZone(Zone.SUPPORT, ambition);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(bbs));
		scn.FreepsPlayCard(bbs);

		// The cost: Sméagol, the only one there is, to the discard pile and not the dead pile.
		assertInDiscard(smeagol);

		// The opponent is asked first, and declines.
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Minions of two cultures and races are offered; a Shadow condition and the companions are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ambition, sam, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		assertInDiscard(troop, bbs);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertInZone(Zone.SUPPORT, ambition);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShireSmeagolPaysTooAndTheShadowPlayerPreventsTheDiscardByExertingAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bbs = scn.GetFreepsCard("bbs");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(bbs);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, scout, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, bbs);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsPlayAvailable(bbs));
		scn.FreepsPlayCard(bbs);

		assertInDiscard(smeagol);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// Only minions able to exert twice: the Scout (vitality 2) could exert once only, the Runner not at all.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(scout, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(savage);

		// Two exertions, and no minion is discarded; Sméagol stays discarded.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertInZone(Zone.SHADOW_CHARACTERS, troop, savage, scout, runner);
		assertInDiscard(smeagol, bbs);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertTwiceThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bbs = scn.GetFreepsCard("bbs");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(bbs);

		var scout = scn.GetShadowCard("scout");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(scout, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, bbs);
		assertInZone(Zone.SHADOW_CHARACTERS, scout, runner);
		scn.FreepsPlayCard(bbs);

		assertInDiscard(smeagol);
		// Straight to the choice of the minion: the opponent has nothing to pay with.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(scout, runner));
		scn.FreepsChooseCard(scout);

		assertInDiscard(scout);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutSmeagol() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bbs = scn.GetFreepsCard("bbs");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveCardsToHand(bbs);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, bbs);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(bbs));
	}

	@Test
	public void WithNoMinionLeftItIsStillPlayedAndOnlyDiscardsSmeagol() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bbs = scn.GetFreepsCard("bbs");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.MoveCardsToHand(bbs, defend);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, bbs, defend);
		// The only minion is killed first, by another maneuver event.
		scn.FreepsPlayCard(defend);
		assertInDiscard(runner);
		scn.ShadowPass();

		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.FreepsPlayAvailable(bbs));
		scn.FreepsPlayCard(bbs);

		assertInDiscard(smeagol, bbs);
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedToDiscardSmeagolWhenHeIsTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSmeagolRingBearerScenario();

		var bbs = scn.GetFreepsCard("bbs");
		var smeagol = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(bbs);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals("Sméagol", smeagol.getBlueprint().getTitle());
		assertInZone(Zone.HAND, bbs);
		assertInZone(Zone.FREE_CHARACTERS, smeagol, sam);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(bbs));
	}
}
