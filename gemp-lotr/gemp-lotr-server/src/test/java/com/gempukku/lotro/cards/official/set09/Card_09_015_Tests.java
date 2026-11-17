package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_015_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gilgalad", "9_15");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion; strength +3 against a Nazgul only
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] Elf companion; an archery ability used only on purpose
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an [elven] Elf who is an ally (cheated in); a fellowship draw used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc minion, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai minion, vitality 3; damage +1 is its whole text
					put("dunlending", "4_17");  // Dunlending Savage: [dunland] Man minion, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GilgaladStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Gil-galad, Elven High King
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 5
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 9
		 * Vitality: 4
		 * Resistance: 6
		 * Game Text: To play, spot 2 [elven] companions.<br><b>Regroup:</b> Exert Gil-galad to wound each wounded minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gilgalad");

		assertEquals("Gil-galad", card.getBlueprint().getTitle());
		assertEquals("Elven High King", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilTwoElvenCompanionsAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(gilgalad, legolas);

		scn.StartGame();

		// One [elven] companion, beside an [elven] ally and a [gondor] companion.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(gilgalad));

		scn.FreepsPlayCard(legolas);
		assertTrue(scn.FreepsPlayAvailable(gilgalad));

		scn.FreepsPlayCard(gilgalad);
		assertInZone(Zone.FREE_CHARACTERS, gilgalad);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupAbilityExertsGilgaladToWoundEachWoundedMinionOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var dunlending = scn.GetShadowCard("dunlending");
		scn.MoveCompanionsToTable(gilgalad, aragorn);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop, savage, dunlending);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetWoundsOn(gilgalad));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(gilgalad));

		scn.FreepsUseCardAction(gilgalad);
		assertEquals(1, scn.GetWoundsOn(gilgalad));
		// A wounded [moria] Orc and a wounded [isengard] Uruk-hai, one wound each.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		// Not the unwounded minion, nor a wounded companion.
		assertEquals(0, scn.GetWoundsOn(dunlending));
		assertInPlay(dunlending);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenGilgaladIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gilgalad);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(gilgalad, 3);
		scn.FreepsRefreshDecision();

		// Vitality 4 with 3 wounds: he cannot exert.
		assertEquals(3, scn.GetWoundsOn(gilgalad));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gilgalad));
	}
}
