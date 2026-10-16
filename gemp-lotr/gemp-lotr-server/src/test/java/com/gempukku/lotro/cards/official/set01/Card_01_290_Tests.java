package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_01_290_Tests
{

	// Frodo, Son of Drogo is the table's Ring-bearer (FOTRFrodo).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Pippin, Friend to Frodo: [shire] Hobbit companion, Frodo signet, vitality 4; a bar on discarding
					// [shire] tales only
					put("pippin", "1_306");
					// Legolas, Greenleaf: [elven] Elf companion, Frodo signet, vitality 3; an archery ability never used here
					put("legolas", "1_50");
					// Sam, Son of Hamfast: [shire] Hobbit companion, Ring-bound like Frodo, with the Aragorn signet; his
					// fellowship ability is never used here
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrodoStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Frodo, Son of Drogo
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 10
		 * Signet: Frodo
		 * Game Text: <b>Ring-bearer (resistance 10).</b><br><b>Fellowship:</b> Exert another companion who has the Frodo signet to heal Frodo.
		*/

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("Frodo", card.getBlueprint().getTitle());
		assertEquals("Son of Drogo", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(10, card.getBlueprint().getResistance());
		assertEquals(Signet.FRODO, card.getBlueprint().getSignet());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void FellowshipActionExertsAnotherCompanionWithTheFrodoSignetToHealFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var legolas = scn.GetFreepsCard("legolas");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(pippin, legolas, sam);

		scn.StartGame();
		// Two wounds, so that one heal is told from a full one.
		scn.AddWoundsToChar(frodo, 2);

		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsActionAvailable(frodo));

		scn.FreepsUseCardAction(frodo);
		// Companions with the Frodo signet of two cultures and races; not Sam (the Aragorn signet), and not Frodo
		// himself ("another").
		assertTrue(scn.FreepsHasCardChoiceAvailable(pippin, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(legolas);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithTheOnlyOtherCompanionWithTheFrodoSignetExhaustedTheActionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(pippin, sam);

		scn.StartGame();
		// Pippin exhausted (vitality 4, 3 wounds); Frodo, wounded, and Sam could still exert.
		scn.AddWoundsToChar(pippin, 3);
		scn.AddWoundsToChar(frodo, 1);
		scn.FreepsRefreshDecision();

		assertEquals(3, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(frodo));
	}

	@Test
	public void WithFrodoUnwoundedTheActionIsStillOfferedAndTheExertionHealsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(pippin));
		// An action with a cost is offered whenever the cost can be paid (Q-0034).
		assertTrue(scn.FreepsActionAvailable(frodo));

		// Pippin is the only one who can pay, and exerts without being asked.
		scn.FreepsUseCardAction(frodo);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
