package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("butcher", "6_60");

					put("savage", "1_151");       // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("legion", "17_129");      // White Hand Legion: an [uruk-hai] Uruk-hai, strength 17, vitality 3; damage +1, to play remove 2 threats (cheated in)
					put("forger", "3_56");        // Isengard Forger: an [isengard] Orc, strength 6, vitality 2; a Shadow ability used only on purpose
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text

					put("ram", "5_44");           // Battering Ram: an [isengard] machine; its abilities are used only on purpose
					put("weapons", "4_211");      // Weapons of Isengard: an [isengard] machine of another title; its abilities are used only on purpose
					put("weapons2", "4_211");     // a second Weapons of Isengard, a machine with no token
					put("attack", "4_137");       // Attack on Helm's Deep: an [isengard] condition that holds [isengard] tokens and is no machine

					put("gimli", "1_13");         // Gimli, Son of Gloin: strength 6, vitality 3; "Skirmish: Exert Gimli to make him strength +2"
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("bow", "1_90");           // Aragorn's Bow: "Archery: Exert Aragorn to wound a minion"
					put("legolas", "1_50");       // Legolas, Greenleaf: "Archery: Exert Legolas to wound a minion"
					put("houseguard", "5_83");    // Household Guard: strength 6, vitality 3; "Valiant" is its whole text
					put("grimir", "1_17");        // Grimir, Dwarven Elder: an ally whose home is site 3, strength 3, vitality 3; a fellowship ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BerserkButcherStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Berserk Butcher
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.
		 * Berserk Butcher is strength +1 for each wound on each character in its skirmish.
		 * <b>Maneuver:</b> Remove an [isengard] token from a machine and exert Berserk Butcher twice to prevent all wounds to Uruk-hai until the assignment phase.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("butcher");

		assertEquals("Berserk Butcher", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void GrowsStrongerAsTheCompanionItIsFightingExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var butcher = scn.GetShadowCard("butcher");

		scn.StartGame();
		scn.MoveMinionsToTable(butcher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, butcher);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(butcher));
		assertEquals(12, scn.GetStrength(butcher));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(13, scn.GetStrength(butcher));

		// A second wound on the same companion counts again.
		scn.ShadowPass();
		scn.FreepsUseCardAction(gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(14, scn.GetStrength(butcher));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CountsEveryWoundOnEveryCharacterInItsOwnSkirmishAndOnlyWhileThatSkirmishIsFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var houseguard = scn.GetFreepsCard("houseguard");
		scn.MoveCompanionsToTable(aragorn, houseguard);

		var butcher = scn.GetShadowCard("butcher");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(butcher, troop, savage);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(houseguard, 1);
		scn.AddWoundsToChar(butcher, 1);
		scn.AddWoundsToChar(troop, 2);
		scn.AddWoundsToChar(savage, 1);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(houseguard));
		assertEquals(1, scn.GetWoundsOn(butcher));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.SkipToPhase(Phase.MANEUVER);
		// Wounded characters about, and no skirmish: the printed strength.
		assertEquals(12, scn.GetStrength(butcher));

		// Aragorn fights the Butcher and the Goblin Patrol Troop; the Household Guard fights the Savage.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, butcher },
				new PhysicalCardImpl[] { houseguard, savage }
		);
		scn.ShadowAssignToMinions(aragorn, troop);

		// The Guard's skirmish comes first.  The Butcher is assigned and its own skirmish has not begun: the wounds
		// on it, on Aragorn and on the two characters now fighting count for nothing.
		scn.FreepsResolveSkirmish(houseguard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharAssigned(butcher));
		assertFalse(scn.IsCharSkirmishing(butcher));
		assertEquals(12, scn.GetStrength(butcher));

		// The Guard's 6 beats the Savage's 5, which takes a second wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(houseguard));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(butcher));
		assertTrue(scn.IsCharSkirmishing(troop));
		assertFalse(scn.IsCharSkirmishing(savage));
		// 1 for each of 5 wounds: Aragorn's 2, the Butcher's own 1 and the Troop's 2 beside it.  Not the Guard's
		// wound, nor the 2 on the Savage, whose skirmish is over.
		assertEquals(12 + 5, scn.GetStrength(butcher));
		// The Troop in the same skirmish gets nothing.
		assertEquals(13, troop.getBlueprint().getStrength());
		assertEquals(13, scn.GetStrength(troop));
	}

	@Test
	public void AWoundOnAnAllyFightingItAtHisHomeSiteCounts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveAlliesToTable(grimir);

		var butcher = scn.GetShadowCard("butcher");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(butcher);
		scn.AddWoundsToChar(butcher, 1);
		scn.AddWoundsToChar(grimir, 1);
		assertEquals(1, scn.GetWoundsOn(butcher));
		assertEquals(1, scn.GetWoundsOn(grimir));

		// The fellowship moves to site 3, which is Grimir's home, and there he may fight.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(grimir, butcher);
		scn.FreepsResolveSkirmish(grimir);

		// 12 + 1 for its own wound + 1 for the ally's.
		assertEquals(14, scn.GetStrength(butcher));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ManeuverAbilityRemovesATokenFromAMachineAndExertsTheButcherTwiceToPreventWoundsToUrukHaiUntilTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(aragorn, legolas);
		scn.AttachCardsTo(aragorn, bow);

		var butcher = scn.GetShadowCard("butcher");
		var legion = scn.GetShadowCard("legion");
		var forger = scn.GetShadowCard("forger");
		var savage = scn.GetShadowCard("savage");
		var ram = scn.GetShadowCard("ram");
		var weapons = scn.GetShadowCard("weapons");
		var weapons2 = scn.GetShadowCard("weapons2");
		var attack = scn.GetShadowCard("attack");
		scn.MoveCardsToSupportArea(ram, weapons, weapons2, attack);

		scn.StartGame();
		assertAttachedTo(bow, aragorn);
		scn.MoveMinionsToTable(butcher, legion, forger, savage);
		scn.AddTokensToCard(ram, 1);
		scn.AddTokensToCard(weapons, 1);
		scn.AddTokensToCard(attack, 1);
		assertInZone(Zone.SUPPORT, ram, weapons, weapons2, attack);
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetCultureTokensOn(weapons));
		assertEquals(0, scn.GetCultureTokensOn(weapons2));
		assertEquals(1, scn.GetCultureTokensOn(attack));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(butcher));
		assertTrue(scn.ShadowActionAvailable(butcher));
		scn.ShadowUseCardAction(butcher);

		// A machine holding an [isengard] token, of either title; not the machine with none, nor the condition
		// that holds one and is no machine.
		assertTrue(scn.ShadowHasCardChoiceAvailable(ram, weapons));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(weapons2, attack));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ram);
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetCultureTokensOn(weapons));
		assertEquals(1, scn.GetCultureTokensOn(attack));
		assertEquals(2, scn.GetWoundsOn(butcher));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(legion));
		assertEquals(0, scn.GetWoundsOn(forger));

		// The archery phase, the last before the assignment phase: an [uruk-hai] Uruk-hai's wound is prevented.
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(butcher, legion, forger, savage));
		scn.FreepsChooseCard(legion);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(legion));

		// An [isengard] Orc's is not.
		scn.ShadowPass();
		scn.FreepsUseCardAction(bow);
		scn.FreepsChooseCard(forger);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(forger));

		// The Butcher's own, with 1 vitality left: prevented, and it lives.
		scn.ShadowPass();
		scn.FreepsUseCardAction(bow);
		scn.FreepsChooseCard(butcher);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(butcher));
		assertInZone(Zone.SHADOW_CHARACTERS, butcher);

		// From the assignment phase on, an Uruk-hai is wounded again: the Savage, 5 against Aragorn's 8.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(butcher));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithoutATokenOnAMachineOrWithTheButcherUnableToExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var butcher = scn.GetShadowCard("butcher");
		var ram = scn.GetShadowCard("ram");
		var attack = scn.GetShadowCard("attack");
		scn.MoveCardsToSupportArea(ram, attack);

		scn.StartGame();
		scn.MoveMinionsToTable(butcher);
		scn.AddTokensToCard(attack, 1);
		assertInZone(Zone.SUPPORT, ram, attack);
		assertEquals(0, scn.GetCultureTokensOn(ram));
		assertEquals(1, scn.GetCultureTokensOn(attack));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The only [isengard] token is on a condition that is no machine.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(butcher));
		assertFalse(scn.ShadowActionAvailable(butcher));

		// A token on the Ram: the cost can be paid.
		scn.AddTokensToCard(ram, 1);
		assertEquals(1, scn.GetCultureTokensOn(ram));
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(butcher));

		// With 1 wound the Butcher has 2 vitality left and cannot exert twice.
		scn.AddWoundsToChar(butcher, 1);
		assertEquals(1, scn.GetWoundsOn(butcher));
		scn.ShadowRefreshDecision();
		assertFalse(scn.ShadowActionAvailable(butcher));
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
