package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_02_107_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sunlight", "52_107");
					put("bilbo", "1_284");

					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NotFearedinSunlightStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: 2
		* Title: Not Feared in Sunlight
		* Unique: False
		* Side: FREE_PEOPLE
		* Culture: Shire
		* Twilight Cost: 1
		* Type: condition
		* Subtype: Support Area
		* Game Text: To play, spot 2 Hobbits.
		* 	Fellowship: Discard this condition to make each Nazgûl strength -4 until the regroup phase.
		*/

		//Pre-game setup
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");

		assertFalse(sunlight.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sunlight.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, sunlight.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, sunlight.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(sunlight, Keyword.SUPPORT_AREA));
		assertEquals(1, sunlight.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotFearedinSunlightRequires2HobbitsToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var sunlight = scn.GetFreepsCard("sunlight");
		var bilbo = scn.GetFreepsCard("bilbo");
		scn.MoveCardsToHand(sunlight, bilbo);

		scn.StartGame();

		assertFalse(scn.FreepsPlayAvailable(sunlight));
		scn.FreepsPlayCard(bilbo);
		assertTrue(scn.FreepsPlayAvailable(sunlight));
		assertEquals(Zone.HAND, sunlight.getZone());
		scn.FreepsPlayCard(sunlight);
		assertEquals(Zone.SUPPORT, sunlight.getZone());
	}

	@Test
	public void SunlightFellowshipActionMakesNazgulMinus4StrengthUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sunlight = scn.GetFreepsCard("sunlight");
		scn.MoveCardsToSupportArea(sunlight);

		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveMinionsToTable(lemenya);

		scn.StartGame();

		assertTrue(scn.FreepsActionAvailable(sunlight));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(Zone.SUPPORT, sunlight.getZone());
		scn.FreepsUseCardAction(sunlight);
		assertEquals(Zone.DISCARD, sunlight.getZone());

		assertEquals(5, scn.GetStrength(lemenya));

		scn.SkipToAssignments();

		assertEquals(5, scn.GetStrength(lemenya));
		scn.FreepsAssignToMinions(frodo, lemenya);
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(5, scn.GetStrength(lemenya));
		scn.PassCurrentPhaseActions();
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(9, scn.GetStrength(lemenya)); //back to normal
	}
}
