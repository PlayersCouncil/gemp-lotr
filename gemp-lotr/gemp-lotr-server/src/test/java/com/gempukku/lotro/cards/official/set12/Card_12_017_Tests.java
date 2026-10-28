package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elrond", "12_17");

					put("legolas", "11_21");    // Legolas, Companion of the Ring: an Elf companion with no requirement to play; +2 only at a battleground or forest
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: an Elf ally, home 3F; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability only

					// Cards for the discard pile.
					put("defiance", "1_37");    // Defiance: an [elven] event
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion
					put("sword", "1_112");      // Ranger's Sword: a [gondor] possession

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; text only when played
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElrondStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Elrond, Witness to History
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 4
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 8
		 * Vitality: 4
		 * Resistance: 7
		 * Game Text: To play, spot an Elf.<br><b>Skirmish:</b> If Elrond is skirmishing a minion, exert him to place an [elven] card from your discard pile on top of your draw deck.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elrond");

		assertEquals("Elrond", card.getBlueprint().getTitle());
		assertEquals("Witness to History", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(7, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnElfCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(elrond, legolas);

		scn.StartGame();

		assertInHand(elrond);
		// Aragorn and Frodo: no Elf to spot.
		assertFalse(scn.FreepsPlayAvailable(elrond));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(legolas);
		assertInZone(Zone.FREE_CHARACTERS, legolas);
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.FREE_CHARACTERS, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfAllyAloneIsSpottedToPlayHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var golradir = scn.GetFreepsCard("golradir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(golradir);
		scn.MoveCardsToHand(elrond);

		scn.StartGame();

		assertInHand(elrond);
		assertTrue(scn.FreepsPlayAvailable(elrond));

		scn.FreepsPlayCard(elrond);
		assertInZone(Zone.FREE_CHARACTERS, elrond);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionIsOfferedOnlyInHisOwnSkirmishAndPlacesAnElvenCardOnTopOfTheDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defiance = scn.GetFreepsCard("defiance");
		var cloak = scn.GetFreepsCard("cloak");
		var arwen = scn.GetFreepsCard("arwen");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(elrond, aragorn);
		scn.MoveCardsToDiscard(defiance, cloak, arwen, sword);

		scn.StartGame();
		assertInDiscard(defiance, cloak, arwen, sword);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner1}, new PhysicalCardImpl[]{elrond, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		// Elrond is assigned to a minion, but his skirmish has not begun.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner1));
		assertTrue(scn.IsCharAssignedAgainst(elrond, runner2));
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(elrond);

		assertTrue(scn.IsCharSkirmishingAgainst(elrond, runner2));
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertNotEquals(cloak, scn.GetFreepsTopOfDeck());
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		// An [elven] event, possession and companion, and not a [gondor] card.
		assertTrue(scn.FreepsHasCardChoiceAvailable(defiance, cloak, arwen));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCards(cloak);

		assertEquals(cloak, scn.GetFreepsTopOfDeck());
		assertInDiscard(defiance, arwen, sword);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileElrondIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var cloak = scn.GetFreepsCard("cloak");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(elrond);
		scn.MoveCardsToDiscard(cloak);

		scn.StartGame();
		scn.AddWoundsToChar(elrond, 3);
		assertEquals(3, scn.GetWoundsOn(elrond));
		assertInDiscard(cloak);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, runner1);
		scn.FreepsResolveSkirmish(elrond);

		// Vitality 4 with 3 wounds: he cannot exert, though an [elven] card waits in the pile.
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, runner1));
		assertFalse(scn.FreepsActionAvailable(elrond));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoElvenCardInTheDiscardPileElrondExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(elrond);
		scn.MoveCardsToDiscard(sword);

		scn.StartGame();
		assertInDiscard(sword);
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, runner1);
		scn.FreepsResolveSkirmish(elrond);

		var top = scn.GetFreepsTopOfDeck();
		assertEquals(0, scn.GetWoundsOn(elrond));
		// The ability has a cost: it is offered with only a [gondor] card in the pile.
		assertTrue(scn.FreepsActionAvailable(elrond));

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(top, scn.GetFreepsTopOfDeck());
		assertInDiscard(sword);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
