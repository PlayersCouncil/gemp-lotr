package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_191_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scout1", "1_191");
					put("scout2", "1_191");

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, cost 6, site 4, no text
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an archery ability only
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, home 6F; an archery ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion; a skirmish ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MoriaScoutStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Moria Scout
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: When you play this minion, spot an Elf to add (2).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scout1");

		assertEquals("Moria Scout", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WithAnElfCompanionEachScoutAddsTwoForItsOwnPlayAndNotForAnotherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var scout2 = scn.GetShadowCard("scout2");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveCardsToHand(scout1, scout2, troop);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 3 companions + 2 for Ettenmoors
		assertEquals(25, scn.GetTwilight());

		scn.ShadowPlayCard(scout1);
		// 25 - 2 to play - 2 roaming (site 4 minion at site 2) + 2
		assertEquals(23, scn.GetTwilight());

		// The second copy adds for its own play only, and the first does not answer it.
		scn.ShadowPlayCard(scout2);
		// 23 - 2 - 2 + 2
		assertEquals(21, scn.GetTwilight());

		// Another minion played beside the Scouts adds nothing.
		scn.ShadowPlayCard(troop);
		// 21 - 6 to play - 2 roaming
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnElfAllyIsSpottedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(scout1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 2 companions + 2 for Ettenmoors
		assertEquals(24, scn.GetTwilight());

		scn.ShadowPlayCard(scout1);
		// 24 - 2 to play - 2 roaming + 2
		assertEquals(22, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoElfToSpotNothingIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scout1 = scn.GetShadowCard("scout1");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		// The Shadow player's own Elf ally is inactive on the other player's turn.
		var shadowOrophin = scn.GetShadowCard("orophin");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToSupportArea(shadowOrophin);
		scn.MoveCardsToHand(scout1);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		// 20 set + 3 companions + 2 for Ettenmoors
		assertEquals(25, scn.GetTwilight());

		scn.ShadowPlayCard(scout1);
		// 25 - 2 to play - 2 roaming, and nothing added
		assertEquals(21, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
