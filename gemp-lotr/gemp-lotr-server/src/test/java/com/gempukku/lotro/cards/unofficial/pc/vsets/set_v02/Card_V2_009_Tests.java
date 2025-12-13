package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_009_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leithio", "102_9");
					put("haldir", "102_8");
					put("troop", "56_22");
					put("bow1", "1_41");
					put("bow2", "1_41");

					put("smith", "3_60");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LeithioiPhillinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Leithio i Phillin!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Spot 2 valiant Elves to wound a minion (or wound a minion twice if each of those elves bears a ranged weapon).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leithio");

		assertEquals("Leithio i Phillin!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void LeithioiPhillinCannotPlayWithout2ValiantElves() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var bow1 = scn.GetFreepsCard("bow1");
		var bow2 = scn.GetFreepsCard("bow2");
		scn.MoveCompanionsToTable(haldir);
		scn.MoveCardsToHand(leithio);

		var smith = scn.GetShadowCard("smith");
		scn.MoveMinionsToTable(smith);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		assertFalse(scn.FreepsPlayAvailable(leithio));
	}

	@Test
	public void LeithioiPhillinSpots2ValiantElvesToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var bow1 = scn.GetFreepsCard("bow1");
		var bow2 = scn.GetFreepsCard("bow2");
		scn.MoveCompanionsToTable(haldir, troop);
		scn.MoveCardsToHand(leithio);

		var smith = scn.GetShadowCard("smith");
		scn.MoveMinionsToTable(smith);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsPlayAvailable(leithio));
		assertEquals(0, scn.GetWoundsOn(smith));

		scn.FreepsPlayCard(leithio);
		assertEquals(1, scn.GetWoundsOn(smith));
	}

	@Test
	public void LeithioiPhillinSpots2ValiantElvesToWoundAMinionTwice() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var leithio = scn.GetFreepsCard("leithio");
		var haldir = scn.GetFreepsCard("haldir");
		var troop = scn.GetFreepsCard("troop");
		var bow1 = scn.GetFreepsCard("bow1");
		var bow2 = scn.GetFreepsCard("bow2");
		scn.MoveCompanionsToTable(haldir, troop);
		scn.MoveCardsToHand(leithio);
		scn.AttachCardsTo(haldir, bow1);
		scn.AttachCardsTo(troop, bow2);

		var smith = scn.GetShadowCard("smith");
		scn.MoveMinionsToTable(smith);

		scn.StartGame();
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.FreepsPlayAvailable(leithio));
		assertEquals(0, scn.GetWoundsOn(smith));

		scn.FreepsPlayCard(leithio);
		assertEquals(2, scn.GetWoundsOn(smith));
	}
}
