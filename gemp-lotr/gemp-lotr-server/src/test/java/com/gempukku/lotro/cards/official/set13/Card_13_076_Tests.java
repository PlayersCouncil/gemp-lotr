package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_076_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead1", "13_76");
					// New Authority: unique [gandalf] condition in the support area, "Response: If a minion uses its
					// special ability, discard this condition to cancel the effect of that special ability." Its
					// other trigger answers a burden added by a Shadow card; to play, spot a [gandalf] Wizard.
					put("authority", "11_37");

					// Madril, Ranger of Ithilien: [gondor] companion, resistance 7. His fellowship ability needs a forest site.
					put("madril", "11_62");
					// Watcher at Sarn Ford: [gondor] companion, resistance 8. A skirmish ability, never used.
					put("watcher", "18_59");
					// Faramir, Dunadan of Gondor: [gondor] companion, resistance 7, cost 3. A skirmish ability, never used.
					put("faramir", "12_47");
					// Ranger of the South: [gondor] companion, resistance 6. Strength +2 while a [gondor] token can be spotted.
					put("ranger", "18_56");
					// Arwen, Staunch Defender: [elven] companion, resistance 7. Her text is about minions skirmishing her.
					put("arwen", "11_15");
					put("sam", "1_311");            // Sam, Son of Hamfast: "Fellowship: Exert Sam to remove a burden."

					put("taskmaster", "2_90");      // Orc Taskmaster: [sauron] Orc, vitality 3, "Shadow: Exert this minion to add (1)."
					put("forger", "3_56");          // Isengard Forger: [isengard] Orc, vitality 2, "Shadow: Exert this minion to add (1)."
					// Morgul Skirmisher: [sauron] Orc, "Maneuver: Remove (3) to exert a Hobbit (except the Ring-bearer)."
					put("skirmisher", "1_257");
					put("runner", "1_178");         // Goblin Runner: when played, adds (2): a trigger, and no special ability
					// Hollowing of Isengard: [isengard] condition in the support area, "Shadow: Discard this condition to add (3)."
					put("hollowing", "3_54");
					// Mordor Enraged: [sauron] condition a minion bears, "Shadow: Exert bearer to add (1) for each archer companion."
					put("enraged", "1_254");

					// Isengard Shaman: [isengard] Orc, vitality 2, "Response: If an [isengard] Orc is about to take a wound,
					// remove (2) to prevent that wound."
					put("shaman", "3_59");
					put("legolas", "1_50");         // Legolas, Greenleaf: archer; an archery ability that is never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StoriedHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Storied Homestead
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this, add a [gondor] token here for each [gondor] companion who has resistance 7 or more.<br><b>Response:</b> If a minion's special ability is used, discard this condition or remove 2 [gondor] tokens from here to cancel its effect.
		*/

		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");

		assertEquals("Storied Homestead", homestead.getBlueprint().getTitle());
		assertNull(homestead.getBlueprint().getSubtitle());
		assertTrue(homestead.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, homestead.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, homestead.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, homestead.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(homestead, Keyword.SUPPORT_AREA));
		assertEquals(2, homestead.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForTwoToTheSupportAreaWithATokenForEachGondorCompanionOfResistance7OrMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");
		var madril = scn.GetFreepsCard("madril");
		var watcher = scn.GetFreepsCard("watcher");
		var ranger = scn.GetFreepsCard("ranger");
		var arwen = scn.GetFreepsCard("arwen");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(madril, watcher, ranger, arwen);
		scn.MoveCardsToHand(homestead, faramir);

		scn.StartGame();

		// Two [gondor] companions of resistance 7 and 8; a [gondor] companion of resistance 6; an [elven] companion
		// of resistance 7. The Ring-bearer, a [shire] companion, has resistance 10.
		assertEquals(7, scn.GetResistance(madril));
		assertEquals(8, scn.GetResistance(watcher));
		assertEquals(6, scn.GetResistance(ranger));
		assertEquals(7, scn.GetResistance(arwen));
		assertEquals(0, scn.GetCultureTokensOn(homestead));

		scn.FreepsPlayCard(homestead);

		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(2, scn.GetCultureTokensOn(homestead));

		// A [gondor] companion of resistance 7 played afterwards brings no token: they are counted when this is played.
		scn.FreepsPlayCard(faramir);
		assertInZone(Zone.FREE_CHARACTERS, faramir);
		assertEquals(7, scn.GetResistance(faramir));
		// 2 for the Homestead + 3 for Faramir.
		assertEquals(5, scn.GetTwilight());
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionWhomABurdenBringsBelowResistance7AddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");
		var madril = scn.GetFreepsCard("madril");
		var watcher = scn.GetFreepsCard("watcher");
		scn.MoveCompanionsToTable(madril, watcher);
		scn.MoveCardsToHand(homestead);

		scn.StartGame();
		// Each burden takes 1 from every companion's resistance.
		scn.AddBurdens(1);

		assertEquals(6, scn.GetResistance(madril));
		assertEquals(7, scn.GetResistance(watcher));

		scn.FreepsPlayCard(homestead);

		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RemovesTwoTokensFromHereToCancelTheEffectOfAMinionsSpecialAbilityAndIsNotOfferedAgainForThatUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");
		var taskmaster = scn.GetShadowCard("taskmaster");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 3);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(3, scn.GetCultureTokensOn(homestead));

		// Orc Taskmaster exerts to add (1). Left unanswered, it does.
		scn.ShadowUseCardAction(taskmaster);
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(twilight + 1, scn.GetTwilight());
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);

		scn.ShadowUseCardAction(taskmaster);
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsAcceptOptionalTrigger();

		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("Remove 2"));
		scn.FreepsChoose("Remove 2");

		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);
		// The Taskmaster's exertion stays paid, and nothing is added.
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertEquals(twilight + 1, scn.GetTwilight());
		// The Homestead could still discard itself, but the ability's effect is already cancelled: no second offer.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(homestead));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFewerThanTwoTokensHereItCanOnlyBeDiscardedToCancelTheEffect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");
		var forger = scn.GetShadowCard("forger");
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(forger);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();

		// Isengard Forger exerts to add (1).
		scn.ShadowUseCardAction(forger);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsAcceptOptionalTrigger();

		// One token is not two: no choice is asked, and the condition is discarded.
		assertInZone(Zone.DISCARD, homestead);
		assertEquals(1, scn.GetWoundsOn(forger));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AManeuverAbilityPaidForWithTwilightIsCancelledAndItsCostStaysPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");
		var sam = scn.GetFreepsCard("sam");
		var skirmisher = scn.GetShadowCard("skirmisher");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(skirmisher);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// Morgul Skirmisher removes (3) to exert a Hobbit who is not the Ring-bearer: Sam.
		scn.ShadowUseCardAction(skirmisher);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChoose("Remove 2");

		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AMinionsResponseAbilityIsAnsweredAndItsEffectCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");
		var legolas = scn.GetFreepsCard("legolas");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(2);
		scn.PassCurrentPhaseActions();

		// Legolas's archery wound goes to the only minion.  The Shaman, about to take it, uses his Response:
		// a minion's special ability, though it bears no phase's name.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shaman));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsChoiceAvailable("Remove 2"));
		scn.FreepsChoose("Discard this condition");

		assertInZone(Zone.DISCARD, homestead);
		// The (2) stays removed and the wound is not prevented; with nothing left to pay for the Response
		// again, the Shaman takes it.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnlyTheSpecialAbilityOfAMinionIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");
		var sam = scn.GetFreepsCard("sam");
		var taskmaster = scn.GetShadowCard("taskmaster");
		var runner = scn.GetShadowCard("runner");
		var hollowing = scn.GetShadowCard("hollowing");
		var enraged = scn.GetShadowCard("enraged");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(homestead, hollowing);
		scn.AddTokensToCard(homestead, 2);
		scn.MoveCardsToHand(runner);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.AttachCardsTo(taskmaster, enraged);
		scn.FreepsRefreshDecision();
		scn.AddBurdens(1);
		scn.SetTwilight(10);

		// A companion's special ability: Sam exerts to remove a burden.
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPass();
		int twilight = scn.GetTwilight();

		// The special ability of a Shadow condition in the support area: Hollowing of Isengard is discarded to add (3).
		scn.ShadowUseCardAction(hollowing);
		assertInZone(Zone.DISCARD, hollowing);
		assertEquals(twilight + 3, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(homestead));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The special ability of a condition that a minion bears: Mordor Enraged exerts its bearer (there is no
		// archer companion, and nothing is added).
		scn.ShadowUseCardAction(enraged);
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(homestead));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// A minion's text that is no special ability: Goblin Runner adds (2) when it is played.
		// 1 to play + 2 roaming, and the 2 it adds.
		scn.ShadowPlayCard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(twilight + 3 - 3 + 2, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(homestead));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// On the same board the minion's own special ability is answered.
		scn.ShadowUseCardAction(taskmaster);
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChoose("Remove 2");
		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertEquals(twilight + 3 - 3 + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void IsNotOfferedOnceAnotherCardHasCancelledTheEffectAndIsOfferedForTheNextUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead1");
		var authority = scn.GetFreepsCard("authority");
		var taskmaster = scn.GetShadowCard("taskmaster");
		// New Authority answers the same use of a minion's special ability by a Response of its own.
		scn.MoveCardsToSupportArea(homestead, authority);
		scn.AddTokensToCard(homestead, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(taskmaster);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();

		scn.ShadowUseCardAction(taskmaster);
		assertEquals(1, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(authority));

		// The other card cancels the effect, by discarding itself.
		scn.FreepsChooseOptionalTrigger(authority);

		assertInZone(Zone.DISCARD, authority);
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(twilight, scn.GetTwilight());
		// The Homestead could pay either cost, and there is nothing left for it to cancel.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(homestead));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The next use of the ability is a new one, and the Homestead answers it.
		scn.ShadowUseCardAction(taskmaster);
		assertEquals(2, scn.GetWoundsOn(taskmaster));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(homestead));
		scn.FreepsAcceptOptionalTrigger();

		// It has its two tokens and is discarded all the same: either may be chosen.
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("Remove 2"));
		scn.FreepsChoose("Discard this condition");

		assertInZone(Zone.DISCARD, homestead);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
