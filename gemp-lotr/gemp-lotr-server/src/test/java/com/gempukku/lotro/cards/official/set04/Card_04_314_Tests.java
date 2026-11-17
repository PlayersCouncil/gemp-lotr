package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_314_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pippin", "4_314");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used
					put("merry", "1_303");      // Merry, From O'er the Brandywine: another [shire] Hobbit companion; +2 only with a weapon

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3; no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Pippin, Woolly-footed Rascal
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Skirmish:</b> Add a burden to discard Pippin. Any Shadow player may remove (2) to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pippin");

		assertEquals("Pippin", card.getBlueprint().getTitle());
		assertEquals("Woolly-footed Rascal", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void SkirmishAbilityAddsABurdenToDiscardPippinWhenTheShadowPlayerDoesNotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(pippin, aragorn, merry);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, pippin, merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// The text names no skirmish: Pippin need not be in one.
		assertTrue(scn.FreepsActionAvailable(pippin));

		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Discarded, not killed: to the discard pile (CR 4.2 "discard"). The other Hobbit stays.
		assertInZone(Zone.DISCARD, pippin);
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(1, scn.GetBurdens());
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ShadowPlayerMayRemove2ToKeepPippinAndTheBurdenStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(3);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(pippin);
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// 3 - 2 removed by the Shadow player; the burden is not taken back.
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithLessThan2InThePoolTheShadowPlayerIsNotAskedAndPippinIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(1);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(pippin);
		assertEquals(1, scn.GetBurdens());
		assertFalse(scn.ShadowDecisionAvailable("prevent"));

		assertInZone(Zone.DISCARD, pippin);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(pippin));
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertEquals(0, scn.GetBurdens());
	}
}
