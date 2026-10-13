package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_006_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "10_6");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Arwen; a maneuver ability used only on purpose

					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7, vitality 2; his text answers a weapon played on him

					put("wall", "5_33");        // City Wall: [gondor] fortification, bearer strength -3; its ability works only from the support area
					put("numenor", "5_37");     // Men of Numenor: Skirmish event, cost 0; wound a minion bearing a [gondor] fortification
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Arwen, Queen of Elves and Men
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Frodo
		 * Game Text: Each minion skirmishing Arwen is strength -2 for each wounded minion you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Queen of Elves and Men", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
	}

	@Test
	public void EachMinionSkirmishingArwenIsStrengthMinus2ForEachWoundedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToHand(numenor);

		var raiders = scn.GetShadowCard("raiders");
		var troop = scn.GetShadowCard("troop");
		var chieftain = scn.GetShadowCard("chieftain");

		scn.StartGame();
		scn.MoveMinionsToTable(raiders, troop, chieftain);
		// City Wall on the Troop is what lets Men of Numenor wound him in the skirmish. It makes him strength -3
		// throughout.
		scn.AttachCardsTo(troop, wall);
		scn.AddWoundsToChar(raiders, 2);
		scn.AddWoundsToChar(chieftain, 1);
		// A wounded companion, who is no minion and is not counted.
		scn.AddWoundsToChar(aragorn, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, raiders},
				new PhysicalCardImpl[]{aragorn, chieftain});
		scn.ShadowAssignToMinions(arwen, troop);

		// Assigned to her, but no skirmish has begun: nobody is reduced.
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(10, scn.GetStrength(troop));   // 13 - 3 for the wall
		assertEquals(7, scn.GetStrength(chieftain));

		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, raiders));
		assertTrue(scn.IsCharSkirmishingAgainst(arwen, troop));
		// Two wounded minions, the Raiding Party (2 wounds, counted once) and the Chieftain (1 wound, in another
		// skirmish): -4 to each minion skirmishing Arwen. Aragorn's wound and the unwounded Troop count for nothing.
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(raiders));  // 9 - 4
		assertEquals(6, scn.GetStrength(troop));    // 13 - 3 for the wall - 4
		// The Chieftain is assigned to Aragorn, whose skirmish has not begun.
		assertEquals(7, scn.GetStrength(chieftain));

		// A third minion wounded in the skirmish: -6 at once.
		scn.FreepsPlayCard(numenor);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(3, scn.GetStrength(raiders));  // 9 - 6
		assertEquals(4, scn.GetStrength(troop));    // 13 - 3 for the wall - 6
		assertEquals(7, scn.GetStrength(chieftain));

		// Arwen at 6 against 3 + 4 loses, and takes a wound and the Raiding Party's damage +1.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		// Her skirmish is over and she lives: her minions are themselves again.
		assertEquals(9, scn.GetStrength(raiders));
		assertEquals(10, scn.GetStrength(troop));

		// A wounded minion skirmishing a companion who is not Arwen.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, chieftain));
		assertEquals(7, scn.GetStrength(chieftain));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
