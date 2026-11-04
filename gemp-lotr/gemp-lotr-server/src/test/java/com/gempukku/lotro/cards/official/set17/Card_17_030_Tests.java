package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_030_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("madril", "17_30");

					put("ranger", "7_116");     // Ranger of Osgiliath: [gondor] Man ranger; a regroup ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf ranger; +3 only against a Nazgul
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion who is no ranger; Knight is its whole text
					put("kog", "5_36");         // Knight of Gondor: Fellowship: discard a [gondor] condition to heal this companion
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose

					put("walls", "7_111");      // Man the Walls: a [gondor] condition in the support area
					put("nostranger", "1_108"); // No Stranger to the Shadows: a [gondor] condition borne by a ranger
					put("armor", "1_92");       // Armor: a [gondor] possession borne by a Man
					put("lordofmoria", "1_21"); // Lord of Moria: a [dwarven] condition in the support area
					put("songs", "1_61");       // Songs of the Blessed Realm: an [elven] condition in the support area

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, site 4; no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, site 5; Damage +1 only
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1, site 4; its play trigger never fires when cheated in
					put("beauty", "1_205");     // Beauty Is Fading: Shadow Maneuver event, exert a Nazgul to discard a Free Peoples possession or condition
					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, site 3 (roaming at site 2); nothing borne by her targets here
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: Nazgul, site 2 (not roaming at site 2); nothing below 2 burdens
					put("orcguard", "3_95");    // Orc Guard: [sauron] Orc, site 6; Maneuver: exert him and discard your [sauron] condition to discard a Free Peoples condition
					put("strength", "1_278");   // Strength Born of Fear: a [sauron] support condition, the Orc Guard's cost; its own ability is never used
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: Nazgul, site 3, fierce
					put("paleblade", "1_221");  // The Pale Blade: on The Witch-king; Response: if he wins a skirmish, exert him to discard a Free Peoples condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MadrilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Madril, Loyal Lieutenant
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Ranger</b>. <b>Hunter 1</b>.<br>While you can spot a roaming minion, your [gondor] conditions cannot be discarded from play by Shadow cards.<br><b>Maneuver:</b> Exert 2 rangers to make a minion's site number +2 until the regroup phase.
		*/

		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");

		assertEquals("Madril", madril.getBlueprint().getTitle());
		assertEquals("Loyal Lieutenant", madril.getBlueprint().getSubtitle());
		assertTrue(madril.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, madril.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, madril.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, madril.getBlueprint().getCardType());
		assertEquals(Race.MAN, madril.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(madril, Keyword.RANGER));
		assertTrue(scn.HasKeyword(madril, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(madril, Keyword.HUNTER));
		assertEquals(3, madril.getBlueprint().getTwilightCost());
		assertEquals(6, madril.getBlueprint().getStrength());
		assertEquals(3, madril.getBlueprint().getVitality());
		assertEquals(6, madril.getBlueprint().getResistance());
	}

	@Test
	public void WhileARoamingMinionIsSpottedAShadowEventCannotDiscardYourGondorConditions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var walls = scn.GetFreepsCard("walls");
		var nostranger = scn.GetFreepsCard("nostranger");
		var armor = scn.GetFreepsCard("armor");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(madril);
		scn.AttachCardsTo(madril, nostranger, armor);
		scn.MoveCardsToSupportArea(walls, lordofmoria);

		var lemenya = scn.GetShadowCard("lemenya");
		var beauty = scn.GetShadowCard("beauty");
		scn.MoveCardsToHand(beauty);
		assertInZone(Zone.SUPPORT, walls, lordofmoria);
		assertInZone(Zone.ATTACHED, nostranger, armor);
		assertInHand(beauty);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2: Lemenya, of site 3, is roaming.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(lemenya, Keyword.ROAMING));
		assertInZone(Zone.SUPPORT, walls, lordofmoria);
		assertInZone(Zone.ATTACHED, nostranger, armor);

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(beauty));
		scn.ShadowPlayCard(beauty);

		// A [gondor] possession and a condition of another culture may be discarded; neither [gondor] condition,
		// in the support area or borne, may be.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, lordofmoria));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(walls, nostranger));
		scn.ShadowChooseCard(lordofmoria);

		assertInDiscard(lordofmoria);
		assertInZone(Zone.SUPPORT, walls);
		assertInZone(Zone.ATTACHED, nostranger, armor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoRoamingMinionAShadowEventDiscardsAGondorCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var walls = scn.GetFreepsCard("walls");
		var nostranger = scn.GetFreepsCard("nostranger");
		var armor = scn.GetFreepsCard("armor");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(madril);
		scn.AttachCardsTo(madril, nostranger, armor);
		scn.MoveCardsToSupportArea(walls, lordofmoria);

		var nelya = scn.GetShadowCard("nelya");
		var beauty = scn.GetShadowCard("beauty");
		scn.MoveCardsToHand(beauty);
		assertInZone(Zone.SUPPORT, walls, lordofmoria);
		assertInZone(Zone.ATTACHED, nostranger, armor);
		assertInHand(beauty);

		scn.StartGame();
		scn.SetTwilight(5);
		scn.MoveMinionsToTable(nelya);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2: Nelya, of site 2, is the only minion and is not roaming.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(nelya, Keyword.ROAMING));
		assertInZone(Zone.SUPPORT, walls, lordofmoria);
		assertInZone(Zone.ATTACHED, nostranger, armor);

		scn.FreepsPass();
		scn.ShadowPlayCard(beauty);

		assertTrue(scn.ShadowHasCardChoiceAvailable(walls, nostranger, armor, lordofmoria));
		scn.ShadowChooseCard(walls);

		assertInDiscard(walls);
		assertInZone(Zone.ATTACHED, nostranger, armor);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AShadowMinionsAbilityCannotDiscardYourGondorConditionsEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var walls = scn.GetFreepsCard("walls");
		var nostranger = scn.GetFreepsCard("nostranger");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var songs = scn.GetFreepsCard("songs");
		scn.MoveCompanionsToTable(madril);
		scn.AttachCardsTo(madril, nostranger);
		scn.MoveCardsToSupportArea(walls, lordofmoria, songs);

		var orcguard = scn.GetShadowCard("orcguard");
		var strength = scn.GetShadowCard("strength");
		scn.MoveCardsToSupportArea(strength);
		assertInZone(Zone.SUPPORT, walls, lordofmoria, songs, strength);
		assertInZone(Zone.ATTACHED, nostranger);

		scn.StartGame();
		scn.MoveMinionsToTable(orcguard);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2: the Orc Guard, of site 6, is roaming.
		assertTrue(scn.HasKeyword(orcguard, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(orcguard));
		assertInZone(Zone.SUPPORT, walls, lordofmoria, songs, strength);
		assertInZone(Zone.ATTACHED, nostranger);

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(orcguard));
		scn.ShadowUseCardAction(orcguard);
		assertEquals(1, scn.GetWoundsOn(orcguard));
		assertInDiscard(strength);

		assertTrue(scn.ShadowHasCardChoiceAvailable(lordofmoria, songs));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(walls, nostranger));
		scn.ShadowChooseCard(songs);

		assertInDiscard(songs);
		assertInZone(Zone.SUPPORT, walls, lordofmoria);
		assertInZone(Zone.ATTACHED, nostranger);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AShadowPossessionsResponseCannotDiscardYourGondorConditionsEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var aragorn = scn.GetFreepsCard("aragorn");
		var walls = scn.GetFreepsCard("walls");
		var nostranger = scn.GetFreepsCard("nostranger");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var songs = scn.GetFreepsCard("songs");
		scn.MoveCompanionsToTable(madril, aragorn);
		scn.AttachCardsTo(madril, nostranger);
		scn.MoveCardsToSupportArea(walls, lordofmoria, songs);

		var witchking = scn.GetShadowCard("witchking");
		var paleblade = scn.GetShadowCard("paleblade");
		assertInZone(Zone.SUPPORT, walls, lordofmoria, songs);
		assertInZone(Zone.ATTACHED, nostranger);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, paleblade);
		// Aragorn 8 + 4 against 14 + 3: he loses without being overwhelmed, and lives.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);

		// Site 2: The Witch-king, of site 3, is roaming.
		assertTrue(scn.HasKeyword(witchking, Keyword.ROAMING));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertInZone(Zone.SUPPORT, walls, lordofmoria, songs);
		assertInZone(Zone.ATTACHED, nostranger);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(paleblade));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(witchking));

		assertTrue(scn.ShadowHasCardChoiceAvailable(lordofmoria, songs));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(walls, nostranger));
		scn.ShadowChooseCard(lordofmoria);

		assertInDiscard(lordofmoria);
		assertInZone(Zone.SUPPORT, walls, songs);
		assertInZone(Zone.ATTACHED, nostranger);
		// The response window is open again for him: The Witch-king can still exert.
		assertTrue(scn.ShadowDecisionAvailable("Optional responses"));
	}

	@Test
	public void AFreePeoplesCardMayStillDiscardYourGondorConditionWhileAMinionRoams() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var kog = scn.GetFreepsCard("kog");
		var walls = scn.GetFreepsCard("walls");
		scn.MoveCompanionsToTable(madril, kog);
		scn.MoveCardsToSupportArea(walls);
		scn.AddWoundsToChar(kog, 1);
		assertEquals(1, scn.GetWoundsOn(kog));
		assertInZone(Zone.SUPPORT, walls);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.FreepsRefreshDecision();

		// Site 1: the Troop, of site 4, is roaming.
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertEquals(1, scn.GetWoundsOn(kog));
		assertInZone(Zone.SUPPORT, walls);

		assertTrue(scn.FreepsActionAvailable(kog));
		scn.FreepsUseCardAction(kog);

		assertInDiscard(walls);
		assertEquals(0, scn.GetWoundsOn(kog));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverAbilityExertsTwoRangersToMakeAMinionsSiteNumberPlusTwoUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var knight = scn.GetFreepsCard("knight");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(madril, ranger, arwen, knight);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(madril));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(madril));

		scn.FreepsUseCardAction(madril);
		// Any two rangers, of either culture, Madril among them; no companion who is no ranger.
		assertTrue(scn.FreepsHasCardChoiceAvailable(madril, ranger, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(ranger, arwen);

		assertEquals(1, scn.GetWoundsOn(ranger));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(madril));
		assertEquals(0, scn.GetWoundsOn(knight));

		// Any minion, of two cultures and races.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(madril, ranger, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// 4 + 2; the Savage is left as printed.
		assertEquals(6, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// Still in force through the skirmishes: the second skirmish, after one already resolved. The Troop is left
		// unassigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{knight, savage}, new PhysicalCardImpl[]{ranger, runner});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(knight);
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(ranger);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetMinionSiteNumber(troop));

		// Gone as the regroup phase starts.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(5, scn.GetMinionSiteNumber(savage));
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWithOnlyOneRangerAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var madril = scn.GetFreepsCard("madril");
		var ranger = scn.GetFreepsCard("ranger");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(madril, ranger, knight);
		// Vitality 3 with 2 wounds: the Ranger is exhausted. The Knight can exert but is no ranger.
		scn.AddWoundsToChar(ranger, 2);
		assertEquals(2, scn.GetWoundsOn(ranger));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(madril));
		assertEquals(2, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(madril));
	}
}
