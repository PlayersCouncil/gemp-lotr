package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("erkenbrand", "102_47");
					put("eowyn", "5_122");
					put("elite", "4_265");
					put("rider", "4_286");
					put("guard", "5_83");
					put("veteran", "7_246");
					put("dwarfguard", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ErkenbrandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Erkenbrand, Kinsman of the House of Eorl
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: When you play Erkenbrand, remove up to 3 non-unique [rohan] companions in the dead pile from the game. 
		* 	Each exhausted companion gains <b>valiant</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("erkenbrand");

		assertEquals("Erkenbrand", card.getBlueprint().getTitle());
		assertEquals("Kinsman of the House of Eorl", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet()); 
	}

	@Test
	public void ErkenbrandBanishes3NonUniqueRohanCompanionsFromDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var eowyn = scn.GetFreepsCard("eowyn");
		var elite = scn.GetFreepsCard("elite");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var veteran = scn.GetFreepsCard("veteran");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCardsToDeadPile(eowyn, elite, rider, guard, veteran, dwarfguard);
		scn.MoveCardsToHand(erkenbrand);

		scn.StartGame();

		assertEquals(Zone.DEAD, eowyn.getZone());
		assertEquals(Zone.DEAD, elite.getZone());
		assertEquals(Zone.DEAD, rider.getZone());
		assertEquals(Zone.DEAD, guard.getZone());
		assertEquals(Zone.DEAD, veteran.getZone());
		assertEquals(Zone.DEAD, dwarfguard.getZone());

		scn.FreepsPlayCard(erkenbrand);
		assertEquals(4, scn.FreepsGetCardChoiceCount());
		assertTrue(scn.FreepsHasCardChoicesAvailable(elite, rider, guard, veteran));
		assertTrue(scn.FreepsHasCardChoicesNotAvailable(eowyn, dwarfguard));

		scn.FreepsChooseCards(elite, rider, guard);
		assertEquals(Zone.DEAD, eowyn.getZone());
		assertEquals(Zone.REMOVED, elite.getZone());
		assertEquals(Zone.REMOVED, rider.getZone());
		assertEquals(Zone.REMOVED, guard.getZone());
		assertEquals(Zone.DEAD, veteran.getZone());
		assertEquals(Zone.DEAD, dwarfguard.getZone());
	}

	@Test
	public void ErkenbrandMakesExhaustedCompanionsValiant() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var erkenbrand = scn.GetFreepsCard("erkenbrand");
		var elite = scn.GetFreepsCard("elite");
		var rider = scn.GetFreepsCard("rider");
		var guard = scn.GetFreepsCard("guard");
		var veteran = scn.GetFreepsCard("veteran");
		var dwarfguard = scn.GetFreepsCard("dwarfguard");
		scn.MoveCompanionsToTable(elite, rider, guard, dwarfguard);
		scn.MoveCardsToHand(erkenbrand);

		scn.StartGame();

		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(elite, 2);
		scn.AddWoundsToChar(rider, 1);

		assertEquals(1, scn.GetVitality(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(1, scn.GetVitality(elite));
		assertFalse(scn.HasKeyword(elite, Keyword.VALIANT));
		assertEquals(2, scn.GetVitality(rider));
		assertFalse(scn.HasKeyword(rider, Keyword.VALIANT));
		assertEquals(2, scn.GetVitality(dwarfguard));
		assertFalse(scn.HasKeyword(dwarfguard, Keyword.VALIANT));

		scn.FreepsPlayCard(erkenbrand);
	}
}
