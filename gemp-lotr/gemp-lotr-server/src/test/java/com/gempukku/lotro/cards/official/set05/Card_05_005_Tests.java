package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
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
					put("baruk", "5_5");
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

	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("baruk", "5_5");
					put("baruk2", "5_5");
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, vitality 3; a skirmish ability only
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally, vitality 3; a fellowship ability only
					// Aragorn, Ranger of the North: no Dwarf, vitality 4.  His own maneuver ability is used only on purpose.
					put("aragorn", "1_89");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, so one wound kills it
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 only
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

		var baruk = scn.GetFreepsCard("baruk");

		assertEquals("Baruk Khazâd", baruk.getBlueprint().getTitle());
		assertNull(baruk.getBlueprint().getSubtitle());
		assertFalse(baruk.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, baruk.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, baruk.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, baruk.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(baruk, Timeword.MANEUVER));
		assertEquals(0, baruk.getBlueprint().getTwilightCost());
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
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, guard, aragorn));
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

	@Test
	public void ADwarfAllyMayPayAndASecondCopyPlayedWithNoMinionLeftWoundsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var baruk = scn.GetFreepsCard("baruk");
		var baruk2 = scn.GetFreepsCard("baruk2");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToHand(baruk, baruk2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		scn.FreepsPlayCard(baruk);

		// The ally is a Dwarf too.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(grimir);

		// The Runner is the one minion, and is chosen unasked: one wound on its single vitality kills it, where an
		// exertion could not have been paid by it at all.
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(runner);
		assertInDiscard(baruk);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The next maneuver action is his: a second copy, with no minion left to wound.  It is played, its Dwarf
		// exerts, and nothing more happens.
		assertTrue(scn.FreepsPlayAvailable(baruk2));
		scn.FreepsPlayCard(baruk2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, grimir));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(baruk2);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileTheOnlyDwarfIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var baruk = scn.GetFreepsCard("baruk");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		// Vitality 3 with 2 wounds: Gimli cannot exert.  Aragorn could, and is no Dwarf.
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(party);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInHand(baruk);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(baruk));
		assertEquals(0, scn.GetWoundsOn(party));
	}

	@Test
	public void HeMayLetTheShadowPlayerTakeTheNextManeuverAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var baruk = scn.GetFreepsCard("baruk");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(baruk);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(party));
		scn.FreepsPlayCard(baruk);
		// Gimli is the one Dwarf and the Raiding Party the one minion: both are chosen unasked.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(party));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// He passes the action the event gave him: the Shadow player is asked next, and nothing else has changed.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
	}
}
