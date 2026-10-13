package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_097_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "1_97");

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] Hobbit companion, strength 3; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3 (cheated in); a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, strength 2; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, strength 3; abilities used only on purpose

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
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
		 * Name: Boromir, Son of Denethor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: <b>Skirmish:</b> Exert Boromir to make a Hobbit strength +3.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boromir");

		assertEquals("Boromir", card.getBlueprint().getTitle());
		assertEquals("Son of Denethor", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void SkirmishAbilityExertsBoromirToMakeAHobbitOfAnyCultureOrSideStrengthPlus3() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(boromir, merry, pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, ted);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, troop1}, new PhysicalCardImpl[]{aragorn, ted});
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.FreepsActionAvailable(boromir));

		scn.FreepsUseCardAction(boromir);
		// Hobbits in this skirmish, in another and in none, companions, an ally and the Shadow player's minion;
		// not Smeagol ([shire] and no Hobbit), the Men or the Troop.
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, frodo, pippin, bounder, ted));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn, boromir, troop1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(3, scn.GetStrength(ted));
		// 3 and The Ruling Ring's +1
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileBoromirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(boromir, merry);
		scn.AddWoundsToChar(boromir, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, troop1);
		scn.FreepsResolveSkirmish(merry);

		// Vitality 3 with 1 wound: Boromir can still exert.
		assertTrue(scn.FreepsActionAvailable(boromir));

		scn.AddWoundsToChar(boromir, 1);
		scn.FreepsRefreshDecision();

		// Vitality 3 with 2 wounds: exhausted.
		assertFalse(scn.FreepsActionAvailable(boromir));
		assertEquals(3, scn.GetStrength(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void StrengthBonusLastsToTheEndOfTheSkirmishPhaseAndNotIntoTheNextSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(boromir, pippin, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop1}, new PhysicalCardImpl[]{pippin, troop2});
		scn.FreepsResolveSkirmish(aragorn);

		// Pippin is a Hobbit who waits for his own skirmish, the second of the turn.
		assertEquals(3, scn.GetStrength(pippin));
		scn.FreepsUseCardAction(boromir);
		scn.FreepsChooseCard(pippin);
		assertEquals(6, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the last decision of the skirmish phase it was made in.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(pippin));

		// Gone when the next skirmish phase begins, and in his own skirmish.
		scn.FreepsPass();
		assertEquals(3, scn.GetStrength(pippin));
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop2));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
