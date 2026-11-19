package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("burdens", "2_11");
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion, vitality 3; a skirmish ability only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion of another title, vitality 2, no text in play
					put("grimir", "1_17");      // Grimir, Dwarven Elder: Dwarf ally, vitality 3; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Dwarf; a maneuver ability used only on purpose
					put("endurance", "2_4");    // Endurance of Dwarves: Free Peoples condition on a Dwarf, vitality +1; a skirmish ability only

					put("evil1", "1_283");      // You Bring Great Evil: [sauron] condition on a companion; bearer cannot heal at a sanctuary
					put("evil2", "1_283");
					put("breath", "1_207");     // Black Breath: [wraith] condition, on any character; bearer cannot heal
					put("poison", "8_28");      // Spider Poison: [gollum] possession; its bearer exerts when assigned
					put("storm", "1_130");      // No Ordinary Storm: [isengard] support condition, no weather; answers a weather played
					put("chill", "1_134");      // Saruman's Chill: [isengard] weather condition on a site; Hobbits exert moving from it
					put("urgency", "19_17");    // Urgency: [isengard] weather condition in the support area; twilight per Ent at a move
					put("troop", "1_177");      // Goblin Patrol Troop: minion, no text: a maneuver phase

					// Draw deck fillers: no text at all.
					put("filler1", "1_177");
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
					put("filler5", "1_177");
					put("filler6", "1_177");
					put("filler7", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MakeLightofBurdensStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Make Light of Burdens
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br><b>Maneuver:</b> Exert a Dwarf companion and discard the top 3 cards from your draw deck to discard either a Shadow condition from a Dwarf or a weather condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("burdens");

		assertEquals("Make Light of Burdens", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingADwarfCompanionAndDiscardingThreeCardsDiscardsAShadowConditionOnADwarfAndThenAWeather() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var endurance = scn.GetFreepsCard("endurance");
		scn.MoveCardsToSupportArea(burdens);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir);

		var evil1 = scn.GetShadowCard("evil1");
		var evil2 = scn.GetShadowCard("evil2");
		var breath = scn.GetShadowCard("breath");
		var poison = scn.GetShadowCard("poison");
		var storm = scn.GetShadowCard("storm");
		var chill = scn.GetShadowCard("chill");
		var urgency = scn.GetShadowCard("urgency");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"),
				scn.GetFreepsCard("filler4"), scn.GetFreepsCard("filler5"), scn.GetFreepsCard("filler6"));
		scn.AttachCardsTo(gimli, evil1, poison);
		scn.AttachCardsTo(grimir, breath);
		scn.AttachCardsTo(aragorn, evil2);
		scn.AttachCardsTo(guard, endurance);
		scn.MoveCardsToSupportArea(storm, urgency);
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.AttachCardsTo(scn.GetCurrentSite(), chill);
		scn.FreepsRefreshDecision();

		assertEquals(6, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInPlay(evil1, evil2, breath, poison, storm, chill, urgency, endurance);
		assertTrue(scn.FreepsActionAvailable(burdens));

		scn.FreepsUseCardAction(burdens);
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(grimir, aragorn));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsDiscardCount());

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(evil1, breath, chill, urgency));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(evil2, endurance, poison, storm, troop));
		scn.FreepsChooseCard(breath);

		assertInDiscard(breath);
		assertInPlay(evil1, evil2, poison, storm, chill, urgency, endurance);

		// Used again in the same phase, for the other alternative.
		scn.ShadowPass();
		scn.FreepsUseCardAction(burdens);
		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetFreepsDeckCount());
		scn.FreepsChooseCard(chill);

		assertInDiscard(chill);
		assertInPlay(evil1, urgency);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheOnlyDwarfCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(burdens);
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		var evil1 = scn.GetShadowCard("evil1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AttachCardsTo(gimli, evil1);
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(troop);
		assertInPlay(evil1);
		// Gimli, vitality 3 with 2 wounds, cannot exert; Grimir (an ally) and Aragorn (no Dwarf) could.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.GetFreepsDeckCount() >= 3);

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsActionAvailable(burdens));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoCardsInTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var filler1 = scn.GetFreepsCard("filler1");
		var filler2 = scn.GetFreepsCard("filler2");
		var filler3 = scn.GetFreepsCard("filler3");
		scn.MoveCardsToSupportArea(burdens);
		scn.MoveCompanionsToTable(gimli);

		var evil1 = scn.GetShadowCard("evil1");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SetFreepsDrawDeck(filler1, filler2);
		scn.AttachCardsTo(gimli, evil1);
		scn.MoveMinionsToTable(troop);
		assertInPlay(evil1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(burdens));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.MoveCardsToTopOfDeck(filler3);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsActionAvailable(burdens));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNothingItCanDiscardTheCostsArePaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var burdens = scn.GetFreepsCard("burdens");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var endurance = scn.GetFreepsCard("endurance");
		scn.MoveCardsToSupportArea(burdens);
		scn.MoveCompanionsToTable(gimli, aragorn);

		var evil2 = scn.GetShadowCard("evil2");
		var poison = scn.GetShadowCard("poison");
		var storm = scn.GetShadowCard("storm");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"),
				scn.GetFreepsCard("filler4"));
		scn.AttachCardsTo(gimli, endurance, poison);
		scn.AttachCardsTo(aragorn, evil2);
		scn.MoveCardsToSupportArea(storm);
		scn.MoveMinionsToTable(troop);
		assertInPlay(endurance, poison, evil2, storm);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(4, scn.GetFreepsDeckCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsActionAvailable(burdens));
		scn.FreepsUseCardAction(burdens);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetFreepsDeckCount());
		assertInPlay(endurance, poison, evil2, storm);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
