package com.gempukku.lotro.cards.official.set00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_00_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("arwen", "0_63");
					// Lorien Elf: Elf companion, strength 4, vitality 2; "To play, spot an Elf", so it is cheated in
					put("elf", "1_53");
					// Silinde, Elf of Mirkwood: Elf ally, home site 3, strength 5, vitality 2; has the text of site 3
					// (Rivendell Valley: a fellowship action) while it can be spotted
					put("silinde", "1_60");
					// Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability used
					// only on purpose
					put("boromir", "1_97");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("savage", "1_151");
					// Footman of Dunland: [men] Man, strength 7, vitality 3; the site gains plains
					put("footman", "11_82");
					// Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("troop", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ArwenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 0
		 * Name: Arwen, Maiden of Rivendell
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>.
		 * 	<b>Skirmish:</b> Exert Arwen to make a minion skirmishing an Elf strength -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("arwen");

		assertEquals("Arwen", card.getBlueprint().getTitle());
		assertEquals("Maiden of Rivendell", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void SkirmishAbilityExertsArwenToMakeAMinionSkirmishingAnotherElfStrengthMinusTwoAndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, elf, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, footman, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elf, savage},
				new PhysicalCardImpl[]{boromir, troop}
		);
		// The minion left over: the Shadow player puts the Footman on the Elf as well.
		scn.ShadowAssignToMinions(elf, footman);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(arwen));

		scn.FreepsUseCardAction(arwen);

		// In Boromir's skirmish the Troop fights a companion who is no Elf, and the two minions assigned to the
		// Elf are not skirmishing yet: Arwen exerts, and no minion is made weaker.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(elf);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(arwen));

		scn.FreepsUseCardAction(arwen);

		// Both minions in the Elf's skirmish; not the Troop, whose skirmish is over, and not the Elf, who is no minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop, elf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(footman);

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(footman));     // 7 - 2
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingAnElfAllyAndOneSkirmishingArwenAreEachMadeWeakerForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(silinde);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage, troop);
		// The fellowship moves to site 3, Silinde's home, where she can skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{silinde, savage},
				new PhysicalCardImpl[]{arwen, troop}
		);
		scn.FreepsResolveSkirmish(silinde);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsUseCardAction(arwen);

		// The Savage is the one minion skirmishing an Elf (the ally), and is chosen unasked; the Troop, assigned to
		// Arwen and not yet skirmishing, keeps its strength.  Read at the next decision of the same skirmish.
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(3, scn.GetStrength(savage));      // 5 - 2
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		// Silinde (5) beats the Savage (3) without overwhelming it; it takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		scn.FreepsResolveSkirmish(arwen);

		// A skirmish ability lasts for the skirmish phase it is used in.
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// In her own skirmish Arwen is the Elf.
		scn.FreepsUseCardAction(arwen);

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(11, scn.GetStrength(troop));      // 13 - 2
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileArwenIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var elf = scn.GetFreepsCard("elf");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, elf);

		scn.StartGame();
		// Vitality 3 with 2 wounds: she cannot exert.
		scn.AddWoundsToChar(arwen, 2);
		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elf, savage);
		scn.FreepsResolveSkirmish(elf);

		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(arwen));
	}
}
