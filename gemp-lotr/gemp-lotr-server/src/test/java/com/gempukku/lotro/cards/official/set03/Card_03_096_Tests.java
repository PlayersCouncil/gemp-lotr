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

public class Card_03_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pillager", "3_96");

					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf, strength 6; a skirmish ability never used here
					put("armor1", "1_8");       // Dwarven Armor: Free Peoples possession; about being overwhelmed only, changes no number
					put("armor2", "1_8");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcPillagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: Orc Pillager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: For each Free Peoples card borne by a character this minion is skirmishing, that character is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pillager");

		assertEquals("Orc Pillager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AHinderedFreePeoplesCardBorneByTheCharacterItSkirmishesIsCounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pillager = scn.GetShadowCard("pillager");
		var gimli = scn.GetFreepsCard("gimli");
		var armor1 = scn.GetFreepsCard("armor1");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveCompanionsToTable(gimli);
		// Two Free Peoples cards on one character; the second copy is put there by the rig.
		scn.AttachCardsTo(gimli, armor1, armor2);

		scn.StartGame();
		scn.MoveMinionsToTable(pillager);
		scn.SkipToAssignments();

		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsAssignAndResolve(gimli, pillager);

		// 6 - 2
		assertEquals(4, scn.GetStrength(gimli));

		// A card that is hindered is still borne, and "a Free Peoples card" with nothing more said of it
		// reaches a hindered card.
		scn.HinderCard(armor2);
		assertTrue(scn.IsHindered(armor2));
		assertAttachedTo(armor2, gimli);
		// 6 - 2
		assertEquals(4, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
