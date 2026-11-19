package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("orophin", "17_11");

					// [elven] hunters who can pay "exert another [elven] hunter".
					put("guardian", "17_7");        // Elven Guardian: [elven] Elf, hunter 1, vitality 3; a Maneuver heal used only on purpose
					put("rumil", "17_12");          // Rúmil, Silvan Elf: [elven] Elf, hunter 1, vitality 3; +1 and archer while a minion is wounded
					// Cards that fail one word each.
					put("gimli", "15_5");           // Gimli, Eager Hunter: a [dwarven] Dwarf, hunter 3, damage +1 and nothing else
					put("elf", "1_53");             // Lórien Elf: an [elven] Elf with no keyword (to play, spot an Elf)
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a Maneuver ability used only on purpose

					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1; no hunter
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play trigger only
					put("tracker", "15_171");       // Tracking Uruk: [uruk-hai] Uruk-hai, strength 14, vitality 3; hunter 1 is its whole text

					// A wound to a minion in the middle of a skirmish, from the Free Peoples side.
					put("numenor", "5_37");         // Men of Numenor: Skirmish event, cost 0: wound a minion bearing a [gondor] fortification
					put("wall", "5_33");            // City Wall: a [gondor] fortification; its bearer is strength -3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrophinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orophin, Silvan Elf
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
		 * While Orophin is skirmishing a wounded minion, he is strength +3.
		 * Archery: Exert another [elven] hunter and add a threat to wound a minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("orophin");

		assertEquals("Orophin", card.getBlueprint().getTitle());
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
	public void HeIsStrengthPlus3OnceTheMinionHeIsSkirmishingIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(orophin);
		scn.MoveCardsToHand(numenor);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(savage);
		// The Savage bears the fortification Men of Numenor needs.
		scn.AttachCardsTo(savage, wall);
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(orophin, savage);
		assertEquals(6, scn.GetStrength(orophin));

		scn.FreepsResolveSkirmish(orophin);
		// Hunter 1 against a minion who is no hunter, and nothing for an unwounded one.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(7, scn.GetStrength(orophin));

		scn.FreepsPlayCard(numenor);
		assertEquals(1, scn.GetWoundsOn(savage));
		// 6 + 1 hunter + 3
		assertEquals(10, scn.GetStrength(orophin));

		// The Savage, 5 - 3 for the wall, is beaten by more than double.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, savage);
		assertEquals(6, scn.GetStrength(orophin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBonusIsHisAloneAndOnlyInHisOwnSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(orophin, aragorn);

		scn.StartGame();

		var troop = scn.GetShadowCard("troop");
		var tracker = scn.GetShadowCard("tracker");
		scn.MoveMinionsToTable(troop, tracker);
		// A wounded [moria] Orc and a wounded [uruk-hai] Uruk-hai who is a hunter.
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(tracker, 1);
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(tracker));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, troop},
				new PhysicalCardImpl[]{orophin, tracker});

		scn.FreepsResolveSkirmish(aragorn);
		// Another companion skirmishing a wounded minion gains nothing, and Orophin, assigned to a wounded
		// minion whose skirmish has not begun, is not skirmishing it yet.
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(orophin));

		// 8 against the Troop's 13: Aragorn loses and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(orophin);
		// 6 + 3: a hunter gains nothing against a hunter, so the 3 are for the wound alone.
		assertEquals(9, scn.GetStrength(orophin));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ArcheryAbilityExertsAnotherElvenHunterAndAddsAThreatToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var guardian = scn.GetFreepsCard("guardian");
		var rumil = scn.GetFreepsCard("rumil");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(orophin, guardian, rumil, gimli, elf);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(savage, runner);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.FreepsActionAvailable(orophin));

		scn.FreepsUseCardAction(orophin);
		// Another [elven] hunter: not Orophin himself, not a [dwarven] hunter, not an [elven] Elf who is no hunter.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian, rumil));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, gimli, elf));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(guardian);
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetThreats());

		// A minion of either culture; the Goblin Runner, of vitality 1, is killed by the wound.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(orophin, guardian));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(orophin));
		scn.FreepsUseCardAction(orophin);
		scn.FreepsChooseCard(rumil);
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(2, scn.GetThreats());
		// The one minion left is wounded with no choice asked.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileTheOnlyOtherElvenHunterIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var guardian = scn.GetFreepsCard("guardian");
		var gimli = scn.GetFreepsCard("gimli");
		var elf = scn.GetFreepsCard("elf");
		scn.MoveCompanionsToTable(orophin, guardian, gimli, elf);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(guardian, 2);
		assertEquals(2, scn.GetWoundsOn(guardian));

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.ARCHERY);
		// A [dwarven] hunter and an [elven] Elf who is no hunter could exert, and a threat can be added.
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(orophin));
	}

	@Test
	public void ArcheryAbilityIsNotOfferedWhileNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var orophin = scn.GetFreepsCard("orophin");
		var guardian = scn.GetFreepsCard("guardian");
		scn.MoveCompanionsToTable(orophin, guardian);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));
		// Frodo, Orophin and the Guardian: three companions, three threats at most.
		scn.AddThreats(3);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetThreats());
		assertEquals(3, scn.GetThreatLimit());
		assertEquals(0, scn.GetWoundsOn(guardian));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(orophin));

		scn.RemoveThreats(1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(orophin));
	}
}
