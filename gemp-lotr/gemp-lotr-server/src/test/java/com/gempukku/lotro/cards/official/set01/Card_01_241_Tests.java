package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_241_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("curse", "1_241");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability only
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc; his text answers a weapon played on him
					put("guard", "10_81");      // Cirith Ungol Guard: a [sauron] minion who is no Orc (Uruk-hai), damage +1 only
					put("troop", "1_177");      // Goblin Patrol Troop: an Orc who is not [sauron] ([moria]), no text

					put("mallorn", "1_54");     // Mallorn-trees: an [elven] condition in the support area
					put("alliance", "1_49");    // The Last Alliance of Elves and Men: an [elven] condition borne by a [gondor] Man
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, to bear the Alliance
					put("mirror", "1_55");      // The Mirror of Galadriel: an [elven] possession, no condition
					put("lord", "1_21");        // Lord of Moria: a condition of another culture ([dwarven])
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CurseFromMordorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Curse From Mordor
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Shadow
		 * Game Text: <b>Shadow:</b> Spot a [sauron] Orc to discard an [elven] condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("curse");

		assertEquals("Curse From Mordor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SHADOW));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingASauronOrcDiscardsAnElvenConditionOfTheShadowPlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var curse = scn.GetShadowCard("curse");
		var soldier = scn.GetShadowCard("soldier");
		var mallorn = scn.GetFreepsCard("mallorn");
		var alliance = scn.GetFreepsCard("alliance");
		var aragorn = scn.GetFreepsCard("aragorn");
		var mirror = scn.GetFreepsCard("mirror");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, alliance);
		scn.MoveCardsToSupportArea(mallorn, mirror, lord);
		scn.MoveCardsToHand(curse);
		assertAttachedTo(alliance, aragorn);
		assertInZone(Zone.SUPPORT, mallorn, mirror, lord);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(curse));

		scn.ShadowPlayCard(curse);
		// An [elven] condition in the support area or borne; not the [elven] possession, nor the [dwarven] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(mallorn, alliance));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(mirror, lord));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(alliance);

		assertInDiscard(alliance, curse);
		assertInZone(Zone.SUPPORT, mallorn, mirror, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithoutASauronOrcItIsNotPlayableAndWithNoElvenConditionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var curse = scn.GetShadowCard("curse");
		var chieftain = scn.GetShadowCard("chieftain");
		var guard = scn.GetShadowCard("guard");
		var troop = scn.GetShadowCard("troop");
		var mirror = scn.GetFreepsCard("mirror");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToSupportArea(mirror, lord);
		scn.MoveCardsToHand(curse, chieftain);
		assertInZone(Zone.SUPPORT, mirror, lord);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, troop);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [sauron] Uruk-hai and a [moria] Orc: no [sauron] Orc to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(curse));

		scn.ShadowPlayCard(chieftain);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(curse));

		// No [elven] condition is in play: the event is played and nothing is discarded.
		scn.ShadowPlayCard(curse);
		assertInDiscard(curse);
		assertInZone(Zone.SUPPORT, mirror, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
