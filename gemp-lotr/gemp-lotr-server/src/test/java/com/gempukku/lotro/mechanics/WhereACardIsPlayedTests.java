package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Where a card of each type goes when it is played from hand, and in which phase it is offered.
 *
 * Comprehensive Rules 4.2: "companion": "Play companion cards in a row, near the other members of your fellowship";
 * "ally": "Play them to your support area"; "minion": "A minion is played to the center of the table, across from
 * the active fellowship"; "follower": "A follower is played to your support area"; "possession", "artifact" and
 * "condition": most tell you who their bearer can be, the others play to your support area; "bearer": "When you
 * play a card on a bearer, put it beneath the bearer"; "event": "Place an event in your discard pile after you
 * have played it from hand and carried out its effects".  "Fellowship phase": "Play a Free Peoples companion, ally,
 * possession, artifact, or condition from your hand to the table"; "Shadow phase(s)": "Play a Shadow minion,
 * possession, artifact, or condition from your hand to the table"; "condition": "A condition is always played
 * during the fellowship phase (if it is a Free Peoples card) or the Shadow phase (if it is a Shadow card), even if
 * it provides a special ability that is performed during a different phase."
 *
 * With TwilightPoolTests this is what lets a card's own tests take its printed type and side as read (Ketura on
 * Q-0279, 2026-10-12): where it goes, when it is offered, and to whom.  A card whose own text says who may bear it
 * ("Bearer must be ...", "Plays on ...") shows that in its own tests; here the bearer is whoever the player
 * chooses among those the text allows.  An event's own phase word is the event's to show.  The Support Area
 * keyword is shown here, on a card of each type that has it, beside a character it could otherwise have gone on.
 *
 * Elsewhere: what a follower's Aid does once it is in the support area, AidTests; what a card in the support area
 * is, and whose, SupportAreaTests and WhoseSupportAreaTests; a starting fellowship, StartingFellowshipTests.
 */
