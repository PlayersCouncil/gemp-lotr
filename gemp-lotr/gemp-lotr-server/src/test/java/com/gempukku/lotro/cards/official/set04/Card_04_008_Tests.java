package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strawheads1", "4_8");
					put("strawheads2", "4_8");

					put("savage", "4_17");      // Dunlending Savage: [dunland] Man, strength 11, vitality 1; no text
					put("warrior", "4_18");     // Dunlending Warrior: [dunland] Man, strength 9; an assignment ability that needs an ally to skirmish, not used
					put("saruman", "4_33");     // Saruman, Rabble-rouser: a [dunland] minion who is no Man; may not be assigned to a skirmish
					put("axeman", "4_224");     // Easterling Axeman: a [raider] Man, strength 6; +3 and fierce only at 2 burdens, and there are none

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: a [dwarven] Dwarf companion, strength 6, vitality 3; a skirmish ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: an ally, not a companion; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DeathtotheStrawheadsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Death to the Strawheads
		 * Unique: False
		 * Side: Shadow
		 * Culture: Dunland
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [dunland] Man strength +1 for each companion you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strawheads1");

		assertEquals("Death to the Strawheads", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.DUNLAND, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndInASkirmishAnyDunlandManMayBeChosen() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strawheads = scn.GetShadowCard("strawheads1");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var saruman = scn.GetShadowCard("saruman");
		var axeman = scn.GetShadowCard("axeman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(strawheads);
		assertInHand(strawheads);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, warrior, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Saruman goes down after the move, which his own text answers by exerting companions.
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		// The assignment phase, next door to the skirmish: not playable.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(strawheads));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, warrior});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(strawheads));
		scn.ShadowPlayCard(strawheads);

		// A [dunland] Man in this skirmish or in one still to come; not the [dunland] minion who is no Man, the
		// [raider] Man or the [gondor] Man companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, warrior));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saruman, axeman, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		// The game rests on the Shadow player's choice of the Man.
		assertTrue(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void MakesADunlandManStrongerByTheNumberOfCompanionsNamedForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strawheads1 = scn.GetShadowCard("strawheads1");
		var strawheads2 = scn.GetShadowCard("strawheads2");
		var savage = scn.GetShadowCard("savage");
		var warrior = scn.GetShadowCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToHand(strawheads1, strawheads2);
		assertInHand(strawheads1, strawheads2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, warrior);
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, warrior});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(warrior));
		scn.ShadowPlayCard(strawheads1);
		scn.ShadowChooseCard(warrior);

		// Nothing is added before the player names how many companions he spots (CR 4.2 "spot").
		assertEquals(9, scn.GetStrength(warrior));
		// Three companions, Frodo, Aragorn and Gimli, of three cultures and races; not the ally or the minions.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		assertInDiscard(strawheads1);
		// Two named of the three: 9 + 2, not + 3.
		assertEquals(11, scn.GetStrength(warrior));
		assertEquals(11, scn.GetStrength(savage));

		scn.FreepsPass();
		scn.ShadowPlayCard(strawheads2);
		scn.ShadowChooseCard(savage);
		assertEquals(11, scn.GetStrength(savage));
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		assertInDiscard(strawheads2);
		// All three named: 11 + 3.
		assertEquals(14, scn.GetStrength(savage));
		assertEquals(11, scn.GetStrength(warrior));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Savage beats Aragorn, 14 to 8; that skirmish was the phase the Warrior's +2 lasted for.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, warrior));
		assertEquals(9, scn.GetStrength(warrior));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
