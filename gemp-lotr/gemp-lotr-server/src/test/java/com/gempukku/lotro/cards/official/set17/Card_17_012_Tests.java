package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rumil", "17_12");

					// [elven] hunters who can pay "exert another [elven] hunter".
					put("guardian", "17_7");        // Elven Guardian: [elven] Elf, hunter 1, vitality 3; a Maneuver heal used only on purpose
					put("orophin", "17_11");        // Orophin, Silvan Elf: [elven] Elf, hunter 1, vitality 3; +3 only while skirmishing a wounded minion
					// Cards that fail one word each.
					put("gimli", "15_5");           // Gimli, Eager Hunter: a [dwarven] Dwarf, hunter 3, damage +1 and nothing else
					put("elf", "1_53");             // Lórien Elf: an [elven] Elf with no keyword (to play, spot an Elf)

					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RumilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Rúmil, Silvan Elf
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Hunter 1.
		 * While you can spot a wounded minion, Rúmil is strength +1 and gains archer.
		 * Archery: Exert another [elven] hunter and add a threat to wound a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rumil");

		assertEquals("Rúmil", card.getBlueprint().getTitle());
		assertEquals("Silvan Elf", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void HeIsStrengthPlus1AndAnArcherWhileAWoundedMinionOfEitherKindCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(rumil, orophin);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Minions in play, none of them wounded.
		assertEquals(6, scn.GetStrength(rumil));
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));

		// A wounded companion is no wounded minion.
		scn.AddWoundsToChar(orophin, 1);
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(6, scn.GetStrength(rumil));
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));

		// A wounded [isengard] Uruk-hai.
		scn.AddWoundsToChar(savage, 1);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(7, scn.GetStrength(rumil));
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));
		// The bonus is Rúmil's alone: another [elven] hunter companion gains nothing.
		assertEquals(6, scn.GetStrength(orophin));
		assertFalse(scn.HasKeyword(orophin, Keyword.ARCHER));

		// A wounded [moria] Orc alone, the Uruk-hai healed.
		scn.RemoveWoundsFromChar(savage, 1);
		scn.AddWoundsToChar(troop, 1);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(7, scn.GetStrength(rumil));
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));

		// No wounded minion left.
		scn.RemoveWoundsFromChar(troop, 1);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(6, scn.GetStrength(rumil));
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryAbilityExertsAnotherElvenHunterAndAddsAThreatToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var guardian = scn.GetFreepsCard("guardian");
		var orophin = scn.GetFreepsCard("orophin");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(rumil, guardian, orophin, gimli, elf);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(savage, runner);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(6, scn.GetStrength(rumil));
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertTrue(scn.FreepsActionAvailable(rumil));

		scn.FreepsUseCardAction(rumil);
		// Another [elven] hunter: not Rúmil himself, not a [dwarven] hunter, not an [elven] Elf who is no hunter.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rumil, gimli, elf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(guardian);
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetThreats());

		// A minion of either culture; the Goblin Runner, of vitality 1, is killed by the wound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rumil, guardian));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		// No wounded minion is left to spot.
		assertEquals(6, scn.GetStrength(rumil));
		assertFalse(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(rumil));
		scn.FreepsUseCardAction(rumil);
		scn.FreepsChooseCard(orophin);
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(2, scn.GetThreats());
		// The one minion left is wounded with no choice asked, and Rúmil can now spot a wounded minion.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(7, scn.GetStrength(rumil));
		assertTrue(scn.HasKeyword(rumil, Keyword.ARCHER));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileTheOnlyOtherElvenHunterIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var guardian = scn.GetFreepsCard("guardian");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(rumil, guardian, gimli, elf);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(guardian, 2);
		assertEquals(2, scn.GetWoundsOn(guardian));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.ARCHERY);
		// A [dwarven] hunter and an [elven] Elf who is no hunter could exert, and a threat can be added.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rumil));
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rumil = scn.GetFreepsCard("rumil");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(rumil, guardian);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));
		// Frodo, Rúmil and the Guardian: three companions, three threats at most.
		scn.AddThreats(3);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(rumil));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(rumil));
	}
}
