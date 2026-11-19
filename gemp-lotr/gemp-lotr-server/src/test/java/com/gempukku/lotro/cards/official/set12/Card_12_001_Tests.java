package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_001_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("argument", "12_1");

					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion; a skirmish ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: a Dwarf ally; a fellowship ability only
					put("legolas", "1_50");     // Legolas, Greenleaf: an Elf companion, no Dwarf; an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose

					// Minions of three races and three cultures, and one with no race.
					put("patrol", "1_177");     // Goblin Patrol Troop: [moria] Orc, no text
					put("trooper", "3_99");     // Orc Trooper: [sauron] Orc; strength +1 for each card in the Shadow hand
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; "Damage +1." is its whole text
					put("troll", "1_165");      // Cave Troll of Moria: [moria] Troll; its other text is about being played
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion with no race; a regroup ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArgumentReadytoHandStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Argument Ready to Hand
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Spot a Dwarf to choose a Shadow player who must spot a race. Discard from play all minions of all other races.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("argument");

		assertEquals("Argument Ready to Hand", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheShadowPlayerSpotsARaceAndEveryMinionOfAnotherRaceIsDiscardedButGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var argument = scn.GetFreepsCard("argument");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(argument);
		assertInHand(argument);

		var patrol = scn.GetShadowCard("patrol");
		var trooper = scn.GetShadowCard("trooper");
		var savage = scn.GetShadowCard("savage");
		var troll = scn.GetShadowCard("troll");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(patrol, trooper, savage, troll, gollum);
		scn.SkipToPhase(Phase.MANEUVER);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol, trooper, savage, troll, gollum);

		assertTrue(scn.FreepsPlayAvailable(argument));
		scn.FreepsPlayCard(argument);

		// The Shadow player spots a race by one of his minions that has one.  Gollum has none to spot
		// (CR 4.2 "Gollum/Sméagol").  The companions' races are not offered (Ketura, F-0643: B).
		assertTrue(scn.ShadowHasCardChoiceAvailable(patrol, trooper, savage, troll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gollum, gimli, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		// Orc, by the [moria] Patrol Troop: the [sauron] Orc stays with it; the Uruk-hai and the Troll go to the
		// discard pile.  Gollum, of no minion race, stays (CR 4.2 "Gollum/Sméagol"), and the companions with him.
		scn.ShadowChooseCard(patrol);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol, trooper, gollum);
		assertInZone(Zone.DISCARD, savage, troll, argument);
		assertInZone(Zone.FREE_CHARACTERS, gimli, aragorn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutADwarfAndADwarfAllyIsEnough() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var argument = scn.GetFreepsCard("argument");
		var grimir = scn.GetFreepsCard("grimir");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToHand(argument);
		assertInHand(argument);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// An Elf, a Man and a Hobbit: no Dwarf.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(argument));

		// A Dwarf ally.
		scn.MoveAlliesToTable(grimir);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(argument));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
