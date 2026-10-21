package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_06_104_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("insurgent", "6_104");
					// Frodo, the Ring-bearer, is the [shire] culture.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a second [gondor] companion; a skirmish ability never used
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion; his text is about his own strength
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] ally; a fellowship ability never used
					put("cloak", "1_42");       // Elven Cloak: [elven] possession on a companion; the minion archery total is -1
					// Gollum, Nasty Treacherous Creature: a [gollum] minion, a Shadow card of a culture Free Peoples
					// cards also have; a regroup ability never used
					put("gollum", "5_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcInsurgentStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Orc Insurgent
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: <b>Shadow:</b> Exert this minion to draw X cards and add (X), where X is the number of Free Peoples cultures you spot over 2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("insurgent");

		assertEquals("Orc Insurgent", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHimToDrawAndAddOneForEachFreePeoplesCultureOver2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var insurgent = scn.GetShadowCard("insurgent");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var grimir = scn.GetFreepsCard("grimir");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn, boromir, gandalf);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();
		scn.MoveMinionsToTable(insurgent, gollum);
		scn.FreepsPass();

		// Five Free Peoples cultures: [shire] (Frodo), [gondor] (Aragorn and Boromir, counted once), [gandalf]
		// (Gandalf), [dwarven] (an ally) and [elven] (a possession only).  The [gollum] minion is no Free Peoples
		// card, and the [sauron] Orc is not one either.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(insurgent));
		int pool = scn.GetTwilight();
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(insurgent));

		scn.ShadowUseCardAction(insurgent);
		assertEquals(1, scn.GetWoundsOn(insurgent));
		// 5 cultures over 2: X is up to 3, the player's to name.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		// The number comes filled in with the most (Q-0341: A, as F-0561), and the player accepts it.
		assertEquals(Integer.valueOf(3), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("3");

		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(deck - 3, scn.GetShadowDeckCount());
		assertEquals(pool + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourFreePeoplesCulturesXIsUpTo2AndThePlayerMayNameFewer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var insurgent = scn.GetShadowCard("insurgent");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(insurgent);
		scn.FreepsPass();

		// [shire], [gondor], [gandalf] and [dwarven]
		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowHandCount());

		scn.ShadowUseCardAction(insurgent);
		assertEquals(1, scn.GetWoundsOn(insurgent));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		// Filled in with the most (Q-0341: A), and fewer may be named.
		assertEquals(Integer.valueOf(2), scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("1");

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(deck - 1, scn.GetShadowDeckCount());
		assertEquals(pool + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTwoFreePeoplesCulturesTheExertionIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var insurgent = scn.GetShadowCard("insurgent");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(insurgent, gollum);
		scn.FreepsPass();

		// [shire] and [gondor] alone: a second [gondor] companion and a [gollum] minion make no third.  The
		// ability has a cost, so it is offered all the same (Q-0034), and X can only be 0, which is not asked.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetWoundsOn(insurgent));
		int pool = scn.GetTwilight();
		int deck = scn.GetShadowDeckCount();
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.ShadowActionAvailable(insurgent));

		scn.ShadowUseCardAction(insurgent);
		assertEquals(1, scn.GetWoundsOn(insurgent));
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(deck, scn.GetShadowDeckCount());
		assertEquals(pool, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var insurgent = scn.GetShadowCard("insurgent");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(insurgent);
		scn.AddWoundsToChar(insurgent, 2);
		assertEquals(2, scn.GetWoundsOn(insurgent));
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: he cannot exert, though four cultures would make X up to 2.
		assertEquals(2, scn.GetWoundsOn(insurgent));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(insurgent));
	}
}
