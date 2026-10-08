package com.gempukku.lotro.cards.unofficial.pc.errata.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalCultureModifier;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_029_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enmity", "51_29");
					put("enmity2", "51_29");

					put("haldir", "1_48");        // Haldir, Elf of the Golden Wood: Elf companion, strength 5 (7 at sites 6 to 8), vitality 3
					put("arwen", "1_30");         // Arwen, Daughter of Elrond: Elf companion, strength 6, vitality 3
					put("lorien", "1_53");        // Lorien Elf: Elf companion, strength 4, vitality 2, no text in play
					put("silinde", "1_60");       // Silinde, Elf of Mirkwood: Elf ally, home site 3, vitality 2
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man, vitality 3

					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; also a Shadow card to hold in hand
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
		 * Game Text: Make an Elf strength +1.
		 * If a minion loses this skirmish to that Elf, you may heal an Elf; any Shadow player may discard a
		 * card from hand to prevent this.
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
	public void MakesAnElfStrengthPlus1AndWhenAMinionLosesTheSkirmishToThatElfAnElfIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var silinde = scn.GetFreepsCard("silinde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(haldir, arwen, aragorn, boromir);
		scn.MoveAlliesToTable(silinde);
		// An Elf of the [gondor] culture, and a Man of the [elven] culture who is still no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, aragorn, null, Race.ELF));
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, boromir, null, Culture.ELVEN));
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(arwen, 1);
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.MoveCardsToHand(enmity);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(enmity));

		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(haldir, runner);
		assertTrue(scn.FreepsPlayAvailable(enmity));
		assertEquals(5, scn.GetStrength(haldir));
		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(enmity);
		// Any Elf, in the skirmish or out of it.
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, arwen, silinde, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(haldir);
		assertEquals(6, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(twilight, scn.GetTwilight());
		assertInZone(Zone.DISCARD, enmity);

		scn.ShadowPass();
		assertEquals(6, scn.GetStrength(haldir));

		// Haldir, 6, beats the Runner, 5; at 5 the tie would have been the Runner's.
		scn.FreepsPass();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.FreepsDecisionAvailable("Choose Elf to heal"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, arwen, silinde, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir));
		assertEquals(1, scn.FreepsGetChoiceMax());

		// The Shadow player has no card in hand, so he is not asked.
		scn.FreepsChooseCard(silinde);
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscardACardFromHandToPreventTheHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var ownSavage = scn.GetFreepsCard("savage");
		var runner = scn.GetShadowCard("runner");
		var shadowSavage = scn.GetShadowCard("savage");
		// The Shadow player's own copy of a Free Peoples card: "a card" is any card of his hand.
		var shadowLorien = scn.GetShadowCard("lorien");
		scn.MoveCompanionsToTable(haldir, arwen);
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(arwen, 1);
		scn.MoveCardsToHand(enmity, ownSavage);
		scn.MoveCardsToHand(shadowSavage, shadowLorien);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(haldir, runner);
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(haldir);
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveActionOrder("Ancient Enmity");
		scn.FreepsChooseCard(arwen);
		assertTrue(scn.ShadowDecisionAvailable("Discard 1 card from hand to prevent"));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetShadowHandCount());

		scn.ShadowChooseYes();
		assertTrue(scn.ShadowHasCardChoiceAvailable(shadowSavage, shadowLorien));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ownSavage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCard(shadowLorien);
		assertEquals(1, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, shadowLorien);
		assertInZone(Zone.HAND, shadowSavage);
		assertInZone(Zone.HAND, ownSavage);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));
		// The event is spent and the skirmish was won all the same.
		assertInZone(Zone.DISCARD, enmity);
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDeclineToPreventAndTheElfIsHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var shadowSavage = scn.GetShadowCard("savage");
		var shadowLorien = scn.GetShadowCard("lorien");
		scn.MoveCompanionsToTable(haldir, arwen);
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(arwen, 1);
		scn.MoveCardsToHand(enmity);
		scn.MoveCardsToHand(shadowSavage, shadowLorien);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(haldir, runner);
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(haldir);
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveActionOrder("Ancient Enmity");
		scn.FreepsChooseCard(haldir);
		assertTrue(scn.ShadowDecisionAvailable("Discard 1 card from hand to prevent"));
		assertEquals(1, scn.GetWoundsOn(haldir));

		scn.ShadowChooseNo();
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.HAND, shadowSavage);
		assertInZone(Zone.HAND, shadowLorien);
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ThatElfLosingTheSkirmishBringsNoHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var lorien = scn.GetFreepsCard("lorien");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(lorien, arwen);
		scn.AddWoundsToChar(arwen, 1);
		scn.MoveCardsToHand(enmity);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(lorien, runner);
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(lorien);
		assertEquals(5, scn.GetStrength(lorien));
		assertEquals(0, scn.GetWoundsOn(lorien));

		// The Lorien Elf, 4 + 1, ties the Runner, 5, and so loses.
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(lorien));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertFalse(scn.FreepsDecisionAvailable("Choose Elf to heal"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnElfWhoIsNotInTheSkirmishMayBeChosenAndThenNoSkirmishOfTheTurnBringsAHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(haldir, arwen);
		// Haldir, 5 + 1, can beat a Runner, 5, without the event.
		scn.ApplyAdHocModifier(new StrengthModifier(null, haldir, null, 1));
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(arwen, 1);
		scn.MoveCardsToHand(enmity);

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
		assertEquals(6, scn.GetStrength(haldir));

		// Haldir beats the other Runner in his own skirmish, which is not the one the event was played in.
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(6, scn.GetStrength(haldir));

		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner2);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoCopiesPlayedOnOneElfAreStrengthPlus2AndEachBringsAHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var enmity2 = scn.GetFreepsCard("enmity2");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		// The minion here is no Orc and is not of the [moria] culture, and it outlives the skirmish it loses.
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(haldir, arwen);
		scn.AddWoundsToChar(haldir, 2);
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveCardsToHand(enmity, enmity2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(haldir, savage);
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(haldir);
		scn.ShadowPass();
		scn.FreepsPlayCard(enmity2);
		scn.FreepsChooseCard(haldir);
		assertEquals(7, scn.GetStrength(haldir));
		assertEquals(0, scn.GetWoundsOn(savage));

		// Haldir, 7, beats the Savage, 5, which takes 1 wound of its 3.
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveActionOrder("Ancient Enmity");
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(haldir));

		scn.FreepsResolveActionOrder("Ancient Enmity");
		scn.FreepsChooseCard(arwen);
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoElfInPlayItMayStillBePlayedAndDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(aragorn);
		// A Man of the [elven] culture is no Elf.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, aragorn, null, Culture.ELVEN));
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveCardsToHand(enmity);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, runner);
		assertTrue(scn.FreepsPlayAvailable(enmity));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(enmity);
		assertInZone(Zone.DISCARD, enmity);
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Aragorn, 8, beats the Runner, 5, and nothing follows from the event.
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheHealMayBeDeclinedAndThenTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var shadowSavage = scn.GetShadowCard("savage");
		var shadowLorien = scn.GetShadowCard("lorien");
		scn.MoveCompanionsToTable(haldir, arwen);
		scn.AddWoundsToChar(haldir, 1);
		scn.AddWoundsToChar(arwen, 1);
		scn.MoveCardsToHand(enmity);
		scn.MoveCardsToHand(shadowSavage, shadowLorien);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(haldir, runner);
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(haldir);
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertTrue(scn.FreepsHasCardChoiceAvailable(haldir, arwen));
		assertEquals(0, scn.FreepsGetChoiceMin());

		scn.FreepsDeclineChoosing();
		assertFalse(scn.ShadowDecisionAvailable("Discard 1 card from hand to prevent"));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoWoundedElfTheShadowPlayerIsNotAskedToPreventAHealOfNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var boromir = scn.GetFreepsCard("boromir");
		var runner = scn.GetShadowCard("runner");
		var shadowSavage = scn.GetShadowCard("savage");
		var shadowLorien = scn.GetShadowCard("lorien");
		scn.MoveCompanionsToTable(haldir, arwen, boromir);
		// The only wounded character of the [elven] culture is a Man.
		scn.ApplyAdHocModifier(new IsAdditionalCultureModifier(null, boromir, null, Culture.ELVEN));
		scn.AddWoundsToChar(boromir, 1);
		scn.MoveCardsToHand(enmity);
		scn.MoveCardsToHand(shadowSavage, shadowLorien);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(haldir, runner);
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(haldir);
		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveActionOrder("Ancient Enmity");
		assertFalse(scn.ShadowDecisionAvailable("Discard 1 card from hand to prevent"));
		assertEquals(2, scn.GetShadowHandCount());
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertInZone(Zone.DISCARD, runner);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoMinionsLosingTheOneSkirmishToThatElfBringOneHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var enmity = scn.GetFreepsCard("enmity");
		var haldir = scn.GetFreepsCard("haldir");
		var arwen = scn.GetFreepsCard("arwen");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(haldir, arwen);
		// Haldir, 5 + 5 and + 1 from the event, can beat two Runners, 5 + 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, haldir, null, 5));
		scn.AddWoundsToChar(haldir, 2);
		scn.AddWoundsToChar(arwen, 2);
		scn.MoveCardsToHand(enmity);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		// The Shadow player sends the second Runner into the same skirmish.
		scn.FreepsAssignToMinions(haldir, runner);
		scn.ShadowAssignToMinions(haldir, runner2);
		scn.FreepsResolveSkirmish(haldir);
		scn.FreepsPlayCard(enmity);
		scn.FreepsChooseCard(haldir);
		assertEquals(11, scn.GetStrength(haldir));

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsResolveActionOrder("Ancient Enmity");
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertInZone(Zone.DISCARD, runner);
		assertInZone(Zone.DISCARD, runner2);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
