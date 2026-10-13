package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_092_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "51_92");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3, valiant only
					put("deadman", "10_27");    // Dead Man of Dunharrow: [gondor] Wraith companion (cheated in, so no threat and no exertions)
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, neither a Man nor [gondor]
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, a fellowship ability used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, strength 8, no text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4

					put("party1", "1_158");     // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 and nothing else
					put("party2", "1_158");
					put("marksman1", "1_176");  // Goblin Marksman: strength 7, archer and nothing else
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Armor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Man or [gondor] companion.
		 * Bearer takes no more than 1 wound during each skirmish phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAManOrAGondorCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var deadman = scn.GetFreepsCard("deadman");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(armor);
		scn.MoveCompanionsToTable(boromir, guard, deadman, gimli);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(armor));

		scn.FreepsPlayCard(armor);
		// A [gondor] Man, a Man of another culture and a [gondor] companion who is no Man; not a Dwarf, nor a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, guard, deadman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		assertAttachedTo(armor, guard);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBeBorneByAManAllyOrAnOpponentsManMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(armor);
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.FreepsPlayCard(armor);
		// "A Man" read on its own, as the original's "Bearer must be a Man" was: a Man ally may bear it, and,
		// a race word with no "your" naming no side (Q-0058), so may the Shadow player's Man minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, barliman, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(barliman);

		assertAttachedTo(armor, barliman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerLosingToADamagePlusOneMinionTakesOneWoundAndAnotherGondorManTakesTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var party1 = scn.GetShadowCard("party1");
		var party2 = scn.GetShadowCard("party2");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(party1, party2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, party1}, new PhysicalCardImpl[]{aragorn, party2});

		// Strength 9 against 7: Boromir loses, but is not overwhelmed. Damage +1 makes it 2 wounds, and Armor lets 1 through.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Strength 9 against 8: Aragorn, a [gondor] Man companion who does not bear it, takes both.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerTakesOneWoundInEachSkirmishPhaseOfTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var boromir = scn.GetFreepsCard("boromir");
		var party = scn.GetShadowCard("party1");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		// The Raiding Party made fierce, so that Boromir fights it in two skirmish phases of one turn.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, party, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, party);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// 2 wounds, 1 taken.
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, party);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		// A new skirmish phase: 2 more wounds, 1 taken again. Vitality 3: he lives.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertInPlay(boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerTakesTwoArcheryWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var boromir = scn.GetFreepsCard("boromir");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, armor);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToArcheryWounds();

		// The archery phase is not a skirmish phase: both arrows land.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.FreepsChooseCard(boromir);
		scn.FreepsChooseCard(boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
