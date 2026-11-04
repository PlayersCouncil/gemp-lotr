package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_017_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("elf", "18_17");
					put("legolas", "11_21");    // Legolas, Companion of the Ring: an [elven] companion; his text needs a battleground or forest, and no site here is one
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] companion of another title; her text needs a Nazgûl
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] companion; a maneuver ability used only on purpose
					// Pallando, Far-travelling One: a [gandalf] follower, Aid (2); a transfer of him to a companion
					// who is not [gandalf] exerts that companion twice
					put("pallando", "13_37");
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
					// Cards that only fill the draw deck
					put("troop1", "1_177");
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WoodhallElfStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Woodhall Elf, Exile
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Follower
		 * Resistance: +1
		 * Game Text: <b>Aid</b> – (2). <helper>(At the start of the maneuver phase, you may add (2) to transfer this to a companion.)</helper><br>When you transfer this to an [elven] companion, you may draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("elf");

		assertEquals("Woodhall Elf", card.getBlueprint().getTitle());
		assertEquals("Exile", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
		assertEquals(2, scn.GetAidTwilightCost(card));
	}

	@Test
	public void TransferredByItsAidToAnElvenCompanionItMayDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, arwen, aragorn);
		scn.MoveCardsToSupportArea(elf);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetFreepsHandCount());
		assertTrue(deck > 3);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));

		scn.FreepsAcceptOptionalTrigger();
		// The Aid: (2) added.
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, aragorn, frodo));
		scn.FreepsChooseCard(legolas);

		assertAttachedTo(elf, legolas);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredToAnotherElvenCompanionTheDrawMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.MoveCardsToSupportArea(elf);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(arwen);
		assertAttachedTo(elf, arwen);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TransferredToACompanionWhoIsNotElvenItOffersNoDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, aragorn);
		scn.MoveCardsToSupportArea(elf);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(elf, aragorn);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnotherFollowerTransferredToAnElvenCompanionOffersNoDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elf = scn.GetFreepsCard("elf");
		var pallando = scn.GetFreepsCard("pallando");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.MoveCardsToSupportArea(elf, pallando);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("runner"));
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(pallando));
		scn.FreepsChooseOptionalTrigger(pallando);
		scn.FreepsChooseCard(legolas);
		assertAttachedTo(pallando, legolas);
		assertInZone(Zone.SUPPORT, elf);

		// What is still offered is this card's own Aid, and no draw.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(elf, "Aid"));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(elf, "Optional trigger"));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, elf);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
