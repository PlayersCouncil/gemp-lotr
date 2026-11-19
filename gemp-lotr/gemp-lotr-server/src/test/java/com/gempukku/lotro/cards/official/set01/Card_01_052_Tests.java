package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lightfoot", "1_52");

					put("lorienelf", "1_53");   // Lorien Elf: [elven] Elf companion; its only text is a requirement to play
					put("guardian", "4_76");    // Lorien Guardian: [elven] Elf companion of another title; a regroup ability only
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: [elven] Elf ally; a fellowship ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability only
					put("aragornsbow", "1_90"); // Aragorn's Bow: makes Aragorn an archer; its archery ability is used only on purpose

					put("marksman1", "1_176");  // Goblin Marksman: a [moria] archer minion and nothing else
					put("marksman2", "1_176");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LightfootednessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Lightfootedness
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Spot an Elf companion to make the minion archery total -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("lightfoot");

		assertEquals("Lightfootedness", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingALorienElfMakesTheMinionArcheryTotalMinusOneForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lightfoot = scn.GetFreepsCard("lightfoot");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(lorienelf);
		scn.MoveCardsToHand(lightfoot);
		var frodo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// Two archer minions.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(lightfoot));

		scn.FreepsPlayCard(lightfoot);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertInDiscard(lightfoot);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// In force as the archery fire is placed: one wound on the companions, not two.  Gone with the phase: the
		// assignment phase reads the two archers' 2 again.
		assertEquals(0, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.PassArcheryActions();
		scn.FreepsChooseCard(lorienelf);
		assertEquals(1, scn.GetWoundsOn(lorienelf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetShadowArcheryTotal());
	}

	@Test
	public void ALorienGuardianIsAnElfCompanionToSpotAndTheFellowshipArcheryTotalIsUnchanged() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lightfoot = scn.GetFreepsCard("lightfoot");
		var guardian = scn.GetFreepsCard("guardian");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragornsbow = scn.GetFreepsCard("aragornsbow");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		scn.MoveCompanionsToTable(guardian, aragorn);
		scn.AttachCardsTo(aragorn, aragornsbow);
		scn.MoveCardsToHand(lightfoot);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		// Aragorn, an archer by his bow, adds 1 to the fellowship archery total.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(lightfoot));

		scn.FreepsPlayCard(lightfoot);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertInDiscard(lightfoot);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAnElfWhoIsNoCompanionOrACompanionWhoIsNoElf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var lightfoot = scn.GetFreepsCard("lightfoot");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var marksman1 = scn.GetShadowCard("marksman1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(celeborn);
		scn.MoveCardsToHand(lightfoot);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman1);
		scn.SkipToPhase(Phase.ARCHERY);

		// Celeborn is an Elf and an ally; Aragorn and Frodo are companions and no Elves.
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertFalse(scn.FreepsPlayAvailable(lightfoot));
		assertInHand(lightfoot);
	}
}
