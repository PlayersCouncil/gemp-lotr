package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pallando", "13_37");
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Glóin: [dwarven] Dwarf, strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is an Ent and no Wizard; strength +2 only beside 3 Ents
					// Rohan Stable Master: another follower, Aid (1); "Bearer is considered to be mounted."
					put("stablemaster", "15_134");
					put("lordofmoria", "1_21");   // Lord of Moria: [dwarven] support-area condition; each Dwarf is damage +1
					put("armor", "1_8");          // Dwarven Armor: a Free Peoples possession on a Dwarf; changes no number
					put("weary", "4_212");        // Weary: [isengard] condition on a companion; answers only an [isengard] tracker
					put("arrows", "1_141");       // Their Arrows Enrage: [isengard] support-area condition; archers -1, and none is here
					put("poison", "8_28");        // Spider Poison: a [gollum] possession lying in the Shadow support area
					put("troop", "1_177");        // Goblin Patrol Troop: no text; only there so that a maneuver phase is played
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("pursuer", "4_190");      // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PallandoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Pallando, Far-travelling One
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Follower
		 * Strength: +1
		 * Game Text: <b>Aid</b> – (2).
		 * 	Each time you transfer this to a companion, except a [gandalf] companion, exert bearer twice.
		 * 	Each time bearer wins a skirmish, you may discard a condition from play.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("pallando");

		assertEquals("Pallando", card.getBlueprint().getTitle());
		assertEquals("Far-travelling One", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
		assertEquals(2, scn.GetAidTwilightCost(card));
	}

	@Test
	public void TransferredByItsAidToACompanionWhoIsNotGandalfCultureItExertsHimTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCompanionsToTable(aragorn, avenger);
		scn.MoveCardsToSupportArea(pallando);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsAcceptOptionalTrigger();

		// Any companion may be given him, a [gandalf] one included.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, avenger));
		scn.FreepsChooseCard(aragorn);

		// Aid - (2), and Aragorn, a [gondor] Man, exerts twice.
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(pallando, aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredToAGandalfCompanionHeExertsNobodyNorDoesAnotherFollowerTransferredToAnotherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var avenger = scn.GetFreepsCard("avenger");
		var pallando = scn.GetFreepsCard("pallando");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.MoveCompanionsToTable(aragorn, avenger);
		scn.MoveCardsToSupportArea(pallando, stablemaster);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(avenger));

		// Ent Avenger is a [gandalf] companion who is neither a Wizard nor a Gandalf.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsChooseOptionalTrigger(pallando);
		scn.FreepsChooseCard(avenger);

		assertAttachedTo(pallando, avenger);
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(avenger));

		// The other follower goes to Aragorn, who would be exerted if Pallando, borne elsewhere, answered it.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(stablemaster));
		scn.FreepsChooseOptionalTrigger(stablemaster);
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(stablemaster, aragorn);
		assertEquals(pool + 3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredAgainInASecondManeuverPhaseHeExertsTheNewBearerTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pallando = scn.GetFreepsCard("pallando");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(pallando);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(pallando, aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));

		// Nobody is assigned to the Troop. The fellowship moves again, and a second maneuver phase is played.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gimli);

		// Gimli, a [dwarven] Dwarf, exerts twice; Aragorn is not exerted again.
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(pallando, gimli);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WhenBearerWinsASkirmishAnyConditionInPlayMayBeDiscardedButNotWhenAnotherCompanionWins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pallando = scn.GetFreepsCard("pallando");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, pallando);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(lordofmoria);

		var weary = scn.GetShadowCard("weary");
		var arrows = scn.GetShadowCard("arrows");
		var poison = scn.GetShadowCard("poison");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.AttachCardsTo(gimli, weary);
		scn.MoveCardsToSupportArea(arrows, poison);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		// His Aid is offered for him where he is borne already; he stays with Aragorn.
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, savage});

		// Gimli, who does not bear him, wins first: nothing is offered.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInDiscard(runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pallando));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));

		// Aragorn, his bearer, wins next.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.SUPPORT, lordofmoria, arrows, poison);
		assertAttachedTo(weary, gimli);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsAcceptOptionalTrigger();

		// Conditions of either side, in a support area or borne; no possession of either side.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lordofmoria, weary, arrows));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, poison, pallando));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(weary);

		assertInDiscard(weary);
		assertInZone(Zone.SUPPORT, lordofmoria, arrows, poison);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheDiscardWhenBearerWinsLeavesEveryConditionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pallando = scn.GetFreepsCard("pallando");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, pallando);
		scn.MoveCardsToSupportArea(lordofmoria);

		var arrows = scn.GetShadowCard("arrows");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(arrows);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertInZone(Zone.SUPPORT, lordofmoria, arrows);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, lordofmoria, arrows);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoConditionInPlayBearerWinningOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var pallando = scn.GetFreepsCard("pallando");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, pallando);
		scn.AttachCardsTo(gimli, armor);

		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(poison);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, poison);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Possessions of either side, and no condition: there is nothing the discard could take.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(pallando));
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, poison);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsAConditionMayBeDiscardedInTheFierceSkirmishToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var pallando = scn.GetFreepsCard("pallando");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, pallando);
		scn.MoveCardsToSupportArea(lordofmoria);

		var arrows = scn.GetShadowCard("arrows");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCardsToSupportArea(arrows);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, pursuer);
		assertEquals(0, scn.GetWoundsOn(pursuer));
		assertInZone(Zone.SUPPORT, lordofmoria, arrows);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(arrows);
		assertInDiscard(arrows);
		assertInZone(Zone.SUPPORT, lordofmoria);

		// The Pursuer, fierce and wounded once, is fought again.
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, pursuer);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsAcceptOptionalTrigger();

		// The one condition left is his own, and it goes.
		assertInDiscard(lordofmoria);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
