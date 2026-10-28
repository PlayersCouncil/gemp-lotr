package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_198_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ruffian", "7_198");
					// Úlairë Lemenya, Lieutenant of Morgul: a [wraith] Nazgûl, cost 4, site 3, vitality 2; her text is
					// about companions bearing [wraith] conditions
					put("lemenya", "1_232");
					// Ulaire Attea, Khamul of the East: a Nazgûl who is [raider], vitality 3; his text needs 3 burdens
					put("khamul", "103_61");
					// Morgul Creeper: a [wraith] Orc who is no Nazgûl, vitality 2; its text needs 3 burdens
					put("creeper", "8_75");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc with no text

					// Legolas, Greenleaf: "Archery: Exert Legolas to wound a minion; Legolas does not add to the
					// fellowship archery total."
					put("legolas", "1_50");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: only spotted, for Long I Fell
					// Long I Fell: Skirmish: the Shadow player may make the Free Peoples player wound a minion of his
					// choice, any minion, in the skirmish or not
					put("fell", "4_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulRuffianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Ruffian
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 7
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: While you can spot a Nazgûl, this minion may only take wounds during skirmish phases.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ruffian");

		assertEquals("Morgul Ruffian", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void OnceANazgulIsPlayedRuffianCannotBeWoundedInTheArcheryPhaseThoughOtherMinionsCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var creeper = scn.GetShadowCard("creeper");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(lemenya);
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian, creeper);
		scn.SetTwilight(10);
		assertInHand(lemenya);

		scn.FreepsPass();
		scn.ShadowPlayCard(lemenya);
		assertInPlay(lemenya);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(ruffian));
		assertEquals(0, scn.GetWoundsOn(creeper));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.FreepsUseCardAction(legolas);

		// The Nazgûl and the [wraith] Orc who is no Nazgûl can be wounded; the Ruffian cannot, outside a skirmish phase.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lemenya, creeper));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ruffian));
		scn.FreepsChooseCard(creeper);
		assertEquals(1, scn.GetWoundsOn(creeper));
		assertEquals(0, scn.GetWoundsOn(ruffian));
		assertInPlay(ruffian);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ARaiderNazgulAlsoKeepsWoundsOffRuffianInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var creeper = scn.GetShadowCard("creeper");
		var khamul = scn.GetShadowCard("khamul");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian, creeper, khamul);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(ruffian));
		assertEquals(0, scn.GetWoundsOn(creeper));
		assertEquals(0, scn.GetWoundsOn(khamul));
		scn.FreepsUseCardAction(legolas);

		assertTrue(scn.FreepsHasCardChoiceAvailable(khamul, creeper));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ruffian));
		scn.FreepsChooseCard(khamul);
		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(ruffian));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithNoNazgulButAWraithOrcRuffianIsWoundedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var creeper = scn.GetShadowCard("creeper");
		var troop = scn.GetShadowCard("troop");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian, creeper, troop);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(ruffian));
		scn.FreepsUseCardAction(legolas);

		// The Creeper, a [wraith] Orc, is no Nazgûl to spot.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ruffian, creeper, troop));
		scn.FreepsChooseCard(ruffian);
		// Vitality 1: the wound kills it.
		assertInDiscard(ruffian);
		assertEquals(0, scn.GetWoundsOn(creeper));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WithANazgulRuffianInNoSkirmishIsWoundedDuringAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruffian = scn.GetShadowCard("ruffian");
		var creeper = scn.GetShadowCard("creeper");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(fell);

		scn.StartGame();
		scn.MoveMinionsToTable(ruffian, creeper, lemenya);
		scn.SkipToAssignments();
		// Aragorn skirmishes the Creeper; the Ruffian and Lemenya are in no skirmish.
		scn.FreepsAssignToMinions(aragorn, creeper);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(ruffian));
		assertInPlay(ruffian);

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		// A skirmish phase: the Ruffian may take the wound, though its own skirmish is none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(ruffian, creeper, lemenya));
		scn.FreepsChooseCard(ruffian);
		assertInDiscard(ruffian);
		assertEquals(0, scn.GetWoundsOn(creeper));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
