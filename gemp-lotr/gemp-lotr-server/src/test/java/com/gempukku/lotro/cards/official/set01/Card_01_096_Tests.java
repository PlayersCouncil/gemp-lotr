package com.gempukku.lotro.cards.official.set01;

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

public class Card_01_096_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_96");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3, valiant only

					put("troop1", "1_177");     // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Boromir, Lord of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Ranger</b>.<br>Boromir is not overwhelmed unless his strength is tripled.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boromir");

		assertEquals("Boromir", card.getBlueprint().getTitle());
		assertEquals("Lord of Gondor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void BoromirIsNotOverwhelmedAtDoubleStrengthAndAnotherCompanionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var guard = scn.GetFreepsCard("guard");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(boromir, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		// 13 + 7 = 20: more than double Boromir's 7, one short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop1, null, 7));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, troop1}, new PhysicalCardImpl[]{guard, troop2});

		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(20, scn.GetStrength(troop1));
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();
		// He loses and takes the 1 wound for losing.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInPlay(boromir);

		// The Guard, at 6 against 13, is overwhelmed.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(13, scn.GetStrength(troop2));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInPlay(boromir);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void BoromirIsOverwhelmedOnceHisStrengthIsTripled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var troop = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Fierce, so that Boromir fights it twice; 13 + 7 = 20, one short of triple his 7.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 7));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(20, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInPlay(boromir);

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, troop);
		// One more: 21, exactly triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 1));
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(21, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassFierceSkirmishActions();

		// Vitality 3 with 1 wound: the wound for losing would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
