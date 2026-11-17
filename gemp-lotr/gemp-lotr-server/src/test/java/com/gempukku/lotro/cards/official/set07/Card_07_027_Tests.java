package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bowman", "7_27");
					put("legolas", "1_50");     // Legolas, Greenleaf: a unique [elven] archer companion; an archery ability used only on purpose
					put("elf1", "1_53");        // Lórien Elf: an [elven] companion who is no archer, vitality 2; no text once in play
					put("elf2", "1_53");
					put("elf3", "1_53");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: a unique [elven] ranger companion, no archer
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an [elven] ally, home site 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion
					put("defiance", "1_37");    // Defiance: an [elven] event
					put("elvenbow", "1_41");    // Elven Bow: an [elven] possession
					put("valor", "1_103");      // Elendil's Valor: a [gondor] event
					put("runner", "1_178");     // Goblin Runner: a Shadow card, [moria]
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text, vitality 3
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, Damage +1 and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MirkwoodBowmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Mirkwood Bowman
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Exert 2 [elven] companions to discard the top 3 cards of your draw deck. Make the fellowship archery total +1 for each [elven] card discarded.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bowman");

		assertEquals("Mirkwood Bowman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTwoElvenCompanionsToDiscardTheTopThreeAndAddsOneForEachElvenCardAmongThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowman = scn.GetFreepsCard("bowman");
		var legolas = scn.GetFreepsCard("legolas");
		var elf1 = scn.GetFreepsCard("elf1");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var defiance = scn.GetFreepsCard("defiance");
		var elvenbow = scn.GetFreepsCard("elvenbow");
		var valor = scn.GetFreepsCard("valor");
		var elf2 = scn.GetFreepsCard("elf2");
		var elf3 = scn.GetFreepsCard("elf3");
		scn.MoveCompanionsToTable(legolas, elf1, arwen, aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.MoveCardsToHand(bowman);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		// The top three: an [elven] event, an [elven] possession and a [gondor] event; an [elven] card fourth.
		scn.SetFreepsDrawDeck(defiance, elvenbow, valor, elf2);
		// An [elven] card already in the discard pile, which is not one discarded by the event.
		scn.MoveCardsToDiscard(elf3);
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(defiance, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(elvenbow, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(valor, scn.GetFromTopOfFreepsDeck(3));
		assertEquals(elf2, scn.GetFromTopOfFreepsDeck(4));
		assertInDiscard(elf3);
		assertEquals(1, scn.GetFreepsDiscardCount());
		// Legolas is the one archer.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(elf1));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.FreepsPlayAvailable(bowman));

		scn.FreepsPlayCard(bowman);
		// [elven] companions only: not the [elven] ally, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, elf1, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(silinde, aragorn, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(elf1, arwen);

		assertEquals(1, scn.GetWoundsOn(elf1));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertInDiscard(defiance, elvenbow, valor, bowman);
		assertEquals(elf2, scn.GetFreepsTopOfDeck());
		assertEquals(1, scn.GetFreepsDeckCount());
		// Two [elven] cards among the three discarded: 1 + 2.
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void OneElvenCardDiscardedAddsOneForTheArcheryPhaseOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowman = scn.GetFreepsCard("bowman");
		var elf1 = scn.GetFreepsCard("elf1");
		var elf2 = scn.GetFreepsCard("elf2");
		var arwen = scn.GetFreepsCard("arwen");
		var elf3 = scn.GetFreepsCard("elf3");
		var runner = scn.GetFreepsCard("runner");
		var valor = scn.GetFreepsCard("valor");
		scn.MoveCompanionsToTable(elf1, elf2, arwen);
		scn.MoveCardsToHand(bowman);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// The top three: an [elven] companion, a Shadow card and a [gondor] event.
		scn.SetFreepsDrawDeck(elf3, runner, valor);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(elf3, scn.GetFromTopOfFreepsDeck(1));
		assertEquals(runner, scn.GetFromTopOfFreepsDeck(2));
		assertEquals(valor, scn.GetFromTopOfFreepsDeck(3));
		// No archer in the fellowship.
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(bowman);
		scn.FreepsChooseCards(elf1, elf2);
		assertEquals(1, scn.GetWoundsOn(elf1));
		assertEquals(1, scn.GetWoundsOn(elf2));
		assertInDiscard(elf3, runner, valor);
		assertEquals(0, scn.GetFreepsDeckCount());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		// The one archery wound is placed on the one minion; in the assignment phase the total is 0 again.
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.PassArcheryActions();
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyOneElvenCompanionAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bowman = scn.GetFreepsCard("bowman");
		var elf1 = scn.GetFreepsCard("elf1");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(elf1, arwen, aragorn);
		scn.MoveCardsToHand(bowman);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// Vitality 2 with 1 wound: the Lórien Elf is exhausted.  Aragorn could exert, but is not [elven].
		scn.AddWoundsToChar(elf1, 1);
		assertEquals(1, scn.GetWoundsOn(elf1));
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(bowman));
	}
}
