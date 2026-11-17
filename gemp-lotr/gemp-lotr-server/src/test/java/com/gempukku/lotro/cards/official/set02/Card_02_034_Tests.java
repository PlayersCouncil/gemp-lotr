package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_034_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("seeitdone", "2_34");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4
					// Pippin, Trollslayer: a [gondor] Hobbit companion, vitality 4; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Gondorian Merchant: a [gondor] ally, vitality 1; a maneuver ability used only on purpose
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so the Merchant can exert; its
					// response answers a wound to its bearer only
					put("narsil", "9_34");
					// Gimli, Son of Glóin: a [dwarven] companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("gimli", "1_13");
					// Thrarin, Dwarven Smith: a [dwarven] ally, vitality 2; a maneuver ability used only on purpose
					put("thrarin", "1_27");

					// Uruk Fighter: strength 8, damage +1; his maneuver ability needs 5 companions
					put("fighter", "1_146");
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text; beats Aragorn without overwhelming him
					// Tower Assassin: "Maneuver: Exert Tower Assassin to wound an ally."
					put("assassin", "2_93");
					// Corpse Lights: strength 7, vitality 2; a skirmish ability used only on purpose
					put("lights", "6_99");
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so his play trigger is not reached
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GondorWillSeeItDoneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Gondor Will See It Done
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a companion is about to take a wound, exert a [gondor] companion to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("seeitdone");

		assertEquals("Gondor Will See It Done", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAGondorCompanionToPreventThatOneWoundToAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var seeitdone = scn.GetFreepsCard("seeitdone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var gimli = scn.GetFreepsCard("gimli");
		var fighter = scn.GetShadowCard("fighter");
		scn.MoveCompanionsToTable(aragorn, pippin, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(seeitdone);

		scn.StartGame();
		assertAttachedTo(narsil, merchant);
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, fighter);
		scn.FreepsResolveSkirmish(gimli);

		// The Fighter's 8 against Gimli's 6, and damage +1: two wounds are due, one at a time.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));

		scn.FreepsPlayCard(seeitdone);
		// A [gondor] companion of either race pays; not the [gondor] ally who could exert, nor a companion of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merchant, gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertInDiscard(seeitdone);

		// That wound is prevented and the second is not.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGondorCompanionsOwnWoundIsPreventedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeitdone = scn.GetFreepsCard("seeitdone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveCardsToHand(seeitdone);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		// The Troop's 13 against Aragorn's 8: one wound is due.
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(seeitdone));

		scn.FreepsPlayCard(seeitdone);
		scn.FreepsChooseCard(pippin);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(seeitdone);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForAWoundToAnAllyOrToAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeitdone = scn.GetFreepsCard("seeitdone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		var assassin = scn.GetShadowCard("assassin");
		var lights = scn.GetShadowCard("lights");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(seeitdone);

		scn.StartGame();
		assertInHand(seeitdone);
		scn.MoveMinionsToTable(assassin, lights, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Thrarin, the one ally, is about to take the Assassin's wound: no companion is, and the event is not offered.
		scn.ShadowUseCardAction(assassin);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertFalse(scn.FreepsPlayAvailable(seeitdone));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		scn.PassManeuverActions();
		scn.SkipToAssignments();
		// Gimli's skirmish waits, so that the moment after Aragorn's is the choice of the next skirmish.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lights}, new PhysicalCardImpl[]{gimli, runner});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn 8 against Corpse Lights' 7: the minion takes the wound, and the event is not offered.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(lights));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsPlayAvailable(seeitdone));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertInHand(seeitdone);
	}

	@Test
	public void NotOfferedWhileTheOnlyGondorCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var seeitdone = scn.GetFreepsCard("seeitdone");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var gimli = scn.GetFreepsCard("gimli");
		var fighter = scn.GetShadowCard("fighter");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToHand(seeitdone);

		scn.StartGame();
		assertAttachedTo(narsil, merchant);
		assertInHand(seeitdone);
		// Vitality 4 with 3 wounds: exhausted.  The Merchant could exert, but he is no companion.
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.MoveMinionsToTable(fighter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, fighter);
		scn.FreepsResolveSkirmish(gimli);

		// The Fighter's 8 against Gimli's 6, damage +1: both wounds land, and nothing is offered before either.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsPlayAvailable(seeitdone));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInHand(seeitdone);
		assertEquals(3, scn.GetWoundsOn(aragorn));
	}
}
