package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_256_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sang", "7_256");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
					// Rider's Mount: [rohan] mount, "Bearer must be a [rohan] Man"; its text acts only in skirmishes
					put("mount", "4_287");
					put("mount2", "4_287");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, Knight and nothing else
					// Horse of Rohan: a mount for a Man, Elf or Wizard; its text acts only in skirmishes
					put("horse", "4_283");
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard companion, vitality 4; a strength bonus only
					// Shadowfax: [gandalf] mount for Gandalf; its texts are about hand weapons and skirmishes
					put("shadowfax", "4_100");
					put("chain", "4_278");      // Heavy Chain: a [rohan] possession that is no mount, borne by a Man
					put("guma", "4_277");       // Guma, Plains Farmer: [rohan] Man ally, who may bear Rider's Mount
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its text answers its own play
					put("explorer", "4_250");   // Southron Explorer: [raider] Man, vitality 3; a regroup ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Two Household Guards, each on a Rider's Mount, and two minions that are still in play in the regroup phase.
	protected VirtualTableScenario GetTwoRidersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sang", "7_256");
					put("guard1", "5_83");      // Household Guard: [rohan] Man, vitality 3, Valiant and nothing else
					put("guard2", "5_83");
					put("mount1", "4_287");     // Rider's Mount: on a [rohan] Man; its text acts at the start of a skirmish
					put("mount2", "4_287");
					put("savage1", "1_151");    // Uruk Savage: vitality 3
					put("savage2", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheySangasTheySlewStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: They Sang as They Slew
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Exert X mounted companions to wound X minions.
		*/

		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");

		assertEquals("They Sang as They Slew", sang.getBlueprint().getTitle());
		assertNull(sang.getBlueprint().getSubtitle());
		assertFalse(sang.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sang.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, sang.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, sang.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(sang, Timeword.REGROUP));
		assertEquals(1, sang.getBlueprint().getTwilightCost());
	}

	// "Exert X mounted companions to wound X minions": an X of exertions is at least 1 (Q-0076), and CR 4.2
	// "exert" says of this card "You cannot pay for the cost of They Sang As They Slew by exerting a character zero
	// times."  The definition let the choice of companions be answered with none.
	// F-0447.
	@Test
	public void AtLeastOneMountedCompanionExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTwoRidersScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard1, guard2);
		scn.AttachCardsTo(guard1, scn.GetFreepsCard("mount1"));
		scn.AttachCardsTo(guard2, scn.GetFreepsCard("mount2"));
		scn.MoveCardsToHand(sang);

		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2);
		// Nobody is assigned, so both minions are in play when the regroup phase starts.
		scn.SkipToPhase(Phase.REGROUP);
		scn.SetTwilight(1);

		// Both riders exhausted (Frodo, who is not mounted, could exert): the event cannot be played.
		scn.AddWoundsToChar(guard1, 2);
		scn.AddWoundsToChar(guard2, 2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetWoundsOn(guard1));
		assertEquals(2, scn.GetWoundsOn(guard2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(sang));

		scn.RemoveWoundsFromChar(guard1, 2);
		scn.RemoveWoundsFromChar(guard2, 2);
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetWoundsOn(guard1));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(savage1));
		assertEquals(0, scn.GetWoundsOn(savage2));
		assertTrue(scn.FreepsPlayAvailable(sang));
		scn.FreepsPlayCard(sang);

		// One rider at the least, and Frodo is not among them.
		assertEquals(2, scn.FreepsGetCardChoiceCount());
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(guard1, guard2);

		// Two exerted, so two minions wounded: there are two, and nothing is asked.
		assertEquals(1, scn.GetWoundsOn(guard1));
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TwoMountedCompanionsExertToWoundTwoOfThreeMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
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
		scn.MoveCardsToHand(sang);

		var runner = scn.GetShadowCard("runner");
		var explorer = scn.GetShadowCard("explorer");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(horse, knight);
		assertAttachedTo(shadowfax, gandalf);
		assertAttachedTo(chain, guard2);
		assertAttachedTo(mount2, guma);
		scn.MoveMinionsToTable(runner, explorer, savage);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsPlayAvailable(sang));
		scn.FreepsPlayCard(sang);

		// Mounted companions of three cultures and two races; not the companion bearing a possession that is no
		// mount, the mounted ally, or Frodo.  One at the least (CR 4.2 "exert"), all three at the most.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, knight, gandalf));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard2, guma, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard, gandalf);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(guard2));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Two exerted: two different minions out of three, no companion among them.
		assertTrue(scn.FreepsHasCardChoiceAvailable(runner, explorer, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(runner, explorer);

		// The Runner, of vitality 1, is wounded and so killed: an exertion would have left him out.
		assertInDiscard(runner);
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(sang);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void OneMountedCompanionExertsToWoundOneMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(guard, gandalf);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(sang);

		var explorer = scn.GetShadowCard("explorer");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(shadowfax, gandalf);
		scn.MoveMinionsToTable(explorer, savage);
		scn.SkipToPhase(Phase.REGROUP);

		scn.FreepsPlayCard(sang);
		scn.FreepsChooseCards(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(guard));

		// One exerted: one minion of the two.
		assertTrue(scn.FreepsHasCardChoiceAvailable(explorer, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(savage);

		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionTheCompanionsExertForNothingAndXIsNotHeldToTheMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sang = scn.GetFreepsCard("sang");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var knight = scn.GetFreepsCard("knight");
		var horse = scn.GetFreepsCard("horse");
		var gandalf = scn.GetFreepsCard("gandalf");
		var shadowfax = scn.GetFreepsCard("shadowfax");
		scn.MoveCompanionsToTable(guard, knight, gandalf);
		scn.AttachCardsTo(guard, mount);
		scn.AttachCardsTo(knight, horse);
		scn.AttachCardsTo(gandalf, shadowfax);
		scn.MoveCardsToHand(sang);

		scn.StartGame();
		assertAttachedTo(mount, guard);
		assertAttachedTo(horse, knight);
		assertAttachedTo(shadowfax, gandalf);
		// No minion: the game goes from the Shadow phase straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		// X is what is paid, whatever it buys: all three may exert, with nothing to wound.
		assertTrue(scn.FreepsPlayAvailable(sang));
		scn.FreepsPlayCard(sang);
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(guard, knight, gandalf);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(sang);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
