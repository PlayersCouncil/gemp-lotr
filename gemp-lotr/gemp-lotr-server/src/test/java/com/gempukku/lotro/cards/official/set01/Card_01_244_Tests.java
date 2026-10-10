package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_244_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "1_244");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetAssignmentScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defense", "1_244");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the bearer, a companion who is not the Ring-bearer
					put("savage", "1_151");     // Uruk Savage: the minion the bearer is assigned to
					put("runner", "1_178");     // Goblin Runner: a minion for the Ring-bearer, who does not bear the condition

					// Two cards for the Free Peoples hand, one short of the three a discard would take
					put("gimli", "1_13");
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DesperateDefensebroftheRingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Desperate Defense of the Ring
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: 
		 * Game Text: To play, exert a [sauron] Orc. Plays on a companion.<br>Each time bearer is assigned to a skirmish, the Free Peoples player chooses to either discard 3 cards from hand or add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Desperate Defense of the Ring", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithFewerThanThreeCardsInHandBearerAssignedToASkirmishAddsABurdenUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAssignmentScenario();

		var defense = scn.GetShadowCard("defense");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveCardsToHand(gimli, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();

		assertAttachedTo(defense, aragorn);
		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());

		// The bearer and the Ring-bearer are assigned in the one answer.  Three cards cannot be discarded from
		// a hand of two, so there is nothing to choose: one burden, for the bearer's assignment and not for
		// the other, and the game has gone on to the skirmishes.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, savage }, new PhysicalCardImpl[] { frodo, runner });
		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetFreepsHandCount());
		assertInHand(gimli, sam);
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
