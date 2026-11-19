package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("connected", "15_40");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: [gollum] companion, Ring-bound, no race; to play, add a burden; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, Ring-bound; an assignment ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound Hobbit; a fellowship ability used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: an unbound Hobbit; +2 only bearing a weapon
					put("swordarm", "1_116");   // Swordarm of the White Tower: a Free Peoples skirmish event, twilight 0

					put("drums", "1_168");      // Drums in the Deep: [moria] skirmish event, twilight 0; make a [moria] Orc strength +2
					put("savagery", "1_139");   // Savagery to Match Their Numbers: [isengard] skirmish event, twilight 0
					put("runner", "1_178");     // Goblin Runner: [moria] Orc minion, twilight 1, strength 5, vitality 1; its trigger is on its play only
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ConnectedByFateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Connected By Fate
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot Sméagol.
		 * While you can spot 2 Ring-bound Hobbits, each Shadow event is twilight cost +1.
		 * <b>Skirmish:</b> Discard this condition to make each Ring-bound companion strength +1 until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("connected");

		assertEquals("Connected By Fate", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilSmeagolIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var connected = scn.GetFreepsCard("connected");
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable("sam");
		scn.MoveCardsToHand(connected, smeagol);

		scn.StartGame();

		// Frodo and Sam, and no Sméagol.
		assertInHand(connected, smeagol);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(connected));

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(smeagol);
		assertInZone(Zone.FREE_CHARACTERS, smeagol);
		assertEquals(1, scn.GetBurdens());

		assertTrue(scn.FreepsPlayAvailable(connected));
		scn.FreepsPlayCard(connected);

		assertInZone(Zone.SUPPORT, connected);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheShireSmeagolIsSpottedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var connected = scn.GetFreepsCard("connected");
		var shiresmeagol = scn.GetFreepsCard("shiresmeagol");
		scn.MoveCompanionsToTable(shiresmeagol);
		scn.MoveCardsToHand(connected);

		scn.StartGame();

		assertInZone(Zone.FREE_CHARACTERS, shiresmeagol);
		assertInHand(connected);
		assertTrue(scn.FreepsPlayAvailable(connected));
		scn.FreepsPlayCard(connected);

		assertInZone(Zone.SUPPORT, connected);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTwoRingBoundHobbitsEachShadowEventCostsOneMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var connected = scn.GetFreepsCard("connected");
		var sam = scn.GetFreepsCard("sam");
		var swordarm = scn.GetFreepsCard("swordarm");
		var drums = scn.GetShadowCard("drums");
		var savagery = scn.GetShadowCard("savagery");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(connected);
		scn.MoveCardsToHand(swordarm, drums, savagery, runner);

		scn.StartGame();

		// Frodo and Sam: Shadow events of two cultures cost one more; a Shadow card that is no event and a Free
		// Peoples event do not.
		assertInZone(Zone.SUPPORT, connected);
		assertInHand(swordarm, drums, savagery, runner);
		assertEquals(1, scn.GetTwilightCost(drums));
		assertEquals(1, scn.GetTwilightCost(savagery));
		assertEquals(1, scn.GetTwilightCost(runner));
		assertEquals(0, scn.GetTwilightCost(swordarm));

		scn.MoveMinionsToTable(runner2);
		scn.SkipToAssignments();
		scn.SetTwilight(5);
		scn.FreepsAssignToMinions(sam, runner2);
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();

		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(drums));
		scn.ShadowPlayCard(drums);

		// 5 - (0 printed + 1)
		assertEquals(4, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(runner2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOneRingBoundHobbitShadowEventsCostWhatIsPrintedUntilSamIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var connected = scn.GetFreepsCard("connected");
		var sam = scn.GetFreepsCard("sam");
		var drums = scn.GetShadowCard("drums");
		// Frodo is the one Ring-bound Hobbit: Sméagol is Ring-bound and no Hobbit, Merry a Hobbit and unbound.
		scn.MoveCompanionsToTable("smeagol", "merry");
		scn.MoveCardsToSupportArea(connected);
		scn.MoveCardsToHand(sam, drums);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, connected);
		assertInHand(sam, drums);
		assertEquals(0, scn.GetTwilightCost(drums));

		scn.FreepsPlayCard(sam);

		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetTwilightCost(drums));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishDiscardMakesEachRingBoundCompanionStrongerUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var connected = scn.GetFreepsCard("connected");
		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam, smeagol, merry);
		scn.MoveCardsToSupportArea(connected);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, connected);
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The phase next to a Skirmish ability.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(connected));
		scn.PassCurrentPhaseActions();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{merry, runner});
		scn.FreepsResolveSkirmish(sam);

		// Frodo 3 + 1 for The Ruling Ring
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(5, scn.GetStrength(savage));

		assertTrue(scn.FreepsActionAvailable(connected));
		scn.FreepsUseCardAction(connected);

		// Each Ring-bound companion, in the skirmish or not, Hobbit or not; the unbound Hobbit and the minion not.
		assertInDiscard(connected);
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(5, scn.GetStrength(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Sam 4 loses to the Savage 5; the next skirmish still has the bonus.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(merry);
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(smeagol));
		assertEquals(3, scn.GetStrength(merry));

		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(smeagol));
	}
}
