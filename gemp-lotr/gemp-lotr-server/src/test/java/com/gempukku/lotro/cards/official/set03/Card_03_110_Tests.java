package com.gempukku.lotro.cards.official.set03;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_110_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "3_110");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetBurdenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("melilot", "3_110");
					// Desperate Defense of the Ring: a [sauron] condition borne by a companion.  "Each time bearer
					// is assigned to a skirmish, the Free Peoples player chooses to either discard 3 cards from
					// hand or add a burden."
					put("defense", "1_244");
					put("runner", "1_178");     // Goblin Runner: the minion its bearer is assigned to
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MelilotBrandybuckStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Melilot Brandybuck, Merry Dancer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Ally
		 * Subtype: Hobbit
		 * Strength: 2
		 * Vitality: 2
		 * Site Number: 1
		 * Game Text: <b>Response:</b> If a burden is about to be added by a Shadow card, exert Melilot Brandybuck to prevent that burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Melilot Brandybuck", card.getBlueprint().getTitle());
		assertEquals("Merry Dancer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 1)));
	}

	@Test
	public void ResponseExertsMelilotToPreventABurdenAShadowCardIsAboutToAdd() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetBurdenScenario();

		var melilot = scn.GetFreepsCard("melilot");
		var frodo = scn.GetRingBearer();
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveAlliesToTable(melilot);

		scn.StartGame();
		scn.AttachCardsTo(frodo, defense);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(melilot));

		// With no card in hand the burden is all the Free Peoples player can choose, and nothing is asked.
		scn.FreepsAssignToMinions(frodo, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(melilot));
		assertEquals(0, scn.GetBurdens());

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(melilot));
		assertEquals(0, scn.GetBurdens());
		assertAttachedTo(defense, frodo);
		assertTrue(scn.IsCharAssignedAgainst(frodo, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
