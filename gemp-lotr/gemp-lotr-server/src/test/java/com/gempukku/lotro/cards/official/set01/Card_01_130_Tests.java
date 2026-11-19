package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_130_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("storm", "1_130");
					put("storm2", "1_130");     // a second copy: a condition that is no weather, played beside the first

					put("urgency", "19_17");    // Urgency: [isengard] weather condition in the support area, cost 0; its move trigger counts Ents (no move is made here)
					put("snows", "1_138");      // Saruman's Snows: [isengard] weather condition played on a site, cost 2; to play, exert an [isengard] minion
					put("caradhras", "1_124");  // Cruel Caradhras: [isengard] weather EVENT (Maneuver), cost 2: exert an [isengard] minion to make the opponent exert the Ring-bearer or add a burden
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, vitality 3, damage +1 and nothing else; pays the weathers' exertions

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] companion, vitality 4; a strength bonus only
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] ally, vitality 2; a fellowship ability used only on purpose
					put("ent", "6_27");         // Ent Avenger: [gandalf] companion, vitality 4; strength +2 only while 3 Ents are spotted (none are)
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion; a maneuver ability used only on purpose
					put("orophin", "1_56");     // Orophin: [elven] ally; an archery ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoOrdinaryStormStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: No Ordinary Storm
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time you play a weather condition, exert a [gandalf] companion or [gandalf] ally.
		*/

		var scn = GetScenario();

		var storm = scn.GetFreepsCard("storm");

		assertEquals("No Ordinary Storm", storm.getBlueprint().getTitle());
		assertNull(storm.getBlueprint().getSubtitle());
		assertFalse(storm.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, storm.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, storm.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, storm.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(storm, Keyword.SUPPORT_AREA));
		assertEquals(1, storm.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachWeatherConditionPlayedExertsAGandalfCompanionOrAllyOfTheShadowPlayersChoosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetShadowCard("storm");
		var urgency = scn.GetShadowCard("urgency");
		var snows = scn.GetShadowCard("snows");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(storm);
		scn.MoveCardsToHand(urgency, snows);

		var frodo = scn.GetRingBearer();
		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		var ent = scn.GetFreepsCard("ent");
		scn.MoveCompanionsToTable(gandalf, aragorn, ent);
		scn.MoveAlliesToTable(barliman, orophin);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		// Ent Avenger has 3 of his 4 vitality gone: exhausted, so he cannot exert (a wound would still be taken).
		scn.AddWoundsToChar(ent, 3);
		assertEquals(3, scn.GetWoundsOn(ent));
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(raiders));

		// A weather condition in the support area.
		scn.ShadowPlayCard(urgency);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, barliman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, orophin, ent));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gandalf);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(3, scn.GetWoundsOn(ent));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A second weather condition, played on a site, sets it off again; the same Gandalf exerts.
		scn.ShadowPlayCard(snows);
		scn.ShadowChooseCard(scn.GetSite(2));
		assertEquals(1, scn.GetWoundsOn(raiders));
		assertTrue(scn.ShadowHasCardChoiceAvailable(gandalf, barliman));
		scn.ShadowChooseCard(gandalf);

		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(3, scn.GetWoundsOn(ent));
		assertAttachedTo(snows, scn.GetSite(2));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AConditionThatIsNoWeatherAndAWeatherEventPlayedExertNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var storm = scn.GetShadowCard("storm");
		var storm2 = scn.GetShadowCard("storm2");
		var caradhras = scn.GetShadowCard("caradhras");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCardsToSupportArea(storm);
		scn.MoveCardsToHand(storm2, caradhras);

		var gandalf = scn.GetFreepsCard("gandalf");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(raiders);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(barliman));

		// A condition that is no weather.
		scn.ShadowPlayCard(storm2);
		assertInZone(Zone.SUPPORT, storm2);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A weather card that is no condition.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetBurdens());
		scn.ShadowPlayCard(caradhras);
		scn.FreepsChoose("Add a burden");

		assertEquals(1, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
