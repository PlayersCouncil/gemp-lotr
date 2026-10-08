package com.gempukku.lotro.cards.unofficial.pc.errata.set05;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_05_010_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balglin", "55_10");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; Ranger, +3 only against a Nazgul
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man, given [elven] where a test needs it

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
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
		 * Game Text: <b>Valiant.</b>
		 * To play, spot an Elf.
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
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void BalglinCanBePlayedForTwoOnlyOnceAnElfCompanionIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balglin = scn.GetFreepsCard("balglin");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(balglin, arwen);
		// An [elven] Man: of the culture, not of the race.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(balglin));

		scn.FreepsPlayCard(arwen);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(balglin));

		scn.FreepsPlayCard(balglin);
		// 2 for Arwen + 2 for Balglin
		assertEquals(4, scn.GetTwilight());
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
	public void AgainstAWoundedMinionBalglinTakesOneWoundInEachSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balglin = scn.GetFreepsCard("balglin");
		scn.MoveCompanionsToTable(balglin);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.AddWoundsToChar(savage, 1);
		// Fierce, so that it skirmishes Balglin a second time in a skirmish phase of its own.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, savage, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balglin, savage);
		scn.FreepsResolveSkirmish(balglin);

		assertEquals(0, scn.GetWoundsOn(balglin));
		// 5 against 5: the Shadow side wins a tie.  1 wound + 1 for the Savage's damage, held to 1.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(balglin));
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(balglin, savage);
		scn.FreepsResolveSkirmish(balglin);
		scn.PassFierceSkirmishActions();

		// The fierce skirmish is another skirmish phase: 1 more wound, not 0 and not 2.
		assertEquals(2, scn.GetWoundsOn(balglin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AgainstAnUnwoundedMinionBalglinTakesEveryWoundAndAnotherElfIsNotProtected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balglin = scn.GetFreepsCard("balglin");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(balglin, arwen);

		var savage = scn.GetShadowCard("savage");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, savage2);
		scn.AddWoundsToChar(savage2, 1);
		// 7 against Arwen's 6, so that she loses.
		scn.ApplyAdHocModifier(new StrengthModifier(null, savage2, null, 2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balglin, savage}, new PhysicalCardImpl[]{arwen, savage2});

		// The wounded minion is on the table, waiting for Arwen, but Balglin's own minion is unwounded.
		scn.FreepsResolveSkirmish(balglin);
		scn.PassSkirmishActions();
		// 5 against 5: 1 wound + 1 for the Savage's damage.
		assertEquals(2, scn.GetWoundsOn(balglin));

		scn.FreepsResolveSkirmish(arwen);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
