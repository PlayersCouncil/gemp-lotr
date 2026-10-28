package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("townsman", "12_61");
					put("townsman2", "12_61");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man; his maneuver ability is used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: Dwarf ally; his maneuver ability is used only on purpose
					put("worker", "15_135");    // Rohan Worker: a follower, no character; its texts answer a transfer and a won skirmish
					// Uruk Pursuer: an [isengard] Uruk-hai minion, played from hand; its texts are about unbound
					// Hobbits being discarded or killed, and none is
					put("pursuer", "4_190");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrookedTownsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Crooked Townsman
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 4
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: This minion is strength +1 for each other character you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("townsman");

		assertEquals("Crooked Townsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void StrengthPlus1ForEachOtherCharacterOfEitherSideAndNotForHimselfOrAFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var townsman = scn.GetShadowCard("townsman");
		var townsman2 = scn.GetShadowCard("townsman2");
		var pursuer = scn.GetShadowCard("pursuer");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var worker = scn.GetFreepsCard("worker");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(worker);
		scn.MoveCardsToHand(pursuer);

		scn.StartGame();
		scn.MoveMinionsToTable(townsman, townsman2);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, worker);
		assertInZone(Zone.HAND, pursuer);

		// Other characters: Frodo, Aragorn, Thrarin and the other Townsman. Not himself, and not the follower.
		assertEquals(8, scn.GetStrength(townsman));
		assertEquals(8, scn.GetStrength(townsman2));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(pursuer);

		// One more character, a minion of another culture and race: 4 + 5.
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertEquals(9, scn.GetStrength(townsman));
		assertEquals(9, scn.GetStrength(townsman2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
