package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("deep", "3_30");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: a strength bonus and nothing else
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("relics", "1_195");     // Relics of Moria: [moria] Shadow condition, support area; a Shadow ability
					put("evil", "1_283");       // You Bring Great Evil: [sauron] Shadow condition borne by a companion; bearer cannot heal at a sanctuary
					put("pinned", "1_192");     // Pinned Down: [moria] Shadow condition, support area; does something only with a [moria] archer
					put("seeking", "1_276");    // Seeking Its Master: [sauron] Shadow condition, support area; a Shadow ability
					put("cover", "2_35");       // Natural Cover: Free Peoples condition, support area; does something only at a forest
					put("poison", "8_28");      // Spider Poison: Shadow possession borne by a companion; does something only on assignment
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeepinThoughtStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Deep in Thought
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 4
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.<br><b>Maneuver:</b> Spot Gandalf and 4 twilight tokens to discard all Shadow conditions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("deep");

		assertEquals("Deep in Thought", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsEveryShadowConditionOfTheShadowPlayerAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deep = scn.GetFreepsCard("deep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var cover = scn.GetFreepsCard("cover");
		// The Free Peoples player's own Shadow condition: in play, and not active on his own turn (Q-0117).
		var ownPinned = scn.GetFreepsCard("pinned");
		var relics = scn.GetShadowCard("relics");
		var evil = scn.GetShadowCard("evil");
		var pinned = scn.GetShadowCard("pinned");
		var seeking = scn.GetShadowCard("seeking");
		var poison = scn.GetShadowCard("poison");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(deep);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(relics, pinned, seeking, cover, ownPinned);
		scn.AttachCardsTo(gandalf, evil);
		scn.AttachCardsTo(frodo, poison);
		scn.SkipToPhase(Phase.MANEUVER);
		// The 4 tokens must be in the pool before the event's own cost is added (CR 4.2 "cost").
		scn.SetTwilight(4);
		scn.FreepsRefreshDecision();

		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, relics, pinned, seeking, cover, ownPinned);
		assertAttachedTo(evil, gandalf);
		assertAttachedTo(poison, frodo);
		assertTrue(scn.FreepsPlayAvailable(deep));

		scn.FreepsPlayCard(deep);

		// Shadow conditions of two cultures, in a support area and borne by a companion: every one of them goes.
		assertInDiscard(relics, pinned, seeking, evil, deep);
		// A Free Peoples condition, a Shadow possession and the Free Peoples player's own Shadow condition stay.
		assertInZone(Zone.SUPPORT, cover, ownPinned);
		assertAttachedTo(poison, frodo);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// 4 spotted and left in the pool + 4 to play
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithThreeTwilightTokensAndCanWithFour() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deep = scn.GetFreepsCard("deep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var relics = scn.GetShadowCard("relics");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(deep);
		scn.MoveCompanionsToTable(gandalf);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(relics);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(deep));

		scn.SetTwilight(4);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(deep));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAGandalfCompanionWhoIsNotGandalf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var deep = scn.GetFreepsCard("deep");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		var relics = scn.GetShadowCard("relics");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToHand(deep);
		scn.MoveCompanionsToTable(avenger);

		scn.StartGame();

		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(relics);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(4);
		scn.FreepsRefreshDecision();

		assertFalse(scn.FreepsPlayAvailable(deep));

		scn.MoveCompanionsToTable(gandalf);
		scn.FreepsRefreshDecision();

		assertTrue(scn.FreepsPlayAvailable(deep));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
