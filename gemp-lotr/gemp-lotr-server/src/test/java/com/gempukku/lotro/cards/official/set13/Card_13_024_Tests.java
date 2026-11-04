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

public class Card_13_024_Tests
{

	// Every skirmish is fought at site 2 of the Fellowship path (Ettenmoors), whose skirmish ability is never used.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nimbly", "13_24");

					put("lorienelf", "1_53");   // Lórien Elf: Elf companion, strength 4, vitality 2; to play, spot an Elf (cheated in)
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: Elf companion, strength 5; +2 only at sites 6 to 8
					put("celeborn", "1_34");    // Celeborn, Lord of Lórien: Elf ally, strength 6; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8; a maneuver ability never used

					put("agility", "6_12");     // Agility: [elven] support-area condition holding [elven] tokens; a skirmish ability used only on purpose
					put("strands", "9_22");     // Strands of Elven Hair: [elven] support-area possession holding [elven] tokens; a fellowship ability only
					put("palaces", "6_10");     // Suspended Palaces: [dwarven] support-area condition holding [dwarven] tokens; a fellowship ability only

					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text answers only its own play
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SpringForthNimblyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Spring Forth Nimbly
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an Elf strength +1 for each card that has an [elven] token on it.
		*/

		var scn = GetScenario();

		var nimbly = scn.GetFreepsCard("nimbly");

		assertEquals("Spring Forth Nimbly", nimbly.getBlueprint().getTitle());
		assertNull(nimbly.getBlueprint().getSubtitle());
		assertFalse(nimbly.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, nimbly.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, nimbly.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, nimbly.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(nimbly, Timeword.SKIRMISH));
		assertEquals(2, nimbly.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnElfStrengthPlusOneForEachCardWithAnElvenTokenNotForEachToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nimbly = scn.GetFreepsCard("nimbly");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var agility = scn.GetFreepsCard("agility");
		var strands = scn.GetFreepsCard("strands");
		var palaces = scn.GetFreepsCard("palaces");
		scn.MoveCompanionsToTable(lorienelf, haldir, aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToSupportArea(agility, strands, palaces);
		scn.AddTokensToCard(agility, 1);
		scn.AddTokensToCard(strands, 2);
		scn.AddTokensToCard(palaces, 2);
		scn.MoveCardsToHand(nimbly);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertEquals(1, scn.GetCultureTokensOn(agility));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertEquals(2, scn.GetCultureTokensOn(palaces));
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{lorienelf, runner}, new PhysicalCardImpl[]{haldir, savage});
		scn.FreepsResolveSkirmish(lorienelf);

		assertEquals(4, scn.GetStrength(lorienelf));
		assertTrue(scn.FreepsPlayAvailable(nimbly));
		scn.FreepsPlayCard(nimbly);
		// Elves skirmishing, assigned to a skirmish still to come, and an ally; not a Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(lorienelf, haldir, celeborn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(lorienelf);

		// 4 + 1 for Agility + 1 for the Strands (two tokens, one card); the [dwarven] tokens count for nothing.
		assertEquals(6, scn.GetStrength(lorienelf));
		assertEquals(5, scn.GetStrength(haldir));
		assertEquals(6, scn.GetStrength(celeborn));
		assertEquals(1, scn.GetCultureTokensOn(agility));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertInDiscard(nimbly);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithOneCardHoldingAnElvenTokenTheBonusIsOneAndLastsOnlyTheSkirmishItIsPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nimbly = scn.GetFreepsCard("nimbly");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var haldir = scn.GetFreepsCard("haldir");
		var agility = scn.GetFreepsCard("agility");
		scn.MoveCompanionsToTable(lorienelf, haldir);
		scn.MoveCardsToSupportArea(agility);
		scn.AddTokensToCard(agility, 2);
		scn.MoveCardsToHand(nimbly);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(agility));
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{lorienelf, runner}, new PhysicalCardImpl[]{haldir, savage});
		scn.FreepsResolveSkirmish(lorienelf);

		// Played in the Lórien Elf's skirmish on Haldir, whose own skirmish is still to come.
		assertEquals(5, scn.GetStrength(haldir));
		scn.FreepsPlayCard(nimbly);
		scn.FreepsChooseCard(haldir);
		// 5 + 1: one card with two [elven] tokens.
		assertEquals(6, scn.GetStrength(haldir));
		assertEquals(4, scn.GetStrength(lorienelf));

		// The Lórien Elf (4) loses to the Goblin Runner (5) and lives; then Haldir's skirmish, a phase of its own.
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(5, scn.GetStrength(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoElfOnTheTableItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nimbly = scn.GetFreepsCard("nimbly");
		var aragorn = scn.GetFreepsCard("aragorn");
		var agility = scn.GetFreepsCard("agility");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(agility);
		scn.AddTokensToCard(agility, 2);
		scn.MoveCardsToHand(nimbly);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(agility));
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsPlayAvailable(nimbly));
		scn.FreepsPlayCard(nimbly);
		assertInDiscard(nimbly);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(5, scn.GetStrength(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nimbly = scn.GetFreepsCard("nimbly");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var agility = scn.GetFreepsCard("agility");
		scn.MoveCompanionsToTable(lorienelf);
		scn.MoveCardsToSupportArea(agility);
		scn.AddTokensToCard(agility, 2);
		scn.MoveCardsToHand(nimbly);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(agility));
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertInHand(nimbly);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(nimbly));
	}
}
