package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_V2_054_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("guard", "102_54");
					put("theoden", "4_292");
					put("rohan1", "4_265");
					put("rohan2", "4_286");
					put("rohan3", "5_83");
					put("spear", "4_288");

					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RoyalGuardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Royal Guard
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Ally
		 * Subtype: Man
		 * Strength: 3
		 * Vitality: 3
		 * Site Number: 3T
		 * Game Text: To play, spot Theoden (or 3 [rohan] Men).
		* 	Skirmish: Exert this ally to add his strength to a skirmishing Hobbit and make that Hobbit gain <b>valiant</b> until the regroup phase (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("guard");

		assertEquals("Royal Guard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.TWO_TOWERS, 3)));
	}

	@Test
	public void RoyalGuardSpotsTheodenToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var theoden = scn.GetFreepsCard("theoden");
		scn.MoveCardsToHand(guard, theoden);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard));
		scn.FreepsPlayCard(theoden);
		assertTrue(scn.FreepsPlayAvailable(guard));
	}

	@Test
	public void RoyalGuardSpots3RohanMenToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var rohan1 = scn.GetFreepsCard("rohan1");
		var rohan2 = scn.GetFreepsCard("rohan2");
		var rohan3 = scn.GetFreepsCard("rohan3");

		scn.MoveCardsToHand(guard, rohan1, rohan2, rohan3);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(guard));
		scn.FreepsPlayCard(rohan1);
		assertFalse(scn.FreepsPlayAvailable(guard));
		scn.FreepsPlayCard(rohan2);
		assertFalse(scn.FreepsPlayAvailable(guard));
		scn.FreepsPlayCard(rohan3);
		assertTrue(scn.FreepsPlayAvailable(guard));
	}

	@Test
	public void RoyalGuardExertsToAddStrengthAndValiantToSkirmishingHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var guard = scn.GetFreepsCard("guard");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCardsToSupportArea(guard);
		scn.AttachCardsTo(guard, spear);

		var runner = scn.GetShadowCard("runner");
		scn.MoveMinionsToTable(runner);

		scn.StartGame();

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(frodo, runner);

		assertEquals(4, scn.GetStrength(frodo));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(5, scn.GetStrength(guard)); // base 3 + 2 from borne spear
		assertTrue(scn.FreepsActionAvailable(guard));

		scn.FreepsUseCardAction(guard);
		assertEquals(9, scn.GetStrength(frodo));
		assertTrue(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.ShadowPassCurrentPhaseAction();
		assertFalse(scn.FreepsActionAvailable(guard)); // limit once per phase
	}
}
