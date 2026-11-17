package com.gempukku.lotro.cards.official.set14;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_14_004_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gildor", "14_4");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally; a maneuver ability only

					put("troop1", "1_177");     // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1 only
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, no text
					put("poison", "8_28");      // Spider Poison: [gollum] possession in the support area, no minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GildorInglorionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 14
		 * Name: Gildor Inglorion, Of the House of Finrod
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: At the start of each archery phase, if you can spot more minions than companions, each of your companions is an <b>archer</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gildor");

		assertEquals("Gildor Inglorion", card.getBlueprint().getTitle());
		assertEquals("Of the House of Finrod", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfTheArcheryPhaseWithMoreMinionsThanCompanionsEachCompanionIsAnArcherForThatPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gildor = scn.GetFreepsCard("gildor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gildor, aragorn);
		// An ally, who is no companion: counted among the companions, he would make 4 against 4.
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		// Four minions against three companions, laid in the fellowship phase of the turn they stand in.
		scn.MoveMinionsToTable(troop1, troop2, savage, brigand);

		// The phase before: nobody is an archer yet.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.HasKeyword(gildor, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		// An Elf, the Hobbit Ring-bearer and a [gondor] Man: every companion; not the ally, nor a minion.
		assertTrue(scn.HasKeyword(gildor, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(thrarin, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(troop1, Keyword.ARCHER));
		// One for each of the three archers.
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		scn.SkipToArcheryWounds();
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.ShadowChooseCard(troop1);
		scn.ShadowChooseCard(troop2);
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(1, scn.GetWoundsOn(savage));

		// The archery phase is over, and the keyword with it.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertFalse(scn.HasKeyword(gildor, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithAsManyMinionsAsCompanionsNobodyBecomesAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gildor = scn.GetFreepsCard("gildor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gildor, aragorn);
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		var poison = scn.GetShadowCard("poison");
		// Three minions against three companions, and a Shadow card in play that is no minion.
		scn.MoveMinionsToTable(troop1, troop2, savage);
		scn.MoveCardsToSupportArea(poison);
		assertInZone(Zone.SUPPORT, poison);

		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.HasKeyword(gildor, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheArcheryPhaseOfTheOwnersNextTurnMakesEachCompanionAnArcherAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gildor = scn.GetFreepsCard("gildor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(gildor, aragorn);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveMinionsToTable(troop1, troop2, savage, brigand);

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.HasKeyword(gildor, Keyword.ARCHER));
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The minions are taken off the table so that the turn ends with no skirmish; player 2's turn has no
		// minion and so no archery phase.
		scn.MoveCardsToDiscard(troop1, troop2, savage, brigand);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		// The owner's next turn: nobody is an archer until its archery phase starts.
		assertFalse(scn.HasKeyword(gildor, Keyword.ARCHER));
		scn.MoveMinionsToTable(troop1, troop2, savage, brigand);
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.HasKeyword(gildor, Keyword.ARCHER));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.HasKeyword(gildor, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(frodo, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
