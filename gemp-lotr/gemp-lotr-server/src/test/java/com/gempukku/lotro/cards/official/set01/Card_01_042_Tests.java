package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cloak", "1_42");
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, archer; his archery ability is not used
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, cost 2, no archer
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, no Elf

					put("marksman1", "1_176");  // Goblin Marksman: [moria] Orc archer and nothing else
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenCloakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Elven Cloak
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Cloak
		 * Game Text: To play, spot an Elf. Bearer must be a companion.<br>The minion archery total is -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cloak");

		assertEquals("Elven Cloak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.CLOAK));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnACompanionWhileAnElfCompanionIsSpottedAndMakesTheMinionArcheryTotalMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(cloak);

		scn.StartGame();
		// Laid in the fellowship phase of the turn whose archery phase they stand in.
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveMinionsToTable(marksman1, marksman2);

		// Two minion archers; Legolas is the one Free Peoples archer.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(cloak));

		// Three companions of three races and cultures are offered as its bearer.
		scn.FreepsPlayCard(cloak);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, legolas, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(aragorn);
		assertInZone(Zone.ATTACHED, cloak);
		assertSame(aragorn, cloak.getAttachedTo());

		// A -2 would read 0; the fellowship archery total is left alone.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedUntilAnElfIsSpottedAndThenNotOnAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cloak = scn.GetFreepsCard("cloak");
		var frodo = scn.GetRingBearer();
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		// Frodo and Aragorn are companions, and neither is an Elf.
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(cloak, silinde);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(cloak));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// An Elf ally is spotted as well as an Elf companion.
		scn.FreepsPlayCard(silinde);
		assertInZone(Zone.SUPPORT, silinde);
		assertTrue(scn.FreepsPlayAvailable(cloak));

		// The Elf ally is a character, and no companion.
		scn.FreepsPlayCard(cloak);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(silinde));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);
		assertSame(frodo, cloak.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
