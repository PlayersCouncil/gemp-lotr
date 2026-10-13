package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_107_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "51_107");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant only
					put("dead", "8_45");        // Sleepless Dead: [gondor] Wraith companion (cheated in, so no threat)
					put("dwarf", "1_7");        // Dwarf Guard: [dwarven] Dwarf companion, neither a Man nor [gondor]
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, a fellowship ability used only on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf archer companion, an archery ability used only on purpose

					put("marksman1", "1_176");  // Goblin Marksman: strength 7, vitality 1, archer and nothing else
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatShieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Great Shield
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Shield
		 * Game Text: Bearer must be a Man or [gondor] companion.
		 * The minion archery total is -1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("Great Shield", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAManOrAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var dead = scn.GetFreepsCard("dead");
		var dwarf = scn.GetFreepsCard("dwarf");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(shield);
		scn.MoveCompanionsToTable(aragorn, guard, dead, dwarf);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(shield));
		scn.FreepsPlayCard(shield);

		// A Man of another culture, a [gondor] companion who is no Man, and one who is both.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, guard, dead));
		// Neither a Man nor [gondor]: the Dwarf and the Hobbit Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwarf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertAttachedTo(shield, guard);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBeBorneByAManAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var aragorn = scn.GetFreepsCard("aragorn");
		var dwarf = scn.GetFreepsCard("dwarf");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCardsToHand(shield);
		scn.MoveCompanionsToTable(aragorn, dwarf);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();

		scn.FreepsPlayCard(shield);
		// "A Man" read on its own, as the original's "Bearer must be a Man" was: a Man ally may bear it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(dwarf));
		scn.FreepsChooseCard(barliman);

		assertAttachedTo(shield, barliman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void MinionArcheryTotalIsOneLessAndFellowshipArcheryTotalIsUnchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var frodo = scn.GetRingBearer();
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCardsToHand(shield);
		scn.MoveCompanionsToTable(aragorn, legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);

		// Two Shadow archers; Legolas is the one Free Peoples archer.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(shield);
		assertAttachedTo(shield, aragorn);

		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// One wound for the minions' two archers.
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// One wound for the fellowship's one archer.
		scn.ShadowChooseCard(marksman1);
		assertInDiscard(marksman1);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
