package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_020_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// The card is not unique: two copies, so that each can be read beside the other.
					put("fanatic1", "102_20");
					put("fanatic2", "102_20");

					// Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text.
					put("savage", "1_151");
					// Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text.
					put("troop", "1_177");

					// Assault Ladder: a machine.  "Skirmish: Spot 3 [isengard] tokens here to make an Uruk-hai
					// strength +3", which is only ever used on purpose.
					put("ladder", "6_58");
					// Devilry of Orthanc: a machine; its abilities need an Uruk-hai to exert in the Shadow
					// phase, or 8 tokens.
					put("devilry", "5_49");
					// Battering Ram: a machine; its skirmish ability needs 5 companions.
					put("ram", "5_44");
					// Uruk Shield Wall: a machine that is a possession.  With 2 tokens on it, it prevents
					// wounds to Uruk-hai costing 2 outside the skirmish phases, and has a maneuver ability.
					put("wall", "102_35");
					// No Business of Ours: an [isengard] condition that is no machine; it keeps the Free
					// Peoples player from looking at the Shadow hand.
					put("business", "2_44");

					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2"
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability
					put("guard", "5_83");        // Household Guard: strength 6, vitality 3; "Valiant" is its whole text
					// Pippin, Wearer of Black and Silver: "Skirmish: If Pippin is not assigned to a skirmish,
					// return him to your hand to wound a roaming minion twice."
					put("pippin", "7_324");
					// Grimir, Dwarven Elder: an ally whose home is site 3, strength 3, vitality 3; a fellowship
					// ability.
					put("grimir", "1_17");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BerserkFanaticStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Berserk Fanatic
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 8
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: Damage +1.
		 * This minion is strength +1 for each wound on each character in its skirmish.
		 * When this minion dies in a skirmish, you may remove 2 [isengard] tokens from a machine to wound a
		 * character it was skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fanatic1");

		assertEquals("Berserk Fanatic", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedFromHandForThreePlusTheRoamingPenalty() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fanatic = scn.GetShadowCard("fanatic1");
		scn.MoveCardsToHand(fanatic);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 10 set + 1 companion + 2 for the site.
		assertEquals(13, scn.GetTwilight());
		assertInZone(Zone.HAND, fanatic);
		assertTrue(scn.ShadowPlayAvailable(fanatic));

		scn.ShadowPlayCard(fanatic);
		// 3 to play + 2 roaming: its site number is 5 and the fellowship is at site 2.
		assertEquals(13 - 5, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, fanatic);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void GrowsStrongerAsTheCompanionItIsFightingExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var fanatic = scn.GetShadowCard("fanatic1");

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, fanatic);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(fanatic));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(fanatic));
		assertEquals(8, scn.GetStrength(fanatic));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(9, scn.GetStrength(fanatic));

		// A second wound on the same companion counts again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(10, scn.GetStrength(fanatic));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CountsEveryWoundOnEveryCharacterInItsOwnSkirmishItselfIncludedAndOnlyWhileThatSkirmishIsFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var fanatic1 = scn.GetShadowCard("fanatic1");
		var fanatic2 = scn.GetShadowCard("fanatic2");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic1, fanatic2, troop, savage);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(fanatic1, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		// Wounded, and in no skirmish.
		assertEquals(8, scn.GetStrength(fanatic1));
		assertEquals(8, scn.GetStrength(fanatic2));

		// Aragorn fights both Fanatics and the Goblin Patrol Troop; the Household Guard fights the Savage.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, fanatic1 },
				new PhysicalCardImpl[] { guard, savage }
		);
		scn.ShadowAssignToMinions(aragorn, fanatic2, troop);

		// The Guard's skirmish comes first.  The Fanatics are assigned and their own skirmish has not begun:
		// the wounds on them, on Aragorn and on the two characters now fighting count for nothing.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharAssigned(fanatic1));
		assertFalse(scn.IsCharSkirmishing(fanatic1));
		assertEquals(8, scn.GetStrength(fanatic1));
		assertEquals(8, scn.GetStrength(fanatic2));

		// The Guard's 6 beats the Savage's 5, which takes a second wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(fanatic1));
		assertTrue(scn.IsCharSkirmishing(fanatic2));
		assertTrue(scn.IsCharSkirmishing(troop));
		assertFalse(scn.IsCharSkirmishing(savage));
		// 8 + 2 for Aragorn's wounds + 1 for the first Fanatic's own + 2 for the Troop's beside it.  Not the
		// Guard's wound, and not the 2 on the Savage whose skirmish is over.
		assertEquals(13, scn.GetStrength(fanatic1));
		// The second Fanatic counts the same five wounds, once: each copy has its own bonus and no more.
		assertEquals(13, scn.GetStrength(fanatic2));
		// The Troop in the same skirmish gets nothing from either.
		assertEquals(13, troop.getBlueprint().getStrength());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void DyingInItsSkirmishItsOwnerMayRemoveTwoTokensFromOneMachineToWoundTheCompanionItWasFighting() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		var fanatic1 = scn.GetShadowCard("fanatic1");
		var fanatic2 = scn.GetShadowCard("fanatic2");
		var savage = scn.GetShadowCard("savage");
		var ladder = scn.GetShadowCard("ladder");
		var devilry = scn.GetShadowCard("devilry");
		var wall = scn.GetShadowCard("wall");
		var ram = scn.GetShadowCard("ram");
		var business = scn.GetShadowCard("business");
		scn.MoveCardsToSupportArea(ladder, devilry, wall, ram, business);
		scn.AddTokensToCard(ladder, 3);
		scn.AddTokensToCard(devilry, 2);
		scn.AddTokensToCard(wall, 2);
		// One token short.
		scn.AddTokensToCard(ram, 1);
		// Tokens enough, on a card that is no machine.
		scn.AddTokensToCard(business, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic1, fanatic2, savage);
		// One wound from death.
		scn.AddWoundsToChar(fanatic1, 2);
		// Wounded twice, the first Fanatic is strength 10 against an unwounded Aragorn, and the Savage beside
		// it makes 15.  Aragorn's 8 + 10 beats that without doubling it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		assertTrue(scn.HasKeyword(ladder, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(devilry, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(wall, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(ram, Keyword.MACHINE));
		assertFalse(scn.HasKeyword(business, Keyword.MACHINE));

		// Aragorn fights the first Fanatic and the Savage; the Household Guard is assigned to the second
		// Fanatic, and that skirmish waits.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, fanatic1 },
				new PhysicalCardImpl[] { guard, fanatic2 }
		);
		scn.ShadowAssignToMinions(aragorn, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(10, scn.GetStrength(fanatic1));
		assertEquals(18, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();

		// The first Fanatic takes its third wound and dies.  The offer is that copy's and not the other's.
		assertInZone(Zone.DISCARD, fanatic1);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fanatic1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(fanatic2));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowChooseOptionalTrigger(fanatic1);
		// Both tokens come off one machine, so only a machine with two is offered, condition or possession.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ladder, devilry, wall));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ram, business));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(ladder);
		assertEquals(3 - 2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetCultureTokensOn(devilry));
		assertEquals(2, scn.GetCultureTokensOn(wall));
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(3, scn.GetCultureTokensOn(business));

		// Aragorn is the one character it was skirmishing.  The Guard, assigned to the other Fanatic, is not;
		// nor is the Savage that fought beside it, which has the wound of losing and no more.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(fanatic2));
		assertInZone(Zone.SHADOW_CHARACTERS, fanatic2);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void DecliningLeavesTheTokensOnTheMachineAndTheCompanionUnwounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fanatic = scn.GetShadowCard("fanatic1");
		var ladder = scn.GetShadowCard("ladder");
		scn.MoveCardsToSupportArea(ladder);
		scn.AddTokensToCard(ladder, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic);
		scn.AddWoundsToChar(fanatic, 2);
		// 8 + 5 against the Fanatic's 8 + 2.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fanatic);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, fanatic);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fanatic));

		scn.ShadowDeclineOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithoutTwoTokensOnOneMachine() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var fanatic = scn.GetShadowCard("fanatic1");
		var ladder = scn.GetShadowCard("ladder");
		var ram = scn.GetShadowCard("ram");
		var business = scn.GetShadowCard("business");
		scn.MoveCardsToSupportArea(ladder, ram, business);
		// Two tokens on machines, one on each; and three on a card that is no machine.
		scn.AddTokensToCard(ladder, 1);
		scn.AddTokensToCard(ram, 1);
		scn.AddTokensToCard(business, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic);
		scn.AddWoundsToChar(fanatic, 2);
		// 8 + 5 against the Fanatic's 8 + 2.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fanatic);
		scn.FreepsResolveSkirmish(aragorn);
		assertInZone(Zone.SHADOW_CHARACTERS, fanatic);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, fanatic);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(ladder));
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(3, scn.GetCultureTokensOn(business));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyFightingItAtHomeCountsForItsStrengthAndIsWoundedWhenItDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);

		var fanatic = scn.GetShadowCard("fanatic1");
		var ladder = scn.GetShadowCard("ladder");
		scn.MoveCardsToSupportArea(ladder);
		scn.AddTokensToCard(ladder, 2);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(fanatic);
		scn.AddWoundsToChar(fanatic, 2);
		scn.AddWoundsToChar(grimir, 1);
		// Grimir's 3 + 9 beats the Fanatic's 8 + 2 + 1 without doubling it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, grimir, null, 9));

		// The fellowship moves to site 3, which is Grimir's home, and there he may fight.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(grimir, fanatic);
		scn.FreepsResolveSkirmish(grimir);

		// 8 + 2 for its own wounds + 1 for the ally's.
		assertEquals(11, scn.GetStrength(fanatic));
		assertEquals(12, scn.GetStrength(grimir));
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, fanatic);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fanatic));
		assertEquals(1, scn.GetWoundsOn(grimir));

		// One machine with two tokens: there is nothing to choose.
		scn.ShadowChooseOptionalTrigger(fanatic);
		assertEquals(0, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DyingInAnotherCharactersSkirmishAfterItsOwnOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(aragorn, guard, pippin);

		var fanatic = scn.GetShadowCard("fanatic1");
		var savage = scn.GetShadowCard("savage");
		var ladder = scn.GetShadowCard("ladder");
		scn.MoveCardsToSupportArea(ladder);
		scn.AddTokensToCard(ladder, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(fanatic, savage);
		scn.AddWoundsToChar(fanatic, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, fanatic },
				new PhysicalCardImpl[] { aragorn, savage }
		);

		// The Fanatic fights first and wins: 8 + 1 against the Guard's 6, who takes two wounds (damage +1)
		// and lives.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(fanatic));
		assertInZone(Zone.SHADOW_CHARACTERS, fanatic);

		// During Aragorn's skirmish Pippin wounds the Fanatic twice: at site 2 its site number of 5 makes it
		// a roaming minion.  It dies while a skirmish is being fought, and the skirmish is not its own.
		scn.FreepsResolveSkirmish(aragorn);
		assertFalse(scn.IsCharSkirmishing(fanatic));
		assertTrue(scn.FreepsActionAvailable(pippin));
		scn.FreepsUseCardAction(pippin);
		scn.FreepsChooseCard(fanatic);

		assertInZone(Zone.DISCARD, fanatic);
		assertInZone(Zone.HAND, pippin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(ladder));
		assertEquals(2, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
