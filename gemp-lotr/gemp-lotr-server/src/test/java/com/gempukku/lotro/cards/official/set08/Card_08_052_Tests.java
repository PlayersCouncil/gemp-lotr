package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ballista", "8_52");

					put("brute", "10_38");      // Corsair Brute: corsair, strength 10; a start-of-skirmish trigger only
					put("boatswain", "10_37");  // Corsair Boatswain: corsair, strength 6; a Shadow ability used only on purpose
					put("southron", "4_254");   // Southron Soldier: [raider] Man, no corsair; Southron and ambush only
					put("champion", "13_84");   // Corsair Champion: [men] Man with "Corsair" in the title and not the keyword

					// Free Peoples cards for the hand: a hand of four keeps the initiative, and playing one gives it up.
					put("stairs1", "1_24");     // Stairs of Khazad-dum: [dwarven] support-area condition, cost 0, no requirement
					put("stairs2", "1_24");
					put("stairs3", "1_24");
					put("stairs4", "1_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CorsairBallistaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Corsair Ballista
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: +2
		 * Game Text: Bearer must be a corsair.<br>While you have initiative, bearer is an <b>archer</b> and <b>fierce</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ballista");

		assertEquals("Corsair Ballista", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnACorsairOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ballista = scn.GetShadowCard("ballista");
		var brute = scn.GetShadowCard("brute");
		var boatswain = scn.GetShadowCard("boatswain");
		var southron = scn.GetShadowCard("southron");
		var champion = scn.GetShadowCard("champion");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(ballista);

		scn.StartGame();
		scn.MoveMinionsToTable(brute, boatswain, southron, champion);
		assertInHand(ballista);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(ballista));
		scn.ShadowPlayCard(ballista);

		// A corsair: not the [raider] Man who is none, nor the Man with the word in his title only, nor a companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(brute, boatswain));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(southron, champion, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(boatswain);

		assertAttachedTo(ballista, boatswain);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileTheShadowHasInitiativeBearerIsAnArcherAndFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ballista = scn.GetShadowCard("ballista");
		var brute = scn.GetShadowCard("brute");
		var boatswain = scn.GetShadowCard("boatswain");
		var stairs1 = scn.GetFreepsCard("stairs1");
		var stairs2 = scn.GetFreepsCard("stairs2");
		var stairs3 = scn.GetFreepsCard("stairs3");
		var stairs4 = scn.GetFreepsCard("stairs4");

		scn.StartGame();
		// Laid in the fellowship phase, to read the standing text before and after the Free Peoples player's play.
		scn.MoveMinionsToTable(brute, boatswain);
		scn.AttachCardsTo(brute, ballista);
		scn.MoveCardsToHand(stairs1, stairs2, stairs3, stairs4);
		scn.FreepsRefreshDecision();

		// Four cards in hand: the Free Peoples player has initiative.
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		assertAttachedTo(ballista, brute);
		assertFalse(scn.HasKeyword(brute, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(brute, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(boatswain, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boatswain, Keyword.FIERCE));

		scn.FreepsPlayCard(stairs1);

		// Three: the Shadow has it.
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		assertTrue(scn.HasKeyword(brute, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(brute, Keyword.FIERCE));
		// Another corsair, who does not bear it.
		assertFalse(scn.HasKeyword(boatswain, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(boatswain, Keyword.FIERCE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
