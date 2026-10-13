package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("durin", "9_3");
					put("gimli", "1_13");       // Gimli, Son of Glóin: Dwarf companion, strength 6; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, cost 1; a fellowship ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: Elf companion, no Dwarf; an archery ability only
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, cost 0, bearer must be a Dwarf; no number
					put("armor2", "1_8");
					// Ring of Artifice: [dwarven] artifact, bearer must be a Dwarf; damage +1, and strength +1 for each
					// [dwarven] artifact in its owner's discard pile, which is empty here.
					put("ring", "17_4");
					// Spider Poison: [gollum] possession; borne by a companion it makes him exert when assigned, nothing else.
					put("poison", "8_28");
					// Endurance of Dwarves: [dwarven] condition, bearer must be a Dwarf; vitality +1 and a skirmish ability.
					put("endurance", "2_4");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DurinIIIStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Durin III, Dwarven Lord
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 7
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: <b>Damage +1</b>.<br>While you can spot 2 Dwarves, Durin III is twilight cost -2.<br>Durin III is strength +1 for each artifact and each possession he bears.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("durin");

		assertEquals("Durin III", card.getBlueprint().getTitle());
		assertEquals("Dwarven Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void DurinCostsTwoLessOnceADwarfCompanionAndADwarfAllyCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var durin = scn.GetFreepsCard("durin");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveCardsToHand(durin, grimir);

		scn.StartGame();

		// One Dwarf, beside an Elf companion and Frodo: no discount.
		assertEquals(4, scn.GetTwilightCost(durin));

		scn.FreepsPlayCard(grimir);
		assertInZone(Zone.SUPPORT, grimir);
		// Gimli, a companion, and Grimir, an ally: 2 Dwarves.
		assertEquals(2, scn.GetTwilightCost(durin));

		int pool = scn.GetTwilight();
		scn.FreepsPlayCard(durin);
		assertInZone(Zone.FREE_CHARACTERS, durin);
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DurinIsStrengthPlus1ForEachArtifactAndEachPossessionHeBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var durin = scn.GetFreepsCard("durin");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var ring = scn.GetFreepsCard("ring");
		var endurance = scn.GetFreepsCard("endurance");
		var poison = scn.GetShadowCard("poison");
		scn.MoveCompanionsToTable(durin, gimli);
		scn.AttachCardsTo(durin, endurance);
		scn.AttachCardsTo(gimli, armor2);
		scn.MoveCardsToHand(armor);

		scn.StartGame();

		// A condition he bears is no artifact or possession; Gimli's own possession is not Durin's.
		assertAttachedTo(endurance, durin);
		assertAttachedTo(armor2, gimli);
		assertEquals(7, scn.GetStrength(durin));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, durin);
		assertEquals(8, scn.GetStrength(durin));
		assertEquals(6, scn.GetStrength(gimli));

		// An artifact, and a possession of the other side: 7 + 3.
		scn.AttachCardsTo(durin, ring, poison);
		assertEquals(0, scn.GetFreepsDiscardCount());
		assertEquals(10, scn.GetStrength(durin));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
