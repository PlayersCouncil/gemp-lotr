package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_226_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("terror", "1_226");

					put("otsea", "1_235");      // Úlairë Otsëa: [wraith] Nazgûl, strength 9, vitality 3; a Shadow ability used only on purpose
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, vitality 3; his texts need 3 or 5 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, vitality 2, so it could exert

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, vitality 3; a fellowship ability
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, vitality 2; a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheirPowerIsinTerrorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Their Power Is in Terror
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a Nazgûl to wound every ally.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("terror");

		assertEquals("Their Power Is in Terror", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheChosenNazgulToWoundEveryAllyAndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var otsea = scn.GetShadowCard("otsea");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(terror);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir, barliman);

		scn.StartGame();
		// Barliman has one vitality left: a wound kills him, where an exertion could not be made.
		scn.AddWoundsToChar(barliman, 1);
		assertEquals(1, scn.GetWoundsOn(barliman));
		scn.MoveMinionsToTable(otsea, khamul, hound);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(terror));

		scn.ShadowPlayCard(terror);
		// Either Nazgûl, of either culture, may pay; the [wraith] Orc may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(otsea, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(khamul);
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(hound));
		// Both allies are wounded, and Barliman is killed by his; the companions are not touched.
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertInZone(Zone.DEAD, barliman);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(terror);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void IsNotPlayableWhileTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var otsea = scn.GetShadowCard("otsea");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(terror);

		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);
		assertInHand(terror);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, hound);
		// Vitality 3 with 2 wounds: Otsëa cannot exert; the Hound beside him could.
		scn.AddWoundsToChar(otsea, 2);
		assertEquals(2, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(hound));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(terror));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertInHand(terror);
	}

	@Test
	public void WithNoAllyInPlayItIsStillPlayedAndTheNazgulExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var terror = scn.GetShadowCard("terror");
		var otsea = scn.GetShadowCard("otsea");
		scn.MoveCardsToHand(terror);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(otsea));
		assertTrue(scn.ShadowPlayAvailable(terror));

		// Otsëa is the one Nazgûl: he exerts unasked.
		scn.ShadowPlayCard(terror);
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(terror);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
