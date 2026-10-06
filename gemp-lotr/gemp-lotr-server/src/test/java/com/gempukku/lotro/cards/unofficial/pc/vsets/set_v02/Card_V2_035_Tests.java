package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.ArcheryTotalModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_035_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wall1", "102_35");
					put("wall2", "102_35");

					// Uruk-hai costing 2.  Damage +1 is the whole text of each.
					put("savage", "1_151");         // Uruk Savage: [isengard], strength 5, vitality 3
					put("orthanc", "4_165");        // Orthanc Warrior: [isengard], strength 7, vitality 2
					put("sentry", "10_84");         // Cirith Ungol Sentry: [sauron], strength 8, vitality 2
					// Uruk-hai costing 3.
					put("urukwarrior", "1_156");    // Uruk Warrior: [isengard], vitality 2; Damage +1, and a maneuver ability that needs 6 companions
					put("guard", "10_81");          // Cirith Ungol Guard: [sauron], vitality 2; Damage +1 and nothing else
					// Uruk-hai costing 4.
					put("party", "4_207");          // Uruk-hai Raiding Party: [isengard], vitality 3; Damage +1 and nothing else
					// Uruk-hai costing 0 and costing 1, each with 1 vitality.
					put("sieger", "18_129");        // White Hand Sieger: [uruk-hai], costs 0; Damage +1, and a requirement to play that a minion cheated in never meets
					put("footsoldier", "4_187");    // Uruk Foot Soldier: [isengard], costs 1; Tracker, Fierce, and the rest of its text is for a fierce skirmish
					// [isengard] Orcs, which are not Uruk-hai.  Each has a regroup ability that is only ever used on purpose.
					put("worker", "3_62");          // Isengard Worker: costs 2, vitality 3
					put("servant", "3_58");         // Isengard Servant: costs 3, vitality 3

					put("legolas", "1_50");         // Legolas, Greenleaf: Archer, vitality 3; "Archery: Exert Legolas to wound a minion; Legolas does not add to the fellowship archery total."
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability that is only ever used on purpose
					put("pippin", "1_306");         // Pippin, Friend to Frodo: an unbound Hobbit, vitality 4; his text is about [shire] tales
					put("gilgalad", "9_15");        // Gil-galad, Elven High King: vitality 4; "Regroup: Exert Gil-galad to wound each wounded minion."
					put("sting", "1_313");          // Sting: Frodo's hand weapon; its own ability is for the fellowship and regroup phases
					put("orcbane", "2_109");        // Orc-bane: "Maneuver: Spot Sting or Glamdring and exert its bearer X times to wound X Orcs or X Uruk-hai."
					put("unheeded", "8_115");       // Unheeded: response event, "If a minion exerts, exert an unbound Hobbit to wound that minion."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukShieldWallStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Uruk Shield Wall
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Machine. Each time an Uruk-hai costing X is about to take a wound (except during a skirmish),
		 * prevent that and exert it. X is the number of [isengard] tokens you can spot here.
		 * Maneuver: Exert an Uruk-hai to add an [isengard] token here.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wall1");

		assertEquals("Uruk Shield Wall", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysFromHandToTheSupportAreaForOneTwilight() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall = scn.GetShadowCard("wall1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(wall);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// 5 set + 1 companion + 2 for the site.
		assertEquals(8, scn.GetTwilight());
		assertInZone(Zone.HAND, wall);
		assertTrue(scn.ShadowPlayAvailable(wall));

		scn.ShadowPlayCard(wall);
		assertEquals(7, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, wall);
		assertNotAttachedTo(wall, savage);
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ArcheryWoundsOnAnUrukHaiCostingXArePreventedAndExertHimUntilHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var wall = scn.GetShadowCard("wall1");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, worker);
		// Legolas is the fellowship's one archer; two more arrows so that one minion can be hit three times.
		scn.ApplyAdHocModifier(new ArcheryTotalModifier(null, Side.FREE_PEOPLE, 2));
		scn.SkipToArcheryWounds();

		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(2, scn.GetCultureTokensOn(wall));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, worker));

		// Uruk Savage costs 2 and vitality 3.  The first arrow: no wound, one exertion.
		scn.ShadowChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		// Nobody was asked whether to use the Wall: the next thing is the second arrow.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, worker));

		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, worker));

		// Exhausted, he cannot exert, and the third arrow is prevented all the same.
		scn.ShadowChooseCard(savage);
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(0, scn.GetWoundsOn(worker));
		assertEquals(2, scn.GetCultureTokensOn(wall));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheExertionIsAnExertionAndAWoundOnAnOrcCostingXIsAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var pippin = scn.GetFreepsCard("pippin");
		var unheeded = scn.GetFreepsCard("unheeded");
		scn.MoveCompanionsToTable(legolas, pippin);
		scn.MoveCardsToHand(unheeded);

		var wall = scn.GetShadowCard("wall1");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, worker);
		scn.SkipToPhase(Phase.ARCHERY);

		// Isengard Worker costs 2 as well, and is an Orc.  He takes the wound, and a wound is nothing Unheeded
		// answers.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, worker));
		scn.FreepsChooseCard(worker);
		assertEquals(1, scn.GetWoundsOn(worker));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPass();

		// Uruk Savage is exerted instead, and Unheeded answers a minion exerting.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unheeded));

		// Unheeded's own wound on him is prevented in turn, and exerts him again.
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.FreepsChooseOptionalTrigger(unheeded);
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void EveryUrukHaiCostingExactlyXIsProtectedAndAnOrcOrAnUrukHaiOfAnotherCostIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gilgalad = scn.GetFreepsCard("gilgalad");
		scn.MoveCompanionsToTable(gilgalad);

		var wall = scn.GetShadowCard("wall1");
		var urukwarrior = scn.GetShadowCard("urukwarrior");
		var guard = scn.GetShadowCard("guard");
		var servant = scn.GetShadowCard("servant");
		var orthanc = scn.GetShadowCard("orthanc");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(urukwarrior, guard, servant, orthanc, party);
		// Every minion is on its last point of vitality: a wound that gets through kills, and one that is
		// prevented leaves an Uruk-hai who cannot exert exactly as he was.
		scn.AddWoundsToChar(urukwarrior, 1);    // [isengard] Uruk-hai costing 3
		scn.AddWoundsToChar(guard, 1);          // [sauron] Uruk-hai costing 3
		scn.AddWoundsToChar(servant, 2);        // [isengard] Orc costing 3
		scn.AddWoundsToChar(orthanc, 1);        // Uruk-hai costing 2
		scn.AddWoundsToChar(party, 2);          // Uruk-hai costing 4
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetCultureTokensOn(wall));
		assertInZone(Zone.SHADOW_CHARACTERS, urukwarrior, guard, servant, orthanc, party);

		// "Wound each wounded minion."
		scn.FreepsUseCardAction(gilgalad);

		assertInZone(Zone.SHADOW_CHARACTERS, urukwarrior, guard);
		assertEquals(1, scn.GetWoundsOn(urukwarrior));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInDiscard(servant, orthanc, party);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ATokenAddedInTheManeuverPhaseMovesTheProtectionFromUrukHaiCostingTwoToThoseCostingThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var legolas = scn.GetFreepsCard("legolas");
		var sting = scn.GetFreepsCard("sting");
		var orcbane = scn.GetFreepsCard("orcbane");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(frodo, sting);
		scn.MoveCardsToHand(orcbane);

		var wall = scn.GetShadowCard("wall1");
		var orthanc = scn.GetShadowCard("orthanc");
		var sentry = scn.GetShadowCard("sentry");
		var urukwarrior = scn.GetShadowCard("urukwarrior");
		var guard = scn.GetShadowCard("guard");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(orthanc, sentry, urukwarrior, guard, party);
		// Two Uruk-hai costing 2 and two costing 3, each on his last point of vitality.  Uruk-hai Raiding Party
		// is unhurt, to pay for the Wall's ability.
		scn.AddWoundsToChar(orthanc, 1);
		scn.AddWoundsToChar(sentry, 1);
		scn.AddWoundsToChar(urukwarrior, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.SkipToPhase(Phase.MANEUVER);

		// 2 tokens: Orc-bane wounds one Uruk-hai of each cost.  The one costing 2 is untouched and the one
		// costing 3 dies.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(2, scn.GetCultureTokensOn(wall));
		scn.FreepsPlayCard(orcbane);
		scn.FreepsChoose("2");
		scn.FreepsChooseCards(orthanc, urukwarrior);
		assertInZone(Zone.SHADOW_CHARACTERS, orthanc);
		assertEquals(1, scn.GetWoundsOn(orthanc));
		assertInDiscard(urukwarrior);

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(wall));
		assertEquals(0, scn.GetWoundsOn(party));
		// Uruk-hai Raiding Party is the only Uruk-hai who can exert.
		scn.ShadowUseCardAction(wall);
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(3, scn.GetCultureTokensOn(wall));

		scn.FreepsPass();
		scn.ShadowPass();

		// 3 tokens: now the Uruk-hai costing 2 dies and the one costing 3 is untouched.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(sentry);
		assertInDiscard(sentry);
		scn.ShadowPass();

		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(guard);
		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void AWallWithNoTokensProtectsAnUrukHaiCostingNothingAndNotOneCostingOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var wall = scn.GetShadowCard("wall1");
		var sieger = scn.GetShadowCard("sieger");
		var footsoldier = scn.GetShadowCard("footsoldier");
		scn.MoveCardsToSupportArea(wall);

		scn.StartGame();
		scn.MoveMinionsToTable(sieger, footsoldier);
		scn.SkipToPhase(Phase.ARCHERY);

		// No tokens, so X is 0.  White Hand Sieger costs 0 and Uruk Foot Soldier costs 1.  Each has 1 vitality:
		// a wound that gets through kills, and one that is prevented leaves an Uruk-hai who cannot exert
		// exactly as he was.
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertInZone(Zone.SHADOW_CHARACTERS, sieger, footsoldier);

		scn.FreepsUseCardAction(legolas);
		assertTrue(scn.FreepsHasCardChoiceAvailable(sieger, footsoldier));
		scn.FreepsChooseCard(sieger);
		assertInZone(Zone.SHADOW_CHARACTERS, sieger);
		assertEquals(0, scn.GetWoundsOn(sieger));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPass();

		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(footsoldier);
		assertInDiscard(footsoldier);
		assertInZone(Zone.SHADOW_CHARACTERS, sieger);
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void WoundsDuringASkirmishAreNotPreventedForAnUrukHaiInItOrOutOfIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var unheeded = scn.GetFreepsCard("unheeded");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveCardsToHand(unheeded);

		var wall = scn.GetShadowCard("wall1");
		var orthanc = scn.GetShadowCard("orthanc");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(wall);
		scn.AddTokensToCard(wall, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(orthanc, savage);
		// Both cost 2.  Orthanc Warrior (vitality 2) is on his last point; Uruk Savage (vitality 3) has one to
		// exert with.
		scn.AddWoundsToChar(orthanc, 1);
		scn.AddWoundsToChar(savage, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, orthanc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetCultureTokensOn(wall));
		assertTrue(scn.IsCharSkirmishing(orthanc));
		assertFalse(scn.IsCharSkirmishing(savage));
		scn.FreepsPass();

		// Ettenmoors lets the Shadow player exert a minion of his in a skirmish.  Uruk Savage, who is not in
		// this one, is the only minion who can.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowUseCardAction(scn.GetCurrentSite());
		assertEquals(2, scn.GetWoundsOn(savage));

		// Unheeded wounds him while the skirmish is going on: not prevented, and it is his last.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unheeded));
		scn.FreepsChooseOptionalTrigger(unheeded);
		assertInDiscard(savage);

		// Aragorn (8) beats Orthanc Warrior (7), whose wound for losing is not prevented either.
		assertInZone(Zone.SHADOW_CHARACTERS, orthanc);
		scn.PassSkirmishActions();
		assertInDiscard(orthanc);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ManeuverActionExertsAChosenUrukHaiToAddATokenToThatCopyUntilNoUrukHaiCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wall1 = scn.GetShadowCard("wall1");
		var wall2 = scn.GetShadowCard("wall2");
		var guard = scn.GetShadowCard("guard");
		var party = scn.GetShadowCard("party");
		var orthanc = scn.GetShadowCard("orthanc");
		var servant = scn.GetShadowCard("servant");
		scn.MoveCardsToSupportArea(wall1, wall2);
		scn.AddTokensToCard(wall1, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(guard, party, orthanc, servant);
		// Orthanc Warrior is an Uruk-hai who cannot exert; Isengard Servant can exert and is an Orc.
		scn.AddWoundsToChar(orthanc, 1);
		scn.FreepsPass();

		// Not in the Shadow phase.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wall1));
		assertFalse(scn.ShadowActionAvailable(wall2));
		scn.ShadowPass();

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(wall1));
		assertTrue(scn.ShadowActionAvailable(wall2));
		assertEquals(3, scn.GetCultureTokensOn(wall1));
		assertEquals(0, scn.GetCultureTokensOn(wall2));
		assertEquals(0, scn.GetWoundsOn(guard));

		scn.ShadowUseCardAction(wall1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, party));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(orthanc, servant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		// Cirith Ungol Guard costs 3 and this copy has 3 tokens: exerting to pay is not taking a wound, so he
		// has the one exertion and no more.
		scn.ShadowChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(4, scn.GetCultureTokensOn(wall1));
		assertEquals(0, scn.GetCultureTokensOn(wall2));

		// The other copy takes its own token.  Uruk-hai Raiding Party (vitality 3) is the only Uruk-hai left
		// who can exert, twice.
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(party));
		scn.ShadowUseCardAction(wall2);
		assertEquals(1, scn.GetWoundsOn(party));
		assertEquals(4, scn.GetCultureTokensOn(wall1));
		assertEquals(1, scn.GetCultureTokensOn(wall2));

		scn.FreepsPass();
		scn.ShadowUseCardAction(wall2);
		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(2, scn.GetCultureTokensOn(wall2));

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(wall1));
		assertFalse(scn.ShadowActionAvailable(wall2));
		assertEquals(0, scn.GetWoundsOn(servant));
	}

	@Test
	public void TwoCopiesWithTheSameTokensExertOnceForOneWoundAndTheirTokensAreNotAddedTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var wall1 = scn.GetShadowCard("wall1");
		var wall2 = scn.GetShadowCard("wall2");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		scn.MoveCardsToSupportArea(wall1, wall2);
		scn.AddTokensToCard(wall1, 2);
		scn.AddTokensToCard(wall2, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, party);
		// Uruk-hai Raiding Party costs 4, the two copies' tokens together, and is on its last point of vitality.
		scn.AddWoundsToChar(party, 2);
		scn.SkipToPhase(Phase.ARCHERY);

		// Uruk Savage costs 2, which is each copy's X.  Both answer the one wound, and the Free Peoples player
		// says which goes first; once it is prevented there is nothing left for the other.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(savage);
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveActionOrder("Uruk Shield Wall");
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		scn.ShadowPass();

		scn.FreepsUseCardAction(legolas);
		scn.FreepsChooseCard(party);
		assertInDiscard(party);
		assertEquals(2, scn.GetCultureTokensOn(wall1));
		assertEquals(2, scn.GetCultureTokensOn(wall2));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
