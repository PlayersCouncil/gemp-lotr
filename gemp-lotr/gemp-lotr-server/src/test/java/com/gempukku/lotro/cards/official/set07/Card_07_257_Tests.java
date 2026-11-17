package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_257_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horseman", "7_257");
					put("horseman2", "7_257");
					// Rider's Mount: [rohan] mount, "Bearer must be a [rohan] Man"; at the start of a skirmish of its
					// bearer each minion skirmishing him exerts (none of the minions here can)
					put("mount", "4_287");
					put("mount2", "4_287");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion; a strength bonus only
					// Shadowfax: [gandalf] mount for Gandalf; its texts are about hand weapons and skirmishes
					put("shadowfax", "4_100");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, Valiant and nothing else
					put("chain", "4_278");      // Heavy Chain: a [rohan] possession that is no mount, borne by a Man
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, who may bear Rider's Mount
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its text answers its own play
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VeteranHorsemanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Veteran Horseman
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Skirmish:</b> Exert this companion to make him strength +1 for each mounted companion you spot.
		*/

		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman");

		assertEquals("Veteran Horseman", horseman.getBlueprint().getTitle());
		assertNull(horseman.getBlueprint().getSubtitle());
		assertFalse(horseman.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, horseman.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, horseman.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, horseman.getBlueprint().getCardType());
		assertEquals(Race.MAN, horseman.getBlueprint().getRace());
		assertEquals(2, horseman.getBlueprint().getTwilightCost());
		assertEquals(6, horseman.getBlueprint().getStrength());
		assertEquals(3, horseman.getBlueprint().getVitality());
		assertEquals(6, horseman.getBlueprint().getResistance());
	}

	@Test
	public void ExertsToBeStrengthPlusOneForEachMountedCompanionSpottedUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman");
		var horseman2 = scn.GetFreepsCard("horseman2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(horseman, horseman2, gandalf, guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(horseman, mount);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.AttachCardsTo(guard, chain);
		scn.AttachCardsTo(guma, mount2);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertAttachedTo(mount, horseman);
		assertAttachedTo(shadowfax, gandalf);
		assertAttachedTo(chain, guard);
		assertAttachedTo(mount2, guma);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{horseman, runner1}, new PhysicalCardImpl[]{horseman2, runner2});
		scn.FreepsResolveSkirmish(horseman);

		assertEquals(0, scn.GetWoundsOn(horseman));
		assertEquals(6, scn.GetStrength(horseman));
		assertEquals(6, scn.GetStrength(horseman2));
		assertTrue(scn.FreepsActionAvailable(horseman));
		scn.FreepsUseCardAction(horseman);
		assertEquals(1, scn.GetWoundsOn(horseman));

		// Two mounted companions to spot, himself and Gandalf: not the companion bearing a possession that is no
		// mount, the mounted ally, or the other Veteran Horseman, who rides nothing.  He may spot fewer; the number
		// comes filled in with all of them.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		assertEquals(2, (int) scn.FreepsGetChoiceDefault());
		scn.FreepsChoose("2");

		// 6 + 2, on him alone.
		assertEquals(8, scn.GetStrength(horseman));
		assertEquals(6, scn.GetStrength(horseman2));
		assertEquals(0, scn.GetWoundsOn(horseman2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Still in force at the next decision of the skirmish; gone in the next skirmish.
		scn.ShadowPass();
		assertEquals(8, scn.GetStrength(horseman));
		scn.FreepsPass();
		scn.FreepsResolveSkirmish(horseman2);
		assertInPlay(horseman);
		assertEquals(6, scn.GetStrength(horseman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishWhileUnassignedHeSpotsOneOfTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman");
		var horseman2 = scn.GetFreepsCard("horseman2");
		var mount = scn.GetFreepsCard("mount");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(horseman, horseman2, gandalf);
		scn.AttachCardsTo(horseman, mount);
		scn.AttachCardsTo(gandalf, shadowfax);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertAttachedTo(mount, horseman);
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		// He is assigned to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, runner1}, new PhysicalCardImpl[]{horseman2, runner2});
		scn.FreepsResolveSkirmish(gandalf);

		assertEquals(0, scn.GetWoundsOn(horseman));
		assertEquals(6, scn.GetStrength(horseman));
		assertTrue(scn.FreepsActionAvailable(horseman));
		scn.FreepsUseCardAction(horseman);
		assertEquals(1, scn.GetWoundsOn(horseman));
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		// One spotted of two: 6 + 1.
		assertEquals(7, scn.GetStrength(horseman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Gone in the next skirmish.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(horseman2);
		assertEquals(6, scn.GetStrength(horseman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoMountedCompanionHeExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var guma = scn.GetFreepsCard("guma");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(horseman, guard);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guard, chain);
		scn.AttachCardsTo(guma, mount2);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		assertAttachedTo(chain, guard);
		assertAttachedTo(mount2, guma);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(horseman, runner1);
		scn.FreepsResolveSkirmish(horseman);

		assertEquals(0, scn.GetWoundsOn(horseman));
		assertEquals(6, scn.GetStrength(horseman));

		// The companion bearing a possession that is no mount and the mounted ally: nobody to spot, and no number
		// is asked.  The exertion is paid all the same.
		assertTrue(scn.FreepsActionAvailable(horseman));
		scn.FreepsUseCardAction(horseman);

		assertEquals(1, scn.GetWoundsOn(horseman));
		assertEquals(6, scn.GetStrength(horseman));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ExhaustedHeCannotUseItWhileTheOtherCopyCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horseman = scn.GetFreepsCard("horseman");
		var horseman2 = scn.GetFreepsCard("horseman2");
		var mount = scn.GetFreepsCard("mount");
		var mount2 = scn.GetFreepsCard("mount2");
		scn.MoveCompanionsToTable(horseman, horseman2);
		scn.AttachCardsTo(horseman, mount);
		scn.AttachCardsTo(horseman2, mount2);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(horseman, 2);

		var runner1 = scn.GetShadowCard("runner1");

		assertEquals(2, scn.GetWoundsOn(horseman));

		scn.StartGame();
		assertAttachedTo(mount, horseman);
		assertAttachedTo(mount2, horseman2);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(horseman, runner1);
		scn.FreepsResolveSkirmish(horseman);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(horseman));
		assertTrue(scn.FreepsActionAvailable(horseman2));
		assertEquals(2, scn.GetWoundsOn(horseman));
	}
}
