package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_309_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("light", "4_309");
					// Smeagol, Once Like Us: a [shire] Ring-bound companion who is no Hobbit; to play, add a burden
					// (cheated in); an assignment ability only
					put("smeagol", "103_116");
					put("boromir", "0_65");     // Boromir, Steward's Heir: a [gondor] Ring-bound Man; keywords only
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit; a skirmish ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LightShiningFaintlyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Light Shining Faintly
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Fellowship
		 * Game Text: <b>Fellowship:</b> Add a burden to heal a Ring-bound companion twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("light");

		assertEquals("Light Shining Faintly", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.FELLOWSHIP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AddsABurdenToHealOneRingBoundCompanionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(smeagol, boromir, merry);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(smeagol, 2);
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(merry, 2);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);
		assertEquals(1, scn.GetBurdens());

		// The Ring-bearer, a [shire] Ring-bound companion who is no Hobbit and a [gondor] Ring-bound Man;
		// not an unbound Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);

		// Both heals go to Frodo.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(smeagol));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetBurdens());
		assertInDiscard(light);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRingBoundCompanionWoundedItIsPlayedAndTheBurdenPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var frodo = scn.GetRingBearer();
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 2);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetBurdens());

		assertTrue(scn.FreepsPlayAvailable(light));
		scn.FreepsPlayCard(light);

		assertEquals(1, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(merry));
		assertInDiscard(light);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var light = scn.GetFreepsCard("light");
		var frodo = scn.GetRingBearer();
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(light);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		assertEquals(2, scn.GetWoundsOn(frodo));

		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(light));
		assertInHand(light);
	}
}
