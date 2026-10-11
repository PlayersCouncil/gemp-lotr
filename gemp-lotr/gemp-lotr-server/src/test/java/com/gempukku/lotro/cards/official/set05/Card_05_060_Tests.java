package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("siege", "5_60");
					put("siege2", "5_60");        // a second copy: the card is not unique and says "this condition"

					// Other machines. Their own abilities place a token on themselves or need tokens to be used.
					put("devilry", "5_49");       // Devilry of Orthanc: [isengard] condition, machine
					put("ram", "5_44");           // Battering Ram: [isengard] condition, machine
					// Scaling Ladder: [isengard] condition, machine. "Skirmish: Spot an [isengard] token here to
					// heal an Uruk-hai. Discard this condition."
					put("ladder", "5_57");
					// Uruk Shield Wall: a machine that is a possession, in the support area. Its text is about an
					// Uruk-hai wounded outside a skirmish.
					put("wall", "102_35");
					// Machinery of War: an [isengard] condition in the support area that is no machine.
					put("machinery", "102_28");

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5, strength 5, vitality 3
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai, cost 3, site 6; damage +1 is its whole text
					put("forger", "3_56");        // Isengard Forger: [isengard] Orc, cost 2, site 4; a Shadow ability used only on purpose
					put("runner", "1_178");       // Goblin Runner: harmless when cheated in

					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: strength 7; a fellowship ability never used here
					put("sleep", "1_84");         // Sleep, Caradhras: "Fellowship: Exert Gandalf to discard every condition."
					// Albert Dreary: ally. "Maneuver: Exert Albert Dreary to discard an [isengard] or [moria]
					// condition." Vitality 3; to play, spot Gandalf, so he is cheated in.
					put("albert", "1_69");
					put("rohanguard1", "5_83");   // Household Guard: [rohan] Man; Valiant is his whole text
					put("rohanguard2", "5_83");
					// Arrow-slits: Free Peoples condition. "Maneuver: Spot 2 [rohan] Men to discard a possession or
					// a machine. Discard this condition."
					put("arrowslits", "5_80");
					put("gimli", "1_13");         // Gimli, Son of Glóin: damage +1, vitality 3
					// Blood Runs Chill: Regroup event. "Spot a Dwarf who is damage +X and exert that Dwarf twice to
					// make an opponent discard X Shadow cards."
					put("chill", "8_3");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetHeightsOfIsengardScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("siege", "5_60");
					put("ram", "5_44");           // Battering Ram: [isengard] condition, machine
					put("gandalf", "1_364");      // Gandalf, The Grey Wizard: [gandalf] companion, strength 7
					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
				}},
				new HashMap<>()
				{{
					// Fangorn Glade: adds twilight for Free Peoples weapons when the fellowship moves to it
					put("site1", "11_239");
					// Heights of Isengard: "Each time a [gandalf] companion wins a skirmish, the Free Peoples
					// player may discard a condition from play."
					put("site2", "11_244");
					put("site3", "11_234");
					put("site4", "17_148");
					put("site5", "18_138");
					put("site6", "11_230");
					put("site7", "12_187");
					put("site8", "12_185");
					put("site9", "17_146");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void SiegeEngineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Siege Engine
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: <b>Machine</b>. Plays to your support area.<br><b>Shadow:</b> Play an Uruk-hai to place an [isengard] token on a machine.<br><b>Response:</b> If one or more machines are about to be discarded by an opponent, discard this condition to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("siege");

		assertEquals("Siege Engine", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysToTheSupportAreaForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var siege = scn.GetShadowCard("siege");
		scn.MoveCardsToHand(siege);

		scn.StartGame();
		scn.FreepsPass();

		// 1 for Frodo + 2 for Ettenmoors
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(siege));
		scn.ShadowPlayCard(siege);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, siege);
		assertNull(siege.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlaysAnUrukHaiFromHandToPlaceATokenOnAMachine() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var siege = scn.GetShadowCard("siege");
		var siege2 = scn.GetShadowCard("siege2");
		var ram = scn.GetShadowCard("ram");
		var wall = scn.GetShadowCard("wall");
		var machinery = scn.GetShadowCard("machinery");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(siege, siege2, ram, wall, machinery);
		scn.MoveCardsToHand(savage, guard, forger);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();

		// 10 set + 1 for Frodo + 2 for Ettenmoors
		assertEquals(13, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(siege));
		scn.ShadowUseCardAction(siege);

		// An Uruk-hai of either culture; not the [isengard] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		// It is played: 3 + 2 for roaming at site 2.
		assertInZone(Zone.SHADOW_CHARACTERS, guard);
		assertEquals(8, scn.GetTwilight());

		// Any machine, a condition or a possession, this card and its other copy among them; not an [isengard]
		// condition that is no machine.
		assertTrue(scn.ShadowHasCardChoiceAvailable(siege, siege2, ram, wall));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(machinery));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(ram);

		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(siege));
		assertEquals(0, scn.GetCultureTokensOn(siege2));
		assertEquals(0, scn.GetCultureTokensOn(wall));
		assertEquals(0, scn.GetCultureTokensOn(machinery));

		// The Savage is the one Uruk-hai left in hand: 2 + 2 for roaming. This time the token goes on the card
		// itself.
		assertTrue(scn.ShadowActionAvailable(siege));
		scn.ShadowUseCardAction(siege);
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertEquals(4, scn.GetTwilight());
		scn.ShadowChooseCard(siege);
		assertEquals(1, scn.GetCultureTokensOn(siege));
		assertEquals(1, scn.GetCultureTokensOn(ram));
		assertEquals(0, scn.GetCultureTokensOn(siege2));

		// The Orc could be played with the 4 that are left, but he is no Uruk-hai.
		assertTrue(scn.ShadowPlayAvailable(forger));
		assertFalse(scn.ShadowActionAvailable(siege));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhileTheUrukHaiInHandCannotBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var siege = scn.GetShadowCard("siege");
		var guard = scn.GetShadowCard("guard");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(siege);
		scn.MoveCardsToHand(guard, forger);

		scn.StartGame();
		scn.FreepsPass();

		// The Guard needs 3 + 2 for roaming; the Orc, 2 + 2, can be played.
		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(forger));
		assertFalse(scn.ShadowPlayAvailable(guard));
		assertFalse(scn.ShadowActionAvailable(siege));

		scn.SetTwilight(5);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(siege));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ASecondCopySavesASiegeEngineAFreePeoplesCardDiscardsAndIsItselfDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrowslits = scn.GetFreepsCard("arrowslits");
		scn.MoveCompanionsToTable("rohanguard1", "rohanguard2");
		scn.MoveCardsToSupportArea(arrowslits);

		var siege = scn.GetShadowCard("siege");
		var siege2 = scn.GetShadowCard("siege2");
		var ram = scn.GetShadowCard("ram");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(siege, siege2, ram);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(arrowslits);
		assertTrue(scn.FreepsHasCardChoiceAvailable(siege, siege2, ram));
		scn.FreepsChooseCard(siege);

		// Siege Engine is itself a machine. The other copy answers for it.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(siege2));
		scn.ShadowChooseOptionalTrigger(siege2);

		assertInZone(Zone.SUPPORT, siege);
		assertInZone(Zone.DISCARD, siege2);
		assertInZone(Zone.SUPPORT, ram);
		// Arrow-slits goes by its own second sentence, and nothing more is asked about the discard prevented.
		assertInZone(Zone.DISCARD, arrowslits);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesAMachineThatIsAPossessionToBeDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrowslits = scn.GetFreepsCard("arrowslits");
		scn.MoveCompanionsToTable("rohanguard1", "rohanguard2");
		scn.MoveCardsToSupportArea(arrowslits);

		var siege = scn.GetShadowCard("siege");
		var wall = scn.GetShadowCard("wall");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(siege, wall);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(arrowslits);
		scn.FreepsChooseCard(wall);

		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(siege));
		scn.ShadowDeclineOptionalTrigger();

		assertInZone(Zone.DISCARD, wall);
		assertInZone(Zone.SUPPORT, siege);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AMachineThatIsAPossessionIsSavedAsAConditionIsAndTheResponsePaysWithItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arrowslits = scn.GetFreepsCard("arrowslits");
		scn.MoveCompanionsToTable("rohanguard1", "rohanguard2");
		scn.MoveCardsToSupportArea(arrowslits);

		var siege = scn.GetShadowCard("siege");
		var wall = scn.GetShadowCard("wall");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(siege, wall);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(arrowslits);
		scn.FreepsChooseCard(wall);

		assertInZone(Zone.SUPPORT, wall);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(siege));
		scn.ShadowAcceptOptionalTrigger();

		// Uruk Shield Wall is a possession, and is no less a machine: it stays, and Siege Engine is what is spent.
		assertInZone(Zone.SUPPORT, wall);
		assertInZone(Zone.DISCARD, siege);
		assertInZone(Zone.DISCARD, arrowslits);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnAllysAbilityThatDiscardsAConditionIsAnsweredForAMachineAndNotForAnotherIsengardCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var albert = scn.GetFreepsCard("albert");
		scn.MoveAlliesToTable(albert);

		var siege = scn.GetShadowCard("siege");
		var ram = scn.GetShadowCard("ram");
		var machinery = scn.GetShadowCard("machinery");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(siege, ram, machinery);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// An [isengard] condition in the Shadow player's support area that is no machine: nothing is offered.
		scn.FreepsUseCardAction(albert);
		assertTrue(scn.FreepsHasCardChoiceAvailable(siege, ram, machinery));
		scn.FreepsChooseCard(machinery);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, machinery);
		assertInZone(Zone.SUPPORT, siege, ram);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The same ability again, on a machine.
		scn.ShadowPass();
		scn.FreepsUseCardAction(albert);
		scn.FreepsChooseCard(ram);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(siege));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetWoundsOn(albert));
		assertInZone(Zone.SUPPORT, ram);
		assertInZone(Zone.DISCARD, siege);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OneResponseSavesEveryMachineAFreePeoplesEventDiscardsAtOnceAndNoOtherCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(sleep);

		var siege = scn.GetShadowCard("siege");
		var devilry = scn.GetShadowCard("devilry");
		var ram = scn.GetShadowCard("ram");
		var ladder = scn.GetShadowCard("ladder");
		var machinery = scn.GetShadowCard("machinery");
		scn.MoveCardsToSupportArea(siege, devilry, ram, ladder, machinery);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, siege, devilry, ram, ladder, machinery);
		assertTrue(scn.HasKeyword(devilry, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(ram, Keyword.MACHINE));
		assertTrue(scn.HasKeyword(ladder, Keyword.MACHINE));
		assertFalse(scn.HasKeyword(machinery, Keyword.MACHINE));

		scn.FreepsPlayCard(sleep);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// Every condition is about to be discarded together, Siege Engine among them.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(siege));
		scn.ShadowAcceptOptionalTrigger();

		// The three other machines stay; Siege Engine goes to pay for it; the condition that is no machine is
		// discarded as the event says.
		assertInZone(Zone.SUPPORT, devilry, ram, ladder);
		assertInZone(Zone.DISCARD, siege);
		assertInZone(Zone.DISCARD, machinery);
		assertInZone(Zone.DISCARD, sleep);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenTheFreePeoplesPlayerDiscardsAMachineByASitesTextItMayBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHeightsOfIsengardScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var siege = scn.GetShadowCard("siege");
		var ram = scn.GetShadowCard("ram");
		var savage = scn.GetShadowCard("savage");
		var heights = scn.GetShadowSite("site2");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(siege, ram);

		scn.StartGame(scn.GetFreepsSite("site1"));

		// The Shadow player lays the next site, and the fellowship moves to it.
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(heights);
		assertEquals("Heights of Isengard", scn.GetCurrentSite().getBlueprint().getTitle());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(gandalf, savage);
		// 7 against 5: a [gandalf] companion wins, and the site lets the Free Peoples player discard a condition.
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(siege, ram));
		scn.FreepsChooseCard(ram);

		// It is the opponent who is discarding, though by no card of his own.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(siege));
		scn.ShadowAcceptOptionalTrigger();
		assertInZone(Zone.SUPPORT, ram);
		assertInZone(Zone.DISCARD, siege);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMachineItsOwnerDiscardsBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);

		var siege = scn.GetShadowCard("siege");
		var ladder = scn.GetShadowCard("ladder");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(siege, ladder);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		// Scaling Ladder places tokens on itself.
		scn.AddTokensToCard(ladder, 1);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, savage);
		scn.FreepsResolveSkirmish(gandalf);
		scn.FreepsPass();

		// The Shadow player uses the Ladder, whose own text discards it.
		assertTrue(scn.ShadowActionAvailable(ladder));
		scn.ShadowUseCardAction(ladder);

		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, ladder);
		assertInZone(Zone.SUPPORT, siege);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BloodRunsChillMakesTheShadowPlayerDiscardAndTheMachineHeChoosesCannotBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var chill = scn.GetFreepsCard("chill");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(chill);

		var siege = scn.GetShadowCard("siege");
		var ram = scn.GetShadowCard("ram");
		scn.MoveCardsToSupportArea(siege, ram);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsPlayAvailable(chill));
		scn.FreepsPlayCard(chill);

		// Gimli is damage +1 and has exerted twice: the Shadow player discards 1 Shadow card of his own choice.
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasCardChoiceAvailable(siege, ram));
		scn.ShadowChooseCard(ram);

		// A Free Peoples card, but the Shadow player is the one discarding.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertInZone(Zone.DISCARD, ram);
		assertInZone(Zone.SUPPORT, siege);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
