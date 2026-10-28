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

public class Card_07_318_Tests
{

	// The King path, which the text names.  Site 5, Pelennor Flat 7_345, asks the Free Peoples player at the start of
	// the maneuver phase to discard his hand or add 2 burdens; site 9, Dagorlad 7_360, adds 3 threats on the move to it.
	// Sites 6 to 8 (Minas Tirith Seventh Circle 7_350, Osgiliath Channel 8_120, Watchers of Cirith Ungol 10_120) have
	// nothing that reaches these tests.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit; his abilities are used only on purpose
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, vitality 3, no text
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion, vitality 1 (exhausted), no text
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc minion, vitality 1; its text answers only its play
				}},
				VirtualTableScenario.KingSites,
				// Frodo, Wicked Masster! is the card under test, laid as the table's Ring-bearer.
				"7_318",
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path: its site 7, Anduin Confluence 1_353, is a site 7 of another block than the King's.
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion, no text
				}},
				VirtualTableScenario.FellowshipSites,
				"7_318",
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Frodo, Wicked Masster!
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Gandalf
		 * Game Text: <b>Ring-bearer (resistance 10)</b>. <b>Ring-bound</b>.<br><b>Skirmish:</b> At sites 6[K] to 8[K], add 4 burdens to wound a minion skirmishing Frodo.
		*/

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Wicked Masster!", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.RING_BOUND));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void AtSite6AddsFourBurdensToWoundAMinionSkirmishingFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		var brigand = scn.GetShadowCard("brigand");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(5);
		scn.MoveMinionsToTable(troop, brigand, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, troop}, new PhysicalCardImpl[]{sam, runner});
		// The Brigand, left over, goes to Frodo as well: two minions of different cultures and races skirmish him.
		scn.ShadowAssignToMinions(frodo, brigand);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(brigand));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsActionAvailable(frodo));

		scn.FreepsUseCardAction(frodo);
		// The cost: 4 burdens, on Frodo, the Ring-bearer.
		assertEquals(4, scn.GetBurdens());
		// The minions skirmishing Frodo, the exhausted one among them; not the Runner, assigned to Sam, whose
		// skirmish has not begun.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(brigand);

		// A wound and not an exertion: the Brigand, of vitality 1, is killed.  Nobody else is wounded.
		assertInZone(Zone.DISCARD, brigand);
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AtSite8WithNoMinionSkirmishingFrodoTheBurdensAreAddedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(sam);

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(troop, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, troop}, new PhysicalCardImpl[]{sam, runner});
		// Sam's skirmish first: the Troop is assigned to Frodo and waits; the Runner fights another companion.
		scn.FreepsResolveSkirmish(sam);

		assertEquals(8, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertTrue(scn.FreepsActionAvailable(frodo));

		scn.FreepsUseCardAction(frodo);
		// The 4 burdens are paid, and no minion is skirmishing Frodo: nothing is wounded.  The Runner, of vitality 1,
		// would have been killed.
		assertEquals(4, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertInZone(Zone.SHADOW_CHARACTERS, troop, runner);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedAtSite5() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(4);
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);
		scn.ShadowPass();

		// Pelennor Flat's choice at the start of the maneuver phase: the hand goes, and no burden is added.
		scn.FreepsChooseOption("Discard");
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(frodo));
	}

	@Test
	public void NotOfferedAtSite9() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(8);
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(9, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(frodo));
	}

	@Test
	public void NotOfferedAtSite7OfTheFellowshipBlock() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var frodo = scn.GetRingBearer();
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(6);
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop);
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(frodo));
	}
}
