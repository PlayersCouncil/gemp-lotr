package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_010_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("darts", "101_10");
					// Legolas, Prince of Mirkwood: unique [elven] companion, archer, strength 6, vitality 3.
					// His +3 is only against a Nazgul.
					put("legolas", "1_51");
					// Arwen, Daughter of Elrond: unique [elven] companion who is not an archer, vitality 3.
					// Her +3 is only against a Nazgul.
					put("arwen", "1_30");
					put("lorienelf", "1_53");   // Lorien Elf: an [elven] companion that is not unique
					// Elven Bow: bearer must be an Elf and is an archer.  No other text.
					put("bow", "1_41");
					// Aragorn, Ranger of the North: unique [gondor] companion, strength 8, vitality 4.  His
					// maneuver ability is never used here.
					put("aragorn", "1_89");
					// Aragorn's Bow: Aragorn is an archer.  Its archery ability is never used here.
					put("gornbow", "1_90");
					put("sword", "1_112");      // Ranger's Sword: Aragorn is damage +1
					// Orophin, Lorien Bowman: unique [elven] ally, home site 6, archer.  His archery ability
					// only wounds an Uruk-hai and is never used here.
					put("orophin", "1_56");

					put("marksman", "1_176");   // Goblin Marksman: archer, strength 7, vitality 1, no other text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1, no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LetFlyTheDartsOfLindonStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Let Fly the Darts of Lindon
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Game Text: Tale.
		 * Bearer must be a unique [elven] companion.
		 * Archery: Exert bearer to make all Free Peoples archers lose archer and gain damage +1 until the
		 * regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("darts");

		assertEquals("Let Fly the Darts of Lindon", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.TALE));
		assertFalse(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysForOneTwilightOnAChosenUniqueElvenCompanionAndOnNoOneElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var darts = scn.GetFreepsCard("darts");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(lorienelf, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(darts, legolas);

		scn.StartGame();

		// Nobody on the table can bear it: the Lorien Elf is not unique, Aragorn and Frodo are not [elven],
		// and Orophin is an ally.
		assertInHand(darts);
		assertFalse(scn.FreepsPlayAvailable(darts));

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(legolas);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(darts));

		// A second unique [elven] companion, so that the bearer is a choice.
		scn.MoveCompanionsToTable(arwen);

		scn.FreepsPlayCard(darts);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorienelf, aragorn, orophin, frodo));

		scn.FreepsChooseCard(arwen);
		assertAttachedTo(darts, arwen);
		assertNotAttachedTo(darts, legolas);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ArcheryActionExertsBearerToMakeEveryFreePeoplesArcherLoseArcherAndGainDamagePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darts = scn.GetFreepsCard("darts");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gornbow = scn.GetFreepsCard("gornbow");
		var sword = scn.GetFreepsCard("sword");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var bow = scn.GetFreepsCard("bow");
		var orophin = scn.GetFreepsCard("orophin");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCompanionsToTable(arwen, legolas, aragorn, lorienelf);
		scn.MoveAlliesToTable(orophin);
		// The bearer is Arwen, who is not an archer herself.
		scn.AttachCardsTo(arwen, darts);
		scn.AttachCardsTo(aragorn, gornbow, sword);
		scn.AttachCardsTo(lorienelf, bow);

		scn.StartGame();

		scn.MoveMinionsToTable(marksman);

		// Not in the maneuver phase, the one before.
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(darts));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertTrue(scn.FreepsActionAvailable(darts));

		// Four Free Peoples archers: an [elven] companion with the keyword in print, a [gondor] companion who
		// has it from his bow and is damage +1 already, a companion who is not unique and has it from an Elven
		// Bow, and an ally away from his home site.
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(lorienelf, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(lorienelf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(orophin, Keyword.DAMAGE));
		// Beside them, a Free Peoples character who is not an archer and an archer who is not Free Peoples.
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(marksman, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(marksman, Keyword.DAMAGE));
		// Legolas + Aragorn + the Lorien Elf.  Orophin's home is site 6 and the fellowship is at site 2.
		assertEquals(3, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.FreepsUseCardAction(darts);
		assertEquals(1, scn.GetWoundsOn(arwen));

		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(aragorn, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(lorienelf, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(orophin, Keyword.ARCHER));
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		// Ranger's Sword's damage +1 and this one.
		assertEquals(2, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(lorienelf, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(orophin, Keyword.DAMAGE));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(marksman, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(marksman, Keyword.DAMAGE));
		assertEquals(1, scn.GetShadowArcheryTotal());

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void EffectLastsThroughTheSkirmishesAndEndsWhenTheRegroupPhaseStarts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darts = scn.GetFreepsCard("darts");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.AttachCardsTo(legolas, darts);

		scn.StartGame();

		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(darts);
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Archery fire with no archer left: neither minion is wounded, and the Shadow player is not asked to
		// place a wound.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		// Nor is it an assignment action, in the phase after.
		assertFalse(scn.FreepsActionAvailable(darts));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{legolas, savage});

		// Aragorn, 8 against 5, kills the Goblin Runner.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// Legolas's skirmish is the last thing before the regroup phase, with a skirmish phase already over.
		scn.FreepsResolveSkirmish(legolas);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));

		// Legolas, 6 against 5, wins: 1 wound for the loss + 1 for his damage +1.
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
	}

	@Test
	public void IsOfferedUntilBearerIsExhaustedAndASecondUseGivesAFormerArcherNothingMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darts = scn.GetFreepsCard("darts");
		var legolas = scn.GetFreepsCard("legolas");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, darts);

		scn.StartGame();

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertTrue(scn.FreepsActionAvailable(darts));

		scn.FreepsUseCardAction(darts);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));

		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(darts));

		// Legolas pays again, but he is no longer an archer and there is no other: nobody gains anything.
		scn.FreepsUseCardAction(darts);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));

		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(darts));
	}

	@Test
	public void ACharacterWhoBecomesAnArcherAfterTheAbilityIsUsedIsNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darts = scn.GetFreepsCard("darts");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.AttachCardsTo(arwen, darts);

		scn.StartGame();

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		// Legolas alone.
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(darts);
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// Arwen becomes an archer once the ability has been used.  The bow is cheated on, because no bow can
		// be played in the archery phase: the board is the one an effect that makes her an archer would leave.
		scn.AttachCardsTo(arwen, bow);

		// "Make all ... until the regroup phase" takes in the archers there are when it is used and no later
		// ones (Comprehensive Rules 4.2, "event": such an effect takes a "snapshot").  Arwen keeps archer and
		// gains no damage +1.
		assertTrue(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(arwen, Keyword.DAMAGE));
		// Arwen alone: Legolas is as the ability left him.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ACharacterWhoHasLostArcherCannotBeGivenItAgainBeforeTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var darts = scn.GetFreepsCard("darts");
		var arwen = scn.GetFreepsCard("arwen");
		var legolas = scn.GetFreepsCard("legolas");
		var bow = scn.GetFreepsCard("bow");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, legolas);
		scn.AttachCardsTo(arwen, darts);

		scn.StartGame();

		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsUseCardAction(darts);
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(0, scn.GetFreepsArcheryTotal());

		// An Elven Bow ("Bearer is an archer") arrives on Legolas afterwards.  Losing beats gaining: he has lost
		// archer until the regroup phase, and the bow does not give it back before then.
		scn.AttachCardsTo(legolas, bow);
		assertAttachedTo(bow, legolas);
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetKeywordCount(legolas, Keyword.DAMAGE));

		// At the regroup phase the loss ends, and the bow counts.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertEquals(0, scn.GetKeywordCount(legolas, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
