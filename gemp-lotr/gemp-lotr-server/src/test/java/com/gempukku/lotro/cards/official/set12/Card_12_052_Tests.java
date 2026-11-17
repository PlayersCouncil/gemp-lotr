package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_052_Tests
{

	// The Towers path with Uruk Camp 4_335 (Battleground, Plains) as site 2.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tireless", "12_52");
					put("tireless2", "12_52");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, resistance 6; a maneuver ability used only on purpose
					put("sword", "1_112");      // Ranger's Sword: hand weapon possession, strength +2 and damage +1, on Aragorn
					put("narsil", "9_34");      // Narsil: hand weapon ARTIFACT, vitality +1; its Response answers a wound on its bearer only
					put("bow", "7_94");         // Gondor Bow: ranged weapon, +1 only on a knight; a skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, resistance 6; "Knight." is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home 3T; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: [gondor] Hobbit companion; a skirmish ability used only on purpose
					put("guard", "5_83");       // Household Guard: [rohan] Man companion; "Valiant." is his whole text

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, site 5, so roaming at site 2; damage +1 is its whole text
					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, strength 10, site 2, so not roaming at site 2; fierce only at 2 burdens
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site2", "4_335");      // Uruk Camp: Battleground, Plains; exerts each Uruk-hai PLAYED, and none is played here
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The plain Towers path: site 2 is Eastfold 4_331, a plains and no battleground.
	protected VirtualTableScenario GetEastfoldScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tireless", "12_52");
					put("tireless2", "12_52");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, resistance 6
					put("bow", "7_94");         // Gondor Bow: ranged weapon, +1 only on a knight; a skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, resistance 6; "Knight." is his whole text

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, site 5, so roaming at site 2
					put("nelya", "2_84");       // Úlairë Nelya: Nazgûl, strength 10, site 2, so not roaming at site 2; fierce and damage +1 at 2 burdens
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: site 4 is White Rocks 4_346, a battleground (its Shadow number counts mounted companions).
	// Site 3, the home of the Gondorian Merchant (3T), is Barrows of Edoras 4_337, a sanctuary and no
	// battleground.  The Merchant is the only [gondor] ally, and no ally's home is a battleground.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tireless", "12_52");
					put("tireless2", "12_52");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, resistance 6; "Knight." is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally, home 3T, strength 6; a maneuver ability used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, site 4 (not roaming at site 4), no text
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TirelessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Tireless
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Discard this condition to make a [gondor] Man strength +1 for each of the following that is true: he or she is at a battleground site; he or she has resistance 4 or more; he or she is bearing a hand weapon; he or she is skirmishing a roaming minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("tireless");

		assertEquals("Tireless", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AllFourAreTrueForPlus4OnlyInTheSkirmishItIsUsedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tireless = scn.GetFreepsCard("tireless");
		var tireless2 = scn.GetFreepsCard("tireless2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(tireless, tireless2);

		var savage = scn.GetShadowCard("savage");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		assertEquals(aragorn, sword.getAttachedTo());
		assertInZone(Zone.SUPPORT, tireless, tireless2);

		scn.MoveMinionsToTable(savage, nelya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(tireless));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{knight, nelya});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		assertEquals(6, scn.GetResistance(aragorn));
		// 8, and 2 for the sword
		assertEquals(10, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(tireless);
		assertInZone(Zone.DISCARD, tireless);
		assertInZone(Zone.SUPPORT, tireless2);

		// [gondor] Man companions and the [gondor] Man ally; not the [gondor] Hobbit or the [rohan] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		// 10, and 1 each for the battleground, resistance 6, the hand weapon and the roaming minion
		assertEquals(14, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(knight);

		// The next skirmish is a phase of its own: the bonus is gone.
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ResistanceOfExactly4CountsAndARangedWeaponANonRoamingMinionAndAWaitingSkirmishDoNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetEastfoldScenario();

		var tireless = scn.GetFreepsCard("tireless");
		var tireless2 = scn.GetFreepsCard("tireless2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(aragorn, bow);
		scn.MoveCardsToSupportArea(tireless, tireless2);

		var savage = scn.GetShadowCard("savage");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		assertEquals(aragorn, bow.getAttachedTo());
		assertInZone(Zone.SUPPORT, tireless, tireless2);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());

		scn.MoveMinionsToTable(savage, nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{knight, savage});
		scn.FreepsResolveSkirmish(aragorn);

		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(savage, Keyword.ROAMING));
		// 6, less 2 burdens
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(knight));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(knight));

		// Aragorn bears a ranged weapon and skirmishes a minion that is not roaming: only his resistance counts.
		scn.FreepsUseCardAction(tireless);
		scn.FreepsChooseCard(aragorn);
		assertEquals(9, scn.GetStrength(aragorn));

		// The Knight is assigned to a roaming minion, but that skirmish has not begun: only his resistance counts.
		scn.ShadowPass();
		scn.FreepsUseCardAction(tireless2);
		scn.FreepsChooseCard(knight);
		assertEquals(7, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ABattlegroundAndAHandWeaponArtifactCountAndResistanceOf3DoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tireless = scn.GetFreepsCard("tireless");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(aragorn, narsil);
		scn.MoveCardsToSupportArea(tireless);

		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		assertEquals(aragorn, narsil.getAttachedTo());
		assertInZone(Zone.SUPPORT, tireless);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());

		scn.MoveMinionsToTable(nelya);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, nelya);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		// 6, less 3 burdens
		assertEquals(3, scn.GetResistance(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsUseCardAction(tireless);
		scn.FreepsChooseCard(aragorn);

		// 8, and 1 each for the battleground and the hand weapon
		assertEquals(10, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllyWhoseHomeIsNoBattlegroundGetsNothingForTheFellowshipAtABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var tireless = scn.GetFreepsCard("tireless");
		var tireless2 = scn.GetFreepsCard("tireless2");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(knight);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(tireless, tireless2);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		assertEquals("White Rocks", scn.GetCurrentSite().getBlueprint().getTitle());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		scn.FreepsAssignToMinions(knight, troop);
		scn.FreepsResolveSkirmish(knight);

		// The companion: at the battleground, with the fellowship, and resistance 6: 6 + 2.
		scn.FreepsUseCardAction(tireless);
		scn.FreepsChooseCard(knight);
		assertEquals(8, scn.GetStrength(knight));

		// The Merchant is at his home site, Barrows of Edoras (CR 4.2 "ally"), and is true of nothing else listed: 6.
		scn.ShadowPass();
		scn.FreepsUseCardAction(tireless2);
		scn.FreepsChooseCard(merchant);
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
