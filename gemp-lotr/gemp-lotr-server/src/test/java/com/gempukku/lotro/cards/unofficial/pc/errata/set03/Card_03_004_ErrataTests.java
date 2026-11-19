package com.gempukku.lotro.cards.unofficial.pc.errata.set03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_03_004_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("welcome", "53_4");
					put("welcome2", "53_4");

					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] companion, strength 6, vitality 3, damage +1; a skirmish ability only used on purpose
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, strength 4, vitality 2, no text that matters
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, vitality 4: the companion who is not [dwarven]
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] ally, strength 3, vitality 3; a fellowship ability only used on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] ally, strength 4, vitality 2; a maneuver ability only used on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] ally, vitality 3: the ally who is not [dwarven]

					put("troop", "1_177");      // Goblin Patrol Troop: minion with no text, strength 13
					put("troop2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges bears the Ring: the one [dwarven] companion who is Ring-bound. No Frodo is on this
	// table, and no other Gimli is ever put in play on it.
	protected VirtualTableScenario GetGimliRBScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("welcome", "53_4");

					put("thorin", "17_6");      // Thorin III, Stonehelm: [dwarven] companion, strength 6, vitality 3, damage +1; his trigger answers a minion losing a skirmish to him
					put("guard", "1_7");        // Dwarf Guard: [dwarven] companion, strength 4, vitality 2, no text that matters
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion: the companion who is not [dwarven]
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] ally, strength 3, vitality 3; a fellowship ability only used on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] ally, strength 4, vitality 2; a maneuver ability only used on purpose
					put("bilbo", "1_284");      // Bilbo, Retired Adventurer: [shire] ally, vitality 3: the ally who is not [dwarven]

					put("troop", "1_177");      // Goblin Patrol Troop: minion with no text, strength 13
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ARoyalWelcomeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 3
		 * Name: A Royal Welcome
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Fellowship: Exert a [dwarven] companion to heal a [dwarven] ally.
		 * Skirmish: Exert a [dwarven] ally to add that ally's strength or <b>damage</b> bonuses to an unbound [dwarven] companion (limit once per phase).
		 * Regroup: Exert a [dwarven] ally to heal a [dwarven] companion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("welcome");

		assertEquals("A Royal Welcome", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void FellowshipActionExertsADwarvenCompanionToHealADwarvenAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir, thrarin, bilbo);

		scn.StartGame();
		// Aragorn is a companion and Bilbo a wounded ally, neither of them [dwarven]
		scn.AddWoundsToChar(grimir, 2);
		scn.AddWoundsToChar(thrarin, 1);
		scn.AddWoundsToChar(bilbo, 1);

		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);

		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, grimir, thrarin, bilbo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Gimli, a [dwarven] companion with a wound, is not an ally
		assertTrue(scn.FreepsHasCardChoiceAvailable(grimir, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(1, scn.GetWoundsOn(bilbo));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void FellowshipActionIsNotOfferedWhenNoDwarvenCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(grimir, 1);

		scn.StartGame();

		// Gimli and Dwarf Guard are exhausted; Aragorn could exert but is not [dwarven]
		assertFalse(scn.FreepsActionAvailable(welcome));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionExertsADwarvenAllyToAddItsStrengthToAnUnboundDwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRBScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(thorin, guard, aragorn);
		scn.MoveAlliesToTable(grimir, thrarin, bilbo);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(thorin, troop);

		assertEquals(6, scn.GetStrength(thorin));
		assertEquals(1, scn.GetKeywordCount(thorin, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);

		assertTrue(scn.FreepsHasCardChoiceAvailable(grimir, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, thorin, guard, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsChooseCard(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// Thrarin has no damage bonus, so his strength is added without asking (Q-0129)
		assertFalse(scn.FreepsChoiceAvailable("damage"));

		// Dwarf Guard is not in a skirmish; Gimli, a [dwarven] companion who bears the Ring and so is Ring-bound,
		// Aragorn of another culture and the allies are out
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, aragorn, thrarin, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thorin);

		// 6 + Thrarin's 4
		assertEquals(10, scn.GetStrength(thorin));
		assertEquals(1, scn.GetKeywordCount(thorin, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guard));
		// 6 + 1 for The Ruling Ring
		assertEquals(7, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionCanAddTheExertedAllysDamageBonusInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliRBScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var thrarin = scn.GetFreepsCard("thrarin");
		var grimir = scn.GetFreepsCard("grimir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(thorin, guard, aragorn);
		scn.MoveAlliesToTable(thrarin, grimir);

		scn.StartGame();
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, thrarin, null, Keyword.DAMAGE, 2));
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(thorin, troop);

		assertEquals(6, scn.GetStrength(thorin));
		assertEquals(1, scn.GetKeywordCount(thorin, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		scn.FreepsUseCardAction(welcome);
		scn.FreepsChooseCard(thrarin);
		scn.FreepsChoose("damage");

		// As for the strength: Dwarf Guard is not in a skirmish; Gimli, the Ring-bound [dwarven] companion, Aragorn
		// of another culture and the allies are out
		assertTrue(scn.FreepsHasCardChoiceAvailable(thorin, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, aragorn, thrarin, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(thorin);

		// Damage +1 of his own and Thrarin's damage +2
		assertEquals(3, scn.GetKeywordCount(thorin, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(thorin));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionWithAnAllyThatHasNoDamageBonusAddsItsStrengthWithoutAsking() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, troop);

		assertEquals(6, scn.GetStrength(gimli));
		// Grimir, the one [dwarven] ally, and Gimli, the one companion who can take it: both chosen for the player
		scn.FreepsUseCardAction(welcome);
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertFalse(scn.FreepsChoiceAvailable("damage"));

		// 6 + Grimir's 3
		assertEquals(9, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsUsedOncePerPhaseByEachCopyAndItsBonusEndsWithTheSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var welcome2 = scn.GetFreepsCard("welcome2");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCardsToSupportArea(welcome, welcome2);
		scn.MoveCompanionsToTable(gimli, guard);
		scn.MoveAlliesToTable(grimir, thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, troop2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, troop}, new PhysicalCardImpl[]{guard, troop2});
		scn.FreepsResolveSkirmish(gimli);

		scn.FreepsUseCardAction(welcome);
		scn.FreepsChooseCard(thrarin);
		scn.FreepsChooseCard(gimli);
		// 6 + Thrarin's 4
		assertEquals(10, scn.GetStrength(gimli));
		scn.ShadowPass();

		// Grimir could still exert and Dwarf Guard could take his strength: this copy is spent for the phase,
		// the other copy is not
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(welcome));
		assertTrue(scn.FreepsActionAvailable(welcome2));
		assertEquals(10, scn.GetStrength(gimli));

		scn.FreepsPass();
		scn.ShadowPass();
		// Gimli's 10 loses to the troop's 13 and takes a wound
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(guard);

		// The next skirmish phase: Gimli's bonus is gone, and the first copy is offered again
		assertTrue(scn.IsCharSkirmishing(guard));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);
		scn.FreepsChooseCard(guard);
		// 4 + Grimir's 3
		assertEquals(7, scn.GetStrength(guard));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhenNoDwarvenAllyCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir, thrarin, bilbo);

		scn.StartGame();
		scn.AddWoundsToChar(grimir, 2);
		scn.AddWoundsToChar(thrarin, 1);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gimli, troop);

		// Grimir and Thrarin are exhausted; Bilbo could exert but is not [dwarven]
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(welcome));
		assertEquals(6, scn.GetStrength(gimli));
	}

	@Test
	public void RegroupActionExertsADwarvenAllyToHealADwarvenCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli, guard, aragorn);
		scn.MoveAlliesToTable(grimir, thrarin, bilbo);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(welcome));
		scn.FreepsUseCardAction(welcome);

		assertTrue(scn.FreepsHasCardChoiceAvailable(grimir, thrarin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bilbo, gimli));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(grimir));
		scn.FreepsChooseCard(grimir);
		assertEquals(1, scn.GetWoundsOn(grimir));

		// Grimir, a [dwarven] ally with a wound, is not a companion
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(thrarin));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhenNoDwarvenAllyCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var welcome = scn.GetFreepsCard("welcome");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var thrarin = scn.GetFreepsCard("thrarin");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToSupportArea(welcome);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(grimir, thrarin, bilbo);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(grimir, 2);
		scn.AddWoundsToChar(thrarin, 1);
		scn.SkipToPhase(Phase.REGROUP);

		// Grimir and Thrarin are exhausted; Bilbo could exert but is not [dwarven]
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(welcome));
		assertEquals(1, scn.GetWoundsOn(gimli));
	}
}
