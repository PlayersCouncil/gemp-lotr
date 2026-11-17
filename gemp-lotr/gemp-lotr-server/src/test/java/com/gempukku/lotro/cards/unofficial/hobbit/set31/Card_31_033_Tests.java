package com.gempukku.lotro.cards.unofficial.hobbit.set31;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_31_033_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer.  The Shadow phase and
	// the maneuver phase of turn 1 are at The Troll Hoard 30_50 (site 2), whose only text is a start-of-turn offer.
	// The minions are cheated in, so none of their "when you play" texts is reached.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("song", "31_33");
					put("song2", "31_33");      // a second copy, for a play from hand beside the one in the pile

					// The Great Goblin, Chieftain of the Misty Mountains: [moria] Orc, vitality 3; a Maneuver ability
					// (exert him twice to reveal the hand) used only through this card's cost.
					put("goblin", "31_36");
					put("troop", "33_41");      // Goblin Troop: [moria] Orc archer, vitality 3; an archery bonus while a [dwarven] artifact is spotted
					put("assaulting", "32_27"); // Assaulting Goblin: [moria] Orc, vitality 2; a response only at a battleground
					put("pursuer", "32_42");    // Orkish Pursuer: [sauron] Orc, vitality 3; a win trigger and a Regroup ability, not reached
					put("troll", "32_31");      // Demolition Troll: [moria] Troll (no Orc), vitality 3; a Skirmish ability, not used

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion; strength +1 for each [dwarven] follower he bears
					put("dain", "32_2");        // Dain Ironfoot: [dwarven] ally (no companion); Thorin +1, a start-of-regroup offer not reached
					put("axe", "31_1");         // Dwarven Axe: [dwarven] possession, on a [dwarven] character; damage +1 at a mountain only
					put("axe2", "31_1");        // the same, on Dain
					put("coat", "31_5");        // Mithril Coat: [dwarven] artifact armor, on Bilbo; archery and Shadow-event costs only
					put("sting", "30_47");      // Sting: [shire] artifact hand weapon, on Bilbo; a skirmish-win trigger not reached
					put("dori", "30_8");        // Dori: [dwarven] follower (neither possession nor artifact), borne by Thorin
					put("barricade", "31_4");   // Great Barricade: [dwarven] possession in the support area, borne by nobody
					put("arkenstone", "31_7");  // The Arkenstone: [dwarven] artifact in the support area, borne by nobody
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void GoblinSongStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 31
		 * Name: Goblin Song
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: You may exert The Great Goblin twice to play this event from your discard pile.
		 * 	Exert 2 Orcs to discard a [dwarven] possession or [dwarven] artifact borne by a companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("song");

		assertEquals("Goblin Song", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTwoOrcsToDiscardADwarvenPossessionOrArtifactBorneByACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetShadowCard("song");
		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var troll = scn.GetShadowCard("troll");
		var troop = scn.GetShadowCard("troop");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var axe = scn.GetFreepsCard("axe");
		var axe2 = scn.GetFreepsCard("axe2");
		var coat = scn.GetFreepsCard("coat");
		var sting = scn.GetFreepsCard("sting");
		var dori = scn.GetFreepsCard("dori");
		var barricade = scn.GetFreepsCard("barricade");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.AttachCardsTo(thorin, axe, dori);
		scn.AttachCardsTo(dain, axe2);
		scn.AttachCardsTo(bilbo, coat, sting);
		scn.MoveCardsToSupportArea(barricade, arkenstone);
		scn.MoveCardsToHand(song);

		scn.StartGame();
		scn.MoveMinionsToTable(assaulting, pursuer, troop, troll);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(dori, thorin);
		assertAttachedTo(axe2, dain);
		assertAttachedTo(coat, bilbo);
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, barricade, arkenstone);
		assertInHand(song);

		scn.SkipToPhase(Phase.MANEUVER);
		// Dori's Aid: a follower's offer at the start of the maneuver phase to move to another companion, declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(dori));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(assaulting));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(song));

		scn.ShadowPlayCard(song);

		// The cost: Orcs of two cultures, three of them to choose two from, and not the [moria] Troll.
		assertTrue(scn.ShadowHasCardChoiceAvailable(assaulting, pursuer, troop));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(assaulting, pursuer);
		assertEquals(1, scn.GetWoundsOn(assaulting));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troll));

		// A [dwarven] possession on a [dwarven] companion and a [dwarven] artifact on the [shire] one are offered.  Not
		// the same possession on an ally, a [shire] artifact on a companion, a [dwarven] follower on a companion, nor a
		// [dwarven] possession or artifact that nobody bears.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, coat));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axe2, sting, dori, barricade, arkenstone));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(coat);

		assertInDiscard(coat, song);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(axe2, dain);
		assertAttachedTo(sting, bilbo);
		assertAttachedTo(dori, thorin);
		assertInZone(Zone.SUPPORT, barricade, arkenstone);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNothingOfThatKindBorneByACompanionTheOrcsExertAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetShadowCard("song");
		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var thorin = scn.GetFreepsCard("thorin");
		var dain = scn.GetFreepsCard("dain");
		var axe2 = scn.GetFreepsCard("axe2");
		var sting = scn.GetFreepsCard("sting");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin);
		scn.MoveAlliesToTable(dain);
		scn.AttachCardsTo(dain, axe2);
		scn.AttachCardsTo(bilbo, sting);
		scn.MoveCardsToSupportArea(arkenstone);
		scn.MoveCardsToHand(song);

		scn.StartGame();
		scn.MoveMinionsToTable(assaulting, pursuer);
		assertAttachedTo(axe2, dain);
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, arkenstone);
		assertInHand(song);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(assaulting));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.ShadowPlayAvailable(song));

		// The target is no requirement: the event is played, its cost paid (the two Orcs, the only two, unasked), and
		// nothing is discarded.
		scn.ShadowPlayCard(song);

		assertEquals(1, scn.GetWoundsOn(assaulting));
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertInDiscard(song);
		assertAttachedTo(axe2, dain);
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, arkenstone);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotPlayableWithOnlyOneOrcAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetShadowCard("song");
		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var troll = scn.GetShadowCard("troll");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToHand(song);

		scn.StartGame();
		scn.MoveMinionsToTable(assaulting, pursuer, troll);
		// Vitality 2 with 1 wound: exhausted.
		scn.AddWoundsToChar(assaulting, 1);
		assertEquals(1, scn.GetWoundsOn(assaulting));
		assertAttachedTo(axe, thorin);
		assertInHand(song);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		// One Orc able to exert, and a Troll able to exert beside him: the cost cannot be paid.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(song));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(troll));
	}

	@Test
	public void ExertingTheGreatGoblinTwicePlaysItFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetShadowCard("song");
		var goblin = scn.GetShadowCard("goblin");
		var troop = scn.GetShadowCard("troop");
		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, troop, assaulting, pursuer);
		scn.MoveCardsToDiscard(song);
		assertInDiscard(song);
		assertAttachedTo(axe, thorin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(assaulting));
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(song));

		scn.ShadowUseCardAction(song);

		// The Great Goblin exerts twice, and not Goblin Troop, the other [moria] Orc able to exert twice.
		assertEquals(2, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The event then asks what it asks from hand: two Orcs (The Great Goblin, exhausted now, is not one).
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, assaulting, pursuer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(goblin));
		scn.ShadowChooseCards(assaulting, pursuer);
		assertEquals(1, scn.GetWoundsOn(assaulting));
		assertEquals(1, scn.GetWoundsOn(pursuer));

		// The Axe, the one card of the kind borne by a companion, is discarded unasked; the event is back in the pile.
		assertInDiscard(axe, song);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotPlayableFromTheDiscardPileWhileTheGreatGoblinCannotExertTwiceThoughACopyInHandIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetShadowCard("song");
		var song2 = scn.GetShadowCard("song2");
		var goblin = scn.GetShadowCard("goblin");
		var troop = scn.GetShadowCard("troop");
		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToHand(song2);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, troop, assaulting, pursuer);
		// Vitality 3 with 1 wound: 2 left, one exertion short of two (CR 4.2 "exert").
		scn.AddWoundsToChar(goblin, 1);
		scn.MoveCardsToDiscard(song);
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertInDiscard(song);
		assertInHand(song2);
		assertAttachedTo(axe, thorin);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		// Goblin Troop, a [moria] Orc able to exert twice, does not stand in for The Great Goblin.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(song));
		assertTrue(scn.ShadowPlayAvailable(song2));
		assertEquals(1, scn.GetWoundsOn(goblin));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Healed, he can pay: the copy in the pile is offered.
		scn.RemoveWoundsFromChar(goblin, 1);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(goblin));
		assertTrue(scn.ShadowActionAvailable(song));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotPlayableFromHandOrDiscardPileInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var song = scn.GetShadowCard("song");
		var song2 = scn.GetShadowCard("song2");
		var goblin = scn.GetShadowCard("goblin");
		var assaulting = scn.GetShadowCard("assaulting");
		var pursuer = scn.GetShadowCard("pursuer");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, axe);
		scn.MoveCardsToHand(song2);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, assaulting, pursuer);
		scn.MoveCardsToDiscard(song);
		assertInDiscard(song);
		assertInHand(song2);
		assertAttachedTo(axe, thorin);

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(goblin));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(song2));
		assertFalse(scn.ShadowActionAvailable(song));
	}
}
