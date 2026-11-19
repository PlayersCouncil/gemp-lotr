package com.gempukku.lotro.cards.official.set00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_00_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("anarion", "0_58");
					put("gimli", "1_13");        // Gimli, Son of Gloin: a Dwarf companion of another culture; his own skirmish ability is used only on purpose
					put("swordarm", "1_116");    // Swordarm of the White Tower: [gondor] Skirmish event, cost 0, a [gondor] companion +2 (+4 if defender)
					put("sentinels", "2_37");    // Sentinels of Numenor: [gondor] Skirmish event, cost 0, a [gondor] companion +2 (+4 in a fierce skirmish)
					put("valor", "1_103");       // Elendil's Valor: a [gondor] event of another phase (Maneuver), cost 0
					put("axe", "1_3");           // Axe Strike: a Skirmish event of another culture ([dwarven]), cost 0, a Dwarf +2

					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("wildmen", "4_4");       // Band of Wild Men: [dunland] Man, strength 11, vitality 2; a trigger on winning a skirmish only
					put("runner", "1_178");      // Goblin Runner: [moria] Orc, strength 5, vitality 1; a trigger on being played only (cheated in)
					put("gollum", "7_59");       // Gollum, Vile Creature: strength 5, vitality 4; his skirmish ability is used only on purpose
					put("controlled", "15_41");  // Controlled By The Ring: on Gollum; "Skirmish: If Gollum is not assigned to a skirmish,
					                             // discard this condition to have him replace a minion skirmishing a companion"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AnarionStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 0
		 * Name: Anárion, Lord of Anórien
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> Play a [gondor] skirmish event to make each minion skirmishing Anárion strength -X, where X is Anárion's vitality.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("anarion");

		assertEquals("Anárion", card.getBlueprint().getTitle());
		assertEquals("Lord of Anórien", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityPlaysAGondorSkirmishEventToMakeEachMinionSkirmishingHimStrengthMinus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anarion = scn.GetFreepsCard("anarion");
		var gimli = scn.GetFreepsCard("gimli");
		var swordarm = scn.GetFreepsCard("swordarm");
		var sentinels = scn.GetFreepsCard("sentinels");
		var valor = scn.GetFreepsCard("valor");
		var axe = scn.GetFreepsCard("axe");
		var troop = scn.GetShadowCard("troop");
		var wildmen = scn.GetShadowCard("wildmen");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(anarion, gimli);
		scn.MoveCardsToHand(swordarm, sentinels, valor, axe);
		assertInZone(Zone.HAND, swordarm, sentinels, valor, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, wildmen, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { anarion, troop }, new PhysicalCardImpl[] { gimli, runner });
		// The minion left over goes to Anarion too: he is skirmishing two.
		scn.ShadowAssignToMinions(anarion, wildmen);
		scn.FreepsResolveSkirmish(anarion);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(anarion));
		assertEquals(7, scn.GetStrength(anarion));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(11, scn.GetStrength(wildmen));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.FreepsActionAvailable(anarion));

		scn.FreepsUseCardAction(anarion);
		// Only a [gondor] event that is a Skirmish event pays.
		assertTrue(scn.FreepsHasCardChoiceAvailable(swordarm, sentinels));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(valor, axe));
		scn.FreepsChooseCard(swordarm);

		// The event is played, and does what it says: Anarion is the only [gondor] companion, +2.
		assertInZone(Zone.DISCARD, swordarm);
		assertInZone(Zone.HAND, sentinels, valor, axe);
		assertEquals(9, scn.GetStrength(anarion));
		// Unwounded, his vitality is 3: each minion skirmishing him is -3.
		assertEquals(10, scn.GetStrength(troop));
		assertEquals(8, scn.GetStrength(wildmen));
		// The Runner is assigned to Gimli, his skirmish still to come: not skirmishing Anarion.
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneWoundTheMinionIsMinus2UntilHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anarion = scn.GetFreepsCard("anarion");
		var gimli = scn.GetFreepsCard("gimli");
		var swordarm = scn.GetFreepsCard("swordarm");
		var sentinels = scn.GetFreepsCard("sentinels");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.MoveCompanionsToTable(anarion, gimli);
		scn.MoveCardsToHand(swordarm, sentinels);
		scn.AddWoundsToChar(anarion, 1);
		assertInZone(Zone.HAND, swordarm, sentinels);
		assertEquals(1, scn.GetWoundsOn(anarion));

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { anarion, troop }, new PhysicalCardImpl[] { gimli, runner });
		scn.FreepsResolveSkirmish(anarion);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(1, scn.GetWoundsOn(anarion));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsUseCardAction(anarion);
		scn.FreepsChooseCard(sentinels);
		assertInZone(Zone.DISCARD, sentinels);
		assertEquals(9, scn.GetStrength(anarion));
		// Vitality 3 with 1 wound: 2.
		assertEquals(11, scn.GetStrength(troop));

		// Still in force at the next decision of that skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(11, scn.GetStrength(troop));

		// The Troop, 11 against 9, wins; in the next skirmish it is back to 13.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(anarion));
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void InAnotherCompanionsSkirmishTheEventIsPlayedAndNoMinionIsWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anarion = scn.GetFreepsCard("anarion");
		var gimli = scn.GetFreepsCard("gimli");
		var swordarm = scn.GetFreepsCard("swordarm");
		var troop = scn.GetShadowCard("troop");
		var wildmen = scn.GetShadowCard("wildmen");

		scn.MoveCompanionsToTable(anarion, gimli);
		scn.MoveCardsToHand(swordarm);
		assertInZone(Zone.HAND, swordarm);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, wildmen);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { anarion, troop }, new PhysicalCardImpl[] { gimli, wildmen });
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(11, scn.GetStrength(wildmen));
		assertTrue(scn.FreepsActionAvailable(anarion));

		scn.FreepsUseCardAction(anarion);
		assertInZone(Zone.DISCARD, swordarm);
		assertEquals(9, scn.GetStrength(anarion));
		// The Wild Men are skirmishing Gimli, and the Troop waits for Anarion's skirmish: neither is skirmishing him.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(11, scn.GetStrength(wildmen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWithNoGondorSkirmishEventInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anarion = scn.GetFreepsCard("anarion");
		var gimli = scn.GetFreepsCard("gimli");
		var valor = scn.GetFreepsCard("valor");
		var axe = scn.GetFreepsCard("axe");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(anarion, gimli);
		scn.MoveCardsToHand(valor, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(anarion, troop);
		scn.FreepsResolveSkirmish(anarion);

		// Axe Strike can be played here (Gimli is a Dwarf), but it is no [gondor] event; Elendil's Valor is no Skirmish event.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(axe));
		assertFalse(scn.FreepsActionAvailable(anarion));
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void AMinionThatComesIntoHisSkirmishAfterTheUseIsNotWeakened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var anarion = scn.GetFreepsCard("anarion");
		var swordarm = scn.GetFreepsCard("swordarm");
		var troop = scn.GetShadowCard("troop");
		var gollum = scn.GetShadowCard("gollum");
		var controlled = scn.GetShadowCard("controlled");

		scn.MoveCompanionsToTable(anarion);
		scn.MoveCardsToHand(swordarm);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, gollum);
		scn.AttachCardsTo(gollum, controlled);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(anarion, troop);
		// Gollum is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(anarion);

		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(gollum));
		scn.FreepsUseCardAction(anarion);
		assertEquals(10, scn.GetStrength(troop));

		assertTrue(scn.ShadowActionAvailable(controlled));
		scn.ShadowUseCardAction(controlled);
		assertTrue(scn.IsCharSkirmishingAgainst(anarion, gollum));
		assertFalse(scn.IsCharSkirmishing(troop));
		// The minions skirmishing him were those there as the ability was used: Gollum is not made weaker, and the
		// Troop it replaced stays so.
		assertEquals(5, scn.GetStrength(gollum));
		assertEquals(10, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
