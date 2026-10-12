package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_078_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swordsman", "4_78");
					put("swordsman2", "4_78");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Lórien Swordsman

					put("orc", "3_98");         // Orc Swordsman: [sauron] Orc, strength 9, vitality 3; +2 strength with a weapon
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; may add (2) when played
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1

					put("wall", "5_33");        // City Wall: [gondor] fortification, bearer strength -3; its ability works only from the support area
					put("numenor", "5_37");     // Men of Númenor: Skirmish event, cost 0; wound a minion bearing a [gondor] fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LorienSwordsmanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Lórien Swordsman
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Each minion skirmishing this companion is strength -2 for each wound on that minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swordsman");

		assertEquals("Lórien Swordsman", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void EachMinionSkirmishingThisCompanionIsStrengthMinus2ForEachWoundOnIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var swordsman = scn.GetFreepsCard("swordsman");
		var swordsman2 = scn.GetFreepsCard("swordsman2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(swordsman, swordsman2, aragorn);
		scn.MoveCardsToHand(numenor);

		var orc = scn.GetShadowCard("orc");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.MoveMinionsToTable(orc, troop, runner, savage, raiders);
		// City Wall on the Orc Swordsman is what lets Men of Númenor wound him in the skirmish. It makes him
		// strength -3 throughout.
		scn.AttachCardsTo(orc, wall);
		scn.AddWoundsToChar(orc, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(raiders, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{swordsman, orc},
				new PhysicalCardImpl[]{swordsman2, savage},
				new PhysicalCardImpl[]{aragorn, raiders});
		scn.ShadowAssignToMinions(swordsman, troop, runner);

		// Assigned to him, but no skirmish has begun: nobody is reduced.
		assertEquals(6, scn.GetStrength(orc));      // 9 - 3 for the wall
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(9, scn.GetStrength(raiders));

		scn.FreepsResolveSkirmish(swordsman);
		assertTrue(scn.IsCharSkirmishingAgainst(swordsman, troop));
		assertEquals(5, scn.GetStrength(swordsman)); // the Swordsman himself is not reduced
		assertEquals(4, scn.GetStrength(orc));      // 9 - 3 for the wall - 2 for 1 wound
		assertEquals(9, scn.GetStrength(troop));    // 13 - 4 for 2 wounds, and not again for the other Swordsman
		assertEquals(5, scn.GetStrength(runner));   // no wounds
		// Wounded minions assigned to the other Swordsman and to Aragorn are not skirmishing this one.
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(9, scn.GetStrength(raiders));

		// A wound taken in the skirmish counts at once.
		scn.FreepsPlayCard(numenor);
		assertEquals(2, scn.GetWoundsOn(orc));
		assertEquals(2, scn.GetStrength(orc));      // 9 - 3 for the wall - 4 for 2 wounds
		assertEquals(9, scn.GetStrength(troop));

		// 5 against 2 + 9 + 5: the Swordsman is overwhelmed.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DEAD, swordsman);
		// His skirmish is over, and he is dead with it: his minions are themselves again.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(runner));

		// The other copy works for its own skirmish.
		scn.FreepsResolveSkirmish(swordsman2);
		assertTrue(scn.IsCharSkirmishingAgainst(swordsman2, savage));
		assertEquals(3, scn.GetStrength(savage));   // 5 - 2 for 1 wound
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(13, scn.GetStrength(troop));

		// Swordsman at 5 beats the Savage at 3, who takes a wound.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));
		// The skirmish is over and the Swordsman lives: the Savage's 2 wounds no longer take anything off him.
		assertEquals(5, scn.GetStrength(savage));

		// A wounded minion skirmishing a companion who is not a Lórien Swordsman.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, raiders));
		assertEquals(9, scn.GetStrength(raiders));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
