package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_101_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("coat", "51_101");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion
					put("guard1", "5_83");      // Household Guard: [rohan] Man companion, strength 6, vitality 3, valiant only
					put("guard2", "5_83");
					put("deadman", "10_27");    // Dead Man of Dunharrow: [gondor] Wraith companion (cheated in, so no threat and no exertions)
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, neither a Man nor [gondor]
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, a fellowship ability used only on purpose
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man minion, strength 8, no text

					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CoatofMailStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Coat of Mail
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Man or [gondor] companion.
		 * Bearer may not be overwhelmed unless his or her strength is tripled.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("coat");

		assertEquals("Coat of Mail", card.getBlueprint().getTitle());
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

		var coat = scn.GetFreepsCard("coat");
		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard1");
		var deadman = scn.GetFreepsCard("deadman");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(coat);
		scn.MoveCompanionsToTable(boromir, guard, deadman, gimli);

		scn.StartGame();

		assertEquals(1, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsPlayAvailable(coat));

		scn.FreepsPlayCard(coat);
		// A [gondor] Man, a Man of another culture and a [gondor] companion who is no Man; not a Dwarf, nor a Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, guard, deadman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(deadman);

		assertAttachedTo(coat, deadman);
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CanBeBorneByAManAllyOrAnOpponentsManMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(coat);
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveAlliesToTable(barliman);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);

		scn.FreepsPlayCard(coat);
		// "A Man" read on its own, as the original's "Bearer must be a Man" was: a Man ally may bear it, and,
		// a race word with no "your" naming no side (Q-0058), so may the Shadow player's Man minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, barliman, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli));
		scn.FreepsChooseCard(barliman);

		assertAttachedTo(coat, barliman);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BearerIsNotOverwhelmedAtDoubleStrengthAndAnotherManIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard1, coat);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, troop1}, new PhysicalCardImpl[]{guard2, troop2});

		// 13 against 6: more than double, less than triple. The Coat spares him; he takes the 1 wound for losing.
		scn.FreepsResolveSkirmish(guard1);
		assertEquals(6, scn.GetStrength(guard1));
		assertEquals(13, scn.GetStrength(troop1));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertInPlay(guard1);

		// The same against the other Guard, who bears no Coat: overwhelmed.
		scn.FreepsResolveSkirmish(guard2);
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard2);
		assertEquals(1, scn.GetFreepsDeadCount());

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BearerIsOverwhelmedOnceStrengthIsTripled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var coat = scn.GetFreepsCard("coat");
		var guard = scn.GetFreepsCard("guard1");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, coat);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Fierce, so that the Guard fights it twice; 13 + 4 = 17, one short of triple his 6.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);
		assertEquals(17, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertInPlay(guard);

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, troop);
		// One more: 18, exactly triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 1));
		scn.FreepsResolveSkirmish(guard);
		assertEquals(18, scn.GetStrength(troop));
		assertEquals(6, scn.GetStrength(guard));
		scn.PassFierceSkirmishActions();

		// Vitality 3 with 1 wound: one more wound would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, guard);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
