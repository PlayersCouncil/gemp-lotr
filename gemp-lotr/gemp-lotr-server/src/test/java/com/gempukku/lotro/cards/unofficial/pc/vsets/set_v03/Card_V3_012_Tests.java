package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_012_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("pass", "103_12");
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: strength 8, vitality 8; fierce; enduring, so +2 strength for each wound
					put("runner", "1_178");     // Goblin Runner: [moria] minion, strength 5
					put("runner2", "1_178");    // a second Goblin Runner, to be stacked on another card
					put("savage", "1_151");     // Uruk Savage: [isengard] minion, strength 5, vitality 3
					put("swordsman", "3_98");   // Orc Swordsman: [sauron] minion, vitality 3; stronger only with a weapon
					put("armory", "1_173");     // Goblin Armory: a Shadow condition, to be stacked where a minion would be
					put("ships", "8_65");       // Ships of Great Draught: another Shadow card in the support area, to stack a minion on
					put("destroyed", "1_220");  // Not Easily Destroyed: Shadow condition. Maneuver or Skirmish: Remove (3) to heal a Nazgul

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("glorfindel", "9_16");  // Glorfindel, Revealed in Wrath: [elven], strength 9, vitality 3; his skirmish ability is never used
					put("gandalf", "1_364");    // Gandalf, The Grey Wizard: exerts for Unknown Perils
					put("sam", "1_311");        // Sam, Son of Hamfast: Fellowship: Exert Sam to remove a burden
					put("goldberry", "9_51");   // Goldberry, River-daughter: ally whose home is site 2, strength 4, vitality 6; her text needs a river
					put("foes", "1_105");       // Foes of Mordor: [gondor] condition, support area. Maneuver: Exert a [gondor] character to wound a [sauron] minion
					put("tale", "1_317");       // There and Back Again: [shire] condition on a Hobbit companion. Maneuver: Discard this condition to make each Hobbit companion strength +2 until the regroup phase
					put("mallorn", "1_54");     // Mallorn-trees: [elven] condition, support area. Fellowship: Reveal an [elven] card from hand and place it beneath your draw deck
					put("perils", "3_36");      // Unknown Perils: [gandalf] condition, support area. Response: If a companion is about to take a wound, spot 4 twilight tokens and exert Gandalf to prevent that wound
					put("gift", "3_19");        // Gift of the Evenstar: [elven] condition on Aragorn. Skirmish: Exert Aragorn (or discard 2 cards from hand) to make a minion skirmishing Aragorn strength -1
					put("shards", "3_44");      // The Shards of Narsil: Free Peoples artifact, support area. Fellowship: Stack a [gondor] card from hand here
					put("keptsafe", "6_111");   // Kept Safe: [shire] condition, support area. When you play this condition, place 2 [shire] tokens here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PassoftheSpiderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Pass of the Spider
		 * Unique: true
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: Each time a special ability on a Free Peoples condition is used, you may discard a minion stacked here to hinder that condition and heal Shelob.
		 * 	Skirmish: If Shelob is skirmishing, stack another minion from play here to make a character skirmishing her strength -2 (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetShadowCard("pass");

		assertEquals("Pass of the Spider", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsAMinionStackedHereToHinderTheConditionWhoseAbilityWasUsedAndHealShelob() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var ships = scn.GetShadowCard("ships");
		var armory = scn.GetShadowCard("armory");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var savage = scn.GetShadowCard("savage");
		var shelob = scn.GetShadowCard("shelob");
		var swordsman = scn.GetShadowCard("swordsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var foes = scn.GetFreepsCard("foes");
		var tale = scn.GetFreepsCard("tale");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(foes);
		scn.AttachCardsTo(frodo, tale);
		scn.MoveCardsToSupportArea(pass, ships);
		// Stacked here: minions of two cultures, and a condition. A third minion is stacked elsewhere.
		scn.StackCardsOn(pass, runner, savage, armory);
		scn.StackCardsOn(ships, runner2);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, swordsman);
		scn.AddWoundsToChar(shelob, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(swordsman));
		assertEquals(2, scn.GetWoundsOn(shelob));
		assertEquals(3, scn.GetStackedCards(pass).size());
		assertFalse(scn.IsHindered(foes));

		// Foes of Mordor: Aragorn exerts to wound the one [sauron] minion.
		scn.FreepsUseCardAction(foes);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));

		scn.ShadowAcceptOptionalTrigger();

		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, savage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armory, runner2, shelob, swordsman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(runner);

		assertInZone(Zone.DISCARD, runner);
		assertEquals(2, scn.GetStackedCards(pass).size());
		assertEquals(1, scn.GetStackedCards(ships).size());
		assertTrue(scn.IsHindered(foes));
		assertFalse(scn.IsHindered(tale));
		assertEquals(1, scn.GetWoundsOn(shelob));
		// The ability did what it does, and the minion it wounded is not the one healed.
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// A second ability, of a condition that a companion bears and that discards itself to pay for it:
		// there is nothing left to hinder, and Shelob is healed all the same.
		scn.ShadowPass();
		int frodoStrength = scn.GetStrength(frodo);
		scn.FreepsUseCardAction(tale);

		assertInZone(Zone.DISCARD, tale);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, savage);
		assertEquals(1, scn.GetStackedCards(pass).size());
		assertInZone(Zone.STACKED, armory);
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertEquals(frodoStrength + 2, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DecliningChangesNothingAndAShadowConditionsAbilityDoesNotSetItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var destroyed = scn.GetShadowCard("destroyed");
		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		var swordsman = scn.GetShadowCard("swordsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var foes = scn.GetFreepsCard("foes");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(foes);
		scn.MoveCardsToSupportArea(pass, destroyed);
		scn.StackCardsOn(pass, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, swordsman);
		scn.AddWoundsToChar(shelob, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(foes);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));

		scn.ShadowDeclineOptionalTrigger();

		assertEquals(1, scn.GetStackedCards(pass).size());
		assertInZone(Zone.STACKED, runner);
		assertFalse(scn.IsHindered(foes));
		assertEquals(2, scn.GetWoundsOn(shelob));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());

		// The Shadow player's own condition: 2 companions + 2 for the site are in the pool, and 3 are removed.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(destroyed));
		scn.ShadowUseCardAction(destroyed);

		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The same Free Peoples condition used again: asked again.
		scn.FreepsUseCardAction(foes);

		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));
	}

	@Test
	public void NotOfferedWhenNoMinionIsStackedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var ships = scn.GetShadowCard("ships");
		var armory = scn.GetShadowCard("armory");
		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		var swordsman = scn.GetShadowCard("swordsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var foes = scn.GetFreepsCard("foes");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(foes);
		scn.MoveCardsToSupportArea(pass, ships);
		// A card that is no minion is stacked here, and a minion is stacked on another card.
		scn.StackCardsOn(pass, armory);
		scn.StackCardsOn(ships, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, swordsman);
		scn.AddWoundsToChar(shelob, 2);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(foes);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(swordsman));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(foes));
		assertEquals(2, scn.GetWoundsOn(shelob));
		assertEquals(1, scn.GetStackedCards(pass).size());
		assertEquals(1, scn.GetStackedCards(ships).size());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OnlyASpecialAbilityOfAFreePeoplesConditionSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var sam = scn.GetFreepsCard("sam");
		var shards = scn.GetFreepsCard("shards");
		var mallorn = scn.GetFreepsCard("mallorn");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		var foes = scn.GetFreepsCard("foes");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(shards, mallorn);
		// In hand: a [gondor] card to stack on the Shards, Kept Safe to play, and an [elven] card for Mallorn-trees.
		scn.MoveCardsToHand(foes, keptsafe, glorfindel);
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(pass, runner, savage);

		scn.StartGame();
		scn.AddBurdens(1);

		// A companion's special ability.
		assertEquals(1, scn.GetBurdens());
		scn.FreepsUseCardAction(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The special ability of an artifact in the support area.
		scn.FreepsUseCardAction(shards, "Stack");

		assertInZone(Zone.STACKED, foes);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Free Peoples condition doing what it does when played, which is no special ability.
		scn.FreepsPlayCard(keptsafe);

		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Free Peoples condition's special ability. No Shelob is in play to be healed.
		scn.FreepsUseCardAction(mallorn);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));

		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(savage);
		// Mallorn-trees still does what it was used for: the [elven] card is shown and goes under the deck.
		scn.ShadowDismissRevealedCards();

		assertInZone(Zone.DISCARD, savage);
		assertEquals(1, scn.GetStackedCards(pass).size());
		assertTrue(scn.IsHindered(mallorn));
		assertFalse(scn.IsHindered(keptsafe));
		assertFalse(scn.IsHindered(shards));
		assertEquals(glorfindel, scn.GetFreepsBottomOfDeck());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ASkirmishAbilityOfAConditionThatACompanionBearsSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gift = scn.GetFreepsCard("gift");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, gift);
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(pass, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AddWoundsToChar(shelob, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, shelob);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsActionAvailable(gift));

		// Gift of the Evenstar: Aragorn exerts, having no cards in hand to discard.
		scn.FreepsUseCardAction(gift);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetStackedCards(pass).size());
		assertTrue(scn.IsHindered(gift));
		assertAttachedTo(gift, aragorn);
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAbilityThatNamesItsBearerIsStillCarriedOutWhenItsConditionIsHinderedInAnswer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gift = scn.GetFreepsCard("gift");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, gift);
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(pass, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, shelob);

		assertEquals(8, scn.GetStrength(shelob));

		// Gift of the Evenstar: Aragorn exerts to make a minion skirmishing him strength -1.
		scn.FreepsUseCardAction(gift);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));

		scn.ShadowAcceptOptionalTrigger();

		// A card hindered while its ability is being worked through still finishes it.
		assertTrue(scn.IsHindered(gift));
		assertInZone(Zone.DISCARD, runner);
		assertEquals(7, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AResponseOnAFreePeoplesConditionSetsItOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var perils = scn.GetFreepsCard("perils");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToSupportArea(perils);
		scn.MoveCardsToSupportArea(pass);
		scn.StackCardsOn(pass, runner);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.AddWoundsToChar(shelob, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, shelob);

		// 3 companions + 2 for the site: Unknown Perils has its 4 twilight tokens to spot.
		assertEquals(5, scn.GetTwilight());
		// Enduring Shelob with one wound is strength 10, and Aragorn, strength 8, loses.
		assertEquals(10, scn.GetStrength(shelob));

		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(perils));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(pass));

		scn.ShadowAcceptOptionalTrigger();

		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.IsHindered(perils));
		assertEquals(0, scn.GetWoundsOn(shelob));
		// The wound was prevented.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Shelob is fierce, so the fierce assignment phase is next.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void SkirmishAbilityStacksAnotherMinionHereToWeakenWhoeverShelobFightsForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var swordsman = scn.GetShadowCard("swordsman");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(glorfindel, aragorn);
		scn.MoveCardsToSupportArea(pass);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner, savage, swordsman);
		scn.SkipToAssignments();
		// Aragorn is assigned to the Swordsman, and their skirmish waits while Shelob's is fought.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{glorfindel, shelob}, new PhysicalCardImpl[]{aragorn, swordsman});
		// The Runner joins Shelob's skirmish; the Savage is left out of the fighting.
		scn.ShadowAssignToMinions(glorfindel, runner);
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(glorfindel));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, swordsman));
		assertEquals(0, scn.GetStackedCards(pass).size());
		assertTrue(scn.ShadowActionAvailable(pass));

		scn.ShadowUseCardAction(pass);

		assertTrue(scn.ShadowHasCardChoiceAvailable(runner, savage, swordsman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shelob));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(runner);

		assertInZone(Zone.STACKED, runner);
		assertEquals(1, scn.GetStackedCards(pass).size());
		assertTrue(scn.GetStackedCards(pass).contains(runner));
		assertInZone(Zone.SHADOW_CHARACTERS, shelob, savage, swordsman);
		assertEquals(7, scn.GetStrength(glorfindel));
		// Aragorn is assigned to a skirmish, but it is not Shelob's.
		assertTrue(scn.IsCharAssignedAgainst(aragorn, swordsman));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(8, scn.GetStrength(shelob));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Shelob, strength 8, now beats Glorfindel, who would have won at his own strength of 9.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(glorfindel));
		assertEquals(0, scn.GetWoundsOn(shelob));
		// Each skirmish is a skirmish phase of its own: Shelob's is over, and so is the penalty, while
		// Aragorn's is still to be fought.
		assertEquals(9, scn.GetStrength(glorfindel));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void SkirmishAbilityIsUsableOnceInEachSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(pass);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(glorfindel, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(pass));
		scn.ShadowUseCardAction(pass);
		scn.ShadowChooseCard(runner);

		assertEquals(7, scn.GetStrength(glorfindel));
		assertEquals(1, scn.GetStackedCards(pass).size());

		// Shelob is still skirmishing and another minion is still in play, but the one use is spent.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.IsCharSkirmishing(shelob));
		assertFalse(scn.ShadowActionAvailable(pass));

		// Shelob wins, 8 to 7, and fights Glorfindel again in her fierce skirmish: a new phase.
		scn.ShadowPass();
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(glorfindel, shelob);
		scn.FreepsPass();

		assertEquals(9, scn.GetStrength(glorfindel));
		assertTrue(scn.ShadowActionAvailable(pass));

		scn.ShadowUseCardAction(pass);

		assertEquals(2, scn.GetStackedCards(pass).size());
		assertInZone(Zone.STACKED, savage);
		assertEquals(7, scn.GetStrength(glorfindel));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedUntilShelobsOwnSkirmishIsBeingFought() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(glorfindel, aragorn);
		scn.MoveCardsToSupportArea(pass);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner, savage);

		// Not a maneuver ability.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(pass));

		scn.ShadowPass();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{glorfindel, shelob});
		scn.ShadowDeclineAssignments();

		// The Savage's skirmish: Shelob is assigned to one that has not begun.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharAssigned(shelob));
		assertFalse(scn.IsCharSkirmishing(shelob));
		assertFalse(scn.ShadowActionAvailable(pass));

		scn.ShadowPass();
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishing(shelob));
		assertTrue(scn.ShadowActionAvailable(pass));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhenShelobIsTheOnlyMinionInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		scn.MoveCompanionsToTable(glorfindel);
		scn.MoveCardsToSupportArea(pass);
		// One minion is already stacked here and another is in hand: neither is in play.
		scn.StackCardsOn(pass, savage);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(glorfindel, shelob);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishing(shelob));
		assertInZone(Zone.HAND, runner);
		assertFalse(scn.ShadowActionAvailable(pass));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingShelobIsWeakenedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var pass = scn.GetShadowCard("pass");
		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var goldberry = scn.GetFreepsCard("goldberry");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(goldberry);
		scn.MoveCardsToSupportArea(pass);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner);
		// The fellowship moves to site 2, Goldberry's home, where she may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(goldberry, shelob);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(goldberry);
		scn.FreepsPass();

		int frodoStrength = scn.GetStrength(frodo);
		assertEquals(4, scn.GetStrength(goldberry));
		assertTrue(scn.IsCharSkirmishingAgainst(goldberry, shelob));
		assertTrue(scn.ShadowActionAvailable(pass));

		scn.ShadowUseCardAction(pass);

		assertInZone(Zone.STACKED, runner);
		assertEquals(2, scn.GetStrength(goldberry));
		assertEquals(frodoStrength, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
