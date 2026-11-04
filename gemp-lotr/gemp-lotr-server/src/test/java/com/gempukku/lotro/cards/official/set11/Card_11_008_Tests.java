package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_008_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "11_8");
					put("pursuer", "4_190");    // Uruk Pursuer: [isengard] Uruk-hai, strength 5, vitality 3, fierce; its texts are about unbound Hobbits
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Gimli, Lively Combatant
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 7
		 * Game Text: <b>Damage +1</b>.<br>Each time Gimli wins a skirmish, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Lively Combatant", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void EachSkirmishGimliWinsOffersADrawWhichCanBeTakenOrDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);

		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(deck > 0);
		scn.PassSkirmishActions();

		// Gimli's 6 beats the Pursuer's 5; the Pursuer takes 2 wounds (damage +1) and lives on, fierce.
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());

		// The fierce round: Gimli wins a second skirmish in the same turn and is offered the draw again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertInDiscard(pursuer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void GimliLosingASkirmishAndLivingOffersNoDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		// The Pursuer made 8 by hand: Gimli's 6 loses, and is not overwhelmed (8 is less than 12).
		scn.ApplyAdHocModifier(new StrengthModifier(null, pursuer, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);

		int hand = scn.GetFreepsHandCount();
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(gimli));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
