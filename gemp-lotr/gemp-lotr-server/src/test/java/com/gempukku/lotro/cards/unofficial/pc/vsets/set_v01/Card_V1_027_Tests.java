
package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Card_V1_027_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("frostbite", "101_27");
					put("frostbite2", "101_27");
					put("weather_condition", "1_134");
					put("weather_event", "1_124");
					put("saruman", "4_173");

					put("seven", "4_318");

				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrostbiteStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		* Set: V1
		* Title: Frostbite
		* Side: Free Peoples
		* Culture: isengard
		* Twilight Cost: 1
		* Type: condition
		* Subtype: 
		* Game Text: To play, spot a weather condition.  Plays on a companion. Limit 1 per bearer.
		* 	Each time a weather card is played, add (2).
		* 	When this condition is discarded by a Free Peoples card, wound bearer.
		*/

		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frostbite = scn.GetFreepsCard("frostbite");

		assertFalse(frostbite.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, frostbite.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, frostbite.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, frostbite.getBlueprint().getCardType());
		//assertEquals(Race.CREATURE, frostbite.getBlueprint().getRace());
		//assertTrue(scn.HasKeyword(frostbite, Keyword.SUPPORT_AREA)); // test for keywords as needed
		assertEquals(1, frostbite.getBlueprint().getTwilightCost());
		//assertEquals(, frostbite.getBlueprint().getStrength());
		//assertEquals(, frostbite.getBlueprint().getVitality());
		//assertEquals(, frostbite.getBlueprint().getResistance());
		//assertEquals(Signet., frostbite.getBlueprint().getSignet());
		//assertEquals(, frostbite.getBlueprint().getSiteNumber()); // Change this to getAllyHomeSiteNumbers for allies

	}

	@Test
	public void RequiresAWeatherConditionToPlayOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frostbite = scn.GetShadowCard("frostbite");
		PhysicalCardImpl frostbite2 = scn.GetShadowCard("frostbite");
		PhysicalCardImpl weather_condition = scn.GetShadowCard("weather_condition");
		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(frostbite, frostbite2, weather_condition);
		scn.MoveMinionsToTable(saruman);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertFalse(scn.ShadowPlayAvailable(frostbite));
		scn.ShadowPlayCard(weather_condition);
		scn.ShadowChooseCard(scn.GetCurrentSite());

		assertTrue(scn.ShadowPlayAvailable(frostbite));
		scn.ShadowPlayCard(frostbite);

		assertEquals(Zone.ATTACHED, frostbite.getZone());
		assertEquals(scn.GetRingBearer(), frostbite.getAttachedTo());

		//limit 1 per bearer
		assertFalse(scn.ShadowPlayAvailable(frostbite2));
	}

	@Test
	public void EachTimeWeatherIsPlayedAdds2Twilight() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();

		PhysicalCardImpl frostbite = scn.GetShadowCard("frostbite");
		PhysicalCardImpl frostbite2 = scn.GetShadowCard("frostbite2");
		PhysicalCardImpl weather_condition = scn.GetShadowCard("weather_condition");
		PhysicalCardImpl weather_event = scn.GetShadowCard("weather_event");
		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");

		scn.AttachCardsTo(frodo, frostbite, frostbite2);
		scn.MoveCardsToHand(weather_condition, weather_event);
		scn.MoveMinionsToTable(saruman);

		scn.StartGame();
		scn.FreepsPassCurrentPhaseAction();

		assertEquals(3, scn.GetTwilight()); // +1 comp move, +2 site
		scn.ShadowPlayCard(weather_condition);
		scn.ShadowChooseCard(scn.GetCurrentSite());
		assertEquals(6, scn.GetTwilight()); // -1 for card cost, +2/+2 from two copies of Frostbite

		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPassCurrentPhaseAction();
		scn.ShadowPlayCard(weather_event);
		assertEquals(8, scn.GetTwilight()); // -2 for card cost, +2/+2 from two copies of Frostbite

	}

	@Test
	public void FreepsDiscardingFrostbiteWoundsBearer() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		VirtualTableScenario scn = GetScenario();

		PhysicalCardImpl frodo = scn.GetRingBearer();
		PhysicalCardImpl seven = scn.GetFreepsCard("seven");
		scn.MoveCardsToHand(seven);

		PhysicalCardImpl frostbite = scn.GetShadowCard("frostbite");
		PhysicalCardImpl frostbite2 = scn.GetShadowCard("frostbite2");
		PhysicalCardImpl saruman = scn.GetShadowCard("saruman");

		scn.AttachCardsTo(frodo, frostbite, frostbite2);
		scn.MoveCardsToHand(saruman);

		scn.StartGame();

		scn.SetTwilight(10);

		scn.FreepsPlayCard(seven);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(Zone.ATTACHED, frostbite.getZone());
		scn.FreepsChooseCard(frostbite);
		assertEquals(Zone.DISCARD, frostbite.getZone());
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.FreepsPassCurrentPhaseAction();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(Zone.ATTACHED, frostbite2.getZone());
		scn.ShadowPlayCard(saruman);
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo)); //No wounds added since it was a shadow card discarding it
		assertEquals(Zone.DISCARD, frostbite2.getZone());
	}
}
