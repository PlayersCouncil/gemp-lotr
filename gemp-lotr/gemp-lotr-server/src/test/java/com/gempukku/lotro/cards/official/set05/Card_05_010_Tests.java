package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_010_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balglin", "5_10");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; Ranger, +3 only against a Nazgul
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion: a companion who is no Elf
					put("finalcount", "4_69");  // Final Count: [elven] condition in the support area, a card of the culture that is no Elf; nothing without Legolas

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("party", "1_158");      // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1 and nothing else
					// Easterling Infantry: [raider] Man, strength 8, vitality 2; fierce and damage +1 while 3 burdens can be spotted
					put("infantry", "4_227");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BalglinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Balglin, Elven Warrior
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot an Elf.
		 * While skirmishing a wounded minion, Balglin takes no more than 1 wound during each skirmish phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balglin");

		assertEquals("Balglin", card.getBlueprint().getTitle());
		assertEquals("Elven Warrior", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
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
	public void BalglinCanBePlayedOnlyOnceAnElfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balglin = scn.GetFreepsCard("balglin");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var finalcount = scn.GetFreepsCard("finalcount");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(finalcount);
		scn.MoveCardsToHand(balglin, arwen);

		scn.StartGame();

		// A companion who is no Elf and an [elven] condition in play, and no Elf.
		assertInZone(Zone.SUPPORT, finalcount);
		assertFalse(scn.FreepsPlayAvailable(balglin));

		scn.FreepsPlayCard(arwen);
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertTrue(scn.FreepsPlayAvailable(balglin));

		scn.FreepsPlayCard(balglin);
		assertInZone(Zone.FREE_CHARACTERS, balglin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyIsEnoughToPlayBalglin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balglin = scn.GetFreepsCard("balglin");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(balglin);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(balglin));
		scn.FreepsPlayCard(balglin);
		assertInZone(Zone.FREE_CHARACTERS, balglin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AgainstAWoundedUrukHaiBalglinTakesOneWoundAndArwenTakesBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balglin = scn.GetFreepsCard("balglin");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(balglin, arwen);

		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, party);
		scn.AddWoundsToChar(savage, 1);
		scn.AddWoundsToChar(party, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(party));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balglin, savage}, new PhysicalCardImpl[]{arwen, party});

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(balglin));
		scn.FreepsResolveSkirmish(balglin);
		scn.PassSkirmishActions();
		// 5 against 5: the Shadow side wins a tie.  1 wound + 1 for the Savage's damage, held to 1.
		assertEquals(1, scn.GetWoundsOn(balglin));

		// 9 against 6, not overwhelmed: Arwen, an Elf companion against a wounded minion, takes both.
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(arwen));
		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AgainstAWoundedMinionBalglinTakesOneWoundInEachSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balglin = scn.GetFreepsCard("balglin");
		scn.MoveCompanionsToTable(balglin);

		var infantry = scn.GetShadowCard("infantry");

		scn.StartGame();
		// Three burdens make the Easterling Infantry fierce and damage +1 by its own text, so that it skirmishes
		// Balglin in two skirmish phases of one turn.
		scn.AddBurdens(3);
		scn.MoveMinionsToTable(infantry);
		scn.AddWoundsToChar(infantry, 1);
		assertEquals(1, scn.GetWoundsOn(infantry));
		assertTrue(scn.HasKeyword(infantry, Keyword.FIERCE));
		assertEquals(1, scn.GetKeywordCount(infantry, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balglin, infantry);
		scn.FreepsResolveSkirmish(balglin);

		assertEquals(1, scn.GetWoundsOn(infantry));
		assertEquals(0, scn.GetWoundsOn(balglin));
		// 8 against 5, not overwhelmed: 1 wound + 1 for the damage, held to 1.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertEquals(1, scn.GetWoundsOn(infantry));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(balglin, infantry);
		scn.FreepsResolveSkirmish(balglin);
		scn.PassFierceSkirmishActions();

		// The fierce skirmish is another skirmish phase: 1 more wound, not 0 and not 2.  Vitality 3: he lives.
		assertEquals(2, scn.GetWoundsOn(balglin));
		assertInZone(Zone.FREE_CHARACTERS, balglin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AgainstAnUnwoundedMinionBalglinTakesEveryWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balglin = scn.GetFreepsCard("balglin");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(balglin, arwen);

		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, party);
		scn.AddWoundsToChar(party, 1);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(party));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balglin, savage}, new PhysicalCardImpl[]{arwen, party});

		// A wounded minion is on the table, waiting for Arwen, but Balglin's own minion is unwounded.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(balglin));
		scn.FreepsResolveSkirmish(balglin);
		scn.PassSkirmishActions();
		// 5 against 5: 1 wound + 1 for the Savage's damage.
		assertEquals(2, scn.GetWoundsOn(balglin));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
