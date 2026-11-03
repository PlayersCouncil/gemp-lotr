package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_114_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ballast", "5_114");
					// Sam, Son of Hamfast: [shire] Hobbit companion; a fellowship ability and a response to Frodo's
					// death, neither used here
					put("sam", "1_311");
					// Merry, From O'er the Brandywine: a [shire] Hobbit companion of another title; +2 only with a weapon
					put("merry", "1_303");
					// Sméagol, Poor Creature: [gollum] companion; to play, add a burden; a skirmish ability used only on purpose
					put("smeagol", "6_45");
					// Smeagol, Once Like Us: the [shire] Sméagol; an assignment ability used only on purpose
					put("oncelikeus", "103_116");
					// Gollum, Half a Wraith Himself: [wraith] minion; a trigger on a Nazgûl played and an assignment
					// ability, neither reached
					put("wraithgollum", "103_66");
					// Gollum, Nasty Treacherous Creature: [gollum] minion; a regroup ability used only on purpose
					put("gollum", "5_24");
					// Shelob, Last Child of Ungoliant: a [gollum] minion of another title; enduring, fierce
					put("shelob", "8_26");
					// Orc Slayer: [sauron] Orc minion; "Regroup: Exert this minion to wound a companion (except the
					// Ring-bearer)", and no other text
					put("slayer", "3_97");
					put("guard", "5_83");           // Household Guard: [rohan] Man companion, valiant, no other text
					// Rohirrim Shield: [rohan] possession on a [rohan] Man; minion archery total -1;
					// "Regroup: Discard this possession to heal bearer"
					put("shield", "5_91");
					put("gimli", "1_13");           // Gimli, Son of Glóin: "Skirmish: Exert Gimli to make him strength +2"
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5, vitality 1; "When you play" only
					// Aragorn, Ranger of the North: the ranger An Able Guide spots; a maneuver ability used only on purpose
					put("aragorn", "1_89");
					put("guide", "1_88");           // An Able Guide: [gondor] Regroup event, "Spot a ranger to remove (4)"
					// Huorn: "Response: If this companion is about to take a wound, exert an Ent to prevent that";
					// strength +1 for each Ent, read nowhere
					put("huorn", "9_25");
					put("avenger", "6_27");         // Ent Avenger: the Ent Huorn exerts; +2 only beside 3 Ents
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RareGoodBallastStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Rare Good Ballast
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype:
		 * Vitality: 1
		 * Game Text: Plays on Sam.<br><b>Response:</b> If a regroup special ability is used, discard Sméagol or Gollum to cancel that action.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ballast");

		assertEquals("Rare Good Ballast", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedUntilSamIsInPlayAndThenGoesOnHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ballast = scn.GetFreepsCard("ballast");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(ballast, sam);

		scn.StartGame();

		// Frodo and Merry are Hobbits who are not Sam.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertInZone(Zone.HAND, ballast, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(ballast));

		scn.FreepsPlayCard(sam);
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertTrue(scn.FreepsPlayAvailable(ballast));
		scn.FreepsPlayCard(ballast);
		assertAttachedTo(ballast, sam);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardsSmeagolToCancelAShadowRegroupSpecialAbilityWhoseCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ballast = scn.GetFreepsCard("ballast");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(sam, merry, smeagol);
		scn.AttachCardsTo(sam, ballast);

		var slayer = scn.GetShadowCard("slayer");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		assertAttachedTo(ballast, sam);
		scn.MoveMinionsToTable(slayer, wraithgollum, shelob);

		// Nobody is assigned, so the minions are still in play in the regroup phase.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Shelob is fierce: the fierce assignment phase is declined too.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(smeagol));
		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ballast));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.FreepsGetChoiceMin());
		// A Sméagol, and a Gollum of the other player's.
		assertTrue(scn.FreepsHasCardChoiceAvailable(smeagol, wraithgollum));
		// A [gollum] minion of another title, and Sam.
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shelob, sam));
		scn.FreepsChooseCard(smeagol);

		assertInZone(Zone.DISCARD, smeagol);
		assertInZone(Zone.SHADOW_CHARACTERS, wraithgollum, shelob);
		// The ability's cost stays paid (CR 4.2 "cancel"); its wound is not placed.
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertAttachedTo(ballast, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DiscardsGollumToCancelAFreePeoplesPossessionsRegroupSpecialAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ballast = scn.GetFreepsCard("ballast");
		var sam = scn.GetFreepsCard("sam");
		var oncelikeus = scn.GetFreepsCard("oncelikeus");
		var guard = scn.GetFreepsCard("guard");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(sam, oncelikeus, guard);
		scn.AttachCardsTo(sam, ballast);
		scn.AttachCardsTo(guard, shield);

		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		assertAttachedTo(ballast, sam);
		assertAttachedTo(shield, guard);
		scn.AddWoundsToChar(guard, 1);
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertTrue(scn.FreepsActionAvailable(shield));
		scn.FreepsUseCardAction(shield);

		// The text names no side: the Free Peoples player's own ability is answered too.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ballast));
		scn.FreepsAcceptOptionalTrigger();
		// The [shire] Sméagol, and a [gollum] Gollum.
		assertTrue(scn.FreepsHasCardChoiceAvailable(oncelikeus, gollum));
		scn.FreepsChooseCard(gollum);

		assertInZone(Zone.DISCARD, gollum, shield);
		assertInZone(Zone.FREE_CHARACTERS, oncelikeus);
		// The Shield is gone as its cost; its heal is cancelled.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForASkirmishSpecialAbilityNorForARegroupEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ballast = scn.GetFreepsCard("ballast");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guide = scn.GetFreepsCard("guide");
		scn.MoveCompanionsToTable(sam, smeagol, gimli, aragorn);
		scn.AttachCardsTo(sam, ballast);
		scn.MoveCardsToHand(guide);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(ballast, sam);
		assertInZone(Zone.HAND, guide);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, runner);
		scn.FreepsResolveSkirmish(gimli);

		// A skirmish special ability, in the phase before the regroup phase.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// A regroup event is no special ability.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.SetTwilight(6);
		assertEquals(6, scn.GetTwilight());
		scn.FreepsPlayCard(guide);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		// 6 - 4
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedForAResponseSpecialAbilityUsedInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ballast = scn.GetFreepsCard("ballast");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var huorn = scn.GetFreepsCard("huorn");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(sam, smeagol, huorn, avenger);
		scn.AttachCardsTo(sam, ballast);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		assertAttachedTo(ballast, sam);
		scn.MoveMinionsToTable(slayer);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertEquals(0, scn.GetWoundsOn(avenger));
		scn.FreepsPass();

		// The Slayer's regroup ability is answered, and let go.
		scn.ShadowUseCardAction(slayer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ballast));
		scn.FreepsDeclineOptionalTrigger();
		scn.ShadowChooseCard(huorn);

		// Huorn's Response, used in the regroup phase, is no regroup special ability.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(huorn));
		scn.FreepsChooseOptionalTrigger(huorn);
		assertEquals(1, scn.GetWoundsOn(avenger));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ballast));
		assertEquals(0, scn.GetWoundsOn(huorn));
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoSmeagolOrGollumToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ballast = scn.GetFreepsCard("ballast");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(sam, merry);
		scn.AttachCardsTo(sam, ballast);

		var slayer = scn.GetShadowCard("slayer");
		var shelob = scn.GetShadowCard("shelob");

		scn.StartGame();
		assertAttachedTo(ballast, sam);
		scn.MoveMinionsToTable(slayer, shelob);
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		// Shelob is fierce: the fierce assignment phase is declined too.
		scn.SkipToAssignments();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, slayer, shelob);
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.FreepsPass();

		scn.ShadowUseCardAction(slayer);
		// No response window: the Slayer's choice of companion comes next.
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ballast));
		scn.ShadowChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertEquals(1, scn.GetWoundsOn(merry));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
