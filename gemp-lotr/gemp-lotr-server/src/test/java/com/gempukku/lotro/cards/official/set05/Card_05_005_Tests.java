package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_005_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "5_5");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetManeuverScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("baruk", "5_5");
					put("gimli", "5_7");        // Gimli, Skilled Defender: a Dwarf, vitality 3; his own text answers a skirmish he wins
					put("guard", "1_7");        // Dwarf Guard: a second Dwarf, so that who exerts is a choice
					// Aragorn, Ranger of the North: no Dwarf.  "Maneuver: Exert Aragorn to make him defender +1 until
					// the regroup phase": a second maneuver action for the Free Peoples player to take.
					put("aragorn", "1_89");
					put("savage", "1_151");     // Uruk Savage: vitality 3, so the wound leaves it in play and the phase goes on
					put("troop", "1_177");      // Goblin Patrol Troop: a second minion, so that who is wounded is a choice
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BarukKhazadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Baruk Khazâd
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a Dwarf to wound a minion. You may take the next maneuver action.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Baruk Khazâd", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AfterItThePlayerTakesTheNextManeuverActionBeforeTheShadowPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetManeuverScenario();

		var baruk = scn.GetFreepsCard("baruk");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(baruk));
		scn.FreepsPlayCard(baruk);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInDiscard(baruk);
		// By the action procedure the Shadow player would act now.  The next maneuver action is the Free Peoples
		// player's again, and the Shadow player is asked nothing.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.ShadowAnyDecisionsAvailable());

		// He takes it.  That was the one action the event gave: after it the Shadow player is the one asked.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(aragorn));
		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}
}
