package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "1_95");
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3; his own skirmish ability is never used
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion who is not Boromir, strength 8; a maneuver ability never used
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, Damage +1 and no other text
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion, neither Orc nor Uruk-hai, no text
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7, vitality 2; his text answers only a weapon played on him
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladeofGondorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Blade of Gondor
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Boromir.
		 * He is damage +1.
		 * Skirmish: Exert Boromir to wound an Orc or Uruk-hai he is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Blade of Gondor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnBoromirAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(boromir, blade);

		scn.StartGame();

		// Frodo and Aragorn, and no Boromir: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(blade));

		scn.FreepsPlayCard(boromir);
		assertTrue(scn.FreepsPlayAvailable(blade));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));

		// Boromir is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(blade);
		assertAttachedTo(blade, boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsBoromirToWoundAnOrcOrUrukHaiHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, blade);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop, brigand, chieftain);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, chieftain},
				new PhysicalCardImpl[]{boromir, savage});
		// The two minions left over go to Boromir as well.
		scn.ShadowAssignToMinions(boromir, troop, brigand);

		// Aragorn's skirmish first: the Orc there skirmishes another companion, and the minions assigned to
		// Boromir are not skirmishing him yet.  The cost is paid and nobody is wounded.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(blade));

		scn.FreepsUseCardAction(blade);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn (8) beats the Chieftain (7), who takes the wound of the lost skirmish and survives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(chieftain));

		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.FreepsActionAvailable(blade));

		scn.FreepsUseCardAction(blade);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand, chieftain, boromir, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileBoromirIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, blade);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(boromir, 2);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(boromir, savage);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blade));
	}
}
