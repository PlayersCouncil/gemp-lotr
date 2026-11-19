package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("memory", "2_88");

					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, vitality 2; answers only a weapon played on him
					put("soldier", "1_271");    // Orc Soldier: a [sauron] Orc, vitality 2; a skirmish ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (an Uruk-hai), vitality 2; Damage +1 is its whole text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text at all

					// [elven] allies: Elrond (home 3) heals allies of site 3 at the start of his owner's turns and has a
					// fellowship ability; Celeborn and Orophin (home 6) have fellowship and archery abilities used only
					// on purpose.
					put("elrond", "1_40");
					put("celeborn", "1_34");
					put("orophin", "1_56");
					// Barliman Butterbur: a [gandalf] Man ally, home site 1; a fellowship ability used only on purpose
					put("barliman", "1_70");
					// Legolas, Greenleaf: an [elven] card that is no ally (a companion); an archery ability used only on purpose
					put("legolas", "1_50");
					// Aragorn, Ranger of the North: a companion, the character next to an ally; a maneuver ability used
					// only on purpose
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MemoryofManyThingsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Memory of Many Things
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a [sauron] Orc to discard an ally (or 2 [elven] allies).
		*/

		var scn = GetScenario();

		var memory = scn.GetFreepsCard("memory");

		assertEquals("Memory of Many Things", memory.getBlueprint().getTitle());
		assertNull(memory.getBlueprint().getSubtitle());
		assertFalse(memory.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, memory.getBlueprint().getSide());
		assertEquals(Culture.SAURON, memory.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, memory.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(memory, Timeword.MANEUVER));
		assertEquals(2, memory.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithOneElvenAllyItExertsASauronOrcToDiscardTheAllyChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memory = scn.GetShadowCard("memory");
		var chieftain = scn.GetShadowCard("chieftain");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var elrond = scn.GetFreepsCard("elrond");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond, barliman);
		scn.MoveCardsToHand(memory);
		assertInHand(memory);

		scn.StartGame();
		assertInPlay(elrond, barliman, aragorn);
		scn.MoveMinionsToTable(chieftain, soldier, guard, troop);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(memory));
		scn.ShadowPlayCard(memory);

		// A [sauron] Orc of either title; not the [sauron] Uruk-hai, nor the [moria] Orc, though each could exert.
		assertTrue(scn.ShadowHasCardChoiceAvailable(chieftain, soldier));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chieftain);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));

		// With one [elven] ally there are not 2 to discard, so the text is "discard an ally": allies of two cultures
		// and races; not a companion, nor the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, barliman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(barliman);

		assertInDiscard(barliman, memory);
		assertInPlay(elrond, aragorn, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithTwoElvenAlliesToDiscardTwoElvenAlliesAreDiscardedAndNoOtherAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memory = scn.GetShadowCard("memory");
		var chieftain = scn.GetShadowCard("chieftain");
		var elrond = scn.GetFreepsCard("elrond");
		var celeborn = scn.GetFreepsCard("celeborn");
		var orophin = scn.GetFreepsCard("orophin");
		var barliman = scn.GetFreepsCard("barliman");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(elrond, celeborn, orophin, barliman);
		scn.MoveCardsToHand(memory);
		assertInHand(memory);

		scn.StartGame();
		assertInPlay(elrond, celeborn, orophin, barliman, legolas);
		scn.MoveMinionsToTable(chieftain);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.ShadowPlayCard(memory);
		// The only [sauron] Orc exerts with no question.
		assertEquals(1, scn.GetWoundsOn(chieftain));

		// Two [elven] allies can be discarded, so the parenthesis is what is done, with no choice of the lesser
		// discard (CR 4.2 "effect"; Q-0035): exactly 2 of the three [elven] allies.  Not the [gandalf] ally, nor
		// the [elven] companion, nor the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(elrond, celeborn, orophin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barliman, legolas, frodo));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(elrond, orophin);

		assertInDiscard(elrond, orophin, memory);
		assertInPlay(celeborn, barliman, legolas, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithExactlyTwoElvenAlliesBothAreDiscardedWithNoQuestionAndTheOtherAllyStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memory = scn.GetShadowCard("memory");
		var chieftain = scn.GetShadowCard("chieftain");
		var elrond = scn.GetFreepsCard("elrond");
		var celeborn = scn.GetFreepsCard("celeborn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(elrond, celeborn, barliman);
		scn.MoveCardsToHand(memory);
		assertInHand(memory);

		scn.StartGame();
		assertInPlay(elrond, celeborn, barliman);
		scn.MoveMinionsToTable(chieftain);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(chieftain));
		scn.ShadowPlayCard(memory);
		// The only [sauron] Orc exerts with no question.
		assertEquals(1, scn.GetWoundsOn(chieftain));

		// Exactly 2 [elven] allies are there, which is enough for the parenthesis: both are discarded with no
		// question, and the [gandalf] ally is not an alternative (CR 4.2 "effect"; Q-0035).
		assertInDiscard(elrond, celeborn, memory);
		assertInPlay(barliman);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsPlayedForNothingWithNoAllyInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memory = scn.GetShadowCard("memory");
		var chieftain = scn.GetShadowCard("chieftain");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(memory);
		assertInHand(memory);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// No ally to discard: the text has no requirement, so the event is offered, the Orc exerts and nothing more
		// happens.
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.ShadowPlayAvailable(memory));
		scn.ShadowPlayCard(memory);

		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInPlay(aragorn, frodo);
		assertInDiscard(memory);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var memory = scn.GetShadowCard("memory");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(memory);
		assertInHand(memory);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain, guard, troop);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 2 with no wound: the Chieftain can exert.
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.ShadowPlayAvailable(memory));

		scn.AddWoundsToChar(chieftain, 1);
		scn.ShadowRefreshDecision();

		// Vitality 2 with 1 wound: exhausted.  The [sauron] Uruk-hai and the [moria] Orc beside him could exert, and
		// an ally is there to discard.
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertFalse(scn.ShadowPlayAvailable(memory));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInPlay(barliman);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
