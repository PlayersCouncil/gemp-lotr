package com.gempukku.lotro.cards.unofficial.pc.errata.set17;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_17_007_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guardian1", "67_7");
					put("guardian2", "67_7");

					put("gimli", "15_5");           // Gimli, Eager Hunter: a [dwarven] Dwarf, vitality 3; damage +1 and hunter 3 are his whole text
					put("legolas", "1_50");         // Legolas, Greenleaf: an [elven] Elf who is no hunter, vitality 3; an archery ability only

					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1; no hunter
					put("troop", "1_177");          // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("tracker1", "15_171");      // Tracking Uruk: [uruk-hai] minion, strength 14, vitality 3; hunter 1 is its whole text
					put("tracker2", "15_171");

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
	public void ElvenGuardianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Elven Guardian
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Valiant.
		 * Hunter 1.
		 * While this companion is skirmishing a wounded minion, he is strength +2.
		 * Maneuver: Exert this companion to heal another hunter.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guardian1");

		assertEquals("Elven Guardian", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedFromHandForTwoAsACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian1");
		scn.MoveCardsToHand(guardian);

		scn.StartGame();
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(guardian));

		scn.FreepsPlayCard(guardian);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, guardian);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void HeIsStrengthPlus2OnlyWhileTheMinionHeIsSkirmishingIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian1 = scn.GetFreepsCard("guardian1");
		var guardian2 = scn.GetFreepsCard("guardian2");
		var numenor = scn.GetFreepsCard("numenor");
		var wall = scn.GetFreepsCard("wall");
		scn.MoveCompanionsToTable(guardian1, guardian2);
		scn.MoveCardsToHand(numenor);

		scn.StartGame();

		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(savage, troop);
		// The Savage is unwounded and bears the fortification Men of Numenor needs.  The Troop has two wounds.
		scn.AttachCardsTo(savage, wall);
		scn.AddWoundsToChar(troop, 2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guardian1, savage},
				new PhysicalCardImpl[]{guardian2, troop});
		assertEquals(6, scn.GetStrength(guardian1));
		assertEquals(6, scn.GetStrength(guardian2));

		scn.FreepsResolveSkirmish(guardian1);
		// Hunter 1 against a minion who is no hunter, and nothing for an unwounded one.
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(7, scn.GetStrength(guardian1));
		// The other Guardian is assigned to a wounded minion, but that skirmish has not begun.
		assertEquals(6, scn.GetStrength(guardian2));

		scn.FreepsPlayCard(numenor);
		assertEquals(1, scn.GetWoundsOn(savage));
		// 6 + 1 hunter + 2
		assertEquals(9, scn.GetStrength(guardian1));
		assertEquals(6, scn.GetStrength(guardian2));

		// The Savage, 5 - 3 for the wall, is beaten by more than double.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, savage);
		assertEquals(6, scn.GetStrength(guardian1));

		scn.FreepsResolveSkirmish(guardian2);
		// 6 + 1 hunter + 2: his own skirmish against the wounded Troop.  The first Guardian is in none.
		assertEquals(9, scn.GetStrength(guardian2));
		assertEquals(6, scn.GetStrength(guardian1));

		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guardian2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AgainstAWoundedHunterMinionTheBonusIsTwoAndAgainstAnUnwoundedOneNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian1 = scn.GetFreepsCard("guardian1");
		var guardian2 = scn.GetFreepsCard("guardian2");
		scn.MoveCompanionsToTable(guardian1, guardian2);

		scn.StartGame();

		var tracker1 = scn.GetShadowCard("tracker1");
		var tracker2 = scn.GetShadowCard("tracker2");
		scn.MoveMinionsToTable(tracker1, tracker2);
		scn.AddWoundsToChar(tracker1, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guardian1, tracker1},
				new PhysicalCardImpl[]{guardian2, tracker2});

		scn.FreepsResolveSkirmish(guardian1);
		// A hunter gains nothing against a hunter: the 2 are for the wound alone.
		assertEquals(8, scn.GetStrength(guardian1));
		assertEquals(6, scn.GetStrength(guardian2));

		// 8 against the Tracking Uruk's 14: he loses and is not overwhelmed.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guardian1));

		scn.FreepsResolveSkirmish(guardian2);
		assertEquals(0, scn.GetWoundsOn(tracker2));
		assertEquals(6, scn.GetStrength(guardian2));
		assertEquals(6, scn.GetStrength(guardian1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsHimToHealAnotherHunterUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian1 = scn.GetFreepsCard("guardian1");
		var guardian2 = scn.GetFreepsCard("guardian2");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(guardian1, guardian2, gimli, legolas);
		// Wounded hunters: another Elven Guardian, a [dwarven] Dwarf and, below, the opponent's minion.
		// Legolas is a wounded [elven] Elf and no hunter.
		scn.AddWoundsToChar(guardian2, 1);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();

		var tracker = scn.GetShadowCard("tracker1");
		scn.MoveMinionsToTable(tracker);
		scn.AddWoundsToChar(tracker, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(guardian1));
		assertTrue(scn.FreepsActionAvailable(guardian1));

		scn.FreepsUseCardAction(guardian1);
		assertEquals(1, scn.GetWoundsOn(guardian1));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian2, gimli, tracker));
		// Not himself, wounded though he now is, and not an Elf who is no hunter.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guardian1, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guardian2));
		assertEquals(1, scn.GetWoundsOn(tracker));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(guardian1));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// "Another hunter" names no side: the opponent's hunter minion is healed like any other.
		assertTrue(scn.FreepsActionAvailable(guardian1));
		scn.FreepsUseCardAction(guardian1);
		assertEquals(2, scn.GetWoundsOn(guardian1));
		assertTrue(scn.FreepsHasCardChoiceAvailable(guardian2, gimli, tracker));
		scn.FreepsChooseCard(tracker);
		assertEquals(0, scn.GetWoundsOn(tracker));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guardian2));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.  The other Guardian, with one, can.
		assertFalse(scn.FreepsActionAvailable(guardian1));
		assertTrue(scn.FreepsActionAvailable(guardian2));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoOtherWoundedHunterTheAbilityIsPaidForAndHealsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guardian = scn.GetFreepsCard("guardian1");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(guardian, legolas);
		scn.AddWoundsToChar(legolas, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("savage"));

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(guardian));

		scn.FreepsUseCardAction(guardian);
		// The only wounded hunter is the Guardian himself, by his own exertion: he is not "another".
		assertEquals(1, scn.GetWoundsOn(guardian));
		assertEquals(1, scn.GetWoundsOn(legolas));
		// Nothing was asked of him: the Shadow player is next to act.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
