package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_029_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enmity", "1_29");

					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5 (7 at sites 6 to 8), vitality 3
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3
					put("lorien", "1_53");      // Lorien Elf: Elf companion, strength 4, vitality 2, no text in play
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: Elf ally, home site 3, vitality 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4

					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AncientEnmityStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Ancient Enmity
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Skirmish: Make an Elf strength +1. If a minion loses this skirmish to that Elf, that minion's owner
		 * discards 2 cards at random from hand.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enmity");

		assertEquals("Ancient Enmity", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlus1AndWhenAMinionLosesTheSkirmishToThatElfItsOwnerDiscardsTwoCardsFromHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ownSavage = scn.GetFreepsCard("savage");
		var runner = scn.GetShadowCard("runner");
		// Two Elf companions and an Elf ally, and Aragorn, a companion who is no Elf.
		scn.MoveCompanionsToTable(haldir, arwen, aragorn);
		scn.MoveAlliesToTable(silinde);
		// A second card in the Free Peoples hand, and three in the Shadow player's: his copies of the Lorien
		// Elf, Arwen and Aragorn.
		scn.MoveCardsToHand(enmity, ownSavage);
		scn.MoveCardsToHand(scn.GetShadowCard("lorien"), scn.GetShadowCard("arwen"), scn.GetShadowCard("aragorn"));

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(enmity));

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(haldir, runner);
		assertTrue(scn.FreepsPlayAvailable(enmity));
		assertEquals(5, scn.GetStrength(haldir));

		scn.FreepsPlayCard(enmity);
		// Any Elf, in the skirmish or out of it; not Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, arwen, silinde));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(haldir);
		assertEquals(6, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetStrength(silinde));
		assertInZone(Zone.DISCARD, enmity);

		scn.ShadowPass();
		assertEquals(6, scn.GetStrength(haldir));
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertEquals(1, scn.GetFreepsHandCount());

		// Haldir, 6, beats the Runner, 5; at 5 the tie would have been the Runner's.  The wound for losing is
		// placed first and kills the Runner: he lost the skirmish all the same.
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetShadowHandCount());
		// 2 cards from hand + the dead Runner
		assertEquals(2 + 1, scn.GetShadowDiscardCount());
		// The other player's hand is his own affair, and nobody was asked which cards.
		assertEquals(1, scn.GetFreepsHandCount());
		assertInZone(Zone.HAND, ownSavage);
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThatElfLosingTheSkirmishDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var lorien = scn.GetFreepsCard("lorien");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(lorien);
		scn.MoveCardsToHand(enmity);
		scn.MoveCardsToHand(scn.GetShadowCard("lorien"), scn.GetShadowCard("arwen"), scn.GetShadowCard("aragorn"));

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(lorien, runner);
		// The Lorien Elf is the only Elf, so he is chosen without asking.
		scn.FreepsPlayCard(enmity);
		assertEquals(5, scn.GetStrength(lorien));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(3, scn.GetShadowHandCount());

		// The Lorien Elf, 4 + 1, ties the Runner, 5, and so loses.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnElfWhoIsNotInTheSkirmishMayBeChosenAndThenNoSkirmishOfTheTurnDiscardsAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(haldir, arwen);
		// Haldir, 5 + 1, can beat a Runner, 5, without the event.
		scn.ApplyAdHocModifier(new StrengthModifier(null, haldir, null, 1));
		scn.MoveCardsToHand(enmity);
		scn.MoveCardsToHand(scn.GetShadowCard("lorien"), scn.GetShadowCard("arwen"), scn.GetShadowCard("aragorn"));

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, runner}, new PhysicalCardImpl[]{haldir, runner2});
		scn.FreepsResolveSkirmish(arwen);
		assertEquals(6, scn.GetStrength(haldir));

		// Arwen is the one fighting; the event is played on Haldir, who waits for his own skirmish.
		scn.FreepsPlayCard(enmity);
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, arwen));

		scn.FreepsChooseCard(haldir);
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(arwen));

		scn.ShadowPass();
		assertEquals(7, scn.GetStrength(haldir));

		// Arwen, 6, beats her Runner, 5: a minion has lost this skirmish, but not to that Elf.
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(3, scn.GetShadowHandCount());
		// The +1 was for that skirmish only.
		assertEquals(6, scn.GetStrength(haldir));

		// Haldir beats the other Runner in his own skirmish, which is not the one the event was played in.
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(6, scn.GetStrength(haldir));

		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner2);
		assertEquals(3, scn.GetShadowHandCount());
		// The two dead Runners
		assertEquals(2, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AMinionOfAnyKindThatLivesThroughItsLossCostsItsOwnerTheOneCardHeHolds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		// The minion here is no Orc and is not of the [moria] culture, and it outlives the skirmish it loses.
		var savage = scn.GetShadowCard("savage");
		var held = scn.GetShadowCard("lorien");
		scn.MoveCompanionsToTable(haldir);
		scn.MoveCardsToHand(enmity);
		scn.MoveCardsToHand(held);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(haldir, savage);
		scn.FreepsPlayCard(enmity);
		assertEquals(6, scn.GetStrength(haldir));
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, held);

		// Haldir, 6, beats the Savage, 5, which takes 1 wound of its 3.  Its owner has one card to discard
		// where the text says two: he discards what he has.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertInZone(Zone.DISCARD, held);
		assertEquals(0, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoMinionsLosingTheOneSkirmishToThatElfDiscard2CardsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(haldir);
		// Haldir, 5 + 5 and + 1 from the event, beats two Runners, 5 + 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, haldir, null, 5));
		scn.MoveCardsToHand(enmity);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.ShadowDrawCards(5);
		scn.SkipToAssignments();
		// The Shadow player sends the second Runner into the same skirmish.
		scn.FreepsAssignToMinions(haldir, runner);
		scn.ShadowAssignToMinions(haldir, runner2);
		scn.FreepsResolveSkirmish(haldir);
		// Haldir is the only Elf, so he is chosen without asking.
		scn.FreepsPlayCard(enmity);
		assertEquals(11, scn.GetStrength(haldir));

		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetShadowDiscardCount());
		scn.ShadowPass();
		scn.FreepsPass();

		// Ketura on Q-0113: "If a minion loses this skirmish to that Elf" is evaluated once, as "if at least 1
		// minion lost this skirmish to that Elf": both Runners are the Shadow player's, and he discards 2, once.
		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertEquals(3, scn.GetShadowHandCount());
		assertEquals(2 + 2, scn.GetShadowDiscardCount()); // 2 hand cards + the two dead Runners
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
