package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "15_25");

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3; +3 only against a Nazgul
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: Elf ally, strength 6, vitality 3; a Fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8, vitality 4
					put("onewhom", "1_109");    // One Whom Men Would Follow: Maneuver, exert Aragorn to have an ally take part in skirmishes

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("raiders", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1
					put("raiders2", "1_158");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1 (exhausted as laid)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordoftheFallenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Sword of the Fallen
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be an Elf.<br>Each minion skirmishing bearer is strength -1 for each wound on that minion.<br><b>Skirmish:</b> Exert bearer to exert a minion skirmishing bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Sword of the Fallen", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnAnElfCompanionOrAllyAndNotOnAMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(sword);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(sword));
		scn.FreepsPlayCard(sword);

		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertAttachedTo(sword, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionSkirmishingBearerIsStrengthMinus1ForEachOfItsOwnWounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(arwen, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, raiders, raiders2);
		scn.AddWoundsToChar(troop, 1);
		scn.AddWoundsToChar(raiders, 2);
		scn.AddWoundsToChar(raiders2, 1);

		assertAttachedTo(sword, arwen);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(raiders));
		assertEquals(1, scn.GetWoundsOn(raiders2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, troop}, new PhysicalCardImpl[]{aragorn, raiders2});
		// The Shadow player puts the minion left over on Arwen too: two minions in her skirmish.
		scn.ShadowAssignToMinions(arwen, raiders);

		// Assigned to the bearer, their skirmish not yet begun: not skirmishing her.
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(raiders));

		scn.FreepsResolveSkirmish(aragorn);
		// A wounded minion skirmishing a companion who bears no Sword.
		assertEquals(9, scn.GetStrength(raiders2));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(9, scn.GetStrength(raiders));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(arwen);
		// Each by its own wounds: 13 - 1, and 9 - 2.
		assertEquals(12, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(raiders));
		assertEquals(9, scn.GetStrength(raiders2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllyBearerInASkirmishPaysTheExertionAndHisMinionLosesStrengthForEachWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var onewhom = scn.GetFreepsCard("onewhom");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.AttachCardsTo(celeborn, sword);
		scn.MoveCardsToHand(onewhom);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(troop, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		assertAttachedTo(sword, celeborn);
		assertEquals(1, scn.GetWoundsOn(troop));
		// Celeborn, the only ally, takes part in skirmishes.
		scn.FreepsPlayCard(onewhom);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(celeborn, troop);
		scn.FreepsResolveSkirmish(celeborn);

		// 13 - 1
		assertEquals(12, scn.GetStrength(troop));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);

		// The lone minion skirmishing him exerts unasked; with a second wound it is 13 - 2.
		assertEquals(1, scn.GetWoundsOn(celeborn));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(11, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsBearerToExertAMinionSkirmishingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var raiders = scn.GetShadowCard("raiders");
		var raiders2 = scn.GetShadowCard("raiders2");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(arwen, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, raiders, raiders2, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, troop}, new PhysicalCardImpl[]{aragorn, raiders2});
		scn.ShadowAssignToMinions(arwen, raiders, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);
		assertEquals(1, scn.GetWoundsOn(arwen));

		// The two minions able to exert in her skirmish; not the exhausted Runner beside them, nor the minion waiting
		// for Aragorn's skirmish.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, raiders));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, raiders2, aragorn));
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(raiders));
		assertEquals(0, scn.GetWoundsOn(raiders2));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertAttachedTo(sword, arwen);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sword));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(arwen, troop);
		scn.FreepsResolveSkirmish(arwen);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(sword));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen);
		scn.AttachCardsTo(arwen, sword);

		scn.StartGame();
		// Vitality 3 with 2 wounds.
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveMinionsToTable(troop);

		assertEquals(2, scn.GetWoundsOn(arwen));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, troop);
		scn.FreepsResolveSkirmish(arwen);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sword));
	}

	@Test
	public void WithBearerInNoSkirmishTheAbilityExertsBearerForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.AttachCardsTo(arwen, sword);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(troop));
		// Arwen is in no skirmish; the Troop is skirmishing Aragorn.
		assertTrue(scn.FreepsActionAvailable(sword));
		scn.FreepsUseCardAction(sword);

		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
