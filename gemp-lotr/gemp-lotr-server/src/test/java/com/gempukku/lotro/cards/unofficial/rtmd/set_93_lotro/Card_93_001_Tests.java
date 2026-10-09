package com.gempukku.lotro.cards.unofficial.rtmd.set_93_lotro;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_93_001_Tests
{
	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: [moria] Orc minion, strength 5, not unique
					put("runner", "1_178");      // Goblin Runner: [moria] Orc minion, strength 5, not unique
					put("lemenya", "1_232");     // Ulaire Lemenya, Lieutenant of Morgul: [wraith] Nazgul minion, strength 9
					put("scimitar", "1_180");    // Goblin Scimitar: [moria] possession (hand weapon), cost 0, strength +2;
					                             //   bearer must be a [moria] Orc; when played, its owner may draw a card
					put("blade", "12_168");      // Nazgul Blade: [wraith] possession (hand weapon), cost 1, strength +1; bearer must be a Nazgul
					put("dagger", "18_69");      // Henchman's Dagger: [men] possession, cost 0; bearer must be a [men] minion
					put("ring", "17_143");       // Ring of Terror: [wraith] artifact, cost 0; bearer must be a Nazgul
					put("intwilight", "19_34");  // In Twilight: [wraith] condition, cost 2; bearer must be a [wraith] minion
					put("hides", "4_19");        // Hides: [dunland] possession, cost 1, which goes to the support area
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "93_1"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("backstabber", "1_174"); // Goblin Backstabber: [moria] Orc minion, strength 5, not unique
					put("scimitar", "1_180");    // Goblin Scimitar: [moria] possession (hand weapon), cost 0, strength +2;
					                             //   bearer must be a [moria] Orc; when played, its owner may draw a card
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] companion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"93_1", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 93
		 * Name: Race Text 93_1
		 * Type: MetaSite
		 * Intensity: -3
		 * Game Text: Skirmish: Play a possession on your minion.
		 */
		var scn = GetShadowScenario();
		var card = scn.GetShadowCard("mod");
		assertEquals("Race Text 93_1", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(-3, card.getBlueprint().getIntensity());
	}

	@Test
	public void PlayPossessionOnMinionDuringSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var scimitar = scn.GetShadowCard("scimitar");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber);
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, backstabber);
		scn.FreepsPass();

		// Skirmish phase — action should be available
		assertTrue(scn.ShadowActionAvailable(mod));
		assertInZone(Zone.HAND, scimitar);
		assertEquals(5, scn.GetStrength(backstabber));
		scn.ShadowUseCardAction(mod);

		// Play the scimitar on the backstabber
		assertAttachedTo(scimitar, backstabber);
		assertEquals(7, scn.GetStrength(backstabber));

		// The Scimitar's own "when you play this possession, you may draw a card".
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OwnerGatingFreepsCantUse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var scimitar = scn.GetShadowCard("scimitar");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber);
		scn.MoveCardsToHand(scimitar);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, backstabber);
		// The owner is the Free Peoples player of this turn and has no minion of his own.
		assertFalse(scn.FreepsActionAvailable(mod));
		scn.FreepsPass();

		// Freeps owns mod, so Shadow can't use it, with a Scimitar in hand and an Orc to bear it
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PossessionsOfTwoCulturesArePlayedOnMinionsInAndOutOfTheSkirmishAndNothingElseInHandIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();

		var mod = scn.GetShadowCard("mod");
		var backstabber = scn.GetShadowCard("backstabber");
		var runner = scn.GetShadowCard("runner");
		var lemenya = scn.GetShadowCard("lemenya");
		var scimitar = scn.GetShadowCard("scimitar");
		var blade = scn.GetShadowCard("blade");
		var dagger = scn.GetShadowCard("dagger");
		var ring = scn.GetShadowCard("ring");
		var intwilight = scn.GetShadowCard("intwilight");
		var hides = scn.GetShadowCard("hides");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber, runner, lemenya);
		scn.MoveCardsToHand(scimitar, blade, dagger, ring, intwilight, hides);

		scn.StartGame();
		scn.SkipToAssignments();
		// Only the Backstabber fights; the Runner and Lemenya are in no skirmish.
		scn.FreepsAssignAndResolve(aragorn, backstabber);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// The two possessions a minion on the table may bear are offered. Not offered: the Dagger
		// (a possession, but no [men] minion is here to bear it), the Ring (an artifact Lemenya could
		// bear), In Twilight (a condition Lemenya could bear) and Hides (a possession, which goes to
		// the support area and onto no minion).
		assertTrue(scn.ShadowHasCardChoiceAvailable(scimitar, blade));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(dagger, ring, intwilight, hides));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(blade);

		// Lemenya is the only Nazgul, so the Blade goes to him unasked, for its cost of 1.
		assertAttachedTo(blade, lemenya);
		assertEquals(pool - 1, scn.GetTwilight());
		assertEquals(10, scn.GetStrength(lemenya)); // 9 + 1

		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// The Scimitar is the one possession left that can be played: either Orc may bear it, the
		// one in the skirmish and the one outside it, and the Nazgul may not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(runner);
		assertAttachedTo(scimitar, runner);
		scn.ShadowDeclineOptionalTrigger(); // the Scimitar's own "you may draw a card"

		// What is left in hand is no possession that a minion here can bear: not offered.
		scn.FreepsPass();
		assertInZone(Zone.HAND, dagger);
		assertInZone(Zone.HAND, hides);
		assertFalse(scn.ShadowActionAvailable(mod));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOwnerUsesItAsTheShadowPlayerOfPlayerTwosTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var mod = scn.GetFreepsCard("mod");
		var backstabber = scn.GetFreepsCard("backstabber");
		var scimitar = scn.GetFreepsCard("scimitar");
		var aragorn = scn.GetShadowCard("aragorn");

		scn.StartGame();

		// Player 2's turn: player 1, who owns the modifier, is now the Shadow player.
		scn.SkipToOtherPlayersTurn();
		scn.MoveCompanionsToTable(aragorn);
		scn.ShadowPass(); // player 2's fellowship moves
		scn.MoveMinionsToTable(backstabber);
		scn.MoveCardsToHand(scimitar);
		scn.SkipToPhaseInverted(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.ShadowAssignToMinions(aragorn, backstabber);
		scn.ShadowResolveSkirmish(aragorn);
		scn.ShadowPass();

		assertTrue(scn.FreepsActionAvailable(mod));
		assertEquals(5, scn.GetStrength(backstabber));
		scn.FreepsUseCardAction(mod);
		assertAttachedTo(scimitar, backstabber);
		assertEquals(7, scn.GetStrength(backstabber));

		scn.FreepsDeclineOptionalTrigger(); // the Scimitar's own "you may draw a card"
		// Player 2, the Free Peoples player of this turn, is asked next.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
