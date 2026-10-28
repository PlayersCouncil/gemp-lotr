package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_12_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("thrarin", "12_15");

					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion with no damage bonus; strength +2 against an Orc only
					put("armor", "1_8");          // Dwarven Armor: [dwarven] possession (armor), bearer must be a Dwarf; changes no number
					put("cloak", "1_42");         // Elven Cloak: [elven] possession (cloak), bearer must be a companion; the minion archery total -1
					put("ring", "9_9");           // Ring of Retribution: [dwarven] artifact (ring), bearer must be a Dwarf; a response used only on purpose
					put("axe", "1_9");            // Dwarven Axe: [dwarven] possession (hand weapon); its trigger answers a skirmish won
					put("endurance", "2_4");      // Endurance of Dwarves: [dwarven] condition, bearer must be a Dwarf; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThrarinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Thrarin, Smith of Erebor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: For each artifact and possession Thrarin bears, he is <b>damage +1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("thrarin");

		assertEquals("Thrarin", card.getBlueprint().getTitle());
		assertEquals("Smith of Erebor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void APossessionPlayedOnThrarinMakesHimDamagePlus1AndNotTheOtherDwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var farin = scn.GetFreepsCard("farin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(thrarin, farin);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		assertInZone(Zone.HAND, armor);
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));

		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);
		assertTrue(scn.FreepsHasCardChoiceAvailable(thrarin, farin));
		scn.FreepsChooseCard(thrarin);

		assertAttachedTo(armor, thrarin);
		assertEquals(1, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachArtifactAndPossessionHeBearsCountsAndAConditionDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var thrarin = scn.GetFreepsCard("thrarin");
		var farin = scn.GetFreepsCard("farin");
		var armor = scn.GetFreepsCard("armor");
		var cloak = scn.GetFreepsCard("cloak");
		var ring = scn.GetFreepsCard("ring");
		var axe = scn.GetFreepsCard("axe");
		var endurance = scn.GetFreepsCard("endurance");
		scn.MoveCompanionsToTable(thrarin, farin);

		scn.StartGame();

		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));

		// A condition is no artifact or possession.
		scn.AttachCardsTo(thrarin, endurance);
		assertAttachedTo(endurance, thrarin);
		assertEquals(0, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));

		// Two possessions of two cultures and an artifact, beside the condition: 3.
		scn.AttachCardsTo(thrarin, armor, cloak, ring);
		assertAttachedTo(armor, thrarin);
		assertAttachedTo(cloak, thrarin);
		assertAttachedTo(ring, thrarin);
		assertEquals(3, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));

		// Another Dwarf bearing a possession is not made damage +1.
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		scn.AttachCardsTo(farin, axe);
		assertAttachedTo(axe, farin);
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.DAMAGE));
		assertEquals(3, scn.GetKeywordCount(thrarin, Keyword.DAMAGE));
	}
}