public class WhereACardIsPlayedTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Free Peoples cards, one of each type.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability
					put("merry", "1_302");      // Merry, Friend to Sam: a Hobbit companion; a skirmish ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: ally; a fellowship ability that exerts him
					put("sword", "1_299");      // Hobbit Sword: possession.  "Bearer must be a Hobbit." is its whole text
					put("mithril", "2_105");    // Mithril-coat: artifact.  "Bearer must be Frodo."; the rest is about archery and skirmishes
					put("tale", "1_317");       // There and Back Again: condition.  "Bearer must be a Hobbit companion."; a MANEUVER ability
					put("pallando", "13_37");   // Pallando, Far-travelling One: follower; his texts answer a transfer and a skirmish won
					put("horn", "5_8");         // Horn of Helm: possession with the Support Area keyword; a maneuver ability for a Dwarf
					put("lord", "1_21");        // Lord of Moria: condition with the Support Area keyword.  "Each Dwarf is damage +1."
					// The Shards of Narsil: artifact with the Support Area keyword; two fellowship abilities about
					// [gondor] cards stacked on it.
					put("shards", "3_44");
					// A Wizard Is Never Late: event.  "Fellowship: Play a [gandalf] character from your draw deck."
					put("late", "1_87");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: the [gandalf] character it finds

					// Shadow cards, one of each type.
					// Bill Ferny, Swarthy Sneering Fellow: minion, site number 2, so not roaming at site 2.  Nobody
					// may be assigned to him, and he is discarded underground (site 2 is plains).
					put("bill", "2_75");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; when played he may add (2), and he is never played here
					put("spear", "1_182");      // Goblin Spear: possession.  "Bearer must be a [moria] minion."; the rest answers a kill
					put("weary", "4_212");      // Weary: condition.  "Plays on a companion or ally."; the rest answers an [isengard] tracker
					put("saruman", "17_38");    // Saruman, Servant of Sauron: follower; "Aid - (1)", and a trigger on a skirmish won
					// Ships of Great Draught: possession with the Support Area keyword.  Its Shadow ability needs a
					// threat or a [raider] card in hand, and there is neither.
					put("ships", "8_65");
					// Library of Orthanc: artifact with the Support Area keyword.  Its Shadow ability is a play of
					// an [isengard] minion from hand, and none is in hand with it.
					put("library", "9_39");
					// They Are Coming: condition with the Support Area keyword.  Its Shadow ability needs 3 cards
					// to discard from hand.
					put("coming", "1_196");
					put("host", "1_187");       // Host of Thousands: event.  "Shadow: Play a [moria] Orc from your discard pile."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACompanionGoesIntoTheFellowshipAndAnAllyAndAFollowerToTheSupportAreaBearingOnNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCardsToHand(aragorn, celeborn, pallando);

		scn.StartGame();
		assertEquals(3, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(aragorn);
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(2, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertEquals(1, scn.GetFreepsHandCount());

		// Two companions stand there, and the follower is not put on either: nothing is asked.  (Frodo bears
		// The One Ring, and is not read.)
		scn.FreepsPlayCard(pallando);
		assertInZone(Zone.SUPPORT, pallando);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.GetAttachedCards(aragorn).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMinionGoesToTheShadowSideAndAShadowFollowerToTheSupportAreaBearingOnNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bill = scn.GetShadowCard("bill");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(bill, saruman);

		scn.StartGame();
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetShadowHandCount());

		scn.ShadowPlayCard(bill);
		assertInZone(Zone.SHADOW_CHARACTERS, bill);
		assertEquals(1, scn.GetShadowHandCount());

		// A minion stands there, and the follower is not put on him: nothing is asked.
		scn.ShadowPlayCard(saruman);
		assertInZone(Zone.SUPPORT, saruman);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.GetAttachedCards(bill).isEmpty());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFreePeoplesPossessionOrConditionThatNamesABearerGoesOnTheCharacterChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var tale = scn.GetFreepsCard("tale");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(sword, tale);

		scn.StartGame();

		// Two Hobbit companions for each: the player is asked.
		scn.FreepsPlayCard(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(sword, merry);
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(tale);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, merry));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(tale, frodo);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowPossessionOrConditionThatNamesABearerGoesOnTheCharacterChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var spear = scn.GetShadowCard("spear");
		var weary = scn.GetShadowCard("weary");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(spear, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// Two [moria] minions for the Spear, two companions for Weary: the player is asked.
		scn.ShadowPlayCard(spear);
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, runner));
		scn.ShadowChooseCard(runner);
		assertAttachedTo(spear, runner);
		assertEquals(1, scn.GetShadowHandCount());

		scn.ShadowPlayCard(weary);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, merry));
		scn.ShadowChooseCard(merry);
		assertAttachedTo(weary, merry);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AFreePeoplesCardWithTheSupportAreaKeywordGoesToTheSupportAreaThoughACharacterStandsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var horn = scn.GetFreepsCard("horn");
		var lord = scn.GetFreepsCard("lord");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(horn, lord, shards);

		scn.StartGame();

		// A possession, a condition and an artifact, with Gimli on the table: none is asked a bearer.
		scn.FreepsPlayCard(horn);
		scn.FreepsPlayCard(lord);
		scn.FreepsPlayCard(shards);
		assertInZone(Zone.SUPPORT, horn, lord, shards);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.GetAttachedCards(gimli).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShadowCardWithTheSupportAreaKeywordGoesToTheSupportAreaThoughAMinionStandsThere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		var ships = scn.GetShadowCard("ships");
		var library = scn.GetShadowCard("library");
		var coming = scn.GetShadowCard("coming");
		scn.MoveCardsToHand(ships, library, coming);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();

		// A possession, an artifact and a condition, with a minion on the table: none is asked a bearer.
		scn.ShadowPlayCard(ships);
		scn.ShadowPlayCard(library);
		scn.ShadowPlayCard(coming);
		assertInZone(Zone.SUPPORT, ships, library, coming);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.GetAttachedCards(troop).isEmpty());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnEventIsInNoZoneWhileItIsCarriedOutAndThenGoesToTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetFreepsCard("late");
		var gandalf = scn.GetFreepsCard("gandalf");
		var host = scn.GetShadowCard("host");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(late, host);
		scn.MoveCardsToDiscard(troop, runner);

		scn.StartGame();

		// The draw deck is being shown for the choice: the event is out of the hand and not yet discarded.
		scn.FreepsPlayCard(late);
		assertInZone(Zone.VOID, late);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(gandalf);
		assertInZone(Zone.FREE_CHARACTERS, gandalf);
		assertInDiscard(late);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// Two [moria] Orcs in the discard pile: the choice is asked, and the event is out of the hand meanwhile.
		scn.ShadowPlayCard(host);
		assertInZone(Zone.VOID, host);
		scn.ShadowChooseCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInDiscard(host, runner);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void FreePeoplesCardsAreOfferedInTheirOwnersFellowshipPhaseAndNotInAnotherPhaseOrToTheShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		// One of each type that is played to the table, in each player's hand.
		var aragorn = scn.GetFreepsCard("aragorn");
		var celeborn = scn.GetFreepsCard("celeborn");
		var sword = scn.GetFreepsCard("sword");
		var mithril = scn.GetFreepsCard("mithril");
		var tale = scn.GetFreepsCard("tale");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCardsToHand(aragorn, celeborn, sword, mithril, tale, pallando);
		var shadowAragorn = scn.GetShadowCard("aragorn");
		var shadowCeleborn = scn.GetShadowCard("celeborn");
		var shadowSword = scn.GetShadowCard("sword");
		var shadowMithril = scn.GetShadowCard("mithril");
		var shadowTale = scn.GetShadowCard("tale");
		var shadowPallando = scn.GetShadowCard("pallando");
		scn.MoveCardsToHand(shadowAragorn, shadowCeleborn, shadowSword, shadowMithril, shadowTale, shadowPallando);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(aragorn));
		assertTrue(scn.FreepsPlayAvailable(celeborn));
		assertTrue(scn.FreepsPlayAvailable(sword));
		assertTrue(scn.FreepsPlayAvailable(mithril));
		assertTrue(scn.FreepsPlayAvailable(tale));
		assertTrue(scn.FreepsPlayAvailable(pallando));

		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		// The Shadow player's own Free Peoples cards, in his Shadow phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shadowAragorn));
		assertFalse(scn.ShadowPlayAvailable(shadowCeleborn));
		assertFalse(scn.ShadowPlayAvailable(shadowSword));
		assertFalse(scn.ShadowPlayAvailable(shadowMithril));
		assertFalse(scn.ShadowPlayAvailable(shadowTale));
		assertFalse(scn.ShadowPlayAvailable(shadowPallando));

		// No minion: the next phase is the regroup phase, and the Free Peoples player is asked first.  There and
		// Back Again's own ability is a maneuver one, and the card is still not played outside the fellowship
		// phase.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(aragorn));
		assertFalse(scn.FreepsPlayAvailable(celeborn));
		assertFalse(scn.FreepsPlayAvailable(sword));
		assertFalse(scn.FreepsPlayAvailable(mithril));
		assertFalse(scn.FreepsPlayAvailable(tale));
		assertFalse(scn.FreepsPlayAvailable(pallando));
		assertInHand(aragorn, celeborn, sword, mithril, tale, pallando);
	}

	@Test
	public void ShadowCardsAreOfferedInTheShadowPhaseAndNotInAnotherPhaseOrToTheFreePeoplesPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troop = scn.GetShadowCard("troop");
		// One of each type that is played to the table, in each player's hand.
		var bill = scn.GetShadowCard("bill");
		var spear = scn.GetShadowCard("spear");
		var library = scn.GetShadowCard("library");
		var coming = scn.GetShadowCard("coming");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(bill, spear, library, coming, saruman);
		var freepsBill = scn.GetFreepsCard("bill");
		var freepsSpear = scn.GetFreepsCard("spear");
		var freepsLibrary = scn.GetFreepsCard("library");
		var freepsComing = scn.GetFreepsCard("coming");
		var freepsSaruman = scn.GetFreepsCard("saruman");
		scn.MoveCardsToHand(freepsBill, freepsSpear, freepsLibrary, freepsComing, freepsSaruman);

		scn.StartGame();
		scn.SetTwilight(20);

		// The Free Peoples player's own Shadow cards, in his fellowship phase.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(freepsBill));
		assertFalse(scn.FreepsPlayAvailable(freepsSpear));
		assertFalse(scn.FreepsPlayAvailable(freepsLibrary));
		assertFalse(scn.FreepsPlayAvailable(freepsComing));
		assertFalse(scn.FreepsPlayAvailable(freepsSaruman));

		scn.MoveMinionsToTable(troop);
		scn.FreepsPass();
		scn.SetTwilight(20);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(bill));
		assertTrue(scn.ShadowPlayAvailable(spear));
		assertTrue(scn.ShadowPlayAvailable(library));
		assertTrue(scn.ShadowPlayAvailable(coming));
		assertTrue(scn.ShadowPlayAvailable(saruman));

		// The Troop is in play, so the maneuver phase comes; the Free Peoples player passes first.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bill));
		assertFalse(scn.ShadowPlayAvailable(spear));
		assertFalse(scn.ShadowPlayAvailable(library));
		assertFalse(scn.ShadowPlayAvailable(coming));
		assertFalse(scn.ShadowPlayAvailable(saruman));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(bill));
		assertFalse(scn.ShadowPlayAvailable(spear));
		assertFalse(scn.ShadowPlayAvailable(library));
		assertFalse(scn.ShadowPlayAvailable(coming));
		assertFalse(scn.ShadowPlayAvailable(saruman));
		assertInHand(bill, spear, library, coming, saruman);
	}
}
