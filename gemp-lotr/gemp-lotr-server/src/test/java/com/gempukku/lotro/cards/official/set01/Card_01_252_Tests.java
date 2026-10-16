package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_252_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "1_252");

					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc, vitality 2; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, vitality 2; adds (2) only when a weapon is played on him
					put("sentry", "10_84");     // Cirith Ungol Sentry: [sauron] Uruk-hai, a [sauron] minion who is no Orc; Damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, an Orc who is not [sauron]; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetLastCardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The Free Peoples player's own copy of this card is the one card of his draw deck
					put("shadow", "1_252");
					// Elrond, Lord of Rivendell: "Fellowship: Exert Elrond to draw a card."  His start-of-turn heal
					// reaches allies of site 3 only.
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFifthBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shadow", "1_252");
					// Goblin Runner: strength 5, which beats Frodo (3, and 1 for the Ring) by one, so that the
					// Ring can be put on and turn the wound into a burden
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheIrresistibleShadowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: The Irresistible Shadow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, exert a [sauron] Orc. Plays to your support area.<br>If you can spot 5 burdens and the Free Peoples player has no cards in his or her draw deck, the Ring-bearer is corrupted.
		*/

		var scn = GetScenario();

		var shadow = scn.GetFreepsCard("shadow");

		assertEquals("The Irresistible Shadow", shadow.getBlueprint().getTitle());
		assertNull(shadow.getBlueprint().getSubtitle());
		assertFalse(shadow.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, shadow.getBlueprint().getSide());
		assertEquals(Culture.SAURON, shadow.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, shadow.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(shadow, Keyword.SUPPORT_AREA));
		assertEquals(0, shadow.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheRingBearerIsCorruptedAtFiveBurdensOnceTheFreePeoplesDrawDeckIsEmptyAndNotWhileItHoldsACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetLastCardScenario();

		var shadow = scn.GetShadowCard("shadow");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCardsToSupportArea(shadow);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.AddBurdens(5);

		// Five burdens, which Frodo's resistance of 10 bears, and one card left to draw: the game goes on.
		assertEquals(5, scn.GetBurdens());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.MarkGameLog();
		scn.FreepsUseCardAction(elrond);
		// The last card is drawn, and with it the game is lost.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetBurdens());
		assertTrue(scn.GameIsFinished());
		// The game's own words for it; a game that ended for any other reason would not say them
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P2() + " lost"));
	}

	@Test
	public void WithTheFreePeoplesDrawDeckEmptyTheRingBearerIsCorruptedAtTheFifthBurdenAndNotAtFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFifthBurdenScenario();

		var shadow = scn.GetShadowCard("shadow");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var ring = scn.GetRing();
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();
		// The Free Peoples player's copies of the two cards are his whole draw deck
		scn.MoveCardsToDiscard(scn.GetFreepsCard("shadow"), scn.GetFreepsCard("runner"));
		scn.AddBurdens(4);
		scn.MoveMinionsToTable(runner);
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, runner);
		scn.FreepsResolveSkirmish(frodo);
		scn.PassSkirmishActions();

		// No card in the draw deck and four burdens, through four phases: the game goes on.
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(4, scn.GetBurdens());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ring));

		// Frodo puts on the Ring, which makes a burden of the wound: the fifth.
		scn.MarkGameLog();
		scn.FreepsChooseOptionalTrigger(ring);
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.GameIsFinished());
		assertTrue(scn.GameLogSinceMarkSays(scn.P1() + " lost due to: The Ring-Bearer is corrupted"));
		assertFalse(scn.GameLogSinceMarkSays(scn.P2() + " lost"));
	}

	@Test
	public void ExertsASauronOrcToPlayToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var soldier = scn.GetShadowCard("soldier");
		var chieftain = scn.GetShadowCard("chieftain");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(shadow);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, chieftain, sentry, troop);
		assertInHand(shadow);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(shadow));
		assertEquals(0, scn.GetWoundsOn(soldier));

		scn.ShadowPlayCard(shadow);
		// Both [sauron] Orcs are offered; the [sauron] Uruk-hai and the [moria] Orc, each able to exert, are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, chieftain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(soldier);

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(sentry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SUPPORT, shadow);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenTheOnlySauronOrcIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		var soldier = scn.GetShadowCard("soldier");
		var sentry = scn.GetShadowCard("sentry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(shadow);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, sentry, troop);
		// Vitality 2 with 1 wound: exhausted.  The Uruk-hai and the [moria] Orc beside it could exert.
		scn.AddWoundsToChar(soldier, 1);
		assertTrue(scn.IsExhausted(soldier));
		assertFalse(scn.IsExhausted(sentry));
		assertFalse(scn.IsExhausted(troop));
		assertInHand(shadow);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(shadow));
		assertInHand(shadow);
	}

	@Test
	public void AnEmptyShadowDrawDeckDoesNotCorruptTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shadow = scn.GetShadowCard("shadow");
		scn.MoveCardsToSupportArea(shadow);

		scn.StartGame();
		scn.AddBurdens(5);
		// The Shadow player's draw deck is empty and the Free Peoples player's holds one card.
		scn.SetShadowDrawDeck();
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("soldier"));

		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertFalse(scn.GameIsFinished());

		scn.FreepsPass();

		// Into the Shadow phase with nothing changed: the game goes on.
		assertEquals(5, scn.GetBurdens());
		assertEquals(0, scn.GetShadowDeckCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertFalse(scn.GameIsFinished());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
