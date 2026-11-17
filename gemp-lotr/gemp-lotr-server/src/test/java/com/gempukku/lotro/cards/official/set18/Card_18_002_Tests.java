package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_002_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("run", "18_2");
					put("run2", "18_2");
					put("gimli", "1_13");         // Gimli, Son of Glóin: [dwarven] companion, no hunter; his Skirmish ability is never used
					put("eager", "15_5");         // Gimli, Eager Hunter: [dwarven] companion who is hunter 3 already (never beside the other Gimli)
					put("farin", "1_11");         // Farin, Dwarven Emissary: [dwarven] companion, no hunter; +2 against an Orc
					put("grimir", "1_17");        // Grimir, Dwarven Elder: a [dwarven] Dwarf ALLY; a Fellowship ability only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a [gondor] companion; his Maneuver ability is never used

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RunUntilFoundStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Run Until Found
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, place a [dwarven] token here.<br><b>Skirmish:</b> Discard this condition from play or remove a [dwarven] token from here to make a [dwarven] companion gain <b>hunter 1</b>.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("run");

		assertEquals("Run Until Found", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedItTakesOneDwarvenTokenAndASecondCopyPlayedAfterAddsNoneHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var run2 = scn.GetFreepsCard("run2");
		scn.MoveCardsToHand(run, run2);

		scn.StartGame();
		assertEquals(0, scn.GetCultureTokensOn(run));
		assertEquals(0, scn.GetCultureTokensOn(run2));

		scn.FreepsPlayCard(run);
		assertInZone(Zone.SUPPORT, run);
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The second copy's play puts its own token on itself, and none on the first.
		scn.FreepsPlayCard(run2);
		assertInZone(Zone.SUPPORT, run2);
		assertEquals(1, scn.GetCultureTokensOn(run2));
		assertEquals(1, scn.GetCultureTokensOn(run));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesATokenToMakeADwarvenCompanionGainHunter1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var eager = scn.GetFreepsCard("eager");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(eager, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(run);
		assertInZone(Zone.SUPPORT, run);

		scn.StartGame();
		scn.AddTokensToCard(run, 1);
		assertEquals(1, scn.GetCultureTokensOn(run));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eager, savage);
		scn.FreepsResolveSkirmish(eager);

		assertEquals(3, scn.GetKeywordCount(eager, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(run));
		scn.FreepsUseCardAction(run);

		// Both costs can be paid, and the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Discard this condition"));
		assertTrue(scn.FreepsChoiceAvailable("token"));
		scn.FreepsChooseOption("token");
		assertEquals(0, scn.GetCultureTokensOn(run));
		assertInZone(Zone.SUPPORT, run);

		// A [dwarven] companion in the skirmish (a hunter already) and one in none; not a companion of another
		// culture, nor a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eager, farin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(farin);

		assertEquals(1, scn.GetKeywordCount(farin, Keyword.HUNTER));
		assertEquals(3, scn.GetKeywordCount(eager, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarvenCompanionTheAbilityIsOfferedAndTheTokenRemovedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(run);
		assertInZone(Zone.SUPPORT, run);

		scn.StartGame();
		scn.AddTokensToCard(run, 1);
		assertEquals(1, scn.GetCultureTokensOn(run));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// The only companions are a [gondor] one and the Ring-bearer, and the [dwarven] ally is no companion.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(run));
		scn.FreepsUseCardAction(run);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("token");

		assertEquals(0, scn.GetCultureTokensOn(run));
		assertInZone(Zone.SUPPORT, run);
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(grimir, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(scn.GetRingBearer(), Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DiscardingItForTheAbilityTheHunterLastsOnlyThroughThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToSupportArea(run);
		assertInZone(Zone.SUPPORT, run);

		scn.StartGame();
		scn.AddTokensToCard(run, 1);
		assertEquals(1, scn.GetCultureTokensOn(run));
		scn.MoveMinionsToTable(savage, troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{farin, troop});
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.FreepsActionAvailable(run));
		scn.FreepsUseCardAction(run);
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		scn.FreepsChooseOption("Discard this condition");
		assertInDiscard(run);
		// Farin, whose own skirmish is still to come, is made a hunter in Gimli's.
		scn.FreepsChooseCard(farin);
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.HUNTER));
		assertEquals(0, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still so at the next decision of that skirmish.
		scn.ShadowPass();
		assertEquals(1, scn.GetKeywordCount(farin, Keyword.HUNTER));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsPass();

		// Each skirmish is a phase of its own: in Farin's, it is gone.
		scn.FreepsResolveSkirmish(farin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetKeywordCount(farin, Keyword.HUNTER));
	}

	@Test
	public void WithNoTokenHereTheAbilityDiscardsThisConditionUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var run2 = scn.GetFreepsCard("run2");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToSupportArea(run, run2);
		assertInZone(Zone.SUPPORT, run, run2);

		scn.StartGame();
		// The other copy holds a token; this one holds none.
		scn.AddTokensToCard(run2, 1);
		assertEquals(0, scn.GetCultureTokensOn(run));
		assertEquals(1, scn.GetCultureTokensOn(run2));
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);

		assertTrue(scn.FreepsActionAvailable(run));
		scn.FreepsUseCardAction(run);

		// No question of which cost: this copy goes, and the other keeps its token.
		assertInDiscard(run);
		assertInZone(Zone.SUPPORT, run2);
		assertEquals(1, scn.GetCultureTokensOn(run2));
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.HUNTER));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var run = scn.GetFreepsCard("run");
		var gimli = scn.GetFreepsCard("gimli");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(run);
		assertInZone(Zone.SUPPORT, run);

		scn.StartGame();
		scn.AddTokensToCard(run, 1);
		assertEquals(1, scn.GetCultureTokensOn(run));
		scn.MoveMinionsToTable(savage);

		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(run));
		assertEquals(1, scn.GetCultureTokensOn(run));
	}
}
