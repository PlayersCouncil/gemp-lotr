package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_11_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "11_22");
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally; a start-of-turn heal and a fellowship ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion who is no archer; +3 against a Nazgûl only
					put("gimli", "1_13");       // Gimli, Son of Glóin: a companion who is no Elf; a skirmish ability only
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Legolas, Woodland Emissary
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: At the start of the archery phase, you may spot another Elf to exert a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Woodland Emissary", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void AtTheStartOfTheArcheryPhaseSpottingAnElfAllyExertsAChosenMinionAndNotAtTheStartOfManeuver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(troop1, troop2, savage);
		// Vitality 3 with 2 wounds: this Troop is exhausted and cannot exert.
		scn.AddWoundsToChar(troop2, 2);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop2, legolas, gimli, frodo, elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void AnElfCompanionCanBeSpottedAndDecliningExertsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(troop1, savage);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWhenLegolasIsTheOnlyElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(legolas, gimli);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveMinionsToTable(troop1);

		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWhenEveryMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(troop1, savage);
		// Vitality 3 with 2 wounds each: no minion can exert, so the exertion would do nothing.
		scn.AddWoundsToChar(troop1, 2);
		scn.AddWoundsToChar(savage, 2);
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(savage));

		scn.SkipToPhase(Phase.ARCHERY);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(legolas));
		assertEquals(2, scn.GetWoundsOn(troop1));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheArcheryPhaseOfTheOwnersNextTurnOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(troop1, savage);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(troop1);
		assertEquals(1, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The minions are taken off the table so that the turn ends with no skirmish; player 2's turn has no
		// minion and so no archery phase.
		scn.MoveCardsToDiscard(troop1, savage);
		scn.SkipToOtherPlayersTurn();
		scn.SkipToOtherPlayersTurn();

		scn.MoveMinionsToTable(troop1, savage);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(legolas));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}
}
