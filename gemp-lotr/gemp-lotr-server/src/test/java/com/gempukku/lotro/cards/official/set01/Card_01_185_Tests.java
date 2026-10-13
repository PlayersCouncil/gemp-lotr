package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_185_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warrior1", "1_185");
					put("warrior2", "1_185");
					put("patrol", "1_177");       // Goblin Patrol Troop: [moria] Orc minion, no text

					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] companion; a skirmish ability
					put("axe", "1_9");            // Dwarven Axe: [dwarven] possession; a trigger when a minion loses to bearer
					// Dwarven Heart: [dwarven] condition borne by a Dwarf; its triggers are on its play and at the start
					// of the turn, both already past when it is laid
					put("heart", "1_10");
					put("grimir", "1_17");        // Grimir, Dwarven Elder: [dwarven] ally; a fellowship ability
					put("lord", "1_21");          // Lord of Moria: [dwarven] condition in the support area; each Dwarf is damage +1
					put("orophin", "1_56");       // Orophin, Lorien Bowman: [elven] ally; an archery ability
					// Far-seeing Eyes: [elven] condition in the support area; a trigger on playing an Elf
					put("eyes", "1_43");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoblinWarriorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Goblin Warrior
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 4
		 * Game Text: When you play this minion, you may discard a [dwarven] ally or [dwarven] condition.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warrior1");

		assertEquals("Goblin Warrior", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenPlayedMayDiscardADwarvenAllyOrDwarvenConditionAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var heart = scn.GetFreepsCard("heart");
		var grimir = scn.GetFreepsCard("grimir");
		var lord = scn.GetFreepsCard("lord");
		var orophin = scn.GetFreepsCard("orophin");
		var eyes = scn.GetFreepsCard("eyes");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir, orophin);
		scn.MoveCardsToSupportArea(lord, eyes);

		var warrior1 = scn.GetShadowCard("warrior1");
		var warrior2 = scn.GetShadowCard("warrior2");
		var patrol = scn.GetShadowCard("patrol");
		scn.MoveCardsToHand(warrior1, warrior2, patrol);

		scn.StartGame();
		scn.AttachCardsTo(gimli, axe, heart);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Offered as it is played, and declined: nothing is discarded.
		scn.ShadowPlayCard(warrior1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior1));
		scn.ShadowDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, warrior1);
		assertInZone(Zone.SUPPORT, grimir, lord);
		assertInZone(Zone.ATTACHED, heart);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another minion played while it is in play: nothing is offered.
		scn.ShadowPlayCard(patrol);
		assertInZone(Zone.SHADOW_CHARACTERS, patrol);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second copy makes its own offer, and the first none.
		scn.ShadowPlayCard(warrior2);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(warrior2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(warrior1));
		scn.ShadowAcceptOptionalTrigger();

		// A [dwarven] ally, a [dwarven] condition in the support area and one a Dwarf bears; not a [dwarven]
		// companion or possession, nor an [elven] ally or condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(grimir, lord, heart));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gimli, axe, orophin, eyes));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(grimir);

		assertInZone(Zone.DISCARD, grimir);
		assertInZone(Zone.SUPPORT, lord, orophin, eyes);
		assertInZone(Zone.ATTACHED, heart, axe);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoDwarvenAllyOrConditionInPlayNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var orophin = scn.GetFreepsCard("orophin");
		var eyes = scn.GetFreepsCard("eyes");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToSupportArea(eyes);

		var warrior = scn.GetShadowCard("warrior1");
		scn.MoveCardsToHand(warrior);

		scn.StartGame();
		scn.SetTwilight(20);
		scn.FreepsPass();

		scn.ShadowPlayCard(warrior);
		assertInZone(Zone.SHADOW_CHARACTERS, warrior);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
