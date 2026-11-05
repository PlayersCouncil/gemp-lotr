package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("child", "4_148");
					// Weapons of Isengard: [isengard] machine condition that holds [isengard] tokens; its abilities are used
					// only on purpose
					put("weapons", "4_211");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; Damage +1 is its whole text
					// White Hand Destroyer: unique [uruk-hai] Uruk-hai, strength 10, vitality 3, printed fierce; hunter only
					// for each site its owner controls, and he controls none
					put("destroyer", "17_124");
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability used only on purpose
					// Saruman, Master of the White Hand: an [uruk-hai] Wizard (the culture without the race); each companion
					// is resistance -1, and a skirmish ability used only on purpose
					put("saruman", "17_116");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; a fellowship ability used only on purpose
					// Thrarin, Dwarven Smith: Dwarf ally, strength 4, vitality 2; "Maneuver: Exert Thrarin to allow him to
					// participate in archery fire and skirmishes until the regroup phase"
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DowntotheLastChildStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Down to the Last Child
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time a companion or ally loses a skirmish involving an Uruk-hai, place an [isengard] token on this card.<br><b>Shadow:</b> Remove 2 [isengard] tokens from this card to take control of a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("child");

		assertEquals("Down to the Last Child", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ACompanionLosingToAnUrukHaiPlacesATokenHereAndLosingToAnotherMinionDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var child = scn.GetShadowCard("child");
		var weapons = scn.GetShadowCard("weapons");
		var guard = scn.GetShadowCard("guard");
		var worker = scn.GetShadowCard("worker");
		var saruman = scn.GetShadowCard("saruman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveCardsToSupportArea(child, weapons);

		scn.StartGame();
		scn.AddTokensToCard(weapons, 2);
		assertInZone(Zone.SUPPORT, child, weapons);
		assertEquals(0, scn.GetCultureTokensOn(child));
		assertEquals(2, scn.GetCultureTokensOn(weapons));
		scn.MoveMinionsToTable(guard, worker, saruman);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, guard}, new PhysicalCardImpl[]{gimli, saruman},
				new PhysicalCardImpl[]{sam, worker});

		// Gimli loses to the [uruk-hai] Wizard, 8 against 6: no Uruk-hai, no token.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetCultureTokensOn(child));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Sam loses to the [isengard] Orc, 5 against 3: no token.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetCultureTokensOn(child));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Aragorn loses to the [sauron] Uruk-hai, 9 against 8: a token here, and none on the other [isengard] card.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The skirmish damage and the token come at one moment; the damage is taken first.
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetCultureTokensOn(child));
		assertEquals(2, scn.GetCultureTokensOn(weapons));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnAllyLosingToAnUrukHaiPlacesAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var child = scn.GetShadowCard("child");
		var savage = scn.GetShadowCard("savage");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(child);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, child);
		assertEquals(0, scn.GetCultureTokensOn(child));
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, savage);
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// 5 against 4: the ally loses to an [isengard] Uruk-hai, and 1 + damage +1 kills him.
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(1, scn.GetCultureTokensOn(child));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoTokenIsPlacedWhenAnUrukHaiLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var child = scn.GetShadowCard("child");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(child);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, child);
		assertEquals(0, scn.GetCultureTokensOn(child));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// 5 against 8: the Uruk-hai loses and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetCultureTokensOn(child));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachLossToAFierceUrukHaiPlacesAnotherToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var child = scn.GetShadowCard("child");
		var destroyer = scn.GetShadowCard("destroyer");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(child);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, child);
		assertEquals(0, scn.GetCultureTokensOn(child));
		scn.MoveMinionsToTable(destroyer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, destroyer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();

		// 10 against 8: Aragorn loses to an [uruk-hai] Uruk-hai.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetCultureTokensOn(child));

		// The fierce skirmish: Aragorn loses to him again, and a second token is placed.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, destroyer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetCultureTokensOn(child));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ShadowAbilityRemovesTwoTokensHereToTakeTheLowestSiteUntilFewerThanTwoAreLeft() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var child = scn.GetShadowCard("child");
		var weapons = scn.GetShadowCard("weapons");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(child, weapons);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, child, weapons);
		// Player 1's second turn at site 3, both markers there: sites 1 and 2 lie behind every marker once the
		// fellowship moves on to site 4.
		scn.SkipToSite(3);
		scn.AddTokensToCard(child, 5);
		scn.AddTokensToCard(weapons, 2);
		assertEquals(5, scn.GetCultureTokensOn(child));
		assertEquals(2, scn.GetCultureTokensOn(weapons));
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowActionAvailable(child));

		// The lowest-numbered site that can be controlled is taken.
		scn.ShadowUseCardAction(child);
		assertEquals(3, scn.GetCultureTokensOn(child));     // 5 - 2
		assertEquals(2, scn.GetCultureTokensOn(weapons));
		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(site1));
		assertFalse(scn.IsSiteControlled(site2));

		// Again: the next one.
		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowUseCardAction(child);
		assertEquals(1, scn.GetCultureTokensOn(child));     // 3 - 2
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertTrue(scn.ShadowControls(site2));

		// One token here, and two on another [isengard] card: the cost cannot be paid from this card.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(child));
		assertEquals(2, scn.GetCultureTokensOn(weapons));
	}

	@Test
	public void WithNoSiteThatCanBeTakenTheAbilityIsOfferedAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var child = scn.GetShadowCard("child");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(child);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, child);
		scn.AddTokensToCard(child, 2);
		assertEquals(2, scn.GetCultureTokensOn(child));
		scn.MoveMinionsToTable(savage);
		scn.FreepsPass();

		// Turn 1 at site 2: the Shadow player's marker is still on site 1, so no site lies behind every marker.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(child));
		scn.ShadowUseCardAction(child);
		assertEquals(0, scn.GetCultureTokensOn(child));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
