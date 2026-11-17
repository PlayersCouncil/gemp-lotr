package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_036_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("throne", "17_36");

					put("denethor", "7_86");    // Denethor, Wizened Steward: [gondor] Man companion; strength +1 for each other [gondor] Man
					// Aragorn, Ranger of the North: a [gondor] Man companion and ranger, strength 8, vitality 4; a
					// maneuver ability used only on purpose
					put("aragorn", "1_89");
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit, cost 2.  "To play, spot a [gondor]
					// companion"; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Gondorian Merchant: the [gondor] Man ally; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Gandalf, Friend of the Shirefolk: vitality 4; strength +1 for each race in the fellowship
					put("gandalf", "1_72");
					// Gandalf's Staff, Focus of Power: vitality +1 on Gandalf; strength +1 only while 4 twilight are spotted
					put("staff", "7_38");
					// Staff Asunder: Skirmish event, cost 0.  "Discard a staff Gandalf is bearing and then exert him
					// twice to discard a minion he is skirmishing."
					put("asunder", "2_27");
					// Defend It and Hope: Maneuver event, cost 0.  "Exert a [gondor] Man to wound a minion once (or twice
					// if that minion is an Uruk-hai)."
					put("defend", "4_115");
					put("whatare", "1_119");    // What Are They?: Maneuver event, cost 1.  "Spot a ranger to discard a roaming minion."
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession on Aragorn; damage +1

					// Garrison of Osgiliath: [gondor] support-area condition that takes 2 [gondor] tokens as it is
					// played; its skirmish ability is not used
					put("garrison", "6_52");
					// Heirs of Gondor: [gondor] support-area condition that takes a [gondor] token as it is played; its
					// skirmish ability is not used
					put("heirs", "13_69");
					// Ithilien Trap: [gondor] support-area condition that takes [gondor] tokens as [gondor] Men win
					// skirmishes, here with none; its maneuver ability is not used
					put("trap", "4_126");
					// Stout and Strong: [dwarven] support-area condition that takes [dwarven] tokens as Dwarves win
					// skirmishes; its skirmish ability is not used
					put("stout", "4_57");

					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its "when you play" is not reached, cheated
					// into play
					put("runner1", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					// Man of Bree: [men] Man, vitality 1, site 4 (roaming at site 2); strength +1 for each card in the
					// Free Peoples hand
					put("bree", "11_90");
					// Morgul Hunter: [sauron] Orc, strength 9, vitality 4; strength +1 for each companion
					put("hunter", "1_256");
					// Banner of the Eye: a [sauron] possession on a [sauron] Orc, strength +1; its trigger answers a
					// skirmish won, and none is
					put("banner", "6_98");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// A minion of 1 vitality to be killed in a skirmish, and a [gondor] card with a token to reinforce.
	protected VirtualTableScenario GetKilledMinionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("throne", "17_36");
					put("heirs", "13_69");
					put("aragorn", "1_89");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ThroneofMinasTirithStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Throne of Minas Tirith
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support area
		 * Game Text: To play, spot Denethor (or 2 [gondor] companions).<br>Each time a minion is killed or discarded from play during a skirmish, you may reinforce a [gondor] token.<br><b>Skirmish:</b> Remove 3 [gondor] tokens to discard a possession.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("throne");

		assertEquals("Throne of Minas Tirith", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// "Each time a minion is killed or discarded from play during a skirmish".  A killed minion is not discarded
	// (CR 4.2 "killed"; F-0444), so the definition has a `killed` trigger beside its `discarded` one: this holds
	// the kill.
	@Test
	public void AMinionKilledInASkirmishLetsAGondorTokenBeReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKilledMinionScenario();

		var throne = scn.GetFreepsCard("throne");
		var heirs = scn.GetFreepsCard("heirs");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(throne, heirs);
		// Heirs of Gondor is played with one token on it.
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		// 8 against 5: the Goblin Runner takes a wound and is killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithDenethorTheOnlyGondorCompanionItCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var denethor = scn.GetFreepsCard("denethor");
		var guard = scn.GetFreepsCard("guard");
		// Denethor is the one [gondor] companion; the other is a [rohan] Man.
		scn.MoveCompanionsToTable(denethor, guard);
		scn.MoveCardsToHand(throne);
		assertInHand(throne);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(throne));
		scn.FreepsPlayCard(throne);
		assertInZone(Zone.SUPPORT, throne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithOneGondorCompanionAndAGondorAllyItCannotBePlayedUntilASecondGondorCompanionIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		// No Denethor: one [gondor] companion, a [gondor] ally and a [rohan] companion.
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(throne, pippin);
		assertInHand(throne, pippin);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(throne));

		// A second [gondor] companion, and a Hobbit: now it can be played.
		scn.FreepsPlayCard(pippin);
		assertInZone(Zone.FREE_CHARACTERS, pippin);
		assertTrue(scn.FreepsPlayAvailable(throne));
		scn.FreepsPlayCard(throne);
		assertInZone(Zone.SUPPORT, throne);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMinionKilledInASkirmishOffersOnlyGondorTokensAndASecondKillInTheNextSkirmishOffersItAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var trap = scn.GetFreepsCard("trap");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.MoveCardsToSupportArea(throne, garrison, heirs, trap, stout);
		// The tokens each card takes in play: 2 as the Garrison is played, 1 as Heirs is, a [dwarven] one for a
		// Dwarf's win; none on the Trap.
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(stout, 1);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));

		var runner1 = scn.GetShadowCard("runner1");
		var bree = scn.GetShadowCard("bree");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, bree);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{guard, bree});

		// Aragorn's 8 against 5: the Runner of vitality 1 takes the wound and is killed.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsChooseOptionalTrigger(throne);

		// The cards with a [gondor] token; not the [gondor] Trap with none, the [dwarven] card or the Throne.
		assertTrue(scn.FreepsHasCardChoiceAvailable(garrison, heirs));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(trap, stout, throne));
		scn.FreepsChooseCard(heirs);
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));
		// The Trap's own trigger answers Aragorn's win, a [gondor] Man's, and is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trap));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(trap));

		// The Guard's 6 against the Man of Bree's 4 (a [men] Man, no card in the Free Peoples hand to add to it):
		// the second minion killed is of another culture and race, and it is offered again.  Declined, nothing changes.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInDiscard(bree);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionDiscardedFromPlayDuringASkirmishLetsAGondorTokenBeReinforced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var heirs = scn.GetFreepsCard("heirs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var asunder = scn.GetFreepsCard("asunder");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(throne, heirs);
		scn.AddTokensToCard(heirs, 1);
		scn.MoveCardsToHand(asunder);
		assertAttachedTo(staff, gandalf);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertInHand(asunder);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);

		// Staff Asunder discards the Savage, an [isengard] Uruk-hai, from play: no kill.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.FreepsPlayCard(asunder);
		assertInDiscard(savage, staff);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsAcceptOptionalTrigger();

		// Heirs of Gondor, the one card with a [gondor] token, is chosen unasked.
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionOfAnotherCultureAndRaceDiscardedFromPlayDuringASkirmishLetsAGondorTokenBeReinforcedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var heirs = scn.GetFreepsCard("heirs");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		var asunder = scn.GetFreepsCard("asunder");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(throne, heirs);
		scn.AddTokensToCard(heirs, 1);
		scn.MoveCardsToHand(asunder);
		assertAttachedTo(staff, gandalf);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertInHand(asunder);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		// Staff Asunder discards the Troop, a [moria] Orc, from play: no kill.
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(asunder);
		assertInDiscard(troop, staff);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsAcceptOptionalTrigger();

		// Heirs of Gondor, the one card with a [gondor] token, is chosen unasked.
		assertEquals(2, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionKilledAndAMinionDiscardedInTheManeuverPhaseSetNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var heirs = scn.GetFreepsCard("heirs");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		var whatare = scn.GetFreepsCard("whatare");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(throne, heirs);
		scn.AddTokensToCard(heirs, 1);
		scn.MoveCardsToHand(defend, whatare);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertInHand(defend, whatare);

		var runner1 = scn.GetShadowCard("runner1");
		var bree = scn.GetShadowCard("bree");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, bree, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// Defend It and Hope wounds the Runner of vitality 1: it is killed, outside any skirmish.
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(runner1);
		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(throne));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// What Are They? discards the Man of Bree, roaming at site 2, from play.
		scn.FreepsPlayCard(whatare);
		scn.FreepsChooseCard(bree);
		assertInDiscard(bree);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(throne));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void ACompanionKilledInASkirmishSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var heirs = scn.GetFreepsCard("heirs");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToSupportArea(throne, heirs);
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, troop);
		scn.FreepsResolveSkirmish(guard);

		// 13 against 6: the Guard is overwhelmed and killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(throne));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoGondorTokenInPlayAMinionKilledInASkirmishOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var trap = scn.GetFreepsCard("trap");
		var stout = scn.GetFreepsCard("stout");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(throne, trap, stout);
		scn.AddTokensToCard(stout, 1);
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner1);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Killed in a skirmish, but nothing could be reinforced: the offer is not made (Q-0224).  The Trap's own
		// trigger answers Aragorn's win at the same moment, and is declined.
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(trap));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(throne));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetCultureTokensOn(trap));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesThreeGondorTokensToDiscardAPossessionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var trap = scn.GetFreepsCard("trap");
		var stout = scn.GetFreepsCard("stout");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gandalf = scn.GetFreepsCard("gandalf");
		var staff = scn.GetFreepsCard("staff");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToSupportArea(throne, garrison, heirs, trap, stout);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);
		scn.AddTokensToCard(stout, 1);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(staff, gandalf);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInZone(Zone.SUPPORT, throne, trap);

		var hunter = scn.GetShadowCard("hunter");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		scn.MoveMinionsToTable(hunter);
		scn.AttachCardsTo(hunter, banner);
		assertAttachedTo(banner, hunter);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hunter);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(throne));
		scn.FreepsUseCardAction(throne);

		// The three [gondor] tokens are taken from the two cards that hold them, a card at a time.
		scn.FreepsChooseCard(garrison);
		scn.FreepsChooseCard(garrison);
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));

		// A possession of either side; not an artifact (the Throne, Gandalf's Staff) or a condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sword, banner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(throne, staff, trap, garrison));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(banner);
		assertInDiscard(banner);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(staff, gandalf);
		assertInZone(Zone.SUPPORT, throne, trap);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(throne, garrison, heirs);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);
		assertAttachedTo(sword, aragorn);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(throne));

		// In the skirmish that follows it is offered.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(throne));
	}

	@Test
	public void WithTwoGondorTokensAndADwarvenTokenTheSkirmishAbilityIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var stout = scn.GetFreepsCard("stout");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(throne, garrison, heirs, stout);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(stout, 1);
		assertAttachedTo(sword, aragorn);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertEquals(1, scn.GetCultureTokensOn(stout));

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(throne));

		// A third [gondor] token, on Heirs of Gondor, and it is offered.
		scn.AddTokensToCard(heirs, 1);
		scn.FreepsRefreshDecision();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(throne));
	}

	@Test
	public void WithNoPossessionInPlayTheSkirmishAbilityIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var throne = scn.GetFreepsCard("throne");
		var garrison = scn.GetFreepsCard("garrison");
		var heirs = scn.GetFreepsCard("heirs");
		var trap = scn.GetFreepsCard("trap");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(throne, garrison, heirs, trap);
		scn.AddTokensToCard(garrison, 2);
		scn.AddTokensToCard(heirs, 1);
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertInZone(Zone.SUPPORT, throne, garrison, heirs, trap);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.FreepsActionAvailable(throne));
		scn.FreepsUseCardAction(throne);
		scn.FreepsChooseCard(garrison);
		scn.FreepsChooseCard(garrison);

		// The tokens are paid, and there is no possession to discard.
		assertEquals(0, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetCultureTokensOn(heirs));
		assertInZone(Zone.SUPPORT, throne, garrison, heirs, trap);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
