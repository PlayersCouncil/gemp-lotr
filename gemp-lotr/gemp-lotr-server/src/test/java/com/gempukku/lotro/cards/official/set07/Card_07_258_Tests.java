package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_258_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "7_258");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, Valiant and nothing else
					put("guard2", "5_83");
					// Rider's Mount: [rohan] mount, "Bearer must be a [rohan] Man"; at the start of a skirmish of its
					// bearer each minion skirmishing him exerts
					put("mount", "4_287");
					put("mount2", "4_287");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, Knight and nothing else
					// Horse of Rohan: a mount for a Man, Elf or Wizard; its text is Rider's Mount's
					put("horse", "4_283");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; a strength bonus only
					// Shadowfax: [gandalf] mount for Gandalf; its texts are about hand weapons and skirmishes
					put("shadowfax", "4_100");
					put("chain", "4_278");      // Heavy Chain: a [rohan] possession that is no mount, borne by a Man
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, strength 4, who may bear Rider's Mount
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, strength 5, vitality 3; a regroup ability only
					put("explorer2", "4_250");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text answers its own play
					// Bladetusk Rearguard: a [raider] artifact that its maneuver ability makes a fierce Southron minion
					// who is no Man, strength 5, vitality 5, until the end of the turn
					put("rearguard", "103_43");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteHotFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: White Hot Fury
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a mounted [rohan] Man strength +1 for each mounted companion. If that Man is skirmishing a [raider] minion, also make him or her strength +2 and <b>damage +2</b>.
		*/

		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");

		assertEquals("White Hot Fury", fury.getBlueprint().getTitle());
		assertNull(fury.getBlueprint().getSubtitle());
		assertFalse(fury.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, fury.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, fury.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, fury.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(fury, Timeword.SKIRMISH));
		assertEquals(2, fury.getBlueprint().getTwilightCost());
	}

	@Test
	public void AMountedRohanManSkirmishingARaiderMinionIsPlusOneForEachMountedCompanionAndPlusTwoAndDamagePlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var knight = scn.GetFreepsCard("knight");
		var horse = scn.GetFreepsCard("horse");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var chain = scn.GetFreepsCard("chain");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(guard, guard2, knight, gandalf);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(knight, horse);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.AttachCardsTo(guard2, chain);
		scn.AttachCardsTo(guma, mount2);
		scn.MoveCardsToHand(fury);

		var explorer = scn.GetShadowCard("explorer");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(horse, knight);
		assertAttachedTo(shadowfax, gandalf);
		assertAttachedTo(chain, guard2);
		assertAttachedTo(mount2, guma);
		scn.MoveMinionsToTable(explorer, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, explorer}, new PhysicalCardImpl[]{knight, runner});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guma));
		assertTrue(scn.FreepsPlayAvailable(fury));
		scn.FreepsPlayCard(fury);

		// A mounted [rohan] Man companion in this skirmish and a mounted [rohan] Man ally in none; not a mounted
		// [gondor] Man, a [rohan] Man bearing a possession that is no mount, a mounted Wizard, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(knight, guard2, gandalf, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guard);

		// Three mounted companions, counted with nothing asked (the Guard, the Knight and Gandalf: not the mounted
		// ally or the Guard on his Chain), and the Explorer is a [raider] minion: 6 + 3 + 2.
		assertEquals(11, scn.GetStrength(guard));
		assertEquals(2, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertEquals(4, scn.GetStrength(guma));
		assertInDiscard(fury);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of the skirmish; gone in the next skirmish.
		scn.ShadowPass();
		assertEquals(11, scn.GetStrength(guard));
		assertEquals(2, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(knight);
		assertInPlay(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AManAssignedToARaiderMinionWhoseSkirmishHasNotBegunGetsTheCountAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(guard, guard2);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(guard2, mount2);
		scn.MoveCardsToHand(fury);

		var explorer = scn.GetShadowCard("explorer");
		var explorer2 = scn.GetShadowCard("explorer2");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(mount2, guard2);
		scn.MoveMinionsToTable(explorer, explorer2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, explorer}, new PhysicalCardImpl[]{guard2, explorer2});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		scn.FreepsPlayCard(fury);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, guard2));
		scn.FreepsChooseCard(guard2);

		// Two mounted companions: 6 + 2.  He is assigned to a [raider] minion, but his skirmish has not begun, so
		// he is not skirmishing one, though the Guard of this skirmish is.
		assertEquals(8, scn.GetStrength(guard2));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AManSkirmishingAMinionOfAnotherCultureGetsTheCountAlone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(guard, gandalf);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(fury);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		// The Guard is the only mounted [rohan] Man: chosen unasked.
		scn.FreepsPlayCard(fury);

		// Two mounted companions, against a [moria] minion: 6 + 2, and no damage bonus.
		assertEquals(8, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AgainstARaiderMinionWhoIsNoManHeIsPlusTwoAndDamagePlusTwoAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(fury);

		var rearguard = scn.GetShadowCard("rearguard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		assertAttachedTo(mount, guard);
		// The Runner is there so that the game has a maneuver phase.
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, rearguard}, new PhysicalCardImpl[]{scn.GetRingBearer(), runner});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(6, scn.GetStrength(guard));
		assertEquals(0, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		scn.FreepsPlayCard(fury);

		// One mounted companion, against a [raider] minion who is a Southron and no Man: 6 + 1 + 2.
		assertEquals(9, scn.GetStrength(guard));
		assertEquals(2, scn.GetKeywordCount(guard, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMountedRohanManItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guard2 = scn.GetFreepsCard("guard2");
		var chain = scn.GetFreepsCard("chain");
		var knight = scn.GetFreepsCard("knight");
		var horse = scn.GetFreepsCard("horse");
		scn.MoveCompanionsToTable(guard2, knight);
		scn.AttachCardsTo(guard2, chain);
		scn.AttachCardsTo(knight, horse);
		scn.MoveCardsToHand(fury);

		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		assertAttachedTo(chain, guard2);
		assertAttachedTo(horse, knight);
		scn.MoveMinionsToTable(explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard2, explorer);
		scn.FreepsResolveSkirmish(guard2);

		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(6, scn.GetStrength(knight));

		// A [rohan] Man with no mount, skirmishing a [raider] minion, and a mounted [gondor] Man: nobody to choose.
		assertTrue(scn.FreepsPlayAvailable(fury));
		scn.FreepsPlayCard(fury);

		assertInDiscard(fury);
		assertEquals(6, scn.GetStrength(guard2));
		assertEquals(0, scn.GetKeywordCount(guard2, Keyword.DAMAGE));
		assertEquals(6, scn.GetStrength(knight));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(fury);

		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		scn.MoveMinionsToTable(explorer);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(fury);
		assertFalse(scn.FreepsPlayAvailable(fury));
	}
}
