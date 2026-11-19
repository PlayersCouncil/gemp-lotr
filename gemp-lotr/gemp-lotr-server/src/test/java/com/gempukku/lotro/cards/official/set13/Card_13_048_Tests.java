package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_048_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boat", "13_48");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, strength 3, vitality 4
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, strength 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, no Sméagol
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, no Sméagol
					// Gladden Homestead and Nice Fish: Free Peoples [gollum] support-area conditions that get [gollum]
					// tokens by their own text; abilities used only on purpose
					put("homestead", "13_49");
					put("fish", "15_46");
					// A Path Appointed: a [gondor] condition that holds [gondor] tokens, abilities used only on purpose
					put("path", "8_42");
					put("pipe", "1_292");       // The Gaffer's Pipe: a Free Peoples possession for a Hobbit, ability on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FishingBoatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Fishing Boat
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype:
		 * Vitality: +1
		 * Game Text: Bearer must be Sméagol.<br>When you play Fishing Boat, you may reinforce a [gollum] token.<br>Each time Sméagol is about to be overwhelmed in a skirmish, you may remove 2 [gollum] tokens to make him strength +2 until the end of that skirmish.
		*/

		var scn = GetScenario();

		var boat = scn.GetFreepsCard("boat");

		assertEquals("Fishing Boat", boat.getBlueprint().getTitle());
		assertNull(boat.getBlueprint().getSubtitle());
		assertTrue(boat.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, boat.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, boat.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, boat.getBlueprint().getCardType());
		assertEquals(2, boat.getBlueprint().getTwilightCost());
		assertEquals(1, boat.getBlueprint().getVitality());
		assertNull(boat.getBlueprint().getPossessionClasses());
	}

	@Test
	public void CannotBePlayedWithoutSmeagolAndGoesOnHimToReinforceAGollumTokenOnACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boat = scn.GetFreepsCard("boat");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var homestead = scn.GetFreepsCard("homestead");
		var fish = scn.GetFreepsCard("fish");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(homestead, fish, path);
		scn.MoveCardsToHand(boat);

		scn.StartGame();
		scn.AddTokensToCard(homestead, 2);
		scn.AddTokensToCard(fish, 1);
		scn.AddTokensToCard(path, 1);
		scn.FreepsRefreshDecision();

		// Frodo and Aragorn, and no Sméagol: nobody may bear it.
		assertInHand(boat);
		assertFalse(scn.FreepsPlayAvailable(boat));

		scn.MoveCompanionsToTable(smeagol);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(boat));
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertEquals(1, scn.GetCultureTokensOn(path));

		// Sméagol is the one bearer it may have: it goes on him with nothing asked.
		scn.FreepsPlayCard(boat);
		assertAttachedTo(boat, smeagol);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));
		scn.FreepsChooseOptionalTrigger(boat);
		// The two cards with [gollum] tokens are offered; the [gondor] tokens, and Sméagol (a [gollum] card
		// with no token), are not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(homestead, fish));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(path, smeagol));
		scn.FreepsChooseCard(fish);

		assertEquals(2, scn.GetCultureTokensOn(fish));
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShireSmeagolMayBearItAndTheReinforcementMayBeDeclinedAndAnotherPossessionPlayedAfterIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boat = scn.GetFreepsCard("boat");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		var homestead = scn.GetFreepsCard("homestead");
		var pipe = scn.GetFreepsCard("pipe");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.MoveCardsToSupportArea(homestead);
		scn.MoveCardsToHand(boat, pipe);

		scn.StartGame();
		scn.AddTokensToCard(homestead, 1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetCultureTokensOn(homestead));

		scn.FreepsPlayCard(boat);
		assertAttachedTo(boat, shiresmeagol);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another possession played after the Boat, with a [gollum] token still there to reinforce.
		scn.FreepsPlayCard(pipe);
		assertAttachedTo(pipe, frodo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(boat));
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoGollumTokenToReinforceTheReinforcementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boat = scn.GetFreepsCard("boat");
		var smeagol = scn.GetFreepsCard("smeagol");
		var fish = scn.GetFreepsCard("fish");
		var path = scn.GetFreepsCard("path");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(fish, path);
		scn.MoveCardsToHand(boat);

		scn.StartGame();
		// A [gollum] card with no token, and a card with a token of another culture.
		scn.AddTokensToCard(path, 1);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetCultureTokensOn(fish));
		assertEquals(1, scn.GetCultureTokensOn(path));

		scn.FreepsPlayCard(boat);
		assertAttachedTo(boat, smeagol);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(boat));
		assertEquals(0, scn.GetCultureTokensOn(fish));
		assertEquals(1, scn.GetCultureTokensOn(path));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeSmeagolIsAboutToBeOverwhelmedTwoGollumTokensMakeHimStrengthPlus2UntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boat = scn.GetFreepsCard("boat");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var homestead = scn.GetFreepsCard("homestead");
		var fish = scn.GetFreepsCard("fish");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol, aragorn);
		scn.AttachCardsTo(smeagol, boat);
		scn.MoveCardsToSupportArea(homestead, fish);

		scn.StartGame();
		scn.AddTokensToCard(homestead, 3);
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		scn.AddTokensToCard(fish, 1);
		assertEquals(1, scn.GetCultureTokensOn(fish));
		assertAttachedTo(boat, smeagol);
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{smeagol, troop}, new PhysicalCardImpl[]{aragorn, runner});
		scn.FreepsResolveSkirmish(smeagol);

		// 3 against 13: more than double.
		assertEquals(3, scn.GetStrength(smeagol));
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));
		scn.FreepsChooseOptionalTrigger(boat);
		// The tokens are asked for a card at a time: Nice Fish's one, and then the Homestead's goes unasked.
		assertTrue(scn.FreepsHasCardChoiceAvailable(homestead, fish));
		scn.FreepsChooseCard(fish);
		assertEquals(0, scn.GetCultureTokensOn(fish));
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertEquals(5, scn.GetStrength(smeagol));

		// 5 against 13 is still more than double: about to be overwhelmed again, and offered again, the +2 in force.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));
		assertEquals(5, scn.GetStrength(smeagol));
		scn.FreepsChooseOptionalTrigger(boat);
		assertEquals(0, scn.GetCultureTokensOn(homestead));

		// 7 against 13: no longer double. He loses, takes one wound and lives.
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The next skirmish is a phase of its own: the bonus is gone.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DeclinedTheShireSmeagolIsOverwhelmedAndTheTokensStay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boat = scn.GetFreepsCard("boat");
		// The [shire] Sméagol, 3 against 13.
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var homestead = scn.GetFreepsCard("homestead");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, boat);
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		scn.AddTokensToCard(homestead, 2);
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertAttachedTo(boat, smeagol);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, troop);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boat));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenSmeagolLosesWithoutBeingOverwhelmedNorWhenAnotherCompanionIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boat = scn.GetFreepsCard("boat");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var homestead = scn.GetFreepsCard("homestead");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.AttachCardsTo(smeagol, boat);
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		scn.AddTokensToCard(homestead, 4);
		assertEquals(4, scn.GetCultureTokensOn(homestead));
		assertAttachedTo(boat, smeagol);
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, troop}, new PhysicalCardImpl[]{smeagol, runner});

		// Sam, 3 against 13, is overwhelmed: he is no Sméagol.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(boat));
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Sméagol, 3 against 5, loses without being overwhelmed.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(boat));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(4, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithOnlyOneGollumTokenAndSmeagolIsOverwhelmed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boat = scn.GetFreepsCard("boat");
		var smeagol = scn.GetFreepsCard("smeagol");
		var homestead = scn.GetFreepsCard("homestead");
		var path = scn.GetFreepsCard("path");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(smeagol);
		scn.AttachCardsTo(smeagol, boat);
		scn.MoveCardsToSupportArea(homestead, path);

		scn.StartGame();
		// One [gollum] token, and two [gondor] tokens that do not pay.
		scn.AddTokensToCard(homestead, 1);
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		scn.AddTokensToCard(path, 2);
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertAttachedTo(boat, smeagol);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, troop);
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(boat));
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(path));
		assertInZone(Zone.DEAD, smeagol);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
