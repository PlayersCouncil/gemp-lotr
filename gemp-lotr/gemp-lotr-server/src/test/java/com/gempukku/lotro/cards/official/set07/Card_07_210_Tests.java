package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_210_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attea", "7_210");
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc minion; nothing below 2 wounds on the Ring-bearer
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc minion with no text
					put("breath", "1_207");     // Black Breath: a [wraith] condition in the support area, no minion; its text reaches only a bearer
					// Goblin Patrol Troop again: cards for the Free Peoples hand, whose size decides the initiative.
					put("hand1", "1_177");
					put("hand2", "1_177");
					put("hand3", "1_177");
					put("hand4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireAtteaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Úlairë Attëa, Wraith on Wings
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 12
		 * Vitality: 3
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br><b>Shadow:</b> If you have initiative, exert Úlairë Attëa and spot another [wraith] minion to add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attea");

		assertEquals("Úlairë Attëa", card.getBlueprint().getTitle());
		assertEquals("Wraith on Wings", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(12, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void ShadowAbilityExertsHerToAddABurdenWithAWraithOrcSpottedAndThreeCardsInTheFreePeoplesHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(scn.GetFreepsCard("hand1"), scn.GetFreepsCard("hand2"), scn.GetFreepsCard("hand3"));

		scn.StartGame();
		scn.MoveMinionsToTable(attea, hound);
		scn.FreepsPass();

		// Three cards in the Free Peoples hand: the Shadow has the initiative.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(attea));

		scn.ShadowUseCardAction(attea);

		// She is the one exerted; the Hound is only spotted.
		assertEquals(1, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AWraithNazgulBesideHerIsAnotherWraithMinionToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var nelya = scn.GetShadowCard("nelya");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, nelya);
		scn.FreepsPass();

		// An empty Free Peoples hand: the Shadow has the initiative.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(attea));

		scn.ShadowUseCardAction(attea);

		assertEquals(1, scn.GetWoundsOn(attea));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileTheFreePeoplesPlayerHasFourCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var hound = scn.GetShadowCard("hound");
		var hand1 = scn.GetFreepsCard("hand1");
		var hand2 = scn.GetFreepsCard("hand2");
		var hand3 = scn.GetFreepsCard("hand3");
		var hand4 = scn.GetFreepsCard("hand4");
		scn.MoveCardsToHand(hand1, hand2, hand3, hand4);

		scn.StartGame();
		scn.MoveMinionsToTable(attea, hound);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertFalse(scn.ShadowHasInitiative());
		assertFalse(scn.ShadowActionAvailable(attea));

		// The hand cut to three, and the decision sent again: now the Shadow has the initiative.
		scn.SetFreepsHand(hand1, hand2, hand3);
		scn.ShadowRefreshDecision();
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.ShadowActionAvailable(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithoutAnotherWraithMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var troop = scn.GetShadowCard("troop");
		var breath = scn.GetShadowCard("breath");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveCardsToSupportArea(breath);
		assertInZone(Zone.SUPPORT, breath);
		scn.MoveMinionsToTable(attea, troop);
		scn.FreepsPass();

		// Herself (no other), a [moria] minion and a [wraith] card that is no minion: nothing to spot.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		assertFalse(scn.ShadowActionAvailable(attea));

		scn.MoveMinionsToTable(hound);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(attea));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWhileSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attea = scn.GetShadowCard("attea");
		var hound = scn.GetShadowCard("hound");

		scn.StartGame();
		scn.MoveMinionsToTable(attea, hound);
		// Vitality 3 with 2 wounds: she cannot exert.
		scn.AddWoundsToChar(attea, 2);
		assertEquals(2, scn.GetWoundsOn(attea));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowHasInitiative());
		assertEquals(0, scn.GetWoundsOn(hound));
		assertFalse(scn.ShadowActionAvailable(attea));
		assertEquals(0, scn.GetBurdens());
	}
}
